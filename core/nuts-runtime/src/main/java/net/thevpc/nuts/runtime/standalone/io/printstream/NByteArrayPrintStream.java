package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class NByteArrayPrintStream extends NPrintStreamRaw implements NMemoryPrintStream {
    public NByteArrayPrintStream(NTerminalMode mode, NSession session) {
        super(new ByteArrayOutputStream(), mode, null, null, session, new Bindings(), null);
        getOutputMetaData().setMessage(
                NMsg.ofNtf(NTexts.of(session).ofStyled("<memory-buffer>", NTextStyle.path()))
        );
    }

    protected NByteArrayPrintStream(NTerminalMode mode, ByteArrayOutputStream bos, NSession session) {
        super(bos, mode, null, null, session, new Bindings(), null);
        getOutputMetaData().setMessage(
                NMsg.ofNtf(NTexts.of(session).ofStyled("<memory-buffer>", NTextStyle.path()))
        );
    }

    @Override
    public NPrintStream setSession(NSession session) {
        if (session == null || session == this.session) {
            return this;
        }
        return new NByteArrayPrintStream(getTerminalMode(), (ByteArrayOutputStream) out, session);
    }

    @Override
    public byte[] getBytes() {
        flush();
        return ((ByteArrayOutputStream) out).toByteArray();
    }

    @Override
    public String toString() {
        flush();
        return out.toString();
    }

    @Override
    public OutputStream getOutputStream() {
        return asOutputStream();
    }
}
