package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.thevpc.nuts.runtime.bundles.common.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsWorkspaceEnvManager implements NutsWorkspaceEnvManager {

    private DefaultNutsWorkspaceEnvManagerModel model;
    private NutsSession session;
    public static final Pattern UNIX_USER_DIRS_PATTERN = Pattern.compile("^\\s*(?<k>[A-Z_]+)\\s*=\\s*(?<v>.*)$");

    public DefaultNutsWorkspaceEnvManager(DefaultNutsWorkspaceEnvManagerModel model) {
        this.model = model;
    }

    NutsWorkspaceConfigMain getStoreModelMain() {
        checkSession();
        return model.getStoreModelMain();
    }

    @Override
    public Map<String, String> getEnvMap() {
        checkSession();
        return model.getEnvMap();
    }

    @Override
    public String getOption(String property) {
        checkSession();
        return model.getOption(property);
    }

    @Override
    public String getEnv(String property) {
        checkSession();
        return model.getOption(property);
    }

    @Override
    public Integer getEnvAsInt(String property, Integer defaultValue) {
        checkSession();
        return model.getEnvAsInt(property, defaultValue);
    }

    @Override
    public Integer getOptionAsInt(String property, Integer defaultValue) {
        checkSession();
        return model.getOptionAsInt(property, defaultValue);
    }

    @Override
    public Boolean getEnvAsBoolean(String property, Boolean defaultValue) {
        checkSession();
        return model.getEnvAsBoolean(property, defaultValue);
    }

    @Override
    public Boolean getOptionAsBoolean(String property, Boolean defaultValue) {
        checkSession();
        return model.getOptionAsBoolean(property, defaultValue);
    }

    @Override
    public String getOption(String property, String defaultValue) {
        checkSession();
        return model.getOption(property, defaultValue);
    }

    @Override
    public String getEnv(String property, String defaultValue) {
        checkSession();
        return model.getEnv(property, defaultValue);
    }

    @Override
    public NutsWorkspaceEnvManager setEnv(String property, String value) {
        checkSession();
        model.setEnv(property, value, session);
        return this;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public NutsId getArch() {
//        checkSession();
        return model.getArch();
    }

    @Override
    public NutsArchFamily getArchFamily() {
//        checkSession();
        return model.getArchFamily();
    }

    @Override
    public NutsOsFamily getOsFamily() {
//        checkSession();
        return model.getOsFamily();
    }

    @Override
    public NutsId getOs() {
//        checkSession();
        return model.getOs();
    }

    @Override
    public NutsId getPlatform() {
//        checkSession();
        return model.getPlatform(session);
    }

    public NutsId getOsDist() {
//        checkSession();
        return model.getOsDist();
    }

    @Override
    public Map<String, Object> getProperties() {
        checkSession();
        return model.getProperties();
    }

    @Override
    public Object getProperty(String property, Object defaultValue) {
        checkSession();
        return model.getProperty(property, defaultValue);
    }

    @Override
    public Integer getPropertyAsInt(String property, Integer defaultValue) {
        checkSession();
        return model.getPropertyAsInt(property, defaultValue);
    }

    @Override
    public String getPropertyAsString(String property, String defaultValue) {
        checkSession();
        return model.getPropertyAsString(property, defaultValue);
    }

    @Override
    public Boolean getPropertyAsBoolean(String property, Boolean defaultValue) {
        checkSession();
        return model.getPropertyAsBoolean(property, defaultValue);
    }

    @Override
    public Object getProperty(String property) {
        checkSession();
        return model.getProperty(property);
    }

    @Override
    public <T> T getOrCreateProperty(Class<T> property, Supplier<T> supplier) {
        checkSession();
        return model.getOrCreateProperty(property, supplier);
    }

    @Override
    public <T> T getOrCreateProperty(String property, Supplier<T> supplier) {
        checkSession();
        return model.getOrCreateProperty(property, supplier);
    }

    @Override
    public NutsWorkspaceEnvManager setProperty(String property, Object value) {
        checkSession();
        model.setProperty(property, value);
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsWorkspaceEnvManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public DefaultNutsWorkspaceEnvManagerModel getModel() {
        return model;
    }

    private DefaultNutsWorkspaceConfigModel _configModel(){
        DefaultNutsWorkspaceConfigManager config = (DefaultNutsWorkspaceConfigManager) session.getWorkspace().config();
        return config.getModel();
    }

    @Override
    public String getBootRepositories() {
        checkSession();
        return _configModel().getBootRepositories();
    }
    @Override
    public long getCreationStartTimeMillis() {
        checkSession();
        return _configModel().getCreationStartTimeMillis();
    }

    @Override
    public long getCreationFinishTimeMillis() {
        checkSession();
        return _configModel().getCreationFinishTimeMillis();
    }

    @Override
    public long getCreationTimeMillis() {
        checkSession();
        return _configModel().getCreationTimeMillis();
    }
    @Override
    public ClassLoader getBootClassLoader() {
        checkSession();
        return _configModel().getBootClassLoader();
    }

    @Override
    public URL[] getBootClassWorldURLs() {
        checkSession();
        return _configModel().getBootClassWorldURLs();
    }

    @Override
    public NutsWorkspaceOptions getBootOptions() {
        checkSession();
        return _configModel().getOptions();
    }

    public boolean matchCondition(NutsActionSupportCondition request, NutsActionSupport support) {
        checkSession();
        if (request == null) {
            request = NutsActionSupportCondition.NEVER;
        }
        if (support == null) {
            support = NutsActionSupport.UNSUPPORTED;
        }
        switch (support) {
            case UNSUPPORTED: {
                return false;
            }
            case SUPPORTED: {
                switch (request) {
                    case NEVER:
                        return false;
                    case ALWAYS:
                    case SUPPORTED: {
                        return true;
                    }
                    case PREFERRED: {
                        return false;
                    }
                    default: {
                        throw new NutsUnsupportedEnumException(getSession(), request);
                    }
                }
            }
            case PREFERRED: {
                switch (request) {
                    case NEVER:
                        return false;
                    case ALWAYS:
                    case PREFERRED: {
                        return true;
                    }
                    case SUPPORTED: {
                        return false;
                    }
                    default: {
                        throw new NutsUnsupportedEnumException(getSession(), request);
                    }
                }
            }
            default: {
                throw new NutsUnsupportedEnumException(getSession(), support);
            }
        }
    }

    @Override
    public NutsActionSupport getDesktopIntegrationSupport(NutsDesktopIntegrationItem item) {
        checkSession();
        if(item==null){
            throw new NutsIllegalArgumentException(getSession(),NutsMessage.cstyle("missing item"));
        }
        switch (getOsFamily()){
            case LINUX:{
                switch (item){
                    case DESKTOP:{
                        return NutsActionSupport.SUPPORTED;
                    }
                    case MENU:{
                        return NutsActionSupport.PREFERRED;
                    }
                    case SHORTCUT:{
                        return NutsActionSupport.PREFERRED;
                    }
                }
                break;
            }
            case UNIX:{
                return NutsActionSupport.UNSUPPORTED;
            }
            case WINDOWS:{
                switch (item){
                    case DESKTOP:{
                        return NutsActionSupport.PREFERRED;
                    }
                    case MENU:{
                        return NutsActionSupport.PREFERRED;
                    }
                    case SHORTCUT:{
                        return NutsActionSupport.PREFERRED;
                    }
                }
                break;
            }
            case MACOS:{
                return NutsActionSupport.UNSUPPORTED;
            }
            case UNKNOWN:{
                return NutsActionSupport.UNSUPPORTED;
            }
        }
        return NutsActionSupport.UNSUPPORTED;
    }

    public Path getDesktopPath() {
        switch (getOsFamily()){
            case LINUX:
            case UNIX:
            case MACOS:{
                File f = new File(System.getProperty("user.home"), ".config/user-dirs.dirs");
                if (f.exists()) {
                    try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                        String line;
                        while ((line = r.readLine()) != null) {
                            line = line.trim();
                            if (line.startsWith("#")) {
                                //ignore
                            } else {
                                Matcher m = UNIX_USER_DIRS_PATTERN.matcher(line);
                                if (m.find()) {
                                    String k = m.group("k");
                                    if (k.equals("XDG_DESKTOP_DIR")) {
                                        String v = m.group("v");
                                        v = v.trim();
                                        if (v.startsWith("\"")) {
                                            int last = v.indexOf('\"', 1);
                                            String s = v.substring(1, last);
                                            s = s.replace("$HOME", System.getProperty("user.home"));
                                            return Paths.get(s);
                                        } else {
                                            return Paths.get(v);
                                        }
                                    }
                                } else {
                                    //this is unexpected format!
                                    break;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        //ignore
                    }
                }
                return new File(System.getProperty("user.home"), "Desktop").toPath();
            }
            case WINDOWS:{
                return new File(System.getProperty("user.home"), "Desktop").toPath();
            }
            default:{
                return new File(System.getProperty("user.home"), "Desktop").toPath();
            }
        }
    }
}
