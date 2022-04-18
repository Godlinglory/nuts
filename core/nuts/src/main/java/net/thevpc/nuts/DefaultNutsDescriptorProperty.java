package net.thevpc.nuts;


import java.util.Objects;

public class DefaultNutsDescriptorProperty implements NutsDescriptorProperty {
    private final String name;
    private final String value;
    private final NutsEnvCondition condition;
    public DefaultNutsDescriptorProperty(String name, String value, NutsEnvCondition condition) {
        this.name = name;
        this.value = value;
        this.condition = condition==null?NutsEnvCondition.BLANK : condition;
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
        return new DefaultNutsDescriptorPropertyBuilder()
                .setName(getName())
                .setValue(getValue())
                .setCondition(getCondition());
    }

    @Override
    public boolean isBlank() {
        if (!NutsBlankable.isBlank(name)) {
            return false;
        }
        if (!NutsBlankable.isBlank(value)) {
            return false;
        }
        return NutsBlankable.isBlank(condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, condition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsDescriptorProperty that = (DefaultNutsDescriptorProperty) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value) && Objects.equals(condition, that.condition) ;
    }

    @Override
    public String toString() {
        return name + "='" + value + '\'' +
                (condition.isBlank() ? "" : (" when " + condition))
                ;
    }

    @Override
    public NutsDescriptorProperty readOnly() {
        return this;
    }
}
