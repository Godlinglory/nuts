package net.vpc.toolbox.tomcat;

public class TomcatAppConfig {
    private String sourceName;
    private String deployName;
    private String startupMessage;
    private String shutdownMessage;
    private String domain;
    private String logFile;


    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getDeployName() {
        return deployName;
    }

    public void setDeployName(String deployName) {
        this.deployName = deployName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getStartupMessage() {
        return startupMessage;
    }

    public void setStartupMessage(String startupMessage) {
        this.startupMessage = startupMessage;
    }

    public String getShutdownMessage() {
        return shutdownMessage;
    }

    public void setShutdownMessage(String shutdownMessage) {
        this.shutdownMessage = shutdownMessage;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }
}
