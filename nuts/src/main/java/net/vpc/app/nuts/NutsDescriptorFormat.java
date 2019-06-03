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
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsDescriptorFormat extends NutsConfigurable {

    boolean isCompact();

    NutsDescriptorFormat compact(boolean compact);

    NutsDescriptorFormat compact();

    NutsDescriptorFormat setCompact(boolean compact);

    void print(NutsDescriptor descriptor);

    void println(NutsDescriptor descriptor);

    void print(NutsDescriptor descriptor, NutsTerminal terminal);

    void println(NutsDescriptor descriptor, NutsTerminal terminal);

    void print(NutsDescriptor descriptor, Path file) throws UncheckedIOException;

    void println(NutsDescriptor descriptor, Path file) throws UncheckedIOException;

    void print(NutsDescriptor descriptor, File file) throws UncheckedIOException;

    void println(NutsDescriptor descriptor, File file) throws UncheckedIOException;

    void print(NutsDescriptor descriptor, PrintStream out) throws UncheckedIOException;

    void println(NutsDescriptor descriptor, PrintStream out) throws UncheckedIOException;

    void print(NutsDescriptor descriptor, OutputStream out) throws UncheckedIOException;

    void println(NutsDescriptor descriptor, OutputStream out) throws UncheckedIOException;

    void print(NutsDescriptor descriptor, Writer out) throws UncheckedIOException;

    void println(NutsDescriptor descriptor, Writer out) throws UncheckedIOException;

    String toString(NutsDescriptor descriptor);

    String format(NutsDescriptor descriptor);

    /**
     * configure the current command with the given arguments.
     * This is an override of the {@link NutsConfigurable#configure(java.lang.String...)}
     * to help return a more specific return type;
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public NutsDescriptorFormat configure(String ... args);

}
