/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.format.xml;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.util.xml.XmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author thevpc
 */
public class NElementFactoryXmlElement implements NElementMapper<Node> {

    public static <V> V runWithDoc(NElementFactoryContext context, Supplier<V> impl, Document doc) {
        Stack<Document> docs = (Stack<Document>) context.getProperties().get(Document.class.getName());
        if (docs == null) {
            docs = new Stack<>();
            context.getProperties().put(Document.class.getName(), docs);
            try {
                docs.push(doc != null ? doc : XmlUtils.createDocument(context.getSession()));
                return impl.get();
            } finally {
                docs.pop();
            }
        } else {
            if (docs.isEmpty() || doc != null) {
                try {
                    docs.push(doc != null ? doc : XmlUtils.createDocument(context.getSession()));
                    return impl.get();
                } finally {
                    docs.pop();
                }
            }
        }
        return impl.get();
    }

    @Override
    public Node createObject(NElement elem, Type typeOfResult, NElementFactoryContext context) {
        return runWithDoc(context, () -> createObject0(elem, typeOfResult, context), null);
    }

    protected Node createObject0(NElement elem, Type typeOfResult, NElementFactoryContext context) {
        NSession session = context.getSession();
        if (context.getProperties().get(Document.class.getName()) == null || !(context.getProperties().get(Document.class.getName()) instanceof Stack)) {
            Stack<Document> docs = new Stack<>();
            context.getProperties().put(Document.class.getName(), docs);
            try {
                docs.push(XmlUtils.createDocument(session));
                return createObject(elem, typeOfResult, context);
            } finally {
                docs.pop();
            }
        } else {
            Stack<Document> docs = (Stack<Document>) context.getProperties().get(Document.class.getName());
            if (docs.isEmpty()) {
                try {
                    docs.push(XmlUtils.createDocument(session));
                    return createObject(elem, typeOfResult, context);
                } finally {
                    docs.pop();
                }
            } else {
                //continue;
            }
        }
        Document doc = ((Stack<Document>) context.getProperties().get(Document.class.getName())).peek();
        switch (elem.type()) {
            case NULL: {
                Element e = doc.createElement("null");
                return e;
            }
            case STRING: {
                Element e = doc.createElement("string");
                final String s = elem.asString().get(session);
                if (isComplexString(s)) {
                    e.setTextContent(s);
                } else {
                    e.setAttribute("value", s);
                }
                return e;
            }
//            case NUTS_STRING: {
//                Element e = doc.createElement("nuts-string");
//                final String s = elem.asPrimitive().getString();
//                if (isComplexString(s)) {
//                    e.setTextContent(s);
//                } else {
//                    e.setAttribute("value", s);
//                }
//                return e;
//            }
            case BOOLEAN: {
                return doc.createElement(String.valueOf(elem.asBoolean()));
            }
            case BYTE: {
                Element e = doc.createElement("byte");
                e.setAttribute("value", String.valueOf(elem.asByte()));
                return e;
            }
            case SHORT: {
                Element e = doc.createElement("short");
                e.setAttribute("value", String.valueOf(elem.asShort()));
                return e;
            }
            case INTEGER: {
                Element e = doc.createElement("int");
                e.setAttribute("value", String.valueOf(elem.asInt()));
                return e;
            }
            case LONG: {
                Element e = doc.createElement("long");
                e.setAttribute("value", String.valueOf(elem.asLong()));
                return e;
            }
            case FLOAT: {
                Element e = doc.createElement("float");
                e.setAttribute("value", String.valueOf(elem.asFloat()));
                return e;
            }
            case DOUBLE: {
                Element e = doc.createElement("double");
                e.setAttribute("value", String.valueOf(elem.asDouble()));
                return e;
            }
            case INSTANT: {
                Element e = doc.createElement("instant");
                e.setAttribute("value", elem.asInstant().toString());
                return e;
            }
            case ARRAY: {
                Element e = doc.createElement("array");
                int count = 0;
                for (NElement attribute : elem.asArray().get(session).items()) {
                    Node c = createObject(attribute, Element.class, context);
                    if (c != null) {
                        e.appendChild(c);
                        count++;
                    }
                }
                return e;
            }
            case OBJECT: {
                Element obj = doc.createElement("object");
                for (NElementEntry ne : elem.asObject().get(session).entries()) {
                    final NElementType kt = ne.getKey().type();
                    boolean complexKey = kt == NElementType.ARRAY || kt == NElementType.OBJECT
                            || (kt == NElementType.STRING && isComplexString(ne.getKey().asString().get(session)))
                            ;
                    if (complexKey) {
                        Element entry = doc.createElement("entry");
                        Element ek = (Element) createObject(ne.getKey(), NElement.class, context);
                        ek.setAttribute("entry-key", null);
                        entry.appendChild(ek);
                        Element ev = (Element) createObject(ne.getValue(), NElement.class, context);
                        ev.setAttribute("entry-value", null);
                        entry.appendChild(ev);
                        obj.appendChild(entry);
                    } else {
                        String tagName
                                = ne.getKey().type() == NElementType.BOOLEAN ? ne.getKey().asString().get(session)
                                : ne.getKey().type().id();
                        Element entryElem = (Element) doc.createElement(tagName);
                        if (ne.getKey().type() != NElementType.BOOLEAN && ne.getKey().type() != NElementType.NULL) {
                            entryElem.setAttribute("key", ne.getKey().asString().get(session));
                        }
                        switch (ne.getValue().type()) {
                            case ARRAY:
                            case OBJECT: {
                                Element ev = (Element) createObject(ne.getValue(), NElement.class, context);
                                ev.setAttribute("entry-value", null);
                                entryElem.appendChild(ev);
                                obj.appendChild(entryElem);
                                break;
                            }
                            case NULL: {
                                entryElem.setAttribute("value-type", ne.getValue().type().id());
                                obj.appendChild(entryElem);
                                break;
                            }
                            case STRING: {
                                entryElem.setAttribute("value", ne.getValue().asString().get(session));
                                obj.appendChild(entryElem);
                                break;
                            }
                            default: {
                                entryElem.setAttribute("value", ne.getValue().asString().get(session));
                                entryElem.setAttribute("value-type", ne.getValue().type().id());
                                obj.appendChild(entryElem);
                                break;
                            }
                        }
                    }
                }
                return obj;
            }
            default: {
                throw new IllegalArgumentException("Unsupported");
            }
        }
    }

