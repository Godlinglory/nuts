package net.vpc.app.nuts.runtime.util;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.DefaultNutsVersion;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;

public class NutsJavaSdkUtils {
    private final NutsLogger LOG;

    private NutsWorkspace ws;

    public static NutsJavaSdkUtils of(NutsWorkspace ws) {
        Map<String, Object> up = ws.userProperties();
        NutsJavaSdkUtils wp = (NutsJavaSdkUtils) up.get(NutsJavaSdkUtils.class.getName());
        if (wp == null) {
            wp = new NutsJavaSdkUtils(ws);
            up.put(NutsJavaSdkUtils.class.getName(), wp);
        }
        return wp;
    }

    private NutsJavaSdkUtils(NutsWorkspace ws) {
        this.ws = ws;
        LOG = ws.log().of(NutsJavaSdkUtils.class);
    }

    public NutsSdkLocation resolveJdkLocation(String requestedJavaVersion, NutsSession session) {
        requestedJavaVersion = CoreStringUtils.trim(requestedJavaVersion);
        NutsSdkLocation bestJava = ws.config().getSdk("java", requestedJavaVersion, session);
        if (bestJava == null) {
            String appSuffix = ws.config().getOsFamily() == NutsOsFamily.WINDOWS ? ".exe" : "";
            String packaging = "jre";
            if (new File(System.getProperty("java.home"), "bin" + File.separator + "javac" + (appSuffix)).isFile()) {
                packaging = "jdk";
            }
            String product = "JDK";
            NutsSdkLocation current = new NutsSdkLocation(
                    ws.config().getPlatform(),
                    product,
                    product + "-" + System.getProperty("java.version"),
                    System.getProperty("java.home"),
                    System.getProperty("java.version"),
                    packaging
            );
            NutsVersionFilter requestedJavaVersionFilter = ws.version().parse(requestedJavaVersion).filter();
            if (requestedJavaVersionFilter == null || requestedJavaVersionFilter.accept(DefaultNutsVersion.valueOf(current.getVersion()), ws.createSession())) {
                bestJava = current;
            }
            if (bestJava == null) {
                if (!CoreStringUtils.isBlank(requestedJavaVersion)) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.with().level(Level.FINE).verb(NutsLogVerb.WARNING).log("No valid JRE found. recommended {0} . Using default java.home at {1}", requestedJavaVersion, System.getProperty("java.home"));
                    }
                } else {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.with().level(Level.FINE).verb(NutsLogVerb.WARNING).log( "No valid JRE found. Using default java.home at {0}", System.getProperty("java.home"));
                    }
                }
                bestJava = current;
            }
        }
        return bestJava;
    }

    public NutsSdkLocation[] searchJdkLocations(NutsSession session) {
        String[] conf = {};
        switch (session.getWorkspace().config().getOsFamily()) {
            case LINUX:
            case UNIX:
            case UNKNOWN: {
                conf = new String[]{"/usr/java", "/usr/lib64/jvm", "/usr/lib/jvm"};
                break;
            }
            case WINDOWS: {
                conf = new String[]{CoreStringUtils.coalesce(System.getenv("ProgramFiles"), "C:\\Program Files") + "\\Java", CoreStringUtils.coalesce(System.getenv("ProgramFiles(x86)"), "C:\\Program Files (x86)") + "\\Java"};
                break;
            }
            case MACOS: {
                conf = new String[]{"/Library/Java/JavaVirtualMachines", "/System/Library/Frameworks/JavaVM.framework"};
                break;
            }
        }
        List<NutsSdkLocation> all = new ArrayList<>();
        for (String s : conf) {
            all.addAll(Arrays.asList(searchJdkLocations(Paths.get(s), session)));
        }
        return all.toArray(new NutsSdkLocation[0]);
    }

    public Future<NutsSdkLocation[]> searchJdkLocationsFuture(NutsSession session) {
        String[] conf = {};
        switch (ws.config().getOsFamily()) {
            case LINUX:
            case UNIX:
            case UNKNOWN: {
                conf = new String[]{"/usr/java", "/usr/lib64/jvm", "/usr/lib/jvm"};
                break;
            }
            case WINDOWS: {
                conf = new String[]{CoreStringUtils.coalesce(System.getenv("ProgramFiles"), "C:\\Program Files") + "\\Java", CoreStringUtils.coalesce(System.getenv("ProgramFiles(x86)"), "C:\\Program Files (x86)") + "\\Java"};
                break;
            }
            case MACOS: {
                conf = new String[]{"/Library/Java/JavaVirtualMachines", "/System/Library/Frameworks/JavaVM.framework"};
                break;
            }
        }
        List<Future<NutsSdkLocation[]>> all = new ArrayList<>();
        for (String s : conf) {
            all.add(searchJdkLocationsFuture(Paths.get(s), session));
        }
        return session.getWorkspace().io().executorService().submit(new Callable<NutsSdkLocation[]>() {
            @Override
            public NutsSdkLocation[] call() throws Exception {
                List<NutsSdkLocation> locs = new ArrayList<>();
                for (Future<NutsSdkLocation[]> nutsSdkLocationFuture : all) {
                    NutsSdkLocation[] e = nutsSdkLocationFuture.get();
                    if(e!=null) {
                        locs.addAll(Arrays.asList(e));
                    }
                }
                return locs.toArray(new NutsSdkLocation[0]);
            }
        });
    }

    public NutsSdkLocation[] searchJdkLocations(Path s, NutsSession session) {
        List<NutsSdkLocation> all = new ArrayList<>();
        if (Files.isDirectory(s)) {
            try (final DirectoryStream<Path> it = Files.newDirectoryStream(s)) {
                for (Path d : it) {
                    NutsSdkLocation r = resolveJdkLocation(d, null, session);
                    if (r != null) {
                        all.add(r);
                        if (session != null && session.isPlainTrace()) {
                            session.out().printf("detected java %s [[%s]] at ==%s==%n", r.getPackaging(), r.getVersion(), r.getPath());
                        }
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return all.toArray(new NutsSdkLocation[0]);
    }

    public Future<NutsSdkLocation[]> searchJdkLocationsFuture(Path s, NutsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }
        List<Future<NutsSdkLocation>> all = new ArrayList<>();
        if (Files.isDirectory(s)) {
            try (final DirectoryStream<Path> it = Files.newDirectoryStream(s)) {
                for (Path d : it) {
                    all.add(
                            session.getWorkspace().io().executorService().submit(new Callable<NutsSdkLocation>() {
                                @Override
                                public NutsSdkLocation call() throws Exception {
                                    NutsSdkLocation r=null;
                                    try {
                                        r = resolveJdkLocation(d, null, session);
                                        if(r!=null) {
                                            if (session.isPlainTrace()) {
                                                synchronized (session.getWorkspace()) {
                                                    session.out().printf("detected java %s [[%s]] at ==%s==%n", r.getPackaging(), r.getVersion(), r.getPath());
                                                }
                                            }
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    return r;
                                }
                            })
                    );
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return session.getWorkspace().io().executorService().submit(new Callable<NutsSdkLocation[]>() {
            @Override
            public NutsSdkLocation[] call() throws Exception {
                List<NutsSdkLocation> locs = new ArrayList<>();
                for (Future<NutsSdkLocation> nutsSdkLocationFuture : all) {
                    NutsSdkLocation e = nutsSdkLocationFuture.get();
                    if (e != null) {
                        locs.add(e);
                    }
                }
                return locs.toArray(new NutsSdkLocation[0]);
            }
        });
    }

    public NutsSdkLocation resolveJdkLocation(Path path, String preferredName, NutsSession session) {
        if (path == null) {
            throw new NutsException(session.getWorkspace(), "Missing path");
        }
        String appSuffix = session.getWorkspace().config().getOsFamily() == NutsOsFamily.WINDOWS ? ".exe" : "";
        Path bin = path.resolve("bin");
        Path javaExePath = bin.resolve("java" + appSuffix);
        if (!Files.isRegularFile(javaExePath)) {
            return null;
        }
        String product = null;
        String jdkVersion = null;
        String cmdOutputString=null;
        int cmdRresult=0;
        boolean loggedError=false;
        try {
            NutsExecCommand cmd = session.getWorkspace().exec().userCmd().addCommand(javaExePath.toString(), "-version")
                    .setRedirectErrorStream(true)
                    .grabOutputString().setFailFast(true).run();
            cmdRresult = cmd.getResult();
            cmdOutputString = cmd.getOutputString();
            if (cmdOutputString.length() > 0) {
                String prefix = "java version \"";
                int i = cmdOutputString.indexOf(prefix);
                if (i >= 0) {
                    i = i + prefix.length();
                    int j = cmdOutputString.indexOf("\"", i);
                    if (i >= 0) {
                        jdkVersion = cmdOutputString.substring(i, j);
                        product = "JDK";
                    }
                }
                if (jdkVersion == null) {

                    prefix = "openjdk version \"";
                    i = cmdOutputString.indexOf(prefix);
                    if (i >= 0) {
                        i = i + prefix.length();
                        int j = cmdOutputString.indexOf("\"", i);
                        if (i > 0) {
                            jdkVersion = cmdOutputString.substring(i, j);
                            product = "OpenJDK";
                        }
                    }
                }
            }
        } catch (Exception ex) {
            loggedError=true;
            LOG.with().error(ex).level(Level.SEVERE).verb(NutsLogVerb.WARNING).log("Unable to execute {0}. JDK Home ignored", javaExePath);
        }
        if (jdkVersion == null) {
            if(!loggedError) {
                LOG.with().level(Level.SEVERE).verb(NutsLogVerb.WARNING).log("Execute {0} failed with result code {1} and result string \"{2}\". JDK Home ignored", javaExePath.toString(),cmdRresult,cmdOutputString);
            }
            return null;
        }
        String packaging = "jre";
        if (Files.isRegularFile(bin.resolve("javac" + appSuffix)) && Files.isRegularFile(bin.resolve("jps" + appSuffix))) {
            packaging = "jdk";
        }
        if (CoreStringUtils.isBlank(preferredName)) {
            preferredName = product + "-" + jdkVersion;
        } else {
            preferredName = CoreStringUtils.trim(preferredName);
        }
        return new NutsSdkLocation(
                NutsWorkspaceUtils.of(ws).createSdkId("java", jdkVersion),
                product,
                preferredName,
                path.toString(),
                jdkVersion,
                packaging
        );
    }

    public NutsId createJdkId(String version) {
        if (CoreStringUtils.isBlank(version)) {
            throw new NutsException(ws, "Missing version");
        }
        NutsVersion jv = ws.version().parse(version);
        int n1 = jv.getNumber(0, 0);
        int n2 = jv.getNumber(1, 0);
        int classFileId = 0;
        String standard = n1 + "." + n2;
        if (n1 == 1) {
            classFileId = 44 + n2;
        }
        if (classFileId == 0) {
            classFileId = 52;
        }
        return ws.id().builder().artifactId("java")
                .property("s", standard)
                .property("c", String.valueOf(classFileId))
                .version(version)
                .build();
    }

    public String resolveJavaCommandByVersion(String requestedJavaVersion, boolean javaw, NutsSession session) {
        String bestJavaPath = resolveJdkLocation(requestedJavaVersion, session).getPath();
        if (bestJavaPath.contains("/") || bestJavaPath.contains("\\") || bestJavaPath.equals(".") || bestJavaPath.equals("..")) {
            Path file = ws.config().getWorkspaceLocation().resolve(bestJavaPath);
            if (Files.isDirectory(file) && Files.isDirectory(file.resolve("bin"))) {
                boolean winOs = ws.config().getOsFamily() == NutsOsFamily.WINDOWS;
                if (winOs) {
                    if (javaw) {
                        bestJavaPath = file.resolve("bin" + File.separatorChar + "javaw.exe").toString();
                    } else {
                        bestJavaPath = file.resolve("bin" + File.separatorChar + "java.exe").toString();
                    }
                } else {
                    bestJavaPath = file.resolve("bin" + File.separatorChar + "java").toString();
                }
            }
        }
        return bestJavaPath;
    }

    public String resolveJavaCommandByHome(String javaHome) {
        String appSuffix = ws.config().getOsFamily() == NutsOsFamily.WINDOWS ? ".exe" : "";
        String exe = "java" + appSuffix;
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
            if (CoreStringUtils.isBlank(javaHome) || "null".equals(javaHome)) {
                //this may happen is using a precompiled image (such as with graalvm)
                return exe;
            }
        }
        return javaHome + File.separator + "bin" + File.separator + exe;
    }
}
