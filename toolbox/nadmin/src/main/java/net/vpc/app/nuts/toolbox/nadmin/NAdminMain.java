package net.vpc.app.nuts.toolbox.nadmin;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsApplication;

import java.io.PrintStream;
import java.util.*;

public class NAdminMain extends NutsApplication {

    private List<NAdminSubCommand> subCommands;

    public static void main(String[] args) {
        new NAdminMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext context) {
        context.getWorkspace().extensions().discoverTypes(Thread.currentThread().getContextClassLoader());
        if (subCommands == null) {
            subCommands = new ArrayList<>(
                    context.getWorkspace().extensions().createAllSupported(NAdminSubCommand.class, new NutsDefaultSupportLevelContext<>(context.getWorkspace(),this))
            );
        }
        Boolean autoSave = true;
        NutsCommandLine cmdLine = context.commandLine();
        boolean empty = true;
        NutsArgument a;
        do {
            if (context.configureFirst(cmdLine)) {
                //
            } else {
                NAdminSubCommand selectedSubCommand = null;
                for (NAdminSubCommand subCommand : subCommands) {
                    if (subCommand.exec(cmdLine, autoSave, context)) {
                        selectedSubCommand = subCommand;
                        empty = false;
                        break;
                    }
                }
                if (selectedSubCommand != null) {
                    continue;
                }

                if (!cmdLine.isExecMode()) {
                    return;
                }
                if (cmdLine.hasNext()) {
                    PrintStream out = context.session().err();
                    out.printf("Unexpected %s%n", cmdLine.peek());
                    out.printf("type for more help : config -h%n");
                    throw new NutsExecutionException(context.getWorkspace(), "Unexpected " + cmdLine.peek(), 1);
                }
                break;
            }
        } while (cmdLine.hasNext());
        if (empty) {
            PrintStream out = context.session().err();
            out.printf("Missing config command%n");
            out.printf("type for more help : config -h%n");
            throw new NutsExecutionException(context.getWorkspace(), "Missing config command", 1);
        }
    }

}
