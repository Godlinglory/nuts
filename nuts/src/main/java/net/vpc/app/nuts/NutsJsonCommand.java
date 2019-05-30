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
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;

/**
 *
 * @author vpc
 */
public interface NutsJsonCommand {

    boolean isCompact();

    NutsJsonCommand compact();

    NutsJsonCommand compact(boolean compact);

    NutsJsonCommand setCompact(boolean compact);

    String toJsonString(Object obj);

    <T> T read(Reader reader, Class<T> cls);

    <T> T read(Path file, Class<T> cls);

    <T> T read(File file, Class<T> cls);

    void write(Object obj, Writer out);

    <T> void write(Object obj, Path file);

    <T> void write(Object obj, File file);

    <T> void write(Object obj, PrintStream printStream);

}
