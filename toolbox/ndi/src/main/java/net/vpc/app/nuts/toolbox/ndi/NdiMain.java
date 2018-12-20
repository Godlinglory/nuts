package net.vpc.app.nuts.toolbox.ndi;

import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;

import java.io.IOException;

public class NdiMain extends NutsApplication {
    public static void main(String[] args) {
        new NdiMain().launch(args);
    }

    @Override
    public int launch(NutsApplicationContext appContext) {
        CommandLine cmd=new CommandLine(appContext);
        cmd.requireNonEmpty();
        Argument a;
        if (cmd.hasNext()) {
            if ((a=cmd.readNonOption("in","install"))!=null) {
                LinuxNdi ndi = null;
                if (appContext.getWorkspace().getPlatformOs().getName().equals("linux")) {
                    ndi = new LinuxNdi(appContext);
                }
                if (ndi == null) {
                    throw new IllegalArgumentException("Platform not supported : " + appContext.getWorkspace().getPlatformOs());
                }
                boolean force = false;
                boolean forceAll = false;
                boolean fetch = false;
                while (cmd.hasNext()){
                    if ((a=cmd.readBooleanOption("-f","--force"))!=null) {
                        force = a.getBooleanValue();
                    }else if ((a=cmd.readBooleanOption("-F","--force-all"))!=null) {
                            forceAll = a.getBooleanValue();
                            if(forceAll && !force){
                                force=true;
                            }
                    }else if ((a=cmd.readBooleanOption("-t","--fetch"))!=null) {
                            fetch = a.getBooleanValue();
                    } else if(cmd.isOption()){
                        cmd.unexpectedArgument("ndi");
                    } else {
                        try {
                            ndi.createNutsScript(cmd.read().getString(), force, forceAll, fetch);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return 0;
        } else {
            appContext.err().printf("Missing arguments\n");
            return 1;
        }
    }
}
