package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.*;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.function.Predicate;

import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.format.NIterableFormat;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.json.DefaultSearchFormatJson;
import net.thevpc.nuts.runtime.standalone.format.plain.DefaultSearchFormatPlain;
import net.thevpc.nuts.runtime.standalone.format.props.DefaultSearchFormatProps;
import net.thevpc.nuts.runtime.standalone.format.table.DefaultSearchFormatTable;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextManagerModel;
import net.thevpc.nuts.runtime.standalone.format.tree.DefaultSearchFormatTree;
import net.thevpc.nuts.runtime.standalone.format.xml.DefaultSearchFormatXml;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NProgressFactory;

public class DefaultNElements extends DefaultFormatBase<NElements> implements NElements {

    //    public static final NutsPrimitiveElement NULL = new DefaultNPrimitiveElement(NutsElementType.NULL, null);
//    public static final NutsPrimitiveElement TRUE = new DefaultNPrimitiveElement(NutsElementType.BOOLEAN, true);
//    public static final NutsPrimitiveElement FALSE = new DefaultNPrimitiveElement(NutsElementType.BOOLEAN, false);
    private static Predicate<Class> DEFAULT_INDESTRUCTIBLE_FORMAT = new Predicate<Class>() {
        @Override
        public boolean test(Class x) {
            switch (x.getName()) {
                case "boolean":
                case "byte":
                case "short":
                case "int":
                case "long":
                case "float":
                case "double":
                case "java.lang.String":
                case "java.lang.StringBuilder":
                case "java.lang.Boolean":
                case "java.lang.Byte":
                case "java.lang.Short":
                case "java.lang.Integer":
                case "java.lang.Long":
                case "java.lang.Float":
                case "java.lang.Double":
                case "java.math.BigDecimal":
                case "java.math.BigInteger":
                case "java.util.Date":
                case "java.sql.Time":
                    return true;
            }
            if (Temporal.class.isAssignableFrom(x)) {
                return true;
            }
            if (java.util.Date.class.isAssignableFrom(x)) {
                return true;
            }
            return (
                    NString.class.isAssignableFrom(x)
                            || NElement.class.isAssignableFrom(x)
                            || NFormattable.class.isAssignableFrom(x)
                            || NMsg.class.isAssignableFrom(x)
            );
        }
    };
    private final DefaultNTextManagerModel model;
    private Object value;
    private NContentType contentType = NContentType.JSON;
    private boolean compact;
    private boolean logProgress;
    private boolean traceProgress;
    private NProgressFactory progressFactory;
    private Predicate<Class> indestructibleObjects;

    public DefaultNElements(NSession session) {
        super(session, "element-format");
        this.model = NWorkspaceExt.of(session).getModel().textModel;
    }


    public boolean isLogProgress() {
        return logProgress;
    }

    public NElements setLogProgress(boolean logProgress) {
        this.logProgress = logProgress;
        return this;
    }

    public boolean isTraceProgress() {
        return traceProgress;
    }

    public NElements setTraceProgress(boolean traceProgress) {
        this.traceProgress = traceProgress;
        return this;
    }

    @Override
    public NContentType getContentType() {
        return contentType;
    }

    @Override
    public NElements setContentType(NContentType contentType) {
//        checkSession();
        if (contentType == null) {
            this.contentType = NContentType.JSON;
        } else {
//            switch (contentType) {
//                case TREE:
//                case TABLE:
//                case PLAIN: {
//                    throw new NutsIllegalArgumentException(getSession(), "invalid content type " + contentType + ". Only structured content types are allowed.");
//                }
//            }
            this.contentType = contentType;
        }
        return this;
    }

