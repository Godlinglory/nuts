package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveDoubleArray implements NElementMapper<double[]> {

    public NElementMapperPrimitiveDoubleArray() {
    }

    @Override
    public NElement createElement(double[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object destruct(double[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._destructArray1(src, context);
    }

    @Override
    public double[] createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NSession session = context.getSession();
        NArrayElement earr = o.asArray().get(session);
        double[] arr = new double[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (double) context.elementToObject(earr.get(i).get(session), double.class);
        }
        return arr;
    }
}
