package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.parser.*;
import net.thevpc.nuts.runtime.core.format.text.bloc.*;
import net.thevpc.nuts.NutsTextFormatTheme;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.DefaultNutsTextStyleGenerator;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.temporal.Temporal;
import java.util.*;
import net.thevpc.nuts.NutsCodeFormat;
import net.thevpc.nuts.runtime.standalone.DefaultNutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsComponent;

public class DefaultNutsTextNodeFactory implements NutsTextNodeFactory {

    private NutsWorkspace ws;
    private NutsSession session;
    private NutsTextFormatTheme styleTheme;
    private PlainBlocTextFormatter plainBlocTextFormatter;
    JavaBlocTextFormatter javaBlocTextFormatter;
    HadraBlocTextFormatter hadraBlocTextFormatter;
    XmlBlocTextFormatter xmlBlocTextFormatter;
    JsonBlocTextFormatter jsonBlocTextFormatter;
    ShellBlocTextFormatter shellBlocTextFormatter;

    public DefaultNutsTextNodeFactory(NutsWorkspace ws, NutsTextFormatTheme styleTheme) {
        this.ws = ws;
        this.styleTheme = styleTheme;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public DefaultNutsTextNodeFactory setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsTextNode blank() {
        return plain("");
    }

    @Override
    public NutsTextNode nodeFor(Object t) {
        if (t == null) {
            return blank();
        }
        if (t instanceof NutsTextNode) {
            return (NutsTextNode) t;
        }
        if (t instanceof NutsFormattable) {
            return ws.formats().text().parse(((NutsFormattable) t).formatter().format());
        }
        if (t instanceof NutsMessage) {
            return _NutsFormattedMessage_toString((NutsMessage) t);
        }
        if (t instanceof NutsString) {
            return ((NutsString) t).toNode();
        }
        if (t instanceof Number) {
            return styled(t.toString(), NutsTextNodeStyle.number());
        }
        if (t instanceof Date || t instanceof Temporal) {
            return styled(t.toString(), NutsTextNodeStyle.date());
        }
        if (t instanceof Boolean) {
            return styled(t.toString(), NutsTextNodeStyle.bool());
        }
        if (t instanceof Path || t instanceof File || t instanceof URL) {
            return styled(t.toString(), NutsTextNodeStyle.path());
        }
        if (t instanceof Throwable) {
            return styled(
                    CoreStringUtils.exceptionToString((Throwable) t),
                    NutsTextNodeStyle.error()
            );
        }
        return plain(t.toString());
    }

    private NutsTextNode _NutsFormattedMessage_toString(NutsMessage m) {
        NutsTextFormatStyle style = m.getStyle();
        if (style == null) {
            style = NutsTextFormatStyle.JSTYLE;
        }
        Object[] params = m.getParams();
        if (params == null) {
            params = new Object[0];
        }
        String msg = m.getMessage();
        String sLocale = session == null ? null : session.getLocale();
        Locale locale = CoreStringUtils.isBlank(sLocale) ? null : new Locale(sLocale);
        Object[] args2 = new Object[params.length];
        NutsTextFormatManager txt = ws.formats().text();
        NutsTextNodeFactory fct = txt.factory().setSession(session);
        for (int i = 0; i < args2.length; i++) {
            Object a = params[i];
            if (a instanceof Number || a instanceof Date || a instanceof Temporal) {
                //do nothing, support format pattern
                args2[i] = a;
            } else {
                args2[i] = fct.nodeFor(a).toString();
            }
        }
        switch (style) {
            case CSTYLE: {
                StringBuilder sb = new StringBuilder();
                new Formatter(sb, locale).format(msg, args2);
                return ws.formats().text().parse(sb.toString());
            }
            case JSTYLE: {
                return ws.formats().text().parse(MessageFormat.format(msg, args2));
            }
        }
        throw new NutsUnsupportedEnumException(ws, style);
    }

    @Override
    public NutsTextNode plain(String t) {
        return new DefaultNutsTextNodePlain(ws, t);
    }

    @Override
    public NutsTextNode list(NutsTextNode... nodes) {
        return list(Arrays.asList(nodes));
    }

    @Override
    public NutsTextNode list(Collection<NutsTextNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return plain("");
        }
        if (nodes.size() == 1) {
            return (NutsTextNode) nodes.toArray()[0];
        }
        return new DefaultNutsTextNodeList(ws, nodes.toArray(new NutsTextNode[0]));
    }

