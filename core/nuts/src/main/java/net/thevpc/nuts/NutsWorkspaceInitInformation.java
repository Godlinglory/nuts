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

import net.thevpc.nuts.spi.NutsBootWorkspaceFactory;

import java.io.Serializable;
import java.net.URL;

/**
 * workspace initialization options.
 *
 * Created by vpc on 1/23/17.
 *
 * @since 0.5.7
 * @category SPI Base
 */
public interface NutsWorkspaceInitInformation extends Serializable {

    NutsWorkspaceOptions getOptions();

    String getWorkspaceLocation();

    String getApiVersion();

    String getRuntimeId();

    NutsBootDescriptor getRuntimeBootDescriptor();
    NutsBootDescriptor[] getExtensionBootDescriptors();

//    String getRuntimeDependencies();
//
//    String getExtensionDependencies();
//    Set<String> getExtensionDependenciesSet();

    String getBootRepositories();

    NutsBootWorkspaceFactory getBootWorkspaceFactory();

    URL[] getClassWorldURLs();

    ClassLoader getClassWorldLoader();

    String getName();

    String getUuid();

    String getApiId();

//    Set<String> getRuntimeDependenciesSet();


    String getJavaCommand();

    String getJavaOptions();

    NutsStoreLocationStrategy getStoreLocationStrategy();

    NutsOsFamily getStoreLocationLayout();

    NutsStoreLocationStrategy getRepositoryStoreLocationStrategy();

    String getStoreLocation(NutsStoreLocation location);

    NutsClassLoaderNode getRuntimeBootDependencyNode();

    NutsClassLoaderNode[] getExtensionBootDependencyNodes();
}
