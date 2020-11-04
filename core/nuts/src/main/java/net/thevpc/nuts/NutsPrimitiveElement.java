/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
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
package net.thevpc.nuts;

import java.time.Instant;

/**
 * primitive values implementation of Nuts Element type. Nuts Element types are
 * generic JSON like parsable objects.
 *
 * @author vpc
 * @since 0.5.6
 * @category Elements
 */
public interface NutsPrimitiveElement extends NutsElement {

    /**
     * value as any java Object
     *
     * @return value as any java Object
     */
    Object getValue();

    /**
     * value as any java date. Best effort is applied to convert to this type.
     *
     * @return value as java Date
     */
    Instant getDate();

    /**
     * value as any java Number. Best effort is applied to convert to this type.
     *
     * @return value as java Number
     */
    Number getNumber();

    /**
     * value as any java Boolean. Best effort is applied to convert to this
     * type.
     *
     * @return value as java Boolean
     */
    boolean getBoolean();

    /**
     * value as any java Double. Best effort is applied to convert to this type.
     *
     * @return value as java double
     */
    double getDouble();

    /**
     * value as any java Float. Best effort is applied to convert to this type.
     *
     * @return value as java float
     */
    float getFloat();

    /**
     * value as any java Integer. Best effort is applied to convert to this
     * type.
     *
     * @return value as java integer
     */
    int getInt();

    /**
     * value as any java Long. Best effort is applied to convert to this type.
     *
     * @return value as java long
     */
    long getLong();

    /**
     * value as any java string. Best effort is applied to convert to this type.
     *
     * @return value as java string
     */
    String getString();

    /**
     * true if the value is or can be converted to double
     *
     * @return true if the value is or can be converted to double
     */
    boolean isDouble();

    /**
     * true if the value is or can be converted to float
     *
     * @return true if the value is or can be converted to double
     */
    boolean isFloat();

    /**
     * true if the value is or can be converted to int.
     *
     * @return true if the value is or can be converted to int
     */
    boolean isInt();

    /**
     * true if the value is or can be converted to long.
     *
     * @return true if the value is or can be converted to long
     */
    boolean isLong();

    /**
     * true if the value is null (in which case, the type should be NULL)
     *
     * @return true if the value is null (in which case, the type is NULL)
     */
    boolean isNull();

}
