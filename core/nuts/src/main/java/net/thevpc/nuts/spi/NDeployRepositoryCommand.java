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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NDescriptor;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.NSession;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * Repository Deploy command provided by Repository and used by Workspace.
 * This class is part of Nuts SPI and is not to be used by end users.
 *
 * @author thevpc
 * @app.category SPI Base
 * @since 0.5.4
 */
public interface NDeployRepositoryCommand extends NRepositoryCommand {

    /**
     * content to deploy
     *
     * @return content to deploy
     */
    NInputSource getContent();

    /**
     * set content to deploy
     *
     * @param content content to deploy
     * @return {@code this} instance
     */
    NDeployRepositoryCommand setContent(NInputSource content);

    /**
     * set content to deploy
     *
     * @param content content to deploy
     * @return {@code this} instance
     */
    NDeployRepositoryCommand setContent(NPath content);

    /**
     * set content to deploy
     *
     * @param content content to deploy
     * @return {@code this} instance
     */
    NDeployRepositoryCommand setContent(Path content);

    /**
     * set content to deploy
     *
     * @param content content to deploy
     * @return {@code this} instance
     */
    NDeployRepositoryCommand setContent(URL content);

    /**
     * set content to deploy
     *
     * @param content content to deploy
     * @return {@code this} instance
     */
    NDeployRepositoryCommand setContent(File content);

    /**
     * set content to deploy
     *
     * @param content content to deploy
     * @return {@code this} instance
     */
    NDeployRepositoryCommand setContent(InputStream content);

    /**
     * descriptor to deploy
     *
     * @return descriptor to deploy
     */
    NDescriptor getDescriptor();

    /**
     * set descriptor to deploy
     *
     * @param descriptor descriptor to deploy
     * @return {@code this} instance
     */
    NDeployRepositoryCommand setDescriptor(NDescriptor descriptor);

    /**
     * id to deploy
     *
     * @return id to deploy
     */
    NId getId();

    /**
     * set id to deploy
     *
     * @param id id to deploy
     * @return {@code this} instance
     */
    NDeployRepositoryCommand setId(NId id);

    /**
     * session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NDeployRepositoryCommand setSession(NSession session);

    /**
     * run deploy command
     *
     * @return {@code this} instance
     */
    @Override
    NDeployRepositoryCommand run();
}
