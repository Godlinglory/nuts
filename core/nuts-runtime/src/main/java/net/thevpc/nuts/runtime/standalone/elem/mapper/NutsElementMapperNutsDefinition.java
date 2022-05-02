package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementMapper;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNutsDefinition;

import java.lang.reflect.Type;

public class NutsElementMapperNutsDefinition implements NutsElementMapper<NutsDefinition> {

    @Override
    public Object destruct(NutsDefinition src, Type typeOfSrc, NutsElementFactoryContext context) {
        DefaultNutsDefinition dd = (src instanceof DefaultNutsDefinition) ? (DefaultNutsDefinition) src : new DefaultNutsDefinition(src, context.getSession());
        return context.defaultDestruct(dd, null);
    }

    @Override
    public NutsElement createElement(NutsDefinition o, Type typeOfSrc, NutsElementFactoryContext context) {
        DefaultNutsDefinition dd = (o instanceof DefaultNutsDefinition) ? (DefaultNutsDefinition) o : new DefaultNutsDefinition(o, context.getSession());
        return context.defaultObjectToElement(dd, null);
    }

    @Override
    public NutsDefinition createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsDefinition d = context.defaultElementToObject(o, DefaultNutsDefinition.class);
        //pass the session the the instance
        return new DefaultNutsDefinition(d, context.getSession());
    }
}
