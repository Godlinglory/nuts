/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2019 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.format.elem;

import net.vpc.app.nuts.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author vpc
 */
public class DefaultNutsObjectElementBuilder implements NutsObjectElementBuilder {

    private final Map<String, NutsElement> values = new LinkedHashMap<String, NutsElement>();

    public DefaultNutsObjectElementBuilder() {

    }

    @Override
    public Collection<NutsNamedElement> children() {
        return values.entrySet().stream().map(x -> new DefaultNutsNamedElement(x.getKey(), x.getValue())).collect(Collectors.toList());
    }

    @Override
    public NutsElement get(String s) {
        return values.get(s);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NutsObjectElementBuilder set(String s, NutsElement e) {
        if (e == null) {
            throw new NullPointerException();
        }
        values.put(s, e);
        return this;
    }


    @Override
    public NutsObjectElementBuilder remove(String s) {
        values.remove(s);
        return this;
    }

    @Override
    public NutsObjectElementBuilder clear() {
        values.clear();
        return this;
    }

    @Override
    public NutsObjectElementBuilder set(NutsObjectElement other) {
        clear();
        add(other);
        return this;
    }

    @Override
    public NutsObjectElementBuilder set(NutsObjectElementBuilder other) {
        clear();
        add(other);
        return this;
    }

    @Override
    public NutsObjectElementBuilder add(NutsObjectElement other) {
        if (other != null) {
            for (NutsNamedElement child : other.children()) {
                set(child.getName(), child.getValue());
            }
        }
        return this;
    }

    @Override
    public NutsObjectElementBuilder add(NutsObjectElementBuilder other) {
        if (other != null) {
            for (NutsNamedElement child : other.children()) {
                set(child.getName(), child.getValue());
            }
        }
        return this;
    }

    @Override
    public NutsObjectElement build() {
        return new DefaultNutsObjectElement(values);
    }

    @Override
    public String toString() {
        return "[" + children().stream().map(x -> "{"
                + x.getName()
                + ":"
                + x.getValue().toString()
                + "}").collect(Collectors.joining(", ")) + "]";
    }

}
