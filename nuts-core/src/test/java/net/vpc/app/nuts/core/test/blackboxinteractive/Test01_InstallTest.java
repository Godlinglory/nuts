/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.blackboxinteractive;

import net.vpc.app.nuts.NutsOsFamily;
import net.vpc.app.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
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
public class Test01_InstallTest {

    private static String baseFolder;

    @Test
    public void nb() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        String wsPath = baseFolder + "/" + TestUtils.getCallerMethodName();
        NutsWorkspace ws = Nuts.openWorkspace("-y","-w="+wsPath,"--standalone","--embedded");
        TestUtils.println(ws.config().getWorkspaceLocation());
        TestUtils.println(ws.exec().embedded().addCommand("ls").which());

//        Nuts.runWorkspace(
////            "--workspace", wsPath,
////            "--standalone",
////            "--skip-companions",
//            "netbeans-launcher"
//        );
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null,new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
    }

    @Before
    public void startup() throws IOException {
        Assume.assumeTrue(Nuts.getPlatformOsFamily().equals(NutsOsFamily.LINUX));
        TestUtils.unsetNutsSystemProperties();
    }

    @After
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
