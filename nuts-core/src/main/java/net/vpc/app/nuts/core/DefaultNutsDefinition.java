/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import java.nio.file.Path;
import java.util.Objects;
import javax.swing.JOptionPane;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.NutsDependencyScopes;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNutsDefinition implements NutsDefinition {

    private NutsId id;
    private NutsDescriptor descriptor;
    private String repositoryUuid;
    private String repositoryName;

    //optional 
    private NutsContent content;
    private NutsInstallInformation installInformation;
    private NutsDependencyTreeNode[] dependencyNodes;
    private NutsDependency[] dependencies;
    private NutsDescriptor effectiveDescriptor;
    private boolean api = false;
    private boolean runtime = false;
    private boolean extension = false;
    private boolean companion = false;
    private NutsId apiId = null;

    public DefaultNutsDefinition() {
    }

    public DefaultNutsDefinition(String repoUuid, String repoName, NutsId id, NutsDescriptor descriptor, NutsContent content, NutsInstallInformation install,
                                 boolean api, boolean runtime,
                                 boolean extension,
                                 boolean companion,
                                 NutsId apiId) {
        this.descriptor = descriptor;
        this.content = content;
        this.id = id;
        this.installInformation = install;
        this.repositoryUuid = repoUuid;
        this.repositoryName = repoName;
        this.api = api;
        this.runtime = runtime;
        this.extension = extension;
        this.companion = companion;
        this.apiId = apiId;
    }

    public DefaultNutsDefinition(NutsDefinition other) {
        if (other != null) {
            this.descriptor = other.getDescriptor();
            this.id = other.getId();
            this.repositoryUuid = other.getRepositoryUuid();
            this.repositoryName = other.getRepositoryName();

            this.content = !other.isSetContent() ? null : other.getContent();
            this.installInformation = !other.isSetInstallInformation() ? null : other.getInstallInformation();
            this.effectiveDescriptor = !other.isSetEffectiveDescriptor() ? null : other.getEffectiveDescriptor();
            this.dependencyNodes = !other.isSetDependencyNodes() ? null : other.getDependencyNodes();
            this.dependencies = !other.isSetDependencies() ? null : other.getDependencies();
            this.apiId=other.getApiId();
            this.api=other.isApi();
            this.runtime=other.isRuntime();
            this.extension=other.isExtension();
            this.companion=other.isCompanion();
        }
    }

    @Override
    public boolean isSetContent() {
        return content != null;
    }

    @Override
    public boolean isSetInstallInformation() {
        return installInformation != null;
    }

    @Override
    public boolean isSetDependencyNodes() {
        return dependencyNodes != null;
    }

    @Override
    public boolean isSetDependencies() {
        return dependencies != null;
    }

    @Override
    public boolean isSetEffectiveDescriptor() {
        return effectiveDescriptor != null;
    }

    @Override
    public String getRepositoryUuid() {
        return repositoryUuid;
    }

    @Override
    public String getRepositoryName() {
        return repositoryName;
    }

    public void setId(NutsId id) {
        this.id = id;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    public boolean isTemporary() {
        return content != null && content.isTemporary();
    }

    public NutsDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public String toString() {
        return "Definition{"
                + " id=" + id
                + ", content=" + content
                + '}';
    }

    public DefaultNutsDefinition copy() {
        return new DefaultNutsDefinition(this);
    }

    @Override
    public Path getPath() {
        return getContent().getPath();
    }

    @Override
    public NutsContent getContent() {
        if (!isSetContent()) {
            throw new NutsException(null, "Unable to get content. You need to call search.content(...) first.");
        }
        return content;
    }

    @Override
    public NutsDescriptor getEffectiveDescriptor() {
        if (!isSetEffectiveDescriptor()) {
            throw new NutsException(null, "Unable to get effectiveDescriptor. You need to call search.effective(...) first.");
        }
        return effectiveDescriptor;
    }

    @Override
    public NutsInstallInformation getInstallInformation() {
        if (!isSetInstallInformation()) {
            throw new NutsException(null, "Unable to get install information. You need to call search.installInformation(...) first.");
        }
        return installInformation;
    }

    @Override
    public NutsDependencyTreeNode[] getDependencyNodes() {
        if (!isSetDependencyNodes()) {
            throw new NutsException(null, "Unable to get dependencyNodes. You need to call search.dependencyNodes(...) first.");
        }
        return dependencyNodes;
    }

    @Override
    public NutsDependency[] getDependencies() {
        if (!isSetDependencies()) {
            throw new NutsException(null, "Unable to get dependencies. You need to call search.dependencies(...) first.");
        }
        return this.dependencies;
    }

    public void setContent(NutsContent content) {
        this.content = content;
    }

    public void setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public int compareTo(NutsDefinition n2) {
        if (n2 == null) {
            return 1;
        }
        if (!(n2 instanceof DefaultNutsDefinition)) {
            return -1;
        }
        NutsId o1 = getId();
        NutsId o2 = ((DefaultNutsDefinition) n2).getId();
        if (o1 == null || o2 == null) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            return 1;
        }
        return o1.toString().compareTo(o2.toString());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultNutsDefinition other = (DefaultNutsDefinition) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    public void setEffectiveDescriptor(NutsDescriptor effectiveDescriptor) {
        this.effectiveDescriptor = effectiveDescriptor;
    }

    public void setInstallInformation(NutsInstallInformation install) {
        this.installInformation = install;
    }

    public void setDependencyNodes(NutsDependencyTreeNode[] dependencyTreeNode) {
        this.dependencyNodes = dependencyTreeNode;
    }

    public void setDependencies(NutsDependency[] dependencies) {
        this.dependencies = dependencies;
    }


    @Override
    public boolean isApi() {
        return api;
    }

    @Override
    public boolean isRuntime() {
        return runtime;
    }

    @Override
    public boolean isExtension() {
        return extension;
    }

    @Override
    public boolean isCompanion() {
        return companion;
    }

    @Override
    public NutsId getApiId() {
        return apiId;
    }

    public DefaultNutsDefinition setRepositoryUuid(String repositoryUuid) {
        this.repositoryUuid = repositoryUuid;
        return this;
    }

    public DefaultNutsDefinition setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
        return this;
    }
}
