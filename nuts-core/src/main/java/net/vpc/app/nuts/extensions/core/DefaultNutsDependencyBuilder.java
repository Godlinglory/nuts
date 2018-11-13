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
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyBuilder;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.common.strings.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsDependencyBuilder implements NutsDependencyBuilder {

    private String namespace;
    private String group;
    private String name;
    private NutsVersion version;
    private String scope;
    private String optional;
    private NutsId[] exclusions;

    public DefaultNutsDependencyBuilder() {
    }

    public DefaultNutsDependencyBuilder(NutsDependencyBuilder other) {
        if (other != null) {
            setNamespace(other.getNamespace());
            setGroup(other.getGroup());
            setName(other.getNamespace());
            setVersion(other.getVersion());
            setScope(other.getScope());
            setOptional(other.getOptional());
            setExclusions(other.getExclusions());
            setName(other.getName());
        }
    }

    public DefaultNutsDependencyBuilder(NutsDependency other) {
        if (other != null) {
            setNamespace(other.getNamespace());
            setGroup(other.getGroup());
            setName(other.getNamespace());
            setVersion(other.getVersion());
            setScope(other.getScope());
            setOptional(other.getOptional());
            setExclusions(other.getExclusions());
            setName(other.getName());
        }
    }

    @Override
    public NutsDependencyBuilder setNamespace(String namespace) {
        this.namespace = StringUtils.trimToNull(namespace);
        return this;
    }

    @Override
    public NutsDependencyBuilder setGroup(String group) {
        this.group = StringUtils.trimToNull(group);
        return this;
    }

    @Override
    public NutsDependencyBuilder setName(String name) {
        this.name = StringUtils.trimToNull(name);
        return this;
    }

    @Override
    public NutsDependencyBuilder setVersion(NutsVersion version) {
        this.version = version;
        return this;
    }

    @Override
    public NutsDependencyBuilder setVersion(String version) {
        this.version = StringUtils.isEmpty(version) ? null : new NutsVersionImpl(StringUtils.trimToNull(version));
        return this;
    }

    @Override
    public NutsDependencyBuilder setScope(String scope) {
        String s = StringUtils.trimToNull(scope);
        this.scope = StringUtils.isEmpty(s) ? "compile" : s;
        return this;
    }

    @Override
    public NutsDependencyBuilder setOptional(String optional) {
        this.optional = StringUtils.isEmpty(optional) ? "false" : StringUtils.trim(optional);
        return this;
    }

    @Override
    public NutsDependencyBuilder setExclusions(NutsId[] exclusions) {
        if (exclusions != null) {
            exclusions = Arrays.copyOf(exclusions, exclusions.length);
        }
        this.exclusions = exclusions;
        return this;
    }

    @Override
    public boolean isOptional() {
        return Boolean.parseBoolean(optional);
    }

    @Override
    public String getOptional() {
        return optional;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public NutsId toId() {
        return new NutsIdImpl(
                getNamespace(),
                getGroup(),
                getName(),
                getVersion().getValue(),
                ""
        );
    }

    @Override
    public NutsDependency build() {
        return new NutsDependencyImpl(
                getNamespace(), getGroup(), getName(),
                getVersion() == null ? null : getVersion().getValue(),
                getScope(),
                getOptional(),
                getExclusions()
        );
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFullName() {
        if (StringUtils.isEmpty(group)) {
            return StringUtils.trim(name);
        }
        return StringUtils.trim(group) + ":" + StringUtils.trim(name);
    }

    @Override
    public NutsVersion getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(namespace)) {
            sb.append(namespace).append("://");
        }
        if (!StringUtils.isEmpty(group)) {
            sb.append(group).append(":");
        }
        sb.append(name);
        if (!StringUtils.isEmpty(version.getValue())) {
            sb.append("#").append(version);
        }
        Map<String, String> p = new TreeMap<>();
        if (!StringUtils.isEmpty(scope)) {
            if (!scope.equals("compile")) {
                p.put("scope", scope);
            }
        }
        if (!StringUtils.isEmpty(optional)) {
            if (!optional.equals("false")) {
                p.put("optional", optional);
            }
        }
        if (!p.isEmpty()) {
            sb.append("?");
            int i = 0;
            for (Map.Entry<String, String> e : p.entrySet()) {
                if (i > 0) {
                    sb.append('&');
                }
                sb.append(CoreStringUtils.simpleQuote(e.getKey(), true, "&="));
                if (e.getValue() != null) {
                    sb.append('=');
                    sb.append(CoreStringUtils.simpleQuote(e.getValue(), true, "&="));
                }
                i++;
            }
        }
        return sb.toString();
    }

    @Override
    public NutsId[] getExclusions() {
        return exclusions == null ? new NutsId[0] : Arrays.copyOf(exclusions, exclusions.length);
    }
}
