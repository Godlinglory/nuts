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
 * <p>
 * Copyright (C) 2016-2020 thevpc
 * <p>
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
package net.thevpc.nuts.runtime.filters.repository;

import net.thevpc.nuts.NutsFilterOp;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.NutsRepositoryFilter;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.util.common.Simplifiable;

/**
 * Created by vpc on 2/20/17.
 */
public class NutsRepositoryFilterFalse extends AbstractNutsFilter implements NutsRepositoryFilter, Simplifiable<NutsRepositoryFilter> {
    public NutsRepositoryFilterFalse(NutsWorkspace ws) {
        super(ws, NutsFilterOp.FALSE);
    }

    @Override
    public boolean acceptRepository(NutsRepository value) {
        return false;
    }

    /**
     * @return null if nothing to check after
     */
    @Override
    public NutsRepositoryFilter simplify() {
        return this;
    }

    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
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
        final NutsRepositoryFilterFalse other = (NutsRepositoryFilterFalse) obj;
        return true;
    }

    @Override
    public String toString() {
        return "false";
    }

    @Override
    public NutsFilterOp getFilterOp() {
        return NutsFilterOp.FALSE;
    }
}
