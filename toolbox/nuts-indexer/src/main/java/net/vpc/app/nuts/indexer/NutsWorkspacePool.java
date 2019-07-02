package net.vpc.app.nuts.indexer;

import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceOptions;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NutsWorkspacePool {

    @Autowired
    private NutsIndexerApplication.Config app;
    private final Map<String, NutsWorkspace> pool = new LinkedHashMap<>();

    public NutsWorkspace openWorkspace(String ws) {
        NutsWorkspace o = pool.get(ws);
        if (o == null) {
            if (app.getApplicationContext().getWorkspace().config().getWorkspaceLocation().toString().equals(ws)) {
                o = app.getApplicationContext().getWorkspace();
            } else {
                o = Nuts.openWorkspace(new NutsWorkspaceOptions()
                        .setSkipCompanions(true)
                        .setWorkspace(ws));
            }
            pool.put(ws, o);
            pool.put(o.getUuid(), o);
        }
        return o;
    }
}
