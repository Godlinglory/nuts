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

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsRepositorySecurityManager {

    boolean isAllowed(String right);

    NutsRepositorySecurityManager checkAllowed(String right, String operationName) throws SecurityException;

    NutsAddUserCommand addUser(String name);

    NutsUpdateUserCommand updateUser(String name);

    NutsRemoveUserCommand removeUser(String name);

    NutsUser[] findUsers();

    NutsUser getEffectiveUser(String username);

    NutsRepositorySecurityManager setAuthenticationAgent(String authenticationAgent, NutsUpdateOptions options);

    NutsAuthenticationAgent getAuthenticationAgent(String id);

    /**
     * check if the given <code>password</code> is valid against the one stored
     * by the Authentication Agent for  <code>credentialsId</code>
     *
     * @param credentialsId credentialsId
     * @param password password
     * @throws NutsSecurityException when check failed
     */
    void checkCredentials(char[] credentialsId, char[] password) throws NutsSecurityException;

    /**
     * get the credentials for the given id. The {@code credentialsId}
     * <strong>MUST</strong> be prefixed with AuthenticationAgent'd id and ':'
     * character
     *
     * @param credentialsId credentials-id
     * @return credentials
     */
    char[] getCredentials(char[] credentialsId);

    /**
     * remove existing credentials with the given id The {@code credentialsId}
     * <strong>MUST</strong> be prefixed with AuthenticationAgent'd id and ':'
     * character
     *
     * @param credentialsId credentials-id
     * @return credentials
     */
    boolean removeCredentials(char[] credentialsId);

    /**
     * store credentials in the agent's and return the credential id to store
     * into the config. if credentialId is not null, the given credentialId will
     * be updated and the credentialId is returned. The {@code credentialsId},if
     * present or returned, <strong>MUST</strong> be prefixed with
     * AuthenticationAgent'd id and ':' character
     *
     * @param credentials credential
     * @param allowRetreive when true {@link #getCredentials(char[])} can be invoked over {@code credentialId}
     * @param credentialId preferred credentialId, if null, a new one is created
     * @return credentials-id
     */
    char[] createCredentials(char[] credentials, boolean allowRetreive, char[] credentialId);
}
