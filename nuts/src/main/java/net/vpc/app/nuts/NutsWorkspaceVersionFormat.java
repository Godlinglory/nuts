/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.File;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 
 * @author vpc
 * @since 0.5.4
 */
public interface NutsWorkspaceVersionFormat {

    NutsWorkspaceVersionFormat addProperty(String key, String value);

    NutsWorkspaceVersionFormat addProperties(Properties p);

    
    NutsSession getSession();

    NutsWorkspaceVersionFormat session(NutsSession session);
    
    NutsWorkspaceVersionFormat setSession(NutsSession session);
    
    @Override
    String toString();

    String format();

//    String formatString();
    void print(PrintStream out);

    void print(Writer out);

    void print(Path out);

    void print(File out);

    void print();

    void print(NutsTerminal terminal);

    void println(Writer w);

    void println(PrintStream out);

    void println(Path path);

    void println();

    void println(NutsTerminal terminal);

    void println(File file);

//    NutsWorkspaceVersionFormat setOutputFormat(NutsOutputFormat outputFormat);

//    NutsOutputFormat getOutputFormat();

    NutsWorkspaceVersionFormat parseOptions(String[] args);
    
    NutsWorkspaceVersionFormat setMinimal(boolean minimal);

    boolean isMinimal();

    NutsWorkspaceVersionFormat setPretty(boolean pretty);

    boolean isPretty();
}
