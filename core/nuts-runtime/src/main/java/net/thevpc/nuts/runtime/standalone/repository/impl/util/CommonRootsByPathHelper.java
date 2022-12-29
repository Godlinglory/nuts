/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.util;

import java.util.*;

import net.thevpc.nuts.NIdFilter;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.id.filter.NIdFilterAnd;
import net.thevpc.nuts.runtime.standalone.id.filter.NIdFilterOr;
import net.thevpc.nuts.runtime.standalone.id.filter.NPatternIdFilter;

/**
 * @author thevpc
 */
public class CommonRootsByPathHelper {

    private static Set<NPath> resolveRootIdAnd(Set<NPath> a, Set<NPath> b, NSession session) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (a.isEmpty()) {
            return Collections.emptySet();
        }
        if (b.isEmpty()) {
            return Collections.emptySet();
        }
        NPath[] aa = a.toArray(new NPath[0]);
        NPath[] bb = b.toArray(new NPath[0]);
        HashSet<NPath> h = new HashSet<>();
        for (NPath path : aa) {
            for (NPath nutsPath : bb) {
                h.add(commonRoot(path, nutsPath, session));
            }
        }
        //TODO
        return compact(h);
    }

    private static Set<NPath> compact(Set<NPath> a) {
        Map<String, NPath> x = new HashMap<>();
        if (a != null) {
            for (NPath t : a) {
                String ts=pathOf(t);
                NPath o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        return new HashSet<>(x.values());
    }

    static private boolean deepOf(NPath p){
        return p.getName().equals("*");
    }
    static private String pathOf(NPath p){
        if(p.getName().equals("*")){
            p=p.getParent();
        }
        if(p==null){
            return "";
        }
        return p.toString();
    }

    private static Set<NPath> resolveRootIdOr(Set<NPath> a, Set<NPath> b) {
        Map<String, NPath> x = new HashMap<>();
        if (a != null) {
            for (NPath t : a) {
                String ts=pathOf(t);
                NPath o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        if (b != null) {
            for (NPath t : b) {
                String ts=pathOf(t);
                NPath o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        return new HashSet<>(x.values());
    }

    private static NPath commonRoot(NPath a, NPath b, NSession session) {
        boolean a_deep;
        String a_path;
        boolean b_deep;
        String b_path;
        if(a.getName().equals("*")){
            a_deep=true;
            a_path=a.getParent()==null?"":a.getParent().toString();
        }else{
            a_deep=false;
            a_path=a.toString();
        }
        if(b.getName().equals("*")){
            b_deep=true;
            b_path=b.getParent()==null?"":b.getParent().toString();
        }else{
            b_deep=false;
            b_path=a.toString();
        }
        String[] aa = a_path.split("[.]");
        String[] bb = b_path.split("[.]");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(aa.length, bb.length); i++) {
            if (aa[i].equals(bb[i])) {
                if (sb.length() > 0) {
                    sb.append(".");
                }
                sb.append(aa[i]);
            }
        }
        if(a_deep || b_deep){
            return NPath.of(sb.toString(),session).resolve("*");
        }
        return NPath.of(sb.toString(),session);
    }

    private static Set<NPath> resolveRootId(String groupId, String artifactId, String version, NSession session) {
        String g = groupId;
        if (g == null) {
            g = "";
        }
        g = g.trim();
        if (g.isEmpty() || g.equals("*")) {
            return new HashSet<>(Collections.singletonList(NPath.of("*", session)));
        }
        int i = g.indexOf("*");
        if (i >= 0) {
            g = g.substring(0, i);
            int j = g.indexOf(".");
            if (j >= 0) {
                g = g.substring(0, j);
            }
            if(g.isEmpty()){
                g="*";
            }else{
                g=g.replace('.', '/');
                if(!g.endsWith("/")){
                    g+="/";
                }
                g+="*";
            }
            return new HashSet<>(Collections.singletonList(NPath.of(g, session)));
        }
        if (artifactId.length() > 0) {
            if (!artifactId.contains("*")) {
                if (version.length() > 0 && !version.contains("*") && !version.contains("[") && !version.contains("]")) {
                    return new HashSet<>(Collections.singletonList(NPath.of(g.replace('.', '/') + "/" + artifactId + "/" + version, session)));
                } else {
                    return new HashSet<>(Collections.singletonList(NPath.of(g.replace('.', '/') + "/" + artifactId, session)));
                }
            }
        }
        return new HashSet<>(Collections.singletonList(NPath.of(g.replace('.', '/'), session)));
    }

    public static List<NPath> resolveRootPaths(NIdFilter filter, NSession session) {
        return new ArrayList<>(CommonRootsByPathHelper.resolveRootIds(filter, session));
    }

    public static Set<NPath> resolveRootIds(NIdFilter filter, NSession session) {
        Set<NPath> v = resolveRootId0(filter, session);
        if (v == null) {
            HashSet<NPath> s = new HashSet<>();
            s.add(NPath.of("*",session));
            return s;
        }
        return v;
    }

    public static Set<NPath> resolveRootId0(NIdFilter filter, NSession session) {
        if (filter == null) {
            return null;
        }
        if (filter instanceof NIdFilterAnd) {
            NIdFilterAnd f = ((NIdFilterAnd) filter);
            Set<NPath> xx = null;
            for (NIdFilter g : f.getChildren()) {
                xx = resolveRootIdAnd(xx, resolveRootId0(g, session), session);
            }
            return xx;
        }
        if (filter instanceof NIdFilterOr) {
            NIdFilterOr f = ((NIdFilterOr) filter);

            NIdFilter[] y = f.getChildren();
            if (y.length == 0) {
                return null;
            }
            Set<NPath> xx = resolveRootId0(y[0], session);
            for (int i = 1; i < y.length; i++) {
                xx = resolveRootIdOr(xx, resolveRootId0(y[i], session));
            }
            return xx;
        }
        if (filter instanceof NPatternIdFilter) {
            NPatternIdFilter f = ((NPatternIdFilter) filter);
            return resolveRootId(f.getId().getGroupId(), f.getId().getArtifactId(), f.getId().getVersion().toString(),session);
        }
        return null;
    }
}
