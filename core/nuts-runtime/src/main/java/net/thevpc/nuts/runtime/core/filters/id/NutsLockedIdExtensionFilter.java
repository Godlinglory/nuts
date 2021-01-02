/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.filters.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.core.util.Simplifiable;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class NutsLockedIdExtensionFilter extends AbstractNutsFilter implements NutsDescriptorFilter, Simplifiable<NutsDescriptorFilter> {
    private NutsId[] lockedIds;
    public NutsLockedIdExtensionFilter(NutsWorkspace ws, NutsId[] lockedIds) {
        super(ws, NutsFilterOp.CUSTOM);
        this.lockedIds =lockedIds;
    }

    public boolean acceptId(NutsId id, NutsSession session) {
        for (NutsId nutsId : lockedIds) {
            if(nutsId.getShortNameId().equalsShortName(id.getShortNameId())){
                return (id.getVersion().filter().acceptVersion(nutsId.getVersion(),session));
            }
        }
        return true;
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor other, NutsSession session) {
        if(!acceptId(other.getId(),session)){
            return false;
        }
        for (NutsDependency dependency : other.getDependencies()) {
            if(!acceptId(dependency.toId(),session)){
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsDescriptorFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        return "LockedIds("+ Arrays.stream(lockedIds).map(NutsId::getLongName).collect(Collectors.joining(","))+")";
    }

}
