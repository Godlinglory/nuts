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
package net.vpc.app.nuts.core.app;

import java.util.NoSuchElementException;
import net.vpc.app.nuts.core.DefaultNutsTokenFilter;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

/**
 * @author vpc
 */
public class DefaultNutsArgument extends DefaultNutsTokenFilter implements NutsArgument {

    /**
     * equal character
     */
    private final char eq;

    /**
     * Constructor
     *
     * @param expression expression
     * @param eq equals
     */
    public DefaultNutsArgument(String expression, char eq) {
        super(expression);
        this.eq = eq;
    }

    public boolean isUnsupported() {
        return expression != null
                && (expression.startsWith("-!!")
                || expression.startsWith("--!!")
                || expression.startsWith("---")
                || expression.startsWith("++")
                || expression.startsWith("!!"));
    }

    @Override
    public boolean isOption() {
        return expression != null
                && expression.length() > 0
                && (expression.charAt(0) == '-' || expression.charAt(0) == '+');
    }

    @Override
    public boolean isNonOption() {
        return !isOption();
    }

    @Override
    public boolean isKeyValue() {
        return expression != null && expression.indexOf(eq) >= 0;
    }

    @Override
    public NutsArgument getArgumentKey() {
        if (expression == null) {
            return this;
        }
        int x = expression.indexOf(eq);
        String p = expression;
        if (x >= 0) {
            p = expression.substring(0, x);
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < p.length()) {
            switch (p.charAt(i)) {
                case '-': {
                    sb.append(p.charAt(i));
                    break;
                }
                case '+': {
                    sb.append(p.charAt(i));
                    break;
                }
                case '!': {
                    sb.append(p.substring(i + 1));
                    return new DefaultNutsArgument(sb.toString(), eq);
                }
                case '/': {
                    if (sb.length() > 0 && i + 1 < p.length() && p.charAt(i + 1) == '/') {
                        sb.append(p.substring(i + 2));
                        return new DefaultNutsArgument(sb.toString(), eq);
                    }
                }
                default: {
                    return new DefaultNutsArgument(p, eq);
                }
            }
            i++;
        }
        return new DefaultNutsArgument(p, eq);
    }

    @Override
    public NutsArgument getArgumentValue() {
        if (expression == null) {
            return this;
        }
        int x = expression.indexOf(eq);
        if (x >= 0) {
            return new DefaultNutsArgument(expression.substring(x + 1), eq);
        }
        return new DefaultNutsArgument(null, eq);
    }

    @Override
    public String getString() {
        return expression;
    }

    @Override
    public String getString(String defaultValue) {
        return expression == null ? defaultValue : expression;
    }

    @Override
    public boolean isNull() {
        return expression == null;
    }

    @Override
    public boolean isBlank() {
        return expression == null || expression.trim().isEmpty();
    }

    @Override
    public boolean isNegated() {
        if (expression == null) {
            return false;
        }
        int i = 0;
        while (i < expression.length()) {
            switch (expression.charAt(i)) {
                case '-': {
                    //ignore leading dashes
                    break;
                }
                case '+': {
                    //ignore leading dashes
                    break;
                }
                case '!': {
                    return true;
                }
                default: {
                    return false;
                }
            }
            i++;
        }
        return false;
    }

    @Override
    public boolean isEnabled() {
        if (expression == null) {
            return true;
        }
        int i = 0;
        boolean opt = false;
        boolean slash = false;
        while (i < expression.length()) {
            switch (expression.charAt(i)) {
                case '-': {
                    opt = true;
                    break;
                }
                case '+': {
                    opt = true;
                    break;
                }
                case '/': {
                    if (!opt) {
                        return false;
                    }
                    if (slash) {
                        return false;
                    }
                    slash = true;
                    break;
                }
                default: {
                    return true;
                }
            }
            i++;
        }
        return true;
    }

    @Override
    public boolean isInt() {
        try {
            if (expression != null) {
                Integer.parseInt(expression);
                return true;
            }
        } catch (NumberFormatException ex) {
            //ignore
        }
        return false;
    }

    @Override
    public int getInt() {
        if (CoreStringUtils.isBlank(expression)) {
            throw new NumberFormatException("Missing value");
        }
        return Integer.parseInt(expression);
    }

    @Override
    public int getInt(int defaultValue) {
        if (CoreStringUtils.isBlank(expression)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(expression);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public boolean isLong() {
        try {
            if (expression != null) {
                Long.parseLong(expression);
                return true;
            }
        } catch (NumberFormatException ex) {
            //ignore
        }
        return false;
    }

    @Override
    public long getLong() {
        if (CoreStringUtils.isBlank(expression)) {
            throw new NumberFormatException("Missing value");
        }
        return Long.parseLong(expression);
    }

    @Override
    public long getLong(long defaultValue) {
        if (CoreStringUtils.isBlank(expression)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(expression);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public boolean isDouble() {
        try {
            if (expression != null) {
                Double.parseDouble(expression);
                return true;
            }
        } catch (NumberFormatException ex) {
            //ignore
        }
        return false;
    }

    @Override
    public double getDouble() {
        if (CoreStringUtils.isBlank(expression)) {
            throw new NumberFormatException("Missing value");
        }
        return Double.parseDouble(expression);
    }

    @Override
    public double getDouble(double defaultValue) {
        if (CoreStringUtils.isBlank(expression)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(expression);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean() {
        boolean b = CoreCommonUtils.parseBoolean(expression, false);
        if (isNegated()) {
            return !b;
        }
        return b;
    }

    @Override
    public boolean isBoolean() {
        return CoreCommonUtils.parseBoolean(expression, null) != null;
    }

    @Override
    public Boolean getBoolean(Boolean defaultValue) {
        return CoreCommonUtils.parseBoolean(expression, defaultValue);
    }

    @Override
    public String toString() {
        return String.valueOf(expression);
    }

    @Override
    public NutsArgument required() {
        if (expression == null) {
            throw new NoSuchElementException("Missing value");
        }
        return this;
    }

    @Override
    public String getStringKey() {
        return getArgumentKey().getString();
    }

    @Override
    public String getStringValue() {
        return getArgumentValue().getString();
    }

    @Override
    public boolean getBooleanValue() {
        return getArgumentValue().getBoolean();
    }

    @Override
    public String getStringValue(String defaultValue) {
        return getArgumentValue().getString(defaultValue);
    }
}
