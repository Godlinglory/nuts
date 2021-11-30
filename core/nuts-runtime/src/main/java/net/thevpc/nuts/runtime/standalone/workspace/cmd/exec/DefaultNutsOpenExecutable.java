/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNutsOpenExecutable extends AbstractNutsExecutableCommand {

    String[] cmd;
    String[] executorOptions;
    NutsSession session;
    NutsSession execSession;
    NutsExecCommand execCommand;
    private boolean showCommand = false;
    private String[] effectiveOpenExecutable;

    public DefaultNutsOpenExecutable(String[] cmd,
                                     String[] executorOptions, NutsSession session, NutsSession execSession, NutsExecCommand execCommand
    ) {
        super(cmd[0],
                NutsCommandLine.of(cmd, session).toString(),
                NutsExecutableType.SYSTEM);
        this.cmd = cmd;
        this.execCommand = execCommand;
        this.executorOptions = executorOptions == null ? new String[0] : executorOptions;
        this.session = session;
        this.execSession = execSession;
        NutsCommandLine cmdLine = NutsCommandLine.of(this.executorOptions, session);
        while (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.peek();
            switch (a.getKey().getString()) {
                case "--show-command": {
                    showCommand = cmdLine.nextBoolean().getBooleanValue();
                    break;
                }
                default: {
                    cmdLine.skip();
                }
            }
        }
        switch (session.env().getOsFamily()) {
            case LINUX: {
                Path execPath = CoreIOUtils.sysWhich("xdg-open");
                if (execPath != null) {
                    effectiveOpenExecutable = new String[]{execPath.toString()};
                    break;
                }
                execPath = CoreIOUtils.sysWhich("gnome-open");
                if (execPath != null) {
                    effectiveOpenExecutable = new String[]{execPath.toString()};
                    break;
                }
                execPath = CoreIOUtils.sysWhich("cygstart");
                if (execPath != null) {
                    effectiveOpenExecutable = new String[]{execPath.toString()};
                    break;
                }
                break;
            }
            case WINDOWS: {
                effectiveOpenExecutable = new String[]{"cmd", "/c", "start"};
                break;
            }
            case MACOS: {
                Path execPath = CoreIOUtils.sysWhich("open");
                if (execPath != null) {
                    effectiveOpenExecutable = new String[]{execPath.toString()};
                }
                break;
            }
        }
    }

    @Override
    public NutsId getId() {
        return null;
    }

    private NutsExecCommand resolveExecHelper() {
        if (effectiveOpenExecutable == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to resolve viewer for %s", cmd[0]));
        }
        NutsExecCommand cc = execCommand.copy();
        cc.setExecutionType(NutsExecutionType.SYSTEM);
        List<String> ss = new ArrayList<>(Arrays.asList(effectiveOpenExecutable));
        ss.addAll(Arrays.asList(cmd));
        cc.setCommand(ss);
        return cc;
    }

    @Override
    public void execute() {
        resolveExecHelper().setDry(false).run();
    }

    @Override
    public void dryExecute() {
        resolveExecHelper().setDry(true).run();
    }

    @Override
    public String getHelpText() {
        switch (execSession.env().getOsFamily()) {
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
        if (effectiveOpenExecutable == null) {
            return "FAIL TO OPEN " + NutsCommandLine.of(cmd, session).toString();
        }
        return "OPEN with " + effectiveOpenExecutable[0] + " : " + NutsCommandLine.of(cmd, session).toString();
    }

}
