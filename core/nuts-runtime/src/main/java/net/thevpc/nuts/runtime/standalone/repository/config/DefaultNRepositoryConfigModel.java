package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryHelper;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryRegistryHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.NSpeedQualifiers;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NStoreLocationsMap;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

public class DefaultNRepositoryConfigModel implements NRepositoryConfigModel {

    private NLogger LOG;

    private final NRepository repository;
    private final NSpeedQualifier speed;
    private final NPath storeLocation;
    private NRepositoryConfig config;
    private final Map<String, NUserConfig> configUsers = new LinkedHashMap<>();
    private boolean configurationChanged = false;
    private int deployWeight;
    private boolean temporary;
    private String globalName;
    private boolean supportedMirroring;
    private final NRepositoryRegistryHelper repositoryRegistryHelper;
    private String repositoryName;
    private String repositoryType;
    private NRepositoryRef repositoryRef;

    public DefaultNRepositoryConfigModel(NRepository repository, NAddRepositoryOptions options, NSession session,
                                         NSpeedQualifier speed,
                                         boolean supportedMirroring, String repositoryType) {
        NUtils.requireNonNull(options, "repository options", session);
        NUtils.requireNonNull(options.getConfig(), "repository options config", session);
        this.repositoryRef = NRepositoryUtils.optionsToRef(options);
        String storeLocation = options.getLocation();
        NRepositoryConfig config = options.getConfig();
        String globalName = options.getConfig().getName();
        String repositoryName = options.getName();

        speed = speed == null ? NSpeedQualifier.NORMAL : speed;

        NUtils.requireNonBlank(repositoryType, "repository type", session);
        NUtils.requireNonBlank(repositoryName, "repository name", session);
        NUtils.requireNonBlank(globalName, "repository global name", session);
        NUtils.requireNonBlank(storeLocation, "repository store location", session);
        Path pfolder = Paths.get(storeLocation);
        if ((Files.exists(pfolder) && !Files.isDirectory(pfolder))) {
            throw new NInvalidRepositoryException(session, storeLocation, NMsg.ofCstyle("unable to resolve root as a valid folder %s", storeLocation));
        }

        this.repositoryRegistryHelper = new NRepositoryRegistryHelper(repository.getWorkspace());
        this.repository = repository;
        this.repositoryName = repositoryName;
        this.globalName = globalName;
        this.storeLocation = NPath.of(storeLocation, session);
        this.speed = speed;
        this.deployWeight = options.getDeployWeight();
        this.temporary = options.isTemporary();
        this.supportedMirroring = supportedMirroring;
        this.repositoryType = repositoryType;
        setConfig(config, session, false);
    }

    protected NLoggerOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLogger _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLogger.of(DefaultNRepositoryConfigModel.class, session);
        }
        return LOG;
    }

    public NRepository getRepository() {
        return repository;
    }

    public NWorkspace getWorkspace() {
        return repository.getWorkspace();
    }

    public NRepositoryRef getRepositoryRef(NSession session) {
        return new NRepositoryRef(repositoryRef);
    }

    public String getName() {
        return repositoryName;
    }

    public int getDeployWeight(NSession session) {
        return deployWeight;
    }

//    public String getEnv(String key, String defaultValue, boolean inherit,NutsSession session) {
//        String t = null;
//        if (config.getEnv() != null) {
//            t = config.getEnv().get(defaultValue);
//        }
//        if (!NutsBlankable.isBlank(t)) {
//            return t;
//        }
//        if (inherit) {
//            t = repository.getWorkspace().env().getEnv(key, null);
//            if (!NutsBlankable.isBlank(t)) {
//                return t;
//            }
//        }
//        return defaultValue;
//    }

