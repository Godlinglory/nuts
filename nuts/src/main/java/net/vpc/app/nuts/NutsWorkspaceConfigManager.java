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
package net.vpc.app.nuts;

import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author vpc
 */
public interface NutsWorkspaceConfigManager extends NutsEnvProvider {

    /**
     * boot time context information of loaded and running context
     *
     * @return
     */
    NutsBootContext getRunningContext();

    /**
     * boot time context information of requested context (from config)
     *
     * @return
     */
    NutsBootContext getBootContext();

    /**
     * current context information of requested context (from config) now
     *
     * @return
     */
    NutsBootContext getConfigContext();

    /**
     * current context information of requested context (from config)
     *
     * @return
     */
    NutsBootConfig getBootConfig();

    boolean isValidWorkspaceFolder();

    String getWorkspaceLocation();

    boolean isReadOnly();

    void setEnv(String property, String value);

    Map<String, String> getRuntimeProperties();

    String resolveNutsJarFile();

    void addImports(String... importExpression);

    void removeAllImports();

    void removeImports(String... importExpression);

    void setImports(String[] imports);

    String[] getImports();

    NutsId[] getExtensions();

    void setRepositoryEnabled(String repoId, boolean enabled);

    boolean addExtension(NutsId extensionId);

    boolean removeExtension(NutsId extensionId);

    boolean updateExtension(NutsId extensionId);

    /**
     * save config file if force is activated or non read only and some changes
     * was detected in config file
     *
     * @param force when true, save will always be performed
     * @return
     */
    boolean save(boolean force);

    void save();

    URL[] getBootClassWorldURLs();

    NutsUserConfig getUser(String userId);

    NutsUserConfig[] getUsers();

    void setUser(NutsUserConfig config);

    boolean isSecure();

    void setSecure(boolean secure);

    void addRepository(NutsRepositoryRef repository);

    void removeRepository(String repositoryName);

    NutsRepositoryRef getRepository(String repositoryName);

    boolean containsExtension(NutsId extensionId);

    void removeUser(String userId);

    void setUsers(NutsUserConfig[] users);

    boolean addSdk(String name, NutsSdkLocation location);

    NutsSdkLocation findSdkByName(String name, String locationName);

    NutsSdkLocation findSdkByPath(String name, String path);

    NutsSdkLocation findSdkByVersion(String name, String version);

    NutsSdkLocation removeSdk(String name, NutsSdkLocation location);

    NutsSdkLocation findSdk(String name, NutsSdkLocation location);

    void setBootConfig(NutsBootConfig other);

    String[] getSdkTypes();

    NutsSdkLocation getSdk(String type, String requestedVersion);

    NutsSdkLocation[] getSdks(String type);

    void setLogLevel(Level levek);

    NutsSdkLocation[] searchJdkLocations(PrintStream out);

    NutsSdkLocation[] searchJdkLocations(String path, PrintStream out);

    NutsSdkLocation resolveJdkLocation(String path);

    NutsWorkspaceOptions getOptions();

    byte[] decryptString(byte[] input);

    byte[] encryptString(byte[] input);

    void installCommandFactory(NutsWorkspaceCommandFactoryConfig commandFactory, NutsSession session);

    boolean uninstallCommandFactory(String name, NutsSession session);

    boolean installCommand(NutsWorkspaceCommandConfig command, NutsInstallOptions options, NutsSession session);

    boolean uninstallCommand(String name, NutsUninstallOptions options, NutsSession session);

    NutsWorkspaceCommand findCommand(String name);

    NutsWorkspaceCommand findEmbeddedCommand(String name);

    List<NutsWorkspaceCommand> findCommands();

    List<NutsWorkspaceCommand> findCommands(NutsId id);

    String getHomeLocation(NutsStoreLocation folderType);

    String getStoreLocation(NutsStoreLocation folderType);

    void setStoreLocation(NutsStoreLocation folderType, String location);

    void setStoreLocationStrategy(NutsStoreLocationStrategy strategy);

    NutsStoreLocationStrategy getStoreLocationStrategy();

    NutsStoreLocationStrategy getRepositoryStoreLocationStrategy();

    void setStoreLocationLayout(NutsStoreLocationLayout layout);

    NutsStoreLocationLayout getStoreLocationLayout();

    String getStoreLocation(String id, NutsStoreLocation folderType);

    String getStoreLocation(NutsId id, NutsStoreLocation folderType);

    NutsOsFamily getPlatformOsFamily();

    NutsId getPlatformOs();

    NutsId getPlatformOsDist();

    NutsId getPlatformArch();

    String getPlatformOsHome(NutsStoreLocation location);

    long getCreationStartTimeMillis();

    long getCreationFinishTimeMillis();

    long getCreationTimeMillis();

    NutsAuthenticationAgent createAuthenticationAgent(String authenticationAgent);

    String getDefaultIdFilename(NutsId id);

    String getDefaultIdExtension(NutsId id);

    NutsId createComponentFaceId(NutsId id, NutsDescriptor desc);

    String getUuid();

    ClassLoader getBootClassLoader();

    NutsWorkspaceCommandFactoryConfig[] getCommandFactories();

    NutsRepositoryRef[] getRepositories();

    String getDefaultIdComponentExtension(String packaging);

    NutsWorkspaceListManager createWorkspaceListManager(String name);

    void setHomeLocation(NutsStoreLocationLayout layout, NutsStoreLocation folderType, String location);

    NutsId getApiId();

    NutsId getRuntimeId();
}
