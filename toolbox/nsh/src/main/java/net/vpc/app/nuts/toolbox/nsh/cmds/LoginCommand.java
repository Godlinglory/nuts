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

import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.DefaultNonOption;
import net.vpc.common.strings.StringUtils;

/**
 * Created by vpc on 1/7/17.
 */
public class LoginCommand extends AbstractNutsCommand {

    public LoginCommand() {
        super("login", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        Argument a;
        while(cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            }else  {
                String login = cmdLine.readRequiredNonOption(new DefaultNonOption("Username")).getStringExpression();
                String password = cmdLine.readNonOption(new DefaultNonOption("Password")).getStringExpression();
                cmdLine.unexpectedArgument(getName());
                if (cmdLine.isExecMode()) {
                    if (!NutsConstants.USER_ANONYMOUS.equals(login) && StringUtils.isEmpty(password)) {
                        password = context.getTerminal().readPassword("Password:");
                    }
                    context.getWorkspace().security().login(login, password);
                }
            }
        }
        return 0;
    }
}
