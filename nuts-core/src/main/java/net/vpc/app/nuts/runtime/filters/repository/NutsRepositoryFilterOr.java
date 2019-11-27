package net.vpc.app.nuts.runtime.filters.repository;

import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryFilter;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

import java.util.ArrayList;
import java.util.List;

public class NutsRepositoryFilterOr implements NutsRepositoryFilter, Simplifiable<NutsRepositoryFilter> {

    private NutsRepositoryFilter[] all;

    public NutsRepositoryFilterOr(NutsRepositoryFilter... all) {
        List<NutsRepositoryFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NutsRepositoryFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NutsRepositoryFilter[0]);
    }

    @Override
    public boolean accept(NutsRepository id) {
        if (all.length == 0) {
            return true;
        }
        for (NutsRepositoryFilter filter : all) {
            if (filter.accept(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NutsRepositoryFilter simplify() {
        if (all.length == 0) {
            return null;
        }
        NutsRepositoryFilter[] newValues = CoreNutsUtils.simplifyAndShrink(NutsRepositoryFilter.class, all);
        if (newValues != null) {
            if (newValues.length == 0) {
                return null;
            }
            if (newValues.length == 1) {
                return newValues[0];
            }
            return new NutsRepositoryFilterOr(newValues);
        }
        return this;
    }

}
