/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.InputStream;

/**
 * context holding useful information for {@link NutsDescriptorContentParserComponent#parse(NutsDescriptorContentParserContext)}
 *
 * @since 0.5.4
 * @category SPI Base
 */
public interface NutsDescriptorContentParserContext {

    /**
     * command line options that can be parsed to
     * configure parsing options.
     * A good example of it is the --all-mains option that can be passed
     * as executor option which will be catched by parser to force resolution
     * of all main classes even though a Main-Class attribute is visited in the MANIFEST.MF
     * file.
     * This array may continue any non supported options. They should be discarded by the parser.
     * @return parser options.
     * @since 0.5.8
     */
    String[] getParseOptions();

    /**
     * return content header stream.
     * if the content size is less than 1Mb, then all the content is returned.
     * If not, at least 1Mb is returned.
     * @return content header stream
     */
    InputStream getHeadStream();

    /**
     * content stream
     * @return content stream
     */
    InputStream getFullStream();

    /**
     * content file extension or null. At least one of file extension or file mime-type is provided.
     * @return content file extension
     */
    String getFileExtension();

    /**
     * content mime-type or null. At least one of file extension or file mime-type is provided.
     * @return content file extension
     */
    String getMimeType();

    /**
     * content name (mostly content file name)
     * @return content name (mostly content file name)
     */
    String getName();

    /**
     * return workspace
     * @return  workspace
     */
    NutsWorkspace getWorkspace();

    /**
     * return session
     * @return session
     */
    NutsSession getSession();

}
