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
package net.thevpc.nuts.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.DefaultNutsWorkspaceBootOptionsBuilder;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.cmdline.NutsCommandLineFormatStrategy;
import net.thevpc.nuts.reserved.*;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
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
            return ((Collection) any).isEmpty();
        }
        if (any instanceof Map) {
            return ((Map) any).isEmpty();
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

    public static String[] parseCommandLineArray(String commandLineString) {
        return NutsCommandLine.parseDefault(commandLineString).get().toStringArray();
    }

    public static int processThrowable(Throwable ex, PrintStream out) {
        return NutsReservedUtils.processThrowable(ex, out);
    }

    public static int processThrowable(Throwable ex, String[] args) {
        NutsReservedBootLog log = new NutsReservedBootLog(null);
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
        boolean showStackTrace = bo.getDebug().isPresent();
        NutsLogConfig nutsLogConfig = bo.getLogConfig().orElseGet(NutsLogConfig::new);
        showStackTrace |= (nutsLogConfig.getLogTermLevel() != null
                && nutsLogConfig.getLogTermLevel().intValue() < Level.INFO.intValue());
        if (!showStackTrace) {
            showStackTrace = NutsApiUtils.getSysBoolNutsProperty("debug", false);
        }
        if (bot) {
            showStackTrace = false;
        }
        return processThrowable(ex, null, true, showStackTrace, gui);
    }

    public static int processThrowable(Throwable ex, PrintStream out, boolean showMessage, boolean showStackTrace, boolean showGui) {
        return NutsReservedUtils.processThrowable(ex, out, showMessage, showStackTrace, showGui);
    }

    public static boolean isGraphicalDesktopEnvironment() {
        return NutsReservedGuiUtils.isGraphicalDesktopEnvironment();
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return NutsReservedUtils.getSysBoolNutsProperty(property, defaultValue);
    }

    public static String resolveNutsVersionFromClassPath(NutsReservedBootLog bLog) {
        return NutsReservedMavenUtils.resolveNutsApiVersionFromClassPath(bLog);
    }

    public static String resolveNutsIdDigestOrError() {
        String d = resolveNutsIdDigest();
        if (d == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL[] urls = NutsReservedClassLoaderUtils.resolveClasspathURLs(cl, true);
            throw new NutsBootException(NutsMessage.ofPlain("unable to detect nuts digest. Most likely you are missing valid compilation of nuts." + "\n\t 'pom.properties' could not be resolved and hence, we are unable to resolve nuts version." + "\n\t java=" + System.getProperty("java.home") + " as " + System.getProperty("java.version") + "\n\t class-path=" + System.getProperty("java.class.path") + "\n\t urls=" + Arrays.toString(urls) + "\n\t class-loader=" + cl.getClass().getName() + " as " + cl));
        }
        return d;

    }

    public static String resolveNutsIdDigest() {
        //TODO COMMIT TO 0.8.4
        return resolveNutsIdDigest(NutsId.ofApi(Nuts.getVersion()).get(), NutsReservedClassLoaderUtils.resolveClasspathURLs(Nuts.class.getClassLoader(), true));
    }

    public static String resolveNutsIdDigest(NutsId id, URL[] urls) {
        return NutsReservedIOUtils.getURLDigest(NutsReservedClassLoaderUtils.findClassLoaderJar(id, urls), null);
    }

    public static URL findClassLoaderJar(NutsId id, URL[] urls) {
        return NutsReservedClassLoaderUtils.findClassLoaderJar(id, urls);
    }

    public static <T extends NutsEnum> void checkNonNullEnum(T objectValue, String stringValue, Class<T> enumType, NutsSession session) {
        if (objectValue == null) {
            if (!NutsBlankable.isBlank(stringValue)) {
                if (session == null) {
                    throw new NutsBootException(NutsMessage.ofCstyle("invalid value %s of type %s", stringValue, enumType.getName()));
                }
                throw new NutsParseEnumException(session, stringValue, NutsCommandLineFormatStrategy.class);
            }
        }
    }

    public static NutsOptional<Integer> parseFileSizeInBytes(String value, Integer defaultMultiplier) {
        return NutsReservedStringUtils.parseFileSizeInBytes(value, defaultMultiplier);
    }

    @SuppressWarnings("unchecked")


    public static <T> T getOrCreateRefProperty(String name, Class<T> type, NutsSession session, Supplier<T> sup) {
        NutsUtils.requireSession(session);
        name = NutsStringUtils.trim(name);
        if (NutsBlankable.isBlank(name)) {
            name = "default";
        }
        String key = type.getName() + "(" + name + ")";
        return session.getOrComputeRefProperty(key, s->sup.get());
    }

    public static <T> T getOrCreateRefProperty(Class<T> type, NutsSession session, Supplier<T> sup) {
        return getOrCreateRefProperty("default", type, session, sup);
    }

}
