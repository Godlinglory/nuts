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
 *
 * <br>
 * <p>
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
package net.thevpc.nuts;

import java.net.URL;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @category Base
 */
public interface NutsWorkspaceEnvManager {

    Map<String, String> getEnvMap();

    String getEnv(String property, String defaultValue);

    String getEnv(String property);

    Integer getEnvAsInt(String property, Integer defaultValue);

    Boolean getEnvAsBoolean(String property, Boolean defaultValue);

    /**
     *
     * @param property property
     * @param value value
//     * @param options options
     * @return {@code this} instance
     */
    NutsWorkspaceEnvManager setEnv(String property, String value);

    /**
     * @since 0.8.1
     * @return properties
     */
    Map<String, Object> getProperties();

    /**
     * @since 0.8.1
     * @param property property
     * @param defaultValue defaultValue
     * @return property value
     */
    Integer getPropertyAsInt(String property, Integer defaultValue);

    /**
     * @since 0.8.1
     * @param property property
     * @param defaultValue defaultValue
     * @return property value
     */
    String getPropertyAsString(String property, String defaultValue);

    /**
     * @since 0.8.1
     * @param property property
     * @param defaultValue defaultValue
     * @return property value
     */
    Boolean getPropertyAsBoolean(String property, Boolean defaultValue);

    /**
     * @since 0.8.1
     * @param property property name
     * @return property value
     */
    Object getProperty(String property);

    <T> T getOrCreateProperty(String property, Supplier<T> supplier);

    <T> T getOrCreateProperty(Class<T> propertyType, Supplier<T> supplier);

    /**
     * @since 0.8.1
     * @param property property name
     * @param defaultValue default value
     * @return property value
     */
    Object getProperty(String property, Object defaultValue);

    /**
     * @since 0.8.1
     * @param property property
     * @param value value
     * @return {@code this} instance
     */
    NutsWorkspaceEnvManager setProperty(String property, Object value);

    NutsWorkspaceOptions getBootOptions();

    NutsOsFamily getOsFamily();

    NutsId getPlatform();

    NutsId getOs();

    NutsId getOsDist();

    NutsId getArch();

    NutsArchFamily getArchFamily();

    String getOption(String property);

    String getOption(String property, String defaultValue);

    Boolean getOptionAsBoolean(String property, Boolean defaultValue);

    Integer getOptionAsInt(String property, Integer defaultValue);

    NutsSession getSession();

    NutsWorkspaceEnvManager setSession(NutsSession session);

    public ClassLoader getBootClassLoader() ;
    public URL[] getBootClassWorldURLs() ;
    public String getBootRepositories() ;
    public long getCreationStartTimeMillis() ;
    public long getCreationFinishTimeMillis() ;
    public long getCreationTimeMillis() ;
}
