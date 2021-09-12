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
package net.thevpc.nuts;

import net.thevpc.nuts.spi.NutsComponent;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Path;

/**
 * I/O Manager supports a set of operations to manipulate terminals and files in
 * a handy manner that is monitorable and Workspace aware.
 *
 * @author thevpc
 * @app.category Input Output
 * @since 0.5.4
 */
public interface NutsIOManager extends NutsComponent<Object/* any object or null */> {

    /**
     * expand path to Workspace Location
     *
     * @param path path to expand
     * @return expanded path
     */
    NutsPath path(String path);

    NutsPath path(File path);

    NutsPath path(Path path);

    NutsPath path(URL path);

    NutsPath path(String path, ClassLoader classLoader);

    /**
     * create a null input stream instance
     *
     * @return null input stream instance
     */
    InputStream nullInputStream();

    /**
     * create a null print stream instance
     *
     * @return null print stream instance
     */
    NutsPrintStream nullPrintStream();

    /**
     * create print stream that supports the given {@code mode}. If the given
     * {@code out} is a PrintStream that supports {@code mode}, it should be
     * returned without modification.
     *
     * @param out stream to wrap
     * @param mode mode to support
     * @return {@code mode} supporting PrintStream
     */
    NutsPrintStream createPrintStream(OutputStream out, NutsTerminalMode mode);

    NutsPrintStream createPrintStream(OutputStream out);

    NutsPrintStream createPrintStream(Writer out);

    NutsMemoryPrintStream createMemoryPrintStream();

    NutsTempAction tmp();

    /**
     * create new {@link NutsIOCopyAction} instance
     *
     * @return create new {@link NutsIOCopyAction} instance
     */
    NutsIOCopyAction copy();

    /**
     * create new {@link NutsIOProcessAction} instance
     *
     * @return create new {@link NutsIOProcessAction} instance
     */
    NutsIOProcessAction ps();

    /**
     * create new {@link NutsIOCompressAction} instance
     *
     * @return create new {@link NutsIOCompressAction} instance
     */
    NutsIOCompressAction compress();

    /**
     * create new {@link NutsIOUncompressAction} instance
     *
     * @return create new {@link NutsIOUncompressAction} instance
     */
    NutsIOUncompressAction uncompress();

    /**
     * create new {@link NutsIODeleteAction} instance
     *
     * @return create new {@link NutsIODeleteAction} instance
     */
    NutsIODeleteAction delete();

    /**
     * create new {@link NutsMonitorAction} instance that helps monitoring
     * streams.
     *
     * @return create new {@link NutsIOLockAction} instance
     */
    NutsMonitorAction monitor();

    /**
     * create new {@link NutsIOHashAction} instance that helps hashing streams
     * and files.
     *
     * @return create new {@link NutsIOHashAction} instance
     */
    NutsIOHashAction hash();

    NutsInputAction input();

    NutsOutputAction output();

    NutsSession getSession();

    NutsIOManager setSession(NutsSession session);

    NutsPrintStream stdout();

    NutsPrintStream stderr();

    InputStream stdin();

    boolean isStandardOutputStream(NutsPrintStream out);

    boolean isStandardErrorStream(NutsPrintStream out);

    boolean isStandardInputStream(InputStream in);

    NutsIOManager addPathFactory(NutsPathFactory pathFactory);

    NutsIOManager removePathFactory(NutsPathFactory pathFactory);
}
