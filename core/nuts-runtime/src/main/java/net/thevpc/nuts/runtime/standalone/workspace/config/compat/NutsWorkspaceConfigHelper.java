//package net.thevpc.nuts.runtime.standalone.config.compat;
//
//import net.thevpc.nuts.NutsContentType;
//import net.thevpc.nuts.NutsSession;
//import net.thevpc.nuts.NutsWorkspace;
//
//import java.util.Map;
//
//public class NutsWorkspaceConfigHelper {
//    private NutsSession ws;
//
//    public NutsWorkspaceConfigHelper(NutsSession ws) {
//        this.ws = ws;
//    }
//
//
//    private Map parseConfigMap(byte[] bytes) {
//        return ws.elem().setContentType(NutsContentType.JSON).parse(bytes, Map.class);
//    }
//
//
//
//}
