/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.bridges.maven;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsContent;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositorySession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.DefaultNutsContent;
import net.vpc.app.nuts.core.DefaultNutsVersion;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.FolderNutIdIterator;
import net.vpc.app.nuts.core.bridges.maven.mvnutil.MavenMetadata;
import net.vpc.app.nuts.core.bridges.maven.mvnutil.MavenMetadataParser;

/**
 *
 * @author vpc
 */
public class MavenRepositoryFolderHelper {

    private NutsRepository repo;
    private NutsWorkspace ws;
    private Path rootPath;

    public MavenRepositoryFolderHelper(NutsRepository repo, NutsWorkspace ws, Path rootPath) {
        this.repo = repo;
        this.ws = ws != null ? ws : repo == null ? null : repo.getWorkspace();
        this.rootPath = rootPath;
    }

    public Path getIdLocalFile(NutsId id) {
        if (CoreStringUtils.isBlank(id.getGroup())) {
            return null;
        }
        if (CoreStringUtils.isBlank(id.getName())) {
            return null;
        }
        if (id.getVersion().isBlank()) {
            return null;
        }
        String alt = CoreStringUtils.trim(id.getAlternative());
        String defaultIdFilename = NutsRepositoryExt.of(repo).getIdFilename(id);
        return (alt.isEmpty() || alt.equals(NutsConstants.QueryKeys.ALTERNATIVE_DEFAULT_VALUE)) ? getLocalVersionFolder(id).resolve(defaultIdFilename) : getLocalVersionFolder(id).resolve(alt).resolve(defaultIdFilename);
    }

    public Path getLocalVersionFolder(NutsId id) {
        return CoreIOUtils.resolveNutsDefaultPath(id, getStoreLocation());
    }

