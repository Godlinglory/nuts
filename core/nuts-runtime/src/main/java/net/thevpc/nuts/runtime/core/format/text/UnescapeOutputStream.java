package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsTerminalMode;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.io.BaseTransparentFilterOutputStream;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.terminals.NutsTerminalModeOp;

import java.io.IOException;
import java.io.OutputStream;
import net.thevpc.nuts.NutsFormatManager;
import net.thevpc.nuts.NutsSession;

public class UnescapeOutputStream extends BaseTransparentFilterOutputStream implements ExtendedFormatAware {

    private NutsSession session;
    private NutsWorkspace ws;

    public UnescapeOutputStream(OutputStream out, NutsSession session) {
        super(out);
        this.session = session;
        this.ws = session.getWorkspace();
        NutsTerminalModeOp t = CoreIOUtils.resolveNutsTerminalModeOp(out);
        if (t.in() != NutsTerminalMode.FORMATTED && t.in() != NutsTerminalMode.FILTERED) {
            throw new IllegalArgumentException("Illegal Formatted");
        }
    }

    @Override
    public NutsTerminalModeOp getModeOp() {
        return NutsTerminalModeOp.UNESCAPE;
    }

    public OutputStream getOut() {
        return out;
    }

    private String filterThanEscape(String b) throws IOException {
        NutsFormatManager txt = ws.formats();
        String filtered = txt.text().builder().append(b).filteredText();
        return txt.text().plain(filtered).toString();
//        return ws.formats().text().escapeText(
//                ws.formats().text().filterText(b)
//        );
    }

    @Override
    public void write(int b) throws IOException {
        out.write(filterThanEscape(Character.toString((char) b)).getBytes());
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byte[] bytes = filterThanEscape(new String(b, off, len)).getBytes();
        out.write(bytes, 0, bytes.length);
    }

    @Override
    public ExtendedFormatAware convert(NutsTerminalModeOp other) {
        if (other == null || other == getModeOp()) {
            return this;
        }
        switch (other) {
            case NOP: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NutsTerminalModeOp.NOP);
                }
                return new RawOutputStream(out, session);
            }
            case FORMAT: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NutsTerminalModeOp.FORMAT);
                }
                return new FormatOutputStream(out, session);
            }
            case FILTER: {
                if (out instanceof ExtendedFormatAware) {
                    return ((ExtendedFormatAware) out).convert(NutsTerminalModeOp.FILTER);
                }
                return this;//new FilterFormatOutputStream(out);
            }
            case ESCAPE: {
                return ((ExtendedFormatAware) out);
            }
            case UNESCAPE: {
                return ((ExtendedFormatAware) out);
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }
}
