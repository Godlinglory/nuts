package net.thevpc.nuts.toolbox.nutsserver;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.DefaultNWorkspaceOptionsBuilder;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NArgumentName;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nutsserver.bundled._IOUtils;
import net.thevpc.nuts.toolbox.nutsserver.http.NHttpServerConfig;
import net.thevpc.nuts.util.NUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NServerMain implements NApplication {

    public static final Pattern HOST_PATTERN = Pattern.compile("((<?protocol>(http|https|admin))://)?(<host>[a-zA-Z0-9_-]+)(<port>:[0-9]+)?");

    public static void main(String[] args) {
        new NServerMain().runAndExit(args);
    }

    private CountDownLatch lock = new CountDownLatch(1);

    @Override
    public void run(NApplicationContext context) {
        NCommandLine cmdLine = context.getCommandLine().setCommandName("nuts-server");
        cmdLine.setCommandName("nuts-server");
        while (cmdLine.hasNext()) {
            if (cmdLine.next("start").isPresent()) {
                start(context, cmdLine);
                return;
            } else if (cmdLine.next("stop").isPresent()) {
                stop(context, cmdLine);
                return;
            } else if (cmdLine.next("list").isPresent()) {
                list(context, cmdLine);
                return;
            } else if (cmdLine.next("status").isPresent()) {
                status(context, cmdLine);
                return;
            } else {
                context.configureLast(cmdLine);
            }
        }
        list(context, cmdLine);
    }

    private void list(NApplicationContext context, NCommandLine cmdLine) {
        NSession session = context.getSession();
        NWorkspaceServerManager serverManager = new DefaultNWorkspaceServerManager(session);
        cmdLine.setCommandName("nuts-server list").throwUnexpectedArgument();
        if (cmdLine.isExecMode()) {
            List<NServer> servers = serverManager.getServers();
            NStream out = session.out();
            if (servers.isEmpty()) {
                out.print("No Server is Running by current instance\n");
            }
            NTexts text = NTexts.of(session);
            for (NServer o : servers) {
                if (o.isRunning()) {
                    out.printf("%s %s\n",
                            text.ofStyled("running", NTextStyle.primary4()),
                            o.getServerId()
                    );
                } else {
                    out.printf("%s %s\n",
                            text.ofStyled("stopped", NTextStyle.primary4()),
                            o.getServerId());
                }
            }
        }
    }

    private void stop(NApplicationContext context, NCommandLine cmdLine) {
        NSession session = context.getSession();
        NWorkspaceServerManager serverManager = new DefaultNWorkspaceServerManager(session);
        String s;
        int count = 0;
        while (cmdLine.hasNext()) {
            if (count == 0) {
                cmdLine.peek().get(session);
            } else if (cmdLine.isEmpty()) {
                break;
            }
            count++;
            s = cmdLine.nextNonOption(NArgumentName.of("ServerName", session)).flatMap(NValue::asString).get(session);
            if (cmdLine.isExecMode()) {
                serverManager.stopServer(s);
            }
        }
    }

    private void start(NApplicationContext context, NCommandLine commandLine) {
        NSession session = context.getSession();
        NWorkspaceServerManager serverManager = new DefaultNWorkspaceServerManager(session);
        SrvInfoList servers = new SrvInfoList(session);
        NArgument a;
        while (commandLine.hasNext()) {
            if (commandLine.next("--http").isPresent()) {
                servers.add().serverType = "http";
            } else if (commandLine.next("--https").isPresent()) {
                servers.add().serverType = "https";
            } else if (commandLine.next("--admin").isPresent()) {
                servers.add().serverType = "admin";
            } else if ((a = commandLine.nextBoolean("-R", "--read-only").orNull()) != null) {
                servers.current().readOnly = a.getBooleanValue().get(session);
            } else if ((a = commandLine.nextString("-n", "--name").orNull()) != null) {
                servers.current().name = a.getStringValue().get(session);
            } else if ((a = commandLine.nextString("-a", "--address").orNull()) != null) {
                servers.current().addr = a.getStringValue().get(session);
            } else if ((a = commandLine.nextString("-p", "--port").orNull()) != null) {
                servers.current().port = a.getValue().asInt().get(session);
            } else if ((a = commandLine.nextString("-h", "--host").orNull()) != null || (a = commandLine.nextNonOption().orNull()) != null) {
                StringBuilder s = new StringBuilder();
                if (a.key().equals("-h") || a.key().equals("--host")) {
                    s.append(a.getStringValue());
                } else {
                    s.append(a.asString());
                }
                HostStr u = parseHostStr(s.toString(), context, true);
                if (u.protocol.isEmpty()) {
                    u.protocol = "http";
                }
                servers.add().set(u);
            } else if ((a = commandLine.nextString("-l", "--backlog").orNull()) != null) {
                servers.current().port = a.getValue().asInt().get(session);
            } else if ((a = commandLine.nextString("--ssl-certificate").orNull()) != null) {
                servers.current().sslCertificate = a.getStringValue().get(session);
            } else if ((a = commandLine.nextString("--ssl-passphrase").orNull()) != null) {
                servers.current().sslPassphrase = a.getStringValue().get(session);
            } else if ((a = commandLine.nextString("-w", "--workspace").orNull()) != null) {
                String ws = a.asString().get(session);
                String serverContext = "";
                if (ws.contains("@")) {
                    serverContext = ws.substring(0, ws.indexOf('@'));
                    ws = ws.substring(ws.indexOf('@') + 1);
                }
                if (servers.current().workspaceLocations.containsKey(serverContext)) {
                    throw new NIllegalArgumentException(session,
                            NMsg.ofCstyle("nuts-server: server workspace context already defined %s", serverContext));
                }
                servers.current().workspaceLocations.put(serverContext, ws);
            } else {
                context.configureLast(commandLine);
            }

        }
        if (commandLine.isExecMode()) {
            if (servers.all.isEmpty()) {
                servers.add().set(new HostStr("http", "0.0.0.0", -1));
            }
            for (SrvInfo server : servers.all) {
                for (Map.Entry<String, String> entry : server.workspaceLocations.entrySet()) {
                    NSession nSession = null;
                    String wsContext = entry.getKey();
                    String wsLocation = entry.getValue();
                    if (NBlankable.isBlank(wsContext) || wsContext.equals(".")) {
                        wsContext = "";
                    }
                    if (NBlankable.isBlank(wsContext)) {
                        NUtils.requireNonNull(context.getWorkspace(), "workspace", session);
                        nSession = session;
                        server.workspaces.put(wsContext, nSession);
                    } else {
                        nSession = server.workspaces.get(wsContext);
                        if (nSession == null) {
                            nSession = Nuts.openWorkspace(
                                    new DefaultNWorkspaceOptionsBuilder()
                                            .setWorkspace(wsLocation)
                                            .setOpenMode(NOpenMode.OPEN_OR_ERROR)
                                            .setReadOnly(server.readOnly)
                            );
                            server.workspaces.put(wsContext, nSession);
                        }
                    }
                }
            }
            HashSet<String> visitedNames = new HashSet<>();
            for (SrvInfo server : servers.all) {
                ServerConfig config0 = null;
                switch (server.serverType) {
                    case "http":
                    case "https": {
                        NHttpServerConfig config = new NHttpServerConfig();
                        try {
                            config.setAddress(server.addr == null ? null : InetAddress.getByName(server.addr));
                        } catch (UnknownHostException e) {
                            throw new UncheckedIOException(e);
                        }
                        config.setServerId(server.name = validateName(server.name, "https".equals(server.serverType) ? "nuts-https-server" : "nuts-http-server", visitedNames));
                        config.setPort(server.port);
                        config.setBacklog(server.backlog);
                        config.getWorkspaces().putAll(server.workspaces);
                        if ("https".equals(server.serverType)) {
                            config.setTls(true);
                            NUtils.requireNonBlank(server.sslCertificate, "SSL certificate", session);
                            NUtils.requireNonBlank(server.sslPassphrase, "SSL passphrase", session);
                            try {
                                config.setSslKeystoreCertificate(_IOUtils.loadByteArray(new File(server.sslCertificate)));
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                            config.setSslKeystorePassphrase(server.sslPassphrase.toCharArray());
                        }
                        config0 = config;
                        break;
                    }
                    case "admin": {
                        AdminServerConfig config = new AdminServerConfig();
                        try {
                            config.setAddress(server.addr == null ? null : InetAddress.getByName(server.addr));
                        } catch (UnknownHostException e) {
                            throw new UncheckedIOException(e);
                        }
                        config.setServerId(server.name = validateName(server.name, "nuts-admin-server", visitedNames));
                        config.setPort(server.port);
                        config.setBacklog(server.backlog);
                        config0 = config;
                        break;
                    }
                    default:
                        throw new NIllegalArgumentException(session,
                                NMsg.ofCstyle("nuts-server: unsupported server type %s", server.serverType)
                        );
                }
                serverManager.startServer(config0);
            }
        }
        waitAllServers();
    }

    private class HostStr {
        String protocol = "http";
        String addr = null;
        int port = -1;

        public HostStr() {
        }

        public HostStr(String protocol, String addr, int port) {
            this.protocol = protocol;
            this.addr = addr;
            this.port = port;
        }
    }

    private HostStr parseHostStr(String host, NApplicationContext context, boolean srv) {
        try {
            Matcher pattern = HOST_PATTERN.matcher(host);
            HostStr v = new HostStr();
            v.protocol = "";
            v.addr = srv ? "0.0.0.0" : "localhost";
            v.port = -1;
            if (pattern.find()) {
                if (pattern.group("protocol") != null) {
                    v.protocol = pattern.group("protocol");
                }
                v.addr = pattern.group("host");
                if (pattern.group("port") != null) {
                    v.port = Integer.parseInt(pattern.group("port"));
                }
            } else {
                throw new NIllegalArgumentException(context.getSession(),
                        NMsg.ofCstyle("invalid Host : %s", v.protocol)
                );
            }
            return v;
        } catch (Exception ex) {
            throw new NIllegalArgumentException(context.getSession(), NMsg.ofPlain("invalid"), ex);
        }
    }

    private static class StatusResult {
        String host;
        String type;
        String status;

        public StatusResult(String host, String type, String status) {
            this.host = host;
            this.status = status;
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public String getHost() {
            return host;
        }

        public String getStatus() {
            return status;
        }
    }

    private void status(NApplicationContext context, NCommandLine commandLine) {
        NSession session = context.getSession();
        NWorkspaceServerManager serverManager = new DefaultNWorkspaceServerManager(session);
        SrvInfoList servers = new SrvInfoList(session);
        NArgument a;
        while (commandLine.hasNext()) {
            if (commandLine.next("--http").isPresent()) {
                servers.add().serverType = "http";
            } else if (commandLine.next("--https").isPresent()) {
                servers.add().serverType = "https";
            } else if (commandLine.next("--admin").isPresent()) {
                servers.add().serverType = "admin";
            } else if ((a = commandLine.nextString("-a", "--address").orNull()) != null) {
                servers.current().addr = a.getStringValue().get(session);
            } else if ((a = commandLine.nextString("-p", "--port").orNull()) != null) {
                servers.current().port = a.getValue().asInt().get(session);
            } else if ((a = commandLine.nextString("-h", "--host").orNull()) != null || (a = commandLine.nextNonOption().orNull()) != null) {
                StringBuilder s = new StringBuilder();
                if (a.key().equals("-h") || a.key().equals("--host")) {
                    s.append(a.getStringValue());
                } else {
                    s.append(a.asString());
                }
                HostStr u = parseHostStr(s.toString(), context, false);
                servers.add().set(u);
            } else {
                context.configureLast(commandLine);
            }

        }
        if (commandLine.isExecMode()) {
            if (servers.all.isEmpty()) {
                servers.add().set(new HostStr("http", "localhost", NServerConstants.DEFAULT_HTTP_SERVER_PORT));
                servers.add().set(new HostStr("https", "localhost", NServerConstants.DEFAULT_HTTP_SERVER_PORT));
                servers.add().set(new HostStr("admin", "localhost", NServerConstants.DEFAULT_ADMIN_SERVER_PORT));
            }
            List<StatusResult> results = new ArrayList<>();
            for (SrvInfo server : servers.all) {
                String aliveType = null;
                String url = null;
                if (server.serverType.isEmpty() || server.serverType.equals("http")) {
                    try {
                        String addr = server.addr != null ? server.addr : "localhost";
                        url = "http://" + addr + (server.port > 0 ? (":" + server.port) : (":" + NServerConstants.DEFAULT_HTTP_SERVER_PORT));
                        new URL(url + "/archetype-catalog.xml").openStream();
                        aliveType = "maven";
                    } catch (Exception ex) {
                        //
                    }
                }
                if (aliveType == null) {
                    if (server.serverType.isEmpty() || server.serverType.equals("https")) {
                        String addr = server.addr != null ? server.addr : "localhost";
                        url = "https://" + addr + (server.port > 0 ? (":" + server.port) : (":" + NServerConstants.DEFAULT_HTTP_SERVER_PORT));
                        try {
                            new URL(url + "/.files").openStream();
                            aliveType = "nuts";
                        } catch (Exception ex) {
                            //
                        }
                    }
                }
                if (aliveType == null) {
                    if (server.serverType.isEmpty() || server.serverType.equals("admin")) {
                        url = "admin://" + server.addr + (server.port > 0 ? (":" + server.port) : (":" + NServerConstants.DEFAULT_ADMIN_SERVER_PORT));
                        try (Socket s = new Socket(InetAddress.getByName(server.addr), (server.port > 0 ? server.port : NServerConstants.DEFAULT_ADMIN_SERVER_PORT))) {
                            Reader in = new InputStreamReader(s.getInputStream());
                            if (readString("Nuts Admin Service", in)) {
                                aliveType = "admin";
                            }
                        } catch (IOException e) {
                            //
                        }
                    }
                }

                if (aliveType != null) {
                    results.add(new StatusResult(url, aliveType, "alive"));
                } else {
                    url = (server.serverType.isEmpty() ? "?" : server.serverType) + "://" + server.addr + (server.port > 0 ? (":" + server.port) : "");
                    results.add(new StatusResult(url, (server.serverType.isEmpty() ? "?" : server.serverType), "stopped"));
                }
            }
            if (session.isPlainOut()) {
                NTexts text = NTexts.of(session);
                for (StatusResult result : results) {
                    session.out().printf(
                            "%s server at %s is %s%n",
                            text.ofStyled(result.type, NTextStyle.primary4()),
                            result.host,
                            result.status.equals("stopped") ?
                                    text.ofStyled("stopped", NTextStyle.error()) :
                                    text.ofStyled("alive", NTextStyle.success())
                    );
                }
            } else {
                session.out().printlnf(results);
            }
        }
    }

    public boolean readString(String toRead, Reader reader) throws IOException {
        for (char c : toRead.toCharArray()) {
            int y = reader.read();
            if (y != c) {
                return false;
            }
        }
        return true;
    }

    public void stopWaiting() {
        if (lock.getCount() > 0) {
            lock.countDown();
        }
    }

    private void waitAllServers() {
        try {
            lock.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class SrvInfoList {
        List<SrvInfo> all = new ArrayList<>();
        NSession session;

        SrvInfoList(NSession session) {
            this.session = session;
        }

        SrvInfo add() {
            SrvInfo s = new SrvInfo();
            all.add(s);
            return s;
        }

        SrvInfo current() {
            NUtils.requireNonBlank(all, "server type", session);
            return all.get(all.size() - 1);
        }
    }

    static class SrvInfo {

        String name = null;
        String serverType = "http";
        String addr = null;
        int port = -1;
        int backlog = -1;
        String sslCertificate = null;
        String sslPassphrase = null;
        Map<String, String> workspaceLocations = new LinkedHashMap<>();
        Map<String, NSession> workspaces = new HashMap<>();
        boolean readOnly = false;

        public void set(HostStr s) {
            if (s != null) {
                serverType = s.protocol;
                addr = s.addr;
                port = s.port;
            }
        }
    }


    private String validateName(String name, String defaultName, Set<String> visited) {
        if (name == null) {
            name = "";
        }
        name = name.trim();
        if (name.isEmpty()) {
            name = defaultName;
        }
        int x = 1;
        while (true) {
            String n = name + (x == 1 ? "" : ("-" + x));
            if (!visited.contains(n)) {
                visited.add(n);
                return n;
            }
            x++;
        }
    }

}
