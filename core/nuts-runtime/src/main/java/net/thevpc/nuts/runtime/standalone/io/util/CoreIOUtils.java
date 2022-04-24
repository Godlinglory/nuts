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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.terminal.NutsTerminalModeOp;
import net.thevpc.nuts.runtime.standalone.repository.index.CacheDB;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.text.ExtendedFormatAware;
import net.thevpc.nuts.runtime.standalone.text.ExtendedFormatAwarePrintWriter;
import net.thevpc.nuts.runtime.standalone.text.RawOutputStream;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.DoWhenExist;
import net.thevpc.nuts.runtime.standalone.util.DoWhenNotExists;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NutsDigestUtils;
import net.thevpc.nuts.runtime.standalone.xtra.download.DefaultHttpTransportComponent;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBTableFile;
import net.thevpc.nuts.spi.*;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreIOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 1024;
    public static final String MIME_TYPE_SHA1 = "text/sha-1";
    public static String newLineString = null;

    @Deprecated
    public static PrintWriter toPrintWriter(Writer writer, NutsSystemTerminalBase term, NutsSession session) {
        if (writer == null) {
            return null;
        }
        if (writer instanceof ExtendedFormatAware) {
            if (writer instanceof PrintWriter) {
                return (PrintWriter) writer;
            }
        }
        ExtendedFormatAwarePrintWriter s = new ExtendedFormatAwarePrintWriter(writer, term, session);
        NutsSessionUtils.setSession(s, session);
        return s;
    }

    @Deprecated
    public static PrintWriter toPrintWriter(OutputStream writer, NutsSystemTerminalBase term, NutsSession session) {
        if (writer == null) {
            return null;
        }
        ExtendedFormatAwarePrintWriter s = new ExtendedFormatAwarePrintWriter(writer, term, session);
        NutsSessionUtils.setSession(s, session);
        return s;
    }

    @Deprecated
    public static OutputStream convertOutputStream(OutputStream out, NutsTerminalMode expected, NutsSystemTerminalBase term, NutsSession session) {
        ExtendedFormatAware a = convertOutputStreamToExtendedFormatAware(out, expected, term, session);
        return (OutputStream) a;
    }

    @Deprecated
    public static ExtendedFormatAware convertOutputStreamToExtendedFormatAware(OutputStream out, NutsTerminalMode expected, NutsSystemTerminalBase term, NutsSession session) {
        if (out == null) {
            return null;
        }
        ExtendedFormatAware aw = null;
        if (out instanceof ExtendedFormatAware) {
            aw = (ExtendedFormatAware) out;
        } else {
            aw = new RawOutputStream(out, term, session);
        }
        switch (expected) {
            case INHERITED: {
                return aw.convert(NutsTerminalModeOp.NOP);
            }
            case FORMATTED: {
                return aw.convert(NutsTerminalModeOp.FORMAT);
            }
            case FILTERED: {
                return aw.convert(NutsTerminalModeOp.FILTER);
            }
            default: {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported terminal mode %s", expected));
            }
        }
    }

    public static String resolveRepositoryPath(NutsAddRepositoryOptions options, Path rootFolder, NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        String loc = options.getLocation();
        String goodName = options.getName();
        if (NutsBlankable.isBlank(goodName)) {
            goodName = options.getConfig().getName();
        }
        if (NutsBlankable.isBlank(goodName)) {
            goodName = options.getName();
        }
        if (NutsBlankable.isBlank(goodName)) {
            if (options.isTemporary()) {
                goodName = "temp-" + UUID.randomUUID();
            } else {
                goodName = "repo-" + UUID.randomUUID();
            }
        }
        if (NutsBlankable.isBlank(loc)) {
            if (options.isTemporary()) {
                if (NutsBlankable.isBlank(goodName)) {
                    goodName = "temp";
                }
                if (goodName.length() < 3) {
                    goodName = goodName + "-repo";
                }
                loc = NutsTmp.of(session)
                        .createTempFolder(goodName + "-").toString();
            } else {
                if (NutsBlankable.isBlank(loc)) {
                    if (NutsBlankable.isBlank(goodName)) {
                        goodName = CoreNutsUtils.randomColorName() + "-repo";
                    }
                    loc = goodName;
                }
            }
        }
        return NutsPath.of(loc, session).toAbsolute(rootFolder.toString())
                .toString();
    }

