/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts;

import java.util.Map;

/**
 * Nuts repository manages a set of packages
 *
 * @since 0.5.4
 * @category Base
 */
public interface NutsRepository {

    /**
     * value for {@link  NutsRepositoryConfigManager#getSpeed() } resolving to 
     * in memory repositories (and hence fastest) repositories.
     */
    int SPEED_FASTEST = 1000000;
    
    /**
     * value for {@link  NutsRepositoryConfigManager#getSpeed() } resolving to 
     * local file system repositories (and hence faster) repositories.
     */
    int SPEED_FASTER = 100000;

    /**
     * value for {@link  NutsRepositoryConfigManager#getSpeed() } resolving to 
     * local network repositories (and hence fast) repositories.
     */
    int SPEED_FAST = 10000;

    /**
     * value for {@link  NutsRepositoryConfigManager#getSpeed() } resolving to 
     * remote network repositories (and hence slow) repositories.
     */
    int SPEED_SLOW = 1000;

    /**
     * value for {@link  NutsRepositoryConfigManager#getSpeed() } resolving to 
     * remote network repositories with limited known bandwidth
     * (and hence slower) repositories.
     */
    int SPEED_SLOWER = 100;

    /**
     * value for {@link  NutsRepositoryConfigManager#getSpeed() } resolving to 
     * remote network repositories with very limited known bandwidth
     * (and hence slowest) repositories.
     */
    int SPEED_SLOWEST = 10;

    /**
     * return repository type
     * @return repository type
     */
    String getRepositoryType();

    /**
     * return repository unique identifier
     * @return repository unique identifier
     */
    String getUuid();

    /**
     * return repository name.
     * equivalent to config().name()
     * @return repository name
     */
    String getName();

    /**
     * env
     * @return env
     */
    NutsRepositoryEnvManager env();

    /**
     * return parent workspace
     * @return parent workspace
     */
    NutsWorkspace getWorkspace();

    /**
     * return parent repository or null
     * @return parent repository or null
     */
    NutsRepository getParentRepository();

    /**
     * return repository configuration manager
     * @return repository configuration manager
     */
    NutsRepositoryConfigManager config();

    /**
     * return repository security manager
     * @return repository security manager
     */
    NutsRepositorySecurityManager security();

    /**
     * create deploy command
     * @return deploy command
     */
    NutsDeployRepositoryCommand deploy();

    /**
     * create undeploy command
     * @return undeploy command
     */
    NutsRepositoryUndeployCommand undeploy();

    /**
     * create push command
     * @return push command
     */
    NutsPushRepositoryCommand push();

    /**
     * create fetchDescriptor command
     * @return fetchDescriptor command
     */
    NutsFetchDescriptorRepositoryCommand fetchDescriptor();

    /**
     * create fetchContent command
     * @return fetchContent command
     */
    NutsFetchContentRepositoryCommand fetchContent();

    /**
     * create search command
     * @return search command
     */
    NutsSearchRepositoryCommand search();

    /**
     * create searchVersions command
     * @return searchVersions command
     */
    NutsSearchVersionsRepositoryCommand searchVersions();

    /**
     * create update statistics command
     *
     * @return update statistics command
     */
    NutsUpdateRepositoryStatisticsCommand updateStatistics();

    /**
     * remove repository listener
     *
     * @param listener listener
     */
    void removeRepositoryListener(NutsRepositoryListener listener);

    /**
     * add repository listener
     *
     * @param listener listener
     */
    void addRepositoryListener(NutsRepositoryListener listener);

    /**
     * Repository Listeners
     *
     * @return Repository Listeners
     */
    NutsRepositoryListener[] getRepositoryListeners();

    /**
     * return mutable instance of user properties
     * @return mutable instance of user properties
     */
    Map<String, Object> getUserProperties();

    /**
     * add listener to user properties
     * @param listener listener
     */
    void addUserPropertyListener(NutsMapListener<String, Object> listener);

    /**
     * remove listener from user properties
     * @param listener listener
     */
    void removeUserPropertyListener(NutsMapListener<String, Object> listener);

    /**
     * return array of registered user properties listeners
     * @return array of registered user properties listeners
     */
    NutsMapListener<String, Object>[] getUserPropertyListeners();

    /**
     * enabled if config is enabled and runtime is enabled
     * @return true if config is enabled and runtime is enabled
     */
    boolean isEnabled() ;

    /**
     * set runtime enabled
     * @param enabled runtime enabled value
     * @return {@code this} instance
     */
    NutsRepository setEnabled(boolean enabled);
}