    @Override
    public NElements json() {
        return setContentType(NContentType.JSON);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NElements setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public NElementPath compilePath(String pathExpression) {
        checkSession();
        return NElementPathFilter.compile(pathExpression, getSession());
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public NElements setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    @Override
    public <T> T parse(URL url, Class<T> clazz) {
        checkSession();
        return parse(NPath.of(url, getSession()), clazz);
    }

    private InputStream prepareInputStream(InputStream is, Object origin) {
        if (isLogProgress() || isTraceProgress()) {
            return NInputStreamMonitor.of(getSession())
                    .setSource(is)
                    .setOrigin(origin)
                    .setLogProgress(isLogProgress())
                    .setTraceProgress(isTraceProgress())
                    .setProgressFactory(getProgressFactory())
                    .create();
        }
        return is;
    }

    private InputStream prepareInputStream(NPath path) {
        if (isLogProgress()) {
            return NInputStreamMonitor.of(getSession())
                    .setSource(path)
                    .setOrigin(path)
                    .setLogProgress(isLogProgress())
                    .setTraceProgress(isTraceProgress())
                    .setProgressFactory(getProgressFactory())
                    .create();
        }
        return path.getInputStream();
    }

    @Override
    public <T> T parse(NPath path, Class<T> clazz) {
        checkSession();
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                try {
                    try (InputStream is = prepareInputStream(path)) {
                        return parse(new InputStreamReader(is), clazz);
                    } catch (NException ex) {
                        throw ex;
                    } catch (UncheckedIOException ex) {
                        throw new NIOException(getSession(), ex);
                    } catch (RuntimeException ex) {
                        throw new NParseException(getSession(), NMsg.ofC("unable to parse path %s", path), ex);
                    }
                } catch (IOException ex) {
                    throw new NParseException(getSession(), NMsg.ofC("unable to parse path %s", path), ex);
                }
            }
        }
        throw new NIllegalArgumentException(getSession(), NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    @Override
    public <T> T parse(InputStream inputStream, Class<T> clazz) {
        checkSession();
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parse(new InputStreamReader(prepareInputStream(inputStream, null)), clazz);
            }
        }
        throw new NIllegalArgumentException(getSession(), NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    @Override
    public <T> T parse(String string, Class<T> clazz) {
        checkSession();
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parse(new StringReader(string), clazz);
            }
        }
        throw new NIllegalArgumentException(getSession(), NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    @Override
    public <T> T parse(byte[] bytes, Class<T> clazz) {
        checkSession();
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parse(new InputStreamReader(prepareInputStream(new ByteArrayInputStream(bytes), null)), clazz);
            }
        }
        throw new NIllegalArgumentException(getSession(), NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    @Override
    public <T> T parse(Reader reader, Class<T> clazz) {
        return (T) elementToObject(resolveStructuredFormat().parseElement(reader, createFactoryContext()), clazz);
    }

    @Override
    public <T> T parse(Path file, Class<T> clazz) {
        checkSession();
        return parse(NPath.of(file, getSession()), clazz);
    }

    @Override
    public <T> T parse(File file, Class<T> clazz) {
        checkSession();
        return parse(NPath.of(file, getSession()), clazz);
    }

    @Override
    public NElement parse(URL url) {
        return parse(url, NElement.class);
    }

    @Override
    public NElement parse(InputStream inputStream) {
        return parse(inputStream, NElement.class);
    }

    @Override
    public NElement parse(String string) {
        if (string == null || string.isEmpty()) {
            return ofNull();
        }
        return parse(string, NElement.class);
    }

    @Override
    public NElement parse(byte[] bytes) {
        return parse(bytes, NElement.class);
    }

    @Override
    public NElement parse(Reader reader) {
        return parse(reader, NElement.class);
    }

    @Override
    public NElement parse(Path file) {
        return parse(file, NElement.class);
    }

    @Override
    public NElement parse(File file) {
        return parse(file, NElement.class);
    }

    @Override
    public NElement parse(NPath file) {
        return parse(file, NElement.class);
    }

    @Override
    public <T> T convert(Object any, Class<T> to) {
        if (to == null || to.isInstance(any)) {
            return (T) any;
        }
        NElement e = toElement(any);
        return (T) elementToObject(e, to);
    }

    //    @Override
//    public NutsElement objectToElement(Object o) {
//        return convert(o, NutsElement.class);
//    }
    @Override
    public Object destruct(Object any) {
        return createFactoryContext().destruct(any, null);
    }

    @Override
    public NElement toElement(Object o) {
        return createFactoryContext().objectToElement(o, null);
    }

    @Override
    public <T> T fromElement(NElement o, Class<T> to) {
        return convert(o, to);
    }

    @Override
    public NElementEntry ofEntry(NElement key, NElement value) {
        return new DefaultNElementEntry(
                key == null ? ofNull() : key,
                value == null ? ofNull() : value
        );
    }

    //    @Override
//    public NutsPrimitiveElementBuilder forPrimitive() {
//        return new DefaultNPrimitiveElementBuilder(getSession());
//    }
    @Override
    public NObjectElementBuilder ofObject() {
        return new DefaultNObjectElementBuilder(getSession());
    }

    @Override
    public NArrayElementBuilder ofArray() {
        return new DefaultNArrayElementBuilder(getSession());
    }

    @Override
    public NArrayElement ofEmptyArray() {
        return ofArray().build();
    }

    @Override
    public NObjectElement ofEmptyObject() {
        return ofObject().build();
    }

    @Override
    public NPrimitiveElement ofBoolean(String value) {
        NOptional<Boolean> o = NLiteral.of(value).asBoolean();
        if (o.isEmpty()) {
            return ofNull();
        }
        return ofBoolean(o.get());
    }

    //    public NutsPrimitiveElement forNutsString(NutsString str) {
//        return str == null ? DefaultNPrimitiveElementBuilder.NULL : new DefaultNPrimitiveElement(NutsElementType.NUTS_STRING, str);
//    }
    @Override
    public NPrimitiveElement ofBoolean(boolean value) {
        checkSession();
        //TODO: perhaps we can optimize this
        if (value) {
            return new DefaultNPrimitiveElement(NElementType.BOOLEAN, true, getSession());
        } else {
            return new DefaultNPrimitiveElement(NElementType.BOOLEAN, false, getSession());
        }
    }

    public NPrimitiveElement ofString(String str) {
        checkSession();
        return str == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.STRING, str, getSession());
    }

    @Override
    public NCustomElement ofCustom(Object object) {
        checkSession();
        NSession session = getSession();
        NAssert.requireNonNull(object, "custom element", session);
        return new DefaultNCustomElement(object, session);
    }

    @Override
    public NPrimitiveElement ofTrue() {
        return ofBoolean(true);
    }

    @Override
    public NPrimitiveElement ofFalse() {
        return ofBoolean(false);
    }

    @Override
    public NPrimitiveElement ofInstant(Instant instant) {
        checkSession();
        return instant == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.INSTANT, instant, getSession());
    }

