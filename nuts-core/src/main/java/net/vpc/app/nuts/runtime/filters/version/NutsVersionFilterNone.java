package net.vpc.app.nuts.runtime.filters.version;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.filters.AbstractNutsFilter;
import net.vpc.app.nuts.runtime.filters.id.NutsScriptAwareIdFilter;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NutsVersionFilterNone extends AbstractNutsFilter implements NutsVersionFilter, Simplifiable<NutsVersionFilter>, NutsScriptAwareIdFilter {

    private NutsVersionFilter[] all;

    public NutsVersionFilterNone(NutsWorkspace ws,NutsVersionFilter... all) {
        super(ws, NutsFilterOp.NOT);
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
        return CoreNutsUtils.simplifyFilterNone(getWorkspace(),NutsVersionFilter.class,this,all);
    }

    @Override
    public String toJsNutsIdFilterExpr() {
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
            if (id instanceof NutsScriptAwareIdFilter) {
                NutsScriptAwareIdFilter b = (NutsScriptAwareIdFilter) id;
                String expr = b.toJsNutsIdFilterExpr();
                if (CoreStringUtils.isBlank(expr)) {
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
        return "Not("+CoreStringUtils.join(" Or ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()))+")";
    }
    public NutsFilter[] getSubFilters() {
        return all;
    }

}