package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.NFormat;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NString;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.io.util.NPathParts;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NStream;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class NCompressedPathBase extends NPathBase {

    private final String compressedForm;
    private final NString formattedCompressedForm;
    private final NPath base;

    public NCompressedPathBase(NPath base) {
        super(base.getSession());
        this.base = base;
        this.compressedForm = compressUrl(base.toString(), base.getSession());
        this.formattedCompressedForm = NTexts.of(base.getSession()).ofStyled(compressedForm, NTextStyle.path());
    }

    public NCompressedPathBase(NPath base, String compressedForm, NString formattedCompressedForm) {
        super(base.getSession());
        this.compressedForm = compressedForm;
        this.formattedCompressedForm = formattedCompressedForm;
        this.base = base;
    }

    @Override
    public NPath copy() {
        return new NCompressedPathBase(base, compressedForm, formattedCompressedForm).copyExtraFrom(this);
    }

    public static String compressUrl(String path, NSession session) {
        NPathParts p = new NPathParts(path, session);
        switch (p.getType()) {
            case FILE_URL:
            case URL: {
                return new NPathParts(p.getType(), p.getProtocol(), p.getAuthority(), NPathParts.compressLocalPath(p.getFile(), 0, 2), p.getQuery().length() > 0 ? "..." : "", p.getRef().length() > 0 ? "..." : "", session

                ).toString();
            }
            case REF: {
                return "#...";
            }
            case FILE: {
                return NPathParts.compressLocalPath(p.getFile());
            }
            case EMPTY:
                return "";
        }
        return path;
    }

    @Override
    public String getContentEncoding() {
        return base.getContentEncoding();
    }

    @Override
    public String getContentType() {
        return base.getContentType();
    }

    @Override
    public String getName() {
        return base.getName();
    }

    @Override
    public String getLocation() {
        return base.getLocation();
    }

    @Override
    public NPath resolve(String other) {
        return base.resolve(other).toCompressedForm();
    }

    @Override
    public NPath resolve(NPath other) {
        return base.resolve(other).toCompressedForm();
    }

    @Override
    public NPath resolveSibling(String other) {
        return base.resolveSibling(other).toCompressedForm();
    }

    @Override
    public NPath resolveSibling(NPath other) {
        return base.resolveSibling(other).toCompressedForm();
    }

    @Override
    public byte[] readBytes(NPathOption... options) {
        return base.readBytes(options);
    }

    @Override
    public NPath writeBytes(byte[] bytes, NPathOption... options) {
        return base.writeBytes(bytes, options);
    }

    @Override
    public String getProtocol() {
        return base.getProtocol();
    }

    @Override
    public NPath toCompressedForm() {
        return this;
    }

    @Override
    public NOptional<URL> toURL() {
        return base.toURL();
    }

    @Override
    public NOptional<Path> toPath() {
        return base.toPath();
    }

    @Override
    public NOptional<File> toFile() {
        return base.toFile();
    }

    @Override
    public NStream<NPath> stream() {
        return base.stream();
    }


    @Override
    public InputStream getInputStream(NPathOption... options) {
        return NIO.of(getSession()).ofInputStream(base.getInputStream(options), getMetaData());
    }

    @Override
    public OutputStream getOutputStream(NPathOption... options) {
        return NIO.of(getSession()).ofRawOutputStream(base.getOutputStream(options), this.getMetaData());
    }

    @Override
    public NPath deleteTree() {
        return base.deleteTree();
    }

    @Override
    public NPath delete(boolean recurse) {
        return base.delete(recurse);
    }

    @Override
    public NPath mkdir(boolean parents) {
        return base.mkdir(parents);
    }

    @Override
    public NPath mkdirs() {
        return base.mkdirs();
    }

    @Override
    public NPath mkdir() {
        return base.mkdir();
    }

    @Override
    public NPath expandPath(Function<String, String> resolver) {
        return base.expandPath(resolver).toCompressedForm();
    }

    @Override
    public NPath mkParentDirs() {
        return base.mkParentDirs();
    }

    @Override
    public boolean isOther() {
        return base.isOther();
    }

    @Override
    public boolean isSymbolicLink() {
        return base.isSymbolicLink();
    }

    @Override
    public boolean isDirectory() {
        return base.isDirectory();
    }

    @Override
    public boolean isRegularFile() {
        return base.isRegularFile();
    }

    @Override
    public boolean isRemote() {
        return base.isRemote();
    }

    @Override
    public boolean isLocal() {
        return base.isLocal();
    }

    @Override
    public boolean exists() {
        return base.exists();
    }

    @Override
    public long getContentLength() {
        return base.getContentLength();
    }

    @Override
    public Instant getLastModifiedInstant() {
        return base.getLastModifiedInstant();
    }

    @Override
    public Instant getLastAccessInstant() {
        return base.getLastAccessInstant();
    }

    @Override
    public Instant getCreationInstant() {
        return base.getCreationInstant();
    }

    @Override
    public NPath getParent() {
        return base.getParent();
    }

    @Override
    public boolean isAbsolute() {
        return base.isAbsolute();
    }

    @Override
    public NPath normalize() {
        return base.normalize();
    }

    @Override
    public NPath toAbsolute() {
        return toAbsolute((NPath) null);
    }

    @Override
    public NPath toAbsolute(String basePath) {
        return base.toAbsolute(basePath).toCompressedForm();
    }

    @Override
    public NPath toAbsolute(NPath basePath) {
        if (base.isAbsolute()) {
            return this;
        }
        return basePath.toAbsolute(basePath).toCompressedForm();
    }

    @Override
    public NPath toRelative(NPath basePath) {
        return base.toRelative(basePath);
    }

    @Override
    public String owner() {
        return base.owner();
    }

    @Override
    public String group() {
        return base.group();
    }

    @Override
    public Set<NPathPermission> getPermissions() {
        return base.getPermissions();
    }

    @Override
    public NPath setPermissions(NPathPermission... permissions) {
        base.setPermissions(permissions);
        return this;
    }

    @Override
    public NPath addPermissions(NPathPermission... permissions) {
        base.addPermissions(permissions);
        return this;
    }

    @Override
    public NPath removePermissions(NPathPermission... permissions) {
        base.removePermissions(permissions);
        return this;
    }

    @Override
    public boolean isName() {
        return base.isName();
    }

    @Override
    public int getLocationItemsCount() {
        return base.getLocationItemsCount();
    }

    @Override
    public boolean isRoot() {
        return base.isRoot();
    }

    @Override
    public NStream<NPath> walk(int maxDepth, NPathOption[] options) {
        return base.walk(maxDepth, options);
    }

    @Override
    public NPath subpath(int beginIndex, int endIndex) {
        return base.subpath(beginIndex, endIndex).toCompressedForm();
    }

    @Override
    public String getLocationItem(int index) {
        return base.getLocationItem(index);
    }

    @Override
    public List<String> getLocationItems() {
        return base.getLocationItems();
    }

    @Override
    public void moveTo(NPath other, NPathOption... options) {
        base.moveTo(other);
    }

    @Override
    public void copyTo(NPath other, NPathOption... options) {
        base.copyTo(other);
    }

    @Override
    public NPath getRoot() {
        return base.getRoot();
    }

    @Override
    public NPath walkDfs(NTreeVisitor<NPath> visitor, NPathOption... options) {
        base.walkDfs(visitor, options);
        return this;
    }

    @Override
    public NPath walkDfs(NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {
        base.walkDfs(visitor, maxDepth, options);
        return this;
    }

    @Override
    public NStream<NPath> walkGlob(NPathOption... options) {
        return base.walkGlob(options);
    }

    @Override
    public String toString() {
        return String.valueOf(compressedForm);
    }

    @Override
    public NFormat formatter(NSession session) {
        return new MyPathFormat(this).setSession(session != null ? session : getSession());
    }

    private static class MyPathFormat extends DefaultFormatBase<NFormat> {

        private final NCompressedPathBase p;

        public MyPathFormat(NCompressedPathBase p) {
            super(p.getSession(), "path");
            this.p = p;
        }

        public NString asFormattedString() {
            return NTexts.of(p.base.getSession()).ofStyled(p.compressedForm, NTextStyle.path());
        }

        @Override
        public void print(NPrintStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NCmdLine cmdLine) {
            return false;
        }

        @Override
        public int getSupportLevel(NSupportLevelContext context) {
            return DEFAULT_SUPPORT;
        }
    }
}
