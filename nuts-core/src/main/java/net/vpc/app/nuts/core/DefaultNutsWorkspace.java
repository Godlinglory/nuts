/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.MapStringMapper;
import net.vpc.app.nuts.core.util.common.ObservableMap;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.terminals.DefaultNutsSystemTerminalBase;
import net.vpc.app.nuts.core.terminals.DefaultSystemTerminal;
import net.vpc.app.nuts.core.terminals.UnmodifiableTerminal;
import net.vpc.app.nuts.core.util.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsFindCommand;
import net.vpc.app.nuts.core.util.mvn.PomId;
import net.vpc.app.nuts.core.util.mvn.PomIdResolver;

/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNutsWorkspace implements NutsWorkspace, NutsWorkspaceImpl, NutsWorkspaceExt {

    public static final Logger LOG = Logger.getLogger(DefaultNutsWorkspace.class.getName());
    public static final NutsInstallInfo NOT_INSTALLED = new DefaultNutsInstallInfo(false, false, null);
    private final List<NutsWorkspaceListener> workspaceListeners = new ArrayList<>();
    private boolean initializing;
    protected final DefaultNutsWorkspaceSecurityManager securityManager = new DefaultNutsWorkspaceSecurityManager(this);
    protected NutsWorkspaceConfigManagerExt configManager;
    protected DefaultNutsWorkspaceExtensionManager extensionManager;
    private final ObservableMap<String, Object> userProperties = new ObservableMap<>();

    private NutsSessionTerminal terminal;
    private NutsSystemTerminal systemTerminal;
    private NutsIOManager ioManager;
    private NutsParseManager parseManager;
    private NutsFormatManager formatManager;
    private DefaultNutsInstalledRepository installedRepository;
    private final List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();

    public DefaultNutsWorkspace() {

    }

    @Override
    public String getUuid() {
        return config().getUuid();
    }

    @Override
    public String uuid() {
        return getUuid();
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    @Override
    public NutsSession createSession() {
        NutsSession nutsSession = new DefaultNutsSession();
        nutsSession.setTerminal(getTerminal());
        return nutsSession;
    }

    @Override
    public NutsDescriptorBuilder createDescriptorBuilder() {
        return new DefaultNutsDescriptorBuilder();
    }

    public DefaultNutsWorkspaceConfigManager config0() {
        return (DefaultNutsWorkspaceConfigManager) configManager;
    }

    @Override
    public NutsWorkspaceConfigManager config() {
        return configManager;
    }

    @Override
    public NutsWorkspaceSecurityManager security() {
        return securityManager;
    }

    public boolean isInitializing() {
        return initializing;
    }

    @Override
    public void removeWorkspaceListener(NutsWorkspaceListener listener) {
        workspaceListeners.add(listener);
    }

    @Override
    public void addWorkspaceListener(NutsWorkspaceListener listener) {
        if (listener != null) {
            workspaceListeners.add(listener);
        }
    }

    @Override
    public NutsWorkspaceListener[] getWorkspaceListeners() {
        return workspaceListeners.toArray(new NutsWorkspaceListener[0]);
    }

    @Override
    public NutsWorkspace openWorkspace(NutsWorkspaceOptions options) {
        if (options == null) {
            options = new NutsWorkspaceOptions();
        } else {
            options = options.copy();
        }

        NutsWorkspaceFactory newFactory = extensions().createSupported(NutsWorkspaceFactory.class, this);
        if (newFactory == null) {
            throw new NutsExtensionMissingException(NutsWorkspaceFactory.class, "WorkspaceFactory");
        }
        NutsWorkspace nutsWorkspace = extensions().createSupported(NutsWorkspace.class, this);
        if (nutsWorkspace == null) {
            throw new NutsExtensionMissingException(NutsWorkspace.class, "Workspace");
        }
        NutsWorkspaceImpl nutsWorkspaceImpl = (NutsWorkspaceImpl) nutsWorkspace;
        if (nutsWorkspaceImpl.initializeWorkspace(newFactory,
                new NutsBootConfig(config().getContext(NutsBootContextType.RUNTIME)),
                new NutsBootConfig(config().getContext(NutsBootContextType.BOOT)),
                config().getBootClassWorldURLs(),
                configManager.getBootClassLoader(), options)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "workspace created");
            }
        }
        return nutsWorkspace;
    }

    @Override
    public NutsWorkspace setSystemTerminal(NutsSystemTerminalBase term) {
        if (term == null) {
            throw new NutsExtensionMissingException(NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        NutsSystemTerminal syst;
        if ((term instanceof NutsSystemTerminal)) {
            syst = (NutsSystemTerminal) term;
        } else {
            try {
                syst = new DefaultSystemTerminal(term);
                syst.install(this);
            } catch (Exception ex) {
                syst = new DefaultSystemTerminal(new DefaultNutsSystemTerminalBase());
                syst.install(this);

            }
        }
        if (this.systemTerminal != null) {
            this.systemTerminal.uninstall();
        }
        NutsSystemTerminal old = this.systemTerminal;
        this.systemTerminal = syst;

        if (old != this.systemTerminal) {
            for (NutsWorkspaceListener workspaceListener : workspaceListeners) {
                workspaceListener.onUpdateProperty("systemTerminal", old, this.systemTerminal);
            }
        }
        return this;
    }

    @Override
    public boolean initializeWorkspace(NutsWorkspaceFactory factory,
            NutsBootConfig runningBootConfig, NutsBootConfig wsBootConfig,
            URL[] bootClassWorldURLs, ClassLoader bootClassLoader,
            NutsWorkspaceOptions options) {

        if (options == null) {
            options = new NutsWorkspaceOptions();
        }
        if (options.getCreationTime() == 0) {
            configManager.setStartCreateTimeMillis(System.currentTimeMillis());
        }
        installedRepository = new DefaultNutsInstalledRepository(this);
        ioManager = new DefaultNutsIOManager(this);
        parseManager = new DefaultNutsParseManager(this);
        formatManager = new DefaultNutsFormatManager(this);
        extensionManager = new DefaultNutsWorkspaceExtensionManager(this, factory);
        configManager = new DefaultNutsWorkspaceConfigManager(this);
        configManager.onInitializeWorkspace(options,
                new DefaultNutsBootContext(this, runningBootConfig),
                new DefaultNutsBootContext(this, wsBootConfig),
                bootClassWorldURLs,
                bootClassLoader == null ? Thread.currentThread().getContextClassLoader() : bootClassLoader);

        boolean exists = config().isValidWorkspaceFolder();
        NutsWorkspaceOpenMode openMode = options.getOpenMode();
        if (openMode != null) {
            switch (openMode) {
                case OPEN_EXISTING: {
                    if (!exists) {
                        throw new NutsWorkspaceNotFoundException(runningBootConfig.getWorkspace());
                    }
                    break;
                }
                case CREATE_NEW: {
                    if (exists) {
                        throw new NutsWorkspaceAlreadyExistsException(runningBootConfig.getWorkspace());
                    }
                    break;
                }
            }
        }
        extensionManager.onInitializeWorkspace(bootClassLoader);

        NutsSystemTerminalBase termb = extensions().createSupported(NutsSystemTerminalBase.class, null);

        setSystemTerminal(termb);
        setTerminal(io().createTerminal());
        NutsSession session = createSession();

        initializing = true;
        try {
            if (!reloadWorkspace(session, options.getExcludedExtensions(), options.getExcludedRepositories())) {
                NutsWorkspaceUtils.checkReadOnly(this);
                LOG.log(Level.CONFIG, "Unable to load existing workspace. Creating new one at {0}", config().getContext(NutsBootContextType.RUNTIME).getWorkspace());
                exists = false;
                NutsWorkspaceConfig config = new NutsWorkspaceConfig();
                //load from config with resolution applied
                config.setBootApiVersion(wsBootConfig.getApiVersion());
                config.setBootRuntime(wsBootConfig.getRuntimeId());
                config.setBootRuntimeDependencies(wsBootConfig.getRuntimeDependencies());
                config.setBootRepositories(wsBootConfig.getRepositories());
                config.setBootJavaCommand(wsBootConfig.getJavaCommand());
                config.setBootJavaOptions(wsBootConfig.getJavaOptions());
                config.setStoreLocationStrategy(wsBootConfig.getStoreLocationStrategy());
                config.setRepositoryStoreLocationStrategy(wsBootConfig.getRepositoryStoreLocationStrategy());
                config.setStoreLocationLayout(wsBootConfig.getStoreLocationLayout());
                config.setGlobal(wsBootConfig.isGlobal());

                CoreNutsUtils.optionsToWconfig(options, config);
                configManager.setConfig(config);
                initializeWorkspace(options.getArchetype(), session);
                if (!config().isReadOnly()) {
                    config().save();
                }
                reconfigurePostInstall(new NutsInstallCompanionOptions().setAsk(true).setForce(false).setTrace(true), session);
                for (NutsWorkspaceListener workspaceListener : workspaceListeners) {
                    workspaceListener.onCreateWorkspace(this);
                }
            } else {
                if (options.getInitMode() == NutsBootInitMode.RECOVER) {
                    //should re
                    configManager.setBootApiVersion(wsBootConfig.getApiVersion());
                    configManager.setBootRuntime(wsBootConfig.getRuntimeId());
                    configManager.setBootRuntimeDependencies(wsBootConfig.getRuntimeDependencies());
                    configManager.setBootRepositories(wsBootConfig.getRepositories());
                    if (!config().isReadOnly()) {
                        config().save();
                    }
                }
            }
            if (configManager.getRepositoryRefs().length == 0) {
                initializeWorkspace(options.getArchetype(), session);
            }
            List<String> transientRepositoriesSet = options.getTransientRepositories() == null ? null : new ArrayList<>(Arrays.asList(options.getTransientRepositories()));
            for (String loc : transientRepositoriesSet) {
                String uuid = "transient_" + UUID.randomUUID().toString().replace("-", "");
                config()
                        .addRepository(
                                new NutsCreateRepositoryOptions()
                                        .setTemporay(true)
                                        .setName(uuid)
                                        .setFailSafe(false)
                                        .setLocation(loc)
                                        .setEnabled(true)
                        );
            }
            if (options.getLogin() != null && options.getLogin().trim().length() > 0) {
                String password = options.getPassword();
                if (CoreStringUtils.isBlank(password)) {
                    password = this.getTerminal().readPassword("Password : ");
                }
                this.security().login(options.getLogin(), password);
            }
            configManager.setStartCreateTimeMillis(options.getCreationTime());
            configManager.setEndCreateTimeMillis(System.currentTimeMillis());
            if (!options.isReadOnly()) {
                config().save(false);
            }
            LOG.log(Level.FINE, "Nuts Workspace loaded in {0}", CoreCommonUtils.formatPeriodMilli(config().getCreationFinishTimeMillis() - config().getCreationStartTimeMillis()));
            if (options.isPerf()) {
                getTerminal().fout().printf("**Nuts** Workspace loaded in [[%s]]%n",
                        CoreCommonUtils.formatPeriodMilli(config().getCreationFinishTimeMillis() - config().getCreationStartTimeMillis())
                );
            }
        } finally {
            initializing = false;
        }
        return !exists;
    }

    public void reconfigurePostInstall(NutsInstallCompanionOptions options, NutsSession session) {
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.log(Level.CONFIG, "Workspace created. running post creation configurator...");
        }
        if (options == null) {
            options = new NutsInstallCompanionOptions();
        }
        session = NutsWorkspaceUtils.validateSession(this, session);
        if (!config().getOptions().isSkipInstallCompanions()) {
            if (options.isTrace()) {
                PrintStream out = terminal.fout();
                StringBuilder version = new StringBuilder(config().getContext(NutsBootContextType.RUNTIME).getRuntimeId().getVersion().toString());
                while (version.length() < 25) {
                    version.append(' ');
                }
                out.println("{{/------------------------------------------------------------------------------\\\\}}");
                out.println("{{|}}==      _   _\\_      _\\_        ==                                                  {{|}}");
                out.println("{{|}}==     / | / /_  _\\_/ /_\\_\\_\\_\\_\\_  == ==N==etwork ==U==pdatable ==T==hings ==S==ervices                {{|}}");
                out.println("{{|}}==    /  |/ / / / / _\\_/ _\\_\\_/  == <<The Open Source Package Manager for __Java__ (TM)>>    {{|}}");
                out.println("{{|}}==   / /|  / /_/ / /_\\(_\\_  \\)   == <<and other __things__>> ... by ==vpc==                      {{|}}");
                out.println("{{|}}==  /_/ |_/\\\\_\\_\\_\\_/\\\\_\\_/_\\_\\_\\_/==     __http://github.com/thevpc/nuts__                    {{|}}");
                out.println("{{|}}      version [[" + version + "]]                                       {{|}}");
                out.println("{{|------------------------------------------------------------------------------|}}");
                out.println("{{|}}  This is the very {{first}} time ==Nuts== has been started for this workspace...     {{|}}");
                out.println("{{\\\\------------------------------------------------------------------------------/}}");
                out.println();
            }
            install().setIncludeCompanions(true).setTrace(true).setAsk(true).setSession(session).run();
        }
    }

    @Override
    public String[] getCompanionTools() {
        return new String[]{
            "net.vpc.app.nuts.toolbox:nsh",
            "net.vpc.app.nuts.toolbox:nfind",
            "net.vpc.app.nuts.toolbox:nadmin",
            "net.vpc.app.nuts.toolbox:ndi"
//                "mvn",
        };
    }

    @Override
    public NutsInstallCommand install() {
        return new DefaultNutsInstallCommand(this);
    }

    @Override
    public NutsUninstallCommand uninstall() {
        return new DefaultNutsUninstallCommand(this);
    }

    @Override
    public NutsUpdateCommand update() {
        return new DefaultNutsUpdateCommand(this);
    }

    @Override
    public NutsPushCommand push() {
        return new DefaultNutsPushCommand(this);
    }

    /**
     * true when core extension is required for running this workspace. A
     * default implementation should be as follow, but developers may implements
     * this with other logic : core extension is required when there are no
     * extensions or when the
     * <code>NutsConstants.ENV_KEY_EXCLUDE_CORE_EXTENSION</code> is forced to
     * false
     *
     * @return true when core extension is required for running this workspace
     */
    @Override
    public boolean requiresCoreExtension() {
        boolean coreFound = false;
        for (NutsId ext : extensions().getExtensions()) {
            if (ext.equalsSimpleName(config().getContext(NutsBootContextType.RUNTIME).getRuntimeId())) {
                coreFound = true;
                break;
            }
        }
        return !coreFound;
    }

    @Override
    public boolean isInstalled(NutsId id, boolean checkDependencies, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(this, session);
        NutsDefinition nutToInstall;
        try {
            nutToInstall = find().id(id).setSession(session).setTransitive(false).includeDependencies(checkDependencies)
                    .offline()
                    .setAcceptOptional(false)
                    .setIncludeInstallInformation(true)
                    .getResultDefinitions().first();
            if (nutToInstall == null) {
                return false;
            }
        } catch (NutsNotFoundException e) {
            return false;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error", e);
            return false;
        }
        return getInstalledRepository().isInstalled(nutToInstall.getId());
    }

    @Override
    public NutsExecCommand exec() {
        return new DefaultNutsExecCommand(this);
    }

    @Override
    public NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsFetchCommand options) {
        options = NutsWorkspaceUtils.validateSession(this, options);
        if (descriptor == null) {
            throw new NutsNotFoundException("<null>");
        }
        NutsId thisId = descriptor.getId();
        if (CoreNutsUtils.isEffectiveId(thisId)) {
            return thisId.setAlternative(descriptor.getAlternative());
        }
        String g = thisId.getGroup();
        String v = thisId.getVersion().getValue();
        if ((CoreStringUtils.isBlank(g)) || (CoreStringUtils.isBlank(v))) {
            NutsId[] parents = descriptor.getParents();
            for (NutsId parent : parents) {
                NutsId p = fetch().copyFrom(options).id(parent).setEffective(true).setSession(options.getSession()).getResultId();
                if (CoreStringUtils.isBlank(g)) {
                    g = p.getGroup();
                }
                if (CoreStringUtils.isBlank(v)) {
                    v = p.getVersion().getValue();
                }
                if (!CoreStringUtils.isBlank(g) && !CoreStringUtils.isBlank(v)) {
                    break;
                }
            }
            if (CoreStringUtils.isBlank(g) || CoreStringUtils.isBlank(v)) {
                throw new NutsNotFoundException(thisId, "Unable to fetchEffective for " + thisId + ". Best Result is " + thisId.toString(), null);
            }
        }
        if (CoreStringUtils.containsVars(g) || CoreStringUtils.containsVars(v)) {
            Map<String, String> p = descriptor.getProperties();
            NutsId bestId = new DefaultNutsId(null, g, thisId.getName(), v, "");
            bestId = bestId.apply(new MapStringMapper(p));
            if (CoreNutsUtils.isEffectiveId(bestId)) {
                return bestId.setAlternative(descriptor.getAlternative());
            }
            Stack<NutsId> all = new Stack<>();
            NutsId[] parents = descriptor.getParents();
            all.addAll(Arrays.asList(parents));
            while (!all.isEmpty()) {
                NutsId parent = all.pop();
                NutsDescriptor dd = fetch().copyFrom(options).id(parent).setEffective(true).getResultDescriptor();
                bestId.apply(new MapStringMapper(dd.getProperties()));
                if (CoreNutsUtils.isEffectiveId(bestId)) {
                    return bestId.setAlternative(descriptor.getAlternative());
                }
                all.addAll(Arrays.asList(dd.getParents()));
            }
            throw new NutsNotFoundException(bestId.toString(), "Unable to fetchEffective for " + thisId + ". Best Result is " + bestId.toString(), null);
        }
        NutsId bestId = new DefaultNutsId(null, g, thisId.getName(), v, "");
        if (!CoreNutsUtils.isEffectiveId(bestId)) {
            throw new NutsNotFoundException(bestId.toString(), "Unable to fetchEffective for " + thisId + ". Best Result is " + bestId.toString(), null);
        }
        return bestId.setAlternative(descriptor.getAlternative());
    }

    @Override
    public NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        if (!descriptor.getId().getVersion().isBlank() && descriptor.getId().getVersion().isSingleValue() && descriptor.getId().toString().indexOf('$') < 0) {
            Path l = config().getStoreLocation(descriptor.getId(), NutsStoreLocation.CACHE);
            String nn = config().getDefaultIdFilename(descriptor.getId().setFace("cache-eff-nuts"));
            Path eff = l.resolve(nn);
            if (Files.isRegularFile(eff)) {
                try {
                    NutsDescriptor d = parser().parseDescriptor(eff);
                    if (d != null) {
                        return d;
                    }
                } catch (Exception ex) {
                    //
                }
            }
        } else {
            //System.out.println("Why");
        }
        NutsDescriptor effectiveDescriptor = _resolveEffectiveDescriptor(descriptor, session);
        Path l = config().getStoreLocation(effectiveDescriptor.getId(), NutsStoreLocation.CACHE);
        String nn = config().getDefaultIdFilename(effectiveDescriptor.getId().setFace("cache-eff-nuts"));
        Path eff = l.resolve(nn);
        try {
            formatter().createDescriptorFormat().setPretty(true).print(effectiveDescriptor, eff);
        } catch (Exception ex) {
            //
        }
        return effectiveDescriptor;
    }

    protected NutsDescriptor _resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(this, session);
        NutsId[] parents = descriptor.getParents();
        NutsDescriptor[] parentDescriptors = new NutsDescriptor[parents.length];
        for (int i = 0; i < parentDescriptors.length; i++) {
            parentDescriptors[i] = resolveEffectiveDescriptor(
                    fetch().id(parents[i]).setEffective(false).setSession(session).getResultDescriptor(),
                    session
            );
        }
        NutsDescriptor effectiveDescriptor = descriptor.applyParents(parentDescriptors).applyProperties();
        NutsDependency[] oldDependencies = effectiveDescriptor.getDependencies();
        List<NutsDependency> newDeps = new ArrayList<>();
        boolean someChange = false;

        for (NutsDependency d : oldDependencies) {
            if (CoreStringUtils.isBlank(d.getScope())
                    || d.getVersion().isBlank()
                    || CoreStringUtils.isBlank(d.getOptional())) {
                NutsDependency standardDependencyOk = null;
                for (NutsDependency standardDependency : effectiveDescriptor.getStandardDependencies()) {
                    if (standardDependency.getSimpleName().equals(d.getId().getSimpleName())) {
                        standardDependencyOk = standardDependency;
                        break;
                    }
                }
                if (standardDependencyOk != null) {
                    if (CoreStringUtils.isBlank(d.getScope())
                            && !CoreStringUtils.isBlank(standardDependencyOk.getScope())) {
                        someChange = true;
                        d = d.setScope(standardDependencyOk.getScope());
                    }
                    if (CoreStringUtils.isBlank(d.getOptional())
                            && !CoreStringUtils.isBlank(standardDependencyOk.getOptional())) {
                        someChange = true;
                        d = d.setOptional(standardDependencyOk.getOptional());
                    }
                    if (d.getVersion().isBlank()
                            && !standardDependencyOk.getVersion().isBlank()) {
                        someChange = true;
                        d = d.setVersion(standardDependencyOk.getVersion());
                    }
                }
            }

            if ("import".equals(d.getScope())) {
                someChange = true;
                newDeps.addAll(Arrays.asList(fetch().id(d.getId()).setEffective(true).setSession(session).getResultDescriptor().getDependencies()));
            } else {
                newDeps.add(d);
            }
        }
        if (someChange) {
            effectiveDescriptor = effectiveDescriptor.setDependencies(newDeps.toArray(new NutsDependency[0]));
        }
        return effectiveDescriptor;
    }

    @Override
    public NutsWorkspaceExtensionManager extensions() {
        return extensionManager;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    protected void initializeWorkspace(String archetype, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(this, session);
        if (CoreStringUtils.isBlank(archetype)) {
            archetype = "default";
        }
        NutsWorkspaceArchetypeComponent instance = null;
        TreeSet<String> validValues = new TreeSet<>();
        for (NutsWorkspaceArchetypeComponent ac : extensions().createAllSupported(NutsWorkspaceArchetypeComponent.class, this)) {
            if (archetype.equals(ac.getName())) {
                instance = ac;
                break;
            }
            validValues.add(ac.getName());
        }
        if (instance == null) {
            //get the default implementation
            throw new NutsException("Invalid archetype " + archetype + ". Valid values are : " + validValues);
        }

        //has all rights (by default)
        //no right nor group is needed for admin user
        security().updateUser(NutsConstants.Names.USER_ADMIN).setCredentials("admin").run();

        instance.initialize(this, session);

//        //isn't it too late for adding extensions?
//        try {
//            addWorkspaceExtension(NutsConstants.NUTS_ID_BOOT_RUNTIME, session);
//        } catch (Exception ex) {
//            log.log(Level.SEVERE, "Unable to load Nuts-core. The tool is running in minimal mode.");
//        }
    }

    @Override
    public NutsInstallerComponent getInstaller(NutsDefinition nutToInstall, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(this, session);
        if (nutToInstall != null && nutToInstall.getPath() != null) {
            NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsExecutorDescriptor installerDescriptor = descriptor.getInstaller();
            NutsDefinition runnerFile = nutToInstall;
            if (installerDescriptor != null && installerDescriptor.getId() != null) {
                if (installerDescriptor.getId() != null) {
                    runnerFile = fetch().id(installerDescriptor.getId()).setSession(session)
                            .setTransitive(false)
                            .setAcceptOptional(false)
                            .includeDependencies()
                            .setIncludeInstallInformation(true)
                            .getResultDefinition();
                }
            }
            if (runnerFile == null) {
                runnerFile = nutToInstall;
            }
            NutsInstallerComponent best = extensions().createSupported(NutsInstallerComponent.class, runnerFile);
            if (best != null) {
                return best;
            }
        }
        return new CommandForIdNutsInstallerComponent();
    }

    /**
     * return installed version
     *
     * @param id
     * @return
     */
    @Override
    public String[] getInstalledVersions(NutsId id) {
        return Arrays.stream(getInstalledRepository().findInstalledVersions(id))
                .map(x -> x.getVersion().getValue())
                .sorted((a, b) -> DefaultNutsVersion.compareVersions(a, b))
                .toArray(String[]::new);
    }

    @Override
    public void installImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean trace, boolean updateDefaultVersion) {
        installOrUpdateImpl(def, args, installerComponent, session, true, trace, updateDefaultVersion, false);
    }
    
    @Override
    public void updateImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean trace, boolean updateDefaultVersion) {
        installOrUpdateImpl(def, args, installerComponent, session, true, trace, updateDefaultVersion, true);
    }
    
    public void installOrUpdateImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean resolveInstaller, boolean trace, boolean updateDefaultVersion, boolean isUpdate) {
        if (def == null) {
            return;
        }
        if (resolveInstaller) {
            if (installerComponent == null) {
                if (def.getPath() != null) {
                    installerComponent = getInstaller(def, session);
                }
            }
        }
        session = NutsWorkspaceUtils.validateSession(this, session);
        boolean reinstall = def.getInstallation().isInstalled();
        PrintStream out = session.getTerminal().fout();
        out.flush();
        if (installerComponent != null) {
            if (def.getPath() != null) {
                NutsExecutionContext executionContext = createNutsExecutionContext(def, args, new String[0], session, true, null);
                getInstalledRepository().install(executionContext.getNutsDefinition().getId());
                if (isUpdate) {
                    try {
                        installerComponent.update(executionContext);
                    } catch (NutsReadOnlyException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        if (trace) {
                            out.printf("%N @@Failed@@ to update : %s.%n", formatter().createIdFormat().toString(def.getId()), ex.toString());
                        }
                        throw new NutsExecutionException("Unable to update " + def.getId().toString(), ex);
                    }
                } else {
                    try {
                        installerComponent.install(executionContext);
//                    out.print(getFormatManager().createIdFormat().format(def.getId()) + " installed ##successfully##.\n");
                    } catch (NutsReadOnlyException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        if (trace) {
                            out.printf("%N @@Failed@@ to install : %s.%n", formatter().createIdFormat().toString(def.getId()), ex.toString());
                        }
                        getInstalledRepository().uninstall(executionContext.getNutsDefinition().getId());
                        throw new NutsExecutionException("Unable to install " + def.getId().toString(), ex);
                    }
                }
                Path installFolder = config().getStoreLocation(def.getId(), NutsStoreLocation.PROGRAMS);
                ((DefaultNutsDefinition) def).setInstallation(new DefaultNutsInstallInfo(true, true, installFolder));
            }
        }
        for (NutsInstallListener nutsListener : session.getListeners(NutsInstallListener.class)) {
            if (isUpdate) {
                nutsListener.onUpdate(def, session);
            } else {
                nutsListener.onInstall(def, reinstall, session);
            }
        }
        ((DefaultNutsInstallInfo) def.getInstallation()).setJustInstalled(true);
        if (updateDefaultVersion) {
            getInstalledRepository().setDefaultVersion(def.getId());
        }
        if (trace) {
            String setAsDefaultString = "";
            if (updateDefaultVersion) {
                setAsDefaultString = " set as ##default##.";
            }
            if (!def.getInstallation().isInstalled()) {
                if (!def.getContent().isCached()) {
                    if (def.getContent().isTemporary()) {
                        out.printf("%N installed ##successfully## from temporarily file %s.%N%n", formatter().createIdFormat().toString(def.getId()), def.getPath(), setAsDefaultString);
                    } else {
                        out.printf("%N installed ##successfully## from remote repository.%N%n", formatter().createIdFormat().toString(def.getId()), setAsDefaultString);
                    }
                } else {
                    if (def.getContent().isTemporary()) {
                        out.printf("%N installed from local temporarily file %s.%N%n", formatter().createIdFormat().toString(def.getId()), def.getPath(), setAsDefaultString);
                    } else {
                        out.printf("%N installed from local repository.%N%n", formatter().createIdFormat().toString(def.getId()), setAsDefaultString);
                    }
                }
            } else {
                out.printf("%N installed ##successfully##.%N%n", formatter().createIdFormat().toString(def.getId()), setAsDefaultString);
            }
        }
    }

    @Override
    public NutsExecutionContext createNutsExecutionContext(NutsDefinition nutToInstall, String[] args, String[] executorArgs, NutsSession session, boolean failFast, String commandName) {
        if (commandName == null) {
            commandName = resolveCommandName(nutToInstall.getId());
        }
        NutsDescriptor descriptor = nutToInstall.getDescriptor();
        NutsExecutorDescriptor installer = descriptor.getInstaller();
        List<String> eargs = new ArrayList<>();
        List<String> aargs = new ArrayList<>();
        Properties props = null;
        if (installer != null) {
            if (installer.getOptions() != null) {
                eargs.addAll(Arrays.asList(installer.getOptions()));
            }
            props = installer.getProperties();
        }
        if (executorArgs != null) {
            eargs.addAll(Arrays.asList(executorArgs));
        }
        if (args != null) {
            aargs.addAll(Arrays.asList(args));
        }
        Path installFolder = config().getStoreLocation(nutToInstall.getId(), NutsStoreLocation.PROGRAMS);
        Properties env = new Properties();
        return new NutsExecutionContextImpl(nutToInstall, aargs.toArray(new String[0]), eargs.toArray(new String[0]), env, props, installFolder.toString(), session, this, failFast, commandName);
    }

    public String resolveCommandName(NutsId id) {
        String nn = id.getName();
        NutsWorkspaceCommandAlias c = config().findCommandAlias(nn);
        if (c != null) {
            if (c.getOwner().getLongName().equals(id.getLongName())) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getName() + "-" + id.getVersion();
        c = config().findCommandAlias(nn);
        if (c != null) {
            if (c.getOwner().getLongName().equals(id.getLongName())) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getGroup() + "." + id.getName() + "-" + id.getVersion();
        c = config().findCommandAlias(nn);
        if (c != null) {
            if (c.getOwner().getLongName().equals(id.getLongName())) {
                return nn;
            }
        } else {
            return nn;
        }
        throw new NutsElementNotFoundException("Unable to resolve command name for " + id.toString());
    }

    protected boolean reloadWorkspace(NutsSession session, String[] excludedExtensions, String[] excludedRepositories) {
        Set<String> excludedExtensionsSet = excludedExtensions == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(excludedExtensions), " ,;"));
        Set<String> excludedRepositoriesSet = excludedRepositories == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(excludedRepositories), " ,;"));
        session = NutsWorkspaceUtils.validateSession(this, session);
        boolean loadedConfig = false;
        try {
            loadedConfig = configManager.load();
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Erroneous config file. Unable to load file " + configManager.getConfigFile() + " : " + ex.toString(), ex);
            if (!config().isReadOnly()) {
                Path newfile = config().getWorkspaceLocation().resolve("nuts-workspace-"
                        + new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date())
                        + ".json");
                LOG.log(Level.SEVERE, "Erroneous config file will replace by fresh one. Old config is copied to {0}", newfile);
                try {
                    Files.move(configManager.getConfigFile(), newfile);
                } catch (IOException e) {
                    throw new UncheckedIOException("Unable to load and re-create config file " + configManager.getConfigFile() + " : " + e.toString(), new IOException(ex));
                }
            } else {
                throw new UncheckedIOException("Unable to load config file " + configManager.getConfigFile(), new IOException(ex));
            }
        }
        if (loadedConfig) {
            config0().removeAllRepositories();

            //extensions already wired... this is needless!
            for (NutsId extensionId : extensions().getExtensions()) {
                if (excludedExtensionsSet != null && CoreNutsUtils.findNutsIdBySimpleNameInStrings(extensionId, excludedExtensionsSet) != null) {
                    continue;
                }
                NutsSession sessionCopy = session.copy();
                extensionManager.wireExtension(extensionId,
                        fetch().session(session).setFetchStratery(NutsFetchStrategy.ONLINE)
                                .setTransitive(true),
                        sessionCopy);
                if (sessionCopy.getTerminal() != session.getTerminal()) {
                    session.setTerminal(sessionCopy.getTerminal());
                }
            }

            for (NutsRepositoryRef ref : configManager.getRepositoryRefs()) {
                if (excludedRepositoriesSet != null && excludedRepositoriesSet.contains(ref.getName())) {
                    continue;
                }
                config0().wireRepository(config().createRepository(
                        CoreNutsUtils.refToOptions(ref), config0().getRepositoriesRoot(), null)
                );
            }

            NutsUserConfig adminSecurity = NutsWorkspaceConfigManagerExt.of(config()).getUser(NutsConstants.Names.USER_ADMIN);
            if (adminSecurity == null || CoreStringUtils.isBlank(adminSecurity.getCredentials())) {
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.log(Level.CONFIG, NutsConstants.Names.USER_ADMIN + " user has no credentials. reset to default");
                }
                security().updateUser(NutsConstants.Names.USER_ADMIN).credentials("admin").session(session).run();
            }
            for (NutsCommandAliasFactoryConfig commandFactory : configManager.getCommandFactories()) {
                try {
                    config().addCommandAliasFactory(commandFactory, new NutsAddOptions().session(session));
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Unable to instantiate Command Factory {0}", commandFactory);
                }
            }
            for (NutsWorkspaceListener listener : workspaceListeners) {
                listener.onReloadWorkspace(this);
            }
            //if save is needed, will be applied
            config().save(false);
            return true;
        }
        return false;
    }

