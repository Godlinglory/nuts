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
package net.vpc.app.nuts.core.archetypes;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

/**
 * Created by vpc on 1/23/17.
 */
public class ServerNutsWorkspaceArchetypeComponent implements NutsWorkspaceArchetypeComponent {

    @Override
    public String getName() {
        return "server";
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public void initialize(NutsWorkspace workspace, NutsSession session) {
        NutsRepository defaultRepo = workspace.config().addRepository(
                new NutsCreateRepositoryOptions()
                        .setName(NutsConstants.Names.DEFAULT_REPOSITORY_NAME)
                        .setLocation(NutsConstants.Names.DEFAULT_REPOSITORY_NAME)
                        .setEnabled(true)
                        .setFailSafe(false)
                        .setCreate(true)
                        .setConfig(new NutsRepositoryConfig()
                                .setName(NutsConstants.Names.DEFAULT_REPOSITORY_NAME)
                                .setType(NutsConstants.RepoTypes.NUTS)
                        )
        );
        if (defaultRepo == null) {
            throw new NutsIllegalArgumentException("Unable to configure repository : " + NutsConstants.Names.DEFAULT_REPOSITORY_NAME);
        }

        //has read rights
        workspace.security().addUser("guest").credentials("user").rights(
                NutsConstants.Rights.FETCH_DESC,
                NutsConstants.Rights.FETCH_CONTENT,
                NutsConstants.Rights.DEPLOY
        ).run();

        //has write rights
        workspace.security().addUser("contributor").credentials("user").rights(
                NutsConstants.Rights.FETCH_DESC,
                NutsConstants.Rights.FETCH_CONTENT,
                NutsConstants.Rights.DEPLOY,
                NutsConstants.Rights.UNDEPLOY
        ).run();
    }
}
