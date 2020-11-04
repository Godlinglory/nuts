/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2020 thevpc
 *
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
package net.thevpc.nuts;

/**
 * Install strategy defines the strategy used by installer

 * @category Base
 */
public enum NutsInstallStrategy {
    /**
     * the default strategy points to 'INSTALL' but this can be configured.
     */
    DEFAULT,

    /**
     * Install the artifact as 'required'.
     */
    REQUIRE,

    /**
     * Install the artifact if not already installed. All dependencies will
     * be fetched and marked as 'required'.
     * If the artifact is 'required', it will be promoted to 'installed'.
     */
    INSTALL,

    /**
     * reinstall the artifact if already installed. re-fetch the artifact if already required.
     * If wont promote 'required' to 'installed'. All dependencies will
     * be fetched and marked as 'required'.
     */
    REINSTALL,

    /**
     * reinstall the artifact if already installed. re-fetch the artifact if already required.
     * If wont promote 'required' to 'installed'.
     * No dependency will be fetched.
     */
    REPAIR,

    /**
     * switch default version. This is applicable only if the artifact is already installed.
     * No dependency will be fetched.
     */
    SWITCH_VERSION
}
