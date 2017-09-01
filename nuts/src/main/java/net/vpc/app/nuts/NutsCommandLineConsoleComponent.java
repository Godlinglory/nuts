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
package net.vpc.app.nuts;

import java.io.File;
import java.io.IOException;

/**
 * Created by vpc on 1/24/17.
 */
@Prototype
public interface NutsCommandLineConsoleComponent extends NutsComponent<Object> {

    void init(NutsWorkspace workspace, NutsSession session) throws IOException;

    void setServiceName(String serviceName);

    void setWorkspace(NutsWorkspace workspace);

    void runFile(File file, String[] args) throws IOException;

    void runLine(String line);

    void run(String[] args);

    NutsCommand[] getCommands();

    NutsCommand getCommand(String command);

    NutsCommand findCommand(String command);

    boolean installCommand(NutsCommand command);

    boolean uninstallCommand(String command);

    String getCwd();

    void setCwd(String path);

    Throwable getLastThrowable();

    String getLastErrorMessage();
}
