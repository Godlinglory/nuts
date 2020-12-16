package net.thevpc.nuts.runtime.standalone.format.props;

import java.io.PrintStream;
import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.ObjectOutputFormatWriterHelper;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.common.CoreCommonUtils;

public class DefaultPropertiesFormat extends DefaultFormatBase<NutsPropertiesFormat> implements NutsPropertiesFormat {

    public static final String OPTION_MULTILINE_PROPERTY = "--multiline-property";
    private boolean sort;
    private boolean compact;
    private boolean javaProps;
    private final String rootName = "";
    private final boolean omitNull = true;
    private boolean escapeText = true;
    private String separator = " = ";
    private Object value;
    private Map<String, String> multilineProperties = new HashMap<>();

    public DefaultPropertiesFormat(NutsWorkspace ws) {
        super(ws, "props-format");
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        NutsArgument a;
        if ((a = commandLine.nextString(OPTION_MULTILINE_PROPERTY)) != null) {
            NutsArgument i = a.getArgumentValue();
            if(i.isEnabled()) {
                addMultilineProperty(i.getStringKey(), i.getStringValue());
            }
            return true;
        } else if ((a = commandLine.nextBoolean("--compact")) != null) {
            if(a.isEnabled()) {
                this.compact = a.getBooleanValue();
            }
            return true;
        } else if ((a = commandLine.nextBoolean("--props")) != null) {
            if(a.isEnabled()) {
                this.javaProps = a.getBooleanValue();
            }
            return true;
        } else if ((a = commandLine.nextBoolean("--escape-text")) != null) {
            if(a.isEnabled()) {
                this.escapeText = a.getBooleanValue();
            }
            return true;
        }
        return false;
    }

    public DefaultPropertiesFormat addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    public Map buildModel() {
        Object value=getValue();
        if(value instanceof Map){
            return (Map) value;
        }
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        fillMap(getWorkspace().formats().element().convert(value,NutsElement.class), map, rootName);
        return map;
    }

    private void fillMap(NutsElement e, Map<String, String> map, String prefix) {
        switch (e.type()) {
            case NULL: {
                if (omitNull) {
                    //do nothing;
                } else {
                    String k = (CoreStringUtils.isBlank(prefix)) ? "value" : prefix;
                    map.put(k, stringValue(e.primitive().getValue()));
                }
                break;
            }
            case BOOLEAN:
            case DATE:
            case INTEGER:
            case FLOAT:
            case STRING: {
                String k = (CoreStringUtils.isBlank(prefix)) ? "value" : prefix;
                map.put(k, stringValue(e.primitive().getValue()));
                break;
            }
            case ARRAY: {
                int index = 1;
                for (NutsElement datum : e.array().children()) {
                    String k = (CoreStringUtils.isBlank(prefix)) ? String.valueOf(index) : (prefix + "." + String.valueOf(index));
                    fillMap(datum, map, k);
                    index++;
                }
                break;
            }
            case OBJECT: {
                for (NutsNamedElement datum : e.object().children()) {
                    String k = (CoreStringUtils.isBlank(prefix)) ? datum.getName() : (prefix + "." + datum.getName());
                    fillMap(datum.getValue(), map, k);
                }
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getWorkspace(), e.type().name());
            }
        }
    }

    @Override
    public Map getModel() {
        return buildModel();
    }

    @Override
    public NutsPropertiesFormat sort() {
        return sort(true);
    }

    @Override
    public NutsPropertiesFormat separator(String separator) {
        return setSeparator(separator);
    }

    @Override
    public NutsPropertiesFormat sort(boolean sort) {
        return setSort(sort);
    }

    public NutsPropertiesFormat setValue(Map model) {
        return setValue(model);
    }

    public boolean isSort() {
        return sort;
    }

