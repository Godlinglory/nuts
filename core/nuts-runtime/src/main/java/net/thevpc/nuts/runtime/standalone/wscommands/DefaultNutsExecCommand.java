package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.commands.ws.NutsExecutableInformationExt;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.terminals.DefaultNutsSessionTerminal;
import net.thevpc.nuts.NutsExecutorComponent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.commands.ws.NutsExecutionContextBuilder;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDefinition;
import net.thevpc.nuts.runtime.core.util.CoreNutsDependencyUtils;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNutsExecCommand extends AbstractNutsExecCommand {

    public DefaultNutsExecCommand(DefaultNutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsExecutableInformation which() {
        checkSession();
        DefaultNutsSessionTerminal terminal = new DefaultNutsSessionTerminal();
        NutsWorkspaceUtils.setSession(terminal, getSession());
        NutsSession traceSession = getSession();
        terminal.setParent(traceSession.getTerminal());
//        if (isGrabOutputString()) {
//            terminal.setOutMode(NutsTerminalMode.INHERITED);
//        }
//        if (isGrabErrorString()) {
//            terminal.setErrMode(NutsTerminalMode.INHERITED);
//        }
        if (this.in != null) {
            terminal.setIn(this.in);
        }
        if (this.out != null) {
            terminal.setOut(this.out);
        }
        if (isRedirectErrorStream()) {
            if (this.out != null) {
                terminal.setErr(this.out);
            } else {
                terminal.setErr(traceSession.getTerminal().out());
            }
        }
        terminal.out().flush();
        terminal.err().flush();
        String[] ts = command.toArray(new String[0]);
        NutsExecutableInformationExt exec = null;
        NutsSession execSession = traceSession.copy();
        execSession.setTerminal(terminal);
        NutsExecutionType executionType = this.getExecutionType();
        if (executionType == null) {
            executionType = session.getExecutionType();
        }
        if (executionType == null) {
            executionType = NutsExecutionType.SPAWN;
        }
        switch (executionType) {
            case USER_CMD: {
                if (commandDefinition != null) {
                    throw new NutsIllegalArgumentException(getSession(), "unable to run nuts as user-cmd");
                }
                exec = new DefaultNutsSystemExecutable(ts, getExecutorOptions(),
                        traceSession,
                        execSession,
                        this,
                        false,
                        isInheritSystemIO()
                );
                break;
            }
            case ROOT_CMD: {
                if (commandDefinition != null) {
                    throw new NutsIllegalArgumentException(getSession(), "unable to run nuts as root-cmd");
                }
                exec = new DefaultNutsSystemExecutable(ts, getExecutorOptions(),
                        traceSession,
                        execSession,
                        this,
                        true,
                        isInheritSystemIO()
                );
                break;
            }
            case SPAWN:
            case EMBEDDED: {
                if (commandDefinition != null) {
                    return ws_execDef(commandDefinition, commandDefinition.getId().getLongName(), ts, getExecutorOptions(), env, directory, failFast,
                            executionType, traceSession, execSession);
                } else {
                    exec = execEmbeddedOrExternal(ts, getExecutorOptions(), traceSession, execSession);
                }
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), "invalid executionType " + executionType);
            }
        }
        return exec;
    }

    @Override
    public NutsExecCommand run() {
        checkSession();
        NutsExecutableInformationExt exec = (NutsExecutableInformationExt) which();
        executed = true;
        try {
            if (dry) {
                exec.dryExecute();
            } else {
                exec.execute();
            }
        } catch (NutsExecutionException ex) {
            String p = getExtraErrorMessage();
            if (p != null) {
                result = new NutsExecutionException(getSession(),
                        "execution failed with code " + ex.getExitCode() + " and message : " + p,
                        ex, ex.getExitCode());
            } else {
                result = ex;
            }
        } catch (Exception ex) {
            String p = getExtraErrorMessage();
            if (p != null) {
                result = new NutsExecutionException(getSession(),
                        "execution failed with code " + 244 + " and message : " + p,
                        ex, 244);
            } else {
                result = new NutsExecutionException(getSession(), ex, 244);
            }
        }
        if (result != null && result.getExitCode() != 0 && failFast) {
            throw result;
//            checkFailFast(result.getExitCode());
        }
        return this;
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsDefinition nutsDefinition) {
        NutsExecutorComponent executorComponent = getSession().getWorkspace().extensions().createSupported(NutsExecutorComponent.class, nutsDefinition);
        if (executorComponent != null) {
            return executorComponent;
        }
        throw new NutsNotFoundException(getSession(), nutsDefinition.getId());
    }

    private NutsExecutableInformationExt execEmbeddedOrExternal(String[] cmd, String[] executorOptions, NutsSession prepareSession, NutsSession execSession) {
        if (cmd == null || cmd.length == 0) {
            throw new NutsIllegalArgumentException(getSession(), "missing command");
        }
        String[] args = new String[cmd.length - 1];
        System.arraycopy(cmd, 1, args, 0, args.length);
        String cmdName = cmd[0];
        //resolve internal commands!
        switch (cmdName) {
            case "update": {
                return new DefaultNutsUpdateInternalExecutable(args, execSession);
            }
            case "check-updates": {
                return new DefaultNutsCheckUpdatesInternalExecutable(args, execSession);
            }
            case "install": {
                return new DefaultNutsInstallInternalExecutable(args, execSession);
            }
            case "uninstall": {
                return new DefaultNutsUninstallInternalExecutable(args, execSession);
            }
            case "deploy": {
                return new DefaultNutsDeployInternalExecutable(args, execSession);
            }
            case "undeploy": {
                return new DefaultNutsUndeployInternalExecutable(args, execSession);
            }
            case "push": {
                return new DefaultNutsPushInternalExecutable(args, execSession);
            }
            case "fetch": {
                return new DefaultNutsFetchInternalExecutable(args, execSession);
            }
            case "search": {
                return new DefaultNutsSearchInternalExecutable(args, execSession);
            }
            case "version": {
                return new DefaultNutsVersionInternalExecutable(args, execSession, this);
            }
            case "license": {
                return new DefaultNutsLicenseInternalExecutable(args, execSession);
            }
            case "help": {
                return new DefaultNutsHelpInternalExecutable(args, execSession);
            }
            case "welcome": {
                return new DefaultNutsWelcomeInternalExecutable(args, execSession);
            }
            case "info": {
                return new DefaultNutsInfoInternalExecutable(args, execSession);
            }
            case "which": {
                return new DefaultNutsWhichInternalExecutable(args, execSession, this);
            }
            case "exec": {
                return new DefaultNutsExecInternalExecutable(args, execSession, this);
            }
        }
        NutsExecutionType executionType = getExecutionType();
        if (executionType == null) {
            executionType = session.getExecutionType();
        }
        if (executionType == null) {
            executionType = NutsExecutionType.SPAWN;
        }
        if (cmdName.contains("/") || cmdName.contains("\\")) {
            return new DefaultNutsArtifactPathExecutable(cmdName, args, executorOptions, executionType, prepareSession, execSession, this, isInheritSystemIO());
        } else if (cmdName.contains(":")) {
            boolean forceInstalled = false;
            if (cmdName.endsWith("!")) {
                cmdName = cmdName.substring(0, cmdName.length() - 1);
                forceInstalled = true;
            }
            NutsId idToExec = findExecId(cmdName, prepareSession, forceInstalled, true);
            if (idToExec == null) {
                throw new NutsNotFoundException(getSession(), cmdName);
            }
            return ws_execId(idToExec, cmdName, args, executorOptions, executionType, prepareSession, execSession);
        } else {
            NutsWorkspaceCommandAlias command = null;
            boolean forceInstalled = false;
            if (cmdName.endsWith("!")) {
                cmdName = cmdName.substring(0, cmdName.length() - 1);
                forceInstalled = true;
            }
            command = prepareSession.getWorkspace().aliases().find(cmdName);
            if (command != null) {
                NutsCommandExecOptions o = new NutsCommandExecOptions().setExecutorOptions(executorOptions).setDirectory(directory).setFailFast(failFast)
                        .setExecutionType(executionType).setEnv(env);
                return new DefaultNutsAliasExecutable(command, o, execSession, args);
            } else {
                NutsId idToExec = findExecId(cmdName, prepareSession, forceInstalled, true);
                if (idToExec == null) {
                    List<String> cmdArr = new ArrayList<>();
                    cmdArr.add(cmdName);
                    cmdArr.addAll(Arrays.asList(args));
                    return new DefaultNutsSystemExecutable(cmdArr.toArray(new String[0]), executorOptions, prepareSession, execSession, this, false,
                            isInheritSystemIO()
                    );
                }
                return ws_execId(idToExec, cmdName, args, executorOptions, executionType, prepareSession, execSession);
            }
        }
    }

    protected NutsId findExecId(String commandName, NutsSession traceSession, boolean forceInstalled, boolean ignoreIfUserCommand) {
        NutsWorkspace ws = traceSession.getWorkspace();
        NutsId nid = ws.id().parser().parse(commandName);
        if (nid == null) {
            return null;
        }
        NutsSession noProgressSession = traceSession.copy().setProgressOptions("none");
        List<NutsId> ff = ws.search().addId(nid).setSession(noProgressSession).setOptional(false).setLatest(true).setFailFast(false)
                .setDefaultVersions(true)
                //                .configure(true,"--trace-monitor")
                .setInstallStatus(ws.filters().installStatus().byDeployed(true))
                .getResultIds().list();
        if (ff.isEmpty()) {
            //retest without checking if the parseVersion is default or not
            // this help recovering from "invalid default parseVersion" issue
            ff = ws.search().addId(nid).setSession(noProgressSession).setOptional(false).setLatest(true).setFailFast(false)
                    .setInstallStatus(ws.filters().installStatus().byDeployed(true))
                    .setSession(noProgressSession)
                    .getResultIds().list();
        }
        if (ff.isEmpty()) {
            if (!forceInstalled) {
                if (ignoreIfUserCommand && isUserCommand(commandName)) {
                    return null;
                }
                //now search online
                // this helps recovering from "invalid default parseVersion" issue
                if (traceSession.isPlainTrace()) {
                    traceSession.out().resetLine().printf("%s is %s, will search for it online. Type ```error CTRL^C``` to stop...\n",
                            ws.text().forStyled(commandName, NutsTextStyle.primary1()),
                            ws.text().forStyled("not installed", NutsTextStyle.error())
                    );
                    traceSession.out().flush();
                }
                ff = ws.search().addId(nid).setSession(noProgressSession.copy().setFetchStrategy(NutsFetchStrategy.ONLINE))
                        .setOptional(false).setFailFast(false)
                        .setLatest(true)
                        //                        .configure(true,"--trace-monitor")
                        .getResultIds().list();
            }
        }
        if (ff.isEmpty()) {
            return null;
        } else {
            List<NutsVersion> versions = ff.stream().map(x -> x.getVersion()).distinct().collect(Collectors.toList());
            if (versions.size() > 1) {
                throw new NutsTooManyElementsException(getSession(), nid.toString() + " can be resolved to all (" + ff.size() + ") of " + ff);
            }
        }
        return ff.get(0);
    }

    public boolean isUserCommand(String s) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        String p = System.getenv().get("PATH");
        if (p != null) {
            char r = File.pathSeparatorChar;
            for (String z : p.split("" + r)) {
                Path t = Paths.get(z);
                switch (ws.env().getOsFamily()) {
                    case WINDOWS: {
                        if (Files.isRegularFile(t.resolve(s))) {
                            return true;
                        }
                        if (Files.isRegularFile(t.resolve(s + ".exe"))) {
                            return true;
                        }
                        if (Files.isRegularFile(t.resolve(s + ".bat"))) {
                            return true;
                        }
                        break;
                    }
                    default: {
                        Path fp = t.resolve(s);
                        if (Files.isRegularFile(fp)) {
                            //if(Files.isExecutable(fp)) {
                            return true;
                            //}
                        }
                    }
                }
            }
        }
        return false;
    }

    protected NutsExecutableInformationExt ws_execId(NutsId goodId, String commandName, String[] appArgs, String[] executorOptions,
            NutsExecutionType executionType,
            NutsSession traceSession, NutsSession execSession) {
        NutsSession noProgressSession = traceSession.copy().setProgressOptions("none");
        NutsDefinition def = ws.fetch().setId(goodId)
                .setSession(noProgressSession)
                .setDependencies(true)
                .setFailFast(true)
                .setEffective(true)
                .setContent(true)
                //
                .setOptional(false)
                .addScope(NutsDependencyScopePattern.RUN)
                .setDependencyFilter(CoreNutsDependencyUtils.createJavaRunDependencyFilter(traceSession))
                //
                .getResultDefinition();
        return ws_execDef(def, commandName, appArgs, executorOptions, env, directory, failFast, executionType, traceSession, execSession);
    }

    protected NutsExecutableInformationExt ws_execDef(NutsDefinition def, String commandName, String[] appArgs, String[] executorOptions, Map<String, String> env, String dir, boolean failFast, NutsExecutionType executionType, NutsSession traceSession, NutsSession execSession) {
        return new DefaultNutsArtifactExecutable(def, commandName, appArgs, executorOptions, env, dir, failFast, traceSession, execSession, executionType, this);
    }

    public void ws_execId(NutsDefinition def, String commandName, String[] appArgs, String[] executorOptions, Map<String, String> env, String dir, boolean failFast, boolean temporary,
            NutsSession traceSession,
            NutsSession execSession,
            NutsExecutionType executionType, boolean dry
    ) {
        //TODO ! one of the sessions needs to be removed!
        NutsWorkspaceUtils.checkSession(ws, session);
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        ws.security().setSession(session).checkAllowed(NutsConstants.Permissions.EXEC, commandName);
        NutsWorkspaceUtils.checkSession(ws, execSession);
        NutsWorkspaceUtils.checkSession(ws, traceSession);
        if (def != null && def.getPath() != null) {
            NutsDescriptor descriptor = def.getDescriptor();
            if (!descriptor.isExecutable()) {
//                session.getTerminal().getErr().println(nutToRun.getId()+" is not executable... will perform extra checks.");
//                throw new NutsNotExecutableException(descriptor.getId());
            }
            NutsArtifactCall executor = descriptor.getExecutor();
            NutsExecutorComponent execComponent = null;
            List<String> executorArgs = new ArrayList<>();
            Map<String, String> execProps = null;
            if (executor == null) {
                execComponent = resolveNutsExecutorComponent(def);
            } else {
                if (executor.getId() == null) {
                    execComponent = resolveNutsExecutorComponent(def);
                } else {
                    NutsDefinition customDef = new DefaultNutsDefinition(null, null, executor.getId(),
                            ws.descriptor().descriptorBuilder()
                                    .setId(executor.getId()).setExecutor(executor)
                                    .setExecutable(true)
                                    .build(), null, null, NutsIdType.REGULAR, null, getSession()
                    );
                    execComponent = resolveNutsExecutorComponent(customDef);
                }
                executorArgs.addAll(Arrays.asList(executor.getArguments()));
                execProps = executor.getProperties();
            }
            executorArgs.addAll(Arrays.asList(executorOptions));
            NutsExecutionContextBuilder ecb = NutsWorkspaceExt.of(ws).createExecutionContext();
            NutsExecutionContext executionContext = ecb
                    .setDefinition(def)
                    .setArguments(appArgs)
                    .setExecutorArguments(executorArgs.toArray(new String[0]))
                    .setEnv(env)
                    .setExecutorProperties(execProps)
                    .setCwd(dir)
                    .setWorkspace(traceSession.getWorkspace())
                    .setTraceSession(traceSession)
                    .setExecSession(execSession)
                    .setFailFast(failFast)
                    .setTemporary(temporary)
                    .setExecutionType(executionType)
                    .setCommandName(commandName)
                    .setSleepMillis(getSleepMillis())
                    .setInheritSystemIO(isInheritSystemIO())
                    .setRedirectOuputFile(getRedirectOuputFile())
                    .setRedirectInpuFile(getRedirectInpuFile())
                    .build();
            if (dry) {
                execComponent.dryExec(executionContext);
            } else {
                execComponent.exec(executionContext);
            }
            return;

        }
        throw new NutsNotFoundException(getSession(), def == null ? null : def.getId());
    }
}
