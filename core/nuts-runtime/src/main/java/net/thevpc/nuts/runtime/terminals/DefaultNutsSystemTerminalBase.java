package net.thevpc.nuts.runtime.terminals;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.main.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.log.NutsLogVerb;
import net.thevpc.nuts.runtime.format.text.FPrint;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.logging.Level;

@NutsPrototype
public class DefaultNutsSystemTerminalBase implements NutsSystemTerminalBase, NutsWorkspaceAware {

    private NutsLogger LOG;
    private Scanner scanner;
    private NutsTerminalMode outMode = NutsTerminalMode.FORMATTED;
    private NutsTerminalMode errMode = NutsTerminalMode.FORMATTED;
    private PrintStream out;
    private PrintStream err;
    private InputStream in;
    private NutsWorkspace workspace;

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        this.workspace=workspace;
        if(workspace!=null) {
            LOG = ((DefaultNutsWorkspace) workspace).LOG;
//        LOG=workspace.log().of(DefaultNutsSystemTerminalBase.class);
            NutsTerminalMode terminalMode = workspace.config().options().getTerminalMode();
            if (terminalMode == null) {
                terminalMode = NutsTerminalMode.FORMATTED;
            }
            setOutMode(terminalMode);
            setErrMode(terminalMode);
            NutsIOManager ioManager = workspace.io();
            this.out = ioManager.createPrintStream(CoreIOUtils.out(workspace), NutsTerminalMode.FORMATTED);
            this.err = ioManager.createPrintStream(CoreIOUtils.err(workspace), NutsTerminalMode.FORMATTED);//.setColor(NutsPrintStream.RED);
            this.in = CoreIOUtils.in(workspace);
            this.scanner = new Scanner(this.in);
        }else{
            //on uninstall do nothing
        }
    }

    @Override
    public NutsTerminalMode getOutMode() {
        return outMode;
    }

    @Override
    public NutsSystemTerminalBase setOutMode(NutsTerminalMode mode) {
        if (mode == null) {
            mode = NutsTerminalMode.FORMATTED;
        }
        if(LOG!=null) {
            LOG.with().level(Level.CONFIG).verb( NutsLogVerb.UPDATE).formatted().log("change terminal Out mode : ##{0}##", mode.id());
        }
        FPrint.installStdOut(this.outMode = mode,this.workspace);
        return this;
    }

    @Override
    public NutsSystemTerminalBase setErrMode(NutsTerminalMode mode) {
        if (mode == null) {
            mode = NutsTerminalMode.FORMATTED;
        }
        if(LOG!=null) {
            LOG.with().level(Level.CONFIG).verb( NutsLogVerb.UPDATE).formatted().log("change terminal Err mode : ##{0}##", mode.id());
        }
        FPrint.installStdErr(this.errMode = mode,this.workspace);
        return this;
    }

    @Override
    public NutsTerminalMode getErrMode() {
        return errMode;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsTerminalSpec> criteria) {
        return DEFAULT_SUPPORT;
    }

//    @Override
    public String readLine(String prompt, Object... params) {
        return readLine(getOut(), prompt, params);
    }

//    @Override
    public char[] readPassword(String prompt, Object... params) {
        return readPassword(getOut(), prompt, params);
    }

    @Override
    public String readLine(PrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = CoreIOUtils.out(workspace);
        }
        out.printf(prompt, params);
        out.flush();
        return scanner.nextLine();
    }

    @Override
    public char[] readPassword(PrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = CoreIOUtils.out(workspace);
        }
        out.printf(prompt, params);
        return scanner.nextLine().toCharArray();
    }

    @Override
    public InputStream getIn() {
        return this.in;
    }

    @Override
    public PrintStream getOut() {
        return this.out;
    }

    @Override
    public PrintStream getErr() {
        return this.err;
    }

    @Override
    public NutsTerminalBase getParent() {
        return null;
    }

}
