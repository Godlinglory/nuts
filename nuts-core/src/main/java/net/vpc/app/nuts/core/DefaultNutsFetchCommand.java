package net.vpc.app.nuts.core;

import java.io.UncheckedIOException;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.filters.CoreFilterUtils;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyOptionFilter;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyScopeFilter;
import net.vpc.app.nuts.core.filters.repository.DefaultNutsRepositoryFilter;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.NutsDependencyScopes;
import net.vpc.app.nuts.core.util.NutsIdGraph;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.IteratorBuilder;
import net.vpc.app.nuts.core.util.common.TraceResult;

public class DefaultNutsFetchCommand extends DefaultNutsQueryBaseOptions<NutsFetchCommand> implements NutsFetchCommand {

    public static final Logger LOG = Logger.getLogger(DefaultNutsFetchCommand.class.getName());
    private NutsId id;

    public DefaultNutsFetchCommand(NutsWorkspace ws) {
        super(ws, "fetch");
        failFast();
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
        this.id = ws.id().parseRequired(id);
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
            throw new NutsParseException(ws, "Invalid Id format : null");
        }
        this.id = id;
        return this;
    }

    @Override
    public NutsFetchCommand inlineDependencies() {
        return this.inlineDependencies(true);
    }

    @Override
    public NutsDefinition getResultDefinition() {
        try {
            NutsDefinition def = fetchDefinition(id, this);
            return def;
        } catch (NutsNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NutsContent getResultContent() {
        try {
            NutsDefinition def = fetchDefinition(id, copy().setContent(true).setEffective(false).setInstallInformation(false));
            return def.getContent();
        } catch (NutsNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NutsId getResultId() {
        try {
            NutsDefinition def = fetchDefinition(id, this);
            if (isEffective()) {
                return NutsWorkspaceExt.of(ws).resolveEffectiveId(def.getEffectiveDescriptor(), this);
            }
            return def.getId();
        } catch (NutsNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public String getResultContentHash() {
        try {
            Path f = getResultDefinition().getPath();
            return ws.io().hash().source(f).computeString();
        } catch (NutsNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public String getResultDescriptorHash() {
        try {
            return ws.io().hash().source(getResultDescriptor()).computeString();
        } catch (NutsNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NutsDescriptor getResultDescriptor() {
        try {
            NutsDefinition def = fetchDefinition(id, copy().setContent(false).setInstallInformation(false));
            if (isEffective()) {
                return def.getEffectiveDescriptor();
            }
            return def.getDescriptor();
        } catch (NutsNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public Path getResultPath() {
        try {
            NutsDefinition def = fetchDefinition(id, copy().setContent(true).setEffective(false).setInstallInformation(false));
            Path p = def.getPath();
            if (getLocation() != null) {
                return getLocation();
            }
            return p;
        } catch (NutsNotFoundException ex) {
            if (!isFailFast()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NutsFetchCommand copy() {
        DefaultNutsFetchCommand b = new DefaultNutsFetchCommand(ws);
        b.copyFrom(this);
        return b;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsFetchCommand copyFrom(NutsFetchCommand other) {
        super.copyFromDefaultNutsQueryBaseOptions((DefaultNutsQueryBaseOptions) other);
        if (other != null) {
            NutsFetchCommand o = other;
            this.id = o.getId();
        }
        return this;
    }

    protected DefaultNutsDefinition fetchDefinitionBase(NutsId id, NutsFetchCommand options) {
        long startTime = System.currentTimeMillis();
        DefaultNutsDefinition result = null;
        NutsFetchStrategy nutsFetchModes = NutsWorkspaceHelper.validate(options.getFetchStrategy());
        Path cachePath = null;
        {
            cachePath = ws.config().getStoreLocation(id, NutsStoreLocation.CACHE)
                    .resolve(ws.config().getDefaultIdFilename(id.setFace("def.cache")));
            if (Files.isRegularFile(cachePath)) {
                try {
                    DefaultNutsDefinition d = ws.json().parse(cachePath, DefaultNutsDefinition.class);
                    if (d != null) {
                        if (LOG.isLoggable(Level.FINEST)) {
                            CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.CACHED, "Fetch definition", startTime);
                        }
                        return d;
                    }
                } catch (Exception ex) {
                    //
                }
            }
        }
        if (LOG.isLoggable(Level.FINEST)) {
            CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.START, "Fetch definition", 0);
        }
        for (NutsFetchMode mode : nutsFetchModes) {
            try {
                result = fetchDescriptorAsDefinition(id, options, mode);
                if (result != null) {
                    break;
                }
            } catch (NutsNotFoundException | UncheckedIOException ex) {
                //ignore
            } catch (Exception ex) {
                //ignore
                if (LOG.isLoggable(Level.FINEST)) {
                    CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.ERROR, "Fetch def", startTime);
                }
            }
        }
        if (result != null) {
            try {
                ws.json().value(result).print(cachePath);
            } catch (Exception ex) {
                //
            }
        }
        return result;
    }

    public NutsDefinition fetchDefinition(NutsId id, NutsFetchCommand options) {
        long startTime = System.currentTimeMillis();
        NutsWorkspaceUtils.checkLongNameNutsId(ws, id);
        options = NutsWorkspaceUtils.validateSession(ws, options);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsFetchStrategy nutsFetchModes = NutsWorkspaceHelper.validate(options.getFetchStrategy());
        NutsId effectiveId = null;
        DefaultNutsDefinition foundDefinition = null;
        try {
            //add env parameters to fetch adequate nuts
            id = NutsWorkspaceUtils.configureFetchEnv(ws, id);
            foundDefinition = fetchDefinitionBase(id, options);
            if (foundDefinition != null) {
                if (options.isEffective()) {
                    try {
                        foundDefinition.setEffectiveDescriptor(dws.resolveEffectiveDescriptor(foundDefinition.getDescriptor(), options.getSession()));
                    } catch (NutsNotFoundException ex) {
                        //ignore
                        LOG.log(Level.WARNING, "Nuts Descriptor Found, but its parent is not: {0} with parent {1}", new Object[]{id.getLongName(), Arrays.toString(foundDefinition.getDescriptor().getParents())});
                        foundDefinition = null;
                    }
                }
                if (foundDefinition != null) {
                    if (isDependenciesTree()) {
                        int hashId = getScope().hashCode() * 31 + (getOptional() == null ? 0 : getOptional().hashCode());
                        if (effectiveId == null) {
                            effectiveId = dws.resolveEffectiveId(foundDefinition.getDescriptor(), ws.fetch().session(options.getSession()));
                        }
                        NutsDependencyTreeNode[] tree = null;
                        Path cachePath = null;
                        {
                            cachePath = ws.config().getStoreLocation(effectiveId, NutsStoreLocation.CACHE)
                                    .resolve(ws.config().getDefaultIdFilename(effectiveId.setFace(Integer.toHexString(hashId) + ".dep-tree.cache")));
                            if (Files.isRegularFile(cachePath)) {
                                try {
                                    NutsDependencyTreeNode[] d = ws.json().parse(cachePath, NutsDependencyTreeNode[].class);
                                    if (d != null) {
                                        tree = d;
                                    }
                                } catch (Exception ex) {
                                    //
                                }
                            }
                        }
                        if (tree == null) {
                            NutsDependencyFilter scope = getScope().isEmpty() ? null : new NutsDependencyScopeFilter().addScopes(getScope());
                            tree = buildTreeNode(null,
                                    new DefaultNutsDependency(id),
                                    foundDefinition, new HashSet<NutsId>(), getSession().copy().trace(false), scope).getChildren();
                            try {
                                ws.json().setValue(tree).print(cachePath);
                            } catch (Exception ex) {
                                //
                            }
                        }
                        foundDefinition.setDependencyNodes(tree);
                    }
                    if (isDependencies()) {
                        int hashId = getScope().hashCode() * 31 + (getOptional() == null ? 0 : getOptional().hashCode());
                        if (effectiveId == null) {
                            effectiveId = dws.resolveEffectiveId(foundDefinition.getDescriptor(), ws.fetch().session(options.getSession()));
                        }
                        DefaultNutsDependency[] list = null;
                        Path cachePath = null;
                        {
                            Path l = ws.config().getStoreLocation(effectiveId, NutsStoreLocation.CACHE);
                            String nn = ws.config().getDefaultIdFilename(effectiveId.setFace(Integer.toHexString(hashId) + ".dep-list.cache"));
                            cachePath = l.resolve(nn);
                            if (Files.isRegularFile(cachePath)) {
                                try {
                                    DefaultNutsDependency[] d = ws.json().parse(cachePath, DefaultNutsDependency[].class);
                                    if (d != null) {
                                        list = d;
                                    }
                                } catch (Exception ex) {
                                    //
                                }
                            }
                        }
                        if (list == null) {
                            NutsSession _session = this.getSession() == null ? ws.createSession() : this.getSession();
                            NutsDependencyFilter _dependencyFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(
                                    new NutsDependencyScopeFilter().addScopes(getScope()),
                                    getOptional() == null ? null : NutsDependencyOptionFilter.valueOf(getOptional()),
                                    null//getDependencyFilter()
                            ));
                            NutsIdGraph graph = new NutsIdGraph(_session, isFailFast());
                            List<NutsId> ids = Arrays.asList(id);
                            graph.push(ids, _dependencyFilter);
                            NutsId[] pp = graph.collect(ids, ids);
                            list = new DefaultNutsDependency[pp.length];
                            for (int i = 0; i < list.length; i++) {
                                list[i] = new DefaultNutsDependency(pp[i]);
                            }
                            try {
                                ws.json().setValue(list).print(cachePath);
                            } catch (Exception ex) {
                                //
                            }
                        }
                        foundDefinition.setDependencies(list);
                    }
                    boolean includeContent = shouldIncludeContent(options);
                    if (includeContent || options.isInstallInformation()) {
                        NutsId id1 = ws.config().createComponentFaceId(foundDefinition.getId(), foundDefinition.getDescriptor());
                        Path copyTo = options.getLocation();
                        if (copyTo != null && Files.isDirectory(copyTo)) {
                            copyTo = copyTo.resolve(ws.config().getDefaultIdFilename(id1));
                        }
//                        boolean escalateMode = false;
                        for (NutsFetchMode mode : nutsFetchModes) {
                            try {
                                NutsRepository repo = ws.config().getRepository(foundDefinition.getRepositoryUuid(), true);
                                NutsContent content = repo.fetchContent()
                                        .id(id1).descriptor(foundDefinition.getDescriptor())
                                        .localPath(copyTo)
                                        .session(NutsWorkspaceHelper.createRepositorySession(options.getSession(), repo, mode, options))
                                        .run().getResult();
                                if (content != null) {
                                    foundDefinition.setContent(content);
                                    foundDefinition.setDescriptor(resolveExecProperties(foundDefinition.getDescriptor(), content.getPath()));
                                    break;
                                }
                            } catch (NutsNotFoundException ex) {
//                                if (mode.ordinal() < modeForSuccessfulDescRetrieval.ordinal()) {
//                                    //ignore because actually there is more chance to find it in later modes!
//                                } else {
//                                    escalateMode = true;
//                                }
                            }
                        }
                        if (foundDefinition.getContent() == null || foundDefinition.getPath() == null) {
                            CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.ERROR, "Fetched Descriptor but failed to fetch Component", startTime);
                            foundDefinition = null;
//                        } else if (escalateMode) {
//                            CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.ERROR, "Fetched Descriptor with mode escalation", startTime);
                        }
                    }
                    if (foundDefinition != null && options.isInstallInformation()) {
                        NutsInstallInfo ii = dws.getInstalledRepository().getInstallInfo(id);
                        if (ii != null) {
                            foundDefinition.setInstallation(ii);
                        } else {
                            foundDefinition.setInstallation(DefaultNutsWorkspace.NOT_INSTALLED);
                        }
                    }
                }
            }
        } catch (NutsNotFoundException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.ERROR, "Fetch definition", startTime);
            }
            throw ex;
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.ERROR, "Fetch definition", startTime);
            }
            throw ex;
        }
        if (foundDefinition != null) {
//            if (LOG.isLoggable(Level.FINEST)) {
//                CoreNutsUtils.traceMessage(nutsFetchModes, id.getLongNameId(), TraceResult.SUCCESS, "Fetch definition", startTime);
//            }
//            if (isInlineDependencies()) {
//                Set<NutsDependencyScope> s = getScope();
//                if (s == null || s.isEmpty()) {
//                    s = NutsDependencyScopePattern.RUN.expand();
//                }
//                ws.search().addId(id).session(getSession()).setFetchStratery(getFetchStrategy())
//                        .addScopes(s.toArray(new NutsDependencyScope[0]))
//                        .setOptional(getOptional())
//                        .main(false).inlineDependencies().getResultDefinitions();
//
//            }
            if (getValidSession().isTrace()) {
                NutsIterableOutput ff = CoreNutsUtils.getValidOutputFormat(getValidSession())
                        .session(getValidSession());
                ff.start();
                ff.next(foundDefinition);
                ff.complete();
            }
            return foundDefinition;
        }
        throw new NutsNotFoundException(ws, id);
    }

    private boolean shouldIncludeContent(NutsFetchCommand options) {
        boolean includeContent = options.isContent();
        if (options instanceof DefaultNutsQueryBaseOptions) {
            if (((DefaultNutsQueryBaseOptions) options).getDisplayOptions().isRequireDefinition()) {
                includeContent = true;
            }
        }
        return includeContent;
    }

    private NutsDependencyTreeNode buildTreeNode(NutsId from, NutsDependency root, NutsDefinition def, Set<NutsId> visited, NutsSession session, NutsDependencyFilter dependencyFilter) {
        List<NutsDependencyTreeNode> all = new ArrayList<NutsDependencyTreeNode>();
        boolean partial = visited.contains(root.getId().getLongNameId());
        if (!partial) {
            visited.add(root.getId().getLongNameId());
            NutsDependency[] d = def.getDescriptor().getDependencies();
            for (NutsDependency nutsDependency : d) {
                if (dependencyFilter == null || dependencyFilter.accept(null, nutsDependency, session)) {
                    NutsDefinition def2 = ws.search()
                            .id(nutsDependency.getId()).session(session.copy().trace(false).setProperty("monitor-allowed", false)).effective()
                            .content(shouldIncludeContent(this))
                            .latest().getResultDefinitions().first();
                    if (def2 != null) {
                        NutsDependency[] dependencies = CoreFilterUtils.filterDependencies(def2.getDescriptor().getId(), def2.getDescriptor().getDependencies(),
                                dependencyFilter, session);
                        for (NutsDependency dd : dependencies) {
                            if (dd.getVersion().equals(nutsDependency.getVersion())) {
                                dd = dd.setId(dd.getId().setQueryProperty("resolved-version", dd.getVersion().getValue()));
                            }
                            all.add(buildTreeNode(root.getId(), dd, def2, visited, session, dependencyFilter));
                        }
                    }
                }
            }
        }
        return new DefaultNutsDependencyTreeNode(root, all.toArray(new NutsDependencyTreeNode[0]), partial);
    }

    protected NutsDescriptor resolveExecProperties(NutsDescriptor nutsDescriptor, Path jar) {
        boolean executable = nutsDescriptor.isExecutable();
        boolean nutsApp = nutsDescriptor.isNutsApplication();
        if (jar.getFileName().toString().toLowerCase().endsWith(".jar") && Files.isRegularFile(jar)) {
            Path cachePath = ws.config().getStoreLocation(nutsDescriptor.getId(), NutsStoreLocation.CACHE)
                    .resolve(ws.config().getDefaultIdFilename(nutsDescriptor.getId().setFace("info.cache"))
                    );
            Map<String, String> map = null;
            try {
                if (Files.isRegularFile(cachePath)) {
                    map = ws.json().parse(cachePath, Map.class);
                }
            } catch (Exception ex) {
                //
            }
            if (map != null) {
                executable = "true".equals(map.get("executable"));
                nutsApp = "true".equals(map.get("nutsApplication"));
            } else {
                try {
                    NutsExecutionEntry[] t = ws.io().parseExecutionEntries(jar);
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
                        ws.json().value(map).print(cachePath);
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
        NutsRepositoryFilter repositoryFilter = new DefaultNutsRepositoryFilter(Arrays.asList(getRepositories())).simplify();
        if (mode == NutsFetchMode.INSTALLED) {
            if (id.getVersion().isBlank()) {
                String v = dws.getInstalledRepository().getDefaultVersion(id);
                if (v != null) {
                    id = id.setVersion(v);
                } else {
                    id = id.setVersion("");
                }
            }
            NutsVersionFilter versionFilter = id.getVersion().isBlank() ? null : id.getVersion().toFilter();
            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(getValidSession(), null, NutsFetchMode.INSTALLED, new DefaultNutsFetchCommand(ws));
            List<NutsVersion> all = IteratorBuilder.of(dws.getInstalledRepository().findVersions(id, CoreFilterUtils.idFilterOf(versionFilter), rsession))
                    .convert(x -> x.getVersion()).list();
            if (all.size() > 0) {
                all.sort(null);
                id = id.setVersion(all.get(all.size() - 1));
                mode = NutsFetchMode.LOCAL;
            } else {
                throw new NutsNotFoundException(ws, id);
            }
        }
        for (NutsRepository repo : NutsWorkspaceUtils.filterRepositories(ws, NutsRepositorySupportedAction.SEARCH, id, repositoryFilter, mode, options)) {
            try {
                NutsDescriptor descriptor = repo.fetchDescriptor().setId(id).setSession(NutsWorkspaceHelper.createRepositorySession(options.getSession(), repo, mode,
                        options
                )).run().getResult();
                if (descriptor != null) {
                    NutsId nutsId = dws.resolveEffectiveId(descriptor,
                            options);
                    NutsIdBuilder newIdBuilder = nutsId.builder();
                    if (CoreStringUtils.isBlank(newIdBuilder.getNamespace())) {
                        newIdBuilder.setNamespace(repo.config().getName());
                    }
                    //inherit classifier from requested parse
                    String classifier = id.getClassifier();
                    if (!CoreStringUtils.isBlank(classifier)) {
                        newIdBuilder.setClassifier(classifier);
                    }
                    Map<String, String> q = id.getQueryMap();
                    if (!NutsDependencyScopes.isDefaultScope(q.get(NutsConstants.QueryKeys.SCOPE))) {
                        newIdBuilder.setScope(q.get(NutsConstants.QueryKeys.SCOPE));
                    }
                    if (!CoreNutsUtils.isDefaultOptional(q.get(NutsConstants.QueryKeys.OPTIONAL))) {
                        newIdBuilder.setOptional(q.get(NutsConstants.QueryKeys.OPTIONAL));
                    }
                    NutsId newId = newIdBuilder.build();
                    return new DefaultNutsDefinition(
                            repo.getUuid(),
                            repo.config().name(),
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
        throw new NutsNotFoundException(ws, id);
    }

    @Override
    public NutsFetchCommand repositories(Collection<String> value) {
        return addRepositories(value);
    }

    @Override
    public NutsFetchCommand repositories(String... values) {
        return addRepositories(values);
    }

    @Override
    public NutsFetchCommand addRepositories(Collection<String> value) {
        if (value != null) {
            addRepositories(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsFetchCommand run() {
        getResultDefinition();
        return this;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

}
