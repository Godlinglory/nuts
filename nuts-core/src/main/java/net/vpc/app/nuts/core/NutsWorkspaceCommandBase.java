/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.NutsConfigurableHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.NutsCommandLine;

/**
 *
 * @author vpc
 * @param <T>
 */
public abstract class NutsWorkspaceCommandBase<T extends NutsWorkspaceCommand> implements NutsWorkspaceCommand {

    protected NutsWorkspace ws;
    private NutsSession session;
    private NutsSession validSession;
    private boolean sessionCopy = false;
    private final String commandName;

    public NutsWorkspaceCommandBase(NutsWorkspace ws, String commandName) {
        this.ws = ws;
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }

    //@Override
    protected T copyFromWorkspaceCommandBase(NutsWorkspaceCommandBase other) {
        if (other != null) {
            this.session = other.getSession();
        }
        return (T) this;
    }

    //@Override
    public NutsSession getSession() {
        return session;
    }

    public NutsSession getSession(boolean autoCreate) {
        if (session != null) {
            return session;
        }
        if (autoCreate) {
            session = ws.createSession();
        }
        return session;
    }

    //@Override
    public T session(NutsSession session) {
        return setSession(session);
    }

    //@Override
    public T setSession(NutsSession session) {
        this.session = session;
        return (T) this;
    }

    protected void invalidateResult() {

    }

    public NutsSession getValidSessionCopy() {
        NutsSession s = getValidSession();
        if (!sessionCopy) {
            s = validSession = s.copy();
            sessionCopy = true;
        }
        return s;
    }

    public NutsSession getValidSession() {
        if (validSession == null) {
            validSession = NutsWorkspaceUtils.validateSession(ws, getSession());
            sessionCopy = true;
        }
        return validSession;
    }

    protected NutsWorkspace getWorkspace() {
        return ws;
    }

    protected void setWs(NutsWorkspace ws) {
        this.ws = ws;
        invalidateResult();
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "--trace": {
                getValidSessionCopy().setTrace(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--ask": {
                getValidSessionCopy().setAsk(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--force": {
                getValidSessionCopy().setForce(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
        }

        if (getValidSessionCopy().configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    @Override
    public T configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, args, getCommandName());
    }

    @Override
    public boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, commandLine);
    }

}
