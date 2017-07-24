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
package net.vpc.app.nuts.extensions.cmd.cmdline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.vpc.app.nuts.ArgumentCandidate;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsSecurityEntityConfig;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public class RightNonOption extends DefaultNonOption {

    private NutsWorkspace workspace;
    private NutsSecurityEntityConfig securityEntityConfig;
    private boolean existing;

//    public RightNonOption(String name, NutsCommandContext context) {
//        super(name);
//        this.workspace = context.getValidWorkspace();
//    }
    public RightNonOption(String name, NutsSecurityEntityConfig securityEntityConfig, boolean existing) {
        super(name);
        this.securityEntityConfig = securityEntityConfig;
        this.existing = existing;
    }

    @Override
    public List<ArgumentCandidate> getValues() {
        List<ArgumentCandidate> all = new ArrayList<>();
        all.add(new DefaultArgumentCandidate(NutsConstants.RIGHT_ADD_REPOSITORY));
        all.add(new DefaultArgumentCandidate(NutsConstants.RIGHT_ADMIN));
        all.add(new DefaultArgumentCandidate(NutsConstants.RIGHT_DEPLOY));
        all.add(new DefaultArgumentCandidate(NutsConstants.RIGHT_EXEC));
        all.add(new DefaultArgumentCandidate(NutsConstants.RIGHT_FETCH_CONTENT));
        all.add(new DefaultArgumentCandidate(NutsConstants.RIGHT_FETCH_DESC));
        all.add(new DefaultArgumentCandidate(NutsConstants.RIGHT_INSTALL));
        all.add(new DefaultArgumentCandidate(NutsConstants.RIGHT_PUSH));
        all.add(new DefaultArgumentCandidate(NutsConstants.RIGHT_REMOVE_REPOSITORY));
        all.add(new DefaultArgumentCandidate(NutsConstants.RIGHT_SAVE_REPOSITORY));
        all.add(new DefaultArgumentCandidate(NutsConstants.RIGHT_SAVE_WORKSPACE));
        all.add(new DefaultArgumentCandidate(NutsConstants.RIGHT_SET_PASSWORD));
        all.add(new DefaultArgumentCandidate(NutsConstants.RIGHT_UNDEPLOY));
        all.add(new DefaultArgumentCandidate(NutsConstants.RIGHT_UNINSTALL));
        Iterator<ArgumentCandidate> i = all.iterator();
        while (i.hasNext()) {
            ArgumentCandidate right = i.next();
            if (existing) {
                if (securityEntityConfig != null) {
                    if (!securityEntityConfig.containsRight(right.getValue())) {
                        i.remove();
                    }
                }
            } else {
                if (securityEntityConfig != null) {
                    if (securityEntityConfig.containsRight(right.getValue())) {
                        i.remove();
                    }
                }
            }
        }
        return all;
    }
}
