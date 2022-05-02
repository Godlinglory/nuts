package net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.io.util.NutsStreamOrPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class CharacterizedDeployFile implements AutoCloseable {

    public Path baseFile;
    public NutsStreamOrPath contentStreamOrPath;
    public List<Path> temps = new ArrayList<>();
    public NutsDescriptor descriptor;
    public NutsSession session;

    public CharacterizedDeployFile(NutsSession session) {
        this.session = session;
    }

    public void addTemp(Path f) {
        temps.add(f);
    }

    @Override
    public void close() {
        for (Iterator<Path> it = temps.iterator(); it.hasNext(); ) {
            Path temp = it.next();
            try {
                Files.delete(temp);
            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
            it.remove();
        }
    }

}
