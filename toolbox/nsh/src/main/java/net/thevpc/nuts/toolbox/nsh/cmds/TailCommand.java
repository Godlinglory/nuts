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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.NutsExecutionException;
import net.thevpc.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;
import net.thevpc.common.io.TextFiles;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.toolbox.nsh.NshExecutionContext;
import net.thevpc.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
public class TailCommand extends AbstractNshBuiltin {

    public TailCommand() {
        super("tail", DEFAULT_SUPPORT);
    }

    private static class Options {

        int max = 0;
    }

    public void exec(String[] args, NshExecutionContext context) {
        NutsCommandLine cmdLine = cmdLine(args, context);
        Options options = new Options();
        List<String> files = new ArrayList<>();
        PrintStream out = context.out();
        while (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.next();
            if (a.isOption()) {
                if (context.configureFirst(cmdLine)) {
                    //
                } else if (ShellHelper.isInt(a.getString().substring(1))) {
                    options.max = Integer.parseInt(a.getString().substring(1));
                } else {
                    throw new NutsExecutionException(context.getWorkspace(), "Not yet supported", 2);
                }
            } else {
                String path = a.getString();
                File file = new File(context.getGlobalContext().getAbsolutePath(path));
                files.add(file.getPath());
            }
        }
        if (files.isEmpty()) {
            throw new NutsExecutionException(context.getWorkspace(), "Not yet supported", 2);
        }
        for (String file : files) {
            try {
                TextFiles.tail(TextFiles.create(file), options.max, out);
            } catch (IOException ex) {
                throw new NutsExecutionException(context.getWorkspace(), ex.getMessage(), ex, 100);
            }
        }
    }
}
