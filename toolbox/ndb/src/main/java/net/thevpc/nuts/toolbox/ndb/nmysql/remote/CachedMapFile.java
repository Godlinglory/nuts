package net.thevpc.nuts.toolbox.ndb.nmysql.remote;

import net.thevpc.nuts.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CachedMapFile {
    private NutsApplicationContext context;
    private Map<String, String> map;
    private boolean enabled;
    private Path path;
    private boolean loaded;

    public CachedMapFile(NutsApplicationContext context, String name) {
        this(context,name,true);
    }

    public CachedMapFile(NutsApplicationContext context, String name, boolean enabled) {
        this.context = context;
        this.enabled = enabled;
        NutsId appId = context.getAppId();
        path = Paths.get(context.getTempFolder())
                .resolve(appId.getGroupId() + "-" + appId.getArtifactId() + "-" + appId.getVersion())
                .resolve(name + ".json");
        if (enabled) {
            if (Files.isRegularFile(path)) {
                try {
                    NutsSession session = context.getSession();
                    map = NutsElements.of(session)
                            .json()
                            .parse(path, Map.class);
                    loaded=true;
                } catch (Exception ex) {
                    //
                }
            }
        }
    }

    public CachedMapFile setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean exists(){
        return loaded;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean contains(String k) {
        if (!enabled) {
            return false;
        }
        if (map == null) {
            return false;
        }
        return map.get(k)!=null;
    }

    public String get(String k) {
        if (!enabled) {
            return null;
        }
        if (map == null) {
            return null;
        }
        return map.get(k);
    }

    public boolean is(String k) {
        return "true".equals(get(k));
    }

    public void put(String k) {
        put("k", "true");
    }

    public void put(String k, String v) {
        if (enabled) {
            if (map == null) {
                map = new HashMap<String, String>();
            }
            map.put(k, v);
            try {
                NutsSession session = context.getSession();
                NutsElements.of(session).setValue(map)
                        .json()
                        .setNtf(false)
                        .print(path);
            } catch (Exception ex) {
                //
            }
        }
    }

    public void reset() {
        dispose();
        map=null;
    }

    public void dispose() {
        if (path != null) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                //
            }
        }
    }
}
