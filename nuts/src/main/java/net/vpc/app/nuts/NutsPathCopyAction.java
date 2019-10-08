/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsPathCopyAction {

    Object getSource();

    NutsPathCopyAction setSource(InputStream source);

    NutsPathCopyAction setSource(File source);

    NutsPathCopyAction setSource(Path source);

    NutsPathCopyAction setSource(URL source);

    NutsPathCopyAction from(Object source);

    NutsPathCopyAction from(String source);

    NutsPathCopyAction from(InputStream source);

    NutsPathCopyAction from(File source);

    NutsPathCopyAction from(Path source);

    NutsPathCopyAction from(URL source);

    Object getTarget();

    NutsPathCopyAction setTarget(OutputStream target);

    NutsPathCopyAction setTarget(Path target);

    NutsPathCopyAction setTarget(File target);

    NutsPathCopyAction to(OutputStream target);

    NutsPathCopyAction to(String target);

    NutsPathCopyAction to(Path target);

    NutsPathCopyAction to(File target);

    Validator getChecker();

    NutsPathCopyAction validator(Validator validator);

    NutsPathCopyAction setValidator(Validator validator);

    boolean isSafeCopy();

    NutsPathCopyAction safeCopy();

    NutsPathCopyAction safeCopy(boolean safeCopy);

    NutsPathCopyAction setSafeCopy(boolean safeCopy);

    NutsSession getSession();

    NutsPathCopyAction session(NutsSession session);

    NutsPathCopyAction setSession(NutsSession session);

    byte[] getByteArrayResult();

    void run();

    NutsPathCopyAction monitorable(boolean monitorable);

    NutsPathCopyAction monitorable();

    boolean isMonitorable();

    NutsPathCopyAction setMonitorable(boolean monitorable);

    NutsPathCopyAction to(Object target);

    boolean isIncludeDefaultMonitorFactory();

    NutsPathCopyAction setIncludeDefaultMonitorFactory(boolean value);

    NutsPathCopyAction includeDefaultMonitorFactory(boolean value);

    NutsPathCopyAction includeDefaultMonitorFactory();

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    NutsInputStreamProgressFactory getProgressMonitorFactory();

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsPathCopyAction setProgressMonitorFactory(NutsInputStreamProgressFactory value);

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsPathCopyAction progressMonitorFactory(NutsInputStreamProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsPathCopyAction setProgressMonitor(NutsInputStreamProgressMonitor value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsPathCopyAction progressMonitor(NutsInputStreamProgressMonitor value);

    class ValidationException extends RuntimeException {

        public ValidationException() {
        }

        public ValidationException(String message) {
            super(message);
        }

        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }

        public ValidationException(Throwable cause) {
            super(cause);
        }

    }

    interface Validator {

        void validate(Path path) throws ValidationException;
    }

}
