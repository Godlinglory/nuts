/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.commands;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.core.NutsWorkspaceExt;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsWelcomeInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsWelcomeInternalExecutable(String[] args, NutsSession session) {
        super("welcome", args, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        getSession().out().println(NutsWorkspaceExt.of(getSession().getWorkspace()).getWelcomeText());
    }

}
