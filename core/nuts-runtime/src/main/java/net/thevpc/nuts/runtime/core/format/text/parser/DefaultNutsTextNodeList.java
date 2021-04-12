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
 *
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
package net.thevpc.nuts.runtime.core.format.text.parser;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeList;
import net.thevpc.nuts.NutsTextNodeType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.thevpc.nuts.NutsSession;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNutsTextNodeList extends AbstractNutsTextNode implements NutsTextNodeList {

    private List<NutsTextNode> children = new ArrayList<NutsTextNode>();

    public DefaultNutsTextNodeList(NutsSession ws, NutsTextNode... children) {
        super(ws);
        if (children != null) {
            for (NutsTextNode c : children) {
                if (c != null) {
                    this.children.add(c);
                }
            }
        }
    }

    @Override
    public NutsTextNode simplify() {
        if (isEmpty()) {
            return new DefaultNutsTextNodePlain(getSession(), "");
        }
        if (size() == 1) {
            return get(0);
        }
        return this;
    }

    @Override
    public NutsTextNodeType getType() {
        return NutsTextNodeType.LIST;
    }

    @Override
    public NutsTextNode get(int index) {
        return children.get(index);
    }

    @Override
    public int size() {
        return children.size();
    }

    @Override
    public Iterator<NutsTextNode> iterator() {
        return children.iterator();
    }
    
}
