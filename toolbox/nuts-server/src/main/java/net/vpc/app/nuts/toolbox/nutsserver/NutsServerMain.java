package net.vpc.app.nuts.toolbox.nutsserver;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsApplication;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NutsServerMain extends NutsApplication {

    public static void main(String[] args) {
        new NutsServerMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext context) {
        String[] args = context.getArgs();
        try {
            boolean autoSave = false;
            NutsWorkspaceServerManager serverManager = new DefaultNutsWorkspaceServerManager(context.getWorkspace());

            NutsCommandLine cmdLine = context.getCommandLine();

            class SrvInfo {

                String name = ("nuts-http-server");
                String addr = null;
                int port = -1;
                int backlog = -1;
                String serverType = "http";
                String sslCertificate = null;
                String sslPassphrase = null;
                Map<String, String> workspaceLocations = new HashMap<>();
                Map<String, NutsWorkspace> workspaces = new HashMap<>();
            }
            if (cmdLine.readAll("start")) {
                List<SrvInfo> servers = new ArrayList<SrvInfo>();
                boolean autocreate = false;
                boolean readOnly = false;
                String archetype = "server"; //default archetype for server

                while (cmdLine.hasNext()) {
                    if (cmdLine.readAllOnce("-c", "--create")) {
                        autocreate = true;
                    } else if (cmdLine.readAllOnce("-h", "--archetype")) {
                        archetype = cmdLine.readRequiredNonOption(cmdLine.createNonOption("Archetype")).required().getString();
                    } else if (cmdLine.readAllOnce("-!s", "--no-save")) {
                        readOnly = true;
                    } else if (cmdLine.readAllOnce("--read-only")) {
                        readOnly = true;
                    } else if (cmdLine.readAllOnce("--http")) {
                        servers.add(new SrvInfo());
                        servers.get(servers.size() - 1).serverType = "http";
                    } else if (cmdLine.readAllOnce("--https")) {
                        servers.add(new SrvInfo());
                        servers.get(servers.size() - 1).serverType = "https";
                    } else if (cmdLine.readAllOnce("--admin")) {
                        servers.add(new SrvInfo());
                        servers.get(servers.size() - 1).serverType = "admin";
                    } else if (cmdLine.readAllOnce("-n", "--name")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).name = cmdLine.readRequiredNonOption(cmdLine.createNonOption("ServerName")).getString();
                    } else if (cmdLine.readAllOnce("-a", "--address")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).addr = cmdLine.readRequiredNonOption(cmdLine.createNonOption("ServerAddress")).getString();

                    } else if (cmdLine.readAllOnce("-p", "--port")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).port = cmdLine.readRequiredNonOption(cmdLine.createNonOption("ServerPort")).getInt();

                    } else if (cmdLine.readAllOnce("-l", "--backlog")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).port = cmdLine.readRequiredNonOption(cmdLine.createNonOption("ServerBacklog")).getInt();
                    } else if (cmdLine.readAllOnce("--ssl-certificate")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).sslCertificate = cmdLine.readRequiredNonOption(cmdLine.createNonOption("SslCertificate")).required().getString();
                    } else if (cmdLine.readAllOnce("--ssl-passphrase")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).sslPassphrase = cmdLine.readRequiredNonOption(cmdLine.createNonOption("SslPassPhrase")).required().getString();
                    } else {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("nuts-server: Server Type missing");
                        }
                        String s = cmdLine.readRequiredNonOption(cmdLine.createNonOption("Workspace")).getString();
                        int eq = s.indexOf('=');
                        if (eq >= 0) {
                            String serverContext = s.substring(0, eq);
                            String workspaceLocation = s.substring(eq + 1);
                            if (servers.get(servers.size() - 1).workspaceLocations.containsKey(serverContext)) {
                                throw new NutsIllegalArgumentException("nuts-server: Server Workspace context Already defined " + serverContext);
                            }
                            servers.get(servers.size() - 1).workspaceLocations.put(serverContext, workspaceLocation);
                        } else {
                            if (servers.get(servers.size() - 1).workspaceLocations.containsKey("")) {
                                throw new NutsIllegalArgumentException("nuts-server: Server Workspace context Already defined " + "");
                            }
                            servers.get(servers.size() - 1).workspaceLocations.put("", s);
                        }
                    }

                }
                if (cmdLine.isExecMode()) {
                    if (servers.isEmpty()) {
                        context.getTerminal().ferr().printf("No Server config found.\n");
                        throw new NutsExecutionException("No Server config found", 1);
                    }
                    Map<String, NutsWorkspace> allWorkspaces = new HashMap<>();
                    for (SrvInfo server : servers) {
                        Map<String, NutsWorkspace> workspaces = new HashMap<>();
                        for (Map.Entry<String, String> entry : server.workspaceLocations.entrySet()) {
                            NutsWorkspace nutsWorkspace = null;
                            if (StringUtils.isEmpty(entry.getValue())) {
                                if (context.getWorkspace() == null) {
                                    throw new NutsIllegalArgumentException("nuts-server: Missing workspace");
                                }
                                nutsWorkspace = context.getWorkspace();
                            } else {
                                nutsWorkspace = allWorkspaces.get(entry.getValue());
                                if (nutsWorkspace == null) {
                                    nutsWorkspace = context.getWorkspace().openWorkspace(
                                            new NutsWorkspaceOptions()
                                                    .setWorkspace(entry.getValue())
                                                    .setOpenMode(autocreate ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : NutsWorkspaceOpenMode.OPEN_EXISTING)
                                                    .setReadOnly(readOnly)
                                                    .setArchetype(archetype)
                                    );
                                    allWorkspaces.put(entry.getValue(), nutsWorkspace);
                                }

                            }
                            workspaces.put(entry.getKey(), nutsWorkspace);
                        }
                    }
                    for (SrvInfo server : servers) {
                        ServerConfig config0 = null;
                        switch (server.serverType) {
                            case "http":
                            case "https": {
                                NutsHttpServerConfig config = new NutsHttpServerConfig();
                                config.setAddress(server.addr == null ? null : InetAddress.getByName(server.addr));
                                config.setServerId(server.name);
                                config.setPort(server.port);
                                config.setBacklog(server.backlog);
                                config.getWorkspaces().putAll(server.workspaces);
                                if ("https".equals(server.serverType)) {
                                    config.setSsh(true);
                                    if (server.sslCertificate == null) {
                                        throw new NutsIllegalArgumentException("nuts-server: Missing SSL Certificate");
                                    }
                                    config.setSslKeystoreCertificate(IOUtils.loadByteArray(new File(server.sslCertificate)));
                                    if (server.sslPassphrase == null) {
                                        throw new NutsIllegalArgumentException("nuts-server: Missing SSL Passphrase");
                                    }
                                    config.setSslKeystorePassphrase(server.sslPassphrase.toCharArray());
                                }
                                config0 = config;
                                break;
                            }
                            case "admin": {
                                AdminServerConfig config = new AdminServerConfig();
                                config.setAddress(server.addr == null ? null : InetAddress.getByName(server.addr));
                                config.setServerId(server.name);
                                config.setPort(server.port);
                                config.setBacklog(server.backlog);
                                config0 = config;
                                break;
                            }
                            default:
                                throw new NutsIllegalArgumentException("nuts-server: Unsupported server type " + server.serverType);
                        }
                        serverManager.startServer(config0);
                    }
                }
            } else if (cmdLine.readAll("stop")) {
                String s = cmdLine.readRequiredNonOption(cmdLine.createNonOption("ServerName")).getString();
                if (cmdLine.isExecMode()) {
                    serverManager.stopServer(s);
                }
                while (cmdLine.hasNext()) {
                    s = cmdLine.readRequiredNonOption(cmdLine.createNonOption("ServerName")).getString();
                    if (cmdLine.isExecMode()) {
                        serverManager.stopServer(s);
                    }
                }
            } else if (cmdLine.readAll("list")) {
                cmdLine.setCommandName("nuts-server list").unexpectedArgument();
                if (cmdLine.isExecMode()) {
                    List<NutsServer> servers = serverManager.getServers();
                    PrintStream out = context.getTerminal().fout();
                    if (servers.isEmpty()) {
                        out.printf("No Server is Running\n");
                    }
                    for (NutsServer o : servers) {
                        if (o.isRunning()) {
                            out.printf("==Running== %s\n", o.getServerId());
                        } else {
                            out.printf("==Stopped== %s\n", o.getServerId());
                        }
                    }
                }
            } else {
                throw new NutsExecutionException("nuts-server: Invalid syntax for command server", 1);
            }
            cmdLine.setCommandName("nuts-server").unexpectedArgument();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
