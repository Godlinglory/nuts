/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
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
package net.thevpc.nuts.runtime.standalone.workspace.archetype;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.DefaultNutsRepositoryDB;
import net.thevpc.nuts.spi.NutsRepositoryURL;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsWorkspaceArchetypeComponent;

/**
 * Created by vpc on 1/23/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class MinimalNutsWorkspaceArchetypeComponent implements NutsWorkspaceArchetypeComponent {
    private NutsLogger LOG;

    @Override
    public String getName() {
        return "minimal";
    }

    @Override
    public void initializeWorkspace(NutsSession session) {
        this.LOG = NutsLogger.of(MinimalNutsWorkspaceArchetypeComponent.class, session);
//        NutsWorkspace ws = session.getWorkspace();

        DefaultNutsWorkspaceConfigManager rm = (DefaultNutsWorkspaceConfigManager) session.config();
//        defaults.put(NutsConstants.Names.DEFAULT_REPOSITORY_NAME, null);
        NutsRepositoryURL[] br = rm.getModel().resolveBootRepositoriesList(session).resolve(
                new NutsRepositoryURL[0], DefaultNutsRepositoryDB.INSTANCE);
        NutsRepositoryManager repos = session.repos().setSession(session);
        for (NutsRepositoryURL s : br) {
            repos.addRepository(s.toString());
        }
        //simple rights for minimal utilization
        NutsUpdateUserCommand uu = session.security().setSession(session)
                .updateUser(NutsConstants.Users.ANONYMOUS);
//        for (String right : NutsConstants.Rights.RIGHTS) {
//            if (!NutsConstants.Rights.ADMIN.equals(right)) {
//                uu.addRights(right);
//            }
//        }
        uu.run();
    }

    @Override
    public void startWorkspace(NutsSession session) {
        NutsBootManager boot = session.boot();
        boolean initializeAllPlatforms = boot.getBootCustomBoolArgument(true,true,false,"---init-platforms");
        if (initializeAllPlatforms && boot.getBootCustomBoolArgument(true,true,false,"---init-java")) {
            NutsWorkspaceUtils.of(session).installAllJVM();
        } else {
            NutsWorkspaceUtils.of(session).installCurrentJVM();
        }
        Boolean initScripts = boot.getBootCustomBoolArgument(true,true,false,"---init-scripts");
        Boolean initLaunchers = boot.getBootCustomBoolArgument(true,true,false,"---init-launchers");
        if (initScripts || initLaunchers) {
            NutsWorkspaceUtils.of(session).installLaunchers(initLaunchers);
        }
        Boolean skipCompanions = session.boot().getBootOptions().getSkipCompanions();
        if (skipCompanions == null) {
            skipCompanions = true;
        }
        if (!skipCompanions) {
            NutsWorkspaceUtils.of(session).installCompanions();
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        return DEFAULT_SUPPORT + 1;
    }
}
