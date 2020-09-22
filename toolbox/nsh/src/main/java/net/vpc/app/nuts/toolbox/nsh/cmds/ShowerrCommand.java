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
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsSingleton;
import net.vpc.common.javashell.JShellResult;
import net.vpc.app.nuts.toolbox.nsh.NshExecutionContext;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.vpc.app.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class ShowerrCommand extends SimpleNshBuiltin {

    public ShowerrCommand() {
        super("showerr", DEFAULT_SUPPORT);
    }

    private static class Options {

        String login;
        char[] password;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a = commandLine.peek();
        if (!a.isOption()) {
            if (options.login == null) {
                options.login = commandLine.next(context.getWorkspace().commandLine().createName("username")).getString();
                return true;
            } else if (options.password == null) {
                options.password = commandLine.next(context.getWorkspace().commandLine().createName("password")).getString().toCharArray();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        context.setPrintOutObject(context.getRootContext().getLastResult());
    }

    @Override
    protected void printPlainObject(SimpleNshCommandContext context) {
        JShellResult r = context.getResult();
        if (r.getCode() == 0) {
            context.out().println("##Last command ended successfully with no errors.##");
        } else {
            context.out().println("@@Last command ended abnormally with the following error :@@");
            if (r.getMessage() != null) {
                context.out().println(r.getMessage());
            }
            if (r.getStackTrace() != null) {
                context.err().println(r.getStackTrace());
            }
        }
    }

}
