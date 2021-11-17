package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.*;

import java.io.File;
import java.lang.reflect.Type;

public class NutsElementMapperFile implements NutsElementMapper<File> {

    @Override
    public Object destruct(File src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsElement createElement(File o, Type typeOfSrc, NutsElementFactoryContext context) {
        if (context.isNtf()) {
            NutsSession session = context.getSession();
//                NutsText n = ws.text().forStyled(o.toString(), NutsTextStyle.path());
//                return ws.elem().forPrimitive().buildNutsString(n);
            NutsText n = NutsTexts.of(session).ofStyled(o.toString(), NutsTextStyle.path());
            return NutsElements.of(session).forString(n.toString());
        } else {
            return context.defaultObjectToElement(o.toString(), null);
        }
    }

    @Override
    public File createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        return new File(o.asPrimitive().getString());
    }
}
