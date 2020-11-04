package net.thevpc.nuts.toolbox.ndi.base;

import net.thevpc.nuts.toolbox.ndi.NdiScriptOptions;
import net.thevpc.nuts.toolbox.ndi.NdiScriptnfo;
import net.thevpc.nuts.toolbox.ndi.util.NdiUtils;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

public abstract class BaseSystemNdi extends AbstractSystemNdi {
    private static final Logger LOG = Logger.getLogger(BaseSystemNdi.class.getName());

    public BaseSystemNdi(NutsApplicationContext appContext) {
        super(appContext);
    }

    public Path getScriptFile(String name) {
        Path bin = context.getAppsFolder();
        return bin.resolve(getExecFileName(name));
    }

    protected abstract String createNutsScriptCommand(NutsId fnutsId, NdiScriptOptions options);

    public String createBootScriptCommand(NutsDefinition f) {
        return NdiUtils.generateScriptAsString("/net/thevpc/nuts/toolbox/" + getTemplateNutsName(),
                ss -> {
                    switch (ss) {
                        case "NUTS_JAR":
                            return f.getPath().toString();
                    }
                    return null;
                }
        );
    }

    public abstract String toCommentLine(String line);

    public boolean saveFile(Path filePath, String content, boolean force) throws IOException {
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
    }

