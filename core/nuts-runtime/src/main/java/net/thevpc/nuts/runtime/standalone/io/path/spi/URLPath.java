package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.format.NTreeVisitResult;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.NPathParts;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.NCachedValue;
import net.thevpc.nuts.runtime.standalone.xtra.download.DefaultHttpTransportComponent;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class URLPath implements NPathSPI {
    public static final Pattern MOSTLY_URL_PATTERN = Pattern.compile("([a-zA-Z][a-zA-Z0-9_-]+):.*");

    private final NSession session;
    protected URL url;
    protected NCachedValue<CacheInfo> cachedHeader = new NCachedValue<>(
            s -> loadCacheInfo(), 1000
    );


    public URLPath(URL url, NSession session) {
        this(url, session, false);
    }

    protected URLPath(URL url, NSession session, boolean acceptNull) {
        this.session = session;
        if (url == null) {
            if (!acceptNull) {
                throw new IllegalArgumentException("invalid url");
            }
        }
        this.url = url;
    }

    public static String getURLParentPath(String ppath) {
        if (ppath == null) {
            return null;
        }
        while (ppath.endsWith("/")) {
            ppath = ppath.substring(0, ppath.length() - 1);
        }
        if (ppath.isEmpty()) {
            return null;
        }
        int i = ppath.lastIndexOf('/');
        if (i <= 0) {
            ppath = "/";
        } else {
            ppath = ppath.substring(0, i + 1);
        }
        return ppath;
    }

    public static String getURLName(String path) {
        String name;
        int index = path.lastIndexOf('/');
        if (index < 0) {
            name = path;
        } else {
            name = path.substring(index + 1);
        }
        index = name.indexOf('?');
        if (index >= 0) {
            name = name.substring(0, index);
        }
        name = name.trim();
        return name;
    }

    private static File _toFile(URL url) {
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

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        URLPath urlPath = (URLPath) o;
        return Objects.equals(url, urlPath.url);
    }

    @Override
    public String toString() {
        return url == null ? ("broken-url") : url.toString();
    }

    @Override
    public NStream<NPath> list(NPath basePath) {
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.stream();
        }
        //should we implement other protocols ?
        return NStream.ofEmpty(getSession());
    }

    @Override
    public NFormatSPI formatter(NPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getName(NPath basePath) {
        return new NPathParts(toString(),session).getName();
    }

    @Override
    public String getProtocol(NPath basePath) {
        return url == null ? null : url.getProtocol();
    }

    @Override
    public NPath resolve(NPath basePath, String path) {
        if(url==null){
            NPathParts p = new NPathParts(toString(), session);
            String u = p.getFile();
            if (!u.endsWith("/") && !path.startsWith("/")) {
                u += "/";
            }
            u += path;
            return rebuildURLPath(rebuildURLString(p.getProtocol(), p.getAuthority(), u, p.getRef()));
        }
        String u = url.getFile();
        if (!u.endsWith("/") && !path.startsWith("/")) {
            u += "/";
        }
        u += path;
        return rebuildURLPath(rebuildURLString(url.getProtocol(), url.getAuthority(), u, url.getRef()));
    }

    @Override
    public NPath resolve(NPath basePath, NPath path) {
        if(url==null){
            NPathParts p = new NPathParts(toString(), session);
            String spath = path.toString().replace("\\", "/");
            String u = p.getFile();
            if (!u.endsWith("/") && !spath.startsWith("/")) {
                u += "/";
            }
            u += spath;
            return rebuildURLPath(rebuildURLString(p.getProtocol(), p.getAuthority(), u, p.getRef()));
        }
        String spath = path.toString().replace("\\", "/");
        String u = url.getFile();
        if (!u.endsWith("/") && !spath.startsWith("/")) {
            u += "/";
        }
        u += spath;
        return rebuildURLPath(rebuildURLString(url.getProtocol(), url.getAuthority(), u, url.getRef()));
    }


    @Override
    public NPath resolveSibling(NPath basePath, String path) {
        if(url==null){
            NPathParts p = new NPathParts(toString(), session);
            String u = _parent(p.getFile());
            String spath = path.replace("\\", "/");
            if (u == null || u.isEmpty()) {
                u = spath;
            } else {
                if (!u.endsWith("/") && !spath.startsWith("/")) {
                    u += "/";
                }
                u += spath;
            }
            return rebuildURLPath(rebuildURLString(p.getProtocol(), p.getAuthority(), u, p.getRef()));
        }
        String u = _parent(url.getFile());
        String spath = path.replace("\\", "/");
        if (u == null || u.isEmpty()) {
            u = spath;
        } else {
            if (!u.endsWith("/") && !spath.startsWith("/")) {
                u += "/";
            }
            u += spath;
        }
        return rebuildURLPath(rebuildURLString(url.getProtocol(), url.getAuthority(), u, url.getRef()));
    }

    @Override
    public NPath resolveSibling(NPath basePath, NPath path) {
        return resolveSibling(basePath, path.toString());
    }

    @Override
    public NPath toCompressedForm(NPath basePath) {
        return null;
    }

    @Override
    public URL toURL(NPath basePath) {
        if (url == null) {
            throw new NIOException(getSession(), NMsg.ofCstyle("unable to resolve url %s", toString()));
        }
        return url;
    }

    @Override
    public Path toFile(NPath basePath) {
        File f = _toFile(toURL(basePath));
        if (f != null) {
            return f.toPath();
        }
        throw new NIOException(getSession(), NMsg.ofCstyle("unable to resolve file %s", toString()));
    }

    public boolean isSymbolicLink(NPath basePath) {
        NPath f = asFilePath(basePath);
        return f != null && f.isSymbolicLink();
    }

    @Override
    public boolean isOther(NPath basePath) {
        NPath f = asFilePath(basePath);
        return f != null && f.isOther();
    }

    @Override
    public boolean isDirectory(NPath basePath) {
        if (toString().endsWith("/")) {
            return exists(basePath);
        }
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.isDirectory();
        }
        return false;
    }

    @Override
    public boolean isLocal(NPath basePath) {
        NPath f = asFilePath(basePath);
        return f != null && f.isLocal();
    }

    @Override
    public boolean isRegularFile(NPath basePath) {
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.isRegularFile();
        }
        if (!toString().endsWith("/")) {
            return exists(basePath);
        }
        return false;
    }

    @Override
    public boolean exists(NPath basePath) {
        if (url == null) {
            return false;
        }
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.exists();
        }
        try {
            CacheInfo a = cachedHeader.getValue(session);
            if (a != null) {
                int r = a.responseCode;
                return r >= 200 && r < 300;
            }
        } catch (Exception e) {
            //
        }
        try (InputStream is = url.openStream()) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public long getContentLength(NPath basePath) {
        if (url == null) {
            return -1;
        }
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.getContentLength();
        }
        try {
            CacheInfo a = cachedHeader.getValue(session);
            if (a != null) {
                return a.contentLength;
            }
        } catch (Exception e) {
            //
        }
        return -1;
    }

    public String getContentEncoding(NPath basePath) {
        try {
            CacheInfo a = cachedHeader.getValue(session);
            if (a != null) {
                return a.contentEncoding;
            }
        } catch (Exception e) {
            //
        }
        return null;
    }

    public String getContentType(NPath basePath) {
        if (url == null) {
            return null;
        }
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.getContentType();
        }
        try {
            CacheInfo a = cachedHeader.getValue(session);
            if (a != null) {
                return a.contentType;
            }
        } catch (Exception e) {
            //
        }
        return NContentTypes.of(session).probeContentType(basePath);
    }

    @Override
    public String getLocation(NPath basePath) {
        return url == null ? null : url.getFile();
    }

    public InputStream getInputStream(NPath basePath, NPathOption... options) {
        if (url == null) {
            throw new NIOException(getSession(), NMsg.ofCstyle("unable to resolve input stream %s", toString()));
        }
        NTransportComponent best = session.extensions()
                .createSupported(NTransportComponent.class, false, url);
        if (best == null) {
            best = DefaultHttpTransportComponent.INSTANCE;
        }
        NTransportConnection uu = best.open(url.toString());
        return uu.open();
    }

    public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
        try {
            if (url == null) {
                throw new NIOException(getSession(), NMsg.ofCstyle("unable to resolve output stream %s", toString()));
            }
            return url.openConnection().getOutputStream();
        } catch (IOException e) {
            throw new NIOException(session, e);
        }
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public void delete(NPath basePath, boolean recurse) {
        if (url != null) {
            NPath f = asFilePath(basePath);
            if (f != null) {
                f.delete(recurse);
                return;
            }
        }
        throw new NIOException(getSession(), NMsg.ofCstyle("unable to delete %s", toString()));
    }

    @Override
    public void mkdir(boolean parents, NPath basePath) {
        if (url != null) {
            NPath f = asFilePath(basePath);
            if (f != null) {
                f.mkdir(parents);
                return;
            }
        }
        throw new NIOException(getSession(), NMsg.ofCstyle("unable to mkdir %s", toString()));
    }

    @Override
    public Instant getLastModifiedInstant(NPath basePath) {
        if (url == null) {
            return null;
        }
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.getLastModifiedInstant();
        }
        try {
            CacheInfo a = cachedHeader.getValue(session);
            if (a != null) {
                return a.lastModified;
            }
        } catch (Exception e) {
            //
        }
        return null;
    }

    @Override
    public Instant getLastAccessInstant(NPath basePath) {
        NPath f = asFilePath(basePath);
        return (f != null) ? f.getLastAccessInstant() : null;
    }

    @Override
    public Instant getCreationInstant(NPath basePath) {
        NPath f = asFilePath(basePath);
        return (f != null) ? f.getCreationInstant() : null;
    }

    @Override
    public NPath getParent(NPath basePath) {
        if (url == null) {
            return null;
        }
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.getParent();
        }
        try {
            String ppath = getURLParentPath(url.getPath());
            if (ppath == null) {
                return null;
            }
            URL url = new URL(
                    new NPathParts(NPathParts.Type.URL,
                            this.url.getProtocol(),
                            this.url.getAuthority(),
                            ppath,
                            this.url.getQuery(),
                            this.url.getRef(),
                            session
                    ).toString()
            );
            return NPath.of(url, getSession());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public NPath toAbsolute(NPath basePath, NPath rootPath) {
        return basePath;
    }

    @Override
    public NPath normalize(NPath basePath) {
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.normalize();
        }
        return basePath;
    }

    @Override
    public boolean isAbsolute(NPath basePath) {
        return true;
    }

    @Override
    public String owner(NPath basePath) {
        NPath f = asFilePath(basePath);
        return (f != null) ? f.owner() : null;
    }

    @Override
    public String group(NPath basePath) {
        NPath f = asFilePath(basePath);
        return (f != null) ? f.group() : null;
    }

    @Override
    public Set<NPathPermission> getPermissions(NPath basePath) {
        NPath f = asFilePath(basePath);
        return (f != null) ? f.getPermissions() : Collections.emptySet();
    }

    @Override
    public void setPermissions(NPath basePath, NPathPermission... permissions) {
        NPath f = asFilePath(basePath);
        if (f != null) {
            f.setPermissions(permissions);
        }
    }

    @Override
    public void addPermissions(NPath basePath, NPathPermission... permissions) {
        NPath f = asFilePath(basePath);
        if (f != null) {
            f.addPermissions(permissions);
        }
    }

    @Override
    public void removePermissions(NPath basePath, NPathPermission... permissions) {
        NPath f = asFilePath(basePath);
        if (f != null) {
            f.removePermissions(permissions);
        }
    }

    @Override
    public boolean isName(NPath basePath) {
        return false;
    }

    @Override
    public int getPathCount(NPath basePath) {
        String location = getLocation(basePath);
        if (NBlankable.isBlank(location)) {
            return 0;
        }
        return NPath.of(location, getSession()).getPathCount();
    }

    @Override
    public boolean isRoot(NPath basePath) {
        String loc = getLocation(basePath);
        if (NBlankable.isBlank(loc)) {
            return false;
        }
        switch (loc) {
            case "/":
            case "\\\\":
                return true;
        }
        return NPath.of(loc, getSession()).isRoot();
    }

    @Override
    public NPath getRoot(NPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        return basePath.getParent().getRoot();
    }

    @Override
    public NStream<NPath> walk(NPath basePath, int maxDepth, NPathOption[] options) {
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.walk(maxDepth, options);
        }
        //should we implement other protocols ?
        return NStream.ofEmpty(getSession());
    }

    @Override
    public NPath subpath(NPath basePath, int beginIndex, int endIndex) {
        return rebuildURLPath(
                NPath.of(getLocation(basePath), getSession()).subpath(beginIndex, endIndex).toString()
        );
    }

    @Override
    public List<String> getItems(NPath basePath) {
        return NPath.of(getLocation(basePath), getSession()).getItems();
    }

    @Override
    public void moveTo(NPath basePath, NPath other, NPathOption... options) {
        throw new NIOException(session, NMsg.ofCstyle("unable to move %s", this));
    }

    @Override
    public void copyTo(NPath basePath, NPath other, NPathOption... options) {
        NCp.of(session).from(basePath).to(other).run();
    }

    @Override
    public void walkDfs(NPath basePath, NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {
        for (NPath x : walk(basePath, maxDepth, options)) {
            if (x.isDirectory()) {
                NTreeVisitResult r = visitor.preVisitDirectory(x, session);
                switch (r) {
                    case CONTINUE: {
                        break;
                    }
                    case TERMINATE: {
                        return;
                    }
                    case SKIP_SIBLINGS:
                    case SKIP_SUBTREE: {
                        throw new NIllegalArgumentException(session, NMsg.ofCstyle("unsupported %s", r));
                    }
                }
            } else if (x.isRegularFile()) {
                NTreeVisitResult r = visitor.visitFile(x, session);
                switch (r) {
                    case CONTINUE: {
                        break;
                    }
                    case TERMINATE: {
                        return;
                    }
                    case SKIP_SIBLINGS:
                    case SKIP_SUBTREE: {
                        throw new NIllegalArgumentException(session, NMsg.ofCstyle("unsupported %s", r));
                    }
                }
            }
        }
    }

    @Override
    public NPath toRelativePath(NPath basePath, NPath parentPath) {
        String child = basePath.getLocation();
        String parent = parentPath.getLocation();
        if (child.startsWith(parent)) {
            child = child.substring(parent.length());
            if (child.startsWith("/") || child.startsWith("\\")) {
                child = child.substring(1);
            }
            return NPath.of(child, session);
        }
        return null;
    }

    private CacheInfo loadCacheInfo() {
        try {
            URLConnection uu = url.openConnection();
            uu.setDoOutput(false);
            CacheInfo cc = new CacheInfo();
            if (uu instanceof HttpURLConnection) {
                HttpURLConnection hc = (HttpURLConnection) uu;
                hc.setRequestMethod("HEAD");
                cc.responseCode = hc.getResponseCode();
            }else{
                cc.responseCode = 200;
            }
            cc.contentLength = uu.getContentLengthLong();
            cc.contentEncoding = uu.getContentEncoding();
            cc.contentType = uu.getContentType();

            long z = uu.getLastModified();
            if (z > 0) {
                cc.lastModified = Instant.ofEpochMilli(z);
            }
            return cc;
        } catch (Exception ex) {
            //
        }
        return null;
    }

    private String _parent(String p) {
        while (p.endsWith("/") || p.endsWith("\\")) {
            p = p.substring(0, p.length() - 1);
        }
        if (p.isEmpty()) {
            return null;
        }
        int x = p.lastIndexOf('/');
        int y = p.lastIndexOf('\\');
        if (x < 0) {
            x = y;
        } else if (y >= 0) {
            if (y > x) {
                x = y;
            }
        }
        if (x < 0) {
            return "";
        }
        return p.substring(0, x);
    }

    protected NPath rebuildURLPath(String other) {
        return NPath.of(other, getSession());
    }

    protected String rebuildURLString(String protocol, String authority, String file, String ref) {
        int len = protocol.length() + 1;
        if (authority != null && authority.length() > 0) {
            len += 2 + authority.length();
        }
        if (file != null) {
            len += file.length();
        }
        if (ref != null) {
            len += 1 + ref.length();
        }
        StringBuilder result = new StringBuilder(len);
        result.append(protocol);
        result.append(":");
        if (authority != null && authority.length() > 0) {
            result.append("//");
            result.append(authority);
        }
        if (file != null) {
            result.append(file);
        }
        if (ref != null) {
            result.append("#");
            result.append(ref);
        }
        return result.toString();
    }

    public NPath asFilePath(NPath basePath) {
        File f = _toFile(toURL(basePath));
        return (f != null) ? NPath.of(f, getSession()) : null;
    }

    private static class CacheInfo {
        long contentLength;
        String contentEncoding;
        String contentType;
        int responseCode;
        Instant lastModified;
    }

    private static class MyPathFormat implements NFormatSPI {

        private final URLPath p;

        public MyPathFormat(URLPath p) {
            this.p = p;
        }

        @Override
        public String getName() {
            return "path";
        }

        public NString asFormattedString() {
            if (p.url == null) {
                return NTexts.of(p.getSession()).ofPlain("");
            }
            return NTexts.of(p.getSession()).ofText(p.url);
        }

        @Override
        public void print(NOutStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NCommandLine commandLine) {
            return false;
        }
    }

    public static class URLPathFactory implements NPathFactory {
        NWorkspace ws;

        public URLPathFactory(NWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NSupported<NPathSPI> createPath(String path, NSession session, ClassLoader classLoader) {
            NSessionUtils.checkSession(ws, session);
            try {
                if (path != null && path.length() > 0) {
                    char s = path.charAt(0);
                    if (Character.isAlphabetic(s)) {
                        URL url = new URL(path);
                        return NSupported.of(5, () -> new URLPath(url, session));
                    }
                }
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }
    }
}
