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
package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNObjectElementBuilder implements NObjectElementBuilder {

    private final Map<NElement, NElement> values = new LinkedHashMap<NElement, NElement>();

    private transient NSession session;

    public DefaultNObjectElementBuilder(NSession session) {
        if(session==null){
            throw new NullPointerException();
        }
        this.session = session;
    }

    @Override
    public Collection<NElementEntry> children() {
        return values.entrySet().stream().map(x -> new DefaultNElementEntry(x.getKey(), x.getValue())).collect(Collectors.toList());
    }

    @Override
    public NElement get(NElement s) {
        return values.get(s);
    }

    @Override
    public NElement get(String s) {
        return values.get(_elements().ofBoolean(s));
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NObjectElementBuilder set(String name, NElement value) {
        values.put(_elements().ofString(name), denull(value));
        return this;
    }

    @Override
    public NObjectElementBuilder set(String name, boolean value) {
        return set(_elements().ofString(name), _elements().ofBoolean(value));
    }

    @Override
    public NObjectElementBuilder set(String name, int value) {
        return set(_elements().ofString(name), _elements().ofInt(value));
    }

    @Override
    public NObjectElementBuilder set(String name, double value) {
        return set(_elements().ofString(name), _elements().ofDouble(value));
    }

    @Override
    public NObjectElementBuilder set(String name, String value) {
        return set(_elements().ofString(name), _elements().ofString(value));
    }

    @Override
    public NObjectElementBuilder remove(String name) {
        NElement v = name==null?_elements().ofNull():_elements().ofString(name);
        values.remove(v);
        return this;
    }

//    @Override
    public NObjectElementBuilder add(NElement name, NElement value) {
        values.put(name, denull(value));
        return this;
    }

    @Override
    public NObjectElementBuilder set(NElement name, NElement value) {
        values.put(name, denull(value));
        return this;
    }

    @Override
    public NObjectElementBuilder addAll(Map<NElement, NElement> other) {
        if(other!=null){
            for (Map.Entry<NElement, NElement> e : other.entrySet()) {
                add(e.getKey(),e.getValue());
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder set(NElement name, boolean value) {
        return set(name, _elements().ofBoolean(value));
    }

    @Override
    public NObjectElementBuilder set(NElement name, int value) {
        return set(name, _elements().ofInt(value));
    }

    @Override
    public NObjectElementBuilder set(NElement name, double value) {
        return set(name, _elements().ofDouble(value));
    }

    @Override
    public NObjectElementBuilder set(NElement name, String value) {
        return set(name, _elements().ofString(value));
    }

    @Override
    public NObjectElementBuilder remove(NElement name) {
        values.remove(name);
        return this;
    }

    @Override
    public NObjectElementBuilder clear() {
        values.clear();
        return this;
    }

    @Override
    public NObjectElementBuilder set(NObjectElement other) {
        clear();
        add(other);
        return this;
    }

    @Override
    public NObjectElementBuilder set(NObjectElementBuilder other) {
        clear();
        add(other);
        return this;
    }

    @Override
    public NObjectElementBuilder add(NObjectElement other) {
        if (other != null) {
            for (NElementEntry child : other.entries()) {
                add(child.getKey(), child.getValue());
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder add(NObjectElementBuilder other) {
        if (other != null) {
            for (NElementEntry child : other.children()) {
                add(child.getKey(), child.getValue());
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder add(NElementEntry entry) {
        if (entry != null) {
            add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addAll(NElementEntry... entries) {
        if(entries!=null){
            for (NElementEntry entry : entries) {
                add(entry);
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addAll(NObjectElement other) {
        if(other!=null){
            for (NElementEntry entry : other) {
                add(entry);
            }
        }
        return this;
    }

    @Override
    public NObjectElementBuilder addAll(NObjectElementBuilder other) {
        if(other!=null){
            for (NElementEntry entry : other.build()) {
                add(entry);
            }
        }
        return this;
    }

    @Override
    public NObjectElement build() {
        return new DefaultNObjectElement(values, session);
    }

    @Override
    public String toString() {
        return "{" + children().stream().map(x
                -> x.getKey()
                + ":"
                + x.getValue().toString()
        ).collect(Collectors.joining(", ")) + "}";
    }

    private NElement denull(NElement e) {
        if (e == null) {
            return _elements().ofNull();
        }
        return e;
    }

    private NElements _elements() {
        return NElements.of(session);
    }

    @Override
    public NElementType type() {
        return NElementType.OBJECT;
    }
}
