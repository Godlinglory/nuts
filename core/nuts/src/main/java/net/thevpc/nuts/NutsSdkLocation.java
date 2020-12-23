/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
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

import java.util.Objects;

/**
 * SDK location
 * @author thevpc
 * @since 0.5.4
 * @category Config
 */
public class NutsSdkLocation extends NutsConfigItem {

    public static final long serialVersionUID = 2;
    private final NutsId id;
    private final String name;
    private final String packaging;
    private final String product;
    private final String path;
    private final String version;
    private final int priority;

    /**
     * default constructor
     * @param id id
     * @param product sdk product. In java this is Oracle JDK or OpenJDK.
     * @param name sdk name
     * @param path sdk path
     * @param version sdk version
     * @param packaging sdk packaging. for Java SDK this is room to set JRE or JDK.
     * @param priority sdk priority
     */
    public NutsSdkLocation(NutsId id, String product, String name, String path, String version, String packaging,int priority) {
        this.id = id;
        this.product = product;
        this.name = name;
        this.path = path;
        this.version = version;
        this.packaging = packaging;
        this.priority = priority;
    }


    public NutsSdkLocation setPriority(int priority) {
        return new NutsSdkLocation(id, product, name, path, version, packaging,priority);
    }

    public int getPriority() {
        return priority;
    }

    public NutsId getId() {
        return id;
    }

    /**
     * sdk product. In java this is
     * Oracle JDK or OpenJDK.
     *
     * @return product name
     */
    public String getProduct() {
        return product;
    }

    /**
     * sdk version
     * @return sdk version
     */
    public String getVersion() {
        return version;
    }

    /**
     * sdk name
     * @return sdk name
     */
    public String getName() {
        return name;
    }

    /**
     * sdk path
     * @return sdk path
     */
    public String getPath() {
        return path;
    }

    /**
     * sdk packaging. for Java SDK this
     * is room to set JRE or JDK.
     *
     * @return packaging name
     */
    public String getPackaging() {
        return packaging;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsSdkLocation that = (NutsSdkLocation) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(packaging, that.packaging) &&
                Objects.equals(product, that.product) &&
                Objects.equals(path, that.path) &&
                Objects.equals(version, that.version) &&
                Objects.equals(priority, that.priority)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, packaging, product, path, version, priority);
    }

    @Override
    public String toString() {
        return "NutsSdkLocation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", packaging='" + packaging + '\'' +
                ", product='" + product + '\'' +
                ", path='" + path + '\'' +
                ", version='" + version + '\'' +
                ", priority=" + priority +
                '}';
    }
}