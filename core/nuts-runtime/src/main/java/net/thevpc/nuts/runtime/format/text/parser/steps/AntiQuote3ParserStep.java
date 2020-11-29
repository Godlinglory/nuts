package net.thevpc.nuts.runtime.format.text.parser.steps;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.format.text.DefaultNutsTextNodeFactory;
import net.thevpc.nuts.runtime.format.text.parser.*;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.format.text.FPrintCommands;
import net.thevpc.nuts.runtime.format.text.TextFormat;
import net.thevpc.nuts.runtime.format.text.TextFormats;

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
    NutsWorkspace ws;

    public AntiQuote3ParserStep(char c, boolean spreadLines,NutsWorkspace ws) {
        start.append(c0=c);
        this.spreadLines=spreadLines;
        this.ws=ws;
    }

//    public AntiQuote3ParserStep(String c) {
//        start.append(c);
//    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State p) {
        switch (status) {
            case START_QUOTES: {
                if (c == c0) {
                    if (start.length() < maxSize) {
                        start.append(c);
                    } else {
                        //too much, ignore it all and consider it as plain
                        start.append(c);
                        p.applyDropReplace(new PlainParserStep(start.toString(),spreadLines,false,ws));
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
                        p.applyDropReplace(new PlainParserStep(start.toString(),spreadLines,false,ws));
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
                            return;//new ConsumeResult(ConsumeResutType.POP, null);
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
                        p.applyPopReject(c);
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
        throw new IllegalArgumentException("Unexpected");
    }

    @Override
    public void appendChild(ParserStep tt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NutsTextNode toNode() {
        char[] dst = new char[value.length()];
        value.getChars(0,value.length(), dst,0 );
        DefaultNutsTextNodeFactory factory0 = (DefaultNutsTextNodeFactory) ws.formats().text().factory();
        if(dst.length>0 && !Character.isWhitespace(dst[0])){
            int i=0;
            int offset=0;
            boolean startWithExclamation=false;
            if(dst[i]=='!'){
                startWithExclamation=true;
                i++;
                offset=1;
            }
            while(i<dst.length){
                if(Character.isWhitespace(dst[i])){
                    int i0=i;
                    StringBuilder w=new StringBuilder();
                    while(i<dst.length && Character.isWhitespace(dst[i])){
                        w.append(dst[i]);
                        i++;
                    }
                    String cmd = new String(dst, 0, i0);
                    String value = new String(dst, i, dst.length - (i));
                    if(cmd.startsWith("!")) {
                        String cmd0 = value.substring(1);
                        String start2 = this.start.toString() + "!";
                        switch (cmd){
                            case "!anchor":{
                                return factory0.createAnchor(
                                        start2,
                                        cmd0,
                                        w.toString(),
                                        end.toString(),
                                        value
                                );
                            }
                            case "!link":{
                                return factory0.createLink(
                                        start2,
                                        cmd0,
                                        w.toString(),
                                        end.toString(),
                                        value
                                );
                            }
                        }

                        TextFormat yy = DefaultNutsTextNodeCommand.parseTextFormat(cmd0);
                        if(yy!=null){
                            return factory0.createCommand(
                                    start2,
                                    cmd.substring(1),
                                    w.toString(),
                                    end.toString(),
                                    value,
                                    yy
                            );
                        }
                    }
                    return factory0.createCommand(
                            start.toString(),
                            cmd,
                            w.toString(),
                            end.toString(),
                            value
                    );
                }else if(!Character.isAlphabetic(dst[i]) && dst[i]!='-'){
                    break;
                }
                i++;
            }
        }
        return factory0.createCommand(
                start.toString(),
                "",
                "",
                end.toString(),
                new String(dst)
        );
    }

    protected TextFormat parseTextFormat(String v){
        //this might be a command !!
        v = v.trim();
        switch (v) {
            case FPrintCommands.MOVE_LINE_START: {
                return TextFormats.MOVE_LINE_START;
            }
            case FPrintCommands.LATER_RESET_LINE: {
                return TextFormats.LATER_RESET_LINE;
            }
            case FPrintCommands.MOVE_UP: {
                return TextFormats.MOVE_UP;
            }
            default: {
                return null;
            }
        }
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
