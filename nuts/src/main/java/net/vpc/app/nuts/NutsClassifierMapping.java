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
package net.vpc.app.nuts;

import java.io.Serializable;

/**
 * classifier selector immutable class.
 * Nuts can select artifact classifier according to filters based on arch, os, os dist and platform.
 * This class defines the mapping to classifier to consider if all the filters.
 * When multiple selectors match, the first on prevails.
 * @since 0.5.7
 * @category Descriptor
 */
public interface NutsClassifierMapping extends Serializable {
    /**
     * classifier to select
     * @return classifier to select
     */
    String getClassifier();

    /**
     * packaging to select
     * @return classifier to select
     */
    String getPackaging();

    /**
     * arch list filter.
     * al least one of the list must match.
     * @return arch list filter
     */
    String[] getArch();

    /**
     * os list filter.
     * al least one of the list must match.
     * @return os list filter
     */
    String[] getOs();

    /**
     * os distribution list filter.
     * al least one of the list must match.
     * @return os distribution list filter
     */
    String[] getOsdist();

    /**
     * platform list filter.
     * al least one of the list must match.
     * @return platform list filter.
     */
    String[] getPlatform();
}
