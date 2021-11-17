package net.thevpc.nuts.runtime.standalone.text.parser.steps;

import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.NutsText;

public class DispatchAction extends ParserStep {
    private boolean spreadLines;
    private boolean lineStart;
    private boolean exitOnBrace;

    public DispatchAction(boolean spreadLines, boolean lineStart,boolean exitOnBrace) {
        this.spreadLines = spreadLines;
        this.lineStart = lineStart;
        this.exitOnBrace = exitOnBrace;
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State p, boolean wasNewLine) {
        p.applyDrop();
        p.applyPush(c, spreadLines, lineStart, exitOnBrace);
    }

    @Override
    public void appendChild(ParserStep tt) {
        throw new IllegalArgumentException();
    }

    @Override
    public NutsText toText() {
        throw new IllegalArgumentException();
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {
        p.applyDrop();
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
