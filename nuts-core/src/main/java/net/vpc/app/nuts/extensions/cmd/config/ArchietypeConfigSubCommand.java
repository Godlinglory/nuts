/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.cmd.config;

import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.extensions.cmd.AbstractConfigSubCommand;
import net.vpc.app.nuts.extensions.cmd.ConfigCommand;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;

/**
 *
 * @author vpc
 */
public class ArchietypeConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CmdLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
       if (cmdLine.read("list archetypes", "la")) {
            if (cmdLine.isExecMode()) {
                for (String archetype : context.getValidWorkspace().getAvailableArchetypes()) {
                    context.getTerminal().getOut().println(archetype);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT;
    }

}
