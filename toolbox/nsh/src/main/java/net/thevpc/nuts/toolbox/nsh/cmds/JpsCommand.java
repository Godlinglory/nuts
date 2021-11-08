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
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class JpsCommand extends SimpleJShellBuiltin {

    public JpsCommand() {
        super("jps", DEFAULT_SUPPORT,Options.class);
    }

    public static String resolveJpsCommand(NutsSession session) {
        return resolveJavaToolCommand(session, null, "jps");
    }

    public static String resolveJavaToolCommand(NutsSession session, String javaHome, String javaCommand) {
        String exe = session.env().getOsFamily().equals(NutsOsFamily.WINDOWS) ? (javaCommand + ".exe") : javaCommand;
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

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
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
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        List<JpsRow> results = new ArrayList<>();

        NutsExecCommand e = context.getSession().exec()
                .setExecutionType(NutsExecutionType.SYSTEM)
                .addCommand(resolveJpsCommand(context.getSession()), "-l", "-v", "-m")
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
        context.getSession().out().printlnf(results);
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
}
