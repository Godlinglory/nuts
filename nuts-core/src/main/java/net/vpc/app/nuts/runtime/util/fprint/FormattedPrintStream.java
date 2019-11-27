package net.vpc.app.nuts.runtime.util.fprint;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceAware;
import net.vpc.app.nuts.runtime.util.fprint.renderer.AnsiUnixTermPrintRenderer;
import net.vpc.app.nuts.runtime.util.fprint.renderer.StyleRenderer;
import net.vpc.app.nuts.runtime.util.fprint.util.FormattedPrintStreamUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.vpc.app.nuts.runtime.util.fprint.parser.FormattedPrintStreamNodePartialParser;
import net.vpc.app.nuts.runtime.util.fprint.parser.TextNode;
import net.vpc.app.nuts.runtime.util.fprint.parser.TextNodeCommand;
import net.vpc.app.nuts.runtime.util.fprint.parser.TextNodeList;
import net.vpc.app.nuts.runtime.util.fprint.parser.TextNodePlain;
import net.vpc.app.nuts.runtime.util.fprint.parser.TextNodeStyled;
import net.vpc.app.nuts.core.io.NutsPrintStreamExt;

public abstract class FormattedPrintStream extends PrintStream implements NutsPrintStreamExt , NutsWorkspaceAware {

    private boolean formatEnabled = true;
    private FormattedPrintStreamParser parser;
    private FormattedPrintStreamRenderer renderer;
    private byte[] buffer = new byte[1024];
    private int bufferSize = 0;
    private boolean enableBuffering = false;
    private OutputStream base;
    private PrintStream ps;
    private NutsWorkspace ws;
    private byte[] later=null;
//    private FormattedPrintStreamNodePartialParser partialParser = new FormattedPrintStreamNodePartialParser();

    public FormattedPrintStream(OutputStream out, FormattedPrintStreamRenderer renderer, FormattedPrintStreamParser parser) {
        super(out);
        init(renderer, parser, out);
    }

    public FormattedPrintStream(OutputStream out, boolean autoFlush, FormattedPrintStreamRenderer renderer, FormattedPrintStreamParser parser) {
        super(out, autoFlush);
        init(renderer, parser, out);
    }

    public FormattedPrintStream(OutputStream out, boolean autoFlush, String encoding, FormattedPrintStreamRenderer renderer, FormattedPrintStreamParser parser) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
        init(renderer, parser, out);
    }

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        this.ws=workspace;
    }

    //    public FormattedPrintStream(String fileName, FormattedPrintStreamRenderer renderer, FormattedPrintStreamParser parser) throws FileNotFoundException {
//        super(fileName);
//        init(renderer, parser);
//    }
//
//    public FormattedPrintStream(String fileName, String csn, FormattedPrintStreamRenderer renderer, FormattedPrintStreamParser parser) throws FileNotFoundException, UnsupportedEncodingException {
//        super(fileName, csn);
//        init(renderer, parser);
//    }
//
//    public FormattedPrintStream(File file, FormattedPrintStreamRenderer renderer, FormattedPrintStreamParser parser) throws FileNotFoundException {
//        super(file);
//        init(renderer, parser);
//    }
//
//    public FormattedPrintStream(File file, String csn, FormattedPrintStreamRenderer renderer, FormattedPrintStreamParser parser) throws FileNotFoundException, UnsupportedEncodingException {
//        super(file, csn);
//        init(renderer, parser);
//    }
    //////////////////////////////////////////
    public FormattedPrintStream(OutputStream out, FormattedPrintStreamRenderer renderer) {
        super(out);
        init(renderer, null, out);
    }

    public FormattedPrintStream(OutputStream out, boolean autoFlush, FormattedPrintStreamRenderer renderer) {
        super(out, autoFlush);
        init(renderer, null, out);
    }

    public FormattedPrintStream(OutputStream out, boolean autoFlush, String encoding, FormattedPrintStreamRenderer renderer) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
        init(renderer, null, out);
    }

