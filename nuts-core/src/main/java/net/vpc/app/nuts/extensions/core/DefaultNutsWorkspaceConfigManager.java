/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.core;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.vpc.app.nuts.MapListener;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsBootWorkspace;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsSecurityException;
import net.vpc.app.nuts.NutsWorkspaceConfig;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;
import net.vpc.app.nuts.NutsWorkspaceObjectFactory;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreJsonUtils;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CorePlatformUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.app.nuts.extensions.util.ObservableMap;

/**
 *
 * @author vpc
 */
class DefaultNutsWorkspaceConfigManager implements NutsWorkspaceConfigManager {

    private final DefaultNutsWorkspace ws;
    private NutsId workspaceBootId;
    private NutsBootWorkspace workspaceBoot;
    private NutsId workspaceRuntimeId;
    private String workspaceRoot;
    private ClassLoader bootClassLoader;
    private NutsWorkspaceConfig config = new NutsWorkspaceConfigImpl();
    private String workspace;
    private File cwd = new File(System.getProperty("user.dir"));
    private ObservableMap<String, Object> sharedObjects = new ObservableMap<>();

    protected DefaultNutsWorkspaceConfigManager(final DefaultNutsWorkspace outer) {
        this.ws = outer;
    }

    public NutsBootWorkspace getWorkspaceBoot() {
        return workspaceBoot;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    public void setConfig(NutsWorkspaceConfig config) {
        this.config = config;
    }

    @Override
    public NutsId getWorkspaceRuntimeId() {
        return workspaceRuntimeId;
    }

    @Override
    public NutsId getWorkspaceBootId() {
        return workspaceBootId;
    }

    @Override
    public void addImports(String... importExpressions) {
        Set<String> imports = new HashSet<>(Arrays.asList(getConfig().getImports()));
        if (importExpressions != null) {
            for (String importExpression : importExpressions) {
                if (importExpression != null) {
                    for (String s : importExpression.split("[,;: ]")) {
                        imports.add(s.trim());
                    }
                }
            }
        }
        String[] arr = imports.toArray(new String[imports.size()]);
        Arrays.sort(arr);
        setImports(arr);
    }

    @Override
    public void removeAllImports() {
        setImports(null);
    }

    @Override
    public void removeImports(String... importExpressions) {
        Set<String> imports = new HashSet<>(Arrays.asList(getConfig().getImports()));
        if (importExpressions != null) {
            for (String importExpression : importExpressions) {
                if (importExpression != null) {
                    for (String s : importExpression.split("[,;: ]")) {
                        imports.remove(s.trim());
                    }
                }
            }
        }
        String[] arr = imports.toArray(new String[imports.size()]);
        Arrays.sort(arr);
        setImports(arr);
    }

    @Override
    public void setImports(String[] imports) {
        Set<String> simports = new HashSet<>();
        if (imports != null) {
            for (String s : imports) {
                if (s == null) {
                    s = "";
                }
                s = s.trim();
                if (!CoreStringUtils.isEmpty(s)) {
                    simports.add(s);
                }
            }
        }
        String[] arr = simports.toArray(new String[simports.size()]);
        Arrays.sort(arr);
        getConfig().setImports(imports);
    }

    @Override
    public String[] getImports() {
        String[] envImports = getConfig().getImports();
        HashSet<String> all = new HashSet<>(Arrays.asList(envImports));
        //        public static final String ENV_KEY_IMPORTS = "imports";
        //workaround
        String extraImports = getEnv("imports", null);
        if (extraImports != null) {
            for (String s : extraImports.split("[,;: ]")) {
                all.add(s);
            }
        }
        return all.toArray(new String[all.size()]);
    }

    @Override
    public String getEnv(String property, String defaultValue) {
        return getConfig().getEnv(property, defaultValue);
    }

    @Override
    public void setEnv(String property, String value) {
        getConfig().setEnv(property, value);
    }

    @Override
    public Properties getEnv() {
        Properties p = new Properties();
        p.putAll(getConfig().getEnv());
        return p;
    }

    @Override
    public NutsWorkspaceConfig getConfig() {
        return config;
    }

    @Override
    public String getWorkspaceLocation() {
        return workspace;
    }

    @Override
    public void save() {
        if (!ws.getSecurityManager().isAllowed(NutsConstants.RIGHT_SAVE_WORKSPACE)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_SAVE_WORKSPACE);
        }
        File file = CoreIOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
        CoreJsonUtils.get(ws.self()).storeJson(config, file, CoreJsonUtils.PRETTY_IGNORE_EMPTY_OPTIONS);
        for (NutsRepository repo : ws.getEnabledRepositories()) {
            repo.save();
        }
    }

