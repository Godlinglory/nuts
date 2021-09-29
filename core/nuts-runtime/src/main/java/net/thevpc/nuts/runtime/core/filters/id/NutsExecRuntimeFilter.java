/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.filters.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.descriptor.AbstractDescriptorFilter;
import net.thevpc.nuts.runtime.core.util.CoreBooleanUtils;

/**
 *
 * @author thevpc
 */
public class NutsExecRuntimeFilter extends AbstractDescriptorFilter {
    private NutsId apiId;
    private boolean communityRuntime;
    public NutsExecRuntimeFilter(NutsSession session, NutsId apiId, boolean communityRuntime) {
        super(session, NutsFilterOp.CUSTOM);
        this.apiId=apiId;
        this.communityRuntime = communityRuntime;
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor other, NutsSession session) {
        if(other.getId().getShortName().equals(NutsConstants.Ids.NUTS_RUNTIME)){
            if(apiId==null){
                return true;
            }
            for (NutsDependency dependency : other.getDependencies()) {
                if (dependency.toId().getShortName().equals(NutsConstants.Ids.NUTS_API)) {
                    if (apiId.getVersion().equals(dependency.toId().getVersion())) {
                        return true;
                    }
                    return false;
                }
            }
        }
        if(communityRuntime) {
            if (!NutsUtilStrings.parseBoolean(other.getPropertyValue("nuts-runtime"), false,false)) {
                return false;
            }
            for (NutsDependency dependency : other.getDependencies()) {
                if (dependency.toId().getShortName().equals(NutsConstants.Ids.NUTS_API)) {
                    if (apiId == null) {
                        return true;
                    }
                    if (apiId.getVersion().equals(dependency.toId().getVersion())) {
                        return true;
                    }
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public NutsDescriptorFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        if(apiId==null){
            return "runtime";
        }
        return "runtime("+ apiId.getVersion()+")";
    }

}
