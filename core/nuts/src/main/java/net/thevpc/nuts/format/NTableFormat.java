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
package net.thevpc.nuts.format;

import net.thevpc.nuts.NContentTypeFormat;
import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;

/**
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
public interface NTableFormat extends NContentTypeFormat {
    static NTableFormat of(NSession session) {
       return NExtensions.of(session).createComponent(NTableFormat.class).get();
    }

    boolean isVisibleHeader();

    NTableFormat setVisibleHeader(boolean visibleHeader);

    NTableBordersFormat getBorder();

    NTableFormat setBorder(NTableBordersFormat border);

    NTableFormat setBorder(String borderName);

    NTableFormat setVisibleColumn(int col, Boolean visible);

    Boolean getVisibleColumn(int col);

    NTableFormat setCellFormat(NTableCellFormat formatter);

    NTableModel getModel();

    @Override
    NTableFormat setValue(Object value);

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NTableFormat setSession(NSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NTableFormat configure(boolean skipUnsupported, String... args);

    @Override
    NTableFormat setNtf(boolean ntf);

}
