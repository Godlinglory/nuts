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
package net.thevpc.nuts.runtime.core.format;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.repos.NutsInstalledRepository;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;
import net.thevpc.nuts.runtime.core.util.CoreEnumUtils;

/**
 *
 * @author thevpc
 */
public class NutsIdFormatHelper {

    private NutsLogger LOG;
    NutsId id;
    NutsInstallStatus installStatus = NutsInstallStatus.NONE;
    Boolean executable = null;
    Boolean executableApp = null;
    boolean fetched = false;
    boolean checkDependencies = false;
    NutsDefinition defFetched;
    NutsDefinition def;
    NutsDescriptor desc;
    NutsDependency dep;
    NutsSession session;
    Instant dte;
    String usr;
    char status_f;
//    char status_obs;
    char status_e;
    char status_i;
    char status_s;
    char status_o;
    //    String display;
    boolean built = false;
    boolean ntf = true;

    public static NutsIdFormatHelper of(Object object, NutsSession session) {
        if (object instanceof NutsId) {
            NutsId v = (NutsId) object;
            return (new NutsIdFormatHelper(v, session));
        } else if (object instanceof NutsDescriptor) {
            NutsDescriptor v = (NutsDescriptor) object;
            return (new NutsIdFormatHelper(v, session));
        } else if (object instanceof NutsDefinition) {
            NutsDefinition v = (NutsDefinition) object;
            return (new NutsIdFormatHelper(v, session));
        } else if (object instanceof NutsDependency) {
            NutsDependency v = (NutsDependency) object;
            return (new NutsIdFormatHelper(v, session));
        } else if (object instanceof NutsDependencyTreeNode) {
            NutsDependencyTreeNode v = (NutsDependencyTreeNode) object;
            return (new NutsIdFormatHelper(v, session));
        } else {
            return null;
        }

    }

    public NutsIdFormatHelper(NutsDependencyTreeNode id, NutsSession session) {
        this(null, null, null, id.getDependency(), session);
    }

    public NutsIdFormatHelper(NutsId id, NutsSession session) {
        this(id, null, null, null, session);
    }

    public NutsIdFormatHelper(NutsDescriptor desc, NutsSession session) {
        this(null, desc, null, null, session);
    }

    public NutsIdFormatHelper(NutsDefinition def, NutsSession session) {
        this(null, null, def, null, session);
    }

    public NutsIdFormatHelper(NutsDependency dep, NutsSession session) {
        this(null, null, null, dep, session);
    }

    private NutsIdFormatHelper(NutsId id, NutsDescriptor desc, NutsDefinition def, NutsDependency dep, NutsSession session) {
        LOG = session.getWorkspace().log().of(NutsIdFormatHelper.class);
        if (id == null) {
            if (def != null) {
                id = def.getId();
            } else if (desc != null) {
                id = desc.getId();
            } else if (dep != null) {
                id = dep.toId();
            }
        }
        if (desc == null) {
            if (def != null) {
                desc = def.getDescriptor();
            }
        }
        this.session = session;
        this.id = id;
        this.def = def;
        this.dep = dep;
        this.desc = desc;
    }

    public String[] getMultiColumnRowStrings(NutsFetchDisplayOptions oo) {
        Object[] oa = getMultiColumnRow(oo);
        String[] ss = new String[oa.length];
        for (int i = 0; i < oa.length; i++) {
            ss[i] = String.valueOf(oa[i]);
        }
        return ss;
    }

    public Object[] getMultiColumnRow(NutsFetchDisplayOptions oo) {
        NutsDisplayProperty[] a = oo.getDisplayProperties();
        Object[] b = new Object[a.length];
        for (int j = 0; j < b.length; j++) {
            b[j] = buildMain(oo, a[j]);
        }
        return b;
    }

