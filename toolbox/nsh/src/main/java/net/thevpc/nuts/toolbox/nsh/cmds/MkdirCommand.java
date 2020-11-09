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

import java.io.IOException;
import net.thevpc.nuts.NutsExecutionException;
import net.thevpc.nuts.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;
import net.thevpc.common.ssh.SshXFile;
import net.thevpc.common.xfile.XFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 * Created by vpc on 1/7/17. ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
@NutsSingleton
public class MkdirCommand extends SimpleNshBuiltin {

    public MkdirCommand() {
        super("mkdir", DEFAULT_SUPPORT);
    }

    public static class Options {

        List<String> files = new ArrayList<>();
        List<XFile> xfiles = new ArrayList<>();

        boolean p;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a;
        if ((a = commandLine.nextBoolean("--parent", "-p")) != null) {
            options.p = a.getBooleanValue();
            return true;
        } else if (commandLine.peek().isNonOption()) {
            options.files.addAll(Arrays.asList(commandLine.toStringArray()));
            commandLine.skipAll();
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        options.xfiles = ShellHelper.xfilesOf(options.files, context.getCwd());
        if (options.xfiles.size() < 1) {
            commandLine.required();
        }
        ShellHelper.WsSshListener listener = new ShellHelper.WsSshListener(context.getSession());
        for (XFile v : options.xfiles) {
            if (v instanceof SshXFile) {
                ((SshXFile) v).setListener(listener);
            }
            try {
                v.mkdir(options.p);
            } catch (IOException ex) {
                throw new NutsExecutionException(context.getWorkspace(), ex.getMessage(), ex, 100);
            }
        }
    }
}
