package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

public class NutsElementMapperNutsEnum implements NutsElementMapper<NutsEnum> {

    public NutsElementMapperNutsEnum() {
    }

    @Override
    public NutsEnum createObject(NutsElement json, Type typeOfResult, NutsElementFactoryContext context) {
        Class cc = ReflectUtils.getRawClass(typeOfResult);
        return NutsEnum.parse(cc,json.asString(),context.getSession());
    }

    public NutsElement createElement(NutsEnum src, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.elem().forString(src.id());
    }

    @Override
    public Object destruct(NutsEnum src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

}
