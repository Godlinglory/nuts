package net.vpc.app.nuts.runtime.format.elem;

import net.vpc.app.nuts.NutsElement;
import net.vpc.app.nuts.NutsElementType;
import net.vpc.app.nuts.NutsNamedElement;
import net.vpc.app.nuts.NutsObjectElement;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultNutsObjectElement extends AbstractNutsElement implements NutsObjectElement {
    private Map<String, NutsElement> values = new LinkedHashMap<>();

    public DefaultNutsObjectElement(Map<String, NutsElement> values) {
        super(NutsElementType.OBJECT);
        if (values != null) {
            for (Map.Entry<String, NutsElement> e : values.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    this.values.put(e.getKey(), e.getValue());
                }
            }
        }
    }

    @Override
    public Collection<NutsNamedElement> children() {
        return values.entrySet().stream().map(x -> new DefaultNutsNamedElement(x.getKey(), x.getValue())).collect(Collectors.toList());
    }

    @Override
    public NutsElement get(String s) {
        return values.get(s);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public String toString() {
        return "[" + children().stream().map(x -> "{"
                + x.getName()
                + ":"
                + x.getValue().toString()
                + "}").collect(Collectors.joining(", ")) + "]";
    }
}
