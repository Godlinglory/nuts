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
package net.vpc.app.nuts;

/**
 * Exception thrown when extension is already registered.
 *
 * @since 0.5.4
 * @category Exception
 */
public class NutsExtensionAlreadyRegisteredException extends NutsExtensionException {

    /**
     * installed id
     */
    private final String installed;

    /**
     * Constructs a new NutsExtensionAlreadyRegisteredException exception
     * @param workspace workspace
     * @param id artifact id
     * @param installed installed id
     */
    public NutsExtensionAlreadyRegisteredException(NutsWorkspace workspace, String id, String installed) {
        super(workspace, id, "Extension Already registered " + (id == null ? "<null>" + " as " + installed : id), null);
        this.installed = installed;
    }

    /**
     * Constructs a new NutsExtensionAlreadyRegisteredException exception
     * @param workspace workspace
     * @param id artifact id
     * @param installed installed id
     * @param cause cause
     */
    public NutsExtensionAlreadyRegisteredException(NutsWorkspace workspace, String id, String installed, Throwable cause) {
        super(workspace, id, "Extension Already registered " + (id == null ? "<null>" : id) + " as " + installed, cause);
        this.installed = installed;
    }

    /**
     * registered/installed extension
     * @return registered/installed extension
     */
    public String getInstalled() {
        return installed;
    }
}