    private static FormatHelper getFormatHelper(NutsSession session) {
        FormatHelper h = (FormatHelper) session.getWorkspace().env().getProperty(FormatHelper.class.getName());
        if (h != null) {
            return h;
        }
        FormatHelperResetListener h2 = (FormatHelperResetListener) session.getWorkspace().env().getProperty(FormatHelperResetListener.class.getName());
        if (h2 == null) {
            h2 = new FormatHelperResetListener();
            session.getWorkspace().events().addWorkspaceListener(h2);
        }
        h = new FormatHelper(session);
        session.getWorkspace().env().setProperty(FormatHelper.class.getName(), h);
        return h;
    }

    public static class FormatHelperResetListener implements NutsWorkspaceListener, NutsRepositoryListener {

        private void _onReset(NutsWorkspace ws) {
            ws.env().setProperty(FormatHelper.class.getName(), null);
        }

        @Override
        public void onAddRepository(NutsWorkspaceEvent event) {
            _onReset(event.getWorkspace());
        }

        @Override
        public void onRemoveRepository(NutsWorkspaceEvent event) {
            _onReset(event.getWorkspace());
        }

        @Override
        public void onReloadWorkspace(NutsWorkspaceEvent event) {
            _onReset(event.getWorkspace());
        }

        @Override
        public void onCreateWorkspace(NutsWorkspaceEvent event) {
            _onReset(event.getWorkspace());
        }

        @Override
        public void onUpdateProperty(NutsWorkspaceEvent event) {

        }

        @Override
        public void onAddRepository(NutsRepositoryEvent event) {
            _onReset(event.getWorkspace());
        }

        @Override
        public void onRemoveRepository(NutsRepositoryEvent event) {
            _onReset(event.getWorkspace());
        }

        @Override
        public void onConfigurationChanged(NutsRepositoryEvent event) {
            _onReset(event.getWorkspace());
        }

        @Override
        public void onConfigurationChanged(NutsWorkspaceEvent event) {
            _onReset(event.getWorkspace());
        }
    }

    public static class FormatHelper {

        NutsSession session;

        public FormatHelper(NutsSession session) {
            this.session = session;
        }

        private Integer maxRepoNameSize;
        private Integer maxUserNameSize;

        public int maxRepoNameSize() {
            if (maxRepoNameSize != null) {
                return maxRepoNameSize;
            }
            int z = 0;
            Stack<NutsRepository> stack = new Stack<>();
            for (NutsRepository repository : session.getWorkspace().repos()
                    .setSession(session)
                    .getRepositories()) {
                stack.push(repository);
            }
            while (!stack.isEmpty()) {
                NutsRepository r = stack.pop();
                int n = r.getName().length();
                if (n > z) {
                    z = n;
                }
                if (r.config().isSupportedMirroring()) {
                    for (NutsRepository repository : r.config()
                            .setSession(session)
                            .getMirrors()) {
                        stack.push(repository);
                    }
                }
            }
            return maxRepoNameSize = z;
        }

        public int maxUserNameSize() {
            if (maxUserNameSize != null) {
                return maxUserNameSize;
            }
            int z = "anonymous".length();
            NutsWorkspaceConfigManagerExt wc = NutsWorkspaceConfigManagerExt.of(session.getWorkspace().config());
            NutsUserConfig[] users = wc.getModel().getStoredConfigSecurity().getUsers();
            if (users != null) {
                for (NutsUserConfig user : users) {
                    String s = user.getUser();
                    if (s != null) {
                        z = Math.max(s.length(), z);
                    }
                }
            }
            Stack<NutsRepository> stack = new Stack<>();
            for (NutsRepository repository : session.getWorkspace().repos().getRepositories()) {
                stack.push(repository);
            }
            while (!stack.isEmpty()) {
                NutsRepository r = stack.pop();
                NutsRepositoryConfigManagerExt rc = NutsRepositoryConfigManagerExt.of(r.config());
                NutsUserConfig[] users1 = rc.getModel().getUsers(session);
                if (users1 != null) {
                    for (NutsUserConfig user : users1) {
                        String s = user.getUser();
                        if (s != null) {
                            z = Math.max(s.length(), z);
                        }
                    }
                }
                if (r.config().isSupportedMirroring()) {
                    for (NutsRepository repository : r.config().setSession(session).getMirrors()) {
                        stack.push(repository);
                    }
                }
            }
            return maxUserNameSize = z;
        }
    }

