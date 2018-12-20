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

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.mvn.PomIdResolver;

import java.io.PrintStream;
import java.util.Properties;

/**
 * Created by vpc on 1/7/17.
 */
public class VersionCommand extends AbstractNutsCommand {

    public VersionCommand() {
        super("version", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        PrintStream out = context.getTerminal().getFormattedOut();
        NutsWorkspace ws = context.getWorkspace();
        boolean fancy = false;
        boolean min = false;
        CommandLine cmdLine=new CommandLine(args);
        Argument a;
        boolean noColors=false;
        while(cmdLine.hasNext()){
            if (context.configure(cmdLine)) {
                //
            }else if ((a = cmdLine.readBooleanOption("-f","--fancy")) != null) {
                fancy = a.getBooleanValue();
            }else if ((a = cmdLine.readBooleanOption("-m","--min")) != null) {
                min = true;
            }else{
                cmdLine.unexpectedArgument(getName());
            }

        }
        Properties extra = new Properties();
        extra.put("nsh-version",PomIdResolver.resolvePomId(getClass()).toString());
        ws.printVersion(out, extra, ((fancy ? "fancy" : "") + "," + (min ? "min" : "")));
        return 0;
    }
}