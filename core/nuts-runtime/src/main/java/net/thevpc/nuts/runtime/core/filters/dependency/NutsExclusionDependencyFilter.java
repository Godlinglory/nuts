package net.thevpc.nuts.runtime.core.filters.dependency;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.common.Simplifiable;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class NutsExclusionDependencyFilter extends AbstractNutsFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {

    private final NutsDependencyFilter base;
    private final NutsId[] exclusions;

    public NutsExclusionDependencyFilter(NutsWorkspace ws, NutsDependencyFilter base, NutsId[] exclusions) {
        super(ws, NutsFilterOp.CUSTOM);
        this.base = base;
        this.exclusions = exclusions;
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        if (base != null) {
            if (!base.acceptDependency(from, dependency, session)) {
                return false;
            }
        }
        for (NutsId exclusion : exclusions) {
            NutsId nutsId = dependency.toId();
            if (nutsId.groupIdToken().like(exclusion.getGroupId())
                    && nutsId.artifactIdToken().like(exclusion.getArtifactId())
                    && exclusion.getVersion().filter().acceptVersion(nutsId.getVersion(), session)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsDependencyFilter simplify() {
        if (exclusions.length == 0) {
            return base;
        }
        NutsDependencyFilter base2 = CoreNutsUtils.simplify(base);
        if (base2 != base) {
            return new NutsExclusionDependencyFilter(getWorkspace(),base2, exclusions);
        }
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.base);
        hash = 67 * hash + Arrays.deepHashCode(this.exclusions);
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
        final NutsExclusionDependencyFilter other = (NutsExclusionDependencyFilter) obj;
        if (!Objects.equals(this.base, other.base)) {
            return false;
        }
        if (!Arrays.deepEquals(this.exclusions, other.exclusions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return base + (exclusions == null ? "" : (" excludes " + Arrays.stream(exclusions)
                .map(x -> x.getLongName())
                .collect(Collectors.joining(","))));
    }

}
