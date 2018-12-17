/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.terminals.NutsDefaultFormattedPrintStream;
import net.vpc.app.nuts.extensions.terminals.NutsTerminalDelegate;
import net.vpc.app.nuts.extensions.util.CoreJsonUtils;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.common.io.URLUtils;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.ListMap;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author vpc
 */
class DefaultNutsWorkspaceExtensionManager implements NutsWorkspaceExtensionManager {

    private Set<Class> SUPPORTED_EXTENSION_TYPES = new HashSet<Class>(
            Arrays.asList(
                    //order is important!!because autowiring should follow this very order
                    NutsDefaultFormattedPrintStream.class,
                    NutsNonFormattedPrintStream.class,
                    NutsFormattedPrintStream.class,
                    NutsTerminalBase.class,
                    NutsTerminal.class,
                    NutsDescriptorContentParserComponent.class,
                    NutsExecutorComponent.class,
                    NutsInstallerComponent.class,
                    NutsRepositoryFactoryComponent.class,
                    NutsTransportComponent.class,
                    NutsWorkspace.class,
                    NutsWorkspaceArchetypeComponent.class
            )
    );
    private NutsURLClassLoader workspaceExtensionsClassLoader;
    private ListMap<String, String> defaultWiredComponents = new ListMap<>();
    private Map<NutsId, NutsWorkspaceExtension> extensions = new HashMap<NutsId, NutsWorkspaceExtension>();
    private final NutsWorkspace ws;
    private final NutsWorkspaceFactory objectFactory;

    protected DefaultNutsWorkspaceExtensionManager(NutsWorkspace ws, NutsWorkspaceFactory objectFactory) {
        this.ws = ws;
        this.objectFactory = objectFactory;
    }

    @Override
    public List<NutsExtensionInfo> findWorkspaceExtensions(NutsSession session) {
        return findWorkspaceExtensions(ws.getConfigManager().getBootAPI().getVersion().toString(), session);
    }

    @Override
    public List<NutsExtensionInfo> findWorkspaceExtensions(String version, NutsSession session) {
        if (version == null) {
            version = ws.getConfigManager().getBootAPI().getVersion().toString();
        }
        NutsId id = ws.getConfigManager().getBootAPI().setVersion(version);
        return findExtensions(id.toString(), "extensions", session);
    }

