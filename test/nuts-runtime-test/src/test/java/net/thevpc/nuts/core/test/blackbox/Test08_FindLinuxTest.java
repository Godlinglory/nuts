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
import java.util.Map;

import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test08_FindLinuxTest {

    @Test
    public void find3() throws Exception {
        //should throw NutsNotFoundException because
        //would not be able to install nsh and other companions
        NutsSession session = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--skip-companions");

        NutsDefinition def = session.search().addId(
                "net.thevpc.common:thevpc-common-io#1.3.12"
//                "netbeans-launcher#1.1.0"
                )
                .setOptional(false).setInlineDependencies(true).setFailFast(true)
                .setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ONLINE))
                .setLatest(true).getResultDefinitions().required();
        TestUtils.println(def);
    }

    @Test
    public void find4() throws Exception {
        //should throw NutsNotFoundException because
        //would not be able to install nsh and other companions
        NutsSession s = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--skip-companions");

        NutsStream<NutsId> resultIds = s.search().setSession(s).addId("net.thevpc.scholar.doovos.kernel:doovos-kernel-core")
                .setLatest(true).setInlineDependencies(true).getResultIds();
        TestUtils.println(resultIds.toList());
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
