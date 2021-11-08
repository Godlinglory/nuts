/**
 * ====================================================================
 * vpc-common-io : common reusable library for
 * input/output
 * <p>
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

import java.util.Map;

/**
 * Class formatting Map/Properties objects
 * @app.category Format
 */
public interface NutsPropertiesFormat extends NutsContentTypeFormat {
    static NutsPropertiesFormat of(NutsSession session){
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsPropertiesFormat.class, true, null);
    }

    /**
     * return model to format
     * @return model to format
     */
    Map<?, ?> getModel();

    /**
     * return true is key has to be sorted when formatting
     * @return true is key has to be sorted when formatting
     */
    boolean isSorted();

    /**
     * enable/disable key sorting
     * @param sort when true enable sorting
     * @return {@code this} instance
     */
    NutsPropertiesFormat setSorted(boolean sort);

    /**
     * return key/value separator, default is " = "
     * @return key/value separator
     */
    String getSeparator();

    /**
     * set key/value separator
     * @param separator key/value separator
     * @return {@code this} instance
     */
    NutsPropertiesFormat setSeparator(String separator);

    @Override
    NutsPropertiesFormat setValue(Object value);

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsPropertiesFormat setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsPropertiesFormat configure(boolean skipUnsupported, String... args);

    @Override
    NutsPropertiesFormat setNtf(boolean ntf);

}
