/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nsh.cmds.config;

import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.cmds.ConfigCommand;
import net.vpc.common.commandline.CommandLine;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vpc
 */
public class LogConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        if (cmdLine.read("set loglevel", "sll")) {
            NutsWorkspaceConfigManager configManager = context.getWorkspace().getConfigManager();
            if (cmdLine.read("verbose", "finest")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.FINEST);
                }
            } else if (cmdLine.read("fine")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.FINE);
                }
            } else if (cmdLine.read("finer")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.FINER);
                }
            } else if (cmdLine.read("info")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.INFO);
                }
            } else if (cmdLine.read("warning")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.WARNING);
                }
            } else if (cmdLine.read("severe", "error")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.SEVERE);
                }
            } else if (cmdLine.read("config")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.CONFIG);
                }
            } else if (cmdLine.read("off")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.OFF);
                }
            } else if (cmdLine.read("all")) {
                if (cmdLine.isExecMode()) {
                    configManager.setLogLevel(Level.ALL);
                }
            } else {
                if (cmdLine.isExecMode()) {
                    throw new NutsIllegalArgumentException("Invalid loglevel");
                }
            }
            cmdLine.requireEmpty();
            return true;
        } else if (cmdLine.read("get loglevel")) {
            if (cmdLine.isExecMode()) {
                Logger rootLogger = Logger.getLogger("");
                context.getTerminal().getFormattedOut().printf("%s\n", rootLogger.getLevel().toString());
            }
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

}
