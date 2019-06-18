/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.commands;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

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
        getSession().getWorkspace().undeploy().session(getSession().trace()).configure(false, args).run();
    }

}
