/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NutsBootWorkspace;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class Test06_UpdateTest {

    private static String baseFolder;

    @BeforeAll
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getPath();
        CoreIOUtils.delete(null, new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ " + TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
        //CoreIOUtils.delete(null,new File(baseFolder));
    }

    @Test
    public void testUpdateApi() throws Exception {
        testUpdate(false, TestUtils.getCallerMethodName());
    }

    @Test
    public void testUpdateImpl() throws Exception {
        testUpdate(true, TestUtils.getCallerMethodName());
    }

    private void testUpdate(boolean implOnly, String callerName) throws Exception {
        CoreIOUtils.delete(null, new File(baseFolder));
        final String workspacePath = baseFolder + "/" + callerName;

        NutsSession uws = TestUtils.openNewTestWorkspace(
                "--workspace", workspacePath + "-update",
                "--archetype", "minimal",
                "--standalone",
                "--standalone-repositories",
                "--yes",
                "--progress=newline",
                "--skip-companions",
                "--verbose"
        );
        NutsRepository updateRepo1 = uws.repos().addRepository("local");
        uws.config().save();
        //NutsRepository updateRepo1 = uws.config().getRepository("local", session);
        String updateRepoPath = updateRepo1.config().getStoreLocation().toString();
        TestUtils.println(updateRepo1.config().getStoreLocationStrategy());
        uws.info().println();
        TestUtils.println("\n------------------------------------------");
        NutsSession nws = TestUtils.openNewTestWorkspace(
                "--workspace", workspacePath,
                "--standalone",
                "--standalone-repositories",
                "--progress=newline",
                "--yes",
                "--skip-companions"
        );
        nws.repos().addRepository(new NutsAddRepositoryOptions().setTemporary(true).setName("temp").setLocation(updateRepoPath)
                .setConfig(new NutsRepositoryConfig().setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE))
        );
        nws.info().setShowRepositories(true).println();
        TestUtils.println("\n------------------------------------------");

        NutsRepository r = nws.repos().getRepository("temp");
        NutsDefinition api = nws.fetch().setContent(true).setNutsApi().getResultDefinition();
        NutsDefinition rt = nws.fetch().setContent(true).setNutsRuntime().getResultDefinition();

        NutsVersion apiv1 = api.getId().getVersion();
        NutsVersion apiv2 = implOnly ? apiv1 : apiv1.inc(-1, 10);
        FromTo fromToAPI = new FromTo(apiv1.toString(), apiv2.toString());

        NutsVersion rtv2 = rt.getId().getVersion().inc(-2, 10);
        FromTo fromToImpl = new FromTo(rt.getId().getVersion().toString(), rtv2.toString());

        if (!fromToAPI.isIdentity()) {
            uws.deploy()
                    .setContent(replaceAPIJar(api.getFile(), fromToAPI, uws))
                    .setDescriptor(api.getDescriptor().builder().setId(api.getId().builder().setVersion(apiv2).build()).build())
                    //                        .setRepository("local")
                    .run();
        }
        uws.deploy()
                .setContent(replaceRuntimeJar(rt.getFile(), fromToAPI, fromToImpl, uws))
                .setDescriptor(
                        rt.getDescriptor()
                                .builder()
                                .setId(rt.getId().builder().setVersion(rtv2).build())
                                .replaceDependency(
                                        x -> x.getSimpleName().equals(api.getId().getShortName()),
                                        x -> x.builder().setVersion(apiv2).build()
                                )
                                .build()
                )
                .run();

        TestUtils.println("[LOCAL]");

        TestUtils.println(uws.repos().getRepository("local").config().getStoreLocationStrategy());
        TestUtils.println(uws.repos().getRepository("local").config().getStoreLocation());
        TestUtils.println(uws.repos().getRepository("local").config().getStoreLocation(NutsStoreLocation.LIB));

        TestUtils.println("[TEMP]");
        TestUtils.println(nws.repos().getRepository("temp").config().getStoreLocationStrategy());
        TestUtils.println(nws.repos().getRepository("temp").config().getStoreLocation());
        TestUtils.println(nws.repos().getRepository("temp").config().getStoreLocation(NutsStoreLocation.LIB));
        TestUtils.println(uws.repos().getRepository("local").config().getStoreLocation(NutsStoreLocation.LIB));
        Assertions.assertEquals(
                uws.repos().getRepository("local").config().getStoreLocation(NutsStoreLocation.LIB),
                nws.repos().getRepository("temp").config().getStoreLocation(NutsStoreLocation.LIB));

        TestUtils.println(uws.search().addId(api.getId().getShortId()).getResultIds().toList());
        TestUtils.println(uws.search().addId(rt.getId().getShortId()).getResultIds().toList());
        List<NutsId> foundApis = uws.search().addId(api.getId().getShortId()).getResultIds().toList();
        List<NutsId> foundRts = uws.search().addId(rt.getId().getShortId()).getResultIds().toList();
        Assertions.assertTrue(foundApis.stream().map(NutsId::getLongName).collect(Collectors.toSet()).contains(api.getId().builder().setVersion(apiv1).getLongName()));
        if (!implOnly) {
            Assertions.assertTrue(foundApis.stream().map(NutsId::getLongName).collect(Collectors.toSet()).contains(api.getId().builder().setVersion(apiv2).getLongName()));
        }
        Assertions.assertTrue(foundRts.stream().map(NutsId::getLongName).collect(Collectors.toSet()).contains(rt.getId().builder().setVersion(rt.getId().getVersion()).getLongName()));
        Assertions.assertTrue(foundRts.stream().map(NutsId::getLongName).collect(Collectors.toSet()).contains(rt.getId().builder().setVersion(rtv2).getLongName()));

        TestUtils.println("========================");
        TestUtils.println(nws.search().addId(api.getId().getShortId()).setRepository("temp").getResultIds().toList());
        TestUtils.println(nws.search().addId(rt.getId().getShortId()).setRepository("temp").getResultIds().toList());
        TestUtils.println(nws.search().addId(api.getId().getShortId()).getResultIds().toList());
        TestUtils.println(nws.search().addId(rt.getId().getShortId()).getResultIds().toList());

        //check updates!
        NutsUpdateCommand foundUpdates = nws.update().setAll().checkUpdates();
        for (NutsUpdateResult u : foundUpdates.getResult().getUpdatable()) {
            TestUtils.println(u.getAvailable());
        }
        Assertions.assertEquals(implOnly ? 1 : 2, foundUpdates.getResultCount(), "checkUpdates result count is incorrect");
        foundUpdates.update();

        final String newApiVersion = foundUpdates.getResult().getApi().getAvailable().getId().getVersion().toString();
        final String newRuntimeVersion = foundUpdates.getResult().getRuntime().getAvailable().getId().getVersion().toString();
//        Path bootFolder=Paths.get(workspacePath).resolve(NutsConstants.Folders.BOOT);
//        Path bootCompFolder=Paths.get(workspacePath).resolve(NutsConstants.Folders.BOOT);
        Path bootCacheFolder = (nws.locations().getStoreLocation(NutsStoreLocation.CACHE)).resolve(NutsConstants.Folders.ID).toFile();
        Path libFolder = (nws.locations().getStoreLocation(NutsStoreLocation.LIB)).resolve(NutsConstants.Folders.ID).toFile();
        Path configFolder = (nws.locations().getStoreLocation(NutsStoreLocation.CONFIG)).resolve(NutsConstants.Folders.ID).toFile();
        Assertions.assertTrue(Files.exists(libFolder.resolve("net/thevpc/nuts/nuts/").resolve(newApiVersion)
                .resolve("nuts-" + newApiVersion + ".jar")
        ));
        Assertions.assertTrue(Files.exists(configFolder.resolve("net/thevpc/nuts/nuts/").resolve(newApiVersion)
                .resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME)
        ));

        Assertions.assertTrue(Files.exists(bootCacheFolder.resolve("net/thevpc/nuts/nuts-runtime/").resolve(newRuntimeVersion)
                .resolve(NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME)
        ));
