package net.thevpc.nuts.toolbox.tomcat.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.tomcat.NTomcatConfigVersions;
import net.thevpc.nuts.toolbox.tomcat.local.LocalTomcatConfigService;
import net.thevpc.nuts.toolbox.tomcat.remote.config.RemoteTomcatAppConfig;
import net.thevpc.nuts.toolbox.tomcat.remote.config.RemoteTomcatConfig;
import net.thevpc.nuts.toolbox.tomcat.util.NamedItemNotFoundException;
import net.thevpc.nuts.toolbox.tomcat.util.TomcatUtils;
import net.thevpc.nuts.toolbox.tomcat.util._StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RemoteTomcatConfigService extends RemoteTomcatServiceBase {
    private static final String NTOMCAT = "net.thevpc.nuts.toolbox:ntomcat";

    public static final String REMOTE_CONFIG_EXT = ".remote-config";
    private String name;
    RemoteTomcatConfig config;
    NutsApplicationContext context;
    RemoteTomcat client;
    Path sharedConfigFolder;

    public RemoteTomcatConfigService(String name, RemoteTomcat client) {
        setName(name);
        this.client = client;
        this.context = client.context;
        sharedConfigFolder = Paths.get(client.getContext().getVersionFolderFolder(NutsStoreLocation.CONFIG, NTomcatConfigVersions.CURRENT));
    }

    public RemoteTomcatConfigService(Path file, RemoteTomcat client) {
        this(
                file.getFileName().toString().substring(0, file.getFileName().toString().length() - LocalTomcatConfigService.LOCAL_CONFIG_EXT.length()),
                client
        );
    }

    public RemoteTomcatConfigService setName(String name) {
        this.name = TomcatUtils.toValidFileName(name, "default");
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RemoteTomcatConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    public Path getConfigPath() {
        return sharedConfigFolder.resolve(name + REMOTE_CONFIG_EXT);
    }

    public RemoteTomcatConfigService save() {
        Path f = getConfigPath();
        context.getWorkspace().elem().setContentType(NutsContentType.JSON).setValue(config).print(f);
        return this;
    }

    public boolean existsConfig() {
        Path f = getConfigPath();
        return (Files.exists(f));
    }

    public void printStatus() {
        execRemoteNuts(
                NTOMCAT,
                "--status",
                "--name",
                getRemoteInstanceName()
        );
    }

    public void start(String[] redeploy, boolean deleteOutLog) {
        List<String> arg = new ArrayList<>();
        arg.add(NTOMCAT);
        arg.add("--start");
        arg.add("--name");
        arg.add(getRemoteInstanceName());
        StringBuilder sb = new StringBuilder();
        for (String s : redeploy) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(s);
        }
        if (sb.length() > 0) {
            arg.add("--deploy");
            arg.add(sb.toString());
        }
        if (deleteOutLog) {
            arg.add("--deleteOutLog");
        }
        execRemoteNuts(arg.toArray(new String[0]));
    }

    public void shutdown() {
        execRemoteNuts(NTOMCAT,
                "--stop",
                "--name",
                getRemoteInstanceName()
        );
    }

    public String getRemoteInstanceName() {
        String n = getConfig().getRemoteName();
        return _StringUtils.isBlank(n) ? "default" : n;
    }

    public void restart(String[] redeploy, boolean deleteOutLog) {
        List<String> arg = new ArrayList<>();
        arg.add(NTOMCAT);
        arg.add("restart");
        arg.add("--name");
        arg.add(getRemoteInstanceName());
        StringBuilder sb = new StringBuilder();
        for (String s : redeploy) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(s);
        }
        if (sb.length() > 0) {
            arg.add("--deploy");
            arg.add(sb.toString());
        }
        if (deleteOutLog) {
            arg.add("--deleteOutLog");
        }
        execRemoteNuts(arg.toArray(new String[0]));
    }

    public RemoteTomcatConfigService loadConfig() {
        if (name == null) {
            throw new NutsExecutionException(context.getSession(), "missing instance name", 2);
        }
        Path f = getConfigPath();
        if (Files.exists(f)) {
            config = context.getWorkspace().elem().setContentType(NutsContentType.JSON).parse(f, RemoteTomcatConfig.class);
            return this;
        }
        throw new NamedItemNotFoundException("instance not found : " + getName(),getName());
    }

    @Override
    public RemoteTomcatConfigService remove() {
        Path f = getConfigPath();
        try {
            Files.delete(f);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return this;
    }

    @Override
    public RemoteTomcatConfigService print(NutsPrintStream out) {
        context.getWorkspace().elem().setContentType(NutsContentType.JSON).setValue(getConfig()).print(out);
        out.flush();
        return this;
    }

    public RemoteTomcatConfigService setConfig(RemoteTomcatConfig config) {
        this.config = config;
        return this;
    }

    public RemoteTomcatAppConfigService getApp(String appName) {
        return getAppOrError(appName);
    }

    public RemoteTomcatAppConfigService getAppOrNull(String appName) {
        RemoteTomcatAppConfig a = getConfig().getApps().get(appName);
        if (a == null) {
            return null;
        }
        return new RemoteTomcatAppConfigService(appName, a, this);
    }

    public RemoteTomcatAppConfigService getAppOrError(String appName) {
        RemoteTomcatAppConfig a = getConfig().getApps().get(appName);
        if (a == null) {
            throw new NutsExecutionException(context.getSession(), "app not found :" + appName, 2);
        }
        return new RemoteTomcatAppConfigService(appName, a, this);
    }

    public RemoteTomcatAppConfigService getAppOrCreate(String appName) {
        RemoteTomcatAppConfig a = getConfig().getApps().get(appName);
        if (a == null) {
            a = new RemoteTomcatAppConfig();
            getConfig().getApps().put(appName, a);
        }
        return new RemoteTomcatAppConfigService(appName, a, this);
    }

    public void deleteOutLog() {
        execRemoteNuts(NTOMCAT,
                "--deleteOutLog",
                "--name",
                getRemoteInstanceName()
        );
    }

    public List<RemoteTomcatAppConfigService> getApps() {
        List<RemoteTomcatAppConfigService> a = new ArrayList<>();
        for (String s : getConfig().getApps().keySet()) {
            a.add(new RemoteTomcatAppConfigService(s, getConfig().getApps().get(s), this));
        }
        return a;
    }

    public void execRemoteNuts(String... cmd) {
        RemoteTomcatConfig cconfig = getConfig();
        List<String> cmdList = new ArrayList<>(Arrays.asList(
                "nsh",
                "-c",
                "ssh"
        ));
        cmdList.add(this.config.getServer());
        cmdList.add("nuts");
        cmdList.add("--bot");
        cmdList.addAll(Arrays.asList(cmd));
        context.getWorkspace().exec()
                .setSession(context.getSession())
                .addCommand(cmdList)
                .setFailFast(true)
                .run();

    }
}
