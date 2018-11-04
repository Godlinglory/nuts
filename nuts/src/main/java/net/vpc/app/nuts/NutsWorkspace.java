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
package net.vpc.app.nuts;

import java.util.*;

/**
 * Created by vpc on 1/5/17.
 */
@Prototype
public interface NutsWorkspace extends NutsComponent<NutsBootWorkspace> {

    NutsWorkspace openWorkspace(String workspace, NutsWorkspaceCreateOptions options);

    Iterator<NutsId> findIterator(NutsSearch search, NutsSession session);

    NutsId findFirst(NutsSearch search, NutsSession session);

    NutsId findOne(NutsSearch search, NutsSession session);

    List<NutsId> find(NutsSearch search, NutsSession session);

    String copyTo(String id, String localPath, NutsSession session);

    NutsFile fetch(String id, NutsSession session);

    NutsFile[] fetchDependencies(NutsDependencySearch search, NutsSession session);

    NutsFile fetchWithDependencies(String id, NutsSession session);

    NutsDescriptor fetchDescriptor(String id, boolean effective, NutsSession session);

    String fetchHash(String id, NutsSession session);

    String fetchDescriptorHash(String id, NutsSession session);

    boolean isFetched(String id, NutsSession session);

    NutsId resolveId(String id, NutsSession session);

    NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsSession session);

    NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session);

    NutsFile updateWorkspace(String nutsVersion, boolean force, NutsSession session);

    NutsUpdate[] checkWorkspaceUpdates(boolean applyUpdates, String[] args, NutsSession session);

    NutsUpdate checkUpdates(String id, NutsSession session);

    NutsFile update(String id, boolean force, NutsSession session);

    NutsFile[] update(String[] toUpdateIds, String[] toRetainDependencies, boolean force, NutsSession session);

    NutsFile install(String id, boolean force, NutsSession session);

    NutsFile checkout(String id, String folder, NutsSession session);

    NutsId commit(String folder, NutsSession session);

    boolean isInstalled(String id, boolean checkDependencies, NutsSession session);

    boolean uninstall(String id, NutsSession session);

    void push(String id, String repoId, boolean force, NutsSession session);

    /**
     * creates a zip file based on the folder. The folder should contain a
     * descriptor file at its root
     *
     * @param contentFolder folder to bundle
     * @param destFile      created bundle file or null to create a file with the
     *                      very same name as the folder
     * @param session       current session
     * @return bundled nuts file, the nuts is neither deployed nor installed!
     */
    NutsFile createBundle(String contentFolder, String destFile, NutsSession session);

    NutsId deploy(NutsDeployment deployment, NutsSession session);

    /**
     * out and err are copied to string result
     *
     * @param cmd
     * @param env
     * @param session
     * @return
     */
    public NutsExecResult execToString(String[] cmd, Properties env, String dir, NutsSession session);

    int exec(String[] cmd, Properties env, String dir, NutsSession session);

    /**
     * exec another instance of nuts
     *
     * @param nutsJarFile
     * @param args
     * @param copyCurrentToFile
     * @param waitFor
     * @param session
     * @return
     */
    int exec(String nutsJarFile, String[] args, boolean copyCurrentToFile, boolean waitFor, NutsSession session);

    NutsWorkspaceRepositoryManager getRepositoryManager();

    NutsSession createSession();

    NutsWorkspaceExtensionManager getExtensionManager();

    NutsWorkspaceConfigManager getConfigManager();

    NutsWorkspaceSecurityManager getSecurityManager();

    String getStoreRoot();

    String getStoreRoot(NutsId id);

    String getStoreRoot(String id);

    NutsFile fetchBootFile(NutsSession session);

    void removeWorkspaceListener(NutsWorkspaceListener listener);

    void addWorkspaceListener(NutsWorkspaceListener listener);

    NutsWorkspaceListener[] getWorkspaceListeners();

    NutsId getBootId();

    NutsId getRuntimeId();

    NutsId resolveNutsIdForClass(Class clazz);

    NutsId[] resolveNutsIdsForClass(Class clazz);

    NutsId parseNutsId(String id);

    String getPlatformOs();

    String getPlatformOsDist();

    String getPlatformOsLib();

    String getPlatformArch();

    ClassLoader createClassLoader(String[] nutsIds, ClassLoader parentClassLoader, NutsSession session);

    ClassLoader createClassLoader(String[] nutsIds, NutsDependencyScope scope, ClassLoader parentClassLoader, NutsSession session);

    String resolvePath(String path);

    String resolveRepositoryPath(String location);
}
