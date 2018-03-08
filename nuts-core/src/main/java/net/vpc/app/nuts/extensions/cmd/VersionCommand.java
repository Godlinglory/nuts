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
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;

/**
 * Created by vpc on 1/7/17.
 */
public class VersionCommand extends AbstractNutsCommand {

    public VersionCommand() {
        super("version", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsPrintStream out = context.getTerminal().getOut();
        NutsWorkspaceConfigManager config = context.getWorkspace().getConfigManager();
        out.printf("workspace-boot       : [[%s]]\n" , config.getWorkspaceBootId() );
        out.printf("workspace-runtime    : [[%s]]\n" , config.getWorkspaceRuntimeId() );
        out.printf("workspace-location   : [[%s]]\n" , config.getWorkspaceLocation() );
        out.printf("boot-java-version    : [[%s]]\n" , System.getProperty("java.version") );
        out.printf("boot-java-executable : [[%s]]\n" , System.getProperty("java.home") + "/bin/java" );
        return 0;
    }
}
