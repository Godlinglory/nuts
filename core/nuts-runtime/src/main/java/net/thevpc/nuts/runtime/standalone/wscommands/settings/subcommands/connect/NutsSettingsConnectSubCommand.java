/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.connect;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.NonBlockingInputStreamAdapter;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreNumberUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.AbstractNutsSettingsSubCommand;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author thevpc
 */
public class NutsSettingsConnectSubCommand extends AbstractNutsSettingsSubCommand {

    public static final int DEFAULT_ADMIN_SERVER_PORT = 8898;

    @Override
    public boolean exec(NutsCommandLine commandLine, Boolean autoSave, NutsSession session) {
        NutsCommandLineManager commandLineFormat = session.commandLine();
        if (commandLine.next("connect") != null) {
            char[] password = null;
            String server = null;
            NutsArgument a;
            while (commandLine.hasNext()) {
                if ((a = commandLine.nextString("--password")) != null) {
                    password = a.getValue().getString("").toCharArray();
                } else if (commandLine.peek().isOption()) {
                    session.configureLast(commandLine);
                } else {
                    server = commandLine.nextRequiredNonOption(commandLineFormat.createName("ServerAddress")).getString();
                    commandLine.setCommandName("settings connect").unexpectedArgument();
                }
            }
            if (!commandLine.isExecMode()) {
                return true;
            }
            String login = null;
            int port = -1;
            if (server == null) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing address"));
            }
            if (server.contains("@")) {
                login = server.substring(0, server.indexOf("@"));
                server = server.substring(server.indexOf("@") + 1);
            }
            if (server.contains(":")) {
                port =  CoreNumberUtils.convertToInteger(server.substring(server.indexOf(":") + 1),-1);
                server = server.substring(0, server.indexOf(":"));
            }
            if (!NutsBlankable.isBlank(login) && NutsBlankable.isBlank(password)) {
                password = session.getTerminal().readPassword("Password:");
            }
            Socket socket = null;
            try {
                try {
                    int validPort = port <= 0 ? DEFAULT_ADMIN_SERVER_PORT : port;
                    socket = new Socket(InetAddress.getByName(server), validPort);
                    CoreIOUtils.pipe("pipe-out-socket-" + server + ":" + validPort, new NonBlockingInputStreamAdapter("pipe-out-socket-" + server + ":" + validPort, socket.getInputStream()), session.out().asPrintStream(),session);
                    PrintStream out = new PrintStream(socket.getOutputStream());
                    if (!NutsBlankable.isBlank(login)) {
                        out.printf("connect ==%s %s== %n", login, new String(password));
                    }
                    while (true) {
                        String line = session.getTerminal().readLine("");
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
                throw new NutsExecutionException(session,NutsMessage.plain("settings connect failed"), ex, 2);
            }
            return true;
        }
        return false;
    }
}
