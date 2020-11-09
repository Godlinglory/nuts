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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.bridges.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.CoreNutsConstants;
import net.thevpc.nuts.runtime.DefaultNutsId;
import net.thevpc.nuts.runtime.log.NutsLogVerb;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.util.SearchTraceHelper;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.io.FilesFoldersApi;
import net.thevpc.nuts.runtime.util.RemoteRepoApi;
import net.thevpc.nuts.runtime.util.iter.IteratorUtils;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.bridges.maven.mvnutil.MavenMetadata;
import net.thevpc.nuts.runtime.main.repos.NutsCachedRepository;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenRemoteRepository extends NutsCachedRepository {

    private final NutsLogger LOG;
    private MvnClient wrapper;

    private RemoteRepoApi versionApi = RemoteRepoApi.DEFAULT;
    private RemoteRepoApi findApi = RemoteRepoApi.DEFAULT;
    private AbstractMavenRepositoryHelper helper = new AbstractMavenRepositoryHelper(this) {
        @Override
        protected String getIdPath(NutsId id) {
            return getIdRemotePath(id);
        }

        @Override
        protected NutsInput openStream(NutsId id, String path, Object source, String typeName, NutsSession session) {
            SearchTraceHelper.progressIndeterminate("search "+CoreIOUtils.compressUrl(path),session);
            return getWorkspace().io().monitor().source(path).origin(source).setSession(session).setSourceTypeName(typeName).createSource();
        }

    };

    private final FilesFoldersApi.IteratorModel findModel = new FilesFoldersApi.IteratorModel() {
        @Override
        public void undeploy(NutsId id, NutsSession session) throws NutsExecutionException {
            throw new NutsUnsupportedOperationException(getWorkspace(), "Not supported undeploy.");
        }

        @Override
        public boolean isDescFile(String pathname) {
            return pathname.endsWith(".pom");
        }

        @Override
        public NutsDescriptor parseDescriptor(String pathname, InputStream in, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) throws IOException {
            SearchTraceHelper.progressIndeterminate("parse "+CoreIOUtils.compressUrl(pathname),session);
            return MavenUtils.of(session.getWorkspace()).parsePomXml(in, fetchMode, pathname, repository, session);
        }
    };

    public MavenRemoteRepository(NutsAddRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_SLOW, false, NutsConstants.RepoTypes.MAVEN);
        LOG=workspace.log().of(MavenRemoteRepository.class);
    }

    protected MavenRemoteRepository(NutsAddRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository, String repoType) {
        super(options, workspace, parentRepository, SPEED_SLOW, false, repoType);
        LOG=workspace.log().of(MavenRemoteRepository.class);
    }

    @Override
    public NutsDescriptor fetchDescriptorCore(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(getWorkspace(), id,new NutsFetchModeNotSupportedException(getWorkspace(),this,fetchMode,id.toString(),null));
        }
        return helper.fetchDescriptorImpl(id, fetchMode, session);
    }

    @Override
    public Iterator<NutsId> searchVersionsCore(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, final NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return null;
        }
        if (id.getVersion().isSingleValue()) {
            String groupId = id.getGroupId();
            String artifactId = id.getArtifactId();
            List<NutsId> ret = new ArrayList<>();
            String metadataURL = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/" + id.getVersion().toString() + "/"
                    + getIdFilename(id.builder().setFaceDescriptor().build())
            );

            try (InputStream metadataStream = helper.openStream(id, metadataURL, id.builder().setFace(CoreNutsConstants.QueryFaces.CATALOG).build(), "artifact catalog", session).open()) {
                // ok found!!
                ret.add(id);
            } catch (UncheckedIOException | IOException | NutsIOException ex) {
                //ko not found
            }
            return ret.iterator();
        }
        NutsIdFilter filter2 = getWorkspace().id().filter().nonnull(idFilter).and(
                getWorkspace().id().filter().byName(id.getShortName())
        );
        switch (versionApi) {
            case DEFAULT:
            case MAVEN: {
                return findVersionsImplMetadataXml(id, filter2, fetchMode, session);
            }
            case GITHUB: {
                return findVersionsImplGithub(id, filter2, fetchMode, session);
            }
            case FILES_FOLDERS: {
                return findVersionsImplFilesFolders(id, filter2, fetchMode, session);
            }
            case UNSUPPORTED: {
                return null;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getWorkspace(), String.valueOf(versionApi));
            }
        }
    }

    public Iterator<NutsId> findVersionsImplGithub(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return IteratorUtils.emptyIterator();
        }
        if (id.getVersion().isSingleValue()) {
            return findSingleVersionImpl(id, idFilter, fetchMode, session);
        }
        String location = config().getLocation(true);
        String[] all = location.split("/+");
        String userName = all[2];
        String repo = all[3];
        String apiUrlBase = "https://api.github.com/repos/" + userName + "/" + repo + "/contents";
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        InputStream metadataStream = null;
        List<NutsId> ret = new ArrayList<>();
        try {
            String metadataURL = CoreIOUtils.buildUrl(apiUrlBase, groupId.replace('.', '/') + "/" + artifactId);

            try {
                metadataStream = helper.openStream(id, metadataURL, id.builder().setFace(CoreNutsConstants.QueryFaces.CATALOG).build(), "artifact catalog", session).open();
            } catch (UncheckedIOException|NutsIOException ex) {
                throw new NutsNotFoundException(getWorkspace(), id, ex);
            }
            List<Map<String, Object>> info = getWorkspace().formats().json().parse(new InputStreamReader(metadataStream), List.class);
            if (info != null) {
                for (Map<String, Object> version : info) {
                    if ("dir".equals(version.get("type"))) {
                        String versionName = (String) version.get("name");
                        final NutsId nutsId = id.builder().setVersion(versionName).build();

                        if (idFilter != null && !idFilter.acceptId(nutsId, session)) {
                            continue;
                        }
                        ret.add(
                                new DefaultNutsId(
                                        null,
                                        groupId,
                                        artifactId,
                                        versionName,
                                        ""
                                )
                        );
                    }

                }
            }
        } finally {
            if (metadataStream != null) {
                try {
                    metadataStream.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return ret.iterator();
    }

    public Iterator<NutsId> findVersionsImplMetadataXml(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, final NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return null;
        }
        if (id.getVersion().isSingleValue()) {
            return findSingleVersionImpl(id, idFilter, fetchMode, session);
        }
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        InputStream metadataStream = null;
        List<NutsId> ret = new ArrayList<>();
        try {
            String metadataURL = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/maven-metadata.xml");

            try {
                metadataStream = helper.openStream(id, metadataURL, id.builder().setFace(CoreNutsConstants.QueryFaces.CATALOG).build(), "artifact catalog", session).open();
            } catch (UncheckedIOException|NutsIOException ex) {
                return null;
            }
            MavenMetadata info = MavenUtils.of(session.getWorkspace()).parseMavenMetaData(metadataStream);
            if (info != null) {
                for (String version : info.getVersions()) {
                    final NutsId nutsId = id.builder().setVersion(version).build();

                    if (idFilter != null && !idFilter.acceptId(nutsId, session)) {
                        continue;
                    }
                    ret.add(
                            new DefaultNutsId(
                                    null,
                                    groupId,
                                    artifactId,
                                    version,
                                    ""
                            )
                    );
                }
            }
        } catch (UncheckedIOException|NutsIOException ex) {
            //unable to access
            return null;
        } finally {
            if (metadataStream != null) {
                try {
                    metadataStream.close();
                } catch (IOException e) {
//                    throw new UncheckedIOException(e);
                    return null;
                }
            }
        }
        return ret.iterator();

    }

    public Iterator<NutsId> findSingleVersionImpl(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, final NutsSession session) {
        if (id.getVersion().isSingleValue()) {
            String groupId = id.getGroupId();
            String artifactId = id.getArtifactId();
            List<NutsId> ret = new ArrayList<>();
            String metadataURL = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/" + id.getVersion().toString() + "/"
                    + getIdFilename(id.builder().setFaceDescriptor().build())
            );

            try (InputStream metadataStream = helper.openStream(id, metadataURL, id.builder().setFace(CoreNutsConstants.QueryFaces.CATALOG).build(), "artifact catalog", session).open()) {
                // ok found!!
                ret.add(id);
            } catch (UncheckedIOException | IOException ex) {
                //ko not found
            }
            return ret.iterator();
        } else {
            throw new NutsIllegalArgumentException(getWorkspace(), "Expected single version in " + id);
        }
    }

    public Iterator<NutsId> findVersionsImplFilesFolders(final NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, final NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return IteratorUtils.emptyIterator();
        }
        if (id.getVersion().isSingleValue()) {
            return findSingleVersionImpl(id, idFilter, fetchMode, session);
        }
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        InputStream foldersFileStream = null;
        List<NutsId> ret = new ArrayList<>();
        try {
            String foldersFileUrl = CoreIOUtils.buildUrl(config().getLocation(true), groupId.replace('.', '/') + "/" + artifactId + "/.folders");
            String[] foldersFileContent = null;
            try {
                SearchTraceHelper.progressIndeterminate("search "+CoreIOUtils.compressUrl(foldersFileUrl),session);
                foldersFileStream = helper.openStream(id, foldersFileUrl, id.builder().setFace(CoreNutsConstants.QueryFaces.CATALOG).build(), "artifact catalog", session).open();
                foldersFileContent = CoreIOUtils.loadString(foldersFileStream, true).split("(\n|\r)+");
            } catch (UncheckedIOException|NutsIOException ex) {
                throw new NutsNotFoundException(getWorkspace(), id, ex);
            }
            if (foldersFileContent != null) {
                for (String version : foldersFileContent) {
                    final NutsId nutsId = id.builder().setVersion(version).build();

                    if (idFilter != null && !idFilter.acceptId(nutsId, session)) {
                        continue;
                    }
                    ret.add(
                            new DefaultNutsId(
                                    null,
                                    groupId,
                                    artifactId,
                                    version,
                                    ""
                            )
                    );
                }
            }
        } finally {
            if (foldersFileStream != null) {
                try {
                    foldersFileStream.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return ret.iterator();
    }

    @Override
    public Iterator<NutsId> searchCore(final NutsIdFilter filter, String[] roots, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return null;
        }
        switch (findApi) {
            case DEFAULT:
            case FILES_FOLDERS:
            case GITHUB:
            case MAVEN: {
                List<Iterator<NutsId>> li = new ArrayList<>();
                for (String root : roots) {
                    SearchTraceHelper.progressIndeterminate("search "+CoreIOUtils.compressUrl(root),session);
                    if(root.endsWith("/*")) {
                        String name = root.substring(0, root.length() - 2);
                        li.add(FilesFoldersApi.createIterator(getWorkspace(), this, config().getLocation(true), name, filter, session, Integer.MAX_VALUE, findModel));
                    }else{
                        li.add(FilesFoldersApi.createIterator(getWorkspace(), this, config().getLocation(true), root, filter, session, 2, findModel));
                    }
                }
                return IteratorUtils.concat(li);
            }
//            case MAVEN: {
//                // this will find only in archetype, not in full index....
//                String url = CoreIOUtils.buildUrl(config().getLocation(true), "/archetype-catalog.xml");
//                try {
//                    CoreInput s = CoreIOUtils.getCachedUrlWithSHA1(getWorkspace(), url, session);
//                    final InputStream is = getWorkspace().io().monitorInputStream(s.open(), session);
//                    return MavenUtils.createArchetypeCatalogIterator(is, filter, true);
//                } catch (UncheckedIOException ex) {
//                    return IteratorUtils.emptyIterator();
//                }
//            }
            case UNSUPPORTED: {
                return null;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getWorkspace(), String.valueOf(versionApi));
            }
        }
    }

    private NutsRepository getLocalMavenRepo(NutsSession session) {
        for (NutsRepository nutsRepository : getWorkspace().repos().getRepositories(session)) {
            if (nutsRepository.getRepositoryType().equals(NutsConstants.RepoTypes.MAVEN)
                    && nutsRepository.config().getLocation(true) != null
                    && nutsRepository.config().getLocation(true).equals(
                    Paths.get(getWorkspace().io().expandPath("~/.m2")).toString()
                    )) {
                return nutsRepository;
            }
        }
        return null;
    }

    protected Path getMavenLocalFolderContent(NutsId id) {
        String p = getIdRelativePath(id);
        if (p != null) {
            return Paths.get(System.getProperty("user.home"), ".m2", p);
        }
        return null;
    }

    private MvnClient getWrapper() {
        if (true) {
            return null;
        }
        return new MvnClient(getWorkspace());
    }

    @Override
    public NutsContent fetchContentCore(NutsId id, NutsDescriptor descriptor, Path localPath, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(getWorkspace(), id,new NutsFetchModeNotSupportedException(getWorkspace(),this,fetchMode,id.toString(),null));
        }
        if (wrapper == null) {
            wrapper = getWrapper();
        }
        if (wrapper != null && wrapper.get(id, config().getLocation(true), session)) {
            NutsRepository loc = getLocalMavenRepo(session);
            if (loc != null) {
                return loc.fetchContent()
                        .setId(id)
                        .setDescriptor(descriptor)
                        .setLocalPath(localPath)
                        .setSession(session)
                        .setFetchMode(NutsFetchMode.LOCAL)
                        .run()
                        .getResult();
            }
            //should be already downloaded to m2 folder
            Path content = getMavenLocalFolderContent(id);
            if (content != null && Files.exists(content)) {
                if (localPath == null) {
                    return new NutsDefaultContent(content, true, false);
                } else {
                    Path tempFile = getWorkspace().io().tmp().createTempFile(content.getFileName().toString(), this);
                    getWorkspace().io().copy()
                            .setSession(session)
                            .from(content).to(tempFile).safe().run();
                    return new NutsDefaultContent(tempFile, true, false);
                }
            }
        }
        if (localPath == null) {
            String p = helper.getIdPath(id);
            Path tempFile = getWorkspace().io().tmp().createTempFile(new File(p).getName(), this);
            try {
                getWorkspace().io().copy()
                        .setSession(session)
                        .from(helper.getStream(id, "artifact content", session)).to(tempFile).validator(new NutsIOCopyValidator() {
                    @Override
                    public void validate(InputStream in) throws IOException {
                        helper.checkSHA1Hash(id.builder().setFace(NutsConstants.QueryFaces.CONTENT_HASH).build(), in, "artifact content", session);
                    }
                }).run();
            } catch (UncheckedIOException|NutsIOException ex) {
                throw new NutsNotFoundException(getWorkspace(), id, null, ex);
            }
            return new NutsDefaultContent(tempFile, false, true);
        } else {
            try {
                getWorkspace().io().copy()
                        .setSession(session)
                        .from(helper.getStream(id, "artifact content", session)).to(localPath).validator(new NutsIOCopyValidator() {
                    @Override
                    public void validate(InputStream in) throws IOException {
                        helper.checkSHA1Hash(id.builder().setFace(NutsConstants.QueryFaces.CONTENT_HASH).build(), in, "artifact content", session);
                    }
                }).run();
            } catch (UncheckedIOException|NutsIOException ex) {
                LOG.with().level(Level.SEVERE).verb(NutsLogVerb.FAIL).log( id.toString() + " : " + CoreStringUtils.exceptionToString(ex));
                throw new NutsNotFoundException(getWorkspace(), id, null, ex);
            }
            return new NutsDefaultContent(localPath, false, false);
        }
    }

    @Override
    protected String getIdExtension(NutsId id) {
        return helper.getIdExtension(id);
    }

}
