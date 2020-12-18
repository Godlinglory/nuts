/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nadmin.subcommands;

import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.*;

/**
 * @author thevpc
 */
public class IndexNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsApplicationContext context) {
        String name = "nadmin update stats";
        NutsArgument a;
        if (cmdLine.next("update stats") != null) {
            List<String> repos = new ArrayList<>();
            while (cmdLine.hasNext()) {
                repos.add(cmdLine.next().getString());
            }
            updateStatistics(context, repos.toArray(new String[0]));
            cmdLine.setCommandName(name).unexpectedArgument();
            return true;
        } else {
            return false;
        }
    }

    private void updateStatistics(NutsApplicationContext context, String[] repos) {
        NutsWorkspaceConfigManager cfg = context.getWorkspace().config();
        NutsSession session = context.getSession();
        NutsUpdateStatisticsCommand cmd = context.getWorkspace().updateStatistics()
                .setSession(session);
        for (String repo : repos) {
            cmd.add(repo);
        }
        cmd.run();
    }
}
