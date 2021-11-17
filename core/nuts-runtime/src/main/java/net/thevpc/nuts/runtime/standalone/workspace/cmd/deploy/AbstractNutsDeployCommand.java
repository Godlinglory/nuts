package net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.NutsStreamOrPath;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NutsWorkspaceCommandBase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNutsDeployCommand extends NutsWorkspaceCommandBase<NutsDeployCommand> implements NutsDeployCommand {

    protected List<Result> result;
    protected NutsStreamOrPath content;
    protected Object descriptor;
    protected String sha1;
    protected String descSha1;
    protected String fromRepository;
    protected String toRepository;
    protected String[] parseOptions;
    protected final List<NutsId> ids = new ArrayList<>();

    protected static class Result{
        NutsString source;
        String repository;
        NutsId id;
        public Result(NutsId nid, String repository,NutsString source) {
            this.id=nid.getLongId();
            this.source=source;
            this.repository=repository;
        }
    }

    public AbstractNutsDeployCommand(NutsWorkspace ws) {
        super(ws, "deploy");
    }

    public String[] getParseOptions() {
        return parseOptions;
    }

    public NutsDeployCommand setParseOptions(String[] parseOptions) {
        this.parseOptions = parseOptions;
        return this;
    }

    public NutsDeployCommand parseOptions(String[] parseOptions) {
        this.parseOptions = parseOptions;
        return this;
    }

    @Override
    public NutsDeployCommand setContent(InputStream stream) {
        content = stream==null?null:NutsStreamOrPath.of(stream);
        return this;
    }

    @Override
    public NutsDeployCommand setContent(String path) {
        content = path==null?null:NutsStreamOrPath.of(path,getSession());
        return this;
    }

    @Override
    public NutsDeployCommand setContent(NutsPath path) {
        content = path==null?null:NutsStreamOrPath.of(path);
        return this;
    }

    @Override
    public NutsDeployCommand setContent(byte[] content) {
        this.content = content ==null?null:NutsStreamOrPath.of(new ByteArrayInputStream(content));
        return this;
    }

    @Override
    public NutsDeployCommand setContent(File file) {
        content = file==null?null:NutsStreamOrPath.of(file,getSession());
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setContent(Path file) {
        content = file==null?null:NutsStreamOrPath.of(file,getSession());
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(InputStream stream) {
        descriptor = stream;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(String path) {
        descriptor = path;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(File file) {
        descriptor = file;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(URL url) {
        descriptor = url;
        invalidateResult();
        return this;
    }

    @Override
    public String getSha1() {
        return sha1;
    }

    @Override
    public NutsDeployCommand setSha1(String sha1) {
        this.sha1 = sha1;
        invalidateResult();
        return this;
    }

    public String getDescSha1() {
        return descSha1;
    }

    @Override
    public NutsDeployCommand setDescSha1(String descSha1) {
        this.descSha1 = descSha1;
        invalidateResult();
        return this;
    }

    public NutsStreamOrPath getContent() {
        return content;
    }

    @Override
    public NutsDeployCommand setContent(URL url) {
        content = url==null?null:NutsStreamOrPath.of(url,getSession());
        invalidateResult();
        return this;
    }

    public Object getDescriptor() {
        return descriptor;
    }

    @Override
    public NutsDeployCommand setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        invalidateResult();
        return this;
    }

    @Override
    public String getTargetRepository() {
        return toRepository;
    }

    @Override
    public NutsDeployCommand to(String repository) {
        return setTargetRepository(repository);
    }

    @Override
    public NutsDeployCommand setRepository(String repository) {
        return setTargetRepository(repository);
    }

    @Override
    public NutsDeployCommand setTargetRepository(String repository) {
        this.toRepository = repository;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand from(String repository) {
        return setSourceRepository(repository);
    }

    @Override
    public NutsDeployCommand setSourceRepository(String repository) {
        this.fromRepository = repository;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(Path path) {
        this.descriptor = path;
        invalidateResult();
        return this;
    }

    @Override
    public NutsId[] getResult() {
        if (result == null) {
            run();
        }
        return result.stream().map(x->x.id).toArray(NutsId[]::new);
    }

    @Override
    protected void invalidateResult() {
        result = null;
    }

    protected void addResult(NutsId nid,String repository,NutsString source) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        if (result == null) {
            result = new ArrayList<>();
        }
        checkSession();
        result.add(new Result(nid,repository,source));
//        if (getSession().isPlainTrace()) {
//            getSession().getTerminal().out().resetLine().printf("Nuts %s deployed successfully to %s%n",
//                    nid,
//                    NutsTexts.of(session).ofStyled(toRepository == null ? "<default-repo>" : toRepository, NutsTextStyle.primary3())
//            );
//        }
    }

    @Override
    public NutsDeployCommand addIds(String... values) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        if (values != null) {
            for (String s : values) {
                if (!NutsBlankable.isBlank(s)) {
                    ids.add(NutsId.of(s,session));
                }
            }
        }
        return this;
    }

    @Override
    public NutsDeployCommand addIds(NutsId... value) {
        if (value != null) {
            for (NutsId s : value) {
                if (s != null) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NutsDeployCommand clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NutsDeployCommand addId(NutsId id) {
        if (id != null) {
            addId(id.toString());
        }
        return this;
    }

    @Override
    public NutsDeployCommand removeId(NutsId id) {
        if (id != null) {
            removeId(id.toString());
        }
        return this;
    }

    @Override
    public NutsDeployCommand removeId(String id) {
        checkSession();
        ids.remove(NutsId.of(id,session));
        return this;
    }

    @Override
    public NutsDeployCommand addId(String id) {
        checkSession();
        if (!NutsBlankable.isBlank(id)) {
            ids.add(NutsId.of(id,session));
        }
        return this;
    }

    @Override
    public NutsId[] getIds() {
        return this.ids.toArray(new NutsId[0]);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isEnabled();
        switch (a.getKey().getString()) {
            case "-d":
            case "--desc": {
                String val = cmdLine.nextString().getValue().getString();
                if (enabled) {
                    setDescriptor(val);
                }
                return true;
            }
            case "-s":
            case "--source":
            case "--from": {
                String val = cmdLine.nextString().getValue().getString();
                if (enabled) {
                    from(val);
                }
                return true;
            }
            case "-r":
            case "--target":
            case "--to": {
                String val = cmdLine.nextString().getValue().getString();
                if (enabled) {
                    to(val);
                }
                return true;
            }
            case "--desc-sha1": {
                String val = cmdLine.nextString().getValue().getString();
                if (enabled) {
                    this.setDescSha1(val);
                }
                return true;
            }
            case "--desc-sha1-file": {
                try {
                    String val = cmdLine.nextString().getValue().getString();
                    if (enabled) {
                        this.setDescSha1(new String(Files.readAllBytes(Paths.get(val))));
                    }
                } catch (IOException ex) {
                    checkSession();
                    throw new NutsIOException(getSession(), ex);
                }
                return true;
            }
            case "--sha1": {
                String val = cmdLine.nextString().getValue().getString();
                if (enabled) {
                    this.setSha1(val);
                }
                return true;
            }
            case "--sha1-file": {
                try {
                    String val = cmdLine.nextString().getValue().getString();
                    if (enabled) {
                        this.setSha1(new String(Files.readAllBytes(Paths.get(val))));
                    }
                } catch (IOException ex) {
                    checkSession();
                    throw new NutsIOException(getSession(), ex);
                }
                return true;
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                if (a.isOption()) {
                    cmdLine.unexpectedArgument();
                } else {
                    cmdLine.skip();
                    String idOrPath = a.getString();
                    if (idOrPath.indexOf('/') >= 0 || idOrPath.indexOf('\\') >= 0) {
                        setContent(idOrPath);
                    } else {
                        addId(idOrPath);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
