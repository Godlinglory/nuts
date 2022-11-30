package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPathPermission;
import net.thevpc.nuts.runtime.standalone.repository.NutsRepositoryRegistryHelper;
import net.thevpc.nuts.runtime.standalone.repository.NutsRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.repository.util.NutsRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsSimpleRepositoryWrapper;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.event.DefaultNutsWorkspaceEvent;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.ConfigEventType;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.util.NutsLoggerVerb;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DefaultNutsRepositoryModel {

    private final NutsRepositoryRegistryHelper repositoryRegistryHelper;
    private final NutsWorkspace workspace;
    public NutsLogger LOG;

    public DefaultNutsRepositoryModel(NutsWorkspace workspace) {
        this.workspace = workspace;
        repositoryRegistryHelper = new NutsRepositoryRegistryHelper(workspace);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsRepositoryModel.class, session);
        }
        return LOG;
    }

    public NutsRepository[] getRepositories(NutsSession session) {
        return repositoryRegistryHelper.getRepositories();
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public NutsRepository findRepositoryById(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepositoryById(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NutsRepository child : repositoryRegistryHelper.getRepositories()) {
                final NutsRepository m = child.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirrorById(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(session,
                                NutsMessage.ofCstyle("ambiguous repository name %s found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )
                        );
                    }
                }
            }
        }
        return y;
    }

    public NutsRepository findRepositoryByName(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepositoryByName(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NutsRepository child : repositoryRegistryHelper.getRepositories()) {
                final NutsRepository m = child.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirrorByName(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(session,
                                NutsMessage.ofCstyle("ambiguous repository name %s found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )
                        );
                    }
                }
            }
        }
        return y;
    }

    public NutsRepository findRepository(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepository(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NutsRepository child : repositoryRegistryHelper.getRepositories()) {
                final NutsRepository m = child.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirror(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(session,
                                NutsMessage.ofCstyle("ambiguous repository name %s found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )

                        );
                    }
                }
            }
        }
        return y;
    }

    public NutsRepository getRepository(String repositoryIdOrName, NutsSession session) throws NutsRepositoryNotFoundException {
        NutsSessionUtils.checkSession(getWorkspace(), session);
        if (DefaultNutsInstalledRepository.INSTALLED_REPO_UUID.equals(repositoryIdOrName)) {
            return NutsWorkspaceExt.of(getWorkspace()).getInstalledRepository();
        }
        NutsRepository r = findRepository(repositoryIdOrName, session);
        if (r != null) {
            return r;
        }
        throw new NutsRepositoryNotFoundException(session, repositoryIdOrName);
    }

    public void removeRepository(String repositoryId, NutsSession session) {
        session.security().setSession(session).checkAllowed(NutsConstants.Permissions.REMOVE_REPOSITORY, "remove-repository");
        final NutsRepository repository = repositoryRegistryHelper.removeRepository(repositoryId, session);
        if (repository != null) {
            session.config().save();
            NutsWorkspaceConfigManagerExt config = NutsWorkspaceConfigManagerExt.of(session.config());
            config.getModel().fireConfigurationChanged("config-main", session, ConfigEventType.MAIN);
            NutsWorkspaceUtils.of(session).events().fireOnRemoveRepository(new DefaultNutsWorkspaceEvent(session, repository, "repository", repository, null));
        }
    }

    public void removeAllRepositories(NutsSession session) {
        for (NutsRepository repository : repositoryRegistryHelper.getRepositories()) {
            removeRepository(repository.getUuid(), session);
        }
    }

    protected void addRepository(NutsRepository repo, NutsSession session, boolean temp, boolean enabled) {
        repositoryRegistryHelper.addRepository(repo, session);
        repo.setEnabled(enabled, session);
        session.config().save();
        if (!temp) {
            NutsWorkspaceConfigManagerExt config = NutsWorkspaceConfigManagerExt.of(session.config());
            config.getModel().fireConfigurationChanged("config-main", session, ConfigEventType.MAIN);
            if (repo != null) {
                // repo would be null if the repo is not accessible
                // like for system repo, if not already created
                NutsWorkspaceUtils.of(session).events().fireOnAddRepository(
                        new DefaultNutsWorkspaceEvent(session, repo, "repository", null, repo)
                );
            }
        }
    }

    public NutsRepository addRepository(NutsAddRepositoryOptions options, NutsSession session) {
        //TODO excludedRepositoriesSet
//        if (excludedRepositoriesSet != null && excludedRepositoriesSet.contains(options.getName())) {
//            return null;
//        }
        NutsRepository r = this.createRepository(options, null, session);
        addRepository(r, session, options.isTemporary(),options.isEnabled());
        return r;
    }

    public NutsRepository createRepository(NutsAddRepositoryOptions options, NutsRepository parentRepository, NutsSession session) {
        return createRepository(options, null, parentRepository, session);
    }

    public NutsRepository createRepository(NutsAddRepositoryOptions options, Path rootFolder, NutsRepository parentRepository, NutsSession session) {
        NutsRepositoryModel repoModel = options.getRepositoryModel();
        if (rootFolder == null) {
            if (parentRepository == null) {
                NutsWorkspaceConfigManagerExt cc = NutsWorkspaceConfigManagerExt.of(session.config());
                rootFolder = options.isTemporary() ?
                        cc.getModel().getTempRepositoriesRoot(session).toFile()
                        : cc.getModel().getRepositoriesRoot(session).toFile();
            } else {
                NutsRepositoryConfigManagerExt cc = NutsRepositoryConfigManagerExt.of(parentRepository.config());
                rootFolder = (options.isTemporary() ? cc.getModel().getTempMirrorsRoot(session)
                        : cc.getModel().getMirrorsRoot(session)).toFile();
            }
        }
        if (repoModel != null) {
            NutsRepositoryConfig config = new NutsRepositoryConfig();
            String name = repoModel.getName();
            String uuid = repoModel.getUuid();
            if (NutsBlankable.isBlank(name)) {
                name = "custom";
            }
            if (NutsBlankable.isBlank(uuid)) {
                uuid = UUID.randomUUID().toString();
            }
            config.setName(name);
            config.setLocation(NutsRepositoryLocation.of("custom@"));
            config.setUuid(uuid);
            config.setStoreLocationStrategy(repoModel.getStoreLocationStrategy());
            NutsAddRepositoryOptions options2 = new NutsAddRepositoryOptions();
            options2.setName(config.getName());
            options2.setConfig(config);
            options2.setDeployWeight(options.getDeployWeight());
            options2.setTemporary(true);
            options2.setEnabled(options.isEnabled());
            options2.setLocation(CoreIOUtils.resolveRepositoryPath(options2, rootFolder, session));
            return new NutsSimpleRepositoryWrapper(options2, session, null, repoModel);
        }

        options = options.copy();
        try {
            boolean temporary = options.isTemporary();
            NutsRepositoryConfig conf = options.getConfig();
            if (temporary) {
//                options.setLocation(options.getName());
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, session));
                options.setEnabled(true);
            } else if (conf == null) {
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, session));
                conf = loadRepository(NutsPath.of(options.getLocation(),session).resolve(NutsConstants.Files.REPOSITORY_CONFIG_FILE_NAME), options.getName(), session);
                if (conf == null) {
                    if (options.isFailSafe()) {
                        return null;
                    }
                    throw new NutsInvalidRepositoryException(session, options.getLocation(),
                            NutsMessage.ofCstyle("invalid repository location ", options.getLocation())
                    );
                }
                options.setConfig(conf);
                if (options.isEnabled()) {
                    options.setEnabled(
                            session.boot().getBootOptions().getRepositories() == null
                                    || NutsRepositorySelectorList.ofAll(
                                            session.boot().getBootOptions().getRepositories().orNull(),
                                    NutsRepositoryDB.of(session),session
                            ).acceptExisting(
                                    conf.getLocation().setName(options.getName())
                            ));
                }
            } else {
                options.setConfig(conf);
                if (options.isEnabled()) {
                    options.setEnabled(
                            session.boot().getBootOptions().getRepositories() == null
                                    || NutsRepositorySelectorList.ofAll(
                                            session.boot().getBootOptions().getRepositories().orNull(),
                                    NutsRepositoryDB.of(session),session
                            ).acceptExisting(
                                    conf.getLocation().setName(options.getName())
                            ));
                }
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, session));
            }
            if (NutsBlankable.isBlank(conf.getName())) {
                conf.setName(options.getName());
            }
            if (NutsBlankable.isBlank(conf.getLocation())
                    && !NutsBlankable.isBlank(options.getLocation())
                    && NutsPath.of(options.getLocation(), session).isFile()
            ) {
                conf.setLocation(NutsRepositoryLocation.of(options.getLocation()));
            }

            NutsRepositoryFactoryComponent factory_ = session.extensions()
                    .setSession(session)
                    .createSupported(NutsRepositoryFactoryComponent.class, false, conf);
            if (factory_ != null) {
                NutsRepository r = factory_.create(options, session, parentRepository);
                if (r != null) {
                    return r;
                }
            }
            String repoType = NutsRepositoryUtils.getRepoType(conf);
            if (options.isTemporary()) {
                if (NutsBlankable.isBlank(repoType)) {
                    throw new NutsInvalidRepositoryException(session, options.getName(), NutsMessage.ofPlain("unable to detect valid type for temporary repository"));
                } else {
                    throw new NutsInvalidRepositoryException(session, options.getName(), NutsMessage.ofCstyle("invalid repository type %s", repoType));
                }
            } else {
                if (NutsBlankable.isBlank(repoType)) {
                    throw new NutsInvalidRepositoryException(session, options.getName(), NutsMessage.ofCstyle("unable to detect valid type for repository %s",options.getName()));
                } else {
                    throw new NutsInvalidRepositoryException(session, options.getName(), NutsMessage.ofCstyle("invalid repository type %s", repoType));
                }
            }
        } catch (RuntimeException ex) {
            if (options.isFailSafe()) {
                return null;
            }
            throw ex;
        }
    }

    public NutsRepository addRepository(String repositoryNamedUrl, NutsSession session) {
        NutsSessionUtils.checkSession(getWorkspace(), session);
        NutsRepositoryLocation r = null;
        try {
            r = NutsRepositoryLocation.of(repositoryNamedUrl,NutsRepositoryDB.of(session),session);
        } catch (Exception ex) {
            throw new NutsInvalidRepositoryException(session, repositoryNamedUrl, NutsMessage.ofPlain("invalid repository definition"));
        }
        NutsAddRepositoryOptions options = NutsRepositorySelectorHelper.createRepositoryOptions(r, true, session);
        return addRepository(options, session);
    }

    public NutsRepositoryConfig loadRepository(NutsPath file, String name, NutsSession session) {
        NutsRepositoryConfig conf = null;
        if (file.isRegularFile() && file.getPermissions().contains(NutsPathPermission.CAN_READ)) {
            byte[] bytes= file.readBytes();
            try {
                NutsElements elem = NutsElements.of(session);
                Map<String, Object> a_config0 = elem.json().parse(bytes, Map.class);
                NutsVersion version = NutsVersion.of((String) a_config0.get("configVersion")).orNull();
                if (version == null || version.isBlank()) {
                    version = session.getWorkspace().getApiVersion();
                }
                int buildNumber = CoreNutsUtils.getApiVersionOrdinalNumber(version);
                if (buildNumber < 506) {

                }
                conf = elem.json().parse(file, NutsRepositoryConfig.class);
            } catch (RuntimeException ex) {
                if (session.boot().getBootOptions().getRecover().orElse(false)) {
                    onLoadRepositoryError(file, name, null, ex, session);
                } else {
                    throw ex;
                }
            }
        }
        return conf;
    }

    public NutsRepositorySPI toRepositorySPI(NutsRepository repo) {
        return (NutsRepositorySPI) repo;
    }

    private void onLoadRepositoryError(NutsPath file, String name, String uuid, Throwable ex, NutsSession session) {
        NutsWorkspaceConfigManager wconfig = session.config().setSession(session);
        NutsBootManager wboot = session.boot().setSession(session);
        NutsWorkspaceEnvManager wenv = session.env().setSession(session);
        if (wconfig.isReadOnly()) {
            throw new NutsIOException(session, NutsMessage.ofCstyle("error loading repository %s", file), ex);
        }
        String fileName = "nuts-repository" + (name == null ? "" : ("-") + name) + (uuid == null ? "" : ("-") + uuid) + "-" + Instant.now().toString();
        LOG.with().session(session).level(Level.SEVERE).verb(NutsLoggerVerb.FAIL).log(
                NutsMessage.ofJstyle("erroneous repository config file. Unable to load file {0} : {1}", file, ex));
        NutsPath logError = session.locations().getStoreLocation(getWorkspace().getApiId(), NutsStoreLocation.LOG)
                .resolve("invalid-config");
        try {
            logError.mkParentDirs();
        } catch (Exception ex1) {
            throw new NutsIOException(session, NutsMessage.ofCstyle("unable to log repository error while loading config file %s : %s", file, ex1), ex);
        }
        NutsPath newfile = logError.resolve(fileName + ".json");
        LOG.with().session(session).level(Level.SEVERE).verb(NutsLoggerVerb.FAIL)
                .log(NutsMessage.ofJstyle("erroneous repository config file will be replaced by a fresh one. Old config is copied to {0}", newfile));
        try {
            Files.move(file.toFile(), newfile.toFile());
        } catch (IOException e) {
            throw new NutsIOException(session, NutsMessage.ofCstyle("nable to load and re-create repository config file %s : %s", file, e), ex);
        }

        try (PrintStream o = new PrintStream(logError.resolve(fileName + ".error").getOutputStream())) {
            o.printf("workspace.path:%s%n", session.locations().getWorkspaceLocation());
            o.printf("repository.path:%s%n", file);
            o.printf("workspace.options:%s%n", wboot.getBootOptions().toCommandLine(new NutsWorkspaceOptionsConfig().setCompact(false)));
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                o.printf("location." + location.id() + ":%s%n", session.locations().getStoreLocation(location));
            }
            o.printf("java.class.path:%s%n", System.getProperty("java.class.path"));
            o.println();
            ex.printStackTrace(o);
        } catch (Exception ex2) {
            //ignore
        }
    }

}
