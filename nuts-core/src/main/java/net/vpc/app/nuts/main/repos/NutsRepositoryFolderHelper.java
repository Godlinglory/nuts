/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.repos;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultWriteTypeProcessor;
import net.vpc.app.nuts.core.SuccessFailResult;
import net.vpc.app.nuts.core.WriteType;
import net.vpc.app.nuts.core.repos.NutsRepositoryExt0;
import net.vpc.app.nuts.main.commands.DefaultNutsArtifactPathExecutable;
import net.vpc.app.nuts.runtime.CoreNutsConstants;
import net.vpc.app.nuts.main.repocommands.DefaultNutsFetchContentRepositoryCommand;
import net.vpc.app.nuts.main.repocommands.DefaultNutsRepositoryUndeployCommand;
import net.vpc.app.nuts.runtime.io.NamedByteArrayInputStream;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.runtime.util.NutsRepositoryUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.io.FolderNutIdIterator;
import net.vpc.app.nuts.runtime.DefaultNutsContentEvent;
import net.vpc.app.nuts.runtime.NutsPatternIdFilter;
import net.vpc.app.nuts.runtime.filters.CoreFilterUtils;
import net.vpc.app.nuts.runtime.filters.id.NutsIdFilterAnd;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.io.InputSource;

/**
 * @author vpc
 */
public class NutsRepositoryFolderHelper {
    private final NutsLogger LOG;

    private NutsRepository repo;
    private NutsWorkspace ws;
    private Path rootPath;
    private boolean readEnabled = true;
    private boolean writeEnabled = true;

    public NutsRepositoryFolderHelper(NutsRepository repo, NutsWorkspace ws, Path rootPath) {
        this.repo = repo;
        this.ws = ws != null ? ws : repo == null ? null : repo.getWorkspace();
        if (ws == null && repo == null) {
            throw new NutsIllegalArgumentException(null, "Both ws and repo are null");
        }
        this.rootPath = rootPath;
        LOG = this.ws.log().of(DefaultNutsFetchContentRepositoryCommand.class);
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

    public Path getLongNameIdLocalFolder(NutsId id) {
        CoreNutsUtils.checkId_GNV(id);
        if (repo == null) {
            return getStoreLocation().resolve(getWorkspace().config().getDefaultIdBasedir(id));
        }
        return getStoreLocation().resolve(NutsRepositoryExt0.of(repo).getIdBasedir(id));
    }

    public Path getLongNameIdLocalFile(NutsId id) {
        if (repo == null) {
            return getLongNameIdLocalFolder(id).resolve(getWorkspace().config().getDefaultIdFilename(id));
        }
        return getLongNameIdLocalFolder(id).resolve(NutsRepositoryExt0.of(repo).getIdFilename(id));
    }

    public Path getShortNameIdLocalFolder(NutsId id) {
        CoreNutsUtils.checkId_GN(id);
        if (repo == null) {
            return getStoreLocation().resolve(getWorkspace().config().getDefaultIdBasedir(id.builder().setVersion("").build()));
        }
        return getStoreLocation().resolve(NutsRepositoryExt0.of(repo).getIdBasedir(id.builder().setVersion("").build()));
    }

    public NutsContent fetchContentImpl(NutsId id, Path localPath, NutsSession session) {
        Path cacheContent = getLongNameIdLocalFile(id);
        if (cacheContent != null && Files.exists(cacheContent)) {
            return new NutsDefaultContent(cacheContent, true, false);
        }
        return null;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    protected String getIdFilename(NutsId id) {
        if (repo == null) {
            return ws.config().getDefaultIdFilename(id);
        }
        return NutsRepositoryExt0.of(repo).getIdFilename(id);
    }

    public Path getGoodPath(NutsId id) {
        String idFilename = getIdFilename(id);
        Path versionFolder = getLongNameIdLocalFolder(id);
        return versionFolder.resolve(idFilename);
    }

    protected NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        String idFilename = getIdFilename(id.builder().setFaceDescriptor().build());
        Path goodFile = null;
        Path versionFolder = getLongNameIdLocalFolder(id);
        goodFile = versionFolder.resolve(idFilename);
        if (Files.exists(goodFile)) {
            return getWorkspace().descriptor().parse(goodFile);
        }
//        String alt = id.getAlternative();
//        String goodAlt = null;
//        if (CoreNutsUtils.isDefaultAlternative(alt)) {
//            goodFile = versionFolder.resolve(idFilename);
//            if (Files.exists(goodFile)) {
//                return getWorkspace().descriptor().parse(goodFile);
//            }
//        } else if (!CoreStringUtils.isBlank(alt)) {
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
        if (Files.exists(file)) {
            NutsDescriptor d = Files.isRegularFile(file) ? getWorkspace().descriptor().parse(file) : null;
            if (d != null) {
                Map<String, String> query = id.getProperties();
                String os = query.get("os");
                String arch = query.get("arch");
                String dist = query.get("dist");
                String platform = query.get("platform");
                if (CoreFilterUtils.matchesEnv(arch, os, dist, platform, d, session)) {
                    return d;
                }
            }
        }
        return null;
    }

    public Path getLocalGroupAndArtifactFile(NutsId id) {
        NutsWorkspaceUtils.of(getWorkspace()).checkSimpleNameNutsId(id);
        Path groupFolder = getStoreLocation().resolve(id.getGroupId().replace('.', File.separatorChar));
        return groupFolder.resolve(id.getArtifactId());
    }

    public Iterator<NutsId> searchVersions(NutsId id, final NutsIdFilter filter, boolean deep, NutsSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        if (id.getVersion().isSingleValue()) {
            NutsId id1 = id.builder().setFaceDescriptor().build();
            Path localFile = getLongNameIdLocalFile(id1);
            if (localFile != null && Files.isRegularFile(localFile)) {
                return Collections.singletonList(id.builder().setNamespace(repo == null ? null : repo.config().getName()).build()).iterator();
            }
            return null;
        }
        NutsIdFilter filter2 = new NutsIdFilterAnd(filter,
                new NutsPatternIdFilter(id.getShortNameId())
        );
        return findInFolder(getLocalGroupAndArtifactFile(id), filter2,
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
        return new FolderNutIdIterator(getWorkspace(), repo == null ? null : repo.config().getName(), folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsSession session) {
                if (repo == null) {
                    NutsRepositoryFolderHelper.this.undeploy(new DefaultNutsRepositoryUndeployCommand(ws)
                            .setFetchMode(NutsFetchMode.LOCAL)
                            .setId(id).setSession(session));
                } else {
                    repo.undeploy().setId(id).setSession(session)
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
                return getWorkspace().descriptor().parse(pathname);
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
        File file = getLocalGroupAndArtifactFile(id).toFile();
        if (file.exists()) {
            File[] versionFolders = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            if (versionFolders != null) {
                for (File versionFolder : versionFolders) {
                    NutsId id2 = id.builder().setVersion(versionFolder.getName()).build();
                    if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                        bestId = id2;
                    }
                }
            }
        }
        return bestId;
    }

    public NutsDescriptor deploy(NutsDeployRepositoryCommand deployment, WriteType writeType) {
        if (!isWriteEnabled()) {
            throw new IllegalArgumentException("Read only Repository");
        }
        if (deployment.getContent() == null) {
            throw new IllegalArgumentException("Invalid deployment. Missing content");
        }
        NutsDescriptor descriptor = deployment.getDescriptor();
        InputSource inputSource = CoreIOUtils.createInputSource(deployment.getContent()).multi();
        if (descriptor == null) {
            try (final DefaultNutsArtifactPathExecutable.CharacterizedExecFile c = DefaultNutsArtifactPathExecutable.characterizeForExec(inputSource,
                    deployment.getSession(), null)) {
                if (c.descriptor == null) {
                    throw new NutsNotFoundException(ws, "", "Unable to resolve a valid descriptor for " + deployment.getContent(), null);
                }
                descriptor = c.descriptor;
            }
        }
        NutsId id = deployment.getId();
        if (id == null) {
            id = descriptor.getId();
        }

        NutsWorkspaceUtils.of(getWorkspace()).checkNutsId(id);

        if (isDeployed(id, descriptor)) {
            NutsId finalId = id;
            if (!DefaultWriteTypeProcessor
                    .of(writeType, deployment.getSession())
                    .ask("Override deployment for %s?", id)
                    .withLog(LOG, "Nuts deployment Overridden {0}", id)
                    .onError(() -> new NutsAlreadyDeployedException(ws, finalId.toString()))
                    .process()) {
                return descriptor;
            }
        }
        switch (writeType) {
            case ERROR:
            case ASK: {
                writeType = WriteType.IGNORE;
                break;
            }
        }

        deployDescriptor(id, descriptor, writeType, deployment.getSession());
        Path pckFile = deployContent(id, inputSource, descriptor, writeType, deployment.getSession());
        if (repo != null) {
            NutsRepositoryUtils.of(repo).events().fireOnDeploy(new DefaultNutsContentEvent(pckFile, deployment, deployment.getSession(), repo));
        }
        return descriptor.builder().setId(id.getLongNameId()).build();
    }

    public Path deployDescriptor(NutsId id, NutsDescriptor desc, WriteType writeType, NutsSession session) {
        if (!isWriteEnabled()) {
            throw new IllegalArgumentException("Read only Repository");
        }
        NutsWorkspaceUtils.of(getWorkspace()).checkNutsId(id);
        Path descFile = getLongNameIdLocalFile(id.builder().setFaceDescriptor().build());
        if (Files.exists(descFile)) {
            if (!DefaultWriteTypeProcessor
                    .of(writeType, session)
                    .ask("Override descriptor file for %s?", id)
                    .withLog(LOG, "Nuts descriptor file Overridden {0}", id)
                    .onError(() -> new NutsAlreadyDeployedException(ws, id.toString()))
                    .process()) {
                return descFile;
            }
        }
        return ws.io().lock().source(descFile).call(() -> {

            getWorkspace().descriptor().value(desc).print(descFile);
            getWorkspace().io().copy().session(session).from(new NamedByteArrayInputStream(
                    getWorkspace().io().hash().sha1().source(desc).computeString().getBytes(),
                    "sha1(" + desc.getId() + ")"
            )).to(descFile.resolveSibling(descFile.getFileName() + ".sha1")).safe().run();
            return descFile;
        });
    }

    public boolean isDeployed(NutsId id, NutsDescriptor descriptor) {
        Path pckFile = getLongNameIdLocalFile(id.builder().setFaceContent().setPackaging(descriptor.getPackaging()).build());
        if (!Files.exists(pckFile)) {
            return false;
        }
        Path descFile = getLongNameIdLocalFile(id.builder().setFaceDescriptor().build());
        if (!Files.exists(descFile)) {
            return false;
        }
        return false;
    }

    public Path deployContent(NutsId id, Object content, NutsDescriptor descriptor, WriteType writeType, NutsSession session) {
        if (!isWriteEnabled()) {
            return null;
        }
        NutsWorkspaceUtils.of(getWorkspace()).checkNutsId(id);
        Path pckFile = getLongNameIdLocalFile(id.builder().setFaceContent().setPackaging(descriptor.getPackaging()).build());
        if (Files.exists(pckFile)) {
            if (!DefaultWriteTypeProcessor
                    .of(writeType, session)
                    .ask("Override content file for %s?", id)
                    .withLog(LOG, "Nuts content file Overridden {0}", id)
                    .onError(() -> new NutsAlreadyDeployedException(ws, id.toString()))
                    .process()) {
                return pckFile;
            }
        }
        return ws.io().lock().source(pckFile).call(() -> {
            getWorkspace().io().copy().session(session).from(content).to(pckFile).safe().run();
            getWorkspace().io().copy().session(session).from(new NamedByteArrayInputStream(
                            CoreIOUtils.evalSHA1Hex(pckFile).getBytes(),
                            "sha1(" + id + ")"
                    )
            ).to(pckFile.resolveSibling(pckFile.getFileName() + ".sha1")).safe().run();
            return pckFile;
        });
    }

    public boolean undeploy(NutsRepositoryUndeployCommand options) {
        if (!isWriteEnabled()) {
            return false;
        }
        Path localFolder = getLongNameIdLocalFile(options.getId());
        if (localFolder != null && Files.exists(localFolder)) {
            if(ws.io().lock().source(localFolder).call(() -> {
                try {
                    CoreIOUtils.delete(ws, localFolder);
                    return false;
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            })){
                if (repo != null) {
                    NutsRepositoryUtils.of(repo).events().fireOnUndeploy(new DefaultNutsContentEvent(localFolder, options, options.getSession(), repo));
                    return true;
                }
            }
        }
        return true;
    }

    public void reindexFolder() {
        reindexFolder(getStoreLocation());
    }

    private boolean reindexFolder(Path path) {
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
                        p.println("#version=" + ws.config().getApiVersion());
                        for (String file : folders) {
                            p.println(file + "/");
                        }
                        for (String file : files) {
                            p.println(file);
                        }
                    } catch (FileNotFoundException e) {
                        throw new UncheckedIOException(e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return true;
    }
}
