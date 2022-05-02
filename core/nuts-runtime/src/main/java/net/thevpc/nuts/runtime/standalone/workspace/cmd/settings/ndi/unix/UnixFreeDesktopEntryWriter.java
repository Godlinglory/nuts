package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.unix;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.executor.system.NutsSysExecUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.FreeDesktopEntry;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base.AbstractFreeDesktopEntryWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UnixFreeDesktopEntryWriter extends AbstractFreeDesktopEntryWriter {
    private final NutsSession session;
    private final NutsPath desktopPath;

    public UnixFreeDesktopEntryWriter(NutsSession session, NutsPath desktopPath) {
        this.session = session;
        this.desktopPath = desktopPath;
    }


    @Override
    public PathInfo[] writeShortcut(FreeDesktopEntry descriptor, NutsPath path, boolean doOverride, NutsId id) {
        path = NutsPath.of(ensureName(path == null ? null : path.toString(), descriptor.getOrCreateDesktopEntry().getName(), "desktop"),session);
        PathInfo.Status s = tryWrite(descriptor, path);
        return new PathInfo[]{new PathInfo("desktop-shortcut", id, path, s)};
    }

    @Override
    public PathInfo[] writeDesktop(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NutsId id) {
        fileName = Paths.get(ensureName(fileName, descriptor.getOrCreateDesktopEntry().getName(), "desktop")).getFileName().toString();
        NutsPath q = desktopPath.resolve(fileName);
        return writeShortcut(descriptor, q, doOverride, id);
    }

    @Override
    public PathInfo[] writeMenu(FreeDesktopEntry descriptor, String fileName, boolean doOverride, NutsId id) {
        String desktopFileName = Paths.get(ensureName(fileName, descriptor.getOrCreateDesktopEntry().getName(), "desktop")).getFileName().toString();

        List<PathInfo> all = new ArrayList<>();
        FreeDesktopEntry.Group root = descriptor.getOrCreateDesktopEntry();
        NutsPath folder4shortcuts = NutsPath.ofUserHome(session).resolve(".local/share/applications");
        folder4shortcuts.mkdirs();
        NutsPath shortcutFile =folder4shortcuts.resolve(desktopFileName);
        all.add(new PathInfo("desktop-icon", id,
                shortcutFile, tryWrite(descriptor, shortcutFile)));

        List<String> categories = new ArrayList<>(root.getCategories());
        if (categories.isEmpty()) {
            categories.add("/");
        }
        NutsPath folder4menus = NutsPath.ofUserHome(session).resolve(".config/menus/applications-merged");
        folder4menus.mkdirs();

        //menu name must include category
        String menuFileName = Paths.get(ensureName(fileName, descriptor.getOrCreateDesktopEntry().getName(), "menu")).getFileName().toString();

        try {
            //menu
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document dom = builder.newDocument();

            for (String menuPath : categories) {
                String[] part = Arrays.stream((menuPath == null ? "" : menuPath).split("/")).filter(x -> !x.isEmpty()).toArray(String[]::new);
                if (part.length == 0) {
                    part = new String[]{"Applications"};
                } else if (!part[0].equals("Applications")) {
                    List<String> li = new ArrayList<>();
                    li.add("Applications");
                    li.addAll(Arrays.asList(part));
                    part = li.toArray(new String[0]);
                }
                createMenuXmlElement(part, desktopFileName, dom);
            }
            // write DOM to XML file
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            tr.transform(new DOMSource(dom), new StreamResult(b));
            NutsPath menuFile = folder4menus.resolve(menuFileName);
            all.add(new PathInfo("desktop-menu", id, menuFile, CoreIOUtils.tryWrite(b.toByteArray(), menuFile, session)));
        } catch (ParserConfigurationException | TransformerException ex) {
            throw new RuntimeException(ex);
        }
        if (all.stream().anyMatch(x -> x.getStatus() != PathInfo.Status.DISCARDED)) {
            updateDesktopMenus();
        }
        return all.toArray(new PathInfo[0]);
    }

    private void callMeMaye(String... command) {
        List<String> cmdList = new ArrayList<>(Arrays.asList(command));
        String sysCmd = cmdList.remove(0);
        Path a = NutsSysExecUtils.sysWhich(sysCmd);
        if (a != null) {
            cmdList.add(0, a.toString());
            String outStr = session.exec()
                    .setCommand(cmdList)
                    .addCommand()
                    .setExecutionType(NutsExecutionType.SYSTEM)
                    .setRedirectErrorStream(true).grabOutputString()
                    .run()
                    .getOutputString().trim();
            if (session.isPlainTrace() && !outStr.isEmpty()) {
                session.out().println(CoreStringUtils.prefixLinesOsNL(outStr, "[" + sysCmd + "] "));
            }
        }
    }

    private void updateDesktopMenus() {
        //    KDE  : 'kbuildsycoca5'
        callMeMaye("kbuildsycoca5");
        //    GNOME: update-desktop-database ~/.local/share/applications
        callMeMaye("update-desktop-database", System.getProperty("user.home") + "/.local/share/applications");
        // more generic : xdg-desktop-menu forceupdate
        callMeMaye("xdg-desktop-menu", "forceupdate");
    }

    private String getDesktopEnvironment() {
        return System.getenv("XDG_SESSION_DESKTOP");
    }

    private Element ensureXmlChild(Node parent, String name) {
        NodeList cn = parent.getChildNodes();
        for (int i = 0; i < cn.getLength(); i++) {
            Node ci = cn.item(i);
            if (ci instanceof Element) {
                Element e = (Element) ci;
                String nn = (e).getNodeName();
                if (name.equals(nn)) {
                    return e;
                }
            }
        }
        Document doc = (parent instanceof Document)?(Document)parent : parent.getOwnerDocument();
        Element elem = doc.createElement(name);
        parent.appendChild(elem);
        return elem;
    }

    /**
     * //        <Menu>
     * //    <Name>Applications</Name>
     * //    <Menu>
     * //        <Directory>YourApp-top.directory</Directory>
     * //        <Name>YourApp-top</Name>
     * //        <Menu>
     * //            <Directory>YourApp-second.directory</Directory>
     * //            <Name>YourApp-second</Name>
     * //            <Include>
     * //                <Filename>YourApp-test.desktop</Filename>
     * //            </Include>
     * //        </Menu>
     * //    </Menu>
     * //</Menu>
     *
     * @param a      a
     * @param name   name
     * @param parent parent
     */
    private void createMenuXmlElement(String[] a, String name, Node parent) {
        if (a.length == 1) {
            Element emenu = ensureXmlChild(parent, "Menu");
            Element ename = ensureXmlChild(emenu, "Name");
            ename.setTextContent(a[0]);
            Element einclude = ensureXmlChild(emenu, "Include");
            Element efilename = ensureXmlChild(einclude, "Filename");
            efilename.setTextContent(name);
        } else {
            Element emenu = ensureXmlChild(parent, "Menu");
            Element ename = ensureXmlChild(emenu, "Name");
            ename.setTextContent(a[0]);
            createMenuXmlElement(Arrays.copyOfRange(a, 1, a.length), name, emenu);
        }
    }

    public void write(FreeDesktopEntry file, Path out) {
        NutsPath.of(out,session).mkParentDirs();
        try (PrintStream p = new PrintStream(Files.newOutputStream(out))) {
            write(file, p);
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }


    public PathInfo.Status tryWrite(FreeDesktopEntry file, NutsPath out) {
        out.mkParentDirs();
        return CoreIOUtils.tryWrite(writeAsString(file).getBytes(), out, session);
    }

    public String writeAsString(FreeDesktopEntry file) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bos);
        write(file, out);
        out.flush();
        return bos.toString();
    }


    public void write(FreeDesktopEntry file, File out) {
        NutsPath.of(out,session).mkParentDirs();
        try (PrintStream p = new PrintStream(out)) {
            write(file, p);
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public void write(FreeDesktopEntry file, NutsPrintStream out) {
        write(file, out.asPrintStream());
    }

    public void write(FreeDesktopEntry file, PrintStream out) {
        out.println("#!/usr/bin/env xdg-open");
        for (FreeDesktopEntry.Group group : file.getGroups()) {
            out.println();
            String gn = group.getGroupName();
            if (gn == null || gn.trim().length() == 0) {
                throw new IllegalArgumentException("invalid group name");
            }
            FreeDesktopEntry.Type t = group.getType();
            if (t == null) {
                throw new IllegalArgumentException("missing type");
            }
            out.println("[" + gn.trim() + "]");
            for (Map.Entry<String, Object> e : group.toMap().entrySet()) {
                Object value = e.getValue();
                String key = e.getKey();
                if (value instanceof FreeDesktopEntry.Type) {
                    String v = value.toString().toLowerCase();
                    v = Character.toUpperCase(v.charAt(0)) + v.substring(1);
                    out.println(key + "=" + v);
                } else if (value instanceof Boolean || value instanceof String) {
                    out.println(key + "=" + value);
                } else if (value instanceof List) {
                    char sep = ';';
                    out.println(key + "=" +
                            ((List<String>) value).stream().map(x -> {
                                StringBuilder sb = new StringBuilder();
                                for (char c : x.toCharArray()) {
                                    if (c == sep || c == '\\') {
                                        sb.append('\\');
                                    }
                                    sb.append(c);
                                }
                                return sb.toString();
                            }).collect(Collectors.joining("" + sep))
                    );
                } else {
                    throw new IllegalArgumentException("unsupported value type for " + key);
                }
            }

        }
    }
}