    public NutsString getSingleColumnRow(NutsFetchDisplayOptions oo) {
        NutsDisplayProperty[] a = oo.getDisplayProperties();
        NutsFormatManager txt = session.getWorkspace().formats();
        NutsTextNodeBuilder sb = txt.text().builder();
        for (int j = 0; j < a.length; j++) {
            NutsString s = buildMain(oo, a[j]);
            int z = 0;
            switch (a[j]) {
                case INSTALL_DATE: {
                    z = CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER_LENGTH;
                    break;
                }
                case REPOSITORY: {
                    z = getFormatHelper(session).maxRepoNameSize();
                    break;
                }
                case REPOSITORY_ID: {
                    z = CoreNutsUtils.DEFAULT_UUID_LENGTH;
                    break;
                }
                case INSTALL_USER: {
                    z = getFormatHelper(session).maxUserNameSize();
                    break;
                }
            }
            int len = txt.text().builder().append(s).textLength();
            if (j > 0) {
                sb.append(' ');
            }
            sb.append(s);
            if (len < z) {
                char[] c = new char[z - len];
                Arrays.fill(c, ' ');
                sb.append(new String(c));
            }
            // sb.append(s);
        }
        return sb.immutable();
    }

    public NutsString buildMain(NutsFetchDisplayOptions oo, NutsDisplayProperty dp) {
        NutsWorkspace ws = session.getWorkspace();
        NutsFormatManager txt = ws.formats();
        NutsTextManager nodeFactory = txt.text();
        if (oo.isRequireDefinition()) {
            buildLong();
        }
        if (dp == null) {
            dp = NutsDisplayProperty.ID;
        }
        switch (dp) {
            case ID: {
                return txt.text().parse(oo.getIdFormat().value(id).setNtf(ntf).format());
            }
            case STATUS: {
                return getFormattedStatusString();
            }
            case FILE: {
                if (def != null && def.getContent() != null && def.getContent().getPath() != null) {
                    return txt.text().nodeFor(def.getContent().getPath());
                }
                return txt.text().styled("missing-path", NutsTextNodeStyle.error());
            }
            case FILE_NAME: {
                if (def != null && def.getContent() != null && def.getContent().getPath() != null) {
                    return txt.text().plain(def.getContent().getPath().getFileName().toString());
                }
                return txt.text().styled("missing-file-name", NutsTextNodeStyle.error());
            }
            case ARCH: {
                if (desc != null) {
                    return keywordArr1(desc.getArch());
                }
                return txt.text().styled("missing-arch", NutsTextNodeStyle.error());
            }
            case NAME: {
                if (desc != null) {
                    return stringValue(desc.getName());
                }
                return txt.text().styled("missing-name", NutsTextNodeStyle.error());
            }
            case OS: {
                if (desc != null) {
                    return keywordArr2(desc.getOs());
                }
                return txt.text().styled("missing-os", NutsTextNodeStyle.error());
            }
            case OSDIST: {
                if (desc != null) {
                    return keywordArr2(desc.getOsdist());
                }
                return txt.text().styled("missing-osdist", NutsTextNodeStyle.error());
            }
            case PACKAGING: {
                if (desc != null) {
                    NutsFormatManager text = session.getWorkspace().formats();
                    return text.text().styled(stringValue(desc.getPackaging()), NutsTextNodeStyle.primary(3));
                }
                return txt.text().styled("missing-packaging", NutsTextNodeStyle.error());
            }
            case PLATFORM: {
                if (desc != null) {
                    return keywordArr1(desc.getPlatform());
                }
                return txt.text().styled("missing-platform", NutsTextNodeStyle.error());
            }
            case INSTALL_DATE: {
                if (def != null && def.getInstallInformation() != null) {
                    return stringValue(def.getInstallInformation().getCreatedDate());
                }
                return txt.text().styled("<null>", NutsTextNodeStyle.pale());
            }
            case REPOSITORY: {
                String rname = null;
                if (def != null) {
                    if (def.getRepositoryName() != null) {
                        rname = def.getRepositoryName();
                    }
                    if (def.getRepositoryUuid() != null) {
                        NutsRepository r = ws.repos()
                                .setSession(session.copy().setTransitive(false))
                                .findRepositoryById(def.getRepositoryUuid());
                        if (r != null) {
                            rname = r.getName();
                        }
                    }
                }
                if (rname == null && id != null) {
                    rname = id.getNamespace();
                }
                return stringValue(rname);
            }
            case REPOSITORY_ID: {
                String ruuid = null;
                if (def != null) {
                    if (def.getRepositoryUuid() != null) {
                        ruuid = def.getRepositoryUuid();
                    }
                }
                if (ruuid == null && id != null) {
                    String p = id.getNamespace();
                    NutsRepository r = ws.repos()
                            .setSession(session.copy().setTransitive(false))
                            .findRepositoryByName(p);
                    if (r != null) {
                        ruuid = r.getUuid();
                    }
                }
                return stringValue(ruuid);
            }
            case INSTALL_USER: {
                if (def != null && def.getInstallInformation() != null) {
                    return stringValue(def.getInstallInformation().getInstallUser());
                }
                return nodeFactory.styled("nobody", NutsTextNodeStyle.error());
            }
            case CACHE_FOLDER: {
                if (def != null) {
                    return stringValue(ws.locations().getStoreLocation(def.getId(), NutsStoreLocation.CACHE));
                }
                return nodeFactory.styled("<null>", NutsTextNodeStyle.error());
            }
            case CONFIG_FOLDER: {
                if (def != null) {
                    return stringValue(ws.locations().getStoreLocation(def.getId(), NutsStoreLocation.CONFIG));
                }
                return nodeFactory.styled("<null>", NutsTextNodeStyle.error());
            }
            case LIB_FOLDER: {
                if (def != null) {
                    return stringValue(ws.locations().getStoreLocation(def.getId(), NutsStoreLocation.LIB));
                }
                return nodeFactory.styled("<null>", NutsTextNodeStyle.error());
            }
            case LOG_FOLDER: {
                if (def != null) {
                    return stringValue(ws.locations().getStoreLocation(def.getId(), NutsStoreLocation.LOG));
                }
                return nodeFactory.styled("<null>", NutsTextNodeStyle.error());
            }
            case TEMP_FOLDER: {
                if (def != null) {
                    return stringValue(ws.locations().getStoreLocation(def.getId(), NutsStoreLocation.TEMP));
                }
                return nodeFactory.styled("<null>", NutsTextNodeStyle.error());
            }
            case VAR_LOCATION: {
                if (def != null) {
                    return stringValue(ws.locations().getStoreLocation(def.getId(), NutsStoreLocation.VAR));
                }
                return nodeFactory.styled("<null>", NutsTextNodeStyle.error());
            }
            case APPS_FOLDER: {
                if (def != null) {
                    return stringValue(ws.locations().getStoreLocation(def.getId(), NutsStoreLocation.APPS));
                }
                return nodeFactory.styled("<null>", NutsTextNodeStyle.error());
            }
            case EXEC_ENTRY: {
                if (def != null && def.getContent() != null && def.getContent().getPath() != null) {
                    List<NutsString> results = new ArrayList<NutsString>();
                    for (NutsExecutionEntry entry : ws.apps().execEntries().parse(def.getContent().getPath())) {
                        if (entry.isDefaultEntry()) {
                            //should all mark?
                            results.add(nodeFactory.plain(entry.getName()));
                        } else {
                            results.add(nodeFactory.plain(entry.getName()));
                        }
                    }
                    if (results.size() == 1) {
                        return results.get(0);
                    }
                    return txt.text().builder().appendJoined(
                            nodeFactory.plain(","),
                            results
                    );
                }
                return nodeFactory.styled("<missing-class>", NutsTextNodeStyle.error());
            }
            case INSTALL_FOLDER: {
                if (def != null && def.getInstallInformation() != null) {
                    return stringValue(def.getInstallInformation().getInstallFolder());
                }
                return txt.text().styled("<null>", NutsTextNodeStyle.pale());
            }
            case LONG_STATUS: {
                NutsTextManager text = ws.formats().text();
                List<NutsString> all = new ArrayList<>();
                if (def != null && def.getType() != null) {
                    switch (def.getType()) {
                        case REGULAR: {
                            all.add(text.plain(def.getType().id()));
                            break;
                        }
                        default: {
                            all.add(text.styled(def.getType().id(), NutsTextNodeStyle.primary(1)));
                            break;
                        }
                    }
                }
                if (executableApp) {
                    all.add(text.styled("application", NutsTextNodeStyle.primary(5)));
                } else if (executable) {
                    all.add(text.styled("executable", NutsTextNodeStyle.primary(3)));
                } else {
                    all.add(text.styled("library", NutsTextNodeStyle.primary(4)));
                }
                if (dep != null) {
                    NutsDependencyScope ss = CoreEnumUtils.parseEnumString(dep.getScope(), NutsDependencyScope.class, true);
                    if (dep.isOptional()) {
                        all.add(text.styled("optional", NutsTextNodeStyle.primary(5)));
                    }
                    if (ss != null) {
                        all.add(text.styled(NutsDependencyScope.API.id(), NutsTextNodeStyle.primary(5)));
                    }
                }
                return text.builder().appendJoined(text.styled(",", NutsTextNodeStyle.pale()),
                        all).build();

            }

            default: {
                throw new NutsUnsupportedArgumentException(session, String.valueOf(dp));
            }
        }
    }

