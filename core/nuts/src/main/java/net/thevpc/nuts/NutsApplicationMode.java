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
 * Modes Application can run with
 *
 * @author thevpc
 * @app.category Application
 * @since 0.5.5
 */
public enum NutsApplicationMode implements NutsEnum {
    /**
     * Default application execution Mode. This mode is considered if the
     * --nuts-exec-mode=... is not present (or is not the very first argument).
     */
    RUN,
    /**
     * application execution Mode in auto-complete mode in which case
     * application MUST accept FIRST argument in the form of
     * "--nuts-exec-mode=auto-complete &lt;WORD-INDEX&gt;" where &lt;WORD-INDEX&gt; is
     * an optional argument to auto-complete mode. It is important to notice
     * that "--nuts-exec-mode=auto-complete &lt;WORD-INDEX&gt;" is a SINGLE
     * argument, so spaces must be escaped.
     */
    AUTO_COMPLETE,
    /**
     * application execution Mode in install mode in which case application MUST
     * accept FIRST argument in the form of "--nuts-exec-mode=install &lt;ARG&gt;
     * ..." where &lt;ARG&gt; arg an optional arguments to install mode. It is
     * important to notice that "--nuts-exec-mode=install &lt;ARG&gt; ..." is a
     * SINGLE argument, so spaces must be escaped.
     */
    INSTALL,
    /**
     * application execution Mode in uninstall mode in which case application
     * MUST accept FIRST argument in the form of "--nuts-exec-mode=uninstall
     * &lt;ARG&gt; ..." where &lt;ARG&gt; arg an optional arguments to uninstall mode.
     * It is important to notice that "--nuts-exec-mode=install &lt;ARG&gt; ..." is
     * a SINGLE argument, so spaces must be escaped.
     */
    UNINSTALL,
    /**
     * application execution Mode in update mode in which case application MUST
     * accept FIRST argument in the form of "--nuts-exec-mode=update &lt;ARG&gt;
     * ..." where &lt;ARG&gt; arg an optional arguments to update mode. It is
     * important to notice that "--nuts-exec-mode=install &lt;ARG&gt; ..." is a
     * SINGLE argument, so spaces must be escaped.
     */
    UPDATE;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NutsApplicationMode() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    /**
     * parse string and return null if parse fails
     *
     * @param value value to parse
     * @return parsed instance or null
     */
    public static NutsApplicationMode parseLenient(String value) {
        return parseLenient(value, null);
    }

    /**
     * parse string and return {@code emptyOrErrorValue} if parse fails
     *
     * @param emptyOrErrorValue emptyOrErrorValue
     * @param value             value to parse
     * @return parsed instance or {@code emptyOrErrorValue}
     */
    public static NutsApplicationMode parseLenient(String value, NutsApplicationMode emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    /**
     * parse string and return {@code emptyValue} when null or {@code errorValue} if parse fails
     *
     * @param value      value to parse
     * @param emptyValue value when the value is null or empty
     * @param errorValue value when the value cannot be parsed
     * @return parsed value
     */
    public static NutsApplicationMode parseLenient(String value, NutsApplicationMode emptyValue, NutsApplicationMode errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsApplicationMode.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    public static NutsApplicationMode parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsApplicationMode parse(String value, NutsApplicationMode emptyValue, NutsSession session) {
        NutsApplicationMode v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v, value, NutsApplicationMode.class, session);
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
