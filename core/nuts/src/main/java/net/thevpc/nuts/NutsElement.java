/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import java.time.Instant;

/**
 * Nuts Element types are generic JSON like parsable objects.
 *
 * @author thevpc
 * @since 0.5.6
 * @category Elements
 */
public interface NutsElement {

    /**
     * element type
     *
     * @return element type
     */
    NutsElementType type();

    /**
     * convert this element to {@link NutsPrimitiveElement} or throw
     * ClassCastException
     *
     * @return {@link NutsPrimitiveElement}
     */
    NutsPrimitiveElement asPrimitive();

    /**
     * convert this element to {@link NutsObjectElement} or throw
     * ClassCastException
     *
     * @return {@link NutsObjectElement}
     */
    NutsObjectElement asObject();

    /**
     * convert this element to {@link NutsArrayElement} or throw
     * ClassCastException
     *
     * @return {@link NutsArrayElement}
     */
    NutsArrayElement asArray();

    public boolean isPrimitive();

    public boolean isNumber();

    public boolean isNull();

    public boolean isString();

//    public boolean isNutsString();

    public boolean isByte();

    public boolean isInt();

    public boolean isLong();

    public boolean isShort();

    public boolean isFloat();

    public boolean isDouble();

    public boolean isObject();

    public boolean isArray();

    public boolean isInstant();

    String asString();

    public boolean isEmpty();

//    NutsString asNutsString();

    boolean asBoolean();

    byte asByte();

    double asDouble();

    float asFloat();

    Instant asInstant();

    int asInt();

    long asLong();

    short asShort();

    
}
