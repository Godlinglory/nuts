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
package net.vpc.app.nuts.extensions.executors;

import net.vpc.app.nuts.NutsExecutionContext;
import net.vpc.app.nuts.NutsExecutorComponent;
import net.vpc.app.nuts.NutsFile;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class ProcessNutsExecutorComponent implements NutsExecutorComponent {

    private static final Logger log = Logger.getLogger(ProcessNutsExecutorComponent.class.getName());
    public static final NutsId ID = CoreNutsUtils.parseNutsId("exec");

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public int getSupportLevel(NutsFile nutsFile) {
        return DEFAULT_SUPPORT;
    }

    public int exec(NutsExecutionContext executionContext) {
        NutsFile nutMainFile = executionContext.getNutsFile();
        String storeFolder = nutMainFile.getInstallFolder();
        String[][] envAndApp0 = CoreNutsUtils.splitEnvAndAppArgs(executionContext.getExecArgs());
        String[][] envAndApp = CoreNutsUtils.splitEnvAndAppArgs(executionContext.getArgs());

        List<String> env = new ArrayList<>();
        env.addAll(Arrays.asList(envAndApp0[0]));
        env.addAll(Arrays.asList(envAndApp[0]));

        List<String> app = new ArrayList<>();
        app.addAll(Arrays.asList(envAndApp0[1]));
        if (app.isEmpty()) {
            if (storeFolder == null) {
                app.add("${nuts.file}");
            } else {
                app.add("${nuts.store}/run");
            }
        }
        app.addAll(Arrays.asList(envAndApp[1]));

        Map<String, String> osEnv = new HashMap<>();
        String bootArgumentsString = executionContext.getWorkspace().getBootOptions().getBootArgumentsString();
        osEnv.put("nuts_boot_args",bootArgumentsString);
        File directory  = null;
        boolean showCommand = false;
        for (Iterator<String> iterator = env.iterator(); iterator.hasNext();) {
            String e = iterator.next();
            if (e.startsWith("-dir=")) {
                directory  =new File(executionContext.getWorkspace().resolvePath(e.substring(("-dir=").length())));
                iterator.remove();
            } else if (e.startsWith("-env-")) {
                String nv = e.substring("-env-".length());
                int endIndex = nv.indexOf('=');
                if (endIndex >= 0) {
                    String n = nv.substring(0, endIndex);
                    String v = nv.substring(endIndex + 1);
                    osEnv.put(n, v);
                }
                iterator.remove();
            } else if (e.equals("-show-command")) {
                iterator.remove();
                showCommand = true;
            }
        }
        if(directory==null) {
            directory = StringUtils.isEmpty(executionContext.getCwd()) ? null :
                    new File(executionContext.getWorkspace().resolvePath(executionContext.getCwd()));
        }
        return CoreIOUtils.execAndWait(nutMainFile, executionContext.getWorkspace(), executionContext.getSession(), executionContext.getExecProperties(),
                app.toArray(new String[app.size()]),
                osEnv, directory, executionContext.getTerminal(), showCommand
        );
    }
}
