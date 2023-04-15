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

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;

import java.util.*;

/**
 * this class is responsible of displaying general information about the current workspace and repositories.
 * Il is invoked by the "info" standard command,
 *
 * @author thevpc
 * @app.category Base
 * @since 0.5.4
 */
public interface NInfoCommand extends NFormat, NWorkspaceCommand {

    static NInfoCommand of(NSession session) {
       return NExtensions.of(session).createComponent(NInfoCommand.class).get();
    }

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NInfoCommand setSession(NSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NInfoCommand configure(boolean skipUnsupported, String... args);

    @Override
    NInfoCommand setNtf(boolean ntf);

    /**
     * include a custom property
     *
     * @param key   custom property key
     * @param value custom property value
     * @return {@code this} instance
     */
    NInfoCommand addProperty(String key, String value);

    /**
     * include custom properties from the given map
     *
     * @param customProperties custom properties
     * @return {@code this} instance
     */
    NInfoCommand addProperties(Map<String, String> customProperties);

    /**
     * return true if displaying repositories is enabled
     *
     * @return true if displaying repositories is enabled
     */
    boolean isShowRepositories();

    /**
     * enable or disable display of all repositories information
     *
     * @param enable if true enable
     * @return {@code this} instance
     */
    NInfoCommand setShowRepositories(boolean enable);

    /**
     * return true if fancy mode armed
     *
     * @return true if fancy mode armed
     */
    boolean isFancy();

    /**
     * enable fancy (custom, pretty) display mode
     *
     * @param fancy if true enable fancy mode
     * @return {@code this} instance
     */
    NInfoCommand setFancy(boolean fancy);


    /**
     * copy session
     *
     * @return {@code this} instance
     */
    NInfoCommand copySession();

    Map<String, Object> getPropertyValues() ;

    NOptional<Object> getPropertyValue(String propertyName) ;
}
