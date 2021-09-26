/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.ProcessExecHelper;

import java.util.*;

/**
 * @author thevpc
 */
public class DefaultNutsSystemExecutable extends AbstractNutsExecutableCommand {

    String[] cmd;
    String[] executorOptions;
    NutsSession traceSession;
    NutsSession execSession;
    NutsExecCommand execCommand;
    private boolean showCommand = false;
    private final boolean inheritSystemIO;

    public DefaultNutsSystemExecutable(String[] cmd,
                                       String[] executorOptions, NutsSession traceSession, NutsSession execSession, NutsExecCommand execCommand) {
        super(cmd[0],
                execSession.getWorkspace().commandLine().create(cmd).toString(),
                NutsExecutableType.SYSTEM);
        this.inheritSystemIO = execCommand.isInheritSystemIO();
        this.cmd = cmd;
        this.execCommand = execCommand;
        this.executorOptions = executorOptions == null ? new String[0] : executorOptions;
        this.traceSession = traceSession;
        this.execSession = execSession;
        NutsCommandLine cmdLine = execSession.getWorkspace().commandLine().create(this.executorOptions);
        while (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.peek();
            switch (a.getKey().getString()) {
                case "--show-command": {
                    showCommand = cmdLine.nextBoolean().getValue().getBoolean();
                    break;
                }
                default: {
                    cmdLine.skip();
                }
            }
        }
    }

    @Override
    public NutsId getId() {
        return null;
    }

    private ProcessExecHelper resolveExecHelper() {
        Map<String, String> e2 = null;
        Map<String, String> env1 = execCommand.getEnv();
        if (env1 != null) {
            e2 = new HashMap<>((Map) env1);
        }
        return ProcessExecHelper.ofArgs(
                execCommand.getCommand(), e2,
                CoreIOUtils.toPath(execCommand.getDirectory()),
                traceSession.getTerminal(),
                execSession.getTerminal(), showCommand, true, execCommand.getSleepMillis(),
                inheritSystemIO,
                /*redirectErr*/ false,
                /*fileIn*/ null,
                /*fileOut*/ null,
                execCommand.getRunAs(),
                traceSession);
    }





    @Override
    public void execute() {
        resolveExecHelper().exec();
    }

    @Override
    public void dryExecute() {
        resolveExecHelper().dryExec();
    }

    @Override
    public String getHelpText() {
        switch (execSession.getWorkspace().env().getOsFamily()) {
            case WINDOWS: {
                return "No help available. Try " + getName() + " /help";
            }
            default: {
                return "No help available. Try 'man " + getName() + "' or '" + getName() + " --help'";
            }
        }
    }

    @Override
    public String toString() {
        return execCommand.getRunAs() + "_CMD " + execSession.getWorkspace().commandLine().create(cmd).toString();
    }

}