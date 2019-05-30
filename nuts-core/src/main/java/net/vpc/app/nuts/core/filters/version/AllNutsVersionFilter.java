package net.vpc.app.nuts.core.filters.version;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsVersionFilter;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.Simplifiable;
import net.vpc.app.nuts.core.filters.id.NutsScriptAwareIdFilter;

public class AllNutsVersionFilter implements NutsVersionFilter, Simplifiable<NutsVersionFilter>, NutsScriptAwareIdFilter {

    public static final AllNutsVersionFilter INSTANCE = new AllNutsVersionFilter();

    public AllNutsVersionFilter() {
    }

    @Override
    public boolean accept(NutsVersion version, NutsWorkspace ws, NutsSession session) {
        return true;
    }

    @Override
    public NutsVersionFilter simplify() {
        return null;
    }

    @Override
    public String toJsNutsIdFilterExpr() {
        return "true";
    }

    @Override
    public String toString() {
        return "AllVersions";
    }

    @Override
    public int hashCode() {
        return 3368;
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
        return true;
    }

}
