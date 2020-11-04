package net.thevpc.nuts.runtime.main.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsRepositorySupportedAction;

import java.nio.file.Path;
import java.util.Iterator;

public class NutsSimpleRepositoryWrapper extends NutsCachedRepository {
    private NutsRepositoryModel base;
    private int mode;

    public NutsSimpleRepositoryWrapper(NutsAddRepositoryOptions options, NutsWorkspace workspace, NutsRepository parent, NutsRepositoryModel base) {
        super(options, workspace, parent,
                base.getSpeed(),
                (base.getMode() & NutsRepositoryModel.MIRRORING) != 0,
                base.getRepositoryType()
        );
        this.mode = base.getMode();
        lib.setReadEnabled((this.mode & NutsRepositoryModel.LIB_READ) != 0);
        lib.setWriteEnabled((this.mode & NutsRepositoryModel.LIB_WRITE) != 0);
        cache.setReadEnabled((this.mode & NutsRepositoryModel.CACHE_READ) != 0);
        cache.setWriteEnabled((this.mode & NutsRepositoryModel.CACHE_WRITE) != 0);
        this.base = base;
    }

    public Iterator<NutsId> searchVersionsCore(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session) {
        return base.searchVersions(id, idFilter, fetchMode, this, session);
    }

    public NutsId searchLatestVersionCore(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
        return base.searchLatestVersion(id, filter, fetchMode, this, session);
    }

    public NutsDescriptor fetchDescriptorCore(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        return base.fetchDescriptor(id, fetchMode, this, session);
    }

    public NutsContent fetchContentCore(NutsId id, NutsDescriptor descriptor, Path localPath, NutsFetchMode fetchMode, NutsSession session) {
        return base.fetchContent(id, descriptor, localPath, fetchMode, this, session);
    }

    public Iterator<NutsId> searchCore(final NutsIdFilter filter, String[] roots, NutsFetchMode fetchMode, NutsSession session) {
        return base.search(filter, roots, fetchMode, this, session);
    }

    public void updateStatistics2(NutsSession session) {
        base.updateStatistics(this, session);
    }

    protected boolean isAllowedOverrideNut(NutsId id) {
        return ((this.mode & NutsRepositoryModel.LIB_OVERRIDE) != 0);
    }

    @Override
    public boolean acceptAction(NutsId id, NutsRepositorySupportedAction supportedAction, NutsFetchMode mode, NutsSession session) {
        if(!super.acceptAction(id, supportedAction, mode, session)){
            return false;
        }
        switch (supportedAction){
            case DEPLOY: return base.acceptDeploy(id, mode, this, session);
            case SEARCH: return base.acceptFetch(id, mode, this, session);
        }
        return false;
    }
}
