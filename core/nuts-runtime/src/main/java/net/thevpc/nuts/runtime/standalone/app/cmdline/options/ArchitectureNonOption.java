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
package net.thevpc.nuts.runtime.standalone.app.cmdline.options;

import net.thevpc.nuts.NutsArgumentCandidate;
import net.thevpc.nuts.NutsCommandAutoComplete;
import net.thevpc.nuts.runtime.standalone.app.cmdline.options.DefaultNonOption;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class ArchitectureNonOption extends DefaultNonOption {

    public ArchitectureNonOption(String name) {
        super(name);
    }

    @Override
    public List<NutsArgumentCandidate> getCandidates(NutsCommandAutoComplete context) {
        List<NutsArgumentCandidate> all = new ArrayList<>();
        all.add(new NutsArgumentCandidate("\"linux x86\""));
        all.add(new NutsArgumentCandidate("\"linux x64\""));
        all.add(new NutsArgumentCandidate("\"win x86\""));
        all.add(new NutsArgumentCandidate("\"win x64\""));
        return all;
    }

}