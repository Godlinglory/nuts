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
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by vpc on 1/6/17.
 */
public class DefaultNutsDefinition implements NutsDefinition{

    private NutsId id;
    private NutsDescriptor descriptor;
    private NutsDescriptor effectiveDescriptor;
    private NutsContent content;
    private NutsInstallInfo install;
    private NutsRepository repository;
    private NutsWorkspace ws;

    public DefaultNutsDefinition(NutsWorkspace ws,NutsRepository repo,NutsId id, NutsDescriptor descriptor, NutsContent content,NutsInstallInfo install) {
        this.descriptor = descriptor;
        this.content = content;
        this.id = id;
        this.install = install;
        this.ws = ws;
        this.repository = repo;
    }

    public DefaultNutsDefinition(DefaultNutsDefinition other) {
        if (other != null) {
            this.descriptor = other.descriptor;
            this.content = other.content;
            this.id = other.id;
            this.install = other.install;
            this.ws = other.ws;
            this.repository = other.repository;
            this.effectiveDescriptor = other.effectiveDescriptor;
        }
    }

    public void setId(NutsId id) {
        this.id = id;
    }

    public NutsId getId() {
        return id;
    }

    public boolean isTemporary() {
        return content != null && content.isTemporary();
    }

    public NutsDescriptor getDescriptor() {
        return descriptor;
    }

    public String getFile() {
        return content == null ?null: content.getFile();
    }

    @Override
    public String toString() {
        return "NutsDefinition{"
                + " id=" + id
                + ", file=" + content
                + '}';
    }

    public DefaultNutsDefinition copy() {
        return new DefaultNutsDefinition(this);
    }

    public NutsContent getContent() {
        return content;
    }

    public void setContent(NutsContent content) {
        this.content = content;
    }

    public void setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public NutsDescriptor getEffectiveDescriptor() {
        if(effectiveDescriptor==null){
            effectiveDescriptor=ws.resolveEffectiveDescriptor(getDescriptor(),null);
        }
        return effectiveDescriptor;
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

    public void setEffectiveDescriptor(NutsDescriptor effectiveDescriptor) {
        this.effectiveDescriptor = effectiveDescriptor;
    }

    public NutsRepository getRepository() {
        return repository;
    }

    public void setRepository(NutsRepository repository) {
        this.repository = repository;
    }

    public void setInstallation(NutsInstallInfo install) {
        this.install = install;
    }

    @Override
    public NutsInstallInfo getInstallation() {
        return install;
    }
}
