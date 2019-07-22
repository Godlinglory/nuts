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

import java.nio.file.Path;
import java.util.Properties;

/**
 * @author vpc
 * @since 0.5.4
 */
public interface NutsRepositoryConfigManager extends NutsEnvProvider {

    String getUuid();

    String uuid();

    /**
     * name is the name attributed by the containing workspace. It is defined in
     * NutsRepositoryRef
     *
     * @return local name
     */
    String getName();

    String name();

    /**
     * global name is independent from workspace
     *
     * @return repository global (workspace independent) name
     */
    String getGlobalName();

    String getType();

    String getGroups();

    int getSpeed();

    int getSpeed(boolean transitive);

    void setEnv(String property, String value, NutsUpdateOptions options);

    boolean isTemporary();

    boolean isIndexSubscribed();

    /**
     * return repository configured location as string
     *
     * @param expand when true, location will be expanded (~ and $ params will
     * be expanded)
     * @return repository location
     */
    String getLocation(boolean expand);

    Path getStoreLocation();

    Path getStoreLocation(NutsStoreLocation folderType);

    boolean save(boolean force, NutsSession session);

    void save(NutsSession session);

    Properties getEnv(boolean inherit);

    String getEnv(String key, String defaultValue, boolean inherit);

    NutsRepositoryConfigManager setIndexEnabled(boolean enabled, NutsUpdateOptions options);

    boolean isIndexEnabled();

    NutsRepositoryConfigManager setMirrorEnabled(String repoName, boolean enabled, NutsUpdateOptions options);

    public int getDeployOrder();

    NutsRepositoryConfigManager setEnabled(boolean enabled, NutsUpdateOptions options);

    NutsRepositoryConfigManager setTemporary(boolean enabled, NutsUpdateOptions options);

    boolean isEnabled();

    boolean subscribeIndex();

    NutsRepositoryConfigManager unsubscribeIndex();

    boolean isSupportedMirroring();

    NutsRepository findMirrorById(String repositoryNameOrId, boolean transitive);

    NutsRepository findMirrorByName(String repositoryNameOrId, boolean transitive);

    NutsRepository[] getMirrors();

    /**
     * search for (or throw error) a repository with the given repository name or id.
     * @param repositoryIdOrName repository name or id
     * @param transitive when true, check into mirrors
     * @return found repository or throw an exception
     * @throws NutsRepositoryNotFoundException if not found
     */
    NutsRepository getMirror(String repositoryIdOrName, boolean transitive);

    /**
     * search for (or return null) a repository with the given repository name or id.
     * @param repositoryIdOrName repository name or id
     * @param transitive when true, check into mirrors
     * @return found repository or return null
     */
    NutsRepository findMirror(String repositoryIdOrName, boolean transitive);

    /**
     * add new repository
     * @param definition repository definition
     * @return {@code this} instance
     */
    NutsRepository addMirror(NutsRepositoryDefinition definition);

    /**
     * add new repository
     * @param options repository definition
     * @return {@code this} instance
     */
    NutsRepository addMirror(NutsCreateRepositoryOptions options);

    /**
     *
     * @param repositoryId repository id pr id
     * @param options remove options
     * @return {@code this} instance
     */
    NutsRepositoryConfigManager removeMirror(String repositoryId, NutsRemoveOptions options);

    int getSupportLevel(NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode fetchMode, boolean transitive);

    NutsStoreLocationStrategy getStoreLocationStrategy();
}
