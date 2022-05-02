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
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPathPermission;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class ChmodCommand extends SimpleJShellBuiltin {

    public ChmodCommand() {
        super("chmod", DEFAULT_SUPPORT,Options.class);
    }


    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        NutsSession session = context.getSession();
        Options options = context.getOptions();
        //invert processing order!
        if (context.configureFirst(commandLine)) {
            return true;
        }
        NutsArgument a = commandLine.peek().get(session);
        String s = a.asString().get(session);
        if (s.equals("-R") || s.equals("--recursive")) {
            commandLine.skip();
            options.m.recursive = true;
            return true;
        } else {
            commandLine.skip();
            boolean add = true;
            int x = 0;
            int w = 0;
            int r = 0;
            boolean user = false;
            boolean group = false;
            boolean others = false;
            for (char c : s.substring(1).toCharArray()) {
                switch (c) {
                    case '+': {
                        add = true;
                        break;
                    }
                    case '-': {
                        add = false;
                        break;
                    }
                    case 'a': {
                        user = true;
                        group = true;
                        others = true;
                        break;
                    }
                    case 'u': {
                        user = true;
                        break;
                    }
                    case 'g': {
                        group = true;
                        break;
                    }
                    case 'o': {
                        others = true;
                        break;
                    }
                    case 'r': {
                        if (user || (!group && !others)) {
                            if (add) {
                                options.m.addPermissions.add(NutsPathPermission.CAN_READ);
                                options.m.addPermissions.add(NutsPathPermission.OWNER_READ);
                            } else {
                                options.m.removePermissions.add(NutsPathPermission.CAN_READ);
                                options.m.removePermissions.add(NutsPathPermission.OWNER_READ);
                            }
                        }
                        if (group) {
                            if (add) {
                                options.m.addPermissions.add(NutsPathPermission.GROUP_READ);
                            } else {
                                options.m.removePermissions.add(NutsPathPermission.GROUP_READ);
                            }
                        }
                        if (others) {
                            if (add) {
                                options.m.addPermissions.add(NutsPathPermission.OTHERS_READ);
                            } else {
                                options.m.removePermissions.add(NutsPathPermission.OTHERS_READ);
                            }
                        }
                        break;
                    }
                    case 'w': {
                        if (user || (!group && !others)) {
                            if (add) {
                                options.m.addPermissions.add(NutsPathPermission.CAN_WRITE);
                                options.m.addPermissions.add(NutsPathPermission.OWNER_WRITE);
                            } else {
                                options.m.removePermissions.add(NutsPathPermission.CAN_WRITE);
                                options.m.removePermissions.add(NutsPathPermission.OWNER_WRITE);
                            }
                        }
                        if (group) {
                            if (add) {
                                options.m.addPermissions.add(NutsPathPermission.GROUP_WRITE);
                            } else {
                                options.m.removePermissions.add(NutsPathPermission.GROUP_WRITE);
                            }
                        }
                        if (others) {
                            if (add) {
                                options.m.addPermissions.add(NutsPathPermission.OTHERS_WRITE);
                            } else {
                                options.m.removePermissions.add(NutsPathPermission.OTHERS_WRITE);
                            }
                        }
                        break;
                    }
                    case 'x': {
                        if (user || (!group && !others)) {
                            if (add) {
                                options.m.addPermissions.add(NutsPathPermission.CAN_EXECUTE);
                                options.m.addPermissions.add(NutsPathPermission.OWNER_EXECUTE);
                            } else {
                                options.m.removePermissions.add(NutsPathPermission.CAN_EXECUTE);
                                options.m.removePermissions.add(NutsPathPermission.OWNER_EXECUTE);
                            }
                        }
                        if (group) {
                            if (add) {
                                options.m.addPermissions.add(NutsPathPermission.GROUP_EXECUTE);
                            } else {
                                options.m.removePermissions.add(NutsPathPermission.GROUP_EXECUTE);
                            }
                        }
                        if (others) {
                            if (add) {
                                options.m.addPermissions.add(NutsPathPermission.OTHERS_EXECUTE);
                            } else {
                                options.m.removePermissions.add(NutsPathPermission.OTHERS_EXECUTE);
                            }
                        }
                        break;
                    }
                    default: {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        NutsSession session = context.getSession();
        Options options = context.getOptions();
        if (options.files.isEmpty()) {
            commandLine.throwMissingArgument(session);
        }
        LinkedHashMap<NutsPath, NutsMessage> errors = new LinkedHashMap<>();
        for (NutsPath f : options.files) {
            chmod(f, options.m, errors);
        }
        if (!errors.isEmpty()) {
            throwExecutionException(errors, 1, session);
        }
    }

    private void chmod(NutsPath f, Mods m, Map<NutsPath, NutsMessage> errors) {
        f.addPermissions(m.addPermissions.toArray(new NutsPathPermission[0]));
        f.removePermissions(m.removePermissions.toArray(new NutsPathPermission[0]));
        if (f.isDirectory()) {
            for (NutsPath file : f.list()) {
                chmod(file, m, errors);
            }
        }
    }

    private static class Mods {

        Set<NutsPathPermission> addPermissions = new HashSet<>();
        Set<NutsPathPermission> removePermissions = new HashSet<>();
        boolean recursive = false;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (NutsPathPermission p : addPermissions) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append("+").append(p.id());
            }
            for (NutsPathPermission p : removePermissions) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append("-").append(p.id());
            }
            if (recursive) {
                sb.append(",recursive");
            }
            return sb.toString();
        }
    }

    private static class Options {

        List<NutsPath> files = new ArrayList<>();
        Mods m = new Mods();
    }
}
