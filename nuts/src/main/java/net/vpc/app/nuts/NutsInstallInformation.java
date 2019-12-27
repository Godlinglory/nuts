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
package net.vpc.app.nuts;

import java.nio.file.Path;
import java.time.Instant;

/**
 * Information about installed artifact
 * @author vpc
 * @since 0.5.5
 */
public interface NutsInstallInformation {
    /**
     * installation date
     * @return installation date
     */
    Instant getInstallDate();

    /**
     * true when the installed artifact is default version
     * @return true when the installed artifact is default version
     */
    boolean isDefaultVersion();

    /**
     * installation formation path.
     * @return installation formation path
     */
    Path getInstallFolder();

    /**
     * true if the installation just occurred in the very last operation
     * @return true if the installation just occurred in the very last operation
     */
    boolean isJustInstalled();

    /**
     * return the user responsible of the installation
     * @return the user responsible of the installation
     */
    String getInstallUser();

    /**
     * return install status
     * @return install status
     */
    NutsInstallStatus getInstallStatus();

}
