/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.format.elem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.vpc.app.nuts.NutsElement;
import net.vpc.app.nuts.NutsNamedElement;

/**
 *
 * @author vpc
 */
class NutsObjectElementMap1 extends NutsObjectElementBase {

    private Map<String, Object> value;

    public NutsObjectElementMap1(Map value, NutsElementFactoryContext context) {
        super(context);
        this.value = value;
    }

    @Override
    public Collection<NutsNamedElement> children() {
        List<NutsNamedElement> all = new ArrayList<>();
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            NutsElement val = context.toElement(entry.getValue());
            all.add(new DefaultNutsNamedElement(entry.getKey(), val));
        }
        return all;
    }

    @Override
    public NutsElement get(String name) {
        if (value.containsKey(name)) {
            return context.toElement(value.get(name));
        }
        return null;
    }

    @Override
    public int size() {
        return value.size();
    }

}
