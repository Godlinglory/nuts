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

import net.thevpc.nuts.boot.PrivateNutsCommaStringParser;
import net.thevpc.nuts.boot.PrivateNutsIdListParser;
import net.thevpc.nuts.boot.PrivateNutsQueryStringParser;
import net.thevpc.nuts.boot.PrivateNutsUtilStrings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsIdBuilder implements NutsIdBuilder {

    private String groupId;
    private String artifactId;
    private String classifier;
    private NutsVersion version;
    private NutsEnvConditionBuilder condition;
    private transient PrivateNutsQueryStringParser propertiesQuery = new PrivateNutsQueryStringParser(true, (name, value) -> {
        if (name != null) {
            switch (name) {
                case NutsConstants.IdProperties.VERSION: {
                    setVersion(value);
                    return true;
                }
                case NutsConstants.IdProperties.CLASSIFIER: {
                    setClassifier(value);
                    return true;
                }
                case NutsConstants.IdProperties.OS: {
                    condition.setOs(PrivateNutsUtilStrings.parseAndTrimToDistinctList(value));
                    return true;
                }
                case NutsConstants.IdProperties.ARCH: {
                    condition.setArch(PrivateNutsUtilStrings.parseAndTrimToDistinctList(value));
                    return true;
                }
                case NutsConstants.IdProperties.PLATFORM: {
                    condition.setPlatform(NutsId.ofList(value).map(x->x.stream().map(Object::toString)).get().collect(Collectors.toList()));
                    return true;
                }
                case NutsConstants.IdProperties.OS_DIST: {
                    condition.setOsDist(PrivateNutsUtilStrings.parseAndTrimToDistinctList(value));
                    return true;
                }
                case NutsConstants.IdProperties.DESKTOP: {
                    condition.setDesktopEnvironment(PrivateNutsUtilStrings.parseAndTrimToDistinctList(value));
                    return true;
                }
            }
        }
        return false;
    });

    public DefaultNutsIdBuilder() {
        this.condition = new DefaultNutsEnvConditionBuilder();
    }

    public DefaultNutsIdBuilder(NutsId id) {
        setAll(id);
    }

    public DefaultNutsIdBuilder(String groupId, String artifactId, NutsVersion version, String classifier, String propertiesQuery, NutsEnvCondition condition) {
        this.groupId = NutsUtilStrings.trimToNull(groupId);
        this.artifactId = NutsUtilStrings.trimToNull(artifactId);
        this.version = version == null ? NutsVersion.BLANK : version;

        setCondition(condition);
        String c0 = NutsUtilStrings.trimToNull(classifier);
        String c1 = null;
        Map<String, String> properties = propertiesQuery == null ? new LinkedHashMap<>() : PrivateNutsQueryStringParser.parseMap(propertiesQuery);
        if (!properties.isEmpty()) {
            c1 = properties.remove(NutsConstants.IdProperties.CLASSIFIER);
        }
        if (c0 == null) {
            if (c1 != null) {
                c0 = NutsUtilStrings.trimToNull(c1);
            }
        }
        this.classifier = c0;
        setProperties(properties);
    }

    @Override
    public NutsIdBuilder setAll(NutsId id) {
        if (id == null) {
            clear();
        } else {
            setCondition(id.getCondition());
            setGroupId(id.getGroupId());
            setArtifactId(id.getArtifactId());
            setVersion(id.getVersion());
            setClassifier(id.getClassifier());
            setCondition(id.getCondition());
            setProperties(id.getPropertiesQuery());
        }
        return this;
    }

    @Override
    public NutsIdBuilder clear() {
        setGroupId(null);
        setArtifactId(null);
        setVersion((NutsVersion) null);
        setProperties("");
        return this;
    }

    @Override
    public NutsIdBuilder setAll(NutsIdBuilder id) {
        if (id == null) {
            clear();
        } else {
            setGroupId(id.getGroupId());
            setArtifactId(id.getArtifactId());
            setVersion(id.getVersion());
            setProperties(id.getPropertiesQuery());
        }
        return this;
    }

    @Override
    public NutsIdBuilder setGroupId(String value) {
        this.groupId = NutsUtilStrings.trimToNull(value);
        return this;
    }

    @Override
    public NutsIdBuilder setRepository(String value) {
        return setProperty(NutsConstants.IdProperties.REPO, NutsUtilStrings.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setVersion(NutsVersion value) {
        this.version = value == null ? NutsVersion.BLANK : value;
        return this;
    }

    @Override
    public NutsIdBuilder setVersion(String value) {
        this.version = NutsVersion.of(value).get();
        return this;
    }

    @Override
    public DefaultNutsIdBuilder setArtifactId(String value) {
        this.artifactId = NutsUtilStrings.trimToNull(value);
        return this;
    }

    @Override
    public String getFace() {
        String s = getProperties().get(NutsConstants.IdProperties.FACE);
        return NutsUtilStrings.trimToNull(s);
    }

//    @Override
//    public String getAlternative() {
//        String s = getProperties().get(NutsConstants.IdProperties.ALTERNATIVE);
//        return NutsUtilStrings.trimToNull(s);
//    }


    @Override
    public String getPackaging() {
        String s = getProperties().get(NutsConstants.IdProperties.PACKAGING);
        return NutsUtilStrings.trimToNull(s);
    }

    @Override
    public NutsIdBuilder setFace(String value) {
        return setProperty(NutsConstants.IdProperties.FACE, NutsUtilStrings.trimToNull(value));
//                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
    }

    @Override
    public NutsIdBuilder setFaceContent() {
        return setFace(NutsConstants.QueryFaces.CONTENT);
    }

    @Override
    public NutsIdBuilder setFaceDescriptor() {
        return setFace(NutsConstants.QueryFaces.DESCRIPTOR);
    }

//    @Override
//    public NutsIdBuilder setAlternative(String value) {
//        return setProperty(NutsConstants.IdProperties.ALTERNATIVE, NutsUtilStrings.trimToNull(value));
////                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
//    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public NutsIdBuilder setClassifier(String value) {
        this.classifier = NutsUtilStrings.trimToNull(value);
        return this;
    }

    @Override
    public NutsIdBuilder setPackaging(String value) {
        return setProperty(NutsConstants.IdProperties.PACKAGING, NutsUtilStrings.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setCondition(NutsEnvCondition c) {
        if (c == null) {
            setProperty(NutsConstants.IdProperties.OS, null);
            setProperty(NutsConstants.IdProperties.OS_DIST, null);
            setProperty(NutsConstants.IdProperties.ARCH, null);
            setProperty(NutsConstants.IdProperties.PLATFORM, null);
            setProperty(NutsConstants.IdProperties.DESKTOP, null);
            setProperty(NutsConstants.IdProperties.PROFILE, null);
            condition.setProperties(null);
        } else {
            setProperty(NutsConstants.IdProperties.OS, PrivateNutsUtilStrings.joinAndTrimToNull(c.getOs()));
            setProperty(NutsConstants.IdProperties.OS_DIST, PrivateNutsUtilStrings.joinAndTrimToNull(c.getOsDist()));
            setProperty(NutsConstants.IdProperties.ARCH, PrivateNutsUtilStrings.joinAndTrimToNull(c.getArch()));
            setProperty(NutsConstants.IdProperties.PLATFORM, PrivateNutsIdListParser.formatStringIdList(c.getPlatform()));
            setProperty(NutsConstants.IdProperties.DESKTOP, PrivateNutsUtilStrings.joinAndTrimToNull(c.getDesktopEnvironment()));
            setProperty(NutsConstants.IdProperties.PROFILE, PrivateNutsUtilStrings.joinAndTrimToNull(c.getProfile()));
            condition.setProperties(c.getProperties());

        }
        return this;
    }

    @Override
    public NutsEnvConditionBuilder getCondition() {
        return condition;
    }

    @Override
    public NutsIdBuilder setCondition(NutsEnvConditionBuilder c) {
        if (c == null) {
            return setCondition((NutsEnvCondition) null);
        } else {
            return setCondition(c.build());
        }
    }


    @Override
    public NutsIdBuilder setProperty(String property, String value) {
        switch (property) {
            case NutsConstants.IdProperties.OS: {
                condition.setOs(PrivateNutsUtilStrings.parseAndTrimToDistinctList(value));
                break;
            }
            case NutsConstants.IdProperties.OS_DIST: {
                condition.setOsDist(PrivateNutsUtilStrings.parseAndTrimToDistinctList(value));
                break;
            }
            case NutsConstants.IdProperties.ARCH: {
                condition.setArch(PrivateNutsUtilStrings.parseAndTrimToDistinctList(value));
                break;
            }
            case NutsConstants.IdProperties.PLATFORM: {
                condition.setPlatform(PrivateNutsIdListParser.parseStringIdList(value));
                break;
            }
            case NutsConstants.IdProperties.DESKTOP: {
                condition.setDesktopEnvironment(PrivateNutsUtilStrings.parseAndTrimToDistinctList(value));
                break;
            }
            case NutsConstants.IdProperties.PROFILE: {
                condition.setProfile(PrivateNutsUtilStrings.parseAndTrimToDistinctList(value));
                break;
            }
            case NutsConstants.IdProperties.PROPERTIES: {
                condition.setProperties(PrivateNutsCommaStringParser.parseMap(value));
                break;
            }
            case NutsConstants.IdProperties.CLASSIFIER: {
                setClassifier(value);
                break;
            }
            default: {
                propertiesQuery.setProperty(property, value);
            }
        }
        return this;
    }


    @Override
    public NutsIdBuilder setProperties(Map<String, String> queryMap) {
        propertiesQuery.clear();
        for (Map.Entry<String, String> e : queryMap.entrySet()) {
            setProperty(e.getKey(), e.getValue());
        }
        return this;
    }

    @Override
    public NutsIdBuilder addProperties(Map<String, String> queryMap) {
        propertiesQuery.addProperties(queryMap);
        return this;
    }

    @Override
    public NutsIdBuilder setProperties(String propertiesQuery) {
        this.propertiesQuery.setProperties(propertiesQuery);
        return this;
    }

    @Override
    public NutsIdBuilder addProperties(String propertiesQuery) {
        this.propertiesQuery.addProperties(propertiesQuery);
        return this;
    }

    @Override
    public String getPropertiesQuery() {
        return PrivateNutsQueryStringParser.formatPropertiesQuery(getProperties());
    }

    @Override
    public Map<String, String> getProperties() {
        return propertiesQuery.getProperties();
    }

    @Override
    public String getRepository() {
        return NutsUtilStrings.trimToNull(getProperties().get(NutsConstants.IdProperties.REPO));
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getFullName() {
        if (NutsBlankable.isBlank(groupId)) {
            return NutsUtilStrings.trim(artifactId);
        }
        return NutsUtilStrings.trim(groupId) + ":" + NutsUtilStrings.trim(artifactId);
    }

    @Override
    public String getShortName() {
        if (NutsBlankable.isBlank(groupId)) {
            return NutsUtilStrings.trim(artifactId);
        }
        return NutsUtilStrings.trim(groupId) + ":" + NutsUtilStrings.trim(artifactId);
    }

    @Override
    public String getLongName() {
        StringBuilder sb = new StringBuilder();
        if (!NutsBlankable.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NutsUtilStrings.trim(artifactId));
        NutsVersion v = getVersion();
        if (!v.isBlank()) {
            sb.append("#");
            sb.append(v);
        }
        if (!NutsBlankable.isBlank(classifier)) {
            sb.append("?");
            sb.append("classifier=");
            sb.append(classifier);
        }
        return sb.toString();
    }


    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public NutsVersion getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!NutsBlankable.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NutsUtilStrings.trim(artifactId));
        NutsVersion v = getVersion();
        if (!v.isBlank()) {
            sb.append("#");
            sb.append(v);
        }
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        if (!NutsBlankable.isBlank(classifier)) {
            m.put(NutsConstants.IdProperties.CLASSIFIER, classifier);
        }
        m.putAll(condition.build().toMap());
        for (Map.Entry<String, String> e : propertiesQuery.getProperties().entrySet()) {
            if (!m.containsKey(e.getKey())) {
                m.put(e.getKey(), e.getValue());
            }
        }
        if (!m.isEmpty()) {
            sb.append("?").append(PrivateNutsQueryStringParser.formatPropertiesQuery(m));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultNutsIdBuilder nutsId = (DefaultNutsIdBuilder) o;

        if (!Objects.equals(groupId, nutsId.groupId)) {
            return false;
        }
        if (!Objects.equals(artifactId, nutsId.artifactId)) {
            return false;
        }
        if (!Objects.equals(version, nutsId.version)) {
            return false;
        }
        if (!Objects.equals(classifier, nutsId.classifier)) {
            return false;
        }
        return Objects.equals(propertiesQuery, nutsId.propertiesQuery);

    }

    @Override
    public int hashCode() {
        int result = (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
        result = 31 * result + (propertiesQuery != null ? propertiesQuery.hashCode() : 0);
        return result;
    }


    @Override
    public NutsId build() {
        return new DefaultNutsId(
                groupId, artifactId, version, classifier, propertiesQuery.getPropertiesQuery(), condition.readOnly()
        );
    }

    @Override
    public boolean equalsShortId(NutsId other) {
        return build().equalsShortId(other);
    }

    @Override
    public boolean equalsLongId(NutsId other) {
        return build().equalsLongId(other);
    }

    @Override
    public boolean isLongId() {
        return build().isLongId();
    }

    @Override
    public boolean isShortId() {
        return build().isShortId();
    }

    @Override
    public NutsId getShortId() {
        return build().getShortId();
    }

    @Override
    public NutsId getLongId() {
        return build().getLongId();
    }

    @Override
    public NutsIdBuilder builder() {
        return new DefaultNutsIdBuilder(this);
    }

    @Override
    public NutsDependency toDependency() {
        return build().toDependency();
    }

    @Override
    public NutsIdFilter filter(NutsSession session) {
        return build().filter(session);
    }

    @Override
    public NutsId compatNewer() {
        return build().compatNewer();
    }

    @Override
    public NutsId compatOlder() {
        return build().compatOlder();
    }

    @Override
    public boolean isNull() {
        return build().isNull();
    }

    @Override
    public boolean isBlank() {
        return build().isBlank();
    }

    @Override
    public int compareTo(NutsId o) {
        return build().compareTo(o);
    }

    @Override
    public NutsFormat formatter(NutsSession session) {
        return build().formatter(session);
    }
}
