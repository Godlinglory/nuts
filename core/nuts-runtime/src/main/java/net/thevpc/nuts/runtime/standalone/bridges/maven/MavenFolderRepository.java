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
package net.thevpc.nuts.runtime.standalone.bridges.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.bundles.io.FolderNutIdIterator;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.standalone.repos.NutsCachedRepository;
import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;

/**
 * Created by vpc on 1/5/17.
 */
public class MavenFolderRepository extends NutsCachedRepository {

    protected final NutsLogger LOG;
    private final AbstractMavenRepositoryHelper helper = new AbstractMavenRepositoryHelper(this) {
        @Override
        protected String getIdPath(NutsId id) {
            return getLocationAsPath().resolve(CoreIOUtils.syspath(getIdRelativePath(id))).toString();
        }

        @Override
        protected NutsInput openStream(NutsId id, String path, Object source, String typeName, NutsSession session) {
            return getWorkspace().io().input().setTypeName(typeName).of(Paths.get(path));
        }

        @Override
        protected String getStreamSHA1(NutsId id, NutsSession session, String typeName) {
            return CoreIOUtils.evalSHA1Hex(getStream(id.builder().setFace(NutsConstants.QueryFaces.CONTENT_HASH).build(), typeName, session).open(), true);
        }

        @Override
        protected void checkSHA1Hash(NutsId id, InputStream stream, String typeName, NutsSession session) {
            try {
                stream.close();
            } catch (IOException e) {
                throw new NutsIOException(getWorkspace(),e);
            }
        }
    };

    public MavenFolderRepository(NutsAddRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_FASTER, false, NutsConstants.RepoTypes.MAVEN);
        LOG=workspace.log().of(MavenFolderRepository.class);
        if (options.getConfig().getStoreLocationStrategy() != NutsStoreLocationStrategy.STANDALONE) {
            cache.setWriteEnabled(false);
            cache.setReadEnabled(false);
        }
    }

    private Path getLocationAsPath() {
        return Paths.get(config().getLocation(true));
    }

