package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.Map;

import org.w3c.dom.Document;

public class NPom {

    private String groupId;
    private String artifactId;
    private String version;
    private String packaging;
    private String name;
    private String url;
    private String inceptionYear;
    private String description;
    private Map<String, String> properties;
    private NPomDependency[] dependencies;
    private NPomDependency[] dependenciesManagement;
    private NPomId parent;
    private NPomRepository[] repositories;
    private NPomRepository[] pluginRepositories;
    private NPomProfile[] profiles;
    private String[] modules;
    private NPomContributor[] contributors;
    private NPomContributor[] developers;
    private NPomLicense[] licenses;

    private Document xml;

    public NPom(String groupId, String artifactId, String version, String packaging,
                NPomId parent,
                String name, String desc,
                String url, String inceptionYear,
                Map<String, String> properties,
                NPomDependency[] dependencies,
                NPomDependency[] dependenciesManagement,
                NPomRepository[] repositories, NPomRepository[] pluginRepositories,
                String[] modules, NPomProfile[] profiles,
                NPomContributor[] contributors,
                NPomContributor[] developers,
                NPomLicense[] licenses,
                Document xml
    ) {
        this.groupId = groupId;
        this.contributors = contributors;
        this.developers = developers;
        this.licenses = licenses;
        this.artifactId = artifactId;
        this.version = version;
        this.properties = properties;
        this.dependencies = dependencies;
        this.dependenciesManagement = dependenciesManagement;
        this.name = name;
        this.description = desc;
        this.url = url;
        this.inceptionYear = inceptionYear;
        this.packaging = packaging;
        this.parent = parent;
        this.repositories = repositories;
        this.pluginRepositories = pluginRepositories;
        this.modules = modules;
        this.profiles = profiles;
        this.xml = xml;
    }

    public NPomProfile[] getProfiles() {
        return profiles;
    }

    public Document getXml() {
        return xml;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getPackaging() {
        return packaging;
    }

    public NPom setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public NPom setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public NPom setVersion(String version) {
        this.version = version;
        return this;
    }

    public NPom setPackaging(String packaging) {
        this.packaging = packaging;
        return this;
    }

    public NPom setName(String name) {
        this.name = name;
        return this;
    }

    public NPom setDescription(String description) {
        this.description = description;
        return this;
    }

    public NPom setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public NPom setDependencies(NPomDependency[] dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public NPom setParent(NPomId parent) {
        this.parent = parent;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public NPomDependency[] getDependencies() {
        return dependencies;
    }

    public NPomId getParent() {
        return parent;
    }

    public NPomRepository[] getRepositories() {
        return repositories;
    }

    public NPom setRepositories(NPomRepository[] repositories) {
        this.repositories = repositories;
        return this;
    }

    public NPomRepository[] getPluginRepositories() {
        return pluginRepositories;
    }

    public NPom setPluginRepositories(NPomRepository[] pluginRepositories) {
        this.pluginRepositories = pluginRepositories;
        return this;
    }

    public NPomId getPomId() {
        String g = groupId;
        String v = version;
        if (parent != null) {
            if (g == null || g.isEmpty() || "${groupId}".equals(g)) {
                g = parent.getGroupId();
            }
            if (v == null || v.isEmpty() || "${version}".equals(v)) {
                v = parent.getVersion();
            }
        }
        return new NPomId(g, artifactId, v);
    }

    public NPomDependency[] getDependenciesManagement() {
        return dependenciesManagement;
    }

    public NPom setDependenciesManagement(NPomDependency[] dependenciesManagement) {
        this.dependenciesManagement = dependenciesManagement;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public NPom setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getInceptionYear() {
        return inceptionYear;
    }

    public NPom setInceptionYear(String inceptionYear) {
        this.inceptionYear = inceptionYear;
        return this;
    }

    public String[] getModules() {
        return modules;
    }

    public NPom setModules(String[] modules) {
        this.modules = modules;
        return this;
    }

    public NPomContributor[] getContributors() {
        return contributors;
    }

    public NPom setContributors(NPomContributor[] contributors) {
        this.contributors = contributors;
        return this;
    }

    public NPom setProfiles(NPomProfile[] profiles) {
        this.profiles = profiles;
        return this;
    }

    public NPomContributor[] getDevelopers() {
        return developers;
    }

    public NPom setDevelopers(NPomContributor[] developers) {
        this.developers = developers;
        return this;
    }

    public NPomLicense[] getLicenses() {
        return licenses;
    }

    public NPom setLicenses(NPomLicense[] licenses) {
        this.licenses = licenses;
        return this;
    }

    public NPom setXml(Document xml) {
        this.xml = xml;
        return this;
    }
}
