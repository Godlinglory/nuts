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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/15/17.
 */
final class NutsUtils {

    private static final Logger log = Logger.getLogger(NutsUtils.class.getName());
    private static Pattern JSON_BOOT_KEY_VAL = Pattern.compile("\"(?<key>(.+))\"\\s*:\\s*\"(?<val>[^\"]*)\"");

    public static boolean isEmpty(String str) {
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

    /**
     * BootRuntimeDependencies are separated with any of ':' ',' ';' ' ' '\n'
     * '\t' if the path contains :, it should be escaped with \
     *
     * @param str
     * @return
     */
    public static String[] splitBootRuntimeDependencies(String str) {
        List<String> result = new ArrayList<>();
        if (str != null) {
            char[] chars = str.toCharArray();
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < chars.length; i++) {
                switch (chars[i]) {
                    case '\\': {
                        if (i + 1 < chars.length && (chars[i + 1] == ':') || chars[i + 1] == '\\' || chars[i + 1] == ',' || chars[i + 1] == ';') {
                            current.append(chars[i + 1]);
                            i++;
                        } else {
                            current.append('\\');
                        }
                        break;
                    }
                    case ':':
                    case ' ':
                    case ',':
                    case ';':
                    case '\t':
                    case '\n': {
                        if (current.length() > 0) {
                            result.add(current.toString());
                            current.delete(0, current.length());
                        }
                        break;
                    }
                    default: {
                        current.append(chars[i]);
                    }
                }
            }
            if (current.length() > 0) {
                result.add(current.toString());
                current.delete(0, current.length());
            }
        }
        return result.toArray(new String[0]);
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

    public static String mergeLists(String sep, String... lists) {
        LinkedHashSet<String> all = new LinkedHashSet<>(Arrays.asList(splitAndRemoveDuplicates(Arrays.asList(lists))));
        return join(sep, all);
    }

    public static String join(String sep, String[] items) {
        return join(sep, Arrays.asList(items));
    }

    public static String join(String sep, Collection<String> items) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> i = items.iterator();
        if (i.hasNext()) {
            sb.append(i.next());
        }
        while (i.hasNext()) {
            sb.append(sep);
            sb.append(i.next());
        }
        return sb.toString();
    }

    public static String[] splitAndRemoveDuplicates(List<String>... possibilities) {
        LinkedHashSet<String> allValid = new LinkedHashSet<>();
        for (List<String> initial : possibilities) {
            for (String v : initial) {
                if (!isEmpty(v)) {
                    v = v.trim();
                    for (String v0 : v.split(";")) {
                        v0 = v0.trim();
                        if (!allValid.contains(v0)) {
                            allValid.add(v0);
                        }
                    }
                }
            }
        }
        return allValid.toArray(new String[0]);
    }

    public static String[] splitAndRemoveDuplicates(String... possibilities) {
        LinkedHashSet<String> allValid = new LinkedHashSet<>();
        for (String v : possibilities) {
            if (!isEmpty(v)) {
                v = v.trim();
                for (String v0 : v.split(";")) {
                    v0 = v0.trim();
                    if (!allValid.contains(v0)) {
                        allValid.add(v0);
                    }
                }
            }
        }
        return allValid.toArray(new String[0]);
    }

    public static Map<String, Object> parseJson(String json) {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine engine = sem.getEngineByName("javascript");
        String script = "Java.asJSONCompatible(" + json + ")";
        Map result = null;
        try {
            result = (Map) engine.eval(script);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("Invalid json " + json);
        }
        return result;
    }

    public static File createFile(String path) {
        return new File(getAbsolutePath(path));
    }

    public static File createFile(File parent, String path) {
        return new File(parent, path);
    }

    public static File createFile(String parent, String path) {
        return new File(getAbsolutePath(parent), path);
    }

    public static String getAbsolutePath(String path) {
        return new File(path).toPath().toAbsolutePath().normalize().toString();
    }