    @Override
    public List<NutsExtensionInfo> findExtensions(String id, String extensionType, NutsSession session) {
        NutsId nid = ws.parseNutsId(id);
        if (nid.getVersion().isEmpty()) {
            throw new NutsIllegalArgumentException("Missing version");
        }
        List<NutsExtensionInfo> ret = new ArrayList<>();
        List<String> allUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(id)) {
            String url = r + "/" + CoreNutsUtils.getPath(nid, "." + extensionType, "/");
            allUrls.add(url);
            URL u = expandURL(url);
            if (u != null) {
                NutsExtensionInfo[] s = new NutsExtensionInfo[0];
                try {
                    s = CoreJsonUtils.get().read(new InputStreamReader(u.openStream()), NutsExtensionInfo[].class);
                } catch (IOException e) {
                    //ignore!
                }
                if (s != null) {
                    for (NutsExtensionInfo nutsExtensionInfo : s) {
                        nutsExtensionInfo.setSource(u.toString());
                        ret.add(nutsExtensionInfo);
                    }
                }
            }
        }
        boolean latestVersion = true;
        if (latestVersion && ret.size() > 1) {
            return CoreNutsUtils.filterNutsExtensionInfoByLatestVersion(ret);
        }
        return ret;
    }

    protected void onInitializeWorkspace(ClassLoader bootClassLoader) {
        //now will iterate over Extension classes to wire them ...
        List<Class> loadedExtensions = discoverTypes(NutsComponent.class, bootClassLoader);
        for (Class extensionImpl : loadedExtensions) {
            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
                Class<? extends NutsComponent> extensionImplType = extensionImpl;
                if (installExtensionComponentType(extensionPointType, extensionImplType)) {
                    defaultWiredComponents.add(extensionPointType.getName(), extensionImplType.getName());
                }
            }
        }
        //versionProperties = IOUtils.loadProperties(DefaultNutsWorkspace.class.getResource("/META-INF/nuts-core-version.properties"));
        this.workspaceExtensionsClassLoader = new NutsURLClassLoader(new URL[0], bootClassLoader);
    }

    @Override
    public NutsWorkspaceExtension addWorkspaceExtension(String id, NutsSession session) {
        session = validateSession(session);
        NutsId oldId = CoreNutsUtils.finNutsIdByFullName(ws.parseRequiredNutsId(id), extensions.keySet());
        NutsWorkspaceExtension old = null;
        if (oldId == null) {
            NutsId nutsId = ws.resolveId(id, session);
            NutsId eid = ws.parseRequiredNutsId(id);
            if (StringUtils.isEmpty(eid.getGroup())) {
                eid = eid.setGroup(nutsId.getGroup());
            }
            ws.getConfigManager().addExtension(eid);
            return wireExtension(eid, session);
        } else {
            old = extensions.get(oldId);
            ws.getConfigManager().addExtension(ws.parseRequiredNutsId(id));
            return old;
        }
    }

    @Override
    public boolean installWorkspaceExtensionComponent(Class extensionPointType, Object extensionImpl) {
        if (NutsComponent.class.isAssignableFrom(extensionPointType)) {
            if (extensionPointType.isInstance(extensionImpl)) {
                return registerInstance(extensionPointType, extensionImpl);
            }
            throw new ClassCastException(extensionImpl.getClass().getName());
        }
        throw new ClassCastException(NutsComponent.class.getName());
    }

    @Override
    public NutsWorkspaceExtension[] getWorkspaceExtensions() {
        return extensions.values().toArray(new NutsWorkspaceExtension[0]);
    }

    protected NutsWorkspaceExtension wireExtension(NutsId id, NutsSession session) {
        session = validateSession(session);
        if (id == null) {
            throw new NutsIllegalArgumentException("Extension Id could not be null");
        }
        NutsId wired = CoreNutsUtils.finNutsIdByFullName(id, extensions.keySet());
        if (wired != null) {
            throw new NutsWorkspaceExtensionAlreadyRegisteredException(id.toString(), wired.toString());
        }
        DefaultNutsWorkspace.log.log(Level.FINE, "Installing extension {0}", id);
        NutsFile[] nutsFiles = ws.fetchDependencies(new NutsDependencySearch(id).setScope(NutsDependencyScope.RUN), session);
        NutsId toWire = null;
        for (NutsFile nutsFile : nutsFiles) {
            if (nutsFile.getId().isSameFullName(id)) {
                if (toWire == null || toWire.getVersion().compareTo(nutsFile.getId().getVersion()) < 0) {
                    toWire = nutsFile.getId();
                }
            }
        }
        if (toWire == null) {
            toWire = id;
        }
        for (NutsFile nutsFile : nutsFiles) {
            if (!isLoadedClassPath(nutsFile, session)) {
                this.workspaceExtensionsClassLoader.addFile(new File(nutsFile.getFile()));
            }
        }
        DefaultNutsWorkspaceExtension workspaceExtension = new DefaultNutsWorkspaceExtension(id, toWire, this.workspaceExtensionsClassLoader);
        //now will iterate over Extension classes to wire them ...
        List<Class> serviceLoader = discoverTypes(NutsComponent.class, workspaceExtension.getClassLoader());
        for (Class extensionImpl : serviceLoader) {
            for (Class extensionPointType : resolveComponentTypes(extensionImpl)) {
                Class<? extends NutsComponent> extensionImplType = extensionImpl;
                if (installExtensionComponentType(extensionPointType, extensionImplType)) {
                    workspaceExtension.getWiredComponents().add(extensionPointType.getName(), extensionImplType.getName());
                }
            }
        }
        extensions.put(id, workspaceExtension);
        DefaultNutsWorkspace.log.log(Level.FINE, "Extension {0} installed successfully", id);
        NutsTerminal newTerminal = createTerminal(session.getTerminal() == null ? null : session.getTerminal().getClass());
        if (newTerminal != null) {
            DefaultNutsWorkspace.log.log(Level.FINE, "Extension {0} changed Terminal configuration. Reloading Session Terminal", id);
            session.setTerminal(newTerminal);
        }
        return workspaceExtension;
    }

    private boolean isLoadedClassPath(NutsFile file, NutsSession session) {
        session = validateSession(session);
        if (file.getId().isSameFullName(ws.parseRequiredNutsId(NutsConstants.NUTS_ID_BOOT_API))) {
            return true;
        }
        try {
            //            NutsFile file = fetch(id.toString(), session);
            if (file.getFile() != null) {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file.getFile());
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        String zname = zipEntry.getName();
                        if (zname.endsWith(".class")) {
                            String clz = zname.substring(0, zname.length() - 6).replace('/', '.');
                            try {
                                Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(clz);
                                return true;
                            } catch (ClassNotFoundException e) {
                                return false;
                            }
                        }
                    }
                } finally {
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e) {
                            //ignore return false;
                        }
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public boolean registerInstance(Class extensionPointType, Object extensionImpl) {
        if (!isRegisteredType(extensionPointType, extensionImpl.getClass().getName()) && !isRegisteredInstance(extensionPointType, extensionImpl)) {
            objectFactory.registerInstance(extensionPointType, extensionImpl);
            return true;
        }
        DefaultNutsWorkspace.log.log(Level.FINE, "Bootstrap Extension Point {0} => {1} ignored. Already registered", new Object[]{extensionPointType.getName(), extensionImpl.getClass().getName()});
        return false;
    }

    public boolean registerType(Class extensionPointType, Class extensionType) {
        if (!isRegisteredType(extensionPointType, extensionType.getName()) && !isRegisteredType(extensionPointType, extensionType)) {
            objectFactory.registerType(extensionPointType, extensionType);
            return true;
        }
        DefaultNutsWorkspace.log.log(Level.FINE, "Bootstrap Extension Point {0} => {1} ignored. Already registered", new Object[]{extensionPointType.getName(), extensionType.getName()});
        return false;
    }

    public List<Class> resolveComponentTypes(Class o) {
        List<Class> a = new ArrayList<>();
        if (o != null) {
            for (Class extensionPointType : SUPPORTED_EXTENSION_TYPES) {
                if (extensionPointType.isAssignableFrom(o)) {
                    a.add(extensionPointType);
                }
            }
        }
        return a;
    }

    public boolean installExtensionComponentType(Class extensionPointType, Class extensionImplType) {
        if (NutsComponent.class.isAssignableFrom(extensionPointType)) {
            if (extensionPointType.isAssignableFrom(extensionImplType)) {
                return registerType(extensionPointType, extensionImplType);
            }
            throw new ClassCastException(extensionImplType.getClass().getName());
        }
        throw new ClassCastException(NutsComponent.class.getName());
    }

    public NutsTerminal createTerminal(Class ignoredClass) {
        NutsTerminalBase termb = createSupported(NutsTerminalBase.class, ws);
        if (termb == null) {
            throw new NutsUnsupportedOperationException("Should never happen ! Terminal could not be resolved.");
        } else {
            if (ignoredClass != null && ignoredClass.equals(termb.getClass())) {
                return null;
            }
            NutsTerminalDelegate term=new NutsTerminalDelegate(termb,true);
            term.install(ws, null, null, null);
            return term;
        }
    }

    @Override
    public URLLocation[] getExtensionURLLocations(String nutsId, String appId, String extensionType) {
        List<URLLocation> bootUrls = new ArrayList<>();
        for (String r : getExtensionRepositoryLocations(nutsId)) {
            String url = r + "/" + CoreNutsUtils.getPath(CoreNutsUtils.parseNutsId(nutsId), "." + extensionType, "/");
            URL u = expandURL(url);
            bootUrls.add(new URLLocation(url, u));
        }
        return bootUrls.toArray(new URLLocation[0]);
    }

    @Override
    public String[] getExtensionRepositoryLocations(String appId) {
        //should read this form config?
        //or should be read from and extension component?
        String repos = ws.getConfigManager().getEnv("bootstrapRepositoryLocations", "") + ";"
                + NutsConstants.URL_BOOTSTRAP_LOCAL
                + ";" + NutsConstants.URL_BOOTSTRAP_REMOTE_NUTS_GIT;
        List<String> urls = new ArrayList<>();
        for (String r : CoreStringUtils.split(repos, "; ")) {
            if (!StringUtils.isEmpty(r)) {
                urls.add(r);
            }
        }
        return urls.toArray(new String[0]);
    }

    protected URL expandURL(String url) {
        try {
            url = CoreNutsUtils.expandPath(url, ws);
            if (URLUtils.isRemoteURL(url)) {
                return new URL(url);
            }
            return new File(url).toURI().toURL();
        } catch (MalformedURLException ex) {
            return null;
        }
    }


    @Override
    public List<Class> discoverTypes(Class type, ClassLoader bootClassLoader) {
        return objectFactory.discoverTypes(type, bootClassLoader);
    }

    @Override
    public <T> List<T> discoverInstances(Class<T> type, ClassLoader bootClassLoader) {
        return objectFactory.discoverInstances(type, bootClassLoader);
    }

    @Override
    public <T extends NutsComponent> T createSupported(Class<T> type, Object supportCriteria) {
        return objectFactory.createSupported(type, supportCriteria);
    }

    @Override
    public <T extends NutsComponent> T createSupported(Class<T> type, Object supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters) {
        return objectFactory.createSupported(type, supportCriteria, constructorParameterTypes, constructorParameters);
    }

    @Override
    public <T extends NutsComponent> List<T> createAllSupported(Class<T> type, Object supportCriteria) {
        return objectFactory.createAllSupported(type, supportCriteria);
    }

    @Override
    public <T> List<T> createAll(Class<T> type) {
        return objectFactory.createAll(type);
    }

    @Override
    public Set<Class> getExtensionPoints() {
        return objectFactory.getExtensionPoints();
    }

    @Override
    public Set<Class> getExtensionTypes(Class extensionPoint) {
        return objectFactory.getExtensionTypes(extensionPoint);
    }

    @Override
    public List<Object> getExtensionObjects(Class extensionPoint) {
        return objectFactory.getExtensionObjects(extensionPoint);
    }

    @Override
    public boolean isRegisteredType(Class extensionPointType, String name) {
        return objectFactory.isRegisteredType(extensionPointType, name);
    }

    @Override
    public boolean isRegisteredInstance(Class extensionPointType, Object extensionImpl) {
        return objectFactory.isRegisteredInstance(extensionPointType, extensionImpl);
    }

    @Override
    public boolean isRegisteredType(Class extensionPointType, Class extensionType) {
        return objectFactory.isRegisteredType(extensionPointType, extensionType);
    }

    protected NutsSession validateSession(NutsSession session) {
        if (session == null) {
            session = ws.createSession();
        }
        return session;
    }


}
