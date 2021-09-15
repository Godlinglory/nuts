/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.settings;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.wscommands.NutsWorkspaceCommandBase;

/**
 *
 * @author thevpc
 */
public abstract class AbstractNutsRemoveUserCommand extends NutsWorkspaceCommandBase<NutsRemoveUserCommand> implements NutsRemoveUserCommand {

    protected NutsRepository repo;
    protected String login;

    public AbstractNutsRemoveUserCommand(NutsWorkspace ws) {
        super(ws, "remove-user");
    }

    public AbstractNutsRemoveUserCommand(NutsRepository repo) {
        super(repo.getWorkspace(), "remove-user");
        this.repo = repo;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public NutsRemoveUserCommand setUsername(String username) {
        this.login = username;
        return this;
    }


    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
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
