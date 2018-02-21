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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

class NutsWorkspaceClassPath {

    private WorkspaceNutsId id;
    private String dependencies;
    private String repositories;

    public NutsWorkspaceClassPath(File url) throws IOException {
        this(url.toURI().toURL());
    }

    public NutsWorkspaceClassPath(URL url) throws IOException {
        this(IOUtils.loadURLProperties(url));
    }

    public NutsWorkspaceClassPath(Properties properties) {
        this(
                properties.getProperty("project.id"),
                properties.getProperty("project.version"),
                properties.getProperty("project.dependencies.compile"),
                properties.getProperty("project.repositories")
        );
    }

    public NutsWorkspaceClassPath(String id, String version, String dependencies, String repositories) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("Missing id");
        }
        if (StringUtils.isEmpty(version)) {
            throw new IllegalArgumentException("Missing version");
        }
        if (StringUtils.isEmpty(dependencies)) {
            throw new IllegalArgumentException("Missing dependencies");
        }
        if (StringUtils.isEmpty(repositories)) {
            throw new IllegalArgumentException("Missing dependencies");
        }
        this.dependencies = dependencies;
        this.repositories = repositories;
        this.id = WorkspaceNutsId.parse(id + "#" + version);
    }

    public WorkspaceNutsId getId() {
        return id;
    }

    public String getRepositoriesString() {
        return repositories;
    }

    public String getDependenciesString() {
        return dependencies;
    }

    public WorkspaceNutsId[] getDependenciesArray() {
        String[] split = dependencies.split("[; ]");
        List<WorkspaceNutsId> ts = new ArrayList<>();
        for (String s : split) {
            s = s.trim();
            if (!s.isEmpty()) {
                ts.add(WorkspaceNutsId.parse(s));
            }
        }
        return ts.toArray(new WorkspaceNutsId[ts.size()]);
    }

    public String[] getRepositoriesArray() {
        List<String> ts = Arrays.asList(repositories.split(";"));
        return ts.toArray(new String[ts.size()]);
    }
}
