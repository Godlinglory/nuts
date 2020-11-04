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
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts.runtime.wscommands;

import net.thevpc.nuts.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public abstract class AbstractNutsAddUserCommand extends NutsWorkspaceCommandBase<NutsAddUserCommand> implements NutsAddUserCommand {

    protected String login;
    protected String remoteIdentity;
    protected char[] remoteCredentials;
    protected char[] password;
    protected final Set<String> permissions = new HashSet<>();
    protected final Set<String> groups = new HashSet<>();
    protected NutsRepository repo;

    public AbstractNutsAddUserCommand(NutsWorkspace ws) {
        super(ws, "add-user");
    }

    public AbstractNutsAddUserCommand(NutsRepository repo) {
        super(repo.getWorkspace(), "add-user");
        this.repo = repo;
    }

    @Override
    public String getUsername() {
        return login;
    }


    @Override
    public AbstractNutsAddUserCommand setUsername(String username) {
        this.login = username;
        return this;
    }

    @Override
    public char[] getCredentials() {
        return password;
    }


    @Override
    public AbstractNutsAddUserCommand setCredentials(char[] password) {
        this.password = password;
        return this;
    }

    @Override
    public char[] getRemoteCredentials() {
        return remoteCredentials;
    }


    @Override
    public AbstractNutsAddUserCommand setRemoteCredentials(char[] password) {
        this.remoteCredentials = password;
        return this;
    }

    @Override
    public String getRemoteIdentity() {
        return remoteIdentity;
    }

    @Override
    public AbstractNutsAddUserCommand setRemoteIdentity(String remoteIdentity) {
        this.remoteIdentity = remoteIdentity;
        return this;
    }

    @Override
    public AbstractNutsAddUserCommand addPermissions(String... permissions) {
        if (permissions != null) {
            return addPermissions(Arrays.asList(permissions));
        }
        return this;
    }

    @Override
    public AbstractNutsAddUserCommand removePermissions(String... permissions) {
        if (permissions != null) {
            return removePermissions(Arrays.asList(permissions));
        }
        return this;
    }

    @Override
    public AbstractNutsAddUserCommand addPermissions(Collection<String> permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (permission != null) {
                    this.permissions.add(permission);
                }
            }
        }
        return this;
    }

    @Override
    public AbstractNutsAddUserCommand removePermissions(Collection<String> permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (permission != null) {
                    this.permissions.remove(permission);
                }
            }
        }
        return this;
    }

    @Override
    public AbstractNutsAddUserCommand addGroups(String... groups) {
        if (groups != null) {
            return addPermissions(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public AbstractNutsAddUserCommand removeGroups(String... groups) {
        if (groups != null) {
            return removeGroups(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public AbstractNutsAddUserCommand addGroup(String group) {
        if (group != null) {
            this.groups.add(group);
        }
        return this;
    }

    @Override
    public AbstractNutsAddUserCommand addPermission(String permission) {
        if (permission != null) {
            this.permissions.add(permission);
        }
        return this;
    }

    @Override
    public AbstractNutsAddUserCommand addGroups(Collection<String> groups) {
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
    public AbstractNutsAddUserCommand removeGroups(Collection<String> groups) {
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
    public String[] getPermissions() {
        return permissions.toArray(new String[0]);
    }

    @Override
    public String[] getGroups() {
        return groups.toArray(new String[0]);
    }


    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }

            }
        }
        return false;
    }
}
