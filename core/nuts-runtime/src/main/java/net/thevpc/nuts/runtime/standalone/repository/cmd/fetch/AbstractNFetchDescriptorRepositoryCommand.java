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
 *
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
package net.thevpc.nuts.runtime.standalone.repository.cmd.fetch;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositoryCommandBase;
import net.thevpc.nuts.spi.NFetchDescriptorRepositoryCommand;

/**
 *
 * @author thevpc
 * %category SPI Base
 */
public abstract class AbstractNFetchDescriptorRepositoryCommand extends NRepositoryCommandBase<NFetchDescriptorRepositoryCommand> implements NFetchDescriptorRepositoryCommand {


    protected NId id;
    protected NDescriptor result;

    public AbstractNFetchDescriptorRepositoryCommand(NRepository repo) {
        super(repo, "fetch-descriptor");
    }

    @Override
    public boolean configureFirst(NCmdLine cmd) {
        if (super.configureFirst(cmd)) {
            return true;
        }
        return false;
    }


    @Override
    public NDescriptor getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

//    @Override
//    public NutsFetchDescriptorRepositoryCommand id(NutsId id) {
//        return setId(id);
//    }

    @Override
    public NFetchDescriptorRepositoryCommand setId(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public NId getId() {
        return id;
    }

}
