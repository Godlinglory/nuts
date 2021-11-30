/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test07_DescriptorTest {
    static NutsSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void testSearchDescriptor() {
        NutsDefinition u = session.search().addId("org.springframework.boot:spring-boot-cli#2.4.1")
                .getResultDefinitions().required();
        TestUtils.println(u.getDescriptor());
        TestUtils.println(u.getId()+":"+(u.getDescriptor().isExecutable() ? "executable" : "non-executable"));
        TestUtils.println(u.getId()+":"+(u.getDescriptor().isApplication() ? "app" : "non-app"));
        Assertions.assertTrue(!u.getDescriptor().isExecutable());
        Assertions.assertTrue(!u.getDescriptor().isApplication());
    }

}
