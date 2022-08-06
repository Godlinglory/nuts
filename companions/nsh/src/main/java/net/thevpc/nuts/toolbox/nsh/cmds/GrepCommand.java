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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsCp;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ColumnRuler;
import net.thevpc.nuts.toolbox.nsh.util.FileInfo;
import net.thevpc.nuts.util.NutsStringUtils;
import net.thevpc.nuts.util.NutsUtils;

/**
 * Created by vpc on 1/7/17.
 */
public class GrepCommand extends SimpleJShellBuiltin {

    public GrepCommand() {
        super("grep", DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        NutsSession session = context.getSession();
        Options options = context.getOptions();
        NutsArgument a;
        if (commandLine.next("-") != null) {
            options.files.add(null);
            return true;
        } else if (commandLine.next("-e", "--regexp") != null) {
            //options.regexp = true;
            return true;
        } else if (commandLine.next("-v", "--invert-match") != null) {
            options.invertMatch = true;
            return true;
        } else if (commandLine.next("-w", "--word-regexp") != null) {
            options.word = true;
            return true;
        } else if (commandLine.next("-x", "--line-regexp") != null) {
            options.lineRegexp = true;
            return true;
        } else if (commandLine.next("-i", "--ignore-case") != null) {
            options.ignoreCase = true;
            return true;
        } else if ((a = commandLine.next("-H", "--highlight", "--highlighter").orNull()) != null) {
            options.highlighter = NutsStringUtils.trim(a.getStringValue().get(session));
            return true;
        } else if ((a = commandLine.next("-S", "--selection-style").orNull()) != null) {
            options.selectionStyle = NutsStringUtils.trimToNull(a.getStringValue().get(session));
            return true;
        } else if (commandLine.next("-n").isPresent()) {
            options.n = true;
            return true;
        } else if (commandLine.peek().get(session).isNonOption()) {
            if (options.expression == null) {
                options.expression = commandLine.next().flatMap(NutsValue::asString).get(session);
            } else {
                String path = commandLine.next().flatMap(NutsValue::asString).get(session);
                options.files.add(new FileInfo(NutsPath.of(path, session), options.highlighter));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsPrintStream out = context.out();
        if (options.files.isEmpty()) {
            options.files.add(null);
        }
        NutsSession session = context.getSession();
        NutsUtils.requireNonBlank(options.expression, "expression", session);
        String baseExpr = simpexpToRegexp(options.expression, true);
        if (options.word) {
            baseExpr = "\\b" + baseExpr + "\\b";
        }
        if (options.lineRegexp) {
            baseExpr = "^" + baseExpr + "$";
        }
        if (options.ignoreCase) {
            baseExpr = "(?i)" + baseExpr;
        }
        Pattern p = Pattern.compile(baseExpr);
        //text mode
        boolean prefixFileName = options.files.size() > 1;
        int x = 0;
        List<GrepResultItem> results = new ArrayList<>();
        for (FileInfo f : options.files) {
            x = grepFile(f, p, options, context, prefixFileName, results);
        }
        switch (session.getOutputFormat()) {
            case PLAIN: {
                ColumnRuler ruler=new ColumnRuler();
                for (GrepResultItem result : results) {
                    if (options.n) {
                        if (result.path != null && prefixFileName) {
                            out.printf(result.path);
                            out.print(":");
                        }
                        out.print(ruler.nextNum(result.number, session));
                    }
                    out.println(result.line);
                }
                break;
            }
            default: {
                if (options.n) {
                    out.printlnf(results);
                } else {
                    out.printlnf(results.stream().map(r -> r.line).collect(Collectors.toList()));
                }
            }
        }
        if (x != 0) {
            throwExecutionException("error", x, session);
        }
    }

    protected int grepFile(FileInfo f, Pattern p, Options options, JShellExecutionContext context, boolean prefixFileName, List<GrepResultItem> results) {

        Reader reader = null;
        boolean closeReader = false;
        NutsSession session = context.getSession();
        try {
            try {
                if (f == null) {
                    closeReader = false;
                    reader = new InputStreamReader(context.in());
                    processByLine(reader, options, p, f, results, session);
                } else if (f.getFile().isDirectory()) {
                    for (NutsPath ff : f.getFile().list()) {
                        grepFile(new FileInfo(ff, f.getHighlighter()), p, options, context, true, results);
                    }
                    return 0;
                } else {
                    closeReader = true;
                    reader = new InputStreamReader(f.getFile().getInputStream());
                    if (f.getHighlighter() == null) {
                        processByLine(reader, options, p, f, results, session);
                    } else {
                        String text = new String(NutsCp.of(session).from(f.getFile()).getByteArrayResult());
                        if(NutsBlankable.isBlank(f.getHighlighter())){
                            f.setHighlighter(f.getFile().getContentType());
                        }
                        processByText(text, options, p, f, results, session);
                    }
                }
            } finally {
                if (reader != null && closeReader) {
                    reader.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsExecutionException(session, NutsMessage.ofCstyle("%s", ex), ex, 100);
        }
        return 0;
    }

    private boolean isNewLine(NutsText t) {
        if (t.getType() == NutsTextType.PLAIN) {
            String txt = ((NutsTextPlain) t).getText();
            return (txt.equals("\n") || txt.equals("\r\n"));
        }
        return false;
    }

    private NutsTextBuilder readLine(NutsTextBuilder flattened, NutsSession session) {
        if (flattened.size() == 0) {
            return null;
        }
        List<NutsText> r = new ArrayList<>();
        while (flattened.size() > 0) {
            NutsText t = flattened.get(0);
            flattened.removeAt(0);
            if (isNewLine(t)) {
                break;
            }
            r.add(t);
        }
        return NutsTexts.of(session).ofBuilder().appendAll(r);
    }

    private void processByLine(Reader reader, Options options, Pattern p, FileInfo f, List<GrepResultItem> results, NutsSession session) throws IOException {
        try (BufferedReader r = new BufferedReader(reader)) {
            String line = null;
            long nn = 1;
            while ((line = r.readLine()) != null) {
                GrepResultItem rr = createResult(nn, line, null, options, p, f, session);
                if (rr!=null) {
                    results.add(rr);
                }
                nn++;
            }
        }
    }

    private GrepResultItem createResult(long nn, String line, NutsTextBuilder coloredLine, Options options, Pattern p, FileInfo f, NutsSession session) {
        if (coloredLine == null) {
            coloredLine = NutsTexts.of(session).ofCode(f.getHighlighter(), line).highlight(session).builder();
        }
        Matcher matcher = p.matcher(line);
        boolean anyMatch = false;
        while (matcher.find()) {
            anyMatch = true;
            int pos = matcher.start();
            int end = matcher.end();
            coloredLine.replace(pos, end,
                    NutsTexts.of(session).ofStyled(
                            coloredLine.substring(pos, end)
                            , selectionStyle(options)
                    )
            );
        }
        if (anyMatch != options.invertMatch) {
            return new GrepResultItem(f.getFile(), nn, coloredLine.build(),true);
        }
        if(options.all){
            return new GrepResultItem(f.getFile(), nn, coloredLine.build(),false);
        }
        if(options.all){

        }
        return null;
    }

    private void processByText(String text, Options options, Pattern p, FileInfo f, List<GrepResultItem> results, NutsSession session) throws IOException {
        NutsTextBuilder flattened = NutsTexts.of(session).ofCode(f.getHighlighter(), text).highlight(session)
                .builder()
                .flatten();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(f.getFile().getInputStream()))) {
            String line = null;
            long nn = 1;
            while ((line = r.readLine()) != null) {
                NutsTextBuilder coloredLine = readLine(flattened, session);
                GrepResultItem rr = createResult(nn, line, coloredLine, options, p, f, session);
                if (rr != null) {
                    results.add(rr);
                }
                nn++;
            }
        }
    }

    public NutsTextStyles selectionStyle(Options options) {
        String s = options.selectionStyle;
        NutsTextStyles def = NutsTextStyles.of(NutsTextStyle.secondary(2));
        if (NutsBlankable.isBlank(s)) {
            return def;
        }
        return NutsTextStyles.parse(s).orElse(def);
    }

    private static class Options {

        //        boolean regexp = false;
        boolean invertMatch = false;
        boolean word = false;
        boolean lineRegexp = false;
        boolean ignoreCase = false;
        String highlighter;
        String selectionStyle;
        boolean n = false;
        boolean all = false;
        int windowBefore = 0;
        int windowAfter = 0;
        List<FileInfo> files = new ArrayList<>();
        String expression = null;
    }

    private static class GrepResultItem {
        NutsPath path;
        long number;
        NutsText line;
        Boolean match;

        public GrepResultItem(NutsPath path, long number, NutsText line,Boolean match) {
            this.path = path;
            this.number = number;
            this.line = line;
            this.match = match;
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
