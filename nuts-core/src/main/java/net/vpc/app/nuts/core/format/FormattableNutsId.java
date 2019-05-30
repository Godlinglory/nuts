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
 * Copyright (C) 2016-2019 Taha BEN SALAH
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
package net.vpc.app.nuts.core.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyScope;
import net.vpc.app.nuts.NutsDependencyTreeNode;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsExecutionEntry;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsInstallInfo;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsUnsupportedArgumentException;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.DefaultNutsInstalledRepository;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class FormattableNutsId {

    NutsId id;
    boolean i;
    boolean d;
    Boolean executable = null;
    Boolean executableApp = null;
    boolean fetched = false;
    boolean checkDependencies = false;
    NutsDefinition defFetched;
    NutsDefinition def;
    NutsDescriptor desc;
    NutsDependency dep;
    NutsSession session;
    NutsWorkspace ws;
    Date dte;
    String usr;
    char status_f;
    char status_i;
    char status_s;
    char status_o;
    String display;
    boolean built = false;

    public static FormattableNutsId of(Object object, NutsWorkspace ws, NutsSession session) {
        if (object instanceof NutsId) {
            NutsId v = (NutsId) object;
            return (new FormattableNutsId(v, ws, session));
        } else if (object instanceof NutsDescriptor) {
            NutsDescriptor v = (NutsDescriptor) object;
            return (new FormattableNutsId(v, ws, session));
        } else if (object instanceof NutsDefinition) {
            NutsDefinition v = (NutsDefinition) object;
            return (new FormattableNutsId(v, ws, session));
        } else if (object instanceof NutsDependency) {
            NutsDependency v = (NutsDependency) object;
            return (new FormattableNutsId(v, ws, session));
        } else if (object instanceof NutsDependencyTreeNode) {
            NutsDependencyTreeNode v = (NutsDependencyTreeNode) object;
            return (new FormattableNutsId(v, ws, session));
        } else {
            return null;
        }

    }

    public FormattableNutsId(NutsDependencyTreeNode id, NutsWorkspace ws, NutsSession session) {
        this(null, id.getDefinition() == null ? null : id.getDefinition().getDescriptor(), id.getDefinition(), id.getDependency(), ws, session);
    }

    public FormattableNutsId(NutsId id, NutsWorkspace ws, NutsSession session) {
        this(id, null, null, null, ws, session);
    }

    public FormattableNutsId(NutsDescriptor desc, NutsWorkspace ws, NutsSession session) {
        this(null, desc, null, null, ws, session);
    }

    public FormattableNutsId(NutsDefinition def, NutsWorkspace ws, NutsSession session) {
        this(null, null, def, null, ws, session);
    }

    public FormattableNutsId(NutsDependency dep, NutsWorkspace ws, NutsSession session) {
        this(null, null, null, dep, ws, session);
    }

    private FormattableNutsId(NutsId id, NutsDescriptor desc, NutsDefinition def, NutsDependency dep, NutsWorkspace ws, NutsSession session) {
        if (id == null) {
            if (def != null) {
                id = def.getId();
            } else if (desc != null) {
                id = desc.getId();
            } else if (dep != null) {
                id = dep.getId();
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
        this.ws = ws;
    }

    public String[] getMultiColumnRow(NutsFetchDisplayOptions oo) {
        NutsDisplayType[] a = oo.getDisplays();
        String[] b = new String[a.length];
        for (int j = 0; j < b.length; j++) {
            b[j] = buildMain(oo, a[j]);
        }
        return b;
    }

    public String getSingleColumnRow(NutsFetchDisplayOptions oo) {
        return CoreStringUtils.join(" ", Arrays.asList(getMultiColumnRow(oo)));
    }

    public String buildMain(NutsFetchDisplayOptions oo, NutsDisplayType dp) {
        if (oo.isRequireDefinition()) {
            buildLong();
        }
        if (dp == null) {
            dp = NutsDisplayType.ID;
        }
        switch (dp) {
            case ID: {
                return oo.getIdFormat().toString(id);
            }
            case STATUS: {
                return getFormattedStatusString();
            }
            case FILE: {
                if (def != null && def.getContent() != null && def.getContent().getPath() != null) {
                    return def.getContent().getPath().toString();
                }
                return "@@missing-path@@";
            }
            case FILE_NAME: {
                if (def != null && def.getContent() != null && def.getContent().getPath() != null) {
                    return def.getContent().getPath().getFileName().toString();
                }
                return "@@missing-file-name@@";
            }
            case ARCH: {
                if (desc != null) {
                    return CoreCommonUtils.stringValue(desc.getArch());
                }
                return "@@missing-arch@@";
            }
            case NAME: {
                if (desc != null) {
                    return String.valueOf(desc.getName());
                }
                return "@@missing-name@@";
            }
            case OS: {
                if (desc != null) {
                    return CoreCommonUtils.stringValue(desc.getOs());
                }
                return "@@missing-os@@";
            }
            case OSDIST: {
                if (desc != null) {
                    return CoreCommonUtils.stringValue(desc.getOsdist());
                }
                return "@@missing-os@@";
            }
            case PACKAGING: {
                if (desc != null) {
                    return CoreCommonUtils.stringValue(desc.getPackaging());
                }
                return "@@missing-packaging@@";
            }
            case PLATFORM: {
                if (desc != null) {
                    return CoreCommonUtils.stringValue(desc.getPlatform());
                }
                return "@@missing-platform@@";
            }
            case INSTALL_DATE: {
                if (def != null && def.getInstallation()!=null) {
                    return CoreCommonUtils.stringValue(def.getInstallation().getInstallDate());
                }
                return "    -  -     :  :  .   ";
            }
            case INSTALL_USER: {
                if (def != null && def.getInstallation()!=null) {
                    return CoreCommonUtils.stringValue(def.getInstallation().getInstallUser());
                }
                return "@@nobody@@";
            }
            case EXEC_ENTRY: {
                if (def != null && def.getContent() != null && def.getContent().getPath() != null) {
                    List<String> results = new ArrayList<String>();
                    for (NutsExecutionEntry entry : ws.parser().parseExecutionEntries(def.getContent().getPath())) {
                        if (entry.isDefaultEntry()) {
                            //should all mark?
                            results.add(entry.getName());
                        } else {
                            results.add(entry.getName());
                        }
                    }
                    if (results.size() == 1) {
                        return results.get(0);
                    }
                    return results.toString();
                }
                return "@@missing-class@@";
            }
            default: {
                throw new NutsUnsupportedArgumentException(ws,String.valueOf(dp));
            }
        }
    }

    public void buildLong() {
        if (!built) {
            built = true;
            DefaultNutsInstalledRepository rr = NutsWorkspaceExt.of(ws).getInstalledRepository();
            this.i = rr.isInstalled(id);
            this.d = rr.isDefaultVersion(id);
            NutsInstallInfo iif = rr.getInstallInfo(id);
            this.dte = iif==null?null:iif.getInstallDate();
            this.usr = iif==null?null:iif.getInstallUser();
//            Boolean updatable = null;
            this.executable = null;
            this.executableApp = null;
            this.fetched = false;

            this.checkDependencies = false;
            this.defFetched = null;

            try {
                if (!this.i || def == null) {
                    this.defFetched = ws.fetch().id(id).setSession(
                            session.setTrace(false)
                    ).offline()
                            .setInstallInformation(true)
                            .setContent(true)
                            .setOptional(false)
                            .dependencies(this.checkDependencies)
                            .getResultDefinition();
                    this.fetched = true;
                } else {
                    this.fetched = true;
                }
            } catch (Exception ex) {
                //ignore!!
            }

            if (def != null) {
                this.executable = def.getDescriptor().isExecutable();
                this.executableApp = def.getDescriptor().isNutsApplication();
            } else if (this.defFetched != null) {
                this.executable = this.defFetched.getDescriptor().isExecutable();
                this.executableApp = this.defFetched.getDescriptor().isNutsApplication();
            } else if (desc != null) {
                this.executable = desc.isExecutable();
                this.executableApp = desc.isNutsApplication();
            }
            this.status_f = this.i && this.d ? 'I' : this.i ? 'i' : this.fetched ? 'f' : 'r';
            this.status_i = this.executableApp != null ? (this.executableApp ? 'X' : this.executable ? 'x' : '-') : '.';
            this.status_s = '-';
            this.status_o = '-';
            if (dep != null) {
                NutsDependencyScope ss = CoreCommonUtils.parseEnumString(dep.getScope(), NutsDependencyScope.class, true);
                if (ss != null) {
                    switch (ss) {
                        case COMPILE: {
                            this.status_s = 'c';
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
                        case TEST: {
                            this.status_s = 't';
                            break;
                        }
                        case IMPLEMENTATION: {
                            this.status_s = 'i';
                            break;
                        }
                        case IMPORT: {
                            this.status_s = 'm';
                            break;
                        }
                        case TEST_PROVIDED: {
                            this.status_s = 'P';
                            break;
                        }
                        case TEST_RUNTIME: {
                            this.status_s = 'R';
                            break;
                        }
                        case OTHER: {
                            this.status_s = 'O';
                            break;
                        }
                        default: {
                            this.status_s = 'x';
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
//                    nut2 = ws.fetch().id(id.setVersion("")).setSession(session.copy().setProperty("monitor-allowed", false)).setTransitive(true).wired().setTrace(false).getResultId();
//                } catch (Exception ex) {
//                    //ignore
//                }
//                if (nut2 != null && nut2.getVersion().compareTo(id.getVersion()) > 0) {
//                    updatable = true;
//                }
//            }   
        }

    }

    public String getFormattedStatusString() {
        if (dep != null) {
            return "<<\\[" + status_f + status_i + status_s + "\\]>>";
        }
        return "<<\\[" + status_f + status_i + "\\]>>";
    }
}
