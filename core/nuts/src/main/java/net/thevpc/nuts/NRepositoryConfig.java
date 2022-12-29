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

import net.thevpc.nuts.spi.NRepositoryLocation;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public class NRepositoryConfig extends NConfigItem {

    private static final long serialVersionUID = 1;
    private String uuid;
    private String name;
    private NRepositoryLocation location;
    private Map<NStoreLocation, String> storeLocations = null;
    private NStoreLocationStrategy storeLocationStrategy = null;
    private String groups;
    private Map<String, String> env;
    private List<NRepositoryRef> mirrors;
    private List<NUserConfig> users;
    private boolean indexEnabled;
    private String authenticationAgent;

    public NRepositoryConfig() {
    }

    public String getName() {
        return name;
    }

    public NRepositoryConfig setName(String name) {
        this.name = name;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NRepositoryConfig setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

//    public String getType() {
//        return type;
//    }
//
//    public NutsRepositoryConfig setType(String type) {
//        this.type = type;
//        return this;
//    }

    public NRepositoryLocation getLocation() {
        return location;
    }

    public NRepositoryConfig setLocation(NRepositoryLocation location) {
        this.location = location;
        return this;
    }

    public NStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NRepositoryConfig setStoreLocationStrategy(NStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public String getGroups() {
        return groups;
    }

    public NRepositoryConfig setGroups(String groups) {
        this.groups = groups;
        return this;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public NRepositoryConfig setEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    public List<NRepositoryRef> getMirrors() {
        return mirrors;
    }

    public NRepositoryConfig setMirrors(List<NRepositoryRef> mirrors) {
        this.mirrors = mirrors;
        return this;
    }

    public List<NUserConfig> getUsers() {
        return users;
    }

    public NRepositoryConfig setUsers(List<NUserConfig> users) {
        this.users = users;
        return this;
    }

    public boolean isIndexEnabled() {
        return indexEnabled;
    }

    public NRepositoryConfig setIndexEnabled(boolean indexEnabled) {
        this.indexEnabled = indexEnabled;
        return this;
    }

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public NRepositoryConfig setAuthenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }

    public Map<NStoreLocation, String> getStoreLocations() {
        return storeLocations;
    }

    public NRepositoryConfig setStoreLocations(Map<NStoreLocation, String> storeLocations) {
        this.storeLocations = storeLocations;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.uuid);
        hash = 53 * hash + Objects.hashCode(this.name);
//        hash = 53 * hash + Objects.hashCode(this.type);
        hash = 53 * hash + Objects.hashCode(this.location);
        hash = 53 * hash + Objects.hashCode(this.storeLocations);
        hash = 53 * hash + Objects.hashCode(this.storeLocationStrategy);
        hash = 53 * hash + Objects.hashCode(this.groups);
        hash = 53 * hash + Objects.hashCode(this.env);
        hash = 53 * hash + Objects.hashCode(this.mirrors);
        hash = 53 * hash + Objects.hashCode(this.users);
        hash = 53 * hash + (this.indexEnabled ? 1 : 0);
        hash = 53 * hash + Objects.hashCode(this.authenticationAgent);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NRepositoryConfig other = (NRepositoryConfig) obj;
        if (this.indexEnabled != other.indexEnabled) {
            return false;
        }
        if (!Objects.equals(this.uuid, other.uuid)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
//        if (!Objects.equals(this.type, other.type)) {
//            return false;
//        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        if (!Objects.equals(this.groups, other.groups)) {
            return false;
        }
        if (!Objects.equals(this.authenticationAgent, other.authenticationAgent)) {
            return false;
        }
        if (!Objects.equals(this.storeLocations, other.storeLocations)) {
            return false;
        }
        if (this.storeLocationStrategy != other.storeLocationStrategy) {
            return false;
        }
        if (!Objects.equals(this.env, other.env)) {
            return false;
        }
        if (!Objects.equals(this.mirrors, other.mirrors)) {
            return false;
        }
        return Objects.equals(this.users, other.users);
    }

    @Override
    public String toString() {
        return "NutsRepositoryConfig{" + ", uuid=" + uuid + ", name=" + name
//                + ", type=" + type
                + ", location=" + location + ", storeLocations=" + (storeLocations == null ? "null" : storeLocations.toString()) + ", storeLocationStrategy=" + storeLocationStrategy + ", groups=" + groups + ", env=" + env + ", mirrors=" + mirrors + ", users=" + users + ", indexEnabled=" + indexEnabled + ", authenticationAgent=" + authenticationAgent + '}';
    }

}
