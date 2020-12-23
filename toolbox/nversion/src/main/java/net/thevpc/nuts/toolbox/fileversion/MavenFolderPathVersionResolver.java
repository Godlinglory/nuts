/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
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
package net.thevpc.nuts.toolbox.fileversion;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.common.mvn.Pom;
import net.thevpc.common.mvn.PomXmlParser;

/**
 *
 * @author thevpc
 */
public class MavenFolderPathVersionResolver implements PathVersionResolver {

    public Set<VersionDescriptor> resolve(String filePath, NutsApplicationContext context) {
        if (Files.isRegularFile(Paths.get(filePath).resolve("pom.xml"))) {
            Properties properties = new Properties();
            Set<VersionDescriptor> all = new HashSet<>();
            try (InputStream inputStream = Files.newInputStream(Paths.get(filePath).resolve("pom.xml"))) {
                Pom d = new PomXmlParser().parse(inputStream);
                properties.put("groupId", d.getGroupId());
                properties.put("artifactId", d.getArtifactId());
                properties.put("version", d.getVersion());
                properties.put("name", d.getName());
                properties.setProperty("nuts.version-provider", "maven");
                if (d.getProperties() != null) {
                    for (Map.Entry<String, String> e : d.getProperties().entrySet()) {
                        properties.put("property." + e.getKey(), e.getValue());
                    }
                }
                all.add(new VersionDescriptor(
                        context.getWorkspace().id().builder().setGroupId(d.getPomId().getGroupId())
                                .setNamespace(d.getPomId().getArtifactId())
                                .setVersion(d.getPomId().getVersion())
                                .build(),
                        properties));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            return all;
        } else {
            return null;
        }
    }
}