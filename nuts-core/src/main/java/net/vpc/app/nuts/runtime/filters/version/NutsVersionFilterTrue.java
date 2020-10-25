package net.vpc.app.nuts.runtime.filters.version;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.filters.AbstractNutsFilter;
import net.vpc.app.nuts.runtime.filters.id.NutsScriptAwareIdFilter;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

public class NutsVersionFilterTrue extends AbstractNutsFilter implements NutsVersionFilter, Simplifiable<NutsVersionFilter>, NutsScriptAwareIdFilter {

    public NutsVersionFilterTrue(NutsWorkspace ws) {
        super(ws, NutsFilterOp.TRUE);
    }

    @Override
    public boolean acceptVersion(NutsVersion id, NutsSession session) {
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
        final NutsVersionFilterTrue other = (NutsVersionFilterTrue) obj;
        return true;
    }

    @Override
    public String toString() {
        return "true";
    }

}
