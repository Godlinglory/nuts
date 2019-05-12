/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.commands;

import net.vpc.app.nuts.NutsExecCommand;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class ExecInternalExecutable extends InternalExecutable {
    
    private final NutsExecCommand execCommand;

    public ExecInternalExecutable(String[] args, NutsWorkspace ws, NutsSession session, NutsExecCommand execCommand) {
        super("exec", args, ws, session);
        this.execCommand = execCommand;
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        execCommand.copy().session(getSession(true)).clearCommand().parseOptions(args).failFast().run();
    }
    
}