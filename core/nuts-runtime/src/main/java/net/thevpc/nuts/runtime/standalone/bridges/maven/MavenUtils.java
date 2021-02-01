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
package net.thevpc.nuts.runtime.standalone.bridges.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.common.MapToFunction;
import net.thevpc.nuts.runtime.bundles.mvn.*;
import net.thevpc.nuts.runtime.core.repos.RepoDefinitionResolver;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDependencyBuilder;
import net.thevpc.nuts.runtime.core.model.DefaultNutsVersion;
import net.thevpc.nuts.runtime.standalone.io.NamedByteArrayInputStream;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsDependencyScopes;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.SearchTraceHelper;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.bundles.iter.IteratorBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by vpc on 2/20/17.
 */
public class MavenUtils {

    private final NutsLogger LOG;
    private NutsWorkspace ws;

    public static MavenUtils of(NutsWorkspace ws) {
        MavenUtils wp = (MavenUtils) ws.env().getProperty(MavenUtils.class.getName());
        if (wp == null) {
            wp = new MavenUtils(ws);
            ws.env().setProperty(MavenUtils.class.getName(), wp,new NutsUpdateOptions(ws.createSession()));
        }
        return wp;
    }

    private MavenUtils(NutsWorkspace ws) {
        this.ws = ws;
        LOG=ws.log().of(MavenUtils.class);
    }

    public static PomIdResolver createPomIdResolver(NutsSession session) {
        PomIdResolver wp = (PomIdResolver) session.getWorkspace().env().getProperty(PomIdResolver.class.getName());
        if (wp == null) {
            wp = new PomIdResolver(new NutsPomUrlReader(session),new NutsPomLogger(session));
            session.getWorkspace().env().setProperty(PomIdResolver.class.getName(), wp,new NutsUpdateOptions(session));
        }
        return wp;
    }

    public NutsId[] toNutsId(PomId[] ids) {
        NutsId[] a = new NutsId[ids.length];
        for (int i = 0; i < ids.length; i++) {
            a[i] = toNutsId(ids[i]);
        }
        return a;
    }

    public NutsDependency[] toNutsDependencies(PomDependency[] deps, NutsSession session) {
        NutsDependency[] a = new NutsDependency[deps.length];
        for (int i = 0; i < deps.length; i++) {
            a[i] = toNutsDependency(deps[i], session);
        }
        return a;
    }

    public NutsId toNutsId(PomId d) {
        return ws.id().builder().setGroupId(d.getGroupId()).setArtifactId(d.getArtifactId()).setVersion(toNutsVersion(d.getVersion())).build();
    }

    public NutsDependency toNutsDependency(PomDependency d, NutsSession session) {
        String s = d.getScope();
        if(s==null){
            s="";
        }
        s=s.trim();
        NutsDependencyScope nds=NutsDependencyScope.API;
        switch (s){
            case "":
            case "compile":
                {
                nds=NutsDependencyScope.API;
                break;
            }
            case "test":{
                nds=NutsDependencyScope.TEST_API;
                break;
            }
            case "system":{
                nds=NutsDependencyScope.SYSTEM;
                break;
            }
            case "runtime":{
                nds=NutsDependencyScope.RUNTIME;
                break;
            }
            case "provided":{
                nds=NutsDependencyScope.PROVIDED;
                break;
            }
            case "import":{
                nds=NutsDependencyScope.IMPORT;
                break;
            }
            default:{
                nds= NutsDependencyScopes.parseScope(s,true);
                if(nds==null){
                    LOG.with().session(session).level(Level.FINER).verb(NutsLogVerb.FAIL).log( "unable to parse maven scope {0} for {1}",s,d);
                    nds=NutsDependencyScope.API;
                }
            }
        }
        return new DefaultNutsDependencyBuilder(ws)
                .setGroupId(d.getGroupId())
                .setArtifactId(d.getArtifactId())
                .setClassifier(d.getClassifier())
                .setVersion(toNutsVersion((d.getVersion())))
                .setOptional(d.getOptional())
                .setScope(nds.id())
                .setExclusions(toNutsId(d.getExclusions()))
        .build();
    }

