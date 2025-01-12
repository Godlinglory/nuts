package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.io.util.AbstractMultiReadNInputSource;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NStream;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class NPathBase extends AbstractMultiReadNInputSource implements NPath {
    public static final int BUFFER_SIZE = 8192;
    private DefaultNPathMetadata omd = new DefaultNPathMetadata(this);
    private boolean deleteOnDispose;

    public NPathBase(NSession session) {
        super(session);
    }

    @Override
    public InputStream getInputStream() {
        return getInputStream(new NPathOption[0]);
    }

    @Override
    public OutputStream getOutputStream() {
        return getOutputStream(new NPathOption[0]);
    }

    protected NPath copyExtraFrom(NPath other) {
        this.deleteOnDispose = other.isDeleteOnDispose();
        if (other instanceof NPathBase) {
            omd.setAll(((NPathBase) other).omd);
        } else {
            omd.setAll(other.getMetaData());
            omd.setAll(other.getMetaData());
        }
        return this;
    }

    @Override
    public PrintStream getPrintStream(Charset cs, NPathOption... options) {
        OutputStream out = getOutputStream(options);
        if (out instanceof PrintStream) {
            return (PrintStream) out;
        }
        try {
            return new PrintStream(out, false, nonNullCharset(cs).name());
        } catch (UnsupportedEncodingException e) {
            throw new NIllegalArgumentException(getSession(), NMsg.ofPlain("unsupported encoding"), e);
        }
    }

    @Override
    public PrintStream getPrintStream(NPathOption... options) {
        OutputStream out = getOutputStream(options);
        if (out instanceof PrintStream) {
            return (PrintStream) out;
        }
        return new PrintStream(out);
    }

    @Override
    public PrintStream getPrintStream() {
        OutputStream out = getOutputStream();
        if (out instanceof PrintStream) {
            return (PrintStream) out;
        }
        return new PrintStream(out);
    }

    @Override
    public BufferedReader getBufferedReader(NPathOption... options) {
        return getBufferedReader(null, options);
    }


    @Override
    public BufferedReader getBufferedReader(Charset cs, NPathOption... options) {
        Reader r = getReader(cs, options);
        if (r instanceof BufferedReader) {
            return (BufferedReader) r;
        }
        return new BufferedReader(r);
    }

    @Override
    public void copyToPrintStream(PrintStream other) {
        copyToPrintStream(other, null);
    }

    @Override
    public void copyToPrintStream(PrintStream other, Charset cs) {
        try (Reader reader = getReader()) {
            char[] buffer = new char[BUFFER_SIZE];
            int count;
            while ((count = reader.read(buffer)) > 0) {
                other.print(Arrays.copyOf(buffer, count));
            }
        } catch (IOException ex) {
            throw new NIOException(getSession(), ex);
        }
    }

    @Override
    public void copyToOutputStream(OutputStream other) {
        try (InputStream reader = getInputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = reader.read(buffer)) > 0) {
                other.write(buffer, 0, count);
            }
        } catch (IOException ex) {
            throw new NIOException(getSession(), ex);
        }
    }

    @Override
    public void copyToWriter(Writer other) {
        copyToWriter(other, null);
    }

    @Override
    public void copyToWriter(Writer other, Charset cs) {
        try (Reader reader = getReader(cs)) {
            char[] buffer = new char[BUFFER_SIZE];
            int count;
            while ((count = reader.read(buffer)) > 0) {
                other.write(buffer, 0, count);
            }
        } catch (IOException ex) {
            throw new NIOException(getSession(), ex);
        }
    }

    @Override
    public boolean isUserCache() {
        return omd.isUserCache();
    }

    @Override
    public NPath setUserCache(boolean userCache) {
        this.omd.setUserCache(userCache);
        return this;
    }

    @Override
    public boolean isUserTemporary() {
        return omd.isUserTemporary();
    }

    @Override
    public NPath setUserTemporary(boolean temporary) {
        this.omd.setUserTemporary(temporary);
        return this;
    }

    @Override
    public String getBaseName() {
        String n = getName();
        int i = n.indexOf('.');
        if (i < 0) {
            return n;
        }
        if (i == n.length() - 1) {
            return n;
        }
        return n.substring(0, i);
    }

    public String[] getSmartParts() {
        String n = getName();
        NLiteral[] vals = NVersion.of(n).get().split();
        int lastDot = -1;
        for (int i = vals.length - 1; i >= 0; i--) {
            NLiteral v = vals[i];
            String u = v.asString().get();
            if (u.equals(".")) {
                if (i == vals.length - 1) {
                    return rebuildSmartParts(vals, i);
                }
                NLiteral v2 = vals[i + 1];
                if (v2.isNumber()) {
                    //check if the part before is also a number
                    if (i > 0 && vals[i - 1].isNumber()) {
                        if (i + 1 == vals.length - 1) {
                            return rebuildSmartParts(vals, i + 2);
                        } else if (vals[i + 1].asString().get().equals(".")) {
                            return rebuildSmartParts(vals, i + 1);
                        }
                    }
                } else {
                    //continue
                }
                if (lastDot == -1) {
                    lastDot = i;
                } else {
                    break;
                }
            }
        }
        if (lastDot < 0) {
            return new String[]{n, ""};
        }
        return rebuildSmartParts(vals, lastDot);
    }

    private String[] rebuildSmartParts(NLiteral[] vals, int split) {
        return new String[]{
                concatSmartParts(vals, 0, split),
                concatSmartParts(vals, split + 1, vals.length),
        };
    }

    private String concatSmartParts(NLiteral[] vals, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) {
            sb.append(vals[i].asString().get());
        }
        return sb.toString();
    }


    @Override
    public String getSmartBaseName() {
        return getSmartParts()[0];
    }

    @Override
    public String getSmartExtension() {
        return getSmartParts()[1];
    }

    @Override
    public String getLongBaseName() {
        String n = getName();
        int i = n.lastIndexOf('.');
        if (i < 0) {
            return n;
        }
        if (i == n.length() - 1) {
            return n;
        }
        return n.substring(0, i);
    }

    @Override
    public String getLastExtension() {
        String n = getName();
        int i = n.lastIndexOf('.');
        if (i < 0) {
            return "";
        }
        return n.substring(i + 1);
    }

    @Override
    public String getLongExtension() {
        String n = getName();
        int i = n.indexOf('.');
        if (i < 0) {
            return "";
        }
        return n.substring(i + 1);
    }

    @Override
    public boolean isURL() {
        return toURL().isPresent();
    }

    @Override
    public boolean isFile() {
        return toFile().orNull()!=null;
    }

    @Override
    public NPath delete() {
        return delete(false);
    }


    public NString toNutsString() {
        return NTexts.of(getSession()).ofPlain(toString());
    }

    @Override
    public NFormat formatter(NSession session) {
        return new PathFormat(this)
                .setSession(session != null ? session : getSession())
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NPathBase that = (NPathBase) o;
        return Objects.equals(toString(), that.toString())
                ;
    }

    private static class PathFormat extends DefaultFormatBase<NFormat> {
        private final NPathBase p;

        public PathFormat(NPathBase p) {
            super(p.getSession(), "path");
            this.p = p;
        }

        @Override
        public void print(NPrintStream out) {
            out.print(NTexts.of(p.getSession()).ofStyled(p.toNutsString(), NTextStyle.path()));
        }

        @Override
        public boolean configureFirst(NCmdLine cmdLine) {
            return false;
        }

        @Override
        public int getSupportLevel(NSupportLevelContext context) {
            return DEFAULT_SUPPORT;
        }
    }

    @Override
    public NStream<NPath> walk() {
        return walk(Integer.MAX_VALUE, new NPathOption[0]);
    }

    @Override
    public NStream<NPath> walk(NPathOption... options) {
        return walk(Integer.MAX_VALUE, options);
    }

    @Override
    public NStream<NPath> walk(int maxDepth) {
        return walk(maxDepth <= 0 ? Integer.MAX_VALUE : maxDepth, new NPathOption[0]);
    }

    @Override
    public Writer getWriter() {
        return getWriter(null, new NPathOption[0]);
    }

    @Override
    public BufferedWriter getBufferedWriter() {
        return getBufferedWriter(null, new NPathOption[0]);
    }

    @Override
    public Writer getWriter(NPathOption... options) {
        return getWriter((Charset) null, options);
    }

    @Override
    public Writer getWriter(Charset charset, NPathOption... options) {
        return new OutputStreamWriter(getOutputStream(options), nonNullCharset(charset));
    }

    @Override
    public BufferedWriter getBufferedWriter(NPathOption... options) {
        Writer w = getWriter(options);
        if (w instanceof BufferedWriter) {
            return (BufferedWriter) w;
        }
        return new BufferedWriter(w);
    }

    @Override
    public BufferedWriter getBufferedWriter(Charset charset, NPathOption... options) {
        Writer w = getWriter(charset, options);
        if (w instanceof BufferedWriter) {
            return (BufferedWriter) w;
        }
        return new BufferedWriter(w);
    }

    @Override
    public Reader getReader(NPathOption... options) {
        return getReader((Charset) null);
    }

    @Override
    public Reader getReader(Charset cs, NPathOption... options) {
        CharsetDecoder decoder = nonNullCharset(cs).newDecoder();
        Reader reader = new InputStreamReader(getInputStream(options), decoder);
        return new BufferedReader(reader);
    }


    @Override
    public boolean isHttp() {
        if (!isURL()) {
            return false;
        }
        String s = toString();
        return s.startsWith("http://") || s.startsWith("https://");
    }


    @Override
    public NContentMetadata getMetaData() {
        return omd.getMetaData();
    }

    @Override
    public boolean isMultiRead() {
        return true;
    }

    @Override
    public void dispose() {
        if (isDeleteOnDispose()) {
            this.deleteTree();
        }
    }

    @Override
    public NPath writeString(String string, Charset cs, NPathOption... options) {
        return writeBytes(string.getBytes(nonNullCharset(cs)));
    }

    @Override
    public NPath writeString(String string, NPathOption... options) {
        return writeString(string, null, options);
    }

    @Override
    public String readString(NPathOption... options) {
        return readString(null, options);
    }

    @Override
    public String readString(Charset cs, NPathOption... options) {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[BUFFER_SIZE];
        try (Reader reader = getReader(cs, options)) {
            while (true) {
                int len = reader.read(buffer);
                if (len > 0) {
                    sb.append(buffer, 0, len);
                } else {
                    break;
                }
            }
        } catch (IOException ex) {
            throw new NIOException(getSession(), ex);
        }
        return sb.toString();
    }


    @Override
    public List<NPath> list() {
        return stream().toList();
    }

    @Override
    public void setDeleteOnDispose(boolean deleteOnDispose) {
        this.deleteOnDispose = deleteOnDispose;
    }

    @Override
    public boolean isDeleteOnDispose() {
        return deleteOnDispose;
    }
}
