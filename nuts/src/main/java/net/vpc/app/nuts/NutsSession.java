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

import java.util.Map;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsSession extends NutsTerminalProvider, NutsPropertiesProvider {

    boolean isTrace();

    NutsSession setTrace(boolean trace);

    NutsSession trace();

    NutsSession trace(boolean trace);

    boolean isForce();

    /**
     * arm or disarm force install non already installed components
     *
     * @param enable if true force install if not yet installed
     * @return current builder instance
     */
    NutsSession setForce(boolean enable);

    /**
     * @see #setForce(boolean)
     * @return
     */
    NutsSession force();

    NutsSession force(boolean force);

    NutsOutputFormat getOutputFormat(NutsOutputFormat defaultValue);

    NutsOutputFormat getOutputFormat();

    NutsOutputListFormat getOutputCustomFormat();

    NutsSession outputCustomFormat(NutsOutputListFormat customFormat);

    NutsSession setOutputCustomFormat(NutsOutputListFormat customFormat);

    NutsSession outputFormat(NutsOutputFormat outputFormat);

    NutsSession setOutputFormat(NutsOutputFormat outputFormat);

    NutsSession json();

    NutsSession plain();

    NutsSession props();

    NutsSession tree();

    NutsSession table();

    NutsSession xml();

    NutsSession copy();

    NutsSession addListeners(NutsListener listener);

    NutsSession removeListeners(NutsListener listener);

    <T extends NutsListener> T[] getListeners(Class<T> type);

    NutsListener[] getListeners();

    NutsSession setTerminal(NutsSessionTerminal terminal);

    @Override
    NutsSession setProperty(String key, Object value);

    @Override
    NutsSession setProperties(Map<String, Object> properties);

    NutsSession ask();

    NutsSession ask(boolean enable);

    NutsSession setAsk(boolean enable);

    boolean isAsk();

    boolean configureFirst(NutsCommandLine cmd);

    boolean configure(NutsCommandLine commandLine, boolean skipIgnored);
}
