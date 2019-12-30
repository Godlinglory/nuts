/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.commands;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.NutsWorkspaceExt;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.NutsLogger;
//import net.vpc.app.nuts.runtime.util.fprint.FormattedPrintStream;
import net.vpc.app.nuts.NutsCommandLine;

/**
 *
 * @author vpc
 */
public class DefaultNutsHelpInternalExecutable extends DefaultInternalNutsExecutableCommand {
    private final NutsLogger LOG;
    public DefaultNutsHelpInternalExecutable(String[] args, NutsSession session) {
        super("help", args, session);
        LOG=session.workspace().log().of(DefaultNutsHelpInternalExecutable.class);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        List<String> helpFor = new ArrayList<>();
        NutsCommandLine cmdLine = getSession().getWorkspace().commandLine().create(args);
        NutsOutputFormat outputFormat = NutsOutputFormat.PLAIN;
        while (cmdLine.hasNext()) {
            NutsOutputFormat of = CoreNutsUtils.readOptionOutputFormat(cmdLine);
            if (of != null) {
                outputFormat = of;
            } else {
                NutsArgument a = cmdLine.peek();
                if (a.isOption()) {
                    switch (a.getStringKey()) {
                        default: {
                            throw new NutsIllegalArgumentException(getSession().getWorkspace(), "Unsupported option " + a);
                        }
                    }
                } else {
                    cmdLine.skip();
                    helpFor.add(a.getString());
                    helpFor.addAll(Arrays.asList(cmdLine.toArray()));
                    cmdLine.skipAll();
                }
            }
        }
        switch (outputFormat) {
            case PLAIN: {
                PrintStream fout = getSession().out();
                if (helpFor.isEmpty()) {
                    fout.println(NutsWorkspaceExt.of(getSession().getWorkspace()).getHelpText());
                    fout.flush();
                }
                for (String arg : helpFor) {
                    NutsExecutableInformation w = null;
                    if (arg.equals("help")) {
                        fout.println(arg + " :");
                        showDefaultHelp();
                        fout.flush();
                    } else {
                        try {
                            w = getSession().getWorkspace().exec().command(arg).which();
                        } catch (Exception ex) {
                            LOG.with().level(Level.FINE).error(ex).log( "Failed to execute : {0}", arg);
                            //ignore
                        }
                        if (w != null) {
                            fout.println(arg + " :");
                            fout.println(w.getHelpText());
                            fout.flush();
                        } else {
                            getSession().getTerminal().err().println(arg + " : Not found");
                        }
                    }
                }
                break;
            }
            default: {
                throw new NutsUnsupportedOperationException(getSession().getWorkspace(), "Unsupported format " + outputFormat);
            }
        }

    }

}
