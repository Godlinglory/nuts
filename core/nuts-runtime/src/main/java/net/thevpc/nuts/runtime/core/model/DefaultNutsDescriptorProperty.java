package net.thevpc.nuts.runtime.core.model;

import net.thevpc.nuts.NutsDescriptorProperty;
import net.thevpc.nuts.NutsDescriptorPropertyBuilder;
import net.thevpc.nuts.NutsEnvCondition;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

import java.util.Objects;

class DefaultNutsDescriptorProperty implements NutsDescriptorProperty {
    private String name;
    private String value;
    private NutsEnvCondition condition;
    private transient NutsSession session;

    public DefaultNutsDescriptorProperty(String name, String value, NutsEnvCondition condition, NutsSession session) {
        this.name = name;
        this.value = value;
        this.condition = CoreNutsUtils.trimToBlank(condition,session);
        this.session = session;
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
    public NutsEnvCondition getCondition() {
        return condition;
    }

    @Override
    public NutsDescriptorPropertyBuilder builder() {
        return new DefaultNutsDescriptorPropertyBuilder(session)
                .setName(getName())
                .setValue(getValue())
                .setCondition(getCondition());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsDescriptorProperty that = (DefaultNutsDescriptorProperty) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value) && Objects.equals(condition, that.condition) && Objects.equals(session, that.session);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, condition);
    }

    @Override
    public String toString() {
        return name+"='" + value + '\'' +
                (condition.isBlank()?"":(" when "+condition))
                ;
    }
}
