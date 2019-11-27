/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.main.repos;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.repos.NutsInstalledRepository;
import net.vpc.app.nuts.main.repocommands.DefaultNutsDeployRepositoryCommand;
import net.vpc.app.nuts.main.repocommands.DefaultNutsRepositoryUndeployCommand;
import net.vpc.app.nuts.main.wscommands.DefaultNutsFetchCommand;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.io.FolderNutIdIterator;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.iter.IteratorBuilder;
import net.vpc.app.nuts.runtime.util.iter.IteratorUtils;
import net.vpc.app.nuts.runtime.util.common.LRUMap;
import net.vpc.app.nuts.runtime.util.common.LazyIterator;
import net.vpc.app.nuts.runtime.DefaultNutsInstallInfo;

/**
 * @author vpc
 */
public class DefaultNutsInstalledRepository implements NutsInstalledRepository {

    public static class InstallInfoConfig {

        private NutsId id;
        private Instant installDate;
        private String installUser;

        public NutsId getId() {
            return id;
        }

        public String getInstallUser() {
            return installUser;
        }

        public void setInstallUser(String installUser) {
            this.installUser = installUser;
        }

        public void setId(NutsId id) {
            this.id = id;
        }

        public Instant getInstallDate() {
            return installDate;
        }

        public void setInstallDate(Instant installDate) {
            this.installDate = installDate;
        }

        @Override
        public String toString() {
            return "InstallInfoConfig{" + "id=" + id + ", installDate=" + installDate + ", installUser=" + installUser + '}';
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + Objects.hashCode(this.id);
            hash = 89 * hash + Objects.hashCode(this.installDate);
            hash = 89 * hash + Objects.hashCode(this.installUser);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InstallInfoConfig other = (InstallInfoConfig) obj;
            if (!Objects.equals(this.installUser, other.installUser)) {
                return false;
            }
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            if (!Objects.equals(this.installDate, other.installDate)) {
                return false;
            }
            return true;
        }

    }

    private static final String NUTS_INSTALL_FILE = "nuts-install.json";

    private final NutsWorkspace ws;
    private final NutsRepositoryFolderHelper deployments;
    private final Map<NutsId, String> cachedDefaultVersions = new LRUMap<>(200);

    public DefaultNutsInstalledRepository(NutsWorkspace ws) {
        this.ws = ws;
        deployments = new NutsRepositoryFolderHelper(null, ws, ws.config().getStoreLocation(NutsStoreLocation.LIB).resolve(NutsConstants.Folders.ID));
    }

    public Set<NutsId> getChildrenDependencies(NutsId id, NutsSession session) {
        return Collections.emptySet();
    }

    public Set<NutsId> getParentDependencies(NutsId id, NutsSession session) {
        return Collections.emptySet();
    }

    public void addDependency(NutsId id, NutsId parentId, NutsSession session) {

    }

    public void removeDependency(NutsId id, NutsId parentId, NutsSession session) {

    }

    public void deploy(NutsDefinition def, NutsSession session) {
        deployments.deploy(new DefaultNutsDeployRepositoryCommand(null)
                .setId(def.getId())
                .setContent(def.getPath())
                .setSession(NutsWorkspaceHelper.createNoRepositorySession(session, NutsFetchMode.LOCAL, new DefaultNutsFetchCommand(ws)))
                .setDescriptor(def.getDescriptor())
        );
    }

    public void undeploy(NutsId id, NutsSession session) {
        deployments.undeploy(new DefaultNutsRepositoryUndeployCommand(null)
                .setId(id)
                .setSession(NutsWorkspaceHelper.createNoRepositorySession(session, NutsFetchMode.LOCAL, new DefaultNutsFetchCommand(ws)))
        );
    }

    @Override
    public boolean isDefaultVersion(NutsId id) {
        String v = getDefaultVersion(id);
        return v.equals(id.getVersion().toString());
    }

    @Override
    public String getDefaultVersion(NutsId id) {
        NutsId baseVersion = id.getShortNameId();
        synchronized (cachedDefaultVersions) {
            String p = cachedDefaultVersions.get(baseVersion);
            if (p != null) {
                return p;
            }
        }
        Path pp = ws.config().getStoreLocation(id
                //.setAlternative("")
                .builder().setVersion("ANY").build(), NutsStoreLocation.CONFIG).resolveSibling("default-version");
        String defaultVersion = "";
        if (Files.isRegularFile(pp)) {
            try {
                defaultVersion = new String(Files.readAllBytes(pp)).trim();
            } catch (IOException ex) {
                defaultVersion = "";
            }
        }
        synchronized (cachedDefaultVersions) {
            cachedDefaultVersions.put(baseVersion, defaultVersion);
        }
        return defaultVersion;
    }

    @Override
    public void setDefaultVersion(NutsId id, NutsSession session) {
        NutsId baseVersion = id.getShortNameId();
        String version = id.getVersion().getValue();
        Path pp = ws.config().getStoreLocation(id
//                .setAlternative("")
                .builder().setVersion("ANY").build(), NutsStoreLocation.CONFIG).resolveSibling("default-version");
        if (CoreStringUtils.isBlank(version)) {
            if (Files.isRegularFile(pp)) {
                try {
                    Files.delete(pp);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        } else {
            try {
                Files.createDirectories(pp.getParent());
                Files.write(pp, version.trim().getBytes());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        synchronized (cachedDefaultVersions) {
            cachedDefaultVersions.put(baseVersion, version);
        }
    }

    public InstallInfoConfig getInstallInfoConfig(NutsId id) {
        Path p = getPath(id, NUTS_INSTALL_FILE);
        if (Files.isRegularFile(p)) {
            try {
                return readJson(id, NUTS_INSTALL_FILE, InstallInfoConfig.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public NutsInstallInformation getInstallInfo(NutsId id) {
        InstallInfoConfig ii = getInstallInfoConfig(id);
        if (ii != null) {
            return new DefaultNutsInstallInfo(true, isDefaultVersion(id),
                    ws.config().getStoreLocation(id, NutsStoreLocation.APPS),
                    ii.getInstallDate(),
                    ii.getInstallUser()
            );
        }
        return null;
    }

    @Override
    public boolean isInstalled(NutsId id) {
        return contains(id, NUTS_INSTALL_FILE);
    }

    protected Iterator<NutsId> findInFolder(Path folder, final NutsIdFilter filter, int maxDepth, NutsSession session) {
        if (folder == null || !Files.exists(folder) || !Files.isDirectory(folder)) {
            return IteratorUtils.emptyIterator();
        }
        return new FolderNutIdIterator(ws, "installed", folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsRepositorySession session) {
                //MavenFolderRepository.this.undeploy(parseId, session);
            }

            @Override
            public boolean isDescFile(Path pathname) {
                return pathname.getFileName().toString().equals("nuts-install.json");
            }

            @Override
            public NutsDescriptor parseDescriptor(Path pathname, NutsRepositorySession session) throws IOException {
                return ws.descriptor().parse(pathname);
//                Map<String, Object> m = ws.json().parse(pathname, Map.class);
//                if (m != null) {
//                    String id = (String) m.get("id");
//                    if (id != null) {
//                        return ws.fetch().id(id).offline().session(session.getSession().copy().silent())
//                                .setTransitive(session.isTransitive())
//                                .setIndexed(session.isIndexed())
//                                .setCached(session.isCached())
//                                .getResultDescriptor();
//                    }
//                }
//                return null;
            }
        }, maxDepth);
    }

    @Override
    public Iterator<NutsId> findAll(NutsIdFilter all, NutsSession session) {
        final Path path = ws.config().getStoreLocation(NutsStoreLocation.CONFIG);
        return findInFolder(path, all, Integer.MAX_VALUE, session);
    }

    @Override
    public Iterator<NutsId> findVersions(NutsId id, NutsIdFilter filter, NutsSession session) {
        return new LazyIterator<NutsId>() {
            @Override
            protected Iterator<NutsId> iterator() {
                File installFolder = ws.config().getStoreLocation(id
                        .builder().setVersion("ANY").build(), NutsStoreLocation.CONFIG).toFile().getParentFile();
                if (installFolder.isDirectory()) {
                    final NutsVersionFilter filter0 = id.getVersion().filter();
                    return IteratorBuilder.of(Arrays.asList(installFolder.listFiles()).iterator())
                            .map(new Function<File, NutsId>() {
                                @Override
                                public NutsId apply(File folder) {
                                    if (folder.isDirectory()
                                            && new File(folder, NUTS_INSTALL_FILE).isFile()) {
                                        NutsVersion vv = ws.version().parse(folder.getName());
                                        if (filter0.accept(vv, session) && (filter == null || filter.accept(
                                                id.builder().setVersion(vv).build()
                                                , session))) {
                                            return id.builder().setVersion(folder.getName()).build();
                                        }
                                    }
                                    return null;
                                }

                            })
                            .notNull().iterator();
                }
                //ok.sort((a, b) -> CoreVersionUtils.compareVersions(a, b));
                return IteratorUtils.emptyIterator();
            }
        };
    }

    @Override
    public NutsId[] findInstalledVersions(NutsId id, NutsSession session) {
        Path installFolder = ws.config().getStoreLocation(id.builder().setVersion("ANY").build(), NutsStoreLocation.CONFIG).getParent();
        List<NutsId> ok = new ArrayList<>();
        final NutsVersionFilter filter = id.getVersion().filter();
        if (Files.isDirectory(installFolder)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(installFolder)) {
                for (Path folder : ds) {
                    if (Files.isDirectory(folder) && Files.isRegularFile(folder.resolve(NUTS_INSTALL_FILE))) {
                        if (filter.accept(ws.version().parse(folder.getFileName().toString()), session)) {
                            ok.add(id.builder().setVersion(folder.getFileName().toString()).build());
                        }
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        //ok.sort((a, b) -> CoreVersionUtils.compareVersions(a, b));
        return ok.toArray(new NutsId[0]);
    }

    @Override
    public NutsId[] findDeployedVersions(NutsId id, NutsSession session) {
        Path installFolder = ws.config().getStoreLocation(id.builder().setVersion("ANY").build(), NutsStoreLocation.CONFIG).getParent();
        return IteratorUtils.toList(deployments.findInFolder(installFolder, id.filter(), 100, session)).toArray(new NutsId[0]);
    }

    @Override
    public void uninstall(NutsId id, NutsSession session) {
        NutsWorkspaceUtils.of(ws).checkReadOnly();
        session = NutsWorkspaceUtils.of(ws).validateSession(session);
        if (!contains(id, NUTS_INSTALL_FILE)) {
            throw new NutsNotInstalledException(ws, id);
        }
        try {
            remove(id, NUTS_INSTALL_FILE);
            String v = getDefaultVersion(id);
            if (v != null && v.equals(id.getVersion().getValue())) {
                Iterator<NutsId> versions = findVersions(id, null, session);
                List<NutsId> nutsIds = CoreCommonUtils.toList(versions == null ? Collections.emptyIterator() : versions);
                nutsIds.sort(null);
                if (nutsIds.size() > 0) {
                    setDefaultVersion(nutsIds.get(0), session);
                } else {
                    setDefaultVersion(id.builder().setVersion("").build(), session);
                }
            }
            undeploy(id, session);
        } catch (Exception ex) {
            throw new NutsNotInstalledException(ws, id);
        }
    }


    @Override
    public NutsInstallInformation install(NutsDefinition def, NutsSession session) {
        for (NutsDependency dependency : def.getDependencies()) {
            NutsId[] old = findDeployedVersions(dependency.getId(), session);
            if (old.length == 0) {
                throw new IllegalArgumentException("Unable to install " + def.getId() + " as dependency " + old[0] + " is missing.");
            }
        }
        deploy(def, session);
        NutsId id = def.getId();
        Instant now = Instant.now();
        String user = ws.security().getCurrentUsername();
        NutsWorkspaceUtils.of(ws).checkReadOnly();
        InstallInfoConfig ii;
        try {
            ii = new InstallInfoConfig();
            ii.setId(id);
            ii.setInstallDate(now);
            ii.setInstallUser(user);
            addJson(id, NUTS_INSTALL_FILE, ii);
        } catch (UncheckedIOException ex) {
            throw new NutsNotInstallableException(ws, id.toString(), "Unable to install "
                    + id.builder().setNamespace(null).build() + " : " + ex.getMessage(), ex);
        }
        return new DefaultNutsInstallInfo(true, isDefaultVersion(id), ws.config().getStoreLocation(id, NutsStoreLocation.APPS), ii.getInstallDate(), ii.getInstallUser());
    }

    public void addString(NutsId id, String name, String value) {
        try {
            Files.write(getPath(id, name), value.getBytes());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public <T> T readJson(NutsId id, String name, Class<T> clazz) {
        return ws.json().parse(getPath(id, name), clazz);
    }

    public void addJson(NutsId id, String name, InstallInfoConfig value) {
        ws.json().value(value).print(getPath(id, name));
    }

    public void remove(NutsId id, String name) {
        try {
            Path path = getPath(id, name);
            Files.delete(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public boolean contains(NutsId id, String name) {
        return Files.isRegularFile(getPath(id, name));
    }

    public Path getPath(NutsId id, String name) {
        return ws.config().getStoreLocation(id, NutsStoreLocation.CONFIG).resolve(name);
    }
}
