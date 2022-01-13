package net.thevpc.nuts.installer.connector;


public class RequestAgent {
    private String apiVersion;
    private String runtimeId;
    private String registeredUser;
    private String registeredUserToken;
    private String userLocale;
    private String userTimeZone;
    private String userDigest;
    private String arch;
    private String os;
    private String osDist;
    private String desktop;
    private String platform;
    private String shell;

    public String getApiVersion() {
        return apiVersion;
    }

    public RequestAgent setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public RequestAgent setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public String getRegisteredUser() {
        return registeredUser;
    }

    public RequestAgent setRegisteredUser(String registeredUser) {
        this.registeredUser = registeredUser;
        return this;
    }

    public String getRegisteredUserToken() {
        return registeredUserToken;
    }

    public RequestAgent setRegisteredUserToken(String registeredUserToken) {
        this.registeredUserToken = registeredUserToken;
        return this;
    }

    public String getUserLocale() {
        return userLocale;
    }

    public RequestAgent setUserLocale(String userLocale) {
        this.userLocale = userLocale;
        return this;
    }

    public String getUserTimeZone() {
        return userTimeZone;
    }

    public RequestAgent setUserTimeZone(String userTimeZone) {
        this.userTimeZone = userTimeZone;
        return this;
    }

    public String getUserDigest() {
        return userDigest;
    }

    public RequestAgent setUserDigest(String userDigest) {
        this.userDigest = userDigest;
        return this;
    }

    public String getArch() {
        return arch;
    }

    public RequestAgent setArch(String arch) {
        this.arch = arch;
        return this;
    }

    public String getOs() {
        return os;
    }

    public RequestAgent setOs(String os) {
        this.os = os;
        return this;
    }

    public String getOsDist() {
        return osDist;
    }

    public RequestAgent setOsDist(String osDist) {
        this.osDist = osDist;
        return this;
    }

    public String getDesktop() {
        return desktop;
    }

    public RequestAgent setDesktop(String desktop) {
        this.desktop = desktop;
        return this;
    }

    public String getPlatform() {
        return platform;
    }

    public RequestAgent setPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    public String getShell() {
        return shell;
    }

    public RequestAgent setShell(String shell) {
        this.shell = shell;
        return this;
    }
}
