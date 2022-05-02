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
package net.thevpc.nuts.format;

import net.thevpc.nuts.NutsEnum;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.boot.NutsApiUtils;

import java.util.function.Function;

/**
 * Text align constants
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
public enum NutsPositionType implements NutsEnum {
    /**
     * LEFT, TOP
     */
    FIRST,
    /**
     * CENTER
     */
    CENTER,
    /**
     * RIGHT, BOTTOM
     */
    LAST,
    /**
     * Mostly like CENTER but limits centering for huge columns
     */
    HEADER;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsPositionType() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsOptional<NutsPositionType> parse(String value) {
        return NutsApiUtils.parse(value, NutsPositionType.class, new Function<String, NutsOptional<NutsPositionType>>() {
            @Override
            public NutsOptional<NutsPositionType> apply(String s) {
                switch (s.toLowerCase()){
                    case "left":
                    case "top":
                    case "before":
                        return NutsOptional.of(FIRST);
                    case "right":
                    case "bottom":
                        return NutsOptional.of(LAST);
                }
                return null;
            }
        });
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