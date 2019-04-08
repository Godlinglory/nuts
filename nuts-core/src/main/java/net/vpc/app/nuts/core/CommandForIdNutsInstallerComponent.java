/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsExecutionContext;
import net.vpc.app.nuts.NutsExecutionEntry;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsInstallerComponent;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
class CommandForIdNutsInstallerComponent implements NutsInstallerComponent {
    
    @Override
    public void install(NutsExecutionContext executionContext) {
        CoreNutsUtils.checkReadOnly(executionContext.getWorkspace());
        NutsId id = executionContext.getNutsDefinition().getId();
        NutsDescriptor descriptor = executionContext.getNutsDefinition().getDescriptor();
        if (descriptor.isNutsApplication()) {
            int r = executionContext.getWorkspace().exec().setCommand(id.setNamespace(null).toString(), "--nuts-execution-mode=on-install").addExecutorOptions().addCommand(executionContext.getArgs()).exec().setFailFast().getResult();
        }
        //            NutsWorkspaceConfigManager cc = executionContext.getWorkspace().getConfigManager();
        //            NutsWorkspaceCommand c = cc.findCommand(id.getName());
        //            if (c != null) {
        //
        //            } else {
        //                //
        //                cc.installCommand(new DefaultNutsWorkspaceCommand()
        //                        .setId(id.setNamespace(""))
        //                        .setName(id.getName())
        //                        .setCommand(new String[0])
        //                );
        //            }
    }

    @Override
    public void uninstall(NutsExecutionContext executionContext, boolean deleteData) {
        CoreNutsUtils.checkReadOnly(executionContext.getWorkspace());
        NutsId id = executionContext.getNutsDefinition().getId();
        if ("jar".equals(executionContext.getNutsDefinition().getDescriptor().getPackaging())) {
            NutsExecutionEntry[] executionEntries = executionContext.getWorkspace().parser().parseExecutionEntries(executionContext.getNutsDefinition().getContent().getPath());
            for (NutsExecutionEntry executionEntry : executionEntries) {
                if (executionEntry.isApp()) {
                    //
                    int r = executionContext.getWorkspace().exec().setCommand(id.toString(), "--nuts-execution-mode=on-uninstall").addCommand(executionContext.getArgs()).exec().getResult();
                    executionContext.getWorkspace().getTerminal().getFormattedOut().printf("Installation Exited with code : " + r);
                }
            }
        }
        //            NutsId id = executionContext.getPrivateStoreNutsDefinition().getId();
        //            NutsWorkspaceConfigManager cc = executionContext.getWorkspace().getConfigManager();
        //            for (NutsWorkspaceCommand command : cc.findCommands(id)) {
        //                //install if installed with the very same version !!
        //                if (id.getLongName().equals(command.getId().getLongName())) {
        //                    cc.uninstallCommand(command.getName());
        //                }
        //            }
    }

    @Override
    public int getSupportLevel(NutsDefinition criteria) {
        return 0;
    }
    
}
