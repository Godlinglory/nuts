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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

class WorkspaceClassPath {
    private WorkspaceNutsId id;
    private String dependencies;
    private String repositories;

    public WorkspaceClassPath(File url) throws IOException {
        this(url.toURI().toURL());
    }

    public WorkspaceClassPath(URL url) throws IOException {
        this(IOUtils.loadProperties(url));
    }

    public WorkspaceClassPath(Properties properties) {
        this(properties.getProperty("project.id"), properties.getProperty("project.dependencies"), properties.getProperty("project.repositories"));
    }

    public WorkspaceClassPath(String id, String dependencies, String repositories) {
        this.dependencies = dependencies;
        this.repositories = repositories;
        this.id = WorkspaceNutsId.parse(id);
        if (StringUtils.isEmpty(id)) {
            throw new NutsIllegalArgumentsException("Empty id");
        }
        if (StringUtils.isEmpty(repositories)) {
            throw new NutsIllegalArgumentsException("Empty repositories");
        }
    }

//    public String getCoreName() {
//        return id.groupId + ":" + id.artifactId;
//    }

    public WorkspaceNutsId getId() {
        return id;
    }

//    public String getGroupId() {
//        return groupId;
//    }
//
//    public String getArtifactId() {
//        return artifactId;
//    }
//
//    public String getVersion() {
//        return version;
//    }
//
//    public String getDependenciesString() {
//        return dependencies;
//    }

    public String getRepositoriesString() {
        return repositories;
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
