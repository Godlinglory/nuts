package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsExprIdFilter;
import net.thevpc.nuts.runtime.standalone.util.Simplifiable;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NutsVersionFilterNone extends AbstractVersionFilter implements NutsVersionFilter, Simplifiable<NutsVersionFilter>, NutsExprIdFilter {

    private NutsVersionFilter[] all;

    public NutsVersionFilterNone(NutsSession session, NutsVersionFilter... all) {
        super(session, NutsFilterOp.NOT);
        List<NutsVersionFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NutsVersionFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NutsVersionFilter[0]);
    }

    @Override
    public boolean acceptVersion(NutsVersion id, NutsSession session) {
        if (all.length == 0) {
            return true;
        }
        for (NutsVersionFilter filter : all) {
            if (filter.acceptVersion(id, session)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsVersionFilter simplify() {
        return CoreNutsUtils.simplifyFilterNone(getSession(),NutsVersionFilter.class,this,all);
    }

    public String toExpr() {
        StringBuilder sb = new StringBuilder();
        if (all.length == 0) {
            return "true";
        }
        if (all.length > 1) {
            sb.append("(");
        }
        for (NutsVersionFilter id : all) {
            if (sb.length() > 0) {
                sb.append(" && ");
            }
            if (id instanceof NutsExprIdFilter) {
                NutsExprIdFilter b = (NutsExprIdFilter) id;
                String expr = b.toExpr();
                if (NutsBlankable.isBlank(expr)) {
                    return null;
                }
                sb.append("(").append(expr).append("')");
            } else {
                return null;
            }
        }
        if (all.length > 0) {
            sb.append(")");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Arrays.deepHashCode(this.all);
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
        final NutsVersionFilterNone other = (NutsVersionFilterNone) obj;
        if (!Arrays.deepEquals(this.all, other.all)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Not("+String.join(" Or ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()))+")";
    }
    public NutsFilter[] getSubFilters() {
        return all;
    }

}