//    public Map<String, String> getEnv(boolean inherit,NutsSession session) {
//        Map<String, String> p = new LinkedHashMap<>();
//        if (inherit) {
//            p.putAll(repository.getWorkspace().env().getEnvMap());
//        }
//        if (config.getEnv() != null) {
//            p.putAll(config.getEnv());
//        }
//        return p;
//    }

    public void setEnv(String property, String value, NSession session) {
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        if (NBlankable.isBlank(value)) {
            if (config.getEnv() != null) {
                config.getEnv().remove(property);
                fireConfigurationChanged("env", session);
            }
        } else {
            if (config.getEnv() == null) {
                config.setEnv(new LinkedHashMap<>());
            }
            if (!value.equals(config.getEnv().get(property))) {
                config.getEnv().put(property, value);
                fireConfigurationChanged("env", session);
            }
        }
    }

    @Override
    public NSpeedQualifier getSpeed(NSession session) {
        List<NSpeedQualifier> all = new ArrayList<>();
        boolean unavailable = false;
        if (speed == NSpeedQualifier.UNAVAILABLE) {
            unavailable = true;
        } else {
            all.add(speed);
        }
        if (isSupportedMirroring(session)) {
            for (NRepository mirror : getMirrors(session)) {
                NSpeedQualifier mspeed = mirror.config().setSession(session).getSpeed();
                if (mspeed == NSpeedQualifier.UNAVAILABLE) {
                    unavailable = true;
                } else {
                    all.add(mspeed);
                }
            }
        }
        if (all.isEmpty()) {
            if (unavailable) {
                return NSpeedQualifier.UNAVAILABLE;
            }
            return NSpeedQualifier.NORMAL;
        }
        return NSpeedQualifiers.max(all.toArray(new NSpeedQualifier[0]));
    }

    @Override
    public String getType(NSession session) {
        return repositoryType;
    }

    @Override
    public String getGroups(NSession session) {
        return config.getGroups();
    }

    @Override
    public NRepositoryLocation getLocation(NSession session) {
        NRepositoryLocation loc = config.getLocation();
        if (loc == null) {
            loc = NRepositoryLocation.of(null);
        }
        String name = config.getName();
        return loc.setName(name);
    }

    @Override
    public NPath getLocationPath(NSession session) {
        String s = NStringUtils.trimToNull(config.getLocation().getPath());
        if (s != null) {
            return NPath.of(s, session).toAbsolute(session.locations().getWorkspaceLocation());
        }
        return null;
    }

    @Override
    public NPath getStoreLocation() {
        return storeLocation;
    }

    @Override
    public NStoreLocationStrategy getStoreLocationStrategy(NSession session) {
        NStoreLocationStrategy strategy = config.getStoreLocationStrategy();
        if (strategy == null) {
            strategy = NStoreLocationStrategy.values()[0];
        }
        return strategy;
    }

    @Override
    public NPath getStoreLocation(NStoreLocation folderType, NSession session) {
        NStoreLocationsMap hlm = new NStoreLocationsMap(config.getStoreLocations());

//        String n = CoreNutsUtils.getArrItem(config.getStoreLocations(), folderType.ordinal());
        String n = hlm.get(folderType);
        if (temporary) {
            if (NBlankable.isBlank(n)) {
                n = folderType.toString().toLowerCase();
                n = n.trim();
            }
            return getStoreLocation().resolve(n);
        } else {
            switch (getStoreLocationStrategy(session)) {
                case STANDALONE: {
                    if (NBlankable.isBlank(n)) {
                        n = folderType.toString().toLowerCase();
                    }
                    n = n.trim();
                    return getStoreLocation().resolve(n);
                }
                case EXPLODED: {
                    NPath storeLocation = session.locations().getStoreLocation(folderType);
                    //uuid is added as
                    return storeLocation.resolve(NConstants.Folders.REPOSITORIES).resolve(getName()).resolve(getUuid());

                }
                default: {
                    throw new NIllegalArgumentException(session, NMsg.ofCstyle("unsupported strategy type %s", getStoreLocation()));
                }
            }
        }
    }

    public String getUuid() {
        return config.getUuid();
    }

    public NRepositoryLocation getLocation() {
        return config.getLocation();
    }

    public void setConfig(NRepositoryConfig newConfig, NSession session, boolean fireChange) {
        NUtils.requireNonBlank(newConfig, "repository config", session);
        this.config = newConfig;
        if (this.config.getUuid() == null) {
            fireChange = true;
            this.config.setUuid(UUID.randomUUID().toString());
        }
        if (this.config.getStoreLocationStrategy() == null) {
            fireChange = true;
            this.config.setStoreLocationStrategy(session.locations().getRepositoryStoreLocationStrategy());
        }
        if (!Objects.equals(NRepositoryUtils.getRepoType(config), repositoryType)) {
            throw new NIllegalArgumentException(session,
                    NMsg.ofCstyle("invalid Repository Type : expected %s, found %s", repositoryType, NRepositoryUtils.getRepoType(config))
            );
        }

        this.globalName = newConfig.getName();
        configUsers.clear();
        if (config.getUsers() != null) {
            for (NUserConfig user : config.getUsers()) {
                configUsers.put(user.getUser(), user);
            }
        }
        removeAllMirrors(session);
        if (config.getMirrors() != null) {
            for (NRepositoryRef ref : config.getMirrors()) {
                NRepository r = ((DefaultNRepositoryManager) session.repos())
                        .getModel()
                        .createRepository(
                                NRepositoryUtils.refToOptions(ref),
                                repository, session
                        );
                addMirror(r, session);
            }
        }
        if (fireChange) {
            fireConfigurationChanged("*", session);
        }
    }

    @Override
    public void addMirror(NRepository repo, NSession session) {
        repositoryRegistryHelper.addRepository(repo, session);
        NRepositoryHelper.of(repository).events().fireOnAddRepository(
                new DefaultNRepositoryEvent(session, repository, repo, "mirror", null, repo)
        );
    }

    @Override
    public void setIndexEnabled(boolean enabled, NSession session) {
        if (enabled != config.isIndexEnabled()) {
            config.setIndexEnabled(enabled);
            fireConfigurationChanged("index-enabled", session);
        }
    }

    @Override
    public boolean isIndexEnabled(NSession session) {
        return config.isIndexEnabled();
    }

    @Override
    public void setUser(NUserConfig user, NSession session) {
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        configUsers.put(user.getUser(), user);
        fireConfigurationChanged("user", session);
//        return this;
    }

    @Override
    public void removeUser(String userId, NSession session) {
        if (configUsers.containsKey(userId)) {
//            session = CoreNutsUtils.validate(session, repository.getWorkspace());
            configUsers.remove(userId);
            fireConfigurationChanged("user", session);
        }
//        return this;
    }

    @Override
    public NUserConfig getUser(String userId, NSession session) {
        NUserConfig u = configUsers.get(userId);
        if (u == null) {
            if (NConstants.Users.ADMIN.equals(userId) || NConstants.Users.ANONYMOUS.equals(userId)) {
                u = new NUserConfig(userId, null, null, null);
                configUsers.put(userId, u);
                fireConfigurationChanged("user", session);
            }
        }
        return u;
    }

    @Override
    public NUserConfig[] getUsers(NSession session) {
        return configUsers.values().toArray(new NUserConfig[0]);
    }

    //
