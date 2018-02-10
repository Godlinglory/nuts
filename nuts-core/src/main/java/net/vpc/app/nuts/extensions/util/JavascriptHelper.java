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
package net.vpc.app.nuts.extensions.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIllegalArgumentsException;
import net.vpc.app.nuts.NutsParseException;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.extensions.filters.dependency.NutsDependencyJavascriptFilter;
import net.vpc.app.nuts.extensions.filters.id.NutsIdJavascriptFilter;
import net.vpc.app.nuts.extensions.filters.version.NutsVersionJavascriptFilter;

/**
 *
 * @author vpc
 */
public class JavascriptHelper {

    public List<Pattern> blacklistClassNamePatterns = new ArrayList<>();
    private ScriptEngine engine;

    private Set<String> blacklistClassNames = new HashSet<>(
            Arrays.asList(
                    "java.io.File",
                    "java.lang.Process",
                    "java.lang.System",
                    "java.lang.Thread"
            )
    );

    public static class NutScriptUtil {

        public boolean matches(Object value, String pattern) {
            if (value == null) {
                value = "";
            }
            if (value instanceof String) {
                if (pattern == null) {
                    pattern = "";
                }
                return value.toString().matches(CoreStringUtils.simpexpToRegexp(pattern, true));
            }
            if (CoreStringUtils.isEmpty(pattern)) {
                return CoreStringUtils.isEmpty(value.toString());
            }
            if (value instanceof NutsId) {
                return NutsIdJavascriptFilter.valueOf(pattern).accept((NutsId) value);
            }
            if (value instanceof NutsDependency) {
                return NutsDependencyJavascriptFilter.valueOf(pattern).accept((NutsDependency) value);
            }
            if (value instanceof NutsVersion) {
                return NutsVersionJavascriptFilter.valueOf(pattern).accept((NutsVersion) value);
            }
            return true;
        }

        public String trim(String s) {
            return CoreStringUtils.trim(s);
        }

        public int compareVersions(String v1, String v2) {
            return CoreVersionUtils.compareVersions(v1, v2);
        }

    }

    public JavascriptHelper(String code, String initExprs, Set<String> blacklist, Object util) {
        if (blacklist == null) {
            blacklistClassNames.addAll(Arrays.asList(
                    "java.io.File",
                    "java.lang.Process",
                    "java.lang.System",
                    "java.lang.Thread"
            ));
        } else {
            for (String s : blacklist) {
                if (s.contains("*")) {
                    blacklistClassNamePatterns.add(Pattern.compile(CoreStringUtils.simpexpToRegexp(s)));
                } else {
                    blacklistClassNames.add(s);
                }
            }
        }
        if (code == null) {
            throw new NutsIllegalArgumentsException("Illegal js filter : empty content");
        }
        if (!code.contains("return")) {
            throw new NutsIllegalArgumentsException("js filter must contain a return clause");
        }
        try {
            engine = createScriptEngine();
        } catch (Exception ex) {
            engine = createManagerJdk();
        }
        try {
            if (CoreStringUtils.isEmpty(initExprs)) {
                initExprs = "";
            }
            engine.eval("function accept(x) { " + initExprs + code + " }");
            if (util == null) {
                util = new NutScriptUtil();
            }
            engine.put("util", util);
        } catch (ScriptException e) {
            throw new NutsParseException(e);
        }
    }

    public void createEngine(String code, String initExprs) {

    }

    private ScriptEngine createScriptEngine() {
        jdk.nashorn.api.scripting.NashornScriptEngineFactory f = new jdk.nashorn.api.scripting.NashornScriptEngineFactory();
        return f.getScriptEngine(new jdk.nashorn.api.scripting.ClassFilter() {
            @Override
            public boolean exposeToScripts(String s) {
                if (blacklistClassNames.contains(s)) {
                    return false;
                }
                for (Pattern pattern : blacklistClassNamePatterns) {
                    if (pattern.matcher(s).matches()) {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    private ScriptEngine createManagerJdk() {
        ScriptEngineManager engineManager
                = new ScriptEngineManager();
        return engineManager.getEngineByName("nashorn");
    }

    public boolean accept(Object id) {
        engine.put("x", id);
        try {
            return Boolean.TRUE.equals(engine.eval("accept(x);"));
        } catch (ScriptException e) {
            throw new NutsParseException(e);
        }
    }
}
