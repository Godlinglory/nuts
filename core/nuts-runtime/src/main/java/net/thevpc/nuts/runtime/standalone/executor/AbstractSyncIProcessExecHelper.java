package net.thevpc.nuts.runtime.standalone.executor;

import net.thevpc.nuts.NutsScheduler;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public abstract class AbstractSyncIProcessExecHelper implements IProcessExecHelper {
    private NutsSession session;

    public AbstractSyncIProcessExecHelper(NutsSession session) {
        this.session = session;
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public Future<Integer> execAsync() {
        return NutsScheduler.of(getSession()).executorService().submit(this::exec);
    }
}