    public NutsIdFormatHelper buildLong() {
        if (!built) {
            built = true;
            NutsWorkspace ws = session.getWorkspace();
            NutsInstalledRepository rr = NutsWorkspaceExt.of(ws).getInstalledRepository();
            this.installStatus = rr.getInstallStatus(id, session);
            NutsInstallInformation iif = rr.getInstallInformation(id, session);
            this.dte = iif == null ? null : iif.getCreatedDate();
            this.usr = iif == null ? null : iif.getInstallUser();
//            Boolean updatable = null;
            this.executable = null;
            this.executableApp = null;
            this.fetched = false;

            this.checkDependencies = false;
            this.defFetched = null;

            try {
                if (this.installStatus.isNonDeployed() || def == null) {
                    this.defFetched = ws.fetch().setId(id).setSession(
                            session.copy().setTrace(false).setFetchStrategy(NutsFetchStrategy.OFFLINE)
                    )
                            .setContent(true)
                            .setOptional(false)
                            .setDependencies(this.checkDependencies)
                            .getResultDefinition();
                    this.fetched = true;
                } else {
                    this.fetched = true;
                }
            } catch (Exception ex) {
                LOG.with().session(session).level(Level.FINE).error(ex).log("failed to build id format for {0}", id);
            }

            if (def != null) {
                this.executable = def.getDescriptor().isExecutable();
                this.executableApp = def.getDescriptor().isApplication();
            } else if (this.defFetched != null) {
                this.executable = this.defFetched.getDescriptor().isExecutable();
                this.executableApp = this.defFetched.getDescriptor().isApplication();
            } else if (desc != null) {
                this.executable = desc.isExecutable();
                this.executableApp = desc.isApplication();
            }
            this.status_f = (this.installStatus.isDefaultVersion()) ? 'I'
                    : (this.installStatus.isInstalled()) ? 'i'
                    : (this.installStatus.isRequired()) ? 'd'
                    : this.fetched ? 'f' : 'r';
//            this.status_obs=(this.installStatus.isInstalled()?'O':'U');
            if (def != null) {
                switch (def.getType()) {
                    case API: {
                        this.status_e = 'a';
                        break;
                    }
                    case RUNTIME: {
                        this.status_e = 'r';
                        break;
                    }
                    case EXTENSION: {
                        this.status_e = 'e';
                        break;
                    }
                    case COMPANION: {
                        this.status_e = 'c';
                        break;
                    }
                    case REGULAR: {
                        this.status_e = '-';
                        break;
                    }
                    default: {
                        this.status_e = '?';
                        break;
                    }
                }
            }
            this.status_i = buildComponentAppStatus();
            this.status_s = '-';
            this.status_o = '-';
            if (dep != null) {
                NutsDependencyScope ss = CoreEnumUtils.parseEnumString(dep.getScope(), NutsDependencyScope.class, true);
                if (ss != null) {
                    switch (ss) {
                        case API: {
                            this.status_s = 'c';
                            break;
                        }
                        case IMPLEMENTATION: {
                            this.status_s = 'i';
                            break;
                        }
                        case RUNTIME: {
                            this.status_s = 'r';
                            break;
                        }
                        case SYSTEM: {
                            this.status_s = 's';
                            break;
                        }
                        case PROVIDED: {
                            this.status_s = 'p';
                            break;
                        }
                        case TEST_API:
                        case TEST_IMPLEMENTATION:
                        case TEST_PROVIDED:
                        case TEST_RUNTIME:
                        case TEST_OTHER: {
                            this.status_s = 't';
                            break;
                        }
                        case IMPORT: {
                            this.status_s = 'm';
                            break;
                        }
                        case OTHER: {
                            this.status_s = 'O';
                            break;
                        }
                        default: {
                            this.status_s = '-';
                            break;
                        }
                    }
                }
                if (dep.isOptional()) {
                    this.status_s = 'o';
                }
            }

//            if (fetched) {
//                NutsId nut2 = null;
//                updatable = false;
//                try {
//                    nut2 = ws.fetch().parse(parse.setVersion("")).setSession(session.copy().setProperty("monitor-allowed", false)).setTransitive(true).wired().setTrace(false).getResultId();
//                } catch (Exception ex) {
//                    //ignore
//                }
//                if (nut2 != null && nut2.getVersion().compareTo(parse.getVersion()) > 0) {
//                    updatable = true;
//                }
//            }   
        }
        return this;
    }

