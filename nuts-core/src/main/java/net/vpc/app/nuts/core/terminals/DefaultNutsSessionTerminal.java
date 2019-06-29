package net.vpc.app.nuts.core.terminals;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsQuestion;

import java.io.*;
import java.util.Scanner;
import net.vpc.app.nuts.core.util.fprint.FPrint;

public class DefaultNutsSessionTerminal implements NutsSessionTerminal {

    protected NutsWorkspace ws;
    protected final OutInfo out = new OutInfo(true);
    protected final OutInfo err = new OutInfo(false);
    protected InputStream in;
    protected BufferedReader inReader;
    protected NutsTerminalBase parent;
    protected NutsTerminalMode mode = NutsTerminalMode.FORMATTED;

    @Override
    public void install(NutsWorkspace workspace) {
        this.ws = workspace;
        this.out.session = this;
        this.err.session = this;
    }

    @Override
    public void setParent(NutsTerminalBase parent) {
        this.parent = parent;
    }

    @Override
    public NutsTerminalBase getParent() {
        return parent;
    }

    @Override
    public NutsSessionTerminal setTerminalMode(NutsTerminalMode mode) {
        if (mode == null) {
            mode = NutsTerminalMode.INHERITED;
        }
        this.mode = mode;
        return this;
    }

    @Override
    public NutsTerminalMode getTerminalMode() {
        return mode;
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
    public InputStream in() {
        return getIn();
    }

    @Override
    public InputStream getIn() {
        if (this.in != null) {
            return this.in;
        }
        if (parent != null) {
            return parent.getIn();
        }
        return null;
    }

    @Override
    public PrintStream getOut() {
        return this.out.curr(mode);
    }

    @Override
    public PrintStream getErr() {
        return this.err.curr(mode);
    }

    @Override
    public void setIn(InputStream in) {
        this.in = in;
        this.inReader = null;
    }

    @Override
    public void setOut(PrintStream out) {
        this.out.setBase(out);
    }

    @Override
    public void setErr(PrintStream err) {
        this.err.setBase(err);
    }

    public BufferedReader getReader() {
        if (this.inReader != null) {
            return this.inReader;
        }
        final InputStream _in = getIn();
        if (_in != null) {
            this.inReader = new BufferedReader(new InputStreamReader(_in));
        }
        return this.inReader;
    }

    @Override
    public String readLine(PrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = out();
        }
        if (out == null) {
            out = FPrint.out();
        }
        if (this.in == null && parent != null) {
            if (this.out == null) {
                return parent.readLine(out, prompt, params);
            } else {
                return parent.readLine(out, prompt, params);
            }
        }
        out.printf(prompt, params);
        out.flush();
        try {
            return getReader().readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean isInOverridden() {
        return in != null;
    }

    public boolean isOutOverridden() {
        return out != null;
    }

    public boolean isErrOverridden() {
        return err != null;
    }

    @Override
    public String readLine(String prompt, Object... params) {
        return readLine(out(), prompt, params);
    }

    @Override
    public char[] readPassword(String prompt, Object... params) {
        return readPassword(out(), prompt, params);
    }

    @Override
    public char[] readPassword(PrintStream out, String prompt, Object... params) {
        if (out == null) {
            out = out();
        }
        if (out == null) {
            out = FPrint.out();
        }

        if (this.in == null && parent != null) {
            if (this.out == null) {
                return parent.readPassword(out, prompt, params);
            } else {
                return parent.readPassword(out, prompt, params);
            }
        }

        InputStream in = getIn();
        Console cons = null;
        char[] passwd = null;
        if (in == null) {
            in = System.in;
        }
        if (in == System.in && ((cons = System.console()) != null)) {
            if ((passwd = cons.readPassword(prompt, params)) != null) {
                return passwd;
            } else {
                return null;
            }
        } else {
            out.printf(prompt, params);
            out.flush();
            Scanner s = new Scanner(in);
            return s.nextLine().toCharArray();
        }
    }

    protected void copyFrom(DefaultNutsSessionTerminal other) {
        this.ws = other.ws;
        this.parent = other.parent;
        this.mode = other.mode;
        this.out.setBase(other.out.base);;
        this.err.setBase(other.err.base);;
        this.in = other.in;
        this.inReader = other.inReader;
    }

    @Override
    public <T> NutsQuestion<T> ask() {
        return new DefaultNutsQuestion<T>(ws, this, out());
    }

    @Override
    public NutsSessionTerminal copy() {
        final DefaultNutsSessionTerminal r = new DefaultNutsSessionTerminal();
        r.copyFrom(this);
        return r;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public void uninstall() {

    }

    protected static class OutInfo {
        PrintStream base;
        PrintStream baseOld;
        PrintStream formatted;
        PrintStream filtered;
        boolean typeOut;
        DefaultNutsSessionTerminal session;

        public OutInfo(boolean out) {
            this.typeOut = out;
        }

//        NutsTerminalMode evalMode(PrintStream b) {
//            NutsTerminalMode currMode = NutsTerminalMode.INHERITED;
//            if (b == null) {
//                currMode = NutsTerminalMode.INHERITED;
//            } else if (b instanceof NutsFormattedPrintStream) {
//                currMode = NutsTerminalMode.FORMATTED;
//            } else if (b instanceof NutsNonFormattedPrintStream) {
//                currMode = NutsTerminalMode.FILTERED;
//            } else {
//                currMode = NutsTerminalMode.INHERITED;
//            }
//            return currMode;
//        }

        void setBase(PrintStream b) {
            if (this.base != b) {
                this.base = b;
                this.formatted = null;
                this.filtered = null;
            }
        }

        PrintStream base() {
            PrintStream _out = base;
            if (_out != null) {
                return _out;
            }
            if (session.parent != null) {
                return typeOut ? session.parent.getOut() : session.parent.getErr();
            }
            _out = typeOut ? FPrint.out() : FPrint.err();
            if (_out != null) {
                return _out;
            }
            return typeOut ? System.out : System.err;
        }

        PrintStream formatted() {
            PrintStream b = base();
            if (b == null) {
                return null;
            }
            if (formatted == null || baseOld!=b) {
                baseOld=b;
                formatted = session.ws.io().createPrintStream(b, NutsTerminalMode.FORMATTED);
            }
            return formatted;
        }

        PrintStream filtered() {
            PrintStream b = base();
            if (b == null) {
                return null;
            }
            if (filtered == null || baseOld!=b) {
                baseOld=b;
                filtered = session.ws.io().createPrintStream(b, NutsTerminalMode.FILTERED);
            }
            return filtered;
        }

        PrintStream curr(NutsTerminalMode mode) {
            switch (mode) {
                case FORMATTED:
                    return formatted();
                case FILTERED:
                    return filtered();
            }
            return base;
        }
    }
}
