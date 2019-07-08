/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/15/17.
 *
 * @since 0.5.4
 */
final class PrivateNutsUtils {

    private static final Logger LOG = Logger.getLogger(PrivateNutsUtils.class.getName());
    private static final Pattern DOLLAR_PLACE_HOLDER_PATTERN = Pattern.compile("[$][{](?<name>([a-zA-Z]+))[}]");

    public static boolean isValidWorkspaceName(String workspace) {
        if (isBlank(workspace)) {
            return true;
        }
        String workspaceName = workspace.trim();
        if (workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..")) {
            return true;
        } else {
            return false;
        }
    }

    public static String resolveValidWorkspaceName(String workspace) {
        if (isBlank(workspace)) {
            return NutsConstants.Names.DEFAULT_WORKSPACE_NAME;
        }
        String workspaceName = workspace.trim();
        if (workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..")) {
            return workspaceName;
        } else {
            String p = null;
            try {
                p = new File(workspaceName).getCanonicalFile().getName();
            } catch (IOException ex) {
                p = new File(workspaceName).getAbsoluteFile().getName();
            }
            if (p.isEmpty() || p.equals(".") || p.equals("..")) {
                return "unknown";
            }
            return p;
        }
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static String trim(String str) {
        if (str == null) {
            return "";
        }
        return str.trim();
    }

    public static List<String> split(String str, String separators, boolean trim) {
        if (str == null) {
            return Collections.EMPTY_LIST;
        }
        StringTokenizer st = new StringTokenizer(str, separators);
        List<String> result = new ArrayList<>();
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            if (trim) {
                s = s.trim();
            }
            result.add(s);
        }
        return result;
    }

    public static List<String> split(String str, String separators) {
        if (str == null) {
            return Collections.EMPTY_LIST;
        }
        StringTokenizer st = new StringTokenizer(str, separators);
        List<String> result = new ArrayList<>();
        while (st.hasMoreElements()) {
            result.add(st.nextToken());
        }
        return result;
    }

    public static String getAbsolutePath(String path) {
        return new File(path).toPath().toAbsolutePath().normalize().toString();
    }

