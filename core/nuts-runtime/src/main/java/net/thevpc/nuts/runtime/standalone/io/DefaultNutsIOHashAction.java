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
package net.thevpc.nuts.runtime.standalone.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 *
 * @author thevpc
 */
public class DefaultNutsIOHashAction implements NutsIOHashAction {

    private Object value;
    private String type;
    private String algorithm;
    private NutsWorkspace ws;
    private NutsSession session;

    public DefaultNutsIOHashAction(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public NutsIOHashAction algorithm(String algorithm) {
        return setAlgorithm(algorithm);
    }

    @Override
    public NutsIOHashAction setAlgorithm(String algorithm) {
        if (NutsUtilStrings.isBlank(algorithm)) {
            algorithm = null;
        }
        try {
            MessageDigest.getInstance(algorithm);
            this.algorithm = algorithm;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsIOHashAction setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsIOHashAction sha1() {
        return setAlgorithm("SHA1");
    }

    @Override
    public NutsIOHashAction md5() {
        return setAlgorithm("MD5");
    }

    @Override
    public NutsIOHashAction setSource(InputStream input) {
        this.value = input;
        this.type = "stream";
        return this;
    }

    @Override
    public NutsIOHashAction setSource(File file) {
        this.value = file;
        this.type = "file";
        return this;
    }

    @Override
    public NutsIOHashAction setSource(Path path) {
        this.value = path;
        this.type = "path";
        return this;
    }

    @Override
    public NutsIOHashAction setSource(NutsPath path) {
        this.value = path;
        this.type = "nuts-path";
        return this;
    }

    @Override
    public NutsIOHashAction setSource(NutsDescriptor descriptor) {
        this.value = descriptor;
        this.type = "desc";
        return this;
    }

    @Override
    public String computeString() {
        return NutsUtilStrings.toHexString(computeBytes());
    }

    protected String getValidAlgo() {
        if (algorithm == null) {
            return "SHA1";
        }
        return algorithm;
    }

    @Override
    public NutsIOHashAction writeHash(OutputStream out) {
        checkSession();
        if (type == null || value == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing Source"));
        }
        switch (type) {
            case "stream": {
                try {
                    out.write(CoreIOUtils.evalHash(((InputStream) value), getValidAlgo()));
                    return this;
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            case "file": {
                try (InputStream is = new BufferedInputStream(Files.newInputStream(((File) value).toPath()))) {
                    out.write(CoreIOUtils.evalHash(is, getValidAlgo()));
                    return this;
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            case "path": {
                try (InputStream is = new BufferedInputStream(Files.newInputStream(((Path) value)))) {
                    out.write(CoreIOUtils.evalHash(is, getValidAlgo()));
                    return this;
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            case "nuts-path": {
                try (InputStream is = new BufferedInputStream((((NutsPath) value)).input().open())) {
                    out.write(CoreIOUtils.evalHash(is, getValidAlgo()));
                    return this;
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            case "desc": {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                getSession().getWorkspace().descriptor().formatter((NutsDescriptor) value).compact().print(new OutputStreamWriter(o));
                try (InputStream is = new ByteArrayInputStream(o.toByteArray())) {
                    out.write(CoreIOUtils.evalHash(is, getValidAlgo()));
                    return this;
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.cstyle("unsupported type %s", type));
            }
        }
    }

    @Override
    public byte[] computeBytes() {
        if (type == null || value == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing Source"));
        }
        checkSession();
        switch (type) {
            case "stream": {
                return CoreIOUtils.evalHash(((InputStream) value), getValidAlgo());
            }
            case "file": {
                try (InputStream is = new BufferedInputStream(Files.newInputStream(((File) value).toPath()))) {
                    return CoreIOUtils.evalHash(is, getValidAlgo());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            case "path": {
                try (InputStream is = new BufferedInputStream(Files.newInputStream(((Path) value)))) {
                    return CoreIOUtils.evalHash(is, getValidAlgo());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            case "desc": {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                getSession().getWorkspace().descriptor().formatter((NutsDescriptor) value).compact().setSession(session).print(new OutputStreamWriter(o));
                try (InputStream is = new ByteArrayInputStream(o.toByteArray())) {
                    return CoreIOUtils.evalHash(is, getValidAlgo());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.cstyle("unsupported type %s", type));
            }
        }
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
    }
}
