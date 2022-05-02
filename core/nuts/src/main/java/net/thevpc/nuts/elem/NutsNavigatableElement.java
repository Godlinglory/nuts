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
package net.thevpc.nuts.elem;

import net.thevpc.nuts.NutsOptional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;

/**
 * Object implementation of Nuts Element type. Nuts Element types are generic
 * JSON like parsable objects.
 *
 * @author thevpc
 * @app.category Elements
 * @since 0.5.6
 */
public interface NutsNavigatableElement extends NutsElement {

    /**
     * return value for name or null. If multiple values are available return
     * any of them.
     *
     * @param key key name
     * @return value for name or null
     */
    NutsOptional<NutsElement> get(String key);

    NutsOptional<String> getStringByPath(String... keys);

    NutsOptional<Integer> getIntByPath(String... keys);

    NutsOptional<Long> getLongByPath(String... keys);

    NutsOptional<Float> getFloatByPath(String... keys);

    NutsOptional<Double> getDoubleByPath(String... keys);

    NutsOptional<Boolean> getBooleanByPath(String... keys);

    NutsOptional<Byte> getByteByPath(String... keys);

    NutsOptional<Short> getShortByPath(String... keys);

    NutsOptional<Instant> getInstantByPath(String... keys);

    NutsOptional<BigInteger> getBigIntByPath(String... keys);

    NutsOptional<BigDecimal> getBigDecimalByPath(String... keys);

    NutsOptional<Number> getNumberByPath(String... keys);

    NutsOptional<NutsElement> getByPath(String... keys);
    NutsOptional<NutsArrayElement> getArrayByPath(String... keys);
    NutsOptional<NutsObjectElement> getObjectByPath(String... keys);
    NutsOptional<NutsNavigatableElement> getNavigatableByPath(String... keys);

    NutsOptional<NutsElement> get(NutsElement key);

    NutsOptional<NutsArrayElement> getArray(String key);

    NutsOptional<NutsArrayElement> getArray(NutsElement key);

    NutsOptional<NutsObjectElement> getObject(String key);

    NutsOptional<NutsObjectElement> getObject(NutsElement key);

    NutsOptional<NutsNavigatableElement> getNavigatable(String key);

    NutsOptional<NutsNavigatableElement> getNavigatable(NutsElement key);

    NutsOptional<String> getString(String key);

    NutsOptional<String> getString(NutsElement key);

    NutsOptional<Boolean> getBoolean(String key);

    NutsOptional<Boolean> getBoolean(NutsElement key);

    NutsOptional<Number> getNumber(String key);

    NutsOptional<Number> getNumber(NutsElement key);

    NutsOptional<Byte> getByte(String key);

    NutsOptional<Byte> getByte(NutsElement key);

    NutsOptional<Integer> getInt(String key);

    NutsOptional<Integer> getInt(NutsElement key);

    NutsOptional<Long> getLong(String key);

    NutsOptional<Long> getLong(NutsElement key);

    NutsOptional<Short> getShort(String key);

    NutsOptional<Short> getShort(NutsElement key);

    NutsOptional<Instant> getInstant(String key);

    NutsOptional<Instant> getInstant(NutsElement key);

    NutsOptional<Float> getFloat(String key);

    NutsOptional<Float> getFloat(NutsElement key);

    NutsOptional<Double> getDouble(String key);

    NutsOptional<Double> getDouble(NutsElement key);

    NutsOptional<BigInteger> getBigInt(NutsElement key);

    NutsOptional<BigDecimal> getBigDecimal(NutsElement key);

    /**
     * object (key,value) attributes
     *
     * @return object attributes
     */
    Collection<NutsElementEntry> entries();

    /**
     * element count
     *
     * @return element count
     */
    int size();
}
