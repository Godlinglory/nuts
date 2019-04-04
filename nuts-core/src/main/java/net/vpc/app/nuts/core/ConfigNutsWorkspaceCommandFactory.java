package net.vpc.app.nuts.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import net.vpc.app.nuts.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class ConfigNutsWorkspaceCommandFactory implements NutsWorkspaceCommandFactory {

    private DefaultNutsWorkspaceConfigManager configManager;

    public ConfigNutsWorkspaceCommandFactory(DefaultNutsWorkspaceConfigManager defaultNutsWorkspaceConfigManager) {
        this.configManager = defaultNutsWorkspaceConfigManager;
    }

    @Override
    public void configure(NutsWorkspaceCommandFactoryConfig config) {

    }

    @Override
    public String getFactoryId() {
        return "default";
    }

    public Path getStoreLocation() {
        return configManager.getStoreLocation(configManager.getApiId(), NutsStoreLocation.PROGRAMS);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    public void uninstallCommand(String name) {
        Path file = getStoreLocation().resolve(name + NutsConstants.NUTS_COMMAND_FILE_EXTENSION);
        if (Files.exists(file)) {
            try {
                Files.delete(file);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    public void installCommand(NutsWorkspaceCommandConfig command) {
        Path file = getStoreLocation().resolve(command.getName() + NutsConstants.NUTS_COMMAND_FILE_EXTENSION);
        configManager.getWorkspace().io().writeJson(command, file, true);
    }

    @Override
    public NutsWorkspaceCommandConfig findCommand(String name, NutsWorkspace workspace) {
        Path file = getStoreLocation().resolve(name + NutsConstants.NUTS_COMMAND_FILE_EXTENSION);
        if (Files.exists(file)) {
            NutsWorkspaceCommandConfig c = configManager.getWorkspace().io().readJson(file, NutsWorkspaceCommandConfig.class);
            if (c != null) {
                c.setName(name);
                return c;
            }
        }
        return null;
    }

    @Override
    public List<NutsWorkspaceCommandConfig> findCommands(NutsWorkspace workspace) {
        return findCommands((NutsObjectFilter<NutsWorkspaceCommandConfig>) null);
    }

    public List<NutsWorkspaceCommandConfig> findCommands(NutsId id, NutsWorkspace workspace) {
        return findCommands(new NutsObjectFilter<NutsWorkspaceCommandConfig>() {
            @Override
            public boolean accept(NutsWorkspaceCommandConfig value) {
                if (id.getVersion().isEmpty()) {
                    return value.getOwner().getSimpleName().equals(id.getSimpleName());
                } else {
                    return value.getOwner().getLongName().equals(id.getLongName());
                }
            }
        });
    }

    public List<NutsWorkspaceCommandConfig> findCommands(NutsObjectFilter<NutsWorkspaceCommandConfig> filter) {
        List<NutsWorkspaceCommandConfig> all = new ArrayList<>();
        try {
            Files.list(getStoreLocation()).forEach(file -> {
                String fileName = file.getFileName().toString();
                if (file.getFileName().toString().endsWith(NutsConstants.NUTS_COMMAND_FILE_EXTENSION)) {
                    NutsWorkspaceCommandConfig c = null;
                    try {
                        c = configManager.getWorkspace().io().readJson(file, NutsWorkspaceCommandConfig.class);
                    } catch (Exception ex) {
                        //
                    }
                    if (c != null) {
                        c.setName(fileName.substring(0, fileName.length() - 4));
                        if (filter == null || filter.accept(c)) {
                            all.add(c);
                        }
                    }
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return all;
    }
}
