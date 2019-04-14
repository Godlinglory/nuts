package net.vpc.app.nuts.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.*;
import static net.vpc.app.nuts.core.DefaultNutsWorkspace.NOT_INSTALLED;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.TraceResult;
import net.vpc.app.nuts.core.util.bundledlibs.util.IteratorBuilder;

public class DefaultNutsFetch extends DefaultNutsQueryBaseOptions<NutsFetchCommand> implements NutsFetchCommand {

    public static final Logger log = Logger.getLogger(DefaultNutsFetch.class.getName());
    private final DefaultNutsWorkspace ws;
    private NutsId id;
    private boolean lenient;

    public DefaultNutsFetch(DefaultNutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsFetchCommand id(String id) {
        return setId(id);
    }

    @Override
    public NutsFetchCommand id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsFetchCommand setId(String id) {
        this.id = ws.parser().parseRequiredId(id);
        return this;
    }

    @Override
    public NutsFetchCommand nutsApi() {
        return setId(ws.config().getApiId());
    }

    @Override
    public NutsFetchCommand nutsRuntime() {
        return setId(ws.config().getRuntimeId());
    }

    @Override
    public NutsFetchCommand setId(NutsId id) {
        if (id == null) {
            throw new NutsParseException("Invalid Id format : null");
        }
        this.id = id;
        return this;
    }

    @Override
    public NutsFetchCommand includeDependencies() {
        return this.includeDependencies(true);
    }

    @Override
    public NutsFetchCommand setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    @Override
    public NutsFetchCommand lenient(boolean lenient) {
        return setLenient(lenient);
    }

    @Override
    public NutsFetchCommand lenient() {
        return lenient(true);
    }

    @Override
    public NutsDefinition getResultDefinition() {
        try {
            NutsDefinition def = fetchDefinition(id, this);
            loadDeps(def.getId());
            return def;
        } catch (NutsNotFoundException ex) {
            if (lenient) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NutsContent getResultContent() {
        try {
            NutsDefinition def = fetchDefinition(id, copy().setIncludeContent(true).setEffective(false).setIncludeInstallInformation(false));
            loadDeps(def.getId());
            return def.getContent();
        } catch (NutsNotFoundException ex) {
            if (lenient) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NutsId getResultId() {
        try {
            NutsDefinition def = fetchDefinition(id, this);
            loadDeps(def.getId());
            if (isEffective()) {
                return NutsWorkspaceExt.of(ws).resolveEffectiveId(def.getEffectiveDescriptor(), this);
            }
            return def.getId();
        } catch (NutsNotFoundException ex) {
            if (lenient) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public String getResultContentHash() {
        try {
            Path f = getResultDefinition().getPath();
            try {
                try (InputStream in = Files.newInputStream(f)) {
                    return ws.io().computeHash((in));
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } catch (NutsNotFoundException ex) {
            if (lenient) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public String getResultDescriptorHash() {
        try {
            return ws.io().computeHash(new ByteArrayInputStream(
                    ws.formatter().createDescriptorFormat().toString(getResultDescriptor()).getBytes()
            ));
        } catch (NutsNotFoundException ex) {
            if (lenient) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NutsDescriptor getResultDescriptor() {
        try {
            NutsDefinition def = fetchDefinition(id, copy().setIncludeContent(false).setIncludeInstallInformation(false));
            loadDeps(def.getId());
            if (isEffective()) {
                return def.getEffectiveDescriptor();
            }
            return def.getDescriptor();
        } catch (NutsNotFoundException ex) {
            if (lenient) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public Path getResultPath() {
        try {
            NutsDefinition def = fetchDefinition(id, copy().setIncludeContent(true).setEffective(false).setIncludeInstallInformation(false));
            Path p = def.getPath();
            if (getLocation() != null) {
                return getLocation();
            }
            return p;
        } catch (NutsNotFoundException ex) {
            if (lenient) {
                return null;
            }
            throw ex;
        }
    }

    private void loadDeps(NutsId id) {
        if (isIncludeDependencies()) {
            NutsDependencyScope[] s = (getScope() == null || getScope().isEmpty())
                    ? new NutsDependencyScope[]{NutsDependencyScope.PROFILE_RUN}
                    : getScope().toArray(new NutsDependencyScope[0]);
            ws.find().addId(id).session(getSession()).setFetchStratery(getFetchStrategy())
                    .addScopes(s)
                    .setAcceptOptional(getAcceptOptional())
                    .dependenciesOnly().getResultDefinitions();

        }
    }

    @Override
    public NutsFetchCommand copy() {
        DefaultNutsFetch b = new DefaultNutsFetch(ws);
        b.copyFrom(this);
        return b;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsFetchCommand copyFrom(NutsFetchCommand other) {
        super.copyFrom0((DefaultNutsQueryBaseOptions) other);
        if (other != null) {
            NutsFetchCommand o = other;
            this.id = o.getId();
        }
        return this;
    }

    public NutsDefinition fetchDefinition(NutsId id, NutsFetchCommand options) {
        long startTime = System.currentTimeMillis();
        options = NutsWorkspaceUtils.validateSession(ws, options);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsFetchStrategy nutsFetchModes = NutsWorkspaceHelper.validate(options.getFetchStrategy());
        if (log.isLoggable(Level.FINEST)) {
            CoreNutsUtils.traceMessage(nutsFetchModes, id, TraceResult.START, "Fetch component", 0);
        }
        DefaultNutsDefinition foundDefinition = null;
        try {
            //add env parameters to fetch adequate nuts
            id = NutsWorkspaceUtils.configureFetchEnv(ws, id);
            NutsFetchMode modeForSuccessfulDescRetreival = null;
            //use
            for (NutsFetchMode mode : nutsFetchModes) {
                try {
                    if (id.getGroup() == null) {
                        String[] groups = ws.config().getImports();
                        for (String group : groups) {
                            try {
                                foundDefinition = fetchDescriptorAsDefinition(id.setGroup(group), options, mode);
                                if (foundDefinition != null) {
                                    break;
                                }
                            } catch (NutsNotFoundException ex) {
                                //not found
                            }
                        }
                        if (foundDefinition != null) {
                            modeForSuccessfulDescRetreival = mode;
                            break;
                        }
                        throw new NutsNotFoundException(id);
                    }
                    foundDefinition = fetchDescriptorAsDefinition(id, options, mode);
                    if (foundDefinition != null) {
                        modeForSuccessfulDescRetreival = mode;
                        break;
                    }
                } catch (NutsNotFoundException ex) {
                    //ignore
                }
            }
            if (foundDefinition != null) {
                if (options.isEffective()) {
                    try {
                        foundDefinition.setEffectiveDescriptor(dws.resolveEffectiveDescriptor(foundDefinition.getDescriptor(), options.getSession()));
                    } catch (NutsNotFoundException ex) {
                        //ignore
                        log.log(Level.WARNING, "Nuts Descriptor Found, but its parent is not: {0} with parent {1}", new Object[]{id.toString(), Arrays.toString(foundDefinition.getDescriptor().getParents())});
                        foundDefinition = null;
                    }
                }
                if (foundDefinition != null) {
                    if (options.isIncludeContent() || options.isIncludeInstallInformation()) {
                        NutsId id1 = ws.config().createComponentFaceId(foundDefinition.getId(), foundDefinition.getDescriptor());
                        Path copyTo = options.getLocation();
                        if (copyTo != null && Files.isDirectory(copyTo)) {
                            copyTo = copyTo.resolve(ws.config().getDefaultIdFilename(id1));
                        }
                        for (NutsFetchMode mode : nutsFetchModes) {
                            try {
                                NutsRepository repo = foundDefinition.getRepository();
                                NutsContent content = repo.fetchContent(id1, copyTo,
                                        NutsWorkspaceHelper.createRepositorySession(options.getSession(), repo, mode, options));
                                if (content != null) {
                                    foundDefinition.setContent(content);
                                    foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), content.getPath()));
                                    break;
                                }
                            } catch (NutsNotFoundException ex) {
                                if (mode.ordinal() < modeForSuccessfulDescRetreival.ordinal()) {
                                    //ignore because actually there is more chance to find it in later modes!
                                } else {
                                    log.log(Level.WARNING, "Nuts Descriptor Found, but component could not be resolved : {0}", id.toString());
                                }
                            }
                        }
                        if (foundDefinition.getContent() == null || foundDefinition.getPath() == null) {
                            CoreNutsUtils.traceMessage(nutsFetchModes, id, TraceResult.ERROR, "Fetched Descriptor but failed to fetch Component", startTime);
                            foundDefinition = null;
                        }
                    }
                    if (foundDefinition != null && options.isIncludeInstallInformation()) {
                        NutsInstallerComponent installer = null;
                        if (foundDefinition.getPath() != null) {
                            installer = dws.getInstaller(foundDefinition, options.getSession());
                        }
                        if (installer != null) {
                            if (dws.getInstalledRepository().isInstalled(foundDefinition.getId())) {
                                foundDefinition.setInstallation(new NutsInstallInfo(true,
                                        ws.config().getStoreLocation(foundDefinition.getId(), NutsStoreLocation.PROGRAMS)
                                ));
                            } else {
                                foundDefinition.setInstallation(NOT_INSTALLED);
                            }
                        } else {
                            foundDefinition.setInstallation(NOT_INSTALLED);
                        }
                    }
                }
            }
        } catch (NutsNotFoundException ex) {
            if (log.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(nutsFetchModes, id, TraceResult.ERROR, "Fetch component", startTime);
            }
            throw ex;
        } catch (RuntimeException ex) {
            if (log.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(nutsFetchModes, id, TraceResult.ERROR, "Fetch component", startTime);
            }
            throw ex;
        }
        if (foundDefinition != null) {
            if (log.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(nutsFetchModes, id, TraceResult.SUCCESS, "Fetch component", startTime);
            }
            return foundDefinition;
        }
        throw new NutsNotFoundException(id);
    }

    protected NutsDescriptor resolveExecProperties(NutsDescriptor nutsDescriptor, Path jar) {
        boolean executable = nutsDescriptor.isExecutable();
        boolean nutsApp = nutsDescriptor.isNutsApplication();
        if (jar.getFileName().toString().toLowerCase().endsWith(".jar") && Files.isRegularFile(jar)) {
            Path f = ws.config().getStoreLocation(nutsDescriptor.getId(), NutsStoreLocation.CACHE).resolve(ws.config().getDefaultIdFilename(nutsDescriptor.getId().setFace("cache-info"))
            );
            Map<String, String> map = null;
            try {
                if (Files.isRegularFile(f)) {
                    map = ws.io().readJson(f, Map.class);
                }
            } catch (Exception ex) {
                //
            }
            if (map != null) {
                executable = "true".equals(map.get("executable"));
                nutsApp = "true".equals(map.get("nutsApplication"));
            } else {
                try {
                    NutsExecutionEntry[] t = ws.parser().parseExecutionEntries(jar);
                    if (t.length > 0) {
                        executable = true;
                        if (t[0].isApp()) {
                            nutsApp = true;
                        }
                    }
                    try {
                        map = new LinkedHashMap<>();
                        map.put("executable", String.valueOf(executable));
                        map.put("nutsApplication", String.valueOf(nutsApp));
                        ws.io().writeJson(map, f, true);
                    } catch (Exception ex) {
                        //
                    }
                } catch (Exception ex) {
                    //
                }
            }
        }
        nutsDescriptor = nutsDescriptor.setExecutable(executable);
        nutsDescriptor = nutsDescriptor.setNutsApplication(nutsApp);

        return nutsDescriptor;
    }

    protected DefaultNutsDefinition fetchDescriptorAsDefinition(NutsId id, NutsFetchCommand options, NutsFetchMode mode) {
        options = NutsWorkspaceUtils.validateSession(ws, options);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsRepositoryFilter repositoryFilter = null;
        if (mode == NutsFetchMode.INSTALLED) {
            if (id.getVersion().isBlank()) {
                String v = dws.getInstalledRepository().getDefaultVersion(id);
                if (v != null) {
                    id = id.setVersion(v);
                } else {
                    id = id.setVersion("");
                }
            }
            if (id.getVersion().isBlank()) {
                List<NutsVersion> all = IteratorBuilder.of(dws.getInstalledRepository().findVersions(id, null))
                        .convert(x -> x.getVersion()).list();
//                all.sort();
                if (all.size() > 0) {
                    all.sort(null);
                    id = id.setVersion(all.get(all.size() - 1));
                    mode = NutsFetchMode.LOCAL;
                } else {
                    throw new NutsNotFoundException(id);
                }
            }
        }
        for (NutsRepository repo : NutsWorkspaceUtils.filterRepositories(ws, NutsRepositorySupportedAction.FIND, id, repositoryFilter, mode, options)) {
            try {
                NutsDescriptor descriptor = repo.fetchDescriptor(id, NutsWorkspaceHelper.createRepositorySession(options.getSession(), repo, mode,
                        options
                ));
                if (descriptor != null) {
                    NutsId nutsId = dws.resolveEffectiveId(descriptor,
                            options);
                    NutsIdBuilder newIdBuilder = nutsId.builder();
                    if (CoreStringUtils.isBlank(newIdBuilder.getNamespace())) {
                        newIdBuilder.setNamespace(repo.config().getName());
                    }
                    //inherit classifier from requested id
                    String classifier = id.getClassifier();
                    if (!CoreStringUtils.isBlank(classifier)) {
                        newIdBuilder.setClassifier(classifier);
                    }
                    Map<String, String> q = id.getQueryMap();
                    if (!CoreNutsUtils.isDefaultScope(q.get(NutsConstants.QueryKeys.SCOPE))) {
                        newIdBuilder.setScope(q.get(NutsConstants.QueryKeys.SCOPE));
                    }
                    if (!CoreNutsUtils.isDefaultOptional(q.get(NutsConstants.QueryKeys.OPTIONAL))) {
                        newIdBuilder.setOptional(q.get(NutsConstants.QueryKeys.OPTIONAL));
                    }
                    NutsId newId = newIdBuilder.build();
                    return new DefaultNutsDefinition(
                            ws,
                            repo,
                            newId,
                            descriptor,
                            null,
                            null
                    );
                }
            } catch (NutsNotFoundException exc) {
                //
            }
        }
        throw new NutsNotFoundException(id);
    }
}
