/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
public class ConfigCommand extends AbstractNutsCommand {

    private List<ConfigSubCommand> subCommands;

    public ConfigCommand() {
        super("config", DEFAULT_SUPPORT);
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) {
        if (subCommands == null) {
            subCommands = new ArrayList<>(
                    context.getWorkspace().getExtensionManager().createAllSupported(ConfigSubCommand.class, this)
            );
        }
        boolean noColors = false;
        Boolean autoSave = null;
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        boolean empty = true;
        Argument a;
        do {
            if (context.configure(cmdLine)) {
                //
            }else if ((a=cmdLine.readOption("--save"))!=null) {
                autoSave = true;
                empty = false;
                continue;
            }else if (cmdLine.readAllOnce("-h", "-?", "--help")) {
                empty = false;
                if (cmdLine.isExecMode()) {
                    PrintStream out = context.out();
                    out.printf("update\n");
                    out.printf("check-updates\n");
                    out.printf("create workspace ...\n");
                    out.printf("set workspace ...\n");
                    out.printf("create repo ...\n");
                    out.printf("add repo ...\n");
                    out.printf("remove repo ...\n");
                    out.printf("list repos ...\n");
                    out.printf("add extension ...\n");
                    out.printf("list extensions ...\n");
                    out.printf("edit repo <repoId> ...\n");
                    out.printf("list imports\n");
                    out.printf("clear imports\n");
                    out.printf("list archetypes\n");
                    out.printf("import\n");
                    out.printf("list imports\n");
                    out.printf("clear imports\n");
                    out.printf("unimport\n");
                    out.printf("list users\n");
                    out.printf("add user\n");
                    out.printf("edit user\n");
                    out.printf("passwd\n");
                    out.printf("secure\n");
                    out.printf("unsecure\n");
                    out.printf("set loglevel verbose|fine|finer|finest|error|severe|config|all|none\n");
                    out.printf("");
                    out.printf("type 'help config' for more detailed help\n");
                }
                continue;
            }else {
                ConfigSubCommand selectedSubCommand = null;
                for (ConfigSubCommand subCommand : subCommands) {
                    if (subCommand.exec(cmdLine, this, autoSave, context)) {
                        selectedSubCommand = subCommand;
                        empty = false;
                        break;
                    }
                }
                if (selectedSubCommand != null) {
                    continue;
                }

                if (!cmdLine.isExecMode()) {
                    return -1;
                }
                if (cmdLine.hasNext()) {
                    PrintStream out = context.err();
                    out.printf("Unexpected %s\n", cmdLine.get());
                    out.printf("type for more help : config -h\n");
                    return 1;
                }
                break;
            }
        } while (cmdLine.hasNext());
        if (empty) {
            PrintStream out = context.err();
            out.printf("Missing config command\n");
            out.printf("type for more help : config -h\n");
            return 1;
        }
        return 0;
    }

    public void showRepo(NutsCommandContext context, NutsRepository repository, String prefix) {
        boolean enabled = repository.isEnabled();
        String disabledString = enabled ? "" : " <DISABLED>";
        PrintStream out = context.out();
        out.print(prefix);
        if (enabled) {
            out.print("==" + repository.getRepositoryId() + disabledString + "==");
        } else {
            out.print("@@" + repository.getRepositoryId() + disabledString + "@@");
        }
        out.print(" : " + repository.getRepositoryType() +" "+repository.getConfigManager().getLocation());
        out.println();

    }

    public void showRepoTree(NutsCommandContext context, NutsRepository repository, String prefix) {
        showRepo(context, repository, prefix);
        String prefix1 = prefix + "  ";
        for (NutsRepository c : repository.getMirrors()) {
            showRepoTree(context, c, prefix1);
        }
    }

    public static boolean trySave(NutsCommandContext context, NutsWorkspace workspace, NutsRepository repository, Boolean save, CommandLine cmdLine) {
        if (save == null) {
            if (cmdLine == null || cmdLine.isExecMode()) {
                if (repository != null) {
                    save = Boolean.parseBoolean(repository.getConfigManager().getEnv("autosave", "false", true));
                } else {
                    save = Boolean.parseBoolean(context.getWorkspace().getConfigManager().getEnv("autosave", "false"));
                }
            } else {
                save = false;
            }
        } else {
            save = false;
        }
        if (cmdLine != null) {
            while (cmdLine.hasNext()) {
                if (cmdLine.readAll("--save")) {
                    save = true;
                } else {
                    cmdLine.unexpectedArgument("config");
                }
            }
        }
        if (save) {
            if (cmdLine == null || cmdLine.isExecMode()) {
                PrintStream out = context.out();
                if (repository == null) {
                    workspace.getConfigManager().save();
                    out.printf("##workspace saved.##\n");
                } else {
                    out.printf("##repository %s saved.##\n", repository.getRepositoryId());
                    repository.save();
                }
            }
        }
        return save;
    }

}
