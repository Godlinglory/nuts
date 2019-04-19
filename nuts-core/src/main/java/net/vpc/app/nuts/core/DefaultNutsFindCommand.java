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

import net.vpc.app.nuts.core.util.NutsIdGraph;
import net.vpc.app.nuts.core.util.NutsCollectionFindResult;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyJavascriptFilter;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyOptionFilter;
import net.vpc.app.nuts.core.filters.dependency.NutsDependencyScopeFilter;
import net.vpc.app.nuts.core.filters.descriptor.NutsDescriptorFilterArch;
import net.vpc.app.nuts.core.filters.descriptor.NutsDescriptorFilterPackaging;
import net.vpc.app.nuts.core.filters.descriptor.NutsDescriptorJavascriptFilter;
import net.vpc.app.nuts.core.filters.id.NutsIdFilterOr;
import net.vpc.app.nuts.core.filters.id.NutsJavascriptIdFilter;
import net.vpc.app.nuts.core.filters.repository.DefaultNutsRepositoryFilter;
import net.vpc.app.nuts.core.filters.repository.ExprNutsRepositoryFilter;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.io.File;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.vpc.app.nuts.core.filters.DefaultNutsIdMultiFilter;

import static net.vpc.app.nuts.core.util.CoreNutsUtils.And;
import static net.vpc.app.nuts.core.util.CoreNutsUtils.simplify;
import net.vpc.app.nuts.core.util.DefaultNutsFindTraceFormat;
import net.vpc.app.nuts.core.util.FailsafeNutsTraceFormat;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.IteratorBuilder;
import net.vpc.app.nuts.core.util.common.IteratorUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

/**
 * @author vpc
 */
public class DefaultNutsFindCommand extends DefaultNutsQueryBaseOptions<NutsFindCommand> implements NutsFindCommand {

    private Comparator<NutsId> idComparator;
    private NutsDependencyFilter dependencyFilter;
    private NutsDescriptorFilter descriptorFilter;
    private NutsIdFilter idFilter;
    private NutsRepositoryFilter repositoryFilter;
    private boolean lenient = false;
    private boolean includeAllVersions = true;
    private boolean includeDuplicatedVersions = true;
    private boolean includeMain = true;
    private boolean sort = false;
    private final DefaultNutsWorkspace ws;
    private final List<String> arch = new ArrayList<>();
    private final List<NutsId> ids = new ArrayList<>();
    private final List<String> scripts = new ArrayList<>();
    private final List<String> packaging = new ArrayList<>();
    private final List<String> repos = new ArrayList<>();
    private FailsafeNutsTraceFormat traceFormat=new FailsafeNutsTraceFormat(null,DefaultNutsFindTraceFormat.INSTANCE);

