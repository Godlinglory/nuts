/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.executors;

import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsDefinition;
import net.vpc.app.nuts.core.DefaultNutsExecutionContext;
import net.vpc.app.nuts.core.util.*;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultNutsContent;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class JavaSourceNutsExecutorComponent implements NutsExecutorComponent {

    public static final Logger LOG = Logger.getLogger(JavaSourceNutsExecutorComponent.class.getName());
    public static final NutsId ID = CoreNutsUtils.parseNutsId("net.vpc.app.nuts.exec:exec-java-src");

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public int getSupportLevel(NutsDefinition nutsDefinition) {
        if (nutsDefinition != null) {
            if ("java".equals(nutsDefinition.getDescriptor().getPackaging())) {
                return DEFAULT_SUPPORT + 1;
            }
        }
        return NO_SUPPORT;
    }

    @Override
    public void exec(NutsExecutionContext executionContext) {
        NutsDefinition nutMainFile = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
        Path javaFile = nutMainFile.getPath();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        NutsWorkspace ws = executionContext.getWorkspace();
        Path folder = ws.io().createTempFolder("jj");
        int res = compiler.run(null, null, null, "-d", folder.toString(), javaFile.toString());
        if (res != 0) {
            throw new NutsExecutionException(ws, "Compilation Failed", res);
        }
        JavaNutsExecutorComponent cc = new JavaNutsExecutorComponent();
        NutsDefinition d = executionContext.getDefinition();
        d = new DefaultNutsDefinition(d);
        ((DefaultNutsDefinition) d).setContent(new DefaultNutsContent(
                folder,
                false,
                true
        ));
        String fileName = javaFile.getFileName().toString();
        NutsExecutionContext executionContext2 = new DefaultNutsExecutionContext(
                d,
                executionContext.getArguments(),
                CoreCommonUtils.concatArrays(
                        executionContext.getExecutorOptions(),
                        new String[]{
                            "--main-class",
                            new File(fileName.substring(fileName.length() - ".java".length())).getName(),
                            "--class-path",
                            folder.toString(),}
                ),
                executionContext.getEnv(),
                executionContext.getExecutorProperties(),
                executionContext.getCwd(),
                executionContext.getSession(),
                ws,
                //failFast
                true,
                //temporary
                true,
                executionContext.getExecutionType(),
                executionContext.getCommandName()
        );
        cc.exec(executionContext2);
    }

}
