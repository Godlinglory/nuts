package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NutsExecutableInformationExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NutsExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.executor.ArtifactExecutorComponent;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy.DefaultNutsDeployInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch.DefaultNutsFetchInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.help.DefaultNutsHelpInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.info.DefaultNutsInfoInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.install.DefaultNutsInstallInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.install.DefaultNutsReinstallInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.license.DefaultNutsLicenseInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.push.DefaultNutsPushInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.search.DefaultNutsSearchInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias.DefaultNutsAliasExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.DefaultNutsSettingsInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.undeploy.DefaultNutsUndeployInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall.DefaultNutsUninstallInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.update.DefaultNutsCheckUpdatesInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.update.DefaultNutsUpdateInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.version.DefaultNutsVersionInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.welcome.DefaultNutsWelcomeInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.which.DefaultNutsWhichInternalExecutable;
import net.thevpc.nuts.spi.NutsExecutorComponent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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
        NutsSession traceSession = getSession();
        NutsSession execSession = traceSession.copy();
        NutsSessionTerminal terminal = NutsSessionTerminal.of(traceSession.getTerminal(),execSession);
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
        if (command == null || command.size() == 0) {
            throw new NutsIllegalArgumentException(traceSession, NutsMessage.plain("missing command"));
        }
        String[] ts = command.toArray(new String[0]);
        NutsExecutableInformationExt exec = null;
        execSession.setTerminal(terminal);
        NutsExecutionType executionType = this.getExecutionType();
        NutsRunAs runAs = this.getRunAs();
        if (executionType == null) {
            executionType = session.getExecutionType();
        }
        if (executionType == null) {
            executionType = NutsExecutionType.SPAWN;
        }
        switch (executionType) {
            case OPEN: {
                if (commandDefinition != null) {
                    throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unable to run open artifact"));
                }
                exec = new DefaultNutsOpenExecutable(ts, getExecutorOptions(), traceSession, execSession, this);
                break;
            }
            case SYSTEM: {
                if (commandDefinition != null) {
                    throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unable to run artifact as " + executionType + "cmd"));
                }
                List<String> tsl = new ArrayList<>(Arrays.asList(ts));
                if (CoreStringUtils.firstIndexOf(ts[0], new char[]{'/', '\\'}) < 0) {
                    Path p = CoreIOUtils.sysWhich(ts[0]);
                    if (p != null) {
                        tsl.set(0, p.toString());
                    }
                }
                exec = new DefaultNutsSystemExecutable(tsl.toArray(new String[0]), getExecutorOptions(),
                        traceSession,
                        execSession,
                        this
                );
                break;
            }
            case SPAWN:
            case EMBEDDED: {
                if (commandDefinition != null) {
                    return ws_execDef(commandDefinition, commandDefinition.getId().getLongName(), ts, getExecutorOptions(), env, directory, failFast,
                            executionType, runAs, traceSession, execSession);
                } else {
                    exec = execEmbeddedOrExternal(ts, getExecutorOptions(), traceSession, execSession);
                }
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.cstyle("invalid execution type %s", executionType));
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
                        NutsMessage.cstyle("execution failed with code %s and message : %s", ex.getExitCode(), p),
                        ex, ex.getExitCode());
            } else {
                result = ex;
            }
        } catch (Exception ex) {
            String p = getExtraErrorMessage();
            NutsExceptionBase ee = NutsExceptionBase.detectExceptionBase(ex);
            int exitCode = 244;
            if (ee instanceof NutsExecutionException) {
                exitCode = ((NutsExecutionException) ee).getExitCode();
            }
            if (exitCode != 0) {
                if (p != null) {
                    result = new NutsExecutionException(getSession(),
                            NutsMessage.cstyle("execution failed with code %s and message :  of : %s ; error was : %s ; notes : %s", 244, exec, ex, p),
                            ex, exitCode);
                } else {
                    result = new NutsExecutionException(getSession(), NutsMessage.cstyle("execution failed of : %s ; error was : %s", exec, ex),
                            ex,
                            exitCode
                    );
                }
            }
        }
        if (result != null && result.getExitCode() != 0 && failFast) {
            throw result;
//            checkFailFast(result.getExitCode());
        }
        return this;
    }

    private NutsExecutableInformationExt execEmbeddedOrExternal(String[] cmd, String[] executorOptions, NutsSession prepareSession, NutsSession execSession) {
        if (cmd == null || cmd.length == 0) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing command"));
        }
        String[] args = new String[cmd.length - 1];
        System.arraycopy(cmd, 1, args, 0, args.length);
        String cmdName = cmd[0];
        //resolve internal commands!
        NutsExecutionType executionType = getExecutionType();
        if (executionType == null) {
            executionType = session.getExecutionType();
        }
        if (executionType == null) {
            executionType = NutsExecutionType.SPAWN;
        }
        NutsRunAs runAs = getRunAs();
        CmdKind cmdKind = null;
        NutsId goodId = null;
        String goodKw = null;
        boolean forceInstalled = false;
        if (cmdName.endsWith("!")) {
            goodId = NutsIdParser.of(session).setLenient(true).parse(cmdName.substring(0, cmdName.length() - 1));
            if (goodId != null) {
                forceInstalled = true;
            }
        } else {
            goodId = NutsIdParser.of(session).setLenient(true).parse(cmdName);
        }

        if (cmdName.contains("/") || cmdName.contains("\\")) {
            if (goodId != null) {
                cmdKind = CmdKind.ID;
            } else {
                cmdKind = CmdKind.PATH;
            }
        } else if (cmdName.contains(":") || cmdName.contains("#")) {
            if (goodId != null) {
                cmdKind = CmdKind.ID;
            } else {
                throw new NutsNotFoundException(getSession(), null, NutsMessage.cstyle("unable to resolve id %", cmdName));
            }
        } else {
            if (cmdName.endsWith("!")) {
                //name that terminates with '!'
                goodKw = cmdName.substring(0, cmdName.length() - 1);
                forceInstalled = true;
            } else {
                goodKw = cmdName;
            }
            cmdKind = CmdKind.KEYWORD;
        }
        switch (cmdKind) {
            case PATH: {
                return new DefaultNutsArtifactPathExecutable(cmdName, args, executorOptions, executionType, runAs, prepareSession, execSession, this, isInheritSystemIO());
            }
            case ID: {
                NutsId idToExec = findExecId(goodId, prepareSession, forceInstalled, true);
                if (idToExec != null) {
                    return ws_execId(idToExec, cmdName, args, executorOptions, executionType, runAs, prepareSession, execSession);
                } else {
                    throw new NutsNotFoundException(getSession(), goodId);
                }
            }
            case KEYWORD: {
                switch (goodKw) {
                    case "update": {
                        return new DefaultNutsUpdateInternalExecutable(args, execSession);
                    }
                    case "check-updates": {
                        return new DefaultNutsCheckUpdatesInternalExecutable(args, execSession);
                    }
                    case "install": {
                        return new DefaultNutsInstallInternalExecutable(args, execSession);
                    }
                    case "reinstall": {
                        return new DefaultNutsReinstallInternalExecutable(args, execSession);
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
                    case "settings": {
                        return new DefaultNutsSettingsInternalExecutable(args, execSession);
                    }
                }
                NutsWorkspaceCustomCommand command = null;
                command = prepareSession.commands().findCommand(goodKw);
                if (command != null) {
                    NutsCommandExecOptions o = new NutsCommandExecOptions().setExecutorOptions(executorOptions).setDirectory(directory).setFailFast(failFast)
                            .setExecutionType(executionType).setEnv(env);
                    return new DefaultNutsAliasExecutable(command, o, execSession, args);
                } else {
                    NutsId idToExec = null;
                    if (goodId != null) {
                        idToExec = findExecId(goodId, prepareSession, forceInstalled, true);
                    }
                    if (idToExec == null) {
                        Path sw = CoreIOUtils.sysWhich(cmdName);
                        if (sw != null) {
                            List<String> cmdArr = new ArrayList<>();
                            cmdArr.add(sw.toString());
                            cmdArr.addAll(Arrays.asList(args));
                            return new DefaultNutsSystemExecutable(cmdArr.toArray(new String[0]), executorOptions, prepareSession, execSession, this);
                        }
                        List<String> cmdArr = new ArrayList<>();
                        cmdArr.add(cmdName);
                        cmdArr.addAll(Arrays.asList(args));
                        return new DefaultUnknownExecutable(cmdArr.toArray(new String[0]), execSession);
                    }
                    return ws_execId(idToExec, cmdName, args, executorOptions, executionType, runAs, prepareSession, execSession);
                }
            }
        }
        throw new NutsNotFoundException(getSession(), goodId, NutsMessage.cstyle("unable to resolve id %", cmdName));
    }

    protected NutsId findExecId(NutsId nid, NutsSession traceSession, boolean forceInstalled, boolean ignoreIfUserCommand) {
        NutsWorkspace ws = traceSession.getWorkspace();
        if (nid == null) {
            return null;
        }
        List<NutsId> ff = traceSession.search().addId(nid).setOptional(false).setLatest(true).setFailFast(false)
                .setInstallStatus(NutsInstallStatusFilters.of(session).byDeployed(true))
                .getResultDefinitions().stream()
                .sorted(Comparator.comparing(x -> !x.getInstallInformation().isDefaultVersion())) // default first
                .map(NutsDefinition::getId).collect(Collectors.toList());
        if (ff.isEmpty()) {
            if (!forceInstalled) {
                if (ignoreIfUserCommand && isUserCommand(nid.toString())) {
                    return null;
                }
                //now search online
                // this helps recovering from "invalid default parseVersion" issue
                if (traceSession.isPlainTrace()) {
                    traceSession.out().resetLine().printf("%s is %s, will search for it online. Type ```error CTRL^C``` to stop...\n",
                            nid,
                            NutsTexts.of(session).ofStyled("not installed", NutsTextStyle.error())
                    );
                    traceSession.out().flush();
                }
                ff = traceSession.search().addId(nid).setSession(traceSession.copy().setFetchStrategy(NutsFetchStrategy.ONLINE))
                        .setOptional(false).setFailFast(false)
                        .setLatest(true)
                        //                        .configure(true,"--trace-monitor")
                        .getResultIds().toList();
            }
        }
        if (ff.isEmpty()) {
            return null;
        } else {
            List<NutsVersion> versions = ff.stream().map(NutsId::getVersion).distinct().collect(Collectors.toList());
            if (versions.size() > 1) {
                throw new NutsTooManyElementsException(getSession(),
                        NutsMessage.cstyle("%s can be resolved to all (%d) of %s", nid, ff.size(), ff)
                );
            }
        }
        return ff.get(0);
    }

    public boolean isUserCommand(String s) {
        checkSession();
        NutsSession ws = getSession();
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
                                                     NutsExecutionType executionType, NutsRunAs runAs,
                                                     NutsSession session, NutsSession execSession) {
        NutsDefinition def = session.fetch().setId(goodId)
                .setDependencies(true)
                .setFailFast(true)
                .setEffective(true)
                .setContent(true)
                //
                .setOptional(false)
                .addScope(NutsDependencyScopePattern.RUN)
                .setDependencyFilter(NutsDependencyFilters.of(session).byRunnable())
                //
                .getResultDefinition();
        return ws_execDef(def, commandName, appArgs, executorOptions, env, directory, failFast, executionType, runAs, session, execSession);
    }

    protected NutsExecutableInformationExt ws_execDef(NutsDefinition def, String commandName, String[] appArgs, String[] executorOptions, Map<String, String> env, String dir, boolean failFast, NutsExecutionType executionType, NutsRunAs runAs, NutsSession traceSession, NutsSession execSession) {
        return new DefaultNutsArtifactExecutable(def, commandName, appArgs, executorOptions, env, dir, failFast, traceSession, execSession, executionType, runAs, this);
    }

    public void ws_execId(NutsDefinition def, String commandName, String[] appArgs, String[] executorOptions, Map<String, String> env, String dir, boolean failFast, boolean temporary,
                          NutsSession session,
                          NutsSession execSession,
                          NutsExecutionType executionType,
                          NutsRunAs runAs,
                          boolean dry
    ) {
        //TODO ! one of the sessions needs to be removed!
        NutsWorkspaceUtils.checkSession(ws, this.session);
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        this.session.security().checkAllowed(NutsConstants.Permissions.EXEC, commandName);
        NutsWorkspaceUtils.checkSession(ws, execSession);
        NutsWorkspaceUtils.checkSession(ws, session);
        if (def != null && def.getFile() != null) {
            NutsDescriptor descriptor = def.getDescriptor();
            if (!descriptor.isExecutable()) {
//                session.getTerminal().getErr().println(nutToRun.getId()+" is not executable... will perform extra checks.");
//                throw new NutsNotExecutableException(descriptor.getId());
            }
            NutsArtifactCall executorCall = descriptor.getExecutor();
            NutsExecutorComponent execComponent = null;
            List<String> executorArgs = new ArrayList<>();
            Map<String, String> execProps = null;

            if (executorCall != null) {
                NutsId eid = executorCall.getId();
                if (eid != null) {
                    //process special executors
                    if (eid.getGroupId() == null) {
                        if (eid.getArtifactId().equals("nuts")) {
                            eid = eid.builder().setGroupId("net.thevpc.nuts").build();
                        } else if (eid.getArtifactId().equals("nsh")) {
                            eid = eid.builder().setGroupId("net.thevpc.nuts.toolbox").build();
                        }
                    }
                    if (eid.getGroupId() != null) {
                        //nutsDefinition
                        NutsStream<NutsDefinition> q = getSession().search().addId(eid).setLatest(true)
                                .setDistinct(true)
                                .getResultDefinitions();
                        NutsDefinition[] availableExecutors = q.stream().limit(2).toArray(NutsDefinition[]::new);
                        if (availableExecutors.length > 1) {
                            throw new NutsTooManyElementsException(this.session, NutsMessage.cstyle("too many results for executor %s", eid));
                        } else if (availableExecutors.length == 1) {
                            execComponent = new ArtifactExecutorComponent(availableExecutors[0].getId(), this.session);
                        } else {
                            // availableExecutors.length=0;
                            throw new NutsNotFoundException(this.session, eid, NutsMessage.cstyle("executor not found %s", eid));
                        }
                    }
                }
            }
            if (execComponent == null) {
                execComponent = getSession().extensions().createSupported(NutsExecutorComponent.class, true, def);
            }
            if (executorCall != null) {
                executorArgs.addAll(Arrays.asList(executorCall.getArguments()));
                execProps = executorCall.getProperties();
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
                    .setWorkspace(session.getWorkspace())
                    .setSession(session)
                    .setExecSession(execSession)
                    .setFailFast(failFast)
                    .setTemporary(temporary)
                    .setExecutionType(executionType)
                    .setRunAs(runAs)
                    .setCommandName(commandName)
                    .setSleepMillis(getSleepMillis())
                    .setInheritSystemIO(isInheritSystemIO())
                    .setRedirectOuputFile(getRedirectOutputFile())
                    .setRedirectInpuFile(getRedirectInputFile())
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

    enum CmdKind {
        PATH,
        ID,
        KEYWORD,
    }
}
