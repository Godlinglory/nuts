package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.spi.NutsRepositoryDB;
import net.thevpc.nuts.spi.NutsRepositoryLocation;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.util.NutsPlatformUtils;

import java.util.*;

public class DefaultNutsRepositoryDB implements NutsRepositoryDB {
    private final Map<String, String> defaultRepositoriesByName = new LinkedHashMap<>();
    private final Map<String, String> aliasToBase = new LinkedHashMap<>();
    private final Map<String, Set<String>> baseToAliases = new LinkedHashMap<>();

    private DefaultNutsRepositoryDB(NutsSession session) {
        reg("system", "nuts@" + NutsPath.of(
                NutsPlatformUtils.getDefaultPlatformHomeFolder(null,
                        NutsStoreLocation.LIB,
                        true,
                        NutsConstants.Names.DEFAULT_WORKSPACE_NAME), session
        ).resolve(NutsConstants.Folders.ID).toString());
        reg("maven-local", "maven@" + NutsPath.ofUserHome(session).resolve(".m2/repository").toString(), ".m2", "m2");
        reg("maven-central", "maven@htmlfs:https://repo.maven.apache.org/maven2", "central", "maven", "central");
        reg("jcenter", "maven@https://jcenter.bintray.com");
        reg("jboss", "maven@https://repository.jboss.org/nexus/content/repositories/releases");
        reg("clojars", "maven@https://repo.clojars.org");
        reg("atlassian", "maven@https://packages.atlassian.com/maven/public");
        reg("atlassian-snapshot", "maven@https://packages.atlassian.com/maven/public-snapshot");
        reg("oracle", "maven@https://maven.oracle.com");
        reg("google", "maven@https://maven.google.com");
        reg("spring", "maven@https://repo.spring.io/release", "spring-framework");
        reg("maven-thevpc-git", "maven@https://raw.githubusercontent.com/thevpc/vpc-public-maven/master", "vpc-public-maven");
        reg("nuts-public", "maven@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-public/master", "vpc-public-nuts","nuts-thevpc-git");
        reg("nuts-preview", "maven@dotfilefs://raw.githubusercontent.com/thevpc/nuts-preview/master", "preview");
        reg("thevpc", "maven@htmlfs:https://thevpc.net/maven", "dev");
        reg("thevpc-goodies", "nuts@htmlfs:https://thevpc.net/maven-goodies", "goodies");
        reg("local", "nuts@local");
    }

    @Override
    public Set<String> getAllNames(String name) {
        Set<String> a = baseToAliases.get(name);
        if (a != null) {
            return Collections.unmodifiableSet(a);
        }
        String base = aliasToBase.get(name);
        if (base != null) {
            a = baseToAliases.get(base);
            if (a != null) {
                return Collections.unmodifiableSet(a);
            }
        }
        return Collections.singleton(name);
    }

    @Override
    public String getRepositoryNameByLocation(String location) {
        NutsRepositoryLocation v0 = NutsRepositoryLocation.of(location).setName(null);
        for (Map.Entry<String, String> entry : defaultRepositoriesByName.entrySet()) {
            NutsRepositoryLocation v = NutsRepositoryLocation.of(entry.getValue()).setName(null);
            if (v.equals(v0)) {
                return entry.getKey();
            }
        }
        v0 = NutsRepositoryLocation.of(location).setName(null).setLocationType(null);
        for (Map.Entry<String, String> entry : defaultRepositoriesByName.entrySet()) {
            NutsRepositoryLocation v = NutsRepositoryLocation.of(entry.getValue()).setName(null).setLocationType(null);
            if (v.equals(v0)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public boolean isDefaultRepositoryName(String name) {
        return defaultRepositoriesByName.containsKey(name)
                || aliasToBase.containsKey(name);
    }

    @Override
    public String getRepositoryLocationByName(String name) {
        String a = defaultRepositoriesByName.get(name);
        if (a != null) {
            return a;
        }
        String base = aliasToBase.get(name);
        if (base != null) {
            return defaultRepositoriesByName.get(base);
        }
        return null;
    }

    private void reg(String name, String url, String... names) {
        defaultRepositoriesByName.put(name, url);
        Set<String> all = new LinkedHashSet<>();
        all.add(name);
        for (String other : names) {
            aliasToBase.put(other, name);
            all.add(other);
        }
        baseToAliases.put(name, all);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
