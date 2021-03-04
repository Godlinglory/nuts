package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.bundles.common.CorePlatformUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.bundles.io.ZipOptions;
import net.thevpc.nuts.runtime.bundles.io.ZipUtils;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static net.thevpc.nuts.runtime.core.util.CoreIOUtils.createInputSource;

/**
 * local implementation
 */
public class DefaultNutsDeployCommand extends AbstractNutsDeployCommand {

    public DefaultNutsDeployCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsDeployCommand run() {
        if (getContent() != null || getDescriptor() != null || getSha1() != null || getDescSha1() != null) {
            runDeployFile();
        }
        if (ids.size() > 0) {
            for (NutsId nutsId : ws.search().setSession(
                    CoreNutsUtils.silent(getValidWorkspaceSession())
            ).addIds(ids.toArray(new NutsId[0])).setLatest(true).setRepository(fromRepository).getResultIds()) {
                NutsDefinition fetched = ws.fetch().setContent(true).setId(nutsId).setSession(getValidWorkspaceSession()).getResultDefinition();
                if (fetched.getPath() != null) {
                    runDeployFile(fetched.getPath(), fetched.getDescriptor(), null);
                }
            }
        }
        if (result == null || result.isEmpty()) {
            throw new NutsIllegalArgumentException(ws, "missing component to Deploy");
        }
        if (getValidWorkspaceSession().isTrace()) {
            getValidWorkspaceSession().formatObject(result).println();
        }
        return this;
    }

    private NutsDeployCommand runDeployFile() {
        return runDeployFile(getContent(), getDescriptor(), getDescSha1());
    }

