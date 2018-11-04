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

import java.util.Iterator;

/**
 * Created by vpc on 1/5/17.
 */
public interface NutsRepository {

    int SPEED_FASTEST = 1;
    int SPEED_FASTER = 100;
    int SPEED_FAST = 1000;
    int SPEED_SLOW = 10000;
    int SPEED_SLOWEST = 100000;

    String getRepositoryType();

    String getRepositoryId();

    String getRepositoryLocation();

    NutsWorkspace getWorkspace();

    NutsRepository getParentRepository();

    NutsRepositoryConfigManager getConfigManager();

    NutsRepositorySecurityManager getSecurityManager();

    /**
     * @param id descriptor Id, mandatory as descriptor may not being effective
     * (id is variable or inherited)
     * @param descriptor
     * @param file
     * @return
     */
    NutsId deploy(NutsId id, NutsDescriptor descriptor, String file, boolean force, NutsSession context);

    void push(NutsId id, String repoId, boolean force, NutsSession session);

    NutsFile fetch(NutsId id, NutsSession session);

    NutsDescriptor fetchDescriptor(NutsId id, NutsSession session);

    String copyTo(NutsId id, String localPath, NutsSession session);

    String copyDescriptorTo(NutsId id, String localPath, NutsSession session);

    String fetchHash(NutsId id, NutsSession session);

    String fetchDescriptorHash(NutsId id, NutsSession session);

    Iterator<NutsId> find(NutsIdFilter filter, NutsSession session);

    Iterator<NutsId> findVersions(NutsId id, NutsIdFilter idFilter, NutsSession session);

    int getSupportLevel(NutsId id, NutsSession session);

    boolean isSupportedMirroring();

    NutsRepository[] getMirrors();

    /**
     *
     * @param repositoryIdPath
     * @return
     */
    NutsRepository getMirror(String repositoryIdPath);

    /**
     *
     * @param repositoryId
     * @param location
     * @param type
     * @param autoCreate
     * @return
     */
    NutsRepository addMirror(String repositoryId, String location, String type, boolean autoCreate);

    /**
     *
     * @param repositoryId
     */
    void removeMirror(String repositoryId);

    /**
     *
     * @return
     */
    boolean save();

    /**
     *
     * @param autoCreate
     */
    void open(boolean autoCreate);

    /**
     *
     * @param listener
     */
    void removeRepositoryListener(NutsRepositoryListener listener);

    /**
     *
     * @param listener
     */
    void addRepositoryListener(NutsRepositoryListener listener);

    /**
     *
     * @return
     */
    NutsRepositoryListener[] getRepositoryListeners();

    /**
     *
     * @param enabled
     */
    void setEnabled(boolean enabled);

    /**
     * true if this repository is enabled
     *
     * @return
     */
    boolean isEnabled();

    int getSpeed();
}
