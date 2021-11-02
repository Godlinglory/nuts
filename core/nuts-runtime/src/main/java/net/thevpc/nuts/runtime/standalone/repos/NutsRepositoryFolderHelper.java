/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.FolderNutIdIterator;
import net.thevpc.nuts.runtime.bundles.io.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.bundles.io.NutsStreamOrPath;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.runtime.core.events.DefaultNutsContentEvent;
import net.thevpc.nuts.runtime.core.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt0;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryUtils;
import net.thevpc.nuts.runtime.core.terminals.DefaultWriteTypeProcessor;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.exec.CharacterizedExecFile;
import net.thevpc.nuts.runtime.standalone.wscommands.exec.DefaultNutsArtifactPathExecutable;
import net.thevpc.nuts.spi.NutsDeployRepositoryCommand;
import net.thevpc.nuts.spi.NutsRepositorySPI;
import net.thevpc.nuts.spi.NutsRepositoryUndeployCommand;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author thevpc
 */
public class NutsRepositoryFolderHelper {
    private final NutsRepository repo;
    private final NutsSession ws;
    private final Path rootPath;
    private final boolean cacheFolder;
    private NutsLogger LOG;
    private boolean readEnabled = true;
    private boolean writeEnabled = true;

    public NutsRepositoryFolderHelper(NutsRepository repo, NutsSession ws, Path rootPath, boolean cacheFolder) {
        this.repo = repo;
        this.ws = ws;
        if (ws == null && repo == null) {
            throw new IllegalArgumentException("both workspace and repository are null");
        }
        this.rootPath = rootPath;
        this.cacheFolder = cacheFolder;
    }

    public boolean isReadEnabled() {
        return readEnabled;
    }

    public void setReadEnabled(boolean readEnabled) {
        this.readEnabled = readEnabled;
    }

    public boolean isWriteEnabled() {
        return writeEnabled;
    }

    public void setWriteEnabled(boolean writeEnabled) {
        this.writeEnabled = writeEnabled;
    }

    public Path getLongIdLocalFolder(NutsId id, NutsSession session) {
        NutsWorkspaceUtils.of(session).checkNutsId(id);
        if (repo == null) {
            return getStoreLocation().resolve(session.locations().setSession(session).getDefaultIdBasedir(id));
        }
        return getStoreLocation().resolve(NutsRepositoryExt0.of(repo).getIdBasedir(id, session));
    }

    public Path getLongIdLocalFile(NutsId id, NutsSession session) {
        if (repo == null) {
            return getLongIdLocalFolder(id, session).resolve(session.locations().setSession(session).getDefaultIdFilename(id));
        }
        return getLongIdLocalFolder(id, session).resolve(NutsRepositoryExt0.of(repo).getIdFilename(id, session));
    }

    public Path getShortIdLocalFolder(NutsId id, NutsSession session) {
        NutsWorkspaceUtils.of(session).checkShortId(id);
        if (repo == null) {
            return getStoreLocation().resolve(session.locations().getDefaultIdBasedir(id.builder().setVersion("").build()));
        }
        return getStoreLocation().resolve(NutsRepositoryExt0.of(repo).getIdBasedir(id.builder().setVersion("").build(), session));
    }

    public NutsContent fetchContentImpl(NutsId id, String localPath, NutsSession session) {
        Path cacheContent = getLongIdLocalFile(id.builder().setFaceContent().build(), session);
        if (cacheContent != null && pathExists(cacheContent, session)) {
            return new NutsDefaultContent(
                    NutsPath.of(cacheContent,session),
                    cacheFolder, false);
        }
        return null;
    }

    public NutsWorkspace getWorkspace() {
        return ws.getWorkspace();
    }

    public NutsSession getSession() {
        return ws;
    }


    protected String getIdFilename(NutsId id, NutsSession session) {
        if (repo == null) {
            return session.locations().getDefaultIdFilename(id);
        }
        return NutsRepositoryExt0.of(repo).getIdFilename(id, session);
    }

