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

import java.util.List;

/**
 * Auto Complete Helper class used to collect argument candidates
 * @author vpc
 * @since 0.5.5
 */
public interface NutsCommandAutoComplete {

    /**
     * command line string
     * @return line
     */
    String getLine();

    /**
     * command line arguments
     * @return words
     */
    List<String> getWords();

    /**
     * current candidates
     * @return current candidates
     */
    List<NutsArgumentCandidate> getCandidates();

    /**
     * candidates index
     * @return candidates index
     */
    int getCurrentWordIndex();

    /**
     * add candidate
     * @param value candidate value
     * @param display candidate user display label
     */
    void addCandidate(String value, String display);

    /**
     * add candidate
     * @param type candidate type
     * @param name candidate name
     */
    void addExpectedTypedValue(String type, String name);

    /**
     * add candidate
     * @param value candidate
     */
    void addCandidate(NutsArgumentCandidate value);

}
