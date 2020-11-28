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
package net.thevpc.nuts.runtime.format.text.parser;


import net.thevpc.nuts.NutsTextNodeCode;
import net.thevpc.nuts.NutsTextNodeType;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNutsTextNodeCode extends NutsTextNodeSpecialBase implements NutsTextNodeCode {

    private final String text;

    public DefaultNutsTextNodeCode(String start, String kind, String extraWhites, String end, String text) {
        super(start,kind,extraWhites,end);
        this.text = text;
    }
    @Override
    public NutsTextNodeType getType() {
        return NutsTextNodeType.CODE;
    }

    public String getText() {
        return text;
    }
}
