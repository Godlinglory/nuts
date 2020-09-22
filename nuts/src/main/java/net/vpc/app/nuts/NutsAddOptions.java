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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.Objects;

/**
 * Generic Add options
 *
 * @author vpc
 * @see NutsWorkspaceConfigManager#addSdk(net.vpc.app.nuts.NutsSdkLocation,
 * net.vpc.app.nuts.NutsAddOptions)
 * @see NutsWorkspaceConfigManager#addCommandAlias(net.vpc.app.nuts.NutsCommandAliasConfig,
 * net.vpc.app.nuts.NutsAddOptions)
 * @see NutsWorkspaceConfigManager#addCommandAliasFactory(net.vpc.app.nuts.NutsCommandAliasFactoryConfig,
 * net.vpc.app.nuts.NutsAddOptions)
 * @since 0.5.4
 * @category Config
 */
public class NutsAddOptions implements Serializable {
    private static final long serialVersionUID = 1;

    /**
     * current session
     */
    private NutsSession session;

    /**
     * current session
     *
     * @return current session
     */
    public NutsSession getSession() {
        return session;
    }

    /**
     * update current session
     *
     * @param session session
     * @return {@code this} instance
     */
    public NutsAddOptions setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsAddOptions that = (NutsAddOptions) o;
        return Objects.equals(session, that.session);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session);
    }

    @Override
    public String toString() {
        return "NutsAddOptions{" +
                "session=" + session +
                '}';
    }
}
