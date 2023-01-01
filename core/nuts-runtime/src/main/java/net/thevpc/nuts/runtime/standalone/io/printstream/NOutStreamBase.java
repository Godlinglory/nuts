package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.io.DefaultNOutputTargetMetadata;
import net.thevpc.nuts.io.NOutputTargetMetadata;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NAssert;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

public abstract class NOutStreamBase implements NOutStream {
    private static String LINE_SEP = System.getProperty("line.separator");
    protected NSession session;
    protected Bindings bindings;
    protected OutputStream osWrapper;
    protected PrintStream psWrapper;
    protected Writer writerWrapper;
    protected boolean autoFlash;
    private NTerminalMode mode;
    private NSystemTerminalBase term;
    private DefaultNOutputTargetMetadata md = new DefaultNOutputTargetMetadata();

    public NOutStreamBase(boolean autoFlash, NTerminalMode mode, NSession session, Bindings bindings, NSystemTerminalBase term) {
        NAssert.requireNonNull(mode, "mode", session);
        this.bindings = bindings;
        this.autoFlash = autoFlash;
        this.mode = mode;
        this.session = session;
        this.term = term;
    }

    public NOutputTargetMetadata getOutputMetaData() {
        return md;
    }

    protected abstract NOutStream convertImpl(NTerminalMode other);

    @Override
    public String toString() {
        return super.toString();
    }

    public NSession getSession() {
        return session;
    }

    @Override
    public NOutStream write(byte[] b) {
        return write(b, 0, b.length);
    }

    @Override
    public NOutStream write(char[] buf) {
        if (buf == null) {
            buf = "null".toCharArray();
        }
        return write(buf, 0, buf.length);
    }

    private NOutStream printNormalized(NText b) {
        if (b != null) {
            switch (b.getType()) {
                case LIST: {
                    for (NText child : ((NTextList) b).getChildren()) {
                        printNormalized(child);
                    }
                    break;
                }
                case PLAIN:
                case STYLED:
                case COMMAND: {
                    print(b.toString());
                    break;
                }
                case ANCHOR:
                case LINK:
                case CODE:
                case TITLE:
                default: {
                    throw new NUnsupportedOperationException(session);
                }
            }
        }
        return this;
    }

    @Override
    public NOutStream print(NString b) {
        if (b != null) {
            NText t = b.toText();
            printNormalized(NTexts.of(session).transform(t,
                    new NTextTransformConfig()
                            .setNormalize(true)
                            .setFlatten(true)
            ));
        }
        return this;
    }

    @Override
    public NOutStream print(NMsg b) {
        this.print(NTexts.of(session).ofText(b));
        return this;
    }

    @Override
    public NOutStream print(boolean b) {
        this.print(b ? "true" : "false");
        return this;
    }

    @Override
    public NOutStream print(char c) {
        this.print(String.valueOf(c));
        return this;
    }

    @Override
    public NOutStream print(int i) {
        this.print(String.valueOf(i));
        return this;
    }

    @Override
    public NOutStream print(long l) {
        this.print(String.valueOf(l));
        return this;
    }

    @Override
    public NOutStream print(float f) {
        this.print(String.valueOf(f));
        return this;
    }

    @Override
    public NOutStream print(double d) {
        this.print(String.valueOf(d));
        return this;
    }

    @Override
    public NOutStream print(char[] s) {
        if (s == null) {
            s = "null".toCharArray();
        }
        write(s, 0, s.length);
        return this;
    }

    @Override
    public NOutStream print(Object obj) {
        this.print(String.valueOf(obj));
        return this;
    }

    @Override
    public NOutStream printf(Object obj) {
        NObjectFormat.of(session).setValue(obj).print(this);
        return this;
    }

    @Override
    public NOutStream printlnf(Object obj) {
        NObjectFormat.of(session).setValue(obj).println(this);
        return this;
    }

    @Override
    public NOutStream println() {
        this.print(LINE_SEP);
        if (this.autoFlash) {
            flush();
        }
        return this;
    }

