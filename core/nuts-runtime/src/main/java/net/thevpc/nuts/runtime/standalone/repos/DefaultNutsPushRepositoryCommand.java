/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repos;

import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsPushRepositoryCommand;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.repocommands.AbstractNutsPushRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 *
 * @author thevpc %category SPI Base
 */
public class DefaultNutsPushRepositoryCommand extends AbstractNutsPushRepositoryCommand {

    private NutsLogger LOG;

    public DefaultNutsPushRepositoryCommand(NutsRepository repo) {
        super(repo);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = session.log().of(DefaultNutsPushRepositoryCommand.class);
        }
        return LOG;
    }

    @Override
    public NutsPushRepositoryCommand run() {
        NutsSession session = getSession();
        NutsWorkspaceUtils.checkSession(getRepo().getWorkspace(), session);
        getRepo().security().setSession(session).checkAllowed(NutsConstants.Permissions.PUSH, "push");
        try {
            NutsRepositoryExt.of(getRepo()).pushImpl(this);
                _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.SUCCESS)
                        .log(NutsMessage.jstyle("{0} push {1}", CoreStringUtils.alignLeft(getRepo().getName(), 20), getId()));
        } catch (RuntimeException ex) {

            if (_LOG(session).isLoggable(Level.FINEST)) {
                _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.FAIL)
                        .log(NutsMessage.jstyle("{0} push {1}", CoreStringUtils.alignLeft(getRepo().getName(), 20), getId()));
            }
        }
        return this;
    }
}
