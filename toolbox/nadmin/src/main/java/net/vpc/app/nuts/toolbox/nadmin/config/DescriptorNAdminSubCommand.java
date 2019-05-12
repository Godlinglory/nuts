/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.NutsDescriptorBuilder;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommandLine;

/**
 * @author vpc
 */
public class DescriptorNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
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
                final boolean value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("boolean","executable")).getBoolean();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.setExecutable(value);
                    }
                });
            } else if (cmdLine.readAll("-packaging")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("packaging")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.setPackaging(value);
                    }
                });
            } else if (cmdLine.readAll("-alternative")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("alternative")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.setAlternative(value);
                    }
                });
            } else if (cmdLine.readAll("-name")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("name", "my-name")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.setId(desc.getId().setName(value));
                    }
                });
            } else if (cmdLine.readAll("-group")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("group", "my-group")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.setId(desc.getId().setGroup(value));
                    }
                });
            } else if (cmdLine.readAll("-id")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("id")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.setId(context.getWorkspace().parser().parseId(value));
                    }
                });

            } else if (cmdLine.readAll("-add-os")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.addOs(value);
                    }
                });
            } else if (cmdLine.readAll("-remove-os")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.removeOs(value);
                    }
                });

            } else if (cmdLine.readAll("-add-osdist")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("osdist")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.addOsdist(value);
                    }
                });
            } else if (cmdLine.readAll("-remove-osdist")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("osdist")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.removeOsdist(value);
                    }
                });

            } else if (cmdLine.readAll("-add-platform")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("platform")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.addPlatform(value);
                    }
                });
            } else if (cmdLine.readAll("-remove-platform")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("paltform")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.removePlatform(value);
                    }
                });

            } else if (cmdLine.readAll("-add-arch")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("arch")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.addArch(value);
                    }
                });
            } else if (cmdLine.readAll("-remove-arch")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("arch")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.removeArch(value);
                    }
                });
            } else if (cmdLine.readAll("-add-property")) {
                String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("property")).getString();
                final String[] nv = splitNameAndValue(value);
                if (nv != null) {
                    all.add(new Runnable() {
                        @Override
                        public void run() {
                            desc.addProperty(nv[0], nv[1]);
                        }
                    });
                }
            } else if (cmdLine.readAll("-remove-property")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("property")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.removeProperty(value);
                    }
                });

            } else if (cmdLine.readAll("-add-dependency")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("dependency")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.addDependency(ws.parser().parseDependency(value));
                    }
                });
            } else if (cmdLine.readAll("-remove-dependency")) {
                final String value = cmdLine.readRequiredNonOption(cmdLine.createNonOption("dependency")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.removeDependency(ws.parser().parseDependency(value));
                    }
                });
            } else if (cmdLine.readAll("-file")) {
                file = cmdLine.readRequiredNonOption(cmdLine.createNonOption("file")).getString();
            } else if (cmdLine.readAll("-save")) {
                save = cmdLine.readRequiredNonOption(cmdLine.createNonOption("boolean","save")).getBoolean();
            } else {
                if (!cmdLine.isExecMode()) {
                    throw new NutsIllegalArgumentException("config new|update descriptor: Unsupported option " + cmdLine.get());
                }
            }
        }
        if (cmdLine.isExecMode()) {
            if (newDesc) {
                desc.set(ws.createDescriptorBuilder().build());
            } else {
                if (file != null) {
                    desc.set(ws.parser().parseDescriptor(new File(file)));
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
                    ws.formatter().createDescriptorFormat().setPretty(true).print(desc.build(), new File(file));
                } else {
                    if (cmdLine.isExecMode()) {
                        throw new NutsIllegalArgumentException("config new|update descriptor: -file missing");
                    }
                }
            } else {
                context.getTerminal().fout().printf("%s\n", ws.formatter().createDescriptorFormat().setPretty(true).toString(desc.build()));
            }
        }
        return true;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    public static String[] splitNameAndValue(String arg) {
        int i = arg.indexOf('=');
        if (i >= 0) {
            return new String[]{
                    i == 0 ? "" : arg.substring(0, i),
                    i == arg.length() - 1 ? "" : arg.substring(i + 1),};
        }
        return null;
    }

}
