/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.util.NutsUtils;

import java.util.List;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public interface NutsDependencySolvers extends NutsComponent {
    static NutsDependencySolvers of(NutsSession session) {
        NutsUtils.requireSession(session);
        return session.extensions().createSupported(NutsDependencySolvers.class, true, null);
    }

    NutsDependencySolver createSolver(NutsSession session);

    NutsDependencySolver createSolver(String solverName, NutsSession session);

    List<String> getSolverNames(NutsSession session);

}
