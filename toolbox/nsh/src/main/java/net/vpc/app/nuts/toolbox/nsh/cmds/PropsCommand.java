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

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.NutsConsoleContext;
import net.vpc.app.nuts.toolbox.nsh.util.ShellHelper;
import net.vpc.common.xfile.XFile;

import java.io.*;
import java.util.*;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsPropertiesFormat;

/**
 * Created by vpc on 1/7/17.
 */
public class PropsCommand extends AbstractNutsCommand {

    public PropsCommand() {
        super("props", DEFAULT_SUPPORT);
    }

    public enum SourceType {
        FILE,
        SYSTEM
    }

    public enum TargetType {
        AUTO,
        FILE,
        CONSOLE
    }

    public enum Format {
        PROPS,
        XML,
        AUTO,
    }

    public static class Options {
        String property = null;
        String action = null;
        Format sourceFormat = Format.AUTO;
        String sourceFile = null;
        String targetFile = null;
        Format targetFormat = Format.AUTO;
        boolean sort = false;
        Map<String, String> updates = new HashMap<>();
        SourceType sourceType = SourceType.FILE;
        TargetType targetType = TargetType.FILE;
        String comments;
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsCommandLine cmdLine = cmdLine(args, context);
        Options o = new Options();
        NutsArgument a;
        do {
            if (context.configure(cmdLine)) {
                //
            }else  if (cmdLine.readAllOnce("get")) {
                o.property = cmdLine.read().getString();
                o.action = "get";
                while (cmdLine.hasNext()) {
                    if (cmdLine.readAllOnce("--xml")) {
                        o.sourceFormat = Format.XML;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.readRequiredNonOption(cmdLine.createNonOption("file")).getString();

                    } else if (cmdLine.readAllOnce("--system")) {
                        o.sourceFormat = Format.PROPS;
                        o.sourceType = SourceType.SYSTEM;
                        o.sourceFile = null;

                    } else if (cmdLine.readAllOnce("--props")) {
                        o.sourceFormat = Format.PROPS;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.readRequiredNonOption(cmdLine.createNonOption("file")).getString();

                    } else if (cmdLine.readAllOnce("--file")) {
                        o.sourceFormat = Format.AUTO;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.readRequiredNonOption(cmdLine.createNonOption("file")).getString();
                    } else {
                        cmdLine.unexpectedArgument(getName());
                    }

                }
            } else if (cmdLine.readAllOnce("set")) {
                String k = cmdLine.read().getString();
                String v = cmdLine.read().getString();
                o.updates.put(k, v);
                o.action = "set";
                while (cmdLine.hasNext()) {
                    if (cmdLine.readAllOnce("--comments")) {
                        o.comments = cmdLine.read().getValue().getString();
                    } else if (cmdLine.readAllOnce("--to-props-file")) {
                        o.targetFormat = Format.PROPS;
                        o.targetType = TargetType.FILE;
                        o.targetFile = cmdLine.readRequiredNonOption(cmdLine.createNonOption("file")).getString();

                    } else if (cmdLine.readAllOnce("--to-xml-file")) {
                        o.targetFormat = Format.XML;
                        o.targetType = TargetType.FILE;
                        o.targetFile = cmdLine.readRequiredNonOption(cmdLine.createNonOption("file")).getString();
                    } else if (cmdLine.readAllOnce("--to-file")) {
                        o.targetFormat = Format.AUTO;
                        o.targetType = TargetType.FILE;
                        o.targetFile = cmdLine.readRequiredNonOption(cmdLine.createNonOption("file")).getString();

                    } else if (cmdLine.readAllOnce("--print-props")) {
                        o.targetFormat = Format.PROPS;
                        o.targetType = TargetType.CONSOLE;
                        o.targetFile = null;

                    } else if (cmdLine.readAllOnce("--print-xml")) {
                        o.targetFormat = Format.XML;
                        o.targetType = TargetType.CONSOLE;
                        o.targetFile = null;

                    } else if (cmdLine.readAllOnce("--save")) {
                        o.targetFormat = Format.AUTO;
                        o.targetType = TargetType.CONSOLE;
                        o.targetFile = null;
                    } else if (cmdLine.readAllOnce("--sort")) {
                        o.sort = true;
                    } else if (cmdLine.readAllOnce("--xml")) {
                        o.sourceFormat = Format.XML;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.readRequiredNonOption(cmdLine.createNonOption("file")).getString();

                    } else if (cmdLine.readAllOnce("--system")) {
                        o.sourceFormat = Format.PROPS;
                        o.sourceType = SourceType.SYSTEM;
                        o.sourceFile = null;

                    } else if (cmdLine.readAllOnce("--props")) {
                        o.sourceFormat = Format.PROPS;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.readRequiredNonOption(cmdLine.createNonOption("file")).getString();
                    } else if (cmdLine.readAllOnce("--file")) {
                        o.sourceFormat = Format.AUTO;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.readRequiredNonOption(cmdLine.createNonOption("file")).getString();
                    } else {
                        cmdLine.unexpectedArgument(getName());
                    }
                }
            } else if (cmdLine.readAllOnce("list")) {
                o.action = "list";
                while (cmdLine.hasNext()) {
                    cmdLine.unexpectedArgument(getName());
                }
            } else {
                cmdLine.unexpectedArgument(getName());
            }
        } while (cmdLine.hasNext());
        if (o.sourceType != SourceType.FILE && o.sourceFile != null) {
            throw new NutsExecutionException("props: Should not use file with --system flag",2);
        }
        if (o.sourceType == SourceType.FILE && o.sourceFile == null) {
            throw new NutsExecutionException("props: Missing file",3);
        }
        if (o.action == null) {
            throw new NutsExecutionException("props: Missing action",4);
        }
        switch (o.action) {
            case "get": {
                return action_get(context, o);
            }
            case "set": {
                switch (o.sourceType) {
                    case FILE: {
                        Properties p = readProperties(o,context);
                        if (o.targetType == TargetType.FILE) {
                            try (FileWriter os = new FileWriter(
                                    o.targetFile == null ? o.targetFile : o.sourceFile
                            )) {
                                p.store(os, o.comments);
                            }
                        } else {
                            try (FileWriter os = new FileWriter(o.sourceFile)) {
                                p.store(os, o.comments);
                            }
                        }
                    }
                }
                return action_get(context, o);
            }
            case "list": {
                return action_list(context, o);
            }
            default: {
                throw new NutsExecutionException("props: Unsupported action " + o.action,2);
            }
        }
    }

