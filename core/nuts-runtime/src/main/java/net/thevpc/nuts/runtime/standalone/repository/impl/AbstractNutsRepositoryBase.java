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
package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.log.NutsLogUtils;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.repository.cmd.deploy.DefaultNutsDeployRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.fetch.DefaultNutsFetchContentRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.fetch.DefaultNutsFetchDescriptorRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.push.DefaultNutsPushRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.search.DefaultNutsSearchRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.search.DefaultNutsSearchVersionsRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy.DefaultNutsRepositoryUndeployCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.updatestats.AbstractNutsUpdateRepositoryStatisticsCommand;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNutsRepositoryConfigModel;
import net.thevpc.nuts.spi.*;

import java.util.*;
import java.util.logging.Level;
import net.thevpc.nuts.runtime.standalone.xtra.glob.GlobUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsRepositoryConfigManagerExt;

/**
 * Created by vpc on 1/18/17.
 */
public abstract class AbstractNutsRepositoryBase extends AbstractNutsRepository implements NutsRepositoryExt {

    private static final long serialVersionUID = 1L;
    protected NutsIndexStore nutsIndexStore;

    private final NutsLogger LOG;

    public AbstractNutsRepositoryBase(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository, NutsSpeedQualifier speed, boolean supportedMirroring, String repositoryType,boolean supportsDeploy) {
        this.initSession=session;
        this.supportsDeploy=supportsDeploy;
        LOG = NutsLogger.of(AbstractNutsRepositoryBase.class,session);
        init(options, session, parentRepository, speed, supportedMirroring, repositoryType);
    }

    @Override
    public NutsIndexStore getIndexStore() {
        return nutsIndexStore;
    }

    protected void init(NutsAddRepositoryOptions options, NutsSession initSession, NutsRepository parent, NutsSpeedQualifier speed, boolean supportedMirroring, String repositoryType) {
        this.workspace = initSession.getWorkspace();
        this.parentRepository = parent;
        this.configModel = new DefaultNutsRepositoryConfigModel(this, options, initSession,speed, supportedMirroring, repositoryType);
        this.nutsIndexStore = initSession.config().getIndexStoreClientFactory().createIndexStore(this);
        setEnabled(options.isEnabled());
    }

    @Override
    public boolean acceptAction(NutsId id, NutsRepositorySupportedAction supportedAction, NutsFetchMode mode, NutsSession session) {
        String groups = config().getGroups();
        if (NutsBlankable.isBlank(groups)) {
            return true;
        }
        return GlobUtils.ofExact(groups).matcher(id.getGroupId()).matches();
    }

    @Override
    public String toString() {
        NutsRepositoryConfigManagerExt c = NutsRepositoryConfigManagerExt.of(config());
        String name = getName();
        String storePath = null;
        NutsRepositoryLocation loc = c.getModel().getLocation();
        String impl = getClass().getSimpleName();
        if (c != null) {
            storePath = c.getModel().getStoreLocation().toAbsolute().toString();
        }
        LinkedHashMap<String, String> a = new LinkedHashMap<>();
        if (name != null) {
            a.put("name", name);
        }
        a.put("impl", impl);
        if (storePath != null) {
            a.put("store", storePath);
        }
        if (loc != null) {
            a.put("location", loc.toString());
        }
        return a.toString();
    }

    @Override
    public void checkAllowedFetch(NutsId id, NutsSession session) {
    }

    @Override
    public NutsFetchDescriptorRepositoryCommand fetchDescriptor() {
        return new DefaultNutsFetchDescriptorRepositoryCommand(this);
    }

    @Override
    public NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
        Iterator<NutsId> allVersions = searchVersions().setSession(session).setId(id).setFilter(filter)
                .setFetchMode(fetchMode)
                .getResult();
        NutsId a = null;
        while (allVersions.hasNext()) {
            NutsId next = allVersions.next();
            if (a == null || next.getVersion().compareTo(a.getVersion()) > 0) {
                a = next;
            }
        }
        return a;
    }

    protected void traceMessage(NutsSession session, NutsFetchMode fetchMode, Level lvl, NutsId id, NutsLogVerb tracePhase, String title, long startTime, NutsMessage extraMessage) {
        NutsLogUtils.traceMessage(LOG, lvl, getName(), session, fetchMode, id, tracePhase, title, startTime, extraMessage);
    }

    @Override
    public NutsDeployRepositoryCommand deploy() {
        return new DefaultNutsDeployRepositoryCommand(this);
    }

    @Override
    public NutsPushRepositoryCommand push() {
        return new DefaultNutsPushRepositoryCommand(this);
    }

    @Override
    public NutsSearchRepositoryCommand search() {
        return new DefaultNutsSearchRepositoryCommand(this);
    }

    @Override
    public NutsFetchContentRepositoryCommand fetchContent() {
        return new DefaultNutsFetchContentRepositoryCommand(this);
    }

    @Override
    public NutsSearchVersionsRepositoryCommand searchVersions() {
        return new DefaultNutsSearchVersionsRepositoryCommand(this);
    }

    @Override
    public NutsRepositoryUndeployCommand undeploy() {
        return new DefaultNutsRepositoryUndeployCommand(this);
    }

    protected String getIdComponentExtension(String packaging, NutsSession session) {
        return session.locations().getDefaultIdContentExtension(packaging);
    }

    protected String getIdExtension(NutsId id, NutsSession session) {
        return session.locations().getDefaultIdExtension(id);
    }

    @Override
    public NutsPath getIdBasedir(NutsId id, NutsSession session) {
        return session.locations().setSession(session).getDefaultIdBasedir(id);
    }

    public NutsPath getIdRemotePath(NutsId id, NutsSession session) {
        return config().setSession(session).getLocationPath().resolve(getIdRelativePath(id, session));
    }

    protected NutsPath getIdRelativePath(NutsId id, NutsSession session) {
        return getIdBasedir(id, session).resolve(getIdFilename(id, session));
    }

    @Override
    public NutsUpdateRepositoryStatisticsCommand updateStatistics() {
        return new AbstractNutsUpdateRepositoryStatisticsCommand(this) {
            @Override
            public NutsUpdateRepositoryStatisticsCommand run() {
                return this;
            }
        };
    }

}
