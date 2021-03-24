/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.core.test.whitebox;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsObjectFormat;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.*;
import net.thevpc.nuts.NutsElementFormat;

/**
 *
 * @author thevpc
 */
public class Test10_ElementPath {
    private static String baseFolder;

    @Test
    public void test1() {
        NutsWorkspace ws = Nuts.openWorkspace("-y","--workspace", baseFolder + "/" + TestUtils.getCallerMethodName());
        NutsElementFormat e = ws.formats().element();
        NutsElement p
                = e.forArray()
                        .add(
                                e.forObject().set("first",
                                        e.forObject()
                                                .set("name", e.forPrimitive().buildString("first name"))
                                                .set("valid", e.forPrimitive().buildFalse())
                                                .set("children",
                                                        e.forArray().add(
                                                                e.forObject()
                                                                        .set("path", e.forPrimitive().buildString("path1"))
                                                                        .set("color", e.forPrimitive().buildString("red"))
                                                        .build())
                                                                .add(
                                                                        e.forObject()
                                                                                .set("path", e.forPrimitive().buildString("path2"))
                                                                                .set("color", e.forPrimitive().buildString("green"))
                                                                        .build()
                                                                ).build()
                                                )
                                .build()
                                )
                                .build()
                        ).add(e.forObject().set("second",
                                e.forObject()
                                        .set("name", e.forPrimitive().buildString("second name"))
                                        .set("valid", e.forPrimitive().buildFalse())
                                        .set("children",
                                                e.forArray().add(
                                                        e.forObject()
                                                                .set("path", e.forPrimitive().buildString("path3"))
                                                                .set("color", e.forPrimitive().buildString("yellow"))
                                                        .build()
                                                )
                                                        .add(
                                                                e.forObject()
                                                                        .set("path", e.forPrimitive().buildString("path4"))
                                                                        .set("color", e.forPrimitive().buildString("magenta"))
                                                                .build()
                                                        ).build()
                                        )
                        .build()
                        ).build())
                .build();
        NutsObjectFormat ss = ws.createSession().json().formatObject(p);
        ss.println();
        String json = ss.format();
        Assertions.assertEquals("[\n"
                + "  {\n"
                + "    \"first\": {\n"
                + "      \"name\": \"first name\",\n"
                + "      \"valid\": true,\n"
                + "      \"children\": [\n"
                + "        {\n"
                + "          \"path\": \"path1\",\n"
                + "          \"color\": \"red\"\n"
                + "        },\n"
                + "        {\n"
                + "          \"path\": \"path2\",\n"
                + "          \"color\": \"green\"\n"
                + "        }\n"
                + "      ]\n"
                + "    }\n"
                + "  },\n"
                + "  {\n"
                + "    \"second\": {\n"
                + "      \"name\": \"second name\",\n"
                + "      \"valid\": true,\n"
                + "      \"children\": [\n"
                + "        {\n"
                + "          \"path\": \"path3\",\n"
                + "          \"color\": \"yellow\"\n"
                + "        },\n"
                + "        {\n"
                + "          \"path\": \"path4\",\n"
                + "          \"color\": \"magenta\"\n"
                + "        }\n"
                + "      ]\n"
                + "    }\n"
                + "  }\n"
                + "]", json);

        class TT {

            String path;
            List<String> expected;

            public TT(String path, String... expected) {
                this.path = path;
                this.expected = Arrays.asList(expected);
            }

            void check(List<NutsElement> a) {

            }
        }

        for (TT tt : new TT[]{
            new TT("","[\n" +
"  [\n" +
"    {\n" +
"      \"first\": {\n" +
"        \"name\": \"first name\",\n" +
"        \"valid\": true,\n" +
"        \"children\": [\n" +
"          {\n" +
"            \"path\": \"path1\",\n" +
"            \"color\": \"red\"\n" +
"          },\n" +
"          {\n" +
"            \"path\": \"path2\",\n" +
"            \"color\": \"green\"\n" +
"          }\n" +
"        ]\n" +
"      }\n" +
"    },\n" +
"    {\n" +
"      \"second\": {\n" +
"        \"name\": \"second name\",\n" +
"        \"valid\": true,\n" +
"        \"children\": [\n" +
"          {\n" +
"            \"path\": \"path3\",\n" +
"            \"color\": \"yellow\"\n" +
"          },\n" +
"          {\n" +
"            \"path\": \"path4\",\n" +
"            \"color\": \"magenta\"\n" +
"          }\n" +
"        ]\n" +
"      }\n" +
"    }\n" +
"  ]\n" +
"]"),
            new TT(".","[\n" +
"  {\n" +
"    \"first\": {\n" +
"      \"name\": \"first name\",\n" +
"      \"valid\": true,\n" +
"      \"children\": [\n" +
"        {\n" +
"          \"path\": \"path1\",\n" +
"          \"color\": \"red\"\n" +
"        },\n" +
"        {\n" +
"          \"path\": \"path2\",\n" +
"          \"color\": \"green\"\n" +
"        }\n" +
"      ]\n" +
"    }\n" +
"  },\n" +
"  {\n" +
"    \"second\": {\n" +
"      \"name\": \"second name\",\n" +
"      \"valid\": true,\n" +
"      \"children\": [\n" +
"        {\n" +
"          \"path\": \"path3\",\n" +
"          \"color\": \"yellow\"\n" +
"        },\n" +
"        {\n" +
"          \"path\": \"path4\",\n" +
"          \"color\": \"magenta\"\n" +
"        }\n" +
"      ]\n" +
"    }\n" +
"  }\n" +
"]"),
            new TT("*","[\n" +
"  {\n" +
"    \"first\": {\n" +
"      \"name\": \"first name\",\n" +
"      \"valid\": true,\n" +
"      \"children\": [\n" +
"        {\n" +
"          \"path\": \"path1\",\n" +
"          \"color\": \"red\"\n" +
"        },\n" +
"        {\n" +
"          \"path\": \"path2\",\n" +
"          \"color\": \"green\"\n" +
"        }\n" +
"      ]\n" +
"    }\n" +
"  },\n" +
"  {\n" +
"    \"second\": {\n" +
"      \"name\": \"second name\",\n" +
"      \"valid\": true,\n" +
"      \"children\": [\n" +
"        {\n" +
"          \"path\": \"path3\",\n" +
"          \"color\": \"yellow\"\n" +
"        },\n" +
"        {\n" +
"          \"path\": \"path4\",\n" +
"          \"color\": \"magenta\"\n" +
"        }\n" +
"      ]\n" +
"    }\n" +
"  }\n" +
"]"),
            new TT(".*.name","[\n" +
"  \"first name\",\n" +
"  \"second name\"\n" +
"]"),
            new TT("..name","[\n" +
"  \"first name\",\n" +
"  \"second name\"\n" +
"]"),
            new TT("*.*.name","[\n" +
"  \"first name\",\n" +
"  \"second name\"\n" +
"]")
        }) {
            TestUtils.println("=====================================");
            TestUtils.println("CHECKING : '" + tt.path+"'");
            List<NutsElement> filtered1 = e.compilePath(tt.path).filter(p);
            ss.setValue(filtered1).println();
            Assertions.assertEquals(tt.expected.get(0), ss.format());
        }
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null,new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

}
