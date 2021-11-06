package net.thevpc.nuts.runtime.standalone.config.compat.v507;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.config.*;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.runtime.standalone.config.compat.AbstractNutsVersionCompat;
import net.thevpc.nuts.runtime.standalone.config.compat.CompatUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class NutsVersionCompat507 extends AbstractNutsVersionCompat {
    public NutsVersionCompat507(NutsSession ws, String apiVersion) {
        super(ws, apiVersion, 507);
    }

    @Override
    public NutsWorkspaceConfigBoot parseConfig(byte[] bytes, NutsSession session) {
        return bytes==null?null:NutsElements.of(session).json().parse(bytes, NutsWorkspaceConfigBoot.class);
    }

    @Override
    public NutsWorkspaceConfigApi parseApiConfig(NutsSession session) {
        Path path = Paths.get(session.locations().getStoreLocation(session.getWorkspace().getApiId(), NutsStoreLocation.CONFIG))
                .resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NutsWorkspaceConfigApi c = bytes==null?null:NutsElements.of(session)
                .setSession(session)
                .json().parse(bytes, NutsWorkspaceConfigApi.class);
        if (c != null) {
            c.setApiVersion(getApiVersion());
        }
        return c;
    }

    @Override
    public NutsWorkspaceConfigRuntime parseRuntimeConfig(NutsSession session) {
        Path path = Paths.get(session.locations().getStoreLocation(session.getWorkspace().getRuntimeId(), NutsStoreLocation.CONFIG))
                .resolve(NutsConstants.Files.WORKSPACE_RUNTIME_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NutsWorkspaceConfigRuntime c = bytes==null?null:NutsElements.of(session)
                .setSession(session)
                .json().parse(bytes, NutsWorkspaceConfigRuntime.class);
        return c;
    }

    @Override
    public NutsWorkspaceConfigSecurity parseSecurityConfig(NutsSession session) {
        Path path = Paths.get(session.locations().getStoreLocation(session.getWorkspace().getApiId()
                .builder().setVersion(NutsConstants.Versions.RELEASE).build(), NutsStoreLocation.CONFIG))
                .resolve(CoreNutsConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NutsWorkspaceConfigSecurity c = bytes==null?null:NutsElements.of(session)
                .setSession(session)
                .json().parse(bytes, NutsWorkspaceConfigSecurity.class);
        return c;
    }

    @Override
    public NutsWorkspaceConfigMain parseMainConfig(NutsSession session) {
        Path path = Paths.get(session.locations().getStoreLocation(session.getWorkspace().getApiId()
                .builder().setVersion(NutsConstants.Versions.RELEASE)
                .build()
                , NutsStoreLocation.CONFIG))
                .resolve(CoreNutsConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NutsWorkspaceConfigMain507 c = bytes==null?null:NutsElements.of(session)
                .setSession(session)
                .json().parse(bytes, NutsWorkspaceConfigMain507.class);
        NutsWorkspaceConfigMain m=new NutsWorkspaceConfigMain();
        m.setEnv(c.getEnv());
        m.setCommandFactories(c.getCommandFactories());
        m.setRepositories(c.getRepositories());
        m.setImports(c.getImports());
        m.setConfigVersion("5.0.7");
        m.setPlatforms(c.getSdk());
        return m;
    }
}
