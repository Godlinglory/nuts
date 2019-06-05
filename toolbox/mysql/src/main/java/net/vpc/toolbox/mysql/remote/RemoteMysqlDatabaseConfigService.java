package net.vpc.toolbox.mysql.remote;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.common.io.FileUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.ssh.SShConnection;
import net.vpc.common.ssh.SshAddress;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.mysql.remote.config.RemoteMysqlDatabaseConfig;
import net.vpc.toolbox.mysql.local.LocalMysql;
import net.vpc.toolbox.mysql.local.LocalMysqlConfigService;
import net.vpc.toolbox.mysql.local.LocalMysqlDatabaseConfigService;

import java.io.PrintStream;
import java.util.List;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsExecCommand;
import net.vpc.app.nuts.NutsCommandLineFormat;

public class RemoteMysqlDatabaseConfigService {

    private RemoteMysqlDatabaseConfig config;
    private NutsApplicationContext context;
    private RemoteMysqlConfigService client;
    private String name;

    public RemoteMysqlDatabaseConfigService(String name, RemoteMysqlDatabaseConfig config, RemoteMysqlConfigService client) {
        this.config = config;
        this.client = client;
        this.context = client.context;
        this.name = name;
    }

    public RemoteMysqlDatabaseConfig getConfig() {
        return config;
    }

    public RemoteMysqlDatabaseConfigService remove() {
        client.getConfig().getDatabases().remove(name);
        context.out().printf("==[%s]== db config removed.%n", name);
        return this;

    }

    public String getName() {
        return name;
    }

    public void write(PrintStream out) {
        context.getWorkspace().io().json().write(getConfig(), out);
    }

    public int pull() {
        LocalMysql ms = new LocalMysql(context);
        LocalMysqlConfigService loc = ms.loadOrCreateMysqlConfig(getConfig().getLocalInstance());
        String localDatabase = getConfig().getLocalDatabase();
        if (StringUtils.isBlank(localDatabase)) {
            throw new NutsExecutionException(context.getWorkspace(), "Missing local database name", 2);
        }

        RemoteMysqlDatabaseConfig cconfig = getConfig();
        String server = cconfig.getServer();
        if (StringUtils.isBlank(server)) {
            server = "ssh://localhost";
        }
        if (!server.startsWith("ssh://")) {
            server = "ssh://" + server;
        }
        String remoteTempPath = StringUtils.trim(cconfig.getRemoteTempPath());
        String localTempPath = cconfig.getPath();
        if (StringUtils.isBlank(localTempPath)) {
            localTempPath = context.getTempFolder().toString();
        }
        context.out().printf("==[%s]== remote restore '%s'%n", name, remoteTempPath);
        remoteTempPath=execRemoteNuts(
                "net.vpc.app.nuts.toolbox:mysql",
                "restore",
                "--name",
                config.getRemoteInstance(),
                "--app",
                config.getRemoteDatabase(),
                remoteTempPath
        );
        String remoteFullFilePath = new SshAddress(server).getPath(remoteTempPath).getPath();
//        LocalMysqlDatabaseConfigService.ArchiveResult archiveResult = loc.getDatabase(localDatabase).backup(null);
//        if (archiveResult.execResult != 0) {
//            return archiveResult.execResult;
//        }

        context.out().printf("==[%s]== copy '%s' to '%s'%n", name, remoteFullFilePath, localTempPath);
        context.getWorkspace().exec()
                .command(
                        "nsh",
                        "cp",
                        "--no-color",
                        remoteFullFilePath,
                        cconfig.getPath()
                ).setSession(context.getSession())
                .redirectErrorStream()
                .grabOutputString()
                .failFast()
                .run();
        context.out().printf("==[%s]== delete %s%n", name, remoteFullFilePath);
        execRemoteNuts(
                "nsh",
                "rm",
                remoteFullFilePath
        );
        return 0;
    }

    public int push() {
        LocalMysql ms = new LocalMysql(context);
        LocalMysqlConfigService loc = ms.loadOrCreateMysqlConfig(getConfig().getLocalInstance());
        String localDatabase = getConfig().getLocalDatabase();
        if (StringUtils.isBlank(localDatabase)) {
            throw new NutsExecutionException(context.getWorkspace(), "Missing local database name", 2);
        }
        LocalMysqlDatabaseConfigService.ArchiveResult archiveResult = loc.getDatabase(localDatabase).backup(null);
        if (archiveResult.execResult != 0) {
            return archiveResult.execResult;
        }
        RemoteMysqlDatabaseConfig cconfig = getConfig();
        String remoteTempPath = cconfig.getRemoteTempPath();
        String server = cconfig.getServer();
        if (StringUtils.isBlank(server)) {
            server = "ssh://localhost";
        }
        if (!server.startsWith("ssh://")) {
            server = "ssh://" + server;
        }
        if (StringUtils.isBlank(remoteTempPath)) {
            String home = null;
            try (SShConnection c = new SShConnection(new SshAddress(server))
                    .addListener(SShConnection.LOGGER)) {
                if (c.grabOutputString()
                        .exec("echo", "$HOME") == 0) {
                    home = c.getOutputString().trim();
                } else {
                    throw new NutsExecutionException(context.getWorkspace(), "Unable to detect user remote home : " + c.getOutputString().trim(), 2);
                }
            }
            remoteTempPath = home + "/tmp";
        }

        String remoteFilePath = (IOUtils.concatPath('/', remoteTempPath, FileUtils.getFileName(archiveResult.path)));
        String remoteFullFilePath = new SshAddress(server).getPath(remoteFilePath).getPath();

        context.out().printf("==[%s]== copy %s to %s%n", name, archiveResult.path, remoteFullFilePath);
        context.getWorkspace().exec()
                .command(
                        "nsh",
                        "cp",
                        "--no-color",
                        archiveResult.path,
                        remoteFullFilePath
                ).setSession(context.getSession())
                .redirectErrorStream()
                .grabOutputString()
                .failFast()
                .run();
        context.out().printf("==[%s]== remote restore %s%n", name, remoteFilePath);
        execRemoteNuts(
                "net.vpc.app.nuts.toolbox:mysql",
                "restore",
                "--name",
                config.getRemoteInstance(),
                "--app",
                config.getRemoteDatabase(),
                remoteFilePath
        );
        context.out().printf("==[%s]== delete %s%n", name, remoteFilePath);
        execRemoteNuts(
                "nsh",
                "rm",
                remoteFilePath
        );
        return 0;
    }

    public String execRemoteNuts(List<String> cmd) {
        return execRemoteNuts(cmd.toArray(new String[0]));
    }

    public String execRemoteNuts(String... cmd) {
        NutsExecCommand b = context.getWorkspace().exec()
                .setSession(context.getSession());
        b.addCommand("nsh", "-c","ssh");
        b.addCommand("--nuts");
        b.addCommand(this.config.getServer());
        b.addCommand(cmd);
        context.out().printf("[[EXEC]] %s%n", b.setCommandLineFormat(new NutsCommandLineFormat() {
            @Override
            public String replaceEnvValue(String envName, String envValue) {
                if (envName.toLowerCase().contains("password")
                        || envName.toLowerCase().contains("pwd")) {
                    return "****";
                }
                return null;
            }
        }).getCommandString());
        b.redirectErrorStream()
                .grabOutputString()
                .failFast();
        return b.run().getOutputString();

    }

}
