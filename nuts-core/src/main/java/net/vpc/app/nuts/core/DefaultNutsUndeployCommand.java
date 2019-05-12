package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

public class DefaultNutsUndeployCommand extends NutsWorkspaceCommandBase<NutsUndeployCommand> implements NutsUndeployCommand {

    private List<NutsId> result;
    private final List<NutsId> ids = new ArrayList<>();
    private String repository;
    private boolean offline = true;
    private boolean transitive = true;

    public DefaultNutsUndeployCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsId[] getIds() {
        return ids.toArray(new NutsId[0]);
    }

    @Override
    public NutsUndeployCommand id(NutsId id) {
        return addId(id);
    }

    @Override
    public NutsUndeployCommand id(String id) {
        return addId(id);
    }

    @Override
    public NutsUndeployCommand addId(NutsId id) {
        if (id != null) {
            ids.add(id);
        }
        invalidateResult();
        return this;
    }

    @Override
    public NutsUndeployCommand addId(String id) {
        return addId(CoreStringUtils.isBlank(id) ? null : ws.parser().parseRequiredId(id));
    }

    @Override
    public NutsUndeployCommand ids(String... values) {
        return addIds(values);
    }

    @Override
    public NutsUndeployCommand addIds(String... values) {
        if (values != null) {
            for (String s : values) {
                if (!CoreStringUtils.isBlank(s)) {
                    ids.add(ws.parser().parseRequiredId(s));
                }
            }
        }
        return this;
    }

    @Override
    public NutsUndeployCommand ids(NutsId... values) {
        return addIds(values);
    }

    @Override
    public NutsUndeployCommand addIds(NutsId... value) {
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
    public NutsUndeployCommand clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NutsUndeployCommand setRepository(String repository) {
        this.repository = repository;
        invalidateResult();
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NutsUndeployCommand setTransitive(boolean transitive) {
        this.transitive = transitive;
        invalidateResult();
        return this;
    }

    @Override
    public NutsUndeployCommand repository(String repository) {
        return setRepository(repository);
    }

    @Override
    public NutsUndeployCommand transitive() {
        return setTransitive(true);
    }

    @Override
    public NutsUndeployCommand transitive(boolean transitive) {
        return setTransitive(transitive);
    }

    @Override
    public NutsUndeployCommand run() {
        NutsWorkspaceUtils.checkReadOnly(ws);

        NutsFetchCommand fetchOptions = ws.fetch().setTransitive(this.isTransitive());
        if (ids.isEmpty()) {
            throw new NutsExecutionException("No component to undeploy", 1);
        }
        for (NutsId id : ids) {
            NutsDefinition p = ws.find()
                    .ids(id)
                    .repositories(getRepository())
                    .setTransitive(isTransitive())
                    .setFetchStratery(isOffline() ? NutsFetchStrategy.LOCAL : NutsFetchStrategy.LOCAL)
                    .duplicateVersions(false)
                    .lenient(false)
                    .getResultDefinitions().required();
            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(getValidSession(), p.getRepository(), NutsFetchMode.LOCAL, fetchOptions);
            p.getRepository().undeploy()
                    .id(p.getId()).session(rsession)
                    .run();
            addResult(id);
        }
        if (getValidSession().isTrace()) {
            if (getValidSession().getOutputFormat() == null || getValidSession().getOutputFormat() == NutsOutputFormat.PLAIN) {
                if (getValidSession().getOutputFormat() != null && getValidSession().getOutputFormat() != NutsOutputFormat.PLAIN) {
                    switch (getValidSession().getOutputFormat()) {
                        case JSON: {
                            getValidSession().getTerminal().out().printf(ws.io().json().pretty().toJsonString(result));
                            break;
                        }
                        case PROPS: {
                            Map<String, String> props = new LinkedHashMap<>();
                            for (int i = 0; i < result.size(); i++) {
                                props.put(String.valueOf(i + 1), result.get(i).toString());
                            }
                            CoreIOUtils.storeProperties(props, getValidSession().getTerminal().out());
                            break;
                        }

                    }
                }
            }
        }
        return this;
    }

    private void addResult(NutsId id) {
        if (result == null) {
            result = new ArrayList<>();
        }
        result.add(id);
        if (getValidSession().isTrace()) {
            if (getValidSession().getOutputFormat() == null || getValidSession().getOutputFormat() == NutsOutputFormat.PLAIN) {
                if (getValidSession().getOutputFormat() == null || getValidSession().getOutputFormat() == NutsOutputFormat.PLAIN) {
                    getValidSession().getTerminal().out().printf("Nuts %N undeployed successfully%n", ws.formatter().createIdFormat().toString(id));
                }
            }
        }
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NutsUndeployCommand setOffline(boolean offline) {
        this.offline = offline;
        invalidateResult();
        return this;
    }

    @Override
    public NutsUndeployCommand offline() {
        return offline(true);
    }

    @Override
    public NutsUndeployCommand offline(boolean offline) {
        return setOffline(offline);
    }

    @Override
    protected void invalidateResult() {
        result = null;
    }

    @Override
    public NutsUndeployCommand parseOptions(String... args) {
        NutsCommandLine cmd = ws.parser().parseCommandLine(args);
        NutsArgument a;
        while ((a = cmd.next()) != null) {
            switch (a.strKey()) {
                case "-o":
                case "--offline": {
                    setOffline(a.getBooleanValue());
                    break;
                }
                case "-r":
                case "-repository":
                case "--from": {
                    setRepository(cmd.getValueFor(a).getString());
                    break;
                }
                default: {
                    if (!super.parseOption(a, cmd)) {
                        if (a.isOption()) {
                            throw new NutsIllegalArgumentException("Unsupported option " + a);
                        } else {
                            id(a.getString());
                        }
                    }
                }
            }
        }
        return this;
    }
}