/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
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
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public class XmlCommand extends SimpleJShellBuiltin {

    public XmlCommand() {
        super("xml", DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean configureFirst(NCommandLine commandLine, JShellExecutionContext context) {
        NSession session = context.getSession();
        Options options = context.getOptions();
        NArgument a;
        if ((a = commandLine.nextString("-f", "--file").orNull()) != null) {
            options.input = a.getStringValue().get(session);
            return true;
        } else if ((a = commandLine.nextString("-q", "--xpath").orNull()) != null) {
            options.xpaths.add(a.getStringValue().get(session));
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        if (options.xpaths.isEmpty()) {
            commandLine.throwMissingArgument();
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (Exception ex) {
            throw new NExecutionException(session, NMsg.ofPlain("unable to initialize xml system"), ex, 3);
        }

        Document doc = null;
        if (options.input != null) {
            NPath file = NPath.of(options.input, session).toAbsolute(context.getCwd());
            if (file.isFile()) {
                try (InputStream is = file.getInputStream()) {
                    doc = dBuilder.parse(is);
                } catch (Exception ex) {
                    throw new NExecutionException(session, NMsg.ofCstyle("invalid xml %s", options.input), ex, 2);
                }
            } else {
                throw new NExecutionException(session, NMsg.ofCstyle("invalid path %s", options.input), 1);
            }
        } else {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = null;
                try {
                    line = reader.readLine();
                } catch (IOException ex) {
                    throw new NExecutionException(session, NMsg.ofPlain("broken Input"), 2);
                }
                if (line == null) {
                    try {
                        doc = dBuilder.parse(new InputSource(new StringReader(sb.toString())));
                    } catch (Exception ex) {
                        throw new NExecutionException(session, NMsg.ofCstyle("invalid xml : %s", sb), ex, 2);
                    }
                    break;
                } else {
                    sb.append(line);
                    try {
                        doc = dBuilder.parse(new InputSource(new StringReader(sb.toString())));
                        break;
                    } catch (Exception ex) {
                        //
                    }
                }
            }
        }

        //optional, but recommended
        //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();
        List<Object> all = new ArrayList<>();
        all.add(doc);
        XPath xPath = XPathFactory.newInstance().newXPath();
        List<NodeList> result = new ArrayList<>();
        for (String query : options.xpaths) {
            try {
                result.add((NodeList) xPath.compile(query).evaluate(doc, XPathConstants.NODESET));
            } catch (XPathExpressionException ex) {
                throw new NExecutionException(session, NMsg.ofCstyle("%s", ex), ex, 103);
            }
        }
        if (all.size() == 1) {
            session.out().printlnf(all.get(0));
        } else {
            session.out().printlnf(all);
        }
    }

    private static class Options {

        String input;
        List<String> xpaths = new ArrayList<>();
    }

}
