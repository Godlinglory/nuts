/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.boot.PrivateNutsUtilCollections;
import net.thevpc.nuts.boot.PrivateNutsUtilMaps;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Nuts environment condition builder, used to check against the current environment
 *
 * @app.category Descriptor
 * @since 0.8.3
 */
public class DefaultNutsEnvConditionBuilder implements Serializable, NutsEnvConditionBuilder {
    private static final long serialVersionUID = 1L;

    private List<String> arch = new ArrayList<>(); //defaults to empty
    private List<String> os = new ArrayList<>(); //defaults to empty;
    private List<String> osDist = new ArrayList<>(); //defaults to empty;
    private List<String> platform = new ArrayList<>(); //defaults to empty;
    private List<String> desktopEnvironment = new ArrayList<>(); //defaults to empty;
    private List<String> profiles = new ArrayList<>(); //defaults to empty;
    private Map<String, String> properties = new HashMap<>();

    public DefaultNutsEnvConditionBuilder() {
    }

    public DefaultNutsEnvConditionBuilder(NutsEnvCondition other) {
        addAll(other);
    }

    public List<String> getArch() {
        return arch;
    }

    @Override
    public NutsEnvConditionBuilder setArch(List<String> arch) {
        this.arch = PrivateNutsUtilCollections.uniqueNonBlankList(arch);
        return this;
    }

    public List<String> getOs() {
        return os;
    }

    @Override
    public NutsEnvConditionBuilder setOs(List<String> os) {
        this.os = PrivateNutsUtilCollections.uniqueNonBlankList(os);
        return this;
    }

    public List<String> getOsDist() {
        return osDist;
    }

    @Override
    public NutsEnvConditionBuilder setOsDist(List<String> osDist) {
        this.osDist = PrivateNutsUtilCollections.uniqueNonBlankList(osDist);
        return this;
    }

    public List<String> getPlatform() {
        return platform;
    }

    @Override
    public NutsEnvConditionBuilder setPlatform(List<String> platform) {
        this.platform = PrivateNutsUtilCollections.uniqueNonBlankList(platform);
        return this;
    }

    public List<String> getDesktopEnvironment() {
        return desktopEnvironment;
    }

    @Override
    public NutsEnvConditionBuilder setDesktopEnvironment(List<String> desktopEnvironment) {
        this.desktopEnvironment = PrivateNutsUtilCollections.uniqueNonBlankList(desktopEnvironment);
        return this;
    }

    public List<String> getProfile() {
        return profiles;
    }

    @Override
    public NutsEnvConditionBuilder setProfile(List<String> profiles) {
        this.profiles = profiles;
        return this;
    }

    @Override
    public NutsEnvConditionBuilder setAll(NutsEnvCondition other) {
        clear();
        addAll(other);
        return this;
    }

    @Override
    public NutsEnvConditionBuilder addAll(NutsEnvCondition other) {
        if (other != null) {
            setArch(mergeLists(getArch(), other.getArch()));
            setOs(mergeLists(getOs(), other.getOs()));
            setOsDist(mergeLists(getOsDist(), other.getOsDist()));
            setPlatform(mergeLists(getPlatform(), other.getPlatform()));
            setDesktopEnvironment(mergeLists(getDesktopEnvironment(), other.getDesktopEnvironment()));
            setProfile(mergeLists(getProfile(), other.getProfile()));
            setProperties(mergeMaps(getProperties(), other.getProperties()));
        }
        return this;
    }

    private Map<String, String> mergeMaps(Map<String, String> a, Map<String, String> b) {
        LinkedHashMap<String, String> n = new LinkedHashMap<>();
        if (a != null) {
            n.putAll(a);
        }
        if (a != null) {
            n.putAll(b);
        }
        return n;
    }

    private List<String> mergeLists(List<String> a, List<String> b) {
        LinkedHashSet<String> n = new LinkedHashSet<>();
        if (a != null) {
            n.addAll(a);
        }
        if (a != null) {
            n.addAll(b);
        }
        return new ArrayList<>(n);
    }


    @Override
    public NutsEnvConditionBuilder clear() {
        setArch(new ArrayList<>());
        setOs(new ArrayList<>());
        setOsDist(new ArrayList<>());
        setPlatform(new ArrayList<>());
        setDesktopEnvironment(new ArrayList<>());
        setProfile(new ArrayList<>());
        setProperties(new LinkedHashMap<>());
        return this;
    }

    @Override
    public NutsEnvCondition build() {
        return readOnly();
    }

    @Override
    public NutsEnvCondition copy() {
        return builder();
    }

    @Override
    public NutsEnvCondition readOnly() {
        return new DefaultNutsEnvCondition(
                getArch(), getOs(), getOsDist(), getPlatform(),
                getDesktopEnvironment(),
                getProfile(),
                properties
        );
    }

    @Override
    public NutsEnvConditionBuilder builder() {
        return new DefaultNutsEnvConditionBuilder(this);
    }

    @Override
    public String toString() {
        String s = String.join(" & ",
                Arrays.stream(new String[]{
                                ts("arch", arch.toArray(new String[0])),
                                ts("os", os.toArray(new String[0])),
                                ts("osDist", osDist.toArray(new String[0])),
                                ts("platform", platform.toArray(new String[0])),
                                ts("desktop", desktopEnvironment.toArray(new String[0])),
                                ts("profile", profiles.toArray(new String[0])),
                                ts("props", properties)
                        })
                        .filter(x -> x.length() > 0)
                        .toArray(String[]::new)
        );
        if (s.isEmpty()) {
            return "blank";
        }
        return s;
    }

    private String ts(String n, String[] vs) {
        if (vs.length == 0) {
            return "";
        }
        return n + "=" + String.join(",", vs[0]);
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public NutsEnvConditionBuilder setProperties(Map<String, String> properties) {
        this.properties = properties == null ? null : new HashMap<>(properties);
        return this;
    }

    private String ts(String n, Map<String, String> properties) {
        if (properties.isEmpty()) {
            return "";
        }
        return n + "={" + properties.entrySet().stream().map(x -> {
            String k = x.getKey();
            String v = x.getValue();
            if (v == null) {
                return k;
            }
            return k + "=" + v;
        }).collect(Collectors.joining(",")) + "}";
    }

    @Override
    public boolean isBlank() {
        if (arch != null && !arch.isEmpty()) return false;
        if (os != null && !os.isEmpty()) return false;
        if (osDist != null && !osDist.isEmpty()) return false;
        if (platform != null && !platform.isEmpty()) return false;
        if (desktopEnvironment != null && !desktopEnvironment.isEmpty()) return false;
        if (profiles != null && !profiles.isEmpty()) return false;
        if (properties != null && !properties.isEmpty()) return false;
        return true;
    }

    public Map<String, String> toMap() {
        return PrivateNutsUtilMaps.toMap(this);
    }
}
