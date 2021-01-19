package net.thevpc.nuts.runtime.core.app;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.NutsHomeLocationsMap;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.runtime.standalone.NutsStoreLocationsMap;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.nio.file.Paths;
import java.util.Map;

public class DefaultNutsWorkspaceLocationManager implements NutsWorkspaceLocationManager {
    private NutsWorkspace ws;
    private String workspaceLocation;

    public DefaultNutsWorkspaceLocationManager(NutsWorkspace ws,NutsWorkspaceInitInformation info) {
        this.ws=ws;
        this.workspaceLocation = Paths.get(info.getWorkspaceLocation()).toString();
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    private DefaultNutsWorkspaceConfigManager cfg(){
        return (DefaultNutsWorkspaceConfigManager)(ws.config());
    }

    @Override
    public void setHomeLocation(NutsOsFamily layout, NutsStoreLocation folder, String location, NutsUpdateOptions options) {
        if (folder == null) {
            throw new NutsIllegalArgumentException(ws, "invalid store root folder null");
        }
        options = CoreNutsUtils.validate(options, ws);
        cfg().onPreUpdateConfig("home-location", options);
        cfg().getStoreModelBoot().setHomeLocations(new NutsHomeLocationsMap(cfg().getStoreModelBoot().getHomeLocations()).set(layout, folder, location).toMapOrNull());
        cfg().onPostUpdateConfig("home-location", options);
    }

    @Override
    public String getWorkspaceLocation() {
        return workspaceLocation;
    }

    @Override
    public String getHomeLocation(NutsStoreLocation folderType) {
        return cfg().current().getHomeLocation(folderType);
    }

    @Override
    public String getStoreLocation(NutsStoreLocation folderType) {
        return cfg().current().getStoreLocation(folderType);
    }

    @Override
    public void setStoreLocation(NutsStoreLocation folderType, String location, NutsUpdateOptions options) {
        if (folderType == null) {
            throw new NutsIllegalArgumentException(ws, "invalid store root folder null");
        }
        options = CoreNutsUtils.validate(options, ws);
        cfg().onPreUpdateConfig("store-location", options);
        cfg().getStoreModelBoot().setStoreLocations(new NutsStoreLocationsMap(cfg().getStoreModelBoot().getStoreLocations()).set(folderType, location).toMapOrNull());
        cfg().onPostUpdateConfig("store-location", options);
    }

    @Override
    public void setStoreLocationStrategy(NutsStoreLocationStrategy strategy, NutsUpdateOptions options) {
        if (strategy == null) {
            strategy = NutsStoreLocationStrategy.EXPLODED;
        }
        options = CoreNutsUtils.validate(options, ws);
        cfg().onPreUpdateConfig("store-location-strategy", options);
        cfg().getStoreModelBoot().setStoreLocationStrategy(strategy);
        cfg().onPostUpdateConfig("store-location-strategy", options);
    }

    @Override
    public void setStoreLocationLayout(NutsOsFamily layout, NutsUpdateOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        cfg().onPreUpdateConfig("store-location-layout", options);
        cfg().getStoreModelBoot().setStoreLocationLayout(layout);
        cfg().onPostUpdateConfig("store-location-layout", options);
    }

    @Override
    public String getStoreLocation(NutsStoreLocation folderType, String repositoryIdOrName, NutsSession session) {
        if(repositoryIdOrName==null){
            return getStoreLocation(folderType);
        }
        NutsRepository repositoryById = ws.repos().getRepository(repositoryIdOrName, session);
        NutsRepositorySPI nutsRepositorySPI = NutsWorkspaceUtils.of(getWorkspace()).repoSPI(repositoryById);
        return nutsRepositorySPI.config().getStoreLocation(folderType);
    }

    @Override
    public String getStoreLocation(NutsId id, NutsStoreLocation folderType, String repositoryIdOrName, NutsSession session) {
        if(repositoryIdOrName==null){
            return getStoreLocation(id,folderType);
        }
        String storeLocation=getStoreLocation(folderType,repositoryIdOrName,session);
        return Paths.get(storeLocation).resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id)).toString();
    }

    @Override
    public String getStoreLocation(NutsId id, NutsStoreLocation folderType) {
        String storeLocation = getStoreLocation(folderType);
        if (storeLocation == null) {
            return null;
        }
        return Paths.get(storeLocation).resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id)).toString();
