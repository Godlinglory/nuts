/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.blackbox;

import net.vpc.app.nuts.NutsOsFamily;
import net.vpc.app.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import org.junit.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class Test04_CreateLoadLinuxTest {

    private Map<String, String> clearUpExtraSystemProperties;

    @Test
    public void customLayout() throws Exception {
        String test_id = "customLayout_use_export";
        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
//        for (NutsStoreFolder folder : NutsStoreFolder.values()) {
//            for (NutsStoreLocationLayout layout : NutsStoreLocationLayout.values()) {
//                extraProperties.put("nuts.export.home." + folder.id() + "." + layout.id(), new File(base, folder.name().toLowerCase() + "." + layout.name().toLowerCase()).getPath());
//            }
//        }
        TestUtils.setSystemProperties(extraProperties);
        clearUpExtraSystemProperties = extraProperties;

        CoreIOUtils.delete(base);
        Nuts.runWorkspace("--reset",
                "--system-apps-home", new File(base, "system.apps").getPath(),
                "--system-config-home", new File(base, "system.config").getPath(),
                "--system-var-home", new File(base, "system.var").getPath(),
                "--system-log-home", new File(base, "system.log").getPath(),
                "--system-temp-home", new File(base, "system.temp").getPath(),
                "--system-cache-home", new File(base, "system.cache").getPath(),
                "--system-lib-home", new File(base, "system.lib").getPath(),
                "--system-run-home", new File(base, "system.run").getPath(),
                //            "--verbose",
                "--skip-companions",
                "--yes",
                "info");

        NutsWorkspace w = Nuts.openWorkspace("--system-config-home", new File(base, "system.config").getPath(),
                "-y","info");
        TestUtils.println("==========================");
        w.info().println();
        TestUtils.println("==========================");
        TestUtils.println(new File(base, "system.apps").getPath());
        TestUtils.println(w.locations().getStoreLocation(NutsStoreLocation.APPS));
        Assert.assertEquals(
                new File(base, "system.apps/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.APPS).toString()
        );
        Assert.assertEquals(
                new File(base, "system.config/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.CONFIG).toString()
        );
        Assert.assertEquals(
                new File(base, "system.var/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.VAR).toString()
        );
        Assert.assertEquals(
                new File(base, "system.log/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.LOG).toString()
        );
        Assert.assertEquals(
                new File(base, "system.temp/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.TEMP).toString()
        );
        Assert.assertEquals(
                new File(base, "system.cache/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.CACHE).toString()
        );
        Assert.assertEquals(
                new File(base, "system.lib/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.LIB).toString()
        );
        Assert.assertEquals(
                new File(base, "system.run/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.RUN).toString()
        );

        w = Nuts.openWorkspace(//            "--workspace", "default-workspace",
//            "--workspace", new File(base, "system.config/default-workspace").getPath(),
                "info");
        TestUtils.println(w.locations().getStoreLocation(NutsStoreLocation.APPS));
        Assert.assertEquals(
                new File(base, "system.apps/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.APPS).toString()
        );
        Assert.assertEquals(
                new File(base, "system.config/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.CONFIG).toString()
        );
        Assert.assertEquals(
                new File(base, "system.var/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.VAR).toString()
        );
        Assert.assertEquals(
                new File(base, "system.log/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.LOG).toString()
        );
        Assert.assertEquals(
                new File(base, "system.temp/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.TEMP).toString()
        );
        Assert.assertEquals(
                new File(base, "system.cache/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.CACHE).toString()
        );
        Assert.assertEquals(
                new File(base, "system.lib/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.LIB).toString()
        );
        Assert.assertEquals(
                new File(base, "system.run/default-workspace").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.RUN).toString()
        );
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        TestUtils.stashLinuxFolders();
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
        TestUtils.unstashLinuxFolders();
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @Before
    public void startup() throws IOException {
        Assume.assumeTrue(Nuts.getPlatformOsFamily()== NutsOsFamily.LINUX);
        TestUtils.resetLinuxFolders();
        TestUtils.unsetNutsSystemProperties();
    }

    @After
    public void cleanup() {
        TestUtils.setSystemProperties(clearUpExtraSystemProperties);
        clearUpExtraSystemProperties = null;
    }

}
