package net.thevpc.nuts.runtime.bundles.mvn;

import net.thevpc.nuts.NutsSession;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PomIdResolver {

    //    private NutsWorkspace ws;
    private PomUrlReader pomUrlReader;
    private PomLogger logger;

    public PomIdResolver(PomUrlReader pomUrlReader, PomLogger logger) {
        this.pomUrlReader = pomUrlReader;
        if (this.pomUrlReader == null) {
            this.pomUrlReader = PomUrlReader.DEFAULT;
        }
        this.logger = logger;
        if (this.logger == null) {
            this.logger = PomLogger.DEFAULT;
        }
    }

    public PomId[] resolvePomId(URL baseUrl, String referenceResourcePath, NutsSession session) {
        List<PomId> all = new ArrayList<PomId>();
        final URLParts aa = new URLParts(baseUrl);
        String basePath = aa.getLastPart().getPath().substring(0, aa.getLastPart().getPath().length() - referenceResourcePath.length());
        if (!basePath.endsWith("/")) {
            basePath += "/";
        }

        final URLParts p = aa.getParent().append(basePath + "META-INF/maven");
        int beforeSize = all.size();
        URL[] children = new URL[0];
        try {
            children = p.getChildren(false, true, new MyURLFilter());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (URL url : children) {
            if (url != null) {
                Properties prop = new Properties();
                try {
                    prop.load(url.openStream());
                } catch (IOException e) {
                    //
                }
                String version = prop.getProperty("version");
                String groupId = prop.getProperty("groupId");
                String artifactId = prop.getProperty("artifactId");
                if (version != null && version.trim().length() != 0) {
                    all.add(new PomId(groupId, artifactId, version));
                }
            }
        }
        if (beforeSize == all.size()) {
            //no found !
            if (basePath.endsWith("/target/classes/")) {
                String s2 = basePath.substring(0, basePath.length() - "/target/classes/".length()) + "/pom.xml";
                //this is most likely to be a maven project
                try {
                    all.add(new PomXmlParser(logger).parse(new URL(s2), session).getPomId());
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "failed to parse pom file {0} : {1}", s2, ex);
                }
            }
        }
        return all.toArray(new PomId[0]);
    }

    /**
     * resolve all Maven/Nuts artifact definitions in the classloader that has
     * loaded <code>clazz</code>
     *
     * @param clazz class
     * @param session session
     * @return artifacts array in the form groupId:artfcatId#version
     */
    public PomId[] resolvePomIds(Class clazz, NutsSession session) {
        List<PomId> all = new ArrayList<PomId>();
        try {
            final String n = clazz.getName().replace('.', '/').concat(".class");
            final Enumeration<URL> r = clazz.getClassLoader().getResources(n);
            for (URL url : Collections.list(r)) {
                all.addAll(Arrays.asList(resolvePomId(url, n, session)));
            }
        } catch (IOException ex) {
            logger.log(Level.FINE, "error : {0}", ex);
        }
        return all.toArray(new PomId[0]);
    }

    public PomId resolvePomId(Class clazz, NutsSession session) {
        return resolvePomId(clazz, new PomId("dev", "dev", "dev"), session);
    }

    public PomId resolvePomId(Class clazz, PomId defaultValue, NutsSession session) {
        PomId[] pomIds = resolvePomIds(clazz, session);
        if (pomIds.length > 1) {
            if (logger != null) {
                logger.log(Level.INFO, "multiple ids found : "+Arrays.asList(pomIds)+" for class "+clazz+" and id "+defaultValue);
            }
        }
        for (PomId v : pomIds) {
            return v;
        }
        return defaultValue;
    }

    public PomId resolvePomId(Class clazz, String groupId, String artifactId, String defaultValue) {
        String ver = resolvePomVersion(clazz, groupId, artifactId, defaultValue);
        return new PomId(
                groupId, artifactId, ver
        );
    }

    public String resolvePomVersion(String groupId, String artifactId, String defaultValue) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties");

        if (url != null) {
            Properties p = new Properties();
            try {
                p.load(url.openStream());
            } catch (IOException e) {
                //
            }
            String version = p.getProperty("version");
            if (version != null && version.trim().length() != 0) {
                return version;
            }
        }
        return defaultValue;
    }

    public String resolvePomVersion(Class clazz, String groupId, String artifactId, String defaultValue) {
        URL url = clazz.getClassLoader().getResource("META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties");

        if (url != null) {
//            System.out.println("== " + url);
            Properties p = new Properties();
            try {
                p.load(url.openStream());
            } catch (IOException e) {
                //
            }
            String version = p.getProperty("version");
            if (version != null && version.trim().length() != 0) {
//                System.out.println("\t found!!");
                return version;
            }
//            System.out.println("\t not found");
        }
        return defaultValue;
    }

    public PomId resolvePropertiesPomId(InputStream stream) {
        Properties prop = new Properties();
        try {
            prop.load(stream);
        } catch (IOException e) {
            //
        }
        String version = prop.getProperty("version");
        String groupId = prop.getProperty("groupId");
        String artifactId = prop.getProperty("artifactId");
        if (version != null && version.trim().length() != 0) {
            return new PomId(groupId, artifactId, version);
        }
        return null;
    }

    public PomId[] resolveJarPomIds(InputStream jarStream) throws IOException {
        final List<PomId> list = new ArrayList<>();
        visitZipStream(jarStream, new InputStreamVisitor() {
            @Override
            public boolean visit(String path, InputStream inputStream) {
                if (path.startsWith("META-INF/")
                        && path.endsWith("/pom.properties")) {

                    PomId id = resolvePropertiesPomId(inputStream);
                    if (id != null) {
                        list.add(new PomId(
                                id.getGroupId(),
                                id.getArtifactId(),
                                id.getVersion()
                        ));
                    }
                }
                return true;
            }
        });
        return list.toArray(new PomId[0]);
    }

    public PomId resolveJarPomId(InputStream jarStream) throws IOException {
        PomId[] v = resolveJarPomIds(jarStream);
        if (v.length == 0) {
            return null;
        }
        if (v.length >= 2) {
            throw new IllegalArgumentException("too many Ids");
        }
        return v[0];
    }

    private boolean visitZipStream(InputStream zipFile, InputStreamVisitor visitor) throws IOException {
        //byte[] buffer = new byte[4 * 1024];

        //get the zip file content
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(zipFile);
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            final ZipInputStream finalZis = zis;
            InputStream entryInputStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    return finalZis.read();
                }

                @Override
                public int read(byte[] b) throws IOException {
                    return finalZis.read(b);
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return finalZis.read(b, off, len);
                }

                @Override
                public void close() throws IOException {
                    finalZis.closeEntry();
                }
            };

            while (ze != null) {

                String fileName = ze.getName();
                if (!fileName.endsWith("/")) {
                    if (!visitor.visit(fileName, entryInputStream)) {
                        break;
                    }
                }
                ze = zis.getNextEntry();
            }
        } finally {
            if (zis != null) {
                zis.close();
            }
        }

        return false;
    }

    private interface InputStreamVisitor {

        boolean visit(String path, InputStream inputStream) throws IOException;
    }

    private static class MyURLFilter implements URLFilter {

        public MyURLFilter() {
        }

        @Override
        public boolean accept(URL path) {
            return new URLParts(path).getName().equals("pom.properties");
        }
    }

}
