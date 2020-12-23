/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.main.installers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.DefaultNutsInstallInfo;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.DefaultNutsDefinition;

import java.util.EnumSet;

/**
 * @author thevpc
 */
public class CommandForIdNutsInstallerComponent implements NutsInstallerComponent {

    private String getNutsVersion(NutsExecutionContext executionContext) {
        NutsDescriptor descriptor = executionContext.getDefinition().getDescriptor();
        if (descriptor.isApplication()) {
            for (NutsDependency dependency : descriptor.getDependencies()) {
                if (dependency.toId().getShortName().equals(NutsConstants.Ids.NUTS_API)) {
                    return dependency.toId().getVersion().getValue();
                }
            }
        }
        for (NutsDependency dependency : executionContext.getDefinition().getDependencies()) {
            if (dependency.toId().getShortName().equals(NutsConstants.Ids.NUTS_API)) {
                return dependency.toId().getVersion().getValue();
            }
        }
        return null;
    }

    @Override
    public void install(NutsExecutionContext executionContext) {
        NutsWorkspaceUtils.of(executionContext.getWorkspace()).checkReadOnly();
//        NutsId id = executionContext.getDefinition().getId();
        NutsDescriptor descriptor = executionContext.getDefinition().getDescriptor();
        if (descriptor.isApplication()) {
            DefaultNutsDefinition def2 = new DefaultNutsDefinition(executionContext.getDefinition())
                    .setInstallInformation(
                            new DefaultNutsInstallInfo(executionContext.getDefinition().getInstallInformation())
                                    .setInstallStatus(EnumSet.of(NutsInstallStatus.INSTALLED))
                    );
            executionContext.getWorkspace().exec()
                    .setSession(executionContext.getExecSession())
                    //                    .executionType(NutsExecutionType.EMBEDDED)
                    .setCommand(def2)
                    .addCommand("--nuts-exec-mode=install")
                    .addExecutorOptions("--nuts-auto-install=false")
                    .addCommand(executionContext.getArguments())
                    .setExecutionType(executionContext.getWorkspace().config().options().getExecutionType())
                    .setFailFast(true)
                    .run();
        }
    }

    @Override
    public void update(NutsExecutionContext executionContext) {
        NutsWorkspaceUtils.of(executionContext.getWorkspace()).checkReadOnly();
//        NutsId id = executionContext.getDefinition().getId();
        NutsDescriptor descriptor = executionContext.getDefinition().getDescriptor();
        if (descriptor.isApplication()) {
            DefaultNutsDefinition def2 = new DefaultNutsDefinition(executionContext.getDefinition())
                    .setInstallInformation(
                            new DefaultNutsInstallInfo(executionContext.getDefinition().getInstallInformation())
                                    .setInstallStatus(EnumSet.of(NutsInstallStatus.INSTALLED))
                    );
            executionContext.getWorkspace().exec()
                    .setCommand(def2)
                    .addCommand("--nuts-exec-mode=update","--yes")
//                    .addCommand(id.builder().setNamespace(null).build().toString(), "--nuts-exec-mode=update", "--force")
                    .addExecutorOptions().addCommand(executionContext.getArguments())
                    .setFailFast(true).run();
        }
    }

    @Override
    public void uninstall(NutsExecutionContext executionContext, boolean deleteData) {
        NutsSession session = executionContext.getExecSession();
        NutsWorkspace ws = executionContext.getWorkspace();
        NutsWorkspaceUtils.of(executionContext.getWorkspace()).checkReadOnly();
        NutsId id = executionContext.getDefinition().getId();
        if ("jar".equals(executionContext.getDefinition().getDescriptor().getPackaging())) {
            NutsExecutionEntry[] executionEntries = ws.apps().execEntries().parse(executionContext.getDefinition().getPath());
            for (NutsExecutionEntry executionEntry : executionEntries) {
                if (executionEntry.isApp()) {
                    //
                    int r = ws.exec().addCommand(id.getLongName(), "--nuts-exec-mode=uninstall", "--yes").addCommand(executionContext.getArguments()).getResult();
                    session.out().printf("Installation Exited with code : " + r + " %n");
                }
            }
        }
        //            NutsId parseId = executionContext.getPrivateStoreNutsDefinition().getId();
        //            NutsWorkspaceConfigManager cc = executionContext.getWorkspace().getConfigManager();
        //            for (NutsWorkspaceCommand command : cc.findCommands(parseId)) {
        //                //install if installed with the very same parseVersion !!
        //                if (parseId.getLongName().equals(command.getId().getLongName())) {
        //                    cc.uninstallCommand(command.getName());
        //                }
        //            }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsDefinition> criteria) {
        return DEFAULT_SUPPORT;
    }

}