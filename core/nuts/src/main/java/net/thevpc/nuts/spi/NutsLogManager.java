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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsLogger;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.boot.NutsApiUtils;

import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * Nuts Log Manager
 * @app.category Logging
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public interface NutsLogManager extends NutsComponent {
    static NutsLogManager of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsLogManager.class,true,null);
    }

    /**
     * Log handler
     * @return Log handler
     */
    Handler[] getHandlers(NutsSession session);

    /**
     * remove the given handler
     * @param handler handler to remove
     * @return this
     */
    NutsLogManager removeHandler(Handler handler,NutsSession session);

    /**
     * add the given handler
     * @param handler handler to add
     * @return this
     */
    NutsLogManager addHandler(Handler handler,NutsSession session);

    /**
     * terminal handler
     * @return terminal handler
     */
    Handler getTermHandler(NutsSession session);

    /**
     * file handler
     * @return file handler
     */
    Handler getFileHandler(NutsSession session);

    /**
     * create an instance of {@link NutsLogger}
     * @param name logger name
     * @return new instance of {@link NutsLogger}
     */
    NutsLogger createLogger(String name, NutsSession session);

    /**
     * create an instance of {@link NutsLogger}
     * @param clazz logger clazz
     * @return new instance of {@link NutsLogger}
     */
    NutsLogger createLogger(Class clazz, NutsSession session);

    /**
     * return terminal logger level
     * @return terminal logger level
     */
    Level getTermLevel(NutsSession session);

    /**
     * set terminal logger level
     * @param level new level
     * @return this
     */
    NutsLogManager setTermLevel(Level level,NutsSession session);

    /**
     * return file logger level
     * @return file logger level
     */
    Level getFileLevel(NutsSession session);

    /**
     * set file logger level
     * @param level new level
     * @return this
     */
    NutsLogManager setFileLevel(Level level,NutsSession session);

}