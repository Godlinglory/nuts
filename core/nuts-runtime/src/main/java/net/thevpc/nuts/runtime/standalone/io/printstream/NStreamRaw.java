package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Print stream from custom output streams like ByteArrayOutputStream
 */
public class NStreamRaw extends NStreamBase {
    protected OutputStream out;
    private PrintStream base;

    protected NStreamRaw(OutputStream out, PrintStream base, Boolean autoFlush, NTerminalMode mode, NSession session, Bindings bindings, NSystemTerminalBase term) {
        super(autoFlush == null || autoFlush, mode, session, bindings, term);
        getOutputMetaData().setMessage(NMsg.ofNtf(NTexts.of(session).ofStyled("<raw-stream>", NTextStyle.path())));
        this.out = out;
        this.base = base;
    }

    public NStreamRaw(OutputStream out, Boolean autoFlush, String encoding, NSession session, Bindings bindings, NSystemTerminalBase term) {
        super(true, NTerminalMode.INHERITED, session, bindings, term);
        getOutputMetaData().setMessage(NMsg.ofNtf(NTexts.of(session).ofStyled("<raw-stream>", NTextStyle.path())));
        this.out = out;
        if (out instanceof PrintStream) {
            PrintStream ps = (PrintStream) out;
            if (autoFlush == null && encoding == null) {
                base = ps;
            }
        }
        if (base == null) {
            try {
                this.base =
                        encoding == null ?
                                new PrintStream(out, autoFlush != null && autoFlush)
                                : new PrintStream(out, autoFlush != null && autoFlush, encoding);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
        }
        switch (getTerminalMode()) {
            case ANSI: {
                if (bindings.ansi != null) {
                    throw new IllegalArgumentException("already bound ansi");
                }
                bindings.ansi = this;
                if (bindings.inherited == null) {
                    bindings.inherited = this;
                }
                break;
            }
            case INHERITED: {
                if (bindings.inherited != null) {
                    throw new IllegalArgumentException("already bound ansi");
                }
                bindings.inherited = this;
                break;
            }
        }
    }

    public PrintStream getBase() {
        return base;
    }

    @Override
    public NStream flush() {
        base.flush();
        return this;
    }

    @Override
    public NStream close() {
        if (getTerminalMode() == NTerminalMode.ANSI) {
            write("\033[0m".getBytes());
            flush();
        }
        base.close();
        return this;
    }

    @Override
    public NStream write(int b) {
        base.write(b);
        return this;
    }

    @Override
    public NStream write(byte[] buf, int off, int len) {
        base.write(buf, off, len);
        return this;
    }

    @Override
    public NStream write(char[] s, int off, int len) {
        if (s == null) {
            base.print("null");
        } else {
            base.print(new String(s, off, len));
        }
        return this;
    }

    @Override
    public NStream print(String s) {
        base.print(s);
        return this;
    }

    @Override
    public NStream setSession(NSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NStreamRaw(out, base, autoFlash, getTerminalMode(), session, new Bindings(), getTerminal());
    }

    @Override
    public NStream run(NTerminalCommand command, NSession session) {
        return this;
    }

    @Override
    public NStream print(char[] s) {
        base.print(s);
        return this;
    }

    @Override
    protected NStream convertImpl(NTerminalMode other) {
        switch (other) {
            case FORMATTED: {
                return new NStreamFormatted(this, getSession(), bindings);
            }
            case FILTERED: {
                return new NStreamFiltered(this, getSession(), bindings);
            }
        }
        throw new NIllegalArgumentException(getSession(), NMsg.ofCstyle("unsupported %s -> %s", getTerminalMode(), other));
    }

    @Override
    public OutputStream getOutputStream() {
        return asOutputStream();
    }
}
