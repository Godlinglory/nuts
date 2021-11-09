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
package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * I/O Action that help monitored uncompress of one or multiple resource types.
 *
 * @author thevpc
 * @app.category Input Output
 * @since 0.5.8
 */
public interface NutsUncompress extends NutsComponent {
    static NutsUncompress of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsUncompress.class, true, null);
    }

    /**
     * format
     *
     * @return format
     */
    String getFormat();

    /**
     * update format
     *
     * @param format format
     * @return {@code this} instance
     */
    NutsUncompress setFormat(String format);

    /**
     * update format option
     *
     * @param option option name
     * @param value  value
     * @return {@code this} instance
     */
    NutsUncompress setFormatOption(String option, Object value);

    /**
     * return format option
     *
     * @param option option name
     * @return option value
     */
    Object getFormatOption(String option);

    /**
     * source to uncompress
     *
     * @return source to uncompress
     */
    Object getSource();

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsUncompress setSource(InputStream source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsUncompress setSource(NutsPath source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsUncompress setSource(File source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsUncompress setSource(Path source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsUncompress setSource(URL source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsUncompress from(InputStream source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsUncompress from(File source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsUncompress from(Path source);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsUncompress from(URL source);

    NutsUncompress to(NutsPath target);

    /**
     * target to uncompress to
     *
     * @return target to uncompress to
     */
    Object getTarget();

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsUncompress setTarget(Path target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsUncompress setTarget(File target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsUncompress setTarget(String target);

    NutsUncompress setTarget(NutsPath target);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsUncompress from(String source);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsUncompress to(String target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsUncompress to(Path target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsUncompress to(File target);

    /**
     * update source to uncompress from
     *
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsUncompress from(NutsPath source);

    /**
     * return current session
     *
     * @return current session
     */
    NutsSession getSession();

    /**
     * update current session
     *
     * @param session current session
     * @return {@code this} instance
     */
    NutsUncompress setSession(NutsSession session);

    /**
     * run this uncompress action
     *
     * @return {@code this} instance
     */
    NutsUncompress run();

    NutsUncompress visit(NutsIOUncompressVisitor visitor);

    /**
     * true if log progress flag is armed
     *
     * @return true if log progress flag is armed
     */
    boolean isLogProgress();

    /**
     * switch log progress flag to {@code value}.
     *
     * @param value value
     * @return {@code this} instance
     */
    NutsUncompress setLogProgress(boolean value);

    /**
     * return true if skip root flag is armed.
     *
     * @return true if skip root flag is armed
     * @since 0.5.8
     */
    boolean isSkipRoot();

    /**
     * set skip root flag to {@code value}
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsUncompress setSkipRoot(boolean value);

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    NutsProgressFactory getProgressMonitorFactory();

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsUncompress setProgressMonitorFactory(NutsProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsUncompress setProgressMonitor(NutsProgressMonitor value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsUncompress progressMonitor(NutsProgressMonitor value);

    /**
     * return true if safe flag is armed
     *
     * @return true if safe flag is armed
     */
    boolean isSafe();

    /**
     * switch safe flag to {@code value}
     *
     * @param value value
     * @return {@code this} instance
     */
    NutsUncompress setSafe(boolean value);
}
