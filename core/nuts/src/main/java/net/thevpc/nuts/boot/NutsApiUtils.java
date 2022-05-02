/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.cmdline.NutsCommandLineFormatStrategy;
import net.thevpc.nuts.util.NutsLogConfig;
import net.thevpc.nuts.util.NutsUtilStrings;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * this class implements several utility methods to be used by Nuts API interfaces
 *
 * @author thevpc
 */
public class NutsApiUtils {

    private NutsApiUtils() {
    }

    public static boolean isBlank(CharSequence s) {
        return s == null || isBlank(s.toString().toCharArray());
    }

    public static boolean isBlank(Object any) {
        if (any == null) {
            return true;
        }
        if (any instanceof NutsBlankable) {
            return ((NutsBlankable) any).isBlank();
        }
        if (any instanceof CharSequence) {
            return isBlank((CharSequence) any);
        }
        if (any instanceof char[]) {
            return isBlank((char[]) any);
        }
        if (any.getClass().isArray()) {
            return Array.getLength(any) == 0;
        }
        if (any instanceof Collection) {
            return ((Collection)any).isEmpty();
        }
        if (any instanceof Map) {
            return ((Map)any).isEmpty();
        }
        return false;
    }

    public static boolean isBlank(char[] string) {
        if (string == null || string.length == 0) {
            return true;
        }
        for (char c : string) {
            if (c > ' ') {
                return false;
            }
        }
        return true;
    }

    public static void checkSession(NutsSession session) {
        if (session == null) {
            throw new NutsMissingSessionException();
        }
    }

    public static String[] parseCommandLineArray(String commandLineString) {
        return NutsCommandLine.parseDefault(commandLineString).get().toStringArray();
    }

    public static int processThrowable(Throwable ex, PrintStream out) {
        return PrivateNutsUtilApplication.processThrowable(ex, out);
    }

    public static int processThrowable(Throwable ex, String[] args) {
        PrivateNutsBootLog log = new PrivateNutsBootLog(null);
        DefaultNutsWorkspaceBootOptionsBuilder bo = new DefaultNutsWorkspaceBootOptionsBuilder();
        bo.setCommandLine(args, null);
        try {
            if (NutsApiUtils.isGraphicalDesktopEnvironment()) {
                bo.setGui(false);
            }
        } catch (Exception e) {
            //exception may occur if the sdk is build without awt package for instance!
            bo.setGui(false);
        }
        boolean bot = bo.getBot().orElse(false);
        boolean gui = !bot && bo.getGui().orElse(false);
        boolean showTrace = bo.getDebug() != null;
        NutsLogConfig nutsLogConfig = bo.getLogConfig().orElseGet(NutsLogConfig::new);
        showTrace |= (nutsLogConfig.getLogTermLevel() != null
                && nutsLogConfig.getLogTermLevel().intValue() < Level.INFO.intValue());
        if (!showTrace) {
            showTrace = NutsApiUtils.getSysBoolNutsProperty("debug", false);
        }
        if (bot) {
            showTrace = false;
        }
        return processThrowable(ex, null, true, showTrace, gui);
    }

    public static int processThrowable(Throwable ex, PrintStream out, boolean showMessage, boolean showTrace, boolean showGui) {
        return PrivateNutsUtilApplication.processThrowable(ex, out, showMessage, showTrace, showGui);
    }

    public static boolean isGraphicalDesktopEnvironment() {
        return PrivateNutsUtilGui.isGraphicalDesktopEnvironment();
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return PrivateNutsUtils.getSysBoolNutsProperty(property, defaultValue);
    }

    public static String resolveNutsVersionFromClassPath(PrivateNutsBootLog bLog) {
        return PrivateNutsUtilMavenRepos.resolveNutsApiVersionFromClassPath(bLog);
    }

    public static String resolveNutsIdDigestOrError() {
        String d = resolveNutsIdDigest();
        if (d == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL[] urls = PrivateNutsUtilClassLoader.resolveClasspathURLs(cl, true);
            throw new NutsBootException(NutsMessage.plain("unable to detect nuts digest. Most likely you are missing valid compilation of nuts." + "\n\t 'pom.properties' could not be resolved and hence, we are unable to resolve nuts version." + "\n\t java=" + System.getProperty("java.home") + " as " + System.getProperty("java.version") + "\n\t class-path=" + System.getProperty("java.class.path") + "\n\t urls=" + Arrays.toString(urls) + "\n\t class-loader=" + cl.getClass().getName() + " as " + cl));
        }
        return d;

    }

