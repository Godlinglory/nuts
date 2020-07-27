package net.vpc.toolbox.mysql.remote;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.common.io.FileUtils;
import net.vpc.toolbox.mysql.remote.config.RemoteMysqlDatabaseConfig;
import net.vpc.toolbox.mysql.remote.config.RemoteMysqlConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import net.vpc.app.nuts.NutsApplicationContext;

public class RemoteMysqlConfigService {

    public static final String CLIENT_CONFIG_EXT = ".remote-config";
    private String name;
    RemoteMysqlConfig config;
    NutsApplicationContext context;
    RemoteMysql client;

    public RemoteMysqlConfigService(String name, RemoteMysql client) {
        setName(name);
        this.client = client;
        this.context = client.context;
    }

    public RemoteMysqlConfigService setName(String name) {
        this.name = FileUtils.toValidFileName(name, "default");
        return this;
    }

    public String getName() {
        return name;
    }

    public RemoteMysqlConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    public RemoteMysqlConfigService saveConfig() {
        Path f = getConfigPath();
        context.getWorkspace().json().value(config).print(f);
        return this;
    }

    private Path getConfigPath() {
        return context.getSharedConfigFolder().resolve(name + CLIENT_CONFIG_EXT);
    }

    public boolean existsConfig() {
        Path f = getConfigPath();
        return (Files.exists(f));
    }

    public RemoteMysqlConfigService loadConfig() {
        if (name == null) {
            throw new NutsExecutionException(context.getWorkspace(), "Missing config name", 2);
        }
        Path f = getConfigPath();
        if (Files.exists(f)) {
            config = context.getWorkspace().json().parse(f, RemoteMysqlConfig.class);
            return this;
        }
        throw new NoSuchElementException("Config not found : " + name);
    }

    public RemoteMysqlConfigService removeConfig() {
        Path f = getConfigPath();
        try {
            Files.delete(f);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return this;
    }

    public RemoteMysqlConfigService write(PrintStream out) {
        context.getWorkspace().json().value(getConfig()).print(out);
        out.flush();
        return this;
    }

    public RemoteMysqlConfigService setConfig(RemoteMysqlConfig config) {
        this.config = config;
        return this;
    }

    public RemoteMysqlDatabaseConfigService getDatabase(String appName) {
        return getDatabaseOrError(appName);
    }

    public RemoteMysqlDatabaseConfigService getDatabaseOrNull(String appName) {
        RemoteMysqlDatabaseConfig a = getConfig().getDatabases().get(appName);
        if (a == null) {
            return null;
        }
        return new RemoteMysqlDatabaseConfigService(appName, a, this);
    }

    public RemoteMysqlDatabaseConfigService getDatabaseOrError(String appName) {
        RemoteMysqlDatabaseConfig a = getConfig().getDatabases().get(appName);
        if (a == null) {
            throw new NutsExecutionException(context.getWorkspace(), "App not found :" + appName, 2);
        }
        return new RemoteMysqlDatabaseConfigService(appName, a, this);
    }

    public RemoteMysqlDatabaseConfigService getDatabaseOrCreate(String appName) {
        RemoteMysqlDatabaseConfig a = getConfig().getDatabases().get(appName);
        if (a == null) {
            a = new RemoteMysqlDatabaseConfig();
            getConfig().getDatabases().put(appName, a);
        }
        return new RemoteMysqlDatabaseConfigService(appName, a, this);
    }

    public List<RemoteMysqlDatabaseConfigService> getDatabases() {
        List<RemoteMysqlDatabaseConfigService> a = new ArrayList<>();
        for (String s : getConfig().getDatabases().keySet()) {
            a.add(new RemoteMysqlDatabaseConfigService(s, getConfig().getDatabases().get(s), this));
        }
        return a;
    }

}