package net.thevpc.nuts.tutorial.cli;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineProcessor;

/**
 * Event Based Command line processing
 * @author vpc
 */
public class CustomCliA implements NApplication {

    public static void main(String[] args) {
        new CustomCliA().runAndExit(args);
    }

    @Override
    public void run(NApplicationContext nac) {
        nac.processCommandLine(new NCmdLineProcessor() {
            boolean noMoreOptions = false;
            boolean clean = false;
            List<String> params = new ArrayList<>();

            @Override
            public boolean onCmdNextOption(NArg option, NCmdLine commandLine, NCmdLineContext context) {
                if (!noMoreOptions) {
                    return false;
                }
                switch (option.key()) {
                    case "-c":
                    case "--clean": {
                        NArg a = commandLine.nextFlag().get();
                        if (a.isEnabled()) {
                            clean = a.getBooleanValue().get();
                        }
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean onCmdNextNonOption(NArg nonOption, NCmdLine commandLine, NCmdLineContext context) {
                params.add(commandLine.next().get().toString());
                return true;
            }

            @Override
            public void onCmdExec(NCmdLine commandLine, NCmdLineContext context) {
                if (clean) {
                    commandLine.getSession().out().println("cleaned!");
                }
            }
        });
    }

}
