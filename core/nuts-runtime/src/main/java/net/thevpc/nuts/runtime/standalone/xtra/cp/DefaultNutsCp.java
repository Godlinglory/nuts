/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.cp;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.util.*;
import net.thevpc.nuts.runtime.standalone.io.progress.DefaultNutsProgressEvent;
import net.thevpc.nuts.runtime.standalone.io.progress.SingletonNutsInputStreamProgressFactory;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultNutsCp implements NutsCp {

    private NutsLogger LOG;

    private NutsIOCopyValidator checker;
    private boolean skipRoot = false;
    private NutsStreamOrPath source;
    private NutsStreamOrPath target;
    private NutsSession session;
    private NutsProgressFactory progressMonitorFactory;
    private boolean interrupted;
    private boolean recursive;
    private boolean mkdirs;
    private Interruptible interruptibleInstance;
    private final NutsWorkspace ws;
    private Set<NutsPathOption> options=new LinkedHashSet<>();

    public DefaultNutsCp(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    private static Path transformPath(Path f, Path sourceBase, Path targetBase) {
        String fs = f.toString();
        String bs = sourceBase.toString();
        if (fs.startsWith(bs)) {
            String relative = fs.substring(bs.length());
            if (!relative.startsWith(File.separator)) {
                relative = File.separator + relative;
            }
            String x = targetBase + relative;
            return Paths.get(x);
        }
        throw new RuntimeException("Invalid path " + f);
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
            LOG = NutsLogger.of(DefaultNutsCp.class, session);
        }
        return LOG;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public NutsCp setSource(NutsPath source) {
        this.source = source == null ? null : NutsStreamOrPath.of(source);
        return this;
    }

    @Override
    public NutsCp setSource(InputStream source) {
        checkSession();
        this.source = source == null ? null : NutsStreamOrPath.of(source,session);
        return this;
    }

    @Override
    public NutsCp setSource(File source) {
        checkSession();
        this.source = source == null ? null : NutsStreamOrPath.of(NutsPath.of(source, session));
        return this;
    }

    @Override
    public NutsCp setSource(Path source) {
        this.source = source == null ? null : NutsStreamOrPath.of(NutsPath.of(source, session));
        return this;
    }

    @Override
    public NutsCp setSource(URL source) {
        this.source = source == null ? null : NutsStreamOrPath.of(NutsPath.of(source, session));
        return this;
    }

    @Override
    public NutsCp setSource(String source) {
        this.source = source == null ? null : NutsStreamOrPath.of(NutsPath.of(source, session));
        return this;
    }

    @Override
    public NutsCp from(String source) {
        this.source = source == null ? null : NutsStreamOrPath.of(NutsPath.of(source, session));
        return this;
    }

    @Override
    public NutsCp from(NutsPath source) {
        this.source = source == null ? null : NutsStreamOrPath.of(source);
        return this;
    }

    @Override
    public NutsCp from(InputStream source) {
        return setSource(source);
    }

    @Override
    public NutsCp from(File source) {
        return setSource(source);
    }

    @Override
    public NutsCp from(Path source) {
        return setSource(source);
    }

    @Override
    public NutsCp from(URL source) {
        return setSource(source);
    }

    @Override
    public NutsCp from(byte[] source) {
        return setSource(source);
    }

    @Override
    public NutsCp setSource(byte[] source) {
        checkSession();
        this.source = source == null ? null : NutsStreamOrPath.of(new ByteArrayInputStream(source),session);
        return this;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public NutsCp setTarget(OutputStream target) {
        checkSession();
        this.target = target == null ? null : NutsStreamOrPath.ofAnyOutputOrErr(target,session);
        return this;
    }

    @Override
    public NutsCp setTarget(NutsPrintStream target) {
        this.target = target == null ? null : NutsStreamOrPath.ofAnyOutputOrErr(target,session);
        return this;
    }

    @Override
    public NutsCp setTarget(NutsPath target) {
        this.target = target == null ? null : NutsStreamOrPath.of(target);
        return this;
    }

    @Override
    public NutsCp setTarget(Path target) {
        this.target = target == null ? null : NutsStreamOrPath.of(NutsPath.of(target, session));
        return this;
    }

    @Override
    public NutsCp setTarget(String target) {
        this.target = target == null ? null : NutsStreamOrPath.of(target, session);
        return this;
    }

    @Override
    public NutsCp setTarget(File target) {
        this.target = target == null ? null : NutsStreamOrPath.of(target, session);
        return this;
    }

    @Override
    public NutsCp to(OutputStream target) {
        return setTarget(target);
    }

    @Override
    public NutsCp to(NutsPrintStream target) {
        return setTarget(target);
    }

    @Override
    public NutsCp to(String target) {
        return setTarget(target);
    }

    @Override
    public NutsCp to(Path target) {
        return setTarget(target);
    }

    @Override
    public NutsCp to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsCp to(NutsPath target) {
        this.target = target == null ? null : NutsStreamOrPath.of(target);
        return this;
    }

    @Override
    public NutsIOCopyValidator getValidator() {
        return checker;
    }

    @Override
    public DefaultNutsCp setValidator(NutsIOCopyValidator checker) {
        this.checker = checker;
        return this;
    }

    @Override
    public boolean isRecursive() {
        return recursive;
    }

    @Override
    public NutsCp setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    @Override
    public boolean isMkdirs() {
        return mkdirs;
    }

    @Override
    public NutsCp setMkdirs(boolean mkdirs) {
        this.mkdirs = mkdirs;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsCp setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public byte[] getByteArrayResult() {
        checkSession();
        NutsMemoryPrintStream b = NutsPrintStream.ofInMemory(session);
        to(b);
        removeOptions(NutsPathOption.SAFE);
        run();
        return b.getBytes();
    }

    @Override
    public NutsCp run() {
        checkSession();
        NutsStreamOrPath _source = source;
        if (_source == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.formatted("missing source"));
        }
        if (target == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.formatted("missing target"));
        }
        if (_source.isPath() && _source.getPath().isDirectory()) {
            // this is a directory!!!
            if (!target.isPath()) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unsupported copy of directory to %s", target));
            }
            Path fromPath = _source.getPath().toFile();
            Path toPath = target.getPath().toFile();
            CopyData cd = new CopyData();
            if (options.contains(NutsPathOption.LOG) || getProgressMonitorFactory() != null) {
                prepareCopyFolder(fromPath, cd);
                copyFolderWithMonitor(fromPath, toPath, cd);
            } else {
                copyFolderNoMonitor(fromPath, toPath, cd);
            }
            return this;
        }
        copyStream();
        return this;
    }

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    @Override
    public NutsProgressFactory getProgressMonitorFactory() {
        return progressMonitorFactory;
    }

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsCp setProgressMonitorFactory(NutsProgressFactory value) {
        this.progressMonitorFactory = value;
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
    public NutsCp setProgressMonitor(NutsProgressMonitor value) {
        this.progressMonitorFactory = value == null ? null : new SingletonNutsInputStreamProgressFactory(value);
        return this;
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NutsCp setSkipRoot(boolean skipRoot) {
        this.skipRoot = skipRoot;
        return this;
    }

    public NutsCp interrupt() {
        if (interruptibleInstance != null) {
            interruptibleInstance.interrupt();
        }
        this.interrupted = true;
        return this;
    }

    private void checkInterrupted() {
        if (interrupted) {
            throw new NutsIOException(session, new InterruptException());
        }
    }

    private void prepareCopyFolder(Path d, CopyData f) {
        try {
            Files.walkFileTree(d, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    checkInterrupted();
                    f.folders++;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    checkInterrupted();
                    f.files++;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    checkInterrupted();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    checkInterrupted();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException exc) {
            throw new NutsIOException(session, exc);
        }
    }

    private void copyFolderWithMonitor(Path srcBase, Path targetBase, CopyData f) {
        checkSession();
        NutsSession session = getSession();
        long start = System.currentTimeMillis();
        NutsProgressMonitor m = CoreIOUtils.createProgressMonitor(CoreIOUtils.MonitorType.DEFAULT, srcBase, srcBase, session, options.contains(NutsPathOption.LOG), getProgressMonitorFactory());
        NutsText srcBaseMessage = NutsTexts.of(session).toText(srcBase);
        m.onStart(new DefaultNutsProgressEvent(srcBase,
                srcBaseMessage
                , 0, 0, 0, 0, f.files + f.folders, null, session, false));
        try {
            NutsSession finalSession = session;
            Files.walkFileTree(srcBase, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFolders++;
                    CoreIOUtils.mkdirs(transformPath(dir, srcBase, targetBase), session);
                    m.onProgress(new DefaultNutsProgressEvent(srcBase, srcBaseMessage, f.doneFiles + f.doneFolders, System.currentTimeMillis() - start, 0, 0, f.files + f.folders, null, finalSession, false));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFiles++;
                    copy(file, transformPath(file, srcBase, targetBase),options);
                    m.onProgress(new DefaultNutsProgressEvent(srcBase, srcBaseMessage, f.doneFiles + f.doneFolders, System.currentTimeMillis() - start, 0, 0, f.files + f.folders, null, finalSession, false));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    checkInterrupted();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    checkInterrupted();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException exc) {
            throw new NutsIOException(session, exc);
        } finally {
            m.onComplete(new DefaultNutsProgressEvent(srcBase, srcBaseMessage, f.files + f.folders, System.currentTimeMillis() - start, 0, 0, f.files + f.folders, null, session, false));
        }
    }

    public Path copy(Path source, Path target, Set<NutsPathOption> options) throws IOException {
        if (options.contains(NutsPathOption.INTERRUPTIBLE)) {
            if(Files.exists(target)){
                if(!options.contains(NutsPathOption.REPLACE_EXISTING)){
                    return null;
                }
            }
            try (InputStream in = CoreIOUtils.interruptible(Files.newInputStream(source))) {
                interruptibleInstance = (Interruptible) in;
                try (OutputStream out = Files.newOutputStream(target)) {
                    transferTo(in, out);
                }
            }
            return target;
        }
        return Files.copy(source, target, CoreIOUtils.asCopyOptions(options).toArray(new CopyOption[0]));
    }

    public long copy(InputStream in, Path target, Set<NutsPathOption> options)
            throws IOException {
        if (options.contains(NutsPathOption.INTERRUPTIBLE)) {
            in = CoreIOUtils.interruptible(in);
            interruptibleInstance = (Interruptible) in;
            try (OutputStream out = Files.newOutputStream(target)) {
                return transferTo(in, out);
            }
        }
        return Files.copy(in, target, CoreIOUtils.asCopyOptions(options).toArray(new CopyOption[0]));
    }

    public long copy(InputStream in, OutputStream out, Set<NutsPathOption> options)
            throws IOException {
        if (options.contains(NutsPathOption.INTERRUPTIBLE)) {
            in = CoreIOUtils.interruptible(in);
            interruptibleInstance = (Interruptible) in;
            return transferTo(in, out);
        }
        return CoreIOUtils.copy(in, out,session);
    }

    public long copy(Path source, OutputStream out) throws IOException {
        if (options.contains(NutsPathOption.INTERRUPTIBLE)) {
            try (InputStream in = CoreIOUtils.interruptible(Files.newInputStream(source))) {
                interruptibleInstance = (Interruptible) in;
                return transferTo(in, out);
            }
        }
        return Files.copy(source, out);
    }

    private long transferTo(InputStream in, OutputStream out) throws IOException {
        int DEFAULT_BUFFER_SIZE = 8192;
        Objects.requireNonNull(out, "out");
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            checkInterrupted();
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }

    private void copyFolderNoMonitor(Path srcBase, Path targetBase, CopyData f) {
        try {
            Files.walkFileTree(srcBase, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFolders++;
                    CoreIOUtils.mkdirs(transformPath(dir, srcBase, targetBase), session);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFiles++;
                    copy(file, transformPath(file, srcBase, targetBase), options);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    checkInterrupted();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    checkInterrupted();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException exc) {
            throw new NutsIOException(session, exc);
        }
    }

    private void copyStream() {
        checkSession();
        NutsStreamOrPath _source = source;
        boolean _target_isPath = target.isPath() && target.getPath().isFile();
        boolean safe=options.contains(NutsPathOption.SAFE);
        if (checker != null && !_target_isPath && !safe) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.formatted("unsupported validation if neither safeCopy is armed nor path is defined"));
        }
        if (options.contains(NutsPathOption.LOG) || getProgressMonitorFactory() != null) {
            NutsInputStreamMonitor monitor = NutsInputStreamMonitor.of(session);
            NutsString name =null;
            if (_source.isInputStream()) {
                InputStream inputStream = _source.getInputStream();
                monitor.setSource(inputStream);
            } else {
                monitor.setSource(_source.getPath());
            }
            _source = NutsStreamOrPath.ofAnyInputOrErr(
                    monitor
                    .setProgressFactory(getProgressMonitorFactory())
                    .setLogProgress(options.contains(NutsPathOption.LOG))
                    .create(),session);
        }
        NutsLoggerOp lop = _LOGOP(session);
        if(lop.isLoggable(Level.FINEST)) {
            NutsString loggedSrc = _source == null ? null : _source.getStreamMetaData().getFormattedPath();
            NutsString loggedTarget = target == null ? null : target.getStreamMetaData().getFormattedPath();
            lop.level(Level.FINEST).verb(NutsLogVerb.START).log(NutsMessage.jstyle("copy {0} to {1}",
                    loggedSrc,
                    loggedTarget));
        }
        try {
            if (safe) {
                Path temp = null;
                if (_target_isPath) {
                    Path to = target.getPath().toFile();
                    CoreIOUtils.mkdirs(to.getParent(), session);
                    temp = to.resolveSibling(to.getFileName() + "~");
                } else {
                    temp = NutsTmp.of(getSession())
                            .createTempFile("temp~").toFile();
                }
                try {
                    if (_source.isPath() && _source.getPath().isFile()) {
                        copy(_source.getPath().toFile(), temp, new HashSet<>(Arrays.asList(NutsPathOption.REPLACE_EXISTING)));
                    } else {
                        try (InputStream ins = _source.getInputStream()) {
                            copy(ins, temp, new HashSet<>(Arrays.asList(NutsPathOption.REPLACE_EXISTING)));
                        }
                    }
                    _validate(temp);
                    if (_target_isPath) {
                        try {
                            Files.move(temp, target.getPath().toFile(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (FileSystemException e) {
                            // happens when the file is used by another process
                            // in that case try to check if the file needs to be copied
                            //if not, return safely!
                            if (CoreIOUtils.compareContent(temp, target.getPath().toFile(),session)) {
                                //cannot write the file (used by another process), but no pbm because does not need to
                                return;
                            }
                            throw e;
                        }
                        temp = null;
                    } else {
                        try (OutputStream ops = target.getOutputStream()) {
                            copy(temp, ops);
                        }
                    }
                } finally {
                    if (temp != null && Files.exists(temp)) {
                        Files.delete(temp);
                    }
                }
            } else {
                if (_target_isPath) {
                    Path to = target.getPath().toFile();
                    CoreIOUtils.mkdirs(to.getParent(), session);
                    if (_source.isPath() && _source.getPath().isFile()) {
                        copy(_source.getPath().toFile(), to, new HashSet<>(Arrays.asList(NutsPathOption.REPLACE_EXISTING)));
                    } else {
                        try (InputStream ins = _source.getInputStream()) {
                            copy(ins, to, new HashSet<>(Arrays.asList(NutsPathOption.REPLACE_EXISTING)));
                        }
                    }
                    _validate(to);
                } else {
                    ByteArrayOutputStream bos = null;
                    if (checker != null) {
                        bos = new ByteArrayOutputStream();
                        if (_source.isPath() && _source.getPath().isFile()) {
                            copy(_source.getPath().toFile(), bos);
                        } else {
                            try (InputStream ins = _source.getInputStream()) {
                                copy(ins, bos,options);
                            }
                        }
                        try (OutputStream ops = target.getOutputStream()) {
                            copy(new ByteArrayInputStream(bos.toByteArray()), ops,options);
                        }
                        _validate(bos.toByteArray());
                    } else {
                        if (_source.isPath() && _source.getPath().isFile()) {
                            try (OutputStream ops = target.getOutputStream()) {
                                copy(_source.getPath().toFile(), ops);
                            }
                        } else {
                            try (InputStream ins = _source.getInputStream()) {
                                try (OutputStream ops = target.getOutputStream()) {
                                    copy(ins, ops,options);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            lop.level(Level.CONFIG).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("error copying {0} to {1} : {2}", _source.getValue(),
                            target.getValue(), ex));
            throw new NutsIOException(session, ex);
        }
    }

    private void _validate(Path temp) {
        if (checker != null) {
            try (InputStream in = Files.newInputStream(temp)) {
                checker.validate(in);
            } catch (NutsIOCopyValidationException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new NutsIOCopyValidationException(session, NutsMessage.cstyle("validate file %s failed", temp), ex);
            }
        }
    }

    private void _validate(byte[] temp) {
        if (checker != null) {
            try (InputStream in = new ByteArrayInputStream(temp)) {
                checker.validate(in);
            } catch (NutsIOCopyValidationException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new NutsIOCopyValidationException(session, NutsMessage.cstyle("validate file failed"), ex);
            }
        }
    }

    private static class CopyData {

        long files;
        long folders;
        long doneFiles;
        long doneFolders;
    }

    @Override
    public NutsCp addOptions(NutsPathOption... pathOptions) {
        if(pathOptions!=null){
            for (NutsPathOption o : pathOptions) {
                if(o!=null){
                    options.add(o);
                }
            }
        }
        return this;
    }

    @Override
    public NutsCp removeOptions(NutsPathOption... pathOptions) {
        if(pathOptions!=null){
            for (NutsPathOption o : pathOptions) {
                if(o!=null){
                    options.remove(o);
                }
            }
        }
        return this;
    }

    @Override
    public Set<NutsPathOption> getOptions() {
        return new LinkedHashSet<>(options);
    }
}
