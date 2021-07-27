package net.thevpc.nuts.toolbox.tomcat.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.tomcat.remote.config.RemoteTomcatAppConfig;
import net.thevpc.nuts.toolbox.tomcat.remote.config.RemoteTomcatConfig;
import net.thevpc.nuts.toolbox.tomcat.util._FileUtils;
import net.thevpc.nuts.toolbox.tomcat.util._StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RemoteTomcatAppConfigService extends RemoteTomcatServiceBase {

    private RemoteTomcatAppConfig config;
    private NutsApplicationContext context;
    private RemoteTomcatConfigService client;
    private String name;

    public RemoteTomcatAppConfigService(String name, RemoteTomcatAppConfig config, RemoteTomcatConfigService client) {
        this.config = config;
        this.client = client;
        this.context = client.context;
        this.name = name;
    }

    public void install() {
        RemoteTomcatConfig cconfig = client.getConfig();
        String localWarPath = this.config.getPath();
        if (!new File(localWarPath).exists()) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("missing source war file %s", localWarPath), 2);
        }
        String remoteTempPath = cconfig.getRemoteTempPath();
        if (_StringUtils.isBlank(remoteTempPath)) {
            remoteTempPath = "/tmp";
        }
        String remoteFilePath = ("/" + remoteTempPath + "/" + _FileUtils.getFileName(localWarPath));
        String server = cconfig.getServer();
        if (_StringUtils.isBlank(server)) {
            server = "localhost";
        }
        if (!server.startsWith("ssh://")) {
            server = "ssh://" + server;
        }
        context.getWorkspace().exec()
                .addCommand(
                        "nsh",
                        "--bot",
                        "cp",
                        "--verbose",
                        "--mkdir",
                        localWarPath,
                        server + "/" + remoteFilePath
                ).setSession(context.getSession())
                .run();
        String v = config.getVersionCommand();
        if (_StringUtils.isBlank(v)) {
            v = "nsh nversion --color=never %file";
        }
        List<String> cmd = Arrays.asList(
                context.getWorkspace().commandLine().parse(v).toStringArray()
        );
        boolean fileAdded = false;
        for (int i = 0; i < cmd.size(); i++) {
            if ("%file".equals(cmd.get(i))) {
                cmd.set(i, config.getPath());
                fileAdded = true;
            }
        }
        if (!fileAdded) {
            cmd.add(config.getPath());
        }
        NutsExecCommand s = context.getWorkspace()
                .exec()
                .setRedirectErrorStream(true)
                .grabOutputString()
                .addCommand(cmd).run();
        if (s.getResult() == 0) {
            client.execRemoteNuts(
                    "net.thevpc.nuts.toolbox:tomcat",
                    "install",
                    "--name",
                    client.getRemoteInstanceName(),
                    "--app",
                    name,
                    "--version",
                    s.getOutputString().trim(),
                    "--file",
                    remoteFilePath
            );
            client.execRemoteNuts(
                    "nsh",
                    "--bot",
                    "rm",
                    remoteFilePath
            );
        } else {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to detect file version of %s.\n%s",localWarPath ,
                    s.getOutputString()), 2);
        }
    }

    public void deploy(String version) {
        client.execRemoteNuts(
                "net.thevpc.nuts.toolbox:tomcat",
                "deploy",
                "--name",
                client.getRemoteInstanceName(),
                "--app",
                name,
                "--version",
                _StringUtils.trim(version)
        );
    }

    public RemoteTomcatAppConfig getConfig() {
        return config;
    }

    public String getName() {
        return name;
    }

    public RemoteTomcatAppConfigService print(NutsPrintStream out) {
        NutsWorkspace ws = context.getWorkspace();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("config-name", getName());
        m.putAll(ws.elem().convert(getConfig(), Map.class));
        ws.formats().object().setSession(context.getSession()).setValue(m).print(out);
        return this;
    }

    public RemoteTomcatAppConfigService remove() {
        client.getConfig().getApps().remove(name);
        context.getSession().out().printf("%s app removed.\n", getBracketsPrefix(name));
        return this;

    }

    public NutsString getBracketsPrefix(String str) {
        return context.getWorkspace().text().builder()
                .append("[")
                .append(str, NutsTextStyle.primary5())
                .append("]");
    }

    public RemoteTomcatConfigService getTomcat() {
        return client;
    }
}