    @Override
    public NPrimitiveElement ofFloat(Float value) {
        checkSession();
        return value == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.FLOAT, value, getSession());
    }

    @Override
    public NPrimitiveElement ofInt(Integer value) {
        checkSession();
        return value == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.INTEGER, value, getSession());
    }

    @Override
    public NPrimitiveElement ofLong(Long value) {
        checkSession();
        return value == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.LONG, value, getSession());
    }

    @Override
    public NPrimitiveElement ofNull() {
        checkSession();
        //perhaps we can optimize this?
        return new DefaultNPrimitiveElement(NElementType.NULL, null, getSession());
    }

    @Override
    public NPrimitiveElement ofNumber(String value) {
        checkSession();
        if (value == null) {
            return ofNull();
        }
        if (value.indexOf('.') >= 0) {
            try {
                return ofNumber(Double.parseDouble(value));
            } catch (Exception ex) {

            }
            try {
                return ofNumber(new BigDecimal(value));
            } catch (Exception ex) {

            }
        } else {
            try {
                return ofNumber(Integer.parseInt(value));
            } catch (Exception ex) {

            }
            try {
                return ofNumber(Long.parseLong(value));
            } catch (Exception ex) {

            }
            try {
                return ofNumber(new BigInteger(value));
            } catch (Exception ex) {

            }
        }
        throw new NParseException(getSession(), NMsg.ofC("unable to parse number %s", value));
    }

    @Override
    public NPrimitiveElement ofInstant(Date value) {
        checkSession();
        if (value == null) {
            return ofNull();
        }
        return new DefaultNPrimitiveElement(NElementType.INSTANT, value.toInstant(), getSession());
    }

    @Override
    public NPrimitiveElement ofInstant(String value) {
        checkSession();
        if (value == null) {
            return ofNull();
        }
        return new DefaultNPrimitiveElement(NElementType.INSTANT, DefaultNLiteral.parseInstant(value).get(getSession()), getSession());
    }

    @Override
    public NPrimitiveElement ofByte(Byte value) {
        checkSession();
        return value == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.BYTE, value, getSession());
    }

    @Override
    public NPrimitiveElement ofDouble(Double value) {
        checkSession();
        return value == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.DOUBLE, value, getSession());
    }

    @Override
    public NPrimitiveElement ofFloat(Short value) {
        checkSession();
        return value == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.SHORT, value, getSession());
    }

    @Override
    public NPrimitiveElement ofNumber(Number value) {
        checkSession();
        if (value == null) {
            return ofNull();
        }
        switch (value.getClass().getName()) {
            case "java.lang.Byte":
                return new DefaultNPrimitiveElement(NElementType.BYTE, value, getSession());
            case "java.lang.Short":
                return new DefaultNPrimitiveElement(NElementType.SHORT, value, getSession());
            case "java.lang.Integer":
                return new DefaultNPrimitiveElement(NElementType.INTEGER, value, getSession());
            case "java.lang.Long":
                return new DefaultNPrimitiveElement(NElementType.LONG, value, getSession());
            case "java.math.BigInteger":
                return new DefaultNPrimitiveElement(NElementType.BIG_INTEGER, value, getSession());
            case "java.lang.float":
                return new DefaultNPrimitiveElement(NElementType.FLOAT, value, getSession());
            case "java.lang.Double":
                return new DefaultNPrimitiveElement(NElementType.DOUBLE, value, getSession());
            case "java.math.BigDecimal":
                return new DefaultNPrimitiveElement(NElementType.BIG_DECIMAL, value, getSession());
        }
        // ???
        return new DefaultNPrimitiveElement(NElementType.FLOAT, value, getSession());
    }

    public Predicate<Class> getIndestructibleObjects() {
        return indestructibleObjects;
    }

    @Override
    public NElements setIndestructibleFormat() {
        return setIndestructibleObjects(DEFAULT_INDESTRUCTIBLE_FORMAT);
    }

    public NElements setIndestructibleObjects(Predicate<Class> destructTypeFilter) {
        this.indestructibleObjects = destructTypeFilter;
        return this;
    }

    @Override
    public NIterableFormat iter(NOutputStream writer) {
        switch (getContentType()) {
            case JSON:
                return new DefaultSearchFormatJson(getSession(), writer, new NFetchDisplayOptions(getSession()));
            case XML:
                return new DefaultSearchFormatXml(getSession(), writer, new NFetchDisplayOptions(getSession()));
            case PLAIN:
                return new DefaultSearchFormatPlain(getSession(), writer, new NFetchDisplayOptions(getSession()));
            case TABLE:
                return new DefaultSearchFormatTable(getSession(), writer, new NFetchDisplayOptions(getSession()));
            case TREE:
                return new DefaultSearchFormatTree(getSession(), writer, new NFetchDisplayOptions(getSession()));
            case PROPS:
                return new DefaultSearchFormatProps(getSession(), writer, new NFetchDisplayOptions(getSession()));
        }
        throw new NUnsupportedOperationException(getSession(), NMsg.ofC("unsupported iterator for %s", getContentType()));
    }

    @Override
    public <T> NElements setMapper(Class<T> type, NElementMapper<T> mapper) {
        checkSession();
        ((DefaultNElementFactoryService) getElementFactoryService())
                .setMapper(type, mapper);
        return this;
    }

    private NElementStreamFormat resolveStructuredFormat() {
        checkSession();
        switch (contentType) {
            case JSON: {
                return model.getJsonMan(getSession());
            }
            case YAML: {
                return model.getYamlMan(getSession());
            }
            case XML: {
                return model.getXmlMan(getSession());
            }
            case TSON: {
                throw new NUnsupportedEnumException(getSession(), contentType);
            }
        }
        throw new NIllegalArgumentException(getSession(), NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    private DefaultNElementFactoryContext createFactoryContext() {
        DefaultNElementFactoryContext c = new DefaultNElementFactoryContext(this);
        switch (getContentType()) {
            case XML:
            case JSON:
            case TSON:
            case YAML: {
                c.setNtf(false);
                break;
            }
        }
        return c;
    }

    @Override
    public boolean configureFirst(NCommandLine cmdLine) {
        return false;
    }

    private void print(NOutputStream out, NElementStreamFormat format) {
        checkSession();
        NElement elem = toElement(value);
        if (out.isNtf()) {
            NOutputStream bos = NMemoryOutputStream.of(getSession());
            format.printElement(elem, bos, compact, createFactoryContext());
            out.print(NTexts.of(getSession()).ofCode(getContentType().id(), bos.toString()));
        } else {
            format.printElement(elem, out, compact, createFactoryContext());
        }
        out.flush();
    }

    @Override
    public void print(NOutputStream out) {
        print(out, resolveStructuredFormat());
    }

    public Object elementToObject(NElement o, Type type) {
        return createFactoryContext().elementToObject(o, type);
    }

    public NElementFactoryService getElementFactoryService() {
        return model.getElementFactoryService(getSession());
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NProgressFactory getProgressFactory() {
        return progressFactory;
    }

    @Override
    public NElements setProgressFactory(NProgressFactory progressFactory) {
        this.progressFactory = progressFactory;
        return this;
    }
}
