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
package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShell;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellBuiltin;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCommandAutoComplete;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellExecutionContext;

/**
 * Created by vpc on 1/7/17.
 */
public interface NshBuiltin extends NutsComponent<JShell>, JShellBuiltin<JShellExecutionContext> {

//    String getName();
//    int exec(String[] args, JShellExecutionContext context) throws Exception;
//    String getHelp();
//    String getHelpHeader();
    default void autoComplete(JShellExecutionContext context, NutsCommandAutoComplete autoComplete) {

    }
}
