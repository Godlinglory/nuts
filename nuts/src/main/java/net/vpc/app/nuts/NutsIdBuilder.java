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

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

/**
 * @author vpc
 * @since 0.5.4
 */
public interface NutsIdBuilder extends Serializable {

    NutsIdBuilder groupId(String newGroupId);

    NutsIdBuilder setGroupId(String newGroupId);

    NutsIdBuilder namespace(String newNamespace);

    NutsIdBuilder setNamespace(String newNamespace);

    NutsIdBuilder version(String newVersion);

    NutsIdBuilder setVersion(String newVersion);

    NutsIdBuilder version(NutsVersion version);

    NutsIdBuilder setVersion(NutsVersion version);

    NutsIdBuilder artifactId(String newName);

    NutsIdBuilder setArtifactId(String newName);

    String getFace();

//    String getAlternative();

    String getOs();

    String getOsdist();

    String getPlatform();

    String getArch();

    String getClassifier();

    NutsIdBuilder faceContent();

    NutsIdBuilder setFaceContent();

    NutsIdBuilder faceDescriptor();

    NutsIdBuilder setFaceDescriptor();

    NutsIdBuilder face(String value);

    NutsIdBuilder setFace(String value);

    NutsIdBuilder classifier(String value);

    NutsIdBuilder setClassifier(String value);

    NutsIdBuilder platform(String value);

    NutsIdBuilder setPlatform(String value);

    NutsIdBuilder arch(String value);

    NutsIdBuilder setArch(String value);

    NutsIdBuilder os(String value);

    NutsIdBuilder setOs(String value);

    NutsIdBuilder osdist(String value);

    NutsIdBuilder setOsdist(String value);

    NutsIdBuilder property(String property, String value);

    NutsIdBuilder setProperty(String property, String value);

    NutsIdBuilder properties(Map<String, String> queryMap);

    NutsIdBuilder setProperties(Map<String, String> queryMap);

    NutsIdBuilder addProperties(Map<String, String> queryMap);

    NutsIdBuilder properties(String query);

    NutsIdBuilder setProperties(String query);

    NutsIdBuilder addProperties(String query);

    NutsIdBuilder packaging(String packaging);

    NutsIdBuilder setPackaging(String packaging);

    String getPropertiesQuery();

    Map<String, String> getProperties();

    String getNamespace();

    String getGroupId();

    String getLongName();

    String getShortName();

    String getFullName();

    String getArtifactId();

    NutsVersion getVersion();

    NutsIdBuilder apply(Function<String, String> properties);

    NutsIdBuilder id(NutsId id);

    NutsIdBuilder set(NutsId id);

    NutsIdBuilder id(NutsIdBuilder id);

    NutsIdBuilder set(NutsIdBuilder id);

    /**
     * clear this instance (set null/default all properties)
     *
     * @return {@code this instance}
     */
    NutsIdBuilder clear();

    NutsId build();
}
