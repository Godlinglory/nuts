package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.*;
import static net.vpc.app.nuts.core.DefaultNutsWorkspace.NOT_INSTALLED;
import net.vpc.app.nuts.core.filters.repository.DefaultNutsRepositoryFilter;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.TraceResult;
import net.vpc.app.nuts.core.util.common.IteratorBuilder;

public class DefaultNutsFetchCommand extends DefaultNutsQueryBaseOptions<NutsFetchCommand> implements NutsFetchCommand {

    public static final Logger LOG = Logger.getLogger(DefaultNutsFetchCommand.class.getName());
    private NutsId id;

    public DefaultNutsFetchCommand(NutsWorkspace ws) {
        super(ws);
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
    public NutsDefinition getResultDefinition() {
        try {
            NutsDefinition def = fetchDefinition(id, this);
            return def;
        } catch (NutsNotFoundException ex) {
            if (isLenient()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NutsContent getResultContent() {
        try {
            NutsDefinition def = fetchDefinition(id, copy().setIncludeContent(true).setEffective(false).setIncludeInstallInformation(false));
            return def.getContent();
        } catch (NutsNotFoundException ex) {
            if (isLenient()) {
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
            if (isLenient()) {
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
            if (isLenient()) {
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
            if (isLenient()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public NutsDescriptor getResultDescriptor() {
        try {
            NutsDefinition def = fetchDefinition(id, copy().setIncludeContent(false).setIncludeInstallInformation(false));
            if (isEffective()) {
                return def.getEffectiveDescriptor();
            }
            return def.getDescriptor();
        } catch (NutsNotFoundException ex) {
            if (isLenient()) {
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
            if (isLenient()) {
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

    public NutsDefinition fetchDefinition(NutsId id, NutsFetchCommand options) {
        long startTime = System.currentTimeMillis();
        if (id == null) {
            throw new NutsIllegalArgumentException("Missing component Id");
        }
        if (CoreStringUtils.isBlank(id.getGroup())) {
            throw new NutsIllegalArgumentException("Missing Group");
        }
        if (CoreStringUtils.isBlank(id.getName())) {
            throw new NutsIllegalArgumentException("Missing Name");
        }
        if (DefaultNutsVersion.isBlank(id.getVersion().getValue())) {
            throw new NutsIllegalArgumentException("Missing Version");
        }
        options = NutsWorkspaceUtils.validateSession(ws, options);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsFetchStrategy nutsFetchModes = NutsWorkspaceHelper.validate(options.getFetchStrategy());
        if (LOG.isLoggable(Level.FINEST)) {
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
                        LOG.log(Level.WARNING, "Nuts Descriptor Found, but its parent is not: {0} with parent {1}", new Object[]{id.toString(), Arrays.toString(foundDefinition.getDescriptor().getParents())});
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
                        boolean escalateMode = false;
                        for (NutsFetchMode mode : nutsFetchModes) {
                            try {
                                NutsRepository repo = foundDefinition.getRepository();
                                NutsContent content = repo.fetchContent(id1, foundDefinition.getDescriptor(), copyTo,
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
                                    escalateMode = true;
                                }
                            }
                        }
                        if (foundDefinition.getContent() == null || foundDefinition.getPath() == null) {
                            CoreNutsUtils.traceMessage(nutsFetchModes, id, TraceResult.ERROR, "Fetched Descriptor but failed to fetch Component", startTime);
                            foundDefinition = null;
                        } else if (escalateMode) {
                            CoreNutsUtils.traceMessage(nutsFetchModes, id, TraceResult.ERROR, "Fetched Descriptor with mode escalation", startTime);
                        }
                    }
                    if (foundDefinition != null && options.isIncludeInstallInformation()) {
                        NutsInstallerComponent installer = null;
                        if (foundDefinition.getPath() != null) {
                            installer = dws.getInstaller(foundDefinition, options.getSession());
                        }
                        if (installer != null) {
                            if (dws.getInstalledRepository().isInstalled(foundDefinition.getId())) {
                                foundDefinition.setInstallation(new DefaultNutsInstallInfo(true,
                                        dws.getInstalledRepository().isDefaultVersion(foundDefinition.getId()),
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
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(nutsFetchModes, id, TraceResult.ERROR, "Fetch component", startTime);
            }
            throw ex;
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(nutsFetchModes, id, TraceResult.ERROR, "Fetch component", startTime);
            }
            throw ex;
        }
        if (foundDefinition != null) {
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(nutsFetchModes, id, TraceResult.SUCCESS, "Fetch component", startTime);
            }
            if (isIncludeDependencies()) {
                NutsDependencyScope[] s = (getScope() == null || getScope().isEmpty())
                        ? new NutsDependencyScope[]{NutsDependencyScope.PROFILE_RUN}
                        : getScope().toArray(new NutsDependencyScope[0]);
                ws.find().addId(id).session(getSession()).setFetchStratery(getFetchStrategy())
                        .addScopes(s)
                        .setAcceptOptional(getAcceptOptional())
                        .dependenciesOnly().getResultDefinitions();

            }
            if (isTrace()) {
                final PrintStream out = NutsWorkspaceUtils.validateSession(ws, getSession()).getTerminal().getOut();
                getTraceFormat().formatElement(foundDefinition, -1, out, ws);
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
                    map = ws.io().json().read(f, Map.class);
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
                        ws.io().json().pretty().write(map, f);
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
            List<NutsVersion> all = IteratorBuilder.of(dws.getInstalledRepository().findVersions(id, new NutsIdFilter() {
                @Override
                public boolean accept(NutsId id, NutsWorkspace ws) {
                    return versionFilter.accept(id.getVersion());
                }
            }))
                    .convert(x -> x.getVersion()).list();
            if (all.size() > 0) {
                all.sort(null);
                id = id.setVersion(all.get(all.size() - 1));
                mode = NutsFetchMode.LOCAL;
            } else {
                throw new NutsNotFoundException(id);
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
    public NutsFetchCommand parseOptions(String... args) {
        NutsCommandLine cmd = new NutsCommandLine(args);
        NutsCommandArg a;
        while ((a = cmd.next()) != null) {
            switch (a.strKey()) {
                default: {
                    if (!super.parseOption(a, cmd)) {
                        if (a.isOption()) {
                            throw new NutsIllegalArgumentException("find: Unsupported option " + a);
                        } else {
                            id(a.getString());
                        }
                    }
                }
            }
        }
        return this;
    }
}
