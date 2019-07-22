package net.vpc.app.nuts.core.impl.def.config.compat.v502;

import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsUserConfig;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.impl.def.config.*;
import net.vpc.app.nuts.core.impl.def.config.compat.AbstractNutsVersionCompat;
import net.vpc.app.nuts.core.impl.def.config.compat.CompatUtils;

import java.util.List;

public class NutsVersionCompat502 extends AbstractNutsVersionCompat {
    public NutsVersionCompat502(NutsWorkspace ws,String apiVersion) {
        super(ws,apiVersion, 502);
    }

    @Override
    public NutsWorkspaceConfigBoot parseConfig(byte[] bytes) {
        return parseConfig502(bytes).toWorkspaceConfig();
    }

    @Override
    public NutsWorkspaceConfigApi parseApiConfig() {
        NutsWorkspaceConfigApi cc = new NutsWorkspaceConfigApi();
        cc.setApiVersion(getApiVersion());
        NutsWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(getWorkspace().config().getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
            cc.setApiVersion(c.getBootApiVersion());
            cc.setRuntimeId(c.getBootRuntime());
            cc.setJavaCommand(c.getBootJavaCommand());
            cc.setJavaOptions(c.getBootJavaOptions());
        }
        return cc;
    }

    @Override
    public NutsWorkspaceConfigRuntime parseRuntimeConfig() {
        NutsWorkspaceConfigRuntime cc = new NutsWorkspaceConfigRuntime();
//        cc.setApiVersion(getApiVersion());
        NutsWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(getWorkspace().config().getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
            cc.setDependencies(c.getBootRuntimeDependencies());
            cc.setId(c.getBootRuntime());
        }
        return cc;
    }

    @Override
    public NutsWorkspaceConfigSecurity parseSecurityConfig() {
        NutsWorkspaceConfigSecurity cc = new NutsWorkspaceConfigSecurity();
        NutsWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(getWorkspace().config().getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
//            cc.setConfigVersion(???);
            cc.setSecure(c.isSecure());
            cc.setAuthenticationAgent(c.getAuthenticationAgent());
            List<NutsUserConfig> users = c.getUsers();
            cc.setUsers(CompatUtils.copyNutsUserConfigArray(users==null?null: users.toArray(new NutsUserConfig[0])));
        }
        return cc;
    }

    @Override
    public NutsWorkspaceConfigMain parseMainConfig() {
        NutsWorkspaceConfigMain cc = new NutsWorkspaceConfigMain();
        NutsWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(getWorkspace().config().getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
            c.setRepositories(CompatUtils.copyNutsRepositoryRefList(c.getRepositories()));
            c.setCommandFactories(CompatUtils.copyNutsCommandAliasFactoryConfigList(c.getCommandFactories()));
            c.setEnv(CompatUtils.copyProperties(c.getEnv()));
            c.setSdk(CompatUtils.copyNutsSdkLocationList(c.getSdk()));
            c.setImports(CompatUtils.copyStringList(c.getImports()));
        }
        return cc;
    }

    public NutsWorkspaceConfigBoot502 parseConfig502(byte[] bytes) {
        return getWorkspace().json().parse(bytes, NutsWorkspaceConfigBoot502.class);
    }


}
