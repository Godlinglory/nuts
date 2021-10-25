/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.spi.NutsSystemTerminalBase;
import net.thevpc.nuts.spi.NutsTerminalSpec;

import java.io.InputStream;

/**
 * @app.category Input Output
 */
public interface NutsTerminalManager {


    /**
     * return workspace system terminal.
     *
     * @return workspace system terminal
     */
    NutsSystemTerminal getSystemTerminal();

    /**
     * update workspace wide system terminal
     *
     * @param terminal system terminal
     * @return {@code this} instance
     */
    NutsTerminalManager setSystemTerminal(NutsSystemTerminalBase terminal);

    NutsTerminalManager enableRichTerm();

    NutsSystemTerminal createSystemTerminal(NutsTerminalSpec spec);

    /**
     * return workspace default terminal
     *
     * @return workspace default terminal
     */
    NutsSessionTerminal getTerminal();

    /**
     * update workspace wide terminal
     *
     * @param terminal terminal
     * @return {@code this} instance
     */
    NutsTerminalManager setTerminal(NutsSessionTerminal terminal);

    /**
     * return new terminal bound to system terminal
     *
     * @return new terminal
     */
    NutsSessionTerminal createTerminal();

    /**
     * return new terminal
     *
     * @param in  in
     * @param out out
     * @param err err
     * @return new terminal
     */
    NutsSessionTerminal createTerminal(InputStream in, NutsPrintStream out, NutsPrintStream err);

    NutsSessionTerminal createTerminal(NutsSessionTerminal terminal);


//    /**
//     * return new terminal bound to the given {@code parent}
//     *
//     * @param parent parent terminal or null
//     * @return new terminal
//     */
//    NutsSessionTerminal createTerminal(NutsSystemTerminalBase parent);

//    /**
//     * prepare PrintStream to handle NutsImmutableString aware format pattern. If the instance
//     * already supports Nuts specific pattern it will be returned unmodified.
//     *
//     * @param out PrintStream to check
//     * @return NutsImmutableString pattern format capable PrintStream
//     */
//    PrintStream prepare(PrintStream out);
//
//    /**
//     * prepare PrintWriter to handle %N (escape) format pattern. If the instance
//     * already supports Nuts specific pattern it will be returned unmodified.
//     *
//     * @param out PrintWriter to check
//     * @return %N pattern format capable PrintWriter
//     */
//    PrintWriter prepare(PrintWriter out);

//    /**
//     * true if the stream is not null and could be resolved as Formatted Output
//     * Stream. If False is returned this does no mean necessarily that the
//     * stream is not formatted.
//     *
//     * @param out stream to check
//     * @return true if formatted
//     */
//    boolean isFormatted(OutputStream out);
//
//    /**
//     * true if the stream is not null and could be resolved as Formatted Output
//     * Stream. If False is returned this does no mean necessarily that the
//     * stream is not formatted.
//     *
//     * @param out stream to check
//     * @return true if formatted
//     */
//    boolean isFormatted(Writer out);

//    NutsTerminalManager sendTerminalCommand(NutsPrintStream out, NutsTerminalCommand command);

    NutsSession getSession();

    NutsTerminalManager setSession(NutsSession session);

}
