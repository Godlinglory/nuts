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

import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.util.ShellHelper;
import net.vpc.common.commandline.Argument;
import net.vpc.common.io.TextFiles;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
public class HeadCommand extends AbstractNutsCommand {


    public HeadCommand() {
        super("head", DEFAULT_SUPPORT);
    }

    private static class Options {
        int max = 0;
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        Options options = new Options();
        List<String> files = new ArrayList<>();
        PrintStream out = context.getTerminal().getOut();
        while (cmdLine.hasNext()) {
            Argument a = cmdLine.read();
            if (a.isOption()) {
                if(ShellHelper.isInt(a.getString().substring(1))){
                    options.max=Integer.parseInt(a.getString().substring(1));
                }else{
                    throw new IllegalArgumentException("Not yet supported");
                }
            } else {
                String path = a.getString();
                File file = new File(context.getAbsolutePath(path));
                    files.add(file.getPath());
            }
        }
        if (files.isEmpty()) {
            throw new IllegalArgumentException("Not yet supported");
        }
        for (String file : files) {
            TextFiles.head(TextFiles.create(file),options.max, out);
        }
        return 0;
    }
}
