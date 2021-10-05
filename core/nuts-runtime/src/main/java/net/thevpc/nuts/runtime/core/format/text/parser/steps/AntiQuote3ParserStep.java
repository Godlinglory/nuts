package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTerminalCommand;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManager;
import net.thevpc.nuts.runtime.core.format.text.parser.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.NutsText;

public class AntiQuote3ParserStep extends ParserStep {

    boolean escape = false;
    StringBuilder start = new StringBuilder();
    StringBuilder end = new StringBuilder();
    StringBuilder value = new StringBuilder();
    int maxSize = 3;
    private static final int START_QUOTES=0;
    private static final int CONTENT=1;
    private static final int END_QUOTES=2;
    private static final int COMPLETED=3;
    int status = START_QUOTES;
    char c0;
    boolean spreadLines;
    NutsSession session;
    boolean exitOnBrace;

    public AntiQuote3ParserStep(char c, boolean spreadLines, NutsSession session,boolean exitOnBrace) {
        start.append(c0=c);
        this.spreadLines=spreadLines;
        this.session = session;
        this.exitOnBrace = exitOnBrace;
    }

//    public AntiQuote3ParserStep(String c) {
//        start.append(c);
//    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State p, boolean wasNewLine) {
        switch (status) {
            case START_QUOTES: {
                if (c == c0) {
                    if (start.length() < maxSize) {
                        start.append(c);
                    } else {
                        //too much, ignore it all and consider it as forPlain
                        start.append(c);
                        p.applyDropReplacePreParsedPlain(start.toString(),exitOnBrace);
                    }
                } else {
                    if (start.length() == maxSize) {
                        switch (c) {
                            case '\\': {
                                escape = true;
                                break;
                            }
                            default: {
                                value.append(c);
                            }
                        }
                        status = CONTENT;
                    }else{
                        start.append(c);
                        p.applyDropReplacePreParsedPlain(start.toString(),exitOnBrace);
                    }
                }
//                    p.applyContinue();
                return;//new ConsumeResut(ConsumeResutType.CONTINUE, null);
            }
            case CONTENT: {
                if (escape) {
                    escape = false;
                    value.append(c);
                } else {
                    if (c == c0) {
                        status = END_QUOTES;
                        end.append(c);
                        if (end.length() >= start.length()) {
                            p.applyPop();
                            return;//new ConsumeResult(ConsumeResultType.POP, null);
                        }
                    } else {
                        switch (c) {
                            case '\\': {
                                escape = true;
                                break;
                            }
                            default: {
                                value.append(c);
                            }
                        }
                    }
                }
                return;//new ConsumeResut(ConsumeResultType.CONTINUE, null);
            }
            case END_QUOTES: {
                if (c == c0) {
                    end.append(c);
                } else {
                    if(end.length()==maxSize){
                        //!!excellent
                        p.applyPopReplay(c);
                    }else {
                        value.append(end);
                        end.delete(0, end.length());
                        switch (c) {
                            case '\\': {
                                escape = true;
                                break;
                            }
                            default: {
                                value.append(c);
                            }
                        }
                        status = CONTENT;
                    }
                }
                return;
            }
        }
        throw new IllegalArgumentException("unexpected");
    }

    @Override
    public void appendChild(ParserStep tt) {
        throw new UnsupportedOperationException("unsupported operation: appendChild");
    }

    @Override
    public NutsText toText() {
        char[] dst = new char[value.length()];
        value.getChars(0,value.length(), dst,0 );
        DefaultNutsTextManager factory0 = (DefaultNutsTextManager) session.text();
        int i=0;
        int endOffset=-1;
        if(dst.length>0 && dst[i]=='!') {
            i++;
        }
        while(i<dst.length) {
            if (Character.isWhitespace(dst[i])) {
                endOffset = i;
                break;
            }else if (!Character.isAlphabetic(dst[i]) && !Character.isDigit(dst[i]) && dst[i]!='-' && dst[i]!='_') {
                endOffset=i;
                break;
            }
            i++;
        }
        if(endOffset==-1){
            endOffset=dst.length;
        }

        StringBuilder w=new StringBuilder();
        i=endOffset;
        while(i<dst.length && Character.isWhitespace(dst[i])){
            w.append(dst[i]);
            i++;
        }

        String cmd = new String(dst, 0, endOffset);
        String value = new String(dst, i, dst.length - i);
        if(cmd.startsWith("!")) {
            String cmd0 = cmd.substring(1);
            String start2 = this.start.toString() + "!";
            switch (cmd){
                case "!anchor":{
                    return factory0.createAnchor(
                            start2,
                            w.toString(),
                            end.toString(),
                            value
                    );
                }
                case "!link":{
                    return factory0.createLink(
                            start2,
                            w.toString(),
                            end.toString(),
                            factory0.ofPlain(value)
                    );
                }
            }
            NutsTerminalCommand ntc=NutsTerminalCommand.of(cmd0,value);
            return factory0.createCommand(
                    start2,
                    ntc,
                    w.toString(),
                    end.toString()
            );
        }
        if(value.isEmpty()){
            if(w.length()>0 && cmd.length()>0){
                return factory0.createCode(
                        start.toString(),
                        cmd,
                        w.toString(),
                        end.toString(),
                        value
                );
            }
            return factory0.createCode(
                    start.toString(),
                    "",
                    "",
                    end.toString(),
                    cmd+w.toString()+value
            );
        }
        return factory0.createCode(
                start.toString(),
                cmd,
                w.toString(),
                end.toString(),
                value
        );
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {
        if(!isComplete()) {
            while (end.length() < start.length()) {
                end.append(c0);
            }
        }
        p.applyPop();
    }

    public boolean isComplete() {
        return status == 2 && end.length() == start.length();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Quoted(" + CoreStringUtils.dblQuote(start.toString()));
        sb.append(",");
        sb.append(CoreStringUtils.dblQuote(value.toString()));
        sb.append(",status=").append(status == 0 ? "EXPECT_START" : status == 1 ? "EXPECT_CONTENT" : status == 2 ? "EXPECT_END" : String.valueOf(status));
        sb.append(",end=");
        sb.append(end);
        if (escape) {
            sb.append(",<ESCAPED>");
        }
        sb.append(isComplete() ? "" : ",incomplete");
        return sb.append(")").toString();
    }
}
