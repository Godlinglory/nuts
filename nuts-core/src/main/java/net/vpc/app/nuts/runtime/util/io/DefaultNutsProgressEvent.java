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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.util.io;

import net.vpc.app.nuts.NutsProgressEvent;
import net.vpc.app.nuts.NutsSession;

/**
 * @author vpc
 */
public class DefaultNutsProgressEvent implements NutsProgressEvent {

    private final Object source;
    private final String message;
    private final long globalCount;
    private final long globalMillis;
    private final long partialCount;
    private final long partialMillis;
    private final long length;
    private final Throwable exception;
    private final NutsSession session;
    private final float percent;
    private final boolean indeterminate;

    public DefaultNutsProgressEvent(Object source, String message, long globalCount, long globalMillis, long partialCount, long partialMillis, long length, Throwable exception, NutsSession session, boolean indeterminate) {
        this.source = source;
        this.length = length;
        this.message = message;
        this.globalCount = globalCount;
        this.globalMillis = globalMillis;
        this.partialCount = partialCount;
        this.partialMillis = partialMillis;
        this.exception = exception;
        this.session = session;
        this.indeterminate = indeterminate;
        if (length > 0) {
            percent = (float) (globalCount * 100.0 / length);
        } else {
            percent = 100;
        }
    }

    public NutsSession getSession() {
        return session;
    }

    public Throwable getError() {
        return exception;
    }

    public long getMaxValue() {
        return length;
    }

    public Object getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }

    public long getCurrentValue() {
        return globalCount;
    }

    public long getTimeMillis() {
        return globalMillis;
    }

    public long getPartialValue() {
        return partialCount;
    }

    public long getPartialMillis() {
        return partialMillis;
    }


    @Override
    public float getPercent() {
        return percent;
    }

    @Override
    public boolean isIndeterminate() {
        return indeterminate;
    }
}
