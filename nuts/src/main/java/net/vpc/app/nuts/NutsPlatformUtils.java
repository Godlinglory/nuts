/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.util.Locale;

/**
 *
 * @author vpc
 */
public class NutsPlatformUtils {

    public static NutsOsFamily getPlatformOsFamily() {
        String property = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (property.startsWith("linux")) {
            return NutsOsFamily.LINUX;
        }
        if (property.startsWith("win")) {
            return NutsOsFamily.WINDOWS;
        }
        if (property.startsWith("mac")) {
            return NutsOsFamily.MACOS;
        }
        if (property.startsWith("sunos")) {
            return NutsOsFamily.UNIX;
        }
        if (property.startsWith("freebsd")) {
            return NutsOsFamily.UNIX;
        }
        return NutsOsFamily.UNKNOWN;
    }

    public static String getPlatformOsHome(NutsStoreLocation location) {
        switch (location) {
            case PROGRAMS: {
                switch (getPlatformOsFamily()) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN:{
                        return "/opt/nuts/programs";
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (NutsUtils.isEmpty(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts";
                    }
                }
                break;
            }
            case LIB: {
                switch (getPlatformOsFamily()) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/opt/nuts/lib";
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (NutsUtils.isEmpty(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts";
                    }
                }
                break;
            }
            case CONFIG: {
                switch (getPlatformOsFamily()) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/etc/opt/nuts";
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (NutsUtils.isEmpty(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts";
                    }
                }
                break;
            }
            case LOGS: {
                switch (getPlatformOsFamily()) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/var/log/nuts";
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (NutsUtils.isEmpty(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts";
                    }
                }
                break;
            }
            case CACHE: {
                switch (getPlatformOsFamily()) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/var/cache/nuts";
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (NutsUtils.isEmpty(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts";
                    }
                }
                break;
            }
            case VAR: {
                switch (getPlatformOsFamily()) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/var/opt/nuts";
                    }
                    case WINDOWS: {
                        String pf = System.getenv("ProgramFiles");
                        if (NutsUtils.isEmpty(pf)) {
                            pf = "C:\\Program Files";
                        }
                        return pf + "\\nuts";
                    }
                }
                break;
            }
            case TEMP: {
                switch (getPlatformOsFamily()) {
                    case LINUX:
                    case MACOS:
                    case UNIX:
                    case UNKNOWN: {
                        return "/tmp/nuts/global";
                    }
                    case WINDOWS: {
                        String pf = System.getenv("TMP");
                        if (NutsUtils.isEmpty(pf)) {
                            pf = "C:\\windows\\TEMP";
                        }
                        return pf + "\\nuts";
                    }
                }
                break;
            }
        }
        throw new UnsupportedOperationException();
    }

    /**
     * resolves nuts home folder.Home folder is the root for nuts folders.It
     * depends on folder type and store layout. For instance log folder depends
     * on on the underlying operating system (linux,windows,...).
     *
     * @param folderType folder type to resolve home for
     * @param storeLocationLayout location layout to resolve home for
     * @param homeLocations
     * @param global
     * @return home folder path
     */
    public static String resolveHomeFolder(NutsStoreLocationLayout storeLocationLayout, NutsStoreLocation folderType, String[] homeLocations, boolean global) {
        if (folderType == null) {
            folderType = NutsStoreLocation.CONFIG;
        }
        boolean wasSystem = false;
        if (storeLocationLayout == null || storeLocationLayout == NutsStoreLocationLayout.SYSTEM) {
            wasSystem = true;
            if ("windows".equals(NutsPlatformUtils.getPlatformOsFamily())) {
                storeLocationLayout = NutsStoreLocationLayout.WINDOWS;
            } else {
                storeLocationLayout = NutsStoreLocationLayout.LINUX;
            }
        }
        String s;
        s = System.getProperty("nuts.export.home." + folderType.name().toLowerCase() + "." + storeLocationLayout.name().toLowerCase());
        if (s != null && s.trim().length() > 0) {
            return s.trim();
        }
        s = homeLocations[storeLocationLayout.ordinal() * NutsStoreLocation.values().length + folderType.ordinal()];
        if (s != null && s.trim().length() > 0) {
            return s.trim();
        }
        s = homeLocations[NutsStoreLocationLayout.SYSTEM.ordinal() * NutsStoreLocation.values().length + folderType.ordinal()];
        if (s != null && s.trim().length() > 0) {
            return s.trim();
        }
        if (global) {
            return getPlatformOsHome(folderType);
        } else {
            switch (folderType) {
                case LOGS:
                case VAR:
                case CONFIG:
                case PROGRAMS:
                case LIB: {
                    switch (storeLocationLayout) {
                        case WINDOWS: {
                            return System.getProperty("user.home") + NutsUtils.syspath("/AppData/Roaming/nuts");
                        }
                        case LINUX:
                            return System.getProperty("user.home") + NutsUtils.syspath("/.nuts");
                    }
                    break;
                }
                case CACHE: {
                    switch (storeLocationLayout) {
                        case WINDOWS:
                            return System.getProperty("user.home") + NutsUtils.syspath("/AppData/Local/nuts");
                        case LINUX:
                            return System.getProperty("user.home") + NutsUtils.syspath("/.cache/nuts");
                    }
                    break;
                }
                case TEMP: {
                    switch (storeLocationLayout) {
                        case WINDOWS:
                            if (NutsPlatformUtils.getPlatformOsFamily().equals("windows")) {
                                //on windows temp folder is user defined
                                return System.getProperty("java.io.tmpdir") + NutsUtils.syspath("/nuts");
                            } else {
                                return System.getProperty("user.home") + NutsUtils.syspath("/AppData/Local/nuts");
                            }
                        case LINUX:
                            if (NutsPlatformUtils.getPlatformOsFamily().equals("linux")) {
                                //on linux temp folder is shared. will add user folder as discriminator
                                return System.getProperty("java.io.tmpdir") + NutsUtils.syspath(("/" + System.getProperty("user.name") + "/nuts"));
                            } else {
                                return System.getProperty("user.home") + NutsUtils.syspath("/tmp/nuts");
                            }
                    }
                }
            }
        }
        throw new NutsIllegalArgumentException("Unsupported " + storeLocationLayout);
    }

}