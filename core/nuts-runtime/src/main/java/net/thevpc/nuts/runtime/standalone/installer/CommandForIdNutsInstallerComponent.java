/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.installer;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNutsInstallInfo;
import net.thevpc.nuts.runtime.standalone.executor.NutsExecutionContextUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNutsDefinition;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.spi.NutsInstallerComponent;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class CommandForIdNutsInstallerComponent implements NutsInstallerComponent {
    NutsDefinition runnerId;

    public CommandForIdNutsInstallerComponent(NutsDefinition runnerId) {
        this.runnerId = runnerId;
    }

    @Override
    public void install(NutsExecutionContext executionContext) {
        runMode(executionContext, "install");
    }

    @Override
    public void update(NutsExecutionContext executionContext) {
        runMode(executionContext, "update");
    }

    @Override
    public void uninstall(NutsExecutionContext executionContext, boolean deleteData) {
        runMode(executionContext, "uninstall");
    }

    public void runMode(NutsExecutionContext executionContext, String mode) {
        NutsWorkspaceUtils.of(executionContext.getSession()).checkReadOnly();
        if (runnerId == null) {
            NutsDefinition definition = executionContext.getDefinition();
            NutsDescriptor descriptor = definition.getDescriptor();
            if (descriptor.isApplication()) {
                DefaultNutsDefinition def2 = new DefaultNutsDefinition(definition, executionContext.getSession())
                        .setInstallInformation(
                                new DefaultNutsInstallInfo(definition.getInstallInformation())
                                        .setInstallStatus(
                                                definition.getInstallInformation().getInstallStatus().withInstalled(true)
                                        )
                        );
                NutsExecCommand cmd = executionContext.getSession().exec()
                        .setSession(executionContext.getExecSession())
                        .setCommand(def2)
                        .addCommand("--nuts-exec-mode=" + mode);
                if (mode.equals("install")) {
                    cmd.addExecutorOptions("--nuts-auto-install=false");
                }else if (mode.equals("uninstall")) {
                    cmd.addExecutorOptions("--nuts-auto-install=false");
                }
                cmd.addCommand(executionContext.getArguments())
                        .setExecutionType(executionContext.getSession().boot().getBootOptions().getExecutionType())
                        .setFailFast(true)
                        .run();
            }
        } else {
            NutsDefinition definition = runnerId;
            NutsDescriptor descriptor = definition.getDescriptor();
            if (descriptor.isApplication()) {
                DefaultNutsDefinition def2 = new DefaultNutsDefinition(definition, executionContext.getSession())
                        .setInstallInformation(
                                new DefaultNutsInstallInfo(definition.getInstallInformation())
                                        .setInstallStatus(
                                                definition.getInstallInformation().getInstallStatus().withInstalled(true)
                                        )
                        );
                List<String> eargs = new ArrayList<>();
                for (String a : executionContext.getExecutorArguments()) {
                    eargs.add(evalString(a, mode, executionContext));
                }
                eargs.addAll(Arrays.asList(executionContext.getArguments()));
                executionContext.getExecSession().exec()
                        .setCommand(def2)
                        .addCommand(eargs)
                        .setExecutionType(executionContext.getExecSession().boot().getBootOptions().getExecutionType())
                        .setExecutionType(
                                "nsh".equals(def2.getId().getArtifactId()) ?
                                        NutsExecutionType.EMBEDDED : NutsExecutionType.SPAWN
                        )
                        .setFailFast(true)
                        .run();
            }
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        return DEFAULT_SUPPORT;
    }


    private String evalString(String s, String mode, NutsExecutionContext executionContext) {
        return StringPlaceHolderParser.replaceDollarPlaceHolders(s, executionContext, executionContext.getSession(),
                (key, context, session) -> {
                    if ("NUTS_MODE".equals(key)) {
                        return mode;
                    }
                    return NutsExecutionContextUtils.EXECUTION_CONTEXT_PLACEHOLDER.get(key, context, session);
                }
        );
    }
}
