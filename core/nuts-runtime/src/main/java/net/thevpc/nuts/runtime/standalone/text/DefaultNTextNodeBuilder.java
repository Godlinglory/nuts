package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NStream;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class DefaultNTextNodeBuilder implements NTextBuilder {

    private final List<NText> children = new ArrayList<>();
    private final NSession session;
    private final NTexts txt;
    private NTextStyleGenerator styleGenerator;
    private boolean flattened = true;

    public DefaultNTextNodeBuilder(NSession session) {
        this.session = session;
        txt = NTexts.of(session);
    }

    @Override
    public NTextStyleGenerator getStyleGenerator() {
        if (styleGenerator == null) {
            styleGenerator = new DefaultNTextStyleGenerator();
        }
        return styleGenerator;
    }

    @Override
    public DefaultNTextNodeBuilder setStyleGenerator(NTextStyleGenerator styleGenerator) {
        this.styleGenerator = styleGenerator;
        return this;
    }

    @Override
    public NTextBuilder appendCommand(NTerminalCommand command) {
        append(txt.ofCommand(command));
        return this;
    }

    @Override
    public NTextBuilder appendCode(String lang, String text) {
        append(txt.ofCode(lang, text));
        return this;
    }

    //
//    @Override
//    public NutsTextBuilder append(String text, NutsTextStyle... styles) {
//        return append(text1.text().forPlain(text), styles);
//    }
    @Override
    public NTextBuilder appendHash(Object text) {
        return appendHash(text, text);
    }

    @Override
    public NTextBuilder appendRandom(Object text) {
        if (text == null) {
            return this;
        }
        return append(text, getStyleGenerator().random());
    }

    @Override
    public NTextBuilder appendHash(Object text, Object hash) {
        if (text == null) {
            return this;
        }
        if (hash == null) {
            hash = text;
        }
        return append(text, getStyleGenerator().hash(hash));
    }

    @Override
    public NTextBuilder append(Object text, NTextStyle style) {
        return append(text, NTextStyles.of(style));
    }

    @Override
    public NTextBuilder append(Object text, NTextStyles styles) {
        if (text != null) {
            if (styles.size() == 0) {
                append(NTexts.of(session).ofText(text));
            } else {
                append(txt.ofStyled(NTexts.of(session).ofText(text), styles));
            }
        }
        return this;
    }

    @Override
    public NTextBuilder append(Object node) {
        if (node != null) {
            return append(NTexts.of(session).ofText(node));
        }
        return this;
    }

    @Override
    public NTextBuilder append(NText node) {
        if (node != null) {
            children.add(node);
            flattened = false;
        }
        return this;
    }

    @Override
    public NTextBuilder appendJoined(Object separator, Collection<?> others) {
        if (others != null) {
            boolean first = true;
            for (Object other : others) {
                if (other != null) {
                    if (first) {
                        first = false;
                    } else {
                        if (separator != null) {
                            append(separator);
                        }
                    }
                    append(other);
                }
            }
        }
        return this;
    }
    //    @Override
//    public NutsTextBuilder append(NutsString str) {
//        if (str != null) {
//            NutsText n = ws.text().parser().parse(new StringReader(str.toString()));
//            if (n != null) {
//                append(n);
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public NutsTextBuilder append(NutsFormattable str) {
//        if (str != null) {
//            append(ws.text().toText(str));
//        }
//        return this;
//    }

    @Override
    public NTextBuilder appendAll(Collection<?> others) {
        if (others != null) {
            for (Object node : others) {
                if (node != null) {
                    append(node);
                }
            }
        }
        return this;
    }

    @Override
    public NText build() {
        if (children.size() == 0) {
            return txt.ofPlain("");
        }
        if (children.size() == 1) {
            return children.get(0);
        }
        return txt.ofList(children).simplify();
    }

    @Override
    public List<NText> getChildren() {
        return new ArrayList<>(children);
    }

    @Override
    public NText subChildren(int from, int to) {
        if (from < 0) {
            from = 0;
        }
        if (to >= size()) {
            to = size() - 1;
        }
        if (to <= from) {
            return NTexts.of(session).ofPlain("");
        }
        return NTexts.of(session).ofBuilder().appendAll(children.subList(from, to)).build();
    }

    public NText substring(int from, int to) {
        if (to <= from) {
            return NTexts.of(session).ofPlain("");
        }
        int firstIndex = ensureCut(from);
        if (firstIndex < 0) {
            return NTexts.of(session).ofPlain("");
        }
        int secondIndex = ensureCut(to);
        if (secondIndex < 0) {
            //the cut is till the end
            return NTexts.of(session).ofBuilder().appendAll(children.subList(firstIndex, children.size())).build();
        }
        return NTexts.of(session).ofBuilder().appendAll(children.subList(firstIndex, secondIndex)).build();
    }

    @Override
    public NTextBuilder insert(int at, NText... newTexts) {
        return replaceChildren(at, at + 1, newTexts);
    }

    @Override
    public NTextBuilder replace(int from, int to, NText... newTexts) {
        if (to <= from) {
            return this;
        }
        int firstIndex = ensureCut(from);
        if (firstIndex < 0) {
            return this;
        }
        int secondIndex = ensureCut(to);
        if (secondIndex < 0) {
            //the cut is till the end
            replaceChildren(firstIndex, children.size(), newTexts);
        }
        replaceChildren(firstIndex, secondIndex, newTexts);
        return this;
    }

    @Override
    public NTextBuilder replaceChildren(int from, int to, NText... newTexts) {
        if (newTexts == null) {
            newTexts = new NText[0];
        } else {
            newTexts = Arrays.stream(newTexts).filter(x -> x != null && !x.isEmpty()).toArray(NText[]::new);
        }
        if (from < to) {
            children.subList(from, to).clear();
            if (newTexts.length > 0) {
                children.addAll(from, Arrays.asList(newTexts));
            }
        }
        return this;
    }

    @Override
    public int size() {
        return children.size();
    }

    public NText get(int index) {
        return children.get(index);
    }

    @Override
    public Iterable<NText> items() {
        return children;
    }

    @Override
    public NTextBuilder flatten() {
        if (!flattened) {
            NText build = build();
            NText a = txt.transform(build, new NTextTransformConfig().setFlatten(true));
            this.children.clear();
            if (a != null) {
                this.children.addAll((a instanceof NTextList) ? ((NTextList) a).getChildren() : Collections.singletonList(a));
            }
            flattened = true;
        }
        return this;
    }

    @Override
    public NTextBuilder removeAt(int index) {
        children.remove(index);
        return this;
    }

    @Override
    public NStream<NTextBuilder> lines() {
        DefaultNTextNodeBuilder z = (DefaultNTextNodeBuilder) copy().flatten();
        return NStream.of(
                new Iterator<NTextBuilder>() {
                    NTextBuilder n;

                    @Override
                    public boolean hasNext() {
                        n = z.readLine();
                        return n != null;
                    }

                    @Override
                    public NTextBuilder next() {
                        return n;
                    }
                }, session
        );
    }

    @Override
    public NTextBuilder readLine() {
        if (this.size() == 0) {
            return null;
        }
        List<NText> r = new ArrayList<>();
        while (this.size() > 0) {
            NText t = this.get(0);
            this.removeAt(0);
            if (isNewLine(t)) {
                break;
            }
            r.add(t);
        }
        return NTexts.of(session).ofBuilder().appendAll(r);
    }

    private boolean isNewLine(NText t) {
        if (t.getType() == NTextType.PLAIN) {
            String txt = ((NTextPlain) t).getText();
            return (txt.equals("\n") || txt.equals("\r\n"));
        }
        return false;
    }


    public NTextBuilder copy() {
        DefaultNTextNodeBuilder c = new DefaultNTextNodeBuilder(session);
        c.appendAll(children);
        c.flattened = flattened;
        return c;
    }

    public int ensureCut(int at) {
//        List<NutsText> newValues=new ArrayList<>();
        if (at <= 0) {
            return 0;
        }
        NTexts text = NTexts.of(session);
        int charPos = 0;
        int index = 0;
        while (index < children.size()) {
            NText c = children.get(index);
            int start = charPos;
            int len = c.textLength();
            int end = start + len;
            if (at < start) {
                //continue
            } else if (at == start) {
                return index;
            } else if (at == end) {
                if (index + 1 < children.size()) {
                    return index + 1;
                }
                return -1;
            } else if (at > start && at < end) {
                List<NText> rv = c.builder().flatten().getChildren();
                List<NText> rv2 = new ArrayList<>(rv.size() + 1);
                int toReturn = -1;
                for (int i = 0; i < rv.size(); i++) {
                    NText child = rv.get(i);
                    start = charPos;
                    len = child.textLength();
                    end = start + len;
                    if (at < start) {
                        rv2.add(child);
                    } else if (at == start) {
                        rv2.add(child);
                        toReturn = i + index;
                    } else if (at >= end) {
                        rv2.add(child);
                    } else {
                        if (child.getType() == NTextType.PLAIN) {
                            NTextPlain p = (NTextPlain) child;
                            String tp = p.getText();
                            String a = tp.substring(0, at - start);
                            String b = tp.substring(at - start);
                            rv2.add(text.ofPlain(a));
                            rv2.add(text.ofPlain(b));
                            toReturn = index + i + 1;
                        } else if (child.getType() == NTextType.STYLED) {
                            NTextStyled p = (NTextStyled) child;
                            String tp = ((NTextPlain) p.getChild()).getText();
                            String a = tp.substring(0, at - start);
                            String b = tp.substring(at - start);
                            rv2.add(text.ofStyled(a, p.getStyles()));
                            rv2.add(text.ofStyled(b, p.getStyles()));
                            toReturn = index + i + 1;
                        }
                    }
                    charPos = end;
                }
                replaceChildren(index, index + 1, rv2.toArray(new NText[0]));
                return toReturn;
            }
            charPos = end;
            index++;
        }
        return -1;
    }

    @Override
    public NString immutable() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NTextNodeWriterStringer ss = new NTextNodeWriterStringer(out, session);
        ss.writeNode(build());
        return new NImmutableString(session, out.toString());
    }

    @Override
    public String filteredText() {
        StringBuilder sb = new StringBuilder();
        for (NText child : children) {
            sb.append(child.filteredText());
        }
        return sb.toString();
    }

    @Override
    public int textLength() {
        int count = 0;
        for (NText child : children) {
            count += child.textLength();
        }
        return count;
//        return immutable().textLength();
    }

    @Override
    public NText toText() {
        return build();
    }

    @Override
    public boolean isEmpty() {
        return immutable().isEmpty();
    }

    @Override
    public NTextBuilder builder() {
        return NTexts.of(session).ofBuilder().append(this);
    }

    @Override
    public String toString() {
        return immutable().toString();
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(filteredText());
    }

}
