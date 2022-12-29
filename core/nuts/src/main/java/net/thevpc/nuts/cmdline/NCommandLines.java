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
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.spi.NComponent;

/**
 * @author thevpc
 * @app.category Command Line
 * @since 0.8.3
 */
public interface NCommandLines extends NComponent {
    static NCommandLines of(NSession session) {
        return NExtensions.of(session).createSupported(NCommandLines.class, session);
    }

    /**
     * return new Command line instance
     *
     * @param line command line to parse
     * @return new Command line instance
     */
    NCommandLine parseCommandline(String line);

    /**
     * create argument name
     *
     * @param type create argument type
     * @return argument name
     */
    default NArgumentName createName(String type) {
        return createName(type, type);
    }

    /**
     * create argument name
     *
     * @param type  argument type
     * @param label argument label
     * @return argument name
     */
    NArgumentName createName(String type, String label);


    NSession getSession();

    NCommandLines setSession(NSession session);

    /**
     * return command line family
     *
     * @return command line family
     * @since 0.8.1
     */
    NShellFamily getShellFamily();

    /**
     * change command line family
     *
     * @param family family
     * @return {@code this} instance
     */
    NCommandLines setShellFamily(NShellFamily family);
}
