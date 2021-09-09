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
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsBootWorkspaceFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * NutsBootWorkspace is responsible of loading initial nuts-runtime.jar and its
 * dependencies and for creating workspaces using the method
 * {@link #openWorkspace()} . NutsBootWorkspace is also responsible of managing
 * local jar cache folder located at ~/.cache/nuts/default-workspace/boot
 * <br>
 * Default Bootstrap implementation. This class is responsible of loading
 * initial nuts-runtime.jar and its dependencies and for creating workspaces
 * using the method {@link #openWorkspace()}.
 * <br>
 *
 * @author thevpc
 * @app.category SPI Base
 * @since 0.5.4
 */
public final class NutsBootWorkspace {

    private static String apiDigest;
    private final long creationTime = System.currentTimeMillis();
    private final NutsWorkspaceOptions options;
    private final PrivateNutsLog LOG = new PrivateNutsLog();
    private Supplier<ClassLoader> contextClassLoaderSupplier;
    private int newInstanceRequirements;
    private PrivateNutsWorkspaceInitInformation workspaceInformation;
    private final Function<String, String> pathExpansionConverter = new Function<String, String>() {
        @Override
        public String apply(String from) {
            switch (from) {
                case "workspace":
                    return workspaceInformation.getWorkspaceLocation();
                case "user.home":
                    return System.getProperty("user.home");
                case "home.apps":
                case "home.config":
                case "home.lib":
                case "home.temp":
                case "home.var":
                case "home.cache":
                case "home.run":
                case "home.log":
                    return PrivateNutsUtils.getHome(NutsStoreLocation.valueOf(from.substring("home.".length()).toUpperCase()), workspaceInformation);
                case "apps":
                case "config":
                case "lib":
                case "cache":
                case "run":
                case "temp":
                case "log":
                case "var": {
                    Map<String, String> s = workspaceInformation.getStoreLocations();
                    if (s == null) {
                        return "${" + from + "}";
                    }
                    return s.get(from);
                }
            }
            return "${" + from + "}";
        }
    };
    private PrivateNutsWorkspaceInitInformation lastWorkspaceInformation;
    private Set<String> parsedBootRuntimeDependenciesRepositories;
    private Set<String> parsedBootRuntimeRepositories;
    private boolean preparedWorkspace;
    private NutsLogger LOG2;

    public NutsBootWorkspace(String... args) {
        this(NutsWorkspaceOptionsBuilder.of().parseArguments(args).build());
    }

    public NutsBootWorkspace(NutsWorkspaceOptions options) {
        if (options == null) {
            this.options = NutsWorkspaceOptionsBuilder.of().setCreationTime(creationTime).build();
        } else if (options.getCreationTime() == 0) {
            NutsWorkspaceOptionsBuilder copy = options.builder();
            copy.setCreationTime(creationTime);
            this.options = copy.build();
        } else {
            this.options = options;
        }
        LOG.setOptions(options);
        newInstanceRequirements = 0;
    }

    private static void revalidateLocations(PrivateNutsWorkspaceInitInformation workspaceInformation, String workspaceName, boolean immediateLocation) {
        if (NutsUtilStrings.isBlank(workspaceInformation.getName())) {
            workspaceInformation.setName(workspaceName);
        }
        Map<String, String> homeLocations = workspaceInformation.getHomeLocations();
        if (workspaceInformation.getStoreLocationStrategy() == null) {
            workspaceInformation.setStoreLocationStrategy(
                    immediateLocation ? NutsStoreLocationStrategy.EXPLODED : NutsStoreLocationStrategy.STANDALONE
            );
        }
        if (workspaceInformation.getRepositoryStoreLocationStrategy() == null) {
            workspaceInformation.setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy.EXPLODED);
        }
        String workspace = workspaceInformation.getWorkspaceLocation();
        String[] homes = new String[NutsStoreLocation.values().length];
        for (NutsStoreLocation type : NutsStoreLocation.values()) {
            homes[type.ordinal()] = NutsUtilPlatforms.getPlatformHomeFolder(workspaceInformation.getStoreLocationLayout(), type, homeLocations,
                    workspaceInformation.isGlobal(), workspaceInformation.getName());
            if (NutsUtilStrings.isBlank(homes[type.ordinal()])) {
                throw new NutsBootException(NutsMessage.cstyle("missing Home for %s", type.id()));
            }
        }
        NutsStoreLocationStrategy storeLocationStrategy = workspaceInformation.getStoreLocationStrategy();
        if (storeLocationStrategy == null) {
            storeLocationStrategy = NutsStoreLocationStrategy.EXPLODED;
        }
        Map<String, String> storeLocations = PrivateNutsUtils.copy(workspaceInformation.getStoreLocations());
        for (NutsStoreLocation location : NutsStoreLocation.values()) {
            String typeId = location.id();
            String _storeLocation = storeLocations.get(typeId);
            if (NutsUtilStrings.isBlank(_storeLocation)) {
                switch (storeLocationStrategy) {
                    case STANDALONE: {
                        storeLocations.put(typeId, (workspace + File.separator + typeId));
                        break;
                    }
                    case EXPLODED: {
                        storeLocations.put(typeId, homes[location.ordinal()]);
                        break;
                    }
                }
            } else if (!PrivateNutsUtils.isAbsolutePath(_storeLocation)) {
                switch (storeLocationStrategy) {
                    case STANDALONE: {
                        storeLocations.put(typeId, (workspace + File.separator + location.id()));
                        break;
                    }
                    case EXPLODED: {
                        storeLocations.put(typeId, homes[location.ordinal()] + PrivateNutsUtilIO.syspath("/" + _storeLocation));
                        break;
                    }
                }
            }
        }
        workspaceInformation.setStoreLocations(storeLocations);
    }

    /**
     * current nuts version, loaded from pom file
     *
     * @return current nuts version
     */
    private static String getApiDigest() {
        if (apiDigest == null) {
            synchronized (Nuts.class) {
                if (apiDigest == null) {
                    String v = NutsApiUtils.resolveNutsIdDigest();
                    if (v == null) {
                        throw new NutsBootException(
                                NutsMessage.plain(
                                        "unable to detect nuts digest. Most likely you are missing valid compilation of nuts. pom.properties could not be resolved and hence, we are unable to resolve nuts version."
                                )
                        );
                    }
                    apiDigest = v;
                }
            }
        }
        return apiDigest;
    }

    public boolean hasUnsatisfiedRequirements() {
        prepareWorkspace();
        return newInstanceRequirements != 0;
    }

    public int startNewProcess() {
        try {
            return createProcessBuilder().inheritIO().start().waitFor();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (InterruptedException ex) {
            throw new UncheckedIOException(new IOException(ex));
        }
    }

    public ProcessBuilder createProcessBuilder() {
        return new ProcessBuilder(createProcessCommandLine());
    }

    /**
     * repositories used to locale nuts-runtime artifact
     * @return repositories
     */
    public Set<String> resolveBootRuntimeRepositories() {
        if (parsedBootRuntimeRepositories != null) {
            return parsedBootRuntimeRepositories;
        }
        LOG.log(Level.FINE, NutsLogVerb.START, "resolve boot repositories to load nuts-runtime from options : {0} and config: {1}", new Object[]{
                (options.getRepositories() == null ? "[]" : Arrays.toString(options.getRepositories()))
                , NutsUtilStrings.isBlank(workspaceInformation.getBootRepositories()) ? "[]" : workspaceInformation.getBootRepositories()
        });
        PrivateNutsRepositorySelectorList bootRepositories = PrivateNutsRepositorySelector.parse(options.getRepositories());
        PrivateNutsRepositorySelector[] old = PrivateNutsRepositorySelector.parse(new String[]{workspaceInformation.getBootRepositories()}).toArray();
        PrivateNutsRepositorySelection[] result = null;
        if (old.length == 0) {
            //no previous config, use defaults!
            result = bootRepositories.resolveSelectors(new PrivateNutsRepositorySelection[]{
                    new PrivateNutsRepositorySelection("maven-local", null),
                    new PrivateNutsRepositorySelection("maven-central", null),
            });
        } else {
            result = bootRepositories.resolveSelectors(Arrays.stream(old).map(x -> new PrivateNutsRepositorySelection(x.getName(), x.getUrl()))
                    .toArray(PrivateNutsRepositorySelection[]::new));
        }
        return parsedBootRuntimeRepositories
                = Arrays.stream(result)
                .map(x -> x.getUrl())
                .collect(Collectors.toSet());
    }

    /**
     * repositories used to locale nuts-runtime dependencies nad extensions artifacts
     * @return repositories
     */
    public Set<String> resolveBootRuntimeDependenciesRepositories() {
        if (parsedBootRuntimeDependenciesRepositories != null) {
            return parsedBootRuntimeDependenciesRepositories;
        }
        LOG.log(Level.FINE, NutsLogVerb.START, "resolve boot repositories to load nuts-runtime dependencies from options : {0} and config: {1}", new Object[]{
                (options.getRepositories() == null ? "[]" : Arrays.toString(options.getRepositories()))
                , NutsUtilStrings.isBlank(workspaceInformation.getBootRepositories()) ? "[]" : workspaceInformation.getBootRepositories()
        });
        PrivateNutsRepositorySelectorList bootRepositories = PrivateNutsRepositorySelector.parse(options.getRepositories());
        PrivateNutsRepositorySelector[] old = PrivateNutsRepositorySelector.parse(new String[]{workspaceInformation.getBootRepositories()}).toArray();
        PrivateNutsRepositorySelection[] result = null;
        if (old.length == 0) {
            //no previous config, use defaults!
            result = bootRepositories.resolveSelectors(new PrivateNutsRepositorySelection[]{
                    new PrivateNutsRepositorySelection("maven-local", null),
                    new PrivateNutsRepositorySelection("maven-central", null),
            });
        } else {
            result = bootRepositories.resolveSelectors(Arrays.stream(old).map(x -> new PrivateNutsRepositorySelection(x.getName(), x.getUrl()))
                    .toArray(PrivateNutsRepositorySelection[]::new));
        }
        return parsedBootRuntimeDependenciesRepositories
                = Arrays.stream(result)
                .map(x -> x.getUrl())
                .collect(Collectors.toSet());
    }

    public String[] createProcessCommandLine() {
        prepareWorkspace();
        LOG.log(Level.FINE, NutsLogVerb.START, "running version {0}.  {1}", new Object[]{workspaceInformation.getApiVersion(), getRequirementsHelpString(true)});
        StringBuilder errors = new StringBuilder();
        String defaultWorkspaceLibFolder = workspaceInformation.getStoreLocation(NutsStoreLocation.LIB);
        List<String> repos = new ArrayList<>();
        repos.add(defaultWorkspaceLibFolder);
        Collection<String> bootRepositories = resolveBootRuntimeDependenciesRepositories();
        repos.addAll(bootRepositories);
        PrivateNutsErrorInfoList errorList = new PrivateNutsErrorInfoList();
        File file = PrivateNutsUtilMaven.resolveOrDownloadJar(
                new NutsBootId("net.thevpc.nuts","nuts",NutsBootVersion.parse(workspaceInformation.getApiVersion())),
                repos.toArray(new String[0]),
                workspaceInformation.getLib(), LOG,
                false,
                options.getExpireTime(),
                errorList
        );
        if (file == null) {
            errors.append("unable to load nuts ").append(workspaceInformation.getApiVersion()).append("\n");
            for (PrivateNutsErrorInfo errorInfo : errorList.list()) {
                errors.append(errorInfo.toString()).append("\n");
            }
            showError(workspaceInformation,
                    options.getWorkspace(), null,
                    errors.toString(),
                    errorList
            );
            throw new NutsBootException(
                    NutsMessage.cstyle("unable to load %s#%s", NutsConstants.Ids.NUTS_API, workspaceInformation.getApiVersion())
            );
        }

        List<String> cmd = new ArrayList<>();
        String jc = workspaceInformation.getJavaCommand();
        if (jc == null || jc.trim().isEmpty()) {
            jc = PrivateNutsUtils.resolveJavaCommand(null);
        }
        cmd.add(jc);
        boolean showCommand = false;
        for (String c : PrivateNutsCommandLine.parseCommandLineArray(options.getJavaOptions())) {
            if (!c.isEmpty()) {
                if (c.equals("--show-command")) {
                    showCommand = true;
                } else {
                    cmd.add(c);
                }
            }
        }
        if (workspaceInformation.getJavaOptions() != null) {
            Collections.addAll(cmd, PrivateNutsCommandLine.parseCommandLineArray(workspaceInformation.getJavaOptions()));
        }
        cmd.add("-jar");
        cmd.add(file.getPath());
        cmd.addAll(Arrays.asList(
                options.formatter().setCompact(true).setApiVersion(workspaceInformation.getApiVersion()).getBootCommandLine()
                        .toStringArray()
        ));
        if (showCommand) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cmd.size(); i++) {
                String s = cmd.get(i);
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(s);
            }
            PrivateNutsTerm.outln("[EXEC] %s", sb);
        }
        return cmd.toArray(new String[0]);
    }

    public NutsWorkspaceOptions getOptions() {
        return options;
    }

    private boolean prepareWorkspace() {
        if (!preparedWorkspace) {
            preparedWorkspace = true;
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.log(Level.CONFIG, NutsLogVerb.START, "bootstrap Nuts version {0} - digest {1}...", new Object[]{Nuts.getVersion(), getApiDigest()});
            }
            workspaceInformation = new PrivateNutsWorkspaceInitInformation();
            workspaceInformation.setOptions(options);

            String _ws = options.getWorkspace();
            String workspaceName;
            String lastNutsWorkspaceJsonConfigPath = null;
            boolean immediateLocation = false;
            PrivateNutsWorkspaceInitInformation lastConfigLoaded = null;
            if (_ws != null && _ws.matches("[a-z-]+://.*")) {
                //this is a protocol based workspace
                //String protocol=ws.substring(0,ws.indexOf("://"));
                workspaceName = "remote-bootstrap";
                lastNutsWorkspaceJsonConfigPath = NutsUtilPlatforms.getPlatformHomeFolder(null, null, null,
                        workspaceInformation.isGlobal(),
                        PrivateNutsUtils.resolveValidWorkspaceName(workspaceName));
                lastConfigLoaded = PrivateNutsBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, LOG);
                immediateLocation = true;

            } else {
                immediateLocation = PrivateNutsUtils.isValidWorkspaceName(_ws);
                int maxDepth = 36;
                for (int i = 0; i < maxDepth; i++) {
                    lastNutsWorkspaceJsonConfigPath
                            = PrivateNutsUtils.isValidWorkspaceName(_ws)
                            ? NutsUtilPlatforms.getPlatformHomeFolder(
                            null, null, null,
                            workspaceInformation.isGlobal(),
                            PrivateNutsUtils.resolveValidWorkspaceName(_ws)
                    ) : PrivateNutsUtilIO.getAbsolutePath(_ws);

                    PrivateNutsWorkspaceInitInformation configLoaded = PrivateNutsBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, LOG);
                    if (configLoaded == null) {
                        //not loaded
                        break;
                    }
                    if (NutsUtilStrings.isBlank(configLoaded.getWorkspaceLocation())) {
                        lastConfigLoaded = configLoaded;
                        break;
                    }
                    _ws = configLoaded.getWorkspaceLocation();
                    if (i >= maxDepth - 1) {
                        throw new NutsBootException(NutsMessage.cstyle("cyclic workspace resolution"));
                    }
                }
                workspaceName = PrivateNutsUtils.resolveValidWorkspaceName(options.getWorkspace());
            }
            workspaceInformation.setWorkspaceLocation(lastNutsWorkspaceJsonConfigPath);
            if (lastConfigLoaded != null) {
                workspaceInformation.setWorkspaceLocation(lastNutsWorkspaceJsonConfigPath);
                workspaceInformation.setName(lastConfigLoaded.getName());
                workspaceInformation.setUuid(lastConfigLoaded.getUuid());
                if (!options.isReset()) {
                    workspaceInformation.setBootRepositories(lastConfigLoaded.getBootRepositories());
                    workspaceInformation.setJavaCommand(lastConfigLoaded.getJavaCommand());
                    workspaceInformation.setJavaOptions(lastConfigLoaded.getJavaOptions());
                    workspaceInformation.setExtensionsSet(PrivateNutsUtils.copy(lastConfigLoaded.getExtensionsSet()));
                    workspaceInformation.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy());
                    workspaceInformation.setRepositoryStoreLocationStrategy(lastConfigLoaded.getRepositoryStoreLocationStrategy());
                    workspaceInformation.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout());
                    workspaceInformation.setStoreLocations(PrivateNutsUtils.copy(lastConfigLoaded.getStoreLocations()));
                    workspaceInformation.setHomeLocations(PrivateNutsUtils.copy(lastConfigLoaded.getHomeLocations()));
                } else {
                    lastWorkspaceInformation = new PrivateNutsWorkspaceInitInformation();
                    lastWorkspaceInformation.setWorkspaceLocation(lastNutsWorkspaceJsonConfigPath);
                    lastWorkspaceInformation.setName(lastConfigLoaded.getName());
                    lastWorkspaceInformation.setUuid(lastConfigLoaded.getUuid());
                    lastWorkspaceInformation.setBootRepositories(lastConfigLoaded.getBootRepositories());
                    lastWorkspaceInformation.setJavaCommand(lastConfigLoaded.getJavaCommand());
                    lastWorkspaceInformation.setJavaOptions(lastConfigLoaded.getJavaOptions());
                    lastWorkspaceInformation.setExtensionsSet(PrivateNutsUtils.copy(lastConfigLoaded.getExtensionsSet()));
                    lastWorkspaceInformation.setStoreLocationStrategy(lastConfigLoaded.getStoreLocationStrategy());
                    lastWorkspaceInformation.setRepositoryStoreLocationStrategy(lastConfigLoaded.getRepositoryStoreLocationStrategy());
                    lastWorkspaceInformation.setStoreLocationLayout(lastConfigLoaded.getStoreLocationLayout());
                    lastWorkspaceInformation.setStoreLocations(PrivateNutsUtils.copy(lastConfigLoaded.getStoreLocations()));
                    lastWorkspaceInformation.setHomeLocations(PrivateNutsUtils.copy(lastConfigLoaded.getHomeLocations()));
                }
            }
            revalidateLocations(workspaceInformation, workspaceName, immediateLocation);
            workspaceInformation.setApiVersion(options.getApiVersion());
            long countDeleted = 0;
            //now that config information is prepared proceed to any cleanup
            if (options.isReset()) {
                if (lastWorkspaceInformation != null) {
                    revalidateLocations(lastWorkspaceInformation, workspaceName, immediateLocation);
                    if (options.isDry()) {
                        PrivateNutsTerm.outln("[dry] [reset] delete ALL workspace folders and configurations");
                    } else {
                        LOG.log(Level.CONFIG, NutsLogVerb.WARNING, "reset workspace");
                        countDeleted = PrivateNutsUtilDeleteFiles.deleteStoreLocations(lastWorkspaceInformation, getOptions(), true, LOG, NutsStoreLocation.values());
                        PrivateNutsUtilLauncher.ndiUndo(LOG);
                    }
                }
            } else if (options.isRecover()) {
                if (options.isDry()) {
                    PrivateNutsTerm.outln("[dry] [recover] delete CACHE/TEMP workspace folders");
                } else {
                    LOG.log(Level.CONFIG, NutsLogVerb.WARNING, "recover workspace.");
                    List<Object> folders = new ArrayList<>();
                    folders.add(NutsStoreLocation.CACHE);
                    folders.add(NutsStoreLocation.TEMP);
                    //delete nuts.jar and nuts-runtime.jar in the lib folder. They will be re-downloaded.
                    String p = PrivateNutsUtilDeleteFiles.getStoreLocationPath(workspaceInformation, NutsStoreLocation.LIB);
                    if (p != null) {
                        folders.add(new File(p, "id/net/thevpc/nuts/nuts"));
                        folders.add(new File(p, "id/net/thevpc/nuts/nuts-runtime"));
                    }
                    countDeleted = PrivateNutsUtilDeleteFiles.deleteStoreLocations(workspaceInformation, getOptions(), false, LOG, folders.toArray());
                }
            }
            //if recover or reset mode with -Q option (SkipBoot)
            //as long as there are no applications to run, will exit before creating workspace
            if (options.getApplicationArguments().length == 0 && options.isSkipBoot()
                    && (options.isRecover() || options.isReset())) {
                if (isPlainTrace()) {
                    if (countDeleted > 0) {
                        PrivateNutsTerm.outln("workspace erased : %s", workspaceInformation.getWorkspaceLocation());
                    } else {
                        PrivateNutsTerm.outln("workspace is not erased because it does not exist : %s", workspaceInformation.getWorkspaceLocation());
                    }
                }
                throw new NutsBootException(NutsMessage.cstyle(""), 0);
            }
            //after eventual clean up
            if (options.isInherited()) {
                //when Inherited, always use the current Api version!
                workspaceInformation.setApiVersion(Nuts.getVersion());
            }else{
                if (NutsConstants.Versions.LATEST.equalsIgnoreCase(workspaceInformation.getApiVersion())
                        || NutsConstants.Versions.RELEASE.equalsIgnoreCase(workspaceInformation.getApiVersion())
                ) {
                    NutsBootId s = PrivateNutsUtilMaven.resolveLatestMavenId(NutsBootId.parse(NutsConstants.Ids.NUTS_API), null, LOG, resolveBootRuntimeDependenciesRepositories());
                    if (s == null) {
                        throw new NutsBootException(NutsMessage.plain("unable to load latest nuts version"));
                    }
                    workspaceInformation.setApiVersion(s.getVersion().toString());
                }
                if (NutsUtilStrings.isBlank(workspaceInformation.getApiVersion())) {
                    workspaceInformation.setApiVersion(Nuts.getVersion());
                }
            }

            Path nutsApiConfigJsonPath = Paths.get(workspaceInformation.getStoreLocations().get(NutsStoreLocation.CONFIG.id()))
                    .resolve(NutsConstants.Folders.ID)
                    .resolve("net/thevpc/nuts/nuts").resolve(workspaceInformation.getApiVersion()).resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME);
            boolean loadedApiConfig = false;

            //This is not cache, but still, if recover or reset, config will be ignored!
            if (isLoadFromCache() && PrivateNutsUtils.isFileAccessible(nutsApiConfigJsonPath, options.getExpireTime(), LOG)) {
                try {
                    Map<String, Object> obj = PrivateNutsJsonParser.parse(nutsApiConfigJsonPath);
                    LOG.log(Level.CONFIG, NutsLogVerb.READ, "loaded {0} file : {1}", new String[]{NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME, nutsApiConfigJsonPath.toString()});
                    loadedApiConfig = true;
                    if (workspaceInformation.getRuntimeId() == null) {
                        String runtimeId = (String) obj.get("runtimeId");
                        if (NutsUtilStrings.isBlank(runtimeId)) {
                            LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "{0} does not contain runtime-id", new Object[]{NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME});
                        }
                        workspaceInformation.setRuntimeId(NutsBootId.parse(runtimeId));
                    }
                    if (workspaceInformation.getBootRepositories() == null) {
                        workspaceInformation.setBootRepositories((String) obj.get("bootRepositories"));
                    }
                    if (workspaceInformation.getJavaCommand() == null) {
                        workspaceInformation.setJavaCommand((String) obj.get("javaCommand"));
                    }
                    if (workspaceInformation.getJavaOptions() == null) {
                        workspaceInformation.setJavaOptions((String) obj.get("javaOptions"));
                    }
                } catch (UncheckedIOException | NutsIOException e) {
                    LOG.log(Level.CONFIG, NutsLogVerb.READ, "unable to read {0}", nutsApiConfigJsonPath.toString());
                }
            }
            if (!loadedApiConfig || workspaceInformation.getRuntimeId() == null
                    || workspaceInformation.getRuntimeBootDescriptor() == null
                    || workspaceInformation.getExtensionBootDescriptors() == null
                    || workspaceInformation.getBootRepositories() == null) {

                //resolve extension id
                if (workspaceInformation.getRuntimeId() == null) {
                    String apiVersion = workspaceInformation.getApiId().substring(workspaceInformation.getApiId().indexOf('#') + 1);
                    NutsBootId runtimeId = PrivateNutsUtilMaven.resolveLatestMavenId(NutsBootId.parse(NutsConstants.Ids.NUTS_RUNTIME), (rtVersion) -> rtVersion.getFrom().startsWith(apiVersion + "."), LOG, resolveBootRuntimeRepositories());
                    if (runtimeId != null) {
                        //LOG.log(Level.FINEST, "[success] Resolved latest runtime-id : {0}", new Object[]{runtimeId});
                    } else {
                        LOG.log(Level.FINEST, NutsLogVerb.FAIL, "unable to resolve latest runtime-id version (is connection ok?)", new Object[0]);
                    }
                    workspaceInformation.setRuntimeId(runtimeId);
                    workspaceInformation.setRuntimeBootDescriptor(null);
                    workspaceInformation.setBootRepositories(null);
                }
                if (workspaceInformation.getRuntimeId() == null) {
                    workspaceInformation.setRuntimeId(
                            new NutsBootId("net.thevpc.nuts", "nuts-runtime",
                                    NutsBootVersion.parse(workspaceInformation.getApiVersion() + ".0")
                            )
                    );
                    LOG.log(Level.CONFIG, NutsLogVerb.READ, "consider default runtime-id : {0}", new Object[]{workspaceInformation.getRuntimeId()});
                }
                if (workspaceInformation.getRuntimeId().getVersion().isBlank()) {
                    workspaceInformation.setRuntimeId(
                            new NutsBootId(workspaceInformation.getRuntimeId().getGroupId(), workspaceInformation.getRuntimeId().getArtifactId(),
                                    NutsBootVersion.parse(workspaceInformation.getApiVersion() + ".0"))
                    );
                }

                //resolve runtime libraries
                if (workspaceInformation.getRuntimeBootDescriptor() == null) {
                    Set<NutsBootId> loadedDeps = null;
                    String extraBootRepositories = null;
                    NutsBootId rid = workspaceInformation.getRuntimeId();
                    try {
                        Path nutsRuntimeCacheConfigPath = Paths.get(workspaceInformation.getCacheBoot())
                                .resolve(PrivateNutsUtils.idToPath(rid)).resolve(NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME);
                        boolean cacheLoaded = false;
                        if (isLoadFromCache() && PrivateNutsUtils.isFileAccessible(nutsRuntimeCacheConfigPath, options.getExpireTime(), LOG)) {
                            try {
                                Map<String, Object> obj = PrivateNutsJsonParser.parse(nutsRuntimeCacheConfigPath);
                                LOG.log(Level.CONFIG, NutsLogVerb.READ, "loaded {0} file : {1}", new String[]{NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME, nutsRuntimeCacheConfigPath.toString()});
                                loadedDeps = PrivateNutsUtils.parseDependencies((String) obj.get("dependencies"));
                                extraBootRepositories = (String) obj.get("bootRepositories");
                            } catch (Exception ex) {
                                LOG.log(Level.FINEST, NutsLogVerb.FAIL, "unable to load {0} file : {1} : {2}", new String[]{NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME, nutsRuntimeCacheConfigPath.toString(), ex.toString()});
                                //ignore...
                            }
                            cacheLoaded = true;
                        }

                        if (!cacheLoaded || loadedDeps == null) {
                            PrivateNutsUtils.Deps depsAndRepos = PrivateNutsUtilMaven.loadDependencies(workspaceInformation.getRuntimeId(),
                                    LOG, resolveBootRuntimeRepositories());
                            if (depsAndRepos != null) {
                                loadedDeps = depsAndRepos.deps;
                                extraBootRepositories = String.join(";", depsAndRepos.repos);
                                LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "detect runtime dependencies : {0}", new Object[]{depsAndRepos.deps});
                                LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "detect runtime repos        : {0}", new Object[]{depsAndRepos.repos});
                            }
                        }
                    } catch (Exception ex) {
                        LOG.log(Level.FINEST, NutsLogVerb.FAIL, "unable to load {0} file : {1}", new String[]{NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME, ex.toString()});
                        //
                    }
                    if (loadedDeps == null) {
                        throw new NutsBootException(NutsMessage.cstyle("unable to load dependencies for %s", rid));
                    }
                    workspaceInformation.setRuntimeBootDescriptor(new NutsBootDescriptor(
                            workspaceInformation.getRuntimeId(),
                            loadedDeps.toArray(new NutsBootId[0])
                    ));
                    LinkedHashSet<String> bootRepositories =
                            Arrays.stream((extraBootRepositories==null?"":extraBootRepositories).split(";"))
                                    .map(String::trim).filter(x->x.length()>0)
                                    .collect(Collectors.toCollection(LinkedHashSet::new));
                    bootRepositories.addAll(resolveBootRuntimeRepositories());
                    if(LOG.isLoggable(Level.CONFIG)) {
                        if(bootRepositories.size()==0){
                            LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "workspace bootRepositories could not be resolved");
                        }else if(bootRepositories.size()==1){
                            LOG.log(Level.CONFIG, NutsLogVerb.INFO, "workspace bootRepositories resolved to : {0}",new Object[]{bootRepositories.toArray()[0]});
                        }else {
                            LOG.log(Level.CONFIG, NutsLogVerb.INFO, "workspace bootRepositories resolved to : ");
                            for (String repository : bootRepositories) {
                                LOG.log(Level.CONFIG, NutsLogVerb.INFO, "    {0}", repository);
                            }
                        }
                    }
                    workspaceInformation.setBootRepositories(String.join(";", bootRepositories));
                }

                //resolve extension libraries
                if (workspaceInformation.getExtensionBootDescriptors() == null) {
//                    LinkedHashSet<String> allExtDependencies = new LinkedHashSet<>();
                    LinkedHashSet<String> excludedExtensions = new LinkedHashSet<>();
                    if (options.getExcludedExtensions() != null) {
                        for (String excludedExtensionGroup : options.getExcludedExtensions()) {
                            for (String excludedExtension : excludedExtensionGroup.split("[;, ]")) {
                                if (excludedExtension.length() > 0) {
                                    excludedExtensions.add(NutsBootId.parse(excludedExtension).getShortName());
                                }
                            }
                        }
                    }
                    if (workspaceInformation.getExtensionsSet() != null) {
                        List<NutsBootDescriptor> all = new ArrayList<>();
                        for (String extension : workspaceInformation.getExtensionsSet()) {
                            NutsBootId eid = NutsBootId.parse(extension);
                            if (!excludedExtensions.contains(eid.getShortName()) && !excludedExtensions.contains(eid.getArtifactId())) {
                                Path extensionFile = Paths.get(workspaceInformation.getCacheBoot())
                                        .resolve(PrivateNutsUtils.idToPath(eid)).resolve(NutsConstants.Files.WORKSPACE_EXTENSION_CACHE_FILE_NAME);
                                Set<NutsBootId> loadedDeps = null;
                                if (isLoadFromCache() && PrivateNutsUtils.isFileAccessible(extensionFile, options.getExpireTime(), LOG)) {
                                    try {
                                        Map<String, Object> obj = PrivateNutsJsonParser.parse(nutsApiConfigJsonPath);
                                        LOG.log(Level.CONFIG, NutsLogVerb.READ, "loaded {0} file : {1}", new String[]{NutsConstants.Files.WORKSPACE_EXTENSION_CACHE_FILE_NAME, extensionFile.toString()});
                                        loadedDeps = PrivateNutsUtils.parseDependencies((String) obj.get("dependencies"));
                                    } catch (Exception ex) {
                                        LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "unable to load {0} file : {1} : {2}", new String[]{NutsConstants.Files.WORKSPACE_EXTENSION_CACHE_FILE_NAME, extensionFile.toString(), ex.toString()});
                                        //ignore
                                    }
                                }
                                if (loadedDeps == null) {
                                    PrivateNutsUtils.Deps depsAndRepos = PrivateNutsUtilMaven.loadDependencies(eid, LOG, resolveBootRuntimeDependenciesRepositories());
                                    if (depsAndRepos != null) {
                                        loadedDeps = depsAndRepos.deps;
                                    }
                                }
                                if (loadedDeps != null) {
                                    all.add(new NutsBootDescriptor(NutsBootId.parse(extension), loadedDeps.toArray(new NutsBootId[0])));
                                } else {
                                    throw new NutsBootException(NutsMessage.cstyle("unable to load dependencies for %s", eid));
                                }
                            }
                        }
                        workspaceInformation.setExtensionBootDescriptors(all.toArray(new NutsBootDescriptor[0]));
                    } else {
                        workspaceInformation.setExtensionBootDescriptors(new NutsBootDescriptor[0]);
                    }
                }
            }

            newInstanceRequirements = checkRequirements(true);
            if (newInstanceRequirements == 0) {
                workspaceInformation.setJavaCommand(null);
                workspaceInformation.setJavaOptions(null);
            }
            this.contextClassLoaderSupplier = options.getClassLoaderSupplier() == null ? () -> Thread.currentThread().getContextClassLoader()
                    : options.getClassLoaderSupplier();
            return true;
        }
        return false;
    }

    private boolean isPlainTrace() {
        return options.isTrace() && !options.isBot()
                && (options.getOutputFormat() == NutsContentType.PLAIN
                || options.getOutputFormat() == null
        );
    }


    private boolean isLoadFromCache() {
        return !options.isRecover() && !options.isReset();
    }

    public NutsSession openWorkspace() {
        prepareWorkspace();
        if (hasUnsatisfiedRequirements()) {
            throw new NutsUnsatisfiedRequirementsException(
                    NutsMessage.cstyle("unable to open a distinct version : %s from nuts#%s",
                            getRequirementsHelpString(true), Nuts.getVersion())
            );
        }
        //if recover or reset mode with -K option (SkipWelcome)
        //as long as there are no applications to run, will exit before creating workspace
        if (options.getApplicationArguments().length == 0 && options.isSkipBoot()
                && (options.isRecover() || options.isReset())) {
            if (isPlainTrace()) {
                PrivateNutsTerm.outln("workspace erased : %s", workspaceInformation.getWorkspaceLocation());
            }
            throw new NutsBootException(null, 0);
        }
        URL[] bootClassWorldURLs = null;
        ClassLoader workspaceClassLoader;
        NutsWorkspace nutsWorkspace = null;
        PrivateNutsErrorInfoList errorList = new PrivateNutsErrorInfoList();
        try {
            if (options.getOpenMode() == NutsOpenMode.OPEN_OR_ERROR) {
                //add fail fast test!!
                if (!new File(workspaceInformation.getWorkspaceLocation(), NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME).isFile()) {
                    throw new NutsWorkspaceNotFoundException(workspaceInformation.getWorkspaceLocation());
                }
            } else if (options.getOpenMode() == NutsOpenMode.CREATE_OR_ERROR) {
                if (new File(workspaceInformation.getWorkspaceLocation(), NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME).exists()) {
                    throw new NutsWorkspaceAlreadyExistsException(workspaceInformation.getWorkspaceLocation());
                }
            }
            if (NutsUtilStrings.isBlank(workspaceInformation.getApiId())
                    || workspaceInformation.getRuntimeId() == null
                    || NutsUtilStrings.isBlank(workspaceInformation.getBootRepositories())
                    || workspaceInformation.getRuntimeBootDescriptor() == null
                    || workspaceInformation.getExtensionBootDescriptors() == null) {
                throw new NutsBootException(NutsMessage.plain("invalid workspace state"));
            }
            boolean recover = options.isRecover() || options.isReset();

//            LinkedHashMap<String, PrivateNutsBootClassLoader.NutsClassLoaderNode> allExtensionFiles = new LinkedHashMap<>();
            List<NutsClassLoaderNode> deps = new ArrayList<>();

            String workspaceBootLibFolder = workspaceInformation.getLib();

            String[] repositories =
                    Arrays.stream((workspaceInformation.getBootRepositories()==null?"":workspaceInformation.getBootRepositories())
                                    .split("[\n;]")
                    ).map(String::trim).filter(x->x.length()>0).toArray(String[]::new);

            NutsClassLoaderNodeBuilder rt = new NutsClassLoaderNodeBuilder();
            File runtimeJarFile = PrivateNutsUtilMaven.getBootCacheJar(workspaceInformation.getRuntimeId(), repositories, workspaceBootLibFolder, !recover, "runtime", options.getExpireTime(), errorList, workspaceInformation, pathExpansionConverter, LOG);

            if (LOG.isLoggable(Level.CONFIG)) {
                String rtHash = "";
                if (workspaceInformation.getRuntimeId() != null) {
                    rtHash = PrivateNutsUtilDigest.getFileOrDirectoryDigest(runtimeJarFile.toPath());
                    if (rtHash == null) {
                        rtHash = "";
                    }
                }
                LOG.log(Level.CONFIG, NutsLogVerb.INFO, "detect nuts-runtime version {0} - digest {1} from {2}", new Object[]{workspaceInformation.getRuntimeId(), rtHash, runtimeJarFile});
            }

            rt.setId(workspaceInformation.getRuntimeId().toString())
                    .setUrl(runtimeJarFile
                            .toURI().toURL());
            for (NutsBootId s : workspaceInformation.getRuntimeBootDescriptor().getDependencies()) {
                NutsClassLoaderNodeBuilder x = new NutsClassLoaderNodeBuilder();
                if (PrivateNutsUtilBootId.isAcceptDependency(s, workspaceInformation.getOptions())) {
                    x.setId(s.toString())
                            .setUrl(PrivateNutsUtilMaven.getBootCacheJar(s, repositories, workspaceBootLibFolder, !recover, "runtime dependency", options.getExpireTime(), errorList, workspaceInformation, pathExpansionConverter, LOG)
                                    .toURI().toURL()
                            );
                    rt.addDependency(x.build());
                }
            }
            workspaceInformation.setRuntimeBootDependencyNode(rt.build());

            for (NutsBootDescriptor nutsBootDescriptor : workspaceInformation.getExtensionBootDescriptors()) {
                NutsClassLoaderNodeBuilder rt2 = new NutsClassLoaderNodeBuilder();
                rt2.setId(nutsBootDescriptor.getId().toString())
                        .setUrl(PrivateNutsUtilMaven.getBootCacheJar(workspaceInformation.getRuntimeId(), repositories, workspaceBootLibFolder, !recover, "extension " + nutsBootDescriptor.getId(), options.getExpireTime(), errorList, workspaceInformation, pathExpansionConverter, LOG)
                                .toURI().toURL());
                for (NutsBootId s : nutsBootDescriptor.getDependencies()) {
                    if (PrivateNutsUtilBootId.isAcceptDependency(s, workspaceInformation.getOptions())) {
                        NutsClassLoaderNodeBuilder x = new NutsClassLoaderNodeBuilder();
                        x.setId(s.toString())
                                .setUrl(PrivateNutsUtilMaven.getBootCacheJar(s, repositories, workspaceBootLibFolder, !recover, "extension " + nutsBootDescriptor.getId() + " dependency", options.getExpireTime(), errorList, workspaceInformation, pathExpansionConverter, LOG)
                                        .toURI().toURL()
                                );
                        rt2.addDependency(x.build());
                    }
                }
                deps.add(rt2.build());
            }
            workspaceInformation.setExtensionBootDependencyNodes(deps.toArray(new NutsClassLoaderNode[0]));
            deps.add(0, workspaceInformation.getRuntimeBootDependencyNode());

            bootClassWorldURLs = PrivateNutsUtilClassLoader.resolveClassWorldURLs(deps.toArray(new NutsClassLoaderNode[0]), getContextClassLoader(), LOG);
            workspaceClassLoader = bootClassWorldURLs.length == 0 ? getContextClassLoader() : new PrivateNutsBootClassLoader(deps.toArray(new NutsClassLoaderNode[0]), getContextClassLoader());
            workspaceInformation.setWorkspaceClassLoader(workspaceClassLoader);
            if (LOG.isLoggable(Level.CONFIG)) {
                if(bootClassWorldURLs.length==0){
                    LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "empty nuts class world. All dependencies are already loaded in classpath, most likely");
                }else if(bootClassWorldURLs.length==1){
                    LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "resolve nuts class world to : {0} {1}",
                            new Object[]{PrivateNutsUtilDigest.getURLDigest(bootClassWorldURLs[0]), bootClassWorldURLs[0]});
                }else {
                    LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "resolve nuts class world is to : ");
                    for (URL u : bootClassWorldURLs) {
                        LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "    {0} : {1}",
                                new Object[]{PrivateNutsUtilDigest.getURLDigest(u), u});
                    }
                }
            }
            workspaceInformation.setBootClassWorldURLs(bootClassWorldURLs);
            LOG.log(Level.CONFIG, NutsLogVerb.INFO, "search for NutsBootWorkspaceFactory service implementations");
            ServiceLoader<NutsBootWorkspaceFactory> serviceLoader = ServiceLoader.load(NutsBootWorkspaceFactory.class, workspaceClassLoader);
            List<NutsBootWorkspaceFactory> factories = new ArrayList<>(5);
            for (NutsBootWorkspaceFactory a : serviceLoader) {
                factories.add(a);
            }
            factories.sort(new PrivateNutsBootWorkspaceFactoryComparator(options));
            if (LOG.isLoggable(Level.CONFIG)) {
                if(factories.isEmpty()){
                    LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "unable to detect NutsBootWorkspaceFactory service implementations");
                }else if(factories.size()==1) {
                    LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "detect NutsBootWorkspaceFactory service implementation : {0}",factories.get(0).getClass().getName());
                }else{
                    LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "detect NutsBootWorkspaceFactory service implementations are :");
                    for (NutsBootWorkspaceFactory u : factories) {
                        LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "    {0}",
                                new Object[]{u.getClass().getName()});
                    }
                }
            }
            NutsBootWorkspaceFactory factoryInstance = null;
            List<Throwable> exceptions = new ArrayList<>();
            for (NutsBootWorkspaceFactory a : factories) {
                factoryInstance = a;
                try {
                    if (LOG.isLoggable(Level.CONFIG)) {
                        LOG.log(Level.CONFIG, NutsLogVerb.INFO, "create workspace using {0}",factoryInstance.getClass().getName());
                    }
                    workspaceInformation.setBootWorkspaceFactory(factoryInstance);
                    nutsWorkspace = a.createWorkspace(workspaceInformation);
                } catch (UnsatisfiedLinkError | Exception ex) {
                    exceptions.add(ex);
                    LOG.log(Level.SEVERE, "unable to create workspace using factory " + a, ex);
                }
                if (nutsWorkspace != null) {
                    break;
                }
            }
            if (nutsWorkspace == null) {
                //should never happen
                PrivateNutsTerm.errln("unable to load Workspace \"%s\" from ClassPath :", workspaceInformation.getName());
                for (URL url : bootClassWorldURLs) {
                    PrivateNutsTerm.errln("\t %s", PrivateNutsUtils.formatURL(url));
                }
                for (Throwable exception : exceptions) {
                    PrivateNutsTerm.errln(exception);
                }
                LOG.log(Level.SEVERE, NutsLogVerb.FAIL, "unable to load Workspace Component from ClassPath : {0}", new Object[]{Arrays.asList(bootClassWorldURLs)});
                throw new NutsInvalidWorkspaceException(this.workspaceInformation.getWorkspaceLocation(),
                        NutsMessage.cstyle("unable to load Workspace Component from ClassPath : %s", Arrays.asList(bootClassWorldURLs))
                );
            }
