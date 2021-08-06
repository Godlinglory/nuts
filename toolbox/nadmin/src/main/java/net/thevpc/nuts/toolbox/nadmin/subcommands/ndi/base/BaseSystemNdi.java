package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.*;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util.NdiUtils;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util.ReplaceString;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class BaseSystemNdi extends AbstractSystemNdi {
    public static final ReplaceString COMMENT_LINE_CONFIG_HEADER = new ReplaceString("net.thevpc.nuts configuration", "((net[.]thevpc[.]nuts)|(net[.]thevpc[.]nuts.toolbox[.]ndi)|(net[.]vpc[.]app[.]nuts)) configuration");

    private static final Logger LOG = Logger.getLogger(BaseSystemNdi.class.getName());
    private static final Set<String> ACCEPTED_ICON_EXTENSIONS = new HashSet<>(Arrays.asList("png", "gif", "jpg", "jpeg", "svg"));

    public BaseSystemNdi(NutsApplicationContext appContext) {
        super(appContext);
    }

    public abstract NdiScriptInfo getSysRC(NutsEnvInfo env);

    public NdiScriptInfo getNutsInit(NutsEnvInfo env) {
        return new NdiScriptInfo() {
            @Override
            public Path path() {
                return env.getIncFolder().resolve(getExecFileName(".nuts-init"));
            }

            @Override
            public PathInfo create() {
                Path apiConfigFile = path();
                return addFileLine(NdiScriptInfoType.NUTS_INIT,
                        env.getNutsApiId(),
                        apiConfigFile, getCommentLineConfigHeader(),
                        getCallScriptCommand(getNutsEnv(env).path().toString()) + newlineString() +
                                createNutsEnvString(env, false, true),
                        getShebanSh());
            }
        };
    }

    public NdiScriptInfo getNutsTermInit(NutsEnvInfo env) {
        return new NdiScriptInfo() {
            @Override
            public Path path() {
                return env.getIncFolder().resolve(getExecFileName(".nuts-term-init"));
            }

            @Override
            public PathInfo create() {
                return scriptBuilderTemplate("nuts-term-init", NdiScriptInfoType.NUTS_TERM, env.getNutsApiId(), env)
                        .setPath(path())
                        .build();
            }
        };
    }

    public FromTemplateScriptBuilder scriptBuilderTemplate(String templateName, NdiScriptInfoType type, NutsId anyId, NutsEnvInfo env) {
        return ScriptBuilder.fromTemplate(templateName, type, anyId, BaseSystemNdi.this, env);
    }

    public SimpleScriptBuilder scriptBuilderSimple(NdiScriptInfoType type, NutsId anyId, NutsEnvInfo env) {
        return ScriptBuilder.simple(type, anyId, BaseSystemNdi.this)/*,env*/;
    }

    public NdiScriptInfo getNutsTerm(NutsEnvInfo env) {
        return new NdiScriptInfo() {
            @Override
            public Path path() {
                return env.getBinFolder().resolve(getExecFileName("nuts-term"));
            }

            @Override
            public PathInfo create() {
                return scriptBuilderTemplate("nuts-term", NdiScriptInfoType.NUTS_TERM, env.getNutsApiId(), env)
                        .setPath(path())
                        .build();
            }
        };
    }


    public NdiScriptInfo getNutsEnv(NutsEnvInfo env) {
        return new NdiScriptInfo() {
            @Override
            public Path path() {
                return env.getIncFolder().resolve(getExecFileName(".nuts-env"));
            }

            @Override
            public PathInfo create() {
                return scriptBuilderTemplate("body", NdiScriptInfoType.NUTS_ENV, env.getNutsApiId(), env)
                        .setPath(path())
                        .println(createNutsEnvString(env, true, false))
                        .build();
            }
        };
    }

    public NdiScriptInfo getNutsStart(NutsEnvInfo env) {
        return new NdiScriptInfo() {
            @Override
            public Path path() {
                return env.getBinFolder().resolve(getExecFileName("nuts"));
            }

            @Override
            public PathInfo create() {
                return null;
            }
        };
    }


    //ws.getApiId().getVersion()


    public Path getScriptFile(String name, NutsEnvInfo env) {
        if (name.indexOf('/') >= 0) {
            return Paths.get(name).toAbsolutePath();
        }
        return env.getBinFolder().resolve(getExecFileName(name)).toAbsolutePath();
//        Path bin =
//                Paths.get(context.getAppsFolder());
//        return bin.resolve(getExecFileName(name)).toAbsolutePath();
    }

    public abstract boolean isComments(String line);

    //    @Override
    public abstract String trimComments(String line);

    public Path getNutsAppsFolder(String apiVersion, String switchWorkspaceLocation) {
        NutsWorkspace ws = context.getWorkspace();
        NutsWorkspaceBootConfig bootConfig = null;
        NutsId apiId = ws.getApiId().builder().setVersion(apiVersion).build();
        apiId = ws.search().addId(apiId).setLatest(true).setFailFast(true).setContent(true).getResultDefinitions().singleton().getId();
        if (switchWorkspaceLocation != null) {
            bootConfig = loadSwitchWorkspaceLocationConfig(switchWorkspaceLocation);
            return Paths.get(bootConfig.getStoreLocation(apiId, NutsStoreLocation.APPS));
        } else {
            return Paths.get(ws.locations().getStoreLocation(apiId, NutsStoreLocation.APPS));
        }
    }

    protected abstract String createNutsScriptContent(NutsId fnutsId, NdiScriptOptions options);

    @Override
    public PathInfo[] createNutsScript(NdiScriptOptions options) {
        NutsId nid = context.getWorkspace().id().parser().parse(options.getId());
        List<PathInfo> r = new ArrayList<>();
        if (isNutsBootId(nid)) {
            r.addAll(Arrays.asList(
                    createBootScripts(
                            null,
                            options.getEnv(),
                            options.isPersistentConfig(),
                            options.isCreateDesktop(),
                            options.isCreateMenu(),
                            options.isCreateShortcut(),
                            options.getPreferredScriptName()
                    )));
        } else {
            if (options.isAddNutsScript()) {
                r.addAll(
                        Arrays.asList(createBootScripts(
                                null,
                                options.getEnv(),
                                options.isPersistentConfig(),
                                options.isCreateDesktop(),
                                options.isCreateMenu(),
                                options.isCreateShortcut(),
                                options.getPreferredScriptName()
                        ))
                );
            }
            NutsDefinition fetched = null;
            if (nid.getVersion().isBlank()) {
                fetched = context.getWorkspace().search()
                        .setSession(context.getSession().copy().setTrace(false))
                        .addId(options.getId()).setLatest(true).getResultDefinitions().required();
                nid = fetched.getId().getShortNameId();
                //nutsId=fetched.getId().getLongNameId();
            }
            String n = nid.getArtifactId();
            Path ff = getScriptFile(n, options.getEnv());
            boolean exists = Files.exists(ff);
            boolean gen = true;
            if (exists) {
                if (!context.getSession().getTerminal().ask()
                        .resetLine()
                        .setDefaultValue(false).setSession(context.getSession())
                        .forBoolean("override existing script %s ?",
                                context.getWorkspace().text().forStyled(
                                        NdiUtils.betterPath(ff.toString()), NutsTextStyle.path()
                                )
                        ).getBooleanValue()) {
                    gen = false;
                }
            }
            if (gen) {
                r.add(scriptBuilderTemplate("body", NdiScriptInfoType.ARTIFACT, nid, options.getEnv())
                        .setPath(options.getPreferredScriptName())
                        .println(createNutsScriptContent(nid, options))
                        .build());
            }
            if (options.isCreateDesktop()) {
                r.addAll(Arrays.asList(createShortcut(
                        AppShortcutTarget.DESKTOP, nid,
                        context.getWorkspace().getApiId().getVersion().toString(),
                        options.getPreferredScriptName(), options.getMenuPath(),
                        options.getIcon(),
                        options.getCwd(),
                        options.isTerminalMode(),
                        options.getAppArgs().toArray(new String[0]),
                        options.getEnv()
                )));
            }
            if (options.isCreateShortcut()) {
                r.addAll(Arrays.asList(createShortcut(
                        AppShortcutTarget.SHORTCUT, nid,
                        context.getWorkspace().getApiId().getVersion().toString(),
                        options.getPreferredScriptName(), options.getMenuPath(),
                        options.getIcon(),
                        options.getCwd(),
                        options.isTerminalMode(),
                        options.getAppArgs().toArray(new String[0]),
                        options.getEnv()
                )));
            }
            if (options.isCreateMenu()) {
                r.addAll(Arrays.asList(createShortcut(
                        AppShortcutTarget.MENU, nid,
                        context.getWorkspace().getApiId().getVersion().toString(),
                        options.getPreferredScriptName(), options.getMenuPath(),
                        options.getIcon(),
                        options.getCwd(),
                        options.isTerminalMode(),
                        options.getAppArgs().toArray(new String[0]),
                        options.getEnv()
                )));
            }
        }
        return r.toArray(new PathInfo[0]);
    }

    @Override
    public void removeNutsScript(String id, NutsSession session, NutsEnvInfo env) {
        NutsId nid = context.getWorkspace().id().parser().parse(id);
        Path f = getScriptFile(nid.getArtifactId(), env);
        NutsTextManager factory = context.getWorkspace().text();
        if (Files.isRegularFile(f)) {
            if (session.getTerminal().ask()
                    .resetLine()
                    .forBoolean("tool %s will be removed. Confirm?",
                            factory.forStyled(NdiUtils.betterPath(f.toString()), NutsTextStyle.path())
                    )
                    .setDefaultValue(true)
                    .setSession(session)
                    .getBooleanValue()) {
                try {
                    Files.delete(f);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                if (session.isPlainTrace()) {
                    session.out().printf("tool %s removed.%n", factory.forStyled(NdiUtils.betterPath(f.toString()), NutsTextStyle.path()));
                }
            }
        }
    }

//    @Override
//    public void addNutsWorkspaceScript(String preferredScriptName, NutsEnvInfo env) {
//        PathInfo[] v = persistConfig(preferredScriptName, env, createDesktop, createMenu);
//        if (context.getSession().isPlainTrace()) {
//            context.getSession().out().printf("added script %s to point to %s %s\n", env.getWorkspaceLocation(), env.getNutsApiVersion(),
//                    Arrays.stream(v).map(x -> x.getPath()).toArray()
//            );
//        }
//    }

    @Override
    public PathInfo[] switchWorkspace(NutsEnvInfo env, boolean createDesktop, boolean createMenu) {

        PathInfo[] v = createBootScripts(null, env, true, createDesktop, createMenu, false, null);

        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("```sh nuts``` switched to workspace %s to point to %s\n", env.getWorkspaceLocation(), env.getNutsApiVersion());
        }
        return v;
    }

    @Override
    public boolean isNutsBootId(NutsId nid) {
        if ("nuts".equals(nid.getShortName()) || "net.thevpc.nuts:nuts".equals(nid.getShortName())) {
            return true;
        }
        return false;
    }

    public PathInfo[] createBootScripts(String preferredName,
                                        NutsEnvInfo env,
                                        boolean global,
                                        boolean desktop,
                                        boolean menu,
                                        boolean shortcut,
                                        String fileName
    ) {
        List<PathInfo> all = new ArrayList<>();
        // create $nuts-api-app/.nutsenv
        all.add(getNutsEnv(env).create());
        // create $nuts-api-app/.nutsrc
        all.add(getNutsInit(env).create());

//        NutsDefinition apiDef = context.getWorkspace().search()
//                .addId(apiId).setOptional(false).setLatest(true).setContent(true).getResultDefinitions().required();
        Path script = null;
        script = getScriptFile(NameBuilder.id(env.getNutsApiId(), preferredName, "%n",
                env.getNutsApiDef().getDescriptor(), context.getSession()).buildName(), env);
        boolean createPath = false;
        if (Files.exists(script)) {
            if (context.getSession().getTerminal().ask()
                    .resetLine()
                    .setDefaultValue(true).setSession(context.getSession())
                    .forBoolean("override existing script %s ?",
                            context.getWorkspace().text().forStyled(
                                    NdiUtils.betterPath(script.toString()), NutsTextStyle.path()
                            )
                    ).getBooleanValue()) {
                createPath = true;
            }
        } else {
            createPath = true;
        }
        if (createPath) {
            all.add(
                    scriptBuilderTemplate("nuts", NdiScriptInfoType.NUTS, env.getNutsApiId(), env)
                            .setPath(script)
                            .build()
            );
        }
//        if (currId && standardPath) {
//            Path ff2 = Paths.get(context.getWorkspace().locations().getWorkspaceLocation())
//                    .resolve("nuts");
//            boolean overridden = Files.exists(ff2);
//            boolean gen = true;
//
//            if (!force && Files.exists(ff2)) {
//                if (!context.getSession().getTerminal().ask()
//                        .resetLine()
//                        .setSession(context.getSession())
//                        .forBoolean("override existing script %s ?",
//                                context.getWorkspace().text().forStyled(NdiUtils.betterPath(ff2.toString()), NutsTextStyle.path()))
//                        .setDefaultValue(false)
//                        .getBooleanValue()) {
//                    gen = false;
//                }
//            }
//            if (gen) {
//
//                if (trace && context.getSession().isPlainTrace()) {
//                    context.getSession().out().printf((Files.exists(ff2) ? "re-install" : "install")
//                                    + " script %s %n",
//                            context.getWorkspace().text().forStyled(NdiUtils.betterPath(ff2.toString()), NutsTextStyle.path())
//                    );
//                }
//                try {
//                    try (BufferedWriter w = Files.newBufferedWriter(ff2)) {
//                        NdiUtils.generateScript("/net/thevpc/nuts/toolbox/nadmin/" + getTemplateBodyName(), w, x -> {
//                            switch (x) {
//                                case "NUTS_ID":
//                                    return env.getNutsApiId().toString();
//                                case "GENERATOR":
//                                    return context.getAppId().toString();
//                                case "BODY": {
//                                    return getCallScriptCommand(NdiUtils.replaceFilePrefix(scriptString, ff2.toString(), ""));
//                                }
//                            }
//                            return null;
//                        });
//                    }
//                } catch (IOException ex) {
//                    throw new UncheckedIOException(ex);
//                }
//                NdiUtils.setExecutable(ff2);
//                all.add(new PathInfo(
//                                NdiScriptInfo.Type.NUTS
//                        , env.getNutsApiId(), ff2, overridden ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED));
//            }
//        }

        all.addAll(Arrays.asList(getNutsTermInit(env).create()));
        all.addAll(Arrays.asList(getNutsTerm(env).create()));

        if (global) {
            // create $home/.bashrc
            PathInfo sysRC = getSysRC(env).create();
            if (sysRC != null) {
                all.add(sysRC);
            }
            if (desktop) {
                all.addAll(Arrays.asList(createLaunchTermShortcutGlobal(AppShortcutTarget.DESKTOP, env)));
            }
            if (menu) {
                all.addAll(Arrays.asList(createLaunchTermShortcutGlobal(AppShortcutTarget.MENU, env)));
            }
        } else {
            if (desktop) {
                all.addAll(Arrays.asList(createLaunchTermShortcut(AppShortcutTarget.DESKTOP, env, preferredName, fileName)));
            }
            if (menu) {
                all.addAll(Arrays.asList(createLaunchTermShortcut(AppShortcutTarget.MENU, env, preferredName, fileName)));
            }
            if (shortcut) {
                all.addAll(Arrays.asList(createLaunchTermShortcut(AppShortcutTarget.SHORTCUT, env, preferredName, fileName)));
            }
        }

        if (global && all.stream().anyMatch(x -> x.getStatus() != PathInfo.Status.DISCARDED)) {
            onPostGlobal(env, all.toArray(new PathInfo[0]));
        }
        return all.toArray(new PathInfo[0]);
    }

    @Override
    public PathInfo[] createNutsScript(
            CreateNutsScriptCommand cmd,
            NutsApplicationContext context) {
        NutsWorkspace ws = context.getWorkspace();
        Path workspaceLocation = Paths.get(ws.locations().getWorkspaceLocation());
        List<PathInfo> result = new ArrayList<>();
        boolean subTrace = context.getSession().isTrace();
        if (!context.getSession().isPlainTrace()) {
            subTrace = false;
        }
        NutsEnvInfo env = new DefaultNutsEnvInfo(
                null, cmd.getSwitchWorkspaceLocation(),
                context.getSession()
        );
        Boolean persistentConfig = cmd.getPersistentConfig();
        if (!cmd.getIdsToInstall().isEmpty()) {
            if (persistentConfig == null) {
                if (workspaceLocation.equals(Paths.get(System.getProperty("user.home")).resolve(".config/nuts/default-workspace"))) {
                    persistentConfig = true;
                } else {
                    persistentConfig = false;
                }
            }
            boolean includeEnv = cmd.isEnv();
            for (String id : cmd.getIdsToInstall()) {
                NutsId nid = ws.id().parser().parse(id);
                if (nid == null) {
                    throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to create script for %s : invalid id", id), 100);
                }
                if (!nid.getVersion().isBlank()) {
                    includeEnv = true;
                }
            }
            String linkNameCurrent = cmd.getLinkName();
            if (includeEnv) {
                linkNameCurrent = prepareLinkName(linkNameCurrent);
            }
            List<String> nutsIds = cmd.getIdsToInstall().stream().filter(x -> isNutsBootId(ws.id().parser().parse(x))).collect(Collectors.toList());
            List<String> nonNutsIds = cmd.getIdsToInstall().stream().filter(x -> !isNutsBootId(ws.id().parser().parse(x))).collect(Collectors.toList());
            boolean bootAlreadyProcessed = false;
            for (String id : nutsIds) {
                try {
                    NutsId nid = ws.id().parser().parse(id);
                    bootAlreadyProcessed = true;
                    if (!nid.getVersion().isBlank()) {
                        String verString = nid.getVersion().toString();
                        if (verString.equalsIgnoreCase("current")
                                || verString.equalsIgnoreCase("curr")) {
                            id = nid.builder().setVersion(ws.getApiId().getVersion()).build().toString();
                        }
                    }

                    result.addAll(Arrays.asList(
                            createNutsScript(
                                    new NdiScriptOptions(env).setId(id)
                                            .setSession(context.getSession().copy().setTrace(subTrace))
                                            .setForceBoot(context.getSession().isYes())
                                            .setFetch(cmd.isFetch())
                                            .setExecType(cmd.getExecType())
                                            .setExecutorOptions(cmd.getExecutorOptions())
                                            .setIncludeEnv(includeEnv)
                                            .setPreferredScriptName(linkNameCurrent)
                                            .setCreateDesktop(cmd.isCreateDesktop())
                                            .setCreateMenu(cmd.isCreateMenu())
                                            .setAppArgs(cmd.getAppArgs())
                                            .setMenuPath(cmd.getMenuPath())
                                            .setIcon(cmd.getIcon())
                                            .setCwd(cmd.getCwd())
                                            .setPersistentConfig(persistentConfig)
                                            .setTerminalMode(cmd.isTerminalMode())
                            )
                    ));
                } catch (UncheckedIOException e) {
                    throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to add script for %s : %s", id, e), e);
                }
            }
            if (!bootAlreadyProcessed && !nonNutsIds.isEmpty()) {
                result.addAll(Arrays.asList(createBootScripts(
                        null,
                        env,
                        persistentConfig,
                        cmd.isCreateDesktop(),
                        cmd.isCreateMenu(),
                        cmd.isCreateShortcut(),
                        cmd.getLinkName()
                )));
            }
            for (String id : nonNutsIds) {
                try {
                    NutsId nid = ws.id().parser().parse(id);
                    if (nid == null) {
                        throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to create script for %s : invalid id", id), 100);
                    }
                    result.addAll(Arrays.asList(
                            createNutsScript(
                                    new NdiScriptOptions(env).setId(id)
                                            .setAddNutsScript(false)
                                            .setSession(context.getSession().copy().setTrace(subTrace))
                                            .setForceBoot(context.getSession().isYes())
                                            .setFetch(cmd.isFetch())
                                            .setExecType(cmd.getExecType())
                                            .setExecutorOptions(cmd.getExecutorOptions())
                                            .setIncludeEnv(includeEnv)
                                            .setPreferredScriptName(linkNameCurrent)
                                            .setCreateDesktop(cmd.isCreateDesktop())
                                            .setCreateMenu(cmd.isCreateMenu())
                                            .setAppArgs(cmd.getAppArgs())
                                            .setMenuPath(cmd.getMenuPath())
                                            .setIcon(cmd.getIcon())
                                            .setCwd(cmd.getCwd())
                                            .setPersistentConfig(persistentConfig)
                                            .setTerminalMode(cmd.isTerminalMode())
                            ))
                    );
                } catch (UncheckedIOException e) {
                    throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to add script for %s : %s", id, e), e);
                }
            }
//            result.addAll(Arrays.asList(configurePath(
//                    env
//                    , cmd.isCreateDesktop(), cmd.isCreateMenu(), persistentConfig, cmd.isCreateShortcut(), linkNameCurrent)));
        }
        return result.toArray(new PathInfo[0]);
    }

    public void onPostGlobal(NutsEnvInfo env, PathInfo[] updatedPaths) {

    }

    @NotNull
    protected String newlineString() {
        return "\n";
    }

    public NutsWorkspaceBootConfig loadSwitchWorkspaceLocationConfig(String switchWorkspaceLocation) {
        NutsWorkspaceBootConfig bootconfig = context.getWorkspace().config().loadBootConfig(switchWorkspaceLocation, false, true);
        if (bootconfig == null) {
            throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("invalid workspace: %s", switchWorkspaceLocation));
        }
        return bootconfig;
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

    public String dblQte(String line) {
        return "\"" + line + "\"";
    }

    public String smpQte(String line) {
        return "'" + line + "'";
    }

    public abstract String toCommentLine(String line);

    public boolean saveFile(Path filePath, String content, boolean force) {
        try {
            String fileContent = "";
            if (Files.isRegularFile(filePath)) {
                fileContent = new String(Files.readAllBytes(filePath));
            }
            if (force || !content.trim().equals(fileContent.trim())) {
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, content.getBytes());
                return true;
            }
            return false;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public PathInfo addFileLine(NdiScriptInfoType type,
                                NutsId id,
                                Path filePath,
                                ReplaceString commentLine,
                                String goodLine,
                                ReplaceString header) {
//        Pattern commentLineConditionPattern = Pattern.compile(commentLineConditionRegexp);
        filePath = filePath.toAbsolutePath();
        List<String> goodLinesList = Arrays.asList(goodLine.split("[\n\r]"));
        boolean found = false;
        List<String> lines = new ArrayList<>();
        if (Files.isRegularFile(filePath)) {
            String fileContent = null;
            try {
                fileContent = new String(Files.readAllBytes(filePath));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            List<String> fileRows = new ArrayList<>(Arrays.asList(fileContent.split("[\n\r]")));
            //trim lines
            while (!fileRows.isEmpty()) {
                if (fileRows.get(0).trim().isEmpty()) {
                    fileRows.remove(0);
                } else if (fileRows.get(fileRows.size() - 1).trim().isEmpty()) {
                    fileRows.remove(fileRows.size() - 1);
                } else {
                    break;
                }
            }
            for (int i = 0; i < fileRows.size(); i++) {
                String row = fileRows.get(i);
                if (isComments(row.trim()) && commentLine.matches(trimComments(row.trim()))) {
                    String clta = toCommentLine(commentLine.getReplacement());
                    if (!clta.equals(row)) {
//                        updatedFile = true;
                    }
                    if (lines.size() > 0) {
                        if (lines.get(lines.size() - 1).trim().length() > 0) {
                            lines.add("");
                        }
                    }
                    lines.add(clta);
                    found = true;
                    i++;
                    List<String> old = new ArrayList<>();
                    while (i < fileRows.size()) {
                        String s = fileRows.get(i);
                        if (s.trim().isEmpty()) {
                            i++;
                            break;
                        } else if (s.trim().startsWith("#")) {
                            break;
                        } else {
                            i++;
                            old.add(s.trim());
                        }
                    }
                    lines.addAll(goodLinesList);
                    lines.add("");
//                    if (!old.equals(goodLinesList)) {
//                    }
                    for (; i < fileRows.size(); i++) {
                        lines.add(fileRows.get(i));
                    }
                } else {
                    lines.add(row);
                }
            }
        }
        if (header != null) {
            if (lines.size() == 0 || !header.matches(lines.get(0).trim())) {
                lines.add(0, header.getReplacement());
            }
        }
        if (!found) {
            if (lines.size() > 0 && !lines.get(0).trim().isEmpty()) {
                lines.add("");
            }
            lines.add(toCommentLine(commentLine.getReplacement()));
            lines.addAll(goodLinesList);
            lines.add("");
        } else {
//            if (lines.size() > 0) {
//                if (lines.get(lines.size() - 1).trim().length() > 0) {
//                    lines.add("");
//                }
//            }
        }
        byte[] oldContent = NdiUtils.loadFile(filePath);
        String oldContentString = oldContent == null ? "" : new String(oldContent);
        byte[] newContent = (String.join(newlineString(), lines)).getBytes();
        String newContentString = new String(newContent);
        PathInfo.Status s = NdiUtils.tryWriteStatus(newContent, filePath);
        return new PathInfo(type, id, filePath, NdiUtils.tryWrite(newContent, filePath));
    }

    public PathInfo removeFileCommented2Lines(NdiScriptInfoType type, NutsId id, Path filePath, String commentLine, boolean force) {
        filePath = filePath.toAbsolutePath();
        boolean alreadyExists = Files.exists(filePath);
        boolean found = false;
        boolean updatedFile = false;
        try {
            List<String> lines = new ArrayList<>();
            if (Files.isRegularFile(filePath)) {
                String fileContent = new String(Files.readAllBytes(filePath));
                String[] fileRows = fileContent.split("[\n\r]");
                for (int i = 0; i < fileRows.length; i++) {
                    String row = fileRows[i];
                    if (row.trim().equals(toCommentLine(commentLine))) {
                        found = true;
                        i += 2;
                        for (; i < fileRows.length; i++) {
                            lines.add(fileRows[i]);
                        }
                    } else {
                        lines.add(row);
                    }
                }
            }
            if (found) {
                updatedFile = true;
            }
            if (force || updatedFile) {
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, (String.join(newlineString(), lines) + newlineString()).getBytes());
            }
            return new PathInfo(type, id, filePath, updatedFile ? alreadyExists ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED : PathInfo.Status.DISCARDED);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected abstract String getSetVarCommand(String name, String value);

    protected abstract String getSetVarStaticCommand(String name, String value);

    protected abstract String getCallScriptCommand(String path, String... args);

    protected abstract String getExecFileName(String name);

    protected NAdminConfig loadNadminConfig(String nutsVersion) {
        Path t = Paths.get(context.getWorkspace().locations().getStoreLocation(context.getAppId(), NutsStoreLocation.CONFIG))
                .resolve("nadmin-config.json");
        if (Files.isRegularFile(t)) {
            return context.getWorkspace().elem().setContentType(NutsContentType.JSON).parse(t, NAdminConfig.class);
        }
        return null;
    }

    protected void saveNadminConfig(NAdminConfig config) {
        Path t = Paths.get(context.getWorkspace().locations().getStoreLocation(context.getAppId(), NutsStoreLocation.CONFIG))
                .resolve("nadmin-config.json");
        context.getWorkspace().elem().setContentType(NutsContentType.JSON).setCompact(false).setValue(config)
                .print(t);
    }

    @NotNull
    protected abstract FreeDesktopEntryWriter createFreeDesktopEntryWriter();

    public PathInfo[] createShortcut(AppShortcutTarget appShortcutTarget, NutsId id, String path, FreeDesktopEntry.Group shortcut) {
        List<PathInfo> results = new ArrayList<>();
        FreeDesktopEntryWriter ww = createFreeDesktopEntryWriter();
        if (appShortcutTarget == AppShortcutTarget.DESKTOP) {
            results.addAll(Arrays.asList(ww.writeDesktop(shortcut, path, true, id)));
        } else if (appShortcutTarget == AppShortcutTarget.MENU) {
            results.addAll(Arrays.asList(ww.writeMenu(shortcut, path, true, id)));
        } else if (appShortcutTarget == AppShortcutTarget.SHORTCUT) {
            results.addAll(Arrays.asList(ww.writeShortcut(shortcut, path == null ? null : Paths.get(path), true, id)));
        } else {
            throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("unsupported"));
        }
        return results.toArray(new PathInfo[0]);
    }

    public String resolveIcon(String iconPath, NutsId appId) {
        NutsWorkspace ws = context.getWorkspace();
        NutsDefinition appDef = ws.search().addId(appId).setLatest(true).setEffective(true).getResultDefinitions().singleton();
        String descAppIcon = appDef.getDescriptor().getIcon();
        if (descAppIcon == null) {
            if (isNutsBootId(appDef.getId())
                    || appDef.getId().getGroupId().startsWith("net.thevpc.nuts")
            ) {
                //get icon from nadmin
                descAppIcon = "nuts-resource://" + context.getAppId().getLongName() + "/net/thevpc/nuts/toolbox/nadmin/nuts.svg";
            }
        }
        if (descAppIcon != null) {
            NutsPath p0 = ws.io().path(descAppIcon);
            if (descAppIcon.startsWith("classpath://")) {
                descAppIcon = "nuts-resource://" + appDef.getId().getLongName() + "" + descAppIcon.substring("classpath://".length() - 1);
            }
            int q = p0.getLocation().lastIndexOf('.');
            String extension = q > 0 ? p0.getLocation().substring(q + 1) : "";
            String bestName = "icon.png";
            if (ACCEPTED_ICON_EXTENSIONS.contains(extension.toLowerCase())) {
                bestName = "icon." + extension.toLowerCase();
            }
            Path localIconPath = Paths.get(ws.locations().getStoreLocation(appDef.getId(), NutsStoreLocation.APPS))
                    .resolve(".nuts")
                    .resolve(bestName);
            if (Files.isRegularFile(localIconPath)) {
                iconPath = localIconPath.toString();
            } else {
                NutsPath p = ws.io().path(descAppIcon);
                if (p.exists()) {
                    ws.io().copy()
                            .from(p)
                            .to(localIconPath)
                            .run();
                    iconPath = localIconPath.toString();
                }
            }
        }
        if (iconPath == null) {
            iconPath = getDefaultIconPath();
        }
        return iconPath;
    }

    public PathInfo[] createShortcut(
            AppShortcutTarget appShortcutTarget,
            NutsId appId,
            String apiVersion,
            String path,
            String menuPath,
            String iconPath,
            String cwd,
            boolean terminalMode,
            String[] extraArgs,
            NutsEnvInfo env
    ) {

        NutsWorkspace ws = context.getWorkspace();
        if (apiVersion == null) {
            throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("missing nuts-api version to link to"));
        }
        NutsId apiId = ws.getApiId().builder().setVersion(apiVersion).build();
        NutsDefinition apiDefinition = ws.search().addId(apiId).setFailFast(true).setLatest(true).setContent(true).getResultDefinitions().singleton();

//            Path apiConfigFolder
//                    = bootConfig != null ? Paths.get(bootConfig.getStoreLocation(apiId, NutsStoreLocation.APPS))
//                    : Paths.get(ws.locations().getStoreLocation(apiId, NutsStoreLocation.APPS));
//            Path startNutsFile = apiConfigFolder.resolve(getExecFileName("start-nuts"));

//        FreeDesktopEntry fdi = new FreeDesktopEntry();
        NutsDefinition appDef = context.getWorkspace().search().addId(appId).setLatest(true).setEffective(true).getResultDefinitions().singleton();
        StringBuilder execCmd = new StringBuilder();

        execCmd.append("'" + getNutsStart(env).path().toString() + "'").append(" -y '").append(appId).append("'");
        if (extraArgs != null) {
            for (String extraArg : extraArgs) {
                execCmd.append(" '").append(extraArg).append("'");
            }
        }
        if (cwd == null) {
            //should it be id's var folder?
            cwd = System.getProperty("user.home");
        }
        iconPath = resolveIcon(iconPath, appId);

        String preferredName = NameBuilder.label(appDef.getId(),
                NameBuilder.extractPathName(path),
                null, appDef.getDescriptor(), context.getSession()).buildName();
        if (preferredName.isEmpty() && appShortcutTarget == AppShortcutTarget.DESKTOP) {
            preferredName = "%N%s%v%s%h";
        }
        preferredName = NameBuilder.label(appDef.getId(), preferredName, null, appDef.getDescriptor(), context.getSession()).buildName();

        FreeDesktopEntry.Group sl = FreeDesktopEntry.Group.desktopEntry(preferredName, execCmd.toString(), cwd);
        sl.setStartNotify(true);
        sl.setIcon(iconPath);
        sl.setGenericName(apiDefinition.getDescriptor().getGenericName());
        sl.setComment(appDef.getDescriptor().getDescription());
        sl.setTerminal(terminalMode);
        if (menuPath != null) {
            sl.addCategory(menuPath);
        }
        String preferredPath = NameBuilder.id(appDef.getId(), path, null, appDef.getDescriptor(), context.getSession()).buildName();
        return createShortcut(appShortcutTarget, appId, preferredPath, sl);
    }

    @NotNull
    protected String getDefaultIconPath() {
        return "apper";
    }


    public PathInfo[] createLaunchTermShortcutGlobal(AppShortcutTarget appShortcutTarget, NutsEnvInfo env) {
        String fileName = env.getNutsApiId().getShortName().replace(':', '-');
        String name = "Nuts Terminal";
        return createLaunchTermShortcut(appShortcutTarget, env, name, fileName);
    }

    public abstract PathInfo[] createLaunchTermShortcut(AppShortcutTarget appShortcutTarget,
                                                        NutsEnvInfo env,
                                                        String name,
                                                        String fileName
    );


    public String createNutsEnvString(NutsEnvInfo env, boolean updateEnv, boolean updatePATH) {
        final NutsWorkspace ws = context.getWorkspace();
        String NUTS_JAR_PATH = ws.search()
                .setSession(context.getSession().copy().setTrace(false))
                .addId(ws.getApiId()).getResultPaths().required();

        /**
         * "#!/bin/sh\n" +
         *                         "# This File is generated by nuts nadmin companion tool.\n" +
         *                         "# Do not edit it manually. All changes will be lost when nadmin runs again\n" +
         *                         "# This file aims to prepare bash environment against current nuts\n" +
         *                         "# workspace installation.\n" +
         *                         "#\n"
         */
        TreeSet<String> exports = new TreeSet<>();
        SimpleScriptBuilder tmp = scriptBuilderSimple(NdiScriptInfoType.NUTS_ENV, env.getNutsApiId(), env);
        if (updateEnv) {
            exports.addAll(Arrays.asList("NUTS_VERSION", "NUTS_WORKSPACE", "NUTS_JAR", "NUTS_WORKSPACE_BINDIR"));
            tmp.printSetStatic("NUTS_VERSION", ws.getApiVersion().toString());
            tmp.printSetStatic("NUTS_WORKSPACE", ws.locations().getWorkspaceLocation().toString());
            for (NutsStoreLocation value : NutsStoreLocation.values()) {
                tmp.printSetStatic("NUTS_WORKSPACE_" + value, ws.locations().getStoreLocation(value));
                exports.add("NUTS_WORKSPACE_" + value);
            }
            if (NUTS_JAR_PATH.startsWith(ws.locations().getStoreLocation(NutsStoreLocation.LIB))) {
                String pp = NUTS_JAR_PATH.substring(ws.locations().getStoreLocation(NutsStoreLocation.LIB).length());
                tmp.printSet("NUTS_JAR", varRef("NUTS_WORKSPACE_LIB") + pp);
            } else {
                tmp.printSetStatic("NUTS_JAR", NUTS_JAR_PATH);
            }
            String p0 = env.getBinFolder().toString().substring(
                    ws.locations().getStoreLocation(NutsStoreLocation.APPS).length()
            );
            tmp.printSet("NUTS_WORKSPACE_BINDIR", varRef("NUTS_WORKSPACE_APPS") + p0);
        }
        if (updatePATH) {
            exports.add("PATH");
            tmp.printSet("PATH", varRef("NUTS_WORKSPACE_BINDIR") + getPathVarSep() + varRef("PATH"));
        }
        tmp.println("export " + String.join(" ", exports));
        return tmp.buildString();
    }

    protected abstract ReplaceString getShebanSh();

    protected abstract ReplaceString getCommentLineConfigHeader();

    protected abstract String getTemplateName(String name);

    protected abstract String varRef(String v);

    public String getPathVarSep() {
        return System.getProperty("path.separator");
    }

}
