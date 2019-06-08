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
package net.vpc.app.nuts.core.format.json;

import net.vpc.app.nuts.core.format.elem.*;
import net.vpc.app.nuts.NutsElementType;
import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.vpc.app.nuts.NutsElement;
import net.vpc.app.nuts.NutsArrayElement;

/**
 *
 * @author vpc
 */
public class NutsArrayElementJson extends AbstractNutsElement implements NutsArrayElement {

    private NutsElementFactoryContext context;
    private List<Object> values = new ArrayList<>();

    public NutsArrayElementJson(JsonArray nl, NutsElementFactoryContext context) {
        super(NutsElementType.ARRAY);
        this.context = context;
        int count = nl.size();
        for (int i = 0; i < count; i++) {
            values.add(nl.get(i));
        }
    }

    @Override
    public Collection<NutsElement> children() {
        return values.stream().map(x -> context.toElement(x)).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "[" + children().stream().map(x -> x.toString()).collect(Collectors.joining(", ")) + "]";
    }

}
