package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.io.NContentMetadataProviderFormatSPI;
import net.thevpc.nuts.text.NTextStyle;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamExt extends OutputStream implements NFormattable, NContentMetadataProvider {

    private OutputStream base;
    private NMsg sourceName;
    private NContentMetadata md;
    private NSession session;

    public OutputStreamExt(OutputStream base, NContentMetadata md0,NSession session) {
        this.base = base;
        this.session = session;
        this.md = CoreIOUtils.createContentMetadata(md0,base);
    }

    @Override
    public NContentMetadata getMetaData() {
        return md;
    }

    @Override
    public void write(int b) throws IOException {
        base.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        base.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        base.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        base.flush();
    }

    @Override
    public void close() throws IOException {
        base.close();
    }

    @Override
    public NFormat formatter(NSession session) {
        return NFormat.of(session, new NContentMetadataProviderFormatSPI(this, null, "output-stream"));
    }

    @Override
    public String toString() {
        NPlainPrintStream out = new NPlainPrintStream();
        NOptional<NMsg> m = getMetaData().getMessage();
        if (m.isPresent()) {
            out.print(m.get());
        } else if (sourceName != null) {
            out.print(sourceName, NTextStyle.path());
        } else {
            out.print(getClass().getSimpleName(), NTextStyle.path());
        }
        return out.toString();
    }
}