//    public NutsRepositoryConfigManager removeMirrorRef(String repositoryId) {
//        if (configMirrorRefs.remove(repositoryId) != null) {
//            fireConfigurationChanged();
//        }
//        return this;
//    }
//    
//    public NutsRepositoryConfigManager addMirrorRef(NutsRepositoryRef c) {
//        repositoryRegistryHelper.addRepositoryRef(c);
//        if (LOG.isLoggable(Level.FINEST)) {
//            LOG.log(Level.FINEST, CoreStringUtils.alignLeft(getName(), 20) + " add repo " + c.getName());
//        }
//        fireConfigurationChanged();
//        return this;
//    }
//    
//    public NutsRepositoryRef getMirrorRef(String name) {
//        return configMirrorRefs.get(name);
//    }
    public void setMirrorEnabled(String repoName, boolean enabled, NSession session) {
        NRepositoryRef e = repositoryRegistryHelper.findRepositoryRef(repoName);
        if (e != null && e.isEnabled() != enabled) {
//            session = CoreNutsUtils.validate(session, repository.getWorkspace());
            e.setEnabled(enabled);
            fireConfigurationChanged("mirror", session);
        }
    }

    ////
//    public NutsRepositoryRef[] getMirrorRefs() {
//        return configMirrorRefs.values().toArray(new NutsRepositoryRef[0]);
//    }
    @Override
    public boolean save(boolean force, NSession session) {
        NSessionUtils.checkSession(repository.getWorkspace(), session);
        boolean ok = false;
        if (force || (!session.config().isReadOnly() && isConfigurationChanged())) {
            NWorkspaceUtils.of(session).checkReadOnly();
            repository.security().setSession(session).checkAllowed(NConstants.Permissions.SAVE, "save");
            NPath file = getStoreLocation().resolve(NConstants.Files.REPOSITORY_CONFIG_FILE_NAME);
            boolean created = false;
            if (!file.exists()) {
                created = true;
            }
            getStoreLocation().mkdirs();
            config.setConfigVersion(DefaultNWorkspace.VERSION_REPOSITORY_CONFIG);
            if (config.getEnv() != null && config.getEnv().isEmpty()) {
                config.setEnv(null);
            }
            config.setMirrors(Arrays.asList(repositoryRegistryHelper.getRepositoryRefs()));
            config.setUsers(configUsers.isEmpty() ? null : new ArrayList<>(configUsers.values()));
//            if (NutsBlankable.isBlank(config.getConfigVersion())) {
//                config.setConfigVersion(repository.getWorkspace().getApiVersion());
//            }
            NElements.of(session).setSession(session).json().setValue(config)
                    .setNtf(false)
                    .print(file);
            configurationChanged = false;
            if (_LOG(session).isLoggable(Level.CONFIG)) {
                if (created) {
                    _LOGOP(session).level(Level.CONFIG).verb(NLoggerVerb.SUCCESS)
                            .log(NMsg.ofJstyle(
                                    "{0} created repository {1} at {2}",
                                    NStringUtils.formatAlign(repository.getName(), 20, NPositionType.FIRST), repository.getName(),
                                    getStoreLocation()
                            ));
                } else {
                    _LOGOP(session).level(Level.CONFIG).verb(NLoggerVerb.SUCCESS).log(NMsg.ofJstyle(
                            "{0} updated repository {1} at {2}",
                            NStringUtils.formatAlign(repository.getName(), 20, NPositionType.FIRST), repository.getName(),
                            getStoreLocation()
                    ));
                }
            }
            ok = true;
        }
        NException error = null;
        for (NRepository repo : getMirrors(session)) {
            try {
                NRepositoryConfigManager config = repo.config();
                if (config instanceof NRepositoryConfigManagerExt) {
                    ok |= ((NRepositoryConfigManagerExt) config)
                            .getModel()
                            .save(force, session);
                }
            } catch (NException ex) {
                error = ex;
            }
        }
        if (error != null) {
            throw error;
        }

        return ok;
    }

    public void save(NSession session) {
        save(true, session);
    }

    @Override
    public void fireConfigurationChanged(String configName, NSession session) {
        this.configurationChanged = true;
        DefaultNRepositoryEvent evt = new DefaultNRepositoryEvent(session, null, repository, "config." + configName, null, true);
        for (NRepositoryListener workspaceListener : repository.getRepositoryListeners()) {
            workspaceListener.onConfigurationChanged(evt);
        }
    }

    public boolean isConfigurationChanged() {
        return configurationChanged;
    }

    @Override
    public void setEnabled(boolean enabled, NSession session) {
        repositoryRef.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled(NSession session) {
        return repositoryRef.isEnabled();
    }

    @Override
    public boolean isTemporary(NSession session) {
        return temporary;
    }

    @Override
    public void setTemporary(boolean transientRepository, NSession session) {
        this.temporary = transientRepository;
    }

    @Override
    public boolean isIndexSubscribed(NSession session) {
        NIndexStore s = getIndexStore();
        return s != null && s.isSubscribed(session);
    }

    private NIndexStore getIndexStore() {
        return NRepositoryExt.of(repository).getIndexStore();
    }

    @Override
    public void subscribeIndex(NSession session) {
        NIndexStore s = getIndexStore();
        if (s != null) {
            s.subscribe(session);
        }
    }

    @Override
    public void unsubscribeIndex(NSession session) {
        NIndexStore s = getIndexStore();
        if (s != null) {
            s.unsubscribe(session);
        }
    }

    @Override
    public String getGlobalName(NSession session) {
        return globalName;
    }

    @Override
    public boolean isSupportedMirroring(NSession session) {
        return supportedMirroring;
    }

    @Override
    public void removeMirror(String repositoryId, NSession session) {
        if (!isSupportedMirroring(session)) {
            throw new NUnsupportedOperationException(session, NMsg.ofCstyle("unsupported operation '%s'", "removeMirror"));
        }
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        repository.security().setSession(session).checkAllowed(NConstants.Permissions.REMOVE_REPOSITORY, "remove-repository");
        final NRepository r = repositoryRegistryHelper.removeRepository(repositoryId, session);
        if (r != null) {
            NRepositoryHelper.of(repository).events().fireOnRemoveRepository(new DefaultNRepositoryEvent(session, repository, r, "mirror", r, null));
        } else {
            throw new NRepositoryNotFoundException(session, repositoryId);
        }
//        return this;
    }

    //
//    public NutsRepository getMirror(String repositoryIdOrName) {
//        return getMirror(repositoryIdOrName, false);
//    }
    public NRepository getMirror(String repositoryIdPath, NSession session) {
        NRepository r = findMirror(repositoryIdPath, session);
        if (r != null) {
            return r;
        }
        throw new NRepositoryNotFoundException(session, repositoryIdPath);
    }

    public NRepository findMirror(String repositoryNameOrId, NSession session) {
        NRepository y = repositoryRegistryHelper.findRepository(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive() && isSupportedMirroring(session)) {
            for (NRepository mirror : getMirrors(session)) {
                NRepository m = mirror.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirror(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NIllegalArgumentException(session,
                                NMsg.ofCstyle("ambiguous repository name %s ; found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )
                        );
                    }
                }

            }
        }
        return y;
    }

    public NRepository findMirrorById(String repositoryNameOrId, NSession session) {
        NRepository y = repositoryRegistryHelper.findRepositoryById(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive() && isSupportedMirroring(session)) {
            for (NRepository mirror : getMirrors(session)) {
                NRepository m = mirror.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirrorById(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NIllegalArgumentException(session,
                                NMsg.ofCstyle("ambiguous repository name %s ; found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )
                        );
                    }
                }

            }
        }
        return y;
    }

    public NRepository findMirrorByName(String repositoryNameOrId, NSession session) {
        NRepository y = repositoryRegistryHelper.findRepositoryByName(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive() && isSupportedMirroring(session)) {
            for (NRepository mirror : getMirrors(session)) {
                NRepository m = mirror.config()
                        .setSession(session.copy().setTransitive(true))
                        .findMirrorByName(repositoryNameOrId);
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NIllegalArgumentException(session,
                                NMsg.ofCstyle("ambiguous repository name %s ; found two Ids %s and %s",
                                        repositoryNameOrId, y.getUuid(), m.getUuid()
                                )
                        );
                    }
                }

            }
        }
        return y;
    }

    public List<NRepository> getMirrors(NSession session) {
        return Arrays.asList(repositoryRegistryHelper.getRepositories());
    }

    public NRepository addMirror(NAddRepositoryOptions options, NSession session) {
        if (!isSupportedMirroring(session)) {
            throw new NUnsupportedOperationException(session, NMsg.ofCstyle("unsupported operation '%s'", "addMirror"));
        }
        if (options.isTemporary()) {
            return null;
        }
        NRepository repo = ((DefaultNRepositoryManager) session.repos())
                .getModel()
                .createRepository(
                        options,
                        repository, session
                );
        addMirror(repo, session);
        return repo;
    }

    @Override
    public NPath getTempMirrorsRoot(NSession session) {
        return getStoreLocation().resolve(NConstants.Folders.REPOSITORIES);
    }

    @Override
    public NPath getMirrorsRoot(NSession session) {
        return getStoreLocation().resolve(NConstants.Folders.REPOSITORIES);
    }

    public NRepositoryConfig getStoredConfig(NSession session) {
        return config;
    }

    //
    public void removeAllMirrors(NSession options) {
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        for (NRepository repo : repositoryRegistryHelper.getRepositories()) {
            removeMirror(repo.getUuid(), options);
        }
    }

    public NRepositoryConfig getConfig(NSession session) {
        return config;
    }

    public Map<String, String> toMap(boolean inherit, NSession session) {
        return config_getEnv(inherit, session);
    }

    public Map<String, String> toMap(NSession session) {
        return config_getEnv(true, session);
    }


    @Override
    public NOptional<NValue> get(String key, boolean inherit, NSession session) {
        NOptional<NValue> o = config_getEnv(key, inherit, session);
        if (o.isBlank() && inherit) {
            return o.orElseUse(() -> session.config().getConfigProperty(key));
        }
        return o;
    }


    public void set(String property, String value, NSession session) {
        config_setEnv(property, value, session);
    }

    private NRepositoryConfigModel getConfig0() {
        return ((DefaultNRepoConfigManager) repository.config()).getModel();
    }


    ////////////////////////////////////////////


    public NOptional<NValue> config_getEnv(String key, boolean inherit, NSession session) {
        NRepositoryConfigModel model = ((DefaultNRepoConfigManager) repository.config()).getModel();
        NRepositoryConfig config = model.getConfig(session);
        String t = null;
        if (config.getEnv() != null) {
            t = config.getEnv().get(key);
        }
        if (!NBlankable.isBlank(t)) {
            return NOptional.of(NValue.of(t));
        }
        if (inherit) {
            return session.config().getConfigProperty(key);
        }
        return NOptional.ofEmpty(s -> NMsg.ofCstyle("repository property not found : %s", key));
    }

    private Map<String, String> config_getEnv(boolean inherit, NSession session) {
        NRepositoryConfigModel model = ((DefaultNRepoConfigManager) repository.config()).getModel();
        NRepositoryConfig config = model.getConfig(session);
        Map<String, String> p = new LinkedHashMap<>();
        if (inherit) {
            p.putAll(session.config().getConfigMap());
        }
        if (config.getEnv() != null) {
            p.putAll(config.getEnv());
        }
        return p;
    }


    private void config_setEnv(String property, String value, NSession session) {
        NRepositoryConfig config = getConfig(session);
//        options = CoreNutsUtils.validate(options, repository.getWorkspace());
        if (NBlankable.isBlank(value)) {
            if (config.getEnv() != null) {
                config.getEnv().remove(property);
                fireConfigurationChanged("env", session);
            }
        } else {
            if (config.getEnv() == null) {
                config.setEnv(new LinkedHashMap<>());
            }
            if (!value.equals(config.getEnv().get(property))) {
                config.getEnv().put(property, value);
                fireConfigurationChanged("env", session);
            }
        }
    }
}
