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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class UnsetCommand extends SimpleNshBuiltin {

    public UnsetCommand() {
        super("unset", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean fct;
        Set<String> list = new HashSet<>();
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a = commandLine.peek();
        if (a.isOption()) {
            if (a.getStringKey().equals("-v")) {
                options.fct = !commandLine.nextBoolean().getBooleanValue();
                return true;
            } else if (a.getStringKey().equals("-f")) {
                options.fct = commandLine.nextBoolean().getBooleanValue();
                return true;
            }
        } else {
            options.list.addAll(Arrays.asList(commandLine.toStringArray()));
            commandLine.skipAll();
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.fct) {
            for (String k : options.list) {
                context.getRootContext().functions().unset(k);
            }
        } else {
            for (String k : options.list) {
                context.getRootContext().aliases().set(k, null);
            }
        }
    }
}
