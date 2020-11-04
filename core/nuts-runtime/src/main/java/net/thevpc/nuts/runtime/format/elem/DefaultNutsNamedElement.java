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
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts.runtime.format.elem;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsNamedElement;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsNamedElement implements NutsNamedElement {

    private final String name;
    private final NutsElement value;

    public DefaultNutsNamedElement(String key, NutsElement value) {
        this.name = key;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NutsElement getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DefaultNutsNamedElement{" + "name=" + CoreStringUtils.dblQuote(name) + ", value=" + value + '}';
    }

}
