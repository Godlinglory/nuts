package net.vpc.toolbox.mysql.remote;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsQuestion;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.mysql.remote.config.RemoteMysqlConfig;
import net.vpc.toolbox.mysql.util.UserCancelException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgument;

public class RemoteMysql {

    public NutsApplicationContext context;

    public RemoteMysql(NutsApplicationContext context) {
        this.context = context;
    }

    public int runArgs(String[] args) {
        NutsCommandLine cmd = context.getWorkspace().parser().parseCommandLine(args);
        NutsArgument a;
        while (cmd.hasNext()) {
            if ((a = cmd.readNonOption("list")) != null) {
                return list(cmd);
            } else if ((a = cmd.readNonOption("add", "set")) != null) {
                return list(cmd);
            } else if ((a = cmd.readNonOption("remove")) != null) {
                return remove(cmd);
            } else if ((a = cmd.readNonOption("push")) != null) {
                return push(cmd);
            } else if ((a = cmd.readNonOption("pull")) != null) {
                return pull(cmd);
            } else {
                cmd.setCommandName("mysql --remote").unexpectedArgument();
            }
        }
        return 0;
    }

    public int list(NutsCommandLine args) {
        boolean json = false;
        String instance = null;
        String app = null;
        String property = null;
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.readBooleanOption("--json")) != null) {
                json = a.getBooleanValue();
            } else if ((a = args.readBooleanOption("--name")) != null) {
                instance = a.getValue().getString();
            } else if ((a = args.readBooleanOption("--db")) != null) {
                app = a.getValue().getString();
            } else if ((a = args.readBooleanOption("--property")) != null) {
                property = a.getValue().getString();
            } else {
                args.setCommandName("mysql --remote list").unexpectedArgument();
            }
        }
        if (property == null) {
            if (app != null) {
                RemoteMysqlConfigService c = loadOrCreateMysqlConfig(instance);
                RemoteMysqlDatabaseConfigService aa = c.getDatabase(app);
                if (json) {
                    context.out().printf("[[%s]] :%n", aa.getName());
                    aa.write(context.out());
                    context.out().println();
                } else {
                    context.out().println(aa.getName());
                }
            } else {
                for (RemoteMysqlConfigService mysqlConfig : listConfig()) {
                    if (json) {
                        context.out().printf("[[%s]] :%n", mysqlConfig.getName());
                        mysqlConfig.write(context.out());
                        context.out().println();
                    } else {
                        context.out().println(mysqlConfig.getName());
                    }
                }
            }
        } else {
            RemoteMysqlConfigService c = loadOrCreateMysqlConfig(instance);
            if (app != null) {
                context.out().printf("%s%n", context.getWorkspace().parser().parseExpression(c.getDatabase(app).getConfig(), property));
            } else {
                for (RemoteMysqlDatabaseConfigService aa : c.getApps()) {
                    context.out().printf("[%s] %s%n", aa.getName(), context.getWorkspace().parser().parseExpression(aa.getConfig(), property));
                }
            }
        }
        return 0;
    }

    private int add(NutsCommandLine args) {
        RemoteMysqlConfigService c = null;
        String appName = null;
        String instanceName = null;
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--name")) != null) {
                if (c == null) {
                    instanceName = a.getValue().getString();
                    c = loadOrCreateMysqlConfig(instanceName);
                } else {
                    throw new NutsExecutionException("instance already defined", 2);
                }
            } else if ((a = args.readStringOption("--server")) != null) {
                if (c == null) {
                    c = loadOrCreateMysqlConfig(null);
                }
                RemoteMysqlDatabaseConfigService db = c.getDatabaseOrError(appName);
                db.getConfig().setServer(a.getValue().getString());
            } else if ((a = args.readStringOption("--remote-instance")) != null) {
                if (c == null) {
                    c = loadOrCreateMysqlConfig(null);
                }
                RemoteMysqlDatabaseConfigService db = c.getDatabaseOrError(appName);
                db.getConfig().setRemoteInstance(a.getValue().getString());
            } else if ((a = args.readStringOption("--remote-temp-path")) != null) {
                if (c == null) {
                    c = loadOrCreateMysqlConfig(null);
                }
                RemoteMysqlDatabaseConfigService db = c.getDatabaseOrError(appName);
                db.getConfig().setRemoteTempPath(a.getValue().getString());
            } else if ((a = args.readStringOption("--app")) != null) {
                appName = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateMysqlConfig(null);
                }
                c.getDatabaseOrCreate(appName);
            } else if ((a = args.readStringOption("--app.path")) != null) {
                String value = a.getValue().getString();
                if (c == null) {
                    c = loadOrCreateMysqlConfig(null);
                }
                RemoteMysqlDatabaseConfigService mysqlAppConfig = c.getDatabaseOrError(appName);
                mysqlAppConfig.getConfig().setPath(value);
            } else {
                args.setCommandName("mysql --remote add").unexpectedArgument();
            }
        }
        if (c == null) {
            c = loadOrCreateMysqlConfig(null);
        }
        boolean ok = false;
        while (!ok) {
            try {
                ok = true;
                RemoteMysqlDatabaseConfigService db = c.getDatabaseOrError(appName);
                if (StringUtils.isEmpty(db.getConfig().getServer())) {
                    ok = false;
                    db.getConfig().setServer(context.terminal().ask(NutsQuestion.forString("[instance=[[%s]]] Would you enter ==%s== value?", c.getName(), "--server").setDefautValue("ssh://login@myserver")));
                }
                if (StringUtils.isEmpty(db.getConfig().getRemoteInstance())) {
                    ok = false;
                    db.getConfig().setRemoteInstance(context.terminal().ask(NutsQuestion.forString("[instance=[[%s]]] Would you enter ==%s== value?", c.getName(), "--remote-instance").setDefautValue("default")));
                }
                if (StringUtils.isEmpty(db.getConfig().getRemoteTempPath())) {
                    ok = false;
                    db.getConfig().setRemoteTempPath(context.terminal().ask(NutsQuestion.forString("[instance=[[%s]]] Would you enter ==%s== value?", c.getName(), "--remote-temp-path").setDefautValue("/tmp")));
                }
                for (RemoteMysqlDatabaseConfigService aa : c.getApps()) {
                    if (StringUtils.isEmpty(aa.getConfig().getPath())) {
                        ok = false;
                        aa.getConfig().setPath(context.terminal().ask(NutsQuestion.forString("[instance=[[%s]]] [app=[[%s]]] Would you enter ==%s== value?", c.getName(), aa.getName(), "-app.path")));
                    }
                }
            } catch (UserCancelException ex) {
                return 1;
            }
        }
        c.saveConfig();
        return 0;
    }

    private int remove(NutsCommandLine args) {
        String instance = null;
        String appName = null;
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--db")) != null) {
                appName = a.getValue().getString();
            } else if ((a = args.readStringOption("--name")) != null) {
                instance = a.getValue().getString();
            } else {
                args.setCommandName("mysql --remote remove").unexpectedArgument();
            }
        }
        if (appName == null) {
            loadMysqlConfig(instance).removeConfig();
        } else {
            RemoteMysqlConfigService c = loadMysqlConfig(instance);
            try {
                c.getDatabaseOrError(appName).remove();
                c.saveConfig();
            } catch (Exception ex) {
                //
            }
        }
        return 0;
    }

    private int pull(NutsCommandLine args) {
        String conf = null;
        String app = null;
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--name")) != null) {
                conf = a.getValue().getString();
            } else if ((a = args.readStringOption("--db")) != null) {
                app = a.getValue().getString();
            } else {
                args.setCommandName("mysql --remote pull").unexpectedArgument();
            }
        }
        RemoteMysqlConfigService c = loadMysqlConfig(conf);
        c.getDatabase(app).pull();
        return 0;
    }

    private int push(NutsCommandLine args) {
        String conf = null;
        String app = null;
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.readStringOption("--name")) != null) {
                conf = a.getValue().getString();
            } else if ((a = args.readStringOption("--db")) != null) {
                app = a.getValue().getString();
            } else {
                args.setCommandName("mysql --remote push").unexpectedArgument();
            }
        }
        RemoteMysqlConfigService c = loadMysqlConfig(conf);
        c.getDatabase(app).push();
        return 0;
    }

    public RemoteMysqlConfigService[] listConfig() {
        List<RemoteMysqlConfigService> all = new ArrayList<>();
        try (DirectoryStream<Path> configFiles = Files.newDirectoryStream(context.getConfigFolder(), x -> x.getFileName().toString().endsWith(".config"))) {
            for (Path file1 : configFiles) {
                try {
                    String nn = file1.getFileName().toString();
                    RemoteMysqlConfigService c = loadMysqlConfig(nn.substring(0, nn.length() - ".config".length()));
                    all.add(c);
                } catch (Exception ex) {
                    //ignore
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return all.toArray(new RemoteMysqlConfigService[0]);
    }

    public RemoteMysqlConfigService loadMysqlConfig(String name) {
        RemoteMysqlConfigService t = new RemoteMysqlConfigService(name, this);
        t.loadConfig();
        return t;
    }

    public RemoteMysqlConfigService createMysqlConfig(String name) {
        RemoteMysqlConfigService t = new RemoteMysqlConfigService(name, this);
        t.setConfig(new RemoteMysqlConfig());
        return t;
    }

    public RemoteMysqlConfigService loadOrCreateMysqlConfig(String name) {
        RemoteMysqlConfigService t = new RemoteMysqlConfigService(name, this);
        if (t.existsConfig()) {
            t.loadConfig();
        } else {
            t.setConfig(new RemoteMysqlConfig());
        }
        return t;
    }
}
