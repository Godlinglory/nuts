/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.blackbox;

import net.vpc.app.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;
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
public class Test02_LoadTest {

    private static String baseFolder;

    @Test
    public void load1() throws Exception {

        NutsWorkspace w1 = Nuts.openWorkspace(new String[]{
            "--workspace", baseFolder+"/"+TestUtils.getCallerMethodName(),
            "--yes",
            "--skip-install-companions"
        });
        NutsWorkspace w2 = Nuts.openWorkspace(new String[]{
            "--workspace", baseFolder+"/"+TestUtils.getCallerMethodName(),
            "--yes",
            "--skip-install-companions"
        });
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/"+TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(new File(baseFolder));
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
    }

    @Before
    public void startup() throws IOException {
        Assume.assumeTrue(CorePlatformUtils.getPlatformOsFamily().equals("linux"));
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        TestUtils.unsetNutsSystemProperties();
    }

    @After
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}