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
import net.thevpc.nuts.spi.NutsComponent;

/**
 * @app.category Base
 */
public interface NutsIdParser extends NutsComponent {

    static NutsIdParser of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsIdParser.class,true,null);
    }

    /**
     * return blank mode.
     * when true, null is returned whenever a blank id is encountered.
     * when false, an error is thrown in that case.
     * @return true if the parse is lenient
     */
    boolean isAcceptBlank();

    /**
     * set blank mode.
     * when true, null is returned whenever a blank id is encountered.
     * when false, an error is thrown in that case.
     * @param acceptBlank acceptBlank
     * @return true if the parse is lenient
     */
    NutsIdParser setAcceptBlank(boolean acceptBlank);

    /**
     * return lenient mode.
     * when true, null is returned whenever a non blank id cannot be parsed as a valid nuts id.
     * when false, an error is thrown in that case.
     * @return true if the parse is lenient
     */
    boolean isLenient();

    /**
     * set lenient mode.
     * when true, null is returned whenever a non blank id cannot be parsed as a valid nuts id
     * when false, an error is thrown in that case.
     * @param lenient true if the parse is lenient
     * @return {@code this instance}
     */
    NutsIdParser setLenient(boolean lenient);

    /**
     * parse id or null if not valid.
     * id is parsed in the form
     * group:name#version?key=&lt;value&gt;{@code &}key=&lt;value&gt; ...
     *
     * an error is thrown if not lenient or do not accept blank at the given condition.
     *
     * @param id to parse
     * @return parsed id
     * @throws NutsParseException if the string cannot be evaluated
     */
    NutsId parse(String id);


}
