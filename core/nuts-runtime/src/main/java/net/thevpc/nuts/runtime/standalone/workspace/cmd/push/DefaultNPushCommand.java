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
package net.thevpc.nuts.runtime.standalone.workspace.cmd.push;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringUtils;

/**
 *
 * @author thevpc
 */
public class DefaultNPushCommand extends AbstractDefaultNPushCommand {

    public DefaultNPushCommand(NSession session) {
        super(session);
    }

    @Override
    public NPushCommand run() {
        checkSession();
        NWorkspace ws = getSession().getWorkspace();
        NSession session = this.getSession();
        NRepositoryFilter repositoryFilter = null;
        Map<NId, NDefinition> toProcess = new LinkedHashMap<>();
        for (NId id : this.getIds()) {
            if (NStringUtils.trim(id.getVersion().getValue()).endsWith(CoreNConstants.Versions.CHECKED_OUT_EXTENSION)) {
                throw new NIllegalArgumentException(getSession(), NMsg.ofC("invalid version %s", id.getVersion()));
            }
            NDefinition file = NFetchCommand.of(id,session.copy().setTransitive(false)).setContent(true).getResultDefinition();
            NAssert.requireNonNull(file, "content to push", session);
            toProcess.put(id, file);
        }
        NWorkspaceExt dws = NWorkspaceExt.of(ws);
        NAssert.requireNonBlank(toProcess, "package tp push", session);
        for (Map.Entry<NId, NDefinition> entry : toProcess.entrySet()) {
            NId id = entry.getKey();
            NDefinition file = entry.getValue();
            NFetchMode fetchMode = this.isOffline() ? NFetchMode.LOCAL : NFetchMode.REMOTE;
            NWorkspaceUtils wu = NWorkspaceUtils.of(session);
            if (NBlankable.isBlank(this.getRepository())) {
                Set<String> errors = new LinkedHashSet<>();
                //TODO : CHECK ME, why offline?
                boolean ok = false;
                for (NRepository repo : wu.filterRepositoriesDeploy(file.getId(), repositoryFilter)) {
                    NDescriptor descr = null;
                    NRepositorySPI repoSPI = wu.repoSPI(repo);
                    try {
                        descr = repoSPI.fetchDescriptor().setSession(session).setFetchMode(fetchMode).setId(file.getId()).getResult();
                    } catch (Exception e) {
                        errors.add(CoreStringUtils.exceptionToString(e));
                        //
                    }
                    if (descr != null && repo.config().isSupportedMirroring()) {
                        NId id2 = CoreNIdUtils.createContentFaceId(dws.resolveEffectiveId(descr, session), descr,session);
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
                    throw new NPushException(session,id, NMsg.ofC(
                            "unable to push %s to repository %s : %s",
                            id == null ? "<null>" : id,
                            this.getRepository(),
                            String.join("\n", errors)
                            ));
                }
            } else {
                NRepository repo = NRepositories.of(session).findRepository(this.getRepository()).get();
                if (!repo.config().isEnabled()) {
                    throw new NIllegalArgumentException(getSession(), NMsg.ofC("repository %s is disabled", repo.getName()));
                }
                NId effId = CoreNIdUtils.createContentFaceId(id.builder().setPropertiesQuery("").build(), file.getDescriptor(),session) //                        .setAlternative(NutsUtilStrings.trim(file.getDescriptor().getAlternative()))
                        ;
                NRepositorySPI repoSPI = wu.repoSPI(repo);
                repoSPI.deploy().setSession(session)
                        .setId(effId)
                        .setContent(file.getContent().orNull())
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
