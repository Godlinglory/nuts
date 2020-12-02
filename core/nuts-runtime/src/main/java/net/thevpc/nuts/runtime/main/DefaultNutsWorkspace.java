/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.main;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.repos.NutsInstalledRepository;
import net.thevpc.nuts.runtime.main.config.*;
import net.thevpc.nuts.runtime.main.repos.DefaultNutsInstalledRepository;
import net.thevpc.nuts.runtime.main.wscommands.*;
import net.thevpc.nuts.runtime.*;
import net.thevpc.nuts.runtime.log.DefaultNutsLogManager;
import net.thevpc.nuts.runtime.security.DefaultNutsWorkspaceSecurityManager;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.main.installers.CommandForIdNutsInstallerComponent;
import net.thevpc.nuts.runtime.ext.DefaultNutsWorkspaceExtensionManager;
import net.thevpc.nuts.runtime.filters.DefaultNutsFilterManager;
import net.thevpc.nuts.runtime.io.DefaultNutsIOManager;
import net.thevpc.nuts.runtime.log.DefaultNutsLogger;
import net.thevpc.nuts.runtime.log.NutsLogVerb;
import net.thevpc.nuts.runtime.security.ReadOnlyNutsWorkspaceOptions;
import net.thevpc.nuts.runtime.terminals.DefaultNutsSessionTerminal;
import net.thevpc.nuts.runtime.terminals.DefaultNutsSystemTerminalBase;
import net.thevpc.nuts.runtime.terminals.DefaultSystemTerminal;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.util.NutsDependencyScopes;
import net.thevpc.nuts.runtime.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.util.common.CoreCommonUtils;
import net.thevpc.nuts.runtime.util.common.MapStringMapper;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import net.thevpc.nuts.spi.NutsBootWorkspaceFactory;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;
import net.thevpc.nuts.spi.NutsWorkspaceArchetypeComponent;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/6/17.
 */
@NutsPrototype
public class DefaultNutsWorkspace extends AbstractNutsWorkspace implements NutsWorkspaceExt {

    public NutsLogger LOG;
    private DefaultNutsInstalledRepository installedRepository;
    private NutsLogManager logCmd;

    public DefaultNutsWorkspace(NutsWorkspaceInitInformation info) {
        super(info);
        logCmd = new DefaultNutsLogManager(this, info);
        filters = new DefaultNutsFilterManager(this);
        LOG = logCmd.of(DefaultNutsWorkspace.class);
        ((DefaultNutsLogger) LOG).suspendTerminal();
        installedRepository = new DefaultNutsInstalledRepository(this, info);
        ioManager = new DefaultNutsIOManager(this);
        repositoryManager = new DefaultNutsRepositoryManager(this);
        configManager = new DefaultNutsWorkspaceConfigManager(this, info);
        NutsLoggerOp LOGCRF = LOG.with().level(Level.CONFIG).verb(NutsLogVerb.READ).formatted();
        NutsLoggerOp LOGCSF = LOG.with().level(Level.CONFIG).verb(NutsLogVerb.START).formatted();
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.with().level(Level.CONFIG).verb(NutsLogVerb.START).log(" ===============================================================================");
            String s = CoreIOUtils.loadString(getClass().getResourceAsStream("/net/thevpc/nuts/includes/standard-header.help"), true);
            s=s.replace("${nuts.workspace-runtime.version}",Nuts.getVersion());
            for (String s1 : s.split("\n")) {
                LOGCSF.log(s1);
            }
            LOG.with().level(Level.CONFIG).verb(NutsLogVerb.START).log(" ");
            LOG.with().level(Level.CONFIG).verb(NutsLogVerb.START).log(" = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
            LOG.with().level(Level.CONFIG).verb(NutsLogVerb.START).log(" ");
            LOGCSF.log("start ##nuts## **{0}** at {1}", Nuts.getVersion(), CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(info.getOptions().getCreationTime())));
            LOGCRF.log("open Nuts Workspace               : {0}", new NutsString(commandLine().formatter().setValue(info.getOptions().format().getBootCommand()).format()));
            LOGCRF.log("open Nuts Workspace (compact)     : {0}", new NutsString(commandLine().formatter().setValue(info.getOptions().format().compact().getBootCommand()).format()));

            LOGCRF.log("open Workspace with config        : ");
            LOGCRF.log("   nuts-uuid                      : {0}", CoreNutsUtils.desc(info.getUuid()));
            LOGCRF.log("   nuts-name                      : {0}", CoreNutsUtils.desc(info.getName()));
            LOGCRF.log("   nuts-api-version               : {0}", Nuts.getVersion());
            LOGCRF.log("   nuts-boot-repositories         : {0}", CoreNutsUtils.desc(info.getBootRepositories()));
            LOGCRF.log("   nuts-runtime-dependencies      : {0}", new NutsString(Arrays.stream(info.getRuntimeBootDescriptor().getDependencies()).map(x -> id().formatter(id().parser().parse(x)).format()).collect(Collectors.joining(";"))));
            LOGCRF.log("   nuts-runtime-urls              : {0}", Arrays.asList(info.getClassWorldURLs()).stream().map(x -> x.toString()).collect(Collectors.joining(";")));
            LOGCRF.log("   nuts-extension-dependencies    : {0}", new NutsString(
                    toIds(info.getExtensionBootDescriptors()).stream().map(x -> id().formatter(id().parser().parse(x)).format()).collect(Collectors.joining(";"))));
//            if (hasUnsatisfiedRequirements()) {
//                LOG.log(Level.CONFIG, "\t execution-requirements         : unsatisfied {0}", getRequirementsHelpString(true));
//            } else {
//                LOG.log(Level.CONFIG, "\t execution-requirements         : satisfied");
//            }
            LOGCRF.log("   nuts-workspace                 : {0}", CoreNutsUtils.formatLogValue(info.getOptions().getWorkspace(), info.getWorkspaceLocation()));
            LOGCRF.log("   nuts-store-apps                : {0}", CoreNutsUtils.formatLogValue(info.getOptions().getStoreLocation(NutsStoreLocation.APPS), info.getStoreLocation(NutsStoreLocation.APPS)));
            LOGCRF.log("   nuts-store-config              : {0}", CoreNutsUtils.formatLogValue(info.getOptions().getStoreLocation(NutsStoreLocation.CONFIG), info.getStoreLocation(NutsStoreLocation.CONFIG)));
            LOGCRF.log("   nuts-store-var                 : {0}", CoreNutsUtils.formatLogValue(info.getOptions().getStoreLocation(NutsStoreLocation.VAR), info.getStoreLocation(NutsStoreLocation.VAR)));
            LOGCRF.log("   nuts-store-log                 : {0}", CoreNutsUtils.formatLogValue(info.getOptions().getStoreLocation(NutsStoreLocation.LOG), info.getStoreLocation(NutsStoreLocation.LOG)));
            LOGCRF.log("   nuts-store-temp                : {0}", CoreNutsUtils.formatLogValue(info.getOptions().getStoreLocation(NutsStoreLocation.TEMP), info.getStoreLocation(NutsStoreLocation.TEMP)));
            LOGCRF.log("   nuts-store-cache               : {0}", CoreNutsUtils.formatLogValue(info.getOptions().getStoreLocation(NutsStoreLocation.CACHE), info.getStoreLocation(NutsStoreLocation.CACHE)));
            LOGCRF.log("   nuts-store-run                 : {0}", CoreNutsUtils.formatLogValue(info.getOptions().getStoreLocation(NutsStoreLocation.RUN), info.getStoreLocation(NutsStoreLocation.RUN)));
            LOGCRF.log("   nuts-store-lib                 : {0}", CoreNutsUtils.formatLogValue(info.getOptions().getStoreLocation(NutsStoreLocation.LIB), info.getStoreLocation(NutsStoreLocation.LIB)));
            LOGCRF.log("   nuts-store-strategy            : {0}", CoreNutsUtils.formatLogValue(info.getOptions().getStoreLocationStrategy(), info.getStoreLocationStrategy()));
            LOGCRF.log("   nuts-repos-store-strategy      : {0}", CoreNutsUtils.formatLogValue(info.getOptions().getRepositoryStoreLocationStrategy(), info.getRepositoryStoreLocationStrategy()));
            LOGCRF.log("   nuts-store-layout              : {0}", CoreNutsUtils.formatLogValue(info.getOptions().getStoreLocationLayout(), info.getStoreLocationLayout() == null ? "system" : info.getStoreLocationLayout().id()));
            LOGCRF.log("   option-read-only               : {0}", info.getOptions().isReadOnly());
            LOGCRF.log("   option-trace                   : {0}", info.getOptions().isTrace());
            LOGCRF.log("   option-progress                : {0}", CoreNutsUtils.desc(info.getOptions().getProgressOptions()));
            LOGCRF.log("   inherited                      : {0}", info.getOptions().isInherited());
            LOGCRF.log("   inherited-nuts-boot-args       : {0}", System.getProperty("nuts.boot.args") == null ? "<EMPTY>" : new NutsString(commandLine().formatter().setValue(System.getProperty("nuts.boot.args")).format()));
            LOGCRF.log("   inherited-nuts-args            : {0}", System.getProperty("nuts.args") == null ? "<EMPTY>" : new NutsString(commandLine().formatter().setValue(System.getProperty("nuts.args")).format()));
            LOGCRF.log("   option-open-mode               : {0}", CoreNutsUtils.formatLogValue(info.getOptions().getOpenMode(), info.getOptions().getOpenMode() == null ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : info.getOptions().getOpenMode()));
            LOGCRF.log("   java-home                      : {0}", System.getProperty("java.home"));
            LOGCRF.log("   java-classpath                 : {0}", System.getProperty("java.class.path"));
            LOGCRF.log("   java-library-path              : {0}", System.getProperty("java.library.path"));
            LOGCRF.log("   os-name                        : {0}", System.getProperty("os.name"));
            LOGCRF.log("   os-arch                        : {0}", System.getProperty("os.arch"));
            LOGCRF.log("   os-version                     : {0}", System.getProperty("os.version"));
            LOGCRF.log("   user-name                      : {0}", System.getProperty("user.name"));
            LOGCRF.log("   user-dir                       : {0}", System.getProperty("user.dir"));
            LOGCRF.log("   user-home                      : {0}", System.getProperty("user.home"));
        }
        securityManager = new DefaultNutsWorkspaceSecurityManager(this);
        String workspaceLocation = info.getWorkspaceLocation();
        String apiVersion = info.getApiVersion();
        String runtimeId = info.getRuntimeId();
        String repositories = info.getBootRepositories();
        NutsWorkspaceOptions uoptions = info.getOptions();
        NutsBootWorkspaceFactory bootFactory = info.getBootWorkspaceFactory();
        ClassLoader bootClassLoader = info.getClassWorldLoader();
        if (uoptions == null) {
            uoptions = new ReadOnlyNutsWorkspaceOptions(config().optionsBuilder());
        } else {
            uoptions = new ReadOnlyNutsWorkspaceOptions(uoptions.copy());
        }
        if (uoptions.getCreationTime() == 0) {
            configManager.setStartCreateTimeMillis(System.currentTimeMillis());
        }

