package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;

import java.io.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsIOManager implements NutsIOManager {

    private DefaultNutsIOModel model;
    private NutsSession session;

    public DefaultNutsIOManager(DefaultNutsIOModel model) {
        this.model = model;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsIOManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public DefaultNutsIOModel getModel() {
        return model;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public String expandPath(String path) {
        checkSession();
        return model.expandPath(path);
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), getSession());
    }

    @Override
    public String expandPath(String path, String baseFolder) {
        checkSession();
        return model.expandPath(path,baseFolder);
    }

    @Override
    public InputStream nullInputStream() {
        checkSession();
        return model.nullInputStream();
    }

    @Override
    public PrintStream nullPrintStream() {
        checkSession();
        return model.nullPrintStream();
    }

    @Override
    public PrintStream createPrintStream(OutputStream out, NutsTerminalMode expectedMode) {
        checkSession();
        return model.createPrintStream(out, expectedMode, session);
    }

    @Override
    public NutsTempAction tmp() {
        checkSession();
        return model.tmp().setSession(session);
    }

//    @Override
//    public PrintStream createPrintStream(Path out) {
//        if (out == null) {
//            return null;
//        }
//        try {
//            return new PrintStream(Files.newOutputStream(out));
//        } catch (IOException ex) {
//            throw new IllegalArgumentException(ex);
//        }
//    }
//
//    @Override
//    public PrintStream createPrintStream(File out) {
//        if (out == null) {
//            return null;
//        }
//        try {
//            return new PrintStream(out);
//        } catch (IOException ex) {
//            throw new IllegalArgumentException(ex);
//        }
//    }
    @Override
    public NutsIOCopyAction copy() {
        return model.copy().setSession(session);
    }

    @Override
    public NutsIOProcessAction ps() {
        return model.ps().setSession(session);
    }

    @Override
    public NutsIOCompressAction compress() {
        return model.compress().setSession(session);
    }

    @Override
    public NutsIOUncompressAction uncompress() {
        return model.uncompress().setSession(session);
    }

    @Override
    public NutsIODeleteAction delete() {
        return model.delete().setSession(session);
    }

    @Override
    public NutsMonitorAction monitor() {
        return model.monitor().setSession(session);
    }

    @Override
    public NutsIOHashAction hash() {
        return model.hash().setSession(session);
    }

    @Override
    public NutsInputAction input() {
        return model.input().setSession(session);
    }

    @Override
    public NutsOutputAction output() {
        return model.output().setSession(session);
    }
}
