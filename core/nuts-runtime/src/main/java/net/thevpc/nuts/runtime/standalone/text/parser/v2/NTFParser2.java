package net.thevpc.nuts.runtime.standalone.text.parser.v2;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.AbstractNutsTextNodeParser;
import net.thevpc.nuts.runtime.standalone.text.DefaultNutsTexts;
import net.thevpc.nuts.runtime.standalone.util.collections.CharQueue;
import net.thevpc.nuts.runtime.standalone.util.collections.NutsMatchType;
import net.thevpc.nuts.runtime.standalone.util.collections.NutsStringMatchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class NTFParser2 extends AbstractNutsTextNodeParser {
    private StringBuffer buffer = new StringBuffer();
    private static String[] SHARPS = {
            "",
            "#",
            "##",
            "###",
            "####",
            "#####",
            "######",
            "#######",
            "########",
            "#########",
            "##########"
    };

    enum StepEnum {
        CODE,
        TEXT,
        TITLE,
        SIMPLE_STYLE,
        COMPOSITE_STYLE,
    }

    private final CharQueue q = new CharQueue();
    private NutsTexts txt;
    private boolean wasNewLine = true;
    private Stack<Embedded> stackedStyles = new Stack<>();

    private static class Embedded {
        NutsTextStyles style;
        int level;
        StepEnum mode;
        List<NutsText> children = new ArrayList<>();

        public Embedded(StepEnum mode, NutsTextStyles style, int level) {
            this.style = style;
            this.mode = mode;
            this.level = level;
        }
    }

    public NTFParser2(NutsSession session) {
        super(session);
        this.txt = new DefaultNutsTexts(session);//NutsTexts.of(
    }

    public void offer(char c) {
        synchronized (q) {
            q.write(c);
        }
    }

    public void eof(boolean c) {
        synchronized (q) {
            q.eof(c);
        }
    }

    public void offer(String c) {
        synchronized (q) {
            q.write(c);
        }
    }

    public void offer(char[] c) {
        synchronized (q) {
            q.write(c);
        }
    }

    public void offer(char[] c, int offset, int len) {
        synchronized (q) {
            q.write(c, offset, len);
        }
    }

    private void resetBuffer() {
        buffer.delete(0, buffer.length());
    }

    public NutsText read() {
        return read(false);
    }

    public NutsText readFully() {
        return read(true);
    }

    public NutsText read(boolean fully) {
        synchronized (q) {
            while (q.hasNext()) {
                Embedded embedded = stackedStyles.isEmpty() ? null : stackedStyles.peek();
                StepEnum mode = embedded == null ? StepEnum.TEXT : embedded.mode;
                switch (q.peek()) {
                    case '}': {
                        wasNewLine = false;
                        if (mode != StepEnum.COMPOSITE_STYLE) {
                            buffer.append(q.read());
                        } else {
                            NutsStringMatchResult n1 = q.peekString("}##", fully);
                            if (n1.mode() == NutsMatchType.FULL_MATCH) {
                                q.read(n1.count());
                                return pushUpCompositeStyle();
                            } else if (n1.mode() == NutsMatchType.NO_MATCH) {
                                buffer.append(q.read());
                            } else {
                                return null;
                            }
                        }
                        break;
                    }
                    case '#': {
                        if (mode == StepEnum.CODE) {
                            buffer.append(q.read());
                        } else {
                            int simpleLvl = (embedded == null ? 0 : embedded.level) + 1;
                            NutsRef<NutsText> ret = NutsRef.ofNull();
                            q.doWithPattern(
                                    new CharQueue.MultiPattern()
                                            .setFully(fully)
                                            .onFullMatch("##:(?<n>[^:]+)[: ]", m -> {
                                                String s = m.get("n");
                                                wasNewLine = false;
                                                q.read(m.count());
                                                pushSimpleStyle(s);
                                            })
                                            .onFullMatch("##\\{(?<n>[^:]+)[: ]", m -> {
                                                wasNewLine = false;
                                                String s = m.get("n");
                                                NutsText p = pushUp(consumeBuffer());
                                                pushCompositeStyle(s);
                                                q.read(m.count());
                                                if (p != null) {
                                                    ret.set(p);
                                                }
                                            })
                                            .onFullMatch("#+[)]",
                                                    wasNewLine && (mode == StepEnum.TEXT || mode == StepEnum.COMPOSITE_STYLE)
                                                    , m -> {
                                                        wasNewLine = false;
                                                        q.read(m.count());
                                                        NutsText p = pushUp(consumeBuffer());
                                                        pushTitle(m.get().length() - 1);
                                                        if (p != null) {
                                                            ret.set(p);
                                                        }
                                                    })
                                            .onFullMatch(SHARPS[simpleLvl] + "($|[^#])",
                                                    mode == StepEnum.SIMPLE_STYLE,
                                                    m -> {
                                                        q.read(SHARPS[simpleLvl].length()); //ignore extra
                                                        ret.set(pushUpSimpleStyle());
                                                    }
                                            )
                                            .onFullMatch("##{" + (simpleLvl + 1) + ",}",
                                                    mode == StepEnum.SIMPLE_STYLE,
                                                    m -> {
                                                        q.read(m.count());
                                                        wasNewLine = false;
                                                        NutsText p = pushUp(consumeBuffer());
                                                        pushSimpleStyle(m.count() - 1);
                                                        if (p != null) {
                                                            ret.set(p);
                                                        }
                                                    }
                                            )
                                            .onFullMatch("##+",
                                                    mode != StepEnum.SIMPLE_STYLE,
                                                    m -> {
                                                        int count = m.count();
                                                        q.read(count);
                                                        wasNewLine = false;
                                                        NutsText p = pushUp(consumeBuffer());
                                                        pushSimpleStyle(count - 1);
                                                        if (p != null) {
                                                            ret.set(p);
                                                        }
                                                    }
                                            )
                                            .onNoMatch(() -> buffer.append(q.read()))
                                            .onMatch(c -> ret.set(null))
                                            .onPartialMatch(c -> ret.set(null))
                            );
                            if (ret.isSet()) {
                                return ret.get();
                            }
                            break;
                        }
                        break;
                    }
                    case '\\': {
                        if (mode == StepEnum.CODE) {
                            String s = q.peek(4);
                            if (s.length() == 4) {
                                if (s.equals("\\```")) {
                                    q.read();
                                    buffer.append(q.read());
                                } else {
                                    buffer.append(q.read(2));
                                }
                            } else {
                                if (q.isEOF()) {
                                    buffer.append(q.read());
                                } else {
                                    return null;
                                }
                            }
                        } else {
                            String s = q.peek(2);
                            if (s.length() == 2) {
                                switch (s.charAt(1)) {
                                    case '\\': {
                                        q.read(2);
                                        buffer.append("\\");
                                        break;
                                    }
                                    case '#': {
                                        q.read(2);
                                        buffer.append("#");
                                        break;
                                    }
                                    case '\u001E': {
                                        //just ignore
                                        q.read(2);
                                        break;
                                    }
                                    default: {
                                        buffer.append(q.read(2));
                                    }
                                }
                            } else {
                                if (q.isEOF()) {
                                    buffer.append(q.read());
                                } else {
                                    return null;
                                }
                            }
                        }
                        break;
                    }
                    case '\u001E': {
                        if (mode == StepEnum.CODE) {
                            buffer.append(q.read());
                        } else {
                            // just ignore
                            q.read();
                        }
                        break;
                    }
                    case '\r':
                    case '\n': {
                        if (mode == StepEnum.CODE) {
                            buffer.append(q.read());
                        } else if (mode == StepEnum.TITLE) {
                            String s = q.readNewLine(true);
                            if (s != null) {
                                return pushUpTitle();
                            }
                            return null;
                        } else {
                            String s = q.readNewLine(fully);
                            if (s != null) {
                                wasNewLine = true;
                                buffer.append(s);
                            } else {
                                return null;
                            }
                        }
                        break;
                    }
                    case '`': {
                        NutsStringMatchResult n = q.peekPattern("```($|[^`])", fully);
                        if (mode == StepEnum.CODE) {
                            switch (n.mode()) {
                                case FULL_MATCH: {
                                    q.read(3); // ignore extra // n.count()
                                    return pushUpCode();
                                }
                                case NO_MATCH: {
                                    buffer.append(q.read());
                                    break;
                                }
                                default: {
                                    return null;
                                }
                            }
                        } else {
                            switch (n.mode()) {
                                case FULL_MATCH: {
                                    q.read(3); // ignore extra // n.count()
                                    NutsText p = pushUp(consumeBuffer());
                                    pushCode();
                                    if (p != null) {
                                        return p;
                                    }
                                    break;
                                }
                                case NO_MATCH: {
                                    buffer.append(q.read(n.count()));
                                    break;
                                }
                                default: {
                                    return null;
                                }
                            }
                        }
                        break;
                    }
                    default: {
                        buffer.append(q.read());
                    }
                }
            }
            if (fully) {
                //push up remaining...
                while (!stackedStyles.isEmpty()) {
                    Embedded e = stackedStyles.peek();
                    switch (e.mode) {
                        case SIMPLE_STYLE: {
                            NutsText p = pushUpSimpleStyle();
                            if (p != null) {
                                return p;
                            }
                            break;
                        }
                        case COMPOSITE_STYLE: {
                            NutsText p = pushUpCompositeStyle();
                            if (p != null) {
                                return p;
                            }
                            break;
                        }
                        case CODE: {
                            NutsText p = pushUpCode();
                            if (p != null) {
                                return p;
                            }
                            break;
                        }
                        case TITLE: {
                            NutsText p = pushUpTitle();
                            if (p != null) {
                                return p;
                            }
                            break;
                        }
                    }
                }
            }
            Embedded embedded = stackedStyles.isEmpty() ? null : stackedStyles.peek();
            StepEnum mode = embedded == null ? StepEnum.TEXT : embedded.mode;
            if (mode == StepEnum.TEXT) {
                return pushUp(consumeBuffer());
            }
            return null;
        }
    }

    private NutsText pushUpCode() {
        Embedded embedded = stackedStyles.peek();
        String b = consumeBuffer();
        stackedStyles.pop();
        return pushUp(txt.ofCodeOrCommand(b));
    }

    private NutsText pushUpTitle() {
        Embedded embedded = stackedStyles.peek();
        pushUp(consumeBuffer());
        stackedStyles.pop();
        return pushUp(txt.ofTitle(txt.ofList(embedded.children), embedded.level));
    }

    private NutsText pushUpCompositeStyle() {
        Embedded embedded = stackedStyles.peek();
        pushUp(consumeBuffer());
        stackedStyles.pop();
        return pushUp(txt.ofStyled(txt.ofList(embedded.children), embedded.style));
    }

    private NutsText pushUpSimpleStyle() {
        Embedded embedded = stackedStyles.peek();
        wasNewLine = false;
        pushUp(consumeBuffer());
        stackedStyles.pop();
        return pushUp(txt.ofStyled(txt.ofList(embedded.children), embedded.style));
    }

    private void pushCompositeStyle(String style) {
        stackedStyles.push(new Embedded(StepEnum.COMPOSITE_STYLE, createStyle(style), 0));
    }

    private void pushCode() {
        stackedStyles.push(new Embedded(StepEnum.CODE, null, 0));
    }

    private void pushSimpleStyle(int level) {
        stackedStyles.push(new Embedded(StepEnum.SIMPLE_STYLE, createStyle("p" + level), level));
    }

    private void pushSimpleStyle(String s) {
        stackedStyles.push(new Embedded(StepEnum.SIMPLE_STYLE, createStyle(s), 1));
    }

    private void pushTitle(int level) {
        stackedStyles.push(new Embedded(StepEnum.TITLE, null, level));
    }

    private NutsTextStyles createStyle(String n1) {
        return NutsTextStyles.of(NutsTextStyle.parse(n1).get(txt.getSession()));
    }

    private NutsText pushUp(NutsText t) {
        if (t == null) {
            return null;
        }
        if (!stackedStyles.isEmpty()) {
            stackedStyles.peek().children.add(t);
            return null;
        }
        return t;
    }

    private NutsText pushUp(String t) {
        if (t == null) {
            return null;
        }
        if (t.length() > 0) {
            return pushUp(txt.ofPlain(t));
        }
        return null;
    }

    private String consumeBuffer() {
        String s = buffer.toString();
        resetBuffer();
        return s;
    }

    @Override
    public void reset() {

    }

    @Override
    public long parseRemaining(NutsTextVisitor visitor) {
        long b = 0;
        while (true) {
            NutsText n = readFully();
            if (n != null) {
                b++;
                visitor.visit(n);
            } else {
                break;
            }
        }
        return b;
    }

    @Override
    public long parseIncremental(char[] buf, int off, int len, NutsTextVisitor visitor) {
        offer(buf, off, len);
        long b = 0;
        while (true) {
            NutsText n = read();
            if (n != null) {
                b++;
                visitor.visit(n);
            } else {
                break;
            }
        }
        return b;
    }

    @Override
    public boolean isIncomplete() {
        return q.length() > 0;
    }
}