//    @Override
//    public Path getComponentsLocation() {
//        return null;
//    }
    public Path getIdFile(NutsId id) {
        String p = getIdRelativePath(id);
        if (p != null) {
            return getLocationAsPath().resolve(p);
        }
        return null;
    }

    @Override
    public NutsDescriptor fetchDescriptorCore(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode == NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(getWorkspace(), id,new NutsFetchModeNotSupportedException(getWorkspace(),this,fetchMode,id.toString(),null));
        }
        return helper.fetchDescriptorImpl(id, fetchMode, session);
    }

    @Override
    public NutsContent fetchContentCore(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode == NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(getWorkspace(), id,new NutsFetchModeNotSupportedException(getWorkspace(),this,fetchMode,id.toString(),null));
        }
        Path f = getIdFile(id);
        if(f==null){
            throw new NutsNotFoundException(getWorkspace(), id,new RuntimeException("Invalid id"));
        }
        if(!Files.exists(f)){
            throw new NutsNotFoundException(getWorkspace(), id,new IOException("File not found : "+f));
        }
        if (localPath == null) {
            return new NutsDefaultContent(f.toString(), true, false);
        } else {
            getWorkspace().io().copy()
                    .setSession(session)
                    .from(f).to(localPath).setSafe(true).run();
            return new NutsDefaultContent(localPath, true, false);
        }
    }

    protected Path getLocalGroupAndArtifactFile(NutsId id) {
        NutsWorkspaceUtils.of(getWorkspace()).checkSimpleNameNutsId(id);
        Path groupFolder = getLocationAsPath().resolve(id.getGroupId().replace('.', File.separatorChar));
        return groupFolder.resolve(id.getArtifactId());
    }

    @Override
    public Iterator<NutsId> searchVersionsCore(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session) {

        Iterator<NutsId> namedNutIdIterator = null;
//        StringBuilder errors = new StringBuilder();
        if (fetchMode != NutsFetchMode.REMOTE) {
            if (id.getVersion().isSingleValue()) {
                Path f = getIdFile(id.builder().setFaceDescriptor().build());
                if (f != null && Files.exists(f)) {
                    NutsDescriptor d = null;
                    try {
                        d = MavenUtils.of(session.getWorkspace()).parsePomXml(f, fetchMode, this, session);
                    } catch (Exception ex) {
                        LOG.with().session(session).level(Level.SEVERE).error(ex)
                                .log("failed to parse pom file {0} : {1}", f, ex);
                        //
                    }
                    if (d != null) {
                        return Collections.singletonList(id.builder().setNamespace(getName()).build()).iterator();
                    }
                }
//                return IteratorUtils.emptyIterator();
                return null;
            }
            try {
                namedNutIdIterator = findInFolder(getLocalGroupAndArtifactFile(id),
                        getWorkspace().id().filter().nonnull(idFilter).and(
                                getWorkspace().id().filter().byName(id.getShortName())
                        ),
                        Integer.MAX_VALUE, session);
            } catch (NutsNotFoundException ex) {
//                errors.append(ex).append(" \n");
            }
        }
//        if (namedNutIdIterator == null) {
//            return IteratorUtils.emptyIterator();
//        }
        return namedNutIdIterator;

    }

    @Override
    public NutsId searchLatestVersionCore(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
        if (id.getVersion().isBlank() && filter == null) {
            Path file = getLocalGroupAndArtifactFile(id);
            NutsId bestId = null;
            if (Files.isDirectory(file)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(file, CoreIOUtils.DIR_FILTER)) {
                    for (Path versionPath : stream) {
                        NutsId id2 = id.builder().setVersion(versionPath.getFileName().toString()).build();
                        String fn = getIdFilename(id2.builder().setFaceDescriptor().build());
                        if (Files.exists(versionPath.resolve(fn))) {
                            if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                                bestId = id2;
                            }
                        }
                    }
                } catch (IOException ex) {
                    //
                }
            }
            return bestId;
        }
        return super.searchLatestVersion(id, filter, fetchMode, session);
    }

    protected Iterator<NutsId> findInFolder(Path folder, final NutsIdFilter filter, int maxDepth, NutsSession session) {
        if (folder == null || !Files.exists(folder) || !Files.isDirectory(folder)) {
            return null;//IteratorUtils.emptyIterator();
        }
        return new FolderNutIdIterator(getWorkspace(), getName(), folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsSession session) {
                MavenFolderRepository.this.undeploy().setId(id).setSession(session)
                        //.setFetchMode(NutsFetchMode.LOCAL)
                        .run();
            }

            @Override
            public boolean isDescFile(Path pathname) {
                return pathname.getFileName().toString().endsWith(".pom");
            }

            @Override
            public NutsDescriptor parseDescriptor(Path pathname, NutsSession session) throws IOException {
                return MavenUtils.of(session.getWorkspace()).parsePomXml(pathname, NutsFetchMode.LOCAL, MavenFolderRepository.this, session);
            }
        }, maxDepth);
    }

    @Override
    public Iterator<NutsId> searchCore(final NutsIdFilter filter, String[] roots, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            Path locationFolder = getLocationAsPath();
            List<Iterator<NutsId>> list = new ArrayList<>();

            for (String root : roots) {
                if(root.endsWith("/*")){
                    String name = root.substring(0, root.length() - 2);
                    list.add(findInFolder(locationFolder.resolve(name), filter, Integer.MAX_VALUE, session));
                }else{
                    list.add(findInFolder(locationFolder.resolve(root), filter, 2, session));
                }
            }

            return IteratorUtils.concat(list);
        }
//        return IteratorUtils.emptyIterator();
        return null;
    }

//    @Override
//    public Path getStoreLocation(NutsStoreLocation folderType) {
//        switch (folderType) {
//            case LIB: {
//                return null;
//            }
//            //cache not supported!
//            case CACHE: {
//                return null;
//            }
//        }
//        return super.getStoreLocation(folderType);
//    }
    @Override
    public void updateStatistics2(NutsSession session) {
        try {
            Files.walkFileTree(Paths.get(config().getLocation(true)), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    throw new NutsIOException(getWorkspace(),"updateStatistics Not supported.");
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    throw new NutsIOException(getWorkspace(),"updateStatistics Not supported.");
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    throw new NutsIOException(getWorkspace(),"updateStatistics Not supported.");
                }
            }
            );
        } catch (IOException ex) {
            throw new NutsIOException(getWorkspace(),ex);
        }
    }

    @Override
    protected String getIdExtension(NutsId id) {
        return helper.getIdExtension(id);
    }

    @Override
    public boolean isAcceptFetchMode(NutsFetchMode mode) {
        return mode==NutsFetchMode.LOCAL;
    }
}
