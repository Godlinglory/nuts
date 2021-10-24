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
package net.thevpc.nuts;

import net.thevpc.nuts.spi.NutsDependencySolver;

import java.util.Set;

/**
 * @app.category Base
 */
public interface NutsDependencyManager {

    /**
     * return mutable id builder instance initialized with {@code this} instance.
     *
     * @return mutable id builder instance initialized with {@code this} instance
     */
    NutsDependencyBuilder builder();

    NutsDependencyParser parser();


    /**
     * create dependency format instance
     *
     * @return dependency format
     * @since 0.5.5
     */
    NutsDependencyFormat formatter();

    /**
     * create dependency format instance
     *
     * @param dependency dependency
     * @return dependency format
     * @since 0.5.5
     */
    NutsDependencyFormat formatter(NutsDependency dependency);

    NutsDependencyFilterManager filter();

    Set<NutsDependencyScope> toScopeSet(NutsDependencyScopePattern other);

    NutsSession getSession();

    NutsDependencyManager setSession(NutsSession session);

    NutsDependencySolver createSolver();

    NutsDependencySolver createSolver(String solverName);

    String[] getSolverNames();
}