//    public static String trimSlashes(String repositoryIdPath) {
//        StringBuilder sb = new StringBuilder(repositoryIdPath);
//        boolean updated = true;
//        while (updated) {
//            updated = false;
//            if (sb.length() > 0) {
//                if (sb.charAt(0) == '/' || sb.charAt(0) == '\\') {
//                    sb.delete(0, 1);
//                    updated = true;
//                } else if (sb.charAt(sb.length() - 1) == '/' || sb.charAt(sb.length() - 1) == '\\') {
//                    sb.delete(sb.length() - 1, sb.length());
//                    updated = true;
//                }
//            }
//        }
//        return sb.toString();
//    }

    public static NutsPrintStream resolveOut(NutsSession session) {
        return (session.getTerminal() == null) ? NutsPrintStream.ofNull(session)
                : session.getTerminal().out();
    }

    /**
     * copy input to output
     *
     * @param in  entree
     * @param out sortie
     */
    public static void copy(Reader in, Writer out, NutsSession session) {
        copy(in, out, DEFAULT_BUFFER_SIZE, session);
    }

    /**
     * copy input to output
     *
     * @param in  entree
     * @param out sortie
     * @return size copied
     */
    public static long copy(java.io.InputStream in, OutputStream out, NutsSession session) {
        return copy(in, out, DEFAULT_BUFFER_SIZE, session);
    }

    /**
     * copy input stream to output stream using the buffer size in bytes
     *
     * @param in         entree
     * @param out        sortie
     * @param bufferSize bufferSize
     * @return size copied
     */
    public static long copy(java.io.InputStream in, OutputStream out, int bufferSize, NutsSession session) {
        byte[] buffer = new byte[bufferSize];
        int len;
        long count = 0;
        try {
            while ((len = in.read(buffer)) > 0) {
                count += len;
                out.write(buffer, 0, len);
            }
            return len;
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    /**
     * copy input stream to output stream using the buffer size in bytes
     *
     * @param in         entree
     * @param out        sortie
     * @param bufferSize bufferSize
     */
    public static void copy(Reader in, Writer out, int bufferSize, NutsSession session) {
        char[] buffer = new char[bufferSize];
        int len;
        try {
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static String loadString(java.io.InputStream is, boolean close, NutsSession session) {
        try {
            try {
                byte[] bytes = loadByteArray(is, session);
                return new String(bytes);
            } finally {
                if (is != null && close) {
                    is.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static String loadString(Reader is, boolean close, NutsSession session) {
        try {
            try {
                char[] bytes = loadCharArray(is, session);
                return new String(bytes);
            } finally {
                if (is != null && close) {
                    is.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static char[] loadCharArray(Reader r, NutsSession session) {
        CharArrayWriter out = null;

        try {
            out = new CharArrayWriter();
            copy(r, out, session);
            out.flush();
            return out.toCharArray();
        } finally {
            if (out != null) {
                out.close();
            }
        }

    }

    public static byte[] loadByteArray(java.io.InputStream r, NutsSession session) {
        ByteArrayOutputStream out = null;

        try {
            try {
                out = new ByteArrayOutputStream();
                copy(r, out, session);
                out.flush();
                return out.toByteArray();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static byte[] loadByteArray(java.io.InputStream r, boolean close, NutsSession session) {
        ByteArrayOutputStream out = null;

        try {
            try {
                out = new ByteArrayOutputStream();
                copy(r, out, session);
                out.flush();
                return out.toByteArray();
            } finally {
                if (out != null) {
                    out.close();
                }
                if (r != null && close) {
                    r.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static byte[] loadByteArray(java.io.InputStream stream, int maxSize, boolean close, NutsSession session) {
        try {
            try {
                if (maxSize > 0) {
                    ByteArrayOutputStream to = new ByteArrayOutputStream();
                    byte[] bytes = new byte[Math.max(maxSize, 10240)];
                    int count;
                    int all = 0;
                    while ((count = stream.read(bytes)) > 0) {
                        if (all + count < maxSize) {
                            to.write(bytes, 0, count);
                            all += count;
                        } else {
                            int count2 = maxSize - all;
                            to.write(bytes, 0, count2);
                            all += count2;
                            break;
                        }
                    }
                    return to.toByteArray();
                } else {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    copy(stream, os, close, true, session);
                    return os.toByteArray();
                }
            } finally {
                if (close) {
                    stream.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static long copy(java.io.InputStream from, OutputStream to, boolean closeInput, boolean closeOutput, NutsSession session) {
        byte[] bytes = new byte[1024];//
        int count;
        long all = 0;
        try {
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
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    //    public static void delete(File file) {
//        delete(null, file);
//    }
    public static void delete(NutsSession session, File file) {
        delete(session, file.toPath());
    }
//
//    public static void delete(Path file) {
//        delete(null, file);
//    }

    public static void delete(NutsSession session, Path file) {
        if (!Files.exists(file)) {
            return;
        }
        if (Files.isRegularFile(file)) {
            try {
                Files.delete(file);
            } catch (IOException e) {
                return;
            }
        }
        final int[] deleted = new int[]{0, 0, 0};
        NutsLogger LOG = session == null ? null : NutsLogger.of(CoreIOUtils.class, session);
        try {
            Files.walkFileTree(file, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        Files.delete(file);
                        if (LOG != null) {
                            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).log(
                                    NutsMessage.jstyle("delete file {0}", file));
                        }
                        deleted[0]++;
                    } catch (IOException e) {
                        if (LOG != null) {
                            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING)
                                    .log(NutsMessage.jstyle("failed deleting file : {0}", file)
                                    );
                        }
                        deleted[2]++;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    try {
                        Files.delete(dir);
                        if (LOG != null) {
                            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING)
                                    .log(NutsMessage.jstyle("delete folder {0}", dir));
                        }
                        deleted[1]++;
                    } catch (IOException e) {
                        if (LOG != null) {
                            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING)
                                    .log(NutsMessage.jstyle("failed deleting folder: {0}", dir)
                                    );
                        }
                        deleted[2]++;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }


    public static String getFileExtension(String s) {
        int i = s.lastIndexOf('.');
        if (i == 0) {
            return s.substring(1);
        } else if (i > 0) {
            if (i < (s.length() - 1)) {
                return s.substring(i + 1);
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public static String getFileExtension(String s, boolean longest, boolean includeDot) {
        int i = longest ? s.indexOf('.') : s.lastIndexOf('.');
        if (i == 0) {
            return includeDot ? s : s.substring(1);
        } else if (i > 0) {
            if (i < (s.length() - 1)) {
                return s.substring(includeDot ? i : (i + 1));
            } else {
                return "";
            }
        } else {
            return "";
        }
    }
//    public static String getAbsoluteFile(File s) {
//        return s.toPath().normalize().toAbsolutePath().toString();
//    }
//    

    public static String buildUrl(String url, String path) {
        if (!url.endsWith("/")) {
            if (path.startsWith("/")) {
                return url + path;
            } else {
                return url + "/" + path;
            }
        } else {
            if (path.startsWith("/")) {
                return url + path.substring(1);
            } else {
                return url + path;
            }
        }
    }

    public static boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    public static NutsTransportConnection getHttpClientFacade(NutsSession session, String url) {
        NutsTransportComponent best = session.extensions()
                .createSupported(NutsTransportComponent.class, false, url);
        if (best == null) {
            best = DefaultHttpTransportComponent.INSTANCE;
        }
        return best.open(url);
    }

    public static String urlEncodeString(String s, NutsSession session) {
        if (s == null || s.trim().length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NutsIOException(session, e);
        }
    }

    public static Path resolveLocalPathFromURL(URL url) {
        try {
            return new File(url.toURI()).toPath();
        } catch (URISyntaxException e) {
            return new File(url.getPath()).toPath();
        }
    }

    public static URL resolveURLFromResource(Class cls, String urlPath, NutsSession session) {
        if (!urlPath.startsWith("/")) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to resolve url from %s", urlPath));
        }
        URL url = cls.getResource(urlPath);
        String urlFile = url.getFile();
        int separatorIndex = urlFile.indexOf("!/");
        if (separatorIndex != -1) {
            String jarFile = urlFile.substring(0, separatorIndex);
            try {
                return new URL(jarFile);
            } catch (MalformedURLException ex) {
                // Probably no protocol in original jar URL, like "jar:C:/mypath/myjar.jar".
                // This usually indicates that the jar file resides in the file system.
                if (!jarFile.startsWith("/")) {
                    jarFile = "/" + jarFile;
                }
                try {
                    return new URL("file:" + jarFile);
                } catch (IOException ex2) {
                    throw new NutsIOException(session, ex2);
                }
            }
        } else {
            String encoded = encodePath(urlPath, session);
            String url_tostring = url.toString();
            if (url_tostring.endsWith(encoded)) {
                try {
                    return new URL(url_tostring.substring(0, url_tostring.length() - encoded.length()));
                } catch (IOException ex) {
                    throw new NutsIOException(session, ex);
                }
            }
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to resolve url from %s", urlPath));
        }
    }

    private static String encodePath(String path, NutsSession session) {
        StringTokenizer st = new StringTokenizer(path, "/", true);
        StringBuilder encoded = new StringBuilder();
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (t.equals("/")) {
                encoded.append(t);
            } else {
                try {
                    encoded.append(URLEncoder.encode(t, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to encode %s", t), ex);
                }
            }
        }
        return encoded.toString();
    }

    public static File resolveLocalFileFromResource(Class cls, String url, NutsSession session) {
        return resolveLocalFileFromURL(resolveURLFromResource(cls, url, session));
    }

    public static File resolveLocalFileFromURL(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            return new File(url.getPath());
        }
    }

    public static byte[] charsToBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0);

        return bytes;
    }

    public static char[] bytesToChars(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(byteBuffer);
        char[] chars = Arrays.copyOfRange(charBuffer.array(),
                charBuffer.position(), charBuffer.limit());
        // clear sensitive data
        Arrays.fill(charBuffer.array(), '\0');
        return chars;
    }

    public static InputStream getCachedUrlWithSHA1(String path, String sourceTypeName, boolean ignoreSha1NotFound, NutsSession session) {
        final NutsPath cacheBasePath = session.locations().getStoreLocation(session.getWorkspace().getRuntimeId(), NutsStoreLocation.CACHE);
        final NutsPath urlContent = cacheBasePath.resolve("urls-content");
        String sha1 = null;
        try {
            ByteArrayOutputStream t = new ByteArrayOutputStream();
            NutsCp.of(session)
                    .from(path + ".sha1").to(t).run();
            sha1 = t.toString().trim();
        } catch (NutsIOException ex) {
            if (!ignoreSha1NotFound) {
                throw ex;
            }
        }
        NanoDB cachedDB = CacheDB.of(session);
        NanoDBTableFile<CachedURL> cacheTable
                = cachedDB.tableBuilder(CachedURL.class, session).setNullable(false)
                .addAllFields()
                .addIndices("url")
                .getOrCreate();

//        final PersistentMap<String, String> cu=getCachedUrls(session);
        CachedURL old = cacheTable.findByIndex("url", path, session).findFirst().orNull();
//        String cachedSha1 = cu.get("sha1://" + path);
//        String oldLastModified =cu.get("lastModified://" + path);
//        String oldSize =cu.get("length://" + path);
        if (sha1 != null) {
            if (old != null && sha1.equalsIgnoreCase(old.sha1)) {
                String cachedID = old.path;
                if (cachedID != null) {
                    NutsPath p = urlContent.resolve(cachedID);
                    if (p.exists()) {
                        return p.getInputStream();
                    }
                }
            }
        }

        NutsPath header = NutsPath.of(path,session);
        long size = header.getContentLength();
        Instant lastModifiedInstant = header.getLastModifiedInstant();
        long lastModified = lastModifiedInstant==null?0:lastModifiedInstant.toEpochMilli();

        //when sha1 was not resolved check size and last modification
        if (sha1 == null) {
            if (old != null && old.lastModified != -1 && old.lastModified == lastModified) {
                if (old != null && old.size == size) {
                    String cachedID = old.path;
                    if (cachedID != null) {
                        NutsPath p = urlContent.resolve(cachedID);
                        if (p.exists()) {
                            return p.getInputStream();
                        }
                    }
                }
            }
        }

        final String s = UUID.randomUUID().toString();
        final NutsPath outPath = urlContent.resolve(s + "~");
        urlContent.mkdirs();
        OutputStream p = outPath.getOutputStream();
        long finalLastModified = lastModified;
        InputStreamTee ist = new InputStreamTee(header.getInputStream(), p, () -> {
            if (outPath.exists()) {
                CachedURL ccu = new CachedURL();
                ccu.url = path;
                ccu.path = s;
                ccu.sha1 = NutsDigestUtils.evalSHA1Hex(outPath, session);
                long newSize = outPath.getContentLength();
                ccu.size = newSize;
                ccu.lastModified = finalLastModified;
                NutsPath newLocalPath = urlContent.resolve(s);
                try {
                    Files.move(outPath.toFile(), newLocalPath.toFile(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException ex) {
                    throw new NutsIOException(session, ex);
                }
                cacheTable.add(ccu, session);
                cacheTable.flush(session);
            }
        });
        return InputStreamMetadataAwareImpl.of(ist, new NutsDefaultStreamMetadata(
                        path,
                        ss->NutsTexts.of(session).ofStyled(path, NutsTextStyle.path()),
                        size, NutsPath.of(path, session).getContentType(), sourceTypeName
                )
        );

    }

    public static void storeProperties(Map<String, String> props, OutputStream out, boolean sort, NutsSession session) {
        storeProperties(props, new OutputStreamWriter(out), sort, session);
    }

    public static void storeProperties(Map<String, String> props, Writer w, boolean sort, NutsSession session) {
        try {
            Set<String> keys = props.keySet();
            if (sort) {
                keys = new TreeSet<>(keys);
            }
            for (String key : keys) {
                String value = props.get(key);
                w.write(escapePropsString(key, true));
                w.write("=");
                w.write(escapePropsString(value, false));
                w.write("\n");
                w.flush();
            }
            w.flush();
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    /*
     * Converts unicodes to encoded &#92;uxxxx and escapes
     * special characters with a preceding slash.
     * This is a modified method from java.util.Properties because the method
     * is private but we need call it handle special properties files
     */
    public static String escapePropsString(String theString,
                                           boolean escapeSpace) {
        if (theString == null) {
            theString = "";
        }
        char[] chars = theString.toCharArray();
        StringBuilder buffer = new StringBuilder(chars.length);
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '\\': {
                    buffer.append("\\\\");
                    break;
                }
                case ' ': {
                    if (i == 0 || escapeSpace) {
                        buffer.append('\\');
                    }
                    buffer.append(' ');
                    break;
                }
                case '\t': {
                    buffer.append("\\t");
                    break;
                }
                case '\n': {
                    buffer.append("\\n");
                    break;
                }
                case '\r': {
                    buffer.append("\\r");
                    break;
                }
                case '\f': {
                    buffer.append("\\f");
                    break;
                }
                case ':':
                case '#':
                case '!':
                case '=': {
                    buffer.append('\\');
                    buffer.append(c);
                    break;
                }
                default: {
                    if ((c > 61) && (c < 127)) {
                        buffer.append(c);
                    } else if (((c < 0x0020) || (c > 0x007e))) {
                        buffer.append('\\');
                        buffer.append('u');
                        buffer.append(NutsUtilStrings.toHexChar((c >> 12) & 0xF));
                        buffer.append(NutsUtilStrings.toHexChar((c >> 8) & 0xF));
                        buffer.append(NutsUtilStrings.toHexChar((c >> 4) & 0xF));
                        buffer.append(NutsUtilStrings.toHexChar(c & 0xF));
                    } else {
                        buffer.append(c);
                    }
                }
            }
        }
        return buffer.toString();
    }

    public static Path toPathInputSource(NutsStreamOrPath is, List<Path> tempPaths, NutsSession session) {
        if (is.isPath() && is.getPath().isFile()) {
            return is.getPath().toFile();
        }
        Path temp = NutsTmp.of(session)
                .createTempFile(is.getName()).toFile();
        NutsCp a = NutsCp.of(session).removeOptions(NutsPathOption.SAFE);
        if (is.isPath()) {
            a.from(is.getPath());
        } else {
            a.from(is.getInputStream());
        }
        a.to(temp).setSession(session).run();
        tempPaths.add(temp);
        return temp;
    }

    public static String getNewLine() {
        if (newLineString == null) {
            synchronized (CoreIOUtils.class) {
                newLineString = System.getProperty("line.separator");
            }
        }
        return newLineString;
    }

    public static boolean isAbsolutePath(String location) {
        return new File(location).isAbsolute();
    }

    public static String getAbsolutePath(String path) {
        return new File(path).toPath().toAbsolutePath().normalize().toString();
    }

    public static void copyFolder(Path src, Path dest, NutsSession session) {
        try {
            Files.walk(src)
                    .forEach(source -> copy(source, dest.resolve(src.relativize(source))));
        } catch (IOException e) {
            throw new NutsIOException(session, e);
        }
    }

    private static void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(CoreStringUtils.exceptionToString(e), e);
        }
    }
    public static java.io.InputStream toInterruptible(java.io.InputStream in) {
        if (in == null) {
            return null;
        }
        if (in instanceof Interruptible) {
            return in;
        }
        return new InputStreamExt(in, null);
    }

    public static boolean isObsoletePath(NutsSession session, Path path) {
        try {
            return isObsoleteInstant(session, Files.getLastModifiedTime(path).toInstant());
        } catch (IOException e) {
            return true;
        }
    }

    public static boolean isObsoletePath(NutsSession session, NutsPath path) {
        try {
            Instant i = path.getLastModifiedInstant();
            if (i == null) {
                return false;
            }
            return isObsoleteInstant(session, i);
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean isObsoleteInstant(NutsSession session, Instant instant) {
        if (session.getExpireTime() != null) {
            return instant == null || instant.isBefore(session.getExpireTime());
        }
        return false;
    }

    public static byte[] loadFileContentLenient(Path out) {
        if (Files.isRegularFile(out)) {
            try {
                return Files.readAllBytes(out);
            } catch (Exception ex) {
                //ignore
            }
        }
        return new byte[0];
    }

    public static byte[] readBestEffort(int len, java.io.InputStream in, NutsSession session) {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return new byte[0];
        }
        byte[] buf = new byte[len];
        int count = readBestEffort(buf, 0, len, in, session);
        if (count == len) {
            return buf;
        }
        byte[] buf2 = new byte[count];
        System.arraycopy(buf, 0, buf2, 0, count);
        return buf2;
    }

    public static int readBestEffort(byte[] b, int off, int len, java.io.InputStream in, NutsSession session) {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = 0;
            try {
                count = in.read(b, off + n, len - n);
            } catch (IOException e) {
                throw new NutsIOException(session, e);
            }
            if (count < 0) {
                break;
            }
            n += count;
        }
        return n;
    }

    public static boolean Arrays_equals(byte[] a, int aFromIndex, int aToIndex,
                                        byte[] b, int bFromIndex, int bToIndex) {
        //method added in JDK 9
        int aLength = aToIndex - aFromIndex;
        int bLength = bToIndex - bFromIndex;
        if (aLength != bLength) {
            return false;
        }
        for (int i = 0; i < aLength; i++) {
            if (a[aFromIndex + i] != b[bFromIndex + i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean compareContent(Path file1, Path file2, NutsSession session) {
        if (Files.isRegularFile(file1) && Files.isRegularFile(file2)) {
            try {
                if (Files.size(file1) == Files.size(file2)) {
                    int max = 2048;
                    byte[] b1 = new byte[max];
                    byte[] b2 = new byte[max];
                    try (java.io.InputStream in1 = Files.newInputStream(file1)) {
                        try (java.io.InputStream in2 = Files.newInputStream(file1)) {
                            while (true) {
                                int c1 = readBestEffort(b1, 0, b1.length, in1, session);
                                int c2 = readBestEffort(b2, 0, b2.length, in2, session);
                                if (c1 != c2) {
                                    return false;
                                }
                                if (c1 == 0) {
                                    return true;
                                }
                                if (!Arrays_equals(b1, 0, c1, b2, 0, c1)) {
                                    return false;
                                }
                                if (c1 < max) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new NutsIOException(session, e);
            }
        }
        return false;
    }

    public static InputStream createBytesStream(byte[] bytes, NutsMessage message, String contentType, String kind, NutsSession session) {
        return InputStreamMetadataAwareImpl.of(
                new ByteArrayInputStream(bytes),
                new NutsDefaultStreamMetadata(
                        message,
                        bytes.length,
                        contentType,
                        kind
                )
        );
    }

    public static String betterPath(String path1) {
        String home = System.getProperty("user.home");
        if (path1.startsWith(home + "/") || path1.startsWith(home + "\\")) {
            return "~" + path1.substring(home.length());
        }
        return path1;
    }

    public static String replaceFilePrefixes(String path, Map<String, String> map) {
        for (Map.Entry<String, String> e : map.entrySet()) {
            String v = replaceFilePrefix(path, e.getKey(), e.getValue());
            if (!v.equals(path)) {
                return v;
            }
        }
        return path;
    }

    public static String replaceFilePrefix(String path, String prefix, String replacement) {
        String path1 = path;
        String fs = File.separator;
        if (!prefix.endsWith(fs)) {
            prefix = prefix + fs;
        }
        if (!path1.endsWith(fs)) {
            path1 = prefix + fs;
        }
        if (path1.equals(prefix)) {
            if (replacement == null) {
                return "";
            }
            return replacement;
        }
        if (path.startsWith(prefix)) {
            if (replacement == null || replacement.equals("")) {
                return path1.substring(prefix.length());
            }
            return replacement + fs + path1.substring(prefix.length());
        }
        return path;
    }

    public static String longestCommonParent(String path1, String path2) {
        int latestSlash = -1;
        final int len = Math.min(path1.length(), path2.length());
        for (int i = 0; i < len; i++) {
            if (path1.charAt(i) != path2.charAt(i)) {
                break;
            } else if (path1.charAt(i) == '/') {
                latestSlash = i;
            }
        }
        if (latestSlash <= 0) {
            return "";
        }
        return path1.substring(0, latestSlash + 1);
    }

    public static PathInfo.Status tryWriteStatus(byte[] content, NutsPath out, NutsSession session) {
        return tryWrite(content, out, DoWhenExist.IGNORE, DoWhenNotExists.IGNORE, session);
    }

    public static PathInfo.Status tryWrite(byte[] content, NutsPath out, NutsSession session) {
        return tryWrite(content, out, DoWhenExist.ASK, DoWhenNotExists.CREATE, session);
    }

    public static PathInfo.Status tryWrite(byte[] content, NutsPath out, /*boolean doNotWrite*/ DoWhenExist doWhenExist, DoWhenNotExists doWhenNotExist, NutsSession session) {
        if (doWhenExist == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("missing doWhenExist"));
        }
        if (doWhenNotExist == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("missing doWhenNotExist"));
        }
//        System.err.println("[DEBUG] try write "+out);
        out = out.toAbsolute().normalize();
        byte[] old = null;
        if (out.isRegularFile()) {
            try {
                old = out.readAllBytes();
            } catch (Exception ex) {
                //ignore
            }
        }
        if (old == null) {
            switch (doWhenNotExist) {
                case IGNORE: {
                    return PathInfo.Status.DISCARDED;
                }
                case CREATE: {
                    out.mkParentDirs();
                    out.writeBytes(content);
                    if (session.isPlainTrace()) {
                        session.out().resetLine().printf("create file %s%n", out);
                    }
                    return PathInfo.Status.CREATED;
                }
                case ASK: {
                    if (session.getTerminal().ask()
                            .resetLine()
                            .setDefaultValue(true).setSession(session)
                            .forBoolean("create %s ?",
                                    NutsTexts.of(session).ofStyled(
                                            betterPath(out.toString()), NutsTextStyle.path()
                                    )
                            ).getBooleanValue()) {
                        out.mkParentDirs();
                        out.writeBytes(content);
                        if (session.isPlainTrace()) {
                            session.out().resetLine().printf("create file %s%n", out);
                        }
                        return PathInfo.Status.CREATED;
                    } else {
                        return PathInfo.Status.DISCARDED;
                    }
                }
                default: {
                    throw new NutsUnsupportedEnumException(session, doWhenNotExist);
                }
            }
        } else {
            if (Arrays.equals(old, content)) {
                return PathInfo.Status.DISCARDED;
            }
            switch (doWhenExist) {
                case IGNORE: {
                    return PathInfo.Status.DISCARDED;
                }
                case OVERRIDE: {
                    out.writeBytes(content);
                    if (session.isPlainTrace()) {
                        session.out().resetLine().printf("update file %s%n", out);
                    }
                    return PathInfo.Status.OVERRIDDEN;
                }
                case ASK: {
                    if (session.getTerminal().ask()
                            .resetLine()
                            .setDefaultValue(true).setSession(session)
                            .forBoolean("override %s ?",
                                    NutsTexts.of(session).ofStyled(
                                            betterPath(out.toString()), NutsTextStyle.path()
                                    )
                            ).getBooleanValue()) {
                        out.writeBytes(content);
                        if (session.isPlainTrace()) {
                            session.out().resetLine().printf("update file %s%n", out);
                        }
                        return PathInfo.Status.OVERRIDDEN;
                    } else {
                        return PathInfo.Status.DISCARDED;
                    }
                }
                default: {
                    throw new NutsUnsupportedEnumException(session, doWhenExist);
                }
            }
        }
    }

    public static Set<CopyOption> asCopyOptions(Set<NutsPathOption> noptions) {
        Set<CopyOption> joptions = new HashSet<>();

        for (NutsPathOption option : noptions) {
            switch (option) {
                case REPLACE_EXISTING: {
                    joptions.add(StandardCopyOption.REPLACE_EXISTING);
                    break;
                }
                case ATOMIC: {
                    joptions.add(StandardCopyOption.ATOMIC_MOVE);
                    break;
                }
                case COPY_ATTRIBUTES: {
                    joptions.add(StandardCopyOption.COPY_ATTRIBUTES);
                    break;
                }
            }
        }
        return joptions;
    }

    //    public static boolean isFileExistsAndIsWritable(Path file) {
//        if(!Files.exists(file)){
//            return false;
//        }
//        boolean writable;
//        Channel channel = null;
//        try {
//            channel = new RandomAccessFile(file.toFile(), "rw").getChannel();
//            writable = true;
//        } catch(Exception ex) {
//            writable = false;
//        } finally {
//            if(channel!=null) {
//                try {
//                    channel.close();
//                } catch (IOException ex) {
//                    // exception handling
//                }
//            }
//        }
//        return writable;
//    }

    public static NutsStream<String> safeLines(byte[] bytes, NutsSession session) {
        return NutsStream.of(
                new Iterator<String>() {
                    BufferedReader br;
                    String line;

                    @Override
                    public boolean hasNext() {
                        if (br == null) {
                            br = CoreIOUtils.bufferedReaderOf(bytes);
                        }
                        try {
                            line = null;
                            line = br.readLine();
                        } catch (IOException e) {
                            //
                        }
                        return line != null;
                    }

                    @Override
                    public String next() {
                        return line;
                    }
                }, session
        );
    }

    public static BufferedReader bufferedReaderOf(byte[] bytes) {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
    }

    public static class CachedURL {

        String url;
        String path;
        String sha1;
        long lastModified;
        long size;
    }
}
