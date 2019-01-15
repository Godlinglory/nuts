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

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface NutsCommandExecBuilder {
    NutsCommandExecBuilder setFailFast();
    /**
     * when the execution returns a non zero result, an exception is thrown.
     * Particularly, if grabOutputString is used, error exception will state the output message
     *
     * @return this instance
     */
    NutsCommandExecBuilder setFailFast(boolean failFast);

    /**
     * failFast value
     * @return true if failFast is armed
     */
    boolean isFailFast();

    NutsSession getSession();

    NutsCommandExecBuilder setSession(NutsSession session);

    List<String> getCommand();

    NutsCommandExecBuilder addCommand(String... command);

    NutsCommandExecBuilder addCommand(List<String> command);

    NutsCommandExecBuilder addExecutorOptions(String... executorOptions);

    NutsCommandExecBuilder addExecutorOptions(List<String> executorOptions);

    NutsCommandExecBuilder setCommand(String... command);

    NutsCommandExecBuilder setCommand(List<String> command);

    NutsCommandExecBuilder setExecutorOptions(String... options);

    NutsCommandExecBuilder setExecutorOptions(List<String> options);

    Properties getEnv();

    NutsCommandExecBuilder addEnv(Map<String, String> env);

    NutsCommandExecBuilder setEnv(String k, String val);

    NutsCommandExecBuilder setEnv(Map<String, String> env);

    NutsCommandExecBuilder setEnv(Properties env);

    String getDirectory();

    NutsCommandExecBuilder setDirectory(String directory);

    InputStream getIn();

    NutsCommandExecBuilder setIn(InputStream in);

    PrintStream getOut();

    NutsCommandExecBuilder grabOutputString();

    NutsCommandExecBuilder grabErrorString();

    String getOutputString();

    String getErrorString();

    NutsCommandExecBuilder setOut(PrintStream out);

    NutsCommandExecBuilder setErr(PrintStream err);

    PrintStream getErr();

    NutsCommandExecBuilder exec();

    boolean isNativeCommand();

    boolean isRedirectErrorStream();

    NutsCommandExecBuilder setRedirectErrorStream();

    NutsCommandExecBuilder setRedirectErrorStream(boolean redirectErrorStream);


    NutsCommandExecBuilder setNativeCommand(boolean nativeCommand);

    int getResult();

    String getCommandString();

    String getCommandString(NutsCommandStringFormatter f);
}