    @Override
    public NutsTextNode styled(String other, NutsTextNodeStyle... decorations) {
        return styled(plain(other), decorations);
    }

    @Override
    public NutsTextNode styled(NutsString other, NutsTextNodeStyle... decorations) {
        return styled(ws.formats().text().parse(other.toString()), decorations);
    }

    @Override
    public NutsTextNode styled(NutsTextNode other, NutsTextNodeStyle... decorations) {
        switch (decorations.length) {
            case 0:
                return other;
            case 1:
                return styled(other, decorations[0]);
        }

        NutsTextNode n = other;
        for (int i = decorations.length - 1; i >= 0; i--) {
            n = styled(n, decorations[i]);
        }
        return n;
    }

    @Override
    public NutsTextNode command(String command) {
        return command(command, "");
    }

    public NutsTextNode command(String command, String args) {
        switch (command) {
            case "anchor": {
                return createAnchor(
                        "```!",
                        command, "", "```", args
                );
            }
            case "link": {
                return createLink(
                        "```!",
                        command, "", "```", args
                );
            }
        }
        return createCommand(
                "```!",
                command, "", "```", args
        );
    }

    @Override
    public NutsTextNode code(String lang, String text) {
        if (text == null) {
            text = "";
        }
        DefaultNutsTextNodeFactory factory0 = (DefaultNutsTextNodeFactory) ws.formats().text().factory();
        if (text.indexOf('\n') >= 0) {
            return factory0.createCode("```",
                    lang, "\n", "```", text
            );
        } else {
            return factory0.createCode("```",
                    lang, "", "```", text
            );
        }
    }

