package net.thevpc.nuts.toolbox.tomcat;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.tomcat.remote.RemoteTomcat;
import net.thevpc.nuts.toolbox.tomcat.local.LocalTomcat;
import net.thevpc.nuts.toolbox.tomcat.util.ApacheTomcatRepositoryModel;

public class NTomcatMain implements NutsApplication {

    public static void main(String[] args) {
        new NTomcatMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        NutsRepository apacheRepo = appContext.getWorkspace().repos().findRepository("apache-tomcat");
        if (apacheRepo == null) {
            appContext.getWorkspace().repos().addRepository(
                    new NutsAddRepositoryOptions()
                            .setRepositoryModel(new ApacheTomcatRepositoryModel())
                            .setTemporary(true)
                            
            );
        }
        NutsCommandLine cmdLine = appContext.getCommandLine();
        Boolean local = null;
        boolean skipFirst = false;
        if (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.peek();
            if ((a.getString().equals   ("--remote") || a.getString().equals("-r"))) {
                cmdLine.skip();
                local = false;
            } else if ((a.getString().equals("--local") || a.getString().equals("-l"))) {
                cmdLine.skip();
                local = true;
            }
        }
        if (local == null) {
            local = true;
        }
        if (local) {
            LocalTomcat m = new LocalTomcat(appContext, cmdLine);
            m.runArgs();
            appContext.getSession().flush();
        } else {
            RemoteTomcat m = new RemoteTomcat(appContext, cmdLine);
            m.runArgs();
            appContext.getSession().flush();
        }
    }

}