    private char buildComponentAppStatus() {
        return this.executableApp != null ? (this.executableApp ? 'X' : this.executable ? 'x' : '-') : '.';
    }

    public NutsString getFormattedStatusString() {
        NutsFormatManager text = session.getWorkspace().formats();
        if (dep != null) {
            return text.text().styled("" + status_f
                    //                    + status_obs
                    + status_e + status_i + status_s, NutsTextNodeStyle.primary(3));
        }
        return text.text().styled("" + status_f
                //                + status_obs
                + status_e + status_i, NutsTextNodeStyle.primary(3));
    }

    public String getStatusString() {
        if (dep != null) {
            return "" + status_f
                    //                    + status_obs
                    + status_e + status_i + status_s;
        }
        return "" + status_f
                //                + status_obs
                + status_e + status_i;
    }

    private NutsString keywordArr1(String[] any) {
        return keywordArr0(any, NutsTextNodeStyle.primary(1));
    }

    private NutsString keywordArr2(String[] any) {
        return keywordArr0(any, NutsTextNodeStyle.primary(3));
    }

    private NutsString keywordArr0(String[] any, NutsTextNodeStyle style) {
        NutsFormatManager txt = session
                .getWorkspace().formats();
        if (any == null || any.length == 0) {
            return txt.text().blank();
        }
        if (any.length == 1) {
            return txt.text().builder().append(txt.text().styled(stringValue(any[0]), style))
                    .immutable();
        }
        return txt.text().builder()
                .append("[")
                .appendJoined(
                        txt.text().plain(","),
                        Arrays.stream(any).map(x -> txt.text().styled(stringValue(x), style)).collect(Collectors.toList())
                )
                .append("]").immutable();
    }

    private NutsString stringValue(Object any) {
        return CoreCommonUtils.stringValueFormatted(any, false, session);
    }
}
