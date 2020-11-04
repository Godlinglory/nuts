package net.thevpc.nuts.runtime.io;

import net.thevpc.nuts.NutsIOLockAction;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

import java.io.File;
import java.nio.file.Path;

public abstract class AbstractNutsIOLockAction implements NutsIOLockAction {
    private NutsWorkspace ws;
    private Object source;
    private Object resource;
    private NutsSession session;

    public AbstractNutsIOLockAction(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public Object getResource() {
        return resource;
    }

    @Override
    public NutsIOLockAction source(Object source) {
        return setSource(source);
    }

    @Override
    public NutsIOLockAction setSource(Object source) {
        this.source=source;
        return this;
    }

    @Override
    public NutsIOLockAction setResource(File source) {
        this.resource=source;
        return this;
    }

    @Override
    public NutsIOLockAction setResource(Path source) {
        this.resource=source;
        return this;
    }

    @Override
    public NutsIOLockAction resource(Object source) {
        this.resource=source;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsIOLockAction setSession(NutsSession session) {
        this.session=session;
        return this;
    }

    public NutsWorkspace getWs() {
        return ws;
    }
}
