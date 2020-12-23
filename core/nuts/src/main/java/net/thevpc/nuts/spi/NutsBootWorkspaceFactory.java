/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsWorkspaceInitInformation;
import net.thevpc.nuts.NutsWorkspaceOptions;

/**
 * Class responsible of creating and initializing Workspace
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 * @category SPI Base
 */
public interface NutsBootWorkspaceFactory {

    /**
     * when multiple factories are available, the best one is selected according to
     * the maximum value of {@code getBootSupportLevel(options)}.
     * Note that default value (for the reference implementation) is {@code NutsComponent.DEFAULT_SUPPORT}.
     * Any value less or equal to zero is ignored (and the factory is discarded)
     * @param options command line options
     * @return support level
     */
    int getBootSupportLevel(NutsWorkspaceOptions options);

    /**
     * create workspace with the given options
     * @param options boot init options
     * @return initialized workspace
     */
    NutsWorkspace createWorkspace(NutsWorkspaceInitInformation options);

}
