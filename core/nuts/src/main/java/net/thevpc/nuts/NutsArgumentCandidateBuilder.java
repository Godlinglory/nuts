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

import java.io.Serializable;

/**
 * @app.category Application
 */
public interface NutsArgumentCandidateBuilder extends Serializable {

    static NutsArgumentCandidateBuilder of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().commandLine().createCandidate();
    }

    /**
     * argument value
     *
     * @return argument value
     */
    String getValue();

    /**
     * set value
     * @param value value
     * @return {@code this} instance
     */
    NutsArgumentCandidateBuilder setValue(String value);

    /**
     * human display
     *
     * @return human display
     */
    String getDisplay();

    /**
     * set display
     * @param value value
     * @return {@code this} instance
     */
    NutsArgumentCandidateBuilder setDisplay(String value);

    /**
     * set all values from the given candidate
     * @param value value
     * @return {@code this} instance
     */
    NutsArgumentCandidateBuilder setAll(NutsArgumentCandidate value);

    /**
     * build a candidate
     * @return candidate
     */
    NutsArgumentCandidate build();

}
