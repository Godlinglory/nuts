/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;

import java.util.*;

import net.thevpc.nuts.runtime.core.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.DefaultNutsAddUserCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.DefaultNutsRemoveUserCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.DefaultNutsUpdateUserCommand;
import net.thevpc.nuts.runtime.standalone.repos.DefaultNutsRepoConfigManager;

/**
 *
 * @author thevpc
 */
public class DefaultNutsRepositorySecurityModel {

//    private final NutsLogger LOG;

    private final NutsRepository repository;
    private final WrapperNutsAuthenticationAgent agent;
    private final Map<String, NutsAuthorizations> authorizations = new HashMap<>();

    public DefaultNutsRepositorySecurityModel(final NutsRepository repo) {
        this.repository = repo;
        this.agent = new WrapperNutsAuthenticationAgent(repo.getWorkspace(), (session) -> repo.env().setSession(session).toMap(), (x, s) -> getAuthenticationAgent(x, s));
        this.repository.addRepositoryListener(new NutsRepositoryListener() {

            public void onConfigurationChanged(NutsRepositoryEvent event) {
                authorizations.clear();
            }
        });
//        LOG = repo.getWorkspace().log().of(DefaultNutsRepositorySecurityModel.class);
    }

    public void checkAllowed(String right, String operationName, NutsSession session) {
        NutsWorkspaceUtils.checkSession(repository.getWorkspace(), session);
        if (!isAllowed(right, session)) {
            if (CoreStringUtils.isBlank(operationName)) {
                throw new NutsSecurityException(session, right + " not allowed!");
            } else {
                throw new NutsSecurityException(session, operationName + ": " + right + " not allowed!");
            }
        }
//        return this;
    }

    public NutsAddUserCommand addUser(String name, NutsSession session) {
        return new DefaultNutsAddUserCommand(repository);
    }

    public NutsUpdateUserCommand updateUser(String name, NutsSession session) {
        return new DefaultNutsUpdateUserCommand(repository);
    }

    public NutsRemoveUserCommand removeUser(String name, NutsSession session) {
        return new DefaultNutsRemoveUserCommand(repository);
    }

    private NutsAuthorizations getAuthorizations(String n, NutsSession session) {
        NutsAuthorizations aa = authorizations.get(n);
        if (aa != null) {
            return aa;
        }
        NutsUserConfig s = NutsRepositoryConfigManagerExt.of(repository.config())
                .getModel()
                .getUser(n, session);
        if (s != null) {
            String[] rr = s.getPermissions();
            aa = new NutsAuthorizations(Arrays.asList(rr == null ? new String[0] : rr));
            authorizations.put(n, aa);
        } else {
            aa = new NutsAuthorizations(Collections.emptyList());
        }
        return aa;
    }

    public boolean isAllowed(String right, NutsSession session) {
        NutsWorkspaceSecurityManager sec = repository.getWorkspace().security().setSession(session);
        if (!sec.isSecure()) {
            return true;
        }
        String name = sec.getCurrentUsername();
        if (NutsConstants.Users.ADMIN.equals(name)) {
            return true;
        }
        Stack<String> items = new Stack<>();
        Set<String> visitedGroups = new HashSet<>();
        visitedGroups.add(name);
        items.push(name);
        while (!items.isEmpty()) {
            String n = items.pop();
            NutsAuthorizations s = getAuthorizations(n, session);
            Boolean ea = s.explicitAccept(right);
            if (ea != null) {
                return ea;
            }
            NutsUserConfig uc = NutsRepositoryConfigManagerExt.of(repository.config())
                    .getModel()
                    .getUser(n, session);
            if (uc != null && uc.getGroups() != null) {
                for (String g : uc.getGroups()) {
                    if (!visitedGroups.contains(g)) {
                        visitedGroups.add(g);
                        items.push(g);
                    }
                }
            }
        }
        return sec
                .isAllowed(right);
    }

    public NutsUser[] findUsers(NutsSession session) {
        List<NutsUser> all = new ArrayList<>();
        for (NutsUserConfig secu : NutsRepositoryConfigManagerExt.of(repository.config())
                .getModel()
                .getUsers(session)) {
            all.add(getEffectiveUser(secu.getUser(), session));
        }
        return all.toArray(new NutsUser[0]);
    }

    public NutsUser getEffectiveUser(String username, NutsSession session) {
        NutsUserConfig u = NutsRepositoryConfigManagerExt.of(repository.config())
                .getModel()
                .getUser(username, session);
        Stack<String> inherited = new Stack<>();
        if (u != null) {
            Stack<String> visited = new Stack<>();
            visited.push(username);
            Stack<String> curr = new Stack<>();
            curr.addAll(Arrays.asList(u.getGroups()));
            while (!curr.empty()) {
                String s = curr.pop();
                visited.add(s);
                NutsUserConfig ss = NutsRepositoryConfigManagerExt.of(repository.config())
                        .getModel()
                        .getUser(s, session);
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

    public NutsAuthenticationAgent getAuthenticationAgent(String id, NutsSession session) {
        id = CoreStringUtils.trim(id);
        if (id.isEmpty()) {
            id = ((DefaultNutsRepoConfigManager) repository.config())
                    .getModel()
                    .getStoredConfig(session).getAuthenticationAgent();
        }
        NutsAuthenticationAgent a = NutsWorkspaceConfigManagerExt.of(repository.getWorkspace().config())
                .getModel()
                .createAuthenticationAgent(id, session);
        return a;
    }

    public void setAuthenticationAgent(String authenticationAgent, NutsSession session) {
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        DefaultNutsRepoConfigManager cc = (DefaultNutsRepoConfigManager) repository.config().setSession(session);

        if (NutsWorkspaceConfigManagerExt.of(repository.getWorkspace().config())
                .getModel().createAuthenticationAgent(authenticationAgent, session) == null) {
            throw new NutsIllegalArgumentException(session, "unsupported Authentication Agent " + authenticationAgent);
        }

        NutsRepositoryConfig conf = cc.getModel().getStoredConfig(session);
        if (!Objects.equals(conf.getAuthenticationAgent(), authenticationAgent)) {
            conf.setAuthenticationAgent(authenticationAgent);
            cc.getModel().fireConfigurationChanged("authentication-agent", session);
        }
//        return this;
    }

    public void checkCredentials(char[] credentialsId, char[] password, NutsSession session) throws NutsSecurityException {
        agent.checkCredentials(credentialsId, password, session);
    }

    public char[] getCredentials(char[] credentialsId, NutsSession session) {
        return agent.getCredentials(credentialsId, session);
    }

    public boolean removeCredentials(char[] credentialsId, NutsSession session) {
        return agent.removeCredentials(credentialsId, session);
    }

    public char[] createCredentials(char[] credentials, boolean allowRetrieve, char[] credentialId, NutsSession session) {
        return agent.createCredentials(credentials, allowRetrieve, credentialId, session);
    }

    public NutsRepository getRepository() {
        return repository;
    }

    public NutsWorkspace getWorkspace() {
        return repository.getWorkspace();
    }

}
