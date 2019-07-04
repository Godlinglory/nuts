package net.vpc.app.nuts.core.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.NutsHomeLocationsMap;
import net.vpc.app.nuts.core.NutsStoreLocationsMap;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

public final class DefaultNutsWorkspaceCurrentConfig {

    private String name;
    private NutsId bootAPI;
    private NutsId bootRuntime;
    private String bootRuntimeDependencies;
    private String bootExtensionDependencies;
    private String bootRepositories;
    private String bootJavaCommand;
    private String bootJavaOptions;
    private NutsStoreLocationStrategy storeLocationStrategy;
    private NutsStoreLocationStrategy repositoryStoreLocationStrategy;
    private NutsOsFamily storeLocationLayout;
    private final Map<String, String> userStoreLocations = new HashMap<>();
    private final Map<String, String> effStoreLocationsMap = new HashMap<>();
    private final Path[] effStoreLocationPath = new Path[NutsStoreLocation.values().length];
    private final Map<String, String> homeLocations = new HashMap<>();
    private boolean global;
    private final NutsWorkspace ws;
    private NutsId platformOs;
    private NutsOsFamily platformOsFamily;
    private NutsId platformArch;
    private NutsId platformOsdist;

    public DefaultNutsWorkspaceCurrentConfig(NutsWorkspace ws) {
        this.ws = ws;
    }

    public DefaultNutsWorkspaceCurrentConfig merge(NutsWorkspaceOptions c) {
        if (c.getName() != null) {
            this.name = c.getName();
        }
//        this.uuid = c.getUuid();
//        this.bootAPI = c.getApiVersion() == null ? null : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + c.getApiVersion());
        if (c.getRuntimeId() != null) {
            this.bootRuntime = c.getRuntimeId().contains("#")
                    ? CoreNutsUtils.parseNutsId(c.getRuntimeId())
                    : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getRuntimeId());
        }
//        this.bootRuntimeDependencies = c.getRuntimeDependencies();
//        this.bootExtensionDependencies = c.getExtensionDependencies();
//        this.bootRepositories = c.getBootRepositories();
        if (c.getJavaCommand() != null) {
            this.bootJavaCommand = c.getJavaCommand();
        }
        if (c.getJavaOptions() != null) {
            this.bootJavaOptions = c.getJavaOptions();
        }
        if (c.getStoreLocationStrategy() != null) {
            this.storeLocationStrategy = c.getStoreLocationStrategy();
        }
        if (c.getRepositoryStoreLocationStrategy() != null) {
            this.repositoryStoreLocationStrategy = c.getRepositoryStoreLocationStrategy();
        }
        if (c.getStoreLocationLayout() != null) {
            this.storeLocationLayout = c.getStoreLocationLayout();
        }
        this.userStoreLocations.putAll(new NutsStoreLocationsMap(c.getStoreLocations()).toMap());
        this.homeLocations.putAll(new NutsHomeLocationsMap(c.getHomeLocations()).toMap());
        this.global |= c.isGlobal();
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig mergeRuntime(NutsWorkspaceOptions c) {
        if (c.getRuntimeId() != null) {
            this.bootRuntime = c.getRuntimeId().contains("#")
                    ? CoreNutsUtils.parseNutsId(c.getRuntimeId())
                    : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getRuntimeId());
        }
//        this.bootRuntimeDependencies = c.getRuntimeDependencies();
//        this.bootExtensionDependencies = c.getExtensionDependencies();
//        this.bootRepositories = c.getBootRepositories();
        if (c.getJavaCommand() != null) {
            this.bootJavaCommand = c.getJavaCommand();
        }
        if (c.getJavaOptions() != null) {
            this.bootJavaOptions = c.getJavaOptions();
        }
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig build(Path workspaceLocation) {
        if (storeLocationStrategy == null) {
            storeLocationStrategy = NutsStoreLocationStrategy.EXPLODED;
        }
        if (repositoryStoreLocationStrategy == null) {
            repositoryStoreLocationStrategy = NutsStoreLocationStrategy.EXPLODED;
        }
        Path[] homes = new Path[NutsStoreLocation.values().length];
        for (NutsStoreLocation type : NutsStoreLocation.values()) {
            String ss = NutsPlatformUtils.getPlatformHomeFolder(getStoreLocationLayout(), type, homeLocations, isGlobal(), getName());
            if (CoreStringUtils.isBlank(ss)) {
                throw new NutsIllegalArgumentException(null, "Missing Home for " + type.id());
            }
            homes[type.ordinal()] = Paths.get(ss);
        }
        Map<String, String> storeLocations = getStoreLocations() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(getStoreLocations());
        for (NutsStoreLocation location : NutsStoreLocation.values()) {
            String typeId = location.id();
            switch (location) {
                default: {
                    String typeLocation = storeLocations.get(typeId);
                    if (CoreStringUtils.isBlank(typeLocation)) {
                        switch (storeLocationStrategy) {
                            case STANDALONE: {
                                storeLocations.put(typeId, workspaceLocation.resolve(location.id()).toString());
                                break;
                            }
                            case EXPLODED: {
                                storeLocations.put(typeId, homes[location.ordinal()].toString());
                                break;
                            }
                        }
                    } else if (!CoreIOUtils.isAbsolutePath(typeLocation)) {
                        switch (storeLocationStrategy) {
                            case STANDALONE: {
                                storeLocations.put(typeId, workspaceLocation.resolve(location.id()).toString());
                                break;
                            }
                            case EXPLODED: {
                                storeLocations.put(typeId, homes[location.ordinal()].resolve(typeLocation).toString());
                                break;
                            }
                        }
                    }

                }
            }
        }
        this.effStoreLocationsMap.clear();
        this.effStoreLocationsMap.putAll(storeLocations);
        for (int i = 0; i < effStoreLocationPath.length; i++) {
            effStoreLocationPath[i]=ws.io().path(effStoreLocationsMap.get(NutsStoreLocation.values()[i].id()));
        }
        if (bootAPI == null) {
            bootAPI = CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + Nuts.getVersion());
        }
        if (storeLocationLayout == null) {
            storeLocationLayout = NutsPlatformUtils.getPlatformOsFamily();
        }
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig merge(NutsWorkspaceConfig c) {
        if (c.getName() != null) {
            this.name = c.getName();
        }
//        this.uuid = c.getUuid();
        if (c.getApiVersion() != null) {
            this.bootAPI = CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + c.getApiVersion());
        }
        if (c.getRuntimeId() != null) {
            this.bootRuntime = c.getRuntimeId().contains("#")
                    ? CoreNutsUtils.parseNutsId(c.getRuntimeId())
                    : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getRuntimeId());
        }
