package net.vpc.toolbox.tomcat;

import net.vpc.common.io.LineVisitor;

class TomcatLogLineVisitor implements LineVisitor {
    boolean outOfMemoryError;
    String startMessage;
    String shutdownMessage;
    Boolean started;

    public TomcatLogLineVisitor(String startMessage, String shutdownMessage) {
        this.startMessage = startMessage;
        this.shutdownMessage = shutdownMessage;
    }

    @Override
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
