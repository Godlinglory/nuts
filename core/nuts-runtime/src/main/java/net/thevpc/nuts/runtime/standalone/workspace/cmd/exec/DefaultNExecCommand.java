package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCmdLineUtils;
import net.thevpc.nuts.runtime.standalone.executor.NExecutionContextUtils;
import net.thevpc.nuts.runtime.standalone.executor.system.NSysExecUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutableInformationExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.executor.ArtifactExecutorComponent;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle.DefaultNBundleInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy.DefaultNDeployInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch.DefaultNFetchInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.help.DefaultNHelpInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.info.DefaultNInfoInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.install.DefaultNInstallInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.install.DefaultNReinstallInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.license.DefaultNLicenseInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.prepare.DefaultNPrepareInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.push.DefaultNPushInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.search.DefaultNSearchInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias.DefaultNAliasExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.DefaultNSettingsInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.undeploy.DefaultNUndeployInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall.DefaultNUninstallInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.update.DefaultNCheckUpdatesInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.update.DefaultNUpdateInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.version.DefaultNVersionInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.welcome.DefaultNWelcomeInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.which.DefaultNWhichInternalExecutable;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.spi.NExecutorComponent;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NConnexionString;
import net.thevpc.nuts.util.NStream;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNExecCommand extends AbstractNExecCommand {

    public DefaultNExecCommand(NSession session) {
        super(session);
    }


    @Override
    public NExecutableInformation which() {
        checkSession();

        NSession traceSession = getSession();
        String target = getTarget();
        if (!NBlankable.isBlank(target)) {
            NConnexionString connexionString = NConnexionString.of(target).get();
            if ("ssh".equals(connexionString.getProtocol())) {
                NExtensions.of(session)
                        .loadExtension(NId.of("net.thevpc.nuts.ext:next-ssh").get());
            }
            NExecCommandExtension ee = NExtensions.of(session).createComponent(NExecCommandExtension.class, connexionString)
                    .orElseThrow(()->new NIllegalArgumentException(session, NMsg.ofC("invalid execution target string : %s",target)))
                    ;
            return whichOnTarget(ee, connexionString);
        }

        NExecutableInformationExt exec = null;
        NExecutionType executionType = this.getExecutionType();
        NRunAs runAs = this.getRunAs();
        if (executionType == null) {
            executionType = session.getExecutionType();
        }
        if (executionType == null) {
            executionType = NExecutionType.SPAWN;
        }
        switch (executionType) {
            case OPEN: {
                NAssert.requireNonNull(commandDefinition, "artifact definition", session);
                NAssert.requireNonBlank(command, "command", session);
                String[] ts = command.toArray(new String[0]);
                exec = new DefaultNOpenExecutable(ts, getExecutorOptions().toArray(new String[0]), this);
                break;
            }
            case SYSTEM: {
                NExecutionType finalExecutionType = executionType;
                NAssert.requireNull(commandDefinition, () -> NMsg.ofC("unable to run artifact as %s cmd", finalExecutionType), session);
                NAssert.requireNonBlank(command, "command", session);
                String[] ts = command.toArray(new String[0]);
                List<String> tsl = new ArrayList<>(Arrays.asList(ts));
                if (CoreStringUtils.firstIndexOf(ts[0], new char[]{'/', '\\'}) < 0) {
                    Path p = NSysExecUtils.sysWhich(ts[0]);
                    if (p != null) {
                        tsl.set(0, p.toString());
                    }
                }
                exec = new DefaultNSystemExecutable(tsl.toArray(new String[0]),
                        getExecutorOptions(),
                        this
                );
                break;
            }
            case SPAWN:
            case EMBEDDED: {
                if (commandDefinition != null) {
                    String[] ts = command == null ? new String[0] : command.toArray(new String[0]);
                    return ws_execDef(commandDefinition, commandDefinition.getId().getLongName(), ts, getExecutorOptions(), workspaceOptions, env, directory, failFast,
                            executionType, runAs);
                } else {
                    NAssert.requireNonBlank(command, "command", session);
                    String[] ts = command.toArray(new String[0]);
                    exec = execEmbeddedOrExternal(ts, getExecutorOptions(), getWorkspaceOptions(), traceSession);
                }
                break;
            }
            default: {
                throw new NUnsupportedArgumentException(getSession(), NMsg.ofC("invalid execution type %s", executionType));
            }
        }
        return exec;
    }

    public NExecutableInformation whichOnTarget(NExecCommandExtension commExec, NConnexionString connexionString) {
        checkSession();
        NExecInput in0 = CoreIOUtils.validateIn(in, session);
        NExecOutput out0 = CoreIOUtils.validateOut(out, session);
        NExecOutput err0 = CoreIOUtils.validateOut(err, session);
        NExecutableInformationExt exec = null;
        NExecutionType executionType = this.getExecutionType();
        if (executionType == null) {
            executionType = session.getExecutionType();
        }
        if (executionType == null) {
            executionType = NExecutionType.SPAWN;
        }
        switch (executionType) {
            case OPEN: {
                throw new NUnsupportedArgumentException(getSession(), NMsg.ofC("invalid open execution type %s on host %s", connexionString));
            }
            case SYSTEM: {
                NExecutionType finalExecutionType = executionType;
                NAssert.requireNull(commandDefinition, () -> NMsg.ofC("unable to run artifact as %s cmd", finalExecutionType), session);
                NAssert.requireNonBlank(command, "command", session);
                String[] ts = command.toArray(new String[0]);
                return new DefaultNSystemExecutableRemote(
                        commExec, ts,
                        getExecutorOptions(),
                        this,
                        in0,
                        out0,
                        err0
                );
            }
            case SPAWN: {
                if (commandDefinition != null) {
                    String[] ts = command == null ? new String[0] : command.toArray(new String[0]);
                    return new DefaultSpawnExecutableRemote(commExec, commandDefinition, ts, getExecutorOptions(), this, in0, out0, err0);
                } else {
                    NAssert.requireNonBlank(command, "command", session);
                    List<String> ts = new ArrayList<>(command);
                    if (ts.size() == 0) {
                        throw new NUnsupportedArgumentException(getSession(), NMsg.ofPlain("missing command"));
                    }
                    String id = ts.get(0);
                    ts.remove(0);
                    NDefinition def2 = NSearchCommand.of(getSession())
                            .addId(id)
                            .setContent(true)
                            .setLatest(true)
                            .setDependencies(true)
                            .setDependencyFilter(NDependencyFilters.of(session).byRunnable())
                            .setFailFast(true)
                            .setEffective(true)
                            .getResultDefinitions()
                            .findFirst().get();
                    return new DefaultSpawnExecutableRemote(commExec, def2, ts.toArray(new String[0]), getExecutorOptions(), this, in0, out0, err0);
                }
            }
            case EMBEDDED: {
                throw new NUnsupportedArgumentException(getSession(), NMsg.ofC("invalid embedded execution type %s on host %s", connexionString));
            }
            default: {
                throw new NUnsupportedArgumentException(getSession(), NMsg.ofC("invalid execution type %s on host %s", executionType, connexionString));
            }
        }
    }

    @Override
    public NExecCommand run() {
        checkSession();
        NExecutableInformationExt exec = (NExecutableInformationExt) which();
        executed = true;
        int exitCode = NExecutionException.SUCCESS;
        try {
            exitCode = exec.execute();
        } catch (NExecutionException ex) {
            String p = getExtraErrorMessage();
            if (p != null) {
                resultException = new NExecutionException(getSession(),
                        NMsg.ofC("execution failed with code %s and message : %s", ex.getExitCode(), p),
                        ex, ex.getExitCode());
            } else {
                resultException = ex;
            }
        } catch (Exception ex) {
            String p = getExtraErrorMessage();
            int exceptionExitCode = NExceptionWithExitCodeBase.resolveExitCode(ex).orElse(NExecutionException.ERROR_255);
            if (exceptionExitCode != NExecutionException.SUCCESS) {
                if (!NBlankable.isBlank(p)) {
                    resultException = new NExecutionException(getSession(),
                            NMsg.ofC("execution of (%s) failed with code %s ; error was : %s ; notes : %s", exec, exceptionExitCode, ex, p),
                            ex, exceptionExitCode);
                } else {
                    resultException = new NExecutionException(getSession(),
                            NMsg.ofC("execution of (%s) failed with code %s ; error was : %s", exec, exceptionExitCode, ex),
                            ex, exceptionExitCode);
                }
            }
        }
        if (resultException == null) {
            if (exitCode != NExecutionException.SUCCESS) {
                resultException = new NExecutionException(getSession(),
                        NMsg.ofC("execution of (%s) failed with code %s", exec, exitCode),
                        exitCode);
            }
        }
        if (resultException != null && resultException.getExitCode() != NExecutionException.SUCCESS && failFast) {
            throw resultException;
//            checkFailFast(result.getExitCode());
        }
        return this;
    }

    private NExecutableInformationExt execEmbeddedOrExternal(String[] cmd, List<String> executorOptions, List<String> workspaceOptions, NSession prepareSession) {
        NAssert.requireNonBlank(cmd, "command", session);
        String[] args = new String[cmd.length - 1];
        System.arraycopy(cmd, 1, args, 0, args.length);
        String cmdName = cmd[0];
        //resolve internal commands!
        NExecutionType executionType = getExecutionType();
        if (executionType == null) {
            executionType = session.getExecutionType();
        }
        if (executionType == null) {
            executionType = NExecutionType.SPAWN;
        }
        NRunAs runAs = getRunAs();
        CmdKind cmdKind = null;
        NId goodId = null;
        String goodKw = null;
        boolean forceInstalled = false;
        if (cmdName.endsWith("!")) {
            goodId = NId.of(cmdName.substring(0, cmdName.length() - 1)).orNull();
            if (goodId != null) {
                forceInstalled = true;
            }
        } else {
            goodId = NId.of(cmdName).orNull();
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
                throw new NNotFoundException(getSession(), null, NMsg.ofC("unable to resolve id %s", cmdName));
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
                return new DefaultNArtifactPathExecutable(cmdName, args, executorOptions, workspaceOptions, executionType, runAs, this);
            }
            case ID: {
                NId idToExec = findExecId(goodId, prepareSession, forceInstalled, true);
                if (idToExec != null) {
                    return ws_execId(idToExec, cmdName, args, executorOptions, workspaceOptions, executionType, runAs);
                } else {
                    throw new NNotFoundException(getSession(), goodId);
                }
            }
            case KEYWORD: {
                switch (goodKw) {
                    case "update": {
                        return new DefaultNUpdateInternalExecutable(args, this);
                    }
                    case "check-updates": {
                        return new DefaultNCheckUpdatesInternalExecutable(args, this);
                    }
                    case "install": {
                        return new DefaultNInstallInternalExecutable(args, this);
                    }
                    case "reinstall": {
                        return new DefaultNReinstallInternalExecutable(args, this);
                    }
                    case "uninstall": {
                        return new DefaultNUninstallInternalExecutable(args, this);
                    }
                    case "deploy": {
                        return new DefaultNDeployInternalExecutable(args, this);
                    }
                    case "undeploy": {
                        return new DefaultNUndeployInternalExecutable(args, this);
                    }
                    case "push": {
                        return new DefaultNPushInternalExecutable(args, this);
                    }
                    case "fetch": {
                        return new DefaultNFetchInternalExecutable(args, this);
                    }
                    case "search": {
                        return new DefaultNSearchInternalExecutable(args, this);
                    }
                    case "version": {
                        return new DefaultNVersionInternalExecutable(args, this);
                    }
                    case "prepare": {
                        return new DefaultNPrepareInternalExecutable(args, this);
                    }
                    case "license": {
                        return new DefaultNLicenseInternalExecutable(args, this);
                    }
                    case "bundle": {
                        return new DefaultNBundleInternalExecutable(args, this);
                    }
                    case "help": {
                        return new DefaultNHelpInternalExecutable(args, this);
                    }
                    case "welcome": {
                        return new DefaultNWelcomeInternalExecutable(args, this);
                    }
                    case "info": {
                        return new DefaultNInfoInternalExecutable(args, this);
                    }
                    case "which": {
                        return new DefaultNWhichInternalExecutable(args, this);
                    }
                    case "exec": {
                        return new DefaultNExecInternalExecutable(args, this);
                    }
                    case "settings": {
                        return new DefaultNSettingsInternalExecutable(args, this);
                    }
                }
                NCustomCommand command = null;
                command = NCommands.of(prepareSession).findCommand(goodKw);
                if (command != null) {
                    NCommandExecOptions o = new NCommandExecOptions().setExecutorOptions(executorOptions).setDirectory(directory).setFailFast(failFast)
                            .setExecutionType(executionType).setEnv(env);
                    return new DefaultNAliasExecutable(command, o, args, this);
                } else {
                    NId idToExec = null;
                    if (goodId != null) {
                        idToExec = findExecId(goodId, prepareSession, forceInstalled, true);
                    }
                    if (idToExec == null) {
                        Path sw = NSysExecUtils.sysWhich(cmdName);
                        if (sw != null) {
                            List<String> cmdArr = new ArrayList<>();
                            cmdArr.add(sw.toString());
                            cmdArr.addAll(Arrays.asList(args));
                            return new DefaultNSystemExecutable(cmdArr.toArray(new String[0]), executorOptions, this);
                        }
                        List<String> cmdArr = new ArrayList<>();
                        cmdArr.add(cmdName);
                        cmdArr.addAll(Arrays.asList(args));
                        return new DefaultUnknownExecutable(cmdArr.toArray(new String[0]), this);
                    }
                    return ws_execId(idToExec, cmdName, args, executorOptions, workspaceOptions, executionType, runAs);
                }
            }
        }
        throw new NNotFoundException(getSession(), goodId, NMsg.ofC("unable to resolve id %s", cmdName));
    }

    protected NId findExecId(NId nid, NSession traceSession, boolean forceInstalled, boolean ignoreIfUserCommand) {
        NWorkspace ws = traceSession.getWorkspace();
        if (nid == null) {
            return null;
        }
        NId ff = NSearchCommand.of(traceSession).addId(nid).setOptional(false).setLatest(true).setFailFast(false)
                .setInstallStatus(NInstallStatusFilters.of(session).byDeployed(true))
                .getResultDefinitions().stream()
                .sorted(Comparator.comparing(x -> !x.getInstallInformation().get(session).isDefaultVersion())) // default first
                .map(NDefinition::getId).findFirst().orElse(null);
        if (ff == null) {
            if (!forceInstalled) {
                if (ignoreIfUserCommand && isUserCommand(nid.toString())) {
                    return null;
                }
                //now search online
                // this helps recover from "invalid default parseVersion" issue
                if (traceSession.isPlainTrace()) {
                    traceSession.out().resetLine().println(NMsg.ofC("%s is %s, will search for it online. Type ```error CTRL^C``` to stop...",
                            nid,
                            NTexts.of(session).ofStyled("not installed", NTextStyle.error())
                    ));
                    traceSession.out().flush();
                }
                ff = NSearchCommand.of(traceSession).addId(nid).setSession(traceSession.copy().setFetchStrategy(NFetchStrategy.ONLINE))
                        .setOptional(false).setFailFast(false)
                        .setLatest(true)
                        //                        .configure(true,"--trace-monitor")
                        .getResultIds().findFirst().orElse(null);
            }
        }
        if (ff == null) {
            return null;
        } else {
            return ff;
        }
    }

    public boolean isUserCommand(String s) {
        checkSession();
        NSession session = getSession();
        String p = System.getenv().get("PATH");
        if (p != null) {
            char r = File.pathSeparatorChar;
            for (String z : p.split("" + r)) {
                Path t = Paths.get(z);
                switch (NEnvs.of(session).getOsFamily()) {
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

    protected NExecutableInformationExt ws_execId(NId goodId, String commandName, String[] appArgs, List<String> executorOptions,
                                                  List<String> workspaceOptions, NExecutionType executionType, NRunAs runAs) {
        NDefinition def = NFetchCommand.of(goodId, session)
                .setDependencies(true)
                .setFailFast(true)
                .setEffective(true)
                .setContent(true)
                //
                .setOptional(false)
                .addScope(NDependencyScopePattern.RUN)
                .setDependencyFilter(NDependencyFilters.of(session).byRunnable())
                //
                .getResultDefinition();
        return ws_execDef(def, commandName, appArgs, executorOptions, this.workspaceOptions, env, directory, failFast, executionType, runAs);
    }

    protected NExecutableInformationExt ws_execDef(NDefinition def, String commandName, String[] appArgs, List<String> executorOptions, List<String> workspaceOptions, Map<String, String> env, NPath dir, boolean failFast, NExecutionType executionType, NRunAs runAs) {
        return new DefaultNArtifactExecutable(def, commandName, appArgs, executorOptions, workspaceOptions, env, dir, failFast, executionType, runAs, this);
    }

    public int ws_execId(NDefinition def, String commandName, String[] appArgs,
                         List<String> executorOptions,
                         List<String> workspaceOptions, Map<String, String> env, NPath dir, boolean failFast, boolean temporary,
                         NSession session,
                         NExecInput in,
                         NExecOutput out,
                         NExecOutput err,
                         NExecutionType executionType,
                         NRunAs runAs
    ) {
        //TODO ! one of the sessions needs to be removed!
        NSessionUtils.checkSession(ws, session);
        checkSession();
        NWorkspace ws = getSession().getWorkspace();
        NWorkspaceSecurityManager.of(session).checkAllowed(NConstants.Permissions.EXEC, commandName);
        if (def != null && def.getContent().isPresent()) {
            NDescriptor descriptor = def.getDescriptor();
            if (!descriptor.isExecutable()) {
//                session.getTerminal().getErr().println(nutToRun.getId()+" is not executable... will perform extra checks.");
//                throw new NutsNotExecutableException(descriptor.getId());
            }
            NArtifactCall executorCall = descriptor.getExecutor();
            NExecutorComponent execComponent = null;

            List<String> executorArgs = new ArrayList<>();

            if (executorCall != null) {
                NId eid = executorCall.getId();
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
                        NStream<NDefinition> q = NSearchCommand.of(getSession()).addId(eid).setLatest(true)
                                .setDistinct(true)
                                .getResultDefinitions();
                        NDefinition[] availableExecutors = q.stream().limit(2).toArray(NDefinition[]::new);
                        if (availableExecutors.length > 1) {
                            throw new NTooManyElementsException(this.session, NMsg.ofC("too many results for executor %s", eid));
                        } else if (availableExecutors.length == 1) {
                            execComponent = new ArtifactExecutorComponent(availableExecutors[0].getId(), this.session);
                        } else {
                            // availableExecutors.length=0;
                            throw new NNotFoundException(this.session, eid, NMsg.ofC("executor not found %s", eid));
                        }
                    }
                }
            }
            if (execComponent == null) {
                execComponent = getSession().extensions().createComponent(NExecutorComponent.class, def).get();
            }
            if (executorCall != null) {
                for (String argument : executorCall.getArguments()) {
                    executorArgs.add(StringPlaceHolderParser.replaceDollarPlaceHolders(argument,
                            def, session, NExecutionContextUtils.DEFINITION_PLACEHOLDER
                    ));
                }
            }
            NCmdLineUtils.OptionsAndArgs optionsAndArgs = NCmdLineUtils.parseOptionsFirst(executorArgs.toArray(new String[0]));

            executorArgs.clear();
            executorArgs.addAll(Arrays.asList(optionsAndArgs.getOptions()));
            executorArgs.addAll(executorOptions);
            executorArgs.addAll(Arrays.asList(optionsAndArgs.getArgs()));

            NExecutionContextBuilder ecb = NWorkspaceExt.of(ws).createExecutionContext();
            NExecutionContext executionContext = ecb
                    .setDefinition(def)
                    .setArguments(appArgs)
                    .setExecutorOptions(executorArgs.toArray(new String[0]))
                    .setWorkspaceOptions(workspaceOptions)
                    .setEnv(env)
                    .setDirectory(dir)
                    .setWorkspace(session.getWorkspace())
                    .setSession(session)
                    .setFailFast(failFast)
                    .setTemporary(temporary)
                    .setExecutionType(executionType)
                    .setRunAs(runAs)
                    .setCommandName(commandName)
                    .setSleepMillis(getSleepMillis())
                    .setIn(in)
                    .setOut(out)
                    .setErr(err)
                    .build();
            return execComponent.exec(executionContext);
        }
        throw new NNotFoundException(getSession(), def == null ? null : def.getId());
    }

    enum CmdKind {
        PATH,
        ID,
        KEYWORD,
    }
}
