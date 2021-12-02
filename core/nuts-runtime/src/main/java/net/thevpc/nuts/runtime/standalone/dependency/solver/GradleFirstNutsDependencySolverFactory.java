package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsDependencySolver;
import net.thevpc.nuts.spi.NutsDependencySolverFactory;

public class GradleFirstNutsDependencySolverFactory implements NutsDependencySolverFactory {
    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return 1;
    }

    @Override
    public NutsDependencySolver create(NutsSession session) {
        return new GradleFirstNutsDependencySolver(session);
    }

    @Override
    public String getName() {
        return "gradle-first";
    }
}