        NutsBootConfig cfg = new NutsBootConfig();
        cfg.setWorkspace(workspaceLocation);
        cfg.setApiVersion(apiVersion);
        cfg.setRuntimeId(runtimeId);
        cfg.setRuntimeBootDescriptor(info.getRuntimeBootDescriptor());
        cfg.setExtensionBootDescriptors(info.getExtensionBootDescriptors());
        NutsSession bootSession = createBootSession();
        extensionManager = new DefaultNutsWorkspaceExtensionManager(this, bootFactory, uoptions.getExcludedExtensions(),bootSession);
        boolean exists = NutsWorkspaceConfigManagerExt.of(config()).isValidWorkspaceFolder();
        NutsWorkspaceOpenMode openMode = uoptions.getOpenMode();
        if (openMode != null) {
            switch (openMode) {
                case OPEN_EXISTING: {
                    if (!exists) {
                        throw new NutsWorkspaceNotFoundException(this, workspaceLocation);
                    }
                    break;
                }
                case CREATE_NEW: {
                    if (exists) {
                        throw new NutsWorkspaceAlreadyExistsException(this, workspaceLocation);
                    }
                    break;
                }
            }
        }
        extensionManager.onInitializeWorkspace(info,bootClassLoader, bootSession);
//        List<DefaultNutsWorkspaceExtensionManager.RegInfo> regInfos = extensionManager.buildRegInfos(bootSession);
//        for (Iterator<DefaultNutsWorkspaceExtensionManager.RegInfo> iterator = regInfos.iterator(); iterator.hasNext(); ) {
//            DefaultNutsWorkspaceExtensionManager.RegInfo regInfo = iterator.next();
//            switch (regInfo.getExtensionPointType().getName()) {
//                case "net.thevpc.nuts.NutsSystemTerminalBase":
//                case "net.thevpc.nuts.NutsSessionTerminalBase":{
//                    extensionManager.registerType(regInfo.getExtensionPointType(),regInfo.getExtensionTypeImpl(),
//                            getApiId()
//                            , bootSession);
//                    iterator.remove();
//                    break;
//                }
//                case "net.thevpc.nuts.runtime.core.io.NutsFormattedPrintStream": {
//                    extensionManager.registerType(regInfo.getExtensionPointType(),regInfo.getExtensionTypeImpl(),
//                            getRuntimeId()
//                            , bootSession);
//                    iterator.remove();
//                    break;
//                }
//            }
//        }

