/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.main.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.main.config.ConfigEventType;
import net.thevpc.nuts.runtime.DefaultNutsInstallEvent;
import net.thevpc.nuts.runtime.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.wscommands.AbstractNutsUninstallCommand;
import net.thevpc.nuts.runtime.NutsExtensionListHelper;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsUninstallCommand extends AbstractNutsUninstallCommand {

    public DefaultNutsUninstallCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsUninstallCommand run() {
        NutsWorkspaceUtils.of(ws).checkReadOnly();
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = NutsWorkspaceUtils.of(ws).validateSession(this.getSession());
        ws.security().checkAllowed(NutsConstants.Permissions.UNINSTALL, "uninstall");
        NutsSession searchSession = CoreNutsUtils.silent(session);
        List<NutsDefinition> defs = new ArrayList<>();
        for (NutsId id : this.getIds()) {
            List<NutsDefinition> resultDefinitions = ws.search().addId(id).addInstallStatus(NutsInstallStatus.INSTALLED).setSession(searchSession.copy())
                    .setTransitive(false).setOptional(false)
                    .getResultDefinitions().list();
            for (Iterator<NutsDefinition> it = resultDefinitions.iterator(); it.hasNext(); ) {
                NutsDefinition resultDefinition = it.next();
                if (!resultDefinition.getInstallInformation().isInstalledOrRequired()) {
                    it.remove();
                }
            }
            if (resultDefinitions.isEmpty()) {
                throw new NutsIllegalArgumentException(ws, id + " is not installed");
            }
            defs.addAll(resultDefinitions);
        }
        for (NutsDefinition def : defs) {
            NutsId id = dws.resolveEffectiveId(def.getDescriptor(), searchSession);

            NutsInstallerComponent ii = dws.getInstaller(def, session);
            PrintStream out = CoreIOUtils.resolveOut(session);
            if (ii != null) {
                NutsExecutionContext executionContext = dws.createExecutionContext()
                        .setDefinition(def)
                        .setArguments(getArgs())
                        .setExecSession(session)
                        .setTraceSession(session)
                        .setFailFast(true)
                        .setTemporary(false)
                        .setExecutionType(ws.config().options().getExecutionType())
                        .build()
                        ;
                ii.uninstall(executionContext, this.isErase());
            }

            dws.getInstalledRepository().uninstall(id, session);
            CoreIOUtils.delete(ws, ws.locations().getStoreLocation(id, NutsStoreLocation.APPS).toFile());
            CoreIOUtils.delete(ws, ws.locations().getStoreLocation(id, NutsStoreLocation.TEMP).toFile());
            CoreIOUtils.delete(ws, ws.locations().getStoreLocation(id, NutsStoreLocation.LOG).toFile());
            if (this.isErase()) {
                CoreIOUtils.delete(ws, ws.locations().getStoreLocation(id, NutsStoreLocation.VAR).toFile());
                CoreIOUtils.delete(ws, ws.locations().getStoreLocation(id, NutsStoreLocation.CONFIG).toFile());
            }

            if (def.getType() == NutsIdType.EXTENSION) {
                NutsWorkspaceConfigManagerExt wcfg = NutsWorkspaceConfigManagerExt.of(ws.config());
                NutsExtensionListHelper h = new NutsExtensionListHelper(wcfg.getStoredConfigBoot().getExtensions())
                        .save();
                h.remove(def.getId());
                wcfg.getStoredConfigBoot().setExtensions(h.getConfs());
                wcfg.fireConfigurationChanged("extensions", session, ConfigEventType.BOOT);
            }
            if (getSession().isPlainTrace()) {
                out.println(ws.id().formatter(id).format() + " uninstalled ##successfully##");
            }
            NutsWorkspaceUtils.of(ws).events().fireOnUninstall(new DefaultNutsInstallEvent(def, session,new NutsId[0], isErase()));
        }
        return this;
    }
}
