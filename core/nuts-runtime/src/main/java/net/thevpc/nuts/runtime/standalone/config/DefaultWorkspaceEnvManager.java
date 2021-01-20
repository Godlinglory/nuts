package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.app.DefaultNutsArgument;
import net.thevpc.nuts.runtime.core.common.DefaultObservableMap;
import net.thevpc.nuts.runtime.core.common.ObservableMap;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.bundles.common.CorePlatformUtils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class DefaultWorkspaceEnvManager implements NutsWorkspaceEnvManager {
    private NutsWorkspace ws;
    private Map<String,String> optionsParsed=new LinkedHashMap<>();
    protected ObservableMap<String, Object> userProperties;
    private NutsId platform;
    private NutsId os;
    private NutsOsFamily osFamily;
    private NutsId arch;
    private NutsId osdist;

    public DefaultWorkspaceEnvManager(NutsWorkspace ws,NutsWorkspaceOptions options) {
        this.ws = ws;
        userProperties = new DefaultObservableMap<>();
        String[] properties = options.getProperties();
        if(properties!=null){
            for (String property : properties) {
                if(property!=null){
                    DefaultNutsArgument a=new DefaultNutsArgument(property);
                    String key = a.getStringKey();
                    String value = a.getStringValue();
                    optionsParsed.put(key,value);
                }
            }
        }
    }

    NutsWorkspaceConfigMain getStoreModelMain(){
        return ((DefaultNutsWorkspaceConfigManager)ws.config()).getStoreModelMain();
    }
    @Override
    public Map<String, String> getEnvMap() {
        Map<String, String> p = new LinkedHashMap<>();
        if (getStoreModelMain().getEnv() != null) {
            p.putAll(getStoreModelMain().getEnv());
        }
        p.putAll(optionsParsed);
        return p;
    }

    @Override
    public String getEnv(String property) {
        return getEnv(property,null);
    }

    @Override
    public Integer getEnvAsInt(String property, Integer defaultValue) {
        String t = getEnv(property);
        try {
            return Integer.parseInt(t);
        }catch (Exception ex){
            return defaultValue;
        }
    }

    @Override
    public String getEnv(String property, String defaultValue) {
        if(optionsParsed.containsKey(property)) {
            return optionsParsed.get(property);
        }
        Map<String, String> env = getStoreModelMain().getEnv();
        if (env == null) {
            return defaultValue;
        }
        String o = env.get(property);
        if (CoreStringUtils.isBlank(o)) {
            return defaultValue;
        }
        return o;
    }

    @Override
    public NutsWorkspaceEnvManager setEnv(String property, String value, NutsUpdateOptions options) {
        Map<String, String> env = getStoreModelMain().getEnv();
        options = CoreNutsUtils.validate(options, ws);
        if (CoreStringUtils.isBlank(value)) {
            if (env != null && env.containsKey(property)) {
                env.remove(property);
                NutsWorkspaceConfigManagerExt.of(ws.config()).fireConfigurationChanged("env", options.getSession(), ConfigEventType.MAIN);
            }
        } else {
            if (env == null) {
                env = new LinkedHashMap<>();
                getStoreModelMain().setEnv(env);
            }
            String old = env.get(property);
            if (!value.equals(old)) {
                env.put(property, value);
                NutsWorkspaceConfigManagerExt.of(ws.config()).fireConfigurationChanged("env", options.getSession(), ConfigEventType.MAIN);
            }
        }
        return this;
    }


    private DefaultNutsWorkspaceCurrentConfig current() {
        return NutsWorkspaceConfigManagerExt.of(ws.config()).current();
    }

//    @Override
//    public NutsOsFamily getOsFamily() {
//        return current().getOsFamily();
//    }

//    @Override
//    public NutsId getPlatform() {
//        return current().getPlatform();
//    }
//
//    @Override
//    public NutsId getOs() {
//        return current().getOs();
//    }
//
//    @Override
//    public NutsId getOsDist() {
//        return current().getOsDist();
//    }
//    @Override
//    public NutsId getArch() {
//        return current().getArch();
//    }

    private static NutsOsFamily getPlatformOsFamily0() {
        String property = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (property.startsWith("linux")) {
            return NutsOsFamily.LINUX;
        }
        if (property.startsWith("win")) {
            return NutsOsFamily.WINDOWS;
        }
        if (property.startsWith("mac")) {
            return NutsOsFamily.MACOS;
        }
        if (property.startsWith("sunos")) {
            return NutsOsFamily.UNIX;
        }
        if (property.startsWith("freebsd")) {
            return NutsOsFamily.UNIX;
        }
        return NutsOsFamily.UNKNOWN;
    }


    public NutsId getArch() {
        if (arch == null) {
            arch = ws.id().parser().parse(CorePlatformUtils.getPlatformArch());
        }
        return arch;
    }

    public NutsOsFamily getOsFamily() {
        if (osFamily == null) {
            osFamily = getPlatformOsFamily0();
        }
        return osFamily;
    }


    public NutsId getOs() {
        if (os == null) {
            os = ws.id().parser().parse(CorePlatformUtils.getPlatformOs(ws));
        }
        return os;
    }

    public NutsId getPlatform() {
        if (platform == null) {
            platform = NutsWorkspaceConfigManagerExt.of(ws.config())
                    .createSdkId("java", System.getProperty("java.version"));
        }
        return platform;
    }


    public NutsId getOsDist() {
        if (osdist == null) {
            String platformOsDist = CorePlatformUtils.getPlatformOsDist(ws);
            if(platformOsDist==null){
                platformOsDist="default";
            }
            osdist = ws.id().parser().parse(platformOsDist);
        }
        return osdist;
    }
    @Override
    public Map<String, Object> getProperties() {
        return userProperties;
    }

    @Override
    public Object getProperty(String property, boolean includeEnv, Object defaultValue) {
        Object v = userProperties.get(property);
        if(v!=null){
            return v;
        }
        if(includeEnv){
            v = getEnv(property);
            if(v!=null){
                return v;
            }
        }
        return defaultValue;
    }

    @Override
    public Integer getPropertyAsInt(String property, boolean includeEnv, Integer defaultValue) {
        Object t = getProperty(property,includeEnv,null);
        try {
            if(t instanceof Number) {
                return ((Number) t).intValue();
            }
            if(t instanceof CharSequence) {
                return Integer.parseInt(t.toString());
            }
        }catch (Exception ex){
            //
        }
        return defaultValue;
    }

    @Override
    public String getPropertyAsString(String property, boolean includeEnv, String defaultValue) {
        Object t = getProperty(property,includeEnv,null);
        if(t!=null){
            return t.toString();
        }
        return defaultValue;
    }

    @Override
    public Boolean getPropertyAsBoolean(String property, boolean includeEnv, Boolean defaultValue) {
        Object t = getProperty(property,includeEnv,null);
        try {
            if(t instanceof Boolean) {
                return ((Boolean) t).booleanValue();
            }
            if(t instanceof Number) {
                return ((Number) t).doubleValue()!=0;
            }
            if(t instanceof CharSequence) {
                return CoreCommonUtils.parseBoolean(t.toString(), defaultValue);
            }
        }catch (Exception ex){
            //
        }
        return defaultValue;
    }

    @Override
    public Object getProperty(String property) {
        return getProperty(property,true,null);
    }

    @Override
    public NutsWorkspaceEnvManager setProperty(String property, Object value, NutsUpdateOptions options) {
        if(value==null){
            userProperties.remove(property);
        }else{
            userProperties.put(property,value);
        }
        return this;
    }
}
