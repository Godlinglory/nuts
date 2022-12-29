package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NDependencySolverFactory;

public class GradleNDependencySolverFactory implements NDependencySolverFactory {
    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return 1;
    }

    @Override
    public NDependencySolver create(NSession session) {
        return new GradleNDependencySolver(session);
    }

    @Override
    public String getName() {
        return "gradle";
    }
}
