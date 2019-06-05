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

import java.util.Arrays;
import net.vpc.app.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.javashell.JShellCommand;

/**
 * Created by vpc on 1/7/17.
 */
public class CommandCommand extends AbstractNshBuiltin {

    public CommandCommand() {
        super("command", DEFAULT_SUPPORT);
    }

    @Override
    public void exec(String[] args, NutsCommandContext context) {
        if (args.length > 0) {
            JShellCommand a = context.getGlobalContext().builtins().find(args[0]);
            if (a != null) {
                a.exec(Arrays.copyOfRange(args, 1, args.length), context);
            } else {
                context.getWorkspace()
                        .exec().command(Arrays.copyOfRange(args, 1, args.length))
                        .setDirectory(context.getGlobalContext().getCwd())
                        .setEnv(context.vars().getExported())
                        .run()
                        .failFast();
            }
        }
    }

}
