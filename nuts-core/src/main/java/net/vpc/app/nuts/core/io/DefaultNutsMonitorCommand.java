/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.io;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.CoreNutsConstants;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author vpc
 */
public class DefaultNutsMonitorCommand implements NutsMonitorCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsMonitorCommand.class.getName());
    private final NutsWorkspace ws;
    private String sourceType;
    private Object source;
    private Object sourceOrigin;
    private String sourceName;
    private long length = -1;
    private NutsSession session;
    private boolean includeDefaultFactory;
    private NutsInputStreamProgressFactory progressFactory;

    public DefaultNutsMonitorCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsMonitorCommand session(NutsSession s) {
        return setSession(s);
    }

    @Override
    public NutsMonitorCommand setSession(NutsSession s) {
        this.session = s;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsMonitorCommand name(String s) {
        return setName(s);
    }

    @Override
    public NutsMonitorCommand setName(String s) {
        this.sourceName = s;
        return this;
    }

    @Override
    public String getName() {
        return sourceName;
    }

    @Override
    public NutsMonitorCommand origin(Object s) {
        return setOrigin(s);
    }

    @Override
    public NutsMonitorCommand setOrigin(Object s) {
        this.sourceOrigin = s;
        return this;
    }

    @Override
    public Object getOrigin() {
        return sourceOrigin;
    }

    @Override
    public NutsMonitorCommand length(long len) {
        return setLength(len);
    }

    @Override
    public NutsMonitorCommand setLength(long len) {
        this.length = len;
        return this;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public NutsMonitorCommand source(String path) {
        return setSource(path);
    }

    @Override
    public NutsMonitorCommand setSource(String path) {
        this.source = path;
        this.sourceType = "string";
        return this;
    }

    @Override
    public NutsMonitorCommand source(InputStream inputStream) {
        return setSource(inputStream);
    }

    @Override
    public NutsMonitorCommand setSource(InputStream path) {
        this.source = path;
        this.sourceType = "stream";
        return this;
    }

    @Override
    public InputStream create() {
        if (source == null || sourceType == null) {
            throw new NutsIllegalArgumentException(ws, "Missing Source");
        }
        switch (sourceType) {
            case "string": {
                return monitorInputStream((String) source, sourceOrigin, sourceName, session);
            }
            case "stream": {
                return monitorInputStream((InputStream) source, length, sourceName, session);
            }
            default:
                throw new NutsUnsupportedArgumentException(ws, sourceType);
        }
    }

    public boolean acceptMonitoring(String path, Object source, String sourceName, NutsSession session) {
        Object o = session.getProperty("monitor-allowed");
        if (o != null) {
            o = ws.commandLine().create(new String[]{String.valueOf(o)}).next().getBoolean();
        }
        boolean monitorable = true;
        if (o instanceof Boolean) {
            monitorable = ((Boolean) o).booleanValue();
        }
        if (monitorable) {
            if (source instanceof NutsId) {
                NutsId d = (NutsId) source;
                if (NutsConstants.QueryFaces.CONTENT_HASH.equals(d.getFace())) {
                    monitorable = false;
                }
                if (NutsConstants.QueryFaces.DESCRIPTOR_HASH.equals(d.getFace())) {
                    monitorable = false;
                }
            }
            if (monitorable) {
                if (path.endsWith("/" + CoreNutsConstants.Files.DOT_FOLDERS) || path.endsWith("/" + CoreNutsConstants.Files.DOT_FILES)
                        || path.endsWith(".pom") || path.endsWith(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION)
                        || path.endsWith(".xml") || path.endsWith(".json")) {
                    monitorable = false;
                }
            }
        }
        if (!CoreCommonUtils.getSysBoolNutsProperty("monitor.enabled", true)) {
            monitorable = false;
        }
        if (!LOG.isLoggable(Level.INFO)) {
            monitorable = false;
        }
        return monitorable;
    }

    public InputStream monitorInputStream(String path, Object source, String sourceName, NutsSession session) {
        if (session == null) {
            session = ws.createSession();
        }
        if (CoreStringUtils.isBlank(path)) {
            throw new UncheckedIOException(new IOException("Missing Path"));
        }
        if (CoreStringUtils.isBlank(sourceName)) {
            sourceName = String.valueOf(path);
        }
        if (session == null) {
            session = ws.createSession();
        }
        NutsInputStreamProgressMonitor monitor = createProgressMonitor(path, source, sourceName, session);
        boolean verboseMode
                = CoreCommonUtils.getSysBoolNutsProperty("monitor.start", false)
                || ws.config().options().getLogConfig() != null && ws.config().options().getLogConfig().getLogLevel() == Level.FINEST;
        InputSource stream = null;
        long size = -1;
        try {
            if (verboseMode && monitor != null) {
                monitor.onStart(new DefaultNutsInputStreamEvent(source, sourceName, 0, 0, 0, 0, size, null, session));
            }
            stream = CoreIOUtils.createInputSource(path);
            size = stream.length();
        } catch (UncheckedIOException e) {
            if (verboseMode && monitor != null) {
                monitor.onComplete(new DefaultNutsInputStreamEvent(source, sourceName, 0, 0, 0, 0, size, e, session));
            }
            throw e;
        }
        if (path.toLowerCase().startsWith("file://")) {
            LOG.log(Level.FINE, "[START  ] Downloading file {0}", new Object[]{path});
        } else {
            LOG.log(Level.FINEST, "[START  ] Download url {0}", new Object[]{path});
        }

        InputStream openedStream = stream.open();
        if (monitor == null) {
            return openedStream;
        }
        NutsInputStreamProgressMonitor finalMonitor = monitor;
        if (!verboseMode) {
            monitor.onStart(new DefaultNutsInputStreamEvent(source, sourceName, 0, 0, 0, 0, size, null, session));
        }
        return CoreIOUtils.monitor(openedStream, source, sourceName, size, new SilentStartNutsInputStreamProgressMonitorAdapter(finalMonitor, path), session);

    }

    public InputStream monitorInputStream(InputStream stream, long length, String name, NutsSession session) {
        if (length > 0) {
            if (session == null) {
                session = ws.createSession();
            }
            NutsInputStreamProgressMonitor m = createProgressMonitor(stream, stream, name, session);
            if (m == null) {
                return stream;
            }
            return CoreIOUtils.monitor(stream, null, (name == null ? "Stream" : name), length, m, session);
        } else {
            if (stream instanceof InputStreamMetadataAware) {
                if (session == null) {
                    session = ws.createSession();
                }
                NutsInputStreamProgressMonitor m = createProgressMonitor(stream, stream, name, session);
                if (m == null) {
                    return stream;
                }
                return CoreIOUtils.monitor(stream, null, m, session);
            } else {
                return stream;
            }
        }
    }

    private NutsInputStreamProgressMonitor createProgressMonitor(Object source, Object sourceOrigin, String sourceName, NutsSession session) {
        if (!isIncludeDefaultFactory()) {
            if (progressFactory != null) {
                return progressFactory.create(source, sourceOrigin, sourceName, session);
            }
            return new DefaultNutsInputStreamProgressFactory().create(source, sourceOrigin, sourceName, session);
        } else {
            NutsInputStreamProgressMonitor m0 = new DefaultNutsInputStreamProgressFactory().create(source, sourceOrigin, sourceName, session);
            NutsInputStreamProgressMonitor m1 = null;
            if (progressFactory != null) {
                m1 = progressFactory.create(source, sourceOrigin, sourceName, session);
            }
            if (m1 == null) {
                return m0;
            }
            if (m0 == null) {
                return m1;
            }
            ;
            return new NutsInputStreamProgressMonitorList(new NutsInputStreamProgressMonitor[]{m0, m1});
        }
    }

    /**
     * when true, will include default factory (console) even if progressFactory is defined
     * @return true if always include default factory
     * @since 0.5.8
     */
    @Override
    public boolean isIncludeDefaultFactory() {
        return includeDefaultFactory;
    }

    /**
     * when true, will include default factory (console) even if progressFactory is defined
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsMonitorCommand setIncludeDefaultFactory(boolean value) {
        this.includeDefaultFactory = value;
        return this;
    }

    /**
     * when true, will include default factory (console) even if progressFactory is defined
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsMonitorCommand includeDefaultFactory(boolean value) {
        return setIncludeDefaultFactory(value);
    }

    /**
     *always include default factory (console) even if progressFactory is defined
     *
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsMonitorCommand includeDefaultFactory() {
        return includeDefaultFactory(true);
    }

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    @Override
    public NutsInputStreamProgressFactory getProgressFactory() {
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
    public NutsMonitorCommand setProgressFactory(NutsInputStreamProgressFactory value) {
        this.progressFactory = value;
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
    public NutsMonitorCommand progressFactory(NutsInputStreamProgressFactory value) {
        return setProgressFactory(value);
    }

    private static class SilentStartNutsInputStreamProgressMonitorAdapter implements NutsInputStreamProgressMonitor {
        private final NutsInputStreamProgressMonitor finalMonitor;
        private final String path;

        public SilentStartNutsInputStreamProgressMonitorAdapter(NutsInputStreamProgressMonitor finalMonitor, String path) {
            this.finalMonitor = finalMonitor;
            this.path = path;
        }

        @Override
        public void onStart(NutsInputStreamEvent event) {
        }

        @Override
        public void onComplete(NutsInputStreamEvent event) {
            finalMonitor.onComplete(event);
            if (event.getException() != null) {
                LOG.log(Level.FINEST, "[ERROR    ] Download Failed    : {0}", new Object[]{path});
            } else {
                LOG.log(Level.FINEST, "[SUCCESS  ] Download Succeeded : {0}", new Object[]{path});
            }
        }

        @Override
        public boolean onProgress(NutsInputStreamEvent event) {
            return finalMonitor.onProgress(event);
        }
    }
}