    public Path getGoodPath(NutsId id, NutsSession session) {
        String idFilename = getIdFilename(id, session);
        Path versionFolder = getLongIdLocalFolder(id, session);
        return versionFolder.resolve(idFilename);
    }

    protected NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        String idFilename = getIdFilename(id.builder().setFaceDescriptor().build(), session);
        Path goodFile = null;
        Path versionFolder = getLongIdLocalFolder(id, session);
        goodFile = versionFolder.resolve(idFilename);
        if (pathExists(goodFile, session)) {
            return NutsDescriptorParser.of(session).parse(goodFile);
        }
//        String alt = id.getAlternative();
//        String goodAlt = null;
//        if (CoreNutsUtils.isDefaultAlternative(alt)) {
//            goodFile = versionFolder.resolve(idFilename);
//            if (Files.exists(goodFile)) {
//                return getWorkspace().descriptor().parse(goodFile);
//            }
//        } else if (!NutsBlankable.isBlank(alt)) {
//            goodAlt = alt.trim();
//            goodFile = versionFolder.resolve(goodAlt).resolve(idFilename);
//            if (Files.exists(goodFile)) {
//                return getWorkspace().descriptor().parse(goodFile).setAlternative(goodAlt);
//            }
//        } else {
//            //should test all files
//            NutsDescriptor best = null;
//            if (Files.isDirectory(versionFolder)) {
//                try (final DirectoryStream<Path> subFolders = Files.newDirectoryStream(versionFolder)) {
//                    for (Path subFolder : subFolders) {
//                        if (Files.isDirectory(subFolder)) {
//                            NutsDescriptor choice = null;
//                            try {
//                                choice = loadMatchingDescriptor(subFolder.resolve(idFilename), id, session).setAlternative(subFolder.getFileName().toString());
//                            } catch (Exception ex) {
//                                //
//                            }
//                            if (choice != null) {
//                                if (best == null || CoreNutsUtils.NUTS_DESC_ENV_SPEC_COMPARATOR.compare(best, choice) < 0) {
//                                    best = choice;
//                                }
//                            }
//                        }
//                    }
//                } catch (IOException ex) {
//                    throw new UncheckedIOException(ex);
//                }
//            }
//            goodFile = versionFolder.resolve(idFilename);
//            if (Files.exists(goodFile)) {
//                NutsDescriptor c = null;
//                try {
//                    c = getWorkspace().descriptor().parse(goodFile).setAlternative("");
//                } catch (Exception ex) {
//                    //
//                }
//                if (c != null) {
//                    if (best == null || CoreNutsUtils.NUTS_DESC_ENV_SPEC_COMPARATOR.compare(best, c) < 0) {
//                        best = c;
//                    }
//                }
//            }
//            if (best != null) {
//                return best;
//            }
//        }
        return null;
    }

    protected NutsDescriptor loadMatchingDescriptor(Path file, NutsId id, NutsSession session) {
        if (pathExists(file, session)) {
            NutsDescriptor d = Files.isRegularFile(file) ? NutsDescriptorParser.of(session).parse(file) : null;
            if (d != null) {
                Map<String, String> query = id.getProperties();
                String os = query.get(NutsConstants.IdProperties.OS);
                String arch = query.get(NutsConstants.IdProperties.ARCH);
                String dist = query.get(NutsConstants.IdProperties.OS_DIST);
                String platform = query.get(NutsConstants.IdProperties.PLATFORM);
                String de = query.get(NutsConstants.IdProperties.DESKTOP_ENVIRONMENT);
                if (CoreFilterUtils.matchesEnv(arch, os, dist, platform, de, d.getCondition(), session)) {
                    return d;
                }
            }
        }
        return null;
    }

    public Path getLocalGroupAndArtifactFile(NutsId id, NutsSession session) {
        NutsWorkspaceUtils.of(session).checkShortId(id);
        Path groupFolder = getStoreLocation().resolve(id.getGroupId().replace('.', File.separatorChar));
        return groupFolder.resolve(id.getArtifactId());
    }

    public Iterator<NutsId> searchVersions(NutsId id, final NutsIdFilter filter, boolean deep, NutsSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        if (id.getVersion().isSingleValue()) {
            NutsId id1 = id.builder().setFaceDescriptor().build();
            Path localFile = getLongIdLocalFile(id1, session);
            if (localFile != null && Files.isRegularFile(localFile)) {
                return Collections.singletonList(id.builder().setRepository(repo == null ? null : repo.getName()).build()).iterator();
            }
            return null;
        }
        NutsWorkspace ws = session.getWorkspace();
        NutsIdFilter filter2 = NutsIdFilters.of(session).all(filter,
                NutsIdFilters.of(session).byName(id.getShortName())
        );
        return findInFolder(getLocalGroupAndArtifactFile(id, session), filter2,
                deep ? Integer.MAX_VALUE : 1,
                session);
    }

    public Iterator<NutsId> searchImpl(NutsIdFilter filter, NutsSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        return findInFolder(null, filter, Integer.MAX_VALUE, session);
    }

    public Iterator<NutsId> findInFolder(Path folder, final NutsIdFilter filter, int maxDepth, NutsSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        if (folder != null) {
            folder = rootPath.resolve(folder);
        } else {
            folder = rootPath;
        }
        return new FolderNutIdIterator(repo == null ? null : repo.getName(), folder, rootPath, filter, session, new FolderNutIdIterator.AbstractFolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsSession session) {
                if (repo == null) {
                    NutsRepositoryFolderHelper.this.undeploy(new DefaultNutsRepositoryUndeployCommand(session.getWorkspace())
                            .setFetchMode(NutsFetchMode.LOCAL)
                            .setId(id).setSession(session));
                } else {
                    NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(repo);
                    repoSPI.undeploy().setId(id).setSession(session)
                            //.setFetchMode(NutsFetchMode.LOCAL)
                            .run();
                }
            }

            @Override
            public boolean isDescFile(Path pathname) {
                return pathname.getFileName().toString().endsWith(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION);
            }

            @Override
            public NutsDescriptor parseDescriptor(Path pathname, NutsSession session) throws IOException {
                if (cacheFolder && CoreIOUtils.isObsoletePath(session, pathname)) {
                    //this is invalid cache!
                    return null;
                } else {
                    return NutsDescriptorParser.of(session).parse(pathname);
                }
            }
        }, maxDepth);
    }

    public Path getStoreLocation() {
        return rootPath;
    }

    public NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        NutsId bestId = null;
        File file = getLocalGroupAndArtifactFile(id, session).toFile();
        if (file.exists()) {
            File[] versionFolders = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            if (versionFolders != null) {
                for (File versionFolder : versionFolders) {
                    if (pathExists(versionFolder.toPath(), session)) {
                        NutsId id2 = id.builder().setVersion(versionFolder.getName()).build();
                        if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                            bestId = id2;
                        }
                    } else {
                        CoreIOUtils.delete(session, versionFolder.toPath());
                    }
                }
            }
        }
        return bestId;
    }

    public NutsDescriptor deploy(NutsDeployRepositoryCommand deployment, NutsConfirmationMode writeType) {
        NutsSession session = deployment.getSession();
        if (!isWriteEnabled()) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("read-only repository"));
        }
        if (deployment.getContent() == null) {
            throw new NutsIllegalArgumentException(session,
                    NutsMessage.cstyle("invalid deployment; missing content for %s", deployment.getId()));
        }
        NutsDescriptor descriptor = deployment.getDescriptor();
        NutsStreamOrPath inputSource = NutsStreamOrPath.ofAnyInputOrNull(deployment.getContent(), session)
                .toMultiRead(session)
                .setKindType(("package content"));
        if (descriptor == null) {
            try (final CharacterizedExecFile c = DefaultNutsArtifactPathExecutable.characterizeForExec(inputSource, session, null)) {
                if (c.descriptor == null) {
                    throw new NutsNotFoundException(session, null,
                            NutsMessage.cstyle("unable to resolve a valid descriptor for %s", deployment.getContent()), null);
                }
                descriptor = c.descriptor;
            }
        }
        NutsId id = deployment.getId();
        if (id == null) {
            id = descriptor.getId();
        }

        NutsWorkspaceUtils.of(session).checkNutsId(id);

        if (isDeployed(id, descriptor, session)) {
            NutsId finalId = id;
            if (!DefaultWriteTypeProcessor
                    .of(writeType, session)
                    .ask("override deployment for %s?", id)
                    .withLog(_LOG(session), "nuts deployment overridden {0}", id)
                    .onError(() -> new NutsAlreadyDeployedException(session, finalId))
                    .process()) {
                return descriptor;
            }
        }
        switch (writeType) {
            case ERROR:
            case ASK: {
                writeType = NutsConfirmationMode.NO;
                break;
            }
        }

        deployDescriptor(id, descriptor, writeType, session);
        Path pckFile = deployContent(id, inputSource, descriptor, writeType, session);
        if (repo != null) {
            NutsRepositoryUtils.of(repo).events().fireOnDeploy(new DefaultNutsContentEvent(
                    NutsPath.of(pckFile,session), deployment, session, repo));
        }
        return descriptor.builder().setId(id.getLongId()).build();
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsFetchContentRepositoryCommand.class,session);
        }
        return LOG;
    }

    public Path deployDescriptor(NutsId id, NutsDescriptor desc, NutsConfirmationMode writeType, NutsSession session) {
        if (!isWriteEnabled()) {
            throw new NutsIllegalArgumentException(session,NutsMessage.cstyle("read only repository"));
        }
        NutsWorkspaceUtils.of(session).checkNutsId(id);
        Path descFile = getLongIdLocalFile(id.builder().setFaceDescriptor().build(), session);
        if (Files.exists(descFile)) {
            if (!DefaultWriteTypeProcessor
                    .of(writeType, session)
                    .ask("override descriptor file for %s?", id)
                    .withLog(_LOG(session), "nuts descriptor file overridden {0}", id)
                    .onError(() -> new NutsAlreadyDeployedException(session, id))
                    .process()) {
                return descFile;
            }
        }
        return NutsLocks.of(session).setSource(descFile).call(() -> {

            desc.formatter().setSession(session).setNtf(false).print(descFile);
            byte[] bytes = NutsHash.of(session).sha1().setSource(desc).computeString().getBytes();
            NutsCp.of(session)
                    .from(
                            InputStreamMetadataAwareImpl.of(
                                    new ByteArrayInputStream(bytes)
                                    , new NutsDefaultInputStreamMetadata(
                                            NutsMessage.cstyle("sha1://%s", desc.getId()),
                                            bytes.length,
                                            CoreIOUtils.MIME_TYPE_SHA1,
                                            "descriptor hash",
                                            session
                                    )
                            )
                    ).to(descFile.resolveSibling(descFile.getFileName() + ".sha1")).setSafe(true).run();
            return descFile;
        });
    }

    public boolean isDeployed(NutsId id, NutsDescriptor descriptor, NutsSession session) {
        Path pckFile = getLongIdLocalFile(id.builder().setFaceContent().setPackaging(descriptor.getPackaging()).build(), session);
        if (!Files.exists(pckFile) || (cacheFolder && CoreIOUtils.isObsoletePath(session, pckFile))) {
            return false;
        }
        Path descFile = getLongIdLocalFile(id.builder().setFaceDescriptor().build(), session);
        return Files.exists(descFile) && (!cacheFolder || !CoreIOUtils.isObsoletePath(session, descFile));
    }

    public Path deployContent(NutsId id, NutsStreamOrPath content, NutsDescriptor descriptor, NutsConfirmationMode writeType, NutsSession session) {
        if (!isWriteEnabled()) {
            return null;
        }
        NutsWorkspaceUtils.of(session).checkNutsId(id);
        Path pckFile = getLongIdLocalFile(id.builder().setFaceContent().setPackaging(descriptor.getPackaging()).build(), session);
        if (Files.exists(pckFile)) {
            if (!DefaultWriteTypeProcessor
                    .of(writeType, session)
                    .ask("override content file for %s?", id)
                    .withLog(_LOG(session), "nuts content file overridden {0}", id)
                    .onError(() -> new NutsAlreadyDeployedException(session, id))
                    .process()) {
                return pckFile;
            }
        }
        return NutsLocks.of(session).setSource(pckFile).call(() -> {
            (content.isPath() ? NutsCp.of(session).from(content.getPath()) : NutsCp.of(session).from(content.getInputStream()))
                    .to(pckFile).setSafe(true).run();
            NutsCp.of(session).from(
                    CoreIOUtils.createBytesStream(CoreIOUtils.evalSHA1Hex(pckFile).getBytes(),
                            NutsMessage.cstyle("sha1://%s", id),
                            CoreIOUtils.MIME_TYPE_SHA1,
                            null,
                            session
                    )
            ).to(pckFile.resolveSibling(pckFile.getFileName() + ".sha1")).setSafe(true).run();
            return pckFile;
        });
    }

    public boolean undeploy(NutsRepositoryUndeployCommand command) {
        if (!isWriteEnabled()) {
            return false;
        }
        Path localFolder = getLongIdLocalFile(command.getId().builder().setFaceContent().build(), command.getSession());
        if (localFolder != null && Files.exists(localFolder)) {
            if (NutsLocks.of(command.getSession()).setSource(localFolder).call(() -> {
                CoreIOUtils.delete(command.getSession(), localFolder);
                return false;
            })) {
                if (repo != null) {
                    NutsRepositoryUtils.of(repo).events().fireOnUndeploy(new DefaultNutsContentEvent(
                            NutsPath.of(localFolder,command.getSession())
                            , command, command.getSession(), repo));
                    return true;
                }
            }
        }
        return true;
    }

    public void reindexFolder(NutsSession session) {
        reindexFolder(getStoreLocation(), session);
    }

    private boolean reindexFolder(Path path, NutsSession session) {
        if (!isWriteEnabled()) {
            return false;
        }
        try {
            Files.walkFileTree(path, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    File folder = dir.toFile();
                    File[] children = folder.listFiles();
                    TreeSet<String> files = new TreeSet<>();
                    TreeSet<String> folders = new TreeSet<>();
                    if (children != null) {
                        for (File child : children) {
                            //&& !DefaultNutsVersion.isBlank(child.getName())
                            if (!child.getName().startsWith(".")) {
                                if (child.isDirectory()) {
                                    folders.add(child.getName());
                                } else if (child.isFile()) {
                                    files.add(child.getName());
                                }
                            }
                        }
                    }
                    try (PrintStream p = new PrintStream(new File(folder, CoreNutsConstants.Files.DOT_FILES))) {
                        p.println("#version=" + ws.getWorkspace().getApiVersion());
                        for (String file : folders) {
                            p.println(file + "/");
                        }
                        for (String file : files) {
                            p.println(file);
                        }
                    } catch (FileNotFoundException e) {
                        throw new NutsIOException(session, e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return true;
    }

    private boolean pathExists(Path p, NutsSession session) {
        return Files.exists(p) &&
                !(cacheFolder && CoreIOUtils.isObsoletePath(session, p));
    }
}
