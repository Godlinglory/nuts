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
package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.ObjectFilter;

import java.lang.reflect.Array;
import java.util.*;

public class CoreCollectionUtils {

    public static <T> T[] filterArray(Class<T> cls,T[] array, ObjectFilter<T> t) {
        List<T> all=new ArrayList<>();
        for (T v : array) {
            if(t==null || t.accept(v)){
                all.add(v);
            }
        }
        return all.toArray((T[]) Array.newInstance(cls));
    }

    public static String[] toArraySet(String[] values0, String[]... values) {
        Set<String> set = toSet(values0);
        if (values != null) {
            for (String[] value : values) {
                set.addAll(toSet(value));
            }
        }
        return set.toArray(new String[set.size()]);
    }

    public static Set<String> toSet(String[] values0) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (values0 != null) {
            for (String a : values0) {
                a = CoreStringUtils.trim(a);
                if (!CoreStringUtils.isEmpty(a) && !set.contains(a)) {
                    set.add(a);
                }
            }
        }
        return set;
    }

    public static <T> List<T> toList(Iterator<T> it) {
        List<T> list = new ArrayList<>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }
}