//    @Override
//    public List<NutsRepository> getEnabledRepositories(NutsRepositorySupportedAction fmode, NutsId nutsId, NutsRepositoryFilter repositoryFilter, NutsSession session, NutsFetchMode mode, NutsFetchCommand options) {
//        return NutsWorkspaceUtils.filterRepositories(this, fmode, nutsId, repositoryFilter, mode, options);
//    }
    public void checkSupportedRepositoryType(String type) {
        if (!config().isSupportedRepositoryType(type)) {
            throw new NutsIllegalArgumentException("Unsupported repository type " + type);
        }
    }

    @Override
    public void addUserPropertyListener(NutsMapListener<String, Object> listener) {
        userProperties.addListener(listener);
    }

    @Override
    public void removeUserPropertyListener(NutsMapListener<String, Object> listener) {
        userProperties.removeListener(listener);
    }

    @Override
    public NutsMapListener<String, Object>[] getUserPropertyListeners() {
        return userProperties.getListeners();
    }

    @Override
    public String toString() {
        return "NutsWorkspace{"
                + configManager
                + '}';
    }

    @Override
    public NutsId resolveIdForClass(Class clazz) {
        PomId u = PomIdResolver.resolvePomId(clazz, null);
        if (u == null) {
            return null;
        }
        return parser().parseId(u.getGroupId() + ":" + u.getArtifactId() + "#" + u.getVersion());
    }

    @Override
    public String resolveDefaultHelpForClass(Class clazz) {
        NutsId nutsId = resolveIdForClass(clazz);
        if (nutsId != null) {
            String urlPath = "/" + nutsId.getGroup().replace('.', '/') + "/" + nutsId.getName() + ".help";
            URL resource = getClass().getResource(urlPath);
            if (resource == null) {
                return null;
            }
            String s;
            try {
                s = CoreIOUtils.loadString(resource.openStream(), true);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            if (!CoreStringUtils.isBlank(s)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public NutsId[] resolveIdsForClass(Class clazz) {
        PomId[] u = PomIdResolver.resolvePomIds(clazz);
        NutsId[] all = new NutsId[u.length];
        for (int i = 0; i < all.length; i++) {
            all[i] = parser().parseId(u[i].getGroupId() + ":" + u[i].getArtifactId() + "#" + u[i].getVersion());
        }
        return all;
    }

    @Override
    public NutsIdBuilder createIdBuilder() {
        return new DefaultNutsIdBuilder();
    }

    @Override
    public NutsDependencyBuilder createDependencyBuilder() {
        return new DefaultNutsDependencyBuilder();
    }

    @Override
    public NutsFindCommand find() {
        return new DefaultNutsFindCommand(this);
    }

    @Override
    public String getHelpText() {
        return this.io().getResourceString("/net/vpc/app/nuts/nuts-help.help", getClass(), "no help found");
    }

    @Override
    public String getWelcomeText() {
        return this.io().getResourceString("/net/vpc/app/nuts/nuts-welcome.help", getClass(), "no welcome found");
    }

    @Override
    public String getLicenseText() {
        return this.io().getResourceString("/net/vpc/app/nuts/nuts-license.help", getClass(), "no license found");
    }

    @Override
    public NutsSystemTerminal getSystemTerminal() {
        return systemTerminal;
    }

    @Override
    public NutsSessionTerminal getTerminal() {
        return terminal;
    }

    @Override
    public void setTerminal(NutsSessionTerminal terminal) {
        if (terminal == null) {
            terminal = io().createTerminal();
        }
        if (!(terminal instanceof UnmodifiableTerminal)) {
            terminal = new UnmodifiableTerminal(terminal);
        }
        this.terminal = terminal;
    }

    @Override
    public NutsDeployCommand deploy() {
        return new DefaultNutsDeployCommand(this);
    }

    @Override
    public NutsUndeployCommand undeploy() {
        return new DefaultNutsUndeployCommand(this);
    }

    @Override
    public NutsIOManager io() {
        return ioManager;
    }

    @Override
    public NutsParseManager parser() {
        return parseManager;
    }

    @Override
    public NutsFormatManager formatter() {
        return formatManager;
    }

    @Override
    public NutsFetchCommand fetch() {
        return new DefaultNutsFetchCommand(this);
    }

    @Override
    public void removeRepositoryListener(NutsRepositoryListener listener) {
        repositoryListeners.add(listener);
    }

    @Override
    public void addRepositoryListener(NutsRepositoryListener listener) {
        if (listener != null) {
            repositoryListeners.add(listener);
        }
    }

    @Override
    public NutsRepositoryListener[] getRepositoryListeners() {
        return repositoryListeners.toArray(new NutsRepositoryListener[0]);
    }

    @Override
    public DefaultNutsInstalledRepository getInstalledRepository() {
        return installedRepository;
    }

    @Override
    public NutsUpdateStatisticsCommand updateStatistics() {
        return new DefaultNutsUpdateStatisticsCommand(this);
    }

    /**
     * creates a zip file based on the folder. The folder should contain a
     * descriptor file at its root
     *
     * @param contentFolder folder to bundle
     * @param destFile created bundle file or null to create a file with the
     * very same name as the folder
     * @param session current session
     * @return bundled nuts file, the nuts is neither deployed nor installed!
     */
//    @Derecated
//    public NutsDefinition createBundle(Path contentFolder, Path destFile, NutsQueryOptions queryOptions, NutsSession session) {
//        session = CoreNutsUtils.validateSession(session, this);
//        if (Files.isDirectory(contentFolder)) {
//            NutsDescriptor descriptor = null;
//            Path ext = contentFolder.resolve(NutsConstants.NUTS_DESC_FILE_NAME);
//            if (Files.exists(ext)) {
//                descriptor = parser().parseDescriptor(ext);
//                if (descriptor != null) {
//                    if ("zip".equals(descriptor.getPackaging())) {
//                        if (destFile == null) {
//                            destFile = io().path(io().expandPath(contentFolder.getParent().resolve(descriptor.getId().getGroup() + "." + descriptor.getId().getName() + "." + descriptor.getId().getVersion() + ".zip")));
//                        }
//                        try {
//                            ZipUtils.zip(contentFolder.toString(), new ZipOptions(), destFile.toString());
//                        } catch (IOException ex) {
//                            throw new UncheckedIOException(ex);
//                        }
//                        return new DefaultNutsDefinition(
//                                this, null,
//                                descriptor.getId(),
//                                descriptor,
//                                new NutsContent(destFile,
//                                        true,
//                                        false),
//                                null
//                        );
//                    } else {
//                        throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
//                    }
//                }
//            }
//            throw new NutsIllegalArgumentException("Invalid Nut Folder source. unable to detect descriptor");
//        } else {
//            throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
//        }
//    }
//    @Override
//    public boolean isFetched(NutsId id, NutsSession session) {
//        session = CoreNutsUtils.validateSession(session, this);
//        NutsSession offlineSession = session.copy();
//        try {
//            NutsDefinition found = fetch().id(id).offline().setSession(offlineSession).setIncludeInstallInformation(false).setIncludeFile(true).getResultDefinition();
//            return found != null;
//        } catch (Exception e) {
//            return false;
//        }
//    }
}
