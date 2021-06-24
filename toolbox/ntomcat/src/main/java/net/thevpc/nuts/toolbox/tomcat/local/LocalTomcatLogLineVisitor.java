package net.thevpc.nuts.toolbox.tomcat.local;

import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class LocalTomcatLogLineVisitor {

    boolean outOfMemoryError;
    String startMessage;
    String shutdownMessage;
    Boolean started;
    String path;
    NutsSession session;

    public LocalTomcatLogLineVisitor(String path, String startMessage, String shutdownMessage, NutsSession session) {
        this.path = path;
        this.startMessage = startMessage;
        this.shutdownMessage = shutdownMessage;
        this.session = session;
    }

    public void visit() {
        session.getWorkspace().io().path(path).input().lines()
                .forEach(this::nextLine);
    }

    public boolean nextLine(String line) {
        if (line.contains("OutOfMemoryError")) {
            outOfMemoryError = true;
        } else if (startMessage != null && line.contains(startMessage)) {
            started = true;
        } else if (shutdownMessage != null && line.contains(shutdownMessage)) {
            started = false;
        }
        return true;
    }
}
