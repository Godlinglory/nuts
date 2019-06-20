/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public enum NutsDependencyScope {
    API,
    IMPLEMENTATION,
    PROVIDED,
    IMPORT,
    RUNTIME,
    SYSTEM,
    TEST_COMPILE,
    TEST_PROVIDED,
    TEST_RUNTIME,
    OTHER;

    private String id;

    private NutsDependencyScope() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public String id() {
        return id;
    }

    public static NutsDependencyScope parseLenient(String s) {
        if (s == null) {
            s = "";
        }
        s = s.trim().toLowerCase();
        switch (s) {
            case "":
            case "compile":
            case "api":
                return API;
            case "implementation":
                return IMPLEMENTATION;
            case "provided": //maven
            case "compileOnly": //gradle
            case "compile-only": //gradle
                return PROVIDED;
            case "runtime":
                return RUNTIME;
            case "import":
                return IMPORT;
            case "system":
                return SYSTEM;
            case "test":
            case "test-compile":
                return TEST_COMPILE;
            case "test-provided":
                return TEST_PROVIDED;
            case "test-runtime":
                return TEST_RUNTIME;
            default:
                return OTHER;
        }
    }

}
