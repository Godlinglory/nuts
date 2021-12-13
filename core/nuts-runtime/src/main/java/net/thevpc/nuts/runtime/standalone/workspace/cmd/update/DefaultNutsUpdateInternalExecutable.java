/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.update;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.app.util.NutsAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNutsExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNutsUpdateInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsUpdateInternalExecutable(String[] args, NutsSession session) {
        super("update", args, session);
    }

    @Override
    public void execute() {
        if (NutsAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        getSession().update()
                .configure(false, args).update();

    }

}