    private boolean testNode(Node n, Predicate<Node> tst) {
        if (tst.test(n)) {
            return true;
        }
        if (n instanceof Element) {
            Element e = (Element) n;
            final NodeList nl = e.getChildNodes();
            final int len = nl.getLength();
            for (int i = 0; i < len; i++) {
                if (testNode(nl.item(i), tst)) {
                    return true;
                }
            }
        }
        return false;
    }

    public NutsDescriptor parsePomXml0(InputStream stream, NutsFetchMode fetchMode, String urlDesc, NutsRepository repository, NutsSession session) {
        long startTime = System.currentTimeMillis();
        try {
            if (stream == null) {
                return null;
            }
            byte[] bytes = CoreIOUtils.loadByteArray(stream);
            Pom pom = new PomXmlParser(new NutsPomLogger(session)).parse(new NamedByteArrayInputStream(bytes,urlDesc), session);
            boolean executable = false;// !"maven-archetype".equals(packaging.toString()); // default is true :)
            boolean application = false;// !"maven-archetype".equals(packaging.toString()); // default is true :)
            if ("true".equals(pom.getProperties().get("nuts.executable"))) {
                executable = true;
            } else {
                final Element ee = pom.getXml().getDocumentElement();
                if (testNode(ee, x -> {
                    if (x instanceof Element) {
                        Element e = (Element) x;
                        if (e.getNodeName().equals("mainClass")) {
                            return true;
                        }
                        if (e.getNodeName().equals("goal")) {
                            if (CoreStringUtils.trim(e.getTextContent()).equals("exec-war-only")) {
                                return true;
                            }
                        }
                    }
                    return false;
                })) {
                    executable = true;
                }
            }
            if ("true".equals(pom.getProperties().get("nuts.application"))) {
                application = true;
            }
            if (application) {
                executable = true;
            }
            if (pom.getPackaging().isEmpty()) {
                pom.setPackaging("jar");
            }

            long time = System.currentTimeMillis() - startTime;
            String fetchString = "[" + CoreStringUtils.alignLeft(fetchMode.id(), 7) + "] ";
            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.SUCCESS).time(time).formatted()
                    .log("{0}{1} parse pom    {2}", fetchString
                            , CoreStringUtils.alignLeft(repository==null?"<no-repo>":repository.getName(), 20)
                            ,urlDesc
                    );

            return ws.descriptor().descriptorBuilder()
                    .setId(toNutsId(pom.getPomId()))
                    .setParents(pom.getParent() == null ? new NutsId[0] : new NutsId[]{toNutsId(pom.getParent())})
                    .setPackaging(pom.getPackaging())
                    .setExecutable(executable)
                    .setApplication(application)
                    .setName(pom.getArtifactId())
                    .setDescription(pom.getDescription())
                    .setPlatform(new String[]{"java"})
                    .setDependencies(toNutsDependencies(pom.getDependencies(), session))
                    .setStandardDependencies(toNutsDependencies(pom.getDependenciesManagement(), session))
                    .setProperties(pom.getProperties())
                    .build();
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.FAIL).time(time).formatted()
                    .log("caching pom file {0}", urlDesc);
            throw new NutsParseException(ws, "error Parsing " + urlDesc, e);
        }
    }

    public String toNutsVersion(String version) {
        /// maven : [cc] [co) (oc] (oo)
        /// nuts  : [cc] [co[ ]oc] ]oo[
        return version == null ? null : version.replace("(", "]").replace(")", "[");
    }

    public NutsDescriptor parsePomXml(Path path, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) throws IOException {
        try {
            SearchTraceHelper.progressIndeterminate("parse "+CoreIOUtils.compressUrl(path.toString()),session);
            try (InputStream is = Files.newInputStream(path)) {
                NutsDescriptor nutsDescriptor = parsePomXml(is, fetchMode, path.toString(), repository, session);
                if (nutsDescriptor.getId().getArtifactId() == null) {
                    //why name is null ? should checkout!
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.with().session(session).level(Level.FINE).verb(NutsLogVerb.FAIL).log( "Unable to fetch Valid Nuts from " + path + " : resolved id was " + nutsDescriptor.getId());
                    }
                    return null;
                }
                return nutsDescriptor;
            }
        } catch (IOException ex) {
            throw new NutsIOException(session.getWorkspace(),ex);
        }
    }

    public NutsDescriptor parsePomXml(InputStream stream, NutsFetchMode fetchMode, String urlDesc, NutsRepository repository, NutsSession session) {
        NutsDescriptor nutsDescriptor = null;
//        if (session == null) {
//            session = ws.createSession();
//        }
        try {
            try {
//            bytes = IOUtils.loadByteArray(stream, true);
                nutsDescriptor = parsePomXml0(stream, fetchMode, urlDesc, repository, session);
                HashMap<String, String> properties = new HashMap<>();
                NutsId parentId = null;
                for (NutsId nutsId : nutsDescriptor.getParents()) {
                    parentId = nutsId;
                }
                NutsDescriptor parentDescriptor = null;
                if (parentId != null) {
                    if (!CoreNutsUtils.isEffectiveId(parentId)) {
                        try {
                            parentDescriptor = ws.fetch().setId(parentId).setEffective(true)
                                    .setSession(session)
                                    .setTransitive(true)
                                    .setFetchStrategy(
                                            fetchMode == NutsFetchMode.REMOTE ? NutsFetchStrategy.ONLINE
                                            : NutsFetchStrategy.OFFLINE
                                    ).getResultDescriptor();
                        } catch (NutsException ex) {
                            throw ex;
                        } catch (Exception ex) {
                            throw new NutsNotFoundException(ws, nutsDescriptor.getId(), "Unable to resolve " + nutsDescriptor.getId() + " parent " + parentId, ex);
                        }
                        parentId = parentDescriptor.getId();
                    }
                }
                if (parentId != null) {
                    properties.put("parent.groupId", parentId.getGroupId());
                    properties.put("parent.artifactId", parentId.getArtifactId());
                    properties.put("parent.version", parentId.getVersion().getValue());

                    properties.put("project.parent.groupId", parentId.getGroupId());
                    properties.put("project.parent.artifactId", parentId.getArtifactId());
                    properties.put("project.parent.version", parentId.getVersion().getValue());
                    nutsDescriptor = nutsDescriptor/*.setProperties(properties, true)*/.builder().applyProperties(properties).build();
                }
                NutsId thisId = nutsDescriptor.getId();
                if (!CoreNutsUtils.isEffectiveId(thisId)) {
                    if (parentId != null) {
                        if (CoreStringUtils.isBlank(thisId.getGroupId())) {
                            thisId = thisId.builder().setGroupId(parentId.getGroupId()).build();
                        }
                        if (CoreStringUtils.isBlank(thisId.getVersion().getValue())) {
                            thisId = thisId.builder().setVersion(parentId.getVersion().getValue()).build();
                        }
                    }
                    HashMap<NutsId, NutsDescriptor> cache = new HashMap<>();
                    Set<String> done = new HashSet<>();
                    Stack<NutsId> todo = new Stack<>();
                    todo.push(nutsDescriptor.getId());
                    cache.put(nutsDescriptor.getId(), nutsDescriptor);
                    while (todo.isEmpty()) {
                        NutsId pid = todo.pop();
                        NutsDescriptor d = cache.get(pid);
                        if (d == null) {
                            try {
                                d = ws.fetch().setId(pid).setEffective(true).setSession(session).getResultDescriptor();
                            } catch (NutsException ex) {
                                throw ex;
                            } catch (Exception ex) {
                                throw new NutsNotFoundException(ws, nutsDescriptor.getId(), "Unable to resolve " + nutsDescriptor.getId() + " parent " + pid, ex);
                            }
                        }
                        done.add(pid.getShortName());
                        if (CoreNutsUtils.containsVars(thisId)) {
                            thisId.builder().apply(new MapToFunction<>(d.getProperties())).build();
                        } else {
                            break;
                        }
                        for (NutsId nutsId : d.getParents()) {
                            if (!done.contains(nutsId.getShortName())) {
                                todo.push(nutsId);
                            }
                        }
                    }
                    if (CoreNutsUtils.containsVars(thisId)) {
                        throw new NutsNotFoundException(ws, nutsDescriptor.getId(), "Unable to resolve " + nutsDescriptor.getId() + " parent " + parentId, null);
                    }
                    nutsDescriptor = nutsDescriptor.builder().setId(thisId).build();
                }
                String nutsPackaging = nutsDescriptor.getProperties().get("nuts-packaging");
                if (!CoreStringUtils.isBlank(nutsPackaging)) {
                    nutsDescriptor = nutsDescriptor.builder().setPackaging(nutsPackaging).build();
                }
                properties.put("pom.groupId", thisId.getGroupId());
                properties.put("pom.version", thisId.getVersion().getValue());
                properties.put("pom.artifactId", thisId.getArtifactId());
                properties.put("project.groupId", thisId.getGroupId());
                properties.put("project.artifactId", thisId.getArtifactId());
                properties.put("project.version", thisId.getVersion().getValue());
                properties.put("version", thisId.getVersion().getValue());
                nutsDescriptor = nutsDescriptor/*.setProperties(properties, true)*/.builder().applyProperties(properties).build();
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session.getWorkspace(),ex);
        } catch (Exception ex) {
            throw new NutsParseException(ws, "error Parsing " + urlDesc, ex);
        }
        return nutsDescriptor;
    }

    public Iterator<NutsId> createArchetypeCatalogIterator(InputStream stream, NutsIdFilter filter, boolean autoClose, NutsSession session) {
        Iterator<PomId> it = ArchetypeCatalogParser.createArchetypeCatalogIterator(stream, filter == null ? null : new PomIdFilter() {
            @Override
            public boolean accept(PomId id) {
                return filter.acceptId(toNutsId(id), session);
            }
        }, autoClose);
        return IteratorBuilder.of(it).convert(pomId -> toNutsId(pomId),"PomId->NutsId").build();
    }

    public MavenMetadata parseMavenMetaData(InputStream metadataStream,NutsSession session) {
        MavenMetadata s = new MavenMetadataParser(session).parseMavenMetaData(metadataStream);
        if (s == null) {
            return s;
        }
        for (Iterator<String> iterator = s.getVersions().iterator(); iterator.hasNext();) {
            String version = iterator.next();
            if (s.getLatest().length() > 0 && DefaultNutsVersion.compareVersions(version, s.getLatest()) > 0) {
                iterator.remove();
            }
        }
        return s;
    }


    public DepsAndRepos loadDependenciesAndRepositoriesFromPomPath(NutsId rid, Collection<String> bootRepositories, NutsSession session) {
        String urlPath = CoreNutsUtils.idToPath(rid) + "/" + rid.getArtifactId() + "-" + rid.getVersion() + ".pom";
        return loadDependenciesAndRepositoriesFromPomPath(urlPath,bootRepositories, session);
    }

    public DepsAndRepos loadDependenciesAndRepositoriesFromPomPath(String urlPath, Collection<String> bootRepositories, NutsSession session) {
        DepsAndRepos depsAndRepos = null;
//        if (!NO_M2) {
            File mavenNutsCorePom = new File(System.getProperty("user.home"), (".m2/repository/" + urlPath).replace("/", File.separator));
            if (mavenNutsCorePom.isFile()) {
                depsAndRepos = loadDependenciesAndRepositoriesFromPomUrl(mavenNutsCorePom.getPath(), session);
            }
//        }
        if (depsAndRepos == null || depsAndRepos.deps.isEmpty()) {
            for (String baseUrl : bootRepositories) {
                NutsAddRepositoryOptions opt = RepoDefinitionResolver.createRepositoryOptions(baseUrl, false, ws);
                String location = opt.getConfig()==null?opt.getLocation():opt.getConfig().getLocation();
                depsAndRepos = loadDependenciesAndRepositoriesFromPomUrl(location + "/" + urlPath, session);
                if (!depsAndRepos.deps.isEmpty()) {
                    break;
                }
            }
        }
        return depsAndRepos;
    }

    public DepsAndRepos loadDependenciesAndRepositoriesFromPomUrl(String url, NutsSession session) {
        SearchTraceHelper.progressIndeterminate("load "+CoreIOUtils.compressUrl(url),ws.createSession());
        DepsAndRepos depsAndRepos = new DepsAndRepos();
//        String repositories = null;
//        String dependencies = null;
        InputStream xml = null;
        try {
            if (CoreIOUtils.isPathHttp(url)) {
                xml = NutsWorkspaceUtils.of(ws).openURL(url);
            } else {
                File file = new File(url);
                if (file.isFile()) {
                    xml = Files.newInputStream(file.toPath());
                } else {
                    return depsAndRepos;
                }
            }
//            List<String> dependenciesList = new ArrayList<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xml);
            Element c = doc.getDocumentElement();
            for (int i = 0; i < c.getChildNodes().getLength(); i++) {
                if (c.getChildNodes().item(i) instanceof Element && c.getChildNodes().item(i).getNodeName().equals("dependencies")) {
                    Element c2 = (Element) c.getChildNodes().item(i);
                    for (int j = 0; j < c2.getChildNodes().getLength(); j++) {
                        if (c2.getChildNodes().item(j) instanceof Element && c2.getChildNodes().item(j).getNodeName().equals("dependency")) {
                            Element c3 = (Element) c2.getChildNodes().item(j);
                            String groupId = null;
                            String artifactId = null;
                            String version = null;
                            String scope = null;
                            for (int k = 0; k < c3.getChildNodes().getLength(); k++) {
                                if (c3.getChildNodes().item(k) instanceof Element) {
                                    Element c4 = (Element) c3.getChildNodes().item(k);
                                    switch (c4.getNodeName()) {
                                        case "groupId": {
                                            groupId = c4.getTextContent().trim();
                                            break;
                                        }
                                        case "artifactId": {
                                            artifactId = c4.getTextContent().trim();
                                            break;
                                        }
                                        case "version": {
                                            version = c4.getTextContent().trim();
                                            break;
                                        }
                                        case "scope": {
                                            scope = c4.getTextContent().trim();
                                            break;
                                        }
                                    }
                                }
                            }
                            if (CoreStringUtils.isBlank(groupId)) {
                                throw new NutsIllegalArgumentException(ws, "unexpected empty groupId");
                            } else if (groupId.contains("$")) {
                                throw new NutsIllegalArgumentException(ws, "unexpected maven variable in groupId=" + groupId);
                            }
                            if (CoreStringUtils.isBlank(artifactId)) {
                                throw new NutsIllegalArgumentException(ws, "unexpected empty artifactId");
                            } else if (artifactId.contains("$")) {
                                throw new NutsIllegalArgumentException(ws, "unexpected maven variable in artifactId=" + artifactId);
                            }
                            if (CoreStringUtils.isBlank(version)) {
                                throw new NutsIllegalArgumentException(ws, "unexpected empty artifactId");
                            } else if (version.contains("$")) {
                                throw new NutsIllegalArgumentException(ws, "unexpected maven variable in artifactId=" + version);
                            }
                            //this is maven dependency, using "compile"
                            if (CoreStringUtils.isBlank(scope) || scope.equals("compile")) {
                                depsAndRepos.deps.add(groupId + ":" + artifactId + "#" + version);
                            } else if (version.contains("$")) {
                                throw new NutsIllegalArgumentException(ws, "unexpected maven variable in artifactId=" + version);
                            }
                        }
                    }
                } else if (c.getChildNodes().item(i) instanceof Element && c.getChildNodes().item(i).getNodeName().equals("properties")) {
                    Element c2 = (Element) c.getChildNodes().item(i);
                    for (int j = 0; j < c2.getChildNodes().getLength(); j++) {
                        if (c2.getChildNodes().item(j) instanceof Element) {
                            Element c3 = (Element) c2.getChildNodes().item(j);
                            switch (c3.getNodeName()) {
                                case "nuts-runtime-repositories": {
                                    String t = c3.getTextContent().trim();
                                    if (t.length() > 0) {
                                        depsAndRepos.deps.addAll(CoreStringUtils.split(t, ";", true));
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            LOG.with().session(session).level(Level.SEVERE).error(ex)
                    .log("failed to loadDependenciesAndRepositoriesFromPomUrl {0} : {1}", url, ex);
            //ignore
        } finally {
            if (xml != null) {
                try {
                    xml.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
        }

        return depsAndRepos;
    }

    /**
     * find latest maven component
     *
     * @param zId id
     * @param filter filter
     * @param session session
     * @return latest runtime version
     */
    public NutsId resolveLatestMavenId(NutsId zId, Predicate<String> filter, NutsSession session) {
        String path = zId.getGroupId().replace('.', '/') + '/' + zId.getArtifactId();
        String bestVersion = null;
//        if (!NO_M2) {
            File mavenNutsCoreFolder = new File(System.getProperty("user.home"), ".m2/repository/" + path + "/".replace("/", File.separator));
            if (mavenNutsCoreFolder.isDirectory()) {
                File[] children = mavenNutsCoreFolder.listFiles();
                if (children != null) {
                    for (File file : children) {
                        if (file.isDirectory()) {
                            String[] goodChildren = file.list(new FilenameFilter() {
                                @Override
                                public boolean accept(File dir, String name) {
                                    return name.endsWith(".pom");
                                }
                            });
                            if (goodChildren != null && goodChildren.length > 0) {
                                String p = file.getName();
                                if (filter==null || filter.test(p)) {
                                    if (bestVersion == null || DefaultNutsVersion.compareVersions(bestVersion, p) < 0) {
                                        bestVersion = p;
                                    }
                                }
                            }
                        }
                    }
                }
            }
//        }
        for (String repoUrl : new String[]{
//                NutsConstants.BootstrapURLs.REMOTE_MAVEN_GIT,
                NutsConstants.BootstrapURLs.REMOTE_MAVEN_CENTRAL
        }) {
            if (!repoUrl.endsWith("/")) {
                repoUrl = repoUrl + "/";
            }
            boolean found = false;
            String mavenMetadataXml = repoUrl + path + "/maven-metadata.xml";
            try {
                URL runtimeMetadata = new URL(mavenMetadataXml);
                found = true;
                DocumentBuilderFactory factory
                        = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(NutsWorkspaceUtils.of(ws).openURL(runtimeMetadata));
                Element c = doc.getDocumentElement();
                for (int i = 0; i < c.getChildNodes().getLength(); i++) {
                    if (c.getChildNodes().item(i) instanceof Element && c.getChildNodes().item(i).getNodeName().equals("versioning")) {
                        Element c2 = (Element) c.getChildNodes().item(i);
                        for (int j = 0; j < c2.getChildNodes().getLength(); j++) {
                            if (c2.getChildNodes().item(j) instanceof Element && c2.getChildNodes().item(j).getNodeName().equals("versions")) {
                                Element c3 = (Element) c2.getChildNodes().item(j);
                                for (int k = 0; k < c3.getChildNodes().getLength(); k++) {
                                    if (c3.getChildNodes().item(k) instanceof Element && c3.getChildNodes().item(k).getNodeName().equals("version")) {
                                        Element c4 = (Element) c3.getChildNodes().item(k);
                                        String p = c4.getTextContent();
                                        if (filter==null || filter.test(p)) {
                                            if (bestVersion == null || DefaultNutsVersion.compareVersions(bestVersion, p) < 0) {
                                                bestVersion = p;
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                    //NutsConstants.Ids.NUTS_RUNTIME.replaceAll("[.:]", "/")
                }
            } catch (Exception ex) {
                LOG.with().session(session).level(Level.SEVERE).error(ex)
                        .log("failed to load and parse {0} : {1}", mavenMetadataXml,ex);
                // ignore any error
            }
            if (found) {
                break;
            }
        }
        if (bestVersion == null) {
            return null;
        }
        return zId.builder().setVersion(bestVersion).build();
    }

    public static class DepsAndRepos {

        public LinkedHashSet<String> deps = new LinkedHashSet<>();
        public LinkedHashSet<String> repos = new LinkedHashSet<>();
    }


    private static class NutsPomLogger implements PomLogger {
        private final NutsSession session;
        NutsLogger LOG;

        public NutsPomLogger(NutsSession session) {
            this.session = session;
            LOG = session.getWorkspace().log().of(PomIdResolver.class);
        }

        @Override
        public void log(Level level, String msg, Object... params) {
            LOG.with().session(session)
                    .level(Level.FINE)
                    .verb(NutsLogVerb.FAIL)
                    .log(msg, params);
        }

        @Override
        public void log(Level level, String msg, Throwable throwable) {
            LOG.with().session(session)
                    .level(Level.FINE)
                    .verb(NutsLogVerb.FAIL)
                    .log("%s",msg);
        }
    }

    private static class NutsPomUrlReader implements PomUrlReader {
        private final NutsSession session;

        public NutsPomUrlReader(NutsSession session) {
            this.session = session;
        }

        @Override
        public InputStream openStream(URL url) {
            return NutsWorkspaceUtils.of(session.getWorkspace()).openURL(url);
        }
    }
}
