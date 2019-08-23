package net.vpc.app.nuts.toolbox.ndi;

import net.vpc.app.nuts.*;
import net.vpc.common.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
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
        return NdiUtils.generateScriptAsString("/net/vpc/app/nuts/toolbox/" + getTemplateNutsName(),
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

    public boolean addLine(String commentLine, String goodLine, File bashrc, boolean force) throws IOException {
        boolean found = false;
        boolean updatedBashrc = false;
        List<String> lines = new ArrayList<>();
        if (bashrc.isFile()) {
            String fileContent = IOUtils.loadString(bashrc);
            String[] fileRows = fileContent.split("\n");
            for (int i = 0; i < fileRows.length; i++) {
                String row = fileRows[i];
                if (row.trim().equals(toCommentLine(commentLine))) {
                    lines.add(row);
                    found = true;
                    i++;
                    if (i < fileRows.length) {
                        if (!fileRows[i].trim().equals(goodLine)) {
                            updatedBashrc = true;
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
            lines.add(toCommentLine(commentLine));
            lines.add(goodLine);
            updatedBashrc = true;
        }
        if (force || updatedBashrc) {
            IOUtils.saveString(String.join("\n", lines) + "\n", bashrc);
        }
        return updatedBashrc;
    }
    @Override
    public NdiScriptnfo[] createNutsScript(NdiScriptOptions options) throws IOException {
        NutsId nid = context.getWorkspace().id().parse(options.getId());
        if ("nuts".equals(nid.getShortName()) || "net.vpc.app.nuts:nuts".equals(nid.getShortName())) {
            return createBootScript(options.isForceBoot() || options.getSession().isYes(), options.getSession().isTrace());
        } else {
            List<NdiScriptnfo> r = new ArrayList<>(Arrays.asList(createBootScript(false, false)));
            NutsDefinition fetched = null;
            if (nid.getVersion().isBlank()) {
                fetched = context.getWorkspace().search()
                        .session(context.getSession().copy().trace(false))
                        .id(options.getId()).latest().getResultDefinitions().required();
                nid = fetched.getId().getShortNameId();
                //nutsId=fetched.getId().getLongNameId();
            }
            String n = nid.getArtifactId();
            Path ff = getScriptFile(n);
            boolean exists = Files.exists(ff);
            if (!options.getSession().isYes() && exists) {
                if (context.getSession().isPlainTrace()) {
                    context.session().out().printf("Script already exists ==%s==%n", NdiUtils.betterPath(ff.toString()));
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

    public NdiScriptnfo[] createBootScript(boolean force, boolean trace) throws IOException {
        NutsId b = context.getWorkspace().config().getApiId();
        NutsDefinition f = context.getWorkspace().search()
                .session(context.getSession().copy().trace(false))
                .id(b).setOptional(false).latest().content().getResultDefinitions().required();
        Path ff = getScriptFile("nuts");
        List<NdiScriptnfo> all = new ArrayList<>();
        if (!force && Files.exists(ff)) {
            if (trace && context.getSession().isPlainTrace()) {
                context.session().out().printf("Script already exists ==%s==%n", NdiUtils.betterPath(ff.toString()));
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
        Path ff2 = context.getWorkspace().config().getWorkspaceLocation().resolve("nuts");
        boolean overridden = Files.exists(ff2);
        if (!force && Files.exists(ff2)) {
            if (trace && context.getSession().isPlainTrace()) {
                context.session().out().printf("script already exists ==%s==%n", ff2);
            }
        } else {
            if (trace && context.getSession().isPlainTrace()) {
                context.session().out().printf("installing" +
                        (Files.exists(ff2) ? " (with override)" : "") +
                        " script ==%s== %n", NdiUtils.betterPath(ff2.toString()));
            }
            try (BufferedWriter w = Files.newBufferedWriter(ff2)) {
                NdiUtils.generateScript("/net/vpc/app/nuts/toolbox/" + getTemplateBodyName(), w, x -> {
                    switch (x) {
                        case "NUTS_ID":
                            return "BOOT : " + f.getId().toString();
                        case "BODY": {
                            String s = NdiUtils.longuestCommonParent(ff.toString(), ff2.toString());
                            if (s.length() > 0) {
                                return ff.toString().substring(s.length());
                            } else {
                                return ff.toString();
                            }
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


    protected abstract String getExecFileName(String name) ;

    protected abstract String getTemplateBodyName();

    protected abstract String getTemplateNutsName() ;

    public NdiScriptnfo createScript(String name, NutsId fnutsId, boolean trace, String desc, Function<String, String> mapper) throws IOException {
        Path script = getScriptFile(name);
        if (script.getParent() != null) {
            if (!Files.exists(script.getParent())) {
                Files.createDirectories(script.getParent());
            }
        }
        boolean _override = Files.exists(script);
        try (BufferedWriter w = Files.newBufferedWriter(script)) {
            NdiUtils.generateScript("/net/vpc/app/nuts/toolbox/" + getTemplateBodyName(), w, mapper);
        }
        NdiUtils.setExecutable(script);
        return new NdiScriptnfo(name, fnutsId, script, _override);
    }

    @Override
    public void removeNutsScript(String id, NutsSession session) throws IOException {
        NutsId nid = context.getWorkspace().id().parse(id);
        Path f = getScriptFile(nid.getArtifactId());
        if (Files.isRegularFile(f)) {
            if (session.terminal().ask().forBoolean("Tool ==%s== will be removed. Confirm?", NdiUtils.betterPath(f.toString()))
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
