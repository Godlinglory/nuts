/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.blackbox;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test05_FindLinuxTest {

    @Test()
    public void find2() throws Exception {
        //should throw NutsNotFoundException because
        //would not be able to install nsh and other companions
        NutsSession s = TestUtils.openNewTestWorkspace(
//                "--archetype", "minimal",
                "--skip-companions" //            "--verbose"
        );

        NutsStream<NutsId> result = s.search()
                .setSession(s.setFetchStrategy(NutsFetchStrategy.REMOTE))
                .setLatest(true).addId(NutsConstants.Ids.NUTS_API).getResultIds();
        //There is one result because nuts id is always installed
        Assertions.assertEquals(1, result.count());
    }

    @Test()
    public void find3() throws Exception {
        //should throw NutsNotFoundException because
        //would not be able to install nsh and other companions
        NutsSession s = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--skip-companions");

        int count = 0;
        NutsStream<NutsId> result = s.search()
                .setSession(s.setFetchStrategy(NutsFetchStrategy.REMOTE))
                .setLatest(true).addId(NutsConstants.Ids.NUTS_API).getResultIds();
        Assertions.assertTrue(result.count() > 0);
    }

    @Test()
    public void find4() throws Exception {
        //should throw NutsNotFoundException because
        //would not be able to install nsh and other companions
        NutsSession s = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--skip-companions");

        List<NutsId> result1 = s.search().setLatest(true).addId("nuts-runtime").getResultIds().toList();
        List<NutsId> result2 = s.search().setLatest(false).addId("nuts-runtime").getResultIds().toList();
        TestUtils.println(result1);
        TestUtils.println(result2);
        Assertions.assertTrue(result1.size() > 0);
    }

    @Test()
    public void find5() throws Exception {
        //should throw NutsNotFoundException because
        //would not be able to install nsh and other companions
        NutsSession s = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--skip-companions");

        List<NutsId> result1 = s.search().configure(false, "nuts-runtime").getResultIds().toList();
        List<NutsId> result2 = s.search().configure(false, "--latest", "nuts-runtime").getResultIds().toList();
        TestUtils.println("=====================");
        TestUtils.println(result1);
        TestUtils.println("=====================");
        TestUtils.println(result2);
        TestUtils.println("=====================");
        Assertions.assertTrue(result1.size() > 0);
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
    }

    @BeforeEach
    public void startup() throws IOException {
//        Assumptions.assumeTrue(NutsOsFamily.getCurrent()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
