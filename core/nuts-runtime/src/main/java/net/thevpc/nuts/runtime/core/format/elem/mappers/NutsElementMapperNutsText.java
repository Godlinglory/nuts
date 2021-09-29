package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.NutsText;

import java.lang.reflect.Type;

public class NutsElementMapperNutsText implements NutsElementMapper<NutsText> {

    @Override
    public Object destruct(NutsText src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src.filteredText();
    }

    @Override
    public NutsElement createElement(NutsText o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(destruct(o, null, context), null);
    }

    @Override
    public NutsText createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        String i = context.defaultElementToObject(o, String.class);
        //return context.getSession().text().parse(i).toText();
        return context.getSession().text().forPlain(i).toText();
    }
}
