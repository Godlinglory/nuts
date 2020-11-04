/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.main.commands;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsLicenseInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsLicenseInternalExecutable(String[] args, NutsSession session) {
        super("license", args, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        getSession().out().println(NutsWorkspaceExt.of(getSession().getWorkspace()).getLicenseText());
    }

}
