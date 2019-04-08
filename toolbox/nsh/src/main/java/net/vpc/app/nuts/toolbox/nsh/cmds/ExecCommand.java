/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.NutsExecutionType;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.commandline.Argument;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
public class ExecCommand extends AbstractNutsCommand {

    public ExecCommand() {
        super("exec", DEFAULT_SUPPORT);
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        List<String> sargs = new ArrayList<>();
        NutsExecutionType executionType = null;
        boolean command = false;
        Argument a;
        List<String> execOptions=new ArrayList<>();
        while (cmdLine.hasNext()) {
            if (!command) {
                if (context.configure(cmdLine)) {
                    //
                }else if ((a=cmdLine.readBooleanOption("-n", "--native"))!=null) {
                    if(a.getBooleanValue()) {
                        executionType = NutsExecutionType.NATIVE;
                    }
                }else if ((a=cmdLine.readBooleanOption("-m", "--embedded"))!=null) {
                    if(a.getBooleanValue()) {
                        executionType = NutsExecutionType.EMBEDDED;
                    }
                }else if ((a=cmdLine.readBooleanOption("-x", "--external"))!=null) {
                    if(a.getBooleanValue()) {
                        executionType = NutsExecutionType.EXTERNAL;
                    }
                }else if (cmdLine.isOption()) {
                    execOptions.add(cmdLine.read().getStringExpression());
                } else {
                    sargs.add(cmdLine.read().getStringExpression());
                    command = true;
                }
            } else {
                sargs.add(cmdLine.read().getStringExpression());
            }
        }
        if (!cmdLine.isExecMode()) {
            return 0;
        }
        return context.getWorkspace()
                .exec()
                .setExecutionType(executionType)
                .setCommand(sargs)
                .setExecutorOptions(execOptions)
                .setEnv(context.consoleContext().env().getEnv())
                .setDirectory(context.consoleContext().getShell().getCwd())
                .setSession(context.getSession())
                .exec().getResult()
                ;
    }
}
