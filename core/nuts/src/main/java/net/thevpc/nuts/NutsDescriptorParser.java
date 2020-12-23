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
 *
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
package net.thevpc.nuts;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * @category Core
 */
public interface NutsDescriptorParser {

    /**
     * parse descriptor.
     *
     * @param url URL to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(URL url);

    /**
     * parse descriptor.
     *
     * @param bytes value to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(byte[] bytes);

    /**
     * parse descriptor.
     *
     * @param path path to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(Path path);

    /**
     * parse descriptor.
     *
     * @param file file to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(File file);

    /**
     * parse descriptor.
     *
     * @param stream stream to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(InputStream stream);

    /**
     * parse descriptor.
     *
     * @param descriptorString string to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(String descriptorString);

    NutsDescriptorParser setLenient(boolean lenient);

    boolean isLenient();
}