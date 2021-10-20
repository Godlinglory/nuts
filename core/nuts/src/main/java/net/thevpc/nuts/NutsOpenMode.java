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
package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

/**
 * @author thevpc
 * @app.category Config
 * @since 0.5.4 (renamed from NutsWorkspaceOpenMode to NutsOpenMode in 0.8.1)
 */
public enum NutsOpenMode implements NutsEnum {
    /**
     * Open or Create. Default Mode. If the workspace is found, it will be
     * created otherwise it will be opened
     * if not exists, then create and open
     * if exists, then error
     */
    OPEN_OR_CREATE,
    /**
     * Create Workspace (if not found) or throw Error (if found)
     * if not exists, then create and open
     * if exists, then error
     */
    CREATE_OR_ERROR,

    /**
     * Open Workspace (if found) or throw Error (if not found)
     * if not exists, then error
     * if exists, then open
     */
    OPEN_OR_ERROR,

    /**
     * Open Workspace (if found) or return null
     * if not exists, then open
     * if exists, then return null
     */
    OPEN_OR_NULL;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsOpenMode() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsOpenMode parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsOpenMode parseLenient(String value, NutsOpenMode emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsOpenMode parseLenient(String value, NutsOpenMode emptyValue, NutsOpenMode errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        switch (value) {
            case "R":
            case "READ":
            case "O":
            case "OE":
            case "O_E":
            case "OPEN":
            case "OPEN_ERROR":
            case "OPEN_OR_ERROR":
            case "EXISTING": {
                return NutsOpenMode.OPEN_OR_ERROR;
            }
            case "W":
            case "WRITE":
            case "C":
            case "CE":
            case "C_E":
            case "CREATE":
            case "CREATE_ERROR":
            case "CREATE_OR_ERROR":
            case "NEW": {
                return NutsOpenMode.CREATE_OR_ERROR;
            }
            case "RW":
            case "R_W":
            case "READ_WRITE":
            case "OC":
            case "O_C":
            case "OPEN_CREATE":
            case "OPEN_OR_CREATE":
            case "AUTO":
            case "AUTO_CREATE": {
                return NutsOpenMode.OPEN_OR_CREATE;
            }
            case "ON":
            case "O_N":
            case "OPEN_NULL":
            case "OPEN_OR_NULL":
            case "TRY": {
                return NutsOpenMode.OPEN_OR_NULL;
            }
        }
        try {
            return NutsOpenMode.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    public static NutsOpenMode parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsOpenMode parse(String value, NutsOpenMode emptyValue, NutsSession session) {
        NutsOpenMode v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v, value, NutsOpenMode.class, session);
        return v;
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