//            LOG2 = nutsWorkspace.log().of(NutsBootWorkspace.class);
//            if (LOG2.isLoggable(Level.FINE)) {
//                LOG2.with().session(nutsWorkspace.createSession())
//                        .level(Level.FINE).verb(NutsLogVerb.SUCCESS).log("end initialize workspace");
//            }
            return nutsWorkspace.createSession();
        } catch (NutsReadOnlyException | NutsUserCancelException | PrivateNutsBootCancelException ex) {
            throw ex;
        } catch (Throwable ex) {
            StringBuilder errors = new StringBuilder();
            errorList.add(new PrivateNutsErrorInfo(
                    null, null, null, "unable to boot workspace : " + ex,
                    ex
            ));
            for (PrivateNutsErrorInfo errorInfo : errorList.list()) {
                errors.append(errorInfo.toString()).append("\n");
            }
            showError(workspaceInformation,
                    options.getWorkspace(),
                    bootClassWorldURLs,
                    errors.toString(),
                    errorList
            );
            if (ex instanceof NutsException) {
                throw (NutsException) ex;
            }
            throw new NutsBootException(NutsMessage.plain("unable to locate valid nuts-runtime package"), ex);
        }
    }


    private Supplier<ClassLoader> getContextClassLoaderSupplier() {
        return contextClassLoaderSupplier;
    }

    protected ClassLoader getContextClassLoader() {
        Supplier<ClassLoader> currentContextClassLoaderProvider = getContextClassLoaderSupplier();
        if (currentContextClassLoaderProvider == null) {
            return null;
        }
        return currentContextClassLoaderProvider.get();
    }

    private String getWorkspaceRunModeString() {
        if (this.getOptions().isReset()) {
            return "reset";
        } else if (this.getOptions().isRecover()) {
            return "recover";
        } else {
            return "exec";
        }
    }

    private void runCommandHelp() {
        NutsContentType f = options.getOutputFormat();
        if (f == null) {
            f = NutsContentType.PLAIN;
        }
        if (options.isDry()) {
            printDryCommand("help");
        } else {
            String msg = "nuts is an open source package manager mainly for java applications. Type 'nuts help' or visit https://github.com/thevpc/nuts for more help.";
            switch (f) {
                case JSON: {
                    PrivateNutsTerm.outln("{");
                    PrivateNutsTerm.outln("  \"help\": \"%s\"", msg);
                    PrivateNutsTerm.outln("}");
                    return;
                }
                case TSON: {
                    PrivateNutsTerm.outln("{");
                    PrivateNutsTerm.outln("  help: \"%s\"", msg);
                    PrivateNutsTerm.outln("}");
                    return;
                }
                case YAML: {
                    PrivateNutsTerm.outln("help: %s", msg);
                    return;
                }
                case TREE: {
                    PrivateNutsTerm.outln("- help: %s", msg);
                    return;
                }
                case TABLE: {
                    PrivateNutsTerm.outln("help  %s", msg);
                    return;
                }
                case XML: {
                    PrivateNutsTerm.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    PrivateNutsTerm.outln("<string>");
                    PrivateNutsTerm.outln(" %s", msg);
                    PrivateNutsTerm.outln("</string>");
                    return;
                }
                case PROPS: {
                    PrivateNutsTerm.outln("help=%s", msg);
                    return;
                }
            }
            PrivateNutsTerm.outln("%s", msg);
        }
    }

    private void printDryCommand(String cmd) {
        NutsContentType f = options.getOutputFormat();
        if (f == null) {
            f = NutsContentType.PLAIN;
        }
        if (options.isDry()) {
            switch (f) {
                case JSON: {
                    PrivateNutsTerm.outln("{");
                    PrivateNutsTerm.outln("  \"dryCommand\": \"%s\"", cmd);
                    PrivateNutsTerm.outln("}");
                    return;
                }
                case TSON: {
                    PrivateNutsTerm.outln("{");
                    PrivateNutsTerm.outln("  dryCommand: \"%s\"", cmd);
                    PrivateNutsTerm.outln("}");
                    return;
                }
                case YAML: {
                    PrivateNutsTerm.outln("dryCommand: %s", cmd);
                    return;
                }
                case TREE: {
                    PrivateNutsTerm.outln("- dryCommand: %s", cmd);
                    return;
                }
                case TABLE: {
                    PrivateNutsTerm.outln("dryCommand  %s", cmd);
                    return;
                }
                case XML: {
                    PrivateNutsTerm.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    PrivateNutsTerm.outln("<object>");
                    PrivateNutsTerm.outln("  <string key=\"%s\" value=\"%s\"/>", "dryCommand", cmd);
                    PrivateNutsTerm.outln("</object>");
                    return;
                }
                case PROPS: {
                    PrivateNutsTerm.outln("dryCommand=%s", cmd);
                    return;
                }
            }
            PrivateNutsTerm.outln("[Dry] %s", Nuts.getVersion());
        }
    }

    private void runCommandVersion() {
//        if (options.isDry()) {
//            session.out().println("[boot-internal-command] show-version");
//        } else {
//            session.out().println("nuts-version :" + Nuts.getVersion());
//        }
        NutsContentType f = options.getOutputFormat();
        if (f == null) {
            f = NutsContentType.PLAIN;
        }
        if (options.isDry()) {
            printDryCommand("version");
            return;
        }
        switch (f) {
            case JSON: {
                PrivateNutsTerm.outln("{");
                PrivateNutsTerm.outln("  \"version\": \"%s\",", Nuts.getVersion());
                PrivateNutsTerm.outln("  \"digest\": \"%s\"", getApiDigest());
                PrivateNutsTerm.outln("}");
                return;
            }
            case TSON: {
                PrivateNutsTerm.outln("{");
                PrivateNutsTerm.outln("  version: \"%s\",", Nuts.getVersion());
                PrivateNutsTerm.outln("  digest: \"%s\"", getApiDigest());
                PrivateNutsTerm.outln("}");
                return;
            }
            case YAML: {
                PrivateNutsTerm.outln("version: %s", Nuts.getVersion());
                PrivateNutsTerm.outln("digest: %s", getApiDigest());
                return;
            }
            case TREE: {
                PrivateNutsTerm.outln("- version: %s", Nuts.getVersion());
                PrivateNutsTerm.outln("- digest: %s", getApiDigest());
                return;
            }
            case TABLE: {
                PrivateNutsTerm.outln("version      %s", Nuts.getVersion());
                PrivateNutsTerm.outln("digest  %s", getApiDigest());
                return;
            }
            case XML: {
                PrivateNutsTerm.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                PrivateNutsTerm.outln("<object>");
                PrivateNutsTerm.outln("  <string key=\"%s\" value=\"%s\"/>", "version", Nuts.getVersion());
                PrivateNutsTerm.outln("  <string key=\"%s\" value=\"%s\"/>", "digest", getApiDigest());
                PrivateNutsTerm.outln("</object>");
                return;
            }
            case PROPS: {
                PrivateNutsTerm.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                PrivateNutsTerm.outln("version=%s", Nuts.getVersion());
                PrivateNutsTerm.outln("digest=%s", getApiDigest());
                PrivateNutsTerm.outln("</object>");
                return;
            }
        }
        PrivateNutsTerm.outln("%s", Nuts.getVersion());
    }

    public void runWorkspace() {
        if (options.isCommandHelp()) {
            runCommandHelp();
            return;
        } else if (options.isCommandVersion()) {
            runCommandVersion();
            return;
        }
        if (hasUnsatisfiedRequirements()) {
            startNewProcess();
            return;
        }
        NutsSession session = this.openWorkspace();
        NutsWorkspace workspace = session.getWorkspace();
        String message = "workspace started successfully";
        NutsWorkspaceOptions o = this.getOptions();
        if (workspace == null) {
            fallbackInstallActionUnavailable(message);
            throw new NutsBootException(
                    NutsMessage.cstyle("workspace not available to run : %s",
                            new PrivateNutsCommandLine(o.getApplicationArguments())
                    )
            );
        }

        session.setAppId(workspace.getApiId());
        if (LOG2 == null) {
            LOG2 = workspace.log().setSession(session).of(NutsBootWorkspace.class);
        }
        NutsLoggerOp logOp = LOG2.with().session(session).level(Level.CONFIG);
        logOp.verb(NutsLogVerb.SUCCESS).log("running workspace in {0} mode", getWorkspaceRunModeString());
        if (workspace == null && o.getApplicationArguments().length > 0) {
            switch (o.getApplicationArguments()[0]) {
                case "version": {
                    runCommandVersion();
                    return;
                }
                case "help": {
                    runCommandHelp();
                    return;
                }
            }
        }
        if (o.getApplicationArguments().length == 0) {
            if (o.isSkipWelcome()) {
                return;
            }
            workspace.exec()
                    .setSession(session)
                    .addCommand("welcome")
                    .addExecutorOptions(o.getExecutorOptions())
                    .setExecutionType(o.getExecutionType())
                    .setFailFast(true)
                    .setDry(options.isDry())
                    .run();
        } else {
            workspace.exec()
                    .setSession(session)
                    .addCommand(o.getApplicationArguments())
                    .addExecutorOptions(o.getExecutorOptions())
                    .setExecutionType(o.getExecutionType())
                    .setFailFast(true)
                    .setDry(options.isDry())
                    .run();
        }
    }

    private void fallbackInstallActionUnavailable(String message) {
        PrivateNutsTerm.outln("%s", message);
        LOG.log(Level.SEVERE, NutsLogVerb.FAIL, message, new Object[0]);
    }

    private void showError(PrivateNutsWorkspaceInitInformation actualBootConfig, String workspace, URL[] bootClassWorldURLs, String extraMessage, PrivateNutsErrorInfoList ths) {
        Map<String, String> rbc_locations = actualBootConfig.getStoreLocations();
        if (rbc_locations == null) {
            rbc_locations = Collections.emptyMap();
        }
        PrivateNutsTerm.errln("unable to bootstrap nuts (hash %s):", getApiDigest());
        PrivateNutsTerm.errln("%s", extraMessage);
        PrivateNutsTerm.errln("here after current environment info:");
        PrivateNutsTerm.errln("  nuts-boot-api-version            : %s", PrivateNutsUtils.nvl(actualBootConfig.getApiVersion(), "<?> Not Found!"));
        PrivateNutsTerm.errln("  nuts-boot-runtime                : %s", PrivateNutsUtils.nvl(actualBootConfig.getRuntimeId(), "<?> Not Found!"));
        PrivateNutsTerm.errln("  nuts-boot-repositories           : %s", PrivateNutsUtils.nvl(actualBootConfig.getBootRepositories(), "<?> Not Found!"));
        PrivateNutsTerm.errln("  workspace-location               : %s", PrivateNutsUtils.nvl(workspace, "<default-location>"));
        PrivateNutsTerm.errln("  nuts-store-apps                  : %s", rbc_locations.get(NutsStoreLocation.APPS.id()));
        PrivateNutsTerm.errln("  nuts-store-config                : %s", rbc_locations.get(NutsStoreLocation.CONFIG.id()));
        PrivateNutsTerm.errln("  nuts-store-var                   : %s", rbc_locations.get(NutsStoreLocation.VAR.id()));
        PrivateNutsTerm.errln("  nuts-store-log                   : %s", rbc_locations.get(NutsStoreLocation.LOG.id()));
        PrivateNutsTerm.errln("  nuts-store-temp                  : %s", rbc_locations.get(NutsStoreLocation.TEMP.id()));
        PrivateNutsTerm.errln("  nuts-store-cache                 : %s", rbc_locations.get(NutsStoreLocation.CACHE.id()));
        PrivateNutsTerm.errln("  nuts-store-run                   : %s", rbc_locations.get(NutsStoreLocation.RUN.id()));
        PrivateNutsTerm.errln("  nuts-store-lib                   : %s", rbc_locations.get(NutsStoreLocation.LIB.id()));
        PrivateNutsTerm.errln("  nuts-store-strategy              : %s", PrivateNutsUtils.desc(actualBootConfig.getStoreLocationStrategy()));
        PrivateNutsTerm.errln("  nuts-store-layout                : %s", PrivateNutsUtils.desc(actualBootConfig.getStoreLocationLayout()));
        PrivateNutsTerm.errln("  nuts-boot-args                   : %s", options.formatter().getBootCommandLine());
        PrivateNutsTerm.errln("  nuts-app-args                    : %s", Arrays.toString(options.getApplicationArguments()));
        PrivateNutsTerm.errln("  option-read-only                 : %s", options.isReadOnly());
        PrivateNutsTerm.errln("  option-trace                     : %s", options.isTrace());
        PrivateNutsTerm.errln("  option-progress                  : %s", PrivateNutsUtils.desc(options.getProgressOptions()));
        PrivateNutsTerm.errln("  option-open-mode                 : %s", PrivateNutsUtils.desc(options.getOpenMode() == null ? NutsOpenMode.OPEN_OR_CREATE : options.getOpenMode()));

        NutsClassLoaderNode rtn = workspaceInformation.getRuntimeBootDependencyNode();
        String rtHash = "";
        if (rtn != null) {
            rtHash = PrivateNutsUtilDigest.getURLDigest(rtn.getURL());
        }
        PrivateNutsTerm.errln("  nuts-runtime-digest                : %s", rtHash);

        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            PrivateNutsTerm.errln("  nuts-runtime-classpath           : %s", "<none>");
        } else {
            PrivateNutsTerm.errln("  nuts-runtime-hash                : %s", "<none>");
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    PrivateNutsTerm.errln("  nuts-runtime-classpath           : %s", PrivateNutsUtils.formatURL(bootClassWorldURL));
                } else {
                    PrivateNutsTerm.errln("                                     %s", PrivateNutsUtils.formatURL(bootClassWorldURL));
                }
            }
        }
        PrivateNutsTerm.errln("  java-version                     : %s", System.getProperty("java.version"));
        PrivateNutsTerm.errln("  java-executable                  : %s", PrivateNutsUtils.resolveJavaCommand(null));
        PrivateNutsTerm.errln("  java-class-path                  : %s", System.getProperty("java.class.path"));
        PrivateNutsTerm.errln("  java-library-path                : %s", System.getProperty("java.library.path"));
        PrivateNutsTerm.errln("  os-name                          : %s", System.getProperty("os.name"));
        PrivateNutsTerm.errln("  os-arch                          : %s", System.getProperty("os.arch"));
        PrivateNutsTerm.errln("  os-version                       : %s", System.getProperty("os.version"));
        PrivateNutsTerm.errln("  user-name                        : %s", System.getProperty("user.name"));
        PrivateNutsTerm.errln("  user-home                        : %s", System.getProperty("user.home"));
        PrivateNutsTerm.errln("  user-dir                         : %s", System.getProperty("user.dir"));
        PrivateNutsTerm.errln("");
        if (options.getLogConfig() == null
                || options.getLogConfig().getLogTermLevel() == null
                || options.getLogConfig().getLogFileLevel().intValue() > Level.FINEST.intValue()) {
            PrivateNutsTerm.errln("If the problem persists you may want to get more debug info by adding '--verbose' arguments.");
        }
        if (!options.isReset() && !options.isRecover() && options.getExpireTime() == null) {
            PrivateNutsTerm.errln("You may also enable recover mode to ignore existing cache info with '--recover' and '--expire' arguments.");
            PrivateNutsTerm.errln("Here is the proper command : ");
            PrivateNutsTerm.errln("  java -jar nuts.jar --verbose --recover --expire [...]");
        } else if (!options.isReset() && options.isRecover() && options.getExpireTime() != null) {
            PrivateNutsTerm.errln("You may also enable full reset mode to ignore existing configuration with '--reset' argument.");
            PrivateNutsTerm.errln("ATTENTION: this will delete all your nuts configuration. Use it at your own risk.");
            PrivateNutsTerm.errln("Here is the proper command : ");
            PrivateNutsTerm.errln("  java -jar nuts.jar --verbose --reset [...]");
        }
        if (!ths.list().isEmpty()) {
            PrivateNutsTerm.errln("error stack trace is:");
            for (PrivateNutsErrorInfo th : ths.list()) {
                StringBuilder msg = new StringBuilder();
                List<Object> msgParams = new ArrayList<>();
                msg.append("[error]");
                if (th.getNutsId() != null) {
                    msg.append(" <id>=%s");
                    msgParams.add(th.getNutsId());
                }
                if (th.getRepository() != null) {
                    msg.append(" <repo>=%s");
                    msgParams.add(th.getRepository());
                }
                if (th.getUrl() != null) {
                    msg.append(" <url>=%s");
                    msgParams.add(th.getUrl());
                }
                if (th.getThrowable() != null) {
                    msg.append(" <error>=%s");
                    msgParams.add(th.getThrowable().toString());
                } else {
                    msg.append(" <error>=%s");
                    msgParams.add("unexpected error");
                }
                PrivateNutsTerm.errln(msg.toString(), msgParams.toArray());
                PrivateNutsTerm.errln(th.getThrowable());
            }
        } else {
            PrivateNutsTerm.errln("no stack trace is available.");
        }
        PrivateNutsTerm.errln("now exiting nuts, Bye!");
    }

    /**
     * build and return unsatisfied requirements
     *
     * @param unsatisfiedOnly when true return requirements for new instance
     * @return unsatisfied requirements
     */
    private int checkRequirements(boolean unsatisfiedOnly) {
        int req = 0;
        if (!NutsUtilStrings.isBlank(workspaceInformation.getApiVersion())) {
            if (!unsatisfiedOnly || !workspaceInformation.getApiVersion().equals(Nuts.getVersion())) {
                req += 1;
            }
        }
        if (!unsatisfiedOnly || !PrivateNutsUtils.isActualJavaCommand(workspaceInformation.getJavaCommand())) {
            req += 2;
        }
        if (!unsatisfiedOnly || !PrivateNutsUtils.isActualJavaOptions(workspaceInformation.getJavaOptions())) {
            req += 4;
        }
        return req;
    }

    /**
     * return a string representing unsatisfied constraints
     *
     * @param unsatisfiedOnly when true return requirements for new instance
     * @return a string representing unsatisfied constraints
     */
    public String getRequirementsHelpString(boolean unsatisfiedOnly) {
        int req = unsatisfiedOnly ? newInstanceRequirements : checkRequirements(false);
        StringBuilder sb = new StringBuilder();
        if ((req & 1) != 0) {
            sb.append("nuts version ").append(workspaceInformation.getApiId());
        }
        if ((req & 2) != 0) {
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append("java command ").append(workspaceInformation.getJavaCommand());
        }
        if ((req & 4) != 0) {
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append("java options ").append(workspaceInformation.getJavaOptions());
        }
        if (sb.length() > 0) {
            sb.insert(0, "required ");
            return sb.toString();
        }
        return null;
    }

}
