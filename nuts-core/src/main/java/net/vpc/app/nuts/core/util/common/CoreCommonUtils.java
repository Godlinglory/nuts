/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.util.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.stream.Collectors;
import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.app.DefaultNutsArgument;

public class CoreCommonUtils {

    public static String[] toArraySet(String[] values0, String[]... values) {
        Set<String> set = toSet(values0);
        if (values != null) {
            for (String[] value : values) {
                set.addAll(toSet(value));
            }
        }
        return set.toArray(new String[0]);
    }

    public static Set<String> toSet(String[] values0) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (values0 != null) {
            for (String a : values0) {
                a = CoreStringUtils.trim(a);
                if (!CoreStringUtils.isBlank(a) && !set.contains(a)) {
                    set.add(a);
                }
            }
        }
        return set;
    }

    public static List<Class> loadServiceClasses(Class service, ClassLoader classLoader) {
        String fullName = "META-INF/services/" + service.getName();
        Enumeration<URL> configs;
        LinkedHashSet<String> names = new LinkedHashSet<>();
        try {
            if (classLoader == null) {
                configs = ClassLoader.getSystemResources(fullName);
            } else {
                configs = classLoader.getResources(fullName);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        while (configs.hasMoreElements()) {
            names.addAll(loadServiceClasses(service, configs.nextElement()));
        }
        List<Class> classes = new ArrayList<>();
        for (String n : names) {
            Class<?> c = null;
            try {
                c = Class.forName(n, false, classLoader);
            } catch (ClassNotFoundException x) {
                throw new NutsException(null, x);
            }
            if (!service.isAssignableFrom(c)) {
                throw new NutsException(null, "Not a valid type " + c + " <> " + service);
            }
            classes.add(c);
        }
        return classes;
    }

    public static List<String> loadServiceClasses(Class<?> service, URL u) throws ServiceConfigurationError {
        InputStream in = null;
        BufferedReader r = null;
        List<String> names = new ArrayList<>();
        try {
            in = u.openStream();
            r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            int lc = 1;
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && line.charAt(0) != '#') {
                    names.add(line);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex2) {
                throw new UncheckedIOException(ex2);
            }
        }
        return names;
    }

    private static String suffix(String s) {
        char[] chars = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public static boolean getSystemBoolean(String property, boolean defaultValue) {
        return new DefaultNutsArgument(System.getProperty(property), '=').getBoolean(defaultValue);
    }

    public static String[] concatArrays(String[]... arrays) {
        return concatArrays(String.class, arrays);
    }

    public static <T> T[] concatArrays(Class<T> cls, T[]... arrays) {
        List<T> all = new ArrayList<>();
        if (arrays != null) {
            for (T[] v : arrays) {
                if (v != null) {
                    all.addAll(Arrays.asList(v));
                }
            }
        }
        return all.toArray((T[]) Array.newInstance(cls, all.size()));
    }

    public static Integer convertToInteger(String value, Integer defaultValue) {
        if (CoreStringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static String formatPeriodMilli(long period) {
        StringBuilder sb = new StringBuilder();
        boolean started = false;
        int h = (int) (period / (1000L * 60L * 60L));
        int mn = (int) ((period % (1000L * 60L * 60L)) / 60000L);
        int s = (int) ((period % 60000L) / 1000L);
        int ms = (int) (period % 1000L);
        if (h > 0) {
            sb.append(CoreStringUtils.alignRight(String.valueOf(h), 2)).append("h ");
            started = true;
        }
        if (mn > 0 || started) {
            sb.append(CoreStringUtils.alignRight(String.valueOf(mn), 2)).append("mn ");
            started = true;
        }
        if (s > 0 || started) {
            sb.append(CoreStringUtils.alignRight(String.valueOf(s), 2)).append("s ");
            //started=true;
        }
        sb.append(CoreStringUtils.alignRight(String.valueOf(ms), 3)).append("ms");
        return sb.toString();
    }

    public static String setterName(String name) {
        //Class<?> type = field.getDataType();
        return "set" + suffix(name);
    }

    public static String getterName(String name, Class type) {
        if (Boolean.TYPE.equals(type)) {
            return "is" + suffix(name);
        }
        return "get" + suffix(name);
    }

    public static <T> List<T> toList(Iterator<T> it) {
        List<T> all = new ArrayList<>();
        while (it.hasNext()) {
            all.add(it.next());
        }
        return all;
    }

    public static <T> Iterator<T> nullifyIfEmpty(Iterator<T> other) {
        if (other == null) {
            return null;
        }
        if (other instanceof PushBackIterator) {
            PushBackIterator<T> b = (PushBackIterator<T>) other;
            if (!b.isEmpty()) {
                return b;
            } else {
                return null;
            }
        }
        PushBackIterator<T> b = new PushBackIterator<>(other);
        if (!b.isEmpty()) {
            return b;
        } else {
            return null;
        }
    }

//    public static boolean isYes(String s) {
//        switch (s == null ? "" : s.trim().toLowerCase()) {
//            case "ok":
//            case "true":
//            case "yes":
//            case "always":
//            case "y":
//                return true;
//        }
//        return false;
//    }
//
//    public static boolean isNo(String s) {
//        switch (s == null ? "" : s.trim().toLowerCase()) {
//            case "false":
//            case "no":
//            case "none":
//            case "never":
//                return true;
//        }
//        return false;
//    }
    public static void putAllInProps(String prefix, Map<String, String> dest, Object value) {
        if (!CoreStringUtils.isBlank(prefix)) {
            if (value instanceof Map) {
                for (Map.Entry<Object, Object> e : ((Map<Object, Object>) value).entrySet()) {
                    putAllInProps(prefix + "." + e.getKey(), dest, e.getValue());
                }
            } else if (value instanceof List) {
                List<Object> li = (List<Object>) value;
                for (int i = 0; i < li.size(); i++) {
                    putAllInProps(prefix + "." + (i + 1), dest, li.get(i));
                }
            } else {
                dest.put(prefix, String.valueOf(value));
            }
        } else {
            if (value instanceof Map) {
                for (Map.Entry<Object, Object> e : ((Map<Object, Object>) value).entrySet()) {
                    putAllInProps(String.valueOf(e.getKey()), dest, (Map) e.getValue());
                }
            } else if (value instanceof List) {
                List<Object> li = (List<Object>) value;
                for (int i = 0; i < li.size(); i++) {
                    putAllInProps("" + (i + 1), dest, li.get(i));
                }
            } else {
                dest.put("value", String.valueOf(value));
            }
        }

    }

    public static Boolean parseBoolean(String value, Boolean defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        value = value.trim().toLowerCase();
        if (value.matches("true|enable|enabled|yes|always|y|on|ok")) {
            return true;
        }
        if (value.matches("false|disable|disabled|no|none|never|n|off|ko")) {
            return false;
        }
        return defaultValue;
    }

    public static String getEnumString(Enum e) {
        return e.toString().toLowerCase().replace("_", "-");
    }

    public static <T extends Enum> T parseEnumString(String val, Class<T> e, boolean lenient) {
        String v2 = val.toUpperCase().replace("-", "_");
        for (T enumConstant : e.getEnumConstants()) {
            if (enumConstant.toString().equals(v2)) {
                return enumConstant;
            }
        }
        if (lenient) {
            return null;
        }
        throw new NoSuchElementException(val + " of type " + e.getSimpleName());
    }

    public static String stringValueFormatted(Object o, NutsWorkspace ws, NutsSession session) {
        if (o == null) {
            return "";
        }
        if (o instanceof Boolean) {
            return ws.io().getTerminalFormat().escapeText(String.valueOf(o));
        }
        if (o.getClass().isEnum()) {
            return ws.io().getTerminalFormat().escapeText(getEnumString((Enum) o));
        }
        if (o instanceof Date) {
            return ws.io().getTerminalFormat().escapeText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format((Date) o));
        }
        if (o instanceof NutsId) {
            return ws.formatter().createIdFormat().toString((NutsId) o);
        }
        if (o instanceof Collection) {
            Collection c = ((Collection) o);
            Object[] a = c.toArray();
            if (a.length == 0) {
                return "";
            }
            if (a.length == 1) {
                return stringValue(a[0]);
            }
            return "\\[" + CoreStringUtils.join(", ", (List) c.stream().map(x -> stringValueFormatted(x, ws, session)).collect(Collectors.toList())) + "\\]";
        }
        if (o.getClass().isArray()) {
            int len = Array.getLength(o);
            if (len == 0) {
                return "";
            }
            if (len == 1) {
                return stringValueFormatted(Array.get(o, 0), ws, session);
            }
            List<String> all = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                all.add(stringValueFormatted(Array.get(o, i), ws, session));
            }
            return "\\[" + CoreStringUtils.join(", ", all) + "\\]";
        }
        if (o instanceof Iterable) {
            Iterable x = (Iterable) o;
            return stringValueFormatted(x.iterator(), ws, session);
        }
        if (o instanceof Iterator) {
            Iterator x = (Iterator) o;
            List<String> all = new ArrayList<>();
            while (x.hasNext()) {
                all.add(stringValueFormatted(x.next(), ws, session));
            }
            return "\\[" + CoreStringUtils.join(", ", (List) all.stream().collect(Collectors.toList())) + "\\]";
        }
        return o.toString();
    }

    public static String stringValue(Object o) {
        if (o == null) {
            return "";
        }
        if (o.getClass().isEnum()) {
            return getEnumString((Enum) o);
        }
        if (o instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format((Date) o);
        }
        if (o instanceof Collection) {
            Collection c = ((Collection) o);
            Object[] a = c.toArray();
            if (a.length == 0) {
                return "";
            }
            if (a.length == 1) {
                return stringValue(a[0]);
            }
            return "[" + CoreStringUtils.join(", ", (List) c.stream().map(x -> stringValue(x)).collect(Collectors.toList())) + "]";
        }
        if (o.getClass().isArray()) {
            int len = Array.getLength(o);
            if (len == 0) {
                return "";
            }
            if (len == 1) {
                return stringValue(Array.get(o, 0));
            }
            List<String> all = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                all.add(stringValue(Array.get(o, i)));
            }
            return "[" + CoreStringUtils.join(", ", all) + "]";
        }
        return o.toString();
    }

}
