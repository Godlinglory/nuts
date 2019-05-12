package net.vpc.app.nuts.core.bridges.maven.mvnutil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MavenMetadataParser {

    public static String toXmlString(MavenMetadata m) {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        try (Writer w = new OutputStreamWriter(s)) {
            writeMavenMetaData(m, new StreamResult(s));
            w.flush();
        } catch (TransformerException | ParserConfigurationException e) {
            throw new UncheckedIOException(new IOException(e));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new String(s.toByteArray());
    }

    public static void writeMavenMetaData(MavenMetadata m, Path path) {
        try (Writer w = Files.newBufferedWriter(path)) {
            writeMavenMetaData(m, new StreamResult(path.toFile()));
        } catch (TransformerException | ParserConfigurationException e) {
            throw new UncheckedIOException(new IOException(e));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void writeMavenMetaData(MavenMetadata m, StreamResult writer) throws TransformerException, ParserConfigurationException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

        Document document = documentBuilder.newDocument();

        Element metadata = document.createElement("metadata");
        document.appendChild(metadata);

        if (!isBlank(m.getGroupId())) {
            Element groupId = document.createElement("groupId");
            groupId.appendChild(document.createTextNode(m.getGroupId()));
            metadata.appendChild(groupId);
        }
        if (!isBlank(m.getArtifactId())) {
            Element artifactId = document.createElement("artifactId");
            artifactId.appendChild(document.createTextNode(m.getArtifactId()));
            metadata.appendChild(artifactId);
        }

        Element versioning = document.createElement("versioning");
        metadata.appendChild(versioning);

        if (!isBlank(m.getRelease())) {
            Element release = document.createElement("release");
            release.appendChild(document.createTextNode(m.getRelease()));
            versioning.appendChild(release);
        }

        if (!isBlank(m.getLatest())) {
            Element latest = document.createElement("latest");
            latest.appendChild(document.createTextNode(m.getLatest()));
            versioning.appendChild(latest);
        }

        Element versions = document.createElement("versions");
        versioning.appendChild(versions);
        if (m.getVersions() != null) {
            for (String sversion : m.getVersions()) {
                if (!isBlank(sversion)) {
                    Element version = document.createElement("version");
                    version.appendChild(document.createTextNode(sversion));
                    versions.appendChild(version);
                }
            }
        }
        if (m.getLastUpdated() != null) {
            Element lastUpdated = document.createElement("lastUpdated");
            lastUpdated.appendChild(document.createTextNode(new SimpleDateFormat("yyyyMMddHHmmss").format(m.getLastUpdated())));
            versioning.appendChild(lastUpdated);
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        document.setXmlStandalone(true);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "false");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource domSource = new DOMSource(document);
        transformer.transform(domSource, writer);
    }

    public static MavenMetadata parseMavenMetaData(Path stream) {
        try (InputStream s = Files.newInputStream(stream)) {
            return parseMavenMetaData(s);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static MavenMetadata parseMavenMetaData(InputStream stream) {
        MavenMetadata info = new MavenMetadata();
        StringBuilder ver = new StringBuilder();
        StringBuilder latest = new StringBuilder();
        StringBuilder release = new StringBuilder();
        StringBuilder lastUpdated = new StringBuilder();
        StringBuilder groupId = new StringBuilder();
        StringBuilder artifactId = new StringBuilder();
        List<String> versions = new ArrayList<>();
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
            XMLEventReader eventReader
                    = factory.createXMLEventReader(new InputStreamReader(stream));
            Stack<String> nodePath = new Stack<>();
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT: {
                        StartElement startElement = event.asStartElement();
                        String qName = startElement.getName().getLocalPart();
                        nodePath.push(qName);
                        ver.delete(0, ver.length());
                        break;
                    }
                    case XMLStreamConstants.CHARACTERS: {
                        if (isStackPath(nodePath, "metadata", "groupId")) {
                            groupId.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "metadata", "artifactId")) {
                            artifactId.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "metadata", "versioning", "versions", "version")) {
                            ver.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "metadata", "versioning", "release")) {
                            release.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "metadata", "versioning", "latest")) {
                            latest.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "metadata", "versioning", "lastUpdated")) {
                            lastUpdated.append(event.asCharacters().getData());
                        }
                        break;
                    }
                    case XMLStreamConstants.END_ELEMENT: {
                        if (isStackPath(nodePath, "metadata", "versioning", "versions", "version")) {
                            nodePath.pop();
                            if (ver.length() > 0) {
                                versions.add(ver.toString());
                            }
                        } else {
                            nodePath.pop();
                        }
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        info.setGroupId(groupId.toString().trim());
        info.setArtifactId(artifactId.toString().trim());
        info.setLatest(latest.toString().trim());
        info.setRelease(release.toString().trim());
        try {
            //<lastUpdated>2018 12 22 22 59 57</lastUpdated>

            info.setLastUpdated(lastUpdated.toString().trim().isEmpty() ? null : new SimpleDateFormat("yyyyMMddHHmmss").parse(lastUpdated.toString().trim()));
        } catch (Exception ex) {
            //ignore!!
        }
        for (String version : versions) {
            info.getVersions().add(version.trim());
        }
        return info;
    }

    private static boolean isStackPath(Stack<String> stack, String... path) {
        if (stack.size() != path.length) {
            return false;
        }
        for (int i = 0; i < path.length; i++) {
            if (!stack.get(stack.size() - path.length + i).equals(path[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }
}
