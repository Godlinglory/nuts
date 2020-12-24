/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.NutsLogVerb;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author thevpc
 */
public class DefaultNutsIOUncompressAction implements NutsIOUncompressAction {

    private final NutsLogger LOG;

    private boolean skipRoot = false;
    private boolean safe = true;
    private boolean logProgress = false;
    private String format = "zip";
    private NutsInput source;
    private NutsOutput target;
    private DefaultNutsIOManager iom;
    private NutsSession session;
    private NutsProgressFactory progressMonitorFactory;

    public DefaultNutsIOUncompressAction(DefaultNutsIOManager iom) {
        this.iom = iom;
        LOG = iom.getWorkspace().log().of(DefaultNutsIOUncompressAction.class);
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public NutsIOUncompressAction setFormat(String format) {
        if (CoreStringUtils.isBlank(format)) {
            format = "zip";
        }
        if ("zip".equals(format)) {
            this.format = format;
        } else {
            throw new NutsUnsupportedArgumentException(iom.getWorkspace(), "Unsupported compression format " + format);
        }
        return this;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public NutsIOUncompressAction setSource(InputStream source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOUncompressAction setSource(File source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOUncompressAction setSource(Path source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOUncompressAction setSource(URL source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOUncompressAction setTarget(Path target) {
        this.target = iom.output().of(target);
        return this;
    }

    @Override
    public NutsIOUncompressAction setTarget(String target) {
        this.target = iom.output().of(target);
        return this;
    }

    @Override
    public NutsIOUncompressAction setTarget(File target) {
        this.target = iom.output().of(target);
        return this;
    }

    public DefaultNutsIOUncompressAction setSource(Object source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOUncompressAction from(String source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOUncompressAction to(String target) {
        this.target = iom.output().of(target);
        return this;
    }

    @Override
    public NutsIOUncompressAction from(Object source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOUncompressAction to(Object target) {
        this.target = iom.output().of(target);
        return this;
    }

    @Override
    public NutsIOUncompressAction to(NutsOutput target) {
        this.target = iom.output().of(target);
        return this;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    //    @Override
    public DefaultNutsIOUncompressAction setTarget(Object target) {
        this.target = iom.output().of(target);
        return this;
    }

    @Override
    public NutsIOUncompressAction setTarget(NutsOutput target) {
        this.target = iom.output().of(target);
        return this;
    }

    @Override
    public boolean isLogProgress() {
        return logProgress;
    }

    @Override
    public DefaultNutsIOUncompressAction setLogProgress(boolean value) {
        this.logProgress = value;
        return this;
    }

    @Override
    public NutsIOUncompressAction from(InputStream source) {
        return setSource(source);
    }

    @Override
    public NutsIOUncompressAction from(File source) {
        return setSource(source);
    }

    @Override
    public NutsIOUncompressAction from(Path source) {
        return setSource(source);
    }

    @Override
    public NutsIOUncompressAction from(URL source) {
        return setSource(source);
    }

    @Override
    public NutsIOUncompressAction to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsIOUncompressAction to(Path target) {
        return setTarget(target);
    }

    @Override
    public boolean isSafe() {
        return safe;
    }

    @Override
    public DefaultNutsIOUncompressAction setSafe(boolean value) {
        this.safe = value;
        return this;
    }

    @Override
    public NutsIOUncompressAction safe() {
        setSafe(true);
        return this;
    }

    @Override
    public NutsIOUncompressAction safe(boolean value) {
        setSafe(value);
        return this;
    }

    @Override
    public NutsIOUncompressAction logProgress() {
        setLogProgress(true);
        return this;
    }

    @Override
    public NutsIOUncompressAction logProgress(boolean value) {
        setLogProgress(value);
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsIOUncompressAction setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsIOUncompressAction run() {
        switch (getFormat()){
            case "zip":{
                runZip();
                break;
            }
            default:{
                throw new NutsUnsupportedArgumentException(iom.getWorkspace(),"Unsupported format "+getFormat());
            }
        }
        return this;
    }

    private void runZip(){
        NutsInput _source = source;
        if (_source == null) {
            throw new UnsupportedOperationException("missing source");
        }
        if (target == null) {
            throw new UnsupportedOperationException("missing target");
        }
        if (isLogProgress() || getProgressMonitorFactory() != null) {
            _source = iom.monitor().source(_source).setSession(session)
                    .progressFactory(getProgressMonitorFactory())
                    .logProgress(isLogProgress())
                    .createSource();
        }
        //boolean _source_isPath = _source.isPath();
//        if (!path.toLowerCase().startsWith("file://")) {
//            LOG.log(Level.FINE, "downloading url {0} to file {1}", new Object[]{path, file});
//        } else {
        LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.START).log( "uncompress {0} to {1}", _source, target);
//        }
        try {

            byte[] buffer = new byte[1024];

            //create output directory is not exists
            Path folder = target.getPath();
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            //get the zip file content
            InputStream _in = _source.open();
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
                                    throw new IOException("tot a single root zip");
                                }
                            }
                            if (fileName.startsWith(root)) {
                                fileName = fileName.substring(root.length());
                            } else {
                                throw new IOException("tot a single root zip");
                            }
                        }
                        if (fileName.endsWith("/")) {
                            Path newFile = folder.resolve(fileName);
                            Files.createDirectories(newFile);
                        } else {
                            Path newFile = folder.resolve(fileName);
                            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).log( "file unzip : " + newFile);
                            //create all non exists folders
                            //else you will hit FileNotFoundException for compressed folder
                            if (newFile.getParent() != null) {
                                Files.createDirectories(newFile.getParent());
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
            LOG.with().session(session).level(Level.CONFIG).verb(NutsLogVerb.FAIL).log( "error uncompressing {0} to {1} : {2}", _source.getSource(), target.getSource(), CoreStringUtils.exceptionToString(ex));
            throw new UncheckedIOException(ex);
        }
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
    public NutsIOUncompressAction setProgressMonitorFactory(NutsProgressFactory value) {
        this.progressMonitorFactory = value;
        return this;
    }

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsIOUncompressAction progressMonitorFactory(NutsProgressFactory value) {
        return setProgressMonitorFactory(value);
    }

    /**
     * set progress monitor. Will create a singeleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsIOUncompressAction setProgressMonitor(NutsProgressMonitor value) {
        this.progressMonitorFactory = value == null ? null : new SingletonNutsInputStreamProgressFactory(value);
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
    public NutsIOUncompressAction progressMonitor(NutsProgressMonitor value) {
        return setProgressMonitor(value);
    }

    @Override
    public NutsIOUncompressAction skipRoot(boolean value) {
        return setSkipRoot(value);
    }

    @Override
    public NutsIOUncompressAction skipRoot() {
        return skipRoot(true);
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NutsIOUncompressAction setSkipRoot(boolean value) {
        this.skipRoot = true;
        return this;
    }

    @Override
    public NutsIOUncompressAction setFormatOption(String option, Object value) {
        return this;
    }

    @Override
    public Object getFormatOption(String option) {
        return null;
    }
}
