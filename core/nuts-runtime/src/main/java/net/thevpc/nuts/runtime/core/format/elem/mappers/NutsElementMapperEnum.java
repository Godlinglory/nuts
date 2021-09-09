package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.reflect.ReflectUtils;

import java.lang.reflect.Type;

public class NutsElementMapperEnum implements NutsElementMapper<Enum> {

    @Override
    public Object destruct(Enum src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsElement createElement(Enum o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.element().forString(String.valueOf(o));
    }

    @Override
    public Enum createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        switch (o.type()) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG: {
                NutsPrimitiveElement p = o.asPrimitive();
                return (Enum) ((Class) to).getEnumConstants()[p.getInt()];
            }
            case STRING: {
                NutsPrimitiveElement p = o.asPrimitive();
                return Enum.valueOf(ReflectUtils.getRawClass(to), p.getString());
            }
        }
        throw new NutsUnsupportedEnumException(context.getSession(), o.type());
    }
}
