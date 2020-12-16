/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.main.commands;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;

/**
 *
 * @author thevpc
 */
public class DefaultNutsFetchInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsFetchInternalExecutable(String[] args, NutsSession session) {
        super("fetch", args, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        getSession().getWorkspace().fetch().setSession(getSession().setTrace(true)).configure(false, args).run();
    }

}
