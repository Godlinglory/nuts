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
package net.thevpc.nuts.runtime.standalone.elem;

import java.time.Instant;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author thevpc
 */
public class DefaultNArrayElement extends AbstractNArrayElement {

    private final NElement[] values;

    public DefaultNArrayElement(Collection<NElement> values, NSession session) {
        super(session);
        this.values = values.toArray(new NElement[0]);
    }


    public DefaultNArrayElement(NElement[] values, NSession session) {
        super(session);
        this.values = Arrays.copyOf(values, values.length);
    }

    @Override
    public Collection<NElement> items() {
        return Arrays.asList(values);
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public Stream<NElement> stream() {
        return Arrays.asList(values).stream();
    }

    @Override
    public NOptional<NElement> get(int index) {
        if(index>=0 && index<values.length){
            return NOptional.of(values[index]);
        }
        return NOptional.ofError(s-> NMsg.ofCstyle("invalid array index %s not in [%s,%s[",index,0,values.length));
    }

    @Override
    public NOptional<String> getString(int index) {
        return get(index).flatMap(NElement::asString);
    }

    @Override
    public NOptional<Boolean> getBoolean(int index) {
        return get(index).flatMap(NElement::asBoolean);
    }

    @Override
    public NOptional<Byte> getByte(int index) {
        return get(index).flatMap(NElement::asByte);
    }

    @Override
    public NOptional<Short> getShort(int index) {
        return get(index).flatMap(NElement::asShort);
    }

    @Override
    public NOptional<Integer> getInt(int index) {
        return get(index).flatMap(NElement::asInt);
    }

    @Override
    public NOptional<Long> getLong(int index) {
        return get(index).flatMap(NElement::asLong);
    }

    @Override
    public NOptional<Float> getFloat(int index) {
        return get(index).flatMap(NElement::asFloat);
    }

    @Override
    public NOptional<Double> getDouble(int index) {
        return get(index).flatMap(NElement::asDouble);
    }

    @Override
    public NOptional<Instant> getInstant(int index) {
        return get(index).flatMap(NElement::asInstant);
    }

    @Override
    public NOptional<NArrayElement> getArray(int index) {
        return get(index).flatMap(NElement::asArray);
    }

    @Override
    public NOptional<NObjectElement> getObject(int index) {
        return get(index).flatMap(NElement::asObject);
    }



    @Override
    public NArrayElementBuilder builder() {
        return NElements.of(session)
                .ofArray()
                .set(this);
    }

    @Override
    public Iterator<NElement> iterator() {
        return Arrays.asList(values).iterator();
    }

    @Override
    public String toString() {
        return "[" + items().stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Arrays.deepHashCode(this.values);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultNArrayElement other = (DefaultNArrayElement) obj;
        if (!Arrays.deepEquals(this.values, other.values)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return values.length == 0;
    }

    @Override
    public boolean isBlank() {
        return values.length == 0;
    }

    @Override
    public NOptional<NElement> get(String key) {
        return NValue.of(key).asInt().flatMap(this::get);
    }

    @Override
    public NOptional<NElement> get(NElement key) {
        return key.isString()?key.asString().flatMap(this::get):key.asInt().flatMap(this::get);
    }

    @Override
    public Collection<NElementEntry> entries() {
        return IntStream.range(0, size())
                .boxed()
                .map(x->new DefaultNElementEntry(
                        NElements.of(session).ofString(String.valueOf(x)),
                        get(x).orNull()
                )).collect(Collectors.toList());
    }
}
