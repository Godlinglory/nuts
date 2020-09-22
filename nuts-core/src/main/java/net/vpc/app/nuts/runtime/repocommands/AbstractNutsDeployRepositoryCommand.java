/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
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
package net.vpc.app.nuts.runtime.repocommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 *
 * @author vpc
 * @category SPI Base
 */
public abstract class AbstractNutsDeployRepositoryCommand extends NutsRepositoryCommandBase<NutsDeployRepositoryCommand> implements NutsDeployRepositoryCommand {

    private NutsId id;
    private Object content;
    private NutsDescriptor descriptor;

    public AbstractNutsDeployRepositoryCommand(NutsRepository repo) {
        super(repo, "deploy");
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        if (super.configureFirst(cmd)) {
            return true;
        }
        return false;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsDeployRepositoryCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public Object getContent() {
        return content;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(Path content) {
        this.content = content;
        return this;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(URL content) {
        this.content = content;
        return this;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(File content) {
        this.content = content;
        return this;
    }

    @Override
    public NutsDeployRepositoryCommand setContent(InputStream content) {
        this.content = content;
        return this;
    }

    @Override
    public NutsDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public NutsDeployRepositoryCommand setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    protected void checkParameters() {
        getRepo().security().checkAllowed(NutsConstants.Permissions.DEPLOY, "deploy");
        if (this.getId() == null) {
            throw new NutsIllegalArgumentException(repo.getWorkspace(), "Missing Id");
        }
        if (this.getContent() == null) {
            throw new NutsIllegalArgumentException(repo.getWorkspace(), "Missing Content");
        }
        if (this.getDescriptor() == null) {
            throw new NutsIllegalArgumentException(repo.getWorkspace(), "Missing Descriptor");
        }
        if (CoreStringUtils.isBlank(this.getId().getGroupId())) {
            throw new NutsIllegalArgumentException(repo.getWorkspace(), "Empty group");
        }
        if (CoreStringUtils.isBlank(this.getId().getArtifactId())) {
            throw new NutsIllegalArgumentException(repo.getWorkspace(), "Empty name");
        }
        if ((this.getId().getVersion().isBlank())) {
            throw new NutsIllegalArgumentException(repo.getWorkspace(), "Empty version");
        }
        if ("RELEASE".equals(this.getId().getVersion().getValue())
                || NutsConstants.Versions.LATEST.equals(this.getId().getVersion().getValue())) {
            throw new NutsIllegalArgumentException(repo.getWorkspace(), "Invalid version " + this.getId().getVersion());
        }
    }


}
