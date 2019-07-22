/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsComponent;
import net.vpc.app.nuts.NutsCommandLine;

/**
 *
 * @author vpc
 */
public interface NAdminSubCommand extends NutsComponent<Object> {

    /**
     * execute command and return true.
     * If the command is not supported return false.
     *
     * @param cmdLine  command line
     * @param autoSave auto save
     * @param context application context
     * @return true if the sub command is supported and executed
     */
    boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsApplicationContext context);
}
