package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NutsStream;

import java.util.*;
import java.util.stream.Collectors;


public class NutsDependencyOsDistIdFilter extends AbstractDependencyFilter  {

    private Set<NutsId> accepted = new HashSet<>();

    public NutsDependencyOsDistIdFilter(NutsSession session) {
        super(session, NutsFilterOp.CUSTOM);
    }

    private NutsDependencyOsDistIdFilter(NutsSession session, Collection<NutsId> accepted) {
        super(session, NutsFilterOp.CUSTOM);
        this.accepted = new LinkedHashSet<>(accepted);
    }

    public NutsDependencyOsDistIdFilter add(Collection<NutsId> os) {
        LinkedHashSet<NutsId> s2 = new LinkedHashSet<>(accepted);
        s2.addAll(os);
        return new NutsDependencyOsDistIdFilter(getSession(), s2);
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        List<String> current = NutsStream.of(dependency.getCondition().getOsDist(),session).filterNonBlank().toList();
        if(current.size()==0 || accepted.isEmpty()){
            return true;
        }
        for (NutsId nutsId : accepted) {
            if(CoreFilterUtils.matchesOsDist(nutsId.toString(),current,session)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return accepted.isEmpty() ? "true" : "os in (" + accepted.stream().map(Object::toString).collect(Collectors.joining(", ")) + ')';
    }

    @Override
    public NutsDependencyFilter simplify() {
        return accepted.isEmpty() ? NutsDependencyFilters.of(getSession()).always() : this;
    }
}
