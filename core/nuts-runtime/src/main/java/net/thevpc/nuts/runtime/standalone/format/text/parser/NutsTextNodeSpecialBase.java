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
package net.thevpc.nuts.runtime.standalone.format.text.parser;

import net.thevpc.nuts.NutsTextNode;

/**
 * Created by vpc on 5/23/17.
 */
public abstract class NutsTextNodeSpecialBase extends AbstractNutsTextNode implements NutsTextNode {

    private String start;
    private String kind;
    private String separator;
    private String end;

    public NutsTextNodeSpecialBase(String start, String kind, String separator, String end) {
        this.start=start;
        this.end=end;
        this.kind = kind;
        this.separator = separator;
    }

    public String getKind() {
        return kind;
    }

    public String getSeparator() {
        return separator;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return (getStart() + getKind() +getSeparator()+"..."+getEnd());
    }

}
