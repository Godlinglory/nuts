/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsExecutionContext;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsInstallerComponent;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.NutsUninstallCommand;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsUninstallCommand implements NutsUninstallCommand {

    private boolean ask = true;
    private boolean trace = true;
    private boolean force = false;
    private boolean erase = false;
    private List<String> args;
    private List<NutsId> ids = new ArrayList<>();
    private NutsSession session;
    private NutsWorkspace ws;

    public DefaultNutsUninstallCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    public NutsUninstallCommand id(String id) {
        return setId(id);
    }

    public NutsUninstallCommand id(NutsId id) {
        return setId(id);
    }

    public NutsUninstallCommand setId(String id) {
        return setId(id == null ? null : ws.parser().parseId(id));
    }

    public NutsUninstallCommand setId(NutsId id) {
        if (id == null) {
            ids.clear();
        } else {
            ids.add(id);
        }
        return this;
    }

    public NutsUninstallCommand addId(String id) {
        return addId(id == null ? null : ws.parser().parseId(id));
    }

    public NutsUninstallCommand addId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(id);
        } else {
            ids.add(id);
        }
        return this;
    }

    public NutsUninstallCommand addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    public NutsUninstallCommand addIds(NutsId... ids) {
        for (NutsId id : ids) {
            addId(id);
        }
        return this;
    }

    public boolean isTrace() {
        return trace;
    }

    public NutsUninstallCommand setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    public boolean isForce() {
        return force;
    }

    public NutsUninstallCommand setForce(boolean forceInstall) {
        this.force = forceInstall;
        return this;
    }

    public boolean isAsk() {
        return ask;
    }

    public NutsUninstallCommand setAsk(boolean ask) {
        this.ask = ask;
        return this;
    }

    public String[] getArgs() {
        return args == null ? new String[0] : args.toArray(new String[0]);
    }

    public NutsUninstallCommand setArgs(String... args) {
        return setArgs(args == null ? null : Arrays.asList(args));
    }

    public NutsUninstallCommand setArgs(List<String> args) {
        this.args = new ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                if (arg == null) {
                    throw new NullPointerException();
                }
                this.args.add(arg);
            }
        }
        return this;
    }

    public NutsUninstallCommand addArg(String arg) {
        if (this.args == null) {
            this.args = new ArrayList<>();
        }
        if (arg == null) {
            throw new NullPointerException();
        }
        this.args.add(arg);
        return this;
    }

    public NutsUninstallCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    public NutsUninstallCommand addArgs(List<String> args) {
        if (this.args == null) {
            this.args = new ArrayList<>();
        }
        if (args != null) {
            for (String arg : args) {
                if (arg == null) {
                    throw new NullPointerException();
                }
                this.args.add(arg);
            }
        }
        return this;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsUninstallCommand setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsId[] getIds() {
        return ids == null ? new NutsId[0] : ids.toArray(new NutsId[0]);
    }

    public void uninstall() {
        CoreNutsUtils.checkReadOnly(ws);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = CoreNutsUtils.validateSession(this.getSession(), ws);
        ws.security().checkAllowed(NutsConstants.Rights.UNINSTALL, "uninstall");
        List<NutsDefinition> defs = new ArrayList<>();
        for (NutsId id : this.getIds()) {
            NutsDefinition def = ws.fetch().id(id).setSession(session.copy()).setTransitive(false).setAcceptOptional(false).includeDependencies()
                    .setIncludeInstallInformation(true).getResultDefinition();
            if (!def.getInstallation().isInstalled()) {
                throw new NutsIllegalArgumentException(id + " Not Installed");
            }
            defs.add(def);
        }
        for (NutsDefinition def : defs) {
            NutsId id = dws.resolveEffectiveId(def.getDescriptor(), ws.fetch().session(session));
            NutsInstallerComponent ii = dws.getInstaller(def, session);
            PrintStream out = CoreIOUtils.resolveOut(ws, session);
            if (ii != null) {
//        NutsDescriptor descriptor = nutToInstall.getDescriptor();
                NutsExecutionContext executionContext = dws.createNutsExecutionContext(def, this.getArgs(), new String[0], session, true, null);
                ii.uninstall(executionContext, this.isErase());
                try {
                    CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.PROGRAMS).toFile());
                    CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.TEMP).toFile());
                    CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.LOGS).toFile());
                    if (this.isErase()) {
                        CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.VAR).toFile());
                        CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.CONFIG).toFile());
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                if (this.isTrace()) {
                    out.printf(ws.formatter().createIdFormat().toString(id) + " uninstalled ##successfully##\n");
                }
            } else {
                if (this.isTrace()) {
                    out.printf(ws.formatter().createIdFormat().toString(id) + " @@could not@@ be uninstalled\n");
                }
            }
        }
//        return true;
    }

    @Override
    public boolean isErase() {
        return erase;
    }

    @Override
    public NutsUninstallCommand setErase(boolean erase) {
        this.erase = erase;
        return this;
    }

}
