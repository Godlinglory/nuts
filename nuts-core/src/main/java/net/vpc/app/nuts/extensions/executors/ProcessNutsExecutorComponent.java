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
package net.vpc.app.nuts.extensions.executors;

import net.vpc.app.nuts.NutsExecutionContext;
import net.vpc.app.nuts.NutsExecutorComponent;
import net.vpc.app.nuts.NutsFile;
import net.vpc.app.nuts.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class ProcessNutsExecutorComponent implements NutsExecutorComponent {
    private static final Logger log = Logger.getLogger(ProcessNutsExecutorComponent.class.getName());

    @Override
    public int getSupportLevel(NutsFile nutsFile) {
        return CORE_SUPPORT;
    }

    public void exec(NutsExecutionContext executionContext) throws IOException {
        NutsFile nutMainFile = executionContext.getNutsFile();
        File storeFolder = nutMainFile.getInstallFolder();
        List<String> args = new ArrayList<>();

        if (executionContext.getExecArgs().length == 0) {
            if (storeFolder == null) {
                args.add("${nuts.file}");
            } else {
                args.add("${nuts.store}/run");
            }
        } else {
            args.addAll(Arrays.asList(executionContext.getExecArgs()));
        }
        args.addAll(Arrays.asList(executionContext.getArgs()));

        IOUtils.execAndWait(nutMainFile, executionContext.getWorkspace(), executionContext.getSession(), executionContext.getExecProperties(),
                (String[]) args.toArray(new String[args.size()]),
                null, null, executionContext.getSession().getTerminal()
        );
    }
}
