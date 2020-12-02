/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.main.commands;

import net.thevpc.nuts.NutsConfirmationMode;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsUpdateInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsUpdateInternalExecutable(String[] args, NutsSession session) {
        super("update", args, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        getSession().getWorkspace().update().setSession(getSession()
                .setConfirm(NutsConfirmationMode.ASK)
                .setTrace(true)).configure(false, args).update();

    }

}
