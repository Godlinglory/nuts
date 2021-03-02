package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.StringReaderExt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCodeFormat;

public class JsonCodeFormatter implements NutsCodeFormat {
    private NutsWorkspace ws;
    private NutsTextManager factory;

    public JsonCodeFormatter(NutsWorkspace ws) {
        this.ws = ws;
        factory = ws.formats().text();
    }


    @Override
    public NutsTextNode tokenToNode(String text, String nodeType) {
        return factory.plain(text);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> criteria) {
        String s = criteria.getConstraints();
        return "json".equals(s) ? NutsComponent.DEFAULT_SUPPORT : NutsComponent.NO_SUPPORT;
    }

    @Override
    public NutsTextNode textToNode(String text) {
        List<NutsTextNode> all = new ArrayList<>();
        StringReaderExt ar = new StringReaderExt(text);
        while (ar.hasNext()) {
            switch (ar.peekChar()) {
                case '{':
                case '}':
                case ':': {
                    all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    break;
                }
                case '\'': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readJSSimpleQuotes(ws, ar)));
                    break;
                }
                case '"': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readJSDoubleQuotesString(ws, ar)));
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
                    all.addAll(Arrays.asList(StringReaderExtUtils.readNumber(ws, ar)));
                    break;
                }
                case '.':
                case '-':{
                    NutsTextNode[] d = StringReaderExtUtils.readNumber(ws, ar);
                    if(d!=null) {
                        all.addAll(Arrays.asList(d));
                    }else{
                        all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    }
                    break;
                }
                case '/':{
                    if(ar.peekChars("//")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashSlashComments(ws,ar)));
                    }else if(ar.peekChars("/*")){
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashStarComments(ws,ar)));
                    }else{
                        all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    }
                    break;
                }
                default: {
                    if(Character.isWhitespace(ar.peekChar())){
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ws,ar)));
                    }else {
                        NutsTextNode[] d = StringReaderExtUtils.readJSIdentifier(ws, ar);
                        if (d != null) {
                            if (d.length == 1 && d[0].getType() == NutsTextNodeType.PLAIN) {
                                String txt = ((NutsTextNodePlain) d[0]).getText();
                                switch (txt) {
                                    case "true":
                                    case "false": {
                                        d[0] = factory.styled(d[0], NutsTextNodeStyle.keyword());
                                        break;
                                    }
                                }
                            }
                            all.addAll(Arrays.asList(d));
                        } else {
                            all.add(factory.styled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                        }
                    }
                    break;
                }
            }
        }
        return factory.list(all.toArray(new NutsTextNode[0]));
    }
}
