package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManager;
import net.thevpc.nuts.runtime.core.format.text.parser.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NutsText;

public class TitleParserStep extends ParserStep {

    StringBuilder start = new StringBuilder();
    List<ParserStep> children = new ArrayList<>();
    private NutsSession session;
    public TitleParserStep(String c, NutsSession session) {
        start.append(c);
        this.session = session;
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State p, boolean wasNewLine) {
        if (c == ' ' && children.isEmpty()) {
            start.append(c);
        } else if (c == '\n' || c == '\r') {
            p.applyPopReplay(c);
        } else {
            p.applyPush(c, false, false, false);
        }
    }

    @Override
    public void appendChild(ParserStep tt) {
        children.add(tt);
    }

    @Override
    public NutsText toText() {
        String s = start.toString();
//        NutsTextManager text = ws.text();
        DefaultNutsTextManager factory0 = (DefaultNutsTextManager) session.getWorkspace().text();
        String s0=s.trim();
        NutsText child=null;
        if (children.size() == 1) {
            child=children.get(0).toText();
        }else{
            List<NutsText> all = new ArrayList<>();
            for (ParserStep a : children) {
                all.add(a.toText());
            }
            child= session.getWorkspace().text().forList(all).simplify();
        }
        return factory0.createTitle(s,s0.length()-1 ,child,isComplete());
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {
        p.applyPop();
    }

    public boolean isComplete() {
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Title(" + CoreStringUtils.dblQuote(start.toString()));
        for (ParserStep parserStep : children) {
            sb.append(",");
            sb.append(parserStep.toString());
        }
        return sb.append(")").toString();
    }

}
