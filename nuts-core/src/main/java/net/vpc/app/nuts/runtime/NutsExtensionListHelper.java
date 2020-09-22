/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
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
package net.vpc.app.nuts.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.main.config.NutsWorkspaceConfigBoot;

/**
 *
 * @author vpc
 */
public class NutsExtensionListHelper {

    private List<NutsWorkspaceConfigBoot.ExtensionConfig> initial = new ArrayList<>();
    private List<NutsWorkspaceConfigBoot.ExtensionConfig> list = new ArrayList<>();

    public NutsExtensionListHelper(List<NutsWorkspaceConfigBoot.ExtensionConfig> old) {
        if (old != null) {
            for (NutsWorkspaceConfigBoot.ExtensionConfig a : old) {
                if (a != null) {
                    this.list.add(a);
                }
            }
        }
    }

    public NutsExtensionListHelper save() {
        initial = new ArrayList<>(list);
        compress();
        return this;
    }

    public boolean hasChanged() {
        return !initial.equals(list);
    }

    public NutsExtensionListHelper copy() {
        return new NutsExtensionListHelper(list);
    }

    public NutsExtensionListHelper compress() {
        LinkedHashMap<String, NutsWorkspaceConfigBoot.ExtensionConfig> m = new LinkedHashMap<>();
        for (NutsWorkspaceConfigBoot.ExtensionConfig id : list) {
            m.put(id.getId().getShortName(),
                    new NutsWorkspaceConfigBoot.ExtensionConfig(id.getId().getLongNameId(), id.isEnabled())
            );
        }
        list.clear();
        list.addAll(m.values());
        return this;
    }

    public NutsExtensionListHelper add(NutsId id) {
        for (int i = 0; i < list.size(); i++) {
            NutsWorkspaceConfigBoot.ExtensionConfig a = list.get(i);
            if (a.getId().getShortName().equals(id.getShortName())) {
                list.set(i, new NutsWorkspaceConfigBoot.ExtensionConfig(id,true));
                return this;
            }
        }
        return this;
    }

    public NutsExtensionListHelper remove(NutsId id) {
        for (int i = 0; i < list.size(); i++) {
            NutsWorkspaceConfigBoot.ExtensionConfig a = list.get(i);
            if (a.getId().getShortName().equals(id.getShortName())) {
                list.remove(i);
                return this;
            }
        }
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.list);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutsExtensionListHelper other = (NutsExtensionListHelper) obj;
        if (!Objects.equals(this.list, other.list)) {
            return false;
        }
        return true;
    }

    public List<NutsId> getIds() {
        List<NutsId> ids=new ArrayList<>();
        for (NutsWorkspaceConfigBoot.ExtensionConfig i : list) {
            ids.add(i.getId());
        }
        return ids;
    }

    public List<NutsWorkspaceConfigBoot.ExtensionConfig> getConfs() {
        List<NutsWorkspaceConfigBoot.ExtensionConfig> copy=new ArrayList<>();
        for (NutsWorkspaceConfigBoot.ExtensionConfig i : list) {
            copy.add(new NutsWorkspaceConfigBoot.ExtensionConfig(i.getId(),i.isEnabled()));
        }
        return copy;
    }

}
