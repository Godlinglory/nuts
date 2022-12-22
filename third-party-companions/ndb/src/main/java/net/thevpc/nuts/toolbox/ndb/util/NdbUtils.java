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
package net.thevpc.nuts.toolbox.ndb.util;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;

/**
 *
 * @author thevpc
 */
public class NdbUtils {
    public static final String SERVER_CONFIG_EXT = ".config";

    public static String checkName(String name, NutsSession session) {
        if (!isName(name)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("invalid name %s", name));
        }
        return name;
    }

    public static boolean isName(String name) {
        if (NutsBlankable.isBlank(name)) {
            return false;
        }
        return name.matches("[a-zA-Z][a-zA-Z0-9_-]*");
    }

    public static String coalesce(String... cmd) {
        for (String string : cmd) {
            if (!NutsBlankable.isBlank(string)) {
                return string;
            }
        }
        return null;
    }
}
