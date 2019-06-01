/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.common.javashell.AutoCompleteCandidate;

import java.util.*;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshCommand;

/**
 * Created by vpc on 1/7/17.
 */
public class AutocompleteCommand extends SimpleNshCommand {

    public AutocompleteCommand() {
        super("autocomplete", DEFAULT_SUPPORT);
    }

    private static class Options {

        String cmd = null;
        List<String> items = new ArrayList<>();
        int index = -1;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }
    @Override
    protected boolean configureFirst(NutsCommand cmdLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (!cmdLine.peek().isOption()) {
            while (cmdLine.hasNext()) {
                String s = cmdLine.next().getString();
                if (options.cmd == null) {
                    options.cmd = s;
                } else {
                    if (s.startsWith("[]") && options.index < 0) {
                        options.index = options.items.size();
                        options.items.add(s.substring(2));
                    } else {
                        options.items.add(s);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommand commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.cmd == null) {
            throw new NutsExecutionException(context.getWorkspace(), "Missing Command", 1);
        }
        if (options.index < 0) {
            options.index = options.items.size();
            options.items.add("");
        }
        List<AutoCompleteCandidate> aa = context.getGlobalContext().resolveAutoCompleteCandidates(
                options.cmd, options.items, options.index,
                context.getWorkspace().parser().parseCommand(options.items).getCommandLine()
        );
        Properties p = new Properties();
        for (AutoCompleteCandidate autoCompleteCandidate : aa) {
            String value = autoCompleteCandidate.getValue();
            String dvalue = autoCompleteCandidate.getDisplay();
            if (dvalue != null && dvalue.equals(value)) {
                dvalue = null;
            }
            p.setProperty(value == null ? "" : value, dvalue == null ? "" : dvalue);
        }
        context.setOutObject(p);
    }

    @Override
    protected void printObjectPlain(SimpleNshCommandContext context) {
        Properties p = context.getResult();
        for (String o : new TreeSet<String>((Set) p.keySet())) {
            if (o.startsWith("-")) {
                // option
                context.out().printf("[[%s]]\n", o);
            } else if (o.startsWith("<")) {
                context.out().printf("**%s**\n", o);
            } else {
                context.out().printf("<<%s>>\n", o);
            }
        }
    }
}
