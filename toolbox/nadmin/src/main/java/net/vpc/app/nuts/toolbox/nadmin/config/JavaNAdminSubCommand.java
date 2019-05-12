/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.NutsSdkLocation;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsTableFormat;

/**
 * @author vpc
 */
public class JavaNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        if (autoSave == null) {
            autoSave = false;
        }
        NutsWorkspace ws = context.getWorkspace();
        PrintStream out = context.getTerminal().fout();
        NutsWorkspaceConfigManager conf = ws.config();
        if (cmdLine.readAll("add java")) {
            if (cmdLine.readAll("--search")) {
                List<String> extraLocations = new ArrayList<>();
                while (cmdLine.hasNext()) {
                    extraLocations.add(cmdLine.read().getString());
                }
                if (extraLocations.isEmpty()) {
                    for (NutsSdkLocation loc : conf.searchSdkLocations("java", out)) {
                        conf.addSdk(loc,null);
                    }
                } else {
                    for (String extraLocation : extraLocations) {
                        for (NutsSdkLocation loc : conf.searchSdkLocations("java", ws.io().path(extraLocation), out)) {
                            conf.addSdk(loc,null);
                        }
                    }
                }
                cmdLine.setCommandName("config java").unexpectedArgument();
                if (autoSave) {
                    conf.save(false);
                }
            } else {
                while (cmdLine.hasNext()) {
                    NutsSdkLocation loc = conf.resolveSdkLocation("java", ws.io().path(cmdLine.read().getString()));
                    if (loc != null) {
                        conf.addSdk(loc,null);
                    }
                }
                if (autoSave) {
                    conf.save(false);
                }
            }
            return true;
        } else if (cmdLine.readAll("remove java")) {
            while (cmdLine.hasNext()) {
                String name = cmdLine.read().getString();
                NutsSdkLocation loc = conf.findSdkByName("java", name);
                if (loc == null) {
                    loc = conf.findSdkByPath("java", ws.io().path(name));
                    if (loc == null) {
                        loc = conf.findSdkByVersion("java", name);
                    }
                }
                if (loc != null) {
                    conf.removeSdk(loc,null);
                }
            }
            if (autoSave) {
                conf.save(false);
            }
            return true;
        } else if (cmdLine.readAll("list java")) {
            NutsTableFormat t = context.getWorkspace().formatter().createTableFormat()
//                    .setBorder(TableFormatter.SPACE_BORDER)
                    .setVisibleHeader(true)
                    .setColumnsConfig("name", "version", "path")
                    .addHeaderCells("==Name==", "==Version==", "==Path==");
            while (cmdLine.hasNext()) {
                if (!t.configure(cmdLine)) {
                    cmdLine.setCommandName("config list java").unexpectedArgument();
                }
            }
            if (cmdLine.isExecMode()) {

                NutsSdkLocation[] sdks = conf.getSdks("java");
                Arrays.sort(sdks, new Comparator<NutsSdkLocation>() {
                    @Override
                    public int compare(NutsSdkLocation o1, NutsSdkLocation o2) {
                        int x = o1.getName().compareTo(o2.getName());
                        if (x != 0) {
                            return x;
                        }
                        x = o1.getVersion().compareTo(o2.getVersion());
                        if (x != 0) {
                            return x;
                        }
                        x = o1.getPath().compareTo(o2.getPath());
                        if (x != 0) {
                            return x;
                        }
                        return x;
                    }
                });
                for (NutsSdkLocation jloc : sdks) {
                    t.addRow(jloc.getName(), jloc.getVersion(), jloc.getPath());
                }
                out.printf(t.toString());
            }
            return true;
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

}
