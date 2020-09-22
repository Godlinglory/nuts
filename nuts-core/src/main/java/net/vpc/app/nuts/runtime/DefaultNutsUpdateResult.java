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
package net.vpc.app.nuts.runtime;

import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsUpdateResult;

/**
 * Created by vpc on 6/23/17.
 */
public final class DefaultNutsUpdateResult implements NutsUpdateResult {

    private NutsId id;
    private NutsDefinition local;
    private NutsDefinition available;
    private NutsId[] dependencies;
    private boolean runtime;
    private boolean updateApplied;
    private boolean updateForced;
    private boolean updateVersionAvailable;
    private boolean updateStatusAvailable;

    public DefaultNutsUpdateResult() {
    }

    public DefaultNutsUpdateResult(NutsId id, NutsDefinition local, NutsDefinition available, NutsId[] dependencies, boolean runtime) {
        this.id = id;
        this.local = local;
        this.available = available;
        this.runtime = runtime;
        this.dependencies = dependencies == null ? new NutsId[0] : dependencies;
    }

    public boolean isRuntime() {
        return runtime;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsDefinition getLocal() {
        return local;
    }

    @Override
    public NutsDefinition getAvailable() {
        return available;
    }

    @Override
    public NutsId[] getDependencies() {
        return dependencies;
    }

    @Override
    public boolean isUpdateApplied() {
        return updateApplied;
    }

    public void setUpdateApplied(boolean updateApplied) {
        this.updateApplied = updateApplied;
    }

    @Override
    public boolean isUpdateForced() {
        return updateForced;
    }

    public void setUpdateForced(boolean updateForced) {
        this.updateForced = updateForced;
    }

    @Override
    public boolean isUpdateAvailable() {
        return isUpdateVersionAvailable() || isUpdateStatusAvailable();
    }

    @Override
    public boolean isUpdateVersionAvailable() {
        return updateVersionAvailable;
    }

    public void setUpdateVersionAvailable(boolean updateVersion) {
        this.updateVersionAvailable = updateVersion;
    }

    @Override
    public boolean isUpdateStatusAvailable() {
        return updateStatusAvailable;
    }

    public void setUpdateStatusAvailable(boolean updateStatus) {
        this.updateStatusAvailable = updateStatus;
    }

    public void setLocal(NutsDefinition local) {
        this.local = local;
    }

    public void setAvailable(NutsDefinition available) {
        this.available = available;
    }

    public void setId(NutsId id) {
        this.id = id;
    }

}
