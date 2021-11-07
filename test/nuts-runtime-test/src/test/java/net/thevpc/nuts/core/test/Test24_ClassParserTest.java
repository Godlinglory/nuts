/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.bundles.io.SimpleClassStream;
import net.thevpc.nuts.runtime.core.util.CoreTimeUtils;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test24_ClassParserTest {

    private static String baseFolder;

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Test24_ClassParserTest.class.getName());
    private static long max = 0;

    @Test
    public void test1() throws Exception {
        NutsSession session = TestUtils.openNewTestWorkspace( );
        Path path = Paths.get(System.getProperty("user.home")).resolve(".m2/repository/org/ow2/asm/asm-commons/7.0/asm-commons-7.0.jar");
        if (Files.exists(path)) {
            parseAnyFile(path, session);
        }
    }

    private static void parseAnyFile(Path file, NutsSession session) throws IOException {
        if (Files.isDirectory(file)) {
            parseFolder(file, session);
        } else {
            parseRegularFile(file, session);
        }
    }

    private static void parseRegularFile(Path file, NutsSession session) throws IOException {
        long from = System.currentTimeMillis();
        if (file.getFileName().toString().endsWith(".class")) {
            parseClassFile(file.toAbsolutePath().normalize(), session);
        }
        if (file.getFileName().toString().endsWith(".jar")) {
            parseJarFile(file.toAbsolutePath().normalize(), session);
        }
        long to = System.currentTimeMillis();
        if (max < to - from) {
            max = to - from;
        }
        TestUtils.println("### TIME [" + file + "] " + CoreTimeUtils.formatPeriodMilli(to - from) + " -- " + max);
    }

    private static void parseFolder(Path file, NutsSession session) throws IOException {
        Files.walkFileTree(file, new FileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                parseRegularFile(file, session);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        }
        );
    }

    private static void parseJarFile(Path file, NutsSession session) throws IOException {
        TestUtils.println("parse jar " + file + " ... ");
        try (InputStream in = Files.newInputStream(file)) {
            TestUtils.println("parse jar " + file + " :: " + Arrays.asList(NutsWorkspaceUtils.of(session).parseJarExecutionEntries(in, file.toString())));
        }
    }

    private static void parseClassFile(Path file, NutsSession session) throws IOException {
        TestUtils.println(file);

        SimpleClassStream scs = new SimpleClassStream(Files.newInputStream(file), new SimpleClassStream.Visitor() {
            @Override
            public void visitVersion(int major, int minor) {
                TestUtils.println("\t" + major + "." + minor);
            }

            public void visitClassDeclaration(int accessFlags, String thisClass, String superClass, String[] interfaces) {
                TestUtils.println("\tclass " + accessFlags + " " + thisClass + " extends " + superClass + " implements " + Arrays.asList(interfaces));
            }

            @Override
            public void visitMethod(int accessFlags, String name, String descriptor) {
                TestUtils.println("\t\tmethod " + accessFlags + " " + name + " " + descriptor);
            }

            @Override
            public void visitField(int accessFlags, String name, String descriptor) {
                TestUtils.println("\t\tfield " + accessFlags + " " + name + " " + descriptor);
            }

        },session
        );
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null, new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ " + TestUtils.getCallerClassSimpleName());
    }
}