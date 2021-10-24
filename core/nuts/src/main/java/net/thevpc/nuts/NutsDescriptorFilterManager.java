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
 * @app.category Base
 */
public interface NutsDescriptorFilterManager extends NutsTypedFilters<NutsDescriptorFilter> {
    static NutsDescriptorFilterManager of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.descriptor().filter();
    }

    NutsDescriptorFilterManager setSession(NutsSession session);

    NutsDescriptorFilter byExpression(String expression);

    NutsDescriptorFilter byPackaging(String... values);

    NutsDescriptorFilter byArch(String... values);

    NutsDescriptorFilter byOs(String... values);

    NutsDescriptorFilter byOsDist(String... values);

    NutsDescriptorFilter byPlatform(String... values);

    NutsDescriptorFilter byDesktopEnvironment(String... values);

    NutsDescriptorFilter byFlag(NutsDescriptorFlag... flags);

    NutsDescriptorFilter byExtension(NutsVersion apiVersion);

    NutsDescriptorFilter byRuntime(NutsVersion apiVersion);

    NutsDescriptorFilter byCompanion(NutsVersion apiVersion);

    NutsDescriptorFilter byApiVersion(NutsVersion apiVersion);

    NutsDescriptorFilter byLockedIds(String... ids);
}
