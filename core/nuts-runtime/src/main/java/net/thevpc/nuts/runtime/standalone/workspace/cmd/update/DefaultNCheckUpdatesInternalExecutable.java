/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.update;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NUpdateCommand;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNCheckUpdatesInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNCheckUpdatesInternalExecutable(String[] args, NSession session) {
        super("check-updates", args, session);
    }

    @Override
    public void execute() {
        if(getSession().isDry()){
            dryExecute();
            return;
        }
        if (NAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        NUpdateCommand.of(getSession()).configure(false, args).checkUpdates();
    }

}