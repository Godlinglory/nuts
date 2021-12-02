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
package net.thevpc.nuts.runtime.standalone.repository.impl.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.AbstractMavenRepositoryHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MvnClient;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsCachedRepository;
import net.thevpc.nuts.runtime.standalone.repository.NutsIdPathIterator;
import net.thevpc.nuts.runtime.standalone.repository.NutsIdPathIteratorBase;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenFolderRepository extends NutsCachedRepository {

    protected final AbstractMavenRepositoryHelper repoHelper = new RepoHelper();
    protected final NutsIdPathIteratorBase repoIter = new RepoIter();
    private final NutsLogger LOG;
    private MvnClient wrapper;

    public MavenFolderRepository(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository) {
        super(options, session, parentRepository,
                NutsPath.of(options.getConfig().getLocation(), session).isRemote() ? NutsSpeedQualifier.SLOW : NutsSpeedQualifier.FASTER,
                false, NutsConstants.RepoTypes.MAVEN);
        LOG = NutsLogger.of(getClass(), session);
        if (!isRemote()) {
            if (options.getConfig().getStoreLocationStrategy() != NutsStoreLocationStrategy.STANDALONE) {
                cache.setWriteEnabled(false);
                cache.setReadEnabled(false);
            }
        }
    }

    @Override
    protected boolean isSupportedDeployImpl(NutsSession session) {
        return false;
    }

    @Override
    protected boolean isAvailableImpl(NutsSession session) {
        long now = System.currentTimeMillis();
        try {
            NutsPath loc = config().setSession(initSession).getLocation(true);
            try {
                return loc.exists();
            } finally {
                LOG.with().level(Level.FINEST).verb(NutsLogVerb.SUCCESS)
                        .time(System.currentTimeMillis() - now)
                        .log(NutsMessage.cstyle("check available %s : success", getName()));
            }
        } catch (Exception e) {
            LOG.with().level(Level.FINEST).verb(NutsLogVerb.FAIL)
                    .time(System.currentTimeMillis() - now)
                    .log(NutsMessage.cstyle("check available %s : failed", getName()));
            return false;
        }
    }

    @Override
    public NutsIterator<NutsId> searchVersionsCore(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, final NutsSession session) {
        if (!acceptedFetchNoCache(fetchMode)) {
            return null;
        }
        NutsIdFilter filter2 = NutsIdFilters.of(session).nonnull(idFilter).and(
                NutsIdFilters.of(session).byName(id.getShortName())
        );
        if (id.getVersion().isSingleValue()) {
            return findSingleVersionImpl(id, filter2, fetchMode, session);
        }
        return findNonSingleVersionImpl(id, filter2, fetchMode, session);
    }

    @Override
    public NutsDescriptor fetchDescriptorCore(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        if (!acceptedFetchNoCache(fetchMode)) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        return repoHelper.fetchDescriptorImpl(id, fetchMode, session);
    }

    @Override
    public NutsContent fetchContentCore(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
        if (!acceptedFetchNoCache(fetchMode)) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        if (wrapper == null) {
            wrapper = getWrapper(session);
        }
        if (wrapper != null && wrapper.get(id, config().setSession(session).getLocation(true).toString(), session)) {
            NutsRepository repo = getLocalMavenRepo(session);
            if (repo != null) {
                NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(repo);
                return repoSPI.fetchContent()
                        .setId(id)
                        .setDescriptor(descriptor)
                        .setLocalPath(localPath)
                        .setSession(session)
                        .setFetchMode(NutsFetchMode.LOCAL)
                        .run()
                        .getResult();
            }
            //should be already downloaded to m2 folder
            NutsPath content = getMavenLocalFolderContent(id, session);
            if (content != null && content.exists()) {
                if (localPath == null) {
                    return new NutsDefaultContent(
                            content, true, false);
                } else {
                    String tempFile = NutsTmp.of(session)
                            .setRepositoryId(getUuid())
                            .createTempFile(content.getName()).toString();
                    NutsCp.of(session)
                            .from(content).to(tempFile).addOptions(NutsPathOption.SAFE).run();
                    return new NutsDefaultContent(
                            NutsPath.of(tempFile, session), true, false);
                }
            }
        }
        if (localPath == null) {
            NutsPath p = repoHelper.getIdPath(id, session);
            if (p.isLocal()) {
                return new NutsDefaultContent(p, false, false);
            } else {
                String tempFile = NutsTmp.of(session)
                        .setRepositoryId(getUuid())
                        .createTempFile(p.getName()).toString();
                try {
                    NutsCp.of(session)
                            .from(repoHelper.getStream(id, "artifact binaries", "retrieve", session)).to(tempFile).setValidator(new NutsIOCopyValidator() {
                                @Override
                                public void validate(InputStream in) throws IOException {
                                    repoHelper.checkSHA1Hash(id.builder().setFace(NutsConstants.QueryFaces.CONTENT_HASH).build(), in, "artifact binaries", session);
                                }
                            }).run();
                } catch (UncheckedIOException | NutsIOException ex) {
                    throw new NutsNotFoundException(session, id, null, ex);
                }
                return new NutsDefaultContent(NutsPath.of(tempFile, session), true, true);
            }
        } else {
            try {
                NutsCp.of(session)
                        .from(repoHelper.getStream(id, "artifact content", "retrieve", session)).to(localPath)
                        .setValidator(in -> repoHelper.checkSHA1Hash(
                                        id.builder().setFace(NutsConstants.QueryFaces.CONTENT_HASH).build(),
                                        in, "artifact binaries", session
                                )
                        ).addOptions(NutsPathOption.LOG)
                        .run();
            } catch (UncheckedIOException | NutsIOException ex) {
                throw new NutsNotFoundException(session, id, null, ex);
            }
            return new NutsDefaultContent(
                    NutsPath.of(localPath, session), true, false);
        }
    }

    @Override
    public NutsIterator<NutsId> searchCore(final NutsIdFilter filter, NutsPath[] basePaths, NutsFetchMode fetchMode, NutsSession session) {
        if (!acceptedFetchNoCache(fetchMode)) {
            return null;
        }
        NutsPath repoRoot = config().setSession(session).getLocation(true);
        List<NutsIterator<? extends NutsId>> list = new ArrayList<>();
        for (NutsPath basePath : basePaths) {
            list.add(
                    (NutsIterator) IteratorBuilder.ofRunnable(
                            () -> session.getTerminal().printProgress("%-14s %-8s %s",getName(), "browse",
                                    (basePath == null ? repoRoot : repoRoot.resolve(basePath)).toCompressedForm()
                            ),
                            "Log"

                    ).build());
            if (basePath.getName().equals("*")) {
                list.add(new NutsIdPathIterator(this, repoRoot, basePath.getParent(), filter, session, repoIter, Integer.MAX_VALUE, "core",null));
            } else {
                list.add(new NutsIdPathIterator(this, repoRoot, basePath, filter, session, repoIter, 2, "core", null));
            }
        }
        return IteratorUtils.concat(list);
    }

    @Override
    public void updateStatistics2(NutsSession session) {
        config().setSession(session).getLocation(true)
                .walkDfs(new NutsTreeVisitor<NutsPath>() {
                             @Override
                             public NutsTreeVisitResult preVisitDirectory(NutsPath dir, NutsSession session) {

                                 return NutsTreeVisitResult.CONTINUE;
                             }

                             @Override
                             public NutsTreeVisitResult visitFile(NutsPath file, NutsSession session) {
                                 throw new NutsIOException(session, NutsMessage.cstyle("updateStatistics Not supported."));
                             }

                             @Override
                             public NutsTreeVisitResult visitFileFailed(NutsPath file, Exception exc, NutsSession session) {
                                 throw new NutsIOException(session, NutsMessage.cstyle("updateStatistics Not supported."));
                             }

                             @Override
                             public NutsTreeVisitResult postVisitDirectory(NutsPath dir, Exception exc, NutsSession session) {
                                 throw new NutsIOException(session, NutsMessage.cstyle("updateStatistics Not supported."));
                             }
                         }
                );
    }

    @Override
    public boolean isAcceptFetchMode(NutsFetchMode mode, NutsSession session) {
        return isRemote() || mode == NutsFetchMode.LOCAL;
    }

    @Override
    public boolean isRemote() {
        return config().setSession(initSession).getLocation(true).isRemote();
    }

    protected boolean acceptedFetchNoCache(NutsFetchMode fetchMode) {
        return (fetchMode == NutsFetchMode.REMOTE) == isRemote();
    }

    public NutsIterator<NutsId> findNonSingleVersionImpl(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, final NutsSession session) {
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        NutsPath foldersFileUrl = config().setSession(session).getLocation(true).resolve(groupId.replace('.', '/') + "/" + artifactId + "/");

        return IteratorBuilder.ofSupplier(
                () -> {
                    List<NutsId> ret = new ArrayList<>();
                    NutsPath[] all = foldersFileUrl.list().filter(
                            NutsPath::isDirectory, "isDirectory"
                    ).toArray(NutsPath[]::new);
                    for (NutsPath version : all) {
                        final NutsId nutsId = id.builder().setVersion(version.getName()).build();
                        if (idFilter != null && !idFilter.acceptId(nutsId, session)) {
                            continue;
                        }
                        ret.add(NutsIdBuilder.of(session).setGroupId(groupId).setArtifactId(artifactId).setVersion(version.getName()).build());
                    }
                    return NutsIterator.of(ret.iterator(), "findNonSingleVersion");
                }
                , e -> e.ofObject()
                        .set("type", "NonSingleVersion")
                        .set("path", foldersFileUrl.toString())
                        .build()
        ).build();
    }

    public NutsIterator<NutsId> findSingleVersionImpl(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, final NutsSession session) {
        if (id.getVersion().isSingleValue()) {
            String groupId = id.getGroupId();
            String artifactId = id.getArtifactId();
            NutsPath metadataURL = config().setSession(session).getLocation(true).resolve(groupId.replace('.', '/') + "/" + artifactId + "/" + id.getVersion().toString() + "/"
                    + getIdFilename(id.builder().setFaceDescriptor().build(), session)
            );
            return IteratorBuilder.ofSupplier(
                    () -> {
                        List<NutsId> ret = new ArrayList<>();
                        session.getTerminal().printProgress("%-14s %-8s %s", getName(), "search", metadataURL.toCompressedForm());
                        if (metadataURL.isRegularFile()) {
                            // ok found!!
                            ret.add(id);
                        }
                        return ret.iterator();
                    }
                    , e -> e.ofObject()
                            .set("type", "SingleVersion")
                            .set("path", metadataURL.toString())
                            .build()
            ).build();
        } else {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("expected single version in %s", id));
        }
    }

    private NutsRepository getLocalMavenRepo(NutsSession session) {
        for (NutsRepository nutsRepository : session.repos().setSession(session).getRepositories()) {
            if (nutsRepository.getRepositoryType().equals(NutsConstants.RepoTypes.MAVEN)
                    && nutsRepository.config().getLocation(true) != null
                    && nutsRepository.config().getLocation(true).toString()
                    .equals(
                            Paths.get(NutsPath.of("~/.m2", session).toAbsolute(session.locations().getWorkspaceLocation()).toString()).toString()
                    )) {
                return nutsRepository;
            }
        }
        return null;
    }

    protected NutsPath getMavenLocalFolderContent(NutsId id, NutsSession session) {
        NutsPath p = getIdRelativePath(id, session);
        if (p != null) {
            return NutsPath.ofUserHome(session).resolve(".m2").resolve(p);
        }
        return null;
    }

    private MvnClient getWrapper(NutsSession session) {
        if (true) {
            return null;
        }
        return new MvnClient(session);
    }

    @Override
    protected String getIdExtension(NutsId id, NutsSession session) {
        return repoHelper.getIdExtension(id, session);
    }

    private class RepoIter extends NutsIdPathIteratorBase {
        @Override
        public void undeploy(NutsId id, NutsSession session) throws NutsExecutionException {
            MavenFolderRepository.this.undeploy().setId(id).setSession(session)
                    //.setFetchMode(NutsFetchMode.LOCAL)
                    .run();
        }

        @Override
        public boolean isDescFile(NutsPath pathname) {
            return pathname.getName().endsWith(".pom");
        }

        @Override
        public NutsDescriptor parseDescriptor(NutsPath pathname, InputStream in, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session, NutsPath rootURL) throws IOException {
            session.getTerminal().printProgress("%-8s %s", "parse", pathname.toCompressedForm());
            return MavenUtils.of(session).parsePomXmlAndResolveParents(in, fetchMode, pathname.toString(), repository);
        }

        @Override
        public NutsId parseId(NutsPath pomFile, NutsPath rootPath, NutsIdFilter filter, NutsRepository repository, NutsSession session) throws IOException {
            String fn = pomFile.getName();
            if (fn.endsWith(".pom")) {
                NutsPath versionFolder = pomFile.getParent();
                if (versionFolder != null) {
                    String vn = versionFolder.getName();
                    NutsPath artifactFolder = versionFolder.getParent();
                    if (artifactFolder != null) {
                        String an = artifactFolder.getName();
                        if (fn.equals(an + "-" + vn + ".pom")) {
                            NutsPath groupFolder = artifactFolder.getParent();
                            if (groupFolder != null) {
                                NutsPath gg = groupFolder.subpath(rootPath.getPathCount(), groupFolder.getPathCount());
                                StringBuilder gn = new StringBuilder();
                                for (int i = 0; i < gg.getPathCount(); i++) {
                                    String ns = gg.getItem(i);
                                    if (i > 0) {
                                        gn.append('.');
                                    }
                                    gn.append(ns);
                                }
                                return validate(
                                        NutsIdBuilder.of(session)
                                                .setGroupId(gn.toString())
                                                .setArtifactId(an)
                                                .setVersion(vn)
                                                .build(),
                                        null, pomFile, rootPath, filter, repository, session);
                            }
                        }
                    }
                }
            }
            return null;
        }
    }

    private class RepoHelper extends AbstractMavenRepositoryHelper {
        public RepoHelper() {
            super(MavenFolderRepository.this);
        }

        @Override
        public NutsPath getIdPath(NutsId id, NutsSession session) {
            return getIdRemotePath(id, session);
        }
    }
}