    public NElement createElement(String type, String value, NElementFactoryContext context) {
        NElements f = NElements.of(context.getSession());
        switch (type) {
            case "null": {
                return f.ofNull();
            }
            case "number": {
                return context.objectToElement(value, Number.class);
            }
            case "boolean": {
                return context.objectToElement(value, Boolean.class);
            }
            case "true": {
                return f.ofTrue();
            }
            case "false": {
                return f.ofTrue();
            }
            case "byte": {
                return context.objectToElement(value, Byte.class);
            }
            case "short": {
                return context.objectToElement(value, Short.class);
            }
            case "int": {
                return context.objectToElement(value, Integer.class);
            }
            case "long": {
                return context.objectToElement(value, Long.class);
            }
            case "float": {
                return context.objectToElement(value, Float.class);
            }
            case "double": {
                return context.objectToElement(value, Double.class);
            }
            case "char": {
                return context.objectToElement(value, Character.class);
            }
            case "string": {
                return context.objectToElement(value, String.class);
            }
            case "nuts-string": {
                return context.objectToElement(value, NString.class);
            }
            case "instant": {
                return context.objectToElement(value, Instant.class);
            }
            case "date": {
                return context.objectToElement(value, Date.class);
            }
            case "file": {
                return context.objectToElement(value, File.class);
            }
            case "path": {
                return context.objectToElement(value, Path.class);
            }
            default: {
                throw new IllegalArgumentException("unsupported");
            }
        }
    }

    public boolean isSimpleObject(NObjectElement obj) {
        for (NElementEntry attribute : obj.entries()) {
            final NElementType tt = attribute.getKey().type();
            if (tt == NElementType.OBJECT || tt == NElementType.ARRAY) {
                return false;
            }
        }
        return true;
    }

    private boolean isComplexString(String string) {
        return string.contains("\n") || string.length() > 120;
    }

    private static class NodeInfo {

        String type;
        String name;
        String value;

        public NodeInfo(Element e) {
            String name0 = e.getAttribute("name");
            name = name0 == null ? e.getTagName() : name0;
            type = e.getAttribute("type") != null ? e.getAttribute("type") : name0 != null ? e.getTagName() : "string";
            value = e.getAttribute("value");
        }

    }

    private String resolveValue(Element e) {
        String value1 = e.getAttribute("value");
        String value2 = e.getTextContent();
        if (value2 == null) {
            return value1;
        }
        if (value1 == null) {
            return value2;
        }
        return value1 + value2;
    }

    @Override
    public Object destruct(Node node, Type typeOfSrc, NElementFactoryContext context) {
        if (node instanceof Attr) {
            Attr at = (Attr) node;

            return new AbstractMap.SimpleEntry<String, Object>(at.getName(),
                    context.destruct(at.getValue(), String.class)
            );
        }
        if (node instanceof CDATASection) {
            CDATASection d = (CDATASection) node;
            return d.getWholeText();
        }
        if (node instanceof Text) {
            Text d = (Text) node;
            return d.getWholeText();
        }
        Element element = (Element) node;
        NodeInfo ni = new NodeInfo(element);
        switch (ni.type) {
            case "object": {
                Set<Object> visited = new HashSet<>();
                boolean map = true;
                List<Map.Entry<Object, Object>> all = new ArrayList<>();
                NamedNodeMap attrs = element.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Attr n = (Attr) attrs.item(i);
                    Object k = n.getName();
                    Object v = n.getValue();
                    if (map && visited.contains(k)) {
                        map = false;
                    } else {
                        visited.add(k);
                    }
                    all.add(new AbstractMap.SimpleEntry<>(k, v));
                }
                if (map) {
                    LinkedHashMap<Object, Object> m = new LinkedHashMap<>();
                    for (Map.Entry<Object, Object> entry : all) {
                        m.put(entry.getKey(), entry.getValue());
                    }
                    return m;
                }
                return all;
            }
            case "array": {
                List<Object> obj = new ArrayList<Object>();
                NodeList attrs = element.getChildNodes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node n = (Node) attrs.item(i);
                    obj.add(createElement(n, typeOfSrc, context));
                }
                return obj;
            }
            case "boolean": {
                return context.destruct(resolveValue(element), Boolean.class);
            }
            case "byte": {
                return context.destruct(resolveValue(element), Byte.class);
            }
            case "short": {
                return context.destruct(resolveValue(element), Short.class);
            }
            case "int": {
                return context.destruct(resolveValue(element), Integer.class);
            }
            case "long": {
                return context.destruct(resolveValue(element), Long.class);
            }
            case "float": {
                return context.destruct(resolveValue(element), Float.class);
            }
            case "double": {
                return context.destruct(resolveValue(element), Double.class);
            }
            case "char": {
                return context.destruct(resolveValue(element), Character.class);
            }
            case "string": {
                return context.destruct(resolveValue(element), String.class);
            }
            case "instant": {
                return context.destruct(resolveValue(element), Instant.class);
            }
            case "date": {
                return context.destruct(resolveValue(element), Date.class);
            }
            case "file": {
                return context.destruct(resolveValue(element), File.class);
            }
            case "path": {
                return context.destruct(resolveValue(element), Path.class);
            }
            default: {
                throw new IllegalArgumentException("unsupported");
            }
        }
    }

    @Override
    public NElement createElement(Node node, Type typeOfSrc, NElementFactoryContext context) {
        NElements elements = NElements.of(context.getSession()).setSession(context.getSession());
        if (node instanceof Attr) {
            Attr at = (Attr) node;
            return elements.ofObject().set(at.getName(), context.objectToElement(at.getValue(), String.class)).build();
        }
        if (node instanceof CDATASection) {
            CDATASection d = (CDATASection) node;
            return elements.ofString(d.getWholeText());
        }
        if (node instanceof Text) {
            Text d = (Text) node;
            return elements.ofString(d.getWholeText());
        }
        Element element = (Element) node;
        NodeInfo ni = new NodeInfo(element);
        switch (ni.type) {
            case "object": {
                NObjectElementBuilder obj = elements.ofObject();
                NamedNodeMap attrs = element.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Attr n = (Attr) attrs.item(i);
                    obj.set(n.getName(), context.objectToElement(n.getValue(), null));
                }
                NodeList children = element.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node n = (Node) children.item(i);
                    if (n instanceof Element) {
                        Element e = (Element) n;
                        NodeInfo ni2 = new NodeInfo(e);
                        obj.set(ni2.name, createElement(ni2.type, ni2.value, context));
                    } else if (n instanceof Text) {
                        NElement e = createElement(n, Text.class, context);
                        obj.set("content", e);
                    }
                }
                return obj.build();
            }
            case "array": {
                NArrayElementBuilder obj = elements.ofArray();
                NodeList attrs = element.getChildNodes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node n = (Node) attrs.item(i);
                    obj.add(createElement(n, typeOfSrc, context));
                }
                return obj.build();
            }
            case "boolean": {
                return context.objectToElement(resolveValue(element), Boolean.class);
            }
            case "byte": {
                return context.objectToElement(resolveValue(element), Byte.class);
            }
            case "short": {
                return context.objectToElement(resolveValue(element), Short.class);
            }
            case "int": {
                return context.objectToElement(resolveValue(element), Integer.class);
            }
            case "long": {
                return context.objectToElement(resolveValue(element), Long.class);
            }
            case "float": {
                return context.objectToElement(resolveValue(element), Float.class);
            }
            case "double": {
                return context.objectToElement(resolveValue(element), Double.class);
            }
            case "char": {
                return context.objectToElement(resolveValue(element), Character.class);
            }
            case "string": {
                return context.objectToElement(resolveValue(element), String.class);
            }
            case "instant": {
                return context.objectToElement(resolveValue(element), Instant.class);
            }
            case "date": {
                return context.objectToElement(resolveValue(element), Date.class);
            }
            case "file": {
                return context.objectToElement(resolveValue(element), File.class);
            }
            case "path": {
                return context.objectToElement(resolveValue(element), Path.class);
            }
            default: {
                throw new IllegalArgumentException("unsupported");
            }
        }
    }

}
