/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.parsers;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.bridges.maven.MavenUtils;
import net.vpc.app.nuts.core.DefaultNutsDescriptorBuilder;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.Ref;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.io.InputStreamVisitor;
import net.vpc.app.nuts.core.util.io.ZipUtils;

/**
 * Created by vpc on 1/15/17.
 */
public class JarNutsDescriptorContentParserComponent implements NutsDescriptorContentParserComponent {

    public static final Set<String> POSSIBLE_EXT = new HashSet<>(Collections.singletonList("jar"));//, "war", "ear"

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsDescriptor parse(final NutsDescriptorContentParserContext parserContext) {
        if (!POSSIBLE_EXT.contains(parserContext.getFileExtension())) {
            return null;
        }
        final NutsId JAVA = CoreNutsUtils.parseNutsId("java");
        final Ref<NutsDescriptor> nutsjson = new Ref<>();
        final Ref<NutsDescriptor> metainf = new Ref<>();
        final Ref<NutsDescriptor> maven = new Ref<>();
        final Ref<String> mainClass = new Ref<>();

        try {
            ZipUtils.visitZipStream(parserContext.getFullStream(), new Predicate<String>() {
                @Override
                public boolean test(String path) {
                    if ("META-INF/MANIFEST.MF".equals(path)) {
                        return true;
                    }
                    if (("META-INF/"+NutsConstants.Files.DESCRIPTOR_FILE_NAME).equals(path)) {
                        return true;
                    }
                    return path.startsWith("META-INF/maven/") && path.endsWith("/pom.xml");
                }
            }, new InputStreamVisitor() {
                @Override
                public boolean visit(String path, InputStream inputStream) throws IOException {
                    switch (path) {
                        case "META-INF/MANIFEST.MF":
                            Manifest manifest = new Manifest(inputStream);
                            Attributes attrs = manifest.getMainAttributes();
                            for (Object o : attrs.keySet()) {
                                Attributes.Name attrName = (Attributes.Name) o;
                                if ("Main-Class".equals(attrName.toString())) {
                                    mainClass.set(attrs.getValue(attrName));
                                }
                            }
                            NutsDescriptor d = new DefaultNutsDescriptorBuilder()
                                    .setId(CoreNutsUtils.parseNutsId("temp:jar#1.0"))
                                    .setExecutable(mainClass.isSet())
                                    .setPackaging("jar")
                                    .setExecutor(new NutsExecutorDescriptor(JAVA, new String[]{"-jar"}))
                                    .build();

                            metainf.set(d);
                            break;
                        case ("META-INF/"+NutsConstants.Files.DESCRIPTOR_FILE_NAME):
                            nutsjson.set(parserContext.getWorkspace().parse().descriptor(inputStream, true));
                            break;
                        default:
                            try {
                                maven.set(MavenUtils.parsePomXml(inputStream, parserContext.getWorkspace(), NutsWorkspaceHelper.createNoRepositorySession(null, parserContext.getSession(), NutsFetchMode.REMOTE, parserContext.getQueryOptions()), path));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    //continue
                    return !nutsjson.isSet() || (!metainf.isSet() && !maven.isSet());
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        if (nutsjson.isSet()) {
            return nutsjson.get();
        }
        NutsDescriptor baseNutsDescriptor = null;
        if (maven.isSet()) {
            baseNutsDescriptor = maven.get();
            if (mainClass.isSet()) {
                return baseNutsDescriptor.setExecutor(new NutsExecutorDescriptor(JAVA, new String[]{
                    "--main-class", mainClass.get()}));
            }
        } else if (metainf.isSet()) {
            baseNutsDescriptor = metainf.get();
        }
        if (baseNutsDescriptor == null) {
            baseNutsDescriptor = new DefaultNutsDescriptorBuilder()
                    .setId(CoreNutsUtils.parseNutsId("temp:jar#1.0"))
                    .setExecutable(true)
                    .setPackaging("jar")
                    .build();
        }
        NutsExecutionEntry[] classes = parserContext.getWorkspace().parse().executionEntries(parserContext.getFullStream(), "java", parserContext.getFullStream().toString());
        if (classes.length == 0) {
            return baseNutsDescriptor;
        } else {
            return baseNutsDescriptor.setExecutor(new NutsExecutorDescriptor(JAVA, new String[]{
                "--main-class=" + CoreStringUtils.join(":",
                Arrays.stream(classes)
                .map(x -> x.getName())
                .collect(Collectors.toList())
                )}, null)).setExecutable(true);
        }
    }
}
