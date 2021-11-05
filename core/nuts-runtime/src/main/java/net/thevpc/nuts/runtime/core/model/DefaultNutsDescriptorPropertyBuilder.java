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
package net.thevpc.nuts.runtime.core.model;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class DefaultNutsDescriptorPropertyBuilder implements NutsDescriptorPropertyBuilder {

    private static final long serialVersionUID = 1L;

    private String name;
    private String value;
    private NutsEnvConditionBuilder condition;
    private transient NutsSession session;

    public DefaultNutsDescriptorPropertyBuilder() {
    }

    public DefaultNutsDescriptorPropertyBuilder(NutsSession session) {
        this.session = session;
        this.condition = NutsEnvConditionBuilder.of(session);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public NutsDescriptorPropertyBuilder setCondition(NutsEnvConditionBuilder condition) {
        this.condition.setAll(condition);
        return this;
    }

    @Override
    public NutsDescriptorPropertyBuilder setCondition(NutsEnvCondition condition) {
        this.condition.setAll(condition);
        return this;
    }

    @Override
    public NutsEnvConditionBuilder getCondition() {
        return condition;
    }

    @Override
    public NutsDescriptorPropertyBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NutsDescriptorPropertyBuilder setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public NutsDescriptorPropertyBuilder setAll(NutsDescriptorPropertyBuilder value) {
        if (value == null) {
            this.setName(null);
            this.setValue(null);
            this.setCondition((NutsEnvCondition) null);
        } else {
            this.setName(value.getName());
            this.setValue(value.getValue());
            this.setCondition(value.getCondition());
        }
        return this;
    }

    @Override
    public NutsDescriptorPropertyBuilder setAll(NutsDescriptorProperty value) {
        if (value == null) {
            this.setName(null);
            this.setValue(null);
            this.setCondition((NutsEnvCondition) null);
        } else {
            this.setName(value.getName());
            this.setValue(value.getValue());
            this.setCondition(value.getCondition());
        }
        return this;
    }

    @Override
    public NutsDescriptorProperty build() {
        return new DefaultNutsDescriptorProperty(getName(), getValue(), getCondition().build(), session);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
