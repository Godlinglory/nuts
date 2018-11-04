package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreCollectionUtils;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.app.nuts.extensions.util.MapStringMapper;

import java.util.*;

public class DefaultNutsDescriptorBuilder implements NutsDescriptorBuilder {
    private static final long serialVersionUID = 1l;

    private NutsId id;
    private String face;
    private NutsId[] parents;
    private String packaging;
    private String ext;
    private boolean executable;
    private NutsExecutorDescriptor executor;
    private NutsExecutorDescriptor installer;
    /**
     * short description
     */
    private String name;
    /**
     * some longer (but not too long) description
     */
    private String description;
    private List<String> arch;
    private List<String> os;
    private List<String> osdist;
    private List<String> platform;
    private List<String> locations;
    private List<NutsDependency> dependencies;
    private Map<String, String> properties;

    public DefaultNutsDescriptorBuilder() {
    }

    public DefaultNutsDescriptorBuilder(NutsId id, String face, NutsId[] parents, String packaging, boolean executable, String ext,
                                        NutsExecutorDescriptor executor, NutsExecutorDescriptor installer, String name, String description,
                                        String[] arch, String[] os, String[] osdist, String[] platform,
                                        NutsDependency[] dependencies, String[] locations, Map<String, String> properties) {
        setId(id);
        setFace(face);
        setPackaging(packaging);
        setParents(parents);
        setExecutable(executable);
        setDescription(description);
        setName(name);
        setExecutor(executor);
        setInstaller(installer);
        setExt(ext);
        setArch(arch);
        setOs(os);
        setOsdist(osdist);
        setPlatform(platform);
        setLocations(locations);
        setDependencies(dependencies);
        setProperties(properties, false);
    }

    public DefaultNutsDescriptorBuilder(NutsDescriptorBuilder other) {
        setId(other.getId());
        setFace(other.getFace());
        setPackaging(other.getPackaging());
        setParents(other.getParents());
        setExecutable(other.isExecutable());
        setDescription(other.getDescription());
        setName(other.getName());
        setExecutor(other.getExecutor());
        setInstaller(other.getInstaller());
        setExt(other.getExt());
        setArch(other.getArch());
        setOs(other.getOs());
        setOsdist(other.getOsdist());
        setPlatform(other.getPlatform());
        setLocations(other.getLocations());
        setDependencies(other.getDependencies());
        setProperties(other.getProperties(), false);
    }

    public DefaultNutsDescriptorBuilder(NutsDescriptor other) {
        setId(other.getId());
        setFace(other.getFace());
        setPackaging(other.getPackaging());
        setParents(other.getParents());
        setExecutable(other.isExecutable());
        setDescription(other.getDescription());
        setName(other.getName());
        setExecutor(other.getExecutor());
        setInstaller(other.getInstaller());
        setExt(other.getExt());
        setArch(other.getArch());
        setOs(other.getOs());
        setOsdist(other.getOsdist());
        setPlatform(other.getPlatform());
        setLocations(other.getLocations());
        setDependencies(other.getDependencies());
        setProperties(other.getProperties(), false);
    }

    @Override
    public NutsDescriptorBuilder setId(String id) {
        this.id = CoreNutsUtils.parseOrErrorNutsId(id);
        return this;
    }

    @Override
    public NutsDescriptorBuilder setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsDescriptorBuilder setName(String name) {
        this.name = CoreStringUtils.trim(name);
        return this;
    }

    @Override
    public NutsDescriptorBuilder setExecutor(NutsExecutorDescriptor executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public NutsDescriptorBuilder setInstaller(NutsExecutorDescriptor installer) {
        this.installer = installer;
        return this;
    }

    @Override
    public NutsDescriptorBuilder setFace(String face) {
        this.face = face;
        return this;
    }

    @Override
    public NutsDescriptorBuilder setDescription(String description) {
        this.description = CoreStringUtils.trim(description);
        return this;
    }

    @Override
    public NutsDescriptorBuilder setExecutable(boolean executable) {
        this.executable = executable;
        return this;
    }

    @Override
    public NutsDescriptorBuilder setExt(String ext) {
        this.ext = CoreStringUtils.trim(ext);
        return this;
    }

    public NutsDescriptorBuilder addPlatform(String platform) {
        if (platform != null) {
            if (this.platform == null) {
                this.platform = new ArrayList<>();
            }
            this.platform.add(platform);
        }
        return this;
    }

    public NutsDescriptorBuilder setPlatform(String[] platform) {
        this.platform = new ArrayList<>(Arrays.asList(CoreCollectionUtils.toArraySet(platform)));
        return this;
    }

    public NutsDescriptorBuilder setOs(String[] os) {
        this.os = new ArrayList<>(Arrays.asList(CoreCollectionUtils.toArraySet(os)));
        return this;
    }

    public NutsDescriptorBuilder setOsdist(String[] osdist) {
        this.osdist = new ArrayList<>(Arrays.asList(CoreCollectionUtils.toArraySet(osdist)));
        return this;
    }

    public NutsDescriptorBuilder setArch(String[] arch) {
        this.arch = new ArrayList<>(Arrays.asList(CoreCollectionUtils.toArraySet(arch)));
        return this;
    }

    @Override
    public NutsDescriptorBuilder setProperties(Map<String, String> map) {
        return setProperties(map, false);
    }

    @Override
    public NutsDescriptorBuilder setProperties(Map<String, String> properties, boolean append) {
        if (append) {
            if (properties == null || properties.isEmpty()) {
                //do nothing
            } else {
                HashMap<String, String> p = new HashMap<>(this.properties);
                p.putAll(properties);
                this.properties = p;
            }

        } else {
            if (properties == null || properties.isEmpty()) {
                this.properties = null;
            } else {
                HashMap<String, String> p = new HashMap<>(properties);
                this.properties = p;
            }
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder addLocation(String location) {
        if (this.locations == null) {
            this.locations = new ArrayList<>();
        }
        this.locations.add(location);
        return this;
    }

    @Override
    public NutsDescriptorBuilder setLocations(String[] locations) {
        this.locations = new ArrayList<>(Arrays.asList(locations));
        return this;
    }

    @Override
    public NutsDescriptorBuilder setPackaging(String packaging) {
        this.packaging = CoreStringUtils.trim(packaging);
        return this;
    }

    public NutsDescriptorBuilder setParents(NutsId[] parents) {
        this.parents = parents == null ? new NutsId[0] : new NutsId[parents.length];
        if (parents != null) {
            System.arraycopy(parents, 0, this.parents, 0, this.parents.length);
        }
        return this;
    }

    public String getFace() {
        return face;
    }

    @Override
    public NutsExecutorDescriptor getInstaller() {
        return installer;
    }

    @Override
    public Map<String, String> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }
        return properties;
    }

    @Override
    public NutsId[] getParents() {
        return parents;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isExecutable() {
        return executable;
    }

    @Override
    public NutsExecutorDescriptor getExecutor() {
        return executor;
    }

    @Override
    public String getExt() {
        return ext;
    }

    @Override
    public String getPackaging() {
        return packaging;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsDependency[] getDependencies() {
        return dependencies == null ? new NutsDependency[0] : dependencies.toArray(new NutsDependency[dependencies.size()]);
    }

    @Override
    public String[] getArch() {
        return arch == null ? new String[0] :
                arch.toArray(new String[arch.size()]);
    }

    public String[] getOs() {
        return os == null ? new String[0] : os.toArray(new String[os.size()]);
    }

    public String[] getOsdist() {
        return osdist == null ? new String[0] : osdist.toArray(new String[osdist.size()]);
    }

    public String[] getPlatform() {
        return platform == null ? new String[0] : platform.toArray(new String[platform.size()]);
    }

    @Override
    public NutsDescriptorBuilder setDependencies(NutsDependency[] dependencies) {
        this.dependencies = new ArrayList<>();
        for (int i = 0; i < dependencies.length; i++) {
            if (dependencies[i] == null) {
                throw new NullPointerException();
            }
            this.dependencies.add(dependencies[i]);
        }
        return this;
    }

    @Override
    public NutsDescriptor build() {
        return new DefaultNutsDescriptor(
                getId(), getFace(), getParents(), getPackaging(), isExecutable(), getExt(), getExecutor(), getInstaller()
                , getName(), getDescription(), getArch(), getOs(), getOsdist(), getPlatform(), getDependencies(),
                getLocations(), getProperties()
        );
    }

    @Override
    public String[] getLocations() {
        return locations == null ? new String[0] : locations.toArray(new String[locations.size()]);
    }

    @Override
    public NutsDescriptorBuilder addProperty(String name, String value) {
        properties.put(name, value);
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeProperty(String name) {
        properties.get(name);
        return this;
    }

    @Override
    public NutsDescriptorBuilder addOs(String os) {
        if (this.os == null) {
            this.os = new ArrayList<>();
        }
        this.os.add(os);
        return this;
    }

    @Override
    public NutsDescriptorBuilder addOsdist(String osdist) {
        if (this.osdist == null) {
            this.osdist = new ArrayList<>();
        }
        this.osdist.add(osdist);
        return this;
    }

    @Override
    public NutsDescriptorBuilder addArch(String arch) {
        if (this.arch == null) {
            this.arch = new ArrayList<>();
        }
        this.arch.add(arch);
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeOs(String os) {
        if (this.os != null) {
            this.os.remove(os);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeOsdist(String osdist) {
        if (this.osdist != null) {
            this.osdist.remove(osdist);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeArch(String arch) {
        if (this.arch != null) {
            this.arch.remove(arch);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder removePlatform(String platform) {
        if (this.platform != null) {
            this.platform.remove(platform);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder removeDependency(NutsDependency dependency) {
        if (this.dependencies != null) {
            this.dependencies.remove(dependency);
        }
        return this;
    }

    @Override
    public NutsDescriptorBuilder addDependency(NutsDependency dependency) {
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<>();
        }
        this.dependencies.add(dependency);
        return this;
    }

    @Override
    public NutsDescriptorBuilder addDependencies(NutsDependency[] dependencies) {
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<>();
        }
        this.dependencies.addAll(Arrays.asList(dependencies));
        return this;
    }

    @Override
    public NutsDescriptorBuilder applyProperties() {
        return applyProperties(getProperties());
    }

    @Override
    public NutsDescriptorBuilder applyParents(NutsDescriptor[] parentDescriptors) {
        NutsId n_id = getId();
        String n_alt = getFace();
        String n_packaging = getPackaging();
        String n_ext = getExt();
        boolean n_executable = isExecutable();
        String n_name = getName();
        String n_desc = getDescription();
        NutsExecutorDescriptor n_executor = getExecutor();
        NutsExecutorDescriptor n_installer = getInstaller();
        Map<String, String> n_props = new HashMap<>();
        for (NutsDescriptor parentDescriptor : parentDescriptors) {
            n_props.putAll(parentDescriptor.getProperties());
        }
        Map<String, String> properties = getProperties();
        if (properties != null) {
            n_props.putAll(properties);
        }
        LinkedHashSet<NutsDependency> n_deps = new LinkedHashSet<>();
        LinkedHashSet<String> n_archs = new LinkedHashSet<>();
        LinkedHashSet<String> n_os = new LinkedHashSet<>();
        LinkedHashSet<String> n_osdist = new LinkedHashSet<>();
        LinkedHashSet<String> n_platform = new LinkedHashSet<>();
        for (NutsDescriptor parentDescriptor : parentDescriptors) {
            n_id = applyNutsIdInheritance(n_id, parentDescriptor.getId());
            if (!n_executable && parentDescriptor.isExecutable()) {
                n_executable = true;
            }
            if (n_executor == null) {
                n_executor = parentDescriptor.getExecutor();
            }
            if (n_executor == null) {
                n_installer = parentDescriptor.getInstaller();
            }

            //packaging is not inherited!!
            //n_packaging = applyStringInheritance(n_packaging, parentDescriptor.getPackaging());
            n_ext = CoreNutsUtils.applyStringInheritance(n_ext, parentDescriptor.getExt());
            n_name = CoreNutsUtils.applyStringInheritance(n_name, parentDescriptor.getName());
            n_desc = CoreNutsUtils.applyStringInheritance(n_desc, parentDescriptor.getDescription());
            n_deps.addAll(Arrays.asList(parentDescriptor.getDependencies()));
            n_archs.addAll(Arrays.asList(parentDescriptor.getArch()));
            n_os.addAll(Arrays.asList(parentDescriptor.getOs()));
            n_osdist.addAll(Arrays.asList(parentDescriptor.getOsdist()));
            n_platform.addAll(Arrays.asList(parentDescriptor.getPlatform()));
        }
        n_deps.addAll(Arrays.asList(getDependencies()));
        n_archs.addAll(Arrays.asList(getArch()));
        n_os.addAll(Arrays.asList(getOs()));
        n_osdist.addAll(Arrays.asList(getOsdist()));
        n_platform.addAll(Arrays.asList(getPlatform()));
        NutsId[] n_parents = new NutsId[0];
        if (n_packaging.isEmpty() && n_ext.isEmpty()) {
            n_packaging = "jar";
            n_ext = "jar";
        } else if (n_packaging.isEmpty()) {
            n_packaging = n_ext;
        } else {
            n_ext = n_packaging;
        }

        setId(n_id);
        setFace(n_alt);
        setParents(n_parents);
        setPackaging(n_packaging);
        setExecutable(n_executable);
        setExt(n_ext);
        setExecutor(n_executor);
        setInstaller(n_installer);
        setName(n_name);
        setDescription(n_desc);
        setArch(n_archs.toArray(new String[n_archs.size()]));
        setOs(n_os.toArray(new String[n_os.size()]));
        setOsdist(n_osdist.toArray(new String[n_osdist.size()]));
        setPlatform(n_platform.toArray(new String[n_platform.size()]));
        setDependencies(n_deps.toArray(new NutsDependency[n_deps.size()]));
        setProperties(n_props);
        return this;
    }

    @Override
    public NutsDescriptorBuilder applyProperties(Map<String, String> properties) {
        MapStringMapper map = new MapStringMapper(properties);

        NutsId n_id = getId().apply(map);
        String n_alt = CoreNutsUtils.applyStringProperties(getFace(), map);
        String n_packaging = CoreNutsUtils.applyStringProperties(getPackaging(), map);
        String n_ext = CoreNutsUtils.applyStringProperties(getExt(), map);
        String n_name = CoreNutsUtils.applyStringProperties(getName(), map);
        String n_desc = CoreNutsUtils.applyStringProperties(getDescription(), map);
        NutsExecutorDescriptor n_executor = getExecutor();
        NutsExecutorDescriptor n_installer = getInstaller();
        Map<String, String> n_props = new HashMap<>();
        Map<String, String> properties1 = getProperties();
        if (properties1 != null) {
            for (Map.Entry<String, String> ee : properties1.entrySet()) {
                n_props.put(CoreNutsUtils.applyStringProperties(ee.getKey(), map), CoreNutsUtils.applyStringProperties(ee.getValue(), map));
            }
        }

        LinkedHashSet<NutsDependency> n_deps = new LinkedHashSet<>();
        for (NutsDependency d2 : getDependencies()) {
            n_deps.add(applyNutsDependencyProperties(d2, map));
        }

        this.setId(n_id);
        this.setFace(n_alt);
        this.setParents(getParents());
        this.setPackaging(n_packaging);
        this.setExecutable(isExecutable());
        this.setExt(n_ext);
        this.setExecutor(n_executor);
        this.setInstaller(n_installer);
        this.setName(n_name);
        this.setDescription(n_desc);
        this.setArch(CoreNutsUtils.applyStringProperties(getArch(), map));
        this.setOs(CoreNutsUtils.applyStringProperties(getOs(), map));
        this.setOsdist(CoreNutsUtils.applyStringProperties(getOsdist(), map));
        this.setPlatform(CoreNutsUtils.applyStringProperties(getPlatform(), map));
        this.setDependencies(n_deps.toArray(new NutsDependency[n_deps.size()]));
        this.setProperties(n_props);
        return this;
    }

    private NutsId applyNutsIdProperties(NutsId child, StringMapper properties) {
        return new NutsIdImpl(
                CoreNutsUtils.applyStringProperties(child.getNamespace(), properties),
                CoreNutsUtils.applyStringProperties(child.getGroup(), properties),
                CoreNutsUtils.applyStringProperties(child.getName(), properties),
                CoreNutsUtils.applyStringProperties(child.getVersion().getValue(), properties),
                CoreNutsUtils.applyMapProperties(child.getQueryMap(), properties)
        );
    }

    private NutsDependency applyNutsDependencyProperties(NutsDependency child, StringMapper properties) {
        NutsId[] exclusions = child.getExclusions();
        for (int i = 0; i < exclusions.length; i++) {
            exclusions[i] = applyNutsIdProperties(exclusions[i], properties);
        }
        return new NutsDependencyImpl(
                CoreNutsUtils.applyStringProperties(child.getNamespace(), properties),
                CoreNutsUtils.applyStringProperties(child.getGroup(), properties),
                CoreNutsUtils.applyStringProperties(child.getName(), properties),
                CoreNutsUtils.applyStringProperties(child.getVersion().getValue(), properties),
                CoreNutsUtils.applyStringProperties(child.getScope(), properties),
                CoreNutsUtils.applyStringProperties(child.getOptional(), properties),
                exclusions
        );
    }

    private NutsId applyNutsIdInheritance(NutsId child, NutsId parent) {
        if (parent != null) {
            boolean modified = false;
            String namespace = child.getNamespace();
            String group = child.getGroup();
            String name = child.getName();
            String version = child.getVersion().getValue();
            Map<String, String> face = child.getQueryMap();
            if (CoreStringUtils.isEmpty(namespace)) {
                modified = true;
                namespace = parent.getNamespace();
            }
            if (CoreStringUtils.isEmpty(group)) {
                modified = true;
                group = parent.getGroup();
            }
            if (CoreStringUtils.isEmpty(name)) {
                modified = true;
                name = parent.getName();
            }
            if (CoreStringUtils.isEmpty(version)) {
                modified = true;
                version = parent.getVersion().getValue();
            }
            Map<String, String> parentFaceMap = parent.getQueryMap();
            if (!parentFaceMap.isEmpty()) {
                modified = true;
                face.putAll(parentFaceMap);
            }
            if (modified) {
                return new NutsIdImpl(
                        namespace,
                        group,
                        name,
                        version,
                        face
                );
            }
        }
        return child;
    }

}
