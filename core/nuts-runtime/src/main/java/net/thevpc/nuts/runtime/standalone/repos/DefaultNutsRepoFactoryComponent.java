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
package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.RepoDefinitionResolver;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.spi.NutsRepositoryFactoryComponent;

import java.io.InputStream;
import java.util.Map;
import net.thevpc.nuts.runtime.standalone.NutsRepositorySelector;

/**
 * Created by vpc on 1/15/17.
 */
@NutsSingleton
public class DefaultNutsRepoFactoryComponent implements NutsRepositoryFactoryComponent {

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsRepositoryConfig> criteria) {
        if (criteria == null) {
            return NO_SUPPORT;
        }
        String repositoryType = criteria.getConstraints().getType();
        if(CoreStringUtils.isBlank(repositoryType)){
                String location = criteria.getConstraints().getLocation();
                if (!CoreStringUtils.isBlank(location)) {
                    String prot = CoreNutsUtils.extractUrlProtocol(location);
                    if(prot!=null){
                        switch (prot){
                            case "nuts":
                            case "nuts+api":{
                                criteria.getConstraints().setType(prot);
                                return DEFAULT_SUPPORT;
                            }
                            case "nuts+http":
                            case "nuts+api+http":
                            case "nuts+https":
                            case "nuts+api+https":{
                                criteria.getConstraints().setType(prot);
                                return DEFAULT_SUPPORT;
                            }
                        }
                    }
                    NutsInput in = criteria.getWorkspace().io().input().setTypeName("nuts-repository.json").of(
                            location + "/nuts-repository.json"
                    );
                    try (InputStream s = in.open()) {
                        Map<String,Object> m=criteria.getWorkspace().formats().element().setSession(criteria.getSession()).setContentType(NutsContentType.JSON)
                                .parse(s,Map.class);
                        if(m!=null){
                            String type = (String) m.get("type");
                            if(type!=null){
                                switch (type){
                                    case "nuts":
                                    case "nuts:api":{
                                        criteria.getConstraints().setType(type);
                                        return DEFAULT_SUPPORT;
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            return NO_SUPPORT;
        }
        String location = criteria.getConstraints().getLocation();
        if (!NutsConstants.RepoTypes.NUTS.equals(repositoryType)
                && !"nuts:api".equals(repositoryType)) {
            return NO_SUPPORT;
        }
        if (CoreStringUtils.isBlank(location)) {
            return DEFAULT_SUPPORT;
        }
        if (!location.contains("://")) {
            return DEFAULT_SUPPORT;
        }
        if (CoreIOUtils.isPathHttp(location)) {
            return DEFAULT_SUPPORT;
        }
        return NO_SUPPORT;
    }

    @Override
    public NutsAddRepositoryOptions[] getDefaultRepositories(NutsSession session) {
        if (!session.getWorkspace().config().isGlobal()) {
            return new NutsAddRepositoryOptions[]{
                    RepoDefinitionResolver.createRepositoryOptions(NutsRepositorySelector.parseOne("system"),true,session)
            };
        }
        return new NutsAddRepositoryOptions[0];
    }

    @Override
    public NutsRepository create(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository) {
        NutsRepositoryConfig config = options.getConfig();
        if (CoreStringUtils.isBlank(config.getType())) {
            if (CoreStringUtils.isBlank(config.getLocation())) {
                config.setType(NutsConstants.RepoTypes.NUTS);
            }
        }
        if (NutsConstants.RepoTypes.NUTS.equals(config.getType())) {
            if (CoreStringUtils.isBlank(config.getLocation()) || CoreIOUtils.isPathFile(config.getLocation())) {
                return new NutsFolderRepository(options, session, parentRepository);
            }
            if (CoreIOUtils.isPathURL(config.getLocation())) {
                return (new NutsHttpFolderRepository(options, session, parentRepository));
            }
        }
        if ("nuts:api".equals(config.getType()) && CoreIOUtils.isPathHttp(config.getLocation())) {
            return (new NutsHttpSrvRepository(options, session, parentRepository));
        }
        return null;
    }
}
