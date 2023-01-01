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
package net.thevpc.nuts.toolbox.nsh.cmds.bash;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.ssh.SShConnection;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;
import net.thevpc.nuts.util.NAssert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17. ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
public class SshCommand extends SimpleJShellBuiltin {

    public SshCommand() {
        super("ssh", DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean configureFirst(NCommandLine commandLine, JShellExecutionContext context) {
        Options o = context.getOptions();
        NArgument a;
        NSession session = context.getSession();
        if (!o.cmd.isEmpty()) {
            o.cmd.add(commandLine.next().flatMap(NValue::asString).get(session));
            return true;
        } else if (commandLine.peek().get(session).isNonOption()) {
            if (o.address == null) {
                o.address = commandLine.next().flatMap(NValue::asString).get(session);
            } else {
                o.cmd.add(commandLine.next().flatMap(NValue::asString).get(session));
            }
            return true;
        } else if ((a = commandLine.next("--nuts").orNull()) != null) {
            if (o.acceptDashNuts) {
                o.invokeNuts = true;
            } else {
                o.cmd.add(a.asString().get(session));
            }
            return true;
        } else if ((a = commandLine.next("--nuts-jre").orNull()) != null) {
            if (o.acceptDashNuts) {
                o.nutsJre = a.getStringValue().get(session);
            } else {
                o.cmd.add(a.asString().get(session));
            }
            return true;
        } else if (o.address == null || commandLine.peek().get(session).isNonOption()) {
            o.acceptDashNuts = false;
            o.cmd.add(commandLine.next().flatMap(NValue::asString).get(session));
            return true;
        }

        return false;
    }

    @Override
    protected void execBuiltin(NCommandLine commandLine, JShellExecutionContext context) {
        Options o = context.getOptions();
        // address --nuts [nuts options] args
        NSession session = context.getSession();
        NAssert.requireNonBlank(o.address, "ssh address", session);
        NAssert.requireNonBlank(o.cmd, () -> NMsg.ofPlain("missing ssh command. Interactive ssh is not yet supported!"), session);
        ShellHelper.WsSshListener listener = new ShellHelper.WsSshListener(session);
        try (SShConnection sshSession = new SShConnection(o.address, session)
                .addListener(listener)) {
            List<String> cmd = new ArrayList<>();
            if (o.invokeNuts) {
                String workspace = null;
                NCommandLine c = NCommandLine.of(o.cmd.subList(1, o.cmd.size()));
                NArgument arg = null;
                while (c.hasNext()) {
                    if ((arg = c.next("--workspace").orNull()) != null) {
                        workspace = c.nextNonOption().get().asString().get(session);
                    } else if (c.peek().isPresent() && c.peek().get().isNonOption()) {
                        break;
                    } else {
                        c.skip();
                    }
                }
                if (!NBlankable.isBlank(o.nutsCommand)) {
                    cmd.add(o.nutsCommand);
                } else {
                    String userHome = null;
                    sshSession.setFailFast(true)
                            .setRedirectErrorStream(true)
                            .grabOutputString().exec("echo", "$HOME");
                    userHome = sshSession.getOutputString().trim();
                    if (NBlankable.isBlank(workspace)) {
                        workspace = userHome + "/.config/nuts/" + NConstants.Names.DEFAULT_WORKSPACE_NAME;
                    }
                    boolean nutsCommandFound = false;
                    int r = sshSession.setFailFast(false).
                            grabOutputString()
                            .setRedirectErrorStream(true).exec("ls", workspace + "/nuts");
                    if (0 == r) {
                        //found
                        nutsCommandFound = true;
                    }
                    if (!nutsCommandFound) {
                        NPath from = session.search().addId(session.getWorkspace().getApiId()).getResultDefinitions().required().getContent().orNull();
                        NAssert.requireNonNull(from, "jar file", session);
                        context.out().printf("Detected nuts.jar location : %s\n", from);
                        String bootApiFileName = "nuts-" + session.getWorkspace().getApiId() + ".jar";
                        sshSession.setFailFast(true).copyLocalToRemote(from.toString(), workspace + "/" + bootApiFileName, true);
                        String javaCmd = null;
                        if (o.nutsJre != null) {
                            javaCmd = (o.nutsJre + "/bin/java");
                        } else {
                            javaCmd = ("java");
                        }
                    }
                    cmd.add(workspace + "/nuts");
                }
            }
            cmd.addAll(o.cmd);
            sshSession.grabOutputString(false).setFailFast(true).exec(cmd);
        }
    }

    private static class Options {
        boolean acceptDashNuts = true;
        boolean invokeNuts;
        String nutsCommand;
        String nutsJre;
        String address;
        List<String> cmd = new ArrayList<>();
    }

}
