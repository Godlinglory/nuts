/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy;

import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NLogOp;
import net.thevpc.nuts.util.NLogVerb;
import net.thevpc.nuts.spi.NRepositoryUndeployCommand;
import net.thevpc.nuts.util.NStringUtils;

/**
 *
 * @author thevpc %category SPI Base
 */
public class DefaultNRepositoryUndeployCommand extends AbstractNRepositoryUndeployCommand {

    private NLog LOG;

    public DefaultNRepositoryUndeployCommand(NRepository repo) {
        super(repo);
    }

    public DefaultNRepositoryUndeployCommand(NWorkspace ws) {
        super(null);
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultNRepositoryUndeployCommand.class,session);
        }
        return LOG;
    }

    @Override
    public NRepositoryUndeployCommand run() {
        NSession session = getSession();
        NSessionUtils.checkSession(getRepo().getWorkspace(), session);
        getRepo().security().setSession(session).checkAllowed(NConstants.Permissions.UNDEPLOY, "undeploy");
        try {
            NRepositoryExt xrepo = NRepositoryExt.of(getRepo());
            xrepo.undeployImpl(this);
            if (session.isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled()) {
                try {
                    xrepo.getIndexStore().invalidate(this.getId(), session);
                } catch (NException ex) {
                    _LOGOP(session).level(Level.FINEST).verb(NLogVerb.FAIL).log(
                            NMsg.ofJ("error invalidating Indexer for {0} : {1}", getRepo().getName(), ex));
                }
            }
            _LOGOP(session).level(Level.FINEST).verb(NLogVerb.SUCCESS)
                    .log(NMsg.ofJ("{0} undeploy {1}", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST), this.getId()));
        } catch (RuntimeException ex) {
            _LOGOP(session).level(Level.FINEST).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("{0} undeploy {1}", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST), this.getId()));
        }
        return this;
    }

}
