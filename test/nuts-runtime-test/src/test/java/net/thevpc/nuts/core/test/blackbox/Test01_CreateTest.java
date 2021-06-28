/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.blackbox;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test01_CreateTest {

    private static String baseFolder;

    @Test
    public void minimal1() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        String wsPath = baseFolder + "/" + TestUtils.getCallerMethodName();

        NutsWorkspace ws = Nuts.openWorkspace("--workspace", wsPath,
                "--standalone",
                "--archetype", "minimal",
                "--verbose",
                "--yes",
                "--skip-companions");
        NutsSession session = ws.createSession();
        ws=session.getWorkspace();
        Assertions.assertEquals(wsPath + "/cache", ws.locations().getStoreLocation(NutsStoreLocation.CACHE));
        Assertions.assertEquals(wsPath + "/cache/" + NutsConstants.Folders.REPOSITORIES + "/"+
                        ws.repos().getRepositories()[0].getName()+
                        "/"+ws.repos().getRepositories()[0].getUuid(),
                ws.repos().getRepositories()[0].config().getStoreLocation(NutsStoreLocation.CACHE).toString());

//        String str="     __        __    \n" +
//                "  /\\ \\ \\ _  __/ /______\n" +
//                " /  \\/ / / / / __/ ___/\n" +
//                "/ /\\  / /_/ / /_(__  )\n" +
//                "\\_\\ \\/\\__,_/\\__/____/\n";
//
//        String str="  /\\ _";
//        String str=" ```underlined prototype``` ";

//        System.out.println("---------------------------------");
//        System.out.println(str);
//        System.out.println("---------------------------------");
//        session.out().println(str);
//        NutsLogger _log = session.getWorkspace().log().of("example");
//        _log.with()
//                .level(Level.INFO)
//                .log(str);
        String str=
                "a\n\nb"
                ;
        System.out.println("-----------------------");
        System.out.println(str);
        NutsText txt = session.getWorkspace().text().parse(str);
        System.out.println("-----------------------");
        System.out.println(txt);
    }

    @Test
    public void minimal2() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = Nuts.openWorkspace("--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                "--standalone",
                "--archetype", "minimal",
                "--verbose",
                "--yes",
                "--skip-companions");
    }

    @Test
    public void minimal3() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = Nuts.openWorkspace("--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--yes",
                "--skip-companions");
    }

    @Test
    public void default1() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        String wsPath = baseFolder + "/" + TestUtils.getCallerMethodName();

        NutsWorkspace ws = Nuts.openWorkspace("--workspace", wsPath,
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--yes",
                "--skip-companions");
        NutsSession session = ws.createSession();
        ws=session.getWorkspace();
        Assertions.assertEquals(System.getProperty("user.home") + "/.cache/nuts/" + new File(wsPath).getName(),
                ws.locations().getStoreLocation(NutsStoreLocation.CACHE));
        Assertions.assertEquals(
                System.getProperty("user.home") + "/.cache/nuts/" + new File(wsPath).getName() + "/"
                + NutsConstants.Folders.REPOSITORIES + "/"
                + ws.repos().getRepositories()[0].getName()
                + "/" + ws.repos().getRepositories()[0].getUuid(),
                ws.repos().getRepositories()[0].config().getStoreLocation(NutsStoreLocation.CACHE).toString());
    }

    @Test
    public void default2() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = Nuts.openWorkspace("--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--yes",
                "--skip-companions");
    }

    @Test
    public void default3() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = Nuts.openWorkspace("--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--yes",
                "--skip-companions");
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null,new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
        CoreIOUtils.delete(null,new File(baseFolder));
    }

    @BeforeEach
    public void startup() throws IOException {
        Assumptions.assumeTrue(Nuts.getPlatformOsFamily()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
