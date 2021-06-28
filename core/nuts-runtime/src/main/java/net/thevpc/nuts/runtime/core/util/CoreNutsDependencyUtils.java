/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.core.util;

import net.thevpc.nuts.NutsDependencyFilter;
import net.thevpc.nuts.NutsDependencyFilterManager;
import net.thevpc.nuts.NutsDependencyScopePattern;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public class CoreNutsDependencyUtils {

    public static String normalizeDependencyType(String s1) {
        return CoreStringUtils.trimToNull(s1);
    }

    public static NutsDependencyFilter createJavaRunDependencyFilter(NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        NutsDependencyFilterManager d = ws.dependency().filter();
        return d
                .byScope(NutsDependencyScopePattern.RUN)
                .and(d.byOptional(false))
                .and(
                        d.byType(null)
                                .or(
                                        d.byType("jar")
                                )
                ).and(d.byOs(ws.env().getOsFamily()))
                .and(d.byArch(ws.env().getArchFamily()));
    }
}