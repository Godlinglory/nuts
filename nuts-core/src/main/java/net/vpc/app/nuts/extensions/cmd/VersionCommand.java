/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.boot.BootNutsWorkspace;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

/**
 * Created by vpc on 1/7/17.
 */
public class VersionCommand extends AbstractNutsCommand {

    public VersionCommand() {
        super("version", CORE_SUPPORT);
    }

    public void run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws Exception {
        NutsWorkspace bws=Main.openBootstrapWorkspace(context.getWorkspace().getWorkspaceRootLocation());
        Map<String, String> runtimeProperties = Main.getRuntimeProperties(bws, context.getSession());
        NutsPrintStream out = context.getTerminal().getOut();
        out.drawln("boot-version         : [[" + runtimeProperties.get("nuts.boot.version") + "]]");
        out.drawln("boot-location        : [[" + runtimeProperties.get("nuts.boot.workspace") + "]]");
        out.drawln("boot-api             : [[" + runtimeProperties.get("nuts.boot.api-component") + "]]");
        out.drawln("boot-core            : [[" + runtimeProperties.get("nuts.boot.core-component") + "]]");
        out.drawln("workspace-version    : [[" + context.getWorkspace().getWorkspaceVersion() + "]]");
        out.drawln("workspace-location   : [[" + context.getWorkspace().getWorkspaceLocation() + "]]");
        out.drawln("boot-java-version    : [[" + System.getProperty("java.version") + "]]");
        out.drawln("boot-java-executable : [[" + System.getProperty("java.home") + "/bin/java" + "]]");
    }
}
