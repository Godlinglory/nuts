/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.deploy;

import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NutsPositionType;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsRepositoryExt;
import net.thevpc.nuts.spi.NutsDeployRepositoryCommand;
import net.thevpc.nuts.util.NutsStringUtils;

/**
 *
 * @author thevpc %category SPI Base
 */
public class DefaultNutsDeployRepositoryCommand extends AbstractNutsDeployRepositoryCommand {

    private NutsLogger LOG;

    public DefaultNutsDeployRepositoryCommand(NutsRepository repo) {
        super(repo);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsDeployRepositoryCommand.class,session);
        }
        return LOG;
    }

    @Override
    public NutsDeployRepositoryCommand run() {
        NutsSession session = getSession();
        getRepo().security().setSession(getSession()).checkAllowed(NutsConstants.Permissions.DEPLOY, "deploy");
        checkParameters();
        try {
            NutsRepositoryExt xrepo = NutsRepositoryExt.of(repo);
            NutsDescriptor rep = xrepo.deployImpl(this);
            this.setDescriptor(rep);
            this.setId(rep.getId());
            if (session.isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled()) {
                try {
                    xrepo.getIndexStore().revalidate(this.getId(), session);
                } catch (NutsException ex) {
                    _LOGOP(session).level(Level.FINEST).verb(NutsLoggerVerb.FAIL)
                            .log(NutsMessage.ofJstyle("error revalidating Indexer for {0} : {1}", getRepo().getName(), ex));
                }
            }
            _LOGOP(session).level(Level.FINEST).verb(NutsLoggerVerb.SUCCESS)
                    .log(NutsMessage.ofJstyle("{0} deploy {1}", NutsStringUtils.formatAlign(getRepo().getName(), 20, NutsPositionType.FIRST), this.getId()));
        } catch (RuntimeException ex) {
            _LOGOP(session).level(Level.FINEST).verb(NutsLoggerVerb.FAIL)
                    .log(NutsMessage.ofJstyle("{0} deploy {1}", NutsStringUtils.formatAlign(getRepo().getName(), 20,NutsPositionType.FIRST), this.getId()));
            throw ex;
        }
        return this;
    }

}
