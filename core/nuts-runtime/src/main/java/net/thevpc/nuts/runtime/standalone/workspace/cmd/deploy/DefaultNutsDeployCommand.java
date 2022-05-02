package net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.descriptor.parser.NutsDescriptorContentResolver;
import net.thevpc.nuts.runtime.standalone.id.util.NutsIdUtils;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.io.util.NutsStreamOrPath;
import net.thevpc.nuts.runtime.standalone.io.util.ZipOptions;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsConstants;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositorySPI;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * local implementation
 */
public class DefaultNutsDeployCommand extends AbstractNutsDeployCommand {

    public DefaultNutsDeployCommand(NutsWorkspace ws) {
        super(ws);
    }

    private static CharacterizedDeployFile characterizeForDeploy(NutsStreamOrPath contentFile, NutsFetchCommand options, List<String> parseOptions, NutsSession session) {
        if (parseOptions == null) {
            parseOptions = new ArrayList<>();
        }
        CharacterizedDeployFile c = new CharacterizedDeployFile(session);
        try {
            c.baseFile = CoreIOUtils.toPathInputSource(contentFile, c.temps, true,session);
            c.contentStreamOrPath = contentFile;
            if (!Files.exists(c.baseFile)) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("file does not exists %s", c.baseFile));
            }
            if (c.descriptor == null && c.contentStreamOrPath.isPath() && c.contentStreamOrPath.getPath().isURL()) {
                try {
                    c.descriptor = NutsDescriptorParser.of(session).parse(
                            c.contentStreamOrPath.getPath().resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME)
                    ).get(session);
                } catch (Exception ex) {
                    //ignore
                }
            }
            if (Files.isDirectory(c.baseFile)) {
                if (c.descriptor == null) {
                    Path ext = c.baseFile.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                    if (Files.exists(ext)) {
                        c.descriptor = NutsDescriptorParser.of(session).parse(ext).get(session);
                    } else {
                        c.descriptor = NutsDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.baseFile, parseOptions, session);
                    }
                }
                if (c.descriptor != null) {
                    if ("zip".equals(c.descriptor.getPackaging())) {
                        Path zipFilePath = Paths.get(NutsPath.of(c.baseFile.toString() + ".zip",session).toAbsolute().toString());
                        ZipUtils.zip(session, c.baseFile.toString(), new ZipOptions(), zipFilePath.toString());
                        c.contentStreamOrPath = NutsStreamOrPath.of(NutsPath.of(zipFilePath,session));
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid Nut Folder source. expected 'zip' ext in descriptor"));
                    }
                }
            } else if (Files.isRegularFile(c.baseFile)) {
                if (c.descriptor == null) {
                    NutsPath ext = NutsPath.of(c.baseFile.toString() + "." + NutsConstants.Files.DESCRIPTOR_FILE_NAME,session)
                            .toAbsolute();
                    if (ext.exists()) {
                        c.descriptor = NutsDescriptorParser.of(session).parse(ext).get(session);
                    } else {
                        c.descriptor = NutsDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.baseFile, parseOptions, session);
                    }
                }
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("path does not denote a valid file or folder %s", c.contentStreamOrPath));
            }
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
        return c;
    }

    @Override
    public NutsDeployCommand run() {
//        checkSession();
        checkSession();
//        NutsWorkspace ws = getSession().getWorkspace();
        if (getContent() != null || getDescriptor() != null || getSha1() != null || getDescSha1() != null) {
            runDeployFile();
        }
        if (ids.size() > 0) {
            for (NutsId nutsId : session.search().setSession(getSession())
                    .addIds(ids.toArray(new NutsId[0])).setLatest(true).setRepositoryFilter(fromRepository).getResultIds()) {
                NutsDefinition fetched = session.fetch().setContent(true).setId(nutsId).setSession(getSession()).getResultDefinition();
                if (fetched.getFile() != null) {
                    runDeployFile(NutsStreamOrPath.of(
                           NutsPath.of(fetched.getFile(),session)
                    ), fetched.getDescriptor(), null);
                }
            }
        }
        if (result == null || result.isEmpty()) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.formatted("missing package to deploy"));
        }
        if (getSession().isTrace()) {
            switch (getSession().getOutputFormat()){
                case PLAIN:{
                    for (Result nid : result) {
                            getSession().getTerminal().out().resetLine().printf("%s deployed successfully as %s to %s%n",
                                    nid.source,
                                    nid.id,
                                    NutsTexts.of(session).ofStyled(nid.repository, NutsTextStyle.primary3())
                            );
                    }
                    break;
                }
                default:{
                    getSession().out().printlnf(result);
                }
            }
        }
        return this;
    }

    private NutsDeployCommand runDeployFile() {
        return runDeployFile(getContent(), getDescriptor(), getDescSha1());
    }

    private NutsDeployCommand runDeployFile(NutsStreamOrPath content, Object descriptor0, String descSHA1) {
        checkSession();
        NutsSession session = getSession();
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(session.getWorkspace());
        NutsWorkspaceUtils wu = NutsWorkspaceUtils.of(this.session);
        wu.checkReadOnly();

        Path tempFile = null;
        NutsStreamOrPath contentSource;
        contentSource = content.toMultiRead(this.session);
        NutsDescriptor descriptor = buildDescriptor(descriptor0, descSHA1);

        CharacterizedDeployFile characterizedFile = null;
        Path contentFile2 = null;
        try {
            if (descriptor == null) {
                NutsFetchCommand p = this.session.fetch()
                        .setSession(session.copy().setTransitive(true));
                characterizedFile = characterizeForDeploy(contentSource, p, getParseOptions(), session);
                if (characterizedFile.descriptor == null) {
                    throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing descriptor"));
                }
                descriptor = characterizedFile.descriptor;
            }
            String name = this.session.locations().getDefaultIdFilename(descriptor.getId().builder().setFaceDescriptor().build());
            tempFile = NutsTmp.of(this.session)
                    .createTempFile(name).toFile();
            NutsCp.of(this.session).setSession(session).from(contentSource.getInputStream()).to(tempFile).addOptions(NutsPathOption.SAFE).run();
            contentFile2 = tempFile;

            Path contentFile0 = contentFile2;
            String repository = this.getTargetRepository();

            wu.checkReadOnly();
            Path contentFile = contentFile0;
            Path tempFile2 = null;
            try {
                if (Files.isDirectory(contentFile)) {
                    Path descFile = contentFile.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                    NutsDescriptor descriptor2;
                    if (Files.exists(descFile)) {
                        descriptor2 = NutsDescriptorParser.of(session).parse(descFile).get(session);
                    } else {
                        descriptor2 = NutsDescriptorContentResolver.resolveNutsDescriptorFromFileContent(
                                contentFile,
                                getParseOptions(), session);
                    }
                    if (descriptor == null) {
                        descriptor = descriptor2;
                    } else {
                        if (descriptor2 != null && !descriptor2.equals(descriptor)) {
                            descriptor.formatter(session).print(descFile);
                        }
                    }
                    if (descriptor != null) {
                        if ("zip".equals(descriptor.getPackaging())) {
                            Path zipFilePath = Paths.get(NutsPath.of(contentFile.toString() + ".zip", this.session)
                                    .toAbsolute().toString());
                            try {
                                ZipUtils.zip(session, contentFile.toString(), new ZipOptions(), zipFilePath.toString());
                            } catch (IOException ex) {
                                throw new NutsIOException(session,ex);
                            }
                            contentFile = zipFilePath;
                            tempFile2 = contentFile;
                        } else {
                            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid nuts folder source; expected 'zip' ext in descriptor"));
                        }
                    }
                } else {
                    if (descriptor == null) {
                        descriptor = NutsDescriptorContentResolver.resolveNutsDescriptorFromFileContent(
                                contentFile, getParseOptions(), session);
                    }
                }
                if (descriptor == null) {
                    throw new NutsNotFoundException(getSession(), null, NutsMessage.cstyle("artifact not found at %s", contentFile));
                }
                //remove workspace
                descriptor = descriptor.builder().setId(descriptor.getId().builder().setRepository(null).build()).build();
                if (NutsStringUtils.trim(descriptor.getId().getVersion().getValue()).endsWith(CoreNutsConstants.Versions.CHECKED_OUT_EXTENSION)) {
                    throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid version %s", descriptor.getId().getVersion()));
                }

                NutsId effId = dws.resolveEffectiveId(descriptor, session);
                CorePlatformUtils.checkAcceptCondition(descriptor.getCondition(),false, session);
                if (NutsBlankable.isBlank(repository)) {
                    effId = NutsIdUtils.createContentFaceId(effId.builder().setPropertiesQuery("").build(), descriptor,session);
                    for (NutsRepository repo : wu.filterRepositoriesDeploy(effId, null)
                            .stream()
                            .filter(x->x.config().getDeployWeight()>0)
                            .sorted(Comparator.comparingInt(x->x.config().getDeployWeight()))
                            .collect(Collectors.toList())) {
                        int deployOrder = repo.config().getDeployWeight();
                        NutsRepositorySPI repoSPI = wu.repoSPI(repo);
                        repoSPI.deploy()
                                .setSession(session)
                                //.setFetchMode(NutsFetchMode.LOCAL)
                                .setId(effId).setContent(contentFile).setDescriptor(descriptor)
                                .run();
                        addResult(effId,repo.getName(), NutsTexts.of(session).toText(content.getValue()));
                        return this;
                    }
                } else {
                    NutsRepository repo = getSession().repos().getRepository(repository);
                    if (repo == null) {
                        throw new NutsRepositoryNotFoundException(getSession(), repository);
                    }
                    if (!repo.config().isEnabled()) {
                        throw new NutsRepositoryDisabledException(getSession(), repository);
                    }
                    effId = NutsIdUtils.createContentFaceId(effId.builder().setPropertiesQuery("").build(), descriptor,session);
                    NutsRepositorySPI repoSPI = wu.repoSPI(repo);
                    repoSPI.deploy()
                            .setSession(session)
                            .setId(effId)
                            .setContent(contentFile)
                            .setDescriptor(descriptor)
                            .run();
                    addResult(effId,repo.getName(), NutsTexts.of(this.session).toText(content.getValue()));
                    return this;
                }
                throw new NutsRepositoryNotFoundException(getSession(), repository);
            } finally {
                if (tempFile2 != null) {
                    try {
                        Files.delete(tempFile2);
                    } catch (IOException ex) {
                        throw new NutsIOException(session,ex);
                    }
                }
            }
        } finally {
            if (characterizedFile != null) {
                characterizedFile.close();
            }
            if (tempFile != null) {
                CoreIOUtils.delete(getSession(), tempFile);
            }
        }

    }

    protected NutsDescriptor buildDescriptor(Object descriptor, String descSHA1) {
        if (descriptor == null) {
            return null;
        }
        checkSession();
        NutsSession session = getSession();
        NutsDescriptor mdescriptor = null;
        if (descriptor instanceof NutsDescriptor) {
            mdescriptor = (NutsDescriptor) descriptor;
            if (descSHA1 != null && !NutsDigest.of(session).sha1().setSource(mdescriptor).computeString().equalsIgnoreCase(descSHA1)) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content Hash"));
            }
            return mdescriptor;
        } else {
            InputStream inputStream = (InputStream) descriptor;
            NutsStreamOrPath nutsStreamOrPath = NutsStreamOrPath.ofAnyInputOrNull(descriptor, this.session);
            if(nutsStreamOrPath!=null) {
                NutsStreamOrPath d = nutsStreamOrPath.isInputStream()?
                        NutsStreamOrPath.of(inputStream,session).toDisposable(this.session)
                        :nutsStreamOrPath
                        ;
                try {
                    if (descSHA1 != null) {
                        try (InputStream is = d.getInputStream()) {
                            if (!NutsDigest.of(session).sha1().setSource(is).computeString().equalsIgnoreCase(descSHA1)) {
                                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content Hash"));
                            }
                        } catch (IOException ex) {
                            throw new NutsIOException(session,ex);
                        }
                    }
                    try (InputStream is = d.getInputStream()) {
                        return NutsDescriptorParser.of(session).parse(is).get(session);
                    } catch (IOException ex) {
                        throw new NutsIOException(session,ex);
                    }
                } finally {
                    d.dispose();
                }
            }else{
                throw new NutsException(getSession(), NutsMessage.cstyle("unexpected type %s", descriptor.getClass().getName()));
            }
        }
    }

    @Override
    public NutsDeployCommand addIds(String... values) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        if (values != null) {
            for (String s : values) {
                if (!NutsBlankable.isBlank(s)) {
                    ids.add(NutsId.of(s).get(session));
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

}
