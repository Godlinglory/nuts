/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceUpdateOptions;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.app.options.NutsIdNonOption;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.ValueNonOption;
import net.vpc.app.nuts.NutsUpdateOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsUpdateCommand;

/**
 * Created by vpc on 1/7/17.
 */
public class UpdateCommand extends AbstractNutsCommand {

    public UpdateCommand() {
        super("update", DEFAULT_SUPPORT);
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        NutsUpdateCommand uoptions = context.getWorkspace().update().setSession(context.getSession()).setTrace(true);
        String version = null;
        List<String> ids = new ArrayList<>();
        Argument a;
        boolean apply = true;
        boolean extensions = false;
        while (cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            } else if (cmdLine.readAllOnce("--dry", "-t")) {
                apply = false;
            } else if (cmdLine.readAllOnce("--force", "-f")) {
                uoptions.setForce(true);
                uoptions.setEnableInstall(true);
            } else if (cmdLine.readAllOnce("--extensions")) {
                extensions = true;
            } else if (cmdLine.readAllOnce("--set-version", "-v")) {
                version = cmdLine.readRequiredNonOption(new ValueNonOption("Version")).getStringExpression();
            } else {
                String id = cmdLine.readRequiredNonOption(new NutsIdNonOption("NutsId", context.getWorkspace())).getStringExpression();
                ids.add(id);
            }
        }
        if (cmdLine.isExecMode()) {
            if (ids.isEmpty()) {
                //should update nuts
                if (context.getWorkspace().updateWorkspace()
                        .setEnableMajorUpdates(version != null)
                        .setForceBootAPIVersion(version)
                        .setTrace(uoptions.isTrace())
                        .setUpdateExtensions(extensions || apply)
                        .setSession(context.getSession())
                        .checkUpdates(apply) == null) {
                    context.out().printf("workspace **upto-date**\n");
                }
            } else {
                for (String id : ids) {
                    uoptions.id(id).checkUpdates(apply);
                }
            }
        }
        return 0;
    }
}
