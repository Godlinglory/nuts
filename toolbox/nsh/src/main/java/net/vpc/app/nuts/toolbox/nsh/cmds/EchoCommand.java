/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.commandline.ValueNonOption;
import net.vpc.common.commandline.ArgVal;

/**
 * Created by vpc on 1/7/17.
 */
public class EchoCommand extends AbstractNutsCommand {

    public EchoCommand() {
        super("echo", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        boolean noTrailingNewLine = false;
        boolean plain = false;
        boolean first = true;
        NutsPrintStream out = context.getTerminal().getOut();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.isOption()) {
                ArgVal option = cmdLine.read();
                if (option.isAny("-n")) {
                    noTrailingNewLine = true;
                } else if (option.isAny("-p")) {
                    plain = true;
                } else {
                    throw new NutsIllegalArgumentException("Unsupported option " + option);
                }
            } else {
                if (cmdLine.isExecMode()) {
                    if (first) {
                        first = false;
                    } else {
                        out.print(" ");
                    }
                    if (plain) {
                        out.print(cmdLine.readNonOptionOrError(new ValueNonOption("value")).getString());
                    } else {
                        out.print(cmdLine.readNonOptionOrError(new ValueNonOption("value")).getString());
                    }
                }
            }
        }
        if (cmdLine.isExecMode()) {
            if (!noTrailingNewLine) {
                out.println();
            }
        }
        return 0;
    }
}