    private NutsDeployCommand runDeployFile(Object content, Object descriptor0, String descSHA1) {
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsWorkspaceUtils.of(ws).checkReadOnly();

            Path tempFile = null;
        NutsInput contentSource;
            contentSource = ws.io().input().setMultiRead(true).setTypeName("artifact content").of(content);
            NutsDescriptor descriptor = buildDescriptor(descriptor0, descSHA1);

            CharacterizedDeployFile characterizedFile = null;
            Path contentFile2 = null;
            try {
                NutsSession validWorkspaceSession = getValidWorkspaceSession();
                if (descriptor == null) {
                    NutsFetchCommand p = ws.fetch()
                            .setSession(validWorkspaceSession.copy().setTransitive(true));
                    characterizedFile = characterizeForDeploy(ws, contentSource, p, getParseOptions(), validWorkspaceSession);
                    if (characterizedFile.descriptor == null) {
                        throw new NutsIllegalArgumentException(ws, "missing descriptor");
                    }
                    descriptor = characterizedFile.descriptor;
                }
                String name = ws.locations().getDefaultIdFilename(descriptor.getId().builder().setFaceDescriptor().build());
                tempFile = Paths.get(ws.io().tmp()
                        .setSession(session)
                        .createTempFile(name));
                ws.io().copy().setSession(validWorkspaceSession).from(contentSource.open()).to(tempFile).setSafe(true).run();
                contentFile2 = tempFile;

                Path contentFile0 = contentFile2;
                String repository = this.getTargetRepository();

                NutsWorkspaceUtils.of(ws).checkReadOnly();
                Path contentFile = contentFile0;
                Path tempFile2 = null;
                try {
                    if (Files.isDirectory(contentFile)) {
                        Path descFile = contentFile.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                        NutsDescriptor descriptor2;
                        if (Files.exists(descFile)) {
                            descriptor2 = ws.descriptor().parser().parse(descFile);
                        } else {
                            descriptor2 = CoreIOUtils.resolveNutsDescriptorFromFileContent(
                                    ws.io().input().setMultiRead(true).of(contentFile),
                                    getParseOptions(), validWorkspaceSession);
                        }
                        if (descriptor == null) {
                            descriptor = descriptor2;
                        } else {
                            if (descriptor2 != null && !descriptor2.equals(descriptor)) {
                                ws.descriptor().formatter(descriptor).print(descFile);
                            }
                        }
                        if (descriptor != null) {
                            if ("zip".equals(descriptor.getPackaging())) {
                                Path zipFilePath = Paths.get(ws.io().expandPath(contentFile.toString() + ".zip"));
                                try {
                                    ZipUtils.zip(ws,contentFile.toString(), new ZipOptions(), zipFilePath.toString());
                                } catch (IOException ex) {
                                    throw new UncheckedIOException(ex);
                                }
                                contentFile = zipFilePath;
                                tempFile2 = contentFile;
                            } else {
                                throw new NutsIllegalArgumentException(ws, "invalid nuts folder source; expected 'zip' ext in descriptor");
                            }
                        }
                    } else {
                        if (descriptor == null) {
                            descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(
                                    ws.io().input().setMultiRead(true).of(contentFile), getParseOptions(), validWorkspaceSession);
                        }
                    }
                    if (descriptor == null) {
                        throw new NutsNotFoundException(ws, " at " + contentFile);
                    }
                    //remove workspace
                    descriptor = descriptor.builder().setId(descriptor.getId().builder().setNamespace(null).build()).build();
                    if (CoreStringUtils.trim(descriptor.getId().getVersion().getValue()).endsWith(CoreNutsConstants.Versions.CHECKED_OUT_EXTENSION)) {
                        throw new NutsIllegalArgumentException(ws, "invalid Version " + descriptor.getId().getVersion());
                    }

                    NutsId effId = dws.resolveEffectiveId(descriptor, validWorkspaceSession);
                    for (String os : descriptor.getOs()) {
                        CorePlatformUtils.checkSupportedOs(ws.id().parser().setLenient(false).parse(os).getShortName());
                    }
                    for (String arch : descriptor.getArch()) {
                        CorePlatformUtils.checkSupportedArch(ws.id().parser().setLenient(false).parse(arch).getShortName());
                    }
                    if (CoreStringUtils.isBlank(repository)) {
                        NutsRepositoryFilter repositoryFilter = null;
                        //TODO CHECK ME, why offline
                        for (NutsRepository repo : NutsWorkspaceUtils.of(ws).filterRepositoriesDeploy(effId, repositoryFilter, validWorkspaceSession)) {

                            effId = ws.config().createContentFaceId(effId.builder().setProperties("").build(), descriptor)
//                                    .setAlternative(CoreStringUtils.trim(descriptor.getAlternative()))
                            ;
                            NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(ws).repoSPI(repo);
                            repoSPI.deploy()
                                    .setSession(validWorkspaceSession)
                                    //.setFetchMode(NutsFetchMode.LOCAL)
                                    .setId(effId).setContent(contentFile).setDescriptor(descriptor)
                                    .run();
                            addResult(effId);
                            return this;
                        }
                    } else {
                        NutsRepository repo = ws.repos().getRepository(repository, session);
                        if (repo == null) {
                            throw new NutsRepositoryNotFoundException(ws, repository);
                        }
                        if (!repo.config().isEnabled()) {
                            throw new NutsRepositoryNotFoundException(ws, "Repository " + repository + " is disabled.");
                        }
                        effId = ws.config().createContentFaceId(effId.builder().setProperties("").build(), descriptor)
//                                .setAlternative(CoreStringUtils.trim(descriptor.getAlternative()))
                        ;
                        NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(ws).repoSPI(repo);
                        repoSPI.deploy()
                                .setSession(validWorkspaceSession)
                                //.setFetchMode(NutsFetchMode.LOCAL)
                                .setId(effId)
                                .setContent(contentFile)
                                .setDescriptor(descriptor)
                                .run();
                        addResult(effId);
                        return this;
                    }
                    throw new NutsRepositoryNotFoundException(ws, repository);
                } finally {
                    if (tempFile2 != null) {
                        try {
                            Files.delete(tempFile2);
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    }
                }
            } finally {
                if (characterizedFile != null) {
                    characterizedFile.close();
                }
                if (tempFile != null) {
                    CoreIOUtils.delete(getValidWorkspaceSession(),tempFile);
                }
            }

    }

    protected NutsDescriptor buildDescriptor(Object descriptor, String descSHA1) {
        if (descriptor == null) {
            return null;
        }
        NutsDescriptor mdescriptor = null;
        if (descriptor instanceof NutsDescriptor) {
            mdescriptor = (NutsDescriptor) descriptor;
            if (descSHA1 != null && !ws.io().hash().sha1().source(mdescriptor).computeString().equalsIgnoreCase(descSHA1)) {
                throw new NutsIllegalArgumentException(ws, "invalid Content Hash");
            }
            return mdescriptor;
        } else if (CoreIOUtils.isValidInputStreamSource(descriptor.getClass())) {
            NutsInput inputStreamSource = ws.io().input().setMultiRead(true).setTypeName("artifact descriptor").of(descriptor);
            if (descSHA1 != null) {
                inputStreamSource = ws.io().input().setMultiRead(true).of(inputStreamSource);
                try (InputStream is = inputStreamSource.open()) {
                    if (!ws.io().hash().sha1().source(is).computeString().equalsIgnoreCase(descSHA1)) {
                        throw new NutsIllegalArgumentException(ws, "invalid Content Hash");
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            try (InputStream is = inputStreamSource.open()) {
                return ws.descriptor().parser().parse(is);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }

        } else {
            throw new NutsException(ws, "Unexpected type " + descriptor.getClass().getName());
        }
    }

    @Override
    public NutsDeployCommand addIds(String... values) {
        if (values != null) {
            for (String s : values) {
                if (!CoreStringUtils.isBlank(s)) {
                    ids.add(ws.id().parser().parse(s));
                }
            }
        }
        return this;
    }

    @Override
    public NutsDeployCommand addIds(NutsId... value) {
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
    public NutsDeployCommand clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NutsDeployCommand addId(NutsId id) {
        if (id != null) {
            addId(id.toString());
        }
        return this;
    }

    private static CharacterizedDeployFile characterizeForDeploy(NutsWorkspace ws, NutsInput contentFile, NutsFetchCommand options, String[] parseOptions, NutsSession session) {
        if(parseOptions==null){
            parseOptions=new String[0];
        }
        session = NutsWorkspaceUtils.of(ws).validateSession( session);
        CharacterizedDeployFile c = new CharacterizedDeployFile();
        try {
            c.baseFile = CoreIOUtils.toPathInputSource(contentFile, c.temps, session);
            c.contentFile = contentFile;
            Path fileSource = c.contentFile.getPath();
            if (!Files.exists(fileSource)) {
                throw new NutsIllegalArgumentException(ws, "file does not exists " + fileSource);
            }
            if (c.descriptor == null && c.baseFile.isURL()) {
                try {
                    c.descriptor = ws.descriptor().parser().parse(ws.io().input().of(c.baseFile.getURL().toString() + "." + NutsConstants.Files.DESCRIPTOR_FILE_NAME).open());
                } catch (Exception ex) {
                    //ignore
                }
            }
            if (Files.isDirectory(fileSource)) {
                if (c.descriptor == null) {
                    Path ext = fileSource.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                    if (Files.exists(ext)) {
                        c.descriptor = ws.descriptor().parser().parse(ext);
                    } else {
                        c.descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(c.contentFile, parseOptions, session);
                    }
                }
                if (c.descriptor != null) {
                    if ("zip".equals(c.descriptor.getPackaging())) {
                        Path zipFilePath = Paths.get(ws.io().expandPath(fileSource.toString() + ".zip"));
                        ZipUtils.zip(ws,fileSource.toString(), new ZipOptions(), zipFilePath.toString());
                        c.contentFile = ws.io().input().setMultiRead(true).of(zipFilePath);
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NutsIllegalArgumentException(ws, "invalid Nut Folder source. expected 'zip' ext in descriptor");
                    }
                }
            } else if (Files.isRegularFile(fileSource)) {
                if (c.descriptor == null) {
                    File ext = new File(ws.io().expandPath(fileSource.toString() + "." + NutsConstants.Files.DESCRIPTOR_FILE_NAME));
                    if (ext.exists()) {
                        c.descriptor = ws.descriptor().parser().parse(ext);
                    } else {
                        c.descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(c.contentFile, parseOptions, session);
                    }
                }
            } else {
                throw new NutsIllegalArgumentException(ws, "path does not denote a valid file or folder " + c.contentFile);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return c;
    }

    private static class CharacterizedDeployFile implements AutoCloseable {

        public NutsInput baseFile;
        public NutsInput contentFile;
        public List<Path> temps = new ArrayList<>();
        public NutsDescriptor descriptor;

        public Path getContentPath() {
            return (Path) contentFile.getSource();
        }

        public void addTemp(Path f) {
            temps.add(f);
        }

        @Override
        public void close() {
            for (Iterator<Path> it = temps.iterator(); it.hasNext();) {
                Path temp = it.next();
                try {
                    Files.delete(temp);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                it.remove();
            }
        }

    }

}
