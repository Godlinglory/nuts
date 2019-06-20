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

/**
 * Argument parse Type
 *
 * @author vpc
 * @since 0.5.5
 */
public enum NutsArgumentType {
    /**
     * argument that accepts no value.
     */
    ANY,
    /**
     * argument that accepts a string as value. Either the string is included in
     * the argument itself (--option=value) or succeeds it (--option value).
     */
    STRING,
    /**
     * argument that accepts a string as value as an immediate (included in the
     * argument) value : in the form of (--option=value).
     */
    IMMEDIATE,
    /**
     * argument that accepts a boolean as value. Either the boolean is not
     * defined (--option), is included in the argument itself (--option=true) or
     * succeeds it (--option true). Parsing boolean is also aware of negated
     * options (--!option) that will be interpreted as (--option=false).
     */
    BOOLEAN;
    private final String id;

    NutsArgumentType() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public String id() {
        return id;
    }
}
