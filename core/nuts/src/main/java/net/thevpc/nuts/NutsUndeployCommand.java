/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
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
 *
 * @author thevpc
 * @since 0.5.4
 * @category Commands
 */
public interface NutsUndeployCommand extends NutsWorkspaceCommand {

    NutsUndeployCommand setRepository(String repository);

    String getRepository();

    boolean isTransitive();


    NutsUndeployCommand setTransitive(boolean transitive);

    NutsId[] getIds();

    NutsUndeployCommand addId(NutsId id);

    NutsUndeployCommand addId(String id);

    NutsUndeployCommand setOffline(boolean offline);

    boolean isOffline();

    NutsUndeployCommand addIds(String... values);

    NutsUndeployCommand addIds(NutsId... value);

    NutsUndeployCommand clearIds();

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NutsUndeployCommand copySession();

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsUndeployCommand setSession(NutsSession session);

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
    NutsUndeployCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NutsUndeployCommand run();
}