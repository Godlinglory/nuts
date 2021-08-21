/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.core.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.thevpc.nuts.NutsClassifierMapping;
import net.thevpc.nuts.NutsIdLocation;
import net.thevpc.nuts.NutsUtilStrings;

/**
 *
 * @author vpc
 */
public class CoreCollectionUtils {

    public static <T> List<T> toList(Iterator<T> it) {
        List<T> all = new ArrayList<>();
        while (it.hasNext()) {
            all.add(it.next());
        }
        return all;
    }

    public static Set<String> toSet(String[] values0) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (values0 != null) {
            for (String a : values0) {
                a = NutsUtilStrings.trim(a);
                if (!NutsUtilStrings.isBlank(a)) {
                    set.add(a);
                }
            }
        }
        return set;
    }

    public static Set<NutsClassifierMapping> toSet(NutsClassifierMapping[] classifierMappings) {
        LinkedHashSet<NutsClassifierMapping> set = new LinkedHashSet<>();
        if (classifierMappings != null) {
            for (NutsClassifierMapping a : classifierMappings) {
                if (a != null) {
                    set.add(a);
                }
            }
        }
        return set;
    }

    public static Set<NutsIdLocation> toSet(NutsIdLocation[] classifierMappings) {
        LinkedHashSet<NutsIdLocation> set = new LinkedHashSet<>();
        if (classifierMappings != null) {
            for (NutsIdLocation a : classifierMappings) {
                if (a != null) {
                    set.add(a);
                }
            }
        }
        return set;
    }
    
}
