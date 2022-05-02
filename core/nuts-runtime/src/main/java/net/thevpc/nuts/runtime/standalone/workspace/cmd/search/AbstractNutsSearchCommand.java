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
package net.thevpc.nuts.runtime.standalone.workspace.cmd.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNutsClassLoader;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNutsQueryBaseOptions;
import net.thevpc.nuts.runtime.standalone.format.NutsDisplayProperty;
import net.thevpc.nuts.runtime.standalone.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.NutsIdFormatHelper;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNutsWorkspaceExtensionManager;
import net.thevpc.nuts.runtime.standalone.dependency.util.NutsClassLoaderUtils;
import net.thevpc.nuts.runtime.standalone.stream.NutsIteratorStream;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;

import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public abstract class AbstractNutsSearchCommand extends DefaultNutsQueryBaseOptions<NutsSearchCommand> implements NutsSearchCommand {

    protected final List<String> arch = new ArrayList<>();
    protected final List<NutsId> ids = new ArrayList<>();
    protected final List<NutsId> lockedIds = new ArrayList<>();
    protected final List<String> scripts = new ArrayList<>();
    protected final List<String> packaging = new ArrayList<>();
    protected NutsComparator comparator;
    protected NutsDescriptorFilter descriptorFilter;
    protected NutsIdFilter idFilter;
    protected boolean latest = false;
    protected boolean distinct = false;
    protected boolean includeBasePackage = true;
    protected boolean sorted = false;
    protected Boolean defaultVersions = null;
    protected String execType = null;
    protected NutsVersion targetApiVersion = null;
    protected NutsInstallStatusFilter installStatus;

    public AbstractNutsSearchCommand(NutsWorkspace ws) {
        super(ws, "search");
    }

    @Override
    public NutsSearchCommand clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NutsSearchCommand addId(String id) {
        checkSession();
        if (!NutsBlankable.isBlank(id)) {
            ids.add(NutsId.of(id).get(getSession()));
        }
        return this;
    }

    @Override
    public NutsSearchCommand addId(NutsId id) {
        if (id != null) {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NutsSearchCommand addIds(String... values) {
        checkSession();
        if (values != null) {
            for (String s : values) {
                if (!NutsBlankable.isBlank(s)) {
                    ids.add(NutsId.of(s).get(getSession()));
                }
            }
        }
        return this;
    }

    @Override
    public NutsSearchCommand addIds(NutsId... value) {
        if (value != null) {
            for (NutsId s : value) {
                if (s != null) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NutsSearchCommand removeId(String id) {
        checkSession();
        ids.remove(NutsId.of(id).get(getSession()));
        return this;
    }

    @Override
    public NutsSearchCommand removeId(NutsId id) {
        if (id != null) {
            removeId(id.toString());
        }
        return this;
    }

    @Override
    public boolean isRuntime() {
        return "runtime".equals(execType);
    }

    @Override
    public NutsSearchCommand setRuntime(boolean enable) {
        this.execType = enable ? "runtime" : null;
        return this;
    }

    @Override
    public boolean isCompanion() {
        return "companion".equals(execType);
    }

    @Override
    public NutsSearchCommand setCompanion(boolean enable) {
        this.execType = enable ? "companion" : null;
        return this;
    }

    @Override
    public boolean isExtension() {
        return "extension".equals(execType);
    }

    @Override
    public NutsSearchCommand setExtension(boolean enable) {
        this.execType = enable ? "extension" : null;
        return this;
    }

    @Override
    public boolean isExec() {
        return "exec".equals(execType);
    }

    @Override
    public NutsSearchCommand setExec(boolean enable) {
        this.execType = enable ? "exec" : null;
        return this;
    }

    @Override
    public boolean isApplication() {
        return "app".equals(execType);
    }

    @Override
    public NutsSearchCommand setApplication(boolean enable) {
        this.execType = enable ? "app" : null;
        return this;
    }

    @Override
    public boolean isLib() {
        return "lib".equals(execType);
    }

    @Override
    public NutsSearchCommand setLib(boolean enable) {
        this.execType = enable ? "lib" : null;
        return this;
    }

    @Override
    public NutsSearchCommand addScript(String value) {
        if (value != null) {
            scripts.add(value);
        }
        return this;
    }

    @Override
    public NutsSearchCommand removeScript(String value) {
        scripts.remove(value);
        return this;
    }

    @Override
    public NutsSearchCommand addScripts(Collection<String> value) {
        if (value != null) {
            addScripts(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsSearchCommand addScripts(String... value) {
        if (value != null) {
            scripts.addAll(Arrays.asList(value));
        }
        return this;

    }

    @Override
    public NutsSearchCommand clearScripts() {
        scripts.clear();
        return this;
    }

    @Override
    public List<String> getScripts() {
        return scripts;
    }

    @Override
    public NutsSearchCommand clearArch() {
        this.arch.clear();
        return this;
    }

    @Override
    public NutsSearchCommand addLockedIds(String... values) {
        checkSession();
        if (values != null) {
            for (String s : values) {
                if (!NutsBlankable.isBlank(s)) {
                    lockedIds.add(NutsId.of(s).get(getSession()));
                }
            }
        }
        return this;
    }

    @Override
    public NutsSearchCommand addLockedIds(List<NutsId> values) {
        return addLockedIds(values.toArray(new NutsId[0]));
    }

    @Override
    public NutsSearchCommand addLockedIds(NutsId... values) {
        if (values != null) {
            for (NutsId s : values) {
                if (s != null) {
                    lockedIds.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NutsSearchCommand clearLockedIds() {
        lockedIds.clear();
        return this;
    }

    @Override
    public NutsSearchCommand addArch(String value) {
        if (!NutsBlankable.isBlank(value)) {
            this.arch.add(value);
        }
        return this;
    }

    @Override
    public NutsSearchCommand removeArch(String value) {
        this.arch.remove(value);
        return this;
    }

    @Override
    public NutsSearchCommand addArch(Collection<String> values) {
        if (values != null) {
            addArch(values.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsSearchCommand addArch(String... values) {
        if (values != null) {
            arch.addAll(Arrays.asList(values));
        }
        return this;
    }

    @Override
    public NutsSearchCommand clearPackaging() {
        packaging.clear();
        return this;
    }

    @Override
    public NutsSearchCommand addPackaging(Collection<String> values) {
        if (values != null) {
            addPackaging(values.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsSearchCommand addPackaging(String... values) {
        if (values != null) {
            this.packaging.addAll(Arrays.asList(values));
        }
        return this;
    }

    @Override
    public NutsSearchCommand addPackaging(String value) {
        if (value != null) {
            packaging.add(value);
        }
        return this;
    }

    @Override
    public NutsSearchCommand removePackaging(String value) {
        packaging.remove(value);
        return this;
    }

    @Override
    public NutsSearchCommand addLockedId(NutsId id) {
        if (id != null) {
            addLockedId(id.toString());
        }
        return this;
    }

    @Override
    public NutsSearchCommand removeLockedId(NutsId id) {
        if (id != null) {
            removeLockedId(id.toString());
        }
        return this;
    }

    @Override
    public NutsSearchCommand removeLockedId(String id) {
        checkSession();
        lockedIds.remove(NutsId.of(id).get(getSession()));
        return this;
    }

    @Override
    public NutsSearchCommand addLockedId(String id) {
        checkSession();
        if (!NutsBlankable.isBlank(id)) {
            lockedIds.add(NutsId.of(id).get(getSession()));
        }
        return this;
    }

    @Override
    public List<NutsId> getLockedIds() {
        return this.lockedIds;
    }


    @Override
    public NutsSearchCommand sort(NutsComparator comparator) {
        this.comparator = comparator;
        this.sorted = true;
        return this;
    }

    @Override
    public NutsSearchCommand copyFrom(NutsSearchCommand other) {
        super.copyFromDefaultNutsQueryBaseOptions((DefaultNutsQueryBaseOptions) other);
        if (other != null) {
            NutsSearchCommand o = other;
            this.comparator = o.getComparator();
            this.descriptorFilter = o.getDescriptorFilter();
            this.idFilter = o.getIdFilter();
            this.latest = o.isLatest();
            this.distinct = (o.isDistinct());
            this.includeBasePackage = o.isBasePackage();
            this.sorted = o.isSorted();
            this.arch.clear();
            this.arch.addAll(o.getArch());
            this.ids.clear();
            this.ids.addAll(o.getIds());
            this.scripts.clear();
            this.scripts.addAll(o.getScripts());
            this.packaging.clear();
            this.packaging.addAll(o.getPackaging());
            this.installStatus = other.getInstallStatus();
        }
        return this;
    }

    @Override
    public NutsSearchCommand copyFrom(NutsFetchCommand other) {
        super.copyFromDefaultNutsQueryBaseOptions((DefaultNutsQueryBaseOptions) other);
        return this;
    }

    @Override
    public List<NutsId> getIds() {
        return this.ids;
    }

    @Override
    public NutsSearchCommand setIds(String... ids) {
        clearIds();
        addIds(ids);
        return this;
    }

    @Override
    public NutsSearchCommand setIds(NutsId... ids) {
        clearIds();
        addIds(ids);
        return this;
    }

    @Override
    public boolean isSorted() {
        return sorted;
    }

    @Override
    public NutsSearchCommand setSorted(boolean sort) {
        this.sorted = sort;
        return this;
    }

    @Override
    public NutsDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    @Override
    public NutsSearchCommand setDescriptorFilter(NutsDescriptorFilter filter) {
        this.descriptorFilter = filter;
        return this;
    }

    @Override
    public NutsSearchCommand setDescriptorFilter(String filter) {
        checkSession();
        this.descriptorFilter = NutsDescriptorFilters.of(session).parse(filter);
        return this;
    }

    @Override
    public NutsIdFilter getIdFilter() {
        return idFilter;
    }

    @Override
    public NutsSearchCommand setIdFilter(NutsIdFilter filter) {
        this.idFilter = filter;
        return this;
    }

    @Override
    public NutsSearchCommand setIdFilter(String filter) {
        checkSession();
        this.idFilter = NutsIdFilters.of(getSession()).parse(filter);
        return this;
    }

    @Override
    public List<String> getArch() {
        return arch;
    }

    @Override
    public List<String> getPackaging() {
        return this.packaging;
    }

    @Override
    public NutsComparator getComparator() {
        return comparator;
    }

    //    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder("NutsSearch{");
//        sb.append(getScope());
//        if (ids.size() > 0) {
//            sb.append(",ids=").append(ids);
//        }
//        if (lockedIds.size() > 0) {
//            sb.append(",lockedIds=").append(lockedIds);
//        }
//        if (idFilter != null) {
//            sb.append(",idFilter=").append(idFilter);
//        }
//        if (dependencyFilter != null) {
//            sb.append(",dependencyFilter=").append(dependencyFilter);
//        }
//        if (repositoryFilter != null) {
//            sb.append(",repositoryFilter=").append(repositoryFilter);
//        }
//        if (descriptorFilter != null) {
//            sb.append(",descriptorFilter=").append(descriptorFilter);
//        }
//        sb.append('}');
//        return sb.toString();
//    }
    @Override
    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public NutsSearchCommand setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    @Override
    public NutsVersion getTargetApiVersion() {
        return targetApiVersion;
    }

    @Override
    public NutsSearchCommand setTargetApiVersion(NutsVersion targetApiVersion) {
        this.targetApiVersion = targetApiVersion;
        return this;
    }

    @Override
    public boolean isBasePackage() {
        return includeBasePackage;
    }

    @Override
    public NutsSearchCommand setBasePackage(boolean includeBasePackage) {
        this.includeBasePackage = includeBasePackage;
        return this;
    }

    @Override
    public boolean isLatest() {
        return latest;
    }

    @Override
    public NutsSearchCommand setLatest(boolean enable) {
        this.latest = enable;
        return this;
    }

    @Override
    public NutsStream<NutsId> getResultIds() {
        return buildCollectionResult(getResultIdIteratorBase(null));
    }

    @Override
    public NutsStream<NutsDependencies> getResultDependencies() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(true, isEffective()), session)
                .map(NutsFunction.of(NutsDefinition::getDependencies, "getDependencies"))
        );
    }

    @Override
    public NutsStream<NutsDependency> getResultInlineDependencies() {
        return buildCollectionResult(
                IteratorBuilder.of(getResultIdIteratorBase(true), session).map(
                                NutsFunction.of(NutsId::toDependency, "Id->Dependency"))
                        .build()
        );
    }

    @Override
    public NutsStream<NutsDefinition> getResultDefinitions() {
        return buildCollectionResult(getResultDefinitionIteratorBase(isContent(), isEffective()));
    }

    @Override
    public ClassLoader getResultClassLoader() {
        return getResultClassLoader(null);
    }

    @Override
    public ClassLoader getResultClassLoader(ClassLoader parent) {
        checkSession();

        //force content and dependencies!
        setContent(true);
        setDependencies(true);

        List<NutsDefinition> nutsDefinitions = getResultDefinitions().toList();
        URL[] allURLs = new URL[nutsDefinitions.size()];
        NutsId[] allIds = new NutsId[nutsDefinitions.size()];
        for (int i = 0; i < allURLs.length; i++) {
            NutsDefinition d = nutsDefinitions.get(i);
            allURLs[i] = d.getPath() == null ? null : d.getPath().toURL();
            allIds[i] = d.getId();
        }
        DefaultNutsClassLoader cl = ((DefaultNutsWorkspaceExtensionManager) getSession().extensions())
                .getModel().getNutsURLClassLoader("SEARCH-" + UUID.randomUUID(), parent, getSession());
        for (NutsDefinition def : nutsDefinitions) {
            cl.add(NutsClassLoaderUtils.definitionToClassLoaderNode(def, getSession()));
        }
        return cl;
    }

    @Override
    public String getResultNutsPath() {
        return getResultIds().toList().stream().map(NutsId::getLongName).collect(Collectors.joining(";"));
    }

    @Override
    public String getResultClassPath() {
        StringBuilder sb = new StringBuilder();
        NutsIterator<NutsDefinition> it = getResultDefinitionIteratorBase(true, isEffective());
        while (it.hasNext()) {
            NutsDefinition nutsDefinition = it.next();
            if (nutsDefinition.getFile() != null) {
                if (sb.length() > 0) {
                    sb.append(File.pathSeparator);
                }
                sb.append(nutsDefinition.getFile());
            }
        }
        return sb.toString();
    }

    /**
     * @return default version or null
     * @since 0.5.5
     */
    @Override
    public Boolean getDefaultVersions() {
        return defaultVersions;
    }

    /**
     * @param acceptDefaultVersion acceptDefaultVersion
     * @return {@code this} instance
     * @since 0.5.5
     */
    @Override
    public NutsSearchCommand setDefaultVersions(Boolean acceptDefaultVersion) {
        this.defaultVersions = acceptDefaultVersion;
        return this;
    }

    @Override
    public NutsStream<String> getResultPaths() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(true, isEffective()), session)
                .map(
                        NutsFunction.of(x -> (x.getContent() == null || x.getContent().getPath() == null) ? null : x.getContent().getPath().toString(), "getPath")
                )
                .notBlank()
        );
    }

    @Override
    public NutsStream<String> getResultPathNames() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(true, isEffective()), session)
                .map(NutsFunction.of(x -> (x.getContent() == null || x.getContent().getPath() == null) ? null : x.getContent().getPath().getName(), "getName"))
                .notBlank());
    }

    @Override
    public NutsStream<Instant> getResultInstallDates() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .map(NutsFunction.of(x -> (x.getInstallInformation() == null) ? null : x.getInstallInformation().getCreatedInstant(), "getCreatedInstant"))
                .notNull());
    }

    @Override
    public NutsStream<String> getResultInstallUsers() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .map(NutsFunction.of(x -> (x.getInstallInformation() == null) ? null : x.getInstallInformation().getInstallUser(), "getInstallUser"))
                .notBlank());
    }

    @Override
    public NutsStream<NutsPath> getResultInstallFolders() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .map(NutsFunction.of(x -> (x.getInstallInformation() == null) ? null : x.getInstallInformation().getInstallFolder(), "getInstallFolder"))
                .notNull());
    }

    @Override
    public NutsStream<NutsPath> getResultStoreLocations(NutsStoreLocation location) {
        checkSession();
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .map(NutsFunction.of(x -> getSession().locations().getStoreLocation(x.getId(), location), "getStoreLocation(" + location.id() + ")"))
                .notNull());
    }

    @Override
    public NutsStream<String[]> getResultStrings(String[] columns) {
        NutsFetchDisplayOptions oo = new NutsFetchDisplayOptions(getSession());
        oo.addDisplay(columns);
        oo.setIdFormat(getDisplayOptions().getIdFormat());
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .map(NutsFunction.of(x
                                -> NutsIdFormatHelper.of(x, getSearchSession())
                                .buildLong().getMultiColumnRowStrings(oo),
                        "getColumns")
                ));
    }

    @Override
    public NutsStream<String> getResultNames() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NutsFunction.of(x -> Arrays.asList(x.getDescriptor().getName()), "getDescriptorName"))
                .notBlank());
    }

    @Override
    public NutsStream<String> getResultOs() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NutsFunction.of(x -> Arrays.asList(x.getDescriptor().getCondition().getOs()), "getOs"))
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NutsStream<NutsExecutionEntry> getResultExecutionEntries() {
        checkSession();
        IteratorBuilder<NutsDefinition> defIter = IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session);
        return postProcessResult(defIter
                .mapMulti(
                        NutsFunction.of(
                                x -> (x.getContent() == null || x.getContent().getFile() == null) ? Collections.emptyList()
                                        : NutsExecutionEntries.of(getSession()).parse(x.getContent().getFile()),
                                "getFile"
                        )));
    }

    @Override
    public NutsStream<String> getResultOsDist() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NutsFunction.of(x -> Arrays.asList(x.getDescriptor().getCondition().getOsDist()), "getOsDist"))
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NutsStream<String> getResultPackaging() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NutsFunction.of(x -> Arrays.asList(x.getDescriptor().getPackaging()), "getPackaging"))
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NutsStream<String> getResultPlatform() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NutsFunction.of(x -> Arrays.asList(x.getDescriptor().getCondition().getPlatform()), "getPlatform"))
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NutsStream<String> getResultProfile() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NutsFunction.of(x -> Arrays.asList(x.getDescriptor().getCondition().getProfile()), "getProfile"))
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NutsStream<String> getResultDesktopEnvironment() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NutsFunction.of(x -> Arrays.asList(x.getDescriptor().getCondition().getDesktopEnvironment()), "getDesktopEnvironment"))
                .notBlank()
                .distinct()
        );
    }

    @Override
    public NutsStream<String> getResultArch() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .mapMulti(NutsFunction.of(x -> Arrays.asList(x.getDescriptor().getCondition().getArch()), "getArch"))
                .notBlank());
    }

    @Override
    public NutsInstallStatusFilter getInstallStatus() {
        return installStatus;
    }

    @Override
    public NutsSearchCommand setInstallStatus(NutsInstallStatusFilter installStatus) {
        this.installStatus = installStatus;
        return this;
    }

    @Override
    public NutsSearchCommand setId(String id) {
        clearIds();
        addId(id);
        return this;
    }

    @Override
    public NutsSearchCommand setId(NutsId id) {
        clearIds();
        addId(id);
        return this;
    }

    public String getExecType() {
        return execType;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch (a.getKey().asString().get(session)) {
            case "--inline-dependencies": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    this.setInlineDependencies(val);
                }
                return true;
            }
            case "-L":
            case "--latest":
            case "--latest-versions": {
                cmdLine.skip();
                if (enabled) {
                    this.setLatest(true);
                }
                return true;
            }
            case "--distinct": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    this.setDistinct(val);
                }
                return true;
            }
            case "--default":
            case "--default-versions": {
                Boolean val = cmdLine.nextBooleanValueLiteral().ifError(false).orElse(null);
                if (enabled) {
                    this.setDefaultVersions(val);
                }
                return true;
            }
            case "--duplicates": {
                boolean val = !cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    this.setDistinct(val);
                }
                return true;
            }
            case "-s":
            case "--sort": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    this.setSorted(val);
                }
                return true;
            }
            case "--base": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    this.includeBasePackage = val;
                }
                return true;
            }
            case "--lib":
            case "--libs": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    this.setLib(val);
                }
                return true;
            }
            case "--app":
            case "--apps": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    this.setExec(val);
                }
                return true;
            }
            case "--companion":
            case "--companions": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    this.setCompanion(val);
                }
                return true;
            }
            case "--extension":
            case "--extensions": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    this.setExtension(val);
                }
                return true;
            }
            case "--runtime": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    this.setRuntime(val);
                }
                return true;
            }
            case "--api-version": {
                String val = cmdLine.nextStringValueLiteral().get(session);
                if (enabled) {
                    this.setTargetApiVersion(NutsVersion.of(val).get( getSession()));
                }
                return true;
            }
            case "--nuts-app":
            case "--nuts-apps": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    this.setApplication(val);
                }
                return true;
            }
            case "--arch": {
                String val = cmdLine.nextStringValueLiteral().get(session);
                if (enabled) {
                    this.addArch(val);
                }
                return true;
            }
            case "--packaging": {
                String val = cmdLine.nextStringValueLiteral().get(session);
                if (enabled) {
                    this.addPackaging(val);
                }
                return true;
            }
            case "--optional": {
                NutsArgument val = cmdLine.nextString().get(session);
                if (enabled) {
                    this.setOptional(val.getValue().asBoolean().orNull());
                }
                return true;
            }
            case "--script": {
                String val = cmdLine.nextStringValueLiteral().get(session);
                if (enabled) {
                    this.addScript(val);
                }
                return true;
            }
            case "--id": {
                String val = cmdLine.nextStringValueLiteral().get(session);
                if (enabled) {
                    this.addId(val);
                }
                return true;
            }
            case "--locked-id": {
                String val = cmdLine.nextStringValueLiteral().get(session);
                if (enabled) {
                    this.addLockedId(val);
                }
                return true;
            }
            case "--deployed": {
                NutsArgument b = cmdLine.nextBoolean().get(session);
                if (enabled) {
                    checkSession();
                    this.setInstallStatus(NutsInstallStatusFilters.of(session).byDeployed(b.getBooleanValue().get(session)));
                }
                return true;
            }
            case "-i":
            case "--installed": {
                NutsArgument b = cmdLine.nextBoolean().get(session);
                if (enabled) {
                    checkSession();
                    this.setInstallStatus(
                            NutsInstallStatusFilters.of(session).byInstalled(b.getBooleanValue().get(session))
                    );
                }
                return true;
            }
            case "--required": {
                NutsArgument b = cmdLine.nextBoolean().get(session);
                if (enabled) {
                    checkSession();
                    this.setInstallStatus(NutsInstallStatusFilters.of(session).byRequired(b.getBooleanValue().get(session)));
                }
                return true;
            }
            case "--obsolete": {
                NutsArgument b = cmdLine.nextBoolean().get(session);
                if (enabled) {
                    checkSession();
                    this.setInstallStatus(NutsInstallStatusFilters.of(session).byObsolete(b.getBooleanValue().get(session)));
                }
                return true;
            }
            case "--status": {
                NutsArgument aa = cmdLine.nextString().get(session);
                if (enabled) {
                    checkSession();
                    this.setInstallStatus(NutsInstallStatusFilters.of(session).parse(aa.getStringValue().get(session)));
                }
                return true;
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                if (a.isOption()) {
                    return false;
                } else {
                    cmdLine.skip();
                    addId(a.asString().get(session));
                    return true;
                }
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{"
                + "failFast=" + isFailFast()
                + ", optional=" + getOptional()
                + ", scope=" + getScope()
                + ", content=" + isContent()
                + ", inlineDependencies=" + isInlineDependencies()
                + ", dependencies=" + isDependencies()
                + ", effective=" + isEffective()
                + ", location=" + getLocation()
                + ", displayOptions=" + getDisplayOptions()
                + ", comparator=" + getComparator()
                + ", dependencyFilter=" + getDependencyFilter()
                + ", descriptorFilter=" + getDescriptorFilter()
                + ", idFilter=" + getIdFilter()
                + ", repositoryFilter=" + getRepositoryFilter()
                + ", latest=" + isLatest()
                + ", distinct=" + isDistinct()
                + ", includeMain=" + isBasePackage()
                + ", sorted=" + isSorted()
                + ", arch=" + getArch()
                + ", ids=" + getIds()
                + ", lockedIds=" + getLockedIds()
                + ", scripts=" + getScripts()
                + ", packaging=" + getPackaging()
                + ", defaultVersions=" + getDefaultVersions()
                + ", execType='" + getExecType() + '\''
                + ", targetApiVersion='" + getTargetApiVersion() + '\''
                + '}';
    }

    private Object dependenciesToElement(NutsDependencyTreeNode d) {
        NutsId id
                = //                getSearchSession().getWorkspace().text().parse(d.getDependency().formatter().setSession(getSearchSession()).setNtf(false).format())
                d.getDependency().toId();
        if (d.isPartial()) {
            id = id.builder().setProperty("partial", "true").build();
        }
        List<Object> li = d.getChildren().stream().map(x -> dependenciesToElement(x)).collect(Collectors.toList());
        if (li.isEmpty()) {
            return id;
        }
        Map<Object, Object> o = new HashMap<>();
        o.put(id, li);
        return o;
    }

    public <T> NutsIterator<T> runIterator() {
        checkSession();
        NutsDisplayProperty[] a = getDisplayOptions().getDisplayProperties();
        NutsStream r = null;
        if (isDependencies() && !isInlineDependencies()) {
            NutsContentType of = getSearchSession().getOutputFormat();
            if (of == null) {
                of = NutsContentType.TREE;
            }
            switch (of) {
                case JSON:
                case TSON:
                case XML:
                case YAML:
                case TREE: {
                    return (NutsIterator) IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                            .flatMap(NutsFunction.of(x -> x.getDependencies().transitiveNodes().iterator(), "getDependencies"))
                            .map(NutsFunction.of(x -> dependenciesToElement(x), "dependenciesToElement"))
                            .build();
                }

                default: {
                    NutsStream<NutsDependency> rr = getResultInlineDependencies();
                    return (NutsIterator) rr.iterator();
                }
            }
        } else {
            if (a.length == 0) {
                r = getResultIds();
            } else if (a.length == 1) {
                //optimized case
                switch (a[0]) {
                    case ARCH: {
                        r = getResultArch();
                        break;
                    }
                    case FILE: {
                        r = getResultPaths();
                        break;
                    }
                    case FILE_NAME: {
                        r = getResultPathNames();
                        break;
                    }
                    case NAME: {
                        r = getResultNames();
                        break;
                    }
                    case PACKAGING: {
                        r = getResultPackaging();
                        break;
                    }
                    case PLATFORM: {
                        r = getResultPlatform();
                        break;
                    }
                    case DESKTOP_ENVIRONMENT: {
                        r = getResultDesktopEnvironment();
                        break;
                    }
                    case EXEC_ENTRY: {
                        r = getResultExecutionEntries();
                        break;
                    }
                    case OS: {
                        r = getResultOs();
                        break;
                    }
                    case OSDIST: {
                        r = getResultOsDist();
                        break;
                    }
                    case ID: {
                        r = getResultIds();
                        break;
                    }
                    case INSTALL_DATE: {
                        r = getResultInstallDates();
                        break;
                    }
                    case INSTALL_USER: {
                        r = getResultInstallUsers();
                        break;
                    }
                    case INSTALL_FOLDER: {
                        r = getResultInstallFolders();
                        break;
                    }
                    case APPS_FOLDER: {
                        r = getResultStoreLocations(NutsStoreLocation.APPS);
                        break;
                    }
                    case CACHE_FOLDER: {
                        r = getResultStoreLocations(NutsStoreLocation.CACHE);
                        break;
                    }
                    case CONFIG_FOLDER: {
                        r = getResultStoreLocations(NutsStoreLocation.CONFIG);
                        break;
                    }
                    case LIB_FOLDER: {
                        r = getResultStoreLocations(NutsStoreLocation.LIB);
                        break;
                    }
                    case LOG_FOLDER: {
                        r = getResultStoreLocations(NutsStoreLocation.LOG);
                        break;
                    }
                    case TEMP_FOLDER: {
                        r = getResultStoreLocations(NutsStoreLocation.TEMP);
                        break;
                    }
                    case VAR_LOCATION: {
                        r = getResultStoreLocations(NutsStoreLocation.VAR);
                        break;
                    }
                    case STATUS: {
                        r = getResultStatuses();
                        break;
                    }
                }
            }
            if (r == null) {
                //this is custom case
                boolean _content = isContent();
                boolean _effective = isEffective();
                for (NutsDisplayProperty display : getDisplayOptions().getDisplayProperties()) {
                    switch (display) {
                        case NAME:
                        case ARCH:
                        case PACKAGING:
                        case PLATFORM:
                        case OS:
                        case OSDIST: {
                            break;
                        }
                        case FILE:
                        case FILE_NAME:
                        case EXEC_ENTRY: {
//                        _content = true;
                            break;
                        }
                        case INSTALL_DATE:
                        case INSTALL_USER: {
                            break;
                        }
                        case STATUS: {
//                        _content = true;
                            break;
                        }
                    }
                }
                r = buildCollectionResult(
                        getResultDefinitionIteratorBase(_content, _effective)
                );
            }
            return r.iterator();
        }
    }

    @Override
    public NutsElement getResultQueryPlan() {
        return toQueryPlan(runIterator());
    }

    @Override
    public NutsSearchCommand run() {
        NutsIterator<Object> it = runIterator();
        if (session.isDry()) {
            displayDryQueryPlan(it);
        } else {
            it = NutsWorkspaceUtils.of(getSearchSession()).decoratePrint(it, getSearchSession(), getDisplayOptions());
            while (it.hasNext()) {
                it.next();
            }
        }
        return this;
    }

    private NutsElement toQueryPlan(NutsIterator it) {
        NutsElements elem = NutsElements.of(session);
        return
                elem.ofObject()
                        .set("SearchQueryPlan",
                                NutsDescribables.resolveOrDestruct(it, session))
                        .build();
    }

    private void displayDryQueryPlan(NutsIterator it) {
        NutsElement n = toQueryPlan(it);
        NutsContentType f = session.getOutputFormat();
        if (f == NutsContentType.PLAIN) {
            f = NutsContentType.TREE;
        }
        NutsSession session2 = session.copy().setOutputFormat(f);
        session2.out().printlnf(n);
    }


    public NutsIterator<NutsDefinition> getResultDefinitionIteratorBase(boolean content, boolean effective) {
        NutsFetchCommand fetch = toFetch().setContent(content).setEffective(effective);
        NutsFetchCommand ofetch = toFetch().setContent(content).setEffective(effective).setSession(getSession().copy().setFetchStrategy(NutsFetchStrategy.OFFLINE));
        final boolean hasRemote = getSession().getFetchStrategy() == null
                || getSession().getFetchStrategy().modes().stream()
                .anyMatch(x -> x == NutsFetchMode.REMOTE);
        return IteratorBuilder.of(getResultIdIteratorBase(null), session)
                .map(NutsFunction.of(next -> {
//                    NutsDefinition d = null;
//                    if (isContent()) {
                    NutsDefinition d = fetch.setId(next).getResultDefinition();
                    if (d == null) {
                        if (isFailFast()) {
                            throw new NutsNotFoundException(getSession(), next);
                        }
                        return d;
                    }
                    return d;
//                    } else {
//                        if (hasRemote) {
//                            fetch.setId(next).getResultDescriptor();
//                        }
//                        d = ofetch.setId(next).getResultDefinition();
//                        if(d==null){
//                            _LOGOP(getSession())
//                                    .verb(NutsLogVerb.FAIL)
//                                    .log("inconsistent repository. id %s was found but its definition could not be resolved!",next);
//                        }
//                    }
//                    return d;
                }, "Id->Definition"))
                .notNull().build();
    }

    protected <T> NutsStream<T> buildCollectionResult(NutsIterator<T> o) {
        NutsSession ss = getSearchSession();
        return new NutsIteratorStream<T>(ss, resolveFindIdBase(), o);
    }

    protected String resolveFindIdBase() {
        return ids.isEmpty() ? null : ids.get(0) == null ? null : ids.get(0).toString();
    }

    public NutsStream<String> getResultStatuses() {
        return postProcessResult(IteratorBuilder.of(getResultDefinitionIteratorBase(isContent(), isEffective()), session)
                .map(NutsFunction.of(
                        x -> NutsIdFormatHelper.of(x, getSearchSession())
                                .buildLong().getStatusString(),
                        "getStatusString")
                )
                .notBlank());
    }

    //    protected NutsStream<NutsDefinition> getResultDefinitionsBase(boolean print, boolean sort, boolean content, boolean effective) {
//        checkSession();
//        return new NutsDefinitionNutsResult(getSession(), resolveFindIdBase(), print, sort, content, effective);
//    }
//
    protected abstract NutsIterator<NutsId> getResultIdIteratorBase(Boolean forceInlineDependencies);

    protected NutsStream<NutsId> getResultIdsBase(boolean sort) {
        return buildCollectionResult(getResultIdIteratorBase(null));
    }

    protected <T> NutsStream<T> postProcessResult(IteratorBuilder<T> a) {
        if (isSorted()) {
            a = a.sort(null, isDistinct());
        }
        return buildCollectionResult(a.build());
    }

    protected NutsSession getSearchSession() {
        return getSession();
    }

    protected NutsIterator<NutsId> applyPrintDecoratorIterOfNutsId(NutsIterator<NutsId> curr, boolean print) {
        return print ? NutsWorkspaceUtils.of(getSearchSession()).decoratePrint(curr, getSearchSession(), getDisplayOptions()) : curr;
    }
}
