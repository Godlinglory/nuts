package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.config.NutsSdkLocationComparator;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class NutsJavaSdkUtils {

    private final NutsWorkspace ws;
    private NutsLogger LOG;

    private NutsJavaSdkUtils(NutsWorkspace ws) {
        this.ws = ws;
    }

    public static NutsJavaSdkUtils of(NutsSession session) {
        return of(session.getWorkspace());
    }

    public static NutsJavaSdkUtils of(NutsWorkspace ws) {
        return new NutsJavaSdkUtils(ws);
//        NutsJavaSdkUtils wp = (NutsJavaSdkUtils) ws.env().getProperty(NutsJavaSdkUtils.class.getName());
//        if (wp == null) {
//            wp = new NutsJavaSdkUtils(ws);
//            ws.env().setProperty(NutsJavaSdkUtils.class.getName(), wp);
//        }
//        return wp;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(NutsJavaSdkUtils.class, session);
        }
        return LOG;
    }

    public NutsPlatformLocation resolveJdkLocation(String requestedJavaVersion, NutsSession session) {
        requestedJavaVersion = NutsUtilStrings.trim(requestedJavaVersion);
        NutsPlatformLocation bestJava = session.env().platforms()
                .findPlatformByVersion(NutsPlatformType.JAVA,
                        NutsVersionFilters.of(session).byValue(requestedJavaVersion)
                );
        if (bestJava == null) {
            String appSuffix = session.env().getOsFamily() == NutsOsFamily.WINDOWS ? ".exe" : "";
            String packaging = "jre";
            if (new File(System.getProperty("java.home"), "bin" + File.separator + "javac" + (appSuffix)).isFile()) {
                packaging = "jdk";
            }
            String product = "JDK";
            NutsPlatformLocation current = new NutsPlatformLocation(
                    session.env().getPlatform(),
                    product,
                    product + "-" + System.getProperty("java.version"),
                    System.getProperty("java.home"),
                    System.getProperty("java.version"),
                    packaging,
                    0
            );
            current.setConfigVersion(DefaultNutsWorkspace.VERSION_SDK_LOCATION);
            NutsVersionFilter requestedJavaVersionFilter = NutsVersion.of(requestedJavaVersion, session).filter();
            if (requestedJavaVersionFilter == null || requestedJavaVersionFilter.acceptVersion(NutsVersion.of(current.getVersion(), session), session)) {
                bestJava = current;
            }
            if (bestJava == null) {
                if (!NutsBlankable.isBlank(requestedJavaVersion)) {
                    _LOGOP(session).level(Level.FINE).verb(NutsLogVerb.WARNING)
                            .log(NutsMessage.jstyle("no valid JRE found. recommended {0} . Using default java.home at {1}", requestedJavaVersion, System.getProperty("java.home")));
                } else {
                    _LOGOP(session).level(Level.FINE).verb(NutsLogVerb.WARNING)
                            .log(NutsMessage.jstyle("no valid JRE found. Using default java.home at {0}", System.getProperty("java.home")));
                }
                bestJava = current;
            }
        }
        return bestJava;
    }

    public NutsPlatformLocation[] searchJdkLocations(NutsSession session) {
        String[] conf = {};
        switch (session.env().getOsFamily()) {
            case LINUX:
            case UNIX:
            case UNKNOWN: {
                conf = new String[]{"/usr/java", "/usr/lib64/jvm", "/usr/lib/jvm"};
                break;
            }
            case WINDOWS: {
                conf = new String[]{
                        NutsUtilPlatforms.getWindowsProgramFiles() + "\\Java",
                        NutsUtilPlatforms.getWindowsProgramFilesX86() + "\\Java"
                };
                break;
            }
            case MACOS: {
                conf = new String[]{"/Library/Java/JavaVirtualMachines", "/System/Library/Frameworks/JavaVM.framework"};
                break;
            }
        }
        List<NutsPlatformLocation> all = new ArrayList<>();
        for (String s : conf) {
            all.addAll(Arrays.asList(searchJdkLocations(s, session)));
        }
        return all.toArray(new NutsPlatformLocation[0]);
    }

    public Future<NutsPlatformLocation[]> searchJdkLocationsFuture(NutsSession session) {
        LinkedHashSet<String> conf = new LinkedHashSet<>();
        File file = new File(System.getProperty("java.home"));
        try {
            file = file.getCanonicalFile();
        } catch (IOException ex) {
            //
        }

        List<Future<NutsPlatformLocation[]>> all = new ArrayList<>();
        NutsPlatformLocation base = resolveJdkLocation(file.getPath(), null, session);
        if (base != null) {
            all.add(CompletableFuture.completedFuture(new NutsPlatformLocation[]{base}));
        }
        switch (session.env().getOsFamily()) {
            case LINUX:
            case UNIX:
            case UNKNOWN: {
                conf.addAll(Arrays.asList("/usr/java", "/usr/lib64/jvm", "/usr/lib/jvm"));
                break;
            }
            case WINDOWS: {
                conf.addAll(Arrays.asList(
                        NutsUtilPlatforms.getWindowsProgramFiles() + "\\Java",
                        NutsUtilPlatforms.getWindowsProgramFilesX86() + "\\Java"
                ));
                break;
            }
            case MACOS: {
                conf.addAll(Arrays.asList("/Library/Java/JavaVirtualMachines", "/System/Library/Frameworks/JavaVM.framework"));
                break;
            }
        }
        for (String s : conf) {
            all.add(searchJdkLocationsFuture(Paths.get(s), session));
        }
        return session.config().executorService().submit(new Callable<NutsPlatformLocation[]>() {
            @Override
            public NutsPlatformLocation[] call() throws Exception {
                List<NutsPlatformLocation> locs = new ArrayList<>();
                for (Future<NutsPlatformLocation[]> nutsSdkLocationFuture : all) {
                    NutsPlatformLocation[] e = nutsSdkLocationFuture.get();
                    if (e != null) {
                        locs.addAll(Arrays.asList(e));
                    }
                }
                locs.sort(new NutsSdkLocationComparator(session));
                return locs.toArray(new NutsPlatformLocation[0]);
            }
        });
    }

    public NutsPlatformLocation[] searchJdkLocations(String loc, NutsSession session) {
        Path s = Paths.get(loc);
        List<NutsPlatformLocation> all = new ArrayList<>();
        if (Files.isDirectory(s)) {
            try (final DirectoryStream<Path> it = Files.newDirectoryStream(s)) {
                for (Path d : it) {
                    NutsPlatformLocation r = resolveJdkLocation(d.toString(), null, session);
                    if (r != null) {
                        all.add(r);
                        if (session != null && session.isPlainTrace()) {
                            NutsTexts factory = NutsTexts.of(session);
                            session.out().printf("detected java %s %s at %s%n", r.getPackaging(),
                                    factory.ofStyled(r.getVersion(), NutsTextStyle.version()),
                                    factory.ofStyled(r.getPath(), NutsTextStyle.path())
                            );
                        }
                    }
                }
            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
        }
        all.sort(new NutsSdkLocationComparator(session));
        return all.toArray(new NutsPlatformLocation[0]);
    }

    public Future<NutsPlatformLocation[]> searchJdkLocationsFuture(Path s, NutsSession session) {
        List<Future<NutsPlatformLocation>> all = new ArrayList<>();
        if (s == null) {
            return CompletableFuture.completedFuture(new NutsPlatformLocation[0]);
        } else if (Files.isDirectory(s)) {
            try (final DirectoryStream<Path> it = Files.newDirectoryStream(s)) {
                for (Path d : it) {
                    all.add(
                            session.config().executorService().submit(new Callable<NutsPlatformLocation>() {
                                @Override
                                public NutsPlatformLocation call() throws Exception {
                                    NutsPlatformLocation r = null;
                                    try {
                                        r = resolveJdkLocation(d.toString(), null, session);
                                        if (r != null) {
                                            if (session.isPlainTrace()) {
                                                synchronized (session.getWorkspace()) {
                                                    NutsTexts factory = NutsTexts.of(session);
                                                    session.out().printf("detected java %s %s at %s%n", r.getPackaging(),
                                                            factory.ofStyled(r.getVersion(), NutsTextStyle.version()),
                                                            factory.ofStyled(r.getPath(), NutsTextStyle.path())
                                                    );
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
                throw new NutsIOException(session, ex);
            }
        }
        return session.config().executorService().submit(new Callable<NutsPlatformLocation[]>() {
            @Override
            public NutsPlatformLocation[] call() throws Exception {
                List<NutsPlatformLocation> locs = new ArrayList<>();
                for (Future<NutsPlatformLocation> nutsSdkLocationFuture : all) {
                    NutsPlatformLocation e = nutsSdkLocationFuture.get();
                    if (e != null) {
                        locs.add(e);
                    }
                }
                return locs.toArray(new NutsPlatformLocation[0]);
            }
        });
    }

    public NutsPlatformLocation resolveJdkLocation(String path, String preferredName, NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        if (path == null) {
            throw new NutsException(session, NutsMessage.formatted("missing path"));
        }
        String appSuffix = session.env().getOsFamily() == NutsOsFamily.WINDOWS ? ".exe" : "";
        Path bin = Paths.get(path).resolve("bin");
        Path javaExePath = bin.resolve("java" + appSuffix);
        if (!Files.isRegularFile(javaExePath)) {
            return null;
        }
        String product = null;
        String jdkVersion = null;
        String cmdOutputString = null;
        int cmdRresult = 0;
        boolean loggedError = false;
        try {
            //I do not know why but sometimes, the process exists before receiving stdout result!!
            final int MAX_ITER = 5;
            for (int i = 0; i < MAX_ITER; i++) {
                NutsExecCommand cmd = session.exec()
                        .setSession(session)
                        .setExecutionType(NutsExecutionType.SYSTEM)
                        .addCommand(javaExePath.toString(), "-version")
                        .setRedirectErrorStream(true)
                        .grabOutputString().setFailFast(true).run();
                cmdRresult = cmd.getResult();
                cmdOutputString = cmd.getOutputString();
                if (cmdOutputString.length() > 0) {
                    break;
                } else {
                    _LOGOP(session).level(i == (MAX_ITER - 1) ? Level.WARNING : Level.FINER).verb(NutsLogVerb.WARNING)
                            .log(NutsMessage.jstyle("unable to execute {0}. returned empty string ({1}/{2})", javaExePath, i + 1, MAX_ITER));
                }
            }
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
            loggedError = true;
            _LOGOP(session).error(ex).level(Level.SEVERE).verb(NutsLogVerb.WARNING)
                    .log(NutsMessage.jstyle("unable to execute {0}. JDK Home ignored", javaExePath));
        }
        if (jdkVersion == null) {
            if (!loggedError) {
                _LOGOP(session).level(Level.SEVERE).verb(NutsLogVerb.WARNING)
                        .log(NutsMessage.jstyle("execute {0} failed with result code {1} and result string \"{2}\". JDK Home ignored", javaExePath.toString(), cmdRresult, cmdOutputString));
            }
            return null;
        }
        String packaging = "jre";
        if (Files.isRegularFile(bin.resolve("javac" + appSuffix)) && Files.isRegularFile(bin.resolve("jps" + appSuffix))) {
            packaging = "jdk";
        }
        if (NutsBlankable.isBlank(preferredName)) {
            preferredName = product + "-" + jdkVersion;
        } else {
            preferredName = NutsUtilStrings.trim(preferredName);
        }
        NutsPlatformLocation r = new NutsPlatformLocation(
                NutsWorkspaceUtils.of(session).createSdkId("java", jdkVersion),
                product,
                preferredName,
                path,
                jdkVersion,
                packaging,
                0
        );
        r.setConfigVersion(DefaultNutsWorkspace.VERSION_SDK_LOCATION);
        return r;
    }

    public NutsId createJdkId(String version, NutsSession session) {
        if (NutsBlankable.isBlank(version)) {
            throw new NutsException(session, NutsMessage.formatted("missing version"));
        }
        NutsVersion jv = NutsVersion.of(version, session);
        long n1 = jv.getNumber(0, BigInteger.ZERO).longValue();
        long n2 = jv.getNumber(1, BigInteger.ZERO).longValue();
        long classFileId = 0;
        String standard = n1 + "." + n2;
        if (n1 == 1) {
            classFileId = 44 + n2;
        }
        if (classFileId == 0) {
            classFileId = 52;
        }
        return NutsIdBuilder.of(session).setArtifactId("java")
                .setProperty("s", standard)
                .setProperty("c", String.valueOf(classFileId))
                .setVersion(version)
                .build();
    }

    public String resolveJavaCommandByVersion(String requestedJavaVersion, boolean javaw, NutsSession session) {
        String bestJavaPath = resolveJdkLocation(requestedJavaVersion, session).getPath();
        //if (bestJavaPath.contains("/") || bestJavaPath.contains("\\") || bestJavaPath.equals(".") || bestJavaPath.equals("..")) {
        NutsPath file = NutsPath.of(bestJavaPath, session).toAbsolute(session.locations().getWorkspaceLocation());
        //if (file.isDirectory() && file.resolve("bin").isDirectory()) {
        boolean winOs = session.env().getOsFamily() == NutsOsFamily.WINDOWS;
        if (winOs) {
            if (javaw) {
                bestJavaPath = file.resolve("bin").resolve("javaw.exe").toString();
            } else {
                bestJavaPath = file.resolve("bin").resolve("java.exe").toString();
            }
        } else {
            bestJavaPath = file.resolve("bin").resolve("java").toString();
        }
        //}
        //}
        return bestJavaPath;
    }

    public String resolveJavaCommandByHome(String javaHome, NutsSession session) {
        String appSuffix = session.env().getOsFamily() == NutsOsFamily.WINDOWS ? ".exe" : "";
        String exe = "java" + appSuffix;
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
            if (NutsBlankable.isBlank(javaHome) || "null".equals(javaHome)) {
                //this may happen is using a precompiled image (such as with graalvm)
                return exe;
            }
        }
        return javaHome + File.separator + "bin" + File.separator + exe;
    }
}
