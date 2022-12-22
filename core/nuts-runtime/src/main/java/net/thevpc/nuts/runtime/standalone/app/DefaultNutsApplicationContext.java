package net.thevpc.nuts.runtime.standalone.app;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NutsCommandLineUtils;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsConfigurableHelper;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.text.NutsText;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTextTransformConfig;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsClock;
import net.thevpc.nuts.util.NutsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DefaultNutsApplicationContext implements NutsApplicationContext {

    private final Class appClass;
    private final NutsPath[] folders = new NutsPath[NutsStoreLocation.values().length];
    private final NutsPath[] sharedFolders = new NutsPath[NutsStoreLocation.values().length];
    /**
     * auto complete info for "auto-complete" mode
     */
    private final NutsCommandAutoComplete autoComplete;
    private NutsWorkspace workspace;
    private NutsSession session;
    private NutsId appId;
    private NutsClock startTime;
    private List<String> args;
    private NutsApplicationMode mode = NutsApplicationMode.RUN;
    private NutsAppStoreLocationResolver storeLocationResolver;
    /**
     * previous parse for "update" mode
     */
    private NutsVersion appPreviousVersion;
    private List<String> modeArgs = new ArrayList<>();

    //    public DefaultNutsApplicationContext(String[] args, NutsWorkspace workspace, Class appClass, String storeId, long startTimeMillis) {
//        this(workspace,
//                args,
//                appClass,
//                storeId,
//                startTimeMillis
//        );
//    }
    public DefaultNutsApplicationContext(NutsWorkspace workspace, NutsSession session, List<String> args, Class appClass, String storeId, NutsClock startTime) {
        this.startTime = startTime == null ? NutsClock.now() : startTime;
        if (workspace == null && session == null) {
            NutsUtils.requireSession(session);
        } else if (workspace != null) {
            if (session == null) {
                this.session = workspace.createSession();
            } else {
                NutsSessionUtils.checkSession(workspace, session);
                this.session = session.copy();
            }
            this.workspace = this.session.getWorkspace();
        } else {
            this.session = session;
            this.workspace = session.getWorkspace(); //get a worspace session aware!
        }
        session = this.session;//will be used later
        int wordIndex = -1;
        if (args.size() > 0 && args.get(0).startsWith("--nuts-exec-mode=")) {
            NutsCommandLine execModeCommand = NutsCommandLine.parseDefault(
                    args.get(0).substring(args.get(0).indexOf('=') + 1)).get(session);
            if (execModeCommand.hasNext()) {
                NutsArgument a = execModeCommand.next().get(session);
                switch (a.key()) {
                    case "auto-complete": {
                        mode = NutsApplicationMode.AUTO_COMPLETE;
                        if (execModeCommand.hasNext()) {
                            wordIndex = execModeCommand.next().get(session).asInt().get(session);
                        }
                        modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "install": {
                        mode = NutsApplicationMode.INSTALL;
                        modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "uninstall": {
                        mode = NutsApplicationMode.UNINSTALL;
                        modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "update": {
                        mode = NutsApplicationMode.UPDATE;
                        if (execModeCommand.hasNext()) {
                            appPreviousVersion = NutsVersion.of(execModeCommand.next().flatMap(NutsValue::asString).get(session)).get(session);
                        }
                        modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    default: {
                        throw new NutsExecutionException(session, NutsMessage.ofCstyle("Unsupported nuts-exec-mode : %s", args.get(0)), 205);
                    }
                }
            }
            args = args.subList(1, args.size());
        }
        NutsId _appId = (NutsId) NutsApplications.getSharedMap().get("nuts.embedded.application.id");
        if (_appId != null) {
            //("=== Inherited "+_appId);
        } else {
            _appId = NutsIdResolver.of(session).resolveId(appClass);
        }
        if (_appId == null) {
            throw new NutsExecutionException(session, NutsMessage.ofCstyle("invalid Nuts Application (%s). Id cannot be resolved", appClass.getName()), 203);
        }
        this.args = (args);
        this.appId = (_appId);
        this.appClass = appClass == null ? null : JavaClassUtils.unwrapCGLib(appClass);
        //always copy the session to bind to appId
        this.session.setAppId(appId);
//        NutsWorkspaceConfigManager cfg = workspace.config();
        NutsWorkspaceLocationManager locations = session.locations();
        for (NutsStoreLocation folder : NutsStoreLocation.values()) {
            setFolder(folder, locations.getStoreLocation(this.appId, folder));
            setSharedFolder(folder, locations.getStoreLocation(this.appId.builder().setVersion("SHARED").build(), folder));
        }
        if (mode == NutsApplicationMode.AUTO_COMPLETE) {
            //TODO fix me
//            this.workspace.term().setSession(session).getSystemTerminal()
//                    .setMode(NutsTerminalMode.FILTERED);
            if (wordIndex < 0) {
                wordIndex = args.size();
            }
            autoComplete = new AppCommandAutoComplete(this.session, args, wordIndex, getSession().out());
        } else {
            autoComplete = null;
        }
    }

    @Override
    public NutsApplicationMode getMode() {
        return mode;
    }

    @Override
    public List<String> getModeArguments() {
        return modeArgs;
    }

    @Override
    public NutsCommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public final NutsApplicationContext configure(boolean skipUnsupported, String... args) {
        NutsId appId = getAppId();
        String appName = appId == null ? "app" : appId.getArtifactId();
        return NutsConfigurableHelper.configure(this, getSession(), skipUnsupported, args, appName);
    }

    @Override
    public void configureLast(NutsCommandLine commandLine) {
        if (!configureFirst(commandLine)) {
            commandLine.throwUnexpectedArgument();
        }
    }

    @Override
    public void printHelp() {
        NutsText h = NutsWorkspaceExt.of(getWorkspace()).resolveDefaultHelp(getAppClass(), session);
        h = NutsTexts.of(session).transform(h, new NutsTextTransformConfig()
                .setProcessTitleNumbers(true)
                .setNormalize(true)
                .setFlatten(true)
        );
        if (h == null) {
            getSession().out().printlnf("Help is %s.", NutsTexts.of(getSession()).ofStyled("missing", NutsTextStyle.error()));
        } else {
            getSession().out().println(h);
        }
        //need flush if the help is syntactically incorrect
        getSession().out().flush();
    }

    @Override
    public Class getAppClass() {
        return appClass;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsSession createSession() {
        return getSession().getWorkspace().createSession();
    }

    @Override
    public NutsApplicationContext setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(workspace, session);
        return this;
    }

    @Override
    public NutsPath getAppsFolder() {
        return getFolder(NutsStoreLocation.APPS);
    }

    @Override
    public NutsPath getConfigFolder() {
        return getFolder(NutsStoreLocation.CONFIG);
    }

    @Override
    public NutsPath getLogFolder() {
        return getFolder(NutsStoreLocation.LOG);
    }

    @Override
    public NutsPath getTempFolder() {
        return getFolder(NutsStoreLocation.TEMP);
    }

    @Override
    public NutsPath getVarFolder() {
        return getFolder(NutsStoreLocation.VAR);
    }

    @Override
    public NutsPath getLibFolder() {
        return getFolder(NutsStoreLocation.LIB);
    }

    @Override
    public NutsPath getRunFolder() {
        return getFolder(NutsStoreLocation.RUN);
    }

    @Override
    public NutsPath getCacheFolder() {
        return getFolder(NutsStoreLocation.CACHE);
    }

    @Override
    public NutsPath getVersionFolder(NutsStoreLocation location, String version) {
        if (version == null
                || version.isEmpty()
                || version.equalsIgnoreCase("current")
                || version.equals(getAppId().getVersion().getValue())) {
            return getFolder(location);
        }
        NutsId newId = this.getAppId().builder().setVersion(version).build();
        if (storeLocationResolver != null) {
            NutsPath r = storeLocationResolver.getStoreLocation(newId, location);
            if (r != null) {
                return r;
            }
        }
        return session.locations().getStoreLocation(newId, location);
    }

    @Override
    public NutsPath getSharedAppsFolder() {
        return getSharedFolder(NutsStoreLocation.APPS);
    }

    @Override
    public NutsPath getSharedConfigFolder() {
        return getSharedFolder(NutsStoreLocation.CONFIG);
    }

    @Override
    public NutsPath getSharedLogFolder() {
        return getSharedFolder(NutsStoreLocation.LOG);
    }

    @Override
    public NutsPath getSharedTempFolder() {
        return getSharedFolder(NutsStoreLocation.TEMP);
    }

    @Override
    public NutsPath getSharedVarFolder() {
        return getSharedFolder(NutsStoreLocation.VAR);
    }

    @Override
    public NutsPath getSharedLibFolder() {
        return getSharedFolder(NutsStoreLocation.LIB);
    }

    @Override
    public NutsPath getSharedRunFolder() {
        return getSharedFolder(NutsStoreLocation.RUN);
    }

    @Override
    public NutsPath getSharedFolder(NutsStoreLocation location) {
        return sharedFolders[location.ordinal()];
    }

    @Override
    public NutsId getAppId() {
        return appId;
    }

    @Override
    public NutsVersion getAppVersion() {
        return appId == null ? null : appId.getVersion();
    }

    @Override
    public List<String> getArguments() {
        return args;
    }

    @Override
    public NutsClock getStartTime() {
        return startTime;
    }

    @Override
    public NutsVersion getAppPreviousVersion() {
        return appPreviousVersion;
    }

    @Override
    public NutsCommandLine getCommandLine() {
        return NutsCommandLine.of(getArguments())
                .setCommandName(getAppId().getArtifactId())
                .setAutoComplete(getAutoComplete())
                .setSession(getSession());
    }

    @Override
    public void processCommandLine(NutsCommandLineProcessor commandLineProcessor) {
        getCommandLine().process(commandLineProcessor, new AppContextNutsCommandLineContext(this));
    }

    @Override
    public NutsPath getFolder(NutsStoreLocation location) {
        return folders[location.ordinal()];
    }

    @Override
    public boolean isExecMode() {
        return getAutoComplete() == null;
    }

    @Override
    public NutsAppStoreLocationResolver getStoreLocationResolver() {
        return storeLocationResolver;
    }

    @Override
    public NutsApplicationContext setAppVersionStoreLocationSupplier(NutsAppStoreLocationResolver appVersionStoreLocationSupplier) {
        this.storeLocationResolver = appVersionStoreLocationSupplier;
        return this;
    }

    public NutsApplicationContext setMode(NutsApplicationMode mode) {
        this.mode = mode;
        return this;
    }

    public NutsApplicationContext setModeArgs(List<String> modeArgs) {
        this.modeArgs = modeArgs;
        return this;
    }

    /**
     * configure the current command with the given arguments.
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     *                        silently
     * @param commandLine     arguments to configure with
     * @return {@code this} instance
     */
    @Override
    public final boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        return NutsConfigurableHelper.configure(this, getSession(), skipUnsupported, commandLine);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek().orNull();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch (a.key()) {
            case "-?":
            case "-h":
            case "--help": {
                cmd.skip();
                if (enabled) {
                    if (cmd.isExecMode()) {
                        printHelp();
                    }
                    cmd.skipAll();
                    throw new NutsExecutionException(session, NutsMessage.ofPlain("help"), 0);
                }
                break;
            }
            case "--skip-event": {
                switch (getMode()) {
                    case INSTALL:
                    case UNINSTALL:
                    case UPDATE: {
                        if (enabled) {
                            cmd.skip();
                            throw new NutsExecutionException(session, NutsMessage.ofPlain("skip-event"), 0);
                        }
                    }
                }
                return true;
            }
            case "--version": {
                cmd.skip();
                if (enabled) {
                    if (cmd.isExecMode()) {
                        getSession().out().printf("%s%n", NutsIdResolver.of(session).resolveId(getClass()).getVersion().toString());
                        cmd.skipAll();
                    }
                    throw new NutsExecutionException(session, NutsMessage.ofPlain("version"), 0);
                }
                return true;
            }
            default: {
                if (getSession() != null && getSession().configureFirst(cmd)) {
                    return true;
                }
            }
        }
        return false;
    }

    public NutsApplicationContext setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
        return this;
    }

    public NutsApplicationContext setFolder(NutsStoreLocation location, NutsPath folder) {
        this.folders[location.ordinal()] = folder;
        return this;
    }

    public NutsApplicationContext setSharedFolder(NutsStoreLocation location, NutsPath folder) {
        this.sharedFolders[location.ordinal()] = folder;
        return this;
    }

    //    @Override
    public NutsApplicationContext setAppId(NutsId appId) {
        this.appId = appId;
        return this;
    }

    //    @Override
    public NutsApplicationContext setArguments(List<String> args) {
        this.args = args;
        return this;
    }

    public NutsApplicationContext setArguments(String[] args) {
        this.args = new ArrayList<>(Arrays.asList(args));
        return this;
    }

    public NutsApplicationContext setStartTime(NutsClock startTime) {
        this.startTime = startTime;
        return this;
    }

    public NutsApplicationContext setAppPreviousVersion(NutsVersion previousVersion) {
        this.appPreviousVersion = previousVersion;
        return this;
    }

    private static class AppCommandAutoComplete extends NutsCommandAutoCompleteBase {

        private final ArrayList<String> words;
        private final NutsPrintStream out0;
        private final NutsSession session;
        private final int wordIndex;

        public AppCommandAutoComplete(NutsSession session, List<String> args, int wordIndex, NutsPrintStream out0) {
            this.session = session;
            words = new ArrayList<>(args);
            this.wordIndex = wordIndex;
            this.out0 = out0;
        }

        @Override
        public NutsSession getSession() {
            return session;
        }

        @Override
        public String getLine() {
            return NutsCommandLine.of(getWords()).toString();
        }

        @Override
        public List<String> getWords() {
            return words;
        }

        @Override
        public int getCurrentWordIndex() {
            return wordIndex;
        }

        @Override
        protected NutsArgumentCandidate addCandidatesImpl(NutsArgumentCandidate value) {
            NutsArgumentCandidate c = super.addCandidatesImpl(value);
            String v = value.getValue();
            if (v == null) {
                throw new NutsExecutionException(session, NutsMessage.ofPlain("candidate cannot be null"), 2);
            }
            String d = value.getDisplay();
            if (Objects.equals(v, d) || d == null) {
                out0.printf("%s%n", AUTO_COMPLETE_CANDIDATE_PREFIX + NutsCommandLineUtils.escapeArgument(v));
            } else {
                out0.printf("%s%n", AUTO_COMPLETE_CANDIDATE_PREFIX + NutsCommandLineUtils.escapeArgument(v) + " " + NutsCommandLineUtils.escapeArgument(d));
            }
            return c;
        }
    }

    private static class AppContextNutsCommandLineContext implements NutsCommandLineContext {
        private NutsApplicationContext context;

        public AppContextNutsCommandLineContext(NutsApplicationContext context) {
            this.context = context;
        }

        @Override
        public Object configure(boolean skipUnsupported, String... args) {
            return this.context.configure(skipUnsupported, args);
        }

        @Override
        public boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
            return this.context.configure(skipUnsupported, commandLine);
        }

        @Override
        public boolean configureFirst(NutsCommandLine commandLine) {
            return this.context.configureFirst(commandLine);
        }

        @Override
        public void configureLast(NutsCommandLine commandLine) {
            this.context.configureLast(commandLine);
        }
    }
}