    public boolean addFileLine(Path filePath, String commentLine, String goodLine, boolean force, String ensureHeader, String headerReplace) throws IOException {
        boolean found = false;
        boolean updatedFile = false;
        List<String> lines = new ArrayList<>();
        if (Files.isRegularFile(filePath)) {
            String fileContent = new String(Files.readAllBytes(filePath));
            String[] fileRows = fileContent.split("\n");
            if (ensureHeader != null) {
                if (fileRows.length == 0 || !fileRows[0].trim().matches(ensureHeader)) {
                    lines.add(headerReplace);
                    updatedFile=true;
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
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, (String.join("\n", lines) + "\n").getBytes());
        }
        return updatedFile;
    }

    public boolean removeFileLine(Path filePath, String commentLine, boolean force) throws IOException {
        boolean found = false;
        boolean updatedFile = false;
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
    }

    @Override
    public NdiScriptnfo[] createNutsScript(NdiScriptOptions options) throws IOException {
        NutsId nid = context.getWorkspace().id().parser().parse(options.getId());
        if ("nuts".equals(nid.getShortName()) || "net.thevpc.nuts:nuts".equals(nid.getShortName())) {
            return createBootScript(options.isForceBoot() || options.getSession().isYes(), options.getSession().isTrace());
        } else {
            List<NdiScriptnfo> r = new ArrayList<>(Arrays.asList(createBootScript(false, false)));
            NutsDefinition fetched = null;
            if (nid.getVersion().isBlank()) {
                fetched = context.getWorkspace().search()
                        .setSession(context.getSession().copy().setSilent())
                        .addId(options.getId()).setLatest(true).getResultDefinitions().required();
                nid = fetched.getId().getShortNameId();
                //nutsId=fetched.getId().getLongNameId();
            }
            String n = nid.getArtifactId();
            Path ff = getScriptFile(n);
            boolean exists = Files.exists(ff);
            if (!options.getSession().isYes() && exists) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("Script already exists ==%s==%n", NdiUtils.betterPath(ff.toString()));
                }
            } else {
                final NutsId fnutsId = nid;
                NdiScriptnfo p = createScript(n, fnutsId, options.getSession().isTrace(), nid.toString(),
                        x -> {
                            switch (x) {
                                case "NUTS_ID":
                                    return "RUN : " + fnutsId;
                                case "BODY": {
                                    return createNutsScriptCommand(fnutsId, options);
                                }
                            }
                            return null;
                        }
                );
                r.add(p);
            }
            return r.toArray(new NdiScriptnfo[0]);
        }
    }

    protected abstract String getCallScriptCommand(String path);

    public NdiScriptnfo[] createBootScript(boolean force, boolean trace) throws IOException {
        NutsId b = context.getWorkspace().getApiId();
        NutsDefinition f = context.getWorkspace().search()
                .setSession(context.getSession().copy().setSilent())
                .addId(b).setOptional(false).setLatest(true).setContent(true).getResultDefinitions().required();
        Path ff = getScriptFile("nuts");
        List<NdiScriptnfo> all = new ArrayList<>();
        if (!force && Files.exists(ff)) {
            if (trace && context.getSession().isPlainTrace()) {
                context.getSession().out().printf("Script already exists ==%s==%n", NdiUtils.betterPath(ff.toString()));
            }
        } else {
            all.add(
                    createScript("nuts", b, trace, f.getId().getLongName(),
                            x -> {
                                switch (x) {
                                    case "NUTS_ID":
                                        return "BOOT : " + f.getId().toString();
                                    case "BODY":
                                        return createBootScriptCommand(f);
                                }
                                return null;
                            }
                    ));
        }
        Path ff2 = context.getWorkspace().locations().getWorkspaceLocation().resolve("nuts");
        boolean overridden = Files.exists(ff2);
        if (!force && Files.exists(ff2)) {
            if (trace && context.getSession().isPlainTrace()) {
                context.getSession().out().printf("script already exists ==%s==%n", ff2);
            }
        } else {
            if (trace && context.getSession().isPlainTrace()) {
                context.getSession().out().printf((Files.exists(ff2) ? "re-installing" : "installing") +
                        " script ==%s== %n", NdiUtils.betterPath(ff2.toString()));
            }
            try (BufferedWriter w = Files.newBufferedWriter(ff2)) {
                NdiUtils.generateScript("/net/thevpc/nuts/toolbox/" + getTemplateBodyName(), w, x -> {
                    switch (x) {
                        case "NUTS_ID":
                            return "BOOT : " + f.getId().toString();
                        case "BODY": {
                            return getCallScriptCommand(NdiUtils.replaceFilePrefix(ff.toString(), ff2.toString(), ""));
                        }
                    }
                    return null;
                });
            }
            NdiUtils.setExecutable(ff2);
            all.add(new NdiScriptnfo("nuts", b, ff2, overridden));
        }
        return all.toArray(new NdiScriptnfo[0]);
    }


    protected abstract String getExecFileName(String name);

    protected abstract String getTemplateBodyName();

    protected abstract String getTemplateNutsName();

    public NdiScriptnfo createScript(String name, NutsId fnutsId, boolean trace, String desc, Function<String, String> mapper) throws IOException {
        Path script = getScriptFile(name);
        if (script.getParent() != null) {
            if (!Files.exists(script.getParent())) {
                Files.createDirectories(script.getParent());
            }
        }
        boolean _override = Files.exists(script);
        try (BufferedWriter w = Files.newBufferedWriter(script)) {
            NdiUtils.generateScript("/net/thevpc/nuts/toolbox/" + getTemplateBodyName(), w, mapper);
        }
        NdiUtils.setExecutable(script);
        return new NdiScriptnfo(name, fnutsId, script, _override);
    }

    @Override
    public void removeNutsScript(String id, NutsSession session) throws IOException {
        NutsId nid = context.getWorkspace().id().parser().parse(id);
        Path f = getScriptFile(nid.getArtifactId());
        if (Files.isRegularFile(f)) {
            if (session.getTerminal().ask().forBoolean("Tool ==%s== will be removed. Confirm?", NdiUtils.betterPath(f.toString()))
                    .defaultValue(true)
                    .getBooleanValue()) {
                Files.delete(f);
                if (session.isPlainTrace()) {
                    session.out().printf("Tool ==%s== removed.%n", NdiUtils.betterPath(f.toString()));
                }
            }
        }
    }

}
