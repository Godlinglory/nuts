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
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsExecutorDescriptor;
import net.vpc.app.nuts.NutsId;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 */
public class DefaultNutsExecutorDescriptor implements NutsExecutorDescriptor, Serializable {

    private static final long serialVersionUID = 1L;

    private final NutsId id;
    private final String[] options;
    private final Properties properties;

    public DefaultNutsExecutorDescriptor(NutsId id) {
        this(id, null, null);
    }

    public DefaultNutsExecutorDescriptor(NutsId id, String[] options) {
        this(id, options, null);
    }

    public DefaultNutsExecutorDescriptor(NutsId id, String[] options, Properties properties) {
        this.id = id;
        this.options = options == null ? new String[0] : options;
        this.properties = properties == null ? new Properties() : properties;
    }

    public NutsId getId() {
        return id;
    }

    public String[] getOptions() {
        return options;
    }

    public Properties getProperties() {
        return properties;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsExecutorDescriptor that = (DefaultNutsExecutorDescriptor) o;
        return Objects.equals(id, that.id) &&
                Arrays.equals(options, that.options) &&
                Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, properties);
        result = 31 * result + Arrays.hashCode(options);
        return result;
    }
}
