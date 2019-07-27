/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.impl.def.repocommands;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsContent;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsFetchContentRepositoryCommand;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.core.repocommands.AbstractNutsFetchContentRepositoryCommand;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.TraceResult;

/**
 *
 * @author vpc
 */
public class DefaultNutsFetchContentRepositoryCommand extends AbstractNutsFetchContentRepositoryCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsFetchContentRepositoryCommand.class.getName());

    public DefaultNutsFetchContentRepositoryCommand(NutsRepository repo) {
        super(repo);
    }

    @Override
    public NutsFetchContentRepositoryCommand run() {
        NutsWorkspaceUtils.checkSession(getRepo().getWorkspace(), getSession());
        NutsDescriptor descriptor0 = descriptor;
        if (descriptor0 == null) {
            descriptor0 = getRepo().fetchDescriptor().setId(id).setSession(getSession()).getResult();
        }
        id = id.setFaceContent();
        getRepo().security().checkAllowed(NutsConstants.Permissions.FETCH_CONTENT, "fetch-content");
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        xrepo.checkAllowedFetch(id, getSession());
        long startTime = System.currentTimeMillis();
        try {
            NutsContent f = xrepo.fetchContentImpl(id, descriptor0, localPath, getSession());
            if (f == null) {
                throw new NutsNotFoundException(getRepo().getWorkspace(), id);
            }
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(LOG, getRepo().config().name(), getSession(), id.getLongNameId(), TraceResult.SUCCESS, "Fetch component", startTime);
            }
            result = f;
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(LOG, getRepo().config().name(), getSession(), id.getLongNameId(), TraceResult.ERROR, "Fetch component", startTime);
            }
            throw ex;
        }
        return this;
    }

    @Override
    public NutsFetchContentRepositoryCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsId getId() {
        return id;
    }

}
