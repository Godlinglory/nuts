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

import java.util.Map;

/**
 * Dependency Builder (mutable).
 * User should use available 'set' method and finally call {@link #build()}
 * to get an instance of immutable NutsDependency
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsDependencyBuilder {

    /**
     * set namespace value
     *
     * @param namespace new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder namespace(String namespace);

    /**
     * set namespace value
     *
     * @param namespace new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setNamespace(String namespace);

    /**
     * set group value
     *
     * @param groupId new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setGroupId(String groupId);

    /**
     * set group value
     *
     * @param groupId new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder groupId(String groupId);

    /**
     * set name value
     *
     * @param artifactId new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setArtifactId(String artifactId);

    /**
     * set name value
     *
     * @param artifactId new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder artifactId(String artifactId);

    /**
     * set version value
     *
     * @param version new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setVersion(NutsVersion version);

    /**
     * set version value
     *
     * @param version new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder version(NutsVersion version);

    /**
     * set version value
     *
     * @param version new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setVersion(String version);

    /**
     * set version value
     *
     * @param version new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder version(String version);

    /**
     * set id value
     *
     * @param id new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder id(NutsId id);

    /**
     * set id value
     *
     * @param id new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setId(NutsId id);

    /**
     * set scope value
     *
     * @param scope new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder scope(String scope);

    /**
     * set scope value
     *
     * @param scope new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setScope(String scope);

    /**
     * set optional value
     *
     * @param optional new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder optional(String optional);

    /**
     * set optional value
     *
     * @param optional new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setOptional(String optional);

    /**
     * set classifier value
     *
     * @param classifier new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setClassifier(String classifier);

    /**
     * set classifier value
     *
     * @param classifier new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder classifier(String classifier);

    /**
     * set exclusions value
     *
     * @param exclusions new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setExclusions(NutsId[] exclusions);

    /**
     * set exclusions value
     *
     * @param exclusions new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder exclusions(NutsId[] exclusions);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder dependency(NutsDependencyBuilder value);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder set(NutsDependencyBuilder value);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder set(NutsDependency value);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder dependency(NutsDependency value);

    /**
     * reset this instance
     *
     * @return {@code this} instance
     */
    NutsDependencyBuilder clear();

    /**
     * true if this dependency is optional.
     * equivalent to {@code Boolean.parseBoolean(getOptional())}
     *
     * @return true if this dependency is optional.
     */
    boolean isOptional();

    /**
     * return optional
     *
     * @return optional
     */
    String getOptional();

    /**
     * return scope
     *
     * @return scope
     */
    String getScope();

    /**
     * return id
     *
     * @return id
     */
    NutsId getId();

    /**
     * return namespace
     *
     * @return namespace
     */
    String getNamespace();

    /**
     * return group
     *
     * @return group
     */
    String getGroupId();

    /**
     * return name
     *
     * @return name
     */
    String getArtifactId();

    /**
     * return classifier
     *
     * @return classifier
     */
    String getClassifier();

    /**
     * return full name
     *
     * @return full name
     */
    String getFullName();

    /**
     * return version
     *
     * @return version
     */
    NutsVersion getVersion();

    /**
     * return exclusions
     *
     * @return exclusions
     */
    NutsId[] getExclusions();

    /**
     * build new instance of NutsDependencies
     *
     * @return new instance of NutsDependencies
     */
    NutsDependency build();

    NutsDependencyBuilder property(String property, String value);

    NutsDependencyBuilder setProperty(String property, String value);

    NutsDependencyBuilder properties(Map<String, String> queryMap);

    NutsDependencyBuilder setProperties(Map<String, String> queryMap);

    NutsDependencyBuilder addProperties(Map<String, String> queryMap);

    NutsDependencyBuilder properties(String propertiesQuery);

    NutsDependencyBuilder setProperties(String propertiesQuery);

    NutsDependencyBuilder addProperties(String propertiesQuery);

    String getPropertiesQuery();

    Map<String, String> getProperties();
}
