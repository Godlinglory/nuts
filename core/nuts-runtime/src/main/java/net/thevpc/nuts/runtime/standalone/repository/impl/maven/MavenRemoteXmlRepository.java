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
package net.thevpc.nuts.runtime.standalone.repository.impl.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenMetadata;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.util.NIterator;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenRemoteXmlRepository extends MavenFolderRepository {

    public MavenRemoteXmlRepository(NAddRepositoryOptions options, NSession session, NRepository parentRepository) {
        super(options, session, parentRepository);
    }

    @Override
    public NIterator<NId> findNonSingleVersionImpl(NId id, NIdFilter idFilter, NFetchMode fetchMode, NSession session) {
        if (!acceptedFetchNoCache(fetchMode)) {
            return IteratorBuilder.emptyIterator();
        }
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        NPath metadataURL = config().setSession(session).getLocationPath().resolve(groupId.replace('.', '/') + "/" + artifactId + "/maven-metadata.xml");

        return IteratorBuilder.ofSupplier(
                () -> {
                    List<NId> ret = new ArrayList<>();
                    InputStream metadataStream = null;
                    session.getTerminal().printProgress(NMsg.ofC("looking for versions of %s at %s", id,metadataURL.toCompressedForm()));
                    try {
                        try {
                            metadataStream = openStream(id, metadataURL, id.builder().setFace(CoreNConstants.QueryFaces.CATALOG).build(), "artifact catalog", "retrieve", session);
                        } catch (UncheckedIOException | NIOException ex) {
                            return IteratorBuilder.emptyIterator();
                        }
                        MavenMetadata info = MavenUtils.of(session).parseMavenMetaData(metadataStream, session);
                        if (info != null) {
                            for (String version : info.getVersions()) {
                                final NId nutsId = id.builder().setVersion(version).build();

                                if (idFilter != null && !idFilter.acceptId(nutsId, session)) {
                                    continue;
                                }
                                ret.add(
                                        NIdBuilder.of(groupId,artifactId).setVersion(version).build()
                                );
                            }
                        }
                    } catch (UncheckedIOException | NIOException ex) {
                        //unable to access
                        return IteratorBuilder.emptyIterator();
                    } finally {
                        if (metadataStream != null) {
                            try {
                                metadataStream.close();
                            } catch (IOException e) {
//                    throw new NutsIOException(getWorkspace(),e);
                                return IteratorBuilder.emptyIterator();
                            }
                        }
                    }
                    return ret.iterator();
                }
                , e -> NElements.of(e).ofObject()
                        .set("type", "ScanMavenMetadataXml")
                        .set("path", metadataURL.toString())
                        .build(),
                session).build();


    }

//            case MAVEN: {
//                // this will find only in archetype, not in full index....
//                String url = config.getLocation(true).resolve("/archetype-catalog.xml").toString();
//                try {
//                    InputStream s = CoreIOUtils.getCachedUrlWithSHA1(url, "archetype-catalog.xml",
//                            true,
//                            session
//                    );
//                    final InputStream is = NutsInputStreamMonitor.of(session).setSource(s).create();
//                    return MavenUtils.of(session)
//                            .createArchetypeCatalogIterator(is, filter, true, session);
//                } catch (UncheckedIOException | NutsIOException ex) {
//                    return IteratorUtils.emptyIterator();
//                }
//            }

}
