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
 *
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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsUtilStrings;
import net.thevpc.nuts.NutsVal;

import java.util.Objects;

/**
 *
 * @author thevpc
 * @app.category Internal
 * @since 0.8.3
 */
public class NutsBootStrValImpl implements NutsVal {
    private final String value;

    public NutsBootStrValImpl(String value) {
        this.value = value;
    }

    @Override
    public boolean isBlank() {
        return NutsBlankable.isBlank(value);
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    public int getInt() {
        if (NutsBlankable.isBlank(value)) {
            throw new NumberFormatException("empty value");
        }
        return Integer.parseInt(value);
    }

    @Override
    public Integer getInt(Integer emptyOrErrorValue) {
        return getInt(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Integer getInt(Integer emptyValue, Integer errorValue) {
        return NutsApiUtils.parseInt(value, emptyValue, errorValue);
    }

    @Override
    public boolean isLong() {
        return !isBlank() && getLong(null, null) != null;
    }

    ////////////

    @Override
    public Long getLong(Long emptyOrErrorValue) {
        return getLong(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Long getLong(Long emptyValue, Long errorValue) {
        if (NutsBlankable.isBlank(value)) {
            return emptyValue;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return errorValue;
        }
    }

    @Override
    public long getLong() {
        if (NutsBlankable.isBlank(value)) {
            throw new NumberFormatException("empty value");
        }
        return Long.parseLong(value);
    }

    @Override
    public boolean isDouble() {
        return !isBlank() && getDouble(null, null) != null;
    }

    @Override
    public boolean isFloat() {
        return !isBlank() && getFloat(null, null) != null;
    }

    @Override
    public Double getDouble(Double emptyOrErrorValue) {
        return getDouble(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Double getDouble(Double emptyValue, Double errorValue) {
        if (NutsBlankable.isBlank(value)) {
            return emptyValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            return errorValue;
        }
    }

    @Override
    public double getDouble() {
        if (NutsBlankable.isBlank(value)) {
            throw new NumberFormatException("empty value");
        }
        return Double.parseDouble(value);
    }

    @Override
    public Float getFloat(Float emptyOrErrorValue) {
        return getFloat(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Float getFloat(Float emptyValue, Float errorValue) {
        if (NutsBlankable.isBlank(value)) {
            return emptyValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (Exception ex) {
            return errorValue;
        }
    }

    @Override
    public float getFloat() {
        if (NutsBlankable.isBlank(value)) {
            throw new NumberFormatException("empty value");
        }
        return Float.parseFloat(value);
    }

    @Override
    public boolean isBoolean() {
        return !isBlank() && getBoolean(null, null) != null;
    }

    @Override
    public boolean isInt() {
        return !isBlank() && getInt(null, null) != null;
    }

    @Override
    public boolean getBoolean() {
        return getBoolean(false);
    }

    @Override
    public Boolean getBoolean(Boolean emptyOrErrorValue) {
        return getBoolean(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Boolean getBoolean(Boolean emptyValue, Boolean errorValue) {
        if (NutsBlankable.isBlank(value)) {
            return emptyValue;
        }
        return NutsUtilStrings.parseBoolean(value, emptyValue, errorValue);
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public String getString() {
        return value;
    }

    @Override
    public String getString(String defaultValue) {
        return value == null ? defaultValue : value;
    }

    public Object getObject() {
        return value;
    }

    public Object getObject(Object defaultValue) {
        return value == null ? defaultValue : value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsBootStrValImpl that = (NutsBootStrValImpl) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }


}
