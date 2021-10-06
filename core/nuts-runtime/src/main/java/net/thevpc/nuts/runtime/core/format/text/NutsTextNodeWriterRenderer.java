package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextCommand;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextStyled;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextTitle;
import net.thevpc.nuts.runtime.core.format.text.renderer.AnsiUnixTermPrintRenderer;
import net.thevpc.nuts.runtime.core.format.text.renderer.StyleRenderer;
import net.thevpc.nuts.runtime.standalone.io.NutsPrintStreamHelper;
import net.thevpc.nuts.runtime.standalone.io.OutputHelper;
import net.thevpc.nuts.runtime.standalone.io.OutputStreamHelper;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NutsTextNodeWriterRenderer extends AbstractNutsTextNodeWriter {

    private byte[] buffer = new byte[1024];
    private int bufferSize = 0;
    private boolean enableBuffering = false;
    private byte[] later = null;
    private FormattedPrintStreamRenderer renderer;
    private OutputHelper rawOutput;
    private RenderedRawStream renderedRawStream = new RenderedRawStream() {

        public OutputHelper baseOutput() {
            return rawOutput;
        }

        @Override
        public void writeRaw(byte[] buf, int off, int len) {
            NutsTextNodeWriterRenderer.this.writeRaw(new String(buf, off, len));
        }

        @Override
        public void writeLater(byte[] buf) {
            NutsTextNodeWriterRenderer.this.writeLater(buf);
        }
    };
    private NutsSession session;
//    private NutsWorkspace ws;

    public NutsTextNodeWriterRenderer(NutsPrintStream rawOutput, FormattedPrintStreamRenderer renderer, NutsSession session) {
        this(new NutsPrintStreamHelper(rawOutput), renderer, session);
    }

    public NutsTextNodeWriterRenderer(OutputStream rawOutput, FormattedPrintStreamRenderer renderer, NutsSession session) {
        this(new OutputStreamHelper(rawOutput, session), renderer, session);
    }

    public NutsTextNodeWriterRenderer(OutputHelper rawOutput, FormattedPrintStreamRenderer renderer, NutsSession session) {
        this.renderer = renderer == null ? AnsiUnixTermPrintRenderer.ANSI_RENDERER : renderer;
        this.rawOutput = rawOutput;
        this.session = session;
    }

    @Override
    public void writeNode(NutsText node) {
        writeNode(node, getWriteConfiguration());
    }

    @Override
    public final void writeRaw(byte[] buf, int off, int len) {
        rawOutput.write(buf, off, len);
    }

    @Override
    public void writeRaw(char[] buf, int off, int len) {
        writeRaw(new String(buffer,off,len));
    }

    @Override
    public final boolean flush() {
        if (bufferSize > 0) {
            rawOutput.write(buffer, 0, bufferSize);
            bufferSize = 0;
            return true;
        }
        rawOutput.flush();
        return false;
    }

    public void writeNode(NutsText node, NutsTextWriteConfiguration ctx) {
        writeNode(new AnsiEscapeCommand[0], node, ctx);
    }

    private void writeNode(AnsiEscapeCommand[] formats, NutsText node, NutsTextWriteConfiguration ctx) {
        if (formats == null) {
            formats = new AnsiEscapeCommand[0];
        }
        switch (node.getType()) {
            case PLAIN: {
                NutsTextPlain p = (NutsTextPlain) node;
                writeRaw(AnsiEscapeCommands.list(formats), p.getText(), ctx.isFiltered());
//        }else if (text instanceof TextNodeEscaped) {
//            TextNodeEscaped p = (TextNodeEscaped) text;
//            writeRaw(AnsiEscapeCommands.forList(formats), p.getChild(), ctx.isFiltered());
                break;
            }
            case LIST: {
                NutsTextList s = (NutsTextList) node;
                for (NutsText n : s) {
                    writeNode(formats, n, ctx);
                }
                break;
            }
            case STYLED: {
                DefaultNutsTextStyled s = (DefaultNutsTextStyled) node;
                NutsTextStyles styles = s.getStyles();
                NutsTextStyles format = session.text().getTheme().toBasicStyles(styles, session);
                AnsiEscapeCommand[] s2 = _appendFormats(formats, format);
                writeNode(s2, s.getChild(), ctx);
                break;
            }
            case TITLE: {
                DefaultNutsTextTitle s = (DefaultNutsTextTitle) node;
                DefaultNutsTextManager factory0 = (DefaultNutsTextManager) session.text();
                AnsiEscapeCommand[] s2 = _appendFormats(formats, session.text().getTheme().toBasicStyles(NutsTextStyles.of(NutsTextStyle.title(s.getLevel())), session
                ));
                if (ctx.isTitleNumberEnabled()) {
                    NutsTextNumbering seq = ctx.getTitleNumberSequence();
                    if (seq == null) {
                        seq = session.text().ofNumbering();
                        ctx.setTitleNumberSequence(seq);
                    }
                    NutsTextNumbering a = seq.newLevel(s.getLevel());
                    NutsText sWithTitle = factory0.ofList(
                            session.text().ofPlain(a.toString() + " "),
                            s.getChild()
                    );
                    writeNode(s2, sWithTitle, ctx);
                } else {
                    writeNode(s2, s.getChild(), ctx);
                }
//        } else if (text instanceof TextNodeUnStyled) {
//            TextNodeUnStyled s = (TextNodeUnStyled) text;
//            writeNode(formats, new NutsTextPlain(s.getStart()), ctx);
//            writeNode(formats, s.getChild(), ctx);
//            writeNode(formats, new NutsTextPlain(s.getEnd()), ctx);
                break;
            }
            case COMMAND: {
                DefaultNutsTextCommand s = (DefaultNutsTextCommand) node;
                AnsiEscapeCommand yy = DefaultNutsTextCommand.parseAnsiEscapeCommand(s.getCommand(), session);
                AnsiEscapeCommand[] s2 = _appendFormats(formats, yy);
                writeRaw(AnsiEscapeCommands.list(s2), "", ctx.isFiltered());
                break;
            }
            case ANCHOR: {
                //ignore!!
                break;
            }
            case LINK: {
                //ignore!!
                DefaultNutsTextManager factory0 = (DefaultNutsTextManager) session.text();
                writeNode(
                        formats,
                        factory0.createStyled(((NutsTextLink) node).getChild(),
                                NutsTextStyles.of(NutsTextStyle.underlined()),
                                true
                        ),
                        ctx
                );
                writeRaw(AnsiEscapeCommands.list(formats), "see: " + ((NutsTextLink) node).getChild(), ctx.isFiltered());
                break;
            }
            case CODE: {
                NutsTextCode node1 = (NutsTextCode) node;
                NutsText cn = node1.parse(session);
                writeNode(formats, cn, ctx);
                break;
            }
            default: {
                throw new UnsupportedOperationException("unsupported NutsTextNode type " + node.getClass().getSimpleName());
            }
        }
    }

    protected void writeRaw(AnsiEscapeCommand format, String rawString, boolean filterFormat) {
        if (!filterFormat && format != null) {
            StyleRenderer f = null;
            f = renderer.createStyleRenderer(simplifyFormat(format), renderedRawStream, session);
            try {
                f.startFormat(renderedRawStream);
                if (rawString.length() > 0) {
                    writeRaw(rawString);
                }
            } finally {
                f.endFormat(renderedRawStream);
            }
        } else {
            if (rawString.length() > 0) {
                writeRaw(rawString);
            }
        }
    }

    public final void writeRaw(String rawString) {
        flushLater();
        byte[] b = rawString.getBytes();
        if (enableBuffering) {
            if (b.length + bufferSize < buffer.length) {
                System.arraycopy(b, 0, buffer, bufferSize, b.length);
                bufferSize += b.length;
            } else {
                flush();
                if (b.length >= buffer.length) {
                    rawOutput.write(b, 0, b.length);
                } else {
                    System.arraycopy(b, 0, buffer, bufferSize, b.length);
                    bufferSize += b.length;
                }
            }
        } else {
            rawOutput.write(b, 0, b.length);
        }
    }

    protected AnsiEscapeCommand simplifyFormat(AnsiEscapeCommand f) {
        if (f instanceof AnsiEscapeCommandList) {
            AnsiEscapeCommand[] o = ((AnsiEscapeCommandList) f).getChildren();
            List<AnsiEscapeCommand> ok = new ArrayList<>();
            if (o != null) {
                for (AnsiEscapeCommand v : o) {
                    if (v != null) {
                        v = simplifyFormat(v);
                        if (v != null) {
                            ok.add(v);
                        }
                    }
                }
            }
            if (ok.isEmpty()) {
                return null;
            }
            if (ok.size() == 1) {
                return simplifyFormat(ok.get(0));
            }
            return AnsiEscapeCommands.list(ok.toArray(new AnsiEscapeCommand[0]));
        }
        return f;
    }

    private AnsiEscapeCommand[] _appendFormats(AnsiEscapeCommand[] old, AnsiEscapeCommand v) {
        List<AnsiEscapeCommand> list = new ArrayList<AnsiEscapeCommand>((old == null ? 0 : old.length) + 1);
        if (old != null) {
            list.addAll(Arrays.asList(old));
        }
        list.add(v);
        return list.toArray(new AnsiEscapeCommand[0]);
    }

    private AnsiEscapeCommand[] _appendFormats(AnsiEscapeCommand[] old, AnsiEscapeCommand... v) {
        List<AnsiEscapeCommand> list = new ArrayList<AnsiEscapeCommand>((old == null ? 0 : old.length) + 1);
        if (old != null) {
            list.addAll(Arrays.asList(old));
        }
        for (AnsiEscapeCommand ansiEscapeCommand : v) {
            if (ansiEscapeCommand != null) {
                list.add(ansiEscapeCommand);
            }
        }
        return list.toArray(new AnsiEscapeCommand[0]);
    }

    private AnsiEscapeCommand[] _appendFormats(AnsiEscapeCommand[] old, NutsTextStyles v) {
        List<AnsiEscapeCommand> list = new ArrayList<AnsiEscapeCommand>((old == null ? 0 : old.length) + 1);
        if (old != null) {
            list.addAll(Arrays.asList(old));
        }
        for (NutsTextStyle textFormat : v) {
            if (textFormat != null) {
                list.add(AnsiEscapeCommandFromNodeStyle.of(textFormat));
            }
        }
        return list.toArray(new AnsiEscapeCommand[0]);
    }

    public final void writeLater(byte[] later) {
//        ws.log().of(NutsTextNodeWriterRenderer.class)
//                .with()
//                .session(ws.createSession())
//                .level(Level.FINEST)
//                .verb(NutsLogVerb.DEBUG)
//                .log("store Later on "+System.identityHashCode(this));
        this.later = later;
        rawOutput.flush();
    }

    public final void flushLater() {
        byte[] b = later;
        if (b != null) {
//                ws.log().of(NutsTextNodeWriterRenderer.class)
//                        .with()
//                        .session(ws.createSession())
//                        .level(Level.FINEST)
//                        .verb(NutsLogVerb.DEBUG)
//                        .log("process Later on "+System.identityHashCode(this));
            later = null;
            if (enableBuffering) {
                if (b.length + bufferSize < buffer.length) {
                    System.arraycopy(b, 0, buffer, bufferSize, b.length);
                    bufferSize += b.length;
                } else {
                    flush();
                    if (b.length >= buffer.length) {
                        rawOutput.write(b, 0, b.length);
                    } else {
                        System.arraycopy(b, 0, buffer, bufferSize, b.length);
                        bufferSize += b.length;
                    }
                }
            } else {
                rawOutput.write(b, 0, b.length);
                rawOutput.flush();
            }
            //flush();
        }
    }

    @Override
    public String toString() {
        return "Printer(" + rawOutput + (this.later != null ? ";withLater" : "") + ")";
    }

}
