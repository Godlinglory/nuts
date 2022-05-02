package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.runtime.standalone.text.FormatOutputStreamSupport;

public abstract class NutsPrintStreamRendered extends NutsPrintStreamBase {
    protected FormatOutputStreamSupport support;
    protected NutsPrintStreamBase base;
    public NutsPrintStreamRendered(NutsPrintStreamBase base, NutsSession session, NutsTerminalMode mode, Bindings bindings) {
        super(true, mode, session, bindings,base.getTerminal());
        this.base=base;
        this.support =new FormatOutputStreamSupport(new NutsPrintStreamHelper(base),session,base.getTerminal(),
                (mode!=NutsTerminalMode.ANSI && mode!=NutsTerminalMode.FORMATTED)
                );
    }

    public NutsPrintStreamBase getBase() {
        return base;
    }

    @Override
    public NutsPrintStream flush() {
        support.flush();
        base.flush();
        return this;
    }

    @Override
    public NutsPrintStream close() {
        flush();
        base.close();
        return this;
    }

    @Override
    public NutsPrintStream write(int b) {
        support.processByte(b);
        return this;
    }

    @Override
    public NutsPrintStream write(byte[] buf, int off, int len) {
        support.processBytes(buf, off, len);
        return this;
    }

    @Override
    public NutsPrintStream write(char[] buf, int off, int len) {
        support.processChars(buf, 0, buf.length);
        return this;
    }

    @Override
    public NutsPrintStream print(String s) {
        if (s == null) {
            write("null".toCharArray());
        } else {
            write(s.toCharArray());
        }
        return this;
    }

    @Override
    protected NutsPrintStream convertImpl(NutsTerminalMode other) {
        switch (other) {
            case FILTERED: {
                return new NutsPrintStreamFiltered(base, getSession(),bindings);
            }
        }
        throw new NutsIllegalArgumentException(base.getSession(),NutsMessage.cstyle("unsupported %s -> %s",mode(), other));
    }

}
