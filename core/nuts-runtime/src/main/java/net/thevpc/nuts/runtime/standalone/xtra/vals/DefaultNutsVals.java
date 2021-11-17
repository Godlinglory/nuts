package net.thevpc.nuts.runtime.standalone.xtra.vals;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsVal;
import net.thevpc.nuts.spi.NutsVals;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class DefaultNutsVals implements NutsVals {
    private NutsSession session;

    public DefaultNutsVals(NutsSession session) {
        this.session = session;
    }

    @Override
    public NutsVal of(Object str) {
        return new DefaultNutsVal(str);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
