package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.outputstream.OutputHelper;
import net.thevpc.nuts.runtime.standalone.text.parser.AbstractNutsTextNodeParserDefaults;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;
import net.thevpc.nuts.text.*;

public class FormatOutputStreamSupport {
    private NutsTextNodeWriter nodeWriter;
    private NutsTextParser parser;
    private boolean formatEnabled = true;
    private NutsSession session;
    private NutsWorkspace ws;
    private NutsTextTransformConfig writeConfiguration = new NutsTextTransformConfig();
    private NutsTextVisitor nutsTextNodeVisitor = node -> {
        nodeWriter.writeNode(node);
    };

    public FormatOutputStreamSupport() {

    }

    public FormatOutputStreamSupport(OutputHelper rawOutput, NutsSession session, NutsSystemTerminalBase term,boolean filtered) {
        this.session = session;
        this.ws = session.getWorkspace();
        this.parser = AbstractNutsTextNodeParserDefaults.createDefault(session);
        this.nodeWriter = new NutsTextNodeWriterRenderer(rawOutput, session, term)
                .setWriteConfiguration(writeConfiguration.setFiltered(false));
        this.writeConfiguration.setFiltered(filtered);
    }

    public NutsTextParser getParser() {
        return parser;
    }

    public FormatOutputStreamSupport setParser(NutsTextParser parser) {
        this.parser = parser == null ? NutsTexts.of(session).parser() : parser;
        return this;
    }

    public boolean isFormatEnabled() {
        return formatEnabled;
    }

    public FormatOutputStreamSupport setFormatEnabled(boolean formatEnabled) {
        this.formatEnabled = formatEnabled;
        writeConfiguration.setFiltered(!formatEnabled);
        return this;
    }

    public void processByte(int oneByte) {
        processBytes(new byte[]{(byte) oneByte}, 0, 1);
    }

    public void processBytes(byte[] buf, int off, int len) {
        if (!isFormatEnabled()) {
            nodeWriter.writeRaw(buf, off, len);
        } else {
            parser.parseIncremental(buf, off, len, new NutsTextVisitor() {
                @Override
                public void visit(NutsText node) {
//                    JOptionPane.showMessageDialog(null,node.getType()+":"+node);
                    nutsTextNodeVisitor.visit(node);
                }
            });
        }
    }

    public void processChars(char[] buf, int off, int len) {
        if (!isFormatEnabled()) {
            nodeWriter.writeRaw(buf, off, len);
        } else {
            parser.parseIncremental(buf, off, len, new NutsTextVisitor() {
                @Override
                public void visit(NutsText node) {
//                    JOptionPane.showMessageDialog(null,node.getType()+":"+node);
                    nutsTextNodeVisitor.visit(node);
                }
            });
        }
    }

    public void reset() {
        flush();
    }

    public void flush() {
        nodeWriter.flush();
        parser.parseRemaining(nutsTextNodeVisitor);
//        if(!some) {
//            flushLater();
//        }
        nodeWriter.flush();
    }

    public boolean isIncomplete() {
        return parser.isIncomplete();
    }

    @Override
    public String toString() {
        return "FormatOutputStreamSupport(" + parser.toString() + ";" + this.nodeWriter + ")";
    }

}
