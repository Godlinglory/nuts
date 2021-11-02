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

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShell;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellCommandType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class TypeCommand extends SimpleNshBuiltin {

    public TypeCommand() {
        super("type", DEFAULT_SUPPORT);
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options config = context.getOptions();
        NutsArgument a = commandLine.peek();
        if (a.isNonOption()) {
            config.commands.add(commandLine.next().getString());
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options config = context.getOptions();
        JShell shell = context.getShell();
        List<ResultItem> result = new ArrayList<>();
        for (String cmd : config.commands) {
            JShellBuiltin ic = context.getRootContext().builtins().find(cmd);
            if (ic != null && ic.isEnabled()) {
                result.add(new ResultItem(
                        cmd,
                        "builtin",
                        cmd + " is a shell builtin"
                ));
            } else {
                String alias = context.getRootContext().aliases().get(cmd);
                if (alias != null) {
                    result.add(new ResultItem(
                            cmd,
                            "alias",
                            cmd + " is aliased to `" + alias + "`"
                    ));
                } else {
                    JShellCommandType pp = shell.getCommandTypeResolver().type(cmd, context.getGlobalContext());
                    if (pp != null) {
                        result.add(new ResultItem(
                                cmd,
                                pp.getType(),
                                pp.getDescription()
                        ));
                    } else {
                        if (ic != null) {
                            result.add(new ResultItem(
                                    cmd,
                                    "error",
                                    cmd + " is disabled"
                            ));
                        } else {
                            result.add(new ResultItem(
                                    cmd,
                                    "error",
                                    cmd + " not found"
                            ));
                        }
                    }
                }
            }
        }
        switch (context.getSession().getOutputFormat()) {
            case PLAIN: {
                for (ResultItem resultItem : result) {
                    context.getSession().out().println(resultItem.message);
                }
                break;
            }
            default: {
                context.getSession().out().printlnf(result);
            }
        }
    }

    private static class Options {

        List<String> commands = new ArrayList<>();
    }

    private static class ResultItem {

        String command;
        String type;
        String message;

        public ResultItem(String command, String type, String message) {
            this.command = command;
            this.type = type;
            this.message = message;
        }

        public ResultItem() {
        }

    }


}