//    public FormattedPrintStream(String fileName, FormattedPrintStreamRenderer renderer) throws FileNotFoundException {
//        super(fileName);
//        init(renderer, null);
//    }
//
//    public FormattedPrintStream(String fileName, String csn, FormattedPrintStreamRenderer renderer) throws FileNotFoundException, UnsupportedEncodingException {
//        super(fileName, csn);
//        init(renderer, null);
//    }
//
//    public FormattedPrintStream(File file, FormattedPrintStreamRenderer renderer) throws FileNotFoundException {
//        super(file);
//        init(renderer, parser);
//    }
//
//    public FormattedPrintStream(File file, String csn, FormattedPrintStreamRenderer renderer) throws FileNotFoundException, UnsupportedEncodingException {
//        super(file, csn);
//        init(renderer, parser);
//    }
    //////////////////////////////////////////
    public FormattedPrintStream(OutputStream out) {
        super(out);
        init(renderer, parser, out);
    }

    public FormattedPrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
        init(renderer, parser, out);
    }

    public FormattedPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
        init(renderer, parser, out);
    }

//    public FormattedPrintStream(String fileName) throws FileNotFoundException {
//        super(fileName);
//        init(renderer, parser);
//    }
//
//    public FormattedPrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
//        super(fileName, csn);
//        init(renderer, parser);
//    }
//
//    public FormattedPrintStream(File file) throws FileNotFoundException {
//        super(file);
//        init(renderer, parser);
//    }
//
//    public FormattedPrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
//        super(file, csn);
//        init(renderer, parser);
//    }
    //////////////////////////////////////////
    private void init(FormattedPrintStreamRenderer renderer, FormattedPrintStreamParser parser, OutputStream base) {
        this.base = base;
        setParser(parser);
        setRenderer(renderer);
    }

    public FormattedPrintStreamParser getParser() {
        return parser;
    }

    public FormattedPrintStream setParser(FormattedPrintStreamParser parser) {
        this.parser = parser == null ? new FormattedPrintStreamNodePartialParser() : parser;
        return this;
    }

    public FormattedPrintStreamRenderer getRenderer() {
        return renderer;
    }

    public FormattedPrintStream setRenderer(FormattedPrintStreamRenderer renderer) {
        this.renderer = renderer == null ? AnsiUnixTermPrintRenderer.ANSI_RENDERER : renderer;
        return this;
    }

    public PrintStream getUnformattedInstance() {
        if (super.out instanceof PrintStream) {
            return (PrintStream) super.out;
        }
        return new PrintStream(super.out);
    }

    public boolean isFormatEnabled() {
        return formatEnabled;
    }

    public void setFormatEnabled(boolean formatEnabled) {
        this.formatEnabled = formatEnabled;
    }

    protected TextFormat simplifyFormat(TextFormat f) {
        if (f instanceof TextFormatList) {
            TextFormatList l = (TextFormatList) f;
            TextFormat[] o = ((TextFormatList) f).getChildren();
            List<TextFormat> ok = new ArrayList<>();
            if (o != null) {
                for (TextFormat v : o) {
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
            return TextFormats.list(ok.toArray(new TextFormat[0]));
        }
        return f;
    }

    @Override
    public FormattedPrintStream format(Locale l, String format, Object... args) {
        print(FormattedPrintStreamUtils.formatCStyle(ws,l, format, args));
        return this;
    }

    @Override
    public PrintStream format(String format, Object... args) {
        print(FormattedPrintStreamUtils.formatCStyle(ws,Locale.getDefault(), format, args));
        return this;
    }

//    @Override
//    public void println(String text) {
//        print(text);
//        println();
//    }
    protected FormattedPrintStream writeRaw(TextFormat format, String rawString) {
        if (isFormatEnabled() && format != null) {
            StyleRenderer f=null;
            f = renderer.createStyleRenderer(simplifyFormat(format));
            try {
                f.startFormat(this);
                if(rawString.length()>0) {
                    writeRaw(rawString);
                }
            } finally {
                f.endFormat(this);
            }
        } else {
            if(rawString.length()>0) {
                writeRaw(rawString);
            }
        }
        return this;
    }

    private void print(TextFormat[] formats, TextNode node) {
        if (formats == null) {
            formats = new TextFormat[0];
        }
        if (node instanceof TextNodePlain) {
            TextNodePlain p = (TextNodePlain) node;
            writeRaw(TextFormats.list(formats), p.getValue());
            } else if (node instanceof TextNodeList) {
            TextNodeList s = (TextNodeList) node;
            for (TextNode n : s) {
                print(formats, n);
            }
        } else if (node instanceof TextNodeStyled) {
            TextNodeStyled s = (TextNodeStyled) node;
            TextFormat[] s2 = _appendFormats(formats, s.getStyle());
            print(s2, s.getChild());
        } else if (node instanceof TextNodeCommand) {
            TextNodeCommand s = (TextNodeCommand) node;
            TextFormat[] s2 = _appendFormats(formats, s.getStyle());
            writeRaw(TextFormats.list(s2), "");
        } else {
            writeRaw(TextFormats.list(formats), String.valueOf(node));
        }
    }

    public void print(TextNode node) {
        if (node == null) {
            node = TextNodePlain.NULL;
        }
        print(new TextFormat[0], node);
    }

    private TextFormat[] _appendFormats(TextFormat[] old, TextFormat v) {
        List<TextFormat> list = new ArrayList<TextFormat>((old == null ? 0 : old.length) + 1);
        if (old != null) {
            list.addAll(Arrays.asList(old));
        }
        list.add(v);
        return list.toArray(new TextFormat[0]);
    }

    @Override
    public PrintStream append(char c) {
        return super.append(c);
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        return super.append(csq, start, end);
    }

    @Override
    public PrintStream append(CharSequence csq) {
        return super.append(csq);
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        return super.printf(l, format, args);
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        return super.printf(format, args);
    }

    @Override
    public void println(Object x) {
        super.println(x);
    }

    @Override
    public void println(char[] x) {
        super.println(x);
    }

    @Override
    public void println(double x) {
        super.println(x);
    }

    @Override
    public void println(float x) {
        super.println(x);
    }

    @Override
    public void println(long x) {
        super.println(x);
    }

    @Override
    public void println(int x) {
        super.println(x);
    }

    @Override
    public void println(char x) {
        super.println(x);
    }

    @Override
    public void println(boolean x) {
        super.println(x);
    }

    @Override
    public void println() {
        super.println();
    }

    @Override
    public void print(Object obj) {
        super.print(obj);
    }

    @Override
    public void print(char[] s) {
        super.print(s);
    }

    @Override
    public void print(double d) {
        super.print(d);
    }

    @Override
    public void print(float f) {
        super.print(f);
    }

    @Override
    public void print(long l) {
        super.print(l);
    }

    @Override
    public void print(int i) {
        super.print(i);
    }

    @Override
    public void print(char c) {
        super.print(c);
    }

    @Override
    public void print(boolean b) {
        super.print(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        if (!isFormatEnabled()) {
            super.write(buf, off, len);
            return;
        }
        if (len == 0) {
            //do nothing!!!
        } else {
            String raw = new String(buf, off, len);
            try {
                parser.take(raw);
                consumeNodes(false);
            } catch (Exception ex) {
                //
            }
        }
    }

    private final void flushBuffer() {
        if (bufferSize > 0) {
            super.write(buffer, 0, bufferSize);
            bufferSize = 0;
        }
    }

    public final void later(byte[] later) {
        this.later=later;
    }

    public final void flushLater() {
        byte[] b = later;
        if(b!=null) {
            later=null;
            if (enableBuffering) {
                if (b.length + bufferSize < buffer.length) {
                    System.arraycopy(b, 0, buffer, bufferSize, b.length);
                    bufferSize += b.length;
                } else {
                    flushBuffer();
                    if (b.length >= buffer.length) {
                        super.write(b, 0, b.length);
                    } else {
                        System.arraycopy(b, 0, buffer, bufferSize, b.length);
                        bufferSize += b.length;
                    }
                }
            } else {
                super.write(b, 0, b.length);
            }
            //flush();
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
                flushBuffer();
                if (b.length >= buffer.length) {
                    super.write(b, 0, b.length);
                } else {
                    System.arraycopy(b, 0, buffer, bufferSize, b.length);
                    bufferSize += b.length;
                }
            }
        } else {
            super.write(b, 0, b.length);
        }
    }

    @Override
    public void write(int b) {
        super.write(b);
    }

    public void consumeNodes(boolean greedy) {
        TextNode n = null;
        while ((n = parser.consumeNode()) != null) {
            print(n);
        }
        if (greedy) {
            parser.forceEnding();
            while ((n = parser.consumeNode()) != null) {
                print(n);
            }
        }
    }

    @Override
    public void flush() {
        //flushLater();
        flushBuffer();
        super.flush();
        try {
            consumeNodes(true);
        } catch (Exception ex) {
            //
        }
        flushLater();
        flushBuffer();
        super.flush();
    }

    @Override
    public PrintStream basePrintStream() {
        OutputStream b = baseOutputStream();
        if (b instanceof PrintStream) {
            return (PrintStream) b;
        }
        if (ps == null) {
            ps = new PrintStream(b);
        }
        return ps;
    }

    @Override
    public OutputStream baseOutputStream() {
        return base;
    }

}
