/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.commands;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsUndeployInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsUndeployInternalExecutable(String[] args, NutsSession session) {
        super("undeploy", args, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        getSession().getWorkspace().undeploy().setSession(getSession().setTrace(true)).configure(false, args).run();
    }

}