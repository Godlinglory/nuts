package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerOp;

public class DefaultNutsTerminalModel {

    private NutsWorkspace ws;
    private NutsLogger LOG;

    public DefaultNutsTerminalModel(NutsWorkspace ws) {
        this.ws = ws;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsTerminalModel.class,session);
        }
        return LOG;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }




}
