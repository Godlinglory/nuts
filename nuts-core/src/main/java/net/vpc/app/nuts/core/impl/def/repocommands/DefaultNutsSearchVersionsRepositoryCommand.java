/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.impl.def.repocommands;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.core.repocommands.AbstractNutsSearchVersionsRepositoryCommand;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.iter.IteratorBuilder;
import net.vpc.app.nuts.NutsSearchVersionsRepositoryCommand;
import net.vpc.app.nuts.core.util.iter.IteratorUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsSearchVersionsRepositoryCommand extends AbstractNutsSearchVersionsRepositoryCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsSearchVersionsRepositoryCommand.class.getName());

    public DefaultNutsSearchVersionsRepositoryCommand(NutsRepository repo) {
        super(repo);
    }

    @Override
    public NutsSearchVersionsRepositoryCommand run() {
        NutsWorkspaceUtils.checkSession(getRepo().getWorkspace(), getSession());
        id = id.setFaceContent();
        getRepo().security().checkAllowed(NutsConstants.Permissions.FETCH_DESC, "find-versions");
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        NutsWorkspaceUtils.checkSimpleNameNutsId(repo.getWorkspace(), id);
        xrepo.checkAllowedFetch(id, getSession());
        try {
            if (getSession().isIndexed() && xrepo.getIndexStoreClient() != null && xrepo.getIndexStoreClient().isEnabled()) {
                List<NutsId> d = null;
                try {
                    d = xrepo.getIndexStoreClient().searchVersions(id, getSession());
                } catch (NutsException ex) {
                    LOG.log(Level.FINEST, "[ERROR  ] Error find version operation with Indexer for {0} : {1}", new Object[]{getRepo().config().getName(), ex});
                }
                if (d != null && !d.isEmpty() && filter != null) {
                    result = IteratorBuilder.of(d.iterator()).filter(x -> filter.accept(x, getSession().getSession())).iterator();
                    return this;
                }
            }
            Iterator<NutsId> rr = xrepo.searchVersionsImpl(id, getFilter(), getSession());
            if (rr == null) {
                rr = IteratorUtils.emptyIterator();
            }
            result = rr;
            return this;
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "[ERROR  ] [{0}] {1} {2} {3}", new Object[]{CoreStringUtils.alignLeft(getSession().getFetchMode().toString(), 7), CoreStringUtils.alignLeft(getRepo().config().getName(), 20), CoreStringUtils.alignLeft("Fetch versions for", 24), id});
            }
            throw ex;
        }
    }

    @Override
    public Iterator<NutsId> getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

}