    public NutsContent fetchContentImpl(NutsId id, Path localPath, NutsRepositorySession session) {
        Path cacheContent = getIdLocalFile(id);
        if (cacheContent != null && Files.exists(cacheContent)) {
            return new DefaultNutsContent(cacheContent, true, false);
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
        return NutsRepositoryExt.of(repo).getIdFilename(id);
    }

    public Path getLocalGroupAndArtifactFile(NutsId id) {
        if (CoreStringUtils.isBlank(id.getGroup())) {
            return null;
        }
        if (CoreStringUtils.isBlank(id.getName())) {
            return null;
        }
        Path groupFolder = getStoreLocation().resolve(id.getGroup().replace('.', File.separatorChar));
        return groupFolder.resolve(id.getName());
    }

    public Iterator<NutsId> findVersions(NutsId id, final NutsIdFilter filter, boolean deep, NutsRepositorySession session) {
        if (id.getVersion().isSingleValue()) {
            NutsId id1 = id.setFaceDescriptor();
            Path localFile = getIdLocalFile(id1);
            if (localFile != null && Files.isRegularFile(localFile)) {
                return Collections.singletonList(id.setNamespace(repo == null ? null : repo.config().getName())).iterator();
            }
            return null;
        }
        return findInFolder(getLocalGroupAndArtifactFile(id), filter,
                deep ? Integer.MAX_VALUE : 1,
                session);
    }

    public Iterator<NutsId> findInFolder(Path folder, final NutsIdFilter filter, int maxDepth, NutsRepositorySession session) {
        folder = rootPath.resolve(folder);
        if (folder == null || !Files.exists(folder) || !Files.isDirectory(folder)) {
            //            return Collections.emptyIterator();
            return null;
        }
        return new FolderNutIdIterator(getWorkspace(), repo == null ? null : repo.config().getName(), folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsRepositorySession session) {
                throw new IllegalArgumentException("Unsupported");
            }

            @Override
            public boolean isDescFile(Path pathname) {
                return pathname.getFileName().toString().equals("pom.xml");
            }

            @Override
            public NutsDescriptor parseDescriptor(Path pathname, NutsRepositorySession session) throws IOException {
                return MavenUtils.parsePomXml(pathname, getWorkspace(), session);
            }
        }, maxDepth);
    }

    public Path getStoreLocation() {
        return rootPath;
    }

    public NutsId findLatestVersion(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
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
                    NutsId id2 = id.setVersion(versionFolder.getName());
                    if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                        bestId = id2;
                    }
                }
            }
        }
        return bestId;
    }

    public void reindexFolder() {
        reindexFolder(getStoreLocation(),true);
    }

    private void reindexFolder(Path path,boolean applyRawNavigation) {
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
                    String subPath = dir.toString().equals(path.toString()) ? "":
                            dir.toString().substring(path.toString().length() + 1).replace('\\', '/');
                    int iii = subPath.lastIndexOf('/');
                    String artifactId = null;
                    String groupId = null;
                    if (iii > 0) {
                        artifactId = subPath.substring(iii + 1);
                        groupId = subPath.substring(0, iii).replace('/', '.');
                    } else {
                        artifactId = subPath;
                        groupId = "";
                    }
                    if (children != null && children.length > 0) {
                        List<File> versions = new ArrayList<>();
                        for (File c : children) {
                            File[] pomFiles = c.listFiles(x -> x.getName().endsWith(".pom"));
                            if (pomFiles != null && pomFiles.length > 0) {
                                //this is package folder!
                                versions.add(c);
                            }
                        }
                        if (versions.size() > 0) {
                            Path metadataxml = dir.resolve("maven-metadata.xml");
                            MavenMetadata old = null;
                            try {
                                if (Files.exists(metadataxml)) {
                                    old = MavenMetadataParser.parseMavenMetaData(metadataxml);
                                }
                            } catch (Exception ex) {
                                //ignore any error!
                            }
                            MavenMetadata m = new MavenMetadata();
                            m.setArtifactId(artifactId);
//                            m.setArtifactId(artifactId);
                            m.setGroupId(groupId);
                            m.setLastUpdated(old == null ? null : old.getLastUpdated());
                            m.setRelease(old == null ? null : old.getRelease());
                            m.setLatest(old == null ? null : old.getLatest());
                            LinkedHashSet<String> sversions = new LinkedHashSet<>();
                            if (old != null) {
                                sversions.addAll(old.getVersions());
                            }
                            for (File version : versions) {
                                sversions.add(version.getName());
                            }
                            ArrayList<String> ll = new ArrayList<>(sversions);
                            ll.sort(new Comparator<String>() {
                                @Override
                                public int compare(String o1, String o2) {
                                    //reverse order
                                    return -DefaultNutsVersion.compareVersions(o1, o2);
                                }
                            });
                            m.setVersions(ll);
                            if(m.getLastUpdated()==null){
                                m.setLastUpdated(new Date());
                            }
//                            System.out.println(MavenMetadataParser.toXmlString(m));
                            MavenMetadataParser.writeMavenMetaData(m,metadataxml);
                            String md5=CoreIOUtils.evalMD5Hex(metadataxml).toLowerCase();
                            Files.write(metadataxml.resolveSibling("maven-metadata.xml.md5"),md5.getBytes());
                            String sha1=CoreIOUtils.evalSHA1Hex(metadataxml).toLowerCase();
                            Files.write(metadataxml.resolveSibling("maven-metadata.xml.sha1"),sha1.getBytes());
                        }
                        if (applyRawNavigation) {
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
                    }
                    if (applyRawNavigation) {
                        try (PrintStream p = new PrintStream(new File(folder, ".files"))) {
                            for (String file : files) {
                                p.println(file);
                            }
                        } catch (FileNotFoundException e) {
                            throw new UncheckedIOException(e);
                        }
                        try (PrintStream p = new PrintStream(new File(folder, ".folders"))) {
                            for (String file : folders) {
                                p.println(file);
                            }
                        } catch (FileNotFoundException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

    }
}
