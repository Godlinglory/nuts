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
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsDeployment;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.extensions.cmd.cmdline.FileNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.RepositoryNonOption;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;

import java.io.File;
import net.vpc.app.nuts.NutsFile;
import net.vpc.app.nuts.NutsSearch;
import net.vpc.app.nuts.extensions.cmd.cmdline.NutsIdNonOption;

/**
 * Created by vpc on 1/7/17.
 */
public class DeployCommand extends AbstractNutsCommand {

    public DeployCommand() {
        super("deploy", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        boolean fileMode = true;
        String to = null;
        String from = null;
        String id = null;
        String contentFile = null;
        String descriptorFile = null;
        while (!cmdLine.isEmpty()) {
            if (contentFile == null && id == null && cmdLine.readOnce("--file", "-f")) {
                fileMode = true;
                contentFile = cmdLine.readNonOptionOrError(new FileNonOption("File")).getString();
            } else if (fileMode && cmdLine.readOnce("--desc", "-d")) {
                descriptorFile = cmdLine.readNonOption(new FileNonOption("DescriptorFile")).getString();
            } else if (cmdLine.readOnce("--id", "-i")) {
                fileMode = false;
            } else if (!fileMode && cmdLine.readOnce("--source", "-s")) {
                from = cmdLine.readNonOption(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString();
            } else if (cmdLine.readOnce("--to", "-t")) {
                to = cmdLine.readNonOption(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString();
            } else {
                if (contentFile != null || id != null) {
                    cmdLine.requireEmpty();
                } else {
                    if (fileMode) {
                        contentFile = cmdLine.readNonOptionOrError(new FileNonOption("File")).getString();
                    } else {
                        id = cmdLine.readNonOptionOrError(new NutsIdNonOption("Nuts", context)).getString();
                    }
                }
            }
        }
        if (cmdLine.isAutoCompleteMode()) {
            return 0;
        }
        if (fileMode) {
            for (String s : CoreIOUtils.expandPath(contentFile, new File(context.getCommandLine().getCwd()))) {
                NutsId nid = null;
                nid = context.getValidWorkspace().deploy(
                        new NutsDeployment()
                                .setContentPath(s)
                                .setDescriptorPath(descriptorFile)
                                .setRepositoryId(to),
                        context.getSession()
                );
                context.getTerminal().getOut().printf("File ==%s== deployed successfully as ==%s== to %s\n" + nid, s, nid,to==null?"<default-repo>":to);
            }
        } else {
            for (NutsId nutsId : context.getValidWorkspace().find(new NutsSearch(id).setRepositoryFilter(from), context.getSession())) {
                NutsFile fetched = context.getValidWorkspace().fetch(nutsId.toString(), context.getSession());
                if (fetched.getFile() != null) {
                    NutsId nid = null;
                    nid = context.getValidWorkspace().deploy(
                            new NutsDeployment()
                                    .setContent(fetched.getFile())
                                    .setDescriptor(fetched.getDescriptor())
                                    .setRepositoryId(to),
                            context.getSession()
                    );
                    context.getTerminal().getOut().printf("Nuts ==%s== deployed successfully to %s\n" + nid, nutsId,to==null?"<default-repo>":to);
                }
            }
        }
        return 0;
    }
}
