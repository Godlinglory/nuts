package net.vpc.app.nuts.runtime.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.DefaultNutsQueryBaseOptions;

import java.util.*;

public abstract class AbstractNutsFetchCommand extends DefaultNutsQueryBaseOptions<NutsFetchCommand> implements NutsFetchCommand {
    private NutsId id;
    protected Boolean installedOrNot;

    public AbstractNutsFetchCommand(NutsWorkspace ws) {
        super(ws, "fetch");
        failFast();
    }

    @Override
    public NutsFetchCommand id(String id) {
        return setId(id);
    }

    @Override
    public NutsFetchCommand id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsFetchCommand setId(String id) {
        this.id = ws.id().parseRequired(id);
        return this;
    }

    @Override
    public NutsFetchCommand nutsApi() {
        return setId(ws.config().getApiId());
    }

    @Override
    public NutsFetchCommand nutsRuntime() {
        return setId(ws.config().getRuntimeId());
    }

    @Override
    public NutsFetchCommand setId(NutsId id) {
        if (id == null) {
            throw new NutsParseException(ws, "Invalid Id format : null");
        }
        this.id = id;
        return this;
    }

    @Override
    public NutsFetchCommand inlineDependencies() {
        return this.inlineDependencies(true);
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsFetchCommand copyFrom(NutsFetchCommand other) {
        super.copyFromDefaultNutsQueryBaseOptions((DefaultNutsQueryBaseOptions) other);
        if (other != null) {
            NutsFetchCommand o = other;
            this.id = o.getId();
            this.installedOrNot = o.getInstalled();
        }
        return this;
    }

    @Override
    public NutsFetchCommand repositories(Collection<String> value) {
        return addRepositories(value);
    }

    @Override
    public NutsFetchCommand repositories(String... values) {
        return addRepositories(values);
    }

    @Override
    public NutsFetchCommand addRepositories(Collection<String> value) {
        if (value != null) {
            addRepositories(value.toArray(new String[0]));
        }
        return this;
    }

    @Override
    public NutsFetchCommand installed() {
        return setInstalled(true);
    }

    @Override
    public Boolean getInstalled() {
        return installedOrNot;
    }

    public NutsFetchCommand setInstalled(Boolean enable) {
        installedOrNot=enable;
        return this;
    }

    public NutsFetchCommand installed(Boolean enable) {
        return setInstalled(enable);
    }

    @Override
    public NutsFetchCommand notInstalled() {
        return setInstalled(false);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "--not-installed": {
                cmdLine.skip();
                this.notInstalled();
                return true;
            }
            case "-i":
            case "--installed": {
                cmdLine.skip();
                this.installed();
                return true;
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"{" +
                "failFast=" + isFailFast() +
                ", optional=" + getOptional() +
                ", scope=" + getScope() +
                ", content=" + isContent() +
                ", inlineDependencies=" + isInlineDependencies() +
                ", dependencies=" + isDependencies() +
                ", dependenciesTree=" + isDependenciesTree() +
                ", effective=" + isEffective() +
                ", location=" + getLocation() +
                ", repos=" + Arrays.toString(getRepositories()) +
                ", displayOptions=" + getDisplayOptions() +
                ", id=" + getId() +
                ", session=" + getSession() +
                '}';
    }
}
