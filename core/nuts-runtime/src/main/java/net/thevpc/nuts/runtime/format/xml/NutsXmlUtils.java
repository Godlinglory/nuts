/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.format.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.thevpc.nuts.*;

import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.log.NutsLogVerb;
import net.thevpc.nuts.runtime.util.common.CoreCommonUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author vpc
 */
public class NutsXmlUtils {

    public static void print(String name, Object object, long elemIndex, Writer out, boolean compact, boolean headerDeclaration, NutsWorkspace ws) {
        print(name, object, elemIndex, (Object) out, compact, headerDeclaration, ws);
    }

    public static void print(String name, Object object, long elemIndex, PrintStream out, boolean compact, boolean headerDeclaration, NutsWorkspace ws) {
        print(name, object, elemIndex, (Object) out, compact, headerDeclaration, ws);
    }

    private static void print(String name, Object object, long elemIndex, Object out, boolean compact, boolean headerDeclaration, NutsWorkspace ws) {
        try {
            Document document = NutsXmlUtils.createDocument(ws);
            String rootName = name;
            document.appendChild(createElement(CoreStringUtils.isBlank(rootName) ? "root" : rootName, object, elemIndex,document, ws));
            StreamResult streamResult = null;
            if (out instanceof PrintStream) {
                streamResult = new StreamResult((PrintStream) out);
            } else {
                streamResult = new StreamResult((Writer) out);
            }
            NutsXmlUtils.writeDocument(document, streamResult, compact,headerDeclaration);
            if (out instanceof PrintStream) {
                ((PrintStream) out).flush();
            } else {
                ((Writer) out).flush();
            }

        } catch (ParserConfigurationException | TransformerException ex) {
            throw new NutsIOException(ws,new IOException(ex));
        } catch (IOException ex) {
            throw new NutsIOException(ws,ex);
        }
    }

    public static Document createDocument(String name, Object object, NutsWorkspace ws) {
        try {
            Document document = createDocument(ws);
            document.appendChild(createElement(CoreStringUtils.isBlank(name) ? "root" : name, object, -1,document, ws));
            return document;
        } catch (ParserConfigurationException ex) {
            throw new NutsException(null, ex);
        }
    }

    public static Element createElement(String name, Object o,long elemIndex, Document document, NutsWorkspace ws) {
        // root element
        Element elem = document.createElement(createElementName(name));
        if(elemIndex>=0){
            elem.setAttribute("index",CoreCommonUtils.stringValue(elemIndex));
        }
        NutsElement elem2 = ws.formats().element().convert(o,NutsElement.class);
        switch (elem2.type()){
            case STRING:{
                elem.setAttribute("type", "string");
                elem.setTextContent(CoreCommonUtils.stringValue(elem2.primitive().getString()));
                break;
            }
            case FLOAT:{
                Number n = elem2.primitive().getNumber();
                if(n instanceof Double){
                    elem.setAttribute("type", "double");
                    elem.setTextContent(CoreCommonUtils.stringValue(elem2.primitive().getDouble()));
                }else if(n instanceof Float){
                    elem.setAttribute("type", "float");
                    elem.setTextContent(CoreCommonUtils.stringValue(elem2.primitive().getFloat()));
                }else{
                    elem.setAttribute("type", "double");
                    elem.setTextContent(CoreCommonUtils.stringValue(elem2.primitive().getDouble()));
                }
                break;
            }
            case INTEGER:{
                Number n = elem2.primitive().getNumber();
                if(n instanceof Integer){
                    elem.setAttribute("type", "int");
                    elem.setTextContent(CoreCommonUtils.stringValue(elem2.primitive().getInt()));
                }else if(n instanceof Long){
                    elem.setAttribute("type", "long");
                    elem.setTextContent(CoreCommonUtils.stringValue(elem2.primitive().getLong()));
                }else{
                    elem.setAttribute("type", "int");
                    elem.setTextContent(CoreCommonUtils.stringValue(elem2.primitive().getInt()));
                }
                break;
            }
            case BOOLEAN:{
                elem.setAttribute("type", "boolean");
                elem.setTextContent(CoreCommonUtils.stringValue(elem2.primitive().getBoolean()));
                break;
            }
            case DATE:{
                elem.setAttribute("type", "date");
                elem.setTextContent(CoreCommonUtils.stringValue(elem2.primitive().getDate()));
                break;
            }
            case NULL:{
                elem.setAttribute("type", "null");
                break;
            }
            case OBJECT:{
                elem.setAttribute("type", "object");
                for (NutsNamedElement child : elem2.object().children()) {
                    elem.appendChild(createElement(child.getName(), child.getValue(),-1, document, ws));
                }
                break;
            }
            case ARRAY:{
                elem.setAttribute("type", "array");
                int index=0;
                for (NutsElement child : elem2.array().children()) {
                    Element item = createElement("item", child, (long)index,document, ws);
                    elem.appendChild(item);
                    index++;
                }
                break;
            }
            default:{
                throw new IllegalArgumentException("Unsupported type"+elem2.type());
            }
        }
        return elem;
    }

    public static String createElementName(String name) {
        if (name == null) {
            name = "";
        }
        name = name.trim();
        if (name.isEmpty()) {
            name = "node";
        }
        if (Character.isDigit(name.charAt(0))) {
            name = "_" + name;
        }
        if (name.toLowerCase().startsWith("xml")) {
            name = "_" + name;
        }
        char[] r = name.toCharArray();
        for (int i = 0; i < r.length; i++) {
            char c = r[i];
            if (Character.isDigit(c)
                    || Character.isLetter(c)
                    || c == '_'
                    || c == '-'
                    || c == '.') {
                //ok
            } else {
                r[i] = '_';
            }
        }
        return new String(r);
    }

    public static Document createDocument(NutsWorkspace ws) throws ParserConfigurationException {
        return createDocumentBuilder(false,ws).newDocument();
    }

    public static DocumentBuilder createDocumentBuilder(boolean safe, NutsWorkspace ws) throws ParserConfigurationException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        if (safe) {
            documentFactory.setExpandEntityReferences(false);
            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
            // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
            setLenientFeature(documentFactory, "http://apache.org/xml/features/disallow-doctype-decl", true);

            // If you can't completely disable DTDs, then at least do the following:
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
            // JDK7+ - http://xml.org/sax/features/external-general-entities
            setLenientFeature(documentFactory, "http://xerces.apache.org/xerces-j/features.html#external-general-entities", false);
            setLenientFeature(documentFactory, "http://xerces.apache.org/xerces2-j/features.html#external-general-entities", false);
            setLenientFeature(documentFactory, "http://xml.org/sax/features/external-general-entities", false);

            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
            // JDK7+ - http://xml.org/sax/features/external-parameter-entities
            setLenientFeature(documentFactory, "http://xerces.apache.org/xerces-j/features.html#external-parameter-entities", false);
            setLenientFeature(documentFactory, "http://xml.org/sax/features/external-parameter-entities", false);
            setLenientFeature(documentFactory, "http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities", false);

            // Disable external DTDs as well
            setLenientFeature(documentFactory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
            documentFactory.setXIncludeAware(false);
            documentFactory.setValidating(false);
        }
        DocumentBuilder b = documentFactory.newDocumentBuilder();

        b.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                ws.log().of(NutsXmlUtils.class).with()
                        .level(Level.FINEST).verb(NutsLogVerb.WARNING).log(exception.toString());
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                ws.log().of(NutsXmlUtils.class).with()
                        .level(Level.FINEST).verb(NutsLogVerb.WARNING).log(exception.toString());
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                ws.log().of(NutsXmlUtils.class).with()
                        .level(Level.FINEST).verb(NutsLogVerb.WARNING).log(exception.toString());
            }
        });
        return b;
    }

    private static void setLenientFeature(DocumentBuilderFactory dbFactory, String s, boolean b) {
        try {
            dbFactory.setFeature(s, b);
        } catch (Exception ex) {
            //
        }
    }

    public static String documentToString(Document document) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            writeDocument(document, new StreamResult(b), true,true);
            return new String(b.toByteArray());
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String elementToString(Element elem,NutsWorkspace ws) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            Document d = createDocument(ws);
            elem = (Element) d.importNode(elem, true);
            d.appendChild(elem);
            writeDocument(d, new StreamResult(b), true,false);
            return new String(b.toByteArray());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void writeDocument(Document document, StreamResult writer, boolean compact,boolean headerDeclaration) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        if (!compact) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        }
        document.setXmlStandalone(false);
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
        if(!headerDeclaration) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        DOMSource domSource = new DOMSource(document);
        transformer.transform(domSource, writer);
    }
}