    public static String readStringFromURL(URL requestURL) throws IOException {
        File f = toFile(requestURL);
        if (f != null) {
            return new String(Files.readAllBytes(f.toPath()));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(requestURL.openStream(), out, true, true);
        return new String(out.toByteArray());
    }

    public static String readStringFromFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static boolean isAbsolutePath(String location) {
        return new File(location).isAbsolute();
    }

    public static boolean storeProperties(Properties p, File file) {
        Writer writer = null;
        try {
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
            try {
                p.store(writer = new FileWriter(file), null);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
            return true;
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "[ERROR  ] Unable to store {0}", file);
        }
        return false;
    }

    public static Properties loadURLPropertiesFromLocalFile(File file) {

        Properties p = new Properties();
        if (file.isFile()) {
            try (InputStream in = Files.newInputStream(file.toPath())) {
                p.load(in);
            } catch (IOException ex) {
                //ignore...
            }
        }
        return p;
    }

    public static Properties loadURLProperties(URL url, File cacheFile, boolean useCache) {
        long startTime = System.currentTimeMillis();
        Properties props = new Properties();
        InputStream inputStream = null;
        File urlFile = toFile(url);
        try {
            if (useCache) {
                if (cacheFile != null && cacheFile.isFile()) {
                    try {
                        inputStream = new FileInputStream(cacheFile);
                        props.load(inputStream);
                        long time = System.currentTimeMillis() - startTime;
                        LOG.log(Level.CONFIG, "[SUCCESS] Loaded cached file from  {0}" + ((time > 0) ? " (time {1})" : ""), new Object[]{cacheFile.getPath(), formatPeriodMilli(time)});
                        return props;
                    } catch (IOException ex) {
                        LOG.log(Level.CONFIG, "[ERROR  ] Invalid cache. Ignored {0} : {1}", new Object[]{cacheFile.getPath(), ex.toString()});
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception ex) {
                                //
                            }
                        }
                    }
                }
            }
            inputStream = null;
            try {
                if (url != null) {
                    String urlString = url.toString();
                    inputStream = url.openStream();
                    if (inputStream != null) {
                        props.load(inputStream);
                        if (cacheFile != null) {
                            boolean copy = true;
                            //dont override self!
                            if (urlFile != null) {
                                if (getAbsolutePath(urlFile.getPath()).equals(getAbsolutePath(cacheFile.getPath()))) {
                                    copy = false;
                                }
                            }
                            if (copy) {
                                File pp = cacheFile.getParentFile();
                                if (pp != null) {
                                    pp.mkdirs();
                                }
                                boolean cachedRecovered = cacheFile.isFile();
                                if (urlFile != null) {
                                    copy(urlFile, cacheFile);
                                } else {
                                    copy(url, cacheFile);
                                }
                                long time = System.currentTimeMillis() - startTime;
                                if (cachedRecovered) {
                                    LOG.log(Level.CONFIG, "[RECOV. ] Cached prp file {0} (from {1})" + ((time > 0) ? " (time {2})" : ""), new Object[]{cacheFile.getPath(), urlString, formatPeriodMilli(time)});
                                } else {
                                    LOG.log(Level.CONFIG, "[CACHED ] Cached prp file {0} (from {1})" + ((time > 0) ? " (time {2})" : ""), new Object[]{cacheFile.getPath(), urlString, formatPeriodMilli(time)});
                                }
                                return props;
                            }
                        }
                        long time = System.currentTimeMillis() - startTime;
                        LOG.log(Level.CONFIG, "[SUCCESS] Loading props file from  {0}" + ((time > 0) ? " (time {1})" : ""), new Object[]{urlString, formatPeriodMilli(time)});
                    } else {
                        long time = System.currentTimeMillis() - startTime;
                        LOG.log(Level.CONFIG, "[ERROR  ] Loading props file from  {0}" + ((time > 0) ? " (time {1})" : ""), new Object[]{urlString, formatPeriodMilli(time)});
                    }
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            LOG.log(Level.CONFIG, "[ERROR  ] Loading props file from  {0}" + ((time > 0) ? " (time {1})" : ""), new Object[]{
                String.valueOf(url),
                formatPeriodMilli(time)});
            //e.printStackTrace();
        }
        return props;
    }

    public static boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            //
        }
        return false;
    }

    public static String toMavenFileName(String nutsId, String extension) {
        String[] arr = nutsId.split("[:#]");
        return arr[1]
                + "-"
                + arr[2]
                + "."
                + extension;
    }

    public static String toMavenPath(String nutsId) {
        String[] arr = nutsId.split("[:#]");
        StringBuilder sb = new StringBuilder();
        sb.append(arr[0].replace(".", "/"));
        sb.append("/");
        sb.append(arr[1]);
        if (arr.length > 2) {
            sb.append("/");
            sb.append(arr[2]);
        }
        return sb.toString();
    }

    public static String resolveMavenReleaseVersion(String mavenURLBase, String nutsId) {
        String mvnUrl = (mavenURLBase + toMavenPath(nutsId) + "/maven-metadata.xml");
        String str = null;
        try {
            str = PrivateNutsUtils.readStringFromURL(new URL(mvnUrl));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (str != null) {
            for (String line : str.split("\n")) {
                line = line.trim();
                if (line.startsWith("<release>")) {
                    return line.substring("<release>".length(), line.length() - "</release>".length()).trim();
                }
            }
        }
        throw new NutsNotFoundException(null, nutsId);
    }

    public static String resolveMavenFullPath(String repo, String nutsId, String ext) {
        String jarPath = toMavenPath(nutsId) + "/" + toMavenFileName(nutsId, ext);
        String mvnUrl = repo;
        String sep = "/";
        if (!isURL(repo)) {
            sep = File.separator;
        }
        if (!mvnUrl.endsWith("/") && !mvnUrl.endsWith(sep)) {
            mvnUrl += sep;
        }
        return mvnUrl + jarPath;
    }

    public static File resolveOrDownloadJar(String nutsId, String[] repositories, String cacheFolder) {
        String jarPath = toMavenPath(nutsId) + "/" + toMavenFileName(nutsId, "jar");
        File cachedFile = new File(resolveMavenFullPath(cacheFolder, nutsId, "jar"));
        if (cachedFile.isFile()) {
            return cachedFile;
        }
        for (String r : repositories) {
            LOG.log(Level.FINE, "Checking {0} jar from {1}", new Object[]{nutsId, r});
            String path = resolveMavenFullPath(r, nutsId, "jar");
            File file = toFile(r);
            if (file == null) {
                try {
                    copy(new URL(path), cachedFile);
                    LOG.log(Level.CONFIG, "[CACHED ] Cached jar file {0}", new Object[]{cachedFile.getPath()});
                    return cachedFile;
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, "[ERROR  ] Unable to load {0} from {1}.\n", new Object[]{nutsId, r});
                    //ex.printStackTrace();
                    //throw new NutsIllegalArgumentException("Unable to load nuts from " + mvnUrl);
                }
            } else {
                //file
                File f = new File(r, jarPath);
                if (f.isFile()) {
                    return f;
                } else {
                    LOG.log(Level.SEVERE, "Unable to load {0} from {1}.\n", new Object[]{nutsId, r});
                }
            }
        }
        return null;
    }

    public static String replaceDollarString(String path, Function<String, String> m) {
        Matcher matcher = DOLLAR_PLACE_HOLDER_PATTERN.matcher(path);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String x = m.apply(matcher.group("name"));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(x));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static List<String> splitUrlStrings(String repositories) {
        return split(repositories, "\n;", true);
    }

    public static PrivateNutsBootConfig createNutsBootConfig(Properties properties) {
        String id = properties.getProperty("project.id");
        String version = properties.getProperty("project.version");
        String dependencies = properties.getProperty("project.dependencies.compile");
        if (PrivateNutsUtils.isBlank(id)) {
            throw new NutsIllegalArgumentException(null, "Missing id");
        }
        if (PrivateNutsUtils.isBlank(version)) {
            throw new NutsIllegalArgumentException(null, "Missing version");
        }
        if (PrivateNutsUtils.isBlank(dependencies)) {
            throw new NutsIllegalArgumentException(null, "Missing dependencies");
        }
        String repositories = properties.getProperty("project.repositories");
        if (repositories == null) {
            repositories = "";
        }
        return new PrivateNutsBootConfig()
                .setRuntimeId(id + "#" + version)
                .setRuntimeDependencies(new LinkedHashSet<>(PrivateNutsUtils.split(dependencies, ";", false)))
                .setBootRepositories(repositories);
    }

    public static int parseFileSize(String s) {
        s = s.toLowerCase();
        int multiplier = 1;
        int val;
        if (s.endsWith("g")) {
            val = Integer.parseInt(s.substring(0, s.length() - 1));
            multiplier = 1024 * 1024 * 1024;
        } else if (s.endsWith("gb")) {
            val = Integer.parseInt(s.substring(0, s.length() - 2));
            multiplier = 1024 * 1024 * 1024;
        } else if (s.endsWith("m")) {
            val = Integer.parseInt(s.substring(0, s.length() - 1));
            multiplier = 1024 * 1024;
        } else if (s.endsWith("mb")) {
            val = Integer.parseInt(s.substring(0, s.length() - 2));
            multiplier = 1024 * 1024;
        } else if (s.endsWith("k")) {
            val = Integer.parseInt(s.substring(0, s.length() - 1));
            multiplier = 1024 * 1024;
        } else if (s.endsWith("kb")) {
            val = Integer.parseInt(s.substring(0, s.length() - 2));
            multiplier = 1024 * 1024;
        } else {
            val = Integer.parseInt(s);
//            multiplier = 1;
        }
        return val * multiplier;
    }

    public static String formatPeriodMilli(long period) {
        StringBuilder sb = new StringBuilder();
        boolean started = false;
        int h = (int) (period / (1000L * 60L * 60L));
        int mn = (int) ((period % (1000L * 60L * 60L)) / 60000L);
        int s = (int) ((period % 60000L) / 1000L);
        int ms = (int) (period % 1000L);
        if (h > 0) {
            sb.append(formatRight(String.valueOf(h), 2)).append("h ");
            started = true;
        }
        if (mn > 0 || started) {
            sb.append(formatRight(String.valueOf(mn), 2)).append("mn ");
            started = true;
        }
        if (s > 0 || started) {
            sb.append(formatRight(String.valueOf(s), 2)).append("s ");
            //started=true;
        }
        sb.append(formatRight(String.valueOf(ms), 3)).append("ms");
        return sb.toString();
    }

    public static String formatRight(String str, int size) {
        StringBuilder sb = new StringBuilder(size);
        sb.append(str);
        while (sb.length() < size) {
            sb.insert(0, ' ');
        }
        return sb.toString();
    }

    public static String resolveJavaCommand(String javaHome) {
        String exe = NutsPlatformUtils.getPlatformOsFamily().equals(NutsOsFamily.WINDOWS) ? "java.exe" : "java";
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
            if (PrivateNutsUtils.isBlank(javaHome) || "null".equals(javaHome)) {
                //this may happen is using a precompiled image (such as with graalvm)
                return exe;
            }
        }
        return javaHome + File.separator + "bin" + File.separator + exe;
    }

    public static int deleteAndConfirmAll(File[] folders, boolean force, String header, NutsTerminal term, NutsSession session) {
        return deleteAndConfirmAll(folders, force, new boolean[1], header, term, session);
    }

    private static int deleteAndConfirmAll(File[] folders, boolean force, boolean[] refForceAll, String header, NutsTerminal term, NutsSession session) {
        int count = 0;
        boolean headerWritten = false;
        if (folders != null) {
            for (File child : folders) {
                if (child.exists()) {
                    if (!headerWritten) {
                        headerWritten = true;
                        if (!force && !refForceAll[0]) {
                            if (header != null) {
                                if (term != null) {
                                    term.out().println(header);
                                } else {
                                    System.out.println(header);
                                }
                            }
                        }
                    }
                    if (PrivateNutsUtils.deleteAndConfirm(child, force, refForceAll, term, session)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static boolean deleteAndConfirm(File directory, boolean force, boolean[] refForceAll, NutsTerminal term, NutsSession session) {
        if (directory.exists()) {
            if (!force && !refForceAll[0]) {
                String line;
                if (term != null) {
                    line = term.ask().forString("Do you confirm deleting %s [y/n/c/a] ? : ", directory).session(session).getValue();
                } else {
                    Scanner s = new Scanner(System.in);
                    System.out.printf("Do you confirm deleting %s [y/n/c/a] ? : ", directory);
                    System.out.flush();
                    line = s.nextLine();
                }
                if ("a".equalsIgnoreCase(line) || "all".equalsIgnoreCase(line)) {
                    refForceAll[0] = true;
                } else if ("c".equalsIgnoreCase(line)) {
                    throw new NutsUserCancelException(null);
                } else if (!PrivateNutsUtils.parseBoolean(line, false)) {
                    return false;
                }
            }
            Path directoryPath = Paths.get(directory.getPath());
            try {
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.log(Level.CONFIG, "Deleting folder : {0}", directory.getPath());
                }
                Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return true;
        }
        return false;
    }

    public static boolean isActualJavaOptions(String options) {
        //FIX ME
        return true;
    }

    public static boolean isActualJavaCommand(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) {
            return true;
        }
        String javaHome = System.getProperty("java.home");
        if (PrivateNutsUtils.isBlank(javaHome) || "null".equals(javaHome)) {
            return cmd.equals("java") || cmd.equals("java.exe") || cmd.equals("javaw.exe") || cmd.equals("javaw");
        }
        String jh = javaHome.replace("\\", "/");
        cmd = cmd.replace("\\", "/");
        if (cmd.equals(jh + "/bin/java")) {
            return true;
        }
        if (cmd.equals(jh + "/bin/java.exe")) {
            return true;
        }
        if (cmd.equals(jh + "/bin/javaw")) {
            return true;
        }
        if (cmd.equals(jh + "/bin/javaw.exe")) {
            return true;
        }
        if (cmd.equals(jh + "/jre/bin/java")) {
            return true;
        }
        if (cmd.equals(jh + "/jre/bin/java.exe")) {
            return true;
        }
        return false;
    }

    public static String desc(Object s) {
        if (s == null) {
            return "<EMPTY>";
        }
        String ss
                = (s instanceof Enum) ? ((Enum) s).name().toLowerCase().replace('_', '-')
                        : s.toString().trim();
        return ss.isEmpty() ? "<EMPTY>" : ss;
    }

    public static String syspath(String s) {
        return s.replace('/', File.separatorChar);
    }

    public static String nvl(Object... all) {
        for (Object object : all) {
            if (object != null) {
                return desc(object);
            }
        }
        return desc(null);
    }

    public static String formatLogValue(Object unresolved, Object resolved) {
        String a = PrivateNutsUtils.desc(unresolved);
        String b = PrivateNutsUtils.desc(resolved);
        if (a.equals(b)) {
            return a;
        } else {
            return a + " => " + b;
        }
    }

    public static File toFile(String url) {
        if (isBlank(url)) {
            return null;
        }
        URL u = null;
        try {
            u = new URL(url);
            return toFile(u);
        } catch (MalformedURLException e) {
            //
            return new File(url);
        }
    }

    public static File toFile(URL url) {
        if (url == null) {
            return null;
        }
        if ("file".equals(url.getProtocol())) {
            try {
                return Paths.get(url.toURI()).toFile();
            } catch (URISyntaxException e) {
                //
            }
        }
        return null;
    }

    public static long copy(InputStream from, OutputStream to, boolean closeInput, boolean closeOutput) throws IOException {
        byte[] bytes = new byte[10240];
        int count;
        long all = 0;
        try {
            try {
                while ((count = from.read(bytes)) > 0) {
                    to.write(bytes, 0, count);
                    all += count;
                }
                return all;
            } finally {
                if (closeInput) {
                    from.close();
                }
            }
        } finally {
            if (closeOutput) {
                to.close();
            }
        }
    }

    public static void copy(File ff, File to) throws IOException {
        if (to.getParentFile() != null) {
            to.getParentFile().mkdirs();
        }
        try {
            Files.copy(ff.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LOG.log(Level.CONFIG, "[ERROR  ] Error copying {0} to {1} : {2}", new Object[]{ff, to, ex.toString()});
            throw ex;
        }
    }

    public static void copy(URL url, File to) throws IOException {
        try {
            InputStream in = url.openStream();
            if (in == null) {
                throw new IOException("Empty Stream " + url);
            }
            if (to.getParentFile() != null) {
                if (!to.getParentFile().isDirectory()) {
                    boolean mkdirs = to.getParentFile().mkdirs();
                    if (!mkdirs) {
                        LOG.log(Level.CONFIG, "[ERROR  ] Error creating folder {0}", new Object[]{url});
                    }
                }
            }
            ReadableByteChannel rbc = Channels.newChannel(in);
            FileOutputStream fos = new FileOutputStream(to);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException ex) {
            LOG.log(Level.CONFIG, "[ERROR  ] Error copying {0} to {1} : {2}", new Object[]{url, to, ex.toString()});
            throw ex;
        }
    }

    public static String formatURL(URL url) {
        if (url == null) {
            return "<EMPTY>";
        }
        File f = toFile(url);
        if (f != null) {
            return f.getPath();
        }
        return url.toString();
    }

    public static String getSystemString(String property, String defaultValue) {
        String v = System.getProperty(property);
        if (v == null || v.trim().isEmpty()) {
            return defaultValue;
        }
        return v;
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return (getSystemBoolean("nuts." + property, defaultValue)
                || getSystemBoolean("nuts.export." + property, defaultValue));
    }

    public static Boolean getSystemBoolean(String property, Boolean defaultValue) {
        String o = System.getProperty(property);
        if (o == null) {
            return defaultValue;
        }
        return parseBoolean(o, defaultValue);
    }

    public static Boolean parseBoolean(String value, Boolean defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        value = value.trim().toLowerCase();
        if (value.matches("true|enable|enabled|yes|always|y|on|ok|t|o")) {
            return true;
        }
        if (value.matches("false|disable|disabled|no|none|never|n|off|ko|f")) {
            return false;
        }
        return defaultValue;
    }

    public static String capitalize(String value) {
        char[] c = value.toCharArray();
        c[0] = Character.toUpperCase(c[0]);
        return new String(c);
    }

    /**
     * v1 and v2 are supposed in the following form nbr1.nbr2, ... where all
     * items (nbr) between dots are positive numbers
     *
     * @param v1 version 1
     * @param v2 version 2
     * @return 1,0 or -1
     */
    public static int compareRuntimeVersion(String v1, String v2) {
        String[] a1 = v1.split("\\.");
        String[] a2 = v2.split("\\.");
        int max = Math.max(a1.length, a2.length);
        for (int i = 0; i < max; i++) {
            if (i >= a1.length) {
                return -1;
            }
            if (i >= a2.length) {
                return 1;
            }
            int i1 = Integer.parseInt(a1[i]);
            int i2 = Integer.parseInt(a2[i]);
            if (i1 != i2) {
                return Integer.compare(i1, i2);
            }
        }
        return 0;
    }

}
