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
package net.vpc.app.nuts.core.util;

public class CoreJsonUtils {
    public static void readJsonPartialString(String str, JsonStatus s) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (s.openSimpleQuotes) {
                if (s.openAntiSlash) {
                    s.openAntiSlash = false;
                } else if (c == '\'') {
                    s.openSimpleQuotes = false;
                }
            } else if (s.openDoubleQuotes) {
                if (s.openAntiSlash) {
                    s.openAntiSlash = false;
                } else if (c == '\"') {
                    s.openDoubleQuotes = false;
                }
            } else if (s.openAntiSlash) {
                s.openAntiSlash = false;
            } else {
                switch (c) {
                    case '\\': {
                        s.openAntiSlash = true;
                        break;
                    }
                    case '\'': {
                        s.openSimpleQuotes = true;
                        break;
                    }
                    case '\"': {
                        s.openDoubleQuotes = true;
                        break;
                    }
                    case '{': {
                        s.openBraces++;
                        s.countBraces++;
                        break;
                    }
                    case '}': {
                        s.openBraces--;
                        break;
                    }
                    case '[': {
                        s.openBrackets++;
                        break;
                    }
                    case ']': {
                        s.openBrackets--;
                        break;
                    }
                }
            }
        }
    }
}