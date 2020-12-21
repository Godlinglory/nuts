package net.thevpc.nuts.runtime.standalone.terminals;

import net.thevpc.nuts.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsQuestion;
import net.thevpc.nuts.spi.NutsInputStreamTransparentAdapter;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

public abstract class AbstractSystemTerminalAdapter extends AbstractNutsTerminal implements NutsSystemTerminal {

    private NutsWorkspace ws;
    private NutsSession session;

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        this.ws = workspace;
        NutsSystemTerminalBase parent = getParent();
        if (ws == null) {
            NutsWorkspaceUtils.unsetWorkspace(parent);
        } else {
            NutsWorkspaceUtils.of(ws).setWorkspace(parent);
        }
    }
    @Override
    public void setSession(NutsSession session) {
        this.session=session;
    }


    public abstract NutsSystemTerminalBase getParent();

    @Override
    public NutsSystemTerminal setMode(NutsTerminalMode mode) {
        if (mode == null) {
//            throw new NullPointerException();
        }
        NutsSystemTerminalBase p = getParent();
        if (p instanceof NutsSystemTerminal) {
            ((NutsSystemTerminal) p).setMode(mode);
        } else {
            p.setOutMode(mode);
            p.setErrMode(mode);
        }
        return this;
    }

    @Override
    public NutsSystemTerminal setOutMode(NutsTerminalMode mode) {
        getParent().setOutMode(mode);
        return this;
    }

    @Override
    public NutsTerminalMode getOutMode() {
        return getParent().getOutMode();
    }

    @Override
    public NutsSystemTerminal setErrMode(NutsTerminalMode mode) {
        getParent().setErrMode(mode);
        return this;
    }

    @Override
    public NutsTerminalMode getErrMode() {
        return getParent().getErrMode();
    }

    @Override
    public String readLine(String promptFormat, Object... params) {
        NutsSystemTerminalBase p = getParent();
        if (p instanceof NutsTerminal) {
            return ((NutsTerminal) p).readLine(promptFormat, params);
        } else {
            return getParent().readLine(out(), promptFormat, params);
        }
    }

    @Override
    public char[] readPassword(String prompt, Object... params) {
        NutsSystemTerminalBase p = getParent();
        if (p instanceof NutsTerminal) {
            return ((NutsTerminal) p).readPassword(prompt, params);
        } else {
            return p.readPassword(out(), prompt, params);
        }
    }

    @Override
    public InputStream getIn() {
        return getParent().getIn();
    }

    @Override
    public PrintStream getOut() {
        return getParent().getOut();
    }

    @Override
    public PrintStream getErr() {
        return getParent().getErr();
    }

    @Override
    public InputStream in() {
        return getIn();
    }

    @Override
    public PrintStream out() {
        return getOut();
    }

    @Override
    public PrintStream err() {
        return getErr();
    }

    @Override
    public <T> NutsQuestion<T> ask() {
        NutsSystemTerminalBase p = getParent();
        if (p instanceof NutsTerminal) {
            return ((NutsTerminal) p).ask();
        } else {
            return new DefaultNutsQuestion<T>(
                    ws,
                    this, out()
            );
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsTerminalSpec> criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public boolean isStandardOutputStream(OutputStream out) {
        if (out == null) {
            return true;
        }
        if (out == System.out || out == CoreIOUtils.out(ws)) {
            return true;
        }
        if (out instanceof NutsOutputStreamTransparentAdapter) {
            return isStandardOutputStream(((NutsOutputStreamTransparentAdapter) out).baseOutputStream());
        }
        return false;
    }

    @Override
    public boolean isStandardErrorStream(OutputStream out) {
        if (out == null) {
            return true;
        }
        if (out == System.err || out == CoreIOUtils.err(ws)) {
            return true;
        }
        if (out instanceof NutsOutputStreamTransparentAdapter) {
            return isStandardErrorStream(((NutsOutputStreamTransparentAdapter) out).baseOutputStream());
        }
        return false;
    }

    @Override
    public boolean isStandardInputStream(InputStream in) {
        if (in == null) {
            return true;
        }
        if (in == System.in || in==CoreIOUtils.in(ws)) {
            return true;
        }
        if (in instanceof NutsInputStreamTransparentAdapter) {
            return isStandardInputStream(((NutsInputStreamTransparentAdapter) in).baseInputStream());
        }
        return false;
    }

    @Override
    public String readLine(PrintStream out, String prompt, Object... params) {
        return getParent().readLine(out, prompt, params);
    }

    @Override
    public char[] readPassword(PrintStream out, String prompt, Object... params) {
        return getParent().readPassword(out, prompt, params);
    }

    public boolean isAutoCompleteSupported(){
        return getParent().isAutoCompleteSupported();
    }


}
