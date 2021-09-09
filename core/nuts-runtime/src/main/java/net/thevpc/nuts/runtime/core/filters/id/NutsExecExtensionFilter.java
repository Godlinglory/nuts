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
public class NutsExecExtensionFilter extends AbstractDescriptorFilter {
    private NutsId apiId;
    public NutsExecExtensionFilter(NutsSession ws, NutsId apiId) {
        super(ws, NutsFilterOp.CUSTOM);
        this.apiId=apiId;
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor other, NutsSession session) {
        if(!NutsUtilStrings.parseBoolean(other.getPropertyValue("nuts-extension"),false,false)){
            return false;
        }
        for (NutsDependency dependency : other.getDependencies()) {
            if(dependency.toId().getShortName().equals(NutsConstants.Ids.NUTS_API)){
                if(apiId==null){
                    return true;
                }
                if(apiId.getVersion().equals(dependency.toId().getVersion())){
                    return true;
                }
                return false;
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
            return "extension";
        }
        return "extension("+ apiId.getVersion()+")";
    }

}