    public NutsTextNode title(NutsTextNode t, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("#");
        }
        sb.append(")");
        return createTitle(sb.toString(), level, t, true);
    }

    public NutsTextNode fg(String t, int level) {
        return fg(plain(t), level);
    }

    public NutsTextNode fg(NutsTextNode t, int level) {
        NutsTextNodeStyle textStyle = NutsTextNodeStyle.primary(level);
        return createStyled("##:p" + level + ":", "##", t, textStyle, true);
    }

    public NutsTextNode bg(String t, int level) {
        return bg(plain(t), level);
    }

    public NutsTextNode bg(NutsTextNode t, int level) {
        NutsTextNodeStyle textStyle = NutsTextNodeStyle.primary(level);
        return createStyled("##:s" + level + ":", "##", t, textStyle, true);
    }

    public NutsTextNode comments(String image) {
        return fg(image, 4);
    }

    public NutsTextNode literal(String image) {
        return fg(image, 1);
    }

    public NutsTextNode stringLiteral(String image) {
        return fg(image, 3);
    }

    public NutsTextNode numberLiteral(String image) {
        return fg(image, 1);
    }

    public NutsTextNode reservedWord(String image) {
        return fg(image, 1);
    }

    public NutsTextNode annotation(String image) {
        return fg(image, 3);
    }

    public NutsTextNode separator(String image) {
        return fg(image, 6);
    }

    public NutsTextNode commandName(String image) {
        return fg(image, 1);
    }

    public NutsTextNode subCommand1Name(String image) {
        return fg(image, 2);
    }

    public NutsTextNode subCommand2Name(String image) {
        return fg(image, 3);
    }

    public NutsTextNode optionName(String image) {
        return fg(image, 4);
    }

    public NutsTextNode userInput(String image) {
        return fg(image, 8);
    }

    /**
     * this is the default theme!
     *
     * @param other other
     * @param textNodeStyle textNodeStyle
     * @return NutsTextNode
     */
    public NutsTextNode styled(NutsTextNode other, NutsTextNodeStyle textNodeStyle) {
        if (other == null) {
            return plain("");
        }
        if (textNodeStyle == null) {
            return other;
        }
        switch (textNodeStyle.getType()) {
            case FORE_COLOR: {
                return createStyled("##:f" + textNodeStyle.getVariant() + ":", "##", other, textNodeStyle, true);
            }
            case BACK_COLOR: {
                return createStyled("##:b" + textNodeStyle.getVariant() + ":", "##", other, textNodeStyle, true);
            }
            case FORE_TRUE_COLOR: {
                String s = Integer.toString(0, textNodeStyle.getVariant());
                while (s.length() < 8) {
                    s = "0" + s;
                }
                return createStyled("##:fx" + s + ":", "##", other, textNodeStyle, true);
            }
            case BACK_TRUE_COLOR: {
                String s = Integer.toString(0, textNodeStyle.getVariant());
                while (s.length() < 8) {
                    s = "0" + s;
                }
                return createStyled("##:bx" + textNodeStyle.getVariant() + ":", "##", other, textNodeStyle, true);
            }
            case UNDERLINED: {
                return createStyled("##:_:", "##", other, textNodeStyle, true);
            }
            case ITALIC: {
                return createStyled("##:/:", "##", other, textNodeStyle, true);
            }
            case STRIKED: {
                return createStyled("##:-:", "##", other, textNodeStyle, true);
            }
            case REVERSED: {
                return createStyled("##:!:", "##", other, textNodeStyle, true);
            }
            case BOLD: {
                return createStyled("##:+:", "##", other, textNodeStyle, true);
            }
            case BLINK: {
                return createStyled("##:%:", "##", other, textNodeStyle, true);
            }
            case PRIMARY: {
                return createStyled("##:p:", "##", other, textNodeStyle, true);
            }
            case SECONDARY: {
                return createStyled("##:s:", "##", other, textNodeStyle, true);
            }
            default: {
                return createStyled("##:"
                        + textNodeStyle.getType().toString().toUpperCase()
                        + ":", "##", other, textNodeStyle, true);
            }
        }
    }

    public NutsCodeFormat resolveBlocTextFormatter(String kind) {
        if (kind == null) {
            kind = "";
        }
        DefaultNutsSupportLevelContext<String> ctx = new DefaultNutsSupportLevelContext<String>(ws, kind);
        int bestCode = NutsComponent.NO_SUPPORT;
        NutsCodeFormat format = null;
        for (NutsCodeFormat codeFormat : ws.formats().text().getCodeFormats()) {
            int s = codeFormat.getSupportLevel(ctx);
            if (s > bestCode) {
                format = codeFormat;
                bestCode = s;
            }
        }
        if (format != null) {
            return format;
        }
        if (kind.length() > 0) {
            switch (kind.toLowerCase()) {
                case "sh": {
                    if (shellBlocTextFormatter == null) {
                        shellBlocTextFormatter = new ShellBlocTextFormatter(ws);
                    }
                    return shellBlocTextFormatter;
                }

                case "json": {
                    if (jsonBlocTextFormatter == null) {
                        jsonBlocTextFormatter = new JsonBlocTextFormatter(ws);
                    }
                    return jsonBlocTextFormatter;
                }

                case "xml": {
                    if (xmlBlocTextFormatter == null) {
                        xmlBlocTextFormatter = new XmlBlocTextFormatter(ws);
                    }
                    return xmlBlocTextFormatter;
                }

                case "java": {
                    if (javaBlocTextFormatter == null) {
                        javaBlocTextFormatter = new JavaBlocTextFormatter(ws);
                    }
                    return javaBlocTextFormatter;
                }
                case "hadra": {
                    if (hadraBlocTextFormatter == null) {
                        hadraBlocTextFormatter = new HadraBlocTextFormatter(ws);
                    }
                    return hadraBlocTextFormatter;
                }

                //Default styles...
                default: {
                    try {
                        String cc = kind.toUpperCase();
                        int x = cc.length();
                        while (Character.isDigit(cc.charAt(x - 1))) {
                            x--;
                        }
                        if (x < cc.length()) {
                            NutsTextNodeStyle found = NutsTextNodeStyle.of(NutsTextNodeStyleType.valueOf(expandAlias(kind.toUpperCase().substring(0, x))),
                                    Integer.parseInt(kind.substring(x))
                            );
                            return new CustomStyleBlocTextFormatter(found, ws);
                        } else {
                            NutsTextNodeStyle found = NutsTextNodeStyle.of(NutsTextNodeStyleType.valueOf(expandAlias(kind.toUpperCase())));
                            return new CustomStyleBlocTextFormatter(found, ws);
                        }
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            }
        }
        if (plainBlocTextFormatter == null) {
            plainBlocTextFormatter = new PlainBlocTextFormatter(ws);
        }
        return plainBlocTextFormatter;
    }

    private String expandAlias(String ss) {
        switch (ss.toUpperCase()) {
            case "BOOL": {
                ss = "BOOLEAN";
                break;
            }
            case "KW": {
                ss = "KEYWORD";
                break;
            }
        }
        return ss;
    }

    public NutsTextNode createStyled(NutsTextNode child, NutsTextNodeStyle textStyle, boolean completed) {
        String svar = textStyle.getVariant() == 0 ? "" : ("" + textStyle.getVariant());
        switch (textStyle.getType()) {
            case PRIMARY: {
                return createStyled("##:p" + svar + ":", "##", child, textStyle, completed);
            }
            case SECONDARY: {
                return createStyled("##:s" + svar + ":", "##", child, textStyle, completed);
            }
            case UNDERLINED: {
                return createStyled("##:_:", "##", child, textStyle, completed);
            }
            case BLINK: {
                return createStyled("##:%:", "##", child, textStyle, completed);
            }
            case ITALIC: {
                return createStyled("##:/:", "##", child, textStyle, completed);
            }
            case BOLD: {
                return createStyled("##:+:", "##", child, textStyle, completed);
            }
            case REVERSED: {
                return createStyled("##:!:", "##", child, textStyle, completed);
            }
            case FORE_COLOR: {
                return createStyled("##:f" + svar + ":", "##", child, textStyle, completed);
            }
            case BACK_COLOR: {
                return createStyled("##:b" + svar + ":", "##", child, textStyle, completed);
            }
            default: {
                return createStyled("##:" + textStyle.getType().toString().toUpperCase() + ":", "##", child, textStyle, completed);
            }
        }
    }

    public NutsTextNode createStyled(String start, String end, NutsTextNode child, NutsTextNodeStyle textStyle, boolean completed) {
        if (textStyle == null) {
            throw new NutsIllegalArgumentException(ws, "missing textStyle");
        }
        return new DefaultNutsTextNodeStyled(ws, start, end, child, completed, textStyle);
    }

    public NutsTextNode createCode(String start, String kind, String separator, String end, String text) {
        return new DefaultNutsTextNodeCode(ws, start, kind, separator, end, text);
    }

    public NutsTextNode createCommand(String start, String command, String separator, String end, String text) {
        return new DefaultNutsTextNodeCommand(ws, start, command, separator, end, text);
    }

    public NutsTextNode createLink(String start, String command, String separator, String end, String value) {
        return new DefaultNutsTextNodeLink(ws, start, command, separator, end, value);
    }

    public NutsTextNode createAnchor(String start, String command, String separator, String end, String value) {
        return new DefaultNutsTextNodeAnchor(ws, start, command, separator, end, value);
    }

    public NutsTextNode createTitle(String start, int level, NutsTextNode child, boolean complete) {
        return new DefaultNutsTextNodeTitle(ws, start, level, child);
    }

    @Override
    public NutsTitleNumberSequence createTitleNumberSequence() {
        return new DefaultNutsTitleNumberSequence("");
    }

    @Override
    public NutsTitleNumberSequence createTitleNumberSequence(String pattern) {
        return new DefaultNutsTitleNumberSequence((pattern == null || pattern.isEmpty()) ? "1.1.1.a.1" : pattern);
    }
}
