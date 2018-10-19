/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.archetypes.DefaultNutsWorkspaceArchetypeComponent;
import net.vpc.app.nuts.extensions.executors.CustomNutsExecutorComponent;
import net.vpc.app.nuts.extensions.util.*;
import net.vpc.app.nuts.extensions.filters.DefaultNutsIdMultiFilter;
import net.vpc.app.nuts.extensions.filters.dependency.NutsExclusionDependencyFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsPatternIdFilter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.vpc.app.nuts.extensions.filters.id.NutsIdFilterOr;
import net.vpc.app.nuts.extensions.filters.id.NutsSimpleIdFilter;
import net.vpc.app.nuts.extensions.repos.NutsBootFolderRepository;
import net.vpc.common.util.ArtifactUtils;

/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNutsWorkspace implements NutsWorkspace, NutsWorkspaceImpl {

    public static final Logger log = Logger.getLogger(DefaultNutsWorkspace.class.getName());
    private static final DefaultNutsDescriptor TEMP_DESC = new DefaultNutsDescriptor(
            CoreNutsUtils.parseNutsId("temp:exe#1.0"),
            null,
            null,
            "exe",
            true, "exe", new NutsExecutorDescriptor(CoreNutsUtils.parseNutsId("exec"), new String[0], null), null, null, null, null, null, null, null, null, null
    );
    private NutsFile nutsComponentId;
    private final List<NutsWorkspaceListener> workspaceListeners = new ArrayList<>();
    private boolean initializing;
    private NutsRepository bootstrapNutsRepository;
    protected final DefaultNutsWorkspaceSecurityManager securityManager = new DefaultNutsWorkspaceSecurityManager(this);
    protected final NutsWorkspaceServerManager serverManager = new DefaultNutsWorkspaceServerManager(this);
    protected final DefaultNutsWorkspaceConfigManager configManager = new DefaultNutsWorkspaceConfigManager(this);
    protected DefaultNutsWorkspaceExtensionManager extensionManager;
    protected final DefaultNutsWorkspaceRepositoryManager repositoryManager = new DefaultNutsWorkspaceRepositoryManager(this);

    public DefaultNutsWorkspace() {

    }

    @Override
    public NutsSession createSession() {
        return getExtensionManager().getFactory().createSession();
    }

    @Override
    public NutsWorkspaceRepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    @Override
    public NutsWorkspaceConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public NutsWorkspaceSecurityManager getSecurityManager() {
        return securityManager;
    }

    @Override
    public NutsWorkspaceServerManager getServerManager() {
        return serverManager;
    }

    public boolean isInitializing() {
        return initializing;
    }

    @Override
    public void removeWorkspaceListener(NutsWorkspaceListener listener) {
        workspaceListeners.add(listener);
    }

    @Override
    public void addWorkspaceListener(NutsWorkspaceListener listener) {
        if (listener != null) {
            workspaceListeners.add(listener);
        }
    }

    @Override
    public NutsWorkspaceListener[] getWorkspaceListeners() {
        return workspaceListeners.toArray(new NutsWorkspaceListener[workspaceListeners.size()]);
    }

    @Override
    public NutsWorkspace openWorkspace(String workspace, NutsWorkspaceCreateOptions options) {
        NutsWorkspaceObjectFactory newFactory = getExtensionManager().getFactory().createSupported(NutsWorkspaceObjectFactory.class, self());
        NutsWorkspace nutsWorkspace = getExtensionManager().getFactory().createSupported(NutsWorkspace.class, self());
        NutsWorkspaceImpl nutsWorkspaceImpl = (NutsWorkspaceImpl) nutsWorkspace;
        if (nutsWorkspaceImpl.initializeWorkspace(configManager.getWorkspaceBoot(), newFactory,
                configManager.getWorkspaceBootId().toString(), configManager.getWorkspaceRuntimeId().toString(),
                workspace,
                configManager.getBootClassWorldURLs(),
                configManager.getBootClassLoader(), options.copy().setIgnoreIfFound(true))) {
            log.log(Level.FINE, "workspace created : " + configManager.getWorkspaceBoot());
        }
        return nutsWorkspace;
    }

    @Override
    public boolean initializeWorkspace(NutsBootWorkspace workspaceBoot, NutsWorkspaceObjectFactory factory, String workspaceBootId, String workspaceRuntimeId, String workspace,
                                       URL[] bootClassWorldURLs, ClassLoader bootClassLoader,
                                       NutsWorkspaceCreateOptions options) {

        if (options == null) {
            options = new NutsWorkspaceCreateOptions();
        }
        extensionManager = new DefaultNutsWorkspaceExtensionManager(this, new DefaultNutsWorkspaceFactory(factory, self()));
        configManager.onInitializeWorkspace(workspaceBoot,
                CoreStringUtils.isEmpty(workspaceBoot.getNutsHomeLocation()) ? NutsConstants.DEFAULT_NUTS_HOME : workspaceBoot.getNutsHomeLocation(),
                factory, getExtensionManager().getFactory().parseNutsId(workspaceBootId),
                getExtensionManager().getFactory().parseNutsId(workspaceRuntimeId), resolveWorkspacePath(workspace),
                bootClassWorldURLs,
                bootClassLoader == null ? Thread.currentThread().getContextClassLoader() : bootClassLoader);

        boolean exists = isWorkspaceFolder(configManager.getWorkspaceLocation());
        if (!options.isCreateIfNotFound() && !exists) {
            throw new NutsWorkspaceNotFoundException(workspace);
        }
        if (!options.isIgnoreIfFound() && exists) {
            throw new NutsWorkspaceAlreadyExistsException(workspace);
        }

        this.bootstrapNutsRepository = new NutsBootFolderRepository(workspaceBoot, self(), null);

        extensionManager.oninitializeWorkspace(bootClassLoader);

        NutsSession session = createSession();

        initializing = true;
        try {
            if (!reloadWorkspace(options.isSaveIfCreated(), session, options.getExcludedExtensions(), options.getExcludedRepositories())) {
                if (!options.isCreateIfNotFound()) {
                    throw new NutsWorkspaceNotFoundException(workspace);
                }
                exists = false;
                configManager.setConfig(new NutsWorkspaceConfigImpl());
                initializeWorkspace(options.getArchetype(), session);
                if (options.isSaveIfCreated()) {
                    getConfigManager().save();
                }
            } else if (configManager.getConfig().getRepositories().length == 0) {
                initializeWorkspace(options.getArchetype(), session);
                if (options.isSaveIfCreated()) {
                    getConfigManager().save();
                }
            }
        } finally {
            initializing = false;
        }
        return !exists;
    }

    @Override
    public NutsFile fetchBootFile(NutsSession session) {
        session = validateSession(session);
        if (nutsComponentId == null) {
            nutsComponentId = fetch(NutsConstants.NUTS_ID_BOOT, session);
        }
        return nutsComponentId;
    }

    @Override
    public NutsFile install(String id, boolean force, NutsSession session) {
        session = validateSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_INSTALL)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_INSTALL);
        }
        NutsFile nutToInstall = fetchWithDependencies(id, session);
        if (nutToInstall != null && nutToInstall.getFile() != null) {
            if (nutToInstall.isInstalled()) {
                if (!force) {
                    throw new NutsAlreadytInstalledException(nutToInstall.getId());
                }
            }
            if (!isInstallable(nutToInstall, session)) {
                if (!force) {
                    throw new NutsNotInstallableException(nutToInstall.getId());
                }
            }
            postInstall(nutToInstall, session);
        }
        return nutToInstall;
    }

    @Override
    public NutsId commit(File folder, NutsSession session) {
        session = validateSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_DEPLOY)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_DEPLOY);
        }
        if (folder == null || !folder.isDirectory()) {
            throw new NutsIllegalArgumentException("Not a directory " + folder);
        }

        File file = new File(folder, NutsConstants.NUTS_DESC_FILE_NAME);
        NutsDescriptor d = CoreNutsUtils.parseNutsDescriptor(file);
        String oldVersion = CoreStringUtils.trim(d.getId().getVersion().getValue());
        if (oldVersion.endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
            oldVersion = oldVersion.substring(0, oldVersion.length() - NutsConstants.VERSION_CHECKED_OUT_EXTENSION.length());
            String newVersion = CoreVersionUtils.incVersion(oldVersion);
            NutsFile newVersionFound = null;
            try {
                newVersionFound = fetch(d.getId().setVersion(newVersion).toString(), session);
            } catch (NutsNotFoundException ex) {
                //ignore
            }
            if (newVersionFound == null) {
                d = d.setId(d.getId().setVersion(newVersion));
            } else {
                d = d.setId(d.getId().setVersion(oldVersion + ".1"));
            }
            NutsId newId = deploy(new NutsDeployment().setContent(folder).setDescriptor(d), session);
            d.write(file);
            try {
                CoreIOUtils.delete(folder);
            } catch (IOException ex) {
                throw new NutsIOException(ex);
            }
            return newId;
        } else {
            throw new NutsUnsupportedOperationException("commit not supported");
        }
    }

    @Override
    public NutsFile checkout(String id, File folder, NutsSession session) {
        session = validateSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_INSTALL)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_INSTALL);
        }
        NutsFile nutToInstall = fetchWithDependencies(id, session);
        if ("zip".equals(nutToInstall.getDescriptor().getExt())) {

            try {
                CoreIOUtils.unzip(nutToInstall.getFile(), folder, getConfigManager().getCwd());

                File file = new File(folder, NutsConstants.NUTS_DESC_FILE_NAME);
                NutsDescriptor d = CoreNutsUtils.parseNutsDescriptor(file);
                NutsVersion oldVersion = d.getId().getVersion();
                NutsId newId = d.getId().setVersion(oldVersion + NutsConstants.VERSION_CHECKED_OUT_EXTENSION);
                d = d.setId(newId);

                d.write(file, true);

                return new NutsFile(
                        newId,
                        d,
                        folder,
                        false,
                        false,
                        null
                );
            } catch (IOException ex) {
                throw new NutsIOException(ex);
            }
        } else {
            throw new NutsUnsupportedOperationException("Checkout not supported");
        }
    }

    @Override
    public NutsUpdate checkUpdates(String id, NutsSession session) {
        session = validateSession(session);
        NutsId baseId = CoreNutsUtils.parseOrErrorNutsId(id);
        NutsVersion version = baseId.getVersion();
        NutsId oldId = null;
        NutsId newId = null;
        boolean runtime = false;
        URL runtimeURL = null;
        //if (version.isSingleValue()) {
        // check runtime value
        try {
            String urlPath = "/META-INF/maven/" + baseId.getGroup() + "/" + baseId.getName() + "/pom.properties";
            URL resource = Nuts.class.getResource(urlPath);
            if (resource != null) {
                runtimeURL = CorePlatformUtils.resolveURLFromResource(Nuts.class, urlPath);
                oldId = baseId.setVersion(CoreIOUtils.loadProperties(resource).getProperty("version", "0.0.0"));
                runtime = true;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        if (oldId == null) {
            try {
                String urlPath = "/META-INF/nuts/" + baseId.getGroup() + "/" + baseId.getName() + "/nuts.properties";
                URL resource = Nuts.class.getResource(urlPath);
                if (resource != null) {
                    runtimeURL = CorePlatformUtils.resolveURLFromResource(Nuts.class, urlPath);
                    oldId = baseId.setVersion(CoreIOUtils.loadProperties(resource).getProperty("project.version", "0.0.0"));
                    runtime = true;
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        //}
        if (oldId == null) {
            if (version.isSingleValue()) {
                try {
                    oldId = bootstrapNutsRepository.fetchDescriptor(getExtensionManager().getFactory().parseNutsId(id), session.setFetchMode(NutsFetchMode.OFFLINE)).getId();
                } catch (Exception ex) {
                    //ignore
                }
            } else {
                try {
                    oldId = bootstrapNutsRepository.fetchDescriptor(getExtensionManager().getFactory().parseNutsId(id), session.setFetchMode(NutsFetchMode.OFFLINE)).getId();
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        NutsFile newFileId = null;
        try {
            newId = bootstrapNutsRepository.fetchDescriptor(getExtensionManager().getFactory().parseNutsId(id), session.setFetchMode(NutsFetchMode.ONLINE)).getId();
            newFileId = bootstrapNutsRepository.fetch(newId, session);
        } catch (Exception ex) {
            //ignore
        }

        //compare canonical forms
        NutsId cnewId = toCanonicalForm(newId);
        NutsId coldId = toCanonicalForm(oldId);
        if (cnewId != null && (coldId == null || !cnewId.equals(coldId))) {
            File oldFile = runtimeURL == null ? null : CorePlatformUtils.resolveLocalFileFromURL(runtimeURL);
            File newFile = newFileId == null ? null : newFileId.getFile();
            return new NutsUpdate(baseId, oldId, newId, oldFile, newFile, runtime);
        }
        return null;
    }

    @Override
    public NutsUpdate[] checkWorkspaceUpdates(boolean applyUpdates, String[] args, NutsSession session) {
        session = validateSession(session);
        List<NutsUpdate> found = new ArrayList<>();
        NutsUpdate r = checkUpdates(NutsConstants.NUTS_ID_BOOT, session);
        if (r != null) {
            found.add(r);
        }
        if (requiresCoreExtension()) {
            r = checkUpdates(getRuntimeId().getFullName(), session);
            if (r != null) {
                found.add(r);
            }
        }
        for (String ext : getConfigManager().getConfig().getExtensions()) {
            NutsId nutsId = CoreNutsUtils.parseOrErrorNutsId(ext);
            r = checkUpdates(nutsId.toString(), session);
            if (r != null) {
                found.add(r);
            }
        }
        NutsUpdate[] updates = found.toArray(new NutsUpdate[found.size()]);
        NutsPrintStream out = resolveOut(session);
        if (updates.length == 0) {
            out.printf("Workspace is [[up-to-date]]\n");
            return updates;
        } else {
            out.printf("Workspace has %s component%s to update\n", updates.length, (updates.length > 1 ? "s" : ""));
            for (NutsUpdate update : updates) {
                out.printf("%s  : %s => [[%s]]\n", update.getBaseId(), update.getLocalId(), update.getAvailableId());
            }
        }

        if (applyUpdates) {
            File myNutsJar = null;
            File newNutsJar = null;
            for (NutsUpdate update : updates) {
                if (update.getAvailableIdFile() != null && update.getOldIdFile() != null) {
                    if (update.getBaseId().getFullName().equals(NutsConstants.NUTS_ID_BOOT)) {
                        myNutsJar = update.getOldIdFile();
                        newNutsJar = update.getAvailableIdFile();
                    }
                }
            }

            if (myNutsJar != null) {
                List<String> all = new ArrayList<>();
                all.add("--apply-updates");
                all.add(CoreIOUtils.getCanonicalPath(myNutsJar));
                for (String arg : args) {
                    if (!"--update".equals(arg) && !"--check-updates".equals(arg)) {
                        all.add(arg);
                    }
                }
                out.printf("applying nuts patch to [[%s]] ...\n", CoreIOUtils.getCanonicalPath(myNutsJar));
                exec(newNutsJar, all.toArray(new String[all.size()]), false, false, session);
                return updates;
            }
        }
        return updates;
    }

    public NutsId getBootId() {
        String bootId = configManager.getWorkspaceBoot().getBootId();
        if (CoreStringUtils.isEmpty(bootId)) {
            bootId = NutsConstants.NUTS_ID_BOOT;
        }
        return getExtensionManager().getFactory().parseNutsId(bootId);
    }

    public NutsId getRuntimeId() {
        String runtimeId = configManager.getWorkspaceBoot().getRuntimeId();
        if (CoreStringUtils.isEmpty(runtimeId)) {
            runtimeId = NutsConstants.NUTS_ID_RUNTIME;
        }
        return getExtensionManager().getFactory().parseNutsId(runtimeId);
    }

    /**
     * true when core extension is required for running this workspace. A
     * default implementation should be as follow, but developers may implements
     * this with other logic : core extension is required when there are no
     * extensions or when the
     * <code>NutsConstants.ENV_KEY_EXCLUDE_CORE_EXTENSION</code> is forced to
     * false
     *
     * @return true when core extension is required for running this workspace
     */
    public boolean requiresCoreExtension() {
        boolean exclude = false;
        if (getConfigManager().getConfig().getExtensions().length > 0) {
            exclude = Boolean.parseBoolean(getConfigManager().getEnv(NutsConstants.ENV_KEY_EXCLUDE_CORE_EXTENSION, "false"));
        }
        if (!exclude) {
            boolean coreFound = false;
            for (String ext : getConfigManager().getConfig().getExtensions()) {
                if (CoreNutsUtils.parseOrErrorNutsId(ext).isSameFullName(getExtensionManager().getFactory().parseNutsId(getRuntimeId().getFullName()))) {
                    coreFound = true;
                    break;
                }
            }
            if (!coreFound) {
                return true;
            }
        }
        return false;
    }

    private Properties getBootInfo(NutsId id) {
        if (id.getVersion().isEmpty()) {
            id = id.setVersion("LATEST");
        }
        List<URLLocation> bootUrls = new ArrayList<>();
        for (URLLocation r : extensionManager.getExtensionURLLocations(id.toString(), NutsConstants.NUTS_ID_BOOT, "properties")) {
            bootUrls.add(r);
            if (r.getUrl() != null) {
                Properties p = CoreIOUtils.loadURLProperties(r.getUrl());
                if (!p.isEmpty() && p.containsKey("runtimeId")) {
                    return p;
                }
            }
        }
        if (bootUrls.isEmpty()) {
            log.log(Level.CONFIG, "Inaccessible runtime info. Fatal error");
        }
        for (URLLocation bootUrl : bootUrls) {
            log.log(Level.CONFIG, "Inaccessible runtime info url : {0}", bootUrl.getPath());
        }
        throw new NutsIllegalArgumentException("Inaccessible runtime info : " + bootUrls);
    }

    protected String expandPath(String path) {
        return CoreNutsUtils.expandPath(path,getConfigManager().getNutsHomeLocation());
    }

    @Override
    public NutsFile updateWorkspace(String version, boolean force, NutsSession session) {
        session = validateSession(session);
        String nutsIdStr = NutsConstants.NUTS_ID_BOOT + (CoreStringUtils.isEmpty(version) ? "" : ("#") + version);
        NutsFile[] bootIdFile = bootstrapUpdate(nutsIdStr, force, session);
        Properties bootInfo = getBootInfo(bootIdFile[0].getId());
        String runtimeId = bootInfo.getProperty("runtimeId");
        NutsFile[] runtimeIdFiles = bootstrapUpdate(runtimeId, force, session);
        if (runtimeIdFiles.length == 0) {
            throw new NutsBootException("Unable to locate update for " + runtimeId);
        }
        Properties bootProperties = new Properties();
        final NutsId runtimeIdFile = runtimeIdFiles[0].getId();
        bootProperties.setProperty("runtimeId", runtimeIdFile.toString());
        NutsRepository[] repositories = getRepositoryManager().getRepositories();
        List<String> repositoryUrls = new ArrayList<>();
        repositoryUrls.add(expandPath(NutsConstants.URL_COMPONENTS_LOCAL));
        for (NutsRepository repository : repositories) {
            if (repository.getRepositoryType().equals(NutsConstants.DEFAULT_REPOSITORY_TYPE) || repository.getRepositoryType().equals("maven")) {
                repositoryUrls.add(repository.getConfigManager().getLocation());
            } else {
                for (NutsRepository mirror : repository.getMirrors()) {
                    if (mirror.getRepositoryType().equals(NutsConstants.DEFAULT_REPOSITORY_TYPE) || repository.getRepositoryType().equals("maven")) {
                        repositoryUrls.add(mirror.getConfigManager().getLocation());
                    }
                }
            }
        }
        String repositoriesPath = CoreStringUtils.join(";", repositoryUrls);
        bootProperties.setProperty("repositories", repositoriesPath);
        File r = bootstrapNutsRepository.getConfigManager().getLocationFolder();
        CoreIOUtils.storeProperties(bootProperties, new File(r, CoreNutsUtils.getPath(runtimeIdFile, ".properties", File.separator)));

        Properties coreProperties = new Properties();
        List<String> dependencies = new ArrayList<>();
        for (NutsFile fetchDependency : runtimeIdFiles) {
            dependencies.add(fetchDependency.getId().setNamespace(null).toString());
        }
        coreProperties.put("project.id", runtimeIdFile.getFullName());
        coreProperties.put("project.version", runtimeIdFile.getVersion().toString());
        coreProperties.put("project.repositories", repositoriesPath);
        coreProperties.put("project.dependencies.compile", CoreStringUtils.join(";", dependencies));
        CoreIOUtils.storeProperties(coreProperties, new File(r, CoreNutsUtils.getPath(runtimeIdFile, ".properties", File.separator)));

        List<NutsFile> updatedExtensions = new ArrayList<>();
        for (String ext : getConfigManager().getConfig().getExtensions()) {
            NutsVersion nversion = CoreNutsUtils.parseOrErrorNutsId(ext).getVersion();
            if (!nversion.isSingleValue()) {
                //will update bootstrap workspace so that next time
                //it will be loaded
                NutsFile[] newVersion = bootstrapUpdate(ext, force, session);
                if (!newVersion[0].getId().getVersion().equals(nversion)) {
                    updatedExtensions.add(newVersion[0]);
                }
            }
        }
        if (updatedExtensions.size() > 0) {
            log.log(Level.INFO, "Some extensions were updated. Nuts should be restarted for extensions to take effect.");
        }
        return bootIdFile[0];
    }

    @Override
    public NutsFile[] update(String[] toUpdateIds, String[] toRetainDependencies, boolean force, NutsSession session) {
        session = validateSession(session);
        Map<String, NutsFile> all = new HashMap<>();
        for (String id : new HashSet<>(Arrays.asList(toUpdateIds))) {
            NutsFile updated = update(id, force, session);
            all.put(updated.getId().getFullName(), updated);
        }
        if (toRetainDependencies != null) {
            for (String d : new HashSet<>(Arrays.asList(toRetainDependencies))) {
                NutsDependency dd = CoreNutsUtils.parseNutsDependency(d);
                if (all.containsKey(dd.getFullName())) {
                    NutsFile updated = all.get(dd.getFullName());
                    if (!dd.getVersion().toFilter().accept(updated.getId().getVersion())) {
                        throw new NutsIllegalArgumentException(dd + " unsatisfied  : " + updated.getId().getVersion());
                    }
                }
            }
        }
        return all.values().toArray(new NutsFile[all.size()]);
    }

    @Override
    public NutsFile update(String id, boolean force, NutsSession session) {
        session = validateSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_INSTALL)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_INSTALL);
        }
        NutsVersion version = CoreNutsUtils.parseOrErrorNutsId(id).getVersion();
        if (version.isSingleValue()) {
            throw new NutsIllegalArgumentException("Version is too restrictive. You would use fetch or install instead");
        }
        NutsFile nutToInstall = fetchWithDependencies(id, session);
        if (nutToInstall != null && nutToInstall.getFile() != null) {
            if (force || (!nutToInstall.isInstalled() && !nutToInstall.isCached())) {
                postInstall(nutToInstall, session);
            }
        }
        return nutToInstall;
    }

    @Override
    public boolean isInstalled(String id, boolean checkDependencies, NutsSession session) {
        session = validateSession(session);
        NutsFile nutToInstall = null;
        try {
            if (checkDependencies) {
                nutToInstall = fetchWithDependencies(id, session.copy().setFetchMode(NutsFetchMode.OFFLINE).setTransitive(false));
            } else {
                nutToInstall = fetch(id, session.copy().setFetchMode(NutsFetchMode.OFFLINE).setTransitive(false));
            }
        } catch (Exception e) {
            return false;
        }
        return isInstalled(nutToInstall, session);
    }

    @Override
    public boolean uninstall(String id, NutsSession session) {
        session = validateSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_UNINSTALL)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_UNINSTALL);
        }
        NutsFile nutToInstall = fetchWithDependencies(id, session.copy().setTransitive(false));
        if (!isInstalled(nutToInstall, session)) {
            throw new NutsIllegalArgumentException(id + " Not Installed");
        }
        NutsInstallerComponent ii = getInstaller(nutToInstall, session);
        if (ii == null) {
            return false;
        }
        NutsDescriptor descriptor = nutToInstall.getDescriptor();
        NutsExecutorDescriptor installer = descriptor.getInstaller();
        NutsExecutionContext executionContext = new NutsExecutionContextImpl(
                nutToInstall, new String[0],
                installer == null ? null : installer.getArgs(), installer == null ? null : installer.getProperties(),
                new Properties(),
                session, self());
        ii.uninstall(executionContext);
        return true;
    }

    @Override
    public int exec(String[] cmd, Properties env, NutsSession session) {
        session = validateSession(session);
        if (cmd == null || cmd.length == 0) {
            throw new NutsIllegalArgumentException("Missing command");
        }
        String[] args2 = new String[cmd.length - 1];
        System.arraycopy(cmd, 1, args2, 0, args2.length);
        return exec(
                cmd[0],
                args2,
                env, session
        );
    }

    @Override
    public int exec(String id, String[] args, Properties env, NutsSession session) {
        session = validateSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_EXEC)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_EXEC + " : " + id);
        }
        if (id.equals("console")) {
            NutsConsole commandLine = getExtensionManager().getFactory().createConsole(session);
            return commandLine.run(args);

        } else if (id.contains("/") || id.contains("\\")) {
            try (CharacterizedFile c = characterize(CoreIOUtils.createInputStreamSource(id, "path", id, getConfigManager().getCwd()), session)) {
                if (c.descriptor == null) {
                    //this is a native file?
                    c.descriptor = TEMP_DESC;
                }
                NutsFile nutToRun = new NutsFile(
                        c.descriptor.getId(),
                        c.descriptor,
                        (File) c.contentFile.getSource(),
                        false,
                        c.temps.size() > 0,
                        null
                );
                return exec(nutToRun, args, env, session);
            }
        } else {
            NutsId nid = CoreNutsUtils.parseOrErrorNutsId(id);
            return exec(nid, args, env, session);
        }
    }

    @Override
    public boolean isFetched(String id, NutsSession session) {
        session = validateSession(session);
        return isFetched(CoreNutsUtils.parseOrErrorNutsId(id), session);
    }

    @Override
    public NutsFile fetchWithDependencies(String id, NutsSession session) {
        session = validateSession(session);
        NutsFile fetched = fetch(id, session);
        fetchDependencies(new NutsDependencySearch(id), session);
        return fetched;
    }

    @Override
    public NutsFile fetch(String id, NutsSession session) {
        session = validateSession(session);
        return fetchSimple(CoreNutsUtils.parseOrErrorNutsId(id), session);
    }

    @Override
    public NutsFile[] fetchDependencies(NutsDependencySearch search, NutsSession session) {
        session = validateSession(session);
        return fetchDependencies(search, new NutsIdGraph(), session);
    }

    @Override
    public NutsId resolveId(String id, NutsSession session) {
        session = validateSession(session);
        NutsId nutsId = CoreNutsUtils.parseOrErrorNutsId(id);
        return resolveId(nutsId, session);
    }

    public NutsId resolveId(NutsId id, NutsSession session) {
        session = validateSession(session);
        //add env parameters to fetch adequate nuts
        id = NutsWorkspaceHelper.configureFetchEnv(id);

        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            session = session.copy().setFetchMode(mode);
            try {
                if (id.getGroup() == null) {
                    String[] groups = getConfigManager().getConfig().getImports();
                    for (String group : groups) {
                        try {
                            NutsId f = resolveId(id.setGroup(group), session);
                            if (f != null) {
                                return f;
                            }
                        } catch (NutsNotFoundException ex) {
                            //not found
                        }
                    }
                    throw new NutsNotFoundException(id);
                }

                for (NutsRepository repo : getEnabledRepositories(id, session)) {
                    try {
                        NutsDescriptor child = repo.fetchDescriptor(id, session);
                        if (child != null) {
                            NutsId id2 = child.getId();
                            if (CoreStringUtils.isEmpty(id2.getNamespace())) {
                                id2 = id2.setNamespace(repo.getRepositoryId());
                            }
                            return id2;
                        }
                    } catch (NutsNotFoundException exc) {
                        //
                    }
                }
            } catch (NutsNotFoundException ex) {
                //ignore
            }
        }
        throw new NutsNotFoundException(id);
    }

    @Override
    public NutsId findOne(NutsSearch search, NutsSession session) {
        List<NutsId> r = find(search, session);
        if (r.isEmpty()) {
            return null;
        }
        if (r.size() > 1) {
            throw new IllegalArgumentException("Too many results (" + r.size() + " but expected one only)");
        }
        return r.get(0);
    }

    @Override
    public NutsId findFirst(NutsSearch search, NutsSession session) {
        List<NutsId> r = find(search, session);
        if (r.isEmpty()) {
            return null;
        }
        return r.get(0);
    }

    @Override
    public List<NutsId> find(NutsSearch search, NutsSession session) {
        session = validateSession(session);
        List<NutsId> li = CoreCollectionUtils.toList(findIterator(search, session));
        if (search.isSort()) {
            li.sort(new Comparator<NutsId>() {
                @Override
                public int compare(NutsId o1, NutsId o2) {
                    int x = o1.getFullName().compareTo(o2.getFullName());
                    if (x != 0) {
                        return x;
                    }
                    //latests versions first
                    x = o1.getVersion().compareTo(o2.getVersion());
                    return -x;
                }
            });
        }
        return li;
    }

    @Override
    public Iterator<NutsId> findIterator(NutsSearch search, NutsSession session) {
        session = validateSession(session);
        HashSet<String> someIds = new HashSet<>(Arrays.asList(search.getIds()));
        HashSet<String> goodIds = new HashSet<>();
        HashSet<String> wildcardIds = new HashSet<>();
        for (String someId : someIds) {
            if (NutsPatternIdFilter.containsWildcad(someId)) {
                wildcardIds.add(someId);
            } else {
                goodIds.add(someId);
            }
        }
        NutsRepositoryFilter repositoryFilter = CoreNutsUtils.createNutsRepositoryFilter(search.getRepositoryFilter());
        NutsVersionFilter versionFilter = CoreNutsUtils.createNutsVersionFilter(search.getVersionFilter());
        NutsDescriptorFilter descriptorFilter = CoreNutsUtils.createNutsDescriptorFilter(search.getDescriptorFilter());
        NutsIdFilter idFilter = CoreNutsUtils.simplify(CoreNutsUtils.createNutsIdFilter(search.getIdFilter()));
        if (idFilter instanceof NutsPatternIdFilter) {
            NutsPatternIdFilter f = (NutsPatternIdFilter) idFilter;
            for (String id : f.getIds()) {
                if (NutsPatternIdFilter.containsWildcad(id)) {
                    wildcardIds.add(id);
                } else {
                    goodIds.add(id);
                }
            }
            idFilter = null;
        }
        if (idFilter instanceof NutsSimpleIdFilter) {
            NutsSimpleIdFilter f = (NutsSimpleIdFilter) idFilter;
            goodIds.add(f.getId().toString());
            idFilter = null;
        }
        if (!wildcardIds.isEmpty()) {
            NutsPatternIdFilter ff = new NutsPatternIdFilter(wildcardIds.toArray(new String[wildcardIds.size()]));
            idFilter = CoreNutsUtils.simplify(new NutsIdFilterOr(idFilter, ff));
        }
        if (goodIds.size() > 0) {
            IteratorList<NutsId> result = new IteratorList<>();
            for (String id : goodIds) {
                Iterator<NutsId> good = null;
                NutsId nutsId = getExtensionManager().getFactory().parseNutsId(id);
                if (nutsId != null) {
                    List<NutsId> nutsId2 = new ArrayList<>();
                    if (nutsId.getGroup() == null) {
                        for (String aImport : getConfigManager().getImports()) {
                            nutsId2.add(nutsId.setGroup(aImport));
                        }
                    } else {
                        nutsId2.add(nutsId);
                    }
                    for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
                        NutsSession session2 = session.copy().setFetchMode(mode);

                        IteratorList<NutsId> all = new IteratorList<NutsId>();
                        for (NutsId nutsId1 : nutsId2) {
                            for (NutsRepository repo : getEnabledRepositories(nutsId1, session2)) {
                                if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                                    try {
                                        DefaultNutsIdMultiFilter filter = new DefaultNutsIdMultiFilter(nutsId1.getQueryMap(), idFilter, versionFilter, descriptorFilter, repo, session);
                                        Iterator<NutsId> child = repo.findVersions(nutsId1, filter, session2);
                                        all.addNonEmpty(child);
                                    } catch (NutsNotFoundException exc) {
                                        //
                                    }
                                }
                            }
                        }
                        Iterator<NutsId> b = CoreNutsUtils.nullifyIfEmpty(all);
                        if (b != null) {
                            good = b;
                            break;
                        }
                    }
                    if (good != null) {
                        result.addNonEmpty(good);
                    } else if (nutsId.getGroup() == null) {
                        //now will look with *:artifactId pattern
                        NutsSearch search2 = new NutsSearch(search);
                        search2.setIds();
                        search2.setIdFilter(new NutsIdFilterOr(
                                new NutsPatternIdFilter(new String[]{nutsId.setGroup("*").toString()}),
                                CoreNutsUtils.simplify(CoreNutsUtils.createNutsIdFilter(search2.getIdFilter()))
                        ));
                        Iterator<NutsId> b = findIterator(search2, session);
                        b = CoreNutsUtils.nullifyIfEmpty(b);
                        if (b != null) {
                            result.addNonEmpty(b);
                        }
                    }
                }
            }
            if (search.isLastestVersions()) {
                return CoreNutsUtils.filterNutsIdByLatestVersion(CoreCollectionUtils.toList(result)).iterator();
            }
            return result;
        }

        if (idFilter instanceof NutsPatternIdFilter) {
            String[] ids = ((NutsPatternIdFilter) idFilter).getIds();
            if (ids.length == 1) {
                String id = ids[0];
                if (id.indexOf('*') < 0 && id.indexOf(':') > 0) {
                    NutsId nid = getExtensionManager().getFactory().parseNutsId(id);
                    if (nid != null) {

                        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
                            NutsSession session2 = session.copy().setFetchMode(mode);
                            IteratorList<NutsId> all = new IteratorList<NutsId>();
                            for (NutsRepository repo : getEnabledRepositories()) {
                                try {
                                    if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                                        DefaultNutsIdMultiFilter filter = new DefaultNutsIdMultiFilter(nid.getQueryMap(), idFilter, versionFilter, descriptorFilter, repo, session);
                                        Iterator<NutsId> child = repo.findVersions(nid, filter, session2);
                                        all.addNonEmpty(child);
                                    }
                                } catch (Exception exc) {
                                    //
                                }
                            }
                            Iterator<NutsId> b = CoreNutsUtils.nullifyIfEmpty(all);
                            if (b != null) {
                                if (search.isLastestVersions()) {
                                    return CoreNutsUtils.filterNutsIdByLatestVersion(CoreCollectionUtils.toList(b)).iterator();
                                }
                                return b;
                            }
                        }
                        return Collections.emptyIterator();
                    }
                }
            }
        }

        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            IteratorList<NutsId> all = new IteratorList<NutsId>();
            for (NutsRepository repo : getEnabledRepositories()) {
                try {
                    if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                        DefaultNutsIdMultiFilter filter = new DefaultNutsIdMultiFilter(null, idFilter, versionFilter, descriptorFilter, repo, session);
                        Iterator<NutsId> child = repo.find(filter, session2);
                        all.addNonEmpty(child);
                    }
                } catch (Exception exc) {
                    //
                }
            }
            Iterator<NutsId> b = CoreNutsUtils.nullifyIfEmpty(all);
            if (b != null) {
                if (search.isLastestVersions()) {
                    return CoreNutsUtils.filterNutsIdByLatestVersion(CoreCollectionUtils.toList(b)).iterator();
                }
                return b;
            }
        }
        return Collections.emptyIterator();
    }

    @Override
    public NutsDescriptor fetchDescriptor(String idString, boolean effective, NutsSession session) {
        session = validateSession(session);
        NutsId id = CoreNutsUtils.parseOrErrorNutsId(idString);
        id = NutsWorkspaceHelper.configureFetchEnv(id);
        Set<String> errors = new LinkedHashSet<>();
        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            session = session.copy().setFetchMode(mode);
            try {
                if (id.getGroup() == null) {
                    String[] groups = getConfigManager().getConfig().getImports();
                    for (String group : groups) {
                        try {
                            NutsDescriptor f = fetchDescriptor(id.setGroup(group).toString(), effective, session);
                            if (f != null) {
                                return f;
                            }
                        } catch (NutsNotFoundException exc) {
                            errors.add(CoreStringUtils.exceptionToString(exc));
                            //not found
                        }
                    }
                    throw new NutsNotFoundException(id);
                }

                for (NutsRepository repo : getEnabledRepositories(id, session)) {
                    try {
                        NutsDescriptor child = repo.fetchDescriptor(id, session);
                        if (child != null) {
//                            if (CoreStringUtils.isEmpty(child.getId().getNamespace())) {
//                                child = child.setId(child.getId().setNamespace(repo.getRepositoryId()));
//                            }
                            if (effective) {
                                try {
                                    return resolveEffectiveDescriptor(child, session);
                                } catch (NutsNotFoundException ex) {
                                    //ignore
                                }
                            } else {
                                return child;
                            }
                        }
                    } catch (NutsNotFoundException exc) {
                        //
                    }
                }
            } catch (NutsNotFoundException ex) {
                //ignore
            }
        }
        throw new NutsNotFoundException(idString, CoreStringUtils.join("\n", errors), null);
    }

    @Override
    public String fetchHash(String id, NutsSession session) {
        session = validateSession(session);
        NutsId nutsId = CoreNutsUtils.parseOrErrorNutsId(id);
        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            for (NutsRepository repo : getEnabledRepositories(nutsId, session2)) {
                try {
                    String hash = repo.fetchHash(nutsId, session2);
                    if (hash != null) {
                        return hash;
                    }
                } catch (NutsNotFoundException exc) {
                    //
                }
            }
        }
        return null;
    }

    @Override
    public String fetchDescriptorHash(String id, NutsSession session) {
        session = validateSession(session);
        NutsId nutsId = CoreNutsUtils.parseOrErrorNutsId(id);
        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            for (NutsRepository repo : getEnabledRepositories(nutsId, session2)) {
                try {
                    String hash = repo.fetchDescriptorHash(nutsId, session2);
                    if (hash != null) {
                        return hash;
                    }
                } catch (NutsNotFoundException exc) {
                    //
                }
            }
        }
        return null;
    }

    @Override
    public void push(String id, String repositoryId, boolean force, NutsSession session) {
        session = validateSession(session);
        NutsId nid = CoreNutsUtils.parseOrErrorNutsId(id);
        session = validateSession(session);
        if (CoreStringUtils.trim(nid.getVersion().getValue()).endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
            throw new NutsIllegalArgumentException("Invalid Version " + nid.getVersion());
        }
        NutsSession nonTransitiveSession = session.copy().setTransitive(false);
        NutsFile file = fetch(id, nonTransitiveSession);
        if (file == null) {
            throw new NutsIllegalArgumentException("Nothing to push");
        }
        if (CoreStringUtils.isEmpty(repositoryId)) {
            Set<String> errors = new LinkedHashSet<>();
            for (NutsRepository repo : getEnabledRepositories(file.getId(), session)) {
                NutsFile id2 = null;
                try {
                    id2 = repo.fetch(file.getId(), session);
                } catch (Exception e) {
                    errors.add(CoreStringUtils.exceptionToString(e));
                    //
                }
                if (id2 != null && repo.isSupportedMirroring()) {
                    try {
                        repo.push(nid, repositoryId, force, session);
                        return;
                    } catch (Exception e) {
                        errors.add(CoreStringUtils.exceptionToString(e));
                        //
                    }
                }
            }
            throw new NutsRepositoryNotFoundException(repositoryId + " : " + CoreStringUtils.join("\n", errors));
        } else {
            NutsRepository repository = getRepositoryManager().findRepository(repositoryId);
            checkEnabled(repository.getRepositoryId());
            repository.deploy(file.getId(), file.getDescriptor(), file.getFile(), force, session);
        }
    }

    @Override
    public NutsFile createBundle(File contentFolder, File destFile, NutsSession session) {
        session = validateSession(session);
        if (contentFolder.isDirectory()) {
            NutsDescriptor descriptor = null;
            File ext = new File(contentFolder, NutsConstants.NUTS_DESC_FILE_NAME);
            if (ext.exists()) {
                descriptor = CoreNutsUtils.parseNutsDescriptor(ext);
            } else {
                descriptor = resolveNutsDescriptorFromFileContent(CoreIOUtils.createInputStreamSource(contentFolder, getConfigManager().getCwd()), session);
            }
            if (descriptor != null) {
                if ("zip".equals(descriptor.getExt())) {
                    if (destFile == null) {
                        destFile = CoreIOUtils.createFileByCwd(contentFolder.getParent() + "/" + descriptor.getId().getGroup() + "." + descriptor.getId().getName() + "." + descriptor.getId().getVersion() + ".zip", getConfigManager().getCwd());
//                        destFile=new File(contentFolder.getPath() + ".zip");
                    }
                    CoreIOUtils.zip(contentFolder, destFile);
                    return new NutsFile(
                            descriptor.getId(),
                            descriptor,
                            destFile,
                            true,
                            false,
                            null
                    );
                } else {
                    throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                }
            }
            throw new NutsIllegalArgumentException("Invalid Nut Folder source. unable to detect descriptor");
        } else {
            throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
        }
    }

    @Override
    public NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsSession session) {
        session = validateSession(session);
        if (descriptor == null) {
            throw new NutsNotFoundException("<null>");
        }
        NutsId thisId = descriptor.getId();
        if (CoreNutsUtils.isEffectiveId(thisId)) {
            return thisId.setFace(descriptor.getFace());
        }
        String g = thisId.getGroup();
        String v = thisId.getVersion().getValue();
        if ((CoreStringUtils.isEmpty(g)) || (CoreStringUtils.isEmpty(v))) {
            NutsId[] parents = descriptor.getParents();
            for (NutsId parent : parents) {
                NutsId p = resolveEffectiveId(fetchDescriptor(parent.toString(), false, session), session);
                if (CoreStringUtils.isEmpty(g)) {
                    g = p.getGroup();
                }
                if (CoreStringUtils.isEmpty(v)) {
                    v = p.getVersion().getValue();
                }
                if (!CoreStringUtils.isEmpty(g) && !CoreStringUtils.isEmpty(v)) {
                    break;
                }
            }
            NutsId bestId = new NutsIdImpl(null, g, thisId.getName(), v, "");
            String bestResult = bestId.toString();
            if (CoreStringUtils.isEmpty(g) || CoreStringUtils.isEmpty(v)) {
                throw new NutsNotFoundException(bestResult, "Unable to fetchEffective for " + thisId + ". Best Result is " + bestResult, null);
            }
            return bestId.setFace(descriptor.getFace());
        } else {
            return thisId.setFace(descriptor.getFace());
        }
    }

    @Override
    public NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        session = validateSession(session);
        NutsId[] parents = descriptor.getParents();
        NutsDescriptor[] parentDescriptors = new NutsDescriptor[parents.length];
        for (int i = 0; i < parentDescriptors.length; i++) {
            parentDescriptors[i] = resolveEffectiveDescriptor(
                    fetchDescriptor(parents[i].toString(), false, session),
                    session
            );
        }
        NutsDescriptor nutsDescriptor = descriptor.applyParents(parentDescriptors).applyProperties();
        if (nutsDescriptor.getPackaging().isEmpty()) {
            descriptor.applyParents(parentDescriptors).applyProperties();
        }
        return nutsDescriptor;
    }

    @Override
    public NutsId deploy(NutsDeployment deployment, NutsSession session) {

        File tempFile = null;
        TypedObject content = deployment.getContent();
        if (content == null || content.getValue() == null) {
            throw new NutsIllegalArgumentException("Missing content");
        }

        TypedObject vdescriptor = deployment.getDescriptor();
        NutsDescriptor descriptor = null;
        if (vdescriptor != null && vdescriptor.getValue() != null) {
            if (NutsDescriptor.class.equals(vdescriptor.getType())) {
                descriptor = (NutsDescriptor) vdescriptor.getValue();
                if (deployment.getDescSHA1() != null && !descriptor.getSHA1().equals(deployment.getDescSHA1())) {
                    throw new NutsIllegalArgumentException("Invalid Content Hash");
                }
            } else if (CoreIOUtils.isValidInputStreamSource(vdescriptor.getType())) {
                InputStreamSource inputStreamSource = CoreIOUtils.createInputStreamSource(vdescriptor.getValue(), vdescriptor.getVariant(), null, getConfigManager().getCwd());
                if (deployment.getDescSHA1() != null && !CoreSecurityUtils.evalSHA1(inputStreamSource.openStream(), true).equals(deployment.getDescSHA1())) {
                    throw new NutsIllegalArgumentException("Invalid Content Hash");
                }

                descriptor = CoreNutsUtils.parseNutsDescriptor(inputStreamSource.openStream(), true);
            } else {
                throw new NutsException("Unexpected type " + vdescriptor.getType());
            }
        }
        InputStreamSource contentSource = CoreIOUtils.createInputStreamSource(content.getValue(), content.getVariant(), null, getConfigManager().getCwd());

        CharacterizedFile characterizedFile = null;
        File contentFile2 = null;
        try {
            if (descriptor == null) {
                characterizedFile = characterize(contentSource, session);
                if (characterizedFile.descriptor == null) {
                    throw new NutsIllegalArgumentException("Missing descriptor");
                }
                descriptor = characterizedFile.descriptor;
            }
            tempFile = CoreIOUtils.createTempFile(descriptor, false);
            CoreIOUtils.copy(contentSource.openStream(), tempFile, true, true);
            contentFile2 = tempFile;
            return deploy(contentFile2, descriptor, deployment.getRepositoryId(), deployment.isForce(), session);
        } finally {
            if (characterizedFile != null) {
                characterizedFile.close();
            }
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    //    public NutsId deploy(InputStream contentInputStream, String sha1, NutsDescriptor descriptor, String repositoryId, NutsSession session) {
//        session = validateSession(session);
//        File tempFile = null;
//        try {
//            tempFile = CoreIOUtils.createTempFile(descriptor, false);
//            try {
//                CoreIOUtils.copy(contentInputStream, tempFile, true, true);
//                return deploy(tempFile, sha1, descriptor, repositoryId, session);
//            } finally {
//                tempFile.delete();
//            }
//        } catch (IOException e) {
//            throw new NutsIOException(e);
//        }
//    }
    public File copyTo(String id, File localPath, NutsSession session) {
        session = validateSession(session);
        return copyTo(CoreNutsUtils.parseOrErrorNutsId(id), session, localPath);
    }

    public NutsWorkspaceExtensionManager getExtensionManager() {
        return extensionManager;
    }

    public List<NutsRepository> getEnabledRepositories() {
        List<NutsRepository> repos = new ArrayList<>();
        for (NutsRepository repository : getRepositoryManager().getRepositories()) {
            if (isEnabledRepository(repository.getRepositoryId())) {
                repos.add(repository);
            }
        }
        Collections.sort(repos, new Comparator<NutsRepository>() {
            @Override
            public int compare(NutsRepository o1, NutsRepository o2) {
                return Integer.compare(o1.getConfigManager().getSpeed(), o2.getConfigManager().getSpeed());
            }
        });
        return repos;
    }

    @Override
    public int getSupportLevel(NutsBootWorkspace criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public int exec(File nutsJarFile, String[] args, boolean copyCurrentToFile, boolean waitFor, NutsSession session) {
        session = validateSession(session);
        NutsPrintStream out = resolveOut(session);
        if (copyCurrentToFile) {
            File acFile = getConfigManager().resolveNutsJarFile();
            if (nutsJarFile == null) {
                nutsJarFile = acFile;
            } else {
                if (acFile != null) {
                    if (!acFile.exists()) {
                        throw new NutsIllegalArgumentException("Could not apply update from non existing source " + acFile.getPath());
                    }
                    if (acFile.isDirectory()) {
                        throw new NutsIllegalArgumentException("Could not apply update from directory source " + acFile.getPath());
                    }
                    if (nutsJarFile.exists()) {
                        if (nutsJarFile.isDirectory()) {
                            throw new NutsIllegalArgumentException("Could not apply update on folder " + nutsJarFile);
                        }
                        if (nutsJarFile.exists()) {
                            int index = 1;
                            while (new File(nutsJarFile.getPath() + "." + index).exists()) {
                                index++;
                            }
                            out.printf("copying [[%s]] to [[%s.%s]]\n", nutsJarFile, nutsJarFile.getPath(), index);
                            try {
                                Files.copy(nutsJarFile.toPath(), new File(nutsJarFile.getPath() + "." + index).toPath());
                            } catch (IOException e) {
                                throw new NutsIOException(e);
                            }
                        }
                        out.printf("copying [[%s]] to [[%s]]\n", acFile.getPath(), nutsJarFile.getPath());
                        try {
                            Files.copy(acFile.toPath(), nutsJarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new NutsIOException(e);
                        }
                    } else if (nutsJarFile.getName().endsWith(".jar")) {
                        out.printf("copying [[%s]] to [[%s]]\n", acFile.getPath(), nutsJarFile.getPath());
                        try {
                            Files.copy(acFile.toPath(), nutsJarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new NutsIOException(e);
                        }
                    } else {
                        throw new NutsIllegalArgumentException("Could not apply update to target " + nutsJarFile + ". expected jar file name");
                    }
                } else {
                    throw new NutsIllegalArgumentException("Unable to resolve source to update from");
                }
                List<String> all = new ArrayList<>();
                for (int i = 0; i < args.length; i++) {
                    String arg = args[i];
                    switch (arg) {
                        case "--update":
                        case "--check-updates":
                            //do nothing...
                            break;
                        case "--apply-updates":
                            i++;
                            break;
                        default:
                            all.add(arg);
                            break;
                    }
                }
                out.printf("nuts patched ===successfully===...\n");
                if (all.size() > 0) {
                    out.printf("running command (%s) with newly patched version (%s)\n", all, nutsJarFile);
                    args = all.toArray(new String[all.size()]);
                }
            }
        }

        if (nutsJarFile == null) {
            nutsJarFile = getConfigManager().resolveNutsJarFile();
        }
        if (nutsJarFile == null) {
            throw new NutsIllegalArgumentException("Unable to locate nutsJarFile");
        }
        List<String> all = new ArrayList<>();
        all.add(System.getProperty("java.home") + "/bin/java");
        if (nutsJarFile.isDirectory()) {
            all.add("-classpath");
            all.add(nutsJarFile.getPath());
        } else {
            all.add("-jar");
            all.add(nutsJarFile.getPath());
        }
        all.addAll(Arrays.asList(args));
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(all);
        pb.inheritIO();
        Process process = null;
        try {
            process = pb.start();
            if (waitFor) {
                return process.waitFor();
            }
        } catch (IOException e) {
            throw new NutsIOException(e);
        } catch (InterruptedException e) {
            throw new NutsException(e);
        }
        return 0;
    }

    private NutsFetchMode[] resolveFetchModes(NutsFetchMode fetchMode) {
        return fetchMode == NutsFetchMode.ONLINE ? new NutsFetchMode[]{NutsFetchMode.OFFLINE, NutsFetchMode.REMOTE} : new NutsFetchMode[]{fetchMode};
    }

    private NutsPrintStream resolveOut(NutsSession session) {
        session = validateSession(session);
        return (session == null || session.getTerminal() == null) ? getExtensionManager().getFactory().createPrintStream(CoreIOUtils.NULL_OUTPUT_STREAM) : session.getTerminal().getOut();
    }

    protected NutsSession validateSession(NutsSession session) {
        if (session == null) {
            session = createSession();
        }
        return session;
    }

    protected void initializeWorkspace(String archetype, NutsSession session) {
        session = validateSession(session);
        if (CoreStringUtils.isEmpty(archetype)) {
            archetype = "default";
        }
        //should be here but the problem is that no repository is already
        //registered so where would we install extension from ?
//        try {
//            addWorkspaceExtension(NutsConstants.NUTS_CORE_ID, session);
//        }catch(Exception ex){
//            log.log(Level.SEVERE, "Unable to load Nuts-core. The tool is running in minimal mode.");
//        }

        NutsWorkspaceArchetypeComponent instance = getExtensionManager().getFactory().createSupported(NutsWorkspaceArchetypeComponent.class, self());
        if (instance == null) {
            //get the default implementation
            instance = new DefaultNutsWorkspaceArchetypeComponent();
        }

        //has all rights (by default)
        //no right nor group is needed for admin user
        getSecurityManager().setUserCredentials(NutsConstants.USER_ADMIN, "admin");

        instance.initialize(self(), session);

//        //isn't it too late for adding extensions?
//        try {
//            addWorkspaceExtension(NutsConstants.NUTS_ID_RUNTIME, session);
//        } catch (Exception ex) {
//            log.log(Level.SEVERE, "Unable to load Nuts-core. The tool is running in minimal mode.");
//        }
    }

    protected NutsInstallerComponent getInstaller(NutsFile nutToInstall, NutsSession session) {
        session = validateSession(session);
        if (nutToInstall != null && nutToInstall.getFile() != null) {
            NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsExecutorDescriptor installerDescriptor = descriptor.getInstaller();
            NutsFile runnerFile = nutToInstall;
            if (installerDescriptor != null && installerDescriptor.getArgs() != null && installerDescriptor.getArgs().length > 0) {
                if (installerDescriptor.getId() != null) {
                    runnerFile = fetchWithDependencies(installerDescriptor.getId().toString(), session.copy().setTransitive(false));
                }
            }
            if (runnerFile == null) {
                runnerFile = nutToInstall;
            }
            NutsInstallerComponent best = getExtensionManager().getFactory().createSupported(NutsInstallerComponent.class, runnerFile);
            if (best != null) {
                return best;
            }
        }
        return null;
    }

    protected boolean isInstalled(NutsFile nutToInstall, NutsSession session) {
        session = validateSession(session);
        if (!getSecurityManager().isAllowed(NutsConstants.RIGHT_FETCH_DESC)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_FETCH_DESC);
        }
        NutsInstallerComponent ii = getInstaller(nutToInstall, session);
        if (ii == null) {
            return true;
        }
        return ii.isInstalled(nutToInstall, self(), session);
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsId nutsId) {
        for (NutsExecutorComponent nutsExecutorComponent : getExtensionManager().getFactory().createAll(NutsExecutorComponent.class)) {
            if (nutsExecutorComponent.getId().isSameFullName(nutsId)) {
                return nutsExecutorComponent;
            }
        }
        return new CustomNutsExecutorComponent(nutsId);
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsFile nutsFile) {
        NutsExecutorComponent executorComponent = getExtensionManager().getFactory().createSupported(NutsExecutorComponent.class, nutsFile);
        if (executorComponent != null) {
            return executorComponent;
        }
        throw new NutsNotFoundException("Nuts Executor not found for " + nutsFile);
    }

    protected int exec(NutsId nutsId, String[] args, Properties env, NutsSession session) {
        session = validateSession(session);
        NutsFile nutToRun = fetchWithDependencies(nutsId.toString(), session);
        //load all needed dependencies!
        fetchDependencies(new NutsDependencySearch(nutToRun.getId()), session);
        return exec(nutToRun, args, env, session);
    }

    protected int exec(NutsFile nutToRun, String[] appArgs, Properties env, NutsSession session) {
        session = validateSession(session);
        if (nutToRun != null && nutToRun.getFile() != null) {
            NutsDescriptor descriptor = nutToRun.getDescriptor();
            if (!descriptor.isExecutable()) {
//                session.getTerminal().getErr().println(nutToRun.getId()+" is not executable... will perform extra checks.");
//                throw new NutsNotExecutableException(descriptor.getId());
            }
            NutsExecutorDescriptor executor = descriptor.getExecutor();
            NutsExecutorComponent execComponent = null;
            String[] executrorArgs = null;
            Properties execProps = null;
            if (executor == null) {
                execComponent = resolveNutsExecutorComponent(nutToRun);
            } else {
                if (executor.getId() == null) {
                    execComponent = resolveNutsExecutorComponent(nutToRun);
                } else {
                    execComponent = resolveNutsExecutorComponent(executor.getId());
//                    NutsFile runnerFile = fetch(executor.getId(), session, true);
//                    execComponent = resolveNutsExecutorComponent(runnerFile);
                }
                executrorArgs = executor.getArgs();
                execProps = executor.getProperties();
            }
            boolean nowait = false;
            if (appArgs.length > 0 && "&".equals(appArgs[appArgs.length - 1])) {
                String[] arg2 = new String[appArgs.length - 1];
                System.arraycopy(appArgs, 0, arg2, 0, arg2.length);
                appArgs = arg2;
                nowait = true;
            }
            if (appArgs.length > 0 && ">null".equals(appArgs[appArgs.length - 1])) {
                String[] arg2 = new String[appArgs.length - 1];
                System.arraycopy(appArgs, 0, arg2, 0, arg2.length);
                appArgs = arg2;
                session = session.copy();
                NutsPrintStream nostream = getExtensionManager().getFactory().createPrintStream(CoreIOUtils.NULL_PRINT_STREAM);
                NutsTerminal t = getExtensionManager().getFactory().createTerminal(null, nostream, nostream);
                session.setTerminal(t);
            }
            final NutsExecutionContext executionContext = new NutsExecutionContextImpl(nutToRun, appArgs, executrorArgs, env, execProps, session, self(), nutToRun.getDescriptor().getExecutor());
            if (nowait) {
                final NutsExecutorComponent finalExecComponent = execComponent;
                Thread thread = new Thread("Exec-" + nutToRun.getId().toString()) {
                    @Override
                    public void run() {
                        try {
                            int result = finalExecComponent.exec(executionContext);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.setDaemon(true);
                thread.start();
                return 0;
            } else {
                return execComponent.exec(executionContext);
            }
        }
        throw new NutsNotFoundException("Nuts not found " + nutToRun);
    }

    protected NutsDependencyFilter createNutsDependencyFilter(NutsDependencyFilter filter, NutsId[] exclusions) {
        if (exclusions == null || exclusions.length == 0) {
            return filter;
        }
        return new NutsExclusionDependencyFilter(filter, exclusions);
    }

    protected NutsId deploy(File contentFile, NutsDescriptor descriptor, String repositoryId, boolean force, NutsSession session) {
        session = validateSession(session);
        File tempFile = null;
        try {
            if (contentFile.isDirectory()) {
                File descFile = new File(contentFile, NutsConstants.NUTS_DESC_FILE_NAME);
                NutsDescriptor descriptor2;
                if (descFile.exists()) {
                    descriptor2 = CoreNutsUtils.parseNutsDescriptor(descFile);
                } else {
                    descriptor2 = resolveNutsDescriptorFromFileContent(CoreIOUtils.createInputStreamSource(contentFile, getConfigManager().getCwd()), session);
                }
                if (descriptor == null) {
                    descriptor = descriptor2;
                } else {
                    if (descriptor2 != null && !descriptor2.equals(descriptor)) {
                        descriptor.write(descFile);
                    }
                }
                if (descriptor != null) {
                    if ("zip".equals(descriptor.getExt())) {
                        File zipFilePath = CoreIOUtils.createFileByCwd(contentFile.getPath() + ".zip", getConfigManager().getCwd());
                        CoreIOUtils.zip(contentFile, zipFilePath);
                        contentFile = zipFilePath;
                        tempFile = contentFile;
                    } else {
                        throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                    }
                }
            } else {
                if (descriptor == null) {
                    descriptor = resolveNutsDescriptorFromFileContent(CoreIOUtils.createInputStreamSource(contentFile, getConfigManager().getCwd()), session);
                }
            }
            if (descriptor == null) {
                throw new NutsNotFoundException(" at " + contentFile);
            }
            if (CoreStringUtils.isEmpty(descriptor.getExt())) {
                int r = contentFile.getName().lastIndexOf(".");
                if (r >= 0) {
                    descriptor = descriptor.setExt(contentFile.getName().substring(r + 1));
                }
            }
            //remove workspace
            descriptor = descriptor.setId(descriptor.getId().setNamespace(null));
            if (CoreStringUtils.trim(descriptor.getId().getVersion().getValue()).endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
                throw new NutsIllegalArgumentException("Invalid Version " + descriptor.getId().getVersion());
            }

            NutsSession transitiveSession = session.copy().setTransitive(true);

            NutsId effId = resolveEffectiveId(descriptor, transitiveSession);
            for (String os : descriptor.getOs()) {
                CorePlatformUtils.checkSupportedOs(CoreNutsUtils.parseOrErrorNutsId(os).getFullName());
            }
            for (String arch : descriptor.getArch()) {
                CorePlatformUtils.checkSupportedArch(CoreNutsUtils.parseOrErrorNutsId(arch).getFullName());
            }
            if (CoreStringUtils.isEmpty(repositoryId)) {
                class NutsRepositoryInfo implements Comparable<NutsRepositoryInfo> {

                    NutsRepository repo;
                    int supportLevel;
                    int deployOrder;

                    @Override
                    public int compareTo(NutsRepositoryInfo o) {
                        int x = Integer.compare(o.deployOrder, this.deployOrder);
                        if (x != 0) {
                            return x;
                        }
                        x = Integer.compare(o.supportLevel, this.supportLevel);
                        if (x != 0) {
                            return x;
                        }
                        return 0;
                    }
                }
                List<NutsRepositoryInfo> possible = new ArrayList<>();
                for (NutsRepository repo : getEnabledRepositories(effId, session)) {
                    int t = 0;
                    try {
                        t = repo.getSupportLevel(effId, session);
                    } catch (Exception e) {
                        //ignore...
                    }
                    if (t > 0) {
                        NutsRepositoryInfo e = new NutsRepositoryInfo();
                        e.repo = repo;
                        e.supportLevel = t;
                        e.deployOrder = CoreStringUtils.parseInt(repo.getConfigManager().getEnv(NutsConstants.ENV_KEY_DEPLOY_PRIORITY, "0", false), 0);
                        possible.add(e);
                    }
                }
                if (possible.size() > 0) {
                    Collections.sort(possible);
                    return possible.get(0).repo.deploy(effId, descriptor, contentFile, force, session);
                }
            } else {
                NutsRepository goodRepo = getEnabledRepositoryOrError(repositoryId);
                if (goodRepo == null) {
                    throw new NutsRepositoryNotFoundException(repositoryId);
                }
                return goodRepo.deploy(effId, descriptor, contentFile, force, session);
            }
            throw new NutsRepositoryNotFoundException(repositoryId);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    protected File copyTo(NutsId id, NutsSession session, File localPath) {
        session = validateSession(session);
        id = resolveId(id, session);
//        id = configureFetchEnv(id);
        Set<String> errors = new LinkedHashSet<>();
        NutsSession transitiveSession = session.copy().setTransitive(true);
        for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
            NutsSession session2 = session.copy().setFetchMode(mode);
            for (NutsRepository repo : getEnabledRepositories(id, transitiveSession)) {
                try {
                    File fetched = null;
                    try {
                        fetched = repo.copyTo(id, session2, localPath);
                    } catch (SecurityException ex) {
                        //ignore
                    }
                    if (fetched != null) {
                        return fetched;
                    } else {
                        errors.add(CoreStringUtils.exceptionToString(new NutsNotFoundException(id.toString())));
                    }
                } catch (Exception ex) {
                    errors.add(CoreStringUtils.exceptionToString(ex));
                }
            }
        }
        throw new NutsNotFoundException(id.toString(), CoreStringUtils.join("\n", errors), null);
    }

    protected void checkNutsId(NutsId id, String right) {
        if (id == null) {
            throw new NutsIllegalArgumentException("Missing id");
        }
        if (!getSecurityManager().isAllowed(right)) {
            throw new NutsSecurityException("Not Allowed " + right);
        }
        if (CoreStringUtils.isEmpty(id.getGroup())) {
            throw new NutsIllegalArgumentException("Missing group");
        }
        if (CoreStringUtils.isEmpty(id.getName())) {
            throw new NutsIllegalArgumentException("Missing name");
        }
    }

    protected NutsRepository getEnabledRepositoryOrError(String repoId) {
        NutsRepository r = getRepositoryManager().findRepository(repoId);
        if (r != null) {
            if (!isEnabledRepository(repoId)) {
                throw new NutsNotFoundException("Repository " + repoId + " is disabled.");
            }
        }
        return r;
    }

    protected boolean isEnabledRepository(String repoId) {
        NutsRepositoryLocation repository = getConfigManager().getConfig().getRepository(repoId);
        return repository != null && repository.isEnabled();
    }

    protected void checkEnabled(String repoId) {
        if (!isEnabledRepository(repoId)) {
            throw new NutsIllegalArgumentException("Repository " + repoId + " is disabled");
        }
    }

    private NutsDescriptor resolveNutsDescriptorFromFileContent(InputStreamSource localPath, NutsSession session) {
        session = validateSession(session);
        if (localPath != null) {
            List<NutsDescriptorContentParserComponent> allParsers = getExtensionManager().getFactory().createAllSupported(NutsDescriptorContentParserComponent.class, self());
            if (allParsers.size() > 0) {
                String fileExtension = CoreIOUtils.getFileExtension(localPath.getName());
                NutsDescriptorContentParserContext ctx = new DefaultNutsDescriptorContentParserContext(self(), session, localPath, fileExtension, null, null);
                for (NutsDescriptorContentParserComponent parser : allParsers) {
                    NutsDescriptor desc = null;
                    try {
                        desc = parser.parse(ctx);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (desc != null) {
                        return desc;
                    }
                }
            }
        }
        return null;
    }

    private String resolveWorkspacePath(String workspace) {
        if (CoreStringUtils.isEmpty(workspace)) {
            File file = CoreIOUtils.resolvePath(getConfigManager().getNutsHomeLocation() + "/" + NutsConstants.DEFAULT_WORKSPACE_NAME, null, getConfigManager().getNutsHomeLocation());
            workspace = file == null ? null : file.getPath();
        } else {
            File file = CoreIOUtils.resolvePath(workspace, null, getConfigManager().getNutsHomeLocation());
            workspace = file == null ? null : file.getPath();
        }

        Set<String> visited = new HashSet<String>();
        while (true) {
            File file = CoreIOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
            NutsWorkspaceConfig nutsWorkspaceConfig = CoreJsonUtils.get(this).loadJson(file, NutsWorkspaceConfigImpl.class);
            if (nutsWorkspaceConfig != null) {
                String nextWorkspace = nutsWorkspaceConfig.getWorkspace();
                if (nextWorkspace != null && nextWorkspace.trim().length() > 0) {
                    if (visited.contains(nextWorkspace)) {
                        throw new NutsException("Circular Workspace Dependency : " + nextWorkspace);
                    }
                    visited.add(nextWorkspace);
                    workspace = nextWorkspace;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return workspace;
    }

    private boolean isWorkspaceFolder(String workspace) {
        workspace = resolveWorkspacePath(workspace);
        File file = CoreIOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
        if (file.isFile() && file.exists()) {
            return true;
        }
        return false;
    }

    private boolean isInstallable(NutsFile nutToInstall, NutsSession session) {
        session = validateSession(session);
        if (nutToInstall != null && nutToInstall.getFile() != null) {
            NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsExecutorDescriptor installer = descriptor.getInstaller();
            NutsInstallerComponent nutsInstallerComponent = getInstaller(nutToInstall, session);
            if (nutsInstallerComponent == null) {
                return false;
            }
            return true;
        }
        return false;
    }

    private void postInstall(NutsFile nutToInstall, NutsSession session) {
        session = validateSession(session);
        if (nutToInstall != null && nutToInstall.getFile() != null) {
            NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsExecutorDescriptor installer = descriptor.getInstaller();
            NutsInstallerComponent nutsInstallerComponent = getInstaller(nutToInstall, session);
            if (nutsInstallerComponent == null) {
                return;
            }
            String[] args = null;
            Properties props = null;
            if (installer != null) {
                args = installer.getArgs();
                props = installer.getProperties();
            }
            Properties env = new Properties();
            NutsExecutionContext executionContext = new NutsExecutionContextImpl(nutToInstall, new String[0], args, env, props, session, self());
            if (!nutsInstallerComponent.isInstalled(executionContext)) {
                nutsInstallerComponent.install(executionContext);
            }
        }
    }

    private NutsId toCanonicalForm(NutsId id) {
        if (id != null) {
            id = id.setNamespace(null);
            if ("default".equals(id.getQueryMap().get("face"))) {
                id = id.setQueryProperty("face", null);
            }
        }
        return id;
    }

    private NutsFile[] bootstrapUpdate(String id, boolean force, NutsSession session) {
        session = validateSession(session);
        NutsFile[] deps = fetchDependencies(new NutsDependencySearch(id).setIncludeMain(true), session);
        for (NutsFile dep : deps) {
            if (dep.getFile() != null && !NutsConstants.DEFAULT_REPOSITORY_NAME.equals(dep.getId().getNamespace())) {
                bootstrapNutsRepository.deploy(dep.getId(),
                        dep.getDescriptor(),
                        dep.getFile(), force,
                        session
                );
            }
        }
        return deps;
    }

    private NutsFile fetchSimple(NutsId id, NutsSession session) {

        LinkedHashSet<String> errors = new LinkedHashSet<>();
        NutsFile main = null;
        NutsId goodId = resolveId(id, session);

        String ns = goodId.getNamespace();
        if (!CoreStringUtils.isEmpty(ns)) {
            for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
                if (main != null) {
                    break;
                }
                NutsSession session2 = session.copy().setFetchMode(mode);
                try {
                    List<NutsRepository> enabledRepositories = new ArrayList<>();
                    if (!CoreStringUtils.isEmpty(ns)) {
                        try {
                            NutsRepository repository = getRepositoryManager().findRepository(ns);
                            if (repository != null) {
                                enabledRepositories.add(repository);
                            }
                        } catch (NutsRepositoryNotFoundException ex) {
                            //
                        }
                    }
                    main = fetchHelperNutsFile(goodId, errors, session2, enabledRepositories);
                } catch (NutsNotFoundException ex) {
                    //
                }
            }
        }

        //try to load component from all repositories
        if (main == null) {
            for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
                if (main != null) {
                    break;
                }
                NutsSession session2 = session.copy().setFetchMode(mode);
                main = fetchHelperNutsFile(goodId, errors, session2, getEnabledRepositories(id, session2.copy().setTransitive(true)));
            }
        }
        if (main == null) {
            throw new NutsNotFoundException(id.toString(), CoreStringUtils.join("\n", errors), null);
        }
        return main;
    }

    private NutsFile[] fetchDependencies(NutsDependencySearch search, NutsIdGraph graph, NutsSession session) {
        session = validateSession(session);
        NutsDependencyFilter dependencyFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(
                search.getScope() == NutsDependencyScope.ALL ? null
                        : search.getScope() == NutsDependencyScope.RUN ? CoreNutsUtils.SCOPE_RUN
                        : search.getScope() == NutsDependencyScope.TEST ? CoreNutsUtils.SCOPE_TEST
                        : search.getScope() == NutsDependencyScope.ALL ? null : CoreNutsUtils.SCOPE_RUN,
                CoreNutsUtils.createNutsDependencyFilter(search.getDependencyFilter())
        ));

        Set<NutsId> mains = new HashSet<>();
        Stack<NutsFileAndNutsDependencyFilterItem> stack = new Stack<>();
        for (String sid : search.getIds()) {
            NutsId id = getExtensionManager().getFactory().parseNutsId(sid);
            LinkedHashSet<String> errors = new LinkedHashSet<>();
            NutsFile main = null;
            try {
                main = fetchSimple(id, session);
            } catch (NutsNotFoundException ex) {
                if (search.isTrackNotFound()) {
                    search.getNoFoundResult().add(getExtensionManager().getFactory().parseNutsId(ex.getNuts()));
                } else {
                    throw ex;
                }
            }

            //try to load component from all repositories
            if (main == null) {
                for (NutsFetchMode mode : resolveFetchModes(session.getFetchMode())) {
                    if (main != null) {
                        break;
                    }
                    NutsSession session2 = session.copy().setFetchMode(mode);
                    main = fetchHelperNutsFile(id, errors, session2, getEnabledRepositories(id, session2.copy().setTransitive(true)));
                }
            }
            if (main == null) {
                throw new NutsNotFoundException(id.toString(), CoreStringUtils.join("\n", errors), null);
            }
            mains.add(main.getId());
            stack.push(new NutsFileAndNutsDependencyFilterItem(main, dependencyFilter));
        }

        while (!stack.isEmpty()) {
            NutsFileAndNutsDependencyFilterItem curr = stack.pop();
            if (!graph.contains(curr.file.getId())) {
                if (curr.file.getDescriptor() != null) {
                    graph.set(curr.file);
                    for (NutsDependency dept : resolveEffectiveDescriptor(curr.file.getDescriptor(), session).getDependencies()) {
                        NutsId[] exclusions = dept.getExclusions();
                        if (dependencyFilter == null || dependencyFilter.accept(dept)) {
                            NutsId item = dept.toId();
                            //if (!graph.contains(curr.file.getId())) {
                            try {
                                NutsFile itemFile = fetchSimple(item, session);
                                graph.add(curr.file, itemFile);
                                if (!graph.contains(itemFile.getId())) {
                                    stack.push(new NutsFileAndNutsDependencyFilterItem(itemFile, createNutsDependencyFilter(curr.filter, exclusions)));
                                }
                            } catch (NutsNotFoundException ex) {
                                if (dept.isOptional()) {
                                    //ignore
                                } else if (!graph.contains(item)) {
                                    stack.push(new NutsFileAndNutsDependencyFilterItem(new NutsFile(item, null, null, false, false, null), createNutsDependencyFilter(curr.filter, exclusions)));
                                }
                            }
                            //}
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, Set<NutsId>> conflict : graph.resolveConflicts().entrySet()) {
            NutsVersion v = null;
            for (NutsId n : conflict.getValue()) {
                if (v == null || n.getVersion().compareTo(v) > 0) {
                    v = n.getVersion();
                }
            }
            for (NutsId n : conflict.getValue()) {
                if (!n.getVersion().equals(v)) {
                    graph.remove(n);
                }
            }
        }
        ArrayList<NutsFile> collected = new ArrayList<>();
        for (NutsId main : mains) {
            graph.visit(main, collected);
        }
        if (!search.isIncludeMain()) {
            Iterator<NutsFile> it = collected.iterator();
            while (it.hasNext()) {
                NutsFile next = it.next();
                if (mains.contains(next.getId())) {
                    it.remove();
                    break;
                }
            }
        }
        return collected.toArray(new NutsFile[collected.size()]);
    }

    private NutsFile fetchHelperNutsFile(NutsId id, LinkedHashSet<String> errors, NutsSession session2, List<NutsRepository> enabledRepositories) {
        NutsFile found = null;
        try {
            for (NutsRepository repo : enabledRepositories) {
                NutsFile fetch = null;
                try {
                    fetch = repo.fetch(id, session2);
                } catch (Exception ex) {
                    errors.add(CoreStringUtils.exceptionToString(ex));
                }
                if (fetch != null) {
                    if (CoreStringUtils.isEmpty(fetch.getId().getNamespace())) {
                        fetch.setId(fetch.getId().setNamespace(repo.getRepositoryId()));
                    }
                    found = fetch;
                    break;
                }
            }
        } catch (NutsNotFoundException ex) {
            //
        }
        return found;
    }

    private CharacterizedFile characterize(InputStreamSource contentFile, NutsSession session) {
        session = validateSession(session);
        CharacterizedFile c = new CharacterizedFile();
        c.contentFile = contentFile;
        if (c.contentFile.getSource() instanceof File) {
            //okkay
        } else {
            File temp = CoreIOUtils.createTempFile(contentFile.getName(), false, null);
            CoreIOUtils.copy(contentFile.openStream(), temp, true, true);
            c.contentFile = CoreIOUtils.createInputStreamSource(temp, getConfigManager().getCwd());
            c.addTemp(temp);
            return characterize(CoreIOUtils.createInputStreamSource(temp, getConfigManager().getCwd()), session);
        }
        File fileSource = (File) c.contentFile.getSource();
        if ((!fileSource.exists())) {
            throw new NutsIllegalArgumentException("File does not exists " + c.contentFile);
        }
        if (fileSource.isDirectory()) {
            File ext = new File(fileSource, NutsConstants.NUTS_DESC_FILE_NAME);
            if (ext.exists()) {
                c.descriptor = CoreNutsUtils.parseNutsDescriptor(ext);
            } else {
                c.descriptor = resolveNutsDescriptorFromFileContent(c.contentFile, session);
            }
            if (c.descriptor != null) {
                if ("zip".equals(c.descriptor.getExt())) {
                    File zipFilePath = CoreIOUtils.createFileByCwd(fileSource.getPath() + ".zip", getConfigManager().getCwd());
                    CoreIOUtils.zip(fileSource, zipFilePath);
                    c.contentFile = CoreIOUtils.createInputStreamSource(zipFilePath, getConfigManager().getCwd());
                    c.addTemp(zipFilePath);
                } else {
                    throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                }
            }
        } else if (fileSource.isFile()) {
            File ext = CoreIOUtils.createFileByCwd(fileSource.getPath() + "." + NutsConstants.NUTS_DESC_FILE_NAME, getConfigManager().getCwd());
            if (ext.exists()) {
                c.descriptor = CoreNutsUtils.parseNutsDescriptor(ext);
            } else {
                c.descriptor = resolveNutsDescriptorFromFileContent(c.contentFile, session);
            }
        } else {
            throw new NutsIllegalArgumentException("Path does not denote a valid file or folder " + c.contentFile);
        }

        return c;
    }

    protected boolean reloadWorkspace(boolean save, NutsSession session, Set<String> excludedExtensions, Set<String> excludedRepositories) {
        session = validateSession(session);
        File file = CoreIOUtils.createFile(getConfigManager().getWorkspaceLocation(), NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
        NutsWorkspaceConfig config = CoreJsonUtils.get(this).loadJson(file, NutsWorkspaceConfigImpl.class);
        if (config != null) {
            repositoryManager.removeAllRepositories();
            configManager.setConfig(config);

            //extensions already wired... this is needless!
            for (String extensionId : config.getExtensions()) {
                if (excludedExtensions != null && CoreNutsUtils.finNutsIdByFullNameInStrings(CoreNutsUtils.parseOrErrorNutsId(extensionId), excludedExtensions) != null) {
                    continue;
                }
                NutsSession sessionCopy = session.copy().setTransitive(true).setFetchMode(NutsFetchMode.ONLINE);
                extensionManager.wireExtension(CoreNutsUtils.parseOrErrorNutsId(extensionId), sessionCopy);
                if (sessionCopy.getTerminal() != session.getTerminal()) {
                    session.setTerminal(sessionCopy.getTerminal());
                }
            }

            for (NutsRepositoryLocation repositoryConfig : config.getRepositories()) {
                if (excludedRepositories != null && excludedRepositories.contains(repositoryConfig.getId())) {
                    continue;
                }
                repositoryManager.openRepository(repositoryConfig.getId(), repositoryConfig.getLocation(), repositoryConfig.getType(), repositoryManager.getRepositoriesRoot(), true);
            }

            NutsSecurityEntityConfig adminSecurity = getConfigManager().getConfig().getSecurity(NutsConstants.USER_ADMIN);
            if (adminSecurity == null || CoreStringUtils.isEmpty(adminSecurity.getCredentials())) {
                log.log(Level.CONFIG, NutsConstants.USER_ADMIN + " user has no credentials. reset to default");
                getSecurityManager().setUserCredentials(NutsConstants.USER_ADMIN, "admin");
                if (save) {
                    getConfigManager().save();
                }
            }
            for (NutsWorkspaceListener listener : workspaceListeners) {
                listener.onReloadWorkspace(self());
            }
            return true;
        }
        return false;
    }

    public List<NutsRepository> getEnabledRepositories(NutsId nutsId, NutsSession session) {
        session = validateSession(session);
        return NutsWorkspaceHelper.filterRepositories(getEnabledRepositories(), nutsId, session);
    }

    public void checkSupportedRepositoryType(String type) {
        if (!getRepositoryManager().isSupportedRepositoryType(type)) {
            throw new NutsIllegalArgumentException("Unsupported repository type " + type);
        }
    }

    protected class NutsFileAndNutsDependencyFilterItem {

        NutsFile file;
        NutsDependencyFilter filter;

        public NutsFileAndNutsDependencyFilterItem(NutsFile file, NutsDependencyFilter filter) {
            this.file = file;
            this.filter = filter;
        }
    }

    public boolean isFetched(NutsId id, NutsSession session) {
        session = validateSession(session);
        NutsSession offlineSession = session.copy().setFetchMode(NutsFetchMode.OFFLINE);
        try {
            NutsFile found = fetch(id.toString(), offlineSession);
            return found != null;
        } catch (Exception e) {
            return false;
        }
    }

    private NutsWorkspace _self;

    @Override
    public NutsWorkspace self() {
        if (_self == null) {
            _self = (NutsWorkspace) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{
                    NutsWorkspace.class,
                    NutsWorkspaceImpl.class
            }, NutsEnvironmentContext.createHandler((NutsWorkspace) this));
        }
        return _self;
    }

    @Override
    public File getStoreRoot(String id) {
        return getStoreRoot(getExtensionManager().getFactory().parseNutsId(id));
    }

    public File getStoreRoot(NutsId id) {
        if (CoreStringUtils.isEmpty(id.getGroup())) {
            throw new NutsElementNotFoundException("Missing group for " + id);
        }
        File groupFolder = new File(getStoreRoot(), id.getGroup().replaceAll("\\.", File.separator));
        if (CoreStringUtils.isEmpty(id.getName())) {
            throw new NutsElementNotFoundException("Missing name for " + id.toString());
        }
        File artifactFolder = new File(groupFolder, id.getName());
        if (id.getVersion().isEmpty()) {
            throw new NutsElementNotFoundException("Missing version for " + id.toString());
        }
        return new File(artifactFolder, id.getVersion().getValue());
    }

    public File getStoreRoot() {
        final String wloc = resolveWorkspacePath(getConfigManager().getWorkspaceLocation());
        //should look into config?
        return new File(wloc, NutsConstants.FOLDER_NAME_COMPONENTS);
    }

    @Override
    public String toString() {
        return "NutsWorkspace{"
                + configManager
                + '}';
    }

    @Override
    public NutsId parseNutsId(String id) {
        return getExtensionManager().getFactory().parseNutsId(id);
    }

    @Override
    public NutsId resolveNutsIdForClass(Class clazz) {
        String u = ArtifactUtils.resolveArtifact(clazz, null);
        if (u == null) {
            return null;
        }
        return getExtensionManager().getFactory().parseNutsId(u);
    }

    @Override
    public NutsId[] resolveNutsIdsForClass(Class clazz) {
        String[] u = ArtifactUtils.resolveArtifacts(clazz);
        NutsId[] all = new NutsId[u.length];
        for (int i = 0; i < all.length; i++) {
            all[i] = parseNutsId(u[i]);
        }
        return all;
    }

}
