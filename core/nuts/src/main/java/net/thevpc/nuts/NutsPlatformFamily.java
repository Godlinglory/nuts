/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NutsEnum;
import net.thevpc.nuts.util.NutsNameFormat;
import net.thevpc.nuts.util.NutsStringUtils;

/**
 * uniform platform
 *
 * @author thevpc
 * @app.category Base
 * @since 0.8.1
 */
public enum NutsPlatformFamily implements NutsEnum {
    JAVA,
    DOTNET,
    PYTHON,
    JAVASCRIPT,
    UNKNOWN;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsPlatformFamily() {
        this.id = NutsNameFormat.ID_NAME.format(name());
    }

    public static NutsOptional<NutsPlatformFamily> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsPlatformFamily.class, s->{
            switch (s.getNormalizedValue()) {
                case "JAVA":
                case "JAVAW":
                case "JRE":
                case "JDK":
                case "OPENJDK":
                    return NutsOptional.of(JAVA);

                case "NET":
                case "DOTNET":
                    return NutsOptional.of(DOTNET);

                case "PYTHON":
                    return NutsOptional.of(PYTHON);

                case "JS":
                case "JAVASCRIPT":
                    return NutsOptional.of(JAVASCRIPT);

                case "UNKNOWN":
                    return NutsOptional.of(UNKNOWN);
            }
            return null;
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
