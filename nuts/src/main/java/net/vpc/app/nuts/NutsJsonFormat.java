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
import java.io.Reader;
import java.nio.file.Path;

/**
 *
 * @author vpc
 */
public interface NutsJsonFormat extends NutsFormat {

    boolean isCompact();

    NutsJsonFormat compact();

    NutsJsonFormat compact(boolean compact);

    NutsJsonFormat setCompact(boolean compact);

    /**
     * @since 0.5.6
     */
    Object getValue();

    /**
     * @since 0.5.6
     */
    NutsJsonFormat set(Object value);

    /**
     * @since 0.5.6
     */
    NutsJsonFormat setValue(Object value);

    <T> T parse(Reader reader, Class<T> cls);

    <T> T parse(Path file, Class<T> cls);

    <T> T parse(File file, Class<T> cls);

    @Override
    NutsJsonFormat session(NutsSession session);

    @Override
    NutsJsonFormat setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public NutsJsonFormat configure(boolean skipUnsupported, String... args);
}
