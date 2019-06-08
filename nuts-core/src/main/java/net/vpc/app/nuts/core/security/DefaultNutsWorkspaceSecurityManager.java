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
package net.vpc.app.nuts.core.security;

import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultNutsAddUserCommand;
import net.vpc.app.nuts.core.DefaultNutsRemoveUserCommand;
import net.vpc.app.nuts.core.DefaultNutsUpdateUserCommand;
import net.vpc.app.nuts.core.DefaultNutsWorkspace;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsWorkspaceSecurityManager implements NutsWorkspaceSecurityManager {

    public static final Logger LOG = Logger.getLogger(DefaultNutsWorkspaceSecurityManager.class.getName());
    private final ThreadLocal<Stack<LoginContext>> loginContextStack = new ThreadLocal<>();
    private final DefaultNutsWorkspace ws;
    private final WrapperNutsAuthenticationAgent agent;

    public DefaultNutsWorkspaceSecurityManager(final DefaultNutsWorkspace ws) {
        this.ws = ws;
        this.agent = new WrapperNutsAuthenticationAgent(ws, ws.config(), x -> getAuthenticationAgent(x));
    }

    @Override
    public NutsWorkspaceSecurityManager login(final String login, final char[] password) {
        login(new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        NameCallback nameCallback = (NameCallback) callback;
                        nameCallback.setName(login);
                    } else if (callback instanceof PasswordCallback) {
                        PasswordCallback passwordCallback = (PasswordCallback) callback;
                        passwordCallback.setPassword(password);
                    } else {
                        throw new UnsupportedCallbackException(callback, "The submitted Callback is unsupported");
                    }
                }
            }
        });
        return this;
    }

    @Override
    public boolean switchUnsecureMode(char[] adminPassword) {
        if (adminPassword == null) {
            adminPassword = new char[0];
        }
        NutsEffectiveUser adminSecurity = findUser(NutsConstants.Users.ADMIN);
        if (adminSecurity == null || !adminSecurity.hasCredentials()) {
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.log(Level.CONFIG, NutsConstants.Users.ADMIN + " user has no credentials. reset to default");
            }
            NutsUserConfig u = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(NutsConstants.Users.ADMIN);
            u.setCredentials(CoreStringUtils.chrToStr(createCredentials("admin".toCharArray(), false, null)));
            NutsWorkspaceConfigManagerExt.of(ws.config()).setUser(u);
        }

        char[] credentials = CoreIOUtils.evalSHA1(adminPassword);
        if (Arrays.equals(credentials, adminPassword)) {
            Arrays.fill(credentials, '\0');
            throw new NutsSecurityException(ws, "Invalid credentials");
        }
        Arrays.fill(credentials, '\0');
        boolean activated = false;
        if (isSecure()) {
            NutsWorkspaceConfigManagerExt.of(ws.config()).setSecure(false);
            activated = true;
        }
        return activated;
    }

    @Override
    public boolean isAdmin() {
        return NutsConstants.Users.ADMIN.equals(getCurrentLogin());
    }

    @Override
    public boolean switchSecureMode(char[] adminPassword) {
        if (adminPassword == null) {
            adminPassword = new char[0];
        }
        boolean deactivated = false;
        char[] credentials = CoreIOUtils.evalSHA1(adminPassword);
        if (Arrays.equals(credentials, adminPassword)) {
            Arrays.fill(credentials, '\0');
            throw new NutsSecurityException(ws, "Invalid credentials");
        }
        Arrays.fill(credentials, '\0');
        if (!isSecure()) {
            NutsWorkspaceConfigManagerExt.of(ws.config()).setSecure(true);
            deactivated = true;
        }
        return deactivated;
    }

    @Override
    public NutsWorkspaceSecurityManager logout() {
        Stack<LoginContext> r = loginContextStack.get();
        if (r == null || r.isEmpty()) {
            throw new NutsLoginException(ws, "Not logged in");
        }
        try {
            LoginContext loginContext = r.pop();
            loginContext.logout();
        } catch (LoginException ex) {
            throw new NutsLoginException(ws, ex);
        }
        return this;
    }

    @Override
    public NutsEffectiveUser findUser(String username) {
        NutsUserConfig security = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(username);
        Stack<String> inherited = new Stack<>();
        if (security != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(Arrays.asList(security.getGroups()));
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NutsUserConfig ss = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(s);
                if (ss != null) {
                    inherited.addAll(Arrays.asList(ss.getRights()));
                    for (String group : ss.getGroups()) {
                        if (!visited.contains(group)) {
                            curr.push(group);
                        }
                    }
                }
            }
        }
        return security == null ? null : new DefaultNutsEffectiveUser(security, inherited.toArray(new String[0]));
    }

    @Override
    public NutsEffectiveUser[] findUsers() {
        List<NutsEffectiveUser> all = new ArrayList<>();
        for (NutsUserConfig secu : NutsWorkspaceConfigManagerExt.of(ws.config()).getUsers()) {
            all.add(findUser(secu.getUser()));
        }
        return all.toArray(new NutsEffectiveUser[0]);
    }

    @Override
    public NutsAddUserCommand addUser(String name) {
        return new DefaultNutsAddUserCommand(ws).login(name);
    }

    @Override
    public NutsUpdateUserCommand updateUser(String name) {
        return new DefaultNutsUpdateUserCommand(ws).login(name);
    }

    @Override
    public NutsRemoveUserCommand removeUser(String name) {
        return new DefaultNutsRemoveUserCommand(ws).login(name);
    }

    @Override
    public NutsWorkspaceSecurityManager checkAllowed(String right, String operationName) {
        if (!isAllowed(right)) {
            if (CoreStringUtils.isBlank(operationName)) {
                throw new NutsSecurityException(ws, right + " not allowed!");
            } else {
                throw new NutsSecurityException(ws, operationName + ": " + right + " not allowed!");
            }
        }
        return this;
    }

    @Override
    public boolean isAllowed(String right) {
        if (!isSecure()) {
            return true;
        }
        String name = getCurrentLogin();
        if (CoreStringUtils.isBlank(name)) {
            return false;
        }
        if (NutsConstants.Users.ADMIN.equals(name)) {
            return true;
        }
        Stack<String> items = new Stack<>();
        Set<String> visitedGroups = new HashSet<>();
        visitedGroups.add(name);
        items.push(name);
        while (!items.isEmpty()) {
            String n = items.pop();
            NutsUserConfig s = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(n);
            if (s != null) {
                if (s.containsRight(right)) {
                    return true;
                }
                for (String g : s.getGroups()) {
                    if (!visitedGroups.contains(g)) {
                        visitedGroups.add(g);
                        items.push(g);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String[] getCurrentLoginStack() {
        List<String> logins = new ArrayList<String>();
        Stack<LoginContext> c = loginContextStack.get();
        if (c != null) {
            for (LoginContext loginContext : c) {
                Subject subject = loginContext.getSubject();
                if (subject != null) {
                    for (Principal principal : subject.getPrincipals()) {
                        logins.add(principal.getName());
                        break;
                    }
                }
            }
        }
        if (logins.isEmpty()) {
            if (ws.isInitializing()) {
                logins.add(NutsConstants.Users.ADMIN);
            } else {
                logins.add(NutsConstants.Users.ANONYMOUS);
            }
        }
        return logins.toArray(new String[0]);
    }

    @Override
    public String getCurrentLogin() {
        if (ws.isInitializing()) {
            return NutsConstants.Users.ADMIN;
        }
        String name = null;
        Subject currentSubject = getLoginSubject();
        if (currentSubject != null) {
            for (Principal principal : currentSubject.getPrincipals()) {
                name = principal.getName();
                if (!CoreStringUtils.isBlank(name)) {
                    if (!CoreStringUtils.isBlank(name)) {
                        return name;
                    }
                }
            }
        }
        return NutsConstants.Users.ANONYMOUS;
    }

    private Subject getLoginSubject() {
        LoginContext c = getLoginContext();
        if (c == null) {
            return null;
        }
        return c.getSubject();
    }

    @Override
    public String login(CallbackHandler handler) {
        NutsWorkspaceLoginModule.configure(ws); //initialize it
        //        if (!NutsConstants.Misc.USER_ANONYMOUS.equals(getCurrentLogin())) {
        //            throw new NutsLoginException("Already logged in");
        //        }
        LoginContext login;
        try {
            login = CorePlatformUtils.runWithinLoader(new Callable<LoginContext>() {
                @Override
                public LoginContext call() throws Exception {
                    return new LoginContext("nuts", handler);
                }
            }, NutsWorkspaceLoginModule.class.getClassLoader());
            login.login();
        } catch (LoginException ex) {
            throw new NutsLoginException(ws, ex);
        }
        Stack<LoginContext> r = loginContextStack.get();
        if (r == null) {
            r = new Stack<>();
            loginContextStack.set(r);
        }
        r.push(login);
        return getCurrentLogin();
    }

    private LoginContext getLoginContext() {
        Stack<LoginContext> c = loginContextStack.get();
        if (c == null) {
            return null;
        }
        if (c.isEmpty()) {
            return null;
        }
        return c.peek();
    }

    @Override
    public NutsAuthenticationAgent getAuthenticationAgent(String name) {
        name = CoreStringUtils.trim(name);
        if (CoreStringUtils.isBlank(name)) {
            name = NutsWorkspaceConfigManagerExt.of(ws.config())
                    .getStoredConfig().getAuthenticationAgent();
        }
        NutsAuthenticationAgent a = ws.config().createAuthenticationAgent(name);
        return a;
    }

    @Override
    public NutsWorkspaceSecurityManager setAuthenticationAgent(String authenticationAgent) {

        NutsWorkspaceConfigManagerExt cc = NutsWorkspaceConfigManagerExt.of(ws.config());

        if (cc.createAuthenticationAgent(authenticationAgent) == null) {
            throw new NutsIllegalArgumentException(ws, "Unsupported Authentication Agent " + authenticationAgent);
        }

        NutsWorkspaceConfig conf = cc.getStoredConfig();
        if (!Objects.equals(conf.getAuthenticationAgent(), authenticationAgent)) {
            conf.setAuthenticationAgent(authenticationAgent);
            cc.fireConfigurationChanged();
        }
        return this;
    }

    @Override
    public boolean isSecure() {
        return NutsWorkspaceConfigManagerExt.of(ws.config()).getStoredConfig().isSecure();
    }

    @Override
    public String currentLogin() {
        return getCurrentLogin();
    }

    @Override
    public String[] currentLoginStack() {
        return getCurrentLoginStack();
    }

    @Override
    public void checkCredentials(char[] credentialsId, char[] password) throws NutsSecurityException {
        agent.checkCredentials(credentialsId, password);
    }

    @Override
    public char[] getCredentials(char[] credentialsId) {
        return agent.getCredentials(credentialsId);
    }

    @Override
    public boolean removeCredentials(char[] credentialsId) {
        return agent.removeCredentials(credentialsId);
    }

    @Override
    public char[] createCredentials(char[] credentials, boolean allowRetreive, char[] credentialId) {
        return agent.createCredentials(credentials, allowRetreive, credentialId);
    }

}
