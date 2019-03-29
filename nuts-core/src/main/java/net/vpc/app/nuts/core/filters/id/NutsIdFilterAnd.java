package net.vpc.app.nuts.core.filters.id;

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.Simplifiable;
import net.vpc.common.strings.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NutsIdFilterAnd implements NutsIdFilter, Simplifiable<NutsIdFilter>, NutsJsAwareIdFilter {

    private NutsIdFilter[] children;

    public NutsIdFilterAnd(NutsIdFilter... all) {
        List<NutsIdFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NutsIdFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.children = valid.toArray(new NutsIdFilter[0]);
    }

    public NutsIdFilter[] getChildren() {
        return Arrays.copyOf(children, children.length);
    }

    @Override
    public boolean accept(NutsId id) {
        if (children.length == 0) {
            return true;
        }
        for (NutsIdFilter filter : children) {
            if (!filter.accept(id)) {
                return false;
            }
        }
        return true;
    }

    public NutsIdFilter simplify() {
        if(children.length==0){
            return null;
        }
        NutsIdFilter[] newValues = CoreNutsUtils.simplifyAndShrink(NutsIdFilter.class, children);
        if (newValues != null) {
            if (newValues.length == 0) {
                return null;
            }
            if (newValues.length == 1) {
                return newValues[0];
            }
            return new NutsIdFilterAnd(newValues);
        }else{
            if (children.length == 0) {
                return null;
            }
            if (children.length == 1) {
                return children[0];
            }
            return this;
        }
    }

    @Override
    public String toJsNutsIdFilterExpr() {
        StringBuilder sb = new StringBuilder();
        if (children.length == 0) {
            return "true";
        }
        if (children.length > 1) {
            sb.append("(");
        }
        for (NutsIdFilter id : children) {
            if (sb.length() > 0) {
                sb.append(" && ");
            }
            if (id instanceof NutsJsAwareIdFilter) {
                NutsJsAwareIdFilter b = (NutsJsAwareIdFilter) id;
                String expr = b.toJsNutsIdFilterExpr();
                if (StringUtils.isEmpty(expr)) {
                    return null;
                }
                sb.append("(").append(expr).append("')");
            } else {
                return null;
            }
        }
        if (children.length > 0) {
            sb.append(")");
        }
        return sb.toString();
    }

        @Override
    public String toString() {
        return StringUtils.join(" And ", Arrays.asList(children).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Arrays.deepHashCode(this.children);
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
        final NutsIdFilterAnd other = (NutsIdFilterAnd) obj;
        if (!Arrays.deepEquals(this.children, other.children)) {
            return false;
        }
        return true;
    }

    
}
