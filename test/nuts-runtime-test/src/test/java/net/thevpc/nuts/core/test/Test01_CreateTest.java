/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineFormat;
import net.thevpc.nuts.cmdline.NCmdLineHistory;
import net.thevpc.nuts.concurrent.NLocks;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.expr.NExpr;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.format.NTableFormat;
import net.thevpc.nuts.format.NTreeFormat;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NPaths;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * @author thevpc
 */
public class Test01_CreateTest {

    @Test
    public void minimal1()  {
        String wsPath = TestUtils.getTestBaseFolder().getPath();

        NSession session = TestUtils.openNewTestWorkspace("--workspace", wsPath,
                "--standalone",
                "--archetype", "minimal",
                "--verbose",
                "--install-companions=false",
                "--verbose"
        );
        Assertions.assertEquals(
                NPath.of(new File(wsPath, "cache"),session),
                NLocations.of(session).getStoreLocation(NStoreType.CACHE));
        Assertions.assertEquals(0, NRepositories.of(session).getRepositories().size());
//        Assertions.assertEquals(new File(wsPath,  "cache/" + NutsConstants.Folders.REPOSITORIES + "/" +
//                        NRepositories.of(session).getRepositories()[0].getName() +
//                        "/" + NRepositories.of(session).getRepositories()[0].getUuid()).getPath(),
//                NRepositories.of(session).getRepositories()[0].config().getStoreLocation(NutsStoreLocation.CACHE));

//        String str="     __        __    \n" +
//                "  /\\ \\ \\ _  __/ /______\n" +
//                " /  \\/ / / / / __/ ___/\n" +
//                "/ /\\  / /_/ / /_(__  )\n" +
//                "\\_\\ \\/\\__,_/\\__/____/\n";
//
//        String str="  /\\ _";
//        String str=" ```underlined prototype``` ";

//        TestUtils.println("---------------------------------");
//        TestUtils.println(str);
//        TestUtils.println("---------------------------------");
//        session.out().println(str);
//        NutsLogger _log = session.log().of("example");
//        _log.with()
//                .level(Level.INFO)
//                .log(str);
        String str =
                "a\n\nb";
        TestUtils.println("-----------------------");
        TestUtils.println(str);
        NText txt = NTexts.of(session).parse(str);
        TestUtils.println("-----------------------");
        TestUtils.println(txt);
    }

    @Test
    public void minimal2()  {
        TestUtils.openNewTestWorkspace(
                "--standalone",
                "--archetype", "minimal",
                "--verbose",
                "--install-companions=false");
    }

    @Test
    public void minimal3()  {
        TestUtils.openNewTestWorkspace(
                "--reset", // required for exploded repos
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--install-companions=false");
    }

    @Test
    public void default1() throws Exception {
        String wsPath = TestUtils.getTestBaseFolder().getPath();

        NSession session = TestUtils.openNewTestWorkspace(
                "--reset", // required for exploded repos
                "--exploded",
                "--archetype", "default",
                "--verbose",
                "--install-companions=false");
        String base = "";
        switch (NOsFamily.getCurrent()) {
            case WINDOWS: {
                base = new File(System.getProperty("user.home"), "AppData\\Local\\nuts\\cache\\ws").getPath();
                break;
            }
            case MACOS:
            case LINUX:
            case UNIX:
            case UNKNOWN: {
                base = new File(System.getProperty("user.home"), ".cache/nuts/ws").getPath();
                break;
            }
        }
        Assertions.assertEquals(
                NPath.of(new File(base, new File(wsPath).getName()),session),
                NLocations.of(session).getStoreLocation(NStoreType.CACHE));
        Assertions.assertEquals(
                NPath.of(new File(base, new File(wsPath).getName() + "/"
                        + NConstants.Folders.REPOSITORIES + "/"
                        + NRepositories.of(session).getRepositories().get(0).getName()
                        + "/" + NRepositories.of(session).getRepositories().get(0).getUuid()
                ),session),
                NRepositories.of(session).getRepositories().get(0).config().getStoreLocation(NStoreType.CACHE));
    }

    @Test
    public void default2() throws Exception {
        TestUtils.openNewTestWorkspace(
                "--reset", // required for exploded repos
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--install-companions=false");
    }

    @Test
    public void default3() throws Exception {
        TestUtils.openNewTestWorkspace(
                "--reset", // required for exploded repos
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--install-companions=false");
    }

    @Test
    public void createUtilTypes() {
        NSession s = TestUtils.runNewTestWorkspace("--verbose","-b");

        {
            NPath home = NPath.of(new File(System.getProperty("user.home")), s);
            Assertions.assertNotNull(home);
        }

        {
            NCmdLine cmd = NCmdLine.of(new String[]{"cmd", "--test"});
            Assertions.assertNotNull(cmd);
        }

        {
            NArg arg = NArg.of("arg");
            Assertions.assertNotNull(arg);
        }

        {
            NExpr expr = NExpr.of(s);
            Assertions.assertNotNull(expr);
        }

        {
            NStream<String> stream = NStream.of(new String[]{"a"}, s);
            Assertions.assertNotNull(stream);
        }

        {
            Pattern g = NGlob.of(s).toPattern("a.*");
            Assertions.assertNotNull(g);
        }

        {
            InputStream stdin = NIO.of(s).stdin();
            Assertions.assertNotNull(stdin);
        }

        {
            NPrintStream stdout = NIO.of(s).stdout();
            Assertions.assertNotNull(stdout);
        }

        {
            NCmdLineHistory h = NCmdLineHistory.of(s);
            Assertions.assertNotNull(h);
        }


        {
            NExecutionEntries c = NExecutionEntries.of(s);
            Assertions.assertNotNull(c);
        }

        {
            NDigest c = NDigest.of(s);
            Assertions.assertNotNull(c);
        }

        {
            NInputStreamMonitor c = NInputStreamMonitor.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NLocks c = NLocks.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NPaths c = NPaths.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NCp c = NCp.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NPs c = NPs.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NCompress c = NCompress.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NUncompress c = NUncompress.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NLog log = NLog.of(Test01_CreateTest.class, s);
            Assertions.assertNotNull(log);
            NLogOp logop = NLogOp.of(Test01_CreateTest.class, s);
            Assertions.assertNotNull(logop);
        }
        {
            NIdResolver r = NIdResolver.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NIdFormat r = NIdFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NIdFilters r = NIdFilters.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NVersionFilters r = NVersionFilters.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NDependencyFilters r = NDependencyFilters.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NDescriptorFilters r = NDescriptorFilters.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NInstallStatusFilters r = NInstallStatusFilters.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NRepositoryFilters r = NRepositoryFilters.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NElements r = NElements.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NDescriptorParser r = NDescriptorParser.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NArtifactCallBuilder r = NArtifactCallBuilder.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NDescriptorFormat r = NDescriptorFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NDependencySolver r = NDependencySolver.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NVersionFormat r = NVersionFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NFilters r = NFilters.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NTexts r = NTexts.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NObjectFormat r = NObjectFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NInfoCommand r = NInfoCommand.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NExecCommandFormat r = NExecCommandFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NCmdLineFormat r = NCmdLineFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NTableFormat r = NTableFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NTreeFormat r = NTreeFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NPropertiesFormat r = NPropertiesFormat.of(s);
            Assertions.assertNotNull(r);
        }

    }

    @Test
    public void testHomePath(){
        Assertions.assertEquals(
                NHomeLocation.of(null, NStoreType.BIN),
                NHomeLocation.parse("system-bin").orElse(null)
        );
        Assertions.assertEquals(
                NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE),
                NHomeLocation.parse("").orElse(NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE) )
        );
        Assertions.assertEquals(
                NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE),
                NHomeLocation.parse("").orElse( NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE))
        );
        Assertions.assertNull(NHomeLocation.parse("any error").orElse(null));
        Assertions.assertEquals(
                NHomeLocation.of(null, NStoreType.BIN),
                NEnum.parse(NHomeLocation.class, "system-bin").orElse(null)
        );
        Assertions.assertEquals(
                NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE),
                NEnum.parse(NHomeLocation.class,"").orElse(NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE))
        );
        Assertions.assertEquals(
                NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE),
                NEnum.parse(NHomeLocation.class,"").orElse(NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE) )
        );
        Assertions.assertNull(NEnum.parse(NHomeLocation.class,"any error")
                .orElse(null));
    }
}
