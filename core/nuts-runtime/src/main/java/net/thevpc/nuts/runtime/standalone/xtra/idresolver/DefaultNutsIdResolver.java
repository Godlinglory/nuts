package net.thevpc.nuts.runtime.standalone.xtra.idresolver;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.PomId;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class DefaultNutsIdResolver implements NutsIdResolver {

    private NutsSession session;

    public DefaultNutsIdResolver(NutsSession session) {
        this.session = session;
    }

    @Override
    public NutsId resolveId(Class clazz) {
        PomId u = MavenUtils.createPomIdResolver(session).resolvePomId(clazz, null, session);
        if (u == null) {
            return null;
        }
        return NutsIdParser.of(session).parse(u.getGroupId() + ":" + u.getArtifactId() + "#" + u.getVersion());
    }

    @Override
    public NutsId[] resolveIds(Class clazz) {
        PomId[] u = MavenUtils.createPomIdResolver(session).resolvePomIds(clazz, session);
        NutsId[] all = new NutsId[u.length];
        NutsIdParser parser = NutsIdParser.of(session);
        for (int i = 0; i < all.length; i++) {
            all[i] = parser.parse(u[i].getGroupId() + ":" + u[i].getArtifactId() + "#" + u[i].getVersion());
        }
        return all;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
