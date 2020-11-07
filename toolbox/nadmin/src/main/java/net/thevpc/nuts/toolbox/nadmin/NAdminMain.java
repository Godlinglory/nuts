package net.thevpc.nuts.toolbox.nadmin;

import net.thevpc.nuts.*;

import java.io.PrintStream;
import java.util.*;

public class NAdminMain extends NutsApplication {

    private List<NAdminSubCommand> subCommands;

    public static void main(String[] args) {
        new NAdminMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext context) {
        context.getWorkspace().extensions().discoverTypes(Thread.currentThread().getContextClassLoader(), context.getSession());
        if (subCommands == null) {
            subCommands = new ArrayList<>(
                    context.getWorkspace().extensions().createAllSupported(NAdminSubCommand.class, this, context.getSession())
            );
        }
        Boolean autoSave = true;
        NutsCommandLine cmdLine = context.getCommandLine();
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
                    PrintStream out = context.getSession().err();
                    out.printf("Unexpected %s%n", cmdLine.peek());
                    out.printf("type for more help : nadmin -h%n");
                    throw new NutsExecutionException(context.getWorkspace(), "Unexpected " + cmdLine.peek(), 1);
                }
                break;
            }
        } while (cmdLine.hasNext());
        if (empty) {
            PrintStream out = context.getSession().err();
            out.printf("Missing nadmin command%n");
            out.printf("type for more help : nadmin -h%n");
            throw new NutsExecutionException(context.getWorkspace(), "Missing nadmin command", 1);
        }
    }

    @Override
    protected void onInstallApplication(NutsApplicationContext applicationContext) {
        NutsWorkspace ws = applicationContext.getWorkspace();
        if(applicationContext.getSession().isPlainTrace()){
            applicationContext.getSession().out().println("looking for java installations in default locations...");
        }
        NutsSdkLocation[] found = ws.sdks().searchSystem("java", applicationContext.getSession().copy().setTrace(false));
        int someAdded=0;
        for (NutsSdkLocation java : found) {
            if(ws.sdks().add(java,new NutsAddOptions().setSession(applicationContext.getSession()))){
                someAdded++;
            }
        }
        if(applicationContext.getSession().isPlainTrace()) {
            if(someAdded==0){
                applicationContext.getSession().out().print("@@no new@@ java installation locations found...\n");
            }else if(someAdded==1){
                applicationContext.getSession().out().print("**1** new java installation location added...\n");
            }else {
                applicationContext.getSession().out().printf("**%s** new java installation locations added...\n", someAdded);
            }
            applicationContext.getSession().out().println("you can always manually add another installation manually using 'nadmin add java' command.");
        }
        if(!ws.config().isReadOnly()) {
            ws.config().save(applicationContext.getSession());
        }
    }

    @Override
    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
        NutsWorkspace ws = applicationContext.getWorkspace();
        for (NutsSdkLocation java : ws.sdks().searchSystem("java", applicationContext.getSession())) {
            ws.sdks().add(java,new NutsAddOptions().setSession(applicationContext.getSession()));
        }
        ws.config().save(applicationContext.getSession());
    }
}
