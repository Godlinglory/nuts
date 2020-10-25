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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class JpsCommand extends SimpleNshBuiltin {

    public JpsCommand() {
        super("jps", DEFAULT_SUPPORT);
    }

    private static class JpsRow {

        String id;
        String name;
        String fullName;
        String vmOptions;
        String arguments;
    }

    private static class Options {

        boolean m = false;
        boolean v = false;
        boolean V = false;
        boolean q = false;
        boolean l = false;
        String host;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (commandLine.next("-l") != null) {
            options.l = true;
            return true;
        } else if (commandLine.next("-v") != null) {
            options.v = true;
            return true;
        } else if (commandLine.next("-m") != null) {
            options.m = true;
            return true;
        } else if (commandLine.next("-q") != null) {
            options.q = true;
            return true;
        } else if (commandLine.next("-V") != null) {
            options.v = true;
            return true;
        } else if (commandLine.peek().isOption()) {
            //
        } else {
            if (options.host == null) {
                options.host = commandLine.next().toString();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        List<JpsRow> results = new ArrayList<>();

        NutsExecCommand e = context.getWorkspace().exec().userCmd()
                .addCommand(resolveJpsCommand(context.getWorkspace()), "-l", "-v", "-m")
                .setRedirectErrorStream(true)
                .grabOutputString()
                .setFailFast(true).run();
        String resultString = e.getOutputString();
        for (String line : resultString.split("[\n\r]+")) {
            line = line.trim();
            if (line.length() > 0) {
                int s1 = line.indexOf(' ');
                int s2 = s1 <= 0 ? -1 : line.indexOf(' ', s1 + 1);
                JpsRow r = new JpsRow();
                r.id = s1 < 0 ? line : line.substring(0, s1);
                r.fullName = s1 <= 0 ? "" : s2 <= 0 ? line.substring(s1 + 1).trim() : line.substring(s1 + 1, s2);
                r.arguments = s2 < 0 ? "" : line.substring(s2 + 1);
                r.name = r.fullName.lastIndexOf('.') >= 0 ? r.fullName.substring(r.fullName.lastIndexOf('.') + 1) : r.fullName;
                if (options.q) {
                    r.fullName = null;
                    r.arguments = null;
                }
                if (!options.l) {
                    r.fullName = null;
                }
                if (!options.m) {
                    r.arguments = null;
                }
                if (!options.v) {
                    r.vmOptions = null;
                }
                results.add(r);
            }
        }
        context.setPrintOutObject(results);
    }

    public static String resolveJpsCommand(NutsWorkspace ws) {
        return resolveJavaToolCommand(ws,null, "jps");
    }

    public static String resolveJavaToolCommand(NutsWorkspace ws,String javaHome, String javaCommand) {
        String exe = ws.env().getOsFamily().equals(NutsOsFamily.WINDOWS) ? (javaCommand + ".exe") : javaCommand;
        if (javaHome == null) {
            javaHome = System.getProperty("java.home");
        }
        Path jh = Paths.get(javaHome);
        Path p = jh.resolve("bin").resolve(exe);
        if (Files.exists(p)) {
            return p.toString();
        }
        p = jh.resolve(exe);
        if (Files.exists(p)) {
            return p.toString();
        }
        if (jh.getFileName().toString().equals("jre")) {
            p = jh.getParent().resolve("bin").resolve(exe);
            if (Files.exists(p)) {
                return p.toString();
            }
            p = jh.getParent().resolve(exe);
            if (Files.exists(p)) {
                return p.toString();
            }
        }
        return exe;
    }
}
