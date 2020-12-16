package net.thevpc.nuts.runtime.core.filters.dependency;

import java.util.EnumSet;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.standalone.util.common.Simplifiable;

import java.util.Objects;
import net.thevpc.nuts.runtime.standalone.util.NutsDependencyScopes;

public class ScopeNutsDependencyFilter extends AbstractNutsFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {

    private EnumSet<NutsDependencyScope> scopes = EnumSet.noneOf(NutsDependencyScope.class);

    public ScopeNutsDependencyFilter(NutsWorkspace ws, NutsDependencyScopePattern... scopes) {
        super(ws, NutsFilterOp.CUSTOM);
        for (NutsDependencyScopePattern scope : scopes) {
            this.scopes.addAll(NutsDependencyScopes.expand(scope));
        }
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {

        NutsDependencyScope d = NutsDependencyScopes.parseScope(dependency.getScope(), true);
        return d != null && scopes.contains(d);
    }

    @Override
    public NutsDependencyFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        return "(" + scopes + ")";
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.scopes);
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
        final ScopeNutsDependencyFilter other = (ScopeNutsDependencyFilter) obj;
        if (!Objects.equals(this.scopes, other.scopes)) {
            return false;
        }
        return true;
    }

}
