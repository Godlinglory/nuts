/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.main.wscommands;

import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.AbstractDefaultNutsPushCommand;
import net.thevpc.nuts.runtime.standalone.CoreNutsConstants;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsFetchMode;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsPushCommand;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.NutsRepositoryFilter;
import net.thevpc.nuts.NutsRepositoryNotFoundException;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.spi.NutsRepositorySPI;

/**
 *
 * @author thevpc
 */
public class DefaultNutsPushCommand extends AbstractDefaultNutsPushCommand {


    public DefaultNutsPushCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsPushCommand run() {
        NutsSession session = this.getValidWorkspaceSession();
        NutsRepositoryFilter repositoryFilter = null;
        Map<NutsId, NutsDefinition> toProcess = new LinkedHashMap<>();
        for (NutsId id : this.getIds()) {
            if (CoreStringUtils.trim(id.getVersion().getValue()).endsWith(CoreNutsConstants.Versions.CHECKED_OUT_EXTENSION)) {
                throw new NutsIllegalArgumentException(ws, "invalid Version " + id.getVersion());
            }
            NutsDefinition file = ws.fetch().setId(id).setSession(session).setContent(true).setTransitive(false).getResultDefinition();
            if (file == null) {
                throw new NutsIllegalArgumentException(ws, "nothing to push");
            }
            toProcess.put(id, file);
        }
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        if (toProcess.isEmpty()) {
            throw new NutsIllegalArgumentException(ws, "missing component to push");
        }
        for (Map.Entry<NutsId, NutsDefinition> entry : toProcess.entrySet()) {
            NutsId id = entry.getKey();
            NutsDefinition file = entry.getValue();
            NutsFetchMode fetchMode = this.isOffline() ? NutsFetchMode.LOCAL : NutsFetchMode.REMOTE;
            if (CoreStringUtils.isBlank(this.getRepository())) {
                Set<String> errors = new LinkedHashSet<>();
                //TODO : CHECK ME, why offline?
                boolean ok = false;
                for (NutsRepository repo : NutsWorkspaceUtils.of(ws).filterRepositoriesDeploy(file.getId(), repositoryFilter, session)) {
                    NutsDescriptor descr = null;
                    NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(ws).repoSPI(repo);
                    try {
                        descr = repoSPI.fetchDescriptor().setSession(session).setFetchMode(fetchMode).setId(file.getId()).getResult();
                    } catch (Exception e) {
                        errors.add(CoreStringUtils.exceptionToString(e));
                        //
                    }
                    if (descr != null && repo.config().isSupportedMirroring()) {
                        NutsId id2 = ws.config().createContentFaceId(dws.resolveEffectiveId(descr, session), descr);
                        try {
                            repoSPI.push().setId(id2)
                                    .setOffline(offline)
                                    .setRepository(getRepository())
                                    .setArgs(args.toArray(new String[0]))
                                    .setSession(session)
//                                    .setFetchMode(fetchMode)
                                    .run();
                            ok = true;
                            break;
                        } catch (Exception e) {
                            errors.add(CoreStringUtils.exceptionToString(e));
                            //
                        }
                    }
                }
                if (!ok) {
                    throw new NutsRepositoryNotFoundException(ws, this.getRepository() + " : " + String.join("\n", errors));
                }
            } else {
                NutsRepository repo = ws.repos().getRepository(this.getRepository(), session);
                if (!repo.config().isEnabled()) {
                    throw new NutsIllegalArgumentException(ws, "repository " + repo.getName() + " is disabled");
                }
                NutsId effId = ws.config().createContentFaceId(id.builder().setProperties("").build(), file.getDescriptor())
//                        .setAlternative(CoreStringUtils.trim(file.getDescriptor().getAlternative()))
                        ;
                NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(ws).repoSPI(repo);
                repoSPI.deploy().setSession(session)
                        .setId(effId)
                        .setContent(file.getPath())
                        .setDescriptor(file.getDescriptor())
//                        .setFetchMode(fetchMode)
//                        .setOffline(this.isOffline())
//                        .setTransitive(true)
                        .run();
            }
        }
        return this;
    }
}
