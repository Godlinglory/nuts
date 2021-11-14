/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.executors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDefinition;
import net.thevpc.nuts.runtime.core.util.CoreArrayUtils;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsExecutorComponent;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.nio.file.Path;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class JavaSourceExecutorComponent implements NutsExecutorComponent {

    public static NutsId ID;
    NutsSession ws;

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public void exec(NutsExecutionContext executionContext) {
        NutsDefinition nutMainFile = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
        Path javaFile = nutMainFile.getFile();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        NutsSession session = executionContext.getSession();
        Path folder = NutsTmp.of(session)
                .createTempFolder("jj").toFile();
        int res = compiler.run(null, null, null, "-d", folder.toString(), javaFile.toString());
        if (res != 0) {
            throw new NutsExecutionException(session, NutsMessage.cstyle("compilation failed"), res);
        }
        JavaExecutorComponent cc = new JavaExecutorComponent();
        NutsDefinition d = executionContext.getDefinition();
        d = new DefaultNutsDefinition(d, session);
        ((DefaultNutsDefinition) d).setContent(new NutsDefaultContent(
                NutsPath.of(folder, session),
                false,
                true
        ));
        String fileName = javaFile.getFileName().toString();
        NutsExecutionContext executionContext2 = NutsWorkspaceExt.of(executionContext.getSession())
                .createExecutionContext()
                .setAll(executionContext)
                .setDefinition(d)
                .setExecutorArguments(CoreArrayUtils.concatArrays(
                        executionContext.getExecutorArguments(),
                        new String[]{
                                "--main-class",
                                new File(fileName.substring(fileName.length() - ".java".length())).getName(),
                                "--class-path",
                                folder.toString()}
                ))
                .setFailFast(true)
                .setTemporary(true)
                .build();
        cc.exec(executionContext2);
    }

    @Override
    public void dryExec(NutsExecutionContext executionContext) throws NutsExecutionException {
        NutsDefinition nutMainFile = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
        Path javaFile = nutMainFile.getFile();
//        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String folder = "__temp_folder";
        NutsPrintStream out = executionContext.getSession().out();
        out.println(NutsTexts.of(executionContext.getSession()).ofStyled("compile", NutsTextStyle.primary4()));
        out.printf("%s%n",
                NutsCommandLine.of(
                        new String[]{
                                "embedded-javac",
                                "-d",
                                "<temp-folder>",
                                javaFile.toString()
                        }, executionContext.getSession()
                )
        );
        JavaExecutorComponent cc = new JavaExecutorComponent();
        NutsDefinition d = executionContext.getDefinition();
        d = new DefaultNutsDefinition(d, executionContext.getSession());
        ((DefaultNutsDefinition) d).setContent(new NutsDefaultContent(
                NutsPath.of(folder, executionContext.getSession()),
                false,
                true
        ));
        String fileName = javaFile.getFileName().toString();
        NutsExecutionContext executionContext2 = NutsWorkspaceExt.of(executionContext.getSession())
                .createExecutionContext()
                .setAll(executionContext)
                .setDefinition(d)
                .setExecutorArguments(CoreArrayUtils.concatArrays(
                        executionContext.getExecutorArguments(),
                        new String[]{
                                "--main-class",
                                new File(fileName.substring(fileName.length() - ".java".length())).getName(),
                                "--class-path",
                                folder.toString()
                        }
                ))
                .setFailFast(true)
                .setTemporary(true)
                .build();
        cc.dryExec(executionContext2);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        this.ws = context.getSession();
        if (ID == null) {
            ID = NutsId.of("net.thevpc.nuts.exec:exec-java-src", ws);
        }
        NutsDefinition def = context.getConstraints(NutsDefinition.class);
        if (def != null) {
            if ("java".equals(def.getDescriptor().getPackaging())) {
                return DEFAULT_SUPPORT + 1;
            }
        }
        return NO_SUPPORT;
    }

}
