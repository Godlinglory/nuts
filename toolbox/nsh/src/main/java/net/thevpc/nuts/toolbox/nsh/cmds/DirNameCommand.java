/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class DirNameCommand extends SimpleNshBuiltin {

    public DirNameCommand() {
        super("dirname", DEFAULT_SUPPORT);
    }

    private static class Options {

        String sep = "\n";
        List<String> names = new ArrayList<>();
        boolean multi = false;
        String suffix = null;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine cmdLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a = cmdLine.peek();
        switch (a.getStringKey()) {
            case "-z":
            case "--zero": {
                cmdLine.skip();
                options.sep = "\0";
                return true;
            }
            default: {
                if (a.isOption()) {

                } else {
                    options.names.add(cmdLine.next().toString());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine cmdLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.names.isEmpty()) {
            cmdLine.required();
        }
        List<String> results = new ArrayList<>();
        for (String name : options.names) {
            StringBuilder sb = new StringBuilder(name);
            int lastNameLen = 0;
            while (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
                sb.deleteCharAt(sb.length() - 1);
                lastNameLen++;
            }
            while (sb.length() > 1 && sb.charAt(sb.length() - 1) == '/') {
                sb.deleteCharAt(sb.length() - 1);
            }
            if (lastNameLen == 0) {
                while (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
                    sb.deleteCharAt(sb.length() - 1);
                    lastNameLen++;
                }
                while (sb.length() > 1 && sb.charAt(sb.length() - 1) == '/') {
                    sb.deleteCharAt(sb.length() - 1);
                }
            }
            if (sb.length() == 0) {
                results.add(".");
            } else {
                results.add(sb.toString());
            }
        }
        context.setPrintlnOutObject(results);
    }

    @Override
    protected void printPlainObject(SimpleNshBuiltin.SimpleNshCommandContext context) {
        List<String> results = context.getResult();
        Options options = context.getOptions();
        for (String name : results) {
            context.out().print(name);
            context.out().print(options.sep);
        }
    }
}
