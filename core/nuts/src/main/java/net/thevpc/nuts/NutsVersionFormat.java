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
 *
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

import java.util.Map;

/**
 *
 * @author thevpc
 * @since 0.5.4
 * @app.category Format
 */
public interface NutsVersionFormat extends NutsFormat {

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsVersionFormat setSession(NutsSession session);

    NutsVersionFormat addProperty(String key, String value);

    NutsVersionFormat addProperties(Map<String,String> p);

    /**
     * return version set by {@link #setVersion(NutsVersion) }
     *
     * @return version set by {@link #setVersion(NutsVersion) }
     */
    NutsVersion getVersion();

    /**
     * set version to print. if null, workspace version will be considered.
     *
     * @param version version to print
     * @return {@code this} instance
     */
    NutsVersionFormat setVersion(NutsVersion version);

    /**
     * return true if version is null (default). In such case, workspace version
     * is considered.
     *
     * @return true if version is null (default)
     */
    boolean isWorkspaceVersion();


    @Override
    public NutsVersionFormat setNtf(boolean ntf);
}
