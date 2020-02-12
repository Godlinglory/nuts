/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.main.repos.DefaultNutsRepoConfigManager;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.common.DefaultObservableMap;
import net.vpc.app.nuts.core.common.ObservableMap;

import java.nio.file.Path;
import java.util.*;

/**
 * Created by vpc on 1/18/17.
 */
public abstract class AbstractNutsRepository implements NutsRepository{

    private static final long serialVersionUID = 1L;

    private final List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();
    protected Map<String, String> extensions = new HashMap<>();
    protected NutsRepository parentRepository;
    protected NutsWorkspace workspace;
    protected NutsRepositorySecurityManager securityManager;
    protected NutsRepositoryConfigManager configManager;
    protected ObservableMap<String, Object> userProperties;

    public AbstractNutsRepository() {
        userProperties = new DefaultObservableMap<>();
    }


    @Override
    public NutsRepository getParentRepository() {
        return parentRepository;
    }

    @Override
    public NutsRepositoryConfigManager config() {
        return configManager;
    }

    @Override
    public NutsRepositorySecurityManager security() {
        return securityManager;
    }



    @Override
    public String getRepositoryType() {
        return config().getType();
    }

    @Override
    public String toString() {
        NutsRepositoryConfigManager c = config();
        String name = config().getName();
        String storePath = null;
        String loc = config().getLocation(false);
        String impl = getClass().getSimpleName();
        if (c != null) {
            Path storeLocation = c.getStoreLocation();
            storePath = storeLocation==null?null:storeLocation.toAbsolutePath().toString();
        }
        LinkedHashMap<String, String> a = new LinkedHashMap<>();
        if (name != null) {
            a.put("name", name);
        }
        if (impl != null) {
            a.put("impl", impl);
        }
        if (storePath != null) {
            a.put("store", storePath);
        }
        if (loc != null) {
            a.put("location", loc);
        }
        return a.toString();
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

    protected String getIdExtension(NutsId id) {
        return getWorkspace().config().getDefaultIdExtension(id);
    }

    public String getIdBasedir(NutsId id) {
        return getWorkspace().config().getDefaultIdBasedir(id);
    }

    public String getIdFilename(NutsId id) {
        //return getWorkspace().config().getDefaultIdFilename(id);
        String classifier = "";
        String ext = getIdExtension(id);
        if (!ext.equals(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION) && !ext.equals(".pom")) {
            String c = id.getClassifier();
            if (!CoreStringUtils.isBlank(c)) {
                classifier = "-" + c;
            }
        }
        return id.getArtifactId() + "-" + id.getVersion().getValue() + classifier + ext;
    }

    protected void checkSession(NutsSession session) {
        if (session == null) {
            throw new NutsIllegalArgumentException(workspace, "Missing Session");
        }
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return workspace;
    }


    @Override
    public String getUuid() {
        return config().getUuid();
    }

    @Override
    public String uuid() {
        return getUuid();
    }

    @Override
    public String getName() {
        return config().getName();
    }

    @Override
    public String name() {
        return getName();
    }

    @Override
    public String repositoryType() {
        return getRepositoryType();
    }

    @Override
    public NutsWorkspace workspace() {
        return getWorkspace();
    }

    @Override
    public NutsRepository parentRepository() {
        return getParentRepository();
    }

    @Override
    public Map<String, Object> userProperties() {
        return userProperties;
    }

    @Override
    public void addUserPropertyListener(NutsMapListener<String, Object> listener) {
        userProperties.addListener(listener);
    }

    @Override
    public void removeUserPropertyListener(NutsMapListener<String, Object> listener) {
        userProperties.removeListener(listener);
    }

    @Override
    public NutsMapListener<String, Object>[] getUserPropertyListeners() {
        return userProperties.getListeners();
    }
}
