package net.thevpc.nuts.indexer;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsWorkspaceListManager;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NutsWorkspaceListManagerPool {

    @Autowired
    private NutsIndexerApplication.Config app;
    private final Map<String, NutsWorkspaceListManager> pool = new LinkedHashMap<>();

    public synchronized NutsWorkspaceListManager openListManager(String name) {
        NutsWorkspaceListManager o = pool.get(name);
        if (o == null) {
            NutsWorkspace ws = app.getApplicationContext().getWorkspace();
            ws=ws.createSession().getWorkspace();
            o = ws.config().createWorkspaceListManager(name);
            pool.put(name, o);
        }
        return o;
    }
}