//    @Override
//    public NutsPropertiesFormat table(boolean table) {
//        return setTable(table);
//    }
//    @Override
//    public NutsPropertiesFormat table() {
//        return table(true);
//    }
//    public boolean isTable() {
//        return table;
//    }
//
//    public DefaultPropertiesFormat setTable(boolean table) {
//        this.table = table;
//        return this;
//    }
    public String getSeparator() {
        return separator;
    }

    public DefaultPropertiesFormat setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public DefaultPropertiesFormat setSort(boolean sort) {
        this.sort = sort;
        return this;
    }

    @Override
    public void print(PrintStream w) {
        PrintStream out = getValidPrintStream(w);
        Map<Object, Object> mm;
        Map model = buildModel();
        if (sort) {
            mm = new LinkedHashMap<>();
            List<Object> keys = new ArrayList(model.keySet());
            if (sort) {
                keys.sort(null);
            }
            for (Object k : keys) {
                Object v = model.get(k);
                mm.put(k, v);
            }
        } else {
            mm = model;
        }
        if (javaProps) {
            CoreIOUtils.storeProperties(ObjectOutputFormatWriterHelper.explodeMap(mm), w, sort);
        } else {
            printMap(out, "", mm);
        }
    }

//    private String formatValue(Object value) {
//        if (value == null) {
//            return "";
//        }
//        StringBuilder sb = new StringBuilder();
//        String svalue = String.valueOf(value);
//        for (char c : svalue.toCharArray()) {
//            switch (c) {
//                case '\n': {
//                    sb.append("\\n");
//                    break;
//                }
//                default: {
//                    sb.append(c);
//                    break;
//                }
//            }
//        }
//        return sb.toString();
//    }
    private void printMap(PrintStream out, String prefix, Map<Object, Object> props) {
        int len = 1;
        for (Object extraKey : props.keySet()) {
            int x = getWorkspace().formats().text().textLength(stringValue(extraKey));
            if (x > len) {
                len = x;
            }
        }
        boolean first = true;
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            if (first) {
                first = false;
            } else {
                out.println();
            }
            printKeyValue(out, prefix, len, stringValue(e.getKey()), stringValue(e.getValue()));
        }
        out.flush();
    }

    private String getMultilineSeparator(String key) {
        String sep = multilineProperties.get(key);
        if (sep != null && sep.length() == 0) {
            sep = ":|;";
        }
        return sep;
    }

    private void printKeyValue(PrintStream out, String prefix, int len, String key, String value) {
        printKeyValue(out, prefix, len, getMultilineSeparator(key), key, value);
    }

    private void printKeyValue(PrintStream out, String prefix, int len, String fancySep, String key, String value) {
        if (prefix == null) {
            prefix = "";
        }
        String ekey = getWorkspace().formats().text().escapeText(key);
        int delta = key.length() - ekey.length();
        String formattedKey = compact ? key : CoreStringUtils.alignLeft(ekey, len - delta);
        if (fancySep != null) {
            String cc = compact ? key : CoreStringUtils.alignLeft("", len+3);
            String[] split = value.split(fancySep);
            if (split.length == 0) {
                out.print(prefix);
                out.print(formattedKey);
                out.print(separator);
            } else {
                for (int i = 0; i < split.length; i++) {
                    String s = split[i];
                    if (i == 0) {
                        out.print(prefix);
                        if(prefix.isEmpty() || prefix.endsWith("#")){
                            out.print("ø");
                        }
                        out.print("####"+formattedKey+"####");
                        if(separator.isEmpty() || separator.startsWith("#")){
                            out.print("ø");
                        }
                        out.print(separator);
                        out.print( s);
                    } else {
                        out.println();
                        out.print(cc);
                        out.print(s);
                    }
                    //                    }
                }
            }
        } else {
            out.print(prefix);
            if(prefix.isEmpty() || prefix.endsWith("#")){
                out.print("ø");
            }
            out.print("####"+formattedKey+"####");
            if(separator.isEmpty() || separator.startsWith("#")){
                out.print("ø");
            }
            out.print(separator);
            out.print(value);
        }
    }

    private String stringValue(Object o) {
        if (escapeText) {
            return CoreCommonUtils.stringValueFormatted(o, escapeText,getValidSession());
        } else {
            return String.valueOf(o);
        }
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NutsPropertiesFormat setValue(Object value) {
        this.value=value;
        return this;
    }
}
