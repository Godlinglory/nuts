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
package net.thevpc.nuts.runtime.standalone.util.filters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.Simplifiable;
import net.thevpc.nuts.util.*;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class CoreFilterUtils {
    private static int andInts(Boolean a, Boolean b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            return b ? 1 : -1;
        } else if (b == null) {
            return a ? 1 : -1;
        } else if (a.equals(b)) {
            return a ? 1 : -1;
        } else {
            return 2;
        }
    }

    private static int orInts(Boolean a, Boolean b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            return b ? 1 : -1;
        } else if (b == null) {
            return a ? 1 : -1;
        } else if (a.equals(b)) {
            return a ? 1 : -1;
        } else {
            return 0;
        }
    }

    public static <T extends NFilter> T[]
    getTopLevelFilters(NFilter idFilter, Class<T> clazz, NSession session) {
        return getTopLevelFilters(idFilter).stream()
                .map(x -> NFilters.of(session).as(clazz, x))
                .toArray(value -> (T[]) Array.newInstance(clazz, value));
    }

    public static List<NFilter> getTopLevelFilters(NFilter idFilter) {
        if (idFilter == null) {
            return Collections.emptyList();
        }
        if (idFilter.getFilterOp() == NFilterOp.AND) {
            return idFilter.getSubFilters();
        }
        return Collections.singletonList(idFilter);
    }

    public static NIdFilter idFilterOf(Map<String, String> map, NIdFilter idFilter, NDescriptorFilter
            descriptorFilter, NSession session) {
        return NIdFilters.of(session).nonnull(idFilter).and(
                CoreFilterUtils.createNutsDescriptorFilter(map, session).and(descriptorFilter).to(NIdFilter.class)
        );
    }


    public static NDescriptorFilter createNutsDescriptorFilter(String arch, String os, String osDist, String
            platform, String desktopEnv, NSession session) {
        NDescriptorFilters d = NDescriptorFilters.of(session);
        return d.byArch(arch)
                .and(d.byOs(os))
                .and(d.byOsDist(osDist))
                .and(d.byPlatform(platform))
                .and(d.byDesktopEnvironment(desktopEnv))
                ;
    }

    public static NDescriptorFilter createNutsDescriptorFilter(Map<String, String> faceMap, NSession session) {
        return createNutsDescriptorFilter(
                faceMap == null ? null : faceMap.get(NConstants.IdProperties.ARCH),
                faceMap == null ? null : faceMap.get(NConstants.IdProperties.OS),
                faceMap == null ? null : faceMap.get(NConstants.IdProperties.OS_DIST),
                faceMap == null ? null : faceMap.get(NConstants.IdProperties.PLATFORM),
                faceMap == null ? null : faceMap.get(NConstants.IdProperties.DESKTOP),
                session);
    }

    public static NPredicate<NId> createFilter(NIdFilter t, NSession session) {
        if (t == null) {
            return null;
        }
        return new NIdFilterToPredicate(t, session);
    }


    public static List<NExtensionInformation> filterNutsExtensionInfoByLatestVersion
            (List<NExtensionInformation> base) {
        LinkedHashMap<String, NExtensionInformation> valid = new LinkedHashMap<>();
        for (NExtensionInformation n : base) {
            NExtensionInformation old = valid.get(n.getId().getShortName());
            if (old == null || old.getId().getVersion().compareTo(n.getId().getVersion()) < 0) {
                valid.put(n.getId().getShortName(), n);
            }
        }
        return new ArrayList<>(valid.values());
    }

    public static List<NId> filterNutsIdByLatestVersion(List<NId> base) {
        LinkedHashMap<String, NId> valid = new LinkedHashMap<>();
        for (NId n : base) {
            NId old = valid.get(n.getShortName());
            if (old == null || old.getVersion().compareTo(n.getVersion()) < 0) {
                valid.put(n.getShortName(), n);
            }
        }
        return new ArrayList<>(valid.values());
    }

    public static boolean matchesPackaging(String packaging, NDescriptor desc, NSession session) {
        if (NBlankable.isBlank(packaging)) {
            return true;
        }
        if (NBlankable.isBlank(desc.getPackaging())) {
            return true;
        }
        NId _v = NId.of(packaging).orNull();
        NId _v2 = NId.of(desc.getPackaging()).orNull();
        if (_v == null || _v2 == null) {
            return _v == _v2;
        }
        if (_v.equalsShortId(_v2)) {
            if (_v.getVersion().filter(session).acceptVersion(_v2.getVersion(), session)) {
                return true;
            }
        }
        return false;
    }

    public static boolean acceptDependency(NDependency dep, NSession session) {
        if (CoreFilterUtils.acceptCondition(dep.getCondition(), false, session)) {
            // fast reject jfx dependencies with different environment defined by classifier!
            if (dep.getGroupId().equals("org.openjfx") && dep.getArtifactId().startsWith("javafx")) {
                String c = NStringUtils.trim(dep.getClassifier());
                if (c.length() > 0) {
                    String[] a = c.split("-");
                    if (a.length > 0) {
                        NOsFamily o = NOsFamily.parse(a[0]).orNull();
                        if (o != null) {
                            if (o != NEnvs.of(session).getOsFamily()) {
                                return false;
                            }
                        }
                        if (a.length > 1) {
                            NArchFamily af = NArchFamily.parse(a[1]).orNull();
                            if (af != null) {
                                if (af != NEnvs.of(session).getArchFamily()) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static boolean matchAny(List<String> all, Function<String, Boolean> accepter) {
        boolean someFalse = false;
        if (all != null) {
            for (String s : all) {
                if (NBlankable.isBlank(s)) {
                    if (accepter.apply(s)) {
                        return true;
                    } else {
                        someFalse = true;
                    }
                }
            }
        }
        return !someFalse;
    }

    public static boolean acceptCondition(NEnvCondition envCond, NEnvCondition cond2, NSession session) {
        if (envCond == null || envCond.isBlank()) {
            return true;
        }
        if (cond2 == null || cond2.isBlank()) {
            return true;
        }
        if (!matchAny(cond2.getArch(), s -> matchesArch(s, envCond.getArch(), session))) {
            return false;
        }
        if (!matchAny(cond2.getOs(), s -> matchesOs(s, envCond.getOs(), session))) {
            return false;
        }
        if (!matchAny(cond2.getOsDist(), s -> matchesOsDist(s, envCond.getOsDist(), session))) {
            return false;
        }
        if (!matchAny(cond2.getPlatform(), s -> matchesOsDist(s, envCond.getPlatform(), session))) {
            return false;
        }
        if (!matchAny(cond2.getDesktopEnvironment(), s -> matchesOsDist(s, envCond.getDesktopEnvironment(), session))) {
            return false;
        }
        if (!matchesProperties(
                envCond.getProperties(), cond2.getProperties()
        )) {
            return false;
        }
        return true;
    }

    public static boolean acceptCondition(NEnvCondition envCond, boolean currentVMOnLy, NSession session) {
        if (envCond == null || envCond.isBlank()) {
            return true;
        }
        NEnvs env = NEnvs.of(session);
        NPlatforms platforms = NPlatforms.of(session);
        if (!matchesArch(
                env.getArchFamily().id(),
                envCond.getArch(), session
        )) {
            return false;
        }
        if (!matchesOs(
                env.getOsFamily().id(),
                envCond.getOs(), session
        )) {
            return false;
        }
        if (!matchesOsDist(
                env.getOsDist().toString(),
                envCond.getOsDist(), session
        )) {
            return false;
        }
        if (currentVMOnLy) {
            if (!matchesPlatform(
                    env.getPlatform().toString(),
                    envCond.getPlatform(), session
            )) {
                return false;
            }
        } else {
            if (!matchesPlatform(
                    platforms.findPlatforms().toList(),
                    envCond.getPlatform(), session
            )) {
                return false;
            }
        }

        if (!matchesDesktopEnvironment(
                env.getDesktopEnvironments(),
                envCond.getDesktopEnvironment(), session
        )) {
            return false;
        }
        if (!matchesProperties(
                envCond.getProperties(), session
        )) {
            return false;
        }
        return true;
    }

    private static boolean matchesProperties(Map<String, String> props, NSession session) {
        for (Map.Entry<String, String> kv : props.entrySet()) {
            if (!matchesProperty(kv.getKey(), kv.getValue(), session)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesProperties(Map<String, String> props, Map<String, String> others) {
        for (Map.Entry<String, String> kv : props.entrySet()) {
            if (!matchesProperty(kv.getKey(), kv.getValue(), x -> {
                String u = others.get(x);
                if (u != null) {
                    return u;
                }
                return System.getProperty(x);
            })) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesProperty(String k, String expected, NSession session) {
        return matchesProperty(k, expected, x -> {
            Object u = session.getProperty(x);
            if (u != null) {
                return String.valueOf(u);
            }
            return System.getProperty(x);
        });
    }

    private static boolean matchesProperty(String k, String expected, Function<String, String> session) {
        //maven always checks System Props
        String f = session.apply(k);
        if (expected == null) {
            return f != null;
        }
        expected = NStringUtils.trim(expected);
        f = NStringUtils.trim(f);
        if (expected.startsWith("!")) {
            expected = expected.substring(1).trim();
            return !expected.equals(f);
        }
        return expected.equals(f);
    }


    public static boolean matchesArch(String current, Collection<String> allConds, NSession session) {
        if (NBlankable.isBlank(current)) {
            return true;
        }
        NId currentId = NId.of(current).get(session);
        if (allConds != null && allConds.size() > 0) {
            for (String cond : allConds) {
                if (NBlankable.isBlank(cond)) {
                    return true;
                }
                NId idCond = NId.of(cond).get(session);
                NArchFamily w = NArchFamily.parse(idCond.getArtifactId()).orNull();
                if (w != null) {
                    idCond = idCond.builder().setArtifactId(w.id()).build();
                }
                if (idCond.equalsShortId(currentId)) {
                    if (idCond.getVersion().filter(session).acceptVersion(currentId.getVersion(), session)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean matchesOs(String os, Collection<String> allConds, NSession session) {
        if (NBlankable.isBlank(os)) {
            return true;
        }
        NId currentId = NId.of(os).get(session);
        if (allConds != null && allConds.size() > 0) {
            for (String cond : allConds) {
                if (NBlankable.isBlank(cond)) {
                    return true;
                }
                NId condId = NId.of(cond).get(session);
                NOsFamily w = NOsFamily.parse(condId.getArtifactId()).orNull();
                if (w != null) {
                    condId = condId.builder().setArtifactId(w.id()).build();
                }
                return condId.compatNewer().filter(session).acceptId(currentId, session);
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean matchesOsDist(String current, Collection<String> allConditions, NSession session) {
        if (NBlankable.isBlank(current)) {
            return true;
        }
        NId currentId = NId.of(current).get(session);
        if (allConditions != null && allConditions.size() > 0) {
            for (String cond : allConditions) {
                if (NBlankable.isBlank(cond)) {
                    return true;
                }
                NId condId = NId.of(cond).get(session);
                return condId.compatNewer().filter(session).acceptId(currentId, session);
            }
            return false;
        } else {
            return true;
        }

    }

    public static boolean matchesPlatform(Collection<NPlatformLocation> platforms, Collection<String> allCond, NSession session) {
        for (NPlatformLocation platform : platforms) {
            NId id = platform.getId();
            if (id != null) {
                if (matchesPlatform(id.toString(), allCond, session)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean matchesPlatform(String current, Collection<String> allConds, NSession session) {
        if (NBlankable.isBlank(current)) {
            return true;
        }
        NId currentId = NId.of(current).get(session);
        if (allConds != null && allConds.size() > 0) {
            for (String cond : allConds) {
                if (NBlankable.isBlank(cond)) {
                    return true;
                }
                NId condId = NId.of(cond).get(session);
                NPlatformFamily w = NPlatformFamily.parse(condId.getArtifactId()).orNull();
                if (w != null) {
                    condId = condId.builder().setArtifactId(w.id()).build();
                }
                return condId.compatNewer().filter(session).acceptId(currentId, session);
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean matchesDesktopEnvironment(Collection<NId> platforms, Collection<String> allConds, NSession session) {
        for (NId platform : platforms) {
            if (matchesDesktopEnvironment(platform.toString(), allConds, session)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesDesktopEnvironment(String current, Collection<String> allConds, NSession session) {
        if (NBlankable.isBlank(current)) {
            return true;
        }
        NId currentId = NId.of(current).get(session);
        if (allConds != null && allConds.size() > 0) {
            for (String cond : allConds) {
                if (NBlankable.isBlank(cond)) {
                    return true;
                }
                NId condId = NId.of(cond).get(session);
                NDesktopEnvironmentFamily w = NDesktopEnvironmentFamily.parse(condId.getArtifactId()).orNull();
                if (w != null) {
                    condId = condId.builder().setArtifactId(w.id()).build();
                }
                return condId.compatNewer().filter(session).acceptId(currentId, session);
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean matchesEnv(String arch, String os, String dist, String platform, String de, NEnvCondition
            desc, NSession session) {
        if (!matchesArch(arch, desc.getArch(), session)) {
            return false;
        }
        if (!matchesOs(os, desc.getOs(), session)) {
            return false;
        }
        if (!matchesOsDist(dist, desc.getOsDist(), session)) {
            return false;
        }
        if (!matchesPlatform(platform, desc.getPlatform(), session)) {
            return false;
        }
        if (!matchesDesktopEnvironment(de, desc.getDesktopEnvironment(), session)) {
            return false;
        }
        return true;
    }

    public static List<NDependency> filterDependencies(NId from, List<NDependency> d0, NDependencyFilter
            dependencyFilter, NSession session) {
        if (dependencyFilter == null) {
            return d0;
        }
        List<NDependency> r = new ArrayList<>(d0.size());
        for (NDependency nutsDependency : d0) {
            if (dependencyFilter.acceptDependency(from, nutsDependency, session)) {
                r.add(nutsDependency);
            }
        }
        return r;
    }

    public static boolean matchesSimpleNameStaticVersion(NId id, NId pattern) {
        if (pattern == null) {
            return id == null;
        }
        if (id == null) {
            return false;
        }
        if (pattern.getVersion().isBlank()) {
            return pattern.getShortName().equals(id.getShortName());
        }
        return pattern.getLongName().equals(id.getLongName());
    }

    public static boolean acceptClassifier(NIdLocation location, String classifier) {
        if (location == null) {
            return false;
        }
        String c0 = NStringUtils.trim(classifier);
        String c1 = NStringUtils.trim(location.getClassifier());
        return c0.equals(c1);
    }

    public static Map<String, String> toMap(NEnvCondition condition, NSession session) {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        String s = condition.getArch().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
        if (!NBlankable.isBlank(s)) {
            m.put(NConstants.IdProperties.ARCH, s);
        }
        s = condition.getOs().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
        if (!NBlankable.isBlank(s)) {
            m.put(NConstants.IdProperties.OS, s);
        }
        s = condition.getOsDist().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
        if (!NBlankable.isBlank(s)) {
            m.put(NConstants.IdProperties.OS_DIST, s);
        }
        s = String.join(",", condition.getPlatform());
        if (!NBlankable.isBlank(s)) {
            m.put(NConstants.IdProperties.PLATFORM, s);
        }
        s = condition.getDesktopEnvironment().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
        if (!NBlankable.isBlank(s)) {
            m.put(NConstants.IdProperties.DESKTOP, s);
        }
        s = condition.getProfile().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
        if (!NBlankable.isBlank(s)) {
            m.put(NConstants.IdProperties.PROFILE, s);
        }
        Map<String, String> properties = condition.getProperties();
        if (!properties.isEmpty()) {
            m.put(NConstants.IdProperties.CONDITIONAL_PROPERTIES, NStringMapFormat.COMMA_FORMAT.format(properties));
        }
        return m;
    }

    public static <T extends NFilter> T simplifyFilterOr(NSession session, Class<T> cls, T base, NFilter... all) {
        if (all.length == 0) {
            return NFilters.of(session).always(cls);
        }
        if (all.length == 1) {
            return (T) all[0].simplify();
        }
        List<T> all2 = new ArrayList<>();
        boolean updates = false;
        boolean someFalse = false;
        for (NFilter t : all) {
            T t2 = t == null ? null : (T) t.simplify();
            if (t2 != null) {
                switch (t2.getFilterOp()) {
                    case TRUE: {
                        return NFilters.of(session).always(cls);
                    }
                    case FALSE: {
                        someFalse = true;
                        break;
                    }
                    default: {
                        if (t2 != t) {
                            updates = true;
                        }
                        all2.add(t2);
                    }
                }
            } else {
                updates = true;
            }
        }
        if (all2.isEmpty()) {
            if (someFalse) {
                return NFilters.of(session).never(cls);
            }
            return NFilters.of(session).always(cls);
        }
        if (all2.size() == 1) {
            return all2.get(0);
        }
        if (!updates) {
            return base;
        }
        return NFilters.of(session).any(cls, all2.toArray((T[]) Array.newInstance(cls, 0)));
    }

    public static <T extends NFilter> T simplifyFilterAnd(NSession session, Class<T> cls, T base, NFilter... all) {
        if (all.length == 0) {
            return NFilters.of(session).always(cls);
        }
        if (all.length == 1) {
            return (T) all[0].simplify();
        }
        List<T> all2 = new ArrayList<>();
        boolean updates = false;
        for (NFilter t : all) {
            T t2 = t == null ? null : (T) t.simplify();
            if (t2 != null) {
                switch (t2.getFilterOp()) {
                    case FALSE: {
                        return NFilters.of(session).never(cls);
                    }
                    case TRUE: {
                        updates = true;
                        break;
                    }
                    default: {
                        if (t2 != t) {
                            updates = true;
                        }
                        all2.add(t2);
                    }
                }
            } else {
                updates = true;
            }
        }
        if (all2.size() == 0) {
            return NFilters.of(session).always(cls);
        }
        if (all2.size() == 1) {
            return all2.get(0);
        }
        if (!updates) {
            return base;
        }
        return NFilters.of(session).all(cls, all2.toArray((T[]) Array.newInstance(cls, 0)));
    }

    public static <T extends NFilter> T simplifyFilterNone(NSession session, Class<T> cls, T base, NFilter... all) {
        if (all.length == 0) {
            return NFilters.of(session).always(cls);
        }
        List<T> all2 = new ArrayList<>();
        boolean updates = false;
        for (NFilter t : all) {
            T t2 = t == null ? null : (T) t.simplify();
            if (t2 != null) {
                switch (t2.getFilterOp()) {
                    case TRUE: {
                        return NFilters.of(session).never(cls);
                    }
                    case FALSE: {
                        updates = true;
                        break;
                    }
                    default: {
                        if (t2 != t) {
                            updates = true;
                        }
                        all2.add(t2);
                    }
                }
            } else {
                updates = true;
            }
        }
        if (all2.size() == 0) {
            return NFilters.of(session).always(cls);
        }
        if (!updates) {
            return base;
        }
        return NFilters.of(session).none(cls, all2.toArray((T[]) Array.newInstance(cls, 0)));
    }

    public static <T> T simplify(T any) {
        if (any == null) {
            return null;
        }
        if (any instanceof Simplifiable) {
            return ((Simplifiable<T>) any).simplify();
        }
        return any;
    }

    public static <T> T[] simplifyAndShrink(Class<T> cls, T... any) {
        List<T> all = new ArrayList<>();
        boolean updates = false;
        for (T t : any) {
            T t2 = simplify(t);
            if (t2 != null) {
                if (t2 != t) {
                    updates = true;
                }
                all.add(t2);
            } else {
                updates = true;
            }
        }
        if (!updates) {
            return null;
        }
        return all.toArray((T[]) Array.newInstance(cls, 0));
    }

    public static <T extends NFilter> T[] simplifyAndShrinkFilters(Class<T> cls, Predicate<T> onRemove, T... any) {
        List<T> all = new ArrayList<>();
        boolean updates = false;
        for (T t : any) {
            T t2 = t == null ? null : (T) t.simplify();
            if (t2 != null) {
                if (onRemove != null && onRemove.test(t2)) {
                    updates = true;
                } else {
                    if (t2 != t) {
                        updates = true;
                    }
                    all.add(t2);
                }
            } else {
                updates = true;
            }
        }
        if (!updates) {
            return null;
        }
        return all.toArray((T[]) Array.newInstance(cls, 0));
    }

    private static class NIdFilterToPredicate extends NPredicates.BasePredicate<NId> {
        private final NIdFilter t;
        private final NSession session;

        public NIdFilterToPredicate(NIdFilter t, NSession session) {
            this.t = t;
            this.session = session;
        }

        @Override
        public boolean test(NId value) {
            return t.acceptId(value, session);
        }

        @Override
        public int hashCode() {
            return Objects.hash(t, session);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NIdFilterToPredicate that = (NIdFilterToPredicate) o;
            return Objects.equals(t, that.t) && Objects.equals(session, that.session);
        }

        @Override
        public String toString() {
            return t.toString();
        }
    }
}
