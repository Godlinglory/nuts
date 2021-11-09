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

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Base Nuts Exception Interface. Parent of all Nuts defined Exceptions.
 *
 * @author thevpc
 * @app.category Exceptions
 * @since 0.5.4
 */
public interface NutsExceptionBase {
    static NutsExceptionBase detectExceptionBase(Throwable th) {
        Set<Throwable> visited = new HashSet<>();
        Stack<Throwable> stack = new Stack<>();
        if (th != null) {
            stack.push(th);
        }
        while (!stack.isEmpty()) {
            Throwable a = stack.pop();
            if (visited.add(a)) {
                if (th instanceof NutsExceptionBase) {
                    return ((NutsExceptionBase) th);
                }
                Throwable c = th.getCause();
                if (c != null) {
                    stack.add(c);
                }
            }
        }
        return null;
    }

    static NutsSession detectSession(Throwable th) {
        NutsExceptionBase e = detectExceptionBase(th);
        if (e != null) {
            return e.getSession();
        }
        return null;
    }

    NutsMessage getFormattedMessage();

    NutsString getFormattedString();

    String getMessage();

    /**
     * current workspace
     *
     * @return current workspace
     */
    NutsWorkspace getWorkspace();

    NutsSession getSession();
}
