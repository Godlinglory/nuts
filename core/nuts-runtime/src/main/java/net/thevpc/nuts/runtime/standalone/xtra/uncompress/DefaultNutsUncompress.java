/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.uncompress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.progress.SingletonNutsInputStreamProgressFactory;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author thevpc
 */
public class DefaultNutsUncompress implements NutsUncompress {

    private NutsLogger LOG;

    private boolean skipRoot = false;
    private boolean safe = true;
    private String format = "zip";
    private NutsWorkspace ws;
    private NutsInputSource source;
    private NutsOutputTarget target;
    private NutsSession session;
    private NutsProgressFactory progressFactory;
    private Set<NutsPathOption> options = new LinkedHashSet<>();

    public DefaultNutsUncompress(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
//        LOG = ws.log().of(DefaultNutsUncompress.class);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsUncompress.class, session);
        }
        return LOG;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public NutsUncompress setFormat(String format) {
        checkSession();
        if (NutsBlankable.isBlank(format)) {
            format = "zip";
        }
        switch (format) {
            case "zip":
            case "gzip":
            case "gz": {
                this.format = format;
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.ofCstyle("unsupported compression format %s", format));
            }
        }
        return this;
    }

    @Override
    public NutsInputSource getSource() {
        return source;
    }

    protected void checkSession() {
        NutsSessionUtils.checkSession(ws, session);
    }

    @Override
    public NutsUncompress setSource(NutsInputSource source) {
        this.source = source;
        return this;
    }

    @Override
    public NutsUncompress setTarget(NutsOutputTarget target) {
        this.target = target;
        return this;
    }


    @Override
    public NutsUncompress setSource(InputStream source) {
        checkSession();
        this.source = source == null ? null : NutsIO.of(session).createInputSource(source);
        return this;
    }

    @Override
    public NutsUncompress setSource(NutsPath source) {
        checkSession();
        this.source = source;
        return this;
    }

    @Override
    public NutsUncompress setSource(File source) {
        this.source = source == null ? null : NutsPath.of(source, session);
        return this;
    }

    @Override
    public NutsUncompress setSource(Path source) {
        this.source = source == null ? null : NutsPath.of(source, session);
        return this;
    }

    @Override
    public NutsUncompress setSource(URL source) {
        this.source = source == null ? null : NutsPath.of(source, session);
        return this;
    }

    @Override
    public NutsUncompress setTarget(Path target) {
        this.target = target == null ? null : NutsPath.of(target, session);
        return this;
    }

    @Override
    public NutsUncompress setTarget(File target) {
        this.target = target == null ? null : NutsPath.of(target, session);
        return this;
    }

    public NutsUncompress setTarget(NutsPath target) {
        this.target = target;
        return this;
    }

    @Override
    public NutsUncompress from(NutsPath source) {
        this.source = source;
        return this;
    }

    @Override
    public NutsUncompress to(NutsPath target) {
        this.target = target;
        return this;
    }

    @Override
    public NutsOutputTarget getTarget() {
        return target;
    }

    //    @Override


    @Override
    public NutsUncompress from(InputStream source) {
        return setSource(source);
    }

    @Override
    public NutsUncompress from(File source) {
        return setSource(source);
    }

    @Override
    public NutsUncompress from(Path source) {
        return setSource(source);
    }

    @Override
    public NutsUncompress from(URL source) {
        return setSource(source);
    }

    @Override
    public NutsUncompress to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsUncompress to(Path target) {
        return setTarget(target);
    }

    @Override
    public boolean isSafe() {
        return safe;
    }

    @Override
    public DefaultNutsUncompress setSafe(boolean value) {
        this.safe = value;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsUncompress setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public NutsUncompress run() {
        checkSession();
        String format = getFormat();
        if (NutsBlankable.isBlank(format)) {
            format = "zip";
        }
        NutsInputSource _source = source;
        NutsUtils.requireNonNull(source, getSession(), "source");
        NutsUtils.requireNonNull(target, getSession(), "target");
        NutsPath _target = asValidTargetPath();
        if (_target == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofCstyle("invalid target %s", target));
        }
        if (options.contains(NutsPathOption.LOG)
                || options.contains(NutsPathOption.TRACE)
                || getProgressFactory() != null) {
            NutsInputStreamMonitor monitor = NutsInputStreamMonitor.of(session);
            monitor.setOrigin(_source);
            monitor.setLogProgress(options.contains(NutsPathOption.LOG));
            monitor.setTraceProgress(options.contains(NutsPathOption.TRACE));
            monitor.setProgressFactory(getProgressFactory());
            monitor.setSource(_source);
            _source = NutsIO.of(session).createInputSource(monitor.create());
        }
        //boolean _source_isPath = _source.isPath();
//        if (!path.toLowerCase().startsWith("file://")) {
//            LOG.log(Level.FINE, "downloading url {0} to file {1}", new Object[]{path, file});
//        } else {
        _LOGOP(session).level(Level.FINEST).verb(NutsLoggerVerb.START)
                .log(NutsMessage.ofJstyle("uncompress {0} to {1}", _source, target));
        Path folder = _target.toFile();
        NutsPath.of(folder, session).mkdirs();

        switch (format) {
            case "zip": {
                runZip();
                break;
            }
            case "gzip":
            case "gz": {
                runGZip();
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.ofCstyle("unsupported format %s", format));
            }
        }
        return this;
    }

    private NutsPath asValidTargetPath() {
        if (target != null) {
            if (target instanceof NutsPath) {
                NutsPath p = (NutsPath) target;
                if (p.isFile()) {
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public NutsUncompress visit(NutsUncompressVisitor visitor) {
        checkSession();
        String format = getFormat();
        if (NutsBlankable.isBlank(format)) {
            format = "zip";
        }
        NutsUtils.requireNonNull(source, getSession(), "source");
        NutsUtils.requireNonNull(target, getSession(), "target");
        NutsPath _target = asValidTargetPath();
        if (_target == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofCstyle("invalid target %s", target));
        }
        NutsInputSource _source = source;
        if (options.contains(NutsPathOption.LOG)
                || options.contains(NutsPathOption.TRACE)
                || getProgressFactory() != null) {
            NutsInputStreamMonitor monitor = NutsInputStreamMonitor.of(session);
            monitor.setOrigin(source);
            monitor.setLogProgress(options.contains(NutsPathOption.LOG));
            monitor.setTraceProgress(options.contains(NutsPathOption.TRACE));
            monitor.setProgressFactory(getProgressFactory());
            monitor.setSource(source);
            _source = NutsIO.of(session).createInputSource(monitor.create());
        }

        _LOGOP(session).level(Level.FINEST).verb(NutsLoggerVerb.START)
                .log(NutsMessage.ofJstyle("uncompress {0} to {1}", _source, target));
        Path folder = _target.toFile();
        NutsPath.of(folder, session).mkdirs();

        switch (format) {
            case "zip": {
                visitZip(visitor);
                break;
            }
            case "gzip":
            case "gz": {
                visitGZip(visitor);
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.ofCstyle("unsupported format %s", format));
            }
        }
        return this;
    }

    private void runZip() {
        checkSession();
        NutsInputSource _source = source;
        try {
            byte[] buffer = new byte[1024];
            //create output directory is not exists
            Path folder = asValidTargetPath().toFile();
            //get the zip file content
            InputStream _in = _source.getInputStream();
            try {
                try (ZipInputStream zis = new ZipInputStream(_in)) {
                    //get the zipped file list entry
                    ZipEntry ze = zis.getNextEntry();
                    String root = null;
                    while (ze != null) {

                        String fileName = ze.getName();
                        if (skipRoot) {
                            if (root == null) {
                                if (fileName.endsWith("/")) {
                                    root = fileName;
                                    ze = zis.getNextEntry();
                                    continue;
                                } else {
                                    throw new IOException("not a single root zip");
                                }
                            }
                            if (fileName.startsWith(root)) {
                                fileName = fileName.substring(root.length());
                            } else {
                                throw new IOException("not a single root zip");
                            }
                        }
                        if (fileName.endsWith("/")) {
                            Path newFile = folder.resolve(fileName);
                            NutsPath.of(newFile, session).mkdirs();
                        } else {
                            Path newFile = folder.resolve(fileName);
                            _LOGOP(session).level(Level.FINEST).verb(NutsLoggerVerb.WARNING)
                                    .log(NutsMessage.ofJstyle("file unzip : {0}", newFile));
                            //create all non exists folders
                            //else you will hit FileNotFoundException for compressed folder
                            if (newFile.getParent() != null) {
                                NutsPath.of(newFile, session).mkParentDirs();
                            }
                            try (OutputStream fos = Files.newOutputStream(newFile)) {
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, len);
                                }
                            }
                        }
                        ze = zis.getNextEntry();
                    }
                    zis.closeEntry();
                }
            } finally {
                _in.close();
            }
        } catch (IOException ex) {
            _LOGOP(session).level(Level.CONFIG).verb(NutsLoggerVerb.FAIL).log(
                    NutsMessage.ofJstyle("error uncompressing {0} to {1} : {2}",
                            _source, target, ex));
            throw new NutsIOException(session, ex);
        }
    }

    private void visitZip(NutsUncompressVisitor visitor) {
        checkSession();
        NutsInputSource _source = source;
        try {
            //get the zip file content
            InputStream _in = _source.getInputStream();
            try {
                try (ZipInputStream zis = new ZipInputStream(_in)) {
                    //get the zipped file list entry
                    ZipEntry ze = zis.getNextEntry();
                    String root = null;
                    while (ze != null) {

                        String fileName = ze.getName();
                        if (skipRoot) {
                            if (root == null) {
                                if (fileName.endsWith("/")) {
                                    root = fileName;
                                    ze = zis.getNextEntry();
                                    continue;
                                } else {
                                    throw new IOException("not a single root zip");
                                }
                            }
                            if (fileName.startsWith(root)) {
                                fileName = fileName.substring(root.length());
                            } else {
                                throw new IOException("not a single root zip");
                            }
                        }
                        if (fileName.endsWith("/")) {
                            if (!visitor.visitFolder(fileName)) {
                                break;
                            }
                        } else {
                            if (!visitor.visitFile(fileName, new InputStream() {
                                @Override
                                public int read() throws IOException {
                                    return zis.read();
                                }

                                @Override
                                public int read(byte[] b, int off, int len) throws IOException {
                                    return zis.read(b, off, len);
                                }

                                @Override
                                public int read(byte[] b) throws IOException {
                                    return zis.read(b);
                                }
                            })) {
                                break;
                            }
                        }
                        ze = zis.getNextEntry();
                    }
                    zis.closeEntry();
                }
            } finally {
                _in.close();
            }
        } catch (IOException ex) {
            _LOGOP(session).level(Level.CONFIG).verb(NutsLoggerVerb.FAIL)
                    .log(NutsMessage.ofJstyle("error uncompressing {0} to {1} : {2}",
                            _source, target, ex));
            throw new NutsIOException(session, ex);
        }
    }

    private void runGZip() {
        NutsInputSource _source = source;
        try {
            String baseName = _source.getInputMetaData().getName().orElse("no-name");
            byte[] buffer = new byte[1024];

            //create output directory is not exists
            Path folder = asValidTargetPath().toFile();

            //get the zip file content
            InputStream _in = _source.getInputStream();
            try {
                try (GZIPInputStream zis = new GZIPInputStream(_in)) {
                    String n = NutsPath.of(baseName, session).getName();
                    if (n.endsWith(".gz")) {
                        n = n.substring(0, n.length() - 3);
                    }
                    if (n.isEmpty()) {
                        n = "data";
                    }
                    //get the zipped file list entry
                    Path newFile = folder.resolve(n);
                    _LOGOP(session).level(Level.FINEST).verb(NutsLoggerVerb.WARNING)
                            .log(NutsMessage.ofJstyle("file unzip : {0}", newFile));
                    //create all non exists folders
                    //else you will hit FileNotFoundException for compressed folder
                    if (newFile.getParent() != null) {
                        NutsPath.of(newFile, session).mkParentDirs();
                    }
                    try (OutputStream fos = Files.newOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            } finally {
                _in.close();
            }
        } catch (IOException ex) {
            _LOGOP(session).level(Level.CONFIG).verb(NutsLoggerVerb.FAIL)
                    .log(NutsMessage.ofJstyle("error uncompressing {0} to {1} : {2}", _source,
                            target, ex));
            throw new NutsIOException(session, ex);
        }
    }

    private void visitGZip(NutsUncompressVisitor visitor) {
        NutsInputSource _source = source;
        try {
            String baseName = _source.getInputMetaData().getName().orElse("no-name");
            byte[] buffer = new byte[1024];

            //get the zip file content
            InputStream _in = _source.getInputStream();
            try {
                try (GZIPInputStream zis = new GZIPInputStream(_in)) {
                    String n = NutsPath.of(baseName, session).getName();
                    if (n.endsWith(".gz")) {
                        n = n.substring(0, n.length() - 3);
                    }
                    if (n.isEmpty()) {
                        n = "data";
                    }
                    //get the zipped file list entry
                    visitor.visitFile(n, new InputStream() {
                        @Override
                        public int read() throws IOException {
                            return zis.read();
                        }

                        @Override
                        public int read(byte[] b, int off, int len) throws IOException {
                            return zis.read(b, off, len);
                        }

                        @Override
                        public int read(byte[] b) throws IOException {
                            return zis.read(b);
                        }
                    });
                }
            } finally {
                _in.close();
            }
        } catch (IOException ex) {
            _LOGOP(session).level(Level.CONFIG).verb(NutsLoggerVerb.FAIL)
                    .log(NutsMessage.ofJstyle("error uncompressing {0} to {1} : {2}", _source,
                            target, ex));
            throw new NutsIOException(session, ex);
        }
    }

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    @Override
    public NutsProgressFactory getProgressFactory() {
        return progressFactory;
    }

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsUncompress setProgressFactory(NutsProgressFactory value) {
        this.progressFactory = value;
        return this;
    }

    /**
     * set progress monitor. Will create a singeleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsUncompress setProgressMonitor(NutsProgressListener value) {
        this.progressFactory = value == null ? null : new SingletonNutsInputStreamProgressFactory(value);
        return this;
    }

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsUncompress progressMonitor(NutsProgressListener value) {
        return setProgressMonitor(value);
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NutsUncompress setSkipRoot(boolean value) {
        this.skipRoot = value;
        return this;
    }

    @Override
    public NutsUncompress setFormatOption(String option, Object value) {
        return this;
    }

    @Override
    public Object getFormatOption(String option) {
        return null;
    }

    @Override
    public NutsUncompress addOptions(NutsPathOption... pathOptions) {
        if (pathOptions != null) {
            for (NutsPathOption o : pathOptions) {
                if (o != null) {
                    options.add(o);
                }
            }
        }
        return this;
    }

    @Override
    public NutsUncompress removeOptions(NutsPathOption... pathOptions) {
        if (pathOptions != null) {
            for (NutsPathOption o : pathOptions) {
                if (o != null) {
                    options.remove(o);
                }
            }
        }
        return this;
    }

    @Override
    public NutsUncompress clearOptions() {
        options.clear();
        return this;
    }

    @Override
    public Set<NutsPathOption> getOptions() {
        return new LinkedHashSet<>(options);
    }

}
