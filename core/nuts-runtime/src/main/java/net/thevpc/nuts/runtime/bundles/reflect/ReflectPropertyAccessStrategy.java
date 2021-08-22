/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.bundles.reflect;

import net.thevpc.nuts.NutsEnum;

/**
 *
 * @author vpc
 */
public enum ReflectPropertyAccessStrategy  implements NutsEnum {
    FIELD,
    METHOD,
    BOTH;
    private String id;

    ReflectPropertyAccessStrategy() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    @Override
    public String id() {
        return id;
    }

    public static ReflectPropertyAccessStrategy parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static ReflectPropertyAccessStrategy parseLenient(String value, ReflectPropertyAccessStrategy emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static ReflectPropertyAccessStrategy parseLenient(String value, ReflectPropertyAccessStrategy emptyValue, ReflectPropertyAccessStrategy errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return ReflectPropertyAccessStrategy.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }
}
