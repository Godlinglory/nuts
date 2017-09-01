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

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.cmd.cmdline.FolderNonOption;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.util.IOUtils;

import java.io.File;
import java.util.Arrays;

/**
 * Created by vpc on 1/7/17.
 */
public class CdCommand extends AbstractNutsCommand {

    public CdCommand() {
        super("cd", CORE_SUPPORT);
    }

    public void run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws Exception {
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        while (!cmdLine.isEmpty()) {
            String folder=cmdLine.readNonOptionOrError(new FolderNonOption("Folder")).getString();
            File[] validFiles = Arrays.stream(CoreIOUtils.findFilesOrError(folder,new File(context.getCommandLine().getCwd()))).filter(
                    x->x.isDirectory()
            ).toArray(File[]::new);
            NutsPrintStream out = context.getTerminal().getOut();
            if(validFiles.length==1) {
                context.getCommandLine().setCwd(validFiles[0].getPath());
            }else if(validFiles.length==0) {
                out.println("invalid folder "+folder);
            }else{
                for (File validFile : validFiles) {
                    out.println(validFile.getPath());
                }
            }
            cmdLine.requireEmpty();
        }
    }
}
