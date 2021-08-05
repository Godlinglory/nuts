package net.thevpc.nuts.runtime.standalone.wscommands;

import java.io.IOException;
import java.nio.file.Files;

import net.thevpc.nuts.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.config.ConfigEventType;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class ConfigNutsWorkspaceCommandFactory implements NutsWorkspaceCommandFactory {

    private NutsLogger LOG;
    private NutsWorkspace ws;

    public ConfigNutsWorkspaceCommandFactory(NutsWorkspace ws) {
        this.ws = ws;
    }

     protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = session.getWorkspace().log().setSession(session).of(ConfigNutsWorkspaceCommandFactory.class);
        }
        return LOG;
    }

    @Override
    public void configure(NutsCommandAliasFactoryConfig config) {

    }

    @Override
    public String getFactoryId() {
        return "default";
    }

    public Path getStoreLocation(NutsSession session) {
        checkSession(session);
        return Paths.get(session.getWorkspace().locations().getStoreLocation(session.getWorkspace().getApiId(), NutsStoreLocation.APPS));
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    private Path getCommandsFolder(NutsSession session){
        checkSession(session);
//        options = CoreNutsUtils.validate(options, ws);
        return getStoreLocation(session).resolve("cmd");
    }

    public void uninstallCommand(String name, NutsSession session) {
        checkSession(session);
//        options = CoreNutsUtils.validate(options, ws);
        Path file = getCommandsFolder(session).resolve(name + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        if (Files.exists(file)) {
            try {
                Files.delete(file);
                NutsWorkspaceConfigManagerExt.of(session.getWorkspace().config()).getModel().fireConfigurationChanged("command", session, ConfigEventType.MAIN);
            } catch (IOException ex) {
                throw new NutsIOException(session,ex);
            }
        }
    }

    protected void checkSession(NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
    }

    public void installCommand(NutsCommandAliasConfig command, NutsSession session) {
        checkSession(session);
        Path path = getCommandsFolder(session).resolve(command.getName() + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        session.getWorkspace().elem().setContentType(NutsContentType.JSON).setValue(command).print(path);
        NutsWorkspaceConfigManagerExt.of(session.getWorkspace().config()).getModel().fireConfigurationChanged("command", session, ConfigEventType.MAIN);
    }

    @Override
    public NutsCommandAliasConfig findCommand(String name, NutsSession session) {
        checkSession(session);
        Path file = getCommandsFolder(session).resolve(name + NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION);
        if (Files.exists(file)) {
            NutsCommandAliasConfig c = session.getWorkspace().elem().setContentType(NutsContentType.JSON).parse(file, NutsCommandAliasConfig.class);
            if (c != null) {
                c.setName(name);
                return c;
            }
        }
        return null;
    }

    @Override
    public List<NutsCommandAliasConfig> findCommands(NutsSession session) {
        return findCommands((Predicate<NutsCommandAliasConfig>) null, session);
    }

    public List<NutsCommandAliasConfig> findCommands(NutsId id, NutsSession session) {
        return findCommands(value -> CoreNutsUtils.matchesSimpleNameStaticVersion(value.getOwner(), id), session);
    }

    public List<NutsCommandAliasConfig> findCommands(Predicate<NutsCommandAliasConfig> filter, NutsSession session) {
        checkSession(session);
        List<NutsCommandAliasConfig> all = new ArrayList<>();
        try {
            Path storeLocation = getCommandsFolder(session);
            if (!Files.isDirectory(storeLocation)) {
                _LOGOP(session).level(Level.SEVERE).log("unable to locate commands. Invalid store locate {0}", storeLocation);
                return all;
            }
            Files.list(storeLocation).forEach(file -> {
                String fileName = file.getFileName().toString();
                if (file.getFileName().toString().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)) {
                    NutsCommandAliasConfig c = null;
                    try {
                        c = session.getWorkspace().elem().setContentType(NutsContentType.JSON).parse(file, NutsCommandAliasConfig.class);
                    } catch (Exception ex) {
                        _LOGOP(session).level(Level.FINE).error(ex).log("unable to parse {0}", file);
                        //
                    }
                    if (c != null) {
                        c.setName(fileName.substring(0, fileName.length() - NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION.length()));
                        if (filter == null || filter.test(c)) {
                            all.add(c);
                        }
                    }
                }
            });
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
        return all;
    }
}
