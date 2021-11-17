package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;

public final class NutsDependencyFilterTrue extends AbstractDependencyFilter{

    public NutsDependencyFilterTrue(NutsSession session) {
        super(session, NutsFilterOp.TRUE);
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        return true;
    }

    @Override
    public NutsDependencyFilter simplify() {
        return null;
    }

    @Override
    public String toString() {
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
        return true;
    }

}