    @Override
    public NOutStream println(boolean x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(char x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(NString b) {
        this.print(b);
        this.println();
        return this;
    }

    @Override
    public NOutStream println(NMsg b) {
        this.println(NTexts.of(session).ofText(b));
        return this;
    }

    @Override
    public NOutStream println(int x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(long x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(float x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(double x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(char[] x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(String x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutStream println(Object x) {
        print(x);
        println();
        return this;
    }

    @Override
    public NOutStream resetLine() {
        run(NTerminalCommand.CLEAR_LINE, session);
        run(NTerminalCommand.MOVE_LINE_START, session);
        return this;
    }

    @Override
    public NOutStream printf(String format, Object... args) {
        format(format, args);
        return this;
    }

    @Override
    public NOutStream printj(String format, Object... args) {
        print(NTexts.of(session).ofText(NMsg.ofJstyle(format, args)));
        return this;
    }

    @Override
    public NOutStream printlnj(String format, Object... args) {
        println(NTexts.of(session).ofText(NMsg.ofJstyle(format, args)));
        return this;
    }

    @Override
    public NOutStream printv(String format, Map<String, ?> args) {
        print(NTexts.of(session).ofText(NMsg.ofVstyle(format, args)));
        return this;
    }

    @Override
    public NOutStream printlnv(String format, Map<String, ?> args) {
        println(NTexts.of(session).ofText(NMsg.ofVstyle(format, args)));
        return this;
    }

    @Override
    public NOutStream printlnf(String format, Object... args) {
        format(format, args);
        println();
        return this;
    }

    @Override
    public NOutStream printf(Locale l, String format, Object... args) {
        format(l, format, args);
        return this;
    }

    @Override
    public NOutStream format(String format, Object... args) {
        return format(null, format, args);
    }

    @Override
    public NOutStream format(Locale l, String format, Object... args) {
        if (l == null) {
            NText s = NTexts.of(session).ofText(
                    NMsg.ofCstyle(
                            format, args
                    )
            );
            print(s);
        } else {
            NSession sess = this.session.copy().setLocale(l.toString());
            NText s = NTexts.of(sess).ofText(
                    NMsg.ofCstyle(
                            format, args
                    )
            );
            print(s);
        }
        return this;
    }

    @Override
    public NOutStream append(CharSequence csq) {
        append(csq, 0, csq.length());
        return this;
    }

    @Override
    public NOutStream append(CharSequence csq, int start, int end) {
        int bufferLength = Math.min(4096, (end - start));
        int i = start;
        while (i < end) {
            int e = Math.min(i + bufferLength, end);
            String s = csq.subSequence(i, e).toString();
            print(s);
            i += bufferLength;
        }
        return this;
    }

    @Override
    public NOutStream append(char c) {
        print(c);
        return this;
    }

    @Override
    public NTerminalMode getTerminalMode() {
        return mode;
    }

    @Override
    public boolean isAutoFlash() {
        return autoFlash;
    }

    @Override
    public NOutStream setTerminalMode(NTerminalMode other) {
        if (other == null || other == this.getTerminalMode()) {
            return this;
        }
        switch (other) {
            case ANSI: {
                if (bindings.ansi != null) {
                    return bindings.filtered;
                }
                return convertImpl(other);
            }
            case INHERITED: {
                if (bindings.inherited != null) {
                    return bindings.inherited;
                }
                return convertImpl(other);
            }
            case FORMATTED: {
                if (bindings.formatted != null) {
                    return bindings.formatted;
                }
                return convertImpl(other);
            }
            case FILTERED: {
                if (bindings.filtered != null) {
                    return bindings.filtered;
                }
                return convertImpl(other);
            }
        }
        throw new IllegalArgumentException("unsupported yet");
    }

    @Override
    public OutputStream asOutputStream() {
        if (osWrapper == null) {
            osWrapper = new OutputStreamFromNPrintStream(this);
        }
        return osWrapper;
    }

    @Override
    public PrintStream asPrintStream() {
        if (psWrapper == null) {
            psWrapper = new PrintStreamFromNPrintStream((OutputStreamFromNPrintStream) asOutputStream());
        }
        return psWrapper;
    }

    @Override
    public Writer asWriter() {
        if (writerWrapper == null) {
            writerWrapper = new WriterFromNPrintStream(this);
        }
        return writerWrapper;
    }

    @Override
    public boolean isNtf() {
        switch (getTerminalMode()) {
            case FORMATTED:
            case FILTERED: {
                return true;
            }
        }
        return false;
    }

    public NSystemTerminalBase getTerminal() {
        return term;
    }

    public static class Bindings {
        protected NOutStreamBase raw;
        protected NOutStreamBase filtered;
        protected NOutStreamBase ansi;
        protected NOutStreamBase inherited;
        protected NOutStreamBase formatted;
    }

    @Override
    public NOutStream append(Object text, NTextStyle style) {
        return append(text, NTextStyles.of(style));
    }

    @Override
    public NOutStream append(Object text, NTextStyles styles) {
        if (text != null) {
            if (styles.size() == 0) {
                print(NTexts.of(session).ofText(text));
            } else {
                print(NTexts.of(session).ofStyled(NTexts.of(session).ofText(text), styles));
            }
        }
        return this;
    }
}