    @Override
    public String getWorkspaceRootLocation() {
        return workspaceRoot;
    }

    @Override
    public Map<String, String> getRuntimeProperties() {
        Map<String, String> map = new HashMap<>();
        map.put("nuts.workspace-boot.version", workspaceBootId.getVersion().toString());
        map.put("nuts.workspace-boot.id", workspaceBootId.toString());
        map.put("nuts.workspace-runtime.id", getWorkspaceRuntimeId().toString());
        map.put("nuts.workspace-runtime.version", getWorkspaceRuntimeId().getVersion().toString());
        map.put("nuts.workspace-location", NutsWorkspaceHelper.resolveImmediateWorkspacePath(workspace, NutsConstants.DEFAULT_WORKSPACE_NAME, getWorkspaceRootLocation()));
        return map;
    }

    @Override
    public NutsBootWorkspace getBoot() {
        return workspaceBoot;
    }

    public void onInitializeWorkspace(NutsBootWorkspace workspaceBoot,
            String worksaceRoot,
            NutsWorkspaceObjectFactory factory,
            NutsId workspaceBootId, NutsId workspaceRuntimeId, String workspace,
            ClassLoader bootClassLoader) {
        this.workspaceBoot = workspaceBoot;
        this.workspaceRoot = worksaceRoot;
        this.workspaceBootId = workspaceBootId;
        this.workspaceRuntimeId = workspaceRuntimeId;
        this.bootClassLoader = bootClassLoader;
        this.workspace = workspace;
    }

    @Override
    public File getCwd() {
        return cwd;
    }

    @Override
    public void setCwd(File cwd) {
        if (cwd == null) {
            throw new NutsIllegalArgumentException("Invalid cwd");
        }
        if (!cwd.isDirectory()) {
            throw new NutsIllegalArgumentException("Invalid cwd " + cwd);
        }
        if (!cwd.isAbsolute()) {
            throw new NutsIllegalArgumentException("Invalid cwd " + cwd);
        }
        this.cwd = cwd;
    }

    @Override
    public File resolveNutsJarFile() {
        try {
            NutsId baseId = CoreNutsUtils.parseOrErrorNutsId(NutsConstants.NUTS_ID_BOOT);
            String urlPath = "/META-INF/maven/" + baseId.getGroup() + "/" + baseId.getName() + "/pom.properties";
            URL resource = Nuts.class.getResource(urlPath);
            if (resource != null) {
                URL runtimeURL = CorePlatformUtils.resolveURLFromResource(Nuts.class, urlPath);
                return CorePlatformUtils.resolveLocalFileFromURL(runtimeURL);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }

    public void addSharedObjectsListener(MapListener<String, Object> listener) {
        sharedObjects.addListener(listener);
    }

    @Override
    public void removeSharedObjectsListener(MapListener<String, Object> listener) {
        sharedObjects.removeListener(listener);
    }

    @Override
    public MapListener<String, Object>[] getSharedObjectsListeners() {
        return sharedObjects.getListeners();
    }

    @Override
    public Map<String, Object> getSharedObjects() {
        return sharedObjects;
    }

    @Override
    public String toString() {
        return "NutsWorkspaceConfig{" +
                "workspaceBootId=" + workspaceBootId +
                ", workspaceBoot=" + workspaceBoot +
                ", workspaceRuntimeId=" + workspaceRuntimeId +
                ", workspaceRoot='" + workspaceRoot + '\'' +
                ", workspace='" + workspace + '\'' +
                ", cwd=" + cwd +
                '}';
    }
}
