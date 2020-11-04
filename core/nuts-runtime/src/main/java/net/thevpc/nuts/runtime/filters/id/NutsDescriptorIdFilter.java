/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.filters.id;

import java.util.Objects;
import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.bridges.maven.MavenRepositoryFolderHelper;
import net.thevpc.nuts.runtime.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.log.NutsLogVerb;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.util.common.Simplifiable;

/**
 *
 * @author vpc
 */
public class NutsDescriptorIdFilter extends AbstractNutsFilter implements NutsIdFilter, Simplifiable<NutsIdFilter> {

    private NutsLogger LOG;
    private final NutsDescriptorFilter filter;

    public NutsDescriptorIdFilter(NutsDescriptorFilter filter) {
        super(filter.getWorkspace(), NutsFilterOp.CONVERT);
        this.filter = filter;
    }

    @Override
    public boolean acceptSearchId(NutsSearchId sid, NutsSession session) {
        return filter == null ? true : filter.acceptSearchId(sid, session);
    }

    @Override
    public boolean acceptId(NutsId id, NutsSession session) {
        if (filter == null) {
            return true;
        }
        if(LOG==null){
            LOG=session.getWorkspace().log().of(MavenRepositoryFolderHelper.class);
        }
        NutsDescriptor descriptor = null;
        try {
//                descriptor = repository.fetchDescriptor().setId(id).setSession(session).getResult();
            descriptor = session.getWorkspace().fetch().setId(id).setSession(session).getResultDescriptor();
            if (!CoreNutsUtils.isEffectiveId(descriptor.getId())) {
                NutsDescriptor nutsDescriptor = null;
                try {
                    //NutsWorkspace ws = repository.getWorkspace();
                    nutsDescriptor = NutsWorkspaceExt.of(session.getWorkspace()).resolveEffectiveDescriptor(descriptor, session);
                } catch (Exception ex) {
                    LOG.with().level(Level.FINE).error(ex).log( "Failed to resolve effective desc {0} for {1}", descriptor.getId(),id);
                    //throw new NutsException(e);
                }
                descriptor = nutsDescriptor;
            }
        } catch (Exception ex) {
            //suppose we cannot retrieve descriptor
            if (LOG.isLoggable(Level.FINER)) {
                LOG.with().level(Level.FINER).verb(NutsLogVerb.FAIL).log( "Unable to fetch Descriptor for " + id + " : " + ex.toString());
            }
            return false;
        }
        if (!filter.acceptDescriptor(descriptor, session)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.filter);
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
        final NutsDescriptorIdFilter other = (NutsDescriptorIdFilter) obj;
        if (!Objects.equals(this.filter, other.filter)) {
            return false;
        }
        return true;
    }

    @Override
    public NutsIdFilter simplify() {
        NutsDescriptorFilter f2 = CoreNutsUtils.simplify(filter);
        if (f2 == null) {
            return null;
        }
        if (f2 == filter) {
            return this;
        }
        return new NutsDescriptorIdFilter(f2);
    }

    @Override
    public String toString() {
        return String.valueOf(filter);
    }

}
