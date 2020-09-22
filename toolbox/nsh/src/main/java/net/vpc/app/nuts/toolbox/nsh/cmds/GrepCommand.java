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

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.toolbox.nsh.AbstractNshBuiltin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.toolbox.nsh.NshExecutionContext;
import net.vpc.app.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
public class GrepCommand extends AbstractNshBuiltin {

    public GrepCommand() {
        super("grep", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean regexp = false;
        boolean invertMatch = false;
        boolean word = false;
        boolean lineRegexp = false;
        boolean ignoreCase = false;
        boolean n = false;
    }

    public void exec(String[] args, NshExecutionContext context) {
        NutsCommandLine cmdLine = cmdLine(args, context);
        Options options = new Options();
        List<File> files = new ArrayList<>();
        String expression = null;
        PrintStream out = context.out();
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
                //
            } else if (cmdLine.next("-") != null) {
                files.add(null);
            } else if (cmdLine.next("-e", "--regexp") != null) {
                options.regexp = true;
            } else if (cmdLine.next("-v", "--invert-match") != null) {
                options.invertMatch = true;
            } else if (cmdLine.next("-w", "--word-regexp") != null) {
                options.word = true;
            } else if (cmdLine.next("-x", "--line-regexp") != null) {
                options.lineRegexp = true;
            } else if (cmdLine.next("-i", "--ignore-case") != null) {
                options.ignoreCase = true;
            } else if (cmdLine.next("--version") != null) {
                out.printf("%s\n", "1.0");
                return;
            } else if (cmdLine.next("-n") != null) {
                options.n = true;
            } else if (cmdLine.next("--help") != null) {
                out.printf("%s\n", getHelp());
                return;
            } else {
                if (expression == null) {
                    expression = cmdLine.next().getString();
                } else {
                    String path = cmdLine.next().getString();
                    File file = new File(context.getGlobalContext().getAbsolutePath(path));
                    files.add(file);
                }
            }
        }
        if (files.isEmpty()) {
            files.add(null);
        }
        if (expression == null) {
            throw new NutsExecutionException(context.getWorkspace(), "Missing Expression", 2);
        }
        String baseExpr = options.regexp ? ("^" + simpexpToRegexp(expression, false) + "$") : expression;
        if (options.word) {
            baseExpr = "\\b" + baseExpr + "\\b";
        }
        if (!options.lineRegexp) {
            baseExpr = ".*" + baseExpr + ".*";
        }
        if (options.ignoreCase) {
            baseExpr = "(?i)" + baseExpr;
        }
        Pattern p = Pattern.compile(baseExpr);
        //text mode
        boolean prefixFileName = files.size() > 1;
        for (File f : files) {
            grepFile(f, p, options, context, prefixFileName);
        }
    }

    protected void grepFile(File f, Pattern p, Options options, NshExecutionContext context, boolean prefixFileName) {

        Reader reader = null;
        try {
            try {
                String fileName = null;
                if (f == null) {
                    reader = new InputStreamReader(context.in());
                } else if (f.isDirectory()) {
                    File[] files = f.listFiles();
                    if (files != null) {
                        for (File ff : files) {
                            grepFile(ff, p, options, context, true);
                        }
                    }
                    return;
                } else {
                    fileName = f.getPath();
                    reader = new FileReader(f);
                }
                try (BufferedReader r = new BufferedReader(reader)) {
                    String line = null;
                    int nn = 1;
                    PrintStream out = context.out();
                    while ((line = r.readLine()) != null) {
                        boolean matches = p.matcher(line).matches();
                        if (matches != options.invertMatch) {
                            if (options.n) {
                                if (fileName != null && prefixFileName) {
                                    out.print(fileName);
                                    out.print(":");
                                }
                                out.print(nn);
                                out.print(":");
                            }
                            out.println(line);
                        }
                        nn++;
                    }
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsExecutionException(context.getWorkspace(), ex.getMessage(), ex, 100);
        }
    }

    public static String simpexpToRegexp(String pattern, boolean contains) {
        if (pattern == null) {
            pattern = "*";
        }
        int i = 0;
        char[] cc = pattern.toCharArray();
        StringBuilder sb = new StringBuilder();
        while (i < cc.length) {
            char c = cc[i];
            switch (c) {
                case '.':
                case '!':
                case '$':
                case '[':
                case ']':
                case '(':
                case ')':
                case '?':
                case '^':
                case '|':
                case '\\': {
                    sb.append('\\').append(c);
                    break;
                }
                case '*': {
//                    if (i + 1 < cc.length && cc[i + 1] == '*') {
//                        i++;
//                        sb.append("[a-zA-Z_0-9_$.-]*");
//                    } else {
//                        sb.append("[a-zA-Z_0-9_$-]*");
//                    }
                    sb.append(".*");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
            i++;
        }
        if (!contains) {
            sb.insert(0, '^');
            sb.append('$');
        }
        return sb.toString();
    }

}
