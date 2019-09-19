package net.vpc.app.nuts.clown;

import net.vpc.app.nuts.NutsId;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.NutsDependencyScope;

public class NutsClownUtils {

    public static Map<String, String> nutsIdToMap(NutsId id) {
        Map<String, String> entity = new HashMap<>();
        id = id.setFace(StringUtils.isEmpty(id.getFace()) ? "default" : id.getFace())
            .scope(StringUtils.isEmpty(id.getScope()) ? NutsDependencyScope.API.id() : id.getScope());
        _condPut(entity, "name", id.getName());
        _condPut(entity, "namespace", id.getNamespace());
        _condPut(entity, "group", id.getGroup());
        _condPut(entity, "version", id.getVersion().getValue());
        _condPut(entity, "face", id.getFace());
        _condPut(entity, "os", id.getOs());
        _condPut(entity, "osdist", id.getOsdist());
        _condPut(entity, "scope", id.getScope());
        _condPut(entity, "arch", id.getArch());
        _condPut(entity, NutsConstants.QueryKeys.CLASSIFIER, id.getClassifier());
//        _condPut(entity, NutsConstants.QueryKeys.ALTERNATIVE, id.getAlternative());
        _condPut(entity, "stringId", id.toString());
        return entity;
    }

    private static void _condPut(Map<String, String> m, String k, String v) {
        if (!trim(v).isEmpty()) {
            m.put(k, v);
        }
    }

    public static String trim(String s) {
        return s == null ? "" : s.trim();
    }

}
