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

/**
 * The processor is called to process the command line arguments.
 * <ul>
 *  <li>{@code init}: called initially</li>
 *  <li>{@code processOption}|{@code processNonOption}: called multiple times until the command line is consumed</li>
 *  <li>{@code prepare}: called when the command line is fully consumed</li>
 *  <li>{@code exec}|{@code autoComplete}: called to process execution of autcomplete</li>
 * </ul>
 *
 * @author thevpc
 * @app.category Command Line
 */
public interface NutsCommandLineProcessor {
    NutsCommandLineProcessor NOP = new NutsCommandLineProcessor() {
        @Override
        public boolean onNextOption(NutsArgument option, NutsCommandLine commandline) {
            return false;
        }

        @Override
        public boolean onNextNonOption(NutsArgument nonOption, NutsCommandLine commandline) {
            return false;
        }

        @Override
        public void onExec() {

        }
    };

    /**
     * process the given option argument that was peeked from the command line.
     * Implementations <strong>MUST</strong> call one of
     * the "next" methods to
     *
     * @param option      peeked argument
     * @param commandline associated commandline
     * @return true if the argument can be processed, false otherwise.
     */
    boolean onNextOption(NutsArgument option, NutsCommandLine commandline);

    /**
     * process the given non option argument that was peeked from the command line.
     * Implementations <strong>MUST</strong> call one of
     * the "next" methods to
     *
     * @param nonOption   peeked argument
     * @param commandline associated commandline
     * @return true if the argument can be processed, false otherwise.
     */
    boolean onNextNonOption(NutsArgument nonOption, NutsCommandLine commandline);

    /**
     * initialize the processor.
     * Called before any other method.
     *
     * @param commandline associated commandline
     */
    default void onInit(NutsCommandLine commandline) {

    }

    /**
     * prepare for execution of for auto-complete.
     * Called after all next methods and before exec and autoComplete methods
     *
     * @param commandline associated commandline
     */
    default void onPrepare(NutsCommandLine commandline) {

    }

    /**
     * execute options, called after all options was processed and
     * cmdLine.isExecMode() return true.
     */
    void onExec();

    /**
     * called when auto-complete ({@code autoComplete} is not null)
     *
     * @param autoComplete autoComplete instance
     */
    default void onAutoComplete(NutsCommandAutoComplete autoComplete) {

    }

}
