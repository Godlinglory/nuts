/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.terminals;

import net.vpc.app.nuts.NutsFormattedPrintStream;
import net.vpc.app.nuts.NutsOutputStreamTransparentAdapter;
import net.vpc.app.nuts.core.util.bundledlibs.fprint.*;

import java.io.*;
import net.vpc.app.nuts.core.util.NullOutputStream;

/**
 * Created by vpc on 2/20/17.
 */
public class NutsDefaultFormattedPrintStream extends FormattedPrintStream implements NutsFormattedPrintStream, NutsOutputStreamTransparentAdapter {

    private OutputStream base;

    public NutsDefaultFormattedPrintStream() {
        super(NullOutputStream.INSTANCE);
        base = NullOutputStream.INSTANCE;
    }

    public NutsDefaultFormattedPrintStream(OutputStream out) {
        super(out);
        base = out;
    }

    public NutsDefaultFormattedPrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
        base = out;
    }

    public NutsDefaultFormattedPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
        base = out;
    }

    @Override
    public OutputStream baseOutputStream() {
        return base;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT + 1;
    }

}