//        this.bootRuntimeDependencies = c.getRuntimeDependencies();
//        this.bootExtensionDependencies = c.getExtensionDependencies();
//        this.bootRepositories = c.getBootRepositories();
        if (c.getJavaCommand() != null) {
            this.bootJavaCommand = c.getJavaCommand();
        }
        this.bootJavaOptions = c.getJavaOptions();
        if (c.getStoreLocationStrategy() != null) {
            this.storeLocationStrategy = c.getStoreLocationStrategy();
        }
        if (c.getRepositoryStoreLocationStrategy() != null) {
            this.repositoryStoreLocationStrategy = c.getRepositoryStoreLocationStrategy();
        }
        if (c.getStoreLocationLayout() != null) {
            this.storeLocationLayout = c.getStoreLocationLayout();
        }
        this.userStoreLocations.putAll(new NutsStoreLocationsMap(c.getStoreLocations()).toMap());
        this.homeLocations.putAll(new NutsHomeLocationsMap(c.getHomeLocations()).toMap());
        this.global |= c.isGlobal();
//        this.gui |= c.isGui();
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig merge(NutsBootConfig c) {
        this.name = c.getName();
        if (c.getApiVersion() != null) {
            this.bootAPI = CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + c.getApiVersion());
        }
        if (c.getRuntimeId() != null) {
            this.bootRuntime = c.getRuntimeId().contains("#")
                    ? CoreNutsUtils.parseNutsId(c.getRuntimeId())
                    : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getRuntimeId());
        }
        if (c.getRuntimeDependencies() != null) {
            this.bootRuntimeDependencies = c.getRuntimeDependencies();
        }
        if (c.getExtensionDependencies() != null) {
            this.bootExtensionDependencies = c.getExtensionDependencies();
        }
        if (c.getBootRepositories() != null) {
            this.bootRepositories = c.getBootRepositories();
        }
        if (c.getJavaCommand() != null) {
            this.bootJavaCommand = c.getJavaCommand();
        }
        if (c.getJavaOptions() != null) {
            this.bootJavaOptions = c.getJavaOptions();
        }
        if (c.getStoreLocationStrategy() != null) {
            this.storeLocationStrategy = c.getStoreLocationStrategy();
        }
        if (c.getRepositoryStoreLocationStrategy() != null) {
            this.repositoryStoreLocationStrategy = c.getRepositoryStoreLocationStrategy();
        }
        if (c.getStoreLocationLayout() != null) {
            this.storeLocationLayout = c.getStoreLocationLayout();
        }
        this.userStoreLocations.putAll(new NutsStoreLocationsMap(c.getStoreLocations()).toMap());
        this.homeLocations.putAll(new NutsHomeLocationsMap(c.getHomeLocations()).toMap());
        this.global |= c.isGlobal();
        return this;
    }

    
    public String getExtensionDependencies() {
        return bootExtensionDependencies;
    }

    
    public String getName() {
        return name;
    }

    
    public boolean isGlobal() {
        return this.global;
    }

    
    public String getApiVersion() {
        return getApiId().getVersion().getValue();
    }

    
    public NutsId getApiId() {
        return bootAPI;
    }

    
    public NutsId getRuntimeId() {
        return bootRuntime;
    }

    
    public String getRuntimeDependencies() {
        return bootRuntimeDependencies;
    }

    
    public String getBootRepositories() {
        return bootRepositories;
    }

    
    public String getJavaCommand() {
        return bootJavaCommand;
    }

    
    public String getJavaOptions() {
        return bootJavaOptions;
    }

    
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    
    public Map<String, String> getStoreLocations() {
        return new LinkedHashMap<>(effStoreLocationsMap);
    }

    
    public Map<String, String> getHomeLocations() {
        return new LinkedHashMap<>(homeLocations);
    }

    
    public Path getStoreLocation(NutsStoreLocation folderType) {
        return effStoreLocationPath[folderType.ordinal()];
    }

    
    public Path getHomeLocation(NutsOsFamily layout, NutsStoreLocation folderType) {
        String path = new NutsHomeLocationsMap(homeLocations).get(layout, folderType);
        return path == null ? null : ws.io().path(path);
    }

    
    public Path getHomeLocation(NutsStoreLocation folderType) {
        return ws.io().path(NutsPlatformUtils.getPlatformHomeFolder(getStoreLocationLayout(),
                folderType, getHomeLocations(),
                isGlobal(),
                getName()
        ));
    }

    
    public NutsOsFamily getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public DefaultNutsWorkspaceCurrentConfig setHomeLocations(Map<String, String> homeLocations) {
        this.homeLocations.clear();
        if (homeLocations != null) {
            this.homeLocations.putAll(homeLocations);
        }
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setUserStoreLocations(Map<String, String> userStoreLocations) {
        this.userStoreLocations.clear();
        if (userStoreLocations != null) {
            this.userStoreLocations.putAll(userStoreLocations);
        }
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setName(String name) {
        this.name = name;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setBootAPI(NutsId bootAPI) {
        this.bootAPI = bootAPI;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setBootRuntime(NutsId bootRuntime) {
        this.bootRuntime = bootRuntime;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setBootRuntimeDependencies(String bootRuntimeDependencies) {
        this.bootRuntimeDependencies = bootRuntimeDependencies;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setBootExtensionDependencies(String bootExtensionDependencies) {
        this.bootExtensionDependencies = bootExtensionDependencies;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setBootRepositories(String bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setBootJavaCommand(String bootJavaCommand) {
        this.bootJavaCommand = bootJavaCommand;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setBootJavaOptions(String bootJavaOptions) {
        this.bootJavaOptions = bootJavaOptions;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setRepositoryStoreLocationStrategy(NutsStoreLocationStrategy repositoryStoreLocationStrategy) {
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setStoreLocationLayout(NutsOsFamily storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    public DefaultNutsWorkspaceCurrentConfig setGlobal(boolean global) {
        this.global = global;
        return this;
    }

    
    public NutsId getPlatformArch() {
        if (platformArch == null) {
            platformArch = ws.id().parse(CorePlatformUtils.getPlatformArch());
        }
        return platformArch;
    }

    
    public NutsOsFamily getPlatformOsFamily() {
        if (platformOsFamily == null) {
            platformOsFamily = NutsPlatformUtils.getPlatformOsFamily();
        }
        return platformOsFamily;
    }

    
    public NutsId getPlatformOs() {
        if (platformOs == null) {
            platformOs = ws.id().parse(CorePlatformUtils.getPlatformOs());
        }
        return platformOs;
    }

    
    public NutsId getPlatformOsDist() {
        if (platformOsdist == null) {
            platformOsdist = ws.id().parse(CorePlatformUtils.getPlatformOsDist());
        }
        return platformOsdist;
    }

//    
    public Path getStoreLocation(String id, NutsStoreLocation folderType) {
        return getStoreLocation(ws.id().parse(id), folderType);
    }

//    
    public Path getStoreLocation(NutsId id, NutsStoreLocation folderType) {
        Path storeLocation = getStoreLocation(folderType);
        if (storeLocation == null) {
            return null;
        }
        switch (folderType) {
            case CACHE:
                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(ws.config().getDefaultIdBasedir(id));
            case CONFIG:
                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(ws.config().getDefaultIdBasedir(id));
        }
        return storeLocation.resolve(ws.config().getDefaultIdBasedir(id));
    }

//    
//    public Path getStoreLocation(NutsStoreLocation folderType) {
//        String s=effStoreLocations.get(folderType.id());
//        return s==null?null:Paths.get(s);
////        String n = CoreNutsUtils.getArrItem(getStoreLocations(), folderType.ordinal());
////        switch (getStoreLocationStrategy()) {
////            case STANDALONE: {
////                if (CoreStringUtils.isBlank(n)) {
////                    n = folderType.toString().toLowerCase();
////                }
////                n = n.trim();
////                return getStoreLocation().resolve(n);
////            }
////            case EXPLODED: {
////                Path storeLocation = repository.getWorkspace().config().getStoreLocation(folderType);
////                //uuid is added as
////                return storeLocation.resolve(NutsConstants.Folders.REPOSITORIES).resolve(getName()).resolve(getUuid());
////
////            }
////            default: {
////                throw new NutsIllegalArgumentException(repository.getWorkspace(), "Unsupported strategy type " + getStoreLocation());
////            }
////        }
//    }

}
