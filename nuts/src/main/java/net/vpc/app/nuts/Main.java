/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import net.vpc.app.nuts.boot.BootNutsWorkspace;
import net.vpc.app.nuts.util.IOUtils;
import net.vpc.app.nuts.util.LogUtils;
import net.vpc.app.nuts.util.MapStringMapper;
import net.vpc.app.nuts.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/5/17.
 */
public class Main {

    public static void main(String[] args) {
        try {
            long startTime = System.currentTimeMillis();
            int startAppArgs = 0;
            String workspace = null;
            String archetype = null;
            String login = null;
            String password = null;
            String callerVersion = null;
            String logFolder = null;
            Level logLevel = null;
            int logSize = 0;
            int logCount = 0;
            boolean save = true;
            boolean version = false;
            boolean doupdate = false;
            boolean checkupdates = false;
            boolean perf = false;
            boolean showHelp = false;
            List<String> showError = new ArrayList<>();
            boolean which = false;
            Set<String> excludedExtensions = new HashSet<>();
            Set<String> excludedRepositories = new HashSet<>();
            NutsSession session = new NutsSession();


            for (int i = 0; i < args.length; i++) {
                String a = args[i];
                if (a.startsWith("-")) {
                    switch (a) {
                        case "--workspace":
                            i++;
                            if (i >= args.length) {
                                throw new IllegalArgumentException("Missing argument for workspace");
                            }
                            workspace = args[i];
                            break;
                        case "--archetype":
                            i++;
                            if (i >= args.length) {
                                throw new IllegalArgumentException("Missing argument for archetype");
                            }
                            archetype = args[i];
                            break;
                        case "--login":
                            i++;
                            if (i >= args.length) {
                                throw new IllegalArgumentException("Missing argument for login ");
                            }
                            login = args[i];
                            break;
                        case "--password":
                            i++;
                            if (i >= args.length) {
                                throw new IllegalArgumentException("Missing argument for password");
                            }
                            password = args[i];
                            break;
                        case "--private-caller-version":
                            i++;
                            if (i >= args.length) {
                                throw new IllegalArgumentException("Missing argument for private-caller-version");
                            }
                            callerVersion = args[i];
                            break;
                        case "--save":
                            save = true;
                            break;
                        case "--nosave":
                            save = false;
                            break;
                        case "--version":
                            version = true;
                            break;
                        case "--update":
                            doupdate = true;
                            break;
                        case "--check-updates":
                            checkupdates = true;
                            break;
                        case "--verbose":
                            logLevel = Level.FINEST;
                            break;
                        case "--info":
                            logLevel = Level.INFO;
                            break;
                        case "--log-finest":
                            logLevel = Level.FINEST;
                            break;
                        case "--log-fine":
                            logLevel = Level.FINE;
                            break;
                        case "--log-info":
                            logLevel = Level.INFO;
                            break;
                        case "--log-all":
                            logLevel = Level.ALL;
                            break;
                        case "--log-off":
                            logLevel = Level.OFF;
                            break;
                        case "--log-severe":
                            logLevel = Level.SEVERE;
                            break;
                        case "--log-finer":
                            logLevel = Level.FINER;
                            break;
                        case "--log-size":
                            i++;
                            if (i >= args.length) {
                                throw new IllegalArgumentException("Missing argument for log-size");
                            }
                            logSize = Integer.parseInt(args[i]);
                            break;
                        case "--log-count":
                            i++;
                            if (i >= args.length) {
                                throw new IllegalArgumentException("Missing argument for log-count");
                            }
                            logCount = Integer.parseInt(args[i]);
                            break;
                        case "--exclude-extensions":
                            i++;
                            if (i >= args.length) {
                                throw new IllegalArgumentException("Missing argument for exclude-extensions");
                            }
                            excludedExtensions.addAll(StringUtils.split(args[i], " ,;"));
                            break;
                        case "--exclude-repositories":
                            i++;
                            if (i >= args.length) {
                                throw new IllegalArgumentException("Missing argument for exclude-repositories");
                            }
                            excludedRepositories.addAll(StringUtils.split(args[i], " ,;"));
                            break;
                        case "--which":
                            which = true;
                            break;
                        case "--help": {
                            showHelp = true;
                            break;
                        }
                        case "--perf": {
                            perf = true;
                            break;
                        }
                        default: {
                            showError.add("nuts: invalid option [["+a+"]]");
                            break;
                        }
                    }
                    startAppArgs = i + 1;
                } else {
                    break;
                }
            }
            NutsWorkspace bws = openBootstrapWorkspace();
            NutsTerminal bootTerminal = null;
            if(!showError.isEmpty()) {
                for (String err : showError) {
                    if (bootTerminal == null) {
                        bootTerminal = bws.createTerminal(null, null, null);
                    }
                    bootTerminal.getErr().drawln(err);
                }
                bootTerminal.getErr().drawln("Try 'nuts --help' for more information.");
                return;
            }
            boolean someProcessing = false;
            if (which) {
                if(bootTerminal==null){
                    bootTerminal=bws.createTerminal(null, null, null);
                }
                perf = showPerf(startTime, perf, session);
                Map<String, String> runtimeProperties = getRuntimeProperties(bws, session);
                bootTerminal.getOut().drawln("boot-version         : [[" + runtimeProperties.get("nuts.boot.version") + "]]");
                bootTerminal.getOut().drawln("boot-location        : [[" + runtimeProperties.get("nuts.boot.workspace") + "]]");
                bootTerminal.getOut().drawln("boot-api             : [[" + runtimeProperties.get("nuts.boot.api-component") + "]]");
                bootTerminal.getOut().drawln("boot-core            : [[" + runtimeProperties.get("nuts.boot.core-component") + "]]");
                bootTerminal.getOut().drawln("target-workspace     : [[" + BootNutsWorkspace.resolveImmediateWorkspacePath(workspace, NutsConstants.DEFAULT_WORKSPACE_NAME) + "]]");
                bootTerminal.getOut().drawln("boot-java-version    : [[" + System.getProperty("java.version") + "]]");
                bootTerminal.getOut().drawln("boot-java-executable : [[" + System.getProperty("java.home") + "/bin/java" + "]]");
                someProcessing = true;
            }

            if (showHelp) {
                NutsTerminal nutsTerminal = bws.createTerminal(null, null, null);
                perf = showPerf(startTime, perf, session);
                help(nutsTerminal.getOut());
                someProcessing = true;
            }


            String[] args2 = new String[args.length - startAppArgs];
            System.arraycopy(args, startAppArgs, args2, 0, args2.length);
            LogUtils.prepare(logLevel, logFolder, logSize, logCount);
            NutsWorkspace ws = bws.openWorkspace(workspace, new NutsWorkspaceCreateOptions()
                            .setArchetype(archetype)
                            .setCreateIfNotFound(true)
                            .setSaveIfCreated(save)
                            .setExcludedRepositories(excludedRepositories)
                            .setExcludedExtensions(excludedExtensions),
                    session
            );
            if (login != null && login.trim().length() > 0) {
                if (StringUtils.isEmpty(password)) {
                    password = session.getTerminal().readPassword("Password : ");
                }
                ws.login(login, password);
            }
            if (checkupdates) {
                someProcessing = true;
                NutsUpdate[] updates = ws.checkWorkspaceUpdates(session);
                if (updates.length == 0) {
                    session.getTerminal().getOut().drawln("Workspace is [[up-to-date]]");
                } else {
                    session.getTerminal().getOut().drawln("Workspace has " + updates.length + " component" + (updates.length > 1 ? "s" : "") + " to update");
                    for (NutsUpdate update : updates) {
                        session.getTerminal().getOut().drawln(update.getBaseId() + "  : " + update.getLocalId() + " => [[" + update.getAvailableId() + "]]");
                    }
                    perf = showPerf(startTime, perf, session);
                }
            }


            NutsFile bestVersion = null;

            if (doupdate) {
                someProcessing = true;
                try {
                    bestVersion = ws.updateWorkspace(session);
                } catch (Exception ex) {
                    //not found
                }
                if (bestVersion != null) {
                    List<String> all = new ArrayList<>();
                    all.add(System.getProperty("java.home") + "/bin/java");
                    all.add("-jar");
                    all.add(bestVersion.getFile().getPath());
                    all.add("--private-caller-version");
                    all.add(StringUtils.isEmpty(callerVersion) ? ws.getWorkspaceVersion() : (callerVersion + "/" + ws.getWorkspaceVersion()));
                    all.addAll(Arrays.asList(args));
                    ProcessBuilder pb=new ProcessBuilder();
                    pb.command(all);
                    pb.inheritIO();
                    Process process = pb.start();
                    int x = process.waitFor();
                    perf = showPerf(startTime, perf, session);
                } else {
                    if (!checkupdates) {
                        session.getTerminal().getOut().drawln("Workspace is [[up-to-date]]");
                    }
                }
            }

            if (version) {
                String fullVersion = getBootVersion() + " -> " + (StringUtils.isEmpty(callerVersion) ? ws.getWorkspaceVersion() : (callerVersion + "/" + ws.getWorkspaceVersion()));
                session.getTerminal().getOut().println(fullVersion);
                perf = showPerf(startTime, perf, session);
                someProcessing = true;
            }

            if (someProcessing && args2.length == 0) {
                return;
            }
            if (args2.length == 0) {
                perf = showPerf(startTime, perf, session);
                help(session.getTerminal().getOut());
                return;
            }
            NutsCommandLineConsoleComponent commandLine = null;
            try {
                commandLine = ws.createCommandLineConsole(session);
            } catch (NutsExtensionMissingException ex) {
                perf = showPerf(startTime, perf, session);
                session.getTerminal().getErr().println("Unable to create Console. Make sure nuts-core is installed properly.");
                return;
            }
            perf = showPerf(startTime, perf, session);
            commandLine.run(args2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean showPerf(long startTime, boolean perf, NutsSession session) {
        if (perf) {
            session.getTerminal().getOut().drawln("Nuts loaded in [[" + (System.currentTimeMillis() - startTime) + "]] ms");
        }
        return false;
    }

    public static String getBootVersion() {
        return
                IOUtils.loadProperties(Main.class.getResource("/META-INF/nuts-version.properties"))
                        .getProperty("project.version", "0.0.0");
    }

    public static NutsWorkspace openBootstrapWorkspace() throws IOException {
        NutsWorkspace w = new BootNutsWorkspace();
        w.initializeWorkspace(null, null, null, null, new NutsSession());
        return w;
    }

    public static void help(NutsPrintStream term) {
        String help = getHelpString();
        term.drawln(help);
    }

    public static Map<String, String> getRuntimeProperties(NutsWorkspace boot, NutsSession session) {
        Map<String, String> map = new HashMap<>();
        String cp_nutsFile = "<unknown>";
        String cp_nutsCoreFile = "<unknown>";
        String cp = System.getProperty("java.class.path");
        if (cp != null) {
            String[] splits = cp.split(System.getProperty("path.separator"));
            for (String split : splits) {
                String uniformPath = split.replace('\\', '/');
                if (uniformPath.matches("(.*/)?nuts-\\d.*\\.jar")) {
                    cp_nutsFile = split;
                } else if (uniformPath.matches("(.*/)?nuts-core-\\d.*\\.jar")) {
                    cp_nutsCoreFile = split;
                } else if (uniformPath.endsWith("/nuts/target/classes")) {
                    cp_nutsFile = split;
                } else if (uniformPath.endsWith("/nuts-core/target/classes")) {
                    cp_nutsCoreFile = split;
                }
            }
        }
        ClassLoader classLoader = Main.class.getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                String split = url.toString();
                String uniformPath = split.replace('\\', '/');
                if (uniformPath.matches("(.*/)?nuts-\\d.*\\.jar")) {
                    cp_nutsFile = split;
                } else if (uniformPath.matches("(.*/)?nuts-core-\\d.*\\.jar")) {
                    cp_nutsCoreFile = split;
                } else if (uniformPath.endsWith("/nuts/target/classes")) {
                    cp_nutsFile = split;
                } else if (uniformPath.endsWith("/nuts-core/target/classes")) {
                    cp_nutsCoreFile = split;
                }
            }
        }
        NutsFile core = null;
        try {
            core = boot.fetch(NutsConstants.NUTS_COMPONENT_CORE_ID, false, session.copy().setFetchMode(FetchMode.OFFLINE));
        } catch (Exception e) {
            //ignore
        }
        if (cp_nutsCoreFile.equals("<unknown>")) {
            if (core == null) {
                cp_nutsCoreFile = "not found, will be downloaded on need";
            } else {
                cp_nutsCoreFile = core.getFile().getPath();
            }
        }
        map.put("nuts.boot.version", getBootVersion());
        map.put("nuts.boot.api-component", cp_nutsFile);
        map.put("nuts.boot.core-component", cp_nutsCoreFile);
        map.put("nuts.boot.workspace", boot.getWorkspaceLocation());
        return map;
    }

    public static String getHelpString() {
        String help = null;
        try {
            InputStream s = null;
            try {
                s = Main.class.getResourceAsStream("/net/vpc/app/nuts/help.help");
                if (s != null) {
                    help = IOUtils.readStreamAsString(s, true);
                }
            } finally {
                if (s != null) {
                    s.close();
                }
            }
        } catch (IOException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Unable to load main help", e);
        }
        if (help == null) {
            help = "no help found";
        }
        HashMap<String, String> props = new HashMap<>();
        props.putAll((Map) System.getProperties());
        props.put("nuts.boot-version", getBootVersion());
        help = StringUtils.replaceVars(help, new MapStringMapper(props));
        return help;
    }

}
