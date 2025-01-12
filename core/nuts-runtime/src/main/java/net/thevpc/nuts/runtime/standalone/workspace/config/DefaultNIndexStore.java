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
 *
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
package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.util.NDescribables;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NPredicates;
import net.thevpc.nuts.util.NStringUtils;

import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class DefaultNIndexStore extends AbstractNIndexStore {


    public DefaultNIndexStore(NRepository repository) {
        super(repository);
    }

    @Override
    public NIterator<NId> searchVersions(NId id, NSession session) {
        return IteratorBuilder.ofSupplier(
                () -> {
                    if (isInaccessible()) {
                        return IteratorBuilder.emptyIterator();
                    }
                    String uu = getIndexURL(session).resolve( NConstants.Folders.ID).resolve( "allVersions")
                            + String.format("?repositoryUuid=%s&name=%s&repo=%s&group=%s"
                                    + "&os=%s&osdist=%s&arch=%s&face=%s&"/*alternative=%s*/,
                            getRepository().getUuid(),
                            NStringUtils.trim(id.getArtifactId()), NStringUtils.trim(id.getRepository()), NStringUtils.trim(id.getGroupId()),
                            NStringUtils.trim(String.join(",",id.getCondition().getOs())),
                            NStringUtils.trim(String.join(",",id.getCondition().getOsDist())),
                            NStringUtils.trim(String.join(",",id.getCondition().getArch())), NStringUtils.trim(id.getFace())
//                , NutsUtilStrings.trim(id.getAlternative())
                    );
                    try {
                        Map[] array = NElements.of(session).json().parse(new InputStreamReader(NPath.of(uu,session).getInputStream()), Map[].class);
                        return Arrays.stream(array)
                                .map(s -> NId.of(s.get("stringId").toString()).get(session))
                                .collect(Collectors.toList()).iterator();
                    } catch (UncheckedIOException | NIOException e) {
                        setInaccessible();
                        return IteratorBuilder.emptyIterator();
                    }
                },
                e-> NElements.of(e)
                        .ofObject()
                        .set("type","SearchIndexVersions")
                        .set("source", getIndexURL(session).resolve( NConstants.Folders.ID).resolve( "allVersions").toString())
                        .build(),
                session).build();
    }

    @Override
    public NIterator<NId> search(NIdFilter filter, NSession session) {
        NElements elems = NElements.of(session);
        return IteratorBuilder.ofSupplier(
                () -> {
                    if (isInaccessible()) {
                        throw new NIndexerNotAccessibleException(session, NMsg.ofC("index search failed for %s",getRepository().getName()));
//                        return IteratorUtils.emptyIterator();
                    }
                    String uu = getIndexURL(session).resolve(NConstants.Folders.ID) + "?repositoryUuid=" + getRepository().getUuid();
                    try {
                        Map[] array = elems.json().parse(new InputStreamReader(NPath.of(uu,session).getInputStream()), Map[].class);
                        return Arrays.stream(array)
                                .map(s -> NId.of(s.get("stringId").toString()).get(session))
                                .filter(filter != null ? new NIdFilterToNIdPredicate(filter, session) : NPredicates.always())
                                .iterator();
                    } catch (UncheckedIOException | NIOException e) {
                        setInaccessible();
                        throw new NIndexerNotAccessibleException(session, NMsg.ofC("index search failed for %s",getRepository().getName()));
//                        return IteratorUtils.emptyIterator();
                    }
                },
                e-> NElements.of(e)
                        .ofObject().set("type","SearchIndexPackages")
                        .set("source", getIndexURL(session).resolve(NConstants.Folders.ID).toString())
                        .set("filter", NDescribables.resolveOrToString(filter,session))
                        .build(),
                session).build();
    }

    private NPath getIndexURL(NSession session) {
        return NPath.of("http://localhost:7070/indexer/",session);
    }

    @Override
    public NIndexStore invalidate(NId id, NSession session) {
        if (isInaccessible()) {
            return this;
        }
        String uu = getIndexURL(session).resolve( NConstants.Folders.ID).resolve("delete")
                + String.format("?repositoryUuid=%s&name=%s&repo=%s&group=%s&version=%s"
                        + "&os=%s&osdist=%s&arch=%s&face=%s"/*&alternative=%s*/, getRepository().getUuid(),
                NStringUtils.trim(id.getArtifactId()), NStringUtils.trim(id.getRepository()), NStringUtils.trim(id.getGroupId()), NStringUtils.trim(id.getVersion().toString()),
                NStringUtils.trim(String.join(",",id.getCondition().getOs())),
                NStringUtils.trim(String.join(",",id.getCondition().getOsDist())),
                NStringUtils.trim(String.join(",",id.getCondition().getArch())),
                NStringUtils.trim(id.getFace())
//                ,NutsUtilStrings.trim(id.getAlternative())
        );
        try {
            NPath.of(uu,session).getInputStream();
        } catch (UncheckedIOException | NIOException e) {
            setInaccessible();
            //
        }
        return this;
    }

    @Override
    public NIndexStore revalidate(NId id, NSession session) {
        if (isInaccessible()) {
            return this;
        }
        String uu = getIndexURL(session).resolve(NConstants.Folders.ID).resolve("addData")
                + String.format("?repositoryUuid=%s&name=%s&repo=%s&group=%s&version=%s"
                        + "&os=%s&osdist=%s&arch=%s&face=%s"/*&alternative=%s*/, getRepository().getUuid(),
                NStringUtils.trim(id.getArtifactId()), NStringUtils.trim(id.getRepository()), NStringUtils.trim(id.getGroupId()), NStringUtils.trim(id.getVersion().toString()),
                NStringUtils.trim(String.join(",",id.getCondition().getOs())),
                NStringUtils.trim(String.join(",",id.getCondition().getOsDist())),
                NStringUtils.trim(String.join(",",id.getCondition().getArch())),
                NStringUtils.trim(id.getFace())
//                ,NutsUtilStrings.trim(id.getAlternative())
        );
        try {
            NPath.of(uu,session).getInputStream();
        } catch (UncheckedIOException | NIOException e) {
            setInaccessible();
            //
        }
        return this;
    }

    @Override
    public NIndexStore subscribe(NSession session) {
        String uu = "http://localhost:7070/indexer/subscription/subscribe?workspaceLocation="
                + CoreIOUtils.urlEncodeString(NLocations.of(session).getWorkspaceLocation().toString(),session)
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid(),session);
        try {
            NPath.of(uu,session).getInputStream();
        } catch (UncheckedIOException | NIOException e) {
            throw new NUnsupportedOperationException(session, NMsg.ofC("unable to subscribe for repository%s", getRepository().getName()), e);
        }
        return this;
    }

    @Override
    public NIndexStore unsubscribe(NSession session) {
        String uu = "http://localhost:7070/indexer/subscription/unsubscribe?workspaceLocation="
                + CoreIOUtils.urlEncodeString(NLocations.of(session).getWorkspaceLocation().toString(),session)
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid(),session);
        try {
            NPath.of(uu,session).getInputStream();
        } catch (UncheckedIOException | NIOException e) {
            throw new NUnsupportedOperationException(session, NMsg.ofC("unable to unsubscribe for repository %s", getRepository().getName()), e);
        }
        return this;
    }

    @Override
    public boolean isSubscribed(NSession session) {
        String uu = "http://localhost:7070/indexer/subscription/isSubscribed?workspaceLocation="
                + CoreIOUtils.urlEncodeString(NLocations.of(session).getWorkspaceLocation().toString(),session)
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid(),session);
        try {
            return new Scanner(NPath.of(uu,session).getInputStream()).nextBoolean();
        } catch (UncheckedIOException | NIOException e) {
            return false;
        }
    }

}
