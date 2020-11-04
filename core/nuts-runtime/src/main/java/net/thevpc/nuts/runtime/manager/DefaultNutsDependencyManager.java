package net.thevpc.nuts.runtime.manager;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.config.DefaultNutsDependencyBuilder;
import net.thevpc.nuts.runtime.format.DefaultNutsDependencyFormat;
import net.thevpc.nuts.runtime.parser.DefaultNutsDependencyParser;
import net.thevpc.nuts.runtime.util.NutsDependencyScopes;

import java.util.Set;

public class DefaultNutsDependencyManager implements NutsDependencyManager {
    private NutsWorkspace workspace;

    public DefaultNutsDependencyManager(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NutsDependencyParser parser() {
        return new DefaultNutsDependencyParser(workspace);
    }

    @Override
    public NutsDependencyBuilder builder() {
        return new DefaultNutsDependencyBuilder();
    }

    @Override
    public NutsDependencyFormat formatter() {
        return new DefaultNutsDependencyFormat(getWorkspace());
    }

    @Override
    public NutsDependencyFormat formatter(NutsDependency dependency) {
        return formatter().setValue(dependency);
    }

    @Override
    public NutsDependencyFilterManager filter() {
        return getWorkspace().filters().dependency();
    }

    @Override
    public Set<NutsDependencyScope> toScopeSet(NutsDependencyScopePattern other) {
        return NutsDependencyScopes.expand(other);
    }
}
