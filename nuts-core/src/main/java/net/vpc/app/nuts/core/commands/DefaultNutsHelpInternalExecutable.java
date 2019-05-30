/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.commands;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.fprint.FormattedPrintStream;

/**
 *
 * @author vpc
 */
public class DefaultNutsHelpInternalExecutable extends DefaultInternalNutsExecutableCommand {

    public DefaultNutsHelpInternalExecutable(String[] args, NutsWorkspace ws, NutsSession session) {
        super("help", args, ws, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        List<String> helpFor = new ArrayList<>();
        NutsCommand cmdLine = ws.parser().parseCommand(args);
        NutsOutputFormat outputFormat = NutsOutputFormat.PLAIN;
        while (cmdLine.hasNext()) {
            NutsOutputFormat of = CoreNutsUtils.readOptionOutputFormat(cmdLine);
            if(of!=null) {
                outputFormat=of;
            }else{
                NutsArgument a = cmdLine.peek();
                if (a.isOption()) {
                    switch (a.getKey().getString()) {
                        default: {
                            throw new NutsIllegalArgumentException(ws, "Unsupported option " + a);
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
                PrintStream fout = getSession(true).getTerminal().fout();
                if (helpFor.isEmpty()) {
                    fout.println(NutsWorkspaceExt.of(ws).getHelpText());
                    fout.flush();
                }
                for (String arg : helpFor) {
                    NutsExecutableInfo w = null;
                    if (arg.equals("help")) {
                        fout.println(arg + " :");
                        showDefaultHelp();
                        fout.flush();
                    } else {
                        try {
                            w = ws.exec().command(arg).which();
                        } catch (Exception ex) {
                            //ignore
                        }
                        if (w != null) {
                            fout.println(arg + " :");
                            FormattedPrintStream t=(FormattedPrintStream)fout;
                            fout.println(w.getHelpText());
                            fout.flush();
                        } else {
                            getSession(true).getTerminal().ferr().println(arg + " : Not found");
                        }
                    }
                }
                break;
            }
            default: {
                throw new NutsUnsupportedOperationException(ws,"Unsupported format " + outputFormat);
            }
        }

    }

}
