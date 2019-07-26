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

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.vpc.app.nuts.toolbox.nsh.util.ShellHelper;
import net.vpc.common.io.FileUtils;
import net.vpc.common.ssh.SShConnection;
import net.vpc.common.strings.StringUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.toolbox.nsh.NshExecutionContext;
import net.vpc.app.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17. ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
public class SshCommand extends AbstractNshBuiltin {

    public SshCommand() {
        super("ssh", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean invokeNuts;
        String nutsCommand;
        String nutsJre;
        String address;
        List<String> cmd = new ArrayList<>();
    }

    public void exec(String[] args, NshExecutionContext context) {
        NutsCommandLine cmdLine = cmdLine(args, context);
        Options o = new Options();
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (cmdLine.peek().isOption()) {
                if (context.configureFirst(cmdLine)) {
                    //
                } else if ((a = cmdLine.next("--nuts")) != null) {
                    o.invokeNuts = true;
                } else if ((a = cmdLine.nextString("--nuts-jre")) != null) {
                    o.nutsJre = a.getStringValue();
                } else {
                    //suppose this is an other nuts option
                    //just consume the rest as of the command
                    while (cmdLine.hasNext()) {
                        o.cmd.add(cmdLine.next().getString());
                    }
                }
            } else {
                o.address = cmdLine.next().getString();
                while (cmdLine.hasNext()) {
                    o.cmd.add(cmdLine.next().getString());
                }
            }
        }
        if (o.address == null) {
            throw new NutsExecutionException(context.getWorkspace(), "Missing ssh address", 2);
        }
        if (o.cmd.isEmpty()) {
            throw new NutsExecutionException(context.getWorkspace(), "Missing ssh command. Interactive ssh is not yet supported!", 2);
        }
        final NutsWorkspace ws = context.getWorkspace();
        ShellHelper.WsSshListener listener = new ShellHelper.WsSshListener(context.getSession());
        try (SShConnection sshSession = new SShConnection(o.address)
                .addListener(listener)) {
            List<String> cmd = new ArrayList<>();
            if (o.invokeNuts) {
                String workspace = null;
                NutsCommandLine c = context.getWorkspace().commandLine().create(o.cmd.subList(1, o.cmd.size()).toArray(new String[0]));
                NutsArgument arg = null;
                while (c.hasNext()) {
                    if ((arg = c.next("--workspace")) != null) {
                        workspace = c.requireNonOption().next().getString();
                    } else if (c.peek().isNonOption()) {
                        break;
                    } else {
                        c.skip();
                    }
                }
                if (!StringUtils.isBlank(o.nutsCommand)) {
                    cmd.add(o.nutsCommand);
                } else {
                    String userHome = null;
                    sshSession.failFast()
                            .setRedirectErrorStream(true)
                            .grabOutputString().exec("echo", "$HOME");
                    userHome = sshSession.getOutputString().trim();
                    if (StringUtils.isBlank(workspace)) {
                        workspace = userHome + "/.config/nuts/" + NutsConstants.Names.DEFAULT_WORKSPACE_NAME;
                    }
                    boolean nutsCommandFound = false;
                    try (SShConnection sShConnection = sshSession.setFailFast(false).
                            grabOutputString()
                            .setRedirectErrorStream(true)) {
                        int r = sShConnection.exec("ls", workspace + "/nuts");
                        if (0 == r) {
                            //found
                            nutsCommandFound = true;
                        }
                    }
                    if (!nutsCommandFound) {
                        Path from = ws.search().id(ws.config().getApiId()).getResultDefinitions().required().getPath();
                        if (from == null) {
                            throw new NutsExecutionException(context.getWorkspace(), "Unable to resolve Nuts Jar File", 2);
                        } else {
                            context.out().printf("Detected nuts.jar location : %s\n", from);
                            String bootApiFileName = "nuts-" + ws.config().getApiId() + ".jar";
                            sshSession.setFailFast(true).copyLocalToRemote(from.toString(), workspace + "/" + bootApiFileName, true);
                            String javaCmd = null;
                            if (o.nutsJre != null) {
                                javaCmd = (o.nutsJre + FileUtils.getNativePath("/bin/java"));
                            } else {
                                javaCmd = ("java");
                            }
                            if (sshSession.exec(javaCmd, "-jar", workspace + "/" + bootApiFileName, "-y", "install", "ndi", "--force") != 0) {
                                throw new NutsExecutionException(context.getWorkspace(), "Install remote nuts failed", 2);
                            }
                        }
                    }
                    cmd.add(workspace + "/nuts");
                }
            }
            cmd.addAll(o.cmd);
            sshSession.grabOutputString(false).failFast().exec(cmd);
        }
    }

}
