package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;

import java.util.*;

import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.NCodeHighlighter;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.*;

public class HadraCodeHighlighter implements NCodeHighlighter {

    private Set<String> reservedWords = new HashSet<>();
    private NWorkspace ws;

    @Override
    public String getId() {
        return "handra";
    }

    public HadraCodeHighlighter(NWorkspace ws) {
        this.ws = ws;
        reservedWords.addAll(NCodeHighlighterHelper.loadNames("hadra.kw1",getClass()));
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        String s = context.getConstraints();
        if(s==null){
            return DEFAULT_SUPPORT;
        }
        switch (s){
            case "hadra":
            case "hadra-lang":
            case "hl":
            case "text/x-hl":
            case "text/x-hadra":
            case "application/x-hadra":
            case "text/hl":
            case "text/hadra":
            case "application/hadra":
            {
                return NComponent.DEFAULT_SUPPORT;
            }
        }
        return NComponent.NO_SUPPORT;
    }

    @Override
    public NText tokenToText(String text, String nodeType, NTexts txt, NSession session) {
        String str = String.valueOf(text);
        switch (nodeType.toLowerCase()) {
            case "separator": {
                return txt.ofStyled(str, NTextStyle.separator());
            }
            case "keyword": {
                return txt.ofStyled(str, NTextStyle.keyword());
            }
        }
        return txt.ofPlain(str);
    }

    @Override
    public NText stringToText(String text, NTexts txt, NSession session) {
        List<NText> all = new ArrayList<>();
        StringReaderExt ar = new StringReaderExt(text);
        while (ar.hasNext()) {
            switch (ar.peekChar()) {
                case '{':
                case '}':
                case '(':
                case ')':
                case '[':
                case ']':
                case '@':
                case '=':
                case '+':
                case '*':
                case '%':
                case ':':
                case '?':
                case '<':
                case '>':
                case '!':
                case ';': {
                    all.add(txt.ofStyled(String.valueOf(ar.nextChar()), NTextStyle.separator()));
                    break;
                }
                case '\'': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readJSSimpleQuotes(session, ar)));
                    break;
                }
                case '"': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readJSDoubleQuotesString(session, ar)));
                    break;
                }
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readNumber(session, ar)));
                    break;
                }
                case '.':
                case '-': {
                    NText[] d = StringReaderExtUtils.readNumber(session, ar);
                    if (d != null) {
                        all.addAll(Arrays.asList(d));
                    } else {
                        all.add(txt.ofStyled(String.valueOf(ar.nextChar()), NTextStyle.separator()));
                    }
                    break;
                }
                case '/': {
                    if (ar.peekChars("//")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashSlashComments(session, ar)));
                    } else if (ar.peekChars("/*")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashStarComments(session, ar)));
                    } else {
                        all.add(txt.ofStyled(String.valueOf(ar.nextChar()), NTextStyle.separator()));
                    }
                    break;
                }
                default: {
                    if (Character.isWhitespace(ar.peekChar())) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(session, ar)));
                    } else {
                        NText[] d = StringReaderExtUtils.readJSIdentifier(session, ar);
                        if (d != null) {
                            if (d.length == 1 && d[0].getType() == NTextType.PLAIN) {
                                String txt2 = ((NTextPlain) d[0]).getText();
                                if (reservedWords.contains(txt2)) {
                                    d[0] = txt.ofStyled(d[0], NTextStyle.keyword());
                                }
                            }
                            all.addAll(Arrays.asList(d));
                        } else {
                            all.add(txt.ofStyled(String.valueOf(ar.nextChar()), NTextStyle.separator()));
                        }
                    }
                    break;
                }
            }
        }
        return txt.ofList(all.toArray(new NText[0]));
    }
}
