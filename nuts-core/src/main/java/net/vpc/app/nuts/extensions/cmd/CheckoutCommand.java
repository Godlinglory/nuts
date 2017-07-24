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
import net.vpc.app.nuts.extensions.cmd.cmdline.*;

import java.io.File;

/**
 * Created by vpc on 1/7/17.
 */
public class CheckoutCommand extends AbstractNutsCommand {

    public CheckoutCommand() {
        super("checkout", CORE_SUPPORT);
    }

    public void run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws Exception {
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        String id = cmdLine.removeNonOptionOrError(new NutsIdNonOption("Nuts",context)).getString();
        String contentFile = cmdLine.removeNonOptionOrError(new FolderNonOption("folder")).getString();
        if (autoComplete != null) {
            return;
        }
        NutsFile nf = context.getValidWorkspace().checkout(
                id,
                new File(contentFile),
                context.getSession()
        );
        context.getTerminal().getOut().println("Folder " + contentFile + " initialized with " + nf.getId());
    }
}
