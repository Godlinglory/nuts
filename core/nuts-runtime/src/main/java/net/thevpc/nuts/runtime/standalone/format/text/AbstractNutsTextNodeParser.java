package net.thevpc.nuts.runtime.standalone.format.text;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeParser;
import net.thevpc.nuts.NutsTextNodeVisitor;
import net.thevpc.nuts.NutsWorkspace;

import java.io.*;

public abstract class AbstractNutsTextNodeParser implements NutsTextNodeParser {
    protected int bufferSize=4096;
    protected NutsWorkspace ws;

    public AbstractNutsTextNodeParser(NutsWorkspace ws) {
        this.ws = ws;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public long parseIncremental(char buf, NutsTextNodeVisitor visitor) {
        return parseIncremental(new char[]{buf}, visitor);
    }

    @Override
    public long parse(InputStream in, NutsTextNodeVisitor visitor) {
        return parse(new BufferedReader(new InputStreamReader(in)),visitor);
    }
    public long parse(Reader in, NutsTextNodeVisitor visitor) {
        int count = 0;
        char[] buffer = new char[bufferSize];
        int r;
        while (true) {
            try {
                if (!((r = in.read(buffer)) > 0)) break;
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            parseIncremental(buffer,0,r,visitor);
        }
        parseRemaining(visitor);
        return count;
    }

    @Override
    public NutsTextNode parse(InputStream in) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(ws);
        parse(in, doc);
        return doc.getRootOrNull();
    }

    @Override
    public NutsTextNode parse(Reader in) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(ws);
        parse(in, doc);
        return doc.getRootOrNull();
    }

    @Override
    public long parseIncremental(byte[] buf, NutsTextNodeVisitor visitor) {
        return parseIncremental(buf, 0, buf.length, visitor);
    }

    @Override
    public long parseIncremental(char[] buf, NutsTextNodeVisitor visitor) {
        return parseIncremental(new String(buf), visitor);
    }

    @Override
    public long parseIncremental(String buf, NutsTextNodeVisitor visitor) {
        return parseIncremental(buf.getBytes(), visitor);
    }

    @Override
    public NutsTextNode parseIncremental(byte[] buf) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(ws);
        parseIncremental(buf, 0, buf.length, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsTextNode parseIncremental(char[] buf) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(ws);
        parseIncremental(new String(buf), doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsTextNode parseIncremental(String buf) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(ws);
        parseIncremental(buf.getBytes(), doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsTextNode parseIncremental(char buf) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(ws);
        parseIncremental(buf, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsTextNode parseIncremental(byte[] buf, int off, int len) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(ws);
        parseIncremental(buf,off,len, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsTextNode parseIncremental(char[] buf, int off, int len) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(ws);
        parseIncremental(buf,off,len, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsTextNode parseRemaining() {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(ws);
        parseRemaining(doc);
        return doc.getRootOrNull();
    }
}
