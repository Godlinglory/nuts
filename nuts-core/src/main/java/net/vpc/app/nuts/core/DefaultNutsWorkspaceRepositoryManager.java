/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.util.*;

/**
 *
 * @author vpc
 */
class DefaultNutsWorkspaceRepositoryManager implements NutsWorkspaceRepositoryManager {

    private Map<String, NutsRepository> repositories = new LinkedHashMap<>();
    private final DefaultNutsWorkspace ws;
    private List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();

    DefaultNutsWorkspaceRepositoryManager(final DefaultNutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public void removeRepository(String repositoryId) {
        ws.getSecurityManager().checkAllowed(NutsConstants.RIGHT_REMOVE_REPOSITORY,"remove-repository");
        NutsRepository removed = repositories.remove(repositoryId);
        ws.getConfigManager().removeRepository(repositoryId);
        if (removed != null) {
            for (NutsWorkspaceListener nutsWorkspaceListener : ws.getWorkspaceListeners()) {
                nutsWorkspaceListener.onRemoveRepository(ws, removed);
            }
        }
    }

    @Override
    public NutsRepository addProxiedRepository(String repositoryId, String location, String type, boolean autoCreate) {
        NutsRepository proxy = addRepository(repositoryId, repositoryId, NutsConstants.REPOSITORY_TYPE_NUTS, autoCreate);
        //Dont need to add mirror if repository is already loadable from config!
        NutsRepository m = null;
        try {
            m = proxy.getMirror(repositoryId + "-ref");
        }catch (NutsRepositoryNotFoundException ex){
            //
        }
        if(m==null) {
            return proxy.addMirror(repositoryId + "-ref", location, type, autoCreate);
        }
        return proxy;
    }

    @Override
    public NutsRepository addRepository(String repositoryId, String location, String type, boolean autoCreate) {
        ws.getSecurityManager().checkAllowed(NutsConstants.RIGHT_ADD_REPOSITORY,"add-repository");
        if(StringUtils.isEmpty(repositoryId)){
            if(StringUtils.isEmpty(location)){
                throw new IllegalArgumentException("You should consider specifying location and/or repositoryId");
            }
            File file=new File(this.resolveRepositoryPath(location));
            if(file.isDirectory()){
                if(new File(file,NutsConstants.NUTS_REPOSITORY_CONFIG_FILE_NAME).exists()){
                    NutsRepositoryConfig c=ws.getIOManager().readJson(new File(file,NutsConstants.NUTS_REPOSITORY_CONFIG_FILE_NAME),NutsRepositoryConfig.class);
                    if(c!=null){
                        repositoryId=c.getId();
                        if (StringUtils.isEmpty(type)) {
                            type=c.getType();
                        }else if(!type.equals(c.getType())){
                            throw new IllegalArgumentException("Invalid repository type "+type+". expected "+c.getType());
                        }
                    }
                }else{
                    repositoryId=file.getName();
                }
            }
        }else if(StringUtils.isEmpty(location)){
            //no pbm!
        }

        if (StringUtils.isEmpty(type)) {
            type = NutsConstants.REPOSITORY_TYPE_NUTS;
        }
        ws.checkSupportedRepositoryType(type);
        NutsRepositoryLocation old = ws.getConfigManager().getRepository(repositoryId);
        if (old != null) {
            throw new NutsRepositoryAlreadyRegisteredException(repositoryId);
        }
        ws.getConfigManager().addRepository(new NutsRepositoryLocation(repositoryId, location, type));
//        NutsRepository repo = openRepository(repositoryId, new File(getRepositoriesRoot(), repositoryId), location, type, autoCreate);
        return openRepository(repositoryId, location, type, getRepositoriesRoot(), autoCreate);
    }

    String getRepositoriesRoot() {
        return CoreIOUtils.createFile(ws.getConfigManager().getWorkspaceLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES).getPath();
    }

    @Override
    public NutsRepository findRepository(String repositoryIdPath) {
        if (!StringUtils.isEmpty(repositoryIdPath)) {
            while (repositoryIdPath.startsWith("/")) {
                repositoryIdPath = repositoryIdPath.substring(1);
            }
            while (repositoryIdPath.endsWith("/")) {
                repositoryIdPath = repositoryIdPath.substring(0, repositoryIdPath.length() - 1);
            }
            if (repositoryIdPath.contains("/")) {
                int s = repositoryIdPath.indexOf("/");
                NutsRepository r = repositories.get(repositoryIdPath.substring(0, s));
                if (r != null) {
                    return r.getMirror(repositoryIdPath.substring(s + 1));
                }
            } else {
                NutsRepository r = repositories.get(repositoryIdPath);
                if (r != null) {
                    return r;
                }
            }
        }
        throw new NutsRepositoryNotFoundException(repositoryIdPath);
    }

    @Override
    public NutsRepository[] getRepositories() {
        return repositories.values().toArray(new NutsRepository[0]);
    }

    @Override
    public boolean isSupportedRepositoryType(String repositoryType) {
        if (StringUtils.isEmpty(repositoryType)) {
            repositoryType = NutsConstants.REPOSITORY_TYPE_NUTS;
        }
        return ws.getExtensionManager().createAllSupported(NutsRepositoryFactoryComponent.class, new NutsRepoInfo(repositoryType, null)).size() > 0;
    }

    @Override
    public NutsRepositoryDefinition[] getDefaultRepositories() {
        List<NutsRepositoryDefinition> all = new ArrayList<>();
        for (NutsRepositoryFactoryComponent provider : ws.getExtensionManager().createAll(NutsRepositoryFactoryComponent.class)) {
            all.addAll(Arrays.asList(provider.getDefaultRepositories(ws)));
        }
        Collections.sort(all, new Comparator<NutsRepositoryDefinition>() {
            @Override
            public int compare(NutsRepositoryDefinition o1, NutsRepositoryDefinition o2) {
                return Integer.compare(o1.getOrder(),o2.getOrder());
            }
        });
        return all.toArray(new NutsRepositoryDefinition[0]);
    }

    protected void wireRepository(NutsRepository repository) {
        CoreNutsUtils.validateRepositoryId(repository.getRepositoryId());
        if (repositories.containsKey(repository.getRepositoryId())) {
            throw new NutsRepositoryAlreadyRegisteredException(repository.getRepositoryId());
        }
        repositories.put(repository.getRepositoryId(), repository);
        for (NutsWorkspaceListener nutsWorkspaceListener : ws.getWorkspaceListeners()) {
            nutsWorkspaceListener.onAddRepository(ws, repository);
        }
    }

    void removeAllRepositories() {
        repositories.clear();
    }

    @Override
    public NutsRepository openRepository(String repositoryId, String location, String type, String repositoryRoot, boolean autoCreate) {
        if (StringUtils.isEmpty(type)) {
            type = NutsConstants.REPOSITORY_TYPE_NUTS;
        }
        NutsRepositoryFactoryComponent factory_ = ws.getExtensionManager().createSupported(NutsRepositoryFactoryComponent.class, new NutsRepoInfo(type, location));
        if (factory_ != null) {
            NutsRepository r = factory_.create(repositoryId, location, type, ws, null, repositoryRoot);
            if (r != null) {
                r.open(autoCreate);
                wireRepository(r);
                return r;
            }
        }
        throw new NutsInvalidRepositoryException(repositoryId, "Invalid type " + type);
    }

    @Override
    public Set<String> getAvailableArchetypes() {
        Set<String> set = new HashSet<>();
        set.add("default");
        for (NutsWorkspaceArchetypeComponent extension : ws.getExtensionManager().createAllSupported(NutsWorkspaceArchetypeComponent.class, ws)) {
            set.add(extension.getName());
        }
        return set;
    }

    @Override
    public void removeRepositoryListener(NutsRepositoryListener listener) {
        repositoryListeners.add(listener);
    }

    @Override
    public void addRepositoryListener(NutsRepositoryListener listener) {
        if (listener != null) {
            repositoryListeners.add(listener);
        }
    }

    @Override
    public NutsRepositoryListener[] getRepositoryListeners() {
        return repositoryListeners.toArray(new NutsRepositoryListener[0]);
    }

    @Override
    public String resolveRepositoryPath(String repositoryLocation) {
        String root = this.getRepositoriesRoot();
        NutsWorkspaceConfigManager configManager = this.ws.getConfigManager();
        return CoreIOUtils.resolvePath(repositoryLocation,
                root != null ? new File(root) : CoreIOUtils.createFile(
                        configManager.getWorkspaceLocation(), NutsConstants.FOLDER_NAME_REPOSITORIES),
                configManager.getHomeLocation()).getPath();
    }

}