//        switch (folderType) {
//            case CACHE:
//                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//            case CONFIG:
//                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//        }
//        return storeLocation.resolve(getDefaultIdBasedir(id));
    }
    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return cfg().current().getStoreLocationStrategy();
    }

    @Override
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return cfg().current().getRepositoryStoreLocationStrategy();
    }

    @Override
    public NutsOsFamily getStoreLocationLayout() {
        return cfg().current().getStoreLocationLayout();
    }

    @Override
    public Map<String, String> getStoreLocations() {
        return cfg().current().getStoreLocations();
    }

    @Override
    public Map<String, String> getHomeLocations(NutsSession session) {
        return cfg().current().getHomeLocations();
    }

    @Override
    public String getHomeLocation(NutsOsFamily layout, NutsStoreLocation location, NutsSession session) {
        return cfg().current().getHomeLocation(layout, location);
    }


    @Override
    public String getDefaultIdBasedir(NutsId id) {
        NutsWorkspaceUtils.of(ws).checkSimpleNameNutsId(id);
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        String plainIdPath = groupId.replace('.', '/') + "/" + artifactId;
        if (id.getVersion().isBlank()) {
            return plainIdPath;
        }
        String version = id.getVersion().getValue();
//        String a = CoreNutsUtils.trimToNullAlternative(id.getAlternative());
        String x = plainIdPath + "/" + version;
//        if (a != null) {
//            x += "/" + a;
//        }
        return x;
    }

    @Override
    public String getDefaultIdFilename(NutsId id) {
        String classifier = "";
        String ext = getDefaultIdExtension(id);
        if (!ext.equals(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION) && !ext.equals(".pom")) {
            String c = id.getClassifier();
            if (!CoreStringUtils.isBlank(c)) {
                classifier = "-" + c;
            }
        }
        return id.getArtifactId() + "-" + id.getVersion().getValue() + classifier + ext;
    }


    @Override
    public String getDefaultIdContentExtension(String packaging) {
        if (CoreStringUtils.isBlank(packaging)) {
            throw new NutsIllegalArgumentException(ws, "unsupported empty Packaging");
        }
        switch (packaging) {
            case "jar":
            case "bundle":
            case "nuts-extension":
            case "maven-archetype":
            case "maven-plugin":
            case "ejb-client":
            case "test-jar":
            case "ejb":
            case "java-source":
            case "javadoc":
            case "eclipse-plugin":
                return ".jar";
            case "dll":
            case "so":
            case "jnilib":
                return "-natives.jar";
            case "war":
                return ".war";
            case "ear":
                return ".ear";
            case "pom":
                return ".pom";
            case "nuts":
                return NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION;
            case "rar":
                return ".rar";
            case "zip":
            case "nbm-application":
                return ".zip";
        }
        return "." + packaging;
    }


    @Override
    public String getDefaultIdExtension(NutsId id) {
        Map<String, String> q = id.getProperties();
        String f = CoreStringUtils.trim(q.get(NutsConstants.IdProperties.FACE));
        switch (f) {
            case NutsConstants.QueryFaces.DESCRIPTOR: {
                return NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION;
            }
            case NutsConstants.QueryFaces.DESCRIPTOR_HASH: {
                return ".nuts.sha1";
            }
            case CoreNutsConstants.QueryFaces.CATALOG: {
                return ".catalog";
            }
            case NutsConstants.QueryFaces.CONTENT_HASH: {
                return getDefaultIdExtension(id.builder().setFaceContent().build()) + ".sha1";
            }
            case NutsConstants.QueryFaces.CONTENT: {
                return getDefaultIdContentExtension(q.get(NutsConstants.IdProperties.PACKAGING));
            }
            default: {
                if (f.equals("cache") || f.endsWith(".cache")) {
                    return "." + f;
                }
                if (CoreStringUtils.isBlank(f)) {
                    throw new NutsIllegalArgumentException(ws, "missing face in " + id);
                }
                throw new NutsIllegalArgumentException(ws, "unsupported face " + f + " in " + id);
            }
        }
    }

}