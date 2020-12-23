package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nadmin.subcommands.AbstractNAdminSubCommand;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.sys.LinuxNdi;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.sys.MacosNdi;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.sys.UnixNdi;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.sys.WindowsNdi;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util.NdiUtils;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NdiSubCommand extends AbstractNAdminSubCommand {

    public SystemNdi createNdi(NutsApplicationContext appContext) {
        SystemNdi ndi = null;
        switch (appContext.getWorkspace().env().getOsFamily()) {
            case LINUX: {
                ndi = new LinuxNdi(appContext);
                break;
            }
            case UNIX: {
                ndi = new UnixNdi(appContext);
                break;
            }
            case MACOS: {
                ndi = new MacosNdi(appContext);
                break;
            }
            case WINDOWS: {
                ndi = new WindowsNdi(appContext);
                break;
            }
        }
        return ndi;
    }

    public void runInstall(NutsCommandLine cmdLine, NutsApplicationContext context) {
        ArrayList<String> idsToInstall = new ArrayList<>();
        NutsWorkspace ws = context.getWorkspace();
        List<NdiScriptnfo> result = new ArrayList<NdiScriptnfo>();
        ArrayList<String> executorOptions = new ArrayList<>();
        NutsExecutionType execType = null;
        boolean fetch = false;
        boolean missingAnyArgument = true;
        NutsArgument a;
//        boolean forceAll = false;
        Boolean persistentConfig = null;
        boolean ignoreUnsupportedOs = false;

        String switchWorkspaceLocation = null;
        String linkName = null;

        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {

            } else if ((a = cmdLine.nextBoolean("-t", "--fetch")) != null) {
                fetch = a.getBooleanValue();
            } else if ((a = cmdLine.nextBoolean("-x", "--external", "--spawn")) != null) {
                if (a.getBooleanValue()) {
                    execType = NutsExecutionType.SPAWN;
                }
            } else if ((a = cmdLine.nextBoolean("-b", "--embedded")) != null) {
                if (a.getBooleanValue()) {
                    execType = NutsExecutionType.EMBEDDED;
                }
            } else if ((a = cmdLine.nextBoolean("--user-cmd")) != null) {
                if (a.getBooleanValue()) {
                    execType = NutsExecutionType.USER_CMD;
                }
            } else if ((a = cmdLine.nextBoolean("--root-cmd")) != null) {
                if (a.getBooleanValue()) {
                    execType = NutsExecutionType.ROOT_CMD;
                }
            } else if ((a = cmdLine.nextString("-X", "--exec-options")) != null) {
                executorOptions.add(a.getStringValue());
            } else if ((a = cmdLine.nextString("-i", "--installed")) != null) {
                context.getSession().setConfirm(NutsConfirmationMode.YES);
                for (NutsId resultId : ws.search().addInstallStatus(NutsInstallStatus.INSTALLED).getResultIds()) {
                    idsToInstall.add(resultId.getLongName());
                    missingAnyArgument = false;
                }
            } else if ((a = cmdLine.nextString("-c", "--companions")) != null) {
                context.getSession().setConfirm(NutsConfirmationMode.YES);
                for (NutsId companion : ws.companionIds()) {
                    idsToInstall.add(ws.search().addId(companion).setLatest(true).getResultIds().required().getLongName());
                    missingAnyArgument = false;
                }
            } else if ((a = cmdLine.nextString("--switch")) != null) {
                Boolean booleanValue = a.getBooleanValue(null);
                if (booleanValue != null) {
                    persistentConfig = booleanValue;
                }
            } else if ((a = cmdLine.nextString("--ignore-unsupported-os")) != null) {
                if (a.isEnabled()) {
                    ignoreUnsupportedOs = a.getBooleanValue();
                }
            } else if (cmdLine.peek().getStringKey().equals("-w") || cmdLine.peek().getStringKey().equals("--workspace")) {
                a=cmdLine.nextString();
                if (a.isEnabled()) {
                    switchWorkspaceLocation = a.getStringValue();
                }
            } else if (cmdLine.peek().getStringKey().equals("-n") || cmdLine.peek().getStringKey().equals("--name")) {
                a=cmdLine.nextString();
                if (a.isEnabled()) {
                    linkName = a.getStringValue();
                }
            } else if (cmdLine.peek().isOption()) {
                cmdLine.unexpectedArgument();
            } else {
                idsToInstall.add(cmdLine.next().getString());
                missingAnyArgument = false;
            }
        }

        if (missingAnyArgument) {
            cmdLine.required();
        }
        Path workspaceLocation = ws.locations().getWorkspaceLocation();
        if (cmdLine.isExecMode()) {
            SystemNdi ndi = createNdi(context);
            if (ndi == null) {
                if (ignoreUnsupportedOs) {
                    return;
                }
                throw new NutsExecutionException(ws, "platform not supported : " + ws.env().getOs(), 2);
            }
            boolean subTrace = context.getSession().isTrace();
            if (!context.getSession().isPlainTrace()) {
                subTrace = false;
            }

            if (!idsToInstall.isEmpty()) {
                if(persistentConfig==null) {
                    if (workspaceLocation.equals(Paths.get(System.getProperty("user.home")).resolve(".config/nuts/default-workspace"))) {
                        persistentConfig = true;
                    } else {
                        persistentConfig = false;
                    }
                }
                for (String id : idsToInstall) {
                    try {
                        NutsId nid = ws.id().parser().parse(id);
                        if(nid==null){
                            throw new NutsExecutionException(ws, "unable to create script for " + id + " : invalid id",100);
                        }
                        boolean includeEnv=false;
                        if(nid.getShortName().equals("nuts") || nid.getShortName().equals("net.thevpc.nuts:nuts")){
                            if(!nid.getVersion().isBlank()){
                                includeEnv=true;
                            }else if(nid.getVersion().toString().equalsIgnoreCase("current")){
                                id=nid.builder().setVersion(ws.getApiId().getVersion()).build().toString();
                                includeEnv=true;
                            }
                        }
                        String linkNameCurrent=linkName;
                        if(includeEnv){
                            linkNameCurrent=prepareLinkName(linkNameCurrent);
                        }
                        result.addAll(
                                Arrays.asList(
                                        ndi.createNutsScript(
                                                new NdiScriptOptions().setId(id)
                                                        .setSession(context.getSession().copy().setTrace(subTrace))
                                                        .setForceBoot(context.getSession().isYes())
                                                        .setFetch(fetch)
                                                        .setExecType(execType)
                                                        .setExecutorOptions(executorOptions)
                                                        .setIncludeEnv(includeEnv)
                                                        .setPreferredScriptName(linkNameCurrent)
                                        )
                                ));
                    } catch (UncheckedIOException e) {
                        throw new NutsExecutionException(ws, "unable to add script for " + id + " : " + e.toString(), e);
                    }
                }
                ndi.configurePath(context.getSession(), persistentConfig);
                printResults(context, result, ws);
            }
        }
    }

    private String prepareLinkName(String linkName) {
        if (linkName == null) {
            linkName = "%n-%v";
        } else if (Files.isDirectory(Paths.get(linkName))) {
            linkName = Paths.get(linkName).resolve("%n-%v").toString();
        } else if (linkName.endsWith("/") || linkName.endsWith("\\")) {
            linkName = Paths.get(linkName).resolve("%n-%v").toString();
        }
        return linkName;
    }

    public void runUninstall(NutsCommandLine cmdLine, NutsApplicationContext context) {
        ArrayList<String> idsToUninstall = new ArrayList<>();
        boolean forceAll = false;
        NutsWorkspace ws = context.getWorkspace();
        boolean missingAnyArgument = true;
        NutsArgument a;
        boolean ignoreUnsupportedOs = false;
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
                //consumed
            } else if ((a = cmdLine.nextString("--ignore-unsupported-os")) != null) {
                if (a.isEnabled()) {
                    ignoreUnsupportedOs = a.getBooleanValue();
                }
            } else if (cmdLine.peek().isOption()) {
                cmdLine.unexpectedArgument();
            } else {
                idsToUninstall.add(cmdLine.next().getString());
                missingAnyArgument = false;
            }
        }
        if (missingAnyArgument) {
            cmdLine.required();
        }
        Path workspaceLocation = ws.locations().getWorkspaceLocation();
        if (cmdLine.isExecMode()) {
            if (forceAll) {
                context.getSession().setConfirm(NutsConfirmationMode.YES);
            }
            SystemNdi ndi = createNdi(context);
            if (ndi == null) {
                if (ignoreUnsupportedOs) {
                    return;
                }
                throw new NutsExecutionException(ws, "platform not supported : " + ws.env().getOs(), 2);
            }
            boolean subTrace = context.getSession().isTrace();
            if (!context.getSession().isPlainTrace()) {
                subTrace = false;
            }
            if (!idsToUninstall.isEmpty()) {
                for (String id : idsToUninstall) {
                    try {
                        ndi.removeNutsScript(
                                id,
                                context.getSession().copy().setTrace(subTrace)
                        );
                    } catch (UncheckedIOException e) {
                        throw new NutsExecutionException(ws, "Unable to run script " + id + " : " + e.toString(), e);
                    }
                }
            }
        }
    }


    public void runSwitch(NutsCommandLine cmdLine, NutsApplicationContext context) {
        NutsWorkspace ws = context.getWorkspace();
        String switchWorkspaceLocation = null;
        String switchWorkspaceApi = null;
        List<NdiScriptnfo> result = new ArrayList<NdiScriptnfo>();
        ArrayList<String> executorOptions = new ArrayList<>();
        NutsExecutionType execType = null;
        NutsArgument a;
        Runnable action = null;
        boolean ignoreUnsupportedOs = false;
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
                //consumed
            } else if ((a = cmdLine.nextString("--ignore-unsupported-os")) != null) {
                if (a.isEnabled()) {
                    ignoreUnsupportedOs = a.getBooleanValue();
                }
            } else if (cmdLine.peek().getStringKey().equals("-w") || cmdLine.peek().getStringKey().equals("--workspace")) {
                switchWorkspaceLocation = cmdLine.nextString().getStringValue();
            } else if (cmdLine.peek().getStringKey().equals("-a") || cmdLine.peek().getStringKey().equals("--api")) {
                switchWorkspaceApi = cmdLine.nextString().getStringValue();
            } else if (cmdLine.peek().isOption()) {
                cmdLine.unexpectedArgument();
            } else if (switchWorkspaceLocation == null) {
                switchWorkspaceLocation = cmdLine.next().getString();
            } else if (switchWorkspaceApi == null) {
                switchWorkspaceApi = cmdLine.next().getString();
            } else {
                cmdLine.unexpectedArgument();
            }
        }
        if (cmdLine.isExecMode()) {
            SystemNdi ndi = createNdi(context);
            if (ndi == null) {
                if (ignoreUnsupportedOs) {
                    return;
                }
                throw new NutsExecutionException(ws, "platform not supported : " + ws.env().getOs(), 2);
            }
            if (switchWorkspaceLocation != null || switchWorkspaceApi != null) {
                ndi.switchWorkspace(switchWorkspaceLocation, switchWorkspaceApi);
            }
        }

    }

    private void printResults(NutsApplicationContext context, List<NdiScriptnfo> result, NutsWorkspace ws) {
        if (context.getSession().isTrace()) {
            if (context.getSession().isPlainTrace()) {
                int namesSize = result.stream().mapToInt(x -> x.getName().length()).max().orElse(1);
                for (NdiScriptnfo ndiScriptnfo : result) {
                    context.getSession().out().printf("%s script ##%-" + namesSize + "s## for " +
                                    ws.id().formatter(ndiScriptnfo.getId().getLongNameId()).format()
                                    + " at ####%s####%n", ndiScriptnfo.isOverride() ?
                                    ws.formats().text().builder().appendStyled("re-installing", NutsTextNodeStyle.SUCCESS2) :
                                    ws.formats().text().builder().appendStyled("installing", NutsTextNodeStyle.SUCCESS1),
                            ndiScriptnfo.getName(), NdiUtils.betterPath(ndiScriptnfo.getPath().toString()));
                }

            } else {
                context.getSession().formatObject(result).println();
            }
        }
    }


    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsApplicationContext context) {
        if (cmdLine.next("add script", "sca") != null) {
            runInstall(cmdLine, context);
            return true;
        } else if (cmdLine.next("remove script", "scrm") != null) {
            runUninstall(cmdLine, context);
            return true;
        } else if (cmdLine.next("switch", "sw") != null) {
            runSwitch(cmdLine, context);
            return true;
        }
        return false;
    }
}