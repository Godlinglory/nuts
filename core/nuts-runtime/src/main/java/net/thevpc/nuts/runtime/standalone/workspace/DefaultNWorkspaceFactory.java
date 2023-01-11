/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
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
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElementNotFoundException;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.ClassClassMap;
import net.thevpc.nuts.runtime.standalone.util.collections.ListMap;
import net.thevpc.nuts.runtime.standalone.extension.CoreServiceUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.NLogger;
import net.thevpc.nuts.util.NLoggerVerb;
import net.thevpc.nuts.util.NStringUtils;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNWorkspaceFactory implements NWorkspaceFactory {

    private final NLogger LOG;
    private final ListMap<Class, Object> instances = new ListMap<>();
    private final Map<Class, Object> singletons = new HashMap<>();
    private final Map<NId, IdCache> discoveredCacheById = new HashMap<>();
    private final Map<Class, CachedConstructor> cachedCtrls = new HashMap<>();
    private final HashMap<String, String> _alreadyLogger = new HashMap<>();
    private final NWorkspace workspace;

    public DefaultNWorkspaceFactory(NWorkspace ws) {
        this.workspace = ws;
        LOG = ((DefaultNWorkspace) ws).LOG;
    }

    @Override
    public Set<Class> discoverTypes(NId id, URL url, ClassLoader bootClassLoader, NSession session) {
        return discoverTypes(id, url, bootClassLoader, new Class[]{NComponent.class}, session);
    }

    @Override
    public Set<Class> discoverTypes(NId id, URL url, ClassLoader bootClassLoader, Class[] extensionPoints, NSession session) {
        if (!discoveredCacheById.containsKey(id)) {
            IdCache value = new IdCache(id, url, bootClassLoader, LOG, extensionPoints, session, workspace);
            discoveredCacheById.put(id, value);
            Set<Class> all = new HashSet<>();
            for (ClassClassMap m : value.classes.values()) {
                all.addAll(m.values());
            }
            return all;
        }
        return Collections.emptySet();
    }

    @Override
    public <T extends NComponent> T createSupported(Class<T> type, Object supportCriteria, boolean required, NSession session) {
        int bestSupportLevel = Integer.MIN_VALUE;
        T bestObj = null;
        NSupportLevelContext context = new NDefaultSupportLevelContext(session, supportCriteria);
        for (T t : createAll(type, session)) {
            int supportLevel = t.getSupportLevel(context);
            if (supportLevel > 0) {
                if (bestObj == null || supportLevel > bestSupportLevel) {
                    bestSupportLevel = supportLevel;
                    bestObj = t;
                }
            }
        }
        if (required && bestObj == null) {
            //at boot time some types are nor yet available, so fall back to defaults!
            throw new NExtensionNotFoundException(session, type, supportCriteria);
        }
        return bestObj;
    }

    @Override
    public <T extends NComponent> List<T> createAllSupported(Class<T> type, Object supportCriteria, NSession session) {
        List<T> list = createAll(type, session);
        class TypeAndLevel {
            final T t;
            final int lvl;

            public TypeAndLevel(T t, int lvl) {
                this.t = t;
                this.lvl = lvl;
            }
        }
        List<TypeAndLevel> r = new ArrayList<>();
        NDefaultSupportLevelContext context = new NDefaultSupportLevelContext(session, supportCriteria);
        for (Iterator<T> iterator = list.iterator(); iterator.hasNext(); ) {
            T t = iterator.next();
            int supportLevel = t.getSupportLevel(context);
            if (supportLevel <= 0) {
                iterator.remove();
            } else {
                r.add(new TypeAndLevel(t, supportLevel));
            }
        }
        return r.stream().sorted(Comparator.comparing(x -> -x.lvl)).map(x -> x.t).collect(Collectors.toList());
    }

    @Override
    public <T> List<T> createAll(Class<T> type, NSession session) {
        List<T> all = new ArrayList<T>();
        for (Object obj : instances.getAll(type)) {
            all.add((T) obj);
        }
        for (Class c : getExtensionTypes(type, session)) {
            T obj = null;
            try {
                obj = (T) resolveInstance(c, type, session);
            } catch (Exception e) {
                LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLoggerVerb.FAIL).error(e)
                        .log(NMsg.ofJ("unable to instantiate {0} for {1} : {2}", c, type, e));
            }
            if (obj != null) {
                all.add(obj);
            }
        }
        return all;
    }

    @Override
    public <T> T createFirst(Class<T> type, NSession session) {
        for (Object obj : instances.getAll(type)) {
            return (T) obj;
        }
        for (Class c : getExtensionTypes(type, session)) {
            return (T) resolveInstance(c, type, session);
        }
        return null;
    }

    @Override
    public Set<Class> getExtensionTypes(Class type, NSession session) {
        LinkedHashSet<Class> all = new LinkedHashSet<>();
        for (IdCache v : discoveredCacheById.values()) {
            all.addAll(v.getExtensionTypes(type));
        }
        return all;
    }

    @Override
    public List<Object> getExtensionObjects(Class extensionPoint) {
        return new ArrayList<>(instances.getAll(extensionPoint));
    }

    @Override
    public boolean isRegisteredType(Class extensionPoint, String implementation, NSession session) {
        return findRegisteredType(extensionPoint, implementation, session) != null;
    }

    @Override
    public boolean isRegisteredInstance(Class extensionPoint, Object implementation, NSession session) {
        return instances.contains(extensionPoint, implementation);
    }

    @Override
    public <T> void registerInstance(Class<T> extensionPoint, T implementation, NSession session) {
        checkSession(session);
        if (isRegisteredInstance(extensionPoint, implementation, session)) {
            throw new NIllegalArgumentException(session, NMsg.ofC("already registered Extension %s for %s", implementation, extensionPoint.getName()));
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLoggerVerb.ADD)
                    .log(NMsg.ofJ("bind    {0} for impl instance {1}", NStringUtils.formatAlign(extensionPoint.getSimpleName(), 40, NPositionType.FIRST),
                            implementation.getClass().getName()));
        }
        instances.add(extensionPoint, implementation);
    }

    @Override
    public void registerType(Class extensionPoint, Class implementation, NId source, NSession session) {
        checkSession(session);
        if (isRegisteredType(extensionPoint, implementation.getName(), session)) {
            throw new NIllegalArgumentException(session, NMsg.ofC("already registered Extension %s for %s", implementation.getName(), extensionPoint.getName()));
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLoggerVerb.ADD)
                    .log(NMsg.ofJ("bind    {0} for impl type {1}", NStringUtils.formatAlign(extensionPoint.getSimpleName(), 40, NPositionType.FIRST),
                            implementation.getName()));
        }
        IdCache t = discoveredCacheById.get(source);
        if (t == null) {
            t = new IdCache(source, workspace);
            discoveredCacheById.put(source, t);
        }
        t.add(NComponent.class, implementation);
    }

    @Override
    public boolean isRegisteredType(Class extensionPoint, Class implementation, NSession session) {
        return getExtensionTypes(extensionPoint, session).contains(implementation);
    }

    public Class findRegisteredType(Class extensionPoint, String implementation, NSession session) {
        for (Class cls : getExtensionTypes(extensionPoint, session)) {
            if (cls.getName().equals(implementation)) {
                return cls;
            }
        }
        return null;
    }

    private void checkSession(NSession session) {
        NSessionUtils.checkSession(workspace, session);
    }

    private Object resolveClassSource(Class implementation) {
        return null;
    }

    protected <T> CachedConstructor<T> getCtrl0(Class<T> t, NSession session) {
        CachedConstructor o = cachedCtrls.get(t);
        if (o != null) {
            return o;
        }
        try {
            Constructor<T> ctrl = t.getDeclaredConstructor(NSession.class);
            ctrl.setAccessible(true);
            CachedConstructor<T> r = new CachedConstructor<T>() {
                @Override
                public Constructor<T> ctrl() {
                    return ctrl;
                }

                @Override
                public Object[] args(NSession session) {
                    return new Object[]{session};
                }
            };
            cachedCtrls.put(t, r);
            return r;
        } catch (NoSuchMethodException e) {
            //
        }
        try {
            Constructor<T> ctrl = t.getDeclaredConstructor(NWorkspace.class);
            ctrl.setAccessible(true);
            CachedConstructor<T> r = new CachedConstructor<T>() {
                @Override
                public Constructor<T> ctrl() {
                    return ctrl;
                }

                @Override
                public Object[] args(NSession session) {
                    return new Object[]{session.getWorkspace()};
                }
            };
            cachedCtrls.put(t, r);
            return r;
        } catch (NoSuchMethodException e) {
            //
        }
        try {
            Constructor<T> ctrl = t.getDeclaredConstructor();
            ctrl.setAccessible(true);
            CachedConstructor<T> r = new CachedConstructor<T>() {
                @Override
                public Constructor<T> ctrl() {
                    return ctrl;
                }

                @Override
                public Object[] args(NSession session) {
                    return new Object[0];
                }
            };
            cachedCtrls.put(t, r);
            return r;
        } catch (NoSuchMethodException e) {
            //
        }
        return null;
    }

    protected <T> T instantiate0(Class<T> t, NSession session, Class apiType) {
        checkSession(session);
        T theInstance = null;
        CachedConstructor<T> ctrl = getCtrl0(t, session);
        if (ctrl == null) {
            throw new NFactoryException(session, NMsg.ofC("instantiate '%s' failed. missing constructor", t));
        }
        try {
            theInstance = ctrl.ctrl().newInstance(ctrl.args(session));
        } catch (InstantiationException | InvocationTargetException e) {
            if (isBootstrapLogType(apiType)) {
                //do not use log. this is a bug that must be resolved fast!
                safeLog(NMsg.ofC("unable to instantiate %s as %s", apiType, t), e, session);
            } else {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLoggerVerb.FAIL).error(e)
                            .log(NMsg.ofJ("unable to instantiate {0}", t));
                }
            }
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new NFactoryException(session, NMsg.ofC("instantiate '%s' failed", t), cause);
        } catch (IllegalAccessException e) {
            if (isBootstrapLogType(apiType)) {
                //do not use log. this is a bug that must be resolved fast!
                safeLog(NMsg.ofC("unable to instantiate %s as %s", apiType, t), e, session);
            } else {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLoggerVerb.FAIL).error(e)
                            .log(NMsg.ofJ("unable to instantiate {0}", t));
                }
            }
            throw new NFactoryException(session, NMsg.ofC("instantiate '%s' failed", t), e);
        }
        //initialize?
        return theInstance;
    }

    protected <T> T instantiate0(Class<T> t, Class[] argTypes, Object[] args, Class apiType, NSession session) {
        checkSession(session);
        T t1 = null;
        try {
            t1 = t.getConstructor(argTypes).newInstance(args);
        } catch (InstantiationException e) {
            if (isBootstrapLogType(apiType)) {
                //do not use log. this is a bug that must be resolved fast!
                safeLog(NMsg.ofC("unable to instantiate %s as %s", apiType, t), e, session);
            } else {

                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLoggerVerb.FAIL).error(e)
                            .log(NMsg.ofJ("unable to instantiate {0}", t));
                }
            }
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new NFactoryException(session, NMsg.ofC("instantiate '%s' failed", t), cause);
        } catch (Exception e) {
            if (isBootstrapLogType(apiType)) {
                //do not use log. this is a bug that must be resolved fast!
                safeLog(NMsg.ofC("unable to instantiate %s as %s", apiType, t), e, session);
            } else {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLoggerVerb.FAIL).error(e)
                            .log(NMsg.ofJ("unable to instantiate {0}", t));
                }
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new NFactoryException(session, NMsg.ofC("instantiate '%s' failed", t), e);
        }
        //initialize?
        return t1;
    }

    protected <T> T resolveInstance(Class<T> implType, Class<T> apiType, NSession session) {
        if (implType == null) {
            return null;
        }
        NComponentScopeType scope = computeScope(implType, apiType, session);
        switch (scope) {
            case WORKSPACE: {
                Object o = singletons.get(implType);
                if (o == null) {
                    o = instantiate0(implType, session, apiType);
                    if (o != null) {
                        singletons.put(implType, o);
                        doLogInstantiation(apiType, o.getClass(), "singleton", session);
                    }
                }
                return (T) o;
            }
            case SESSION: {
                //the same class wont be create twice for this session!
                String key = "session-scoped:" + Integer.toHexString(System.identityHashCode(session)).toUpperCase() + ":" + implType.getName();
                Object o = session.getProperty(key);
                if (o == null) {
                    o = instantiate0(implType, session, apiType);
                    if (o != null) {
                        session.setProperty(key, o);
                        doLogInstantiation(apiType, o.getClass(), "session", session);
                    }
                }
                return (T) o;
            }
            default: {
                T o = instantiate0(implType, session, apiType);
                if (o != null) {
                    doLogInstantiation(apiType, o.getClass(), "prototype", session);
                }
                return o;
            }
        }
    }

    private <T> NComponentScopeType computeScope(Class<T> implType, Class<T> apiType, NSession session) {
        NComponentScope apiScope = apiType.getAnnotation(NComponentScope.class);
        NComponentScope implScope = implType.getAnnotation(NComponentScope.class);
        NComponentScopeType scope = NComponentScopeType.PROTOTYPE;
        if (apiScope != null || implScope != null) {
            if (apiScope != null && implScope == null) {
                scope = apiScope.value();
            } else if (apiScope == null && implScope != null) {
                scope = implScope.value();
            } else {
                if (apiScope.value() == implScope.value()) {
                    scope = apiScope.value();
                } else {
                    //bo defined! stick with api!
                    scope = apiScope.value();
                    if (LOG.isLoggable(Level.CONFIG)) {
                        switch (apiType.getName()) {
                            //skip logging for NutsTexts to avoid infinite recursion
                            case "net.thevpc.nuts.text.NTexts": {
                                break;
                            }
                            default: {
                                LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLoggerVerb.FAIL)
                                        .log(NMsg.ofJ("invalid scope {0} ; expected {1} for  {2}",
                                                implScope.value(),
                                                apiScope.value(),
                                                implType.getName()
                                        ));
                            }
                        }
                    }
                }
            }
        }
        return scope;
    }

    public void safeLog(NMsg msg, Throwable any, NSession session) {
        //TODO: should we use boot stdio?
        PrintStream err = NWorkspaceExt.of(session).getModel().bootModel.getBootTerminal().getErr();
        if (err == null) {
            err = System.err;
        }
        err.println(msg.toString() + ":");
        any.printStackTrace();
    }

    public boolean isBootstrapLogType(Class apiType) {
        switch (apiType.getName()) {
            //skip logging this to avoid infinite recursion
            case "net.thevpc.nuts.spi.NPaths":
            case "net.thevpc.nuts.text.NTexts":
            case "net.thevpc.nuts.spi.NLogManager":
            case "net.thevpc.nuts.util.NLogger":
            case "net.thevpc.nuts.util.NLoggerOp": {
                return true;
            }
        }
        return false;
    }

    private void doLogInstantiation(Class baseType, Class implType, String scope, NSession session) {
        //skip logging this to avoid infinite recursion
        if (isBootstrapLogType(baseType)) {
            return;
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            String old = _alreadyLogger.get(baseType.getName());
            if (old == null || !old.equals(implType.getName())) {
                _alreadyLogger.put(baseType.getName(), implType.getName());
                LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLoggerVerb.READ)
                        .log(NMsg.ofJ("resolve {0} to  ```underlined {1}``` {2}",
                                NStringUtils.formatAlign(baseType.getSimpleName(), 40, NPositionType.FIRST),
                                scope,
                                implType.getName()
                        ));
            }
        }
    }

    private NSession validLogSession(NSession session) {
        if (session == null) {
            //this is a bug
            return NSessionUtils.defaultSession(workspace);
        }
        if (session.getTerminal() == null) {
            //chances are that we are creating the session or the session's Terminal
            return NSessionUtils.defaultSession(workspace);
        }
        return session;
    }

    protected <T> T resolveInstance(Class<T> type, Class<T> apiType, Class[] argTypes, Object[] args, NSession session) {
        checkSession(session);
        if (type == null) {
            return null;
        }
        Boolean singleton = null;
        if (apiType.getAnnotation(NSingleton.class) != null) {
            singleton = true;
        } else if (apiType.getAnnotation(NPrototype.class) != null) {
            singleton = false;
        }
        if (type.getAnnotation(NSingleton.class) != null) {
            singleton = true;
        } else if (type.getAnnotation(NPrototype.class) != null) {
            singleton = false;
        }
        if (singleton == null) {
            singleton = false;
        }
        if (singleton) {
            if (argTypes.length > 0) {
                throw new NIllegalArgumentException(session, NMsg.ofPlain("singletons should have no arg types"));
            }
            Object o = singletons.get(type);
            if (o == null) {
                o = instantiate0(type, session, apiType);
                singletons.put(type, o);
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLoggerVerb.READ)
                            .log(NMsg.ofJ("resolve {0} to  ```underlined singleton``` {1}", NStringUtils.formatAlign(apiType.getSimpleName(), 40, NPositionType.FIRST), o.getClass().getName()));
                }
            }
            return (T) o;
        } else {
            T o = instantiate0(type, argTypes, args, apiType, session);
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLoggerVerb.READ)
                        .log(NMsg.ofJ("resolve {0} to  ```underlined prototype``` {1}", NStringUtils.formatAlign(apiType.getSimpleName(), 40, NPositionType.FIRST), o.getClass().getName()));
            }
            return o;
        }
    }

    //    @Override
    public <T> T create(Class<T> type, NSession session) {
        checkSession(session);
        Object one = instances.getOne(type);
        if (one != null) {
            //if static instance found, always return it!
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.with().session(validLogSession(session)).level(Level.FINEST).verb(NLoggerVerb.READ)
                        .log(NMsg.ofJ("resolve {0} to singleton {1}", NStringUtils.formatAlign(type.getSimpleName(), 40, NPositionType.FIRST), one.getClass().getName()));
            }
            return (T) one;
        }
        Set<Class> extensionTypes = getExtensionTypes(type, session);
        for (Class e : extensionTypes) {
            return (T) resolveInstance(e, type, session);
        }
        for (Class<T> t : extensionTypes) {
            return instantiate0(t, session, type);
        }
        throw new NElementNotFoundException(session, NMsg.ofC("type %s not found", type));
    }

    public <T> List<T> createAll(Class<T> type, Class[] argTypes, Object[] args, NSession session) {
        List<T> all = new ArrayList<T>();
        for (Class c : getExtensionTypes(type, session)) {
            T obj = null;
            try {
                obj = (T) resolveInstance(c, type, argTypes, args, session);
            } catch (Exception e) {
                LOG.with().session(validLogSession(session)).level(Level.WARNING).verb(NLoggerVerb.FAIL).error(e)
                        .log(NMsg.ofJ("unable to instantiate {0} for {1} : {2}", c, type, e));
            }
            if (obj != null) {
                all.add(obj);
            }
        }
        return all;
    }

    private interface CachedConstructor<T> {
        Constructor<T> ctrl();

        Object[] args(NSession session);
    }

    private final static class ClassExtension {

        Class clazz;
        Object source;
        boolean enabled = true;

        public ClassExtension(Class clazz, Object source, boolean enabled) {
            this.clazz = clazz;
            this.source = source;
            this.enabled = enabled;
        }
    }

    private static class IdCache {

        private final NId id;
        private final Map<Class, ClassClassMap> classes = new HashMap<>();
        private final Map<Class, Set<Class>> cacheExtensionTypes = new HashMap<>();
        private final NWorkspace workspace;
        private URL url;

        public IdCache(NId id, NWorkspace workspace) {
            this.id = id;
            this.workspace = workspace;
        }

        public IdCache(NId id, URL url, ClassLoader bootClassLoader, NLogger LOG, Class[] extensionPoints, NSession session, NWorkspace workspace) {
            this.id = id;
            this.url = url;
            this.workspace = workspace;
            for (Class extensionPoint : extensionPoints) {
                ClassClassMap cc = new ClassClassMap();
                classes.put(extensionPoint, cc);
                Class<NComponent> serviceClass = NComponent.class;
                for (String n : CoreServiceUtils.loadZipServiceClassNames(url, serviceClass, session)) {
                    Class<?> c = null;
                    try {
                        c = Class.forName(n, false, bootClassLoader);
                    } catch (ClassNotFoundException x) {
                        LOG.with().session(validLogSession(session)).verb(NLoggerVerb.WARNING).level(Level.FINE).error(x)
                                .log(NMsg.ofJ("not a valid type {0}", c));
                    }
                    if (c != null) {
                        if (!serviceClass.isAssignableFrom(c)) {
                            LOG.with().session(validLogSession(session)).verb(NLoggerVerb.WARNING).level(Level.FINE)
                                    .log(NMsg.ofJ("not a valid type {0} <> {1}, ignore...", c, serviceClass));
                        } else {
                            cc.add(c);
                        }
                    }
                }
//                int size=cc.size();
            }
        }

        private NSession validLogSession(NSession session) {
            if (session == null) {
                //this is a bug
                return NSessionUtils.defaultSession(workspace);
            }
            if (session.getTerminal() == null) {
                //chances are that we are creating the session or the session's Terminal
                return NSessionUtils.defaultSession(workspace);
            }
            return session;
        }


        private void add(Class extensionPoint, Class implementation) {
            ClassClassMap y = getClassClassMap(extensionPoint, true);
            if (!y.containsExactKey(implementation)) {
                y.add(implementation);
                invalidateCache();
            }
        }

        private void invalidateCache() {
            cacheExtensionTypes.clear();
        }

        private ClassClassMap getClassClassMap(Class extensionPoint, boolean create) {
            ClassClassMap r = classes.get(extensionPoint);
            if (r == null && create) {
                r = new ClassClassMap();
                classes.put(extensionPoint, r);
            }
            return r;
        }

        public NId getId() {
            return id;
        }

        public URL getUrl() {
            return url;
        }

        public Set<Class> getExtensionPoints() {
            return new LinkedHashSet<>(classes.keySet());
        }

        public Set<Class> getExtensionTypes(Class extensionPoint) {
            Set<Class> r = cacheExtensionTypes.get(extensionPoint);
            if (r != null) {
                return r;
            }
            LinkedHashSet<Class> all = new LinkedHashSet<>();
            for (Map.Entry<Class, ClassClassMap> rr : this.classes.entrySet()) {
                if (rr.getKey().isAssignableFrom(extensionPoint)) {
                    all.addAll(Arrays.asList(rr.getValue().getAll(extensionPoint)));
                }
            }
            cacheExtensionTypes.put(extensionPoint, r = Collections.unmodifiableSet(all));
            return r;
        }
    }

}
