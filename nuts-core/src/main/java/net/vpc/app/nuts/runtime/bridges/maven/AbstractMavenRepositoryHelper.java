/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.bridges.maven;

import net.vpc.app.nuts.*;

import java.io.*;
import java.util.Map;
import java.util.logging.Level;

import net.vpc.app.nuts.runtime.CoreNutsConstants;
import net.vpc.app.nuts.runtime.io.NamedByteArrayInputStream;
import net.vpc.app.nuts.NutsLogger;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.io.InputSource;

/**
 * Created by vpc on 2/20/17.
 */
public abstract class AbstractMavenRepositoryHelper {

    private final NutsLogger LOG;
    private NutsRepository repository;

    public AbstractMavenRepositoryHelper(NutsRepository repository) {
        this.repository = repository;
        LOG=repository.getWorkspace().log().of(AbstractMavenRepositoryHelper.class);
    }

    protected abstract String getIdPath(NutsId id);

    protected InputSource getStream(NutsId id, NutsRepositorySession session) {
        String url = getIdPath(id);
        return openStream(id, url, id, session);
    }

    protected String getStreamAsString(NutsId id, NutsRepositorySession session) {
        String url = getIdPath(id);
        return CoreIOUtils.loadString(openStream(id, url, id, session).open(), true);
    }

    protected void checkSHA1Hash(NutsId id, InputStream stream, NutsRepositorySession session) throws IOException {
        switch (CoreStringUtils.trim(id.getFace())) {
            case NutsConstants.QueryFaces.CONTENT_HASH:
            case NutsConstants.QueryFaces.DESCRIPTOR_HASH: {
                break;
            }
            default: {
                LOG.log(Level.SEVERE, "[BUG] Unsupported Hash Type " + id.getFace(), new RuntimeException());
                throw new IOException("Unsupported hash type " + id.getFace());
            }
        }
        try {
            String rhash = getStreamSHA1(id, session);
            String lhash = CoreIOUtils.evalSHA1Hex(stream, true);
            if (!rhash.equalsIgnoreCase(lhash)) {
                throw new IOException("Invalid file hash " + id);
            }
        } finally {
            stream.close();
        }
    }

    protected String getStreamSHA1(NutsId id, NutsRepositorySession session) {
        String hash = getStreamAsString(id, session).toUpperCase();
        for (String s : hash.split("[ \n\r]")) {
            if (s.length() > 0) {
                return s;
            }
        }
        return hash.split("[ \n\r]")[0];
    }

    protected abstract InputSource openStream(NutsId id, String path, Object source, NutsRepositorySession session);

    public NutsDescriptor fetchDescriptorImpl(NutsId id, NutsRepositorySession session) {
        InputSource stream = null;
        try {
            NutsDescriptor nutsDescriptor = null;
            byte[] bytes = null;
            String name=null;
            NutsId idDesc = id.builder().setFaceDescriptor().build();
            try {
                stream = getStream(idDesc, session);
                bytes = CoreIOUtils.loadByteArray(stream.open(), true);
                name = stream.getName();
                nutsDescriptor = MavenUtils.of(session.getWorkspace()).parsePomXml(new NamedByteArrayInputStream(bytes, name), session, getIdPath(id));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            checkSHA1Hash(id.builder().setFace(NutsConstants.QueryFaces.DESCRIPTOR_HASH).build(), new NamedByteArrayInputStream(bytes,name), session);
            return nutsDescriptor;
        } catch (IOException ex) {
            throw new NutsNotFoundException(repository.getWorkspace(), id, null, ex);
        } catch (UncheckedIOException ex) {
            throw new NutsNotFoundException(repository.getWorkspace(), id, ex);
        }
    }

    protected String getIdExtension(NutsId id) {
        Map<String, String> q = id.getProperties();
        String f = CoreStringUtils.trim(q.get(NutsConstants.IdProperties.FACE));
        switch (f) {
            case NutsConstants.QueryFaces.DESCRIPTOR: {
                return ".pom";
            }
            case NutsConstants.QueryFaces.DESCRIPTOR_HASH: {
                return ".pom.sha1";
            }
            case CoreNutsConstants.QueryFaces.CATALOG: {
                return ".catalog";
            }
            case NutsConstants.QueryFaces.CONTENT_HASH: {
                return getIdExtension(id.builder().setFaceContent().build()) + ".sha1";
            }
            case NutsConstants.QueryFaces.CONTENT: {
                String packaging = q.get(NutsConstants.IdProperties.PACKAGING);
                return repository.getWorkspace().config().getDefaultIdContentExtension(packaging);
            }
            default: {
                throw new NutsUnsupportedArgumentException(repository.getWorkspace(), "Unsupported fact " + f);
            }
        }
    }
}