    public static String readStringFromURL(URL requestURL) throws IOException {
        try {
            return new String(Files.readAllBytes(Paths.get(requestURL.toURI())));
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
//        try (Scanner scanner = new Scanner(requestURL.openStream(),
//                StandardCharsets.UTF_8.toString())) {
//            scanner.useDelimiter("\\A");
//            return scanner.hasNext() ? scanner.next() : "";
//        }
    }

    public static String readStringFromFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static void copy(InputStream from, File to, boolean mkdirs, boolean closeInput) throws IOException {
        try {
            File parentFile = to.getParentFile();
            if (mkdirs && parentFile != null) {
                parentFile.mkdirs();
            }
            File temp = new File(to.getPath() + "~");
            try {
                Files.copy(from, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Files.move(temp.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } finally {
                temp.delete();
            }
        } finally {
            if (closeInput) {
                from.close();
            }
        }
    }

    public static void copy(File from, File to, boolean mkdirs) throws IOException {
        File parentFile = to.getParentFile();
        if (mkdirs && parentFile != null) {
            parentFile.mkdirs();
        }
        File temp = new File(to.getPath() + "~");
        try {
            Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            temp.delete();
        }
    }

    public static boolean isAbsolutePath(String location) {
        return new File(location).isAbsolute();
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

    public static String expandPath(String path) {
        if (path.equals("~") || path.equals("~/") || path.equals("~\\") || path.equals("~\\")) {
            return System.getProperty("user.home");
        }
        if (path.startsWith("~/") || path.startsWith("~\\")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    public static File resolvePath(String path, File baseFolder, String nutsHome) {
        System.out.println("resolvePath " + path + " :: " + baseFolder + " ::" + nutsHome);
        if (path != null && path.length() > 0) {
            String firstItem = "";
            if ('\\' == File.separatorChar) {
                String[] split = path.split("([/\\\\])");
                if (split.length > 0) {
                    firstItem = split[0];
                }
            } else {
                String[] split = path.split("(/|" + File.separatorChar + ")");
                if (split.length > 0) {
                    firstItem = split[0];
                }
            }
            if (firstItem.equals("~~")) {
                System.out.println("\t ##1");
                return resolvePath(nutsHome + File.separator + path.substring(2), null, nutsHome);
            } else if (path.indexOf('/') < 0 && path.indexOf('\\') < 0) {
                System.out.println("\t ##2");
                return resolvePath(nutsHome + File.separator + path.substring(2), null, nutsHome);
            } else if (firstItem.equals("~")) {
                System.out.println("\t ##3");
                return new File(System.getProperty("user.home"), path.substring(1));
            } else if (isAbsolutePath(path)) {
                System.out.println("\t ##4");
                return new File(path);
            } else if (baseFolder != null) {
                System.out.println("\t ##5");
                return createFile(baseFolder, path);
            } else {
                System.out.println("\t ##6");
                return createFile(path);
            }
        }
        return null;
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
            log.log(Level.SEVERE, "[ERROR  ] Unable to store {0}", file);
        }
        return false;
    }

    public static Properties loadFileProperties(File file) {
        Properties props = new Properties();
        InputStream inputStream = null;
        try {
            try {
                if (file != null && file.isFile()) {
                    inputStream = new FileInputStream(file);
                    props.load(inputStream);
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return props;
    }

    public static Properties loadURLProperties(String url, File cacheFile) {
        try {
            if (url != null) {
                return loadURLProperties(new URL(url), cacheFile);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return new Properties();
    }

    public static Properties loadFileProperties(String file) {
        try {
            if (file != null) {
                return loadFileProperties(new File(file));
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return new Properties();
    }

    public static File urlToFile(String url) {
        if (url != null) {
            URL u = null;
            try {
                u = new URL(url);
            } catch (Exception ex) {
                //
            }
            if (u != null) {
                if ("file".equals(u.getProtocol())) {
                    try {
                        return new File(u.toURI());
                    } catch (Exception ex) {
                        return new File(u.getPath());
                    }
                }
            }
        }
        return null;
    }

    public static Properties loadURLProperties(URL url, File cacheFile) {
        long startTime = System.currentTimeMillis();
        Properties props = new Properties();
        InputStream inputStream = null;
        try {
            try {
                if (url != null) {
                    inputStream = url.openStream();
                    if (inputStream != null) {
                        props.load(inputStream);
                        if (cacheFile != null && !isFileURL(url.toString())) {
                            copy(url.openStream(), cacheFile, true, true);
                            log.log(Level.CONFIG, "[CACHED ] Caching props file to    {0}", new Object[]{cacheFile.getPath()});
                        }
                        long time = System.currentTimeMillis() - startTime;
                        if (time > 0) {
                            log.log(Level.CONFIG, "[SUCCESS] Loading props file from  {0} (time {1})", new Object[]{url.toString(), formatPeriodMilli(time)});
                        } else {
                            log.log(Level.CONFIG, "[SUCCESS] Loading props file from  {0}", new Object[]{url.toString()});
                        }
                    } else {
                        long time = System.currentTimeMillis() - startTime;
                        if (time > 0) {
                            log.log(Level.CONFIG, "[ERROR  ] Loading props file from  {0} (time {1})", new Object[]{url.toString(), formatPeriodMilli(time)});
                        } else {
                            log.log(Level.CONFIG, "[ERROR  ] Loading props file from  {0}", new Object[]{url.toString()});
                        }
                    }
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            if (time > 0) {
                log.log(Level.CONFIG, "[ERROR  ] Loading props file from  {0} (time {1})", new Object[]{url.toString(), formatPeriodMilli(time)});
            } else {
                log.log(Level.CONFIG, "[ERROR  ] Loading props file from  {0}", new Object[]{url.toString()});
            }
            //e.printStackTrace();
        }
        return props;
    }

    public static boolean isRemoteURL(String url) {
        if (url == null) {
            return false;
        }
        url = url.toLowerCase();
        return (url.startsWith("http://") || url.startsWith("https://"));
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
            str = NutsUtils.readStringFromURL(new URL(mvnUrl));
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
        if (str != null) {
            for (String line : str.split("\n")) {
                line = line.trim();
                if (line.startsWith("<release>")) {
                    return line.substring("<release>".length(), line.length() - "</release>".length()).trim();
                }
            }
        }
        throw new NutsIOException("Nuts not found " + nutsId);
    }

    public static boolean isFileURL(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return false;
        }
        return true;
    }

    public static String resolveMavenFullPath(String repo, String nutsId, String ext) {
        String jarPath = toMavenPath(nutsId) + "/" + toMavenFileName(nutsId, ext);
        String mvnUrl = repo;
        String sep = "/";
        if (isFileURL(repo)) {
            sep = File.separator;
        }
        if (!mvnUrl.endsWith("/") && !mvnUrl.endsWith(sep)) {
            mvnUrl += sep;
        }
        return mvnUrl + jarPath;
    }

    public static File resolveOrDownloadJar(String nutsId, String[] repositories, String cacheFolder) {
        String jarPath = toMavenPath(nutsId) + "/" + toMavenFileName(nutsId, "jar");
        for (String r : repositories) {
            log.fine("Checking " + nutsId + " jar from " + r);
            String path = resolveMavenFullPath(r, nutsId, "jar");
            if (!isFileURL(r)) {
                try {
                    File cachedFile = new File(resolveMavenFullPath(cacheFolder, nutsId, "jar"));
                    if (cachedFile.getParentFile() != null) {
                        cachedFile.getParentFile().mkdirs();
                    }
                    ReadableByteChannel rbc = Channels.newChannel(new URL(path).openStream());
                    FileOutputStream fos = new FileOutputStream(cachedFile);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    log.log(Level.CONFIG, "[CACHED ] Caching jar file {0}", new Object[]{cachedFile.getPath()});
                    return cachedFile;
                } catch (Exception ex) {
                    System.err.printf("Unable to load " + nutsId + " from " + r + ".\n");
                    //ex.printStackTrace();
                    //throw new NutsIllegalArgumentException("Unable to load nuts from " + mvnUrl);
                }
            } else {
                //file
                File f = new File(r, jarPath);
                if (f.isFile()) {
                    return f;
                } else {
                    System.err.printf("Unable to load " + nutsId + " from " + r + ".\n");
                }
            }
        }
        return null;
    }

    public static String replaceDollarString(String path, NutsObjectConverter<String, String> m) {
        Pattern compiled = Pattern.compile("[$][{](?<name>([a-zA-Z]+))[}]");
        Matcher matcher = compiled.matcher(path);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String x = m.convert(matcher.group("name"));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(x));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static NutsBootConfig loadNutsBootConfig(String workspace) {
        File versionFile = new File(workspace, NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
        boolean loadedFile = false;
        try {
            if (versionFile.isFile()) {
                String str = readStringFromFile(versionFile);
                if (str.length() > 0) {
                    loadedFile = true;
                    if (log.isLoggable(Level.FINEST)) {
                        log.log(Level.FINEST, "Loading Workspace Config {0}", versionFile.getPath());
                    }
                    str = str.trim();
                    if (str.length() > 0) {
                        Pattern bootRuntime = JSON_BOOT_KEY_VAL;
                        Matcher matcher = bootRuntime.matcher(str);
                        NutsBootConfig c = new NutsBootConfig();
                        while (matcher.find()) {
                            String k = matcher.group("key");
                            String val = matcher.group("val");
                            if (k != null) {
                                switch (k) {
                                    case "bootApiVersion": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Workspace Config {0}={1}", new Object[]{k, val});
                                        }
                                        c.setApiVersion(val);
                                        break;
                                    }
                                    case "bootRuntime": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Workspace Config {0}={1}", new Object[]{k, val});
                                        }
                                        c.setRuntimeId(val);
                                        break;
                                    }
                                    case "bootRepositories": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Workspace Config {0}={1}", new Object[]{k, val});
                                        }
                                        c.setRepositories(val);
                                        break;
                                    }
                                    case "bootRuntimeDependencies": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Workspace Config {0}={1}", new Object[]{k, val});
                                        }
                                        c.setRuntimeDependencies(val);
                                        break;
                                    }
                                    case "bootJavaCommand": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Workspace Config {0}={1}", new Object[]{k, val});
                                        }
                                        c.setJavaCommand(val);
                                        break;
                                    }
                                    case "bootJavaOptions": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Workspace Config {0}={1}", new Object[]{k, val});
                                        }
                                        c.setJavaOptions(val);
                                        break;
                                    }
                                    case "workspace": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Workspace Config {0}={1}", new Object[]{k, val});
                                        }
                                        c.setWorkspace(val);
                                        break;
                                    }
                                    case "programsStoreLocation": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Workspace Config {0}={1}", new Object[]{k, val});
                                        }
                                        c.setProgramsStoreLocation(val);
                                        break;
                                    }
                                    case "configStoreLocation": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Workspace Config {0}={1}", new Object[]{k, val});
                                        }
                                        c.setConfigStoreLocation(val);
                                        break;
                                    }
                                    case "varStoreLocation": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Workspace Config {0}={1}", new Object[]{k, val});
                                        }
                                        c.setVarStoreLocation(val);
                                        break;
                                    }
                                    case "logsStoreLocation": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Json Config {0}={1}", new Object[]{k, val});
                                        }
                                        c.setLogsStoreLocation(val);
                                        break;
                                    }
                                    case "tempStoreLocation": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Workspace Config {0}={1}", new Object[]{k, val});
                                        }
                                        c.setTempStoreLocation(val);
                                        break;
                                    }
                                    case "cacheStoreLocation": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Workspace Config {0}={1}", new Object[]{k, val});
                                        }
                                        c.setCacheStoreLocation(val);
                                        break;
                                    }
                                    case "storeLocationStrategy": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Workspace Config {0}={1}", new Object[]{k, val});
                                        }
                                        NutsStoreLocationStrategy strategy = NutsStoreLocationStrategy.SYSTEM;
                                        if (!val.isEmpty()) {
                                            try {
                                                strategy = NutsStoreLocationStrategy.valueOf(val.toUpperCase());
                                            } catch (Exception ex) {
                                                //
                                            }
                                        }
                                        c.setStoreLocationStrategy(strategy);
                                        break;
                                    }
                                    case "storeLocationLayout": {
                                        if (log.isLoggable(Level.FINEST)) {
                                            log.log(Level.FINEST, "\tLoaded Workspace Config {0}={1}", new Object[]{k, val});
                                        }
                                        NutsStoreLocationLayout layout = NutsStoreLocationLayout.SYSTEM;
                                        if (!val.isEmpty()) {
                                            try {
                                                layout = NutsStoreLocationLayout.valueOf(val.toUpperCase());
                                            } catch (Exception ex) {
                                                //
                                            }
                                        }
                                        c.setStoreLocationLayout(layout);
                                        break;
                                    }
                                }
                            }
                        }
                        return c;
                        //return parseJson(str);
                    }
                }
            }
            if (!loadedFile) {
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "Empty Workspace Config {0}", versionFile.getPath());
                }
            }
        } catch (Exception ex) {
            log.log(Level.CONFIG, "Unable to load nuts version file " + versionFile + ".\n", ex);
        }
        return new NutsBootConfig();
    }

    public static List<String> splitUrlStrings(String repositories) {
        return split(repositories, "\n;", true);
    }

    public static NutsBootConfig createNutsBootConfig(Properties properties) {
        String id = properties.getProperty("project.id");
        String version = properties.getProperty("project.version");
        String dependencies = properties.getProperty("project.dependencies.compile");
        if (NutsUtils.isEmpty(id)) {
            throw new IllegalArgumentException("Missing id");
        }
        if (NutsUtils.isEmpty(version)) {
            throw new IllegalArgumentException("Missing version");
        }
        if (NutsUtils.isEmpty(dependencies)) {
            throw new IllegalArgumentException("Missing dependencies");
        }
        String repositories = properties.getProperty("project.repositories");
        if (repositories == null) {
            repositories = "";
        }
        return new NutsBootConfig()
                .setRuntimeId(id + "#" + version)
                .setRuntimeDependencies(dependencies)
                .setRepositories(repositories);
    }

    public static int parseFileSize(String s) {
        s = s.toLowerCase();
        int multiplier = 1;
        int val = 1;
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
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
        }
        String exe = isOSWindow() ? "java.exe" : "java";
        return javaHome + File.separator + "bin" + File.separator + exe;
    }

    public static boolean isOSWindow() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static String[] parseDependenciesFromMaven(URL url, File cacheFile) {

        long startTime = System.currentTimeMillis();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setExpandEntityReferences(false);
        DocumentBuilder dBuilder = null;
        List<String> deps = new ArrayList<>();
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            InputStream stream = url.openStream();
            if (stream == null) {
                return null;
            }
            Document doc = dBuilder.parse(stream);
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            NodeList properties = doc.getDocumentElement().getElementsByTagName("properties");
            NodeList rootChildList = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < rootChildList.getLength(); i++) {
                Element dependencies = toElement(rootChildList.item(i), "dependencies");
                if (dependencies != null) {
                    NodeList dependenciesChildList = dependencies.getChildNodes();
                    for (int j = 0; j < dependenciesChildList.getLength(); j++) {
                        Element dependency = toElement(dependenciesChildList.item(j), "dependency");
                        if (dependency != null) {
                            NodeList dependencyChildList = dependency.getChildNodes();
                            String groupId = "";
                            String artifactId = "";
                            String version = "";
                            String scope = "";
                            for (int k = 0; k < dependencyChildList.getLength(); k++) {
                                Element c = toElement(dependencyChildList.item(k));
                                if (c != null) {
                                    switch (c.getTagName()) {
                                        case "groupId": {
                                            groupId = c.getTextContent() == null ? "" : c.getTextContent().trim();
                                            break;
                                        }
                                        case "artifactId": {
                                            artifactId = c.getTextContent() == null ? "" : c.getTextContent().trim();
                                            break;
                                        }
                                        case "version": {
                                            version = c.getTextContent() == null ? "" : c.getTextContent().trim();
                                            break;
                                        }
                                        case "scope": {
                                            scope = c.getTextContent() == null ? "" : c.getTextContent().trim();
                                            break;
                                        }
                                    }
                                }
                            }
                            if (scope.isEmpty() || scope.equals("compile")) {
                                deps.add(new NutsBootId(
                                        groupId, artifactId, version
                                ).toString());
                            }
                        }
                    }
                }
            }
            if (cacheFile != null && !isFileURL(url.toString())) {
                copy(url.openStream(), cacheFile, true, true);
                log.log(Level.CONFIG, "[CACHED ] Caching pom.xml file {0}", new Object[]{cacheFile.getPath()});
            }
            long time = System.currentTimeMillis() - startTime;
            if (time > 0) {
                log.log(Level.CONFIG, "[SUCCESS] Loading pom.xml file from  {0} (time {1})", new Object[]{url.toString(), formatPeriodMilli(time)});
            } else {
                log.log(Level.CONFIG, "[SUCCESS] Loading pom.xml file from  {0}", new Object[]{url.toString()});
            }
            return deps.toArray(new String[0]);
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            if (time > 0) {
                log.log(Level.CONFIG, "[ERROR  ] Loading pom.xml file from  {0} (time {1})", new Object[]{url.toString(), formatPeriodMilli(time)});
            } else {
                log.log(Level.CONFIG, "[ERROR  ] Loading pom.xml file from  {0}", new Object[]{url.toString()});
            }
            return null;
        }
    }

    private static Element toElement(Node n) {
        if (n instanceof Element) {
            return (Element) n;
        }
        return null;
    }

    private static Element toElement(Node n, String name) {
        if (n instanceof Element) {
            if (((Element) n).getTagName().equals(name)) {
                return (Element) n;
            }
        }
        return null;
    }

    public static int deleteAndConfirmAll(File[] folders, boolean force) throws IOException {
        return deleteAndConfirmAll(folders, force,new boolean[1]);
    }
    
    public static int deleteAndConfirmAll(File[] folders, boolean force, boolean[] refForceAll) throws IOException {
        int count = 0;
        if (folders != null) {
            for (File child : folders) {
                if (child.exists()) {
                    NutsUtils.deleteAndConfirm(child, force, refForceAll);
                    count++;
                }
            }
        }
        return count;
    }

    public static boolean deleteAndConfirm(File directory, boolean force, boolean[] refForceAll) throws IOException {
        if (directory.exists()) {
            if (!force && !refForceAll[0]) {
                Scanner s = new Scanner(System.in);
                System.out.println("Deleting folder " + directory);
                System.out.print("\t Are you sure [y/n] ? : ");
                String line = s.nextLine();
                if ("y".equalsIgnoreCase(line) || "yes".equalsIgnoreCase(line)) {
                    //ok
                } else if ("a".equalsIgnoreCase(line) && !"all".equalsIgnoreCase(line)) {
                    refForceAll[0]=true;
                } else {
                    throw new NutsUserCancelException();
                }
            }
            delete(directory.getPath());
            return true;
        }
        return false;
    }

    public static void delete(String directoryName) throws IOException {

        Path directory = Paths.get(directoryName);
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs) throws IOException {
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
    }

    public static String getPlatformOsFamily() {
        String property = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (property.startsWith("linux")) {
            return "linux";
        }
        if (property.startsWith("win")) {
            return "windows";
        }
        if (property.startsWith("mac")) {
            return "mac";
        }
        if (property.startsWith("sunos")) {
            return "unix";
        }
        if (property.startsWith("freebsd")) {
            return "unix";
        }
        return "unknown";
    }

    public static boolean isActualJavaCommand(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) {
            return true;
        }
        String jh = System.getProperty("java.home").replace("\\", "/");
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
        String ss = s.toString().trim();
        return ss.isEmpty() ? "<EMPTY>" : ss;
    }

    public static String syspath(String s) {
        return s.replace('/', File.separatorChar);
    }
}
