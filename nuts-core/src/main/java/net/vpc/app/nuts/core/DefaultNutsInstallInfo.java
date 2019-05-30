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
package net.vpc.app.nuts.core;

import java.nio.file.Path;
import java.util.Date;
import net.vpc.app.nuts.NutsInstallInfo;

/**
 *
 * @author vpc
 * @since 0.5.5
 */
public class DefaultNutsInstallInfo implements NutsInstallInfo {

    private final boolean installed;
    private boolean justInstalled;
    private boolean defaultVersion;
    private Date installDate;
    private String installUser;
    private final Path installFolder;

    public DefaultNutsInstallInfo(boolean installed, boolean defaultVersion, Path installFolder,Date installDate,String installUser) {
        this.installed = installed;
        this.installFolder = installFolder;
        this.defaultVersion = defaultVersion;
        this.installDate = installDate;
        this.installUser = installUser;
    }

    public String getInstallUser() {
        return installUser;
    }

    public void setInstallUser(String installUser) {
        this.installUser = installUser;
    }

    @Override
    public Date getInstallDate() {
        return installDate;
    }

    public void setInstallDate(Date installDate) {
        this.installDate = installDate;
    }

    @Override
    public boolean isDefaultVersion() {
        return defaultVersion;
    }

    public void setDefaultVersion(boolean defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    @Override
    public boolean isInstalled() {
        return installed;
    }

    @Override
    public Path getInstallFolder() {
        return installFolder;
    }

    @Override
    public boolean isJustInstalled() {
        return justInstalled;
    }

    public void setJustInstalled(boolean justInstalled) {
        this.justInstalled = justInstalled;
    }

}
