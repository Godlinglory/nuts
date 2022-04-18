package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class NutsDependencyOsFilter extends AbstractDependencyFilter  {

    private Set<NutsOsFamily> os = EnumSet.noneOf(NutsOsFamily.class);

    public NutsDependencyOsFilter(NutsSession session) {
        super(session, NutsFilterOp.CUSTOM);
    }

    private NutsDependencyOsFilter(NutsSession session, Collection<NutsOsFamily> os) {
        super(session, NutsFilterOp.CUSTOM);
        this.os = EnumSet.copyOf(os);
    }

    public NutsDependencyOsFilter(NutsSession session, String os) {
        super(session, NutsFilterOp.CUSTOM);
        this.os = EnumSet.noneOf(NutsOsFamily.class);
        for (String e : StringTokenizerUtils.splitDefault(os)) {
            if (!e.isEmpty()) {
                this.os.add(NutsOsFamily.parse(e).orElse(null));
            }
        }
    }

    public NutsDependencyOsFilter add(Collection<NutsOsFamily> os) {
        EnumSet<NutsOsFamily> s2 = EnumSet.copyOf(this.os);
        s2.addAll(os);
        return new NutsDependencyOsFilter(getSession(), s2);
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        List<String> current = dependency.getCondition().getOs();
        boolean empty = true;
        if (current != null) {
            for (String e : current) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (os.contains(NutsOsFamily.parse(e).orElse(null))) {
                        return true;
                    }
                }
            }
        }
        return empty;
    }

    @Override
    public String toString() {
        return os.isEmpty() ? "true" : "os in (" + os.stream().map(x -> x.id()).collect(Collectors.joining(", ")) + ')';
    }

    @Override
    public NutsDependencyFilter simplify() {
        return os.isEmpty() ? NutsDependencyFilters.of(getSession()).always() : this;
    }
}
