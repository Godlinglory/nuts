package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptOptions;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptnfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.WorkspaceAndApiVersion;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util.NdiUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class BaseSystemNdi extends AbstractSystemNdi {

    private static final Logger LOG = Logger.getLogger(BaseSystemNdi.class.getName());

    public BaseSystemNdi(NutsApplicationContext appContext) {
        super(appContext);
    }

    public Path getScriptFile(String name) {
        Path bin = Paths.get(context.getAppsFolder());
        return bin.resolve(getExecFileName(name));
    }

    protected abstract String createNutsScriptCommand(NutsId fnutsId, NdiScriptOptions options);

    public String createBootScriptCommand(NutsDefinition f, boolean includeEnv) {
        String txt = NdiUtils.generateScriptAsString("/net/thevpc/nuts/toolbox/nadmin/" + getTemplateNutsName(),
                ss -> {
                    switch (ss) {
                        case "NUTS_JAR":
                            return f.getPath().toString();
                    }
                    return null;
                }
        );
        if (includeEnv) {
            Path ndiAppsFolder = Paths.get(context.getAppsFolder());
            Path ndiConfigFile = ndiAppsFolder.resolve(getExecFileName(".nadmin-bashrc"));
            txt = getCallScriptCommand(ndiConfigFile.toString()) + "\n"
                    + txt;
        }
        return txt;
    }

    public WorkspaceAndApiVersion persistConfig(NutsWorkspaceBootConfig bootConfig, String apiVersion, String preferredName, NutsSession session) {
        NutsWorkspace ws = context.getWorkspace();
        if (session == null) {
            throw new NutsIllegalArgumentException(session, "missing session");
        }
        if (apiVersion == null) {
            if (bootConfig == null) {
                apiVersion = ws.getApiVersion();
            } else {
                NutsVersion _latestVersion = null;
                try {
                    _latestVersion = Files.list(
                            Paths.get(bootConfig.getStoreLocation(ws.getApiId(), NutsStoreLocation.CONFIG))
                                    .getParent())
                            .filter(
                                    f
                                    -> ws.version().parse(f.getFileName().toString()).getNumber(0, -1) != -1
                                    && Files.exists(f.resolve("nuts-api-config.json"))
                            ).map(
                                    f -> ws.version().parse(f.getFileName().toString())
                            ).sorted(Comparator.reverseOrder()).findFirst().orElse(null);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                if (_latestVersion == null) {
                    throw new NutsIllegalArgumentException(session, "missing nuts-api version to link to");
                }
                apiVersion = _latestVersion.toString();
            }
        }
        NutsId apiId = ws.getApiId().builder().setVersion(apiVersion).build();
        ws.fetch().setSession(session).setId(apiId).setFailFast(true).getResultDefinition();
        String wsEff = bootConfig != null ? bootConfig.getEffectiveWorkspace()
                : ws.locations().getWorkspaceLocation().toString();
        UpdatedPaths t = persistConfig2(bootConfig, apiId, preferredName, session);
        return new WorkspaceAndApiVersion(wsEff, ws.version().parse(apiVersion), t.getUpdated(), t.getDiscarded());
    }

    @Override
    public boolean isNutsBootId(NutsId nid) {
        if ("nuts".equals(nid.getShortName()) || "net.thevpc.nuts:nuts".equals(nid.getShortName())) {
            return true;
        }
        return false;
    }

    @Override
    public List<NdiScriptnfo> createNutsScript(
            List<String> idsToInstall,
            String switchWorkspaceLocation,
            String linkName,
            Boolean persistentConfig,
            ArrayList<String> executorOptions,
            boolean env,
            boolean fetch,
            NutsExecutionType execType,
            NutsApplicationContext context) {
        NutsWorkspace ws = context.getWorkspace();
        Path workspaceLocation = Paths.get(ws.locations().getWorkspaceLocation());
        List<NdiScriptnfo> result = new ArrayList<>();
        boolean subTrace = context.getSession().isTrace();
        if (!context.getSession().isPlainTrace()) {
            subTrace = false;
        }
        if (!idsToInstall.isEmpty()) {
            if (persistentConfig == null) {
                if (workspaceLocation.equals(Paths.get(System.getProperty("user.home")).resolve(".config/nuts/default-workspace"))) {
                    persistentConfig = true;
                } else {
                    persistentConfig = false;
                }
            }
            boolean includeEnv = env;
            for (String id : idsToInstall) {
                NutsId nid = ws.id().parser().parse(id);
                if (nid == null) {
                    throw new NutsExecutionException(context.getSession(), "unable to create script for " + id + " : invalid id", 100);
                }
                if (!nid.getVersion().isBlank()) {
                    includeEnv = true;
                }
            }
            String linkNameCurrent = linkName;
            if (includeEnv) {
                linkNameCurrent = prepareLinkName(linkNameCurrent);
            }
            List<String> nutsIds = idsToInstall.stream().filter(x -> isNutsBootId(ws.id().parser().parse(x))).collect(Collectors.toList());
            List<String> nonNutsIds = idsToInstall.stream().filter(x -> !isNutsBootId(ws.id().parser().parse(x))).collect(Collectors.toList());
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

                    result.addAll(
                            createNutsScript(
                                    new NdiScriptOptions().setId(id)
                                            .setSession(context.getSession().copy().setTrace(subTrace))
                                            .setForceBoot(context.getSession().isYes())
                                            .setFetch(fetch)
                                            .setExecType(execType)
                                            .setExecutorOptions(executorOptions)
                                            .setIncludeEnv(includeEnv)
                                            .setPreferredScriptName(linkNameCurrent)
                            )
                    );
                } catch (UncheckedIOException e) {
                    throw new NutsExecutionException(context.getSession(), "unable to add script for " + id + " : " + e.toString(), e);
                }
            }
            if (!bootAlreadyProcessed && !nonNutsIds.isEmpty()) {
                result.addAll(createBootScript(
                        null,
                        null,
                        false, false, includeEnv));
            }
            for (String id : nonNutsIds) {
                try {
                    NutsId nid = ws.id().parser().parse(id);
                    if (nid == null) {
                        throw new NutsExecutionException(context.getSession(), "unable to create script for " + id + " : invalid id", 100);
                    }
                    result.addAll(
                            createNutsScript(
                                    new NdiScriptOptions().setId(id)
                                            .setAddNutsScript(false)
                                            .setSession(context.getSession().copy().setTrace(subTrace))
                                            .setForceBoot(context.getSession().isYes())
                                            .setFetch(fetch)
                                            .setExecType(execType)
                                            .setExecutorOptions(executorOptions)
                                            .setIncludeEnv(includeEnv)
                                            .setPreferredScriptName(linkNameCurrent)
                            )
                    );
                } catch (UncheckedIOException e) {
                    throw new NutsExecutionException(context.getSession(), "unable to add script for " + id + " : " + e.toString(), e);
                }
            }
            configurePath(context.getSession(), persistentConfig);
        }
        return result;
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

    @Override
    public List<NdiScriptnfo> createNutsScript(NdiScriptOptions options) {
        NutsId nid = context.getWorkspace().id().parser().parse(options.getId());
        if (isNutsBootId(nid)) {
            return createBootScript(
                    options.getPreferredScriptName(),
                    nid.getVersion().toString(),
                    options.isForceBoot() || options.getSession().isYes(), options.getSession().isTrace(), options.isIncludeEnv());
        } else {
            List<NdiScriptnfo> r = new ArrayList<>();
            if (options.isAddNutsScript()) {
                r.addAll(createBootScript(
                        null,
                        null,
                        false, false, options.isIncludeEnv()));
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
            Path ff = getScriptFile(n);
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
                final NutsId fnutsId = nid;
                NdiScriptnfo p = createScript(n, options.getPreferredScriptName(), fnutsId, options.getSession().isTrace(), nid.toString(),
                        x -> {
                            switch (x) {
                                case "NUTS_ID":
                                    return "RUN : " + fnutsId;
                                case "GENERATOR":
                                    return context.getAppId().toString();
                                case "BODY": {
                                    return createNutsScriptCommand(fnutsId, options);
                                }
                            }
                            return null;
                        }
                );
                r.add(p);
            }
            return r;
        }
    }

    @Override
    public void removeNutsScript(String id, NutsSession session) {
        NutsId nid = context.getWorkspace().id().parser().parse(id);
        Path f = getScriptFile(nid.getArtifactId());
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

    @Override
    public void addNutsWorkspaceScript(String preferredScriptName, String switchWorkspaceLocation, String apiVersion) {
        NutsWorkspaceBootConfig bootconfig = null;
        if (switchWorkspaceLocation != null) {
            bootconfig = context.getWorkspace().config().loadBootConfig(switchWorkspaceLocation, false, true);
            if (bootconfig == null) {
                throw new NutsIllegalArgumentException(context.getSession(), "invalid workspace: " + switchWorkspaceLocation);
            }
        }
        WorkspaceAndApiVersion v = persistConfig(bootconfig, apiVersion, preferredScriptName, context.getSession());
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("added script %s to point to %s %s\n", v.getWorkspace(), v.getApiVersion(), v.getUpdatedPaths());
        }
    }

    @Override
    public void switchWorkspace(String switchWorkspaceLocation, String apiVersion) {
        NutsWorkspaceBootConfig bootconfig = null;
        if (switchWorkspaceLocation != null) {
            bootconfig = context.getWorkspace().config().loadBootConfig(switchWorkspaceLocation, false, true);
            if (bootconfig == null) {
                throw new NutsIllegalArgumentException(context.getSession(), "invalid workspace: " + switchWorkspaceLocation);
            }
        }
        WorkspaceAndApiVersion v = persistConfig(bootconfig, apiVersion, null, context.getSession());
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("```sh nuts``` switched to workspace %s to point to %s\n", v.getWorkspace(), v.getApiVersion());
        }
    }

    protected abstract UpdatedPaths persistConfig2(NutsWorkspaceBootConfig bootConfig, NutsId nutsId, String rcPath, NutsSession session);

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

    public boolean addFileLine(Path filePath, String commentLine, String goodLine, boolean force, String ensureHeader, String headerReplace) {
        filePath = filePath.toAbsolutePath();
        boolean found = false;
        boolean updatedFile = false;
        List<String> lines = new ArrayList<>();
        if (Files.isRegularFile(filePath)) {
            String fileContent = null;
            try {
                fileContent = new String(Files.readAllBytes(filePath));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            String[] fileRows = fileContent.split("\n");
            if (ensureHeader != null) {
                if (fileRows.length == 0 || !fileRows[0].trim().matches(ensureHeader)) {
                    lines.add(headerReplace);
                    updatedFile = true;
                }
            }
            for (int i = 0; i < fileRows.length; i++) {
                String row = fileRows[i];
                if (row.trim().equals(toCommentLine(commentLine))) {
                    lines.add(row);
                    found = true;
                    i++;
                    if (i < fileRows.length) {
                        if (!fileRows[i].trim().equals(goodLine)) {
                            updatedFile = true;
                        }
                    }
                    lines.add(goodLine);
                    i++;
                    for (; i < fileRows.length; i++) {
                        lines.add(fileRows[i]);
                    }
                } else {
                    lines.add(row);
                }
            }
        }
        if (!found) {
            if (ensureHeader != null && headerReplace != null && lines.isEmpty()) {
                lines.add(headerReplace);
            }
            lines.add(toCommentLine(commentLine));
            lines.add(goodLine);
            updatedFile = true;
        }
        if (force || updatedFile) {
            try {
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, (String.join("\n", lines) + "\n").getBytes());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return updatedFile;
    }

    public boolean removeFileCommented2Lines(Path filePath, String commentLine, boolean force) {
        filePath = filePath.toAbsolutePath();
        boolean found = false;
        boolean updatedFile = false;
        try {
            List<String> lines = new ArrayList<>();
            if (Files.isRegularFile(filePath)) {
                String fileContent = new String(Files.readAllBytes(filePath));
                String[] fileRows = fileContent.split("\n");
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
                Files.write(filePath, (String.join("\n", lines) + "\n").getBytes());
            }
            return updatedFile;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected abstract String getCallScriptCommand(String path);

    public List<NdiScriptnfo> createBootScript(String preferredName, String apiVersion, boolean force, boolean trace, boolean includeEnv) {
        boolean currId = false;
        NutsId apiId = null;
        NutsId b = context.getWorkspace().getApiId();
        if (apiVersion == null || apiVersion.isEmpty()) {
            apiId = b;
            apiVersion = b.getVersion().toString();
            currId = true;
        } else {
            if (!apiVersion.equals(b.getVersion().toString())) {
                apiId = b.builder().setVersion(apiVersion).build();
            } else {
                apiId = b;
            }
        }
        NutsDefinition f = context.getWorkspace().search()
                .setSession(context.getSession().copy().setTrace(false))
                .addId(apiId).setOptional(false).setLatest(true).setContent(true).getResultDefinitions().required();
        Path script = null;
        boolean standardPath = true;
        if (preferredName != null && preferredName.length() > 0) {
            standardPath = false;
            if (preferredName.contains("%v")) {
                preferredName = preferredName.replace("%v", apiId.getVersion().toString());
            }
            script = Paths.get(preferredName);
        } else {
            if (currId) {
                script = getScriptFile("nuts");
            } else {
                script = getScriptFile("nuts-" + apiVersion);
            }
        }
        script = script.toAbsolutePath();
        String scriptString = script.toString();
        List<NdiScriptnfo> all = new ArrayList<>();
        boolean createPath = false;
        if (!force && Files.exists(script)) {
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
                    createScript("nuts", script.toString(), b, trace, f.getId().getLongName(),
                            x -> {
                                switch (x) {
                                    case "NUTS_ID":
                                        return f.getId().toString();
                                    case "GENERATOR":
                                        return context.getAppId().toString();
                                    case "BODY":
                                        return createBootScriptCommand(f, includeEnv);
                                }
                                return null;
                            }
                    ));
        }
        if (currId && standardPath) {
            Path ff2 = Paths.get(context.getWorkspace().locations().getWorkspaceLocation())
                    .resolve("nuts");
            boolean overridden = Files.exists(ff2);
            boolean gen = true;

            if (!force && Files.exists(ff2)) {
                if (!context.getSession().getTerminal().ask()
                        .resetLine()
                        .setSession(context.getSession())
                        .forBoolean("override existing script %s ?",
                                context.getWorkspace().text().forStyled(NdiUtils.betterPath(ff2.toString()), NutsTextStyle.path()))
                        .setDefaultValue(false)
                        .getBooleanValue()) {
                    gen = false;
                }
            }
            if (gen) {

                if (trace && context.getSession().isPlainTrace()) {
                    context.getSession().out().printf((Files.exists(ff2) ? "re-install" : "install")
                            + " script %s %n",
                            context.getWorkspace().text().forStyled(NdiUtils.betterPath(ff2.toString()), NutsTextStyle.path())
                    );
                }
                try {
                    try (BufferedWriter w = Files.newBufferedWriter(ff2)) {
                        NdiUtils.generateScript("/net/thevpc/nuts/toolbox/nadmin/" + getTemplateBodyName(), w, x -> {
                            switch (x) {
                                case "NUTS_ID":
                                    return f.getId().toString();
                                case "GENERATOR":
                                    return context.getAppId().toString();
                                case "BODY": {
                                    return getCallScriptCommand(NdiUtils.replaceFilePrefix(scriptString, ff2.toString(), ""));
                                }
                            }
                            return null;
                        });
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                NdiUtils.setExecutable(ff2);
                all.add(new NdiScriptnfo("nuts", b, ff2, overridden));
            }
        }
        return all;
    }

    protected abstract String getExecFileName(String name);

    protected abstract String getTemplateBodyName();

    protected abstract String getTemplateNutsName();

    public NdiScriptnfo createScript(String name, String preferredName, NutsId fnutsId, boolean trace, String desc, Function<String, String> mapper) {
        try {
            Path script = getScriptFile(name);
            if (preferredName != null && preferredName.length() > 0) {
                if (preferredName.contains("%v")) {
                    preferredName = preferredName.replace("%v", fnutsId.getVersion().toString());
                }
                if (preferredName.contains("%n")) {
                    preferredName = preferredName.replace("%n", fnutsId.getArtifactId());
                }
                if (preferredName.contains("%g")) {
                    preferredName = preferredName.replace("%g", fnutsId.getGroupId());
                }
                script = Paths.get(preferredName);
            }
            if (script.getParent() != null) {
                if (!Files.exists(script.getParent())) {
                    Files.createDirectories(script.getParent());
                }
            }
            boolean _override = Files.exists(script);
            try (BufferedWriter w = Files.newBufferedWriter(script)) {
                NdiUtils.generateScript("/net/thevpc/nuts/toolbox/nadmin/" + getTemplateBodyName(), w, mapper);
            }
            NdiUtils.setExecutable(script);
            return new NdiScriptnfo(name, fnutsId, script, _override);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected NdiConfig loadNdiConfig(String nutsVersion) {
        Path t = Paths.get(context.getWorkspace().locations().getStoreLocation(context.getAppId(), NutsStoreLocation.CONFIG))
                .resolve("nadmin-config.json");
        if (Files.isRegularFile(t)) {
            return context.getWorkspace().elem().setContentType(NutsContentType.JSON).parse(t, NdiConfig.class);
        }
        return null;
    }

    protected void saveNdiConfig(NdiConfig config) {
        Path t = Paths.get(context.getWorkspace().locations().getStoreLocation(context.getAppId(), NutsStoreLocation.CONFIG))
                .resolve("nadmin-config.json");
        context.getWorkspace().elem().setContentType(NutsContentType.JSON).setCompact(false).setValue(config)
                .print(t);
    }

}
