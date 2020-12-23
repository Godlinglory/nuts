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
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for managing a Workspace list
 *
 * @author Nasreddine Bac Ali
 * date 2019-03-02
 * @since 0.5.4
 * @category Config
 */
public class NutsWorkspaceListConfig implements Serializable {

    private static final long serialVersionUID = 2;
    private String uuid;
    private String name;
    private List<NutsWorkspaceLocation> workspaces;

    public NutsWorkspaceListConfig() {
    }

    public NutsWorkspaceListConfig(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public NutsWorkspaceListConfig(NutsWorkspaceListConfig other) {
        this.uuid = other.getUuid();
        this.name = other.getName();
        this.workspaces = other.getWorkspaces() == null ? null : new ArrayList<>(other.getWorkspaces());
    }

    public String getUuid() {
        return uuid;
    }

    public NutsWorkspaceListConfig setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getName() {
        return name;
    }

    public NutsWorkspaceListConfig setName(String name) {
        this.name = name;
        return this;
    }

    public List<NutsWorkspaceLocation> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<NutsWorkspaceLocation> workspaces) {
        this.workspaces = workspaces;
    }

    @Override
    public String toString() {
        return "NutsWorkspaceListConfig{" + "uuid=" + uuid + ", name=" + name + ", workspaces=" + workspaces + '}';
    }
}
