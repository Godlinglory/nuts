/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.filters.id;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.core.NutsWorkspaceExt;
import java.util.Objects;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

/**
 *
 * @author vpc
 */
public class NutsDefaultVersionIdFilter implements NutsIdFilter, Simplifiable<NutsIdFilter> {

    private final Boolean defaultVersion;

    public NutsDefaultVersionIdFilter(Boolean defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    @Override
    public boolean accept(NutsId other, NutsSession session) {
        if (defaultVersion == null) {
            return true;
        }
        return NutsWorkspaceExt.of(session.getWorkspace()).getInstalledRepository().isDefaultVersion(other) == defaultVersion;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.defaultVersion);
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
        final NutsDefaultVersionIdFilter other = (NutsDefaultVersionIdFilter) obj;
        if (!Objects.equals(this.defaultVersion, other.defaultVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public NutsIdFilter simplify() {
        if (defaultVersion == null) {
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        return "defaultVersion(" + defaultVersion + ")";
    }

}
