package net.thevpc.nuts.runtime.standalone.text.parser.steps;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.DefaultNutsTexts;
import net.thevpc.nuts.runtime.standalone.text.parser.*;

public class AntiQuote3ParserStep extends ParserStep {

    StringBuilder start = new StringBuilder();
    StringBuilder end = new StringBuilder();
    StringBuilder value = new StringBuilder();
    int maxSize = 3;
    private static final int START_QUOTES=0;
    private static final int CONTENT=1;
    private static final int CONTENT_ANTISLASH=2;
    private static final int CONTENT_ANTISLASH_TIC1=3;
    private static final int CONTENT_ANTISLASH_TIC2=4;
    private static final int END_QUOTES=20;
    int status = START_QUOTES;
    char antiQuote;
    boolean spreadLines;
    NutsSession session;
    boolean exitOnBrace;

    public AntiQuote3ParserStep(char c, boolean spreadLines, NutsSession session,boolean exitOnBrace) {
        start.append(antiQuote =c);
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
                if (c == antiQuote) {
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
                                status = CONTENT_ANTISLASH;
                                break;
                            }
                            default: {
                                value.append(c);
                                status = CONTENT;
                            }
                        }
                    }else{
                        start.append(c);
                        p.applyDropReplacePreParsedPlain(start.toString(),exitOnBrace);
                    }
                }
//                    p.applyContinue();
                return;//new ConsumeResut(ConsumeResutType.CONTINUE, null);
            }
            case CONTENT: {
                if (c == antiQuote) {
                    status = END_QUOTES;
                    end.append(c);
                    if (end.length() >= start.length()) {
                        p.applyPop();
                        return;//new ConsumeResult(ConsumeResultType.POP, null);
                    }
                } else {
                    switch (c) {
                        case '\\': {
                            status=CONTENT_ANTISLASH;
                            break;
                        }
                        default: {
                            value.append(c);
                        }
                    }
                }
                return;//new ConsumeResut(ConsumeResultType.CONTINUE, null);
            }
            case CONTENT_ANTISLASH: {
                if (c == antiQuote) {
                    status = CONTENT_ANTISLASH_TIC1;
                } else {
                    value.append('\\');
                    value.append(c);
                    status = CONTENT;
                }
                return;
            }
            case CONTENT_ANTISLASH_TIC1: {
                if (c == antiQuote) {
                    status = CONTENT_ANTISLASH_TIC2;
                } else {
                    value.append('\\');
                    value.append('`');
                    value.append(c);
                    status = CONTENT;
                }
                return;
            }
            case CONTENT_ANTISLASH_TIC2: {
                if (c == antiQuote) {
                    value.append("```");
                    status = CONTENT;
                } else {
                    value.append('\\');
                    value.append('`');
                    value.append('`');
                    value.append(c);
                    status = CONTENT;
                }
                return;
            }
            case END_QUOTES: {
                if (c == antiQuote) {
                    end.append(c);
                } else {
                    if(end.length()==maxSize){
                        //valid ending reached!
                        p.applyPopReplay(c);
                    }else {
                        switch (c) {
                            case '\\': {
                                value.append(end);
                                end.delete(0, end.length());
                                status = CONTENT_ANTISLASH;
                                break;
                            }
                            default: {
                                value.append(end);
                                end.delete(0, end.length());
                                value.append(c);
                                status = CONTENT;
                            }
                        }
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
        StringBuilder value2 = new StringBuilder(getPartialValue());
        char[] dst = new char[value2.length()];
        value2.getChars(0,value2.length(), dst,0 );

        DefaultNutsTexts factory0 = (DefaultNutsTexts) NutsTexts.of(session);
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

    private String getPartialValue() {
        StringBuilder value2=new StringBuilder(value);
        switch (status){
            case CONTENT_ANTISLASH:{
                value2.append('\\');
                break;
            }
            case CONTENT_ANTISLASH_TIC1:{
                value2.append("\\`");
                break;
            }
            case CONTENT_ANTISLASH_TIC2:{
                value2.append("\\``");
                break;
            }
        }
        return value2.toString();
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {
        if(!isComplete()) {
            while (end.length() < start.length()) {
                end.append(antiQuote);
            }
        }
        p.applyPop();
    }

    public boolean isComplete() {
        return status == END_QUOTES && end.length() == start.length();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Quoted(" + NutsUtilStrings.dblQuotes(start.toString()));
        sb.append(",");
        sb.append(NutsUtilStrings.dblQuotes(getPartialValue()));
        sb.append(",status=").append(status == 0 ? "EXPECT_START" : status == 1 ? "EXPECT_CONTENT" : status == 2 ? "EXPECT_END" : String.valueOf(status));
        sb.append(",end=");
        sb.append(end);
        sb.append(isComplete() ? "" : ",incomplete");
        return sb.append(")").toString();
    }
}
