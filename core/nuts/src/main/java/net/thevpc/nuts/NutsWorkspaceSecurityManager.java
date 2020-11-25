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

import javax.security.auth.callback.CallbackHandler;

/**
 * Workspace Security configuration manager
 * @author vpc
 * @since 0.5.4
 * %category Security
 */
public interface NutsWorkspaceSecurityManager {

    /**
     * equivalent to {@link #getCurrentUsername()}.
     *
     * @return current login
     */
    String currentUsername();

    /**
     * current user
     * @return current user
     */
    String getCurrentUsername();

    /**
     * equivalent to {@link #getCurrentLoginStack()}.
     *
     * @return current login
     */
    String[] currentLoginStack();

    /**
     * current user stack.
     * this is useful when login with multiple user identities.
     * @return current user stack
     */
    String[] getCurrentLoginStack();

    /**
     * impersonate user and log as a distinct user with the given credentials.
     * @param username user name
     * @param password user password
     * @return {@code this} instance
     */
    NutsWorkspaceSecurityManager login(String username, char[] password);

    /**
     * impersonate user and log as a distinct user with the given credentials and stack
     * user name so that it can be retrieved using @{code getCurrentLoginStack()}.
     * @param handler security handler
     * @return {@code this} instance
     */
    NutsWorkspaceSecurityManager login(CallbackHandler handler);

    /**
     * log out from last logged in user (if any) and pop out from user name stack.
     * @return {@code this} instance
     */
    NutsWorkspaceSecurityManager logout();

    /**
     * create a User Create command.
     * No user will be added when simply calling this method.
     * You must fill in command parameters then call {@link NutsAddUserCommand#run()}.
     * @param name user name
     * @return create add user command.
     */
    NutsAddUserCommand addUser(String name);

    /**
     * create a Update Create command.
     * No user will be updated when simply calling this method.
     * You must fill in command parameters then call {@link NutsUpdateUserCommand#run()}.
     * @param name user name
     * @return create update user command.
     */
    NutsUpdateUserCommand updateUser(String name);

    /**
     * create a Remove Create command.
     * No user will be removed when simply calling this method.
     * You must fill in command parameters then call {@link NutsRemoveUserCommand#run()}.
     * @param name user name
     * @return create remove user command.
     */
    NutsRemoveUserCommand removeUser(String name);

    /**
     * find all registered users
     * @return all registered users
     * @param session
     */
    NutsUser[] findUsers(NutsSession session);

    /**
     * find user with the given name or null.
     * @param username user name
     * @return user effective information
     */
    NutsUser findUser(String username);

    /**
     * return true if permission is valid and allowed for the current user.
     * @param permission permission name. see {@code NutsConstants.Rights } class
     * @return true if permission is valid and allowed for the current user
     */
    boolean isAllowed(String permission);

    /**
     * check if allowed and throw a Security exception if not.
     * @param permission permission name. see {@code NutsConstants.Rights } class
     * @param operationName operation name
     * @return {@code this} instance
     */
    NutsWorkspaceSecurityManager checkAllowed(String permission, String operationName);

    /**
     * switch from/to secure mode.
     * when secure mode is disabled, no authorizations are checked against.
     * @param secure true if secure mode
     * @param adminPassword password for admin user
     * @param options update options
     * @return true if mode was switched correctly
     * @since 0.5.7
     */
    boolean setSecureMode(boolean secure, char[] adminPassword, NutsUpdateOptions options);

    /**
     * return true if current user has admin privileges
     *
     * @return true if current user has admin privileges
     */
    boolean isAdmin();

    /**
     * update default authentication agent.
     * @param authenticationAgentId  authentication agent id
     * @param options update options
     * @return {@code this} instance
     */
    NutsWorkspaceSecurityManager setAuthenticationAgent(String authenticationAgentId, NutsUpdateOptions options);

    /**
     * get authentication agent with id {@code authenticationAgentId}.
     * if is blank, return default authentication agent
     * @param authenticationAgentId agent id
     * @param session
     * @return authentication agent
     */
    NutsAuthenticationAgent getAuthenticationAgent(String authenticationAgentId, NutsSession session);

    /**
     * return true if workspace is running secure mode
     *
     * @return true if workspace is running secure mode
     */
    boolean isSecure();

    /**
     * check if the given <code>password</code> is valid against the one stored
     * by the Authentication Agent for  <code>credentialsId</code>
     *
     * @param credentialsId credentialsId
     * @param password password
     * @param session
     * @throws NutsSecurityException when check failed
     */
    void checkCredentials(char[] credentialsId, char[] password, NutsSession session) throws NutsSecurityException;

    /**
     * get the credentials for the given id. The {@code credentialsId}
     * <strong>MUST</strong> be prefixed with AuthenticationAgent'd id and ':'
     * character
     *
     * @param credentialsId credentials-id
     * @param session
     * @return credentials
     */
    char[] getCredentials(char[] credentialsId, NutsSession session);

    /**
     * remove existing credentials with the given id The {@code credentialsId}
     * <strong>MUST</strong> be prefixed with AuthenticationAgent'd id and ':'
     * character
     *
     * @param credentialsId credentials-id
     * @param session
     * @return credentials
     */
    boolean removeCredentials(char[] credentialsId, NutsSession session);

    /**
     * store credentials in the agent's and return the credential id to store
     * into the config. if credentialId is not null, the given credentialId will
     * be updated and the credentialId is returned. The {@code credentialsId},if
     * present or returned, <strong>MUST</strong> be prefixed with
     * AuthenticationAgent'd id and ':' character
     *
     * @param credentials credential
     * @param allowRetrieve when true {@link #getCredentials(char[], NutsSession)} can be
     * invoked over {@code credentialId}
     * @param credentialId preferred credentialId, if null, a new one is created
     * @param session session
     * @return credentials-id
     */
    char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId, NutsSession session);

}
