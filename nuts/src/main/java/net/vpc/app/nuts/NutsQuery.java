package net.vpc.app.nuts;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public interface NutsQuery {
    NutsQuery addJs(Collection<String> value);

    NutsQuery addJs(String... value);

    NutsQuery addId(Collection<String> value);

    NutsQuery addId(String... value);

    NutsQuery addId(NutsId... value);

    NutsQuery setId(String value);

    NutsQuery setId(NutsId value);

    NutsQuery addArch(Collection<String> value);

    NutsQuery addArch(String... value);

    NutsQuery addPackaging(Collection<String> value);

    NutsQuery addPackaging(String... value);

    NutsQuery addRepository(Collection<String> value);

    NutsQuery addRepository(String... value);

    NutsQuery copy();

    void copyFrom(NutsQuery other);

    //    NutsQuery setAll(NutsSearch other);
    NutsQuery setAll(NutsQuery other);

    boolean isSort();

    NutsQuery setSort(boolean sort);

    boolean isLatestVersions();

    NutsQuery setLatestVersions(boolean latestVersions);

    NutsQuery addIds(String... ids);

    NutsQuery addIds(NutsId... ids);

    NutsQuery setIds(String... ids);

    NutsQuery addId(String id);

    NutsQuery addId(NutsId id);

    String[] getIds();

    Set<NutsDependencyScope> getScope();

    NutsQuery setScope(NutsDependencyScope scope);

    NutsQuery setScope(Set<NutsDependencyScope> scope);

    NutsQuery addScope(Collection<NutsDependencyScope> scope);

    NutsQuery addScope(NutsDependencyScope scope);

    NutsQuery addScope(NutsDependencyScope... scope);

    NutsQuery removeScope(Collection<NutsDependencyScope> scope);

    NutsQuery removeScope(NutsDependencyScope scope);

    NutsQuery setDependencyFilter(NutsDependencyFilter filter);

    NutsDependencyFilter getDependencyFilter();

    NutsQuery setDependencyFilter(String filter);

    NutsQuery setRepositoryFilter(NutsRepositoryFilter filter);

    NutsRepositoryFilter getRepositoryFilter();

    NutsQuery setRepositoryFilter(String filter);

    NutsQuery setVersionFilter(NutsVersionFilter filter);

    NutsVersionFilter getVersionFilter();

    NutsQuery setVersionFilter(String filter);

    NutsQuery setDescriptorFilter(NutsDescriptorFilter filter);

    NutsDescriptorFilter getDescriptorFilter();

    NutsQuery setDescriptorFilter(String filter);

    NutsQuery setIdFilter(NutsIdFilter filter);

    NutsIdFilter getIdFilter();

    NutsQuery setIdFilter(String filter);

    NutsQuery setIds(Collection<String> ids);

    NutsSession getSession();

    NutsQuery setSession(NutsSession session);

    String[] getJs();

    String[] getArch();

    String[] getPackaging();

    String[] getRepos();

    NutsId findOne();

    NutsId findFirst();

    List<NutsId> find();

    Iterator<NutsId> findIterator();

    NutsDefinition fetchOne();

    NutsDefinition fetchFirst();

    String findNutspathString();

    String findClasspathString();

    List<NutsDefinition> fetch();

    Iterator<NutsDefinition> fetchIterator();

    NutsQuery dependenciesOnly();

    NutsQuery includeDependencies();

    NutsQuery mainOnly();

    Boolean getAcceptOptional();

    NutsQuery setAcceptOptional(Boolean acceptOptional);

    NutsQuery setIncludeOptional(boolean includeOptional);
}
