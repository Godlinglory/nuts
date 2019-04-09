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
package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.filters.id.NutsJsAwareIdFilter;
import net.vpc.app.nuts.core.util.*;

import java.io.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.util.bundledlibs.util.IteratorBuilder;

public class NutsHttpSrvRepository extends AbstractNutsRepository {

    private static final Logger log = Logger.getLogger(NutsHttpSrvRepository.class.getName());
    private NutsId remoteId;

    public NutsHttpSrvRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_SLOW, false, NutsConstants.RepoTypes.NUTS);
        try {
            remoteId = CoreNutsUtils.parseRequiredNutsId(httpGetString(options.getLocation() + "/version"));
        } catch (Exception ex) {
            log.log(Level.WARNING, "Unable to initialize Repository NutsId for repository {0}", options.getLocation());
        }
    }

    @Override
    public void pushImpl(NutsId id, NutsPushCommand options, NutsRepositorySession session) {
        throw new NutsUnsupportedOperationException();
    }

    public String getUrl(String path) {
        return CoreIOUtils.buildUrl(config().getLocation(true), path);
    }

    public NutsId getRemoteId() {
        if (remoteId == null) {
            try {
                remoteId = CoreNutsUtils.parseRequiredNutsId(httpGetString(getUrl("/version")));
            } catch (Exception ex) {
                log.log(Level.WARNING, "Unable to resolve Repository NutsId for remote repository {0}", config().getLocation(false));
            }
        }
        return remoteId;
    }

    @Override
    protected void deployImpl(NutsRepositoryDeploymentOptions deployment, NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            throw new NutsIllegalArgumentException("Offline");
        }
        ByteArrayOutputStream descStream = new ByteArrayOutputStream();
        getWorkspace().formatter().createDescriptorFormat().setPretty(true).print(deployment.getDescriptor(), new OutputStreamWriter(descStream));
        httpUpload(CoreIOUtils.buildUrl(config().getLocation(true), "/deploy?" + resolveAuthURLPart()),
                new NutsTransportParamBinaryStreamPart("descriptor", "Project.nuts",
                        new ByteArrayInputStream(descStream.toByteArray())),
                new NutsTransportParamBinaryFilePart("content", deployment.getContent().getFileName().toString(), deployment.getContent()),
                new NutsTransportParamParamPart("descriptor-hash", getWorkspace().io().getSHA1(deployment.getDescriptor())),
                new NutsTransportParamParamPart("content-hash", CoreIOUtils.evalSHA1(deployment.getContent())),
                new NutsTransportParamParamPart("force", String.valueOf(deployment.isForce()))
        );
        //TODO should read the id
    }

    @Override
    public NutsDescriptor fetchDescriptorImpl(NutsId id, NutsRepositorySession session) {
        boolean transitive = session.isTransitive();
        InputStream stream = null;
        try {
            try {
                stream = CoreIOUtils.getHttpClientFacade(getWorkspace(), getUrl("/fetch-descriptor?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart())).open();
                NutsDescriptor descriptor = getWorkspace().parser().parseDescriptor(stream, true);
                if (descriptor != null) {
                    String hash = httpGetString(getUrl("/fetch-descriptor-hash?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
                    if (hash.equals(descriptor.toString())) {
                        return descriptor;
                    }
                }
            } catch (UncheckedIOException ex) {
                throw new NutsNotFoundException(id);
            }
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        throw new NutsNotFoundException(id);
    }

    protected void copyToImpl(NutsId id, Path localPath, NutsRepositorySession session) {
        boolean transitive = session.isTransitive();

        try {
            helperHttpDownloadToFile(getUrl("/fetch?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()), localPath, true);
            String rhash = httpGetString(getUrl("/fetch-hash?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
            String lhash = CoreIOUtils.evalSHA1(localPath);
            if (rhash.equalsIgnoreCase(lhash)) {
                return;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        throw new NutsNotFoundException(id);
    }

    @Override
    public Iterator<NutsId> findVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {
        boolean transitive = session.isTransitive();
        InputStream ret = null;
        try {
            ret = CoreIOUtils.getHttpClientFacade(getWorkspace(), getUrl("/find-versions?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart())).open();
        } catch (UncheckedIOException e) {
            return Collections.emptyIterator();
        }
        Iterator<NutsId> it = new NamedNutIdFromStreamIterator(ret);
        if (idFilter != null) {
            it = IteratorBuilder.of(it).filter(CoreNutsUtils.createFilter(idFilter)).iterator();
        }
        return it;
    }

    @Override
    public Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsRepositorySession session) {

        boolean transitive = session.isTransitive();
        InputStream ret = null;
        String[] ulp = resolveEncryptedAuth();
        if (filter instanceof NutsJsAwareIdFilter) {
            String js = ((NutsJsAwareIdFilter) filter).toJsNutsIdFilterExpr();
            if (js != null) {
                ret = httpUpload(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart()),
                        new NutsTransportParamParamPart("root", "/"),
                        new NutsTransportParamParamPart("ul", ulp[0]),
                        new NutsTransportParamParamPart("up", ulp[1]),
                        new NutsTransportParamTextReaderPart("js", "search.js", new StringReader(js))
                );
                return IteratorBuilder.of(new NamedNutIdFromStreamIterator(ret)).filter(CoreNutsUtils.createFilter(filter)).iterator();
            }
        } else {
            ret = httpUpload(getUrl("/find?" + (transitive ? ("transitive") : "") + "&" + resolveAuthURLPart()),
                    new NutsTransportParamParamPart("root", "/"),
                    new NutsTransportParamParamPart("ul", ulp[0]),
                    new NutsTransportParamParamPart("up", ulp[1]),
                    new NutsTransportParamParamPart("pattern", ("*")),
                    new NutsTransportParamParamPart("transitive", String.valueOf(transitive))
            );
        }
        if (filter == null) {
            return new NamedNutIdFromStreamIterator(ret);
        }
        return IteratorBuilder.of(new NamedNutIdFromStreamIterator(ret)).filter(CoreNutsUtils.createFilter(filter)).iterator();

    }

    //    @Override
//    public int getSupportLevel(NutsId id, boolean transitive) throws IOException {
//        int val = super.getSupportLevel(id, transitive);
//        if (val > 1) {
//            return val;
//        }
//        String s = httpGetString(getIdPath("/get-support/?id=" + HttpUtils.urlEncodeString(id.toString())  + (transitive ? ("&transitive") : "") + resolveAuthURLPart()));
//        return s == null ? -1 : Integer.parseInt(s);
//    }
//    @Override
//    public NutsDefinition fetchImpl(NutsId id, NutsSession session) {
//        return helperFetchImplByCopy(id,session);
//    }
    @Override
    protected NutsContent fetchContentImpl(NutsId id, Path localPath, NutsRepositorySession session) {
        boolean transitive = session.isTransitive();
        boolean temp = false;
        if (localPath == null) {
            temp = true;
            String p = getIdFilename(id);
            localPath = getWorkspace().io().createTempFile(new File(p).getName(), this);
        }

        try {
            helperHttpDownloadToFile(getUrl("/fetch?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()), localPath, true);
            String rhash = httpGetString(getUrl("/fetch-hash?id=" + CoreIOUtils.urlEncodeString(id.toString()) + (transitive ? ("&transitive") : "") + "&" + resolveAuthURLPart()));
            String lhash = CoreIOUtils.evalSHA1(localPath);
            if (rhash.equalsIgnoreCase(lhash)) {
                return new NutsContent(localPath, false, temp);
            }
        } catch (IOException ex) {
            throw new NutsNotFoundException(id, ex);
            //
        }
        throw new NutsNotFoundException(id);

    }

    private String httpGetString(String url) {
        log.log(Level.FINEST, "Get URL{0}", url);
        return CoreIOUtils.loadString(CoreIOUtils.getHttpClientFacade(getWorkspace(), url).open(), true);
    }

    private InputStream httpUpload(String url, NutsTransportParamPart... parts) {
        log.log(Level.FINEST, "Uploading URL {0}", url);
        return CoreIOUtils.getHttpClientFacade(getWorkspace(), url).upload(parts);
    }

    @Override
    public String toString() {
        return super.toString() + ((this.remoteId == null ? "" : " ; desc=" + this.remoteId));
    }

    private String[] resolveEncryptedAuth() {
        String login = getWorkspace().security().getCurrentLogin();
        NutsUserConfig security = config().getUser(login);
        String newLogin = "";
        String credentials = "";
        if (security == null) {
            newLogin = "anonymous";
            credentials = "anonymous";
        } else {
            newLogin = security.getMappedUser();
            if (CoreStringUtils.isBlank(newLogin)) {
                NutsEffectiveUser security2 = getWorkspace().security().findUser(login);
                if (security2 != null) {
                    newLogin = security2.getMappedUser();
                }
            }
            if (CoreStringUtils.isBlank(newLogin)) {
                newLogin = login;
            }
            credentials = getWorkspace().config().createAuthenticationAgent(security.getAuthenticationAgent())
                    .getCredentials(credentials, security.getAuthenticationAgent(),
                            config());
        }

        String passphrase = config().getEnv(NutsConstants.ENV_KEY_PASSPHRASE, CoreNutsUtils.DEFAULT_PASSPHRASE, true);
        newLogin = new String(CoreSecurityUtils.httpEncrypt(CoreStringUtils.trim(newLogin).getBytes(), passphrase));
        credentials = new String(CoreSecurityUtils.httpEncrypt(CoreStringUtils.trim(credentials).getBytes(), passphrase));
        return new String[]{newLogin, credentials};
    }

    private String resolveAuthURLPart() {
        String[] auth = resolveEncryptedAuth();
        return "ul=" + CoreIOUtils.urlEncodeString(auth[0]) + "&up=" + CoreIOUtils.urlEncodeString(auth[0]);
    }

    @Override
    protected void undeployImpl(NutsId id, NutsRepositorySession session) {
        throw new NutsUnsupportedOperationException();
    }

    @Override
    public void checkAllowedFetch(NutsId id, NutsRepositorySession session) {
        super.checkAllowedFetch(id, session);
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(id);
        }
    }

    private class NamedNutIdFromStreamIterator implements Iterator<NutsId> {

        private final BufferedReader br;
        private String line;

        public NamedNutIdFromStreamIterator(InputStream ret) {
            br = new BufferedReader(new InputStreamReader(ret));
            line = null;
        }

        @Override
        public boolean hasNext() {
            while (true) {
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    return false;
                }
                if (line == null) {
                    return false;
                }
                line = line.trim();
                if (line.length() > 0) {
                    return true;
                }
            }
        }

        @Override
        public NutsId next() {
            NutsId nutsId = getWorkspace().parser().parseRequiredId(line);
            return nutsId.setNamespace(config().getName());
        }
    }

//    @Override
//    public Path getComponentsLocation() {
//        return null;
//    }
}