    public static String resolveNutsIdDigest() {
        //TODO COMMIT TO 0.8.4
        return resolveNutsIdDigest(NutsId.ofApi(Nuts.getVersion()).get(), PrivateNutsUtilClassLoader.resolveClasspathURLs(Nuts.class.getClassLoader(), true));
    }

    public static String resolveNutsIdDigest(NutsId id, URL[] urls) {
        return PrivateNutsUtilDigest.getURLDigest(PrivateNutsUtilClassLoader.findClassLoaderJar(id, urls), null);
    }

    public static URL findClassLoaderJar(NutsId id, URL[] urls) {
        return PrivateNutsUtilClassLoader.findClassLoaderJar(id, urls);
    }

    public static <T extends NutsEnum> void checkNonNullEnum(T objectValue, String stringValue, Class<T> enumType, NutsSession session) {
        if (objectValue == null) {
            if (!NutsBlankable.isBlank(stringValue)) {
                if (session == null) {
                    throw new NutsBootException(NutsMessage.cstyle("invalid value %s of type %s", stringValue, enumType.getName()));
                }
                throw new NutsParseEnumException(session, stringValue, NutsCommandLineFormatStrategy.class);
            }
        }
    }

    public static Integer parseInt(String value, Integer emptyValue, Integer errorValue) {
        return PrivateNutsUtilStrings.parseInt(value, emptyValue, errorValue);
    }

    public static Integer parseInt16(String value, Integer emptyValue, Integer errorValue) {
        return PrivateNutsUtilStrings.parseInt16(value, emptyValue, errorValue);
    }

    public static NutsOptional<Integer> parseFileSizeInBytes(String value, Integer defaultMultiplier) {
        return PrivateNutsUtilStrings.parseFileSizeInBytes(value, defaultMultiplier);
    }

    @SuppressWarnings("unchecked")
    public static <T> T createSessionCachedType(String name, Class<T> t, NutsSession session, Supplier<T> sup) {
        checkSession(session);
        name = NutsUtilStrings.trim(name);
        if (NutsBlankable.isBlank(name)) {
            name = "default";
        }
        String key = t.getName() + "(" + name + "@" + System.identityHashCode(session) + ")";
        Object v = session.getProperty(key);
        if (v != null && t.isInstance(v)) {
            return (T) v;
        }
        v = sup.get();
        session.setProperty(key, v);
        return (T) v;
    }

    public static <T> T createSessionCachedType(Class<T> t, NutsSession session, Supplier<T> sup) {
        return createSessionCachedType("default", t, session, sup);
    }

    public static <T extends Enum> NutsOptional<T> parse(String value, Class<T> type) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return NutsOptional.ofEmpty(s -> NutsMessage.cstyle(type.getSimpleName() + " is empty"));
        }
        try {
            return NutsOptional.of((T) Enum.valueOf(type, value.toUpperCase()));
        } catch (Exception notFound) {
            String finalValue = value;
            return NutsOptional.ofError(s -> NutsMessage.cstyle(type.getSimpleName() + " invalid value : %s", finalValue));
        }
    }

    public static <T extends Enum> NutsOptional<T> parse(String value, Class<T> type, Function<String, NutsOptional<T>> mapper) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return NutsOptional.ofEmpty(s -> NutsMessage.cstyle(type.getSimpleName() + " is empty"));
        }
        try {
            NutsOptional<T> o = mapper.apply(value);
            if (o != null) {
                return o;
            }
        } catch (Exception notFound) {
            //ignore
        }
        try {
            return NutsOptional.of((T) Enum.valueOf(type, value.toUpperCase()));
        } catch (Exception notFound) {
            String finalValue = value;
            return NutsOptional.ofError(s -> NutsMessage.cstyle(type.getSimpleName() + " invalid value : %s", finalValue),notFound);
        }
    }
}