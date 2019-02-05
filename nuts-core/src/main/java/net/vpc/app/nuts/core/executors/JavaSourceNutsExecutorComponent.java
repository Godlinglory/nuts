/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.executors;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsDefinition;
import net.vpc.app.nuts.core.NutsExecutionContextImpl;
import net.vpc.app.nuts.core.util.*;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.ArrayUtils;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class JavaSourceNutsExecutorComponent implements NutsExecutorComponent {

    public static final Logger log = Logger.getLogger(JavaSourceNutsExecutorComponent.class.getName());
    public static final NutsId ID = CoreNutsUtils.parseNutsId("java");

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
    public int exec(NutsExecutionContext executionContext) {
        NutsDefinition nutMainFile = executionContext.getNutsDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
        String javaFile = nutMainFile.getContent().getFile();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        File folder = executionContext.getWorkspace().getIOManager().createTempFolder("jj");
        int res = compiler.run(null, null, null, "-d", folder.getPath(),javaFile);
        if(res!=0){
            return res;
        }
        JavaNutsExecutorComponent cc=new JavaNutsExecutorComponent();
        NutsDefinition d = executionContext.getNutsDefinition();
        d=new DefaultNutsDefinition(d);
        ((DefaultNutsDefinition) d).setContent(new NutsContent(
                folder.getPath(),
                false,
                true
        ));
        NutsExecutionContext executionContext2=new NutsExecutionContextImpl(
                d,
                executionContext.getArgs(),
                ArrayUtils.concatArrays(
                        executionContext.getExecutorOptions(),
                        new String[]{
                                "--main-class",
                                new File(javaFile.substring(javaFile.length()-".java".length())).getName(),
                                "--class-path",
                                folder.getName(),
                        }
                ),
                executionContext.getEnv(),
                executionContext.getExecutorProperties(),
                executionContext.getCwd(),
                executionContext.getSession(),
                executionContext.getWorkspace(),
                executionContext.isFailFast(),
                executionContext.getCommandName()
        );
        return cc.exec(executionContext2);
    }

}
