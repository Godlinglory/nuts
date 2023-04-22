package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NIOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractNInputSource implements NInputSource {
    private NSession session;

    public AbstractNInputSource(NSession session) {
        NAssert.requireSession(session);
        this.session = session;
    }

    public NSession getSession() {
        return session;
    }

    @Override
    public Stream<String> getLines() {
        return getLines(null);
    }


    @Override
    public String readString() {
        return new String(readBytes());
    }

    @Override
    public byte[] readBytes() {
        try (InputStream in = getInputStream()) {
            return NIOUtils.readBytes(in);
        } catch (IOException e) {
            throw new NIOException(getSession(), e);
        }
    }

    @Override
    public BufferedReader getBufferedReader() {
        return getBufferedReader(null);
    }


    @Override
    public BufferedReader getBufferedReader(Charset cs) {
        Reader r = getReader(cs);
        if (r instanceof BufferedReader) {
            return (BufferedReader) r;
        }
        return new BufferedReader(r);
    }

    @Override
    public List<String> tail(int count, Charset cs) {
        LinkedList<String> lines = new LinkedList<>();
        BufferedReader br = getBufferedReader(cs);
        String line;
        try {
            int count0 = 0;
            while ((line = br.readLine()) != null) {
                lines.add(line);
                count0++;
                if (count0 > count) {
                    lines.remove();
                }
            }
        } catch (IOException e) {
            throw new NIOException(getSession(), e);
        }
        return lines;
    }

    @Override
    public List<String> head(int count) {
        return head(count, null);
    }

    @Override
    public List<String> head(int count, Charset cs) {
        return getLines(cs).limit(count).collect(Collectors.toList());
    }

    @Override
    public List<String> tail(int count) {
        return tail(count, null);
    }


    @Override
    public Stream<String> getLines(Charset cs) {
        BufferedReader br = getBufferedReader(cs);
        try {
            return br.lines().onClose(() -> {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (Error | RuntimeException e) {
            try {
                br.close();
            } catch (IOException ex) {
                try {
                    e.addSuppressed(ex);
                } catch (Throwable ignore) {
                }
            }
            throw e;
        }
    }

    @Override
    public Reader getReader() {
        return getReader(null);
    }

    @Override
    public Reader getReader(Charset cs) {
        CharsetDecoder decoder = nonNullCharset(cs).newDecoder();
        Reader reader = new InputStreamReader(getInputStream(), decoder);
        return new BufferedReader(reader);
    }

    protected Charset nonNullCharset(Charset c) {
        if (c == null) {
            return StandardCharsets.UTF_8;
        }
        return c;
    }

}
