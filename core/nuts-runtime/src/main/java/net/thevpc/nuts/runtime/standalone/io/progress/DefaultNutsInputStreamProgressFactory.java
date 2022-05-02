package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsConstants;
import net.thevpc.nuts.util.NutsProgressFactory;
import net.thevpc.nuts.util.NutsProgressMonitor;

public class DefaultNutsInputStreamProgressFactory implements NutsProgressFactory {

    @Override
    public NutsProgressMonitor create(Object source, Object sourceOrigin, NutsSession session) {
        if (!acceptMonitoring(source, sourceOrigin, session)) {
            return null;
        }
        return new TraceNutsProgressMonitor();
    }

    public boolean acceptMonitoring(Object source, Object sourceOrigin, NutsSession session) {
        if (!NutsProgressUtils.acceptProgress(session)) {
            return false;
        }
        if (sourceOrigin instanceof NutsId) {
            NutsId d = (NutsId) sourceOrigin;
            if (NutsConstants.QueryFaces.CONTENT_HASH.equals(d.getFace())) {
                return false;
            }
            if (NutsConstants.QueryFaces.DESCRIPTOR_HASH.equals(d.getFace())) {
                return false;
            }
        }
        if (source instanceof String) {
            String path = null;
            path = (String) source;
            if (path.endsWith("/" + CoreNutsConstants.Files.DOT_FOLDERS) || path.endsWith("/" + CoreNutsConstants.Files.DOT_FILES)
                    || path.endsWith(".pom") || path.endsWith(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION)
                    || path.endsWith(".xml") || path.endsWith(".json")) {
                return false;
            }
        }
        return true;
    }
}
