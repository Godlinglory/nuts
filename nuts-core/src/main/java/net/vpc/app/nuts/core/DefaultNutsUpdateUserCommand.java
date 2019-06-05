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
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.core.spi.NutsRepositoryConfigManagerExt;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public class DefaultNutsUpdateUserCommand extends NutsWorkspaceCommandBase<NutsUpdateUserCommand> implements NutsUpdateUserCommand {

    private String login;
    private String remoteIdentity;
//    private boolean remoteIdentityUpdated;
    private boolean resetRights;
    private boolean resetGroups;
    private char[] credentials;
    private char[] oldCredentials;
    private char[] remoteCredentials;
    private final Set<String> rights = new HashSet<>();
    private final Set<String> groups = new HashSet<>();
    private final Set<String> rm_rights = new HashSet<>();
    private final Set<String> rm_groups = new HashSet<>();
    private NutsRepository repo;

    public DefaultNutsUpdateUserCommand(NutsWorkspace ws) {
        super(ws, "update-user");
    }

    public DefaultNutsUpdateUserCommand(NutsRepository repo) {
        super(repo.getWorkspace(), "update-user");
        this.repo = repo;
    }

    @Override
    public char[] getCredentials() {
        return credentials;
    }

    @Override
    public DefaultNutsUpdateUserCommand credentials(char[] password) {
        return setCredentials(password);
    }

    @Override
    public DefaultNutsUpdateUserCommand setCredentials(char[] password) {
        this.credentials = password;
        return this;
    }

    @Override
    public char[] getOldCredentials() {
        return oldCredentials;
    }

    @Override
    public DefaultNutsUpdateUserCommand oldCredentials(char[] password) {
        return setOldCredentials(password);
    }

    @Override
    public DefaultNutsUpdateUserCommand setOldCredentials(char[] password) {
        this.oldCredentials = password;
        return this;
    }

    @Override
    public char[] getRemoteCredentials() {
        return remoteCredentials;
    }

    @Override
    public DefaultNutsUpdateUserCommand remoteCredentials(char[] password) {
        return setRemoteCredentials(password);
    }

    @Override
    public DefaultNutsUpdateUserCommand setRemoteCredentials(char[] password) {
        this.remoteCredentials = password;
        return this;
    }

    @Override
    public String getRemoteIdentity() {
        return remoteIdentity;
    }

    @Override
    public DefaultNutsUpdateUserCommand remoteIdentity(String remoteIdentity) {
        return setRemoteIdentity(remoteIdentity);
    }

    @Override
    public DefaultNutsUpdateUserCommand setRemoteIdentity(String remoteIdentity) {
        this.remoteIdentity = remoteIdentity;
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand addRights(String... rights) {
        if (rights != null) {
            return addRights(Arrays.asList(rights));
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand removeRights(String... rights) {
        if (rights != null) {
            return removeRights(Arrays.asList(rights));
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand undoAddRights(String... rights) {
        if (rights != null) {
            return undoAddRights(Arrays.asList(rights));
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand undoRemoveRights(String... rights) {
        if (rights != null) {
            return undoRemoveRights(Arrays.asList(rights));
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand addRights(Collection<String> rights) {
        if (rights != null) {
            for (String right : rights) {
                if (right != null) {
                    this.rights.add(right);
                }
            }
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand removeRights(Collection<String> rights) {
        if (rights != null) {
            for (String right : rights) {
                if (right != null) {
                    this.rm_rights.add(right);
                }
            }
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand undoAddRights(Collection<String> rights) {
        if (rights != null) {
            for (String right : rights) {
                if (right != null) {
                    this.rights.remove(right);
                }
            }
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand undoRemoveRights(Collection<String> rights) {
        if (rights != null) {
            for (String right : rights) {
                if (right != null) {
                    this.rm_rights.remove(right);
                }
            }
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand addGroups(String... groups) {
        if (groups != null) {
            return addGroups(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand undoAddGroups(String... groups) {
        if (groups != null) {
            return undoAddGroups(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand removeGroups(String... groups) {
        if (groups != null) {
            return removeRights(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand undoRemoveGroups(String... groups) {
        if (groups != null) {
            return undoRemoveGroups(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand addGroup(String group) {
        if (group != null) {
            this.groups.add(group);
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand removeGroup(String group) {
        if (group != null) {
            this.rm_groups.add(group);
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand addRight(String right) {
        if (right != null) {
            this.rights.add(right);
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand undoAddRight(String right) {
        if (right != null) {
            this.rights.remove(right);
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand undoAddGroup(String group) {
        if (group != null) {
            this.groups.remove(group);
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand removeRight(String right) {
        if (right != null) {
            this.rm_rights.add(right);
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand addGroups(Collection<String> groups) {
        if (groups != null) {
            for (String right : groups) {
                if (right != null) {
                    this.groups.add(right);
                }
            }
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand removeGroups(Collection<String> groups) {
        if (groups != null) {
            for (String right : groups) {
                if (right != null) {
                    this.rm_groups.add(right);
                }
            }
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand undoAddGroups(Collection<String> groups) {
        if (groups != null) {
            for (String right : groups) {
                if (right != null) {
                    this.groups.remove(right);
                }
            }
        }
        return this;
    }

    @Override
    public DefaultNutsUpdateUserCommand undoRemoveGroups(Collection<String> groups) {
        if (groups != null) {
            for (String right : groups) {
                if (right != null) {
                    this.rm_groups.remove(right);
                }
            }
        }
        return this;
    }

    @Override
    public String[] getAddRights() {
        return rights.toArray(new String[0]);
    }

    @Override
    public String[] getAddGroups() {
        return groups.toArray(new String[0]);
    }

    @Override
    public String[] getRemoveRights() {
        return rm_rights.toArray(new String[0]);
    }

    @Override
    public String[] getRemoveGroups() {
        return rm_groups.toArray(new String[0]);
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public NutsUpdateUserCommand login(String login) {
        return setLogin(login);
    }

    @Override
    public NutsUpdateUserCommand setLogin(String login) {
        this.login = login;
        return this;
    }

    @Override
    public boolean isResetRights() {
        return resetRights;
    }

    @Override
    public NutsUpdateUserCommand resetRights() {
        return resetRights(true);
    }

    @Override
    public NutsUpdateUserCommand resetRights(boolean resetRights) {
        return setResetRights(resetRights);
    }

    @Override
    public NutsUpdateUserCommand setResetRights(boolean resetRights) {
        this.resetRights = resetRights;
        return this;
    }

    @Override
    public boolean isResetGroups() {
        return resetGroups;
    }

    @Override
    public NutsUpdateUserCommand resetGroups() {
        return resetGroups(true);
    }

    @Override
    public NutsUpdateUserCommand resetGroups(boolean resetGroups) {
        return setResetGroups(resetGroups);
    }

    @Override
    public NutsUpdateUserCommand setResetGroups(boolean resetGroups) {
        this.resetGroups = resetGroups;
        return this;
    }

    @Override
    public NutsUpdateUserCommand run() {
        if (!CoreStringUtils.isBlank(getCredentials())) {
            ws.security().checkAllowed(NutsConstants.Rights.SET_PASSWORD, "set-user-credentials");
            String currentLogin = ws.security().getCurrentLogin();
            if (CoreStringUtils.isBlank(login)) {
                if (!NutsConstants.Users.ANONYMOUS.equals(currentLogin)) {
                    login = currentLogin;
                } else {
                    throw new NutsIllegalArgumentException(ws, "Not logged in");
                }
            }
            if (repo != null) {
                NutsUserConfig u = NutsRepositoryConfigManagerExt.of(repo.config()).getUser(login);
                if (u == null) {
                    throw new NutsIllegalArgumentException(ws, "No such user " + login);
                }
                fillNutsUserConfig(u);

                NutsRepositoryConfigManagerExt.of(repo.config()).setUser(u);

            } else {

                NutsUserConfig u = NutsWorkspaceConfigManagerExt.of(ws.config()).getUser(login);
                if (u == null) {
                    throw new NutsIllegalArgumentException(ws, "No such user " + login);
                }

                fillNutsUserConfig(u);
                NutsWorkspaceConfigManagerExt.of(ws.config()).setUser(u);
            }
        }
        return this;
    }

    protected void fillNutsUserConfig(NutsUserConfig u) {
        String currentLogin = ws.security().getCurrentLogin();
        if (!currentLogin.equals(login)) {
            repo.security().checkAllowed(NutsConstants.Rights.ADMIN, "set-user-credentials");
        }
        if (!ws.security().isAllowed(NutsConstants.Rights.ADMIN)) {
            ws.security().getAuthenticationAgent()
                    .checkCredentials(u.getCredentials().toCharArray(),
                            getOldCredentials());
//
//            if (CoreStringUtils.isEmpty(password)) {
//                throw new NutsSecurityException("Missing old password");
//            }
//            //check old password
//            if (CoreStringUtils.isEmpty(u.getCredentials()) || u.getCredentials().equals(CoreSecurityUtils.evalSHA1(password))) {
//                throw new NutsSecurityException("Invalid password");
//            }
        }
        if (getCredentials() != null) {
            u.setCredentials(CoreStringUtils.chrToStr(ws.security().getAuthenticationAgent().setCredentials(getCredentials(), false, CoreStringUtils.strToChr(u.getCredentials()))));
        }
        if (getRemoteCredentials() != null) {
            u.setRemoteCredentials(CoreStringUtils.chrToStr(ws.security().getAuthenticationAgent().setCredentials(getRemoteCredentials(), true, CoreStringUtils.strToChr(u.getRemoteCredentials()))));
        }

        if (resetGroups) {
            u.setGroups(new String[0]);
        }
        if (resetRights) {
            u.setRights(new String[0]);
        }
        for (String group : groups) {
            u.addGroup(group);
        }
        for (String group : rm_groups) {
            u.removeGroup(group);
        }
        for (String group : rights) {
            u.addRight(group);
        }
        for (String group : rm_rights) {
            u.removeRight(group);
        }
        if (remoteIdentity != null) {
            u.setRemoteIdentity(remoteIdentity);
        }
        if (remoteCredentials != null) {
            u.setRemoteCredentials(remoteIdentity);
        }
    }

    @Override
    public boolean configureFirst(NutsCommand cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getKey().getString()) {
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

}