        NutsSystemTerminalBase termb = extensions().createSupported(NutsSystemTerminalBase.class, null, bootSession);
        if (termb == null) {
            throw new NutsExtensionNotFoundException(this, NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        io().term().setSystemTerminal(termb, bootSession);
        io().term().setTerminal(io().term().createTerminal(bootSession), bootSession);
        NutsSession session = createSession();
        ((DefaultNutsLogger) LOG).resumeTerminal();

//        for (Iterator<DefaultNutsWorkspaceExtensionManager.RegInfo> iterator = regInfos.iterator(); iterator.hasNext(); ) {
//            DefaultNutsWorkspaceExtensionManager.RegInfo regInfo = iterator.next();
//            extensionManager.registerType(regInfo.getExtensionPointType(),re, session);
//            iterator.remove();
//        }
        configManager.onExtensionsPrepared(session);
        initializing = true;
        try {
            if (!loadWorkspace(session, uoptions.getExcludedExtensions(), null)) {
                //workspace wasn't loaded. Create new configuration...
                NutsWorkspaceUtils.of(this).checkReadOnly();
                LOG.with().level(Level.CONFIG).verb(NutsLogVerb.SUCCESS).log("Creating NEW workspace at {0}", locations().getWorkspaceLocation());
                NutsWorkspaceConfigBoot bconfig = new NutsWorkspaceConfigBoot();
                //load from config with resolution applied
                bconfig.setUuid(UUID.randomUUID().toString());
                NutsWorkspaceConfigApi aconfig = new NutsWorkspaceConfigApi();
                aconfig.setApiVersion(apiVersion);
                aconfig.setRuntimeId(runtimeId);
                aconfig.setJavaCommand(uoptions.getJavaCommand());
                aconfig.setJavaOptions(uoptions.getJavaOptions());

                NutsWorkspaceConfigRuntime rconfig = new NutsWorkspaceConfigRuntime();
                rconfig.setDependencies(String.join(";",info.getRuntimeBootDescriptor().getDependencies()));
                rconfig.setId(runtimeId);

                bconfig.setBootRepositories(repositories);
                bconfig.setStoreLocationStrategy(uoptions.getStoreLocationStrategy());
                bconfig.setRepositoryStoreLocationStrategy(uoptions.getRepositoryStoreLocationStrategy());
                bconfig.setStoreLocationLayout(uoptions.getStoreLocationLayout());
                bconfig.setGlobal(uoptions.isGlobal());
                bconfig.setStoreLocations(new NutsStoreLocationsMap(uoptions.getStoreLocations()).toMapOrNull());
                bconfig.setHomeLocations(new NutsHomeLocationsMap(uoptions.getHomeLocations()).toMapOrNull());

                boolean namedWorkspace = CoreNutsUtils.isValidWorkspaceName(uoptions.getWorkspace());
                if (bconfig.getStoreLocationStrategy() == null) {
                    bconfig.setStoreLocationStrategy(namedWorkspace ? NutsStoreLocationStrategy.EXPLODED : NutsStoreLocationStrategy.STANDALONE);
                }
                if (bconfig.getRepositoryStoreLocationStrategy() == null) {
                    bconfig.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
                }
                bconfig.setName(CoreNutsUtils.resolveValidWorkspaceName(uoptions.getWorkspace()));

                configManager.setCurrentConfig(new DefaultNutsWorkspaceCurrentConfig(this)
                        .merge(aconfig)
                        .merge(bconfig)
                        .build(locations().getWorkspaceLocation()));
                NutsUpdateOptions updateOptions = new NutsUpdateOptions().setSession(session);
                configManager.setConfigBoot(bconfig, updateOptions);
                configManager.setConfigApi(aconfig, updateOptions);
                configManager.setConfigRuntime(rconfig, updateOptions);
                initializeWorkspace(uoptions.getArchetype(), session);
                if (!config().isReadOnly()) {
                    config().save(session);
                }
                String nutsVersion = getRuntimeId().getVersion().toString();
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().level(Level.CONFIG).verb(NutsLogVerb.SUCCESS).log("nuts workspace v{0} created.", nutsVersion);
                }

                if (session.isPlainTrace()) {
                    PrintStream out = session.out();
                    out.printf("##nuts## workspace v###%s### created.%n", nutsVersion);
                }

                reconfigurePostInstall(session);
                DefaultNutsWorkspaceEvent workspaceCreatedEvent = new DefaultNutsWorkspaceEvent(session, null, null, null, null);
                for (NutsWorkspaceListener workspaceListener : events().getWorkspaceListeners()) {
                    workspaceListener.onCreateWorkspace(workspaceCreatedEvent);
                }
            } else {
                if (uoptions.isRecover()) {
                    NutsUpdateOptions updateOptions = new NutsUpdateOptions().setSession(session);
                    configManager.setBootApiVersion(cfg.getApiVersion(), updateOptions);
                    configManager.setBootRuntimeId(cfg.getRuntimeId(), updateOptions);
                    configManager.setBootRuntimeDependencies(String.join(";",cfg.getRuntimeBootDescriptor().getDependencies()), updateOptions);
                    configManager.setBootRepositories(cfg.getBootRepositories(), updateOptions);
                    try {
                        install().installed().getResult();
                    } catch (Exception ex) {
                        LOG.with().level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("Reinstall artifacts failed : " + CoreStringUtils.exceptionToString(ex));
                        LOG.with().level(Level.FINEST).verb(NutsLogVerb.FAIL).log("Reinstall artifacts failed : " + CoreStringUtils.exceptionToString(ex), ex);
                    }
                }
            }
            if (repos().getRepositoryRefs(session).length == 0) {
                LOG.with().level(Level.CONFIG).verb(NutsLogVerb.FAIL).log("Workspace has no repositories. Will re-create defaults");
                initializeWorkspace(uoptions.getArchetype(), session);
            }
            List<String> transientRepositoriesSet = uoptions.getTransientRepositories() == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(uoptions.getTransientRepositories()));
            for (String loc : transientRepositoriesSet) {
                String uuid = "transient_" + UUID.randomUUID().toString().replace("-", "");
                repos()
                        .addRepository(
                                new NutsAddRepositoryOptions()
                                        .setTemporary(true)
                                        .setName(uuid)
                                        .setFailSafe(false)
                                        .setLocation(loc)
                                        .setEnabled(true)
                        );
            }
            configManager.prepareBoot(false, session);
            if (!config().isReadOnly()) {
                config().save(false, session);
            }
            configManager.setStartCreateTimeMillis(uoptions.getCreationTime());
            configManager.setEndCreateTimeMillis(System.currentTimeMillis());
            if (uoptions.getUserName() != null && uoptions.getUserName().trim().length() > 0) {
                char[] password = uoptions.getCredentials();
                if (CoreStringUtils.isBlank(password)) {
                    password = io().term().getTerminal().readPassword("Password : ");
                }
                this.security().login(uoptions.getUserName(), password);
            }
            LOG.with().level(Level.FINE).verb(NutsLogVerb.SUCCESS)
                    .formatted().log("##nuts## workspace loaded in ```error {0}```",
                    CoreCommonUtils.formatPeriodMilli(config().getCreationFinishTimeMillis() - config().getCreationStartTimeMillis())
            );

            if (CoreCommonUtils.getSysBoolNutsProperty("perf", false)) {
                session.out().printf("##nuts## workspace loaded in #####%s#####%n",
                        CoreCommonUtils.formatPeriodMilli(config().getCreationFinishTimeMillis() - config().getCreationStartTimeMillis())
                );
            }
        } finally {
            initializing = false;
        }
//        return !exists;
    }

    private static String escapeText0(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\':
                case '_':
                case '=': {
                    sb.append('\\').append(c);
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    private NutsSession createBootSession() {
        NutsSession s = new DefaultNutsSession(this);
        DefaultNutsSessionTerminal t = new DefaultNutsSessionTerminal();
        DefaultNutsSystemTerminalBase base = new DefaultNutsSystemTerminalBase();
        base.setWorkspace(this);
        DefaultSystemTerminal sys = new DefaultSystemTerminal(base);
        sys.setWorkspace(this);
        t.setParent(sys);
        t.setWorkspace(this);
        s.setTerminal(t);
        return s;
    }

    public void reconfigurePostInstall(NutsSession session) {
        String nutsVersion = getRuntimeId().getVersion().toString();
        session = NutsWorkspaceUtils.of(this).validateSession(session);
        //should install default
        if (session.isPlainTrace() && !config().options().isSkipWelcome()) {
            PrintStream out = session.out();

            StringBuilder version = new StringBuilder(nutsVersion);
            CoreStringUtils.fillString(' ', 25 - version.length(), version);
            out.println(formats().text().loadFormattedString("/net/thevpc/nuts/includes/standard-header.help", getClass().getClassLoader(), "no help found"));
            out.println("###/------------------------------------------------------------------------------\\\\###");
            out.println("###|###  This is the very ###first### time ##nuts## has been started for this workspace...     ###|###");
            out.println("###\\\\------------------------------------------------------------------------------/###");
            out.println();
        }
//        NutsSession finalSession = session;
//        MavenUtils mvn = MavenUtils.of(DefaultNutsWorkspace.this);
        for (URL bootClassWorldURL : config().getBootClassWorldURLs()) {
            NutsDeployRepositoryCommand desc = getInstalledRepository().deploy()
                    .setContent(bootClassWorldURL)
                    //.setFetchMode(NutsFetchMode.LOCAL)
                    .setSession(session.copy().copy().setConfirm(NutsConfirmationMode.YES))
                    .run();
            if (desc.getId().getLongNameId().equals(getApiId().getLongNameId())
                    || desc.getId().getLongNameId().equals(getRuntimeId().getLongNameId())) {
                getInstalledRepository().install(desc.getId(), session, null);
            } else {
                getInstalledRepository().install(desc.getId(), session, getRuntimeId());
            }

//            try (InputStream is=bootClassWorldURL.openStream()){
//                ZipUtils.visitZipStream(is, new Predicate<String>() {
//                    @Override
//                    public boolean test(String path) {
//                        return path.startsWith("META-INF/maven/") && path.endsWith("/pom.xml");
//                    }
//                }, new InputStreamVisitor() {
//                    @Override
//                    public boolean visit(String path, InputStream inputStream) throws IOException {
//                        NutsDescriptor desc = mvn.parsePomXml(inputStream, NutsWorkspaceHelper.createNoRepositorySession(finalSession, NutsFetchMode.REMOTE), path);
//                        getInstalledRepository().deploy()
//                                .setDescriptor(desc)
//                                .setId(desc.getId())
//                                .setContent().setSession(finalSession)
//                                .run()
//                        return false;
//                    }
//                });
//            } catch (IOException ex) {
//                throw new UncheckedIOException(ex);
//            }
        }
        if (!config().options().isSkipCompanions()) {
            if (session.isPlainTrace()) {
                PrintStream out = session.out();
                Set<NutsId> companionIds = companionIds();
                out.println("looking for recommended companion tools to install... detected : " + companionIds.stream()
                        .map(x -> id().formatter(x).format()).collect(Collectors.toList())
                );
            }
            try {
                install().companions().setSession(session.copy().setTrace(session.isTrace() && session.isPlainOut()))
                        .addConditionalArgs(d->d.getId().getShortName().equals("net.thevpc.nuts:ndi")
                                && config().options().getSwitchWorkspace()!=null
                                ,"--switch="+config().options().getSwitchWorkspace())
                        .run();
            } catch (Exception ex) {
                LOG.with().level(Level.FINEST).verb(NutsLogVerb.WARNING).error(ex).log("Unable to install companions");
                if (session.isPlainTrace()) {
                    PrintStream out = session.out();
                    out.printf("```error unable to install companion tools```. This happens when none of the following repositories are able to locate them : %s\n",
                            new NutsString(Arrays.stream(repos().getRepositories(session)).map(x->"####"+x.getName()+"####").collect(Collectors.joining(", ")))
                    );
                }
            }
            if (session.isPlainTrace()) {
                PrintStream out = session.out();
                out.println("Workspace is ##ready##!");
            }
        }
    }

    protected NutsDescriptor _resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        LOG.with().level(Level.FINEST).verb(NutsLogVerb.START).formatted()
                .log("resolve effective {0}", descriptor.getId());
        session = NutsWorkspaceUtils.of(this).validateSession(session);
        NutsId[] parents = descriptor.getParents();
        NutsDescriptor[] parentDescriptors = new NutsDescriptor[parents.length];
        for (int i = 0; i < parentDescriptors.length; i++) {
            parentDescriptors[i] = resolveEffectiveDescriptor(
                    fetch().setId(parents[i]).setEffective(false).setSession(session).getResultDescriptor(),
                    session
            );
        }
        NutsDescriptor effectiveDescriptor = descriptor.builder().applyParents(parentDescriptors).applyProperties().build();
        NutsDependency[] oldDependencies = effectiveDescriptor.getDependencies();
        List<NutsDependency> newDeps = new ArrayList<>();
        boolean someChange = false;

        for (NutsDependency d : oldDependencies) {
            if (CoreStringUtils.isBlank(d.getScope())
                    || d.getVersion().isBlank()
                    || CoreStringUtils.isBlank(d.getOptional())) {
                NutsDependency standardDependencyOk = null;
                for (NutsDependency standardDependency : effectiveDescriptor.getStandardDependencies()) {
                    if (standardDependency.getSimpleName().equals(d.toId().getShortName())) {
                        standardDependencyOk = standardDependency;
                        break;
                    }
                }
                if (standardDependencyOk != null) {
                    if (CoreStringUtils.isBlank(d.getScope())
                            && !CoreStringUtils.isBlank(standardDependencyOk.getScope())) {
                        someChange = true;
                        d = d.builder().setScope(standardDependencyOk.getScope()).build();
                    }
                    if (CoreStringUtils.isBlank(d.getOptional())
                            && !CoreStringUtils.isBlank(standardDependencyOk.getOptional())) {
                        someChange = true;
                        d = d.builder().setOptional(standardDependencyOk.getOptional()).build();
                    }
                    if (d.getVersion().isBlank()
                            && !standardDependencyOk.getVersion().isBlank()) {
                        someChange = true;
                        d = d.builder().setVersion(standardDependencyOk.getVersion()).build();
                    }
                }
            }

            if ("import".equals(d.getScope())) {
                someChange = true;
                newDeps.addAll(Arrays.asList(fetch().setId(d.toId()).setEffective(true).setSession(session).getResultDescriptor().getDependencies()));
            } else {
                newDeps.add(d);
            }
        }
        if (someChange) {
            effectiveDescriptor = effectiveDescriptor.builder().setDependencies(newDeps.toArray(new NutsDependency[0])).build();
        }
        return effectiveDescriptor;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsWorkspaceOptions> criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public String toString() {
        return "NutsWorkspace{"
                + configManager
                + '}';
    }

    protected void initializeWorkspace(String archetype, NutsSession session) {
        session = NutsWorkspaceUtils.of(this).validateSession(session);
        if (CoreStringUtils.isBlank(archetype)) {
            archetype = "default";
        }
        NutsWorkspaceArchetypeComponent instance = null;
        TreeSet<String> validValues = new TreeSet<>();
        for (NutsWorkspaceArchetypeComponent ac : extensions().createAllSupported(NutsWorkspaceArchetypeComponent.class, archetype, session)) {
            if (archetype.equals(ac.getName())) {
                instance = ac;
                break;
            }
            validValues.add(ac.getName());
        }
        if (instance == null) {
            //get the default implementation
            throw new NutsException(this, "Invalid archetype " + archetype + ". Valid values are : " + validValues);
        }

        //has all rights (by default)
        //no right nor group is needed for admin user
        security().updateUser(NutsConstants.Users.ADMIN).setCredentials("admin".toCharArray()).run();

        instance.initialize(session);

//        //isn't it too late for adding extensions?
//        try {
//            addWorkspaceExtension(NutsConstants.NUTS_ID_BOOT_RUNTIME, session);
//        } catch (Exception ex) {
//            log.log(Level.SEVERE, "Unable to loadWorkspace nuts-runtime. The tool is running in minimal mode.");
//        }
    }

    public void installOrUpdateImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session,
                                    boolean resolveInstaller, boolean updateDefaultVersion, InstallStrategy0 strategy0, boolean requireDependencies, NutsId[] forIds,
                                    NutsDependencyScope scope) {
        if (def == null) {
            return;
        }
        NutsDependencyFilter ndf = dependency().filter().byScope(NutsDependencyScopePattern.RUN);
        def.getContent();
        def.getEffectiveDescriptor();
        if (def.getInstallInformation() == null) {
            throw new NutsIllegalArgumentException(this, "Missing Install Information");
        }
        boolean reinstall = false;
        if (session.isPlainTrace()) {
            if (strategy0 == InstallStrategy0.UPDATE) {
                session.out().println("updating " + id().formatter(def.getId().getLongNameId()).format() + " ...");
            } else if (strategy0 == InstallStrategy0.REQUIRE) {
                reinstall = def.getInstallInformation().getInstallStatus()
                        .contains(NutsInstallStatus.REQUIRED);
                if (reinstall) {
                    //session.out().println("re-requiring  " + id().formatter().set(def.getId().getLongNameId()).format() + " ...");
                } else {
                    //session.out().println("requiring  " + id().formatter().set(def.getId().getLongNameId()).format() + " ...");
                }
            } else {
                reinstall = def.getInstallInformation().getInstallStatus()
                        .contains(NutsInstallStatus.INSTALLED);
                if (reinstall) {
                    session.out().println("re-installing " + id().formatter(def.getId().getLongNameId()).format() + " ...");
                } else {
                    session.out().println("installing " + id().formatter(def.getId().getLongNameId()).format() + " ...");
                }
            }
        }
        if (resolveInstaller) {
            if (installerComponent == null) {
                if (def.getPath() != null) {
                    installerComponent = getInstaller(def, session);
                }
            }
        }
        session = NutsWorkspaceUtils.of(this).validateSession(session);
        NutsDefinition oldDef = null;
        if (strategy0 == InstallStrategy0.UPDATE) {
            switch (def.getType()) {
                case API: {
                    oldDef = fetch().setSession(CoreNutsUtils.silent(session)).setId(NutsConstants.Ids.NUTS_API + "#" + Nuts.getVersion()).setOnline().setFailFast(false).getResultDefinition();
                    break;
                }
                case RUNTIME: {
                    oldDef = fetch().setSession(CoreNutsUtils.silent(session)).setId(getRuntimeId()).setOnline().setFailFast(false).getResultDefinition();
                    break;
                }
                default: {
                    oldDef = search().setSession(CoreNutsUtils.silent(session)).addId(def.getId().getShortNameId())
                            .addInstallStatus(NutsInstallStatus.INSTALLED)
                            .addInstallStatus(NutsInstallStatus.REQUIRED)
                            .setFailFast(false).getResultDefinitions().first();
                    break;
                }
            }
        }
        NutsSession traceSession = session;
        PrintStream out = traceSession.out();
        out.flush();
        switch (def.getType()) {
            case API: {
                configManager.prepareBootApi(def.getId(), null, true, session);
                break;
            }
            case RUNTIME: {
                configManager.prepareBootRuntime(def.getId(), true, session);
                break;
            }
            case EXTENSION: {
                configManager.prepareBootExtension(def.getId(), true, session);
                break;
            }
        }
        NutsInstallInformation newNutsInstallInformation = null;
        if (def.getPath() != null) {
            NutsExecutionContextBuilder cc = createExecutionContext()
                    .setDefinition(def).setArguments(args).setFailFast(true).setTemporary(false)
                    .setExecutionType(config().options().getExecutionType());
            NutsArtifactCall installer = def.getDescriptor().getInstaller();
            if(installer!=null) {
                cc.addExecutorArguments(installer.getArguments());
                cc.addExecutorProperties(installer.getProperties());
            }
            NutsExecutionContext executionContext = cc.build();
//            NutsInstallInformation iinfo = null;
            if (strategy0 == InstallStrategy0.REQUIRE) {
                newNutsInstallInformation = getInstalledRepository().require(executionContext.getDefinition(), true, forIds, scope, traceSession);
            } else if (strategy0 == InstallStrategy0.UPDATE) {
                newNutsInstallInformation = getInstalledRepository().install(executionContext.getDefinition(), traceSession);
            } else if (strategy0 == InstallStrategy0.INSTALL) {
                newNutsInstallInformation = getInstalledRepository().install(executionContext.getDefinition(), traceSession);
            }

            if (requireDependencies) {
                def.getDependencies();
                for (NutsDependency dependency : def.getDependencies()) {
                    if (ndf == null || ndf.acceptDependency(def.getId(), dependency, traceSession)) {
                        if (!getInstalledRepository().
                                searchVersions().setId(dependency.toId())
                                .setFetchMode(NutsFetchMode.LOCAL)
                                .setSession(traceSession)
                                .getResult()
                                .hasNext()) {
                            NutsDefinition dd = search().addId(dependency.toId()).setContent(true).setLatest(true)
                                    //.setDependencies(true)
                                    .setEffective(true)
                                    .getResultDefinitions().first();
                            if (dd != null) {
                                getInstalledRepository().deploy()
                                        .setId(dd.getId())
                                        .setContent(dd.getPath())
                                        //.setFetchMode(NutsFetchMode.LOCAL)
                                        .setSession(traceSession)
                                        .setDescriptor(dd.getDescriptor())
                                        .run();
                                requireImpl(dd,
                                        traceSession,
                                        false, //transitive dependencies already evaluated
                                        new NutsId[]{def.getId()});
                            }
                        }
                    }

                }
            }

            if (strategy0 == InstallStrategy0.REQUIRE) {
                //
            } else if (strategy0 == InstallStrategy0.UPDATE) {
                if (installerComponent != null) {
                    try {
                        installerComponent.update(executionContext);
                    } catch (NutsReadOnlyException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        if (session.isPlainTrace()) {
                            out.printf(id().formatter().value(def.getId()).format() + " ```error failed``` to update : %s.%n", CoreStringUtils.exceptionToString(ex));
                        }
                        throw new NutsExecutionException(this, "unable to update " + def.getId().toString(), ex);
                    }
                }
            } else if (strategy0 == InstallStrategy0.INSTALL) {
                if (installerComponent != null) {
                    try {
                        installerComponent.install(executionContext);
//                    out.print(getFormatManager().parse().print(def.getId()) + " installed ##successfully##.\n");
                    } catch (NutsReadOnlyException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        if (session.isPlainTrace()) {
                            out.printf("```error error: failed to install``` "+id().formatter().value(def.getId()).format()+": %s.%n", CoreStringUtils.exceptionToString(ex));
                        }
                        try {
                            getInstalledRepository().uninstall(executionContext.getDefinition().getId(), session);
                        } catch (Exception ex2) {
                            LOG.with().level(Level.FINE).error(ex).log("failed to uninstall  {0}", executionContext.getDefinition().getId());
                            //ignore if we could not uninstall
                        }
                        throw new NutsExecutionException(this, "unable to install " + def.getId().toString(), ex);
                    }
                }
            }
        }

//        if (def.getInstallInformation() instanceof DefaultNutsInstallInfo) {
//            DefaultNutsInstallInfo t = (DefaultNutsInstallInfo) def.getInstallInformation();
//            t.setJustInstalled(true);
//            t.setJustReInstalled(reinstall);
//        }
        if (updateDefaultVersion) {
            getInstalledRepository().setDefaultVersion(def.getId(), session);
        }

        if (strategy0 == InstallStrategy0.UPDATE) {
            NutsWorkspaceUtils.of(this).events().fireOnUpdate(new DefaultNutsUpdateEvent(oldDef, def, session, reinstall));
        } else if (strategy0 == InstallStrategy0.REQUIRE) {
            NutsWorkspaceUtils.of(this).events().fireOnRequire(new DefaultNutsInstallEvent(def, session, forIds, reinstall));
        } else if (strategy0 == InstallStrategy0.INSTALL) {
            NutsWorkspaceUtils.of(this).events().fireOnInstall(new DefaultNutsInstallEvent(def, session, new NutsId[0], reinstall));
        }

        if (def.getType() == NutsIdType.EXTENSION) {
            NutsWorkspaceConfigManagerExt wcfg = NutsWorkspaceConfigManagerExt.of(config());
            NutsExtensionListHelper h = new NutsExtensionListHelper(wcfg.getStoredConfigBoot().getExtensions())
                    .save();
            h.add(def.getId());
            wcfg.getStoredConfigBoot().setExtensions(h.getConfs());
            wcfg.fireConfigurationChanged("extensions", session, ConfigEventType.BOOT);
            wcfg.prepareBootExtension(def.getId(), true, session);
        }

        if (session.isPlainTrace()) {
            String setAsDefaultString = "";
            if (updateDefaultVersion) {
                setAsDefaultString = " set as ##default##.";
            }
            if (
                    newNutsInstallInformation != null &&
                            (newNutsInstallInformation.isJustInstalled()
                                    || newNutsInstallInformation.isJustRequired())
            ) {
                String installedString = null;
                if (newNutsInstallInformation != null) {
                    if (newNutsInstallInformation.isJustReInstalled()) {
                        installedString = "re-installed ";
                    } else if (newNutsInstallInformation.isJustInstalled()) {
                        installedString = "installed ";
                    } else if (newNutsInstallInformation.isJustReRequired()) {
                        installedString = "re-required  ";
                    } else if (newNutsInstallInformation.isJustRequired()) {
                        installedString = "required  ";
                    }
                }
                if (installedString != null) {
                    //(reinstalled ? "re-installed" : "installed")
                    if (!def.getContent().isCached()) {
                        if (def.getContent().isTemporary()) {
                            if (session.isPlainTrace()) {
                                out.printf("%s %s ##successfully## from remote repository (%s) temporarily file %s.%s%n", installedString,
                                        new NutsString(id().formatter().value(def.getId().getLongNameId()).format()),
                                        def.getRepositoryName(),
                                        def.getPath(), new NutsString(setAsDefaultString));
                            }
                        } else {
                            if (session.isPlainTrace()) {
                                out.printf("%s %s ##successfully## from remote repository (%s).%s%n", installedString,
                                        new NutsString(id().formatter().value(def.getId().getLongNameId()).format()),
                                        def.getRepositoryName(),
                                        new NutsString(setAsDefaultString));
                            }
                        }
                    } else {
                        if (def.getContent().isTemporary()) {
                            if (session.isPlainTrace()) {
                                out.printf("%s %s from local repository (%s) temporarily file %s.%s%n", installedString,
                                        new NutsString(id().formatter().value(def.getId().getLongNameId()).format()),
                                        def.getRepositoryName(),
                                        def.getPath(), new NutsString(setAsDefaultString));
                            }
                        } else {
                            if (session.isPlainTrace()) {
                                out.printf("%s %s from local repository (%s).%s%n", installedString,
                                        new NutsString(id().formatter().value(def.getId().getLongNameId()).format()),
                                        def.getRepositoryName(),
                                        new NutsString(setAsDefaultString)
                                );
                            }
                        }
                    }
                }
            } else {
                String installedString = null;
                if (newNutsInstallInformation != null) {
                    if (newNutsInstallInformation.isJustReInstalled()) {
                        installedString = "re-installed";
                    } else if (newNutsInstallInformation.isJustInstalled()) {
                        installedString = "installed";
                    } else if (newNutsInstallInformation.isJustReRequired()) {
                        installedString = "re-required";
                    } else if (newNutsInstallInformation.isJustRequired()) {
                        installedString = "required";
                    }
                }
                if (installedString != null) {
                    if (session.isPlainTrace()) {
                        out.printf("%s  %s ##successfully##.%s%n", installedString, new NutsString(id().formatter().value(def.getId().getLongNameId()).format()), new NutsString(setAsDefaultString));
                    }
                }
            }
        }
    }

    public String resolveCommandName(NutsId id, NutsSession session) {
        String nn = id.getArtifactId();
        NutsWorkspaceCommandAlias c = aliases().find(nn, session);
        if (c != null) {
            if (CoreNutsUtils.matchesSimpleNameStaticVersion(c.getOwner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getArtifactId() + "-" + id.getVersion();
        c = aliases().find(nn, session);
        if (c != null) {
            if (CoreNutsUtils.matchesSimpleNameStaticVersion(c.getOwner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        nn = id.getGroupId() + "." + id.getArtifactId() + "-" + id.getVersion();
        c = aliases().find(nn, session);
        if (c != null) {
            if (CoreNutsUtils.matchesSimpleNameStaticVersion(c.getOwner(), id)) {
                return nn;
            }
        } else {
            return nn;
        }
        throw new NutsElementNotFoundException(this, "Unable to resolve command name for " + id.toString());
    }

    protected boolean loadWorkspace(NutsSession session, String[] excludedExtensions, String[] excludedRepositories) {
        session = NutsWorkspaceUtils.of(this).validateSession(session);
        if (configManager.loadWorkspace(session)) {
            //extensions already wired... this is needless!
            for (NutsId extensionId : extensions().getConfigExtensions(session)) {
                if (extensionManager.isExcludedExtension(extensionId)) {
                    continue;
                }
                NutsSession sessionCopy = session.copy();
                extensionManager.wireExtension(extensionId,
                        fetch().setSession(sessionCopy)
                                .setFetchStrategy(NutsFetchStrategy.ONLINE)
                                .setTransitive(true)
                );
                if (sessionCopy.getTerminal() != session.getTerminal()) {
                    session.setTerminal(sessionCopy.getTerminal());
                }
            }
            NutsUserConfig adminSecurity = NutsWorkspaceConfigManagerExt.of(config()).getUser(NutsConstants.Users.ADMIN);
            if (adminSecurity == null || CoreStringUtils.isBlank(adminSecurity.getCredentials())) {
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().level(Level.CONFIG).verb(NutsLogVerb.FAIL).log(NutsConstants.Users.ADMIN + " user has no credentials. reset to default");
                }
                security().updateUser(NutsConstants.Users.ADMIN).credentials("admin".toCharArray()).setSession(session).run();
            }
            for (NutsCommandAliasFactoryConfig commandFactory : aliases().getFactories(session)) {
                try {
                    aliases().addFactory(commandFactory, new NutsAddOptions().setSession(session));
                } catch (Exception e) {
                    LOG.with().level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("Unable to instantiate Command Factory {0}", commandFactory);
                }
            }
            DefaultNutsWorkspaceEvent worksppaeReloadedEvent = new DefaultNutsWorkspaceEvent(session, null, null, null, null);
            for (NutsWorkspaceListener listener : events().getWorkspaceListeners()) {
                listener.onReloadWorkspace(worksppaeReloadedEvent);
            }
            //if save is needed, will be applied
            //config().save(false, session);
            return true;
        }
        return false;
    }

    @Override
    public NutsSearchCommand search() {
        return new DefaultNutsSearchCommand(this);
    }

    @Override
    public NutsFetchCommand fetch() {
        return new DefaultNutsFetchCommand(this);
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
    public NutsExecCommand exec() {
        return new DefaultNutsExecCommand(this);
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

    @Override
    public Set<NutsId> companionIds() {
        return Collections.unmodifiableSet(
                new HashSet<>(
                        Arrays.asList(
                                id().parser().parse("net.thevpc.nuts.toolbox:nsh"),
                                id().parser().parse("net.thevpc.nuts.toolbox:nadmin"),
                                id().parser().parse("net.thevpc.nuts.toolbox:ndi")
                                //            "net.thevpc.nuts.toolbox:mvn"
                        )
                )
        );
    }

    @Override
    public NutsFilterManager filters() {
        return filters;
    }

    @Override
    public NutsLogManager log() {
        return logCmd;
    }

    @Override
    public String getWelcomeText() {
        return this.formats().text().loadFormattedString("/net/thevpc/nuts/nuts-welcome.help", getClass().getClassLoader(), "no welcome found");
    }

    @Override
    public String getHelpText() {
        return this.formats().text().loadFormattedString("/net/thevpc/nuts/nuts-help.help", getClass().getClassLoader(), "no help found");
    }

    @Override
    public String getLicenseText() {
        return this.formats().text().loadFormattedString("/net/thevpc/nuts/nuts-license.help", getClass().getClassLoader(), "no license found");
    }

    @Override
    public String resolveDefaultHelp(Class clazz) {
        NutsId nutsId = id().resolveId(clazz);
        if (nutsId != null) {
            String urlPath = "/" + nutsId.getGroupId().replace('.', '/') + "/" + nutsId.getArtifactId() + ".help";
            return formats().text().loadFormattedString(urlPath, clazz.getClassLoader(), "no help found");
        }
        return null;
    }

    @Override
    public NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsSession session) {
        session = NutsWorkspaceUtils.of(this).validateSession(session);
        if (descriptor == null) {
            throw new NutsNotFoundException(this, "<null>");
        }
        NutsId thisId = descriptor.getId();
//        if (CoreNutsUtils.isEffectiveId(thisId)) {
//            return thisId.setAlternative(descriptor.getAlternative());
//        }
        String a = thisId.getArtifactId();
        String g = thisId.getGroupId();
        String v = thisId.getVersion().getValue();
        if ((CoreStringUtils.isBlank(g)) || (CoreStringUtils.isBlank(v))) {
            NutsId[] parents = descriptor.getParents();
            for (NutsId parent : parents) {
                NutsId p = fetch().setSession(session).setId(parent).setEffective(true).getResultId();
                if (CoreStringUtils.isBlank(g)) {
                    g = p.getGroupId();
                }
                if (CoreStringUtils.isBlank(v)) {
                    v = p.getVersion().getValue();
                }
                if (!CoreStringUtils.isBlank(g) && !CoreStringUtils.isBlank(v)) {
                    break;
                }
            }
            if (CoreStringUtils.isBlank(g) || CoreStringUtils.isBlank(v)) {
                throw new NutsNotFoundException(this, thisId, "Unable to fetchEffective for " + thisId + ". Best Result is " + thisId.toString(), null);
            }
        }
        if (CoreStringUtils.containsVars(g) || CoreStringUtils.containsVars(v) || CoreStringUtils.containsVars(a)) {
            Map<String, String> p = descriptor.getProperties();
            NutsId bestId = new DefaultNutsId(null, g, thisId.getArtifactId(), v, "");
            bestId = bestId.builder().apply(new MapStringMapper(p)).build();
            if (CoreNutsUtils.isEffectiveId(bestId)) {
                return bestId;
            }
            Stack<NutsId> all = new Stack<>();
            NutsId[] parents = descriptor.getParents();
            all.addAll(Arrays.asList(parents));
            while (!all.isEmpty()) {
                NutsId parent = all.pop();
                NutsDescriptor dd = fetch().setSession(session).setId(parent).setEffective(true).getResultDescriptor();
                bestId = bestId.builder().apply(new MapStringMapper(dd.getProperties())).build();
                if (CoreNutsUtils.isEffectiveId(bestId)) {
                    return bestId;
                }
                all.addAll(Arrays.asList(dd.getParents()));
            }
            throw new NutsNotFoundException(this, bestId.toString(), "Unable to fetchEffective for " + thisId + ". Best Result is " + bestId.toString(), null);
        }
        NutsId bestId = new DefaultNutsId(null, g, thisId.getArtifactId(), v, "");
        if (!CoreNutsUtils.isEffectiveId(bestId)) {
            throw new NutsNotFoundException(this, bestId.toString(), "Unable to fetchEffective for " + thisId + ". Best Result is " + bestId.toString(), null);
        }
//        return bestId.setAlternative(descriptor.getAlternative());
        return bestId;
    }

    @Override
    public NutsIdType resolveNutsIdType(NutsId id) {
        NutsIdType idType = NutsIdType.REGULAR;
        String shortName = id.getShortName();
        if (shortName.equals(NutsConstants.Ids.NUTS_API)) {
            idType = NutsIdType.API;
        } else if (shortName.equals(NutsConstants.Ids.NUTS_RUNTIME)) {
            idType = NutsIdType.RUNTIME;
        } else {
            for (NutsId companionTool : companionIds()) {
                if (companionTool.getShortName().equals(shortName)) {
                    idType = NutsIdType.COMPANION;
                }
            }
        }
        return idType;
    }

    @Override
    public NutsInstallerComponent getInstaller(NutsDefinition nutToInstall, NutsSession session) {
        session = NutsWorkspaceUtils.of(this).validateSession(session);
        if (nutToInstall != null && nutToInstall.getPath() != null) {
            NutsDescriptor descriptor = nutToInstall.getDescriptor();
            NutsArtifactCall installerDescriptor = descriptor.getInstaller();
            NutsDefinition runnerFile = nutToInstall;
            if (installerDescriptor != null && installerDescriptor.getId() != null) {
                if (installerDescriptor.getId() != null) {
                    runnerFile = fetch().setId(installerDescriptor.getId()).setSession(session)
                            .setTransitive(false)
                            .setOptional(false)
                            .setContent(true)
                            .setDependencies(true)
                            .getResultDefinition();
                }
            }
            if (runnerFile == null) {
                runnerFile = nutToInstall;
            }
            NutsInstallerComponent best = extensions().createSupported(NutsInstallerComponent.class, runnerFile, session);
            if (best != null) {
                return best;
            }
        }
        return new CommandForIdNutsInstallerComponent();
    }

    @Override
    public void requireImpl(NutsDefinition def, NutsSession session, boolean withDependencies, NutsId[] forId) {
        installOrUpdateImpl(def, new String[0], null, session, true, false, InstallStrategy0.REQUIRE, withDependencies, forId, null);
    }

    @Override
    public void installImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean updateDefaultVersion) {
        installOrUpdateImpl(def, args, installerComponent, session, true, updateDefaultVersion, InstallStrategy0.INSTALL, true, null, null);
    }

    @Override
    public void updateImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean updateDefaultVersion) {
        installOrUpdateImpl(def, args, installerComponent, session, true, updateDefaultVersion, InstallStrategy0.UPDATE, true, null, null);
    }

    /**
     * true when core extension is required for running this workspace. A
     * default implementation should be as follow, but developers may implements
     * this with other logic : core extension is required when there are no
     * extensions or when the
     * <code>NutsConstants.ENV_KEY_EXCLUDE_RUNTIME_EXTENSION</code> is forced to
     * false
     *
     * @param session session
     * @return true when core extension is required for running this workspace
     */
    @Override
    public boolean requiresRuntimeExtension(NutsSession session) {
        boolean coreFound = false;
        for (NutsId ext : extensions().getConfigExtensions(session)) {
            if (ext.equalsShortName(getRuntimeId())) {
                coreFound = true;
                break;
            }
        }
        return !coreFound;
    }

    @Override
    public NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session) {
        Path eff = null;
        if (!descriptor.getId().getVersion().isBlank() && descriptor.getId().getVersion().isSingleValue() && descriptor.getId().toString().indexOf('$') < 0) {
            Path l = locations().getStoreLocation(descriptor.getId(), NutsStoreLocation.CACHE);
            String nn = locations().getDefaultIdFilename(descriptor.getId().builder().setFace("eff-nuts.cache").build());
            eff = l.resolve(nn);
            if (Files.isRegularFile(eff)) {
                try {
                    NutsDescriptor d = descriptor().parser().parse(eff);
                    if (d != null) {
                        return d;
                    }
                } catch (Exception ex) {
                    LOG.with().level(Level.FINE).error(ex).log("failed to parse {0}", eff);
                    //
                }
            }
        } else {
            //
        }
        NutsDescriptor effectiveDescriptor = _resolveEffectiveDescriptor(descriptor, session);
        if (eff == null) {
            Path l = locations().getStoreLocation(effectiveDescriptor.getId(), NutsStoreLocation.CACHE);
            String nn = locations().getDefaultIdFilename(effectiveDescriptor.getId().builder().setFace("eff-nuts.cache").build());
            eff = l.resolve(nn);
        }
        try {
            descriptor().formatter(effectiveDescriptor).print(eff);
        } catch (Exception ex) {
            LOG.with().level(Level.FINE).error(ex).log("failed to print {0}", eff);
            //
        }
        return effectiveDescriptor;
    }

    @Override
    public NutsInstalledRepository getInstalledRepository() {
        return installedRepository;
    }

    @Override
    public Set<NutsInstallStatus> getInstallStatus(NutsId id, boolean checkDependencies, NutsSession session) {
        session = NutsWorkspaceUtils.of(this).validateSilentSession(session);
        NutsDefinition nutToInstall;
        try {
            nutToInstall = search().addId(id).setSession(session).setTransitive(false)
                    .setInlineDependencies(checkDependencies)
                    .addInstallStatus(NutsInstallStatus.INSTALLED)
                    .addInstallStatus(NutsInstallStatus.REQUIRED)
                    .setOptional(false)
                    .getResultDefinitions().first();
            if (nutToInstall == null) {
                return EnumSet.of(NutsInstallStatus.NOT_INSTALLED);
            }
        } catch (NutsNotFoundException e) {
            return EnumSet.of(NutsInstallStatus.NOT_INSTALLED);
        } catch (Exception ex) {
            LOG.with().level(Level.SEVERE).error(ex).log("error: "+CoreStringUtils.exceptionToString(ex));
            return EnumSet.of(NutsInstallStatus.NOT_INSTALLED);
        }
        return getInstalledRepository().getInstallStatus(nutToInstall.getId(), session);
    }

    @Override
    public NutsExecutionContextBuilder createExecutionContext(){
        return new DefaultNutsExecutionContextBuilder().setWorkspace(this);
    }
//    public NutsExecutionContext createNutsExecutionContext(
//            NutsDefinition def,
//            String[] args,
//            String[] executorArgs,
//            NutsSession traceSession,
//            NutsSession execSession,
//            boolean failFast,
//            boolean temporary,
//            NutsExecutionType executionType,
//            String commandName
//            ,long sleepMillis
//    ) {
//        if (commandName == null) {
//            commandName = resolveCommandName(def.getId(), traceSession);
//        }
//        NutsDescriptor descriptor = def.getDescriptor();
//        NutsArtifactCall installer = descriptor.getInstaller();
//        List<String> eargs = new ArrayList<>();
//        List<String> aargs = new ArrayList<>();
//        Map<String, String> props = null;
//        if (installer != null) {
//            if (installer.getArguments() != null) {
//                eargs.addAll(Arrays.asList(installer.getArguments()));
//            }
//            props = installer.getProperties();
//        }
//        if (executorArgs != null) {
//            eargs.addAll(Arrays.asList(executorArgs));
//        }
//        if (args != null) {
//            aargs.addAll(Arrays.asList(args));
//        }
//        Path installFolder = locations().getStoreLocation(def.getId(), NutsStoreLocation.APPS);
//        Map<String, String> env = new LinkedHashMap<>();
//        return new DefaultNutsExecutionContext(def, aargs.toArray(new String[0]), eargs.toArray(new String[0]), env, props, installFolder.toString(),
//                traceSession,
//                execSession, this, failFast, temporary, executionType, commandName,sleepMillis);
//    }

    @Override
    public void deployBoot(NutsSession session, NutsId id, boolean withDependencies) {
        Map<NutsId, NutsDefinition> defs = new HashMap<>();
        NutsDefinition m = fetch().setId(id).setContent(true).setDependencies(true).setFailFast(false).getResultDefinition();
        Map<String, String> a = new LinkedHashMap<>();
        a.put("configVersion", Nuts.getVersion());
        a.put("id", id.getLongName());
        a.put("dependencies", Arrays.stream(m.getDependencies()).map(NutsDependency::getLongName).collect(Collectors.joining(";")));
        defs.put(m.getId().getLongNameId(), m);
        if (withDependencies) {
            for (NutsDependency dependency : m.getDependencies()) {
                if (!defs.containsKey(dependency.toId().getLongNameId())) {
                    m = fetch().setId(id).setContent(true).setDependencies(true).setFailFast(false).getResultDefinition();
                    defs.put(m.getId().getLongNameId(), m);
                }
            }
        }
        for (NutsDefinition def : defs.values()) {
            Path bootstrapFolder = locations().getStoreLocation(NutsStoreLocation.LIB).resolve(NutsConstants.Folders.ID);
            NutsId id2 = def.getId();
            this.io().copy().setSession(session).from(def.getPath())
                    .to(bootstrapFolder.resolve(locations().getDefaultIdBasedir(id2))
                            .resolve(locations().getDefaultIdFilename(id2.builder().setFaceContent().setPackaging("jar").build()))
                    ).run();
            this.descriptor().formatter(this.fetch().setId(id2).getResultDescriptor())
                    .print(bootstrapFolder.resolve(locations().getDefaultIdBasedir(id2))
                            .resolve(locations().getDefaultIdFilename(id2.builder().setFaceDescriptor().build())));

            Map<String, String> pr = new LinkedHashMap<>();
            pr.put("file.updated.date", Instant.now().toString());
            pr.put("project.id", def.getId().getShortNameId().toString());
            pr.put("project.name", def.getId().getShortNameId().toString());
            pr.put("project.version", def.getId().getVersion().toString());
            pr.put("repositories", "~/.m2/repository;https\\://raw.githubusercontent.com/thevpc/vpc-public-maven/master;https\\://repo.maven.apache.org/maven2/;https\\://raw.githubusercontent.com/thevpc/vpc-public-nuts/master");
//            pr.put("bootRuntimeId", runtimeUpdate.getAvailable().getId().getLongName());
            pr.put("project.dependencies.compile",
                    String.join(";",
                            Arrays.stream(def.getDependencies())
                                    .filter(x -> !x.isOptional() && NutsDependencyScopes.SCOPE_RUN(DefaultNutsWorkspace.this).acceptDependency(def.getId(), x, session))
                                    .map(x -> x.toId().getLongName())
                                    .collect(Collectors.toList())
                    )
            );

            try (Writer writer = Files.newBufferedWriter(
                    bootstrapFolder.resolve(this.locations().getDefaultIdBasedir(def.getId().getLongNameId()))
                            .resolve("nuts.properties")
            )) {
                CoreIOUtils.storeProperties(pr, writer, false);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    public enum InstallStrategy0 {
        INSTALL,
        UPDATE,
        REQUIRE,
    }
    //    /**
//     * creates a zip file based on the folder. The folder should contain a
//     * descriptor file at its root
//     *
//     * @return bundled nuts file, the nuts is neither deployed nor installed!
//     */
//    @Derecated
//    public NutsDefinition createBundle(Path contentFolder, Path destFile, NutsQueryOptions queryOptions, NutsSession session) {
//        session = CoreNutsUtils.validateSession(session, this);
//        if (Files.isDirectory(contentFolder)) {
//            NutsDescriptor descriptor = null;
//            Path ext = contentFolder.resolve(NutsConstants.NUTS_DESC_FILE_NAME);
//            if (Files.exists(ext)) {
//                descriptor = parse().descriptor(ext);
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
//    public boolean isFetched(NutsId parse, NutsSession session) {
//        session = CoreNutsUtils.validateSession(session, this);
//        NutsSession offlineSession = session.copy();
//        try {
//            NutsDefinition found = fetch().parse(parse).offline().setSession(offlineSession).setIncludeInstallInformation(false).setIncludeFile(true).getResultDefinition();
//            return found != null;
//        } catch (Exception e) {
//            return false;
//        }
//    }
    private static Set<String> toIds(NutsBootDescriptor[] all){
        Set<String> set=new LinkedHashSet<>();
        for (NutsBootDescriptor i : all) {
            set.add(i.getId());
            Collections.addAll(set, i.getDependencies());
        }
        return set;
    }
}
