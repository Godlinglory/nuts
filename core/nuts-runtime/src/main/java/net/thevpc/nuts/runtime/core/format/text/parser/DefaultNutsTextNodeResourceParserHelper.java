package net.thevpc.nuts.runtime.core.format.text.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.StringPlaceHolderParser;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.NutsWorkspaceVarExpansionFunction;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class DefaultNutsTextNodeResourceParserHelper {

    private NutsTextParser parser;
    private NutsSession session;
    private NutsWorkspaceVarExpansionFunction pathExpansionConverter;

    public DefaultNutsTextNodeResourceParserHelper(NutsTextParser parser, NutsSession session) {
        this.parser = parser;
        this.session = session;
        pathExpansionConverter = new NutsWorkspaceVarExpansionFunction(session.getWorkspace());
    }

    public NutsText parseResource(String resourceName, NutsTextFormatLoader loader) {
        if (loader == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing loader"));
        }
        Reader reader = loader.forPath(resourceName);
        if (reader == null) {
            return null;
        }
        return parseResource(resourceName, reader, loader);
    }

    public NutsTextFormatLoader createClassPathLoader(ClassLoader loader) {
        return new NutsTextFormatLoaderClassPath(loader);
    }

    public NutsTextFormatLoader createFileLoader(File root) {
        return new NutsTextFormatLoaderFile(root);
    }

    public NutsText parseResource(String resourceName, Reader reader, NutsTextFormatLoader loader) {
        if (loader == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing loader"));
        }
        if (reader == null) {
            reader = loader.forPath(resourceName);
        }
        if (reader == null) {
            return null;
        }
        return processHelp(CoreIOUtils.loadString(reader, true), loader, true, null);
    }

//    private String loadHelp(String urlPath, ClassLoader clazz, boolean err, boolean vars, String defaultValue) {
//        return loadHelp(urlPath, clazz, err, 36, vars, defaultValue);
//    }
//
//    private String loadHelp(String urlPath, ClassLoader classLoader, boolean err, int depth, boolean vars, String defaultValue) {
//        if (depth <= 0) {
//            throw new IllegalArgumentException("Unable to load " + urlPath + ". Too many recursions");
//        }
//        if (classLoader == null) {
//            classLoader = Thread.currentThread().getContextClassLoader();
//        }
//        if (urlPath.startsWith("/")) {
//            urlPath = urlPath.substring(1);
//        }
//        int interr = urlPath.indexOf('?');
//        String forAnchor = null;
//        if (interr > 0) {
//            forAnchor = urlPath.substring(interr + 1);
//            urlPath = urlPath.substring(interr + 1);
//        }
//        URL resource = classLoader.getResource(urlPath);
//        Reader in = null;
//        if (resource == null) {
//            if (err) {
//                return builder().append(
//                        "not found resource " + urlPath, NutsTextStyle.error()
//                ).toString();
//            }
//            if (defaultValue == null) {
//                return null;
//            }
//            in = new StringReader(defaultValue);
//        } else {
//            in = new InputStreamReader(NutsWorkspaceUtils.of(ws).openURL(resource));
//        }
//        try (Reader is = in) {
//            return loadHelp(is, classLoader, true, depth, vars, forAnchor);
//        } catch (IOException ex) {
//            throw new UncheckedIOException(ex);
//        }
//    }
//
//    private String loadHelp(Reader is, ClassLoader classLoader, boolean err, int depth, boolean vars, String forAnchor) {
//        return processHelp(CoreIOUtils.loadString(is, true), classLoader, err, depth, vars, forAnchor);
//    }
    private NutsText processHelp(String s, NutsTextFormatLoader classLoader, boolean vars, String anchor) {

        StringBuilder sb = new StringBuilder();
        if (s != null) {
            StringTokenizer st = new StringTokenizer(s, "\n\r", true);
            while (st.hasMoreElements()) {
                String e = st.nextToken();
                if (e.length() > 0) {
                    if (e.charAt(0) == '\n' || e.charAt(0) == '\r') {
                        sb.append(e);
                    } else if (e.startsWith("#!include<") && e.trim().endsWith(">")) {
                        e = e.trim();
                        e = e.substring("#!include<".length(), e.length() - 1);
                        NutsText other = null;
                        try {
                            other = parseResource(e, classLoader);
                        } catch (Throwable t) {
                            other = session.text().
                                    builder().append("NOT FOUND", NutsTextStyle.error())
                                    .append(" <" + e + ">").toText();
                        }
                        sb.append(other);//if(!other.toText().toString().endsWith("\n")){sb.append("\n");}
                    } else {
                        sb.append(e);
                    }
                }
            }
        }
        String help = sb.toString();
        if (vars) {
            help = StringPlaceHolderParser.replaceDollarPlaceHolders(help, pathExpansionConverter);
        }
//        NutsTextParser p = session.text().parser().parse()new DefaultNutsTextNodeParser(session);
        NutsText node = session.text().parser().parse(new StringReader(help));
        if (anchor != null) {
            List<NutsText> ok = new ArrayList<>();
            boolean start = false;
            if (node.getType() == NutsTextType.LIST) {
                for (NutsText o : ((NutsTextList) node)) {
                    if (start) {
                        ok.add(o);
                    } else if (o.getType() == NutsTextType.ANCHOR) {
                        if (anchor.equals(((DefaultNutsTextAnchor) o).getValue())) {
                            start = true;
                        }
                    }
                }
            }
            if (start) {
                node = session.text().forList(ok).simplify();
            }
            return node;
        }
        return parser.parse(new StringReader(help));
    }

    private static class NutsTextFormatLoaderClassPath implements NutsTextFormatLoader {

        private final ClassLoader loader;

        public NutsTextFormatLoaderClassPath(ClassLoader loader) {
            this.loader = loader;
        }

        @Override
        public Reader forPath(String path) {
            ClassLoader classLoader = loader;
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            URL r = classLoader.getResource(path);
            if (r == null) {
                if (path.length() > 0 && path.startsWith("/")) {
                    r = classLoader.getResource(path.substring(1));
                }
            }
            if (r == null) {
                return null;
            }
            try {
                return new InputStreamReader(r.openStream());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static class NutsTextFormatLoaderFile implements NutsTextFormatLoader {

        private final File root;

        public NutsTextFormatLoaderFile(File root) {
            this.root = root;
        }

        @Override
        public Reader forPath(String path) {
            File r = root == null ? new File(path) : new File(root, path);
            if (!r.isFile()) {
                return null;
            }
            try {
                return new FileReader(r);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

//    public String loadFormattedString(Reader is, ClassLoader classLoader) {
//        return loadHelp(is, classLoader, true, 36, true, null);
//    }
//
//    public String loadFormattedString(String resourcePath, ClassLoader classLoader, String defaultValue) {
//        return loadHelp(resourcePath, classLoader, false, true, defaultValue);
//    }
}
