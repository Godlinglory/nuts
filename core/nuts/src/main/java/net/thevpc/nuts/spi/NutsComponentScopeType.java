/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsEnum;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.util.NutsUtils;

/**
 * Modes Application can run with
 *
 * @app.category Application
 * @since 0.5.5
 */
public enum NutsComponentScopeType implements NutsEnum {
    /**
     * a new instance will be created per workspace reference
     */
    WORKSPACE,
    /**
     * a new instance will be created per session reference
     */
    SESSION,
    /**
     * a new instance will be created at each call
     */
    PROTOTYPE;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NutsComponentScopeType() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsOptional<NutsComponentScopeType> parse(String value) {
        return NutsUtils.parseEnum(value, NutsComponentScopeType.class);
    }


    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }

}
