package net.thevpc.nuts.runtime.core.filters.installstatus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.core.filters.id.NutsScriptAwareIdFilter;
import net.thevpc.nuts.runtime.core.util.Simplifiable;

public class NutsInstallStatusFilterFalse extends AbstractNutsFilter implements NutsInstallStatusFilter, Simplifiable<NutsInstallStatusFilter>, NutsScriptAwareIdFilter {

    public NutsInstallStatusFilterFalse(NutsWorkspace ws) {
        super(ws, NutsFilterOp.FALSE);
    }

    @Override
    public boolean acceptInstallStatus(NutsInstallStatus status, NutsSession session) {
        return false;
    }

    public NutsInstallStatusFilter simplify() {
        return this;
    }

    @Override
    public String toJsNutsIdFilterExpr() {
        return "false";
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
        final NutsInstallStatusFilterFalse other = (NutsInstallStatusFilterFalse) obj;
        return true;
    }

    @Override
    public String toString() {
        return "false";
    }

}
