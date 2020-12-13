/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.security;

import net.thevpc.nuts.*;

import java.util.*;

import net.thevpc.nuts.runtime.core.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.main.wscommands.DefaultNutsAddUserCommand;
import net.thevpc.nuts.runtime.main.wscommands.DefaultNutsRemoveUserCommand;
import net.thevpc.nuts.runtime.main.wscommands.DefaultNutsUpdateUserCommand;
import net.thevpc.nuts.runtime.main.repos.DefaultNutsRepoConfigManager;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;

/**
 *
 * @author thevpc
 */
public class DefaultNutsRepositorySecurityManager implements NutsRepositorySecurityManager {

    private final NutsLogger LOG;

    private final NutsRepository repo;
    private final WrapperNutsAuthenticationAgent agent;
    private final Map<String, NutsAuthorizations> authorizations = new HashMap<>();

    public DefaultNutsRepositorySecurityManager(final NutsRepository repo) {
        this.repo = repo;
        this.agent = new WrapperNutsAuthenticationAgent(repo.getWorkspace(), ()->repo.env().toMap(), x -> getAuthenticationAgent(x, repo.getWorkspace().createSession()));
        this.repo.addRepositoryListener(new NutsRepositoryListener() {
            @Override
            public void onConfigurationChanged(NutsRepositoryEvent event) {
                authorizations.clear();
            }
        });
        LOG=repo.getWorkspace().log().of(DefaultNutsRepositorySecurityManager.class);
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

    private NutsAuthorizations getAuthorizations(String n) {
        NutsAuthorizations aa = authorizations.get(n);
        if (aa != null) {
            return aa;
        }
        NutsUserConfig s = NutsRepositoryConfigManagerExt.of(repo.config()).getUser(n);
        if (s != null) {
            String[] rr = s.getPermissions();
            aa = new NutsAuthorizations(Arrays.asList(rr == null ? new String[0] : rr));
            authorizations.put(n, aa);
        } else {
            aa = new NutsAuthorizations(Collections.emptyList());
        }
        return aa;
    }

    @Override
    public boolean isAllowed(String right) {
        if (!repo.getWorkspace().security().isSecure()) {
            return true;
        }
        String name = repo.getWorkspace().security().getCurrentUsername();
        if (NutsConstants.Users.ADMIN.equals(name)) {
            return true;
        }
        Stack<String> items = new Stack<>();
        Set<String> visitedGroups = new HashSet<>();
        visitedGroups.add(name);
        items.push(name);
        while (!items.isEmpty()) {
            String n = items.pop();
            NutsAuthorizations s = getAuthorizations(n);
            Boolean ea = s.explicitAccept(right);
            if (ea != null) {
                return ea;
            }
            NutsUserConfig uc = NutsRepositoryConfigManagerExt.of(repo.config()).getUser(n);
            if (uc != null && uc.getGroups() != null) {
                for (String g : uc.getGroups()) {
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
    public NutsUser[] findUsers(NutsSession session) {
        List<NutsUser> all = new ArrayList<>();
        for (NutsUserConfig secu : NutsRepositoryConfigManagerExt.of(repo.config()).getUsers()) {
            all.add(getEffectiveUser(secu.getUser(), session));
        }
        return all.toArray(new NutsUser[0]);
    }

    @Override
    public NutsUser getEffectiveUser(String username, NutsSession session) {
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
                    inherited.addAll(Arrays.asList(ss.getPermissions()));
                    for (String group : ss.getGroups()) {
                        if (!visited.contains(group)) {
                            curr.push(group);
                        }
                    }
                }
            }
        }
        return u == null ? null : new DefaultNutsUser(u, inherited.toArray(new String[0]));
    }

    @Override
    public NutsAuthenticationAgent getAuthenticationAgent(String id, NutsSession session) {
        id = CoreStringUtils.trim(id);
        if (id.isEmpty()) {
            id = ((DefaultNutsRepoConfigManager) repo.config())
                    .getStoredConfig().getAuthenticationAgent();
        }
        NutsAuthenticationAgent a = NutsWorkspaceConfigManagerExt.of(repo.getWorkspace().config()).createAuthenticationAgent(id, session);
        return a;
    }

    @Override
    public NutsRepositorySecurityManager setAuthenticationAgent(String authenticationAgent, NutsUpdateOptions options) {
        options= CoreNutsUtils.validate(options,repo.getWorkspace());
        DefaultNutsRepoConfigManager cc = (DefaultNutsRepoConfigManager) repo.config();

        if (NutsWorkspaceConfigManagerExt.of(repo.getWorkspace().config()).createAuthenticationAgent(authenticationAgent, options.getSession()) == null) {
            throw new NutsIllegalArgumentException(repo.getWorkspace(), "Unsupported Authentication Agent " + authenticationAgent);
        }

        NutsRepositoryConfig conf = cc.getStoredConfig();
        if (!Objects.equals(conf.getAuthenticationAgent(), authenticationAgent)) {
            conf.setAuthenticationAgent(authenticationAgent);
            cc.fireConfigurationChanged("authentication-agent",options.getSession());
        }
        return this;
    }

    @Override
    public void checkCredentials(char[] credentialsId, char[] password, NutsSession session) throws NutsSecurityException {
        agent.checkCredentials(credentialsId, password, session);
    }

    @Override
    public char[] getCredentials(char[] credentialsId, NutsSession session) {
        return agent.getCredentials(credentialsId, session);
    }

    @Override
    public boolean removeCredentials(char[] credentialsId, NutsSession session) {
        return agent.removeCredentials(credentialsId, session);
    }

    @Override
    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId, NutsSession session) {
        return agent.createCredentials(credentials, allowRetrieve, credentialId, session);
    }
}
