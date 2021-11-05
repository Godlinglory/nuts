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
 * <p>
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

import net.thevpc.nuts.spi.NutsComponent;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 * @app.category Base
 */
public interface NutsWorkspace extends NutsComponent {

    /**
     * Workspace identifier, most likely to be unique cross machines
     *
     * @return uuid
     */
    String getUuid();

    /**
     * Workspace name
     *
     * @return name
     */
    String getName();

    String getHashName();

    NutsVersion getApiVersion();

    NutsId getApiId();

    NutsId getRuntimeId();

    String getLocation();

    ///////////////////// factory
    NutsSession createSession();

    NutsSearchCommand search();

    NutsFetchCommand fetch();

    NutsDeployCommand deploy();

    NutsUndeployCommand undeploy();

    NutsExecCommand exec();

    NutsInstallCommand install();

    NutsUninstallCommand uninstall();

    NutsUpdateCommand update();

    NutsPushCommand push();

    NutsUpdateStatisticsCommand updateStatistics();


    ///////////////////// sub system
    NutsWorkspaceExtensionManager extensions();

    NutsWorkspaceConfigManager config();

    NutsRepositoryManager repos();

    NutsWorkspaceSecurityManager security();

    NutsWorkspaceEventManager events();

    /**
     * create info format instance
     *
     * @return info format
     * @since 0.5.5
     */
    NutsInfoFormat info();

    NutsFormatManager formats();

    NutsImportManager imports();

    NutsCustomCommandManager commands();

    NutsWorkspaceLocationManager locations();

    NutsWorkspaceEnvManager env();

    NutsBootManager boot();

}
