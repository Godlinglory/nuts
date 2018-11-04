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
package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.toolbox.nsh.cmdline.FileNonOption;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.common.commandline.DefaultNonOption;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/7/17.
 */
public class GrepCommand extends AbstractNutsCommand {


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

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        Options options = new Options();
        List<File> files = new ArrayList<>();
        String expression = null;
        NutsPrintStream out = context.getTerminal().getOut();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.read("-")) {
                files.add(null);
            } else if (cmdLine.read("-e", "--regexp")) {
                options.regexp = true;
            } else if (cmdLine.read("-v", "--invert-match")) {
                options.invertMatch = true;
            } else if (cmdLine.read("-w", "--word-regexp")) {
                options.word = true;
            } else if (cmdLine.read("-x", "--line-regexp")) {
                options.lineRegexp = true;
            } else if (cmdLine.read("-i", "--ignore-case")) {
                options.ignoreCase = true;
            } else if (cmdLine.read("--version")) {
                out.printf("%s\n", "1.0");
                return 0;
            } else if (cmdLine.read("-n")) {
                options.n = true;
            } else if (cmdLine.read("--help")) {
                out.printf("%s\n", getHelp());
                return 0;
            } else {
                if (expression == null) {
                    expression = cmdLine.readNonOptionOrError(new DefaultNonOption("expression")).getString();
                } else {
                    String path = cmdLine.readNonOptionOrError(new FileNonOption("file")).getString();
                    File file = new File(context.resolvePath(path));
                    files.add(file);
                }
            }
        }
        if (files.isEmpty()) {
            files.add(null);
        }
        if (expression == null) {
            throw new IllegalArgumentException("Missing Expression");
        }
        String baseExpr = options.regexp ? CoreStringUtils.simpexpToRegexp(expression) : expression;
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
        boolean prefixFileName=files.size()>1;
        for (File f : files) {
            grepFile(f, p, options, context,prefixFileName);
        }
        return 0;
    }

    protected void grepFile(File f, Pattern p, Options options, NutsCommandContext context,boolean prefixFileName) throws IOException {

        Reader reader = null;
        try {
            String fileName=null;
            if (f == null) {
                reader = new InputStreamReader(context.getTerminal().getIn());
            } else if (f.isDirectory()) {
                File[] files = f.listFiles();
                if (files != null) {
                    for (File ff : files) {
                        grepFile(ff, p, options, context,true);
                    }
                }
                return;
            } else {
                fileName=f.getPath();
                reader = new FileReader(f);
            }
            try (BufferedReader r = new BufferedReader(reader)) {
                String line = null;
                int nn = 1;
                NutsPrintStream out = context.getTerminal().getOut();
                while ((line = r.readLine()) != null) {
                    boolean matches = p.matcher(line).matches();
                    if (matches != options.invertMatch) {
                        if (options.n) {
                            if(fileName!=null && prefixFileName){
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
    }
}