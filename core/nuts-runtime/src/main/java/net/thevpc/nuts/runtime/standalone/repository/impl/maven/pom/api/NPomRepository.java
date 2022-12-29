package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.Objects;

/**
 * <pre>
 * &lt;repository&gt;
 * &lt;url&gt;http://repository.primefaces.org/&lt;/url&gt;
 * &lt;id&gt;PrimeFaces-maven-lib&lt;/id&gt;
 * &lt;layout&gt;default&lt;/layout&gt;
 * &lt;name&gt;Repository for library PrimeFaces-maven-lib&lt;/name&gt;
 * &lt;/repository&gt;
 * &lt;repository&gt;
 * &lt;id&gt;vpc-public-maven&lt;/id&gt;
 * &lt;url&gt;https://raw.github.com/thevpc/vpc-public-maven/master&lt;/url&gt;
 * &lt;snapshots&gt;
 * &lt;enabled&gt;true&lt;/enabled&gt;
 * &lt;updatePolicy&gt;always&lt;/updatePolicy&gt;
 * &lt;/snapshots&gt;
 * &lt;/repository&gt;
 * </pre>
 */
public class NPomRepository {

    private String id;
    private String layout;
    private String url;
    private String name;
    private NPomRepositoryPolicy releases;
    private NPomRepositoryPolicy snapshots;

    public NPomRepository() {
    }

    public NPomRepository(String id, String layout, String url, String name, NPomRepositoryPolicy releases, NPomRepositoryPolicy snapshots) {
        this.id = id;
        this.layout = layout;
        this.url = url;
        this.name = name;
        this.releases = releases;
        this.snapshots = snapshots;
    }

    public String getId() {
        return id;
    }

    public NPomRepository setId(String id) {
        this.id = id;
        return this;
    }

    public String getLayout() {
        return layout;
    }

    public NPomRepository setLayout(String layout) {
        this.layout = layout;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public NPomRepository setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getName() {
        return name;
    }

    public NPomRepository setName(String name) {
        this.name = name;
        return this;
    }

    public NPomRepositoryPolicy getReleases() {
        return releases;
    }

    public NPomRepository setReleases(NPomRepositoryPolicy releases) {
        this.releases = releases;
        return this;
    }

    public NPomRepositoryPolicy getSnapshots() {
        return snapshots;
    }

    public NPomRepository setSnapshots(NPomRepositoryPolicy snapshots) {
        this.snapshots = snapshots;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NPomRepository that = (NPomRepository) o;
        return Objects.equals(id, that.id)
                && Objects.equals(layout, that.layout)
                && Objects.equals(url, that.url)
                && Objects.equals(name, that.name)
                && Objects.equals(releases, that.releases)
                && Objects.equals(snapshots, that.snapshots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, layout, url, name, releases, snapshots);
    }
}
