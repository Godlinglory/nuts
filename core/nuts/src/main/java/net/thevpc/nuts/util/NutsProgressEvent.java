/**
 * ====================================================================
 * vpc-common-io : common reusable library for
 * input/output
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
package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

/**
 * Progress event
 *
 * @author thevpc
 * @app.category Toolkit
 * @since 0.5.8
 */
public interface NutsProgressEvent {

    static NutsProgressEvent ofStart(Object source, NutsMessage message, long length, NutsSession session) {
        return new DefaultNutsProgressEvent(source, message, 0, 0, null, 0, 0,
                length, null, session, null, NutsProgressEventType.START);
    }

    static NutsProgressEvent ofComplete(Object source, NutsMessage message, long globalCount, long globalDurationNanos,
                                        Double percent,
                                        long partialCount, long partialDurationNanos, long length, Throwable exception, NutsSession session) {
        return new DefaultNutsProgressEvent(source, message, globalCount, globalDurationNanos, percent, partialCount, partialDurationNanos,
                length, exception, session, null, NutsProgressEventType.COMPLETE);
    }

    static NutsProgressEvent ofProgress(Object source, NutsMessage message,
                                        long globalCount, long globalDurationNanos,
                                        Double percent,
                                        long partialCount, long partialDurationNanos, long length, Throwable exception, NutsSession session) {
        return new DefaultNutsProgressEvent(source, message, globalCount, globalDurationNanos, percent, partialCount, partialDurationNanos,
                length, exception, session, null, NutsProgressEventType.PROGRESS);
    }

    /**
     * Nuts Session
     *
     * @return Nuts Session
     */
    NutsSession getSession();

    /**
     * Nuts Workspace
     *
     * @return Nuts Workspace
     */
    NutsWorkspace getWorkspace();

    /**
     * error or null
     *
     * @return error or null
     */
    Throwable getError();

    /**
     * progress max value or -1 if intermediate
     *
     * @return progress max value
     */
    long getMaxValue();

    /**
     * progress current value
     *
     * @return progress current value
     */
    long getCurrentCount();

    /**
     * progress value from the last mark point.
     * Mark point occurs when {@link NutsProgressListener#onProgress(NutsProgressEvent)} return false.
     *
     * @return progress value from the last mark point.
     */
    long getPartialCount();

    /**
     * progress source object
     *
     * @return progress source object
     */
    Object getSource();

    /**
     * event message
     *
     * @return event message
     */
    NutsMessage getMessage();

    /**
     * progress percentage ([0..1])
     *
     * @return progress percentage ([0..1])
     */
    double getProgress();

    NutsDuration getDuration();

    NutsDuration getPartialDuration();

    /**
     * when true, max value is unknown, and the progress is indeterminate
     *
     * @return true when max value is unknown, and the progress is indeterminate
     */
    boolean isIndeterminate();

    NutsProgressEventType getState();

}
