package net.vpc.app.nuts.extensions.filters.descriptor;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsDescriptorFilter;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.Simplifiable;

import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

public class NutsDescriptorFilterAnd implements NutsDescriptorFilter, Simplifiable<NutsDescriptorFilter>, JsNutsDescriptorFilter {

    private NutsDescriptorFilter[] all;

    public NutsDescriptorFilterAnd(NutsDescriptorFilter... all) {
        List<NutsDescriptorFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NutsDescriptorFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NutsDescriptorFilter[valid.size()]);
    }

    @Override
    public boolean accept(NutsDescriptor id) {
        if (all.length == 0) {
            return true;
        }
        for (NutsDescriptorFilter filter : all) {
            if (!filter.accept(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsDescriptorFilter simplify() {
        NutsDescriptorFilter[] newValues = CoreNutsUtils.simplifyAndShrink(NutsDescriptorFilter.class, all);
        if (newValues != null) {
            if (newValues.length == 0) {
                return null;
            }
            if (newValues.length == 1) {
                return newValues[0];
            }
            return new NutsDescriptorFilterAnd(newValues);
        }
        return this;
    }

    @Override
    public String toJsNutsDescriptorFilterExpr() {
        StringBuilder sb = new StringBuilder();
        if (all.length == 0) {
            return "true";
        }
        if (all.length > 1) {
            sb.append("(");
        }
        for (NutsDescriptorFilter id : all) {
            if (sb.length() > 0) {
                sb.append(" && ");
            }
            if (id instanceof JsNutsDescriptorFilter) {
                JsNutsDescriptorFilter b = (JsNutsDescriptorFilter) id;
                String expr = b.toJsNutsDescriptorFilterExpr();
                if (CoreStringUtils.isEmpty(expr)) {
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
}
