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

import java.io.IOException;

/**
 * Base Nuts Exception. Parent of all Nuts defined Exceptions.
 *
 * @author thevpc
 * @category Exceptions
 * @since 0.5.4
 */
public class NutsException extends RuntimeException {

    private final NutsWorkspace workspace;
    private final NutsString formattedMessage;

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     *
     * @param workspace the workspace of this Nuts Exception
     */
    public NutsException(NutsWorkspace workspace) {
        this.workspace = workspace;
        this.formattedMessage = null;
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param workspace the workspace of this Nuts Exception
     * @param message   the detail message. The detail message is saved for
     *                  later retrieval by the {@link #getMessage()} method.
     */
    public NutsException(NutsWorkspace workspace, NutsString message) {
        super(message == null ? null : workspace.formats().text().nodeFor(message).filteredText());
        this.workspace = workspace;
        this.formattedMessage = message==null?workspace.formats().text().blank():message;
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param workspace the workspace of this Nuts Exception
     * @param message   the detail message. The detail message is saved for
     *                  later retrieval by the {@link #getMessage()} method.
     */
    public NutsException(NutsWorkspace workspace, NutsMessage message) {
        super(message == null ? null : workspace.formats().text().nodeFor(message).filteredText());
        this.workspace = workspace;
        this.formattedMessage = workspace.formats().text().nodeFor(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param workspace the workspace of this Nuts Exception
     * @param message   the detail message. The detail message is saved for
     *                  later retrieval by the {@link #getMessage()} method.
     */
    public NutsException(NutsWorkspace workspace, String message) {
        super(message);
        this.workspace = workspace;
        this.formattedMessage = workspace.formats().text().nodeFor(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.
     * <br>
     * Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param message   the detail message (which is saved for later retrieval
     *                  by the {@link #getMessage()} method).
     * @param cause     the cause (which is saved for later retrieval by the
     *                  {@link #getCause()} method).  (A <tt>null</tt> value is
     *                  permitted, and indicates that the cause is nonexistent or
     *                  unknown.)
     * @param workspace the workspace of this Nuts Exception
     */
    public NutsException(NutsWorkspace workspace, NutsString message, Throwable cause) {
        super(message == null ? null : message.filteredText(), cause);
        this.workspace = workspace;
        this.formattedMessage = message==null?workspace.formats().text().blank():message;
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.
     * <br>
     * Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param message   the detail message (which is saved for later retrieval
     *                  by the {@link #getMessage()} method).
     * @param cause     the cause (which is saved for later retrieval by the
     *                  {@link #getCause()} method).  (A <tt>null</tt> value is
     *                  permitted, and indicates that the cause is nonexistent or
     *                  unknown.)
     * @param workspace the workspace of this Nuts Exception
     */
    public NutsException(NutsWorkspace workspace, NutsMessage message, Throwable cause) {
        super(message == null ? null : workspace.formats().text().nodeFor(message).filteredText(), cause);
        this.workspace = workspace;
        this.formattedMessage = workspace.formats().text().nodeFor(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.
     * <br>
     * Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param message   the detail message (which is saved for later retrieval
     *                  by the {@link #getMessage()} method).
     * @param cause     the cause (which is saved for later retrieval by the
     *                  {@link #getCause()} method).  (A <tt>null</tt> value is
     *                  permitted, and indicates that the cause is nonexistent or
     *                  unknown.)
     * @param workspace the workspace of this Nuts Exception
     */
    public NutsException(NutsWorkspace workspace, String message, Throwable cause) {
        super(message, cause);
        this.workspace = workspace;
        this.formattedMessage = workspace.formats().text().nodeFor(message);
    }

    /**
     * Constructs a new runtime exception with the specified cause and a
     * detail message of <tt>(cause==null ? null : cause.toString())</tt>
     * (which typically contains the class and detail message of
     * <tt>cause</tt>).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause     the cause (which is saved for later retrieval by the
     *                  {@link #getCause()} method).  (A <tt>null</tt> value is
     *                  permitted, and indicates that the cause is nonexistent or
     *                  unknown.)
     * @param workspace the workspace of this Nuts Exception
     */
    public NutsException(NutsWorkspace workspace, Throwable cause) {
        super(cause);
        this.workspace = workspace;
        this.formattedMessage = workspace.formats().text().blank();
    }

    /**
     * Constructs a new runtime exception with the specified cause and a
     * detail message of <tt>(cause==null ? null : cause.toString())</tt>
     * (which typically contains the class and detail message of
     * <tt>cause</tt>).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause     the cause (which is saved for later retrieval by the
     *                  {@link #getCause()} method).  (A <tt>null</tt> value is
     *                  permitted, and indicates that the cause is nonexistent or
     *                  unknown.)
     * @param workspace the workspace of this Nuts Exception
     */
    public NutsException(NutsWorkspace workspace, IOException cause) {
        super(cause);
        this.workspace = workspace;
        this.formattedMessage = workspace.formats().text().blank();
    }

    /**
     * Constructs a new runtime exception with the specified detail
     * message, cause, suppression enabled or disabled, and writable
     * stack trace enabled or disabled.
     *
     * @param message            the detail message.
     * @param cause              the cause.  (A {@code null} value is permitted,
     *                           and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression  whether or not suppression is enabled
     *                           or disabled
     * @param writableStackTrace whether or not the stack trace should
     *                           be writable
     * @param workspace          the workspace of this Nuts Exception
     */
    public NutsException(NutsWorkspace workspace, NutsString message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message == null ? null : message.filteredText()
                , cause, enableSuppression, writableStackTrace);
        this.workspace = workspace;
        this.formattedMessage = message==null?workspace.formats().text().blank():message;
    }

    /**
     * Constructs a new runtime exception with the specified detail
     * message, cause, suppression enabled or disabled, and writable
     * stack trace enabled or disabled.
     *
     * @param message            the detail message.
     * @param cause              the cause.  (A {@code null} value is permitted,
     *                           and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression  whether or not suppression is enabled
     *                           or disabled
     * @param writableStackTrace whether or not the stack trace should
     *                           be writable
     * @param workspace          the workspace of this Nuts Exception
     */
    public NutsException(NutsWorkspace workspace, NutsMessage message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message == null ? null : workspace.formats().text().nodeFor(message).filteredText()
                , cause, enableSuppression, writableStackTrace);
        this.workspace = workspace;
        this.formattedMessage = workspace.formats().text().nodeFor(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail
     * message, cause, suppression enabled or disabled, and writable
     * stack trace enabled or disabled.
     *
     * @param message            the detail message.
     * @param cause              the cause.  (A {@code null} value is permitted,
     *                           and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression  whether or not suppression is enabled
     *                           or disabled
     * @param writableStackTrace whether or not the stack trace should
     *                           be writable
     * @param workspace          the workspace of this Nuts Exception
     */
    public NutsException(NutsWorkspace workspace, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.workspace = workspace;
        this.formattedMessage = workspace.formats().text().nodeFor(message);
    }

    /**
     * Returns the workspace of this Nuts Exception.
     *
     * @return the workspace of this {@code NutsException} instance
     * (which may be {@code null}).
     */
    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    /**
     * @return formatted message
     */
    public NutsString getFormattedMessage() {
        return formattedMessage;
    }
}
