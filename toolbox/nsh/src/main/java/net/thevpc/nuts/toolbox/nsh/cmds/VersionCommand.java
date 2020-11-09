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
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.thevpc.common.mvn.PomIdResolver;
import net.thevpc.nuts.toolbox.nsh.NshExecutionContext;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsVersionFormat;

/**
 * Created by vpc on 1/7/17.
 */
public class VersionCommand extends AbstractNshBuiltin {

    public VersionCommand() {
        super("version", DEFAULT_SUPPORT);
    }

    @Override
    public void exec(String[] args, NshExecutionContext context) {
        NutsWorkspace ws = context.getWorkspace();
        NutsCommandLine cmdLine = context.getWorkspace().commandLine().create(args);
        NutsVersionFormat version = ws.version().formatter();
        version.configure(true, cmdLine);
        version
                .setSession(context.getSession())
                .addProperty("nsh-version", PomIdResolver.resolvePomId(getClass()).toString())
                .println(context.out());
    }
}
