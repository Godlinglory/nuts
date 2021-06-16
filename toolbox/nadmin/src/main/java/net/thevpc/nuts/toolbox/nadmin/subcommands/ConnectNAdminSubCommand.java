/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nadmin.subcommands;

import net.thevpc.nuts.*;
import net.thevpc.common.io.IOUtils;
import net.thevpc.common.io.NonBlockingInputStreamAdapter;
import net.thevpc.common.strings.StringUtils;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author thevpc
 */
public class ConnectNAdminSubCommand extends AbstractNAdminSubCommand {

    public static final int DEFAULT_ADMIN_SERVER_PORT = 8898;

    @Override
    public boolean exec(NutsCommandLine commandLine, Boolean autoSave, NutsApplicationContext context) {
        NutsCommandLineManager commandLineFormat = context.getWorkspace().commandLine();
        if (commandLine.next("connect") != null) {
            char[] password = null;
            String server = null;
            NutsArgument a;
            while (commandLine.hasNext()) {
                if ((a = commandLine.nextString("--password")) != null) {
                    password = a.getStringValue("").toCharArray();
                } else if (commandLine.peek().isOption()) {
                    context.configureLast(commandLine);
                } else {
                    server = commandLine.nextRequiredNonOption(commandLineFormat.createName("ServerAddress")).getString();
                    commandLine.setCommandName("nadmin connect").unexpectedArgument();
                }
            }
            if (!commandLine.isExecMode()) {
                return true;
            }
            String login = null;
            int port = -1;
            if (server == null) {
                throw new NutsIllegalArgumentException(context.getSession(), "missing address");
            }
            if (server.contains("@")) {
                login = server.substring(0, server.indexOf("@"));
                server = server.substring(server.indexOf("@") + 1);
            }
            if (server.contains(":")) {
                port = Integer.parseInt(server.substring(server.indexOf(":") + 1));
                server = server.substring(0, server.indexOf(":"));
            }
            if (!StringUtils.isBlank(login) && isBlank(password)) {
                password = context.getSession().getTerminal().readPassword("Password:");
            }
            Socket socket = null;
            try {
                try {
                    int validPort = port <= 0 ? DEFAULT_ADMIN_SERVER_PORT : port;
                    socket = new Socket(InetAddress.getByName(server), validPort);
                    IOUtils.pipe("pipe-out-socket-" + server + ":" + validPort, new NonBlockingInputStreamAdapter("pipe-out-socket-" + server + ":" + validPort, socket.getInputStream()), context.getSession().out().asPrintStream());
                    PrintStream out = new PrintStream(socket.getOutputStream());
                    if (!StringUtils.isBlank(login)) {
                        out.printf("connect ==%s %s== %n", login, new String(password));
                    }
                    while (true) {
                        String line = context.getSession().getTerminal().readLine("");
                        if (line == null) {
                            break;
                        }
                        if (line.trim().length() > 0) {
                            if (line.trim().equals("quit") || line.trim().equals("exit")) {
                                break;
                            }
                            out.printf("%s%n", line);
                        }
                    }
                } finally {
                    if (socket != null) {
                        socket.close();
                    }
                }
            } catch (Exception ex) {
                throw new NutsExecutionException(context.getSession(), ex, 2);
            }
            return true;
        }
        return false;
    }

    public static boolean isBlank(char[] string) {
        if (string == null || string.length == 0) {
            return true;
        }
        for (char c : string) {
            if (c > ' ') {
                return false;
            }
        }
        return true;
    }

}
