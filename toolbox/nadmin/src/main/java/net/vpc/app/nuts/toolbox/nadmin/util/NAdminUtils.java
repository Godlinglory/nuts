package net.vpc.app.nuts.toolbox.nadmin.util;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsRepository;

import java.io.PrintStream;

public class NAdminUtils {
    public static void showRepo(NutsApplicationContext context, NutsRepository repository, String prefix) {
        boolean enabled = repository.config().isEnabled();
        String disabledString = enabled ? "" : " <DISABLED>";
        PrintStream out = context.getSession().out();
        out.print(prefix);
        if (enabled) {
            out.print("==" + repository.config().getName() + disabledString + "==");
        } else {
            out.print("@@" + repository.config().getName() + disabledString + "@@");
        }
        out.print(" : " + repository.getRepositoryType() + " " + repository.config().getLocation(false));
        out.println();

    }

    public static void showRepoTree(NutsApplicationContext context, NutsRepository repository, String prefix) {
        showRepo(context, repository, prefix);
        String prefix1 = prefix + "  ";
        if (repository.config().isSupportedMirroring()) {
            for (NutsRepository c : repository.config().getMirrors(context.getSession())) {
                showRepoTree(context, c, prefix1);
            }
        }
    }
}
