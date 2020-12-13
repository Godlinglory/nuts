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
 *
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

import java.util.Collection;

/**
 *
 * @author thevpc
 * @since 0.5.5
 * %category Security
 */
public interface NutsUpdateUserCommand extends NutsWorkspaceCommand {

    NutsUpdateUserCommand removeGroup(String group);

    NutsUpdateUserCommand addGroup(String group);

    NutsUpdateUserCommand undoAddGroup(String group);

    NutsUpdateUserCommand addGroups(String... groups);

    NutsUpdateUserCommand undoAddGroups(String... groups);

    NutsUpdateUserCommand addGroups(Collection<String> groups);

    NutsUpdateUserCommand undoAddGroups(Collection<String> groups);

    NutsUpdateUserCommand removePermission(String permission);

    NutsUpdateUserCommand addPermission(String permission);

    NutsUpdateUserCommand undoAddPermission(String permissions);

    NutsUpdateUserCommand addPermissions(String... permissions);

    NutsUpdateUserCommand undoAddPermissions(String... permissions);

    NutsUpdateUserCommand addPermissions(Collection<String> permissions);

    NutsUpdateUserCommand undoAddPermissions(Collection<String> permissions);

    NutsUpdateUserCommand removeGroups(String... groups);

    NutsUpdateUserCommand undoRemoveGroups(String... groups);

    NutsUpdateUserCommand removeGroups(Collection<String> groups);

    NutsUpdateUserCommand undoRemoveGroups(Collection<String> groups);

    NutsUpdateUserCommand removePermissions(String... permissions);

    NutsUpdateUserCommand undoRemovePermissions(String... permissions);

    NutsUpdateUserCommand removePermissions(Collection<String> permissions);

    NutsUpdateUserCommand undoRemovePermissions(Collection<String> permissions);

    NutsUpdateUserCommand credentials(char[] password);

    NutsUpdateUserCommand setCredentials(char[] password);

    NutsUpdateUserCommand oldCredentials(char[] password);

    NutsUpdateUserCommand setOldCredentials(char[] oldCredentials);

    NutsUpdateUserCommand remoteIdentity(String remoteIdentity);

    NutsUpdateUserCommand setRemoteIdentity(String remoteIdentity);

    String getUsername();

    NutsUpdateUserCommand username(String login);

    NutsUpdateUserCommand setUsername(String login);

    boolean isResetPermissions();

    NutsUpdateUserCommand resetPermissions();

    NutsUpdateUserCommand resetPermissions(boolean resetPermissions);

    NutsUpdateUserCommand setResetPermissions(boolean resetPermissions);

    boolean isResetGroups();

    NutsUpdateUserCommand resetGroups();

    NutsUpdateUserCommand resetGroups(boolean resetGroups);

    NutsUpdateUserCommand setResetGroups(boolean resetGroups);

    NutsUpdateUserCommand setRemoteCredentials(char[] password);

    NutsUpdateUserCommand remoteCredentials(char[] password);

    char[] getRemoteCredentials();

    String[] getAddGroups();

    String[] getRemoveGroups();

    char[] getCredentials();

    char[] getOldCredentials();

    String getRemoteIdentity();

    String[] getAddPermissions();

    String[] getRemovePermissions();

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NutsUpdateUserCommand copySession();

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsUpdateUserCommand setSession(NutsSession session);

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
    NutsUpdateUserCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NutsUpdateUserCommand run();
}