//        try {
//            NutsWorkspace updatedws = TestUtils.openNewTestWorkspace(new String[]{
//                "--workspace", workspacePath});
//            Assertions.assertFalse(true);
//        } catch (NutsUnsatisfiedRequirementsException e) {
//            Assertions.assertTrue(true);
//        }

        NutsBootWorkspace b = new NutsBootWorkspace(null,
                "--workspace", workspacePath,
                "--boot-version=" + newApiVersion,
                "--bot",
                "--color=never",
                "--skip-companions",
                "--json",
                "version"
        );
        TestUtils.println(NutsCommandLine.of(b.createProcessCommandLine(), uws).toString());

        String ss = uws.exec().setExecutionType(NutsExecutionType.SYSTEM).addCommand(b.createProcessCommandLine()).grabOutputString().run().getOutputString();
        TestUtils.println("================");
        TestUtils.println(ss);
        Map m = NutsElements.of(uws).json().parse(ss, Map.class);
        Assertions.assertEquals(newApiVersion, m.get("nuts-api-version"));
        Assertions.assertEquals(newRuntimeVersion, m.get("nuts-runtime-version"));
    }

    private Path replaceAPIJar(Path p, FromTo api, NutsSession session) {
        try {
            Path zipFilePath = NutsTmp.of(session)
                    .createTempFile(".zip").toFile();
            Files.copy(p, zipFilePath, StandardCopyOption.REPLACE_EXISTING);
            try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {

                Path fileInsideZipPath = fs.getPath("/META-INF/maven/net.thevpc.nuts/nuts/pom.properties");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath)).replace(api.from, api.to);
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                fileInsideZipPath = fs.getPath("/META-INF/maven/net.thevpc.nuts/nuts/pom.xml");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath)).replace(">" + api.from + "<", ">" + api.to + "<");
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                fileInsideZipPath = fs.getPath("/META-INF/nuts/net.thevpc.nuts/nuts/nuts.properties");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath)).replace(api.from, api.to);
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
            Path jar = zipFilePath.resolveSibling("nuts-" + api.to + ".jar");
            Files.move(zipFilePath, jar, StandardCopyOption.REPLACE_EXISTING);
            return jar;
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    private Path replaceRuntimeJar(Path p, FromTo api, FromTo impl, NutsSession session) {
        try {
            Path zipFilePath = NutsTmp.of(session)
                    .createTempFile(".zip").toFile();
            Files.copy(p, zipFilePath, StandardCopyOption.REPLACE_EXISTING);
            try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {

                Path fileInsideZipPath = fs.getPath("/META-INF/maven/net.thevpc.nuts/nuts-runtime/pom.properties");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath))
                            .replace("project.version=" + impl.from, "project.version=" + impl.to);
                    if (!api.isIdentity()) {
                        ss = ss.replace("net.thevpc.nuts:nuts:" + api.from, "net.thevpc.nuts:nuts:" + api.to);
                    }
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                fileInsideZipPath = fs.getPath("/META-INF/maven/net.thevpc.nuts/nuts-runtime/pom.xml");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath))
                            .replace(">" + impl.from + "<", ">" + impl.to + "<");
                    if (!api.isIdentity()) {
                        ss = ss.replace(">" + api.from + "<", ">" + api.to + "<");
                    }
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                if (!api.isIdentity()) {
                    fileInsideZipPath = fs.getPath("/META-INF/nuts/net.thevpc.nuts/nuts-runtime/nuts.properties");
                    if (Files.exists(fileInsideZipPath)) {
                        String ss = new String(Files.readAllBytes(fileInsideZipPath)).replace(api.from, api.to);
//                    Files.write(fileInsideZipPath, ss.getBytes());
                        Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }

            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
            Path jar = zipFilePath.resolveSibling("nuts-" + api.to + ".jar");
            Files.move(zipFilePath, jar, StandardCopyOption.REPLACE_EXISTING);
            return jar;
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    @BeforeEach
    public void startup() throws IOException {
//        Assumptions.assumeTrue(NutsOsFamily.getCurrent() == NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

    public static class FromTo {

        String from;
        String to;

        public FromTo(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public boolean isIdentity() {
            return from.equals(to);
        }
    }

}
