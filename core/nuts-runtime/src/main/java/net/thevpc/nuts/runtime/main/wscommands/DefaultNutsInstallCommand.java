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
package net.thevpc.nuts.runtime.main.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.NutsInstalledRepository;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.wscommands.AbstractNutsInstallCommand;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.log.NutsLogVerb;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.util.NutsCollectionResult;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.util.iter.IteratorUtils;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsInstallCommand extends AbstractNutsInstallCommand {

    public final NutsLogger LOG;

    public DefaultNutsInstallCommand(NutsWorkspace ws) {
        super(ws);
        LOG = ws.log().of(DefaultNutsInstallCommand.class);
    }

    private NutsDefinition _loadIdContent(NutsId id, NutsId forId, NutsSession session, boolean includeDeps, InstallIdList loaded, NutsInstallStrategy installStrategy) {
        NutsId longNameId = id.getLongNameId();
        InstallIdInfo def = loaded.get(longNameId);
        if (def != null) {
            if (forId != null) {
                def.forIds.add(forId);
            }
            if (def.definition != null) {
                return def.definition;
            }
        } else {
            def = loaded.addForInstall(id, installStrategy,false);
            def.extra = true;
            def.doRequire = true;
            if (forId != null) {
                def.forIds.add(forId);
            }
        }
        NutsSession ss = CoreNutsUtils.silent(session).copy();
        def.definition = ws.fetch().setId(id).setSession(ss)
                .setOptional(false)
                .setContent(true)
                .setEffective(true)
                .setDependencies(true)
                .setInstalled(null)
                .addScope(NutsDependencyScopePattern.RUN)
                .setFailFast(true)
                .getResultDefinition();
        if (def.definition.getInstallInformation() == null) {
            def.definition = ws.fetch().setId(id).setSession(ss)
                    .setOptional(false)
                    .setContent(true)
                    .setEffective(true)
                    .setDependencies(true)
                    .setInstalled(null)
                    .addScope(NutsDependencyScopePattern.RUN)
                    .setFailFast(true)
                    .getResultDefinition();
        }
        def.doRequire = true;
        if (includeDeps) {
            for (NutsDependency dependency : def.definition.getDependencies()) {
                _loadIdContent(dependency.toId(), id, session, includeDeps, loaded,NutsInstallStrategy.REQUIRE);
            }
        }
        return def.definition;
    }

    private boolean doThis(NutsId id, InstallIdList list, NutsSession session) {
        List<String> cmdArgs = new ArrayList<>(Arrays.asList(this.getArgs()));
        if (session.isForce()) {
            cmdArgs.add(0, "--force");
        }
        if (session.isTrace()) {
            cmdArgs.add(0, "--force");
            cmdArgs.add(0, "--trace");
        }

        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        InstallIdInfo info = list.get(id);
        if (info.doInstall) {
            _loadIdContent(info.id, null, session, true, list,info.strategy);
            if(info.definition!=null) {
                for (ConditionalArguments conditionalArgument : conditionalArguments) {
                    if (conditionalArgument.getPredicate().test(info.definition)){
                        cmdArgs.addAll(conditionalArgument.getArgs());
                    }
                }
            }
            dws.installImpl(info.definition, cmdArgs.toArray(new String[0]), null, session, info.doSwitchVersion);
            return true;
        } else if (info.doRequire) {
            _loadIdContent(info.id, null, session, true, list,info.strategy);
            dws.requireImpl(info.definition, session, info.doRequireDependencies, new NutsId[0]);
            return true;
        } else if (info.doSwitchVersion) {
            dws.getInstalledRepository().setDefaultVersion(info.id, session);
            return true;
        } else if (info.ignored) {
            return false;
        } else {
            throw new NutsUnexpectedException(getWorkspace(), "unexpected");
        }
    }

    @Override
    public NutsResultList<NutsDefinition> getResult() {
        if (result == null) {
            run();
        }
        return new NutsCollectionResult<NutsDefinition>(ws,
                ids.isEmpty() ? null : ids.get(0).toString(),
                Arrays.asList(result)
        );
    }

    @Override
    public NutsInstallCommand run() {
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = getSession();
        NutsSession searchSession = CoreNutsUtils.silent(session);
        PrintStream out = CoreIOUtils.resolveOut(session);
        ws.security().checkAllowed(NutsConstants.Permissions.INSTALL, "install");
//        LinkedHashMap<NutsId, Boolean> allToInstall = new LinkedHashMap<>();
        InstallIdList list = new InstallIdList(NutsInstallStrategy.INSTALL);
        for (Map.Entry<NutsId, NutsInstallStrategy> idAndStrategy : this.getIdMap().entrySet()) {
            if (!list.isVisited(idAndStrategy.getKey())) {
                List<NutsId> allIds = ws.search().addId(idAndStrategy.getKey()).setSession(searchSession).setLatest(true).getResultIds().list();
                if (allIds.isEmpty()) {
                    throw new NutsNotFoundException(ws, idAndStrategy.getKey());
                }
                for (NutsId id0 : allIds) {
                    list.addForInstall(id0, idAndStrategy.getValue(), false);
                }
            }
        }
        if (this.isCompanions()) {
            for (NutsId sid : ws.companionIds()) {
                if (!list.isVisited(sid)) {
                    List<NutsId> allIds = ws.search().addId(sid).setSession(searchSession).setLatest(true).setTargetApiVersion(ws.getApiVersion()).getResultIds().list();
                    if (allIds.isEmpty()) {
                        throw new NutsNotFoundException(ws, sid);
                    }
                    for (NutsId id0 : allIds) {
                        list.addForInstall(id0.builder().setNamespace(null).build(), this.getCompanions(), false);
                    }
                }
            }
        }
//        Map<NutsId, NutsDefinition> defsAll = new LinkedHashMap<>();
//        Map<NutsId, NutsDefinition> defsToInstall = new LinkedHashMap<>();
//        Map<NutsId, NutsDefinition> defsToInstallForced = new LinkedHashMap<>();
//        Map<NutsId, NutsDefinition> defsToDefVersion = new LinkedHashMap<>();
//        Map<NutsId, NutsDefinition> defsToIgnore = new LinkedHashMap<>();
//        Map<NutsId, NutsDefinition> defsOk = new LinkedHashMap<>();
        if (isInstalled()) {
            for (NutsId resultId : ws.search().setSession(searchSession).addInstallStatus(NutsInstallStatus.INSTALLED).getResultIds()) {
                list.addForInstall(resultId, getInstalled(), true);
            }
            // This bloc is to handle packages that were installed but their jar/content was removed for any reason!
            NutsInstalledRepository ir = dws.getInstalledRepository();
            for (NutsInstallInformation y : IteratorUtils.toList(ir.searchInstallInformation(session))) {
                if (y != null && y.getInstallStatus().contains(NutsInstallStatus.INSTALLED) && y.getId() != null) {
                    list.addForInstall(y.getId(), getInstalled(), true);
                }
            }
        }

        for (InstallIdInfo info : list.infos()) {
            NutsId nid = info.id;
            info.oldInstallStatus = dws.getInstalledRepository().getInstallStatus(nid, session);
//            boolean _installed = installStatus.contains(NutsInstallStatus.INSTALLED);
//            boolean _defVer = dws.getInstalledRepository().isDefaultVersion(nid, session);


//            if (defsToInstallForced.containsKey(nid)) {
//                _installed = true;
//            }
//            boolean nForced = session.isForce() || nutsIdBooleanEntry.getValue();
            //must load dependencies because will be run later!!

            NutsInstallStrategy strategy = getStrategy();
            if (strategy == null) {
                strategy = NutsInstallStrategy.DEFAULT;
            }
            if (strategy == NutsInstallStrategy.DEFAULT) {
                strategy = NutsInstallStrategy.INSTALL;
            }
            if (info.isOldInstallStatus(NutsInstallStatus.NOT_INSTALLED)) {
                switch (strategy) {
                    case REQUIRE: {
                        info.doRequire = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case INSTALL: {
                        info.doInstall = true;
                        info.doRequireDependencies = true;
                        info.doSwitchVersion = true;
                        break;
                    }
                    case REINSTALL: {
                        info.doInstall = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case REPAIR: {
                        info.doError = "cannot repair non installed artifact";
                        break;
                    }
                    case SWITCH_VERSION: {
                        info.doError = "cannot switch version for non installed artifact";
                        break;
                    }
                    default: {
                        throw new NutsUnexpectedException(ws, "unsupported strategy " + strategy);
                    }
                }
            } else if (info.isOldInstallStatus(NutsInstallStatus.OBSOLETE)) {
                switch (strategy) {
                    case REQUIRE: {
                        info.doRequire = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case INSTALL: {
                        info.doInstall = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case REINSTALL: {
                        info.doInstall = info.isOldInstallStatus(NutsInstallStatus.INSTALLED);
                        info.doRequire = !info.doInstall;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case REPAIR: {
                        info.doRequire = true;
                        break;
                    }
                    case SWITCH_VERSION: {
                        if (info.isOldInstallStatus(NutsInstallStatus.INSTALLED)) {
                            info.doSwitchVersion = true;
                        } else {
                            info.doError = "cannot switch version for non installed artifact";
                        }
                        break;
                    }
                    default: {
                        throw new NutsUnexpectedException(ws, "unsupported strategy " + strategy);
                    }
                }
            } else if (info.isOldInstallStatus(NutsInstallStatus.INSTALLED)) {
                switch (strategy) {
                    case REQUIRE: {
                        info.doRequire = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case INSTALL: {
                        info.ignored = true;
                        break;
                    }
                    case REINSTALL: {
                        info.doInstall = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case REPAIR: {
                        info.doRequire = true;
                        break;
                    }
                    case SWITCH_VERSION: {
                        info.doSwitchVersion = true;
                        break;
                    }
                    default: {
                        throw new NutsUnexpectedException(ws, "unsupported strategy " + strategy);
                    }
                }
            } else if (info.isOldInstallStatus(NutsInstallStatus.REQUIRED)) {
                switch (strategy) {
                    case REQUIRE: {
                        info.doRequire = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case INSTALL: {
                        info.doInstall = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case REINSTALL: {
                        info.doRequire = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case REPAIR: {
                        info.doRequire = true;
                        break;
                    }
                    case SWITCH_VERSION: {
                        info.doError = "cannot switch version for non installed artifact";
                        break;
                    }
                    default: {
                        throw new NutsUnexpectedException(ws, "unsupported strategy " + strategy);
                    }
                }
            } else {
                throw new NutsUnexpectedException(ws, "unsupported status " + info.oldInstallStatus);
            }
        }
        Map<String, List<InstallIdInfo>> error = list.infos().stream().filter(x -> x.doError != null).collect(Collectors.groupingBy(installIdInfo -> installIdInfo.doError));
        if (error.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<InstallIdInfo>> stringListEntry : error.entrySet()) {
                out.println("the following " + (stringListEntry.getValue().size() > 1 ? "artifacts are" : "artifact is") + " cannot be @@installed@@ (" + stringListEntry.getKey() + ") : "
                        + stringListEntry.getValue().stream().map(x -> x.id)
                        .map(x -> ws.id().formatter().omitImportedGroupId().value(x.getLongNameId()).format())
                        .collect(Collectors.joining(", ")));
                sb.append("\n" + "the following ").append(stringListEntry.getValue().size() > 1 ? "artifacts are" : "artifact is").append(" cannot be installed (").append(stringListEntry.getKey()).append(") : ").append(stringListEntry.getValue().stream().map(x -> x.id)
                        .map(x -> ws.id().formatter().omitImportedGroupId().value(x.getLongNameId()).format())
                        .collect(Collectors.joining(", ")));
            }
            throw new NutsInstallException(getWorkspace(), "", sb.toString().trim(), null);
        }

        if (getSession().isPlainTrace() || (!list.emptyCommand && getSession().getConfirm() == NutsConfirmationMode.ASK)) {
            printList(out, "###new###", "##installed##", list.ids(x -> x.doInstall && !x.isAlreadyExists()));
            printList(out, "###new###", "##required##", list.ids(x -> x.doRequire && !x.doInstall && !x.isAlreadyExists()));
            printList(out, "###required###", "##re-required##", list.ids(x -> (!x.doInstall && x.doRequire) && x.isAlreadyRequired()));
            printList(out, "###required###", "##installed##", list.ids(x -> x.doInstall && x.isAlreadyRequired() && !x.isAlreadyInstalled()));
            printList(out, "###installed###", "##re-reinstalled##", list.ids(x -> x.doInstall && x.isAlreadyInstalled()));
            printList(out, "###installed###", "####set as default####", list.ids(x -> x.doSwitchVersion && x.isAlreadyInstalled()));
            printList(out, "###installed###", "########ignored########", list.ids(x -> x.ignored));
        }
        if (!list.ids(x -> !x.ignored).isEmpty() && !ws.io().term().getTerminal().ask().forBoolean("should we proceed?")
                .defaultValue(true)
                .setSession(session).getBooleanValue()) {
            throw new NutsUserCancelException(ws,"installation cancelled");
        }
//        List<String> cmdArgs = new ArrayList<>(Arrays.asList(this.getArgs()));
//        if (session.isForce()) {
//            cmdArgs.add(0, "--force");
//        }
//        if (session.isTrace()) {
//            cmdArgs.add(0, "--force");
//            cmdArgs.add(0, "--trace");
//        }
        List<NutsDefinition> resultList = new ArrayList<>();
        List<NutsId> failedList = new ArrayList<>();
        try {
            if (!list.ids(x -> !x.ignored).isEmpty()) {
                for (InstallIdInfo info : list.infos(x -> !x.ignored)) {
                    try {
                        if (doThis(info.id, list, session)) {
                            resultList.add(info.definition);
                        }
                    } catch (RuntimeException ex) {
                        LOG.with().error(ex).verb(NutsLogVerb.WARNING).level(Level.FINE).log("failed to install " + ws.id().formatter(info.id).format());
                        failedList.add(info.id);
                        if (session.isPlainTrace()) {
                            if (!ws.io().term().getTerminal().ask().forBoolean("@@failed to install@@ " + ws.id().formatter(info.id).format() + " and its dependencies... Continue installation?")
                                    .defaultValue(true)
                                    .setSession(session).getBooleanValue()) {
                                session.out().printf(ws.id().formatter(info.id).omitNamespace().format() + " @@installation cancelled with error:@@ %s%n", CoreStringUtils.exceptionToString(ex));
                                result = new NutsDefinition[0];
                                return this;
                            } else {
                                session.out().printf(ws.id().formatter(info.id).omitNamespace().format() + " @@installation cancelled with error:@@ %s%n", CoreStringUtils.exceptionToString(ex));
                            }
                        } else {
                            throw ex;
                        }
                    }
                }
            }
        } finally {
            result = resultList.toArray(new NutsDefinition[0]);
            failed = failedList.toArray(new NutsId[0]);
        }
        if (list.emptyCommand) {
            throw new NutsExecutionException(ws, "Missing components to install", 1);
        }
        return this;
    }

    private void printList(PrintStream out, String kind, String action, List<NutsId> all) {
        if (all.size() > 0) {
            out.println("the following " + kind + " " + (all.size() > 1 ? "artifacts are" : "artifact is") + " going to be " + action + " : "
                    + all.stream()
                    .map(x -> ws.id().formatter().omitImportedGroupId().value(x.getLongNameId()).format())
                    .collect(Collectors.joining(", ")));
        }
    }

    private static class InstallIdInfo {
        boolean extra;
        String sid;
        NutsId id;
        boolean forced;
        boolean doRequire;
        boolean doRequireDependencies;
        boolean doInstall;
        boolean ignored;
        boolean doSwitchVersion;
        NutsInstallStrategy strategy;
        String doError;
        Set<NutsInstallStatus> oldInstallStatus;
        Set<NutsId> forIds = new HashSet<>();
        NutsDefinition definition;

        public boolean isOldInstallStatus(NutsInstallStatus o0, NutsInstallStatus... o) {
            if (!oldInstallStatus.contains(o0)) {
                return false;
            }
            for (NutsInstallStatus s : o) {
                if (!oldInstallStatus.contains(s)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isAlreadyRequired() {
            return oldInstallStatus.contains(NutsInstallStatus.REQUIRED);
        }

        public boolean isAlreadyInstalled() {
            return oldInstallStatus.contains(NutsInstallStatus.INSTALLED);
        }

        public boolean isAlreadyExists() {
            return oldInstallStatus.contains(NutsInstallStatus.INSTALLED)
                    || oldInstallStatus.contains(NutsInstallStatus.REQUIRED);
        }
    }

    private static class InstallIdList {
        boolean emptyCommand = true;
        NutsInstallStrategy defaultStrategy;
        Map<String, InstallIdInfo> visited = new LinkedHashMap<>();

        public InstallIdList(NutsInstallStrategy defaultStrategy) {
            this.defaultStrategy = defaultStrategy;
        }

        public boolean isVisited(NutsId id) {
            return visited.containsKey(normalizeId(id));
        }

        private String normalizeId(NutsId id) {
            return id.builder().setNamespace(null).setProperty("optional",null).build().toString();
        }

        public List<NutsId> ids(Predicate<InstallIdInfo> filter) {
            return infos().stream().filter(filter).map(x -> x.id).collect(Collectors.toList());
        }

        public List<InstallIdInfo> infos(Predicate<InstallIdInfo> filter) {
            if (filter == null) {
                return infos();
            }
            return infos().stream().filter(filter).collect(Collectors.toList());
        }

        public List<InstallIdInfo> infos() {
            return new ArrayList<>(visited.values());
        }

        public InstallIdInfo addForInstall(NutsId id, NutsInstallStrategy strategy, boolean forced) {
            emptyCommand = false;
            InstallIdInfo ii = new InstallIdInfo();
            ii.forced = forced;
            ii.id = id;
            ii.sid = normalizeId(id);
            if (strategy == null) {
                strategy = NutsInstallStrategy.DEFAULT;
            }
            if (strategy == NutsInstallStrategy.DEFAULT) {
                strategy = defaultStrategy;
            }
            ii.strategy = strategy;
            visited.put(normalizeId(id), ii);
            return ii;
        }

        public InstallIdInfo get(NutsId id) {
            return visited.get(normalizeId(id));
        }
    }
}
