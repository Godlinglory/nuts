/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import net.vpc.app.nuts.util.*;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.util.*;

/**
 * Created by vpc on 2/19/17.
 */
public abstract class AbstractNutsDescriptor implements NutsDescriptor {

    public boolean matchesEnv(String arch, String os, String dist, String platform) {
        NutsId _arch = NutsId.parseNullableOrError(arch);
        NutsId _os = NutsId.parseNullableOrError(os);
        NutsId _dist = NutsId.parseNullableOrError(dist);
        NutsId _platform = NutsId.parseNullableOrError(platform);
        boolean ok;
        if (_arch != null && getArch().length > 0) {
            ok = false;
            for (String x : getArch()) {
                NutsId y = NutsId.parseOrError(x);
                if (y.isSameFullName(_arch)) {
                    if (y.getVersion().toFilter().accept(_arch.getVersion())) {
                        ok = true;
                        break;
                    }
                }
            }
            if (!ok) {
                return false;
            }
        }
        if (_os != null && getOs().length > 0) {
            ok = false;
            for (String x : getOs()) {
                NutsId y = NutsId.parseOrError(x);
                if (y.isSameFullName(_os)) {
                    if (y.getVersion().toFilter().accept(_os.getVersion())) {
                        ok = true;
                        break;
                    }
                }
            }
            if (!ok) {
                return false;
            }
        }

        if (_dist != null && getOsdist().length > 0) {
            ok = false;
            for (String x : getOsdist()) {
                NutsId y = NutsId.parseOrError(x);
                if (y.isSameFullName(_dist)) {
                    if (y.getVersion().toFilter().accept(_dist.getVersion())) {
                        ok = true;
                        break;
                    }
                }
            }
            if (!ok) {
                return false;
            }
        }

        if (_platform != null && getPlatform().length > 0) {
            ok = false;
            for (String x : getPlatform()) {
                NutsId y = NutsId.parseOrError(x);
                if (y.isSameFullName(_platform)) {
                    if (y.getVersion().toFilter().accept(_platform.getVersion())) {
                        ok = true;
                        break;
                    }
                }
            }
            return ok;
        }
        return true;
    }

    @Override
    public String getSHA1() throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        write(o, false);
        return SecurityUtils.evalSHA1(new ByteArrayInputStream(o.toByteArray()));
    }

    @Override
    public void write(File file) throws IOException {
        write(file, false);
    }

    @Override
    public void write(OutputStream file) throws IOException {
        write(file, false);
    }

    @Override
    public void write(File file, boolean pretty) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            write(os, pretty);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    public String toString(boolean pretty) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try {
            write(b, pretty);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String(b.toByteArray());
    }

    @Override
    public String toString() {
        return toString(true);
    }

    @Override
    public void write(OutputStream os, boolean pretty) throws IOException {
        NutsDescriptor desc = this;
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        objectBuilder.add("nuts-version", "1.0");
        objectBuilder.add("id", desc.getId().toString());
        objectBuilder.add("face", StringUtils.isEmpty(desc.getFace()) ? NutsConstants.QUERY_FACE_DEFAULT_VALUE : desc.getFace());
        objectBuilder.add("executable", desc.isExecutable());
        if (desc.getParents().length > 0) {
            JsonArrayBuilder p = Json.createArrayBuilder();
            for (NutsId nutsId : getParents()) {
                p.add(nutsId.toString());
            }
            objectBuilder.add("parents", p);
        }
        if (StringUtils.isEmpty(desc.getPackaging())) {
            //objectBuilder.add("packaging", JsonValue.NULL);
        } else {
            objectBuilder.add("packaging", desc.getPackaging());
        }
        if (StringUtils.isEmpty(desc.getExt())) {
            //objectBuilder.add("ext", JsonValue.NULL);
        } else {
            objectBuilder.add("ext", desc.getExt());
        }
        if (StringUtils.isEmpty(desc.getName())) {
            //objectBuilder.add("ext", JsonValue.NULL);
        } else {
            objectBuilder.add("name", desc.getName());
        }

        if (StringUtils.isEmpty(desc.getDescription())) {
            //objectBuilder.add("ext", JsonValue.NULL);
        } else {
            objectBuilder.add("description", desc.getDescription());
        }
        Map<String, String> properties = getProperties();
        if (properties != null && !properties.isEmpty()) {
            objectBuilder.add("properties", JsonUtils.serializeStringsMap(desc.getProperties(), JsonUtils.IGNORE_EMPTY_OPTIONS));
        }
        if (desc.getExecutor() != null) {
            JsonObjectBuilder objectBuilder2 = nutsExecutorDescriptorToJsonObjectBuilder(desc.getExecutor());
            if (objectBuilder2 != null) {
                objectBuilder.add("executor", objectBuilder2);
            }
        }
        if (desc.getInstaller() != null) {
            JsonObjectBuilder objectBuilder2 = nutsExecutorDescriptorToJsonObjectBuilder(desc.getInstaller());
            if (objectBuilder2 != null) {
                objectBuilder.add("installer", objectBuilder2);
            }
        }

        String[] architectures = desc.getArch();
        if (architectures != null && architectures.length > 0) {
            JsonArrayBuilder arch = Json.createArrayBuilder();
            for (String nutsId : architectures) {
                arch.add(nutsId);
            }
            objectBuilder.add("arch", arch);
        }

        architectures = desc.getOs();
        if (architectures != null && architectures.length > 0) {
            JsonArrayBuilder arch = Json.createArrayBuilder();
            for (String nutsId : architectures) {
                arch.add(nutsId);
            }
            objectBuilder.add("os", arch);
        }

        architectures = desc.getOsdist();
        if (architectures != null && architectures.length > 0) {
            JsonArrayBuilder arch = Json.createArrayBuilder();
            for (String nutsId : architectures) {
                arch.add(nutsId);
            }
            objectBuilder.add("osdist", arch);
        }

        architectures = desc.getPlatform();
        if (architectures != null && architectures.length > 0) {
            JsonArrayBuilder arch = Json.createArrayBuilder();
            for (String nutsId : architectures) {
                arch.add(nutsId);
            }
            objectBuilder.add("platform", arch);
        }

        JsonArrayBuilder dep = Json.createArrayBuilder();
        NutsDependency[] dependencies = desc.getDependencies();
        if (dependencies != null && dependencies.length > 0) {
            for (NutsDependency nutsDependency : dependencies) {
                dep.add(nutsDependency.toString());
            }
            objectBuilder.add("dependencies", dep);
        }
        JsonUtils.storeJson(objectBuilder.build(), new OutputStreamWriter(os), pretty);
    }

    private JsonObjectBuilder nutsExecutorDescriptorToJsonObjectBuilder(NutsExecutorDescriptor e) {
        JsonObjectBuilder objectBuilder2 = Json.createObjectBuilder();
        if (e != null) {
            if (e.getId() != null) {
                objectBuilder2.add("id", e.getId().toString());
            }
            if (e.getArgs() != null && e.getArgs().length > 0) {
                objectBuilder2.add("args", JsonUtils.serializeArr(e.getArgs(), JsonUtils.IGNORE_EMPTY_OPTIONS));
            }
            if (e.getProperties() != null && !e.getProperties().isEmpty()) {
                objectBuilder2.add("properties", JsonUtils.serializeObj(e.getProperties(), JsonUtils.IGNORE_EMPTY_OPTIONS));
            }
        }
        return objectBuilder2;
    }

    @Override
    public NutsDescriptor applyProperties() throws IOException {
        return applyProperties(getProperties());
    }

    @Override
    public NutsDescriptor applyParents(NutsDescriptor[] parentDescriptors) {
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
            n_ext = NutsUtils.applyStringInheritance(n_ext, parentDescriptor.getExt());
            n_name = NutsUtils.applyStringInheritance(n_name, parentDescriptor.getName());
            n_desc = NutsUtils.applyStringInheritance(n_desc, parentDescriptor.getDescription());
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
        return createInstance(
                n_id, n_alt, n_parents, n_packaging, n_executable, n_ext, n_executor, n_installer, n_name, n_desc,
                n_archs.toArray(new String[n_archs.size()]),
                n_os.toArray(new String[n_os.size()]),
                n_osdist.toArray(new String[n_osdist.size()]),
                n_platform.toArray(new String[n_platform.size()]),
                n_deps.toArray(new NutsDependency[n_deps.size()]),
                n_props
        );
    }

    @Override
    public NutsDescriptor applyProperties(Map<String, String> properties) {
        MapStringMapper map = new MapStringMapper(properties);

        NutsId n_id = getId().apply(map);
        String n_alt = NutsUtils.applyStringProperties(getFace(), map);
        String n_packaging = NutsUtils.applyStringProperties(getPackaging(), map);
        String n_ext = NutsUtils.applyStringProperties(getExt(), map);
        String n_name = NutsUtils.applyStringProperties(getName(), map);
        String n_desc = NutsUtils.applyStringProperties(getDescription(), map);
        NutsExecutorDescriptor n_executor = getExecutor();
        NutsExecutorDescriptor n_installer = getInstaller();
        Map<String, String> n_props = new HashMap<>();
        Map<String, String> properties1 = getProperties();
        if (properties1 != null) {
            for (Map.Entry<String, String> ee : properties1.entrySet()) {
                n_props.put(NutsUtils.applyStringProperties(ee.getKey(), map), NutsUtils.applyStringProperties(ee.getValue(), map));
            }
        }

        LinkedHashSet<NutsDependency> n_deps = new LinkedHashSet<>();
        for (NutsDependency d2 : getDependencies()) {
            n_deps.add(applyNutsDependencyProperties(d2, map));
        }
        return createInstance(
                n_id, n_alt, getParents(), n_packaging, isExecutable(), n_ext, n_executor, n_installer, n_name, n_desc,
                NutsUtils.applyStringProperties(getArch(), map),
                NutsUtils.applyStringProperties(getOs(), map),
                NutsUtils.applyStringProperties(getOsdist(), map),
                NutsUtils.applyStringProperties(getPlatform(), map),
                n_deps.toArray(new NutsDependency[n_deps.size()]),
                n_props
        );
    }

    private NutsId applyNutsIdProperties(NutsId child, StringMapper properties) {
        return new NutsId(
                NutsUtils.applyStringProperties(child.getNamespace(), properties),
                NutsUtils.applyStringProperties(child.getGroup(), properties),
                NutsUtils.applyStringProperties(child.getName(), properties),
                NutsUtils.applyStringProperties(child.getVersion().getValue(), properties),
                NutsUtils.applyMapProperties(child.getQueryMap(), properties)
        );
    }

    private NutsDependency applyNutsDependencyProperties(NutsDependency child, StringMapper properties) {
        return new NutsDependency(
                NutsUtils.applyStringProperties(child.getNamespace(), properties),
                NutsUtils.applyStringProperties(child.getGroup(), properties),
                NutsUtils.applyStringProperties(child.getName(), properties),
                NutsUtils.applyStringProperties(child.getVersion().getValue(), properties),
                NutsUtils.applyStringProperties(child.getScope(), properties),
                NutsUtils.applyStringProperties(child.getOptional(), properties)
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
            if (StringUtils.isEmpty(namespace)) {
                modified = true;
                namespace = parent.getNamespace();
            }
            if (StringUtils.isEmpty(group)) {
                modified = true;
                group = parent.getGroup();
            }
            if (StringUtils.isEmpty(name)) {
                modified = true;
                name = parent.getName();
            }
            if (StringUtils.isEmpty(version)) {
                modified = true;
                version = parent.getVersion().getValue();
            }
            Map<String, String> parentFaceMap = parent.getQueryMap();
            if (!parentFaceMap.isEmpty()) {
                modified = true;
                face.putAll(parentFaceMap);
            }
            if (modified) {
                return new NutsId(
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


    @Override
    public NutsDescriptor addDependency(NutsDependency dependency) {
        if (dependency == null) {
            return this;
        }
        ArrayList<NutsDependency> dependencies = new ArrayList<>(Arrays.asList(getDependencies()));
        dependencies.add(dependency);
        return setDependencies(dependencies.toArray(new NutsDependency[dependencies.size()]));
    }

    @Override
    public NutsDescriptor removeDependency(NutsDependency dependency) {
        if (dependency == null) {
            return this;
        }
        NutsDependency[] dependencies = getDependencies();
        ArrayList<NutsDependency> dependenciesList = new ArrayList<>();
        for (int i = 0; i < dependencies.length; i++) {
            NutsDependency d = dependencies[i];
            if (d.getFullName().equals(dependency.getFullName())
                    &&
                    Objects.equals(d.getScope(), dependency.getScope())
                    ) {
                //do not add
            } else {
                dependenciesList.add(d);
            }
        }
        return setDependencies(dependenciesList.toArray(new NutsDependency[dependenciesList.size()]));
    }

    @Override
    public NutsDescriptor addDependencies(NutsDependency[] dependencies) {
        if (dependencies == null || dependencies.length == 0) {
            return this;
        }
        ArrayList<NutsDependency> dependenciesList = new ArrayList<>(Arrays.asList(getDependencies()));
        dependenciesList.addAll(Arrays.asList(dependencies));
        return setDependencies(dependenciesList.toArray(new NutsDependency[dependenciesList.size()]));
    }

    @Override
    public NutsDescriptor setDependencies(NutsDependency[] dependencies) {
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                getPlatform(),
                dependencies,
                getProperties()
        );
    }

    @Override
    public NutsDescriptor addOs(String os) {
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                CollectionUtils.toArraySet(getOs(), new String[]{os}),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor removeOs(String os) {
        Set<String> vals = CollectionUtils.toSet(getOs());
        vals.remove(os);
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                vals.toArray(new String[vals.size()]),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor addOsdist(String osdist) {
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                CollectionUtils.toArraySet(getOsdist(), new String[]{osdist}),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor removeOsdist(String os) {
        Set<String> vals = CollectionUtils.toSet(getOs());
        vals.remove(os);
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                vals.toArray(new String[vals.size()]),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor addPlatform(String platform) {
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                CollectionUtils.toArraySet(getPlatform(), new String[]{platform}),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor removePlatform(String os) {
        Set<String> vals = CollectionUtils.toSet(getPlatform());
        vals.remove(os);
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                vals.toArray(new String[vals.size()]),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor addArch(String arch) {
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                CollectionUtils.toArraySet(getArch(), new String[]{arch}),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor removeArch(String arch) {
        Set<String> vals = CollectionUtils.toSet(getArch());
        vals.remove(arch);
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                vals.toArray(new String[vals.size()]),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor addProperty(String name, String value) {
        Map<String, String> properties = new HashMap<>(getProperties());
        properties.put(name,value);
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                properties
        );
    }

    @Override
    public NutsDescriptor removeProperty(String name) {
        Map<String, String> properties = new HashMap<>(getProperties());
        properties.remove(name);
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                properties
        );
    }

    @Override
    public NutsDescriptor setExt(String ext) {
        if (StringUtils.trim(ext).equals(getExt())) {
            return this;
        }
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                ext,
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor setPackaging(String packaging) {
        if (StringUtils.trim(packaging).equals(getPackaging())) {
            return this;
        }
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                packaging,
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor setExecutable(boolean executable) {
        if (executable == isExecutable()) {
            return this;
        }
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                executable,
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor setExecutor(NutsExecutorDescriptor executor) {
        if (Objects.equals(executor, getExecutor())) {
            return this;
        }
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                executor,
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor setId(NutsId id) {
        if (Objects.equals(id, getId())) {
            return this;
        }
        return createInstance(
                id,
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                getProperties()
        );
    }

    @Override
    public NutsDescriptor setProperties(Map<String, String> map, boolean append) {
        Map<String, String> l_properties = new HashMap<>();
        if (append) {
            l_properties.putAll(getProperties());
        }
        if (map != null) {
            l_properties.putAll(map);
        }
        if (Objects.equals(l_properties, getProperties())) {
            return this;
        }
        return createInstance(
                getId(),
                getFace(),
                getParents(),
                getPackaging(),
                isExecutable(),
                getExt(),
                getExecutor(),
                getInstaller(),
                getName(),
                getDescription(),
                getArch(),
                getOs(),
                getOsdist(),
                getPlatform(),
                getDependencies(),
                l_properties
        );
    }

    protected NutsDescriptor createInstance(NutsId id, String face, NutsId[] parents, String packaging, boolean executable, String ext, NutsExecutorDescriptor executor, NutsExecutorDescriptor installer, String name, String description,
                                            String[] arch, String[] os, String[] osdist, String[] platform,
                                            NutsDependency[] dependencies, Map<String, String> properties) {
        throw new IllegalArgumentException("Unmodifiable instance");
    }
}
