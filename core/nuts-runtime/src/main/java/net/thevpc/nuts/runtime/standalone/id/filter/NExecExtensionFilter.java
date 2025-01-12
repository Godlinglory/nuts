/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.filter.AbstractDescriptorFilter;

/**
 *
 * @author thevpc
 */
public class NExecExtensionFilter extends AbstractDescriptorFilter {
    private NId apiId;
    public NExecExtensionFilter(NSession session, NId apiId) {
        super(session, NFilterOp.CUSTOM);
        this.apiId=apiId;
    }

    @Override
    public boolean acceptDescriptor(NDescriptor other, NSession session) {
        if(other.getIdType()!= NIdType.EXTENSION){
            return false;
        }
        for (NDependency dependency : other.getDependencies()) {
            if(dependency.toId().getShortName().equals(NConstants.Ids.NUTS_API)){
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
    public NDescriptorFilter simplify() {
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
