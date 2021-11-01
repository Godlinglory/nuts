package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;

import java.io.File;
import java.nio.file.Path;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public abstract class AbstractNutsRm implements NutsRm {
    private NutsWorkspace ws;
    private Object target;
    private NutsSession session;
    private boolean failFast;

    public AbstractNutsRm(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsRm setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    public NutsWorkspace getWs() {
        return ws;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public NutsRm setTarget(Object target) {
        NutsWorkspaceUtils.checkSession(ws, session);
        if (target == null) {
            this.target = null;
            return this;
        }
        if (target instanceof File) {
            return setTarget((File) target);
        }
        if (target instanceof Path) {
            return setTarget((Path) target);
        }
        throw new NutsException(session, NutsMessage.cstyle("unsupported delete %s",target));
    }

    @Override
    public NutsRm setTarget(File target) {
        this.target = target;
        return this;
    }

    @Override
    public NutsRm setTarget(Path target) {
        this.target = target;
        return this;
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public NutsRm setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }
}