    private int action_list(NutsCommandContext context, Options o) throws IOException {
        Properties p = getProperties(o,context);
        PrintStream out = context.out();
        NutsPropertiesFormat f = context.getWorkspace().formatter().createPropertiesFormat()
                .setSort(o.sort)
                .setTable(true);
        f.format(p, out);
        return 0;
    }

    private int action_get(NutsCommandContext context, Options o) throws IOException {
        Properties p = getProperties(o,context);
        PrintStream out = context.out();
        String v = p.getProperty(o.property);
        if (v != null) {
            out.println(v);
            return 0;
        }
        out.println("");
        return 1;
    }

    private Properties getProperties(Options o,NutsCommandContext context) throws IOException {
        Properties p = new Properties();
        switch (o.sourceType) {
            case FILE: {
                p = readProperties(o,context);
                break;
            }
            case SYSTEM: {
                p = System.getProperties();
                break;
            }
        }
        return p;
    }


    private Format detectFileFormat(String file) {
        if (
                file.toLowerCase().endsWith(".props")
                        || file.toLowerCase().endsWith(".properties")
        ) {
            return Format.PROPS;
        } else if (file.toLowerCase().endsWith(".xml")) {
            return Format.XML;
        }
        throw new NutsExecutionException("Unknown file format " + file,2);
    }

    private Properties readProperties(Options o,NutsCommandContext context) throws IOException {
        Properties p = new Properties();
        String sourceFile = o.sourceFile;
        XFile filePath = ShellHelper.xfileOf(sourceFile,context.getShell().getCwd());
        try (InputStream is = filePath.getInputStream()) {

            Format sourceFormat = o.sourceFormat;
            if (sourceFormat == Format.AUTO) {
                sourceFormat = detectFileFormat(filePath.getPath());
            }
            switch (sourceFormat) {
                case PROPS: {
                    p.load(is);
                    break;
                }
                case XML: {
                    p.loadFromXML(is);
                    break;
                }
            }
        }
        return p;
    }

    private void storeProperties(Properties p, Options o, NutsConsoleContext context) throws IOException {
        String targetFile = o.targetFile;
        boolean console = false;
        switch (o.targetType) {
            case AUTO: {
                if (targetFile == null) {
                    targetFile = o.sourceFile;
                }
                break;
            }
            case CONSOLE: {
                console = true;
                break;
            }
        }
        if (console) {
            Format format = o.targetFormat;
            switch (format) {
                case AUTO: {
                    NutsPropertiesFormat f = context.getWorkspace().formatter().createPropertiesFormat()
                            .setSort(o.sort)
                            .setTable(true);
                    f.format(p, context.getFormattedOut());
                    break;
                }
                case PROPS: {
                    if (o.sort) {
                        p = new SortedProperties(p);
                    }
                    p.store(context.out(), o.comments);
                    break;
                }
                case XML: {
                    if (o.sort) {
                        p = new SortedProperties(p);
                    }
                    p.storeToXML(context.out(), o.comments);
                    break;
                }
            }
        } else {
            XFile filePath = ShellHelper.xfileOf(targetFile,context.getShell().getCwd());
            try (OutputStream os = filePath.getOutputStream()) {
                Format format = o.targetFormat;
                if (format == Format.AUTO) {
                    format = detectFileFormat(filePath.getPath());
                }
                switch (format) {
                    case PROPS: {
                        if (o.sort) {
                            p = new SortedProperties(p);
                        }
                        p.store(os, o.comments);
                        break;
                    }
                    case XML: {
                        if (o.sort) {
                            p = new SortedProperties(p);
                        }
                        p.storeToXML(os, o.comments);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public String getHelpHeader() {
        return "show properties vars";
    }

    private static class SortedProperties extends Properties {
        public SortedProperties(Properties other) {
            putAll(other);
        }

        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<>((Set) super.keySet()));
        }
    }
}