    public DefaultNutsFindCommand(DefaultNutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsFindCommand clearScripts() {
        scripts.clear();
        return this;
    }

    @Override
    public NutsFindCommand scripts(Collection<String> value) {
        return addScripts(value);
    }

    @Override
    public NutsFindCommand scripts(String... value) {
        return addScripts(value);
    }

    @Override
    public NutsFindCommand addScripts(Collection<String> value) {
        if (value != null) {
            addScripts(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsFindCommand removeScript(String value) {
        scripts.remove(value);
        return this;
    }

    @Override
    public NutsFindCommand script(String value) {
        return addScript(value);
    }

    @Override
    public NutsFindCommand addScript(String value) {
        if (value != null) {
            scripts.add(value);
        }
        return this;
    }

    @Override
    public NutsFindCommand addScripts(String... value) {
        if (value != null) {
            scripts.addAll(Arrays.asList(value));
        }
        return this;

    }

    @Override
    public NutsFindCommand ids(String... values) {
        return addIds(values);
    }

    @Override
    public NutsFindCommand addIds(String... values) {
        if (values != null) {
            for (String s : values) {
                if (!CoreStringUtils.isBlank(s)) {
                    ids.add(ws.parser().parseRequiredId(s));
                }
            }
        }
        return this;
    }

    @Override
    public NutsFindCommand ids(NutsId... values) {
        return addIds(values);
    }

    @Override
    public NutsFindCommand addIds(NutsId... value) {
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
    public NutsFindCommand clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NutsFindCommand addArch(String value) {
        if (!CoreStringUtils.isBlank(value)) {
            this.arch.add(value);
        }
        return this;
    }

    @Override
    public NutsFindCommand removeArch(String value) {
        this.arch.remove(value);
        return this;
    }

    @Override
    public NutsFindCommand arch(String value) {
        return addArch(value);
    }

    @Override
    public NutsFindCommand archs(Collection<String> value) {
        return addArchs(value);
    }

    @Override
    public NutsFindCommand clearArchs() {
        this.arch.clear();
        return this;
    }

    @Override
    public NutsFindCommand addArchs(Collection<String> value) {
        if (value != null) {
            addArchs(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsFindCommand archs(String... value) {
        return addArchs(arch);
    }

    @Override
    public NutsFindCommand addArchs(String... value) {
        if (value != null) {
            arch.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsFindCommand packaging(String value) {
        return addPackaging(value);
    }

    @Override
    public NutsFindCommand addPackaging(String value) {
        if (value != null) {
            packaging.add(value);
        }
        return this;
    }

    @Override
    public NutsFindCommand removePackaging(String value) {
        packaging.remove(value);
        return this;
    }

    @Override
    public NutsFindCommand clearPackagings() {
        packaging.clear();
        return this;
    }

    @Override
    public NutsFindCommand addPackagings(Collection<String> value) {
        if (value != null) {
            addPackagings(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsFindCommand packagings(Collection<String> value) {
        return addPackagings(value);
    }

    @Override
    public NutsFindCommand packagings(String... value) {
        return addPackagings(value);
    }

    @Override
    public NutsFindCommand addPackagings(String... value) {
        if (value != null) {
            this.packaging.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsFindCommand repositories(Collection<String> value) {
        return addRepositories(value);
    }

    @Override
    public NutsFindCommand repositories(String... values) {
        return addRepositories(values);
    }

    @Override
    public NutsFindCommand addRepositories(Collection<String> value) {
        if (value != null) {
            addRepositories(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsFindCommand removeRepository(String value) {
        repos.remove(value);
        return this;
    }

    @Override
    public NutsFindCommand addRepositories(String... value) {
        if (value != null) {
            repos.addAll(Arrays.asList(value));
        }
        return this;
    }

    @Override
    public NutsFindCommand clearRepositories() {
        repos.clear();
        return this;
    }

    @Override
    public NutsFindCommand addRepository(String value) {
        repos.add(value);
        return this;
    }

    @Override
    public NutsFindCommand repository(String value) {
        return addRepository(value);
    }

    @Override
    public NutsFindCommand copy() {
        DefaultNutsFindCommand b = new DefaultNutsFindCommand(ws);
        b.copyFrom(this);
        return b;
    }

    @Override
    public NutsFindCommand copyFrom(NutsFetchCommand other) {
        super.copyFrom0((DefaultNutsQueryBaseOptions) other);
        return this;
    }

    @Override
    public NutsFindCommand copyFrom(NutsFindCommand other) {
        super.copyFrom0((DefaultNutsQueryBaseOptions) other);
        if (other != null) {
            NutsFindCommand o = other;
            this.idComparator = o.getSortIdComparator();
            this.dependencyFilter = o.getDependencyFilter();
            this.descriptorFilter = o.getDescriptorFilter();
            this.idFilter = o.getIdFilter();
            this.lenient = o.isLenient();
            this.includeAllVersions = o.isAllVersions();
            this.includeDuplicatedVersions = o.isDuplicatedVersions();
            this.includeMain = o.isIncludeMain();
            this.sort = o.isSort();
            this.arch.clear();
            this.arch.addAll(Arrays.asList(o.getArch()));
            this.ids.clear();
            this.ids.addAll(Arrays.asList(o.getIds()));
            this.scripts.clear();
            this.scripts.addAll(Arrays.asList(o.getScripts()));
            this.packaging.clear();
            this.packaging.addAll(Arrays.asList(o.getPackaging()));
            this.repositoryFilter = o.getRepositoryFilter();
            this.repos.clear();
            this.repos.addAll(Arrays.asList(o.getRepos()));
        }
        return this;
    }

    @Override
    public boolean isSort() {
        return sort;
    }

    @Override
    public NutsFindCommand sort() {
        return setSort(true);
    }

    @Override
    public NutsFindCommand sort(Comparator<NutsId> comparator) {
        this.idComparator = comparator;
        this.sort = true;
        return this;
    }

    @Override
    public NutsFindCommand sort(boolean sort) {
        return setSort(sort);
    }

    @Override
    public NutsFindCommand setSort(boolean sort) {
        this.sort = sort;
        return this;
    }

    @Override
    public boolean isAllVersions() {
        return includeAllVersions;
    }

    @Override
    public NutsFindCommand latestVersions() {
        return setAllVersions(false);
    }

    @Override
    public NutsFindCommand allVersions() {
        return setAllVersions(true);
    }

    @Override
    public NutsFindCommand allVersions(boolean includeAllVersions) {
        return setAllVersions(includeAllVersions);
    }

    @Override
    public NutsFindCommand setAllVersions(boolean includeAllVersions) {
        this.includeAllVersions = includeAllVersions;
        return this;
    }

    @Override
    public NutsFindCommand id(NutsId id) {
        return addId(id);
    }

    @Override
    public NutsFindCommand addId(NutsId id) {
        if (id != null) {
            addId(id.toString());
        }
        return this;
    }

    @Override
    public NutsFindCommand removeId(NutsId id) {
        if (id != null) {
            removeId(id.toString());
        }
        return this;
    }

    @Override
    public NutsFindCommand id(String id) {
        return addId(id);
    }

    @Override
    public NutsFindCommand removeId(String id) {
        ids.remove(ws.parser().parseId(id));
        return this;
    }

    @Override
    public NutsFindCommand addId(String id) {
        if (!CoreStringUtils.isBlank(id)) {
            ids.add(ws.parser().parseRequiredId(id));
        }
        return this;
    }

    @Override
    public NutsId[] getIds() {
        return this.ids.toArray(new NutsId[0]);
    }

    //    public NutsQuery setDependencyFilter(TypedObject filter) {
//        if (filter == null) {
//            this.dependencyFilter = null;
//        } else if (NutsDependencyFilter.class.equals(filter.getType()) || String.class.equals(filter.getType())) {
//            this.dependencyFilter = filter;
//        } else {
//            throw new IllegalArgumentException("Invalid Object");
//        }
//        return this;
//    }
//
    @Override
    public NutsFindCommand setDependencyFilter(NutsDependencyFilter filter) {
        this.dependencyFilter = filter;
        return this;
    }

    @Override
    public NutsDependencyFilter getDependencyFilter() {
        return dependencyFilter;
    }

    @Override
    public NutsFindCommand setDependencyFilter(String filter) {
        this.dependencyFilter = CoreStringUtils.isBlank(filter) ? null : new NutsDependencyJavascriptFilter(filter);
        return this;
    }

    @Override
    public NutsFindCommand setRepositoryFilter(NutsRepositoryFilter filter) {
        this.repositoryFilter = filter;
        return this;
    }

    @Override
    public NutsRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    @Override
    public NutsFindCommand setRepository(String filter) {
        this.repositoryFilter = CoreStringUtils.isBlank(filter) ? null : new ExprNutsRepositoryFilter(filter);
        return this;
    }

    @Override
    public NutsFindCommand setDescriptorFilter(NutsDescriptorFilter filter) {
        this.descriptorFilter = filter;
        return this;
    }

    @Override
    public NutsDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    @Override
    public NutsFindCommand setDescriptorFilter(String filter) {
        this.descriptorFilter = CoreStringUtils.isBlank(filter) ? null : new NutsDescriptorJavascriptFilter(filter);
        return this;
    }

    @Override
    public NutsFindCommand setIdFilter(NutsIdFilter filter) {
        this.idFilter = filter;
        return this;
    }

    @Override
    public NutsIdFilter getIdFilter() {
        return idFilter;
    }

    @Override
    public NutsFindCommand setIdFilter(String filter) {
        this.idFilter = CoreStringUtils.isBlank(filter) ? null : new NutsJavascriptIdFilter(filter);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NutsSearch{");
        sb.append(getScope());
        if (ids != null && ids.size() > 0) {
            sb.append(",ids=").append(ids);
        }
        if (idFilter != null) {
            sb.append(",idFilter=").append(idFilter);
        }
        if (dependencyFilter != null) {
            sb.append(",dependencyFilter=").append(dependencyFilter);
        }
        if (repositoryFilter != null) {
            sb.append(",repositoryFilter=").append(repositoryFilter);
        }
        if (descriptorFilter != null) {
            sb.append(",descriptorFilter=").append(descriptorFilter);
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String[] getScripts() {
        return scripts.toArray(new String[0]);
    }

    @Override
    public String[] getArch() {
        return arch.toArray(new String[0]);
    }

    @Override
    public String[] getPackaging() {
        return this.packaging.toArray(new String[0]);
    }

    @Override
    public String[] getRepos() {
        return repos.toArray(new String[0]);
    }

    //@Override
    private DefaultNutsSearch build() {
        HashSet<String> someIds = new HashSet<>();
        for (NutsId id : this.getIds()) {
            someIds.add(id.toString());
        }
        HashSet<String> goodIds = new HashSet<>();
        HashSet<String> wildcardIds = new HashSet<>();
        for (String someId : someIds) {
            if (NutsPatternIdFilter.containsWildcad(someId)) {
                wildcardIds.add(someId);
            } else {
                goodIds.add(someId);
            }
        }
        NutsIdFilter idFilter0 = getIdFilter();
        if (idFilter0 instanceof NutsPatternIdFilter) {
            NutsPatternIdFilter f = (NutsPatternIdFilter) idFilter0;
            if (!f.isWildcard()) {
                goodIds.add(f.getId().toString());
                idFilter0 = null;
            }
        }
        if (idFilter0 instanceof NutsIdFilterOr) {
            List<NutsIdFilter> oo = new ArrayList<>(Arrays.asList(((NutsIdFilterOr) idFilter0).getChildren()));
            boolean someChange = false;
            for (Iterator<NutsIdFilter> it = oo.iterator(); it.hasNext();) {
                NutsIdFilter curr = it.next();
                if (curr instanceof NutsPatternIdFilter) {
                    NutsPatternIdFilter f = (NutsPatternIdFilter) curr;
                    if (!f.isWildcard()) {
                        goodIds.add(f.getId().toString());
                        it.remove();
                        someChange = true;
                    }
                }
            }
            if (someChange) {
                if (oo.isEmpty()) {
                    idFilter0 = null;
                } else {
                    idFilter0 = new NutsIdFilterOr(oo.toArray(new NutsIdFilter[0]));
                }
            }
        }

        NutsDescriptorFilter _descriptorFilter = null;
        NutsIdFilter _idFilter = null;
        NutsDependencyFilter depFilter = null;
        DefaultNutsRepositoryFilter rfilter = null;
        for (String j : this.getScripts()) {
            if (!CoreStringUtils.isBlank(j)) {
                if (CoreStringUtils.containsTopWord(j, "descriptor")) {
                    _descriptorFilter = simplify(And(_descriptorFilter, NutsDescriptorJavascriptFilter.valueOf(j)));
                } else if (CoreStringUtils.containsTopWord(j, "dependency")) {
                    depFilter = simplify(And(depFilter, NutsDependencyJavascriptFilter.valueOf(j)));
                } else {
                    _idFilter = simplify(And(_idFilter, NutsJavascriptIdFilter.valueOf(j)));
                }
            }
        }
        NutsDescriptorFilter packs = null;
        for (String v : this.getPackaging()) {
            packs = CoreNutsUtils.simplify(CoreNutsUtils.Or(packs, new NutsDescriptorFilterPackaging(v)));
        }
        NutsDescriptorFilter archs = null;
        for (String v : this.getArch()) {
            archs = CoreNutsUtils.simplify(CoreNutsUtils.Or(archs, new NutsDescriptorFilterArch(v)));
        }

        _descriptorFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(_descriptorFilter, packs, archs));

        if (this.getRepos().length > 0) {
            rfilter = new DefaultNutsRepositoryFilter(new HashSet<>(Arrays.asList(this.getRepos())));
        }

        NutsRepositoryFilter _repositoryFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(rfilter, this.getRepositoryFilter()));
        _descriptorFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(_descriptorFilter, this.getDescriptorFilter()));
        _idFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(_idFilter, idFilter0));

        if (!wildcardIds.isEmpty()) {
            for (String wildcardId : wildcardIds) {
                _idFilter = CoreNutsUtils.simplify(new NutsIdFilterOr(_idFilter, new NutsPatternIdFilter(ws.parser().parseId(wildcardId))));
            }
        }
        return new DefaultNutsSearch(
                goodIds.toArray(new String[0]),
                _repositoryFilter,
                _idFilter, _descriptorFilter,
                ws,
                toFetch()
        );
    }

    @Override
    public NutsFetchCommand toFetch() {
        return new DefaultNutsFetchCommand(ws).copyFrom0((DefaultNutsQueryBaseOptions) this).setSession(evalSession(true));
    }

    @Override
    public NutsFindResult<NutsId> getResultIds() {
        return findBasket(isTrace());
    }

    private NutsSession evalSession(boolean create) {
        NutsSession s = getSession();
        if (create) {
            if (s == null) {
                s = ws.createSession();
            }
        }
        return s;
//        if (mode != null) {
//            if (s == null) {
//                s = ws.createSession();
//            }
//            s.setFetchMode(mode);
//            return s;
//        } else {
//            return s;
//        }
    }

    @Override
    public NutsFindResult<NutsDefinition> getResultDefinitions() {
        return new NutsDefinitionNutsFindResult(resolveFindIdBase());
    }

    private String resolveFindIdBase() {
        return ids.isEmpty() ? null : ids.get(0) == null ? null : ids.get(0).toString();
    }

    private List<NutsId> applyTraceDecoratorListOfNutsId(List<NutsId> curr, boolean trace) {
        if (!trace) {
            return curr;
        }
        return CoreCommonUtils.toList(applyTraceDecoratorIterOfNutsId(curr.iterator(), trace));
    }

    private Collection<NutsId> applyTraceDecoratorCollectionOfNutsId(Collection<NutsId> curr, boolean trace) {
        if (!trace) {
            return curr;
        }
        return CoreCommonUtils.toList(applyTraceDecoratorIterOfNutsId(curr.iterator(), trace));
    }

    private Iterator<NutsId> applyTraceDecoratorIterOfNutsId(Iterator<NutsId> curr, boolean trace) {
        if (trace) {
            final PrintStream out = NutsWorkspaceUtils.validateSession(ws, getSession()).getTerminal().getOut();
            switch (getOutputFormat()) {
                case PLAIN: {
                    return new PlainTraceIterator<NutsId>(curr, ws, out, x -> traceFormat.format(x,getOutputFormat(),ws));
                }
                case PROPS: {
                    return new PropsTraceIterator<NutsId>(curr, ws, out, x -> traceFormat.format(x,getOutputFormat(),ws));
                }
                case JSON: {
                    return new JsonTraceIterator<NutsId>(curr, ws, out, x -> traceFormat.format(x,getOutputFormat(),ws));
                }
            }
            throw new NutsUnsupportedArgumentException("Unsupported " + getOutputFormat());
        } else {
            return curr;
        }
    }

    private NutsCollectionFindResult<NutsId> applyVersionFlagFilters(Iterator<NutsId> curr, boolean trace) {
        if (includeAllVersions && includeDuplicatedVersions) {
            return new NutsCollectionFindResult<NutsId>(resolveFindIdBase(), applyTraceDecoratorIterOfNutsId(curr, trace));
            //nothind
        } else if (includeAllVersions && !includeDuplicatedVersions) {
            return new NutsCollectionFindResult<NutsId>(resolveFindIdBase(),
                    applyTraceDecoratorIterOfNutsId(IteratorBuilder.of(curr).unique(new Function<NutsId, String>() {
                        @Override
                        public String apply(NutsId nutsId) {
                            return nutsId.getLongNameId().setAlternative(nutsId.getAlternative()).toString();
                        }
                    }).iterator(), trace));
        } else if (!includeAllVersions && !includeDuplicatedVersions) {
            Map<String, NutsId> visited = new LinkedHashMap<>();
            while (curr.hasNext()) {
                NutsId nutsId = curr.next();
                String k = nutsId.getSimpleNameId().setAlternative(nutsId.getAlternative()).toString();
                NutsId old = visited.get(k);
                if (old == null || old.getVersion().isBlank() || old.getVersion().compareTo(nutsId.getVersion()) < 0) {
                    visited.put(k, nutsId);
                }
            }
            return new NutsCollectionFindResult<NutsId>(resolveFindIdBase(), applyTraceDecoratorCollectionOfNutsId(visited.values(), trace));
        } else if (!includeAllVersions && includeDuplicatedVersions) {
            Map<String, List<NutsId>> visited = new LinkedHashMap<>();
            while (curr.hasNext()) {
                NutsId nutsId = curr.next();
                String k = nutsId.getSimpleNameId().setAlternative(nutsId.getAlternative()).toString();
                List<NutsId> oldList = visited.get(k);
                if (oldList == null || oldList.get(0).getVersion().isBlank() || oldList.get(0).getVersion().compareTo(nutsId.getVersion()) < 0) {
                    visited.put(k, new ArrayList<>(Arrays.asList(nutsId)));
                } else if (oldList.get(0).getVersion().compareTo(nutsId.getVersion()) == 0) {
                    oldList.add(nutsId);
                }
            }
            List<NutsId> list = new ArrayList<>();
            for (List<NutsId> li : visited.values()) {
                list.addAll(li);
            }
            return new NutsCollectionFindResult<NutsId>(resolveFindIdBase(), applyTraceDecoratorListOfNutsId(list, trace));
        }
        throw new NutsUnexpectedException();
    }

    private NutsCollectionFindResult<NutsId> findBasket(boolean trace) {
        Iterator<NutsId> base0 = findIterator(build());
        if (base0 == null) {
            return new NutsCollectionFindResult<NutsId>(resolveFindIdBase());
        }
        if (includeAllVersions && includeDuplicatedVersions && !sort && !isIncludeDependencies()) {
            return new NutsCollectionFindResult<NutsId>(resolveFindIdBase(), applyTraceDecoratorIterOfNutsId(base0, trace));
        }
        NutsCollectionFindResult<NutsId> a = applyVersionFlagFilters(base0, false);
        Iterator<NutsId> curr = a.iterator();
        if (isIncludeDependencies()) {
            if (!includeMain) {
                curr = Arrays.asList(findDependencies(a.list())).iterator();
            } else {
                List<Iterator<NutsId>> it = new ArrayList<>();
                Iterator<NutsId> a0 = a.iterator();
                List<NutsId> base = new ArrayList<>();
                it.add(new Iterator<NutsId>() {
                    @Override
                    public boolean hasNext() {
                        return a0.hasNext();
                    }

                    @Override
                    public NutsId next() {
                        NutsId x = a0.next();
                        base.add(x);
                        return x;
                    }
                });
                it.add(new Iterator<NutsId>() {
                    Iterator<NutsId> deps = null;

                    @Override
                    public boolean hasNext() {
                        if (deps == null) {
                            //will be called when base is already filled up!
                            deps = Arrays.asList(findDependencies(base)).iterator();
                        }
                        return deps.hasNext();
                    }

                    @Override
                    public NutsId next() {
                        return deps.next();
                    }
                });
                curr = IteratorUtils.concat(it);
            }
        }
        if (sort) {
            List<NutsId> listToSort = applyVersionFlagFilters(curr, false).list();
            listToSort.sort(idComparator == null ? DefaultNutsIdComparator.INSTANCE : idComparator);
            return new NutsCollectionFindResult<NutsId>(resolveFindIdBase(), applyTraceDecoratorListOfNutsId(listToSort, trace));
        } else {
            return applyVersionFlagFilters(curr, trace);
        }
    }

    private NutsId[] findDependencies(List<NutsId> ids) {
        NutsSession _session = this.getSession() == null ? ws.createSession() : this.getSession();
        NutsDependencyFilter _dependencyFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(
                new NutsDependencyScopeFilter(getScope()),
                getAcceptOptional() == null ? null : NutsDependencyOptionFilter.valueOf(getAcceptOptional()),
                getDependencyFilter()
        ));
        NutsIdGraph graph = new NutsIdGraph(ws, _session, lenient);
        graph.push(ids, _dependencyFilter);
        return graph.collect(ids, ids);
    }

    @Override
    public NutsFindCommand dependenciesOnly() {
        includeMain = false;
        includeDependencies(true);
        return this;
    }

    @Override
    public NutsFindCommand mainAndDependencies() {
        includeMain = true;
        includeDependencies(true);
        return this;
    }

    @Override
    public NutsFindCommand mainOnly() {
        includeMain = true;
        includeDependencies(false);
        return this;
    }

    @Override
    public String getResultNutsPath() {
        StringBuilder sb = new StringBuilder();
        for (NutsId nutsDefinition : getResultIds()) {
            if (nutsDefinition != null) {
                if (sb.length() > 0) {
                    sb.append(";");
                }
                sb.append(nutsDefinition.setNamespace(null).toString());
            }
        }
        return sb.toString();
    }

    @Override
    public String getResultClassPath() {
        StringBuilder sb = new StringBuilder();
        for (NutsDefinition nutsDefinition : getResultDefinitions()) {
            if (nutsDefinition.getPath() != null) {
                if (sb.length() > 0) {
                    sb.append(File.pathSeparator);
                }
                sb.append(nutsDefinition.getPath());
            }
        }
        return sb.toString();
    }

    @Override
    public boolean isLenient() {
        return lenient;
    }

    @Override
    public DefaultNutsFindCommand setLenient(boolean ignoreNotFound) {
        this.lenient = ignoreNotFound;
        return this;
    }

    @Override
    public NutsFindCommand lenient() {
        return setLenient(true);
    }

    @Override
    public NutsFindCommand lenient(boolean lenient) {
        return setLenient(lenient);
    }

    @Override
    public boolean isDuplicatedVersions() {
        return includeDuplicatedVersions;
    }

    @Override
    public NutsFindCommand duplicateVersions() {
        return duplicateVersions(true);
    }

    @Override
    public NutsFindCommand duplicateVersions(boolean includeDuplicateVersions) {
        return setDuplicateVersions(includeDuplicateVersions);
    }

    @Override
    public NutsFindCommand setDuplicateVersions(boolean includeDuplicateVersion) {
        this.includeDuplicatedVersions = includeDuplicateVersion;
        return this;
    }

    @Override
    public Comparator<NutsId> getSortIdComparator() {
        return idComparator;
    }

    @Override
    public boolean isIncludeMain() {
        return includeMain;
    }

    

    private class NutsDefinitionNutsFindResult extends AbstractNutsFindResult<NutsDefinition> {

        public NutsDefinitionNutsFindResult(String nutsBase) {
            super(nutsBase);
        }

        @Override
        public List<NutsDefinition> list() {
            if (isTrace()) {
                return CoreCommonUtils.toList(iterator());
            }
            List<NutsId> mi = findBasket(false).list();
            List<NutsDefinition> li = new ArrayList<>(mi.size());
//            NutsSession s = evalSession(true);
            NutsFetchCommand fetch = toFetch();
            PrintStream out = NutsWorkspaceUtils.validateSession(ws, getSession()).getTerminal().out();

            Properties props = new Properties();
            List<Object> json = new ArrayList<>();
            for (NutsId nutsId : mi) {
                NutsDefinition y = fetch.id(nutsId).getResultDefinition();
                li.add(y);
            }
            return li;
        }

        @Override
        public Stream<NutsDefinition> stream() {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator<NutsDefinition>) iterator(), Spliterator.ORDERED), false);
        }

        @Override
        public Iterator<NutsDefinition> iterator() {
            Iterator<NutsId> base = findBasket(false).iterator();
            NutsSession s = ws.createSession();
            NutsFetchCommand fetch = toFetch();
            Iterator<NutsDefinition> ii = new Iterator<NutsDefinition>() {
                private NutsDefinition n = null;

                @Override
                public boolean hasNext() {
                    while (base.hasNext()) {
                        NutsDefinition d = fetch.id(base.next()).getResultDefinition();
                        if (d != null) {
                            n = d;
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public NutsDefinition next() {
                    return n;
                }
            };
            if (!isTrace()) {
                return ii;
            }
            final PrintStream out = NutsWorkspaceUtils.validateSession(ws, getSession()).getTerminal().getOut();
            switch (getOutputFormat()) {
                case PLAIN: {
                    return new PlainTraceIterator<NutsDefinition>(ii, ws, out, x -> traceFormat.format(x,getOutputFormat(),ws));
                }
                case PROPS: {
                    return new PropsTraceIterator<NutsDefinition>(ii, ws, out, x -> traceFormat.format(x,getOutputFormat(),ws));
                }
                case JSON: {
                    return new JsonTraceIterator<NutsDefinition>(ii, ws, out, x -> traceFormat.format(x,getOutputFormat(),ws));
                }
            }
            throw new NutsUnsupportedArgumentException("Unsupported " + getOutputFormat());
        }

    }

    public Iterator<NutsId> findIterator(DefaultNutsSearch search) {

        List<Iterator<NutsId>> allResults = new ArrayList<>();

        NutsSession session = NutsWorkspaceUtils.validateSession(ws, search.getOptions().getSession());
        NutsIdFilter idFilter = search.getIdFilter();
        NutsRepositoryFilter repositoryFilter = search.getRepositoryFilter();
        NutsDescriptorFilter descriptorFilter = search.getDescriptorFilter();
        String[] regularIds = search.getRegularIds();
        NutsFetchStrategy fetchMode = NutsWorkspaceHelper.validate(search.getOptions().getFetchStrategy());
        if (regularIds.length > 0) {
            for (String id : regularIds) {
                NutsId nutsId = ws.parser().parseId(id);
                if (nutsId != null) {
                    List<NutsId> nutsId2 = new ArrayList<>();
                    if (nutsId.getGroup() == null) {
                        for (String aImport : ws.config().getImports()) {
                            nutsId2.add(nutsId.setGroup(aImport));
                        }
                    } else {
                        nutsId2.add(nutsId);
                    }
                    List<Iterator<NutsId>> coalesce = new ArrayList<>();
                    for (NutsFetchMode mode : fetchMode) {
                        List<Iterator<NutsId>> all = new ArrayList<>();
                        for (NutsId nutsId1 : nutsId2) {
                            if (mode == NutsFetchMode.INSTALLED) {
                                all.add(
                                        IteratorBuilder.ofLazy(new Iterable<NutsId>() {
                                            @Override
                                            public Iterator<NutsId> iterator() {
                                                NutsIdFilter filter = new DefaultNutsIdMultiFilter(
                                                        nutsId1.getQueryMap(), idFilter, descriptorFilter, null,
                                                        NutsWorkspaceHelper.createNoRepositorySession(session, mode,
                                                                search.getOptions())
                                                ).simplify();
                                                return ws.getInstalledRepository().findVersions(nutsId1, filter);
                                            }
                                        }).safeIgnore().iterator());
                            } else {
                                for (NutsRepository repo : NutsWorkspaceUtils.filterRepositories(ws, NutsRepositorySupportedAction.FIND, nutsId1, repositoryFilter, mode, search.getOptions())) {
                                    if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                                        NutsIdFilter filter = new DefaultNutsIdMultiFilter(nutsId1.getQueryMap(), idFilter, descriptorFilter, repo, NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions())).simplify();
                                        all.add(
                                                IteratorBuilder.ofLazy(new Iterable<NutsId>() {
                                                    @Override
                                                    public Iterator<NutsId> iterator() {
                                                        return repo.findVersions(nutsId1, filter, NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions()));
                                                    }
                                                }).safeIgnore().iterator()
                                        );
                                    }
                                }
                            }
                        }
                        coalesce.add(IteratorUtils.concat(all));
                    }
                    if (nutsId.getGroup() == null) {
                        //now will look with *:artifactId pattern
                        NutsFindCommand search2 = ws.find()
                                .copyFrom(search.getOptions())
                                .setRepositoryFilter(search.getRepositoryFilter())
                                .setDescriptorFilter(search.getDescriptorFilter())
                                .setFetchStratery(search.getOptions().getFetchStrategy())
                                .setSession(session);
                        search2.setIdFilter(new NutsIdFilterOr(
                                new NutsPatternIdFilter(nutsId.setGroup("*")),
                                CoreNutsUtils.simplify(search2.getIdFilter())
                        ));
                        coalesce.add(search2.getResultIds().iterator());
                    }
                    allResults.add(fetchMode.isStopFast()
                            ? IteratorUtils.coalesce(coalesce)
                            : IteratorUtils.concat(coalesce)
                    );
                }
            }
        } else {

            List<Iterator<NutsId>> coalesce = new ArrayList<>();
            for (NutsFetchMode mode : fetchMode) {
                if (mode == NutsFetchMode.INSTALLED) {
                    NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, null, mode, search.getOptions());
                    coalesce.add(NutsWorkspaceExt.of(ws).getInstalledRepository().findAll(idFilter, rsession));
                } else {
                    List<Iterator<NutsId>> all = new ArrayList<>();
                    for (NutsRepository repo : NutsWorkspaceUtils.filterRepositories(ws, NutsRepositorySupportedAction.FIND, null, repositoryFilter, mode, search.getOptions())) {
                        if (repositoryFilter == null || repositoryFilter.accept(repo)) {
                            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, repo, mode, search.getOptions());
                            NutsIdFilter filter = new DefaultNutsIdMultiFilter(null, idFilter, descriptorFilter, repo, rsession).simplify();
                            all.add(
                                    IteratorBuilder.ofLazy(new Iterable<NutsId>() {
                                        @Override
                                        public Iterator<NutsId> iterator() {
                                            return repo.find(filter, rsession);
                                        }
                                    }).safeIgnore().iterator()
                            );
                        }

                    }
                    coalesce.add(IteratorUtils.concat(all));
                }
            }
            allResults.add(fetchMode.isStopFast() ? IteratorUtils.coalesce(coalesce) : IteratorUtils.concat(coalesce));
        }
        return IteratorUtils.concat(allResults);
    }

    @Override
    public ClassLoader getResultClassLoader() {
        return getResultClassLoader(null);
    }

    @Override
    public ClassLoader getResultClassLoader(ClassLoader parent) {
        List<NutsDefinition> nutsDefinitions = getResultDefinitions().list();
        URL[] all = new URL[nutsDefinitions.size()];
        for (int i = 0; i < all.length; i++) {
            try {
                all[i] = nutsDefinitions.get(i).getPath().toUri().toURL();
            } catch (MalformedURLException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return new NutsURLClassLoader(ws, all, parent);
    }

    @Override
    public NutsFindCommand run() {
        for (NutsDefinition d : getResultDefinitions().list()) {
            //just to iterator over
        }
        return this;
    }

    
    @Override
    public NutsTraceFormat getTraceFormat() {
        return traceFormat.getOther();
    }

    @Override
    public NutsFindCommand traceFormat(NutsTraceFormat traceFormat) {
        return setTraceFormat(traceFormat);
    }
    
    @Override
    public NutsFindCommand setTraceFormat(NutsTraceFormat traceFormat) {
        this.traceFormat.setOther(traceFormat);
        return this;
    }
    

    @Override
    public NutsFindCommand parseOptions(String... args) {
        NutsCommandLine cmd = new NutsCommandLine(args);
        NutsCommandArg a;
        while ((a = cmd.next()) != null) {
            switch (a.getKey().getString()) {
                case "--lenient": {
                    this.setLenient(a.getBooleanValue());
                    break;
                }
                case "--trace": {
                    this.setTrace(a.getBooleanValue());
                    break;
                }
                case "--all-versions": {
                    this.setAllVersions(a.getBooleanValue());
                    break;
                }
                case "--latest-versions": {
                    this.latestVersions();
                    break;
                }
                case "--duplicate-versions": {
                    this.duplicateVersions(a.getBooleanValue());
                    break;
                }
                case "--sort": {
                    this.sort(a.getBooleanValue());
                    break;
                }
                case "--main": {
                    this.includeMain = a.getBooleanValue();
                    break;
                }
                case "--main-only": {
                    this.mainOnly();
                    break;
                }
                case "--main-and-dependencies": {
                    this.mainAndDependencies();
                    break;
                }
                case "--dependencies": {
                    this.mainAndDependencies();
                    break;
                }
                case "--dependencies-only": {
                    this.dependenciesOnly();
                    break;
                }
                case "--repo": {
                    this.addRepository(cmd.getValueFor(a).getString());
                    break;
                }
                case "--arch": {
                    this.addArch(cmd.getValueFor(a).getString());
                    break;
                }
                case "--packaging": {
                    this.addPackaging(cmd.getValueFor(a).getString());
                    break;
                }
                case "--script": {
                    this.addScript(cmd.getValueFor(a).getString());
                    break;
                }
                case "--id": {
                    this.addId(cmd.getValueFor(a).getString());
                    break;
                }
                case "--scope": {
                    this.addScope(NutsDependencyScope.valueOf(cmd.getValueFor(a).getString().toUpperCase().replace("-", "_")));
                    break;
                }
                case "--trace-format": {
                    this.setOutputFormat(NutsOutputFormat.valueOf(cmd.getValueFor(a).getString().toUpperCase()));
                    break;
                }
                case "--json": {
                    this.setOutputFormat(NutsOutputFormat.JSON);
                    break;
                }
                case "--props": {
                    this.setOutputFormat(NutsOutputFormat.PROPS);
                    break;
                }
                case "--plain": {
                    this.setOutputFormat(NutsOutputFormat.PLAIN);
                    break;
                }
                default: {
                    if (a.isOption()) {
                        throw new NutsIllegalArgumentException("Unsupported option " + a);
                    } else {
                        id(a.getString());
                    }
                }
            }
        }
        return this;
    }

    public static class JsonTraceIterator<T> implements Iterator<T> {

        Iterator<T> curr;
        NutsWorkspace ws;
        Function<T, Object> conv;
        PrintStream out;
        List<Object> props = new ArrayList<>();

        public JsonTraceIterator(Iterator<T> curr, NutsWorkspace ws, PrintStream out, Function<T, Object> conv) {
            this.curr = curr;
            this.ws = ws;
            this.out = out;
            this.conv = conv;
        }

        @Override
        public boolean hasNext() {
            boolean p = curr.hasNext();
            if (!p) {
                ws.io().writeJson(props, out, true);
            }
            return p;
        }

        @Override
        public T next() {
            T n = curr.next();
            props.add(conv.apply(n));
            return n;
        }
    };

    public static class PropsTraceIterator<T> implements Iterator<T> {

        Iterator<T> curr;
        NutsWorkspace ws;
        Function<T, Object> conv;
        PrintStream out;
        long count = 1;
        Properties props = new Properties();

        public PropsTraceIterator(Iterator<T> curr, NutsWorkspace ws, PrintStream out, Function<T, Object> conv) {
            this.curr = curr;
            this.ws = ws;
            this.out = out;
            this.conv = conv;
        }

        @Override
        public boolean hasNext() {
            boolean p = curr.hasNext();
            if (!p) {
                CoreIOUtils.storeProperties(props, out);
            }
            return p;
        }

        @Override
        public T next() {
            T n = curr.next();
            Object r = conv.apply(n);
            if (r instanceof Map) {
                Map<Object, Object> m = (Map<Object, Object>) r;
                for (Map.Entry<Object, Object> e : m.entrySet()) {
                    props.put(String.valueOf(count) + "." + e.getKey(), String.valueOf(e.getValue()));
                }
            } else {
                props.put(String.valueOf(count), r);
            }
            count++;
            return n;
        }
    };

    public static class PlainTraceIterator<T> implements Iterator<T> {

        Iterator<T> curr;
        NutsWorkspace ws;
        Function<T, Object> conv;
        PrintStream out;

        public PlainTraceIterator(Iterator<T> curr, NutsWorkspace ws, PrintStream out, Function<T, Object> conv) {
            this.curr = curr;
            this.ws = ws;
            this.out = out;
            this.conv = conv;
        }

        @Override
        public boolean hasNext() {
            return curr.hasNext();
        }

        @Override
        public T next() {
            T n = curr.next();
            out.printf("%N%n", conv.apply(n));
            return n;
        }
    };
}
