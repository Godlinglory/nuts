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
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author vpc
 * @since 0.5.4
 */
public interface NutsWorkspaceConfigManager extends NutsEnvProvider {

    String getUuid();

    /**
     * context information for the context type
     *
     * @param contextType
     * @return
     */
    NutsBootContext getContext(NutsBootContextType contextType);

    ClassLoader getBootClassLoader();

    URL[] getBootClassWorldURLs();

//    Path getBootNutsJar();
//
    /**
     * return a copy of workspace boot configuration
     *
     * @return a copy of workspace boot configuration
     */
    NutsBootConfig getBootConfig();

    /**
     * update workspace boot configuration
     *
     * @param other
     */
    void setBootConfig(NutsBootConfig other);

    boolean isValidWorkspaceFolder();

    Path getWorkspaceLocation();

    boolean isReadOnly();

    void setEnv(String property, String value);

    void addImports(String... importExpression);

    void removeAllImports();

    void removeImports(String... importExpression);

    void setImports(String[] imports);

    Set<String> getImports();

    /**
     * save config file if force is activated or non read only and some changes
     * was detected in config file
     *
     * @param force when true, save will always be performed
     * @return
     */
    boolean save(boolean force);

    void save();

    void setLogLevel(Level levek);

    String[] getSdkTypes();

    boolean addSdk(NutsSdkLocation location, NutsAddOptions options);

    NutsSdkLocation removeSdk(NutsSdkLocation location, NutsRemoveOptions options);

    NutsSdkLocation findSdkByName(String sdkType, String locationName);

    NutsSdkLocation findSdkByPath(String sdkType, Path path);

    NutsSdkLocation findSdkByVersion(String sdkType, String version);

    NutsSdkLocation findSdk(String sdkType, NutsSdkLocation location);

    NutsSdkLocation getSdk(String sdkType, String requestedVersion);

    NutsSdkLocation[] getSdks(String sdkType);

    NutsSdkLocation[] searchSdkLocations(String sdkType, PrintStream out);

    NutsSdkLocation[] searchSdkLocations(String sdkType, Path path, PrintStream out);

    /**
     * verify if the path is a valid a
     *
     * @param sdkType
     * @param path
     * @return null if not a valid jdk path
     */
    NutsSdkLocation resolveSdkLocation(String sdkType, Path path);

    NutsWorkspaceOptions getOptions();

    char[] decryptString(char[] input);

    byte[] decryptString(byte[] input);

    char[] encryptString(char[] input);

    byte[] encryptString(byte[] input);

    void addCommandAliasFactory(NutsCommandAliasFactoryConfig commandFactory, NutsAddOptions options);

    boolean removeCommandAliasFactory(String name, NutsRemoveOptions options);

    boolean addCommandAlias(NutsCommandAliasConfig command, NutsAddOptions options);

    boolean removeCommandAlias(String name, NutsRemoveOptions options);

    NutsWorkspaceCommandAlias findCommandAlias(String name);

    List<NutsWorkspaceCommandAlias> findCommandAliases();

    List<NutsWorkspaceCommandAlias> findCommandAliases(NutsId id);

    Path getHomeLocation(NutsStoreLocation folderType);

    Path getStoreLocation(NutsStoreLocation folderType);

    void setStoreLocation(NutsStoreLocation folderType, String location);

    void setStoreLocationStrategy(NutsStoreLocationStrategy strategy);

    NutsStoreLocationStrategy getStoreLocationStrategy();

    NutsStoreLocationStrategy getRepositoryStoreLocationStrategy();

    void setStoreLocationLayout(NutsStoreLocationLayout layout);

    NutsStoreLocationLayout getStoreLocationLayout();

    Path getStoreLocation(String id, NutsStoreLocation folderType);

    Path getStoreLocation(NutsId id, NutsStoreLocation folderType);

    Path getStoreLocation(NutsId id, Path path);

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
    
    String getDefaultIdBasedir(NutsId id);

    NutsId createComponentFaceId(NutsId id, NutsDescriptor desc);

    NutsCommandAliasFactoryConfig[] getCommandFactories();

    NutsRepositoryRef[] getRepositoryRefs();

    NutsWorkspaceListManager createWorkspaceListManager(String name);

    void setHomeLocation(NutsStoreLocationLayout layout, NutsStoreLocation folderType, String location);

    NutsId getApiId();

    NutsId getRuntimeId();

    boolean isSupportedRepositoryType(String repositoryType);

    NutsRepository addRepository(NutsRepositoryDefinition definition);

    NutsRepository addRepository(NutsCreateRepositoryOptions options);

    /**
     *
     * @param repositoryIdPath
     * @return null if not found
     */
    NutsRepository findRepository(String repositoryIdPath);

    NutsRepository getRepository(String repositoryIdPath) throws NutsRepositoryNotFoundException;

    void removeRepository(String locationOrRepositoryId);

    NutsRepository[] getRepositories();

    NutsRepositoryDefinition[] getDefaultRepositories();

    Set<String> getAvailableArchetypes();

    Path resolveRepositoryPath(String repositoryLocation);

    NutsIndexStoreClientFactory getIndexStoreClientFactory();

    NutsRepository createRepository(NutsCreateRepositoryOptions options, Path rootFolder, NutsRepository parentRepository);

    boolean isGlobal();

    String getDefaultIdComponentExtension(String packaging);

    String getDefaultIdExtension(NutsId id);

}
