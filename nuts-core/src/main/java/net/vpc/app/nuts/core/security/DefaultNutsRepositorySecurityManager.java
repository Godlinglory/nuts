/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.security;

import net.vpc.app.nuts.core.spi.NutsAuthenticationAgentSpi;
import net.vpc.app.nuts.*;

import java.util.*;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultNutsAddUserCommand;
import net.vpc.app.nuts.core.DefaultNutsRemoveUserCommand;
import net.vpc.app.nuts.core.DefaultNutsUpdateUserCommand;
import net.vpc.app.nuts.core.repos.DefaultNutsRepositoryConfigManager;
import net.vpc.app.nuts.core.spi.NutsRepositoryConfigManagerExt;
import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsRepositorySecurityManager implements NutsRepositorySecurityManager {

    private static final Logger LOG = Logger.getLogger(DefaultNutsRepositorySecurityManager.class.getName());

    private final NutsRepository repo;
    private final WrapperNutsAuthenticationAgent agent;

    public DefaultNutsRepositorySecurityManager(final NutsRepository repo) {
        this.repo = repo;
        this.agent = new WrapperNutsAuthenticationAgent(repo.getWorkspace(), repo.config(), x -> getAuthenticationAgent(x));
    }

    @Override
    public NutsRepositorySecurityManager checkAllowed(String right, String operationName) {
        if (!isAllowed(right)) {
            if (CoreStringUtils.isBlank(operationName)) {
                throw new NutsSecurityException(repo.getWorkspace(), right + " not allowed!");
            } else {
                throw new NutsSecurityException(repo.getWorkspace(), operationName + ": " + right + " not allowed!");
            }
        }
        return this;
    }

    @Override
    public NutsAddUserCommand addUser(String name) {
        return new DefaultNutsAddUserCommand(repo);
    }

    @Override
    public NutsUpdateUserCommand updateUser(String name) {
        return new DefaultNutsUpdateUserCommand(repo);
    }

    @Override
    public NutsRemoveUserCommand removeUser(String name) {
        return new DefaultNutsRemoveUserCommand(repo);
    }

    @Override
    public boolean isAllowed(String right) {
        String name = repo.getWorkspace().security().getCurrentLogin();
        if (NutsConstants.Users.ADMIN.equals(name)) {
            return true;
        }
        Stack<String> items = new Stack<>();
        Set<String> visitedGroups = new HashSet<>();
        visitedGroups.add(name);
        items.push(name);
        while (!items.isEmpty()) {
            String n = items.pop();
            NutsUserConfig s = NutsRepositoryConfigManagerExt.of(repo.config()).getUser(n);
            if (s != null) {
                if (s.containsRight("!" + right)) {
                    return false;
                }
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
        return repo.getWorkspace().security().isAllowed(right);
    }

    @Override
    public NutsEffectiveUser[] findUsers() {
        List<NutsEffectiveUser> all = new ArrayList<>();
        for (NutsUserConfig secu : NutsRepositoryConfigManagerExt.of(repo.config()).getUsers()) {
            all.add(getEffectiveUser(secu.getUser()));
        }
        return all.toArray(new NutsEffectiveUser[0]);
    }

    @Override
    public NutsEffectiveUser getEffectiveUser(String username) {
        NutsUserConfig u = NutsRepositoryConfigManagerExt.of(repo.config()).getUser(username);
        Stack<String> inherited = new Stack<>();
        if (u != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(Arrays.asList(u.getGroups()));
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NutsUserConfig ss = NutsRepositoryConfigManagerExt.of(repo.config()).getUser(s);
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
        return u == null ? null : new DefaultNutsEffectiveUser(u, inherited.toArray(new String[0]));
    }

    @Override
    public NutsAuthenticationAgent getAuthenticationAgent(String id) {
        id = CoreStringUtils.trim(id);
        if (id.isEmpty()) {
            id = ((DefaultNutsRepositoryConfigManager) repo.config())
                    .getStoredConfig().getAuthenticationAgent();
        }
        NutsAuthenticationAgent a = NutsWorkspaceConfigManagerExt.of(repo.getWorkspace().config()).createAuthenticationAgent(id);
        return a;
    }

    @Override
    public NutsRepositorySecurityManager setAuthenticationAgent(String authenticationAgent) {

        DefaultNutsRepositoryConfigManager cc = (DefaultNutsRepositoryConfigManager) repo.config();

        if (NutsWorkspaceConfigManagerExt.of(repo.getWorkspace().config()).createAuthenticationAgent(authenticationAgent) == null) {
            throw new NutsIllegalArgumentException(repo.getWorkspace(), "Unsupported Authentication Agent " + authenticationAgent);
        }

        NutsRepositoryConfig conf = cc.getStoredConfig();
        if (!Objects.equals(conf.getAuthenticationAgent(), authenticationAgent)) {
            conf.setAuthenticationAgent(authenticationAgent);
            cc.fireConfigurationChanged();
        }
        return this;
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
