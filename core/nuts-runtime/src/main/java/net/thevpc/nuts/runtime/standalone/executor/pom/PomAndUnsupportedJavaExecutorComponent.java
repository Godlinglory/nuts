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
package net.thevpc.nuts.runtime.standalone.executor.pom;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsExecutorComponent;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.util.NutsStringUtils;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class PomAndUnsupportedJavaExecutorComponent implements NutsExecutorComponent {

    public static NutsId ID;
    NutsSession ws;

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        this.ws=context.getSession();
        if(ID==null){
            ID = NutsId.of("net.thevpc.nuts.exec:java-unsupported").get( ws);
        }
        NutsDefinition def = context.getConstraints(NutsDefinition.class);
        if (def != null) {
            switch (NutsStringUtils.trim(def.getDescriptor().getPackaging())){
                case "jar":
                case "war":
                case "zip":
                {
                    return NO_SUPPORT;
                }
            }
            return DEFAULT_SUPPORT + 1;
        }
        return NO_SUPPORT;
    }

    @Override
    public void exec(NutsExecutionContext executionContext) {
        execHelper(executionContext).exec();
    }



    public IProcessExecHelper execHelper(NutsExecutionContext executionContext) {
        return new AbstractSyncIProcessExecHelper(executionContext.getSession()) {

            @Override
            public int exec() {
                throw new NutsIOException(getSession(),NutsMessage.ofCstyle("unsupported execution of %s with packaging %s",
                        executionContext.getDefinition().getId(),
                        executionContext.getDefinition().getDescriptor().getPackaging()
                ));
            }
        };
    }
}
