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
package net.thevpc.nuts.runtime.standalone.executor.war;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.spi.NExecutorComponent;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NStringUtils;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public class WarExecutorComponent implements NExecutorComponent {

    public static NId ID;
    NSession ws;

    @Override
    public NId getId() {
        return ID;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        this.ws=context.getSession();
        if(ID==null){
            ID = NId.of("net.thevpc.nuts.exec:war").get( ws);
        }
        NDefinition def = context.getConstraints(NDefinition.class);
        if (def != null) {
            if ("war".equals(NStringUtils.trim(def.getDescriptor().getPackaging()))) {
                return DEFAULT_SUPPORT + 1;
            }
        }
        return NO_SUPPORT;
    }

    @Override
    public void exec(NExecutionContext executionContext) {
        execHelper(executionContext).exec();
    }

    public IProcessExecHelper execHelper(NExecutionContext executionContext) {
        return new AbstractSyncIProcessExecHelper(executionContext.getSession()) {
            public int exec() {
                throw new NIOException(getSession(), NMsg.ofCstyle("unsupported yet execution of %s with packaging %s",
                        executionContext.getDefinition().getId(),
                        executionContext.getDefinition().getDescriptor().getPackaging()
                ));
            }
        };
    }
}
