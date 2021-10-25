package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.shell.NutsShellHelper;
import net.thevpc.nuts.runtime.core.shell.ReplaceString;
import net.thevpc.nuts.runtime.core.shell.ScriptBuilder;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.PathInfo;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.*;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.script.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseSystemNdi extends AbstractSystemNdi {
    public static final ReplaceString COMMENT_LINE_CONFIG_HEADER = new ReplaceString("net.thevpc.nuts configuration", "((net[.]thevpc[.]nuts)|(net[.]thevpc[.]nuts.toolbox[.]ndi)|(net[.]vpc[.]app[.]nuts)) configuration");

    public BaseSystemNdi(NutsSession session) {
        super(session);
    }

    public NdiScriptInfo[] getSysRC(NdiScriptOptions options) {
        List<NdiScriptInfo> scriptInfos = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        for (NutsShellFamily sf : session.env().getShellFamilies()) {
            String z = NutsShellHelper.of(sf).getSysRcName();
            if (!visited.contains(z)) {
                visited.add(z);
                NdiScriptInfo i = new RcNdiScriptInfo(z, options, sf);
                scriptInfos.add(i);
            }
        }
        return scriptInfos.toArray(new NdiScriptInfo[0]);
    }

    public NdiScriptInfo[] getIncludeNutsInit(NdiScriptOptions options) {
        return Arrays.stream(getShellGroups())
                .map(x -> getIncludeNutsInit(options, x))
                .filter(Objects::nonNull)
                .toArray(NdiScriptInfo[]::new);
    }

    public NdiScriptInfo getIncludeNutsInit(NdiScriptOptions options, NutsShellFamily shellFamily) {
        switch (shellFamily) {
            case SH:
            case BASH:
            case CSH:
            case KSH:
            case ZSH: {
                return new NdiScriptInfo() {
                    @Override
                    public Path path() {
                        return options.resolveIncFolder().resolve(".nuts-init.sh");
                    }

                    @Override
                    public PathInfo create() {
                        Path apiConfigFile = path();
                        return scriptBuilderTemplate("nuts-init", NutsShellFamily.SH, "nuts-init", options.resolveNutsApiId(), options)
                                .setPath(apiConfigFile)
                                .buildAddLine(BaseSystemNdi.this);
                    }
                };
            }
            case FISH: {
                return new NdiScriptInfo() {
                    @Override
                    public Path path() {
                        return options.resolveIncFolder().resolve(".nuts-init.fish");
                    }

                    @Override
                    public PathInfo create() {
                        Path apiConfigFile = path();
                        return scriptBuilderTemplate("nuts-init", NutsShellFamily.FISH, "nuts-init", options.resolveNutsApiId(), options)
                                .setPath(apiConfigFile)
                                .buildAddLine(BaseSystemNdi.this);
                    }
                };
            }
        }
        return null;
    }

    public NdiScriptInfo[] getIncludeNutsTermInit(NdiScriptOptions options) {
        return Arrays.stream(getShellGroups())
                .map(x -> getIncludeNutsTermInit(options, x))
                .filter(Objects::nonNull)
                .toArray(NdiScriptInfo[]::new);
    }

    protected abstract NutsShellFamily[] getShellGroups();

    public abstract NdiScriptInfo getIncludeNutsTermInit(NdiScriptOptions options, NutsShellFamily shellFamily);

    public FromTemplateScriptBuilder scriptBuilderTemplate(String templateName, NutsShellFamily shellFamily, String type, NutsId anyId, NdiScriptOptions options) {
        return ScriptBuilder.fromTemplate(templateName, shellFamily, type, anyId, BaseSystemNdi.this, options);
    }

    public SimpleScriptBuilder scriptBuilderSimple(NutsShellFamily shellFamily, String type, NutsId anyId, NdiScriptOptions options) {
        return ScriptBuilder.simple(shellFamily,type, anyId, BaseSystemNdi.this)/*,options*/;
    }

    public NdiScriptInfo[] getNutsTerm(NdiScriptOptions options) {
        return Arrays.stream(getShellGroups())
                .map(x -> getNutsTerm(options, x))
                .filter(Objects::nonNull)
                .toArray(NdiScriptInfo[]::new);
    }

    public abstract NdiScriptInfo getNutsTerm(NdiScriptOptions options,NutsShellFamily shellFamily) ;


    public NdiScriptInfo[] getIncludeNutsEnv(NdiScriptOptions options) {
        return Arrays.stream(getShellGroups())
                .map(x -> getIncludeNutsEnv(options, x))
                .filter(Objects::nonNull)
                .toArray(NdiScriptInfo[]::new);
    }

    public abstract NdiScriptInfo getIncludeNutsEnv(NdiScriptOptions options, NutsShellFamily shellFamily) ;

    public NdiScriptInfo getNutsStart(NdiScriptOptions options) {
        return new NdiScriptInfo() {
            @Override
            public Path path() {
                return options.resolveBinFolder().resolve(getExecFileName("nuts"));
            }

            @Override
            public PathInfo create() {
                return null;
            }
        };
    }


    //ws.getApiId().getVersion()


    public Path getBinScriptFile(String name, NdiScriptOptions options) {
        if (CoreIOUtils.isPath(name)) {
            return Paths.get(name).toAbsolutePath();
        }
        return options.resolveBinFolder().resolve(getExecFileName(name)).toAbsolutePath();
//        Path bin =
//                Paths.get(context.getAppsFolder());
//        return bin.resolve(getExecFileName(name)).toAbsolutePath();
    }

    protected abstract String createNutsScriptContent(NutsId fnutsId, NdiScriptOptions options, NutsShellFamily shellFamily);

    @Override
    public PathInfo[] createArtifactScript(NdiScriptOptions options) {
        NutsId nid = session.id().parser().parse(options.getId());
        List<PathInfo> r = new ArrayList<>();
        if (isNutsBootId(nid)) {
            r.addAll(Arrays.asList(
                    createBootScripts(options.copy().setId(options.resolveNutsApiId().toString()))));
        } else {
            if (options.isAddNutsScript()) {
                r.addAll(
                        Arrays.asList(createBootScripts(options.copy().setId(options.resolveNutsApiId().toString())))
                );
            }
            NutsDefinition fetched = null;
            if (nid.getVersion().isBlank()) {
                fetched = session.search()
                        .setSession(session.copy())
                        .addId(options.getId()).setLatest(true).getResultDefinitions().required();
                nid = fetched.getId().getShortId();
                //nutsId=fetched.getId().getLongNameId();
            }
            String n = nid.getArtifactId();
            Path ff = getBinScriptFile(n, options);
            {
                String s = options.getLauncher().getCustomScriptPath();
                if (NutsBlankable.isBlank(s)) {
                    NutsDefinition appDef = loadIdDefinition(nid);
                    s = NameBuilder.id(appDef.getId(), "%n", null, appDef.getDescriptor(), session).buildName();
                    s = getBinScriptFile(s, options).toString();
                } else if (CoreIOUtils.isPathFolder(s)) {
                    NutsDefinition appDef = loadIdDefinition(nid);
                    s = s + File.separator + NameBuilder.id(appDef.getId(), getExecFileName("%n"), null, appDef.getDescriptor(), session).buildName();
                } else {
                    NutsDefinition appDef = loadIdDefinition(nid);
                    s = NameBuilder.id(appDef.getId(), s, null, appDef.getDescriptor(), session).buildName();
                    s = getBinScriptFile(s, options).toString();
                }
                NutsShellFamily shellFamily = getShellGroups()[0];
                r.add(scriptBuilderTemplate("body", shellFamily, "artifact", nid, options)
                        .setPath(s)
                        .println(createNutsScriptContent(nid, options, shellFamily))
                        .build());
            }
            if (matchCondition(options.getLauncher().getCreateDesktopShortcut(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.DESKTOP))) {
                r.addAll(Arrays.asList(createShortcut(NutsDesktopIntegrationItem.DESKTOP, options.copy().setId(nid.toString()))));
            }
            if (matchCondition(options.getLauncher().getCreateCustomShortcut(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.SHORTCUT))) {
                r.addAll(Arrays.asList(createShortcut(NutsDesktopIntegrationItem.SHORTCUT, options.copy().setId(nid.toString()))));
            }
            if (matchCondition(options.getLauncher().getCreateMenuShortcut(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.MENU))) {
                r.addAll(Arrays.asList(createShortcut(NutsDesktopIntegrationItem.MENU, options.copy().setId(nid.toString()))));
            }
        }
        return r.toArray(new PathInfo[0]);
    }

    @Override
    public void removeNutsScript(String id, String switchWorkspaceLocation, NutsSession session) {
        NdiScriptOptions options = new NdiScriptOptions().setSession(session);
        options.getLauncher().setSwitchWorkspaceLocation(switchWorkspaceLocation);
        NutsId nid = this.session.id().parser().parse(id);
        Path f = getBinScriptFile(nid.getArtifactId(), options);
        NutsTextManager factory = this.session.text();
        if (Files.isRegularFile(f)) {
            if (session.getTerminal().ask()
                    .resetLine()
                    .forBoolean("tool %s will be removed. Confirm?",
                            factory.ofStyled(CoreIOUtils.betterPath(f.toString()), NutsTextStyle.path())
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
                    session.out().printf("tool %s removed.%n", factory.ofStyled(CoreIOUtils.betterPath(f.toString()), NutsTextStyle.path()));
                }
            }
        }
    }

    @Override
    public PathInfo[] switchWorkspace(NdiScriptOptions options) {
        options = options.copy();
        options.getLauncher().setSystemWideConfig(true);
        PathInfo[] v = createBootScripts(options);

        if (session.isPlainTrace()) {
            session.out().printf("```sh nuts``` switched to workspace %s to point to %s\n",
                    options.getWorkspaceLocation(),
                    options.getNutsApiVersion()
            );
        }
        return v;
    }

    @Override
    public boolean isNutsBootId(NutsId nid) {
        return "nuts".equals(nid.getShortName()) || "net.thevpc.nuts:nuts".equals(nid.getShortName());
    }

    @Override
    public PathInfo[] addScript(NdiScriptOptions options, String[] all) {
        List<String> idsToInstall = Arrays.asList(all);
        NutsSession session = options.getSession();
        if (session == null) {
            throw new IllegalArgumentException("missing session");
        }
        Path workspaceLocation = Paths.get(session.locations().getWorkspaceLocation());
        List<PathInfo> result = new ArrayList<>();
        Boolean systemWideConfig = options.getLauncher().getSystemWideConfig();
        if (!idsToInstall.isEmpty()) {
            if (systemWideConfig == null) {
                systemWideConfig = workspaceLocation.equals(Paths.get(System.getProperty("user.home")).resolve(".config/nuts/").resolve(NutsConstants.Names.DEFAULT_WORKSPACE_NAME));
            }
            boolean includeEnv = options.isIncludeEnv();
            for (String id : idsToInstall) {
                NutsId nid = session.id().parser().parse(id);
                if (nid == null) {
                    throw new NutsExecutionException(session, NutsMessage.cstyle("unable to create script for %s : invalid id", id), 100);
                }
                if (!nid.getVersion().isBlank()) {
                    includeEnv = true;
                }
            }
            String linkNameCurrent = options.getLauncher().getCustomScriptPath();
//            if (includeEnv) {
//                linkNameCurrent = prepareLinkName(linkNameCurrent);
//            }
            List<String> nutsIds = idsToInstall.stream().filter(x -> isNutsBootId(session.id().parser().parse(x))).collect(Collectors.toList());
            List<String> nonNutsIds = idsToInstall.stream().filter(x -> !isNutsBootId(session.id().parser().parse(x))).collect(Collectors.toList());
            boolean bootAlreadyProcessed = false;
            for (String id : nutsIds) {
                try {
                    NutsId nid = session.id().parser().parse(id);
                    bootAlreadyProcessed = true;
                    if (!nid.getVersion().isBlank()) {
                        String verString = nid.getVersion().toString();
                        if (verString.equalsIgnoreCase("current")
                                || verString.equalsIgnoreCase("curr")) {
                            id = nid.builder().setVersion(session.getWorkspace().getApiId().getVersion()).build().toString();
                        }
                    }

                    NdiScriptOptions oo = options.copy().setId(id);
                    oo.getLauncher().setCustomScriptPath(linkNameCurrent);
                    oo.getLauncher().setSystemWideConfig(systemWideConfig != null && systemWideConfig);

                    result.addAll(Arrays.asList(createArtifactScript(oo)));
                } catch (UncheckedIOException e) {
                    throw new NutsExecutionException(session, NutsMessage.cstyle("unable to add launcher for %s : %s", id, e), e);
                }
            }
            if (!bootAlreadyProcessed && !nonNutsIds.isEmpty()) {
                NdiScriptOptions oo = options.copy()
                        .setId(options.resolveNutsApiId().toString());
                oo.getLauncher().setCustomScriptPath(null);//reset script path!
                oo.getLauncher().setCustomScriptPath(linkNameCurrent);
                oo.getLauncher().setSystemWideConfig(systemWideConfig != null && systemWideConfig);
                result.addAll(Arrays.asList(createBootScripts(oo)));
            }
            for (String id : nonNutsIds) {
                try {
                    NutsId nid = session.id().parser().parse(id);
                    if (nid == null) {
                        throw new NutsExecutionException(session, NutsMessage.cstyle("unable to create script for %s : invalid id", id), 100);
                    }
                    NdiScriptOptions oo = options.copy()
                            .setId(id);
                    oo.getLauncher().setCustomScriptPath(linkNameCurrent);
                    oo.getLauncher().setSystemWideConfig(systemWideConfig != null && systemWideConfig);
                    oo.setIncludeEnv(includeEnv);
                    oo.setSession(session);
                    result.addAll(Arrays.asList(createArtifactScript(oo)));
                } catch (UncheckedIOException e) {
                    throw new NutsExecutionException(session, NutsMessage.cstyle("unable to add launcher for %s : %s", id, e), e);
                }
            }
//            result.addAll(Arrays.asList(configurePath(
//                    env
//                    , cmd.isCreateDesktop(), cmd.isCreateMenu(), systemWideConfig, cmd.isCreateShortcut(), linkNameCurrent)));
        }
        return result.toArray(new PathInfo[0]);
    }

    public PathInfo[] createBootScripts(NdiScriptOptions options) {
        String preferredName = options.getLauncher().getShortcutName();
        List<PathInfo> all = new ArrayList<>();

        // create $nuts-api-app/.nutsenv
        for (NdiScriptInfo i : getIncludeNutsEnv(options)) {
            all.add(i.create());
        }

        // create $nuts-api-app/.nutsrc
        for (NdiScriptInfo i : getIncludeNutsInit(options)) {
            all.add(i.create());
        }

        String scriptPath = options.getLauncher().getCustomScriptPath();
        all.add(scriptBuilderTemplate("nuts",getShellGroups()[0], "nuts", options.resolveNutsApiId(), options)
                .setPath(getBinScriptFile(NameBuilder.id(options.resolveNutsApiId(), scriptPath, "%n",
                        options.resolveNutsApiDef().getDescriptor(), session).buildName(), options))
                .build());
        for (NdiScriptInfo i : getIncludeNutsTermInit(options)) {
            all.add(i.create());
        }
        for (NdiScriptInfo i : getNutsTerm(options)) {
            all.add(i.create());
        }

        if (options.getLauncher().getSystemWideConfig() != null && options.getLauncher().getSystemWideConfig()) {
            // create $home/.bashrc
            //PathInfo sysRC = getSysRC(options).create();

            //  if (sysRC != null) {
            //    all.add(sysRC);
            //}
            for (NdiScriptInfo ndiScriptInfo : getSysRC(options)) {
                PathInfo sysRC = ndiScriptInfo.create();
                if (sysRC != null) {
                    all.add(sysRC);
                }

            }
            if (matchCondition(options.getLauncher().getCreateDesktopShortcut(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.DESKTOP))) {
                all.addAll(Arrays.asList(createLaunchTermShortcutGlobal(NutsDesktopIntegrationItem.DESKTOP, options)));
            }
            if (matchCondition(options.getLauncher().getCreateMenuShortcut(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.MENU))) {
                all.addAll(Arrays.asList(createLaunchTermShortcutGlobal(NutsDesktopIntegrationItem.MENU, options)));
            }
        } else {
            if (matchCondition(options.getLauncher().getCreateDesktopShortcut(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.DESKTOP))) {
                all.addAll(Arrays.asList(createLaunchTermShortcut(NutsDesktopIntegrationItem.DESKTOP, options, scriptPath, preferredName)));
            }
            if (matchCondition(options.getLauncher().getCreateMenuShortcut(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.MENU))) {
                all.addAll(Arrays.asList(createLaunchTermShortcut(NutsDesktopIntegrationItem.MENU, options, scriptPath, preferredName)));
            }
            if (matchCondition(options.getLauncher().getCreateCustomShortcut(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.SHORTCUT))) {
                all.addAll(Arrays.asList(createLaunchTermShortcut(NutsDesktopIntegrationItem.SHORTCUT, options, scriptPath, preferredName)));
            }
        }

        if (options.getLauncher().getSystemWideConfig() != null && options.getLauncher().getSystemWideConfig()
                && all.stream().anyMatch(x -> x.getStatus() != PathInfo.Status.DISCARDED)) {
            onPostGlobal(options, all.toArray(new PathInfo[0]));
        }
        return all.toArray(new PathInfo[0]);
    }

    private NutsDefinition loadIdDefinition(NutsId nid) {
        return session.search().addId(nid).setLatest(true).setEffective(true).setDistinct(true).getResultDefinitions().singleton();
    }

    public NutsSupportMode getDesktopIntegrationSupport(NutsDesktopIntegrationItem target) {
        return session.env().getDesktopIntegrationSupport(target);
    }

    protected boolean matchCondition(NutsSupportCondition createDesktop, NutsSupportMode desktopIntegrationSupport) {
        if (desktopIntegrationSupport == null) {
            desktopIntegrationSupport = NutsSupportMode.UNSUPPORTED;
        }
        return desktopIntegrationSupport.acceptCondition(createDesktop, session);
    }

    public void onPostGlobal(NdiScriptOptions options, PathInfo[] updatedPaths) {

    }

    public NutsWorkspaceBootConfig loadSwitchWorkspaceLocationConfig(String switchWorkspaceLocation) {
        NutsWorkspaceBootConfig bootConfig = session.config().loadBootConfig(switchWorkspaceLocation, false, true);
        if (bootConfig == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid workspace: %s", switchWorkspaceLocation));
        }
        return bootConfig;
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

    public List<String> splitLines(String text) {
        ArrayList<String> lines = new ArrayList<>();
        if (text == null) {
            return lines;
        }
        try (BufferedReader br = new BufferedReader(new StringReader(text))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return lines;
    }

    public PathInfo addFileLine(String type,
                                NutsId id,
                                Path filePath,
                                ReplaceString commentLine,
                                String contentToAdd,
                                ReplaceString header, NutsShellFamily shellFamily) {
//        Pattern commentLineConditionPattern = Pattern.compile(commentLineConditionRegexp);
        filePath = filePath.toAbsolutePath();
        List<String> contentToAddRows = splitLines(contentToAdd);
        boolean found = false;
        List<String> newFileContentRows = new ArrayList<>();
        List<String> oldFileContentRows = null;
        NutsShellHelper sh = NutsShellHelper.of(shellFamily);
        if (Files.isRegularFile(filePath)) {
            String fileContentString = null;
            try {
                fileContentString = new String(Files.readAllBytes(filePath));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            oldFileContentRows = splitLines(fileContentString);
            //trim lines
            while (!oldFileContentRows.isEmpty()) {
                if (oldFileContentRows.get(0).trim().isEmpty()) {
                    oldFileContentRows.remove(0);
                } else if (oldFileContentRows.get(oldFileContentRows.size() - 1).trim().isEmpty()) {
                    oldFileContentRows.remove(oldFileContentRows.size() - 1);
                } else {
                    break;
                }
            }
            for (int i = 0; i < oldFileContentRows.size(); i++) {
                String row = oldFileContentRows.get(i);
                if (sh.isComments(row.trim()) && commentLine.matches(sh.trimComments(row.trim()))) {
                    String clta = sh.toCommentLine(commentLine.getReplacement());
                    if (!clta.equals(row)) {
//                        updatedFile = true;
                    }
                    if (newFileContentRows.size() > 0) {
                        if (newFileContentRows.get(newFileContentRows.size() - 1).trim().length() > 0) {
                            newFileContentRows.add("");
                        }
                    }
                    newFileContentRows.add(clta);
                    found = true;
                    i++;
                    List<String> old = new ArrayList<>();
                    while (i < oldFileContentRows.size()) {
                        String s = oldFileContentRows.get(i);
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
                    newFileContentRows.addAll(contentToAddRows);
                    newFileContentRows.add("");
//                    if (!old.equals(contentToAddRows)) {
//                    }
                    for (; i < oldFileContentRows.size(); i++) {
                        newFileContentRows.add(oldFileContentRows.get(i));
                    }
                } else {
                    newFileContentRows.add(row);
                }
            }
        }
        if (header != null) {
            if (newFileContentRows.size() == 0 || !header.matches(newFileContentRows.get(0).trim())) {
                newFileContentRows.add(0, header.getReplacement());
            }
        }
        if (!found) {
            if (newFileContentRows.size() > 0 && !newFileContentRows.get(0).trim().isEmpty()) {
                newFileContentRows.add("");
            }
            newFileContentRows.add(sh.toCommentLine(commentLine.getReplacement()));
            newFileContentRows.addAll(contentToAddRows);
            newFileContentRows.add("");
        } else {
//            if (lines.size() > 0) {
//                if (lines.get(lines.size() - 1).trim().length() > 0) {
//                    lines.add("");
//                }
//            }
        }
//        byte[] oldContent = NdiUtils.loadFile(filePath);
//        String oldContentString = oldContent == null ? "" : new String(oldContent);
        byte[] newContent = (String.join(sh.newlineString(), newFileContentRows)).getBytes();
//        String newContentString = new String(newContent);
//        PathInfo.Status s = NdiUtils.tryWriteStatus(newContent, filePath,session);
        return new PathInfo(type, id, filePath, CoreIOUtils.tryWrite(newContent, filePath, session));
    }

    public PathInfo removeFileCommented2Lines(String type, NutsId id, Path filePath, String commentLine, boolean force, NutsShellFamily shellFamily) {
        filePath = filePath.toAbsolutePath();
        boolean alreadyExists = Files.exists(filePath);
        boolean found = false;
        boolean updatedFile = false;
        NutsShellHelper sh = NutsShellHelper.of(shellFamily);
        try {
            List<String> lines = new ArrayList<>();
            if (Files.isRegularFile(filePath)) {
                String fileContent = new String(Files.readAllBytes(filePath));
                String[] fileRows = fileContent.split("[\n\r]");
                for (int i = 0; i < fileRows.length; i++) {
                    String row = fileRows[i];
                    if (row.trim().equals(sh.toCommentLine(commentLine))) {
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
                Files.write(filePath, (String.join(sh.newlineString(), lines) + sh.newlineString()).getBytes());
            }
            return new PathInfo(type, id, filePath, updatedFile ? alreadyExists ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED : PathInfo.Status.DISCARDED);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected abstract String getExecFileName(String name);

    protected abstract FreeDesktopEntryWriter createFreeDesktopEntryWriter();

    public PathInfo[] createShortcut(NutsDesktopIntegrationItem nutsDesktopIntegrationItem, NutsId id, String path, FreeDesktopEntry.Group shortcut) {
        List<PathInfo> results = new ArrayList<>();
        FreeDesktopEntryWriter ww = createFreeDesktopEntryWriter();
        if (nutsDesktopIntegrationItem == NutsDesktopIntegrationItem.DESKTOP) {
            results.addAll(Arrays.asList(ww.writeDesktop(shortcut, path, true, id)));
        } else if (nutsDesktopIntegrationItem == NutsDesktopIntegrationItem.MENU) {
            results.addAll(Arrays.asList(ww.writeMenu(shortcut, path, true, id)));
        } else if (nutsDesktopIntegrationItem == NutsDesktopIntegrationItem.SHORTCUT) {
            results.addAll(Arrays.asList(ww.writeShortcut(shortcut, path == null ? null : Paths.get(path), true, id)));
        } else {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported"));
        }
        return results.toArray(new PathInfo[0]);
    }

    /**
     * bigger is better
     *
     * @param extension extension
     * @return extension support order (bigger is better, 0 or less is ignored)
     */
    protected int resolveIconExtensionPriority(String extension) {
        extension = extension.toLowerCase();
        switch (extension) {
            case "svg":
                return 10;
            case "png":
                return 8;
            case "jpg":
                return 6;
            case "jpeg":
                return 5;
            case "gif":
                return 4;
            case "ico":
                return 3;
        }
        return -1;
    }

    protected int compareIconExtensions(String a, String b) {
        int ai = resolveIconExtensionPriority(a);
        int bi = resolveIconExtensionPriority(b);
        //bigger is first
        return Integer.compare(bi, ai);
    }

    protected int compareIconPaths(String a, String b) {
        String n1 = session.io().path(a).getLastExtension();
        String n2 = session.io().path(b).getLastExtension();
        return compareIconExtensions(n1, n2);
    }

    protected String resolveBestIcon(String... iconPaths) {
        if (iconPaths != null) {
            List<String> all = Arrays.stream(iconPaths).map(x -> (x == null) ? "" : x.trim())
                    .filter(x -> !x.isEmpty())
                    .filter(x ->
                            resolveIconExtensionPriority(session.io().path(x).getLastExtension()) >= 0
                    )
                    .sorted(this::compareIconPaths).collect(Collectors.toList());
            if (all.size() > 0) {
                return all.get(0);
            }
        }
        return null;
    }

    public String resolveIcon(String iconPath, NutsId appId) {
        if (iconPath != null && iconPath.length() > 0) {
            return iconPath;
        }
        return getPreferredIconPath(appId);
    }

    public String getPreferredIconPath(NutsId appId) {
        NutsDefinition appDef = session.search().addId(appId).setLatest(true).setEffective(true).setDistinct(true).getResultDefinitions().singleton();
        String descAppIcon = resolveBestIcon(appDef.getDescriptor().getIcons());
        if (descAppIcon == null) {
            if (isNutsBootId(appDef.getId())
                    || appDef.getId().getGroupId().startsWith("net.thevpc.nuts")
            ) {
                //get default icon
                NutsId appId0 = session.getAppId();
                if (appId0 == null) {
                    appId0 = session.getWorkspace().getRuntimeId();
                }
                descAppIcon =
                        resolveBestIcon(
                                "nuts-resource://" + appId0.getLongName() + "/net/thevpc/nuts/runtime/nuts.svg",
                                "nuts-resource://" + appId0.getLongName() + "/net/thevpc/nuts/runtime/nuts.png",
                                "nuts-resource://" + appId0.getLongName() + "/net/thevpc/nuts/runtime/nuts.ico"
                        );
            }
        }
        String iconPath = null;
        if (descAppIcon != null) {
            String descAppIcon0 = descAppIcon;
            String descAppIconDigest = session.io().hash().md5().setSource(new ByteArrayInputStream(descAppIcon0.getBytes())).computeString();
            NutsPath p0 = NutsPath.of(descAppIcon, session);
            if (descAppIcon.startsWith("classpath://")) {
                descAppIcon = "nuts-resource://" + appDef.getId().getLongName() + "" + descAppIcon.substring("classpath://".length() - 1);
            }
            String bestName = descAppIconDigest + "." + p0.getLastExtension();
            Path localIconPath = Paths.get(session.locations().getStoreLocation(appDef.getId(), NutsStoreLocation.CACHE))
                    .resolve("icons")
                    .resolve(bestName);
            if (Files.isRegularFile(localIconPath)) {
                iconPath = localIconPath.toString();
            } else {
                NutsPath p = session.io().path(descAppIcon);
                if (p.exists()) {
                    session.io().copy()
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

    public Path getShortcutPath(NdiScriptOptions options) {
        NutsDefinition appDef = options.getSession().search()
                .addId(options.getId())
                .setLatest(true)
                .setEffective(true)
                .setDistinct(true)
                .getResultDefinitions().singleton();

        String fileName = options.getLauncher().getCustomScriptPath();
        fileName = resolveShortcutFileName(appDef.getId(), appDef.getDescriptor(), fileName, null);
        return Paths.get(fileName);
    }

    public PathInfo[] createShortcut(NutsDesktopIntegrationItem nutsDesktopIntegrationItem, NdiScriptOptions options) {
        String apiVersion = options.getNutsApiVersion().toString();
        if (apiVersion == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing nuts-api version to link to"));
        }
        NutsId apiId = session.getWorkspace().getApiId().builder().setVersion(apiVersion).build();
        NutsDefinition apiDefinition = session.search().addId(apiId).setFailFast(true).setLatest(true).setContent(true)
                .setDistinct(true)
                .getResultDefinitions().singleton();

        NutsId appId = options.getSession().id().parser().parse(options.getId());
        NutsDefinition appDef = loadIdDefinition(appId);
        List<String> cmd = new ArrayList<>();

        cmd.add(getNutsStart(options).path().toString());
        cmd.add("-y");
        cmd.add(appId.toString());
        if (options.getLauncher().getArgs() != null) {
            cmd.addAll(options.getLauncher().getArgs());
        }
        String cwd = options.getLauncher().getWorkingDirectory();
        if (cwd == null) {
            //should it be id's var folder?
            cwd = System.getProperty("user.home");
        }
        String iconPath = resolveIcon(options.getLauncher().getIcon(), appId);

        String shortcutName = options.getLauncher().getShortcutName();
        if (shortcutName == null) {
            if (nutsDesktopIntegrationItem == NutsDesktopIntegrationItem.SHORTCUT) {
                shortcutName = options.getLauncher().getCustomShortcutPath();
                if (shortcutName == null) {
                    shortcutName = options.getLauncher().getCustomScriptPath();
                }
            }
        }
        shortcutName = NameBuilder.extractPathName(shortcutName);
        if (shortcutName.isEmpty()) {
            shortcutName = "%N";
        }
        shortcutName += "%s%v%s%h";
        shortcutName = NameBuilder.label(appDef.getId(), shortcutName, null, appDef.getDescriptor(), session).buildName();

        String execCmd = session.commandLine().create(cmd.toArray(new String[0])).toString();
        FreeDesktopEntry.Group sl = FreeDesktopEntry.Group.desktopEntry(shortcutName, execCmd, cwd);
        sl.setStartNotify(true);
        sl.setIcon(iconPath);
        sl.setGenericName(apiDefinition.getDescriptor().getGenericName());
        sl.setComment(appDef.getDescriptor().getDescription());
        sl.setTerminal(options.getLauncher().isOpenTerminal());
        if (options.getLauncher().getMenuCategory() != null) {
            sl.addCategory(options.getLauncher().getMenuCategory());
        } else {
            sl.setCategories(Arrays.asList(appDef.getDescriptor().getCategories()));
        }
        String preferredPath = getShortcutPath(options).toString();
        return createShortcut(nutsDesktopIntegrationItem, appId, preferredPath, sl);
    }

    protected String getDefaultIconPath() {
        return "apper";
    }


    public PathInfo[] createLaunchTermShortcutGlobal(NutsDesktopIntegrationItem nutsDesktopIntegrationItem, NdiScriptOptions options) {
        String fileName = options.resolveNutsApiId().getShortName().replace(':', '-');
        String name = "Nuts Terminal";
        return createLaunchTermShortcut(nutsDesktopIntegrationItem, options, fileName, name);
    }

    public abstract boolean isShortcutFileNameUserFriendly();

    public String resolveShortcutFileName(NutsId id, NutsDescriptor descriptor, String fileName, String name) {
        if (NutsBlankable.isBlank(fileName)) {
            if (isShortcutFileNameUserFriendly()) {
                fileName = name;
            }
            if (NutsBlankable.isBlank(fileName)) {
                if (isShortcutFileNameUserFriendly()) {
                    fileName = "%N%s%v%s%h";
                } else {
//                    fileName = "%g-%n-%v-%h";
                    fileName = "%g-%n-%v%s%h";
                }
            }
        }
        fileName =
                (isShortcutFileNameUserFriendly() ?
                        NameBuilder.label(id, fileName, null, descriptor, session)
                        : NameBuilder.id(id, fileName, null, descriptor, session)
                )
                        .buildName();
        return fileName;
    }

    public PathInfo[] createLaunchTermShortcut(NutsDesktopIntegrationItem nutsDesktopIntegrationItem,
                                               NdiScriptOptions options,
                                               String fileName, String name
    ) {
        String cmd = getNutsTerm(options)[0].path().toString();
        fileName = resolveShortcutFileName(options.resolveNutsApiId(), options.resolveNutsApiDef().getDescriptor(), fileName, name);
        if (name == null) {
            name = NameBuilder.label(options.resolveNutsApiId(), "Nuts Terminal%s%v%s%h", null, options.resolveNutsApiDef().getDescriptor(), session)
                    .buildName();
        }
        String execCmd = session.commandLine().create(cmd).toString();
        return createShortcut(nutsDesktopIntegrationItem,
                options.resolveNutsApiId(),
                fileName,
                FreeDesktopEntry.Group.desktopEntry(name, execCmd, System.getProperty("user.home"))
                        .setIcon(resolveIcon(null, options.resolveNutsApiId()))
                        .setStartNotify(true)
                        .addCategory("/Utility/Nuts")
                        .setGenericName(options.resolveNutsApiDef().getDescriptor().getGenericName())
                        .setComment(options.resolveNutsApiDef().getDescriptor().getDescription())
                        .setTerminal(true)
        );
    }


//    public String createNutsEnvString(NdiScriptOptions options, boolean updateEnv, boolean updatePATH) {
//        final NutsWorkspace ws = session.getWorkspace();
//        String NUTS_JAR_PATH = ws.search()
//                .setSession(session.copy())
//                .addId(ws.getApiId()).getResultPaths().required();
//
//        TreeSet<String> exports = new TreeSet<>();
//        SimpleScriptBuilder tmp = scriptBuilderSimple("nuts-env", options.resolveNutsApiId(), options);
//        if (updateEnv) {
//            exports.addAll(Arrays.asList("NUTS_VERSION", "NUTS_WORKSPACE", "NUTS_JAR", "NUTS_WORKSPACE_BINDIR"));
//            tmp.printSetStatic("NUTS_VERSION", ws.getApiVersion().toString());
//            tmp.printSetStatic("NUTS_WORKSPACE", ws.locations().getWorkspaceLocation());
//            for (NutsStoreLocation value : NutsStoreLocation.values()) {
//                tmp.printSetStatic("NUTS_WORKSPACE_" + value, ws.locations().getStoreLocation(value));
//                exports.add("NUTS_WORKSPACE_" + value);
//            }
//            if (NUTS_JAR_PATH.startsWith(ws.locations().getStoreLocation(NutsStoreLocation.LIB))) {
//                String pp = NUTS_JAR_PATH.substring(ws.locations().getStoreLocation(NutsStoreLocation.LIB).length());
//                tmp.printSet("NUTS_JAR", varRef("NUTS_WORKSPACE_LIB") + pp);
//            } else {
//                tmp.printSetStatic("NUTS_JAR", NUTS_JAR_PATH);
//            }
//            String p0 = options.resolveBinFolder().toString().substring(
//                    ws.locations().getStoreLocation(NutsStoreLocation.APPS).length()
//            );
//            tmp.printSet("NUTS_WORKSPACE_BINDIR", varRef("NUTS_WORKSPACE_APPS") + p0);
//        }
//        if (updatePATH) {
//            exports.add("PATH");
//            tmp.printSet("PATH", varRef("NUTS_WORKSPACE_BINDIR") + getPathVarSep() + varRef("PATH"));
//        }
//        String export = getExportCommand(exports.toArray(new String[0]));
//        if (!NutsBlankable.isBlank(export)) {
//            tmp.println(export);
//        }
//        return tmp.buildString();
//    }

    public ReplaceString getCommentLineConfigHeader() {
        return COMMENT_LINE_CONFIG_HEADER;
    }

    public abstract String getTemplateName(String name, NutsShellFamily shellFamily);

    private class RcNdiScriptInfo implements NdiScriptInfo {
        private final String bashrcName;
        private final NdiScriptOptions options;
        private final NutsShellFamily shellFamily;

        public RcNdiScriptInfo(String bashrcName, NdiScriptOptions options, NutsShellFamily shellFamily) {
            this.bashrcName = bashrcName;
            this.options = options;
            this.shellFamily = shellFamily;
        }

        @Override
        public Path path() {
            if (bashrcName == null) {
                return null;
            }
            return Paths.get(System.getProperty("user.home")).resolve(bashrcName);
        }

        @Override
        public PathInfo create() {
            Path apiConfigFile = path();
            if (apiConfigFile == null) {
                return null;
            }
            NutsShellHelper sh = NutsShellHelper.of(shellFamily);
            return addFileLine("sysrc",
                    options.resolveNutsApiId(),
                    apiConfigFile, getCommentLineConfigHeader(),
                    sh.getCallScriptCommand(getIncludeNutsInit(options, shellFamily).path().toString()),
                    sh.getShebanSh(), shellFamily);
        }
    }

}
