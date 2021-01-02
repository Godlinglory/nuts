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
package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;

import java.io.IOException;
import java.io.InputStream;

import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.NamedByteArrayInputStream;
import net.thevpc.nuts.spi.NutsDescriptorContentParserContext;

/**
 * Created by vpc on 1/29/17.
 */
public class DefaultNutsDescriptorContentParserContext implements NutsDescriptorContentParserContext {

    private final NutsSession session;
    private final NutsInput file;
    private final String fileExtension;
    private final String mimeType;
    private byte[] bytes;
    private final String[] parseOptions;

    public DefaultNutsDescriptorContentParserContext(NutsSession session, NutsInput file, String fileExtension, String mimeType, String[] parseOptions) {
        this.file = session.getWorkspace().io().input().setMultiRead(true).of(file);
        this.session = session;
        this.fileExtension = fileExtension;
        this.mimeType = mimeType;
        this.parseOptions = parseOptions;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public String[] getParseOptions() {
        return parseOptions;
    }

    @Override
    public InputStream getHeadStream() {
        if (bytes == null) {
            try {
                try (InputStream is = file.open()) {
                    bytes = CoreIOUtils.loadByteArray(is, 1024 * 1024 * 10, true);
                }
            } catch (IOException e) {
                throw new NutsIOException(getWorkspace(),e);
            }
        }
        return new NamedByteArrayInputStream(bytes, file.getName());
    }

    @Override
    public InputStream getFullStream() {
        return file.open();
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getName() {
        return file.getName();
    }

}
