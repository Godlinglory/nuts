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
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.core.model;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsDependencyScopes;
import net.thevpc.nuts.runtime.bundles.parsers.QueryStringParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import net.thevpc.nuts.runtime.core.util.CoreNutsDependencyUtils;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsDependencyBuilder implements NutsDependencyBuilder {

    private String repo;
    private String groupId;
    private String artifactId;
    private NutsVersion version;
    private String scope;
    private String optional;
    private String type;
    private NutsEnvConditionBuilder condition;
    private String classifier;
    private NutsId[] exclusions = new NutsId[0];
    private transient NutsSession session;
    private transient QueryStringParser propertiesQuery = new QueryStringParser(true, (name, value) -> {
        if (name != null) {
            switch (name) {
                case NutsConstants.IdProperties.SCOPE: {
                    setScope(value);
                    return true;
                }
                case NutsConstants.IdProperties.VERSION: {
                    setVersion(value);
                    return true;
                }
                case NutsConstants.IdProperties.OPTIONAL: {
                    setOptional(value);
                    return true;
                }
                case NutsConstants.IdProperties.CLASSIFIER: {
                    setClassifier(value);
                    return true;
                }
                case NutsConstants.IdProperties.REPO: {
                    setRepository(value);
                    return true;
                }
                case NutsConstants.IdProperties.EXCLUSIONS: {
                    setExclusions(value);
                    return true;
                }
                case NutsConstants.IdProperties.OS: {
                    condition.setOs(new String[]{value});
                    return true;
                }
                case NutsConstants.IdProperties.ARCH: {
                    condition.setArch(new String[]{value});
                    return true;
                }
                case NutsConstants.IdProperties.PLATFORM: {
                    condition.setPlatform(new String[]{value});
                    return true;
                }
                case NutsConstants.IdProperties.OS_DIST: {
                    condition.setOsDist(new String[]{value});
                    return true;
                }
                case NutsConstants.IdProperties.DESKTOP_ENVIRONMENT: {
                    condition.setDesktopEnvironment(new String[]{value});
                    return true;
                }
                case NutsConstants.IdProperties.TYPE: {
                    setType(value);
                    return true;
                }
            }
        }
        return false;
    });

    public DefaultNutsDependencyBuilder() {
        //for serialization
    }

    public DefaultNutsDependencyBuilder(NutsSession session) {
        this.session = session;
        condition=new DefaultNutsEnvConditionBuilder(session);
    }

    @Override
    public NutsDependencyBuilder setCondition(NutsEnvCondition condition) {
        this.condition.setAll(condition);
        return this;
    }

    @Override
    public NutsDependencyBuilder setCondition(NutsEnvConditionBuilder condition) {
        this.condition.setAll(condition);
        return this;
    }

    @Override
    public NutsDependencyBuilder setRepository(String repository) {
        this.repo = NutsUtilStrings.trimToNull(repository);
        return this;
    }

    @Override
    public NutsDependencyBuilder setGroupId(String groupId) {
        this.groupId = NutsUtilStrings.trimToNull(groupId);
        return this;
    }

    @Override
    public NutsDependencyBuilder setArtifactId(String artifactId) {
        this.artifactId = NutsUtilStrings.trimToNull(artifactId);
        return this;
    }

    @Override
    public NutsDependencyBuilder setVersion(NutsVersion version) {
        this.version = version == null ? session.version().parser().parse("") : version;
        return this;
    }

    @Override
    public NutsDependencyBuilder setVersion(String classifier) {
        this.version = session.version().parser().parse(classifier);
        return this;
    }

    @Override
    public NutsDependencyBuilder setId(NutsId id) {
        if (id == null) {
            setRepository(null);
            setGroupId(null);
            setArtifactId(null);
            setVersion((String) null);
        } else {
            setRepository(id.getRepository());
            setGroupId(id.getGroupId());
            setArtifactId(id.getArtifactId());
            setVersion(id.getVersion());
            addProperties(id.getProperties());
        }
        return this;
    }

    @Override
    public NutsDependencyBuilder setScope(NutsDependencyScope scope) {
        this.scope = scope == null ? "" : scope.toString();
        return this;
    }

    @Override
    public NutsDependencyBuilder setScope(String scope) {
        this.scope = NutsDependencyScope.parseLenient(scope,NutsDependencyScope.API,NutsDependencyScope.OTHER).id();
        return this;
    }

    @Override
    public NutsDependencyBuilder setType(String type) {
        this.type = CoreNutsDependencyUtils.normalizeDependencyType(type);
        return this;
    }

    public String getType() {
        return type;
    }

    @Override
    public NutsDependencyBuilder setOptional(String optional) {
        String o = NutsUtilStrings.trimToNull(optional);
        if ("false".equals(o)) {
            o = null;
        } else if ("true".equalsIgnoreCase(o)) {
            o = "true";//remove case and formatting
        }
        this.optional = o;
        return this;
    }

    @Override
    public NutsDependencyBuilder setClassifier(String classifier) {
        this.classifier = NutsUtilStrings.trimToNull(classifier);
        return this;
    }

    @Override
    public NutsDependencyBuilder setExclusions(NutsId[] exclusions) {
        if (exclusions != null) {
            exclusions = Arrays.copyOf(exclusions, exclusions.length);
        }
        this.exclusions = exclusions;
        return this;
    }

    @Override
    public NutsDependencyBuilder setDependency(NutsDependencyBuilder value) {
        return set(value);
    }

    @Override
    public NutsDependencyBuilder set(NutsDependencyBuilder value) {
        if (value != null) {
            setRepository(value.getRepository());
            setGroupId(value.getGroupId());
            setArtifactId(value.getArtifactId());
            setVersion(value.getVersion());
            setScope(value.getScope());
            setOptional(value.getOptional());
            setExclusions(value.getExclusions());
            setClassifier(value.getClassifier());
            getCondition().setAll(value.getCondition());
            setType(value.getType());
            setProperties(value.getProperties());
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NutsDependencyBuilder set(NutsDependency value) {
        if (value != null) {
            setRepository(value.getRepository());
            setGroupId(value.getGroupId());
            setArtifactId(value.getArtifactId());
            setVersion(value.getVersion());
            setScope(value.getScope());
            setOptional(value.getOptional());
            setExclusions(value.getExclusions());
            setClassifier(value.getClassifier());
            getCondition().setAll(value.getCondition());
            setType(value.getType());
            setProperties(value.getProperties());
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NutsDependencyBuilder setDependency(NutsDependency value) {
        return set(value);
    }

    @Override
    public NutsDependencyBuilder clear() {
        setRepository(null);
        setGroupId(null);
        setArtifactId(null);
        setVersion((NutsVersion) null);
        setScope((String) null);
        setOptional(null);
        setExclusions((NutsId[]) null);
        setClassifier(null);
        getCondition().clear();
        setType(null);
        setProperties((Map<String, String>) null);
        return this;
    }

    @Override
    public boolean isOptional() {
        return optional != null && Boolean.parseBoolean(optional);
    }

    @Override
    public String getOptional() {
        return optional;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public NutsId toId() {
        Map<String, String> m = new LinkedHashMap<>();
        if (!NutsDependencyScopes.isDefaultScope(scope)) {
            m.put(NutsConstants.IdProperties.SCOPE, scope);
        }
        if (!NutsBlankable.isBlank(optional) && !"false".equals(optional)) {
            m.put(NutsConstants.IdProperties.OPTIONAL, optional);
        }
        if (!NutsBlankable.isBlank(classifier)) {
            m.put(NutsConstants.IdProperties.CLASSIFIER, classifier);
        }
        if (!NutsBlankable.isBlank(type)) {
            m.put(NutsConstants.IdProperties.TYPE, type);
        }
        if (exclusions.length > 0) {
            TreeSet<String> ex = new TreeSet<>();
            for (NutsId exclusion : exclusions) {
                ex.add(exclusion.getShortName());
            }
            m.put(NutsConstants.IdProperties.EXCLUSIONS, String.join(",", ex));
        }
        return session.id().builder()
                .setRepository(getRepository())
                .setGroupId(getGroupId())
                .setArtifactId(getArtifactId())
                .setVersion(getVersion())
                .setCondition(getCondition())
                .setProperties(m).build()
                ;
    }

    @Override
    public String getRepository() {
        return repo;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public String getFullName() {
        if (NutsBlankable.isBlank(groupId)) {
            return NutsUtilStrings.trim(artifactId);
        }
        return NutsUtilStrings.trim(groupId) + ":" + NutsUtilStrings.trim(artifactId);
    }

    @Override
    public NutsVersion getVersion() {
        return version;
    }

    @Override
    public NutsId[] getExclusions() {
        return exclusions == null ? new NutsId[0] : Arrays.copyOf(exclusions, exclusions.length);
    }

    @Override
    public NutsDependency build() {
        return new DefaultNutsDependency(
                getRepository(), getGroupId(), getArtifactId(), getClassifier(),
                getVersion(),
                getScope(),
                getOptional(),
                getExclusions(),
                getCondition().build(),
                getType(),
                getPropertiesQuery(), session
        );
    }

    @Override
    public NutsDependencyBuilder setProperty(String property, String value) {
        this.propertiesQuery.setProperty(property, value);
        return this;
    }

    @Override
    public NutsDependencyBuilder setProperties(Map<String, String> queryMap) {
        this.propertiesQuery.setProperties(queryMap);
        return this;
    }

    @Override
    public NutsDependencyBuilder addProperties(Map<String, String> queryMap) {
        this.propertiesQuery.addProperties(queryMap);
        return this;
    }

    @Override
    public NutsDependencyBuilder setProperties(String propertiesQuery) {
        this.propertiesQuery.setProperties(propertiesQuery);
        return this;
    }

    @Override
    public NutsDependencyBuilder addProperties(String propertiesQuery) {
        this.propertiesQuery.addProperties(propertiesQuery);
        return this;
    }

    @Override
    public String getPropertiesQuery() {
        return propertiesQuery.getPropertiesQuery();
    }

    @Override
    public Map<String, String> getProperties() {
        return propertiesQuery.getProperties();
    }

    //@Override
    public NutsDependencyBuilder setExclusions(String exclusions) {
        if (exclusions == null) {
            exclusions = "";
        }
        List<NutsId> ids = new ArrayList<>();
        NutsIdParser parser = session.id().parser();
        for (String s : exclusions.split("[;,]")) {
            NutsId ii = parser.parse(s.trim());
            if (ii != null) {
                ids.add(ii);
            }
        }
        setExclusions(ids.toArray(new NutsId[0]));
        return this;
    }

    @Override
    public String toString() {
        return build().toString();
    }

    public NutsEnvConditionBuilder getCondition() {
        return condition;
    }
}
