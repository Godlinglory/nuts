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

/**
 * This Exception is fired when an artifact fails to be uninstalled for the artifact not being installed yet.
 *
 * @since 0.5.4
 * @category Exception
 */
public class NutsNotInstalledException extends NutsInstallationException {

    /**
     * Constructs a new NutsNotInstalledException exception
     * @param workspace workspace
     * @param id artifact
     */
    public NutsNotInstalledException(NutsWorkspace workspace, NutsId id) {
        this(workspace, id == null ? null : id.toString());
    }

    /**
     * Constructs a new NutsNotInstalledException exception
     * @param workspace workspace
     * @param id artifact
     */
    public NutsNotInstalledException(NutsWorkspace workspace, String id) {
        this(workspace, id, null, null);
    }

    /**
     * Constructs a new NutsNotInstalledException exception
     * @param workspace workspace
     * @param id artifact
     * @param msg message
     * @param ex error
     */
    public NutsNotInstalledException(NutsWorkspace workspace, NutsId id, String msg, Exception ex) {
        this(workspace, id == null ? null : id.toString(), msg, ex);
    }

    /**
     * Constructs a new NutsNotInstalledException exception
     * @param workspace workspace
     * @param id artifact
     * @param msg message
     * @param ex exception
     */
    public NutsNotInstalledException(NutsWorkspace workspace, String id, String msg, Exception ex) {
        super(workspace, id, PrivateNutsUtils.isBlank(msg) ? "Not installed " + (id == null ? "<null>" : id) : msg, ex);
    }
}
