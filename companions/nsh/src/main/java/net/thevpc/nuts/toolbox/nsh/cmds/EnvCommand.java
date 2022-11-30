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
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class EnvCommand extends SimpleJShellBuiltin {

    public EnvCommand() {
        super("env", DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsSession session = context.getSession();
        NutsArgument a = commandLine.peek().get(session);
        switch (options.readStatus) {
            case 0: {
                switch (a.key()) {
                    case "--sort": {
                        commandLine.withNextBoolean((v, r, s) -> options.sort = v, session);
                        return true;
                    }
                    case "--external":
                    case "--spawn":
                    case "-x": {
                        commandLine.withNextTrue((v, r, s) -> options.executionType = NutsExecutionType.SPAWN, session);
                        return true;
                    }
                    case "--embedded":
                    case "-b": {
                        commandLine.withNextTrue((v, r, s) -> options.executionType = NutsExecutionType.EMBEDDED, session);
                        return true;
                    }
                    case "--system": {
                        commandLine.withNextTrue((v, r, s) -> options.executionType = NutsExecutionType.SYSTEM, session);
                        return true;
                    }
                    case "--current-user": {
                        commandLine.withNextTrue((v, r, s) -> options.runAs = NutsRunAs.currentUser(), session);
                        return true;
                    }
                    case "--as-root": {
                        commandLine.withNextTrue((v, r, s) -> options.runAs = NutsRunAs.root(), session);
                        return true;
                    }
                    case "--sudo": {
                        commandLine.withNextTrue((v, r, s) -> options.runAs = NutsRunAs.sudo(), session);
                        return true;
                    }
                    case "--as-user": {
                        commandLine.withNextString((v, r, s) -> options.runAs = NutsRunAs.user(v), session);
                        return true;
                    }
                    case "-C":
                    case "--chdir": {
                        commandLine.withNextString((v, r, s) -> options.dir = v, session);
                        return true;
                    }
                    case "-u":
                    case "--unset": {
                        commandLine.withNextString((v, r, s) -> options.unsetVers.add(v), session);
                        return true;
                    }
                    case "-i":
                    case "--ignore-environment": {
                        commandLine.withNextBoolean((v, r, s) -> options.ignoreEnvironment = v, session);
                        return true;
                    }
                    case "-": {
                        commandLine.skip();
                        options.readStatus = 1;
                        return true;
                    }
                    default: {
                        if (a.isKeyValue()) {
                            options.newEnv.put(a.getKey().asString().get(session), a.getStringValue().get(session));
                            commandLine.skip();
                            options.readStatus = 1;
                            return true;
                        } else if (a.isOption()) {
                            return false;
                        } else {
                            options.command.add(a.asString().get(session));
                            commandLine.skip();
                            options.readStatus = 2;
                            return true;
                        }
                    }
                }
            }
            case 1: {
                if (a.isKeyValue()) {
                    options.newEnv.put(a.getKey().asString().get(session), a.getStringValue().get(session));
                } else {
                    options.command.add(a.asString().get(session));
                    options.readStatus = 2;
                }
                commandLine.skip();
                return true;
            }
            case 2: {
                options.command.add(a.asString().get(session));
                commandLine.skip();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.sort) {
            context.getSession().addOutputFormatOptions("--sort");
        }
        SortedMap<String, String> env = new TreeMap<>();
        if (!options.ignoreEnvironment) {
            env.putAll((Map) context.vars().getAll());
        }
        for (String v : options.unsetVers) {
            env.remove(v);
        }
        env.putAll(options.newEnv);
        if (options.command.isEmpty()) {
            if (context.getSession().isPlainOut()) {
                for (Map.Entry<String, String> e : env.entrySet()) {
                    context.getSession().out().println(e.getKey() + "=" + e.getValue());
                }
            } else {
                context.getSession().out().printlnf(env);
            }
        } else {
            final NutsExecCommand e = context.getSession().exec().addCommand(options.command)
                    .setEnv(env)
                    .setFailFast(true);
            if (options.dir != null) {
                e.setDirectory(options.dir);
            }
            if (options.executionType != null) {
                e.setExecutionType(options.executionType);
            }
            e.run();
        }
    }

    public static class Options {

        int readStatus = 0;
        LinkedHashMap<String, String> newEnv = new LinkedHashMap<>();
        List<String> command = new ArrayList<String>();
        Set<String> unsetVers = new HashSet<String>();
        boolean sort = true;
        boolean ignoreEnvironment = false;
        String dir = null;
        NutsExecutionType executionType = null;
        NutsRunAs runAs = null;
    }

}
