/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test;

import net.vpc.app.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsCreateRepositoryOptions;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryConfig;
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.NutsStoreLocationStrategy;
import net.vpc.app.nuts.NutsWorkspaceUpdateResult;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CorePlatformUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class TestUpate {

    private static String baseFolder;
    private static String workpacePath;

    @Test
    public void customLayout() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace uws = Nuts.openWorkspace(new String[]{
            "--workspace", workpacePath + "-update",
            "--archetype", "minimal",
            "--standalone",
            "--yes",
            "--skip-install-companions"
        });
        NutsRepository updateRepo1 = uws.config().getRepository("local");
        String updateRepoPath = updateRepo1.config().getStoreLocation().toString();
        System.out.println(updateRepo1.config().getStoreLocationStrategy());
        uws.getTerminal().getOut().println("Hello");
        uws.formatter().createWorkspaceInfoFormat().format();
        System.out.println("\n------------------------------------------");
        NutsWorkspace nws = Nuts.openWorkspace(new String[]{
            "--workspace", workpacePath,
            "--standalone",
            "--yes",
            "--skip-install-companions"
        });
        nws.config().addRepository(new NutsCreateRepositoryOptions().setTemporay(true).setName("temp").setLocation(updateRepoPath)
                .setConfig(new NutsRepositoryConfig().setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE))
        );
        nws.formatter().createWorkspaceInfoFormat().format();
        System.out.println("\n------------------------------------------");

        NutsRepository r = nws.config().getRepository("temp");
        NutsDefinition api = nws.fetch().nutsApi().getResultDefinition();
        NutsDefinition rt = nws.fetch().nutsRuntime().getResultDefinition();

        NutsVersion apiv2 = api.getId().getVersion().inc(0, 10);
        NutsVersion rtv2 = rt.getId().getVersion().inc(0, 10);

        uws.deploy()
                .setContent(api.getContent().getPath())
                .setDescriptor(api.getDescriptor().setId(api.getId().setVersion(apiv2)))
                //                        .setRepository("local")
                .deploy();

        uws.deploy()
                .setContent(rt.getContent().getPath())
                .setDescriptor(
                        rt.getDescriptor().setId(rt.getId().setVersion(rtv2))
                                .replaceDependency(
                                        x -> x.getSimpleName().equals(api.getId().getSimpleName()),
                                        x -> x.setVersion(apiv2)
                                )
                )
                .deploy();

        System.out.println("[LOCAL]");
        System.out.println(uws.config().getRepository("local").config().getStoreLocationStrategy());
        System.out.println(uws.config().getRepository("local").config().getStoreLocation());
        System.out.println(uws.config().getRepository("local").config().getStoreLocation(NutsStoreLocation.LIB));

        System.out.println("[TEMP]");
        System.out.println(nws.config().getRepository("temp").config().getStoreLocationStrategy());
        System.out.println(nws.config().getRepository("temp").config().getStoreLocation());
        System.out.println(nws.config().getRepository("temp").config().getStoreLocation(NutsStoreLocation.LIB));
        Assert.assertEquals(
                uws.config().getRepository("local").config().getStoreLocation(NutsStoreLocation.LIB),
                nws.config().getRepository("temp").config().getStoreLocation(NutsStoreLocation.LIB));

        System.out.println(uws.find().addId(api.getId().getSimpleNameId()).getResultIds());
        System.out.println(uws.find().addId(rt.getId().getSimpleNameId()).getResultIds());
        Assert.assertEquals(1, uws.find().addId(api.getId().getSimpleNameId()).getResultIds().list().size());
        Assert.assertEquals(1, uws.find().addId(rt.getId().getSimpleNameId()).getResultIds().list().size());
        System.out.println("========================");
        System.out.println(nws.find().addId(api.getId().getSimpleNameId()).setRepositoryFilter("temp").getResultIds());
        System.out.println(nws.find().addId(rt.getId().getSimpleNameId()).setRepositoryFilter("temp").getResultIds());
        System.out.println(nws.find().addId(api.getId().getSimpleNameId()).getResultIds());
        System.out.println(nws.find().addId(rt.getId().getSimpleNameId()).getResultIds());
        NutsWorkspaceUpdateResult foundUpdates = nws.updateWorkspace().checkUpdates();
        Assert.assertEquals(2, foundUpdates == null ? 0 : foundUpdates.getUpdatesCount());
        nws.updateWorkspace().update();
        final String newApiVersion = foundUpdates.getApi().getAvailableId().getVersion().toString();
        final String newRuntimeVersion = foundUpdates.getRuntime().getAvailableId().getVersion().toString();
        Assert.assertEquals(true, Files.exists(Paths.get(workpacePath).resolve("cache/bootstrap/net/vpc/app/nuts/nuts/").resolve(newApiVersion)
                .resolve("nuts-" + newApiVersion + ".jar")
        ));
        Assert.assertEquals(true, Files.exists(Paths.get(workpacePath).resolve("cache/bootstrap/net/vpc/app/nuts/nuts/").resolve(newApiVersion)
                .resolve("nuts-" + newApiVersion + ".nuts")
        ));
        Assert.assertEquals(true, Files.exists(Paths.get(workpacePath).resolve("cache/bootstrap/net/vpc/app/nuts/nuts/").resolve(newApiVersion)
                .resolve("nuts.properties")
        ));

        Assert.assertEquals(true, Files.exists(Paths.get(workpacePath).resolve("/cache/bootstrap/net/vpc/app/nuts/nuts-core/").resolve(newRuntimeVersion)
                .resolve("nuts-core-" + newRuntimeVersion + ".jar")
        ));
        Assert.assertEquals(true, Files.exists(Paths.get(workpacePath).resolve("/cache/bootstrap/net/vpc/app/nuts/nuts-core/").resolve(newRuntimeVersion)
                .resolve("nuts-core-" + newRuntimeVersion + ".nuts")
        ));
        Assert.assertEquals(true, Files.exists(Paths.get(workpacePath).resolve("/cache/bootstrap/net/vpc/app/nuts/nuts-core/").resolve(newRuntimeVersion)
                .resolve("nuts.properties")
        ));
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        String test_id = TestUpate.class.getSimpleName();
        baseFolder = new File("./runtime/test/" + test_id).getCanonicalFile().getPath();
        workpacePath = baseFolder + "/default-workspace";
        CoreIOUtils.delete(new File(baseFolder));
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
    }

    @Before
    public void startup() throws IOException {
        Assume.assumeTrue(CorePlatformUtils.getPlatformOsFamily().equals("linux"));
        TestUtils.unsetNutsSystemProperties();
    }

    @After
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
