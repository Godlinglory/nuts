/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.core.app;

import java.io.File;
import java.nio.file.Path;
import net.thevpc.nuts.NutsCommandHistory;
import net.thevpc.nuts.NutsCommandHistoryBuilder;
import net.thevpc.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
class NutsCommandHistoryBuilderImpl implements NutsCommandHistoryBuilder {
    
    private NutsWorkspace ws;

    public NutsCommandHistoryBuilderImpl(NutsWorkspace ws) {
        this.ws = ws;
    }
    private Path path;

    @Override
    public NutsCommandHistoryBuilder setPath(Path path) {
        this.path = path;
        return this;
    }

    @Override
    public NutsCommandHistoryBuilder setPath(File path) {
        this.path = path==null?null:path.toPath();
        return this;
    }

    @Override
    public NutsCommandHistory build() {
        return new NutsCommandHistoryImpl(ws,path);
    }
    
}
