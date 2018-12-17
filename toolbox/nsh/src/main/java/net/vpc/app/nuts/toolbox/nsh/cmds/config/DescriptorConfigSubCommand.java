/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nsh.cmds.config;

import net.vpc.app.nuts.NutsDescriptorBuilder;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.cmds.ConfigCommand;
import net.vpc.app.nuts.toolbox.nsh.util.ShellHelper;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.FileNonOption;
import net.vpc.common.commandline.ValueNonOption;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vpc
 */
public class DescriptorConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        boolean newDesc = false;
        String file = null;
        boolean save = false;
        NutsWorkspace ws = context.getWorkspace();
        final NutsDescriptorBuilder desc = ws.createDescriptorBuilder();
        if (cmdLine.readAll("new descriptor", "nd")) {
            newDesc = true;
        } else if (cmdLine.readAll("update descriptor", "ud")) {
            newDesc = false;
        } else {
            return false;
        }

        List<Runnable> all = new ArrayList<>();
        while (cmdLine.hasNext()) {
            if (cmdLine.readAll("-executable")) {
                final boolean value = cmdLine.readRequiredNonOption(new ValueNonOption("executable", "true", "false")).getBoolean();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.setExecutable(value);
                    }
                });
            } else if (cmdLine.readAll("-ext")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("ext", "jar")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.setExt(value);
                    }
                });
            } else if (cmdLine.readAll("-packaging")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("packaging",  "jar")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.setPackaging(value);
                    }
                });
            } else if (cmdLine.readAll("-name")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("name",  "my-name")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.setId(desc.getId().setName(value));
                    }
                });
            } else if (cmdLine.readAll("-group")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("group", "my-group")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.setId(desc.getId().setGroup(value));
                    }
                });
            } else if (cmdLine.readAll("-id")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("id", "my-group:my-name#1.0")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.setId(context.getWorkspace().parseNutsId(value));
                    }
                });

            } else if (cmdLine.readAll("-add-os")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("os",  "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.addOs(value);
                    }
                });
            } else if (cmdLine.readAll("-remove-os")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("os", "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.removeOs(value);
                    }
                });

            } else if (cmdLine.readAll("-add-osdist")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("os",  "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.addOsdist(value);
                    }
                });
            } else if (cmdLine.readAll("-remove-osdist")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("os",  "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.removeOsdist(value);
                    }
                });

            } else if (cmdLine.readAll("-add-platform")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("os", "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.addPlatform(value);
                    }
                });
            } else if (cmdLine.readAll("-remove-platform")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("os", "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.removePlatform(value);
                    }
                });

            } else if (cmdLine.readAll("-add-arch")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("os", "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.addArch(value);
                    }
                });
            } else if (cmdLine.readAll("-remove-arch")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("os",  "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.removeArch(value);
                    }
                });
            } else if (cmdLine.readAll("-add-property")) {
                String value = cmdLine.readRequiredNonOption(new ValueNonOption("os", "os")).getString();
                final String[] nv = ShellHelper.splitNameAndValue(value);
                if (nv != null) {
                    all.add(new Runnable() {
                        @Override
                        public void run() {
                            desc.addProperty(nv[0], nv[1]);
                        }
                    });
                }
            } else if (cmdLine.readAll("-remove-property")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("os",  "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.removeProperty(value);
                    }
                });

            } else if (cmdLine.readAll("-add-dependency")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("dependency",  "my-group:my-name#1.0")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.addDependency(ws.parseDependency(value));
                    }
                });
            } else if (cmdLine.readAll("-remove-dependency")) {
                final String value = cmdLine.readRequiredNonOption(new ValueNonOption("dependency", "my-group:my-name#1.0")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.removeDependency(ws.parseDependency(value));
                    }
                });
            } else if (cmdLine.readAll("-file")) {
                file = cmdLine.readRequiredNonOption(new FileNonOption("file")).getString();
            } else if (cmdLine.readAll("-save")) {
                save = cmdLine.readRequiredNonOption(new ValueNonOption("save",  "true", "false")).getBoolean();
            } else {
                if (!cmdLine.isExecMode()) {
                    throw new NutsIllegalArgumentException("config new|update descriptor: Unsupported option "+cmdLine.get());
                }
            }
        }
        if (cmdLine.isExecMode()) {
            if (newDesc) {
                desc.set(ws.createDescriptorBuilder().build());
            } else {
                if (file != null) {
                    desc.set(ws.parseDescriptor(new File(file)));
                } else {
                    if (cmdLine.isExecMode()) {
                        throw new NutsIllegalArgumentException("config new|update descriptor: -file missing");
                    }
                }
            }

            for (Runnable r : all) {
                r.run();
            }
            if (save) {
                if (file != null) {
                    desc.build().write(new File(file));
                } else {
                    if (cmdLine.isExecMode()) {
                        throw new NutsIllegalArgumentException("config new|update descriptor: -file missing");
                    }
                }
            } else {
                context.getTerminal().getFormattedOut().printf("%s\n", desc.build().toString(true));
            }
        }
        return true;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

}
