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
package net.vpc.app.nuts.extensions.parsers;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.util.JsonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by vpc on 1/15/17.
 */
public class NutsShellDescriptorContentParserComponent implements NutsDescriptorContentParserComponent {

    public static final NutsId NSH = NutsId.parse("nuts");
    public static final Set<String> POSSIBLE_EXT = new HashSet<>(Arrays.asList("nsh","nuts"));

    @Override
    public NutsDescriptor parse(NutsDescriptorContentParserContext parserContext) {
        if (!POSSIBLE_EXT.contains(parserContext.getFileExtension())) {
            return null;
        }
        try {
            return readNutDescriptorFromBashScriptFile(parserContext.getFullStream());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT;
    }

    private static String removeBashComment(String str) {
        int x = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '#' || str.charAt(i) == ' ') {
                x = i + 1;
            } else {
                break;
            }
        }
        if (x < str.length()) {
            return str.substring(x);
        }
        return "";
    }

    private static NutsDescriptor readNutDescriptorFromBashScriptFile(InputStream file) throws IOException {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(file));
            String line = null;
            boolean firstLine = true;
            JsonUtils.JsonStringBuffer comment = new JsonUtils.JsonStringBuffer();
            String sheban = "";
            boolean start = false;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    if (line.startsWith("#")) {
                        line = removeBashComment(line);
                        if (firstLine) {
                            firstLine = false;
                            //sheban
                            if (line.startsWith("!")) {
                                sheban = line.substring(1).trim();
                            } else {
                                break;
                            }
                        }
                        if (!start && line.matches("@nuts((\\s|\\{).*)?")) {
                            start = true;
                            String substring = line.substring(line.indexOf("@nuts") + "@nuts".length());
                            if (comment.append(substring)) {
                                break;
                            }

                        } else if (start) {
                            if (comment.append(line)) {
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
            if(comment.toString().trim().isEmpty()){
                return new DefaultNutsDescriptor(
                        NutsId.parse("temp:nsh#1.0"),
                        null,
                        null,
                        "nsh",
                        true, "nsh", new NutsExecutorDescriptor(NSH, new String[0], null), null, null, null, null, null, null, null, null, null
                );
            }
            return NutsDescriptor.parse(comment.getValidString());
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }
}
