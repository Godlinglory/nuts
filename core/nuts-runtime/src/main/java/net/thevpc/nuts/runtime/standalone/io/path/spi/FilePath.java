package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NTreeVisitResult;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NPathFactory;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilePath implements NPathSPI {

    private final Path value;
    private final NSession session;

    public FilePath(Path value, NSession session) {
        if (value == null) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid null value"));
        }
        this.value = value;
        this.session = session;
    }

    private NPath fastPath(Path p, NSession s) {
        return NPath.of(new FilePath(p, s), s);
    }

    @Override
    public NStream<NPath> list(NPath basePath) {
        if (Files.isDirectory(value)) {
            try {
                try (Stream<Path> files = Files.list(value)) {
                    //ensure closed!!
                    return NStream.of(files.collect(Collectors.toList()), getSession()).map(x -> fastPath(x, getSession()));
                }
            } catch (IOException e) {
                //
            }
        }
        return NStream.ofEmpty(getSession());
    }

    @Override
    public NFormatSPI formatter(NPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getName(NPath basePath) {
        Path a = value.getFileName();
        return a == null ? null : a.toString();
    }

    @Override
    public String getProtocol(NPath basePath) {
        return "";
    }

    @Override
    public NPath resolve(NPath basePath, String path) {
        if (NBlankable.isBlank(path)) {
            return fastPath(value, getSession());
        }
        try {
            return fastPath(value.resolve(path), getSession());
        } catch (Exception ex) {
            //always return an instance if is invalid
            return NPath.of(value + getSep() + path, getSession());
        }
    }

    @Override
    public NPath resolve(NPath basePath, NPath path) {
        if (path == null) {
            return fastPath(value, getSession());
        }
        if (path.toString().isEmpty()) {
            return fastPath(value, getSession());
        }
        Path f = path.asFile();
        if (f != null) {
            return fastPath(value.resolve(f), getSession());
        }
        return resolve(basePath, path.toString());
    }

    @Override
    public NPath resolveSibling(NPath basePath, String path) {
        if (path == null) {
            return getParent(basePath);
        }
        if (path.isEmpty()) {
            return getParent(basePath);
        }
        try {
            return fastPath(value.resolveSibling(path), getSession());
        } catch (Exception e) {
            Path p = value.getParent();
            if (p == null) {
                return NPath.of(path, session);
            }
            return fastPath(p, session).resolve(path);
        }
    }

    @Override
    public NPath resolveSibling(NPath basePath, NPath path) {
        if (path == null) {
            return getParent(basePath);
        }
        return resolveSibling(basePath, path.toString());
    }

    @Override
    public NPath toCompressedForm(NPath basePath) {
        return null;
    }

    @Override
    public URL toURL(NPath basePath) {
        try {
            return value.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new NIOException(session, e);
        }
    }

    @Override
    public Path toFile(NPath basePath) {
        return value;
    }

    @Override
    public boolean isSymbolicLink(NPath basePath) {
        PosixFileAttributes a = getUattr();
        return a != null && a.isSymbolicLink();
    }

    @Override
    public boolean isOther(NPath basePath) {
        PosixFileAttributes a = getUattr();
        return a != null && a.isOther();
    }

    @Override
    public boolean isDirectory(NPath basePath) {
        return Files.isDirectory(value);
    }

    @Override
    public boolean isLocal(NPath basePath) {
        //how about NFS?
        return true;
    }

    @Override
    public boolean isRegularFile(NPath basePath) {
        return Files.isRegularFile(value);
    }

    public boolean exists(NPath basePath) {
        return Files.exists(value);
    }

    public long getContentLength(NPath basePath) {
        try {
            return Files.size(value);
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public String getContentEncoding(NPath basePath) {
        return null;
    }

    @Override
    public String getContentType(NPath basePath) {
        return NContentTypes.of(session).probeContentType(value);
    }

    @Override
    public String getLocation(NPath basePath) {
        return value.toString();
    }

    public InputStream getInputStream(NPath basePath, NPathOption... options) {
        try {
            return Files.newInputStream(value);
        } catch (IOException e) {
            throw new NIOException(session, e);
        }
    }

    public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
        try {
            return Files.newOutputStream(value);
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
        if (Files.isRegularFile(value)) {
            try {
                Files.delete(value);
            } catch (IOException e) {
                throw new NIOException(getSession(), e);
            }
        } else if (Files.isDirectory(value)) {
            if (recurse) {
                CoreIOUtils.delete(getSession(), value);
            } else {
                try {
                    Files.delete(value);
                } catch (IOException e) {
                    throw new NIOException(getSession(), e);
                }
            }
        } else {
            throw new NIOException(getSession(), NMsg.ofC("unable to delete path %s", value));
        }
    }

    @Override
    public void mkdir(boolean parents, NPath basePath) {
        if (Files.isRegularFile(value)) {
            throw new NIOException(getSession(), NMsg.ofC("unable to create folder out of regular file %s", value));
        } else if (Files.isDirectory(value)) {
            return;
        } else {
            try {
                Files.createDirectories(value);
            } catch (IOException e) {
                throw new NIOException(getSession(), NMsg.ofC("unable to create folders %s", value));
            }
        }
    }

    @Override
    public Instant getLastModifiedInstant(NPath basePath) {
        FileTime r = null;
        try {
            r = Files.getLastModifiedTime(value);
            if (r != null) {
                return r.toInstant();
            }
        } catch (IOException e) {
            //
        }
        return null;
    }

    @Override
    public Instant getLastAccessInstant(NPath basePath) {
        BasicFileAttributes a = getBattr();
        if (a != null) {
            FileTime t = a.lastAccessTime();
            return t == null ? null : Instant.ofEpochMilli(t.toMillis());
        }
        return null;
    }

    @Override
    public Instant getCreationInstant(NPath basePath) {
        BasicFileAttributes a = getBattr();
        if (a != null) {
            FileTime t = a.creationTime();
            return t == null ? null : Instant.ofEpochMilli(t.toMillis());
        }
        return null;
    }

    @Override
    public NPath getParent(NPath basePath) {
        Path p = value.getParent();
        if (p == null) {
            return null;
        }
        return fastPath(p, getSession());
    }

    @Override
    public NPath toAbsolute(NPath basePath, NPath rootPath) {
        if (isAbsolute(basePath)) {
            return basePath;
        }
        if (rootPath == null) {
            return fastPath(value.normalize().toAbsolutePath(), session);
        }
        return rootPath.toAbsolute().resolve(toString());
    }

    @Override
    public NPath normalize(NPath basePath) {
        return fastPath(value.normalize(), session);
    }

    @Override
    public boolean isAbsolute(NPath basePath) {
        return value.isAbsolute();
    }

    @Override
    public String owner(NPath basePath) {
        PosixFileAttributes a = getUattr();
        if (a != null) {
            UserPrincipal o = a.owner();
            return o == null ? null : o.getName();
        }
        return null;
    }

    @Override
    public String group(NPath basePath) {
        PosixFileAttributes a = getUattr();
        if (a != null) {
            GroupPrincipal o = a.group();
            return o == null ? null : o.getName();
        }
        return null;
    }

    @Override
    public Set<NPathPermission> getPermissions(NPath basePath) {
        Set<NPathPermission> p = new LinkedHashSet<>();
        PosixFileAttributes a = getUattr();
        File file = value.toFile();
        if (file.canRead()) {
            p.add(NPathPermission.CAN_READ);
        }
        if (file.canWrite()) {
            p.add(NPathPermission.CAN_WRITE);
        }
        if (file.canExecute()) {
            p.add(NPathPermission.CAN_EXECUTE);
        }
        if (a != null) {
            for (PosixFilePermission permission : a.permissions()) {
                switch (permission) {
                    case OWNER_READ: {
                        p.add(NPathPermission.OWNER_READ);
                    }
                    case OWNER_WRITE: {
                        p.add(NPathPermission.OWNER_WRITE);
                    }
                    case OWNER_EXECUTE: {
                        p.add(NPathPermission.OWNER_EXECUTE);
                    }
                    case GROUP_READ: {
                        p.add(NPathPermission.GROUP_READ);
                    }
                    case GROUP_WRITE: {
                        p.add(NPathPermission.GROUP_WRITE);
                    }
                    case GROUP_EXECUTE: {
                        p.add(NPathPermission.GROUP_EXECUTE);
                    }
                    case OTHERS_READ: {
                        p.add(NPathPermission.OTHERS_READ);
                    }
                    case OTHERS_WRITE: {
                        p.add(NPathPermission.OTHERS_WRITE);
                    }
                    case OTHERS_EXECUTE: {
                        p.add(NPathPermission.OTHERS_EXECUTE);
                    }
                }
            }
        }
        return Collections.unmodifiableSet(p);
    }

    @Override
    public void setPermissions(NPath basePath, NPathPermission... permissions) {
        Set<NPathPermission> add = new LinkedHashSet<>(Arrays.asList(permissions));
        Set<NPathPermission> remove = new LinkedHashSet<>(EnumSet.allOf(NPathPermission.class));
        remove.addAll(add);
        setPermissions(add.toArray(new NPathPermission[0]), true);
//        setPermissions(remove.toArray(new NutsPathPermission[0]),false);
    }

    @Override
    public void addPermissions(NPath basePath, NPathPermission... permissions) {
        setPermissions(permissions, true);
    }

    @Override
    public void removePermissions(NPath basePath, NPathPermission... permissions) {
        //
    }

    @Override
    public boolean isName(NPath basePath) {
        if (value.getNameCount() > 1) {
            return false;
        }
        String v = value.toString();
        switch (v) {
            case "/":
            case "\\":
            case ".":
            case "..": {
                return false;
            }
        }
        for (char c : v.toCharArray()) {
            switch (c) {
                case '/':
                case '\\': {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int getPathCount(NPath basePath) {
        return value.getNameCount();
    }

    @Override
    public boolean isRoot(NPath basePath) {
        return value.getNameCount() == 0;
    }

    @Override
    public NPath getRoot(NPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        NPath parent = basePath.getParent();
        if (parent == null) {
            return null;
        }
        return parent.getRoot();
    }

    @Override
    public NStream<NPath> walk(NPath basePath, int maxDepth, NPathOption[] options) {
        FileVisitOption[] fileOptions = Arrays.stream(options)
                .map(x -> {
                    if (x == null) {
                        return null;
                    }
                    switch (x) {
                        case FOLLOW_LINKS:
                            return FileVisitOption.FOLLOW_LINKS;
                    }
                    return null;
                }).filter(Objects::nonNull).toArray(FileVisitOption[]::new);
        if (Files.isDirectory(value)) {
            try {
                return NStream.of(Files.walk(value, maxDepth, fileOptions).map(x -> fastPath(x, getSession())),
                        getSession());
            } catch (IOException e) {
                //
            }
        }
        return NStream.ofEmpty(getSession());
    }

    @Override
    public NPath subpath(NPath basePath, int beginIndex, int endIndex) {
        return fastPath(value.subpath(beginIndex, endIndex), getSession());
    }

    @Override
    public List<String> getItems(NPath basePath) {
        int nameCount = value.getNameCount();
        String[] names = new String[nameCount];
        for (int i = 0; i < nameCount; i++) {
            names[i] = value.getName(i).toString();
        }
        return Arrays.asList(names);
    }

    @Override
    public void moveTo(NPath basePath, NPath other, NPathOption... options) {
        Path f = other.asFile();
        if (f != null) {
            try {
                Files.move(value, f, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new NIOException(session, e);
            }
        } else {
            copyTo(basePath, other, options);
            delete(basePath, true);
        }
    }

    @Override
    public void copyTo(NPath basePath, NPath other, NPathOption... options) {
        NCp.of(session).from(fastPath(value, session)).to(other).run();
    }

    @Override
    public void walkDfs(NPath basePath, NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {
        Set<FileVisitOption> foptions = new HashSet<>();
        for (NPathOption option : options) {
            switch (option) {
                case FOLLOW_LINKS: {
                    foptions.add(FileVisitOption.FOLLOW_LINKS);
                    break;
                }
            }
        }
        try {
            Files.walkFileTree(value, foptions, maxDepth, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return fileVisitResult(visitor.preVisitDirectory(fastPath(dir, session), session));
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    return fileVisitResult(visitor.visitFile(fastPath(file, session), session));
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return fileVisitResult(visitor.visitFileFailed(fastPath(file, session), exc, session));
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return fileVisitResult(visitor.postVisitDirectory(fastPath(dir, session), exc, session));
                }

                private FileVisitResult fileVisitResult(NTreeVisitResult z) {
                    if (z != null) {
                        switch (z) {
                            case CONTINUE:
                                return FileVisitResult.CONTINUE;
                            case TERMINATE:
                                return FileVisitResult.TERMINATE;
                            case SKIP_SUBTREE:
                                return FileVisitResult.SKIP_SUBTREE;
                            case SKIP_SIBLINGS:
                                return FileVisitResult.SKIP_SIBLINGS;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new NIOException(getSession(), e);
        }
    }

    private String getSep() {
        for (char c : value.toString().toCharArray()) {
            switch (c) {
                case '/':
                case '\\': {
                    return String.valueOf(c);
                }
            }
        }
        return "/";
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FilePath urlPath = (FilePath) o;
        return Objects.equals(value, urlPath.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    private BasicFileAttributes getBattr() {
        try {
            return Files.readAttributes(value, BasicFileAttributes.class);
        } catch (Exception ex) {
            //
        }
        return null;
    }

    private PosixFileAttributes getUattr() {
        try {
            return Files.readAttributes(value, PosixFileAttributes.class);
        } catch (Exception ex) {
            //
        }
        return null;
    }

    public boolean setPermissions(NPathPermission[] permissions, boolean f) {
        int count = 0;
        permissions = permissions == null ? new NPathPermission[0]
                : Arrays.stream(permissions).filter(Objects::nonNull).distinct().toArray(NPathPermission[]::new);
        for (NPathPermission permission : permissions) {
            switch (permission) {
                case CAN_READ: {
                    boolean b = value.toFile().setReadable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case CAN_WRITE: {
                    boolean b = value.toFile().setWritable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case CAN_EXECUTE: {
                    boolean b = value.toFile().setExecutable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case OWNER_READ: {
                    boolean b = value.toFile().setReadable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case OWNER_WRITE: {
                    boolean b = value.toFile().setWritable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case OWNER_EXECUTE: {
                    boolean b = value.toFile().setExecutable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case GROUP_READ: {
                    boolean b = value.toFile().setReadable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case GROUP_WRITE: {
                    boolean b = value.toFile().setWritable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case GROUP_EXECUTE: {
                    boolean b = value.toFile().setExecutable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case OTHERS_READ: {
                    boolean b = value.toFile().setReadable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case OTHERS_WRITE: {
                    boolean b = value.toFile().setWritable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
                case OTHERS_EXECUTE: {
                    boolean b = value.toFile().setExecutable(f);
                    if (b) {
                        count++;
                    }
                    break;
                }
            }
        }
        return count == permissions.length;
    }

    private static class MyPathFormat implements NFormatSPI {

        private final FilePath p;

        public MyPathFormat(FilePath p) {
            this.p = p;
        }

        @Override
        public String getName() {
            return "path";
        }

        public NString asFormattedString() {
            return NTexts.of(p.getSession()).ofText(p.value);
        }

        @Override
        public void print(NPrintStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NCmdLine cmdLine) {
            return false;
        }
    }

    public static class FilePathFactory implements NPathFactory {
        NWorkspace ws;

        public FilePathFactory(NWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NSupported<NPathSPI> createPath(String path, NSession session, ClassLoader classLoader) {
            NSessionUtils.checkSession(ws, session);
            try {
                if (URLPath.MOSTLY_URL_PATTERN.matcher(path).matches()) {
                    return null;
                }
                Path value = Paths.get(path);
                return NSupported.of(10, () -> new FilePath(value, session));
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }

        @Override
        public int getSupportLevel(NSupportLevelContext context) {
            String path= context.getConstraints();
            try {
                if (URLPath.MOSTLY_URL_PATTERN.matcher(path).matches()) {
                    return NO_SUPPORT;
                }
                Path value = Paths.get(path);
                return DEFAULT_SUPPORT;
            } catch (Exception ex) {
                //ignore
            }
            return NO_SUPPORT;
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

}
