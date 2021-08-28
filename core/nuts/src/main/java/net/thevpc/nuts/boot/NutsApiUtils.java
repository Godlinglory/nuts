package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.io.PrintStream;
import java.net.URL;

public class NutsApiUtils {

    private NutsApiUtils() {
    }

    public static void checkSession(NutsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("missing session");
        }
    }

    public static String[] parseCommandLineArray(String commandLineString) {
        return PrivateNutsCommandLine.parseCommandLineArray(commandLineString);
    }

    public static int processThrowable(Throwable ex, PrintStream out) {
        return PrivateNutsApplicationUtils.processThrowable(ex, out);
    }

    public static int processThrowable(Throwable ex, PrintStream out, boolean showMessage, boolean showTrace, boolean showGui) {
        return PrivateNutsApplicationUtils.processThrowable(ex, out, showMessage, showTrace, showGui);
    }

    public static boolean isGraphicalDesktopEnvironment() {
        return PrivateNutsGui.isGraphicalDesktopEnvironment();
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return PrivateNutsUtils.getSysBoolNutsProperty(property, defaultValue);
    }

    public static String resolveNutsVersionFromClassPath() {
        return PrivateNutsMavenUtils.resolveNutsApiVersionFromClassPath();
    }

    public static String resolveNutsIdHash() {
        return resolveNutsIdHash(
                new NutsBootId("net.thevpc.nuts", "nuts", NutsBootVersion.parse(Nuts.getVersion())),
                PrivateNutsClasspathUtils.resolveClasspathURLs(Thread.currentThread().getContextClassLoader())
        );
    }

    public static String resolveNutsIdHash(NutsBootId id, URL[] urls) {
        return PrivateNutsIOUtils.getURLDigest(
                PrivateNutsClasspathUtils.findClassLoaderJar(id, urls)
        );
    }

    public static URL findClassLoaderJar(NutsBootId id, URL[] urls) {
        return PrivateNutsClasspathUtils.findClassLoaderJar(id, urls);
    }


    public static NutsWorkspaceOptionsBuilder createOptionsBuilder() {
        return new PrivateBootWorkspaceOptions();
    }

    public static NutsWorkspaceOptions createOptions() {
        return new PrivateBootWorkspaceOptions();
    }

    /**
     * creates a string key combining layout and location.
     * le key has the form of a concatenated layout and location ids separated by ':'
     * where null layout is replaced by 'system' keyword.
     * used in {@link NutsWorkspaceOptions#getHomeLocations()}.
     *
     * @param storeLocationLayout layout
     * @param location            location
     * @return combination of layout and location separated by ':'.
     */
    public static String createHomeLocationKey(NutsOsFamily storeLocationLayout, NutsStoreLocation location) {
        return (storeLocationLayout == null ? "system" : storeLocationLayout.id()) + ":" + (location == null ? "system" : location.id());
    }

}