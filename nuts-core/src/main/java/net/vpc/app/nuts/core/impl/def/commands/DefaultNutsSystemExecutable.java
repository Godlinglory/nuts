/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.impl.def.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

/**
 * @author vpc
 */
public class DefaultNutsSystemExecutable extends AbstractNutsExecutableCommand {

    String[] cmd;
    String[] executorOptions;
    NutsSession session;
    private boolean showCommand = false;
    NutsExecCommand execCommand;

    public DefaultNutsSystemExecutable(String[] cmd, String[] executorOptions, NutsSession session, NutsExecCommand execCommand) {
        super(cmd[0],
                session.getWorkspace().commandLine().setArguments(cmd).toString(),
                NutsExecutableType.SYSTEM);
        this.cmd = cmd;
        this.execCommand = execCommand;
        this.executorOptions = executorOptions == null ? new String[0] : executorOptions;
        this.session = session;
        NutsCommandLine cmdLine = session.getWorkspace().commandLine().setArguments(this.executorOptions);
        while (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.peek();
            switch (a.getStringKey()) {
                case "--show-command": {
                    showCommand = cmdLine.nextBoolean().getBooleanValue();
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

    @Override
    public void execute() {
        Map<String, String> e2 = null;
        Properties env1 = execCommand.getEnv();
        if (env1 != null) {
            e2 = new HashMap<>((Map) env1);
        }
        CoreIOUtils.execAndWait(session.getWorkspace(), execCommand.getCommand(), e2, session.getWorkspace().io().path(execCommand.getDirectory()), session.getTerminal(), showCommand, true)
                .exec();
    }

    @Override
    public void dryExecute() {
        Map<String, String> e2 = null;
        Properties env1 = execCommand.getEnv();
        if (env1 != null) {
            e2 = new HashMap<>((Map) env1);
        }
        CoreIOUtils.execAndWait(session.getWorkspace(), execCommand.getCommand(), e2, session.getWorkspace().io().path(execCommand.getDirectory()), session.getTerminal(), showCommand, true)
                .dryExec();
    }

    @Override
    public String getHelpText() {
        switch (NutsPlatformUtils.getPlatformOsFamily()) {
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
        return "SYSEXEC " + session.workspace().commandLine().setArguments(cmd).toString();
    }

}