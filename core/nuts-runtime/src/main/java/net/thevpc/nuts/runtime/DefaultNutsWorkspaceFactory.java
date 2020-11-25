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
package net.thevpc.nuts.runtime;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceFactory;
import net.thevpc.nuts.runtime.main.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.log.NutsLogVerb;
import net.thevpc.nuts.runtime.util.common.ClassClassMap;
import net.thevpc.nuts.runtime.util.common.CoreCommonUtils;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.common.ListMap;

import java.lang.reflect.Modifier;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsWorkspaceFactory implements NutsWorkspaceFactory {

    private final NutsLogger LOG;
    private final static class ClassExtension{
        Class clazz;
        Object source;
        boolean enabled=true;

        public ClassExtension(Class clazz, Object source, boolean enabled) {
            this.clazz = clazz;
            this.source = source;
            this.enabled = enabled;
        }
    }

    private final ListMap<Class, ClassExtension> classes = new ListMap<>();
    private final ListMap<Class, Object> instances = new ListMap<>();
    private final Map<Class, Object> singletons = new HashMap<>();
    private final Map<ClassLoader, List<Class>> discoveredCacheByLoader = new HashMap<>();
    private final Map<URL, List<Class>> discoveredCacheByURL = new HashMap<>();
    private final Map<NutsId, List<Class>> discoveredCacheById = new HashMap<>();
    private final ClassClassMap discoveredCacheByClass = new ClassClassMap();
    private NutsWorkspace workspace;

    public DefaultNutsWorkspaceFactory(NutsWorkspace ws) {
        this.workspace = ws;
        LOG= ((DefaultNutsWorkspace)ws).LOG;
    }

    @Override
    public List<Class> discoverTypes(ClassLoader bootClassLoader) {
        List<Class> types = discoveredCacheByLoader.get(bootClassLoader);
        if (types == null) {
            types = CoreCommonUtils.loadServiceClasses(NutsComponent.class, bootClassLoader);
            discoveredCacheByLoader.put(bootClassLoader, types);
            for (Iterator<Class> it = types.iterator(); it.hasNext();) {
                Class type = it.next();
                if (!discoveredCacheByClass.containsExactKey(type)) {
                    if (type.isInterface()
                            || (type.getModifiers() & Modifier.ABSTRACT) != 0) {
                        LOG.with().level(Level.WARNING).verb( NutsLogVerb.WARNING).formatted()
                        .log("abstract type {0} is defined as implementation. Ignored.", type.getName());
                        it.remove();
                    } else {
                        discoveredCacheByClass.add(type);
                    }
                }
            }
        }
        return Collections.unmodifiableList(types);
    }

    @Override
    public List<Class> getImplementationTypes(Class type) {
        return Arrays.asList(discoveredCacheByClass.getAll(type));
    }

//    @Override
//    public <T> List<T> discoverInstances(Class<T> type) {
//        List<Class> types = discoverTypes(type, bootClassLoader);
//        List<T> valid = new ArrayList<>();
//        for (Class t : types) {
//            valid.add((T) instantiate0(t));
//        }
//        return valid;
//    }
    @Override
    public boolean isRegisteredInstance(Class extensionPoint, Object implementation) {
        return instances.contains(extensionPoint, implementation);
    }

    @Override
    public boolean isRegisteredType(Class extensionPoint, Class implementation) {
        for (ClassExtension cls : classes.getAll(extensionPoint)) {
            if (cls.clazz.equals(implementation)) {
                return cls.enabled;
            }
        }
        return false;
    }

    public Class findRegisteredType(Class extensionPoint, String implementation) {
        for (ClassExtension cls : classes.getAll(extensionPoint)) {
            if (cls.clazz.getName().equals(implementation)) {
                return cls.clazz;
            }
        }
        return null;
    }

    @Override
    public boolean isRegisteredType(Class extensionPoint, String implementation) {
        return findRegisteredType(extensionPoint, implementation) != null;
    }

    @Override
    public <T> void registerInstance(Class<T> extensionPoint, T implementation) {
        if (isRegisteredInstance(extensionPoint, implementation)) {
            throw new NutsIllegalArgumentException(workspace, "Already Registered Extension " + implementation + " for " + extensionPoint.getName());
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.with().level(Level.FINEST).verb( NutsLogVerb.UPDATE).formatted()
            .log("bind    {0} for __impl instance__ {1}", CoreStringUtils.alignLeft(extensionPoint.getSimpleName(), 40), implementation.getClass().getName());
        }
        instances.add(extensionPoint, implementation);
    }

    @Override
    public Set<Class> getExtensionPoints() {
        return new HashSet<>(classes.keySet());
    }

    @Override
    public Set<Class> getExtensionTypes(Class extensionPoint) {
        return
                classes.getAll(extensionPoint).stream().map(x->x.clazz).collect(Collectors.toSet())
        ;
    }

    @Override
    public List<Object> getExtensionObjects(Class extensionPoint) {
        return new ArrayList<>(instances.getAll(extensionPoint));
    }

    private Object resolveClassSource(Class implementation){
        return null;
    }

    @Override
    public void registerType(Class extensionPoint, Class implementation) {
        if (isRegisteredType(extensionPoint, implementation.getName())) {
            throw new NutsIllegalArgumentException(workspace, "Already Registered Extension " + implementation.getName() + " for " + extensionPoint.getName());
        }
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.with().level(Level.FINEST).verb( NutsLogVerb.UPDATE).formatted()
            .log("bind    {0} for __impl type__ {1}", CoreStringUtils.alignLeft(extensionPoint.getSimpleName(), 40), implementation.getName());
        }
        classes.add(extensionPoint, new ClassExtension(
                implementation,
                resolveClassSource(implementation),
                true
        ));
    }

    public void unregisterType(Class extensionPoint, Class implementation) {
        Class registered = findRegisteredType(extensionPoint, implementation.getName());
        if (registered != null) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.UPDATE).formatted()
                .log("unbind  {0} for __impl type__ {1}", extensionPoint, registered.getName());
            }
            ClassExtension found = classes.getAll(extensionPoint).stream().filter(x->x.clazz.equals(registered)).findFirst().orElse(null);
            if(found!=null) {
                classes.remove(extensionPoint, found);
            }
        }
    }

    public void unregisterType(Class extensionPoint, String implementation) {
        Class registered = findRegisteredType(extensionPoint, implementation);
        if (registered != null) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.UPDATE).formatted()
                .log("unbind  Unregistering {0} for __impl type__ {1}", extensionPoint, registered.getName());
            }
            ClassExtension found = classes.getAll(extensionPoint).stream().filter(x->x.clazz.equals(registered)).findFirst().orElse(null);
            if(found!=null) {
                classes.remove(extensionPoint, found);
            }
        }
    }

    protected <T> T instantiate0(Class<T> t) {
        T theInstance = null;
        try {
            theInstance = t.newInstance();
        } catch (InstantiationException e) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.FAIL).formatted().error(e)
                .log("unable to instantiate {0}", t);
            }
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new NutsFactoryException(workspace, cause);
        } catch (IllegalAccessException e) {
            throw new NutsFactoryException(workspace, e);
        }
        //initialize?
        return theInstance;
    }

    protected <T> T instantiate0(Class<T> t, Class[] argTypes, Object[] args) {
        T t1 = null;
        try {
            t1 = t.getConstructor(argTypes).newInstance(args);
        } catch (InstantiationException e) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.FAIL).formatted().error(e)
                .log( "unable to instantiate {0}" , t);
            }
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new NutsFactoryException(workspace, cause);
        } catch (Exception e) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.FAIL).formatted().error(e)
                .log( "unable to instantiate {0}",t);
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new NutsFactoryException(workspace, e);
        }
        //initialize?
        return t1;
    }

    protected <T> T resolveInstance(Class<T> type, Class<T> baseType) {
        if (type == null) {
            return null;
        }
        Boolean singleton = null;
        if (baseType.getAnnotation(NutsSingleton.class) != null) {
            singleton = true;
        } else if (baseType.getAnnotation(NutsPrototype.class) != null) {
            singleton = false;
        }
        if (type.getAnnotation(NutsSingleton.class) != null) {
            singleton = true;
        } else if (type.getAnnotation(NutsPrototype.class) != null) {
            singleton = false;
        }
        if (singleton == null) {
            singleton = false;
        }
        if (singleton) {
            Object o = singletons.get(type);
            if (o == null) {
                o = instantiate0(type);
                singletons.put(type, o);
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().level(Level.FINEST).verb( NutsLogVerb.READ).formatted()
                            .log("resolve {0} to  __singleton__ {1}", CoreStringUtils.alignLeft(baseType.getSimpleName(), 40), o.getClass().getName());
                }
            }
            return (T) o;
        } else {
            T o = instantiate0(type);
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.READ).formatted()
                .log("resolve {0} to  __prototype__ {1}", CoreStringUtils.alignLeft(baseType.getSimpleName(), 40), o.getClass().getName());
            }
            return o;
        }
    }

    protected <T> T resolveInstance(Class<T> type, Class<T> baseType, Class[] argTypes, Object[] args) {
        if (type == null) {
            return null;
        }
        Boolean singleton = null;
        if (baseType.getAnnotation(NutsSingleton.class) != null) {
            singleton = true;
        } else if (baseType.getAnnotation(NutsPrototype.class) != null) {
            singleton = false;
        }
        if (type.getAnnotation(NutsSingleton.class) != null) {
            singleton = true;
        } else if (type.getAnnotation(NutsPrototype.class) != null) {
            singleton = false;
        }
        if (singleton == null) {
            singleton = false;
        }
        if (singleton) {
            if (argTypes.length > 0) {
                throw new NutsIllegalArgumentException(workspace, "Singletons should have no arg types");
            }
            Object o = singletons.get(type);
            if (o == null) {
                o = instantiate0(type);
                singletons.put(type, o);
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.with().level(Level.FINEST).verb( NutsLogVerb.READ).formatted()
                    .log("resolve {0} to  __singleton__ {1}", CoreStringUtils.alignLeft(baseType.getSimpleName(), 40), o.getClass().getName());
                }
            }
            return (T) o;
        } else {
            T o = instantiate0(type, argTypes, args);
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.READ).formatted()
                .log("resolve {0} to  __prototype__ {1}", CoreStringUtils.alignLeft(baseType.getSimpleName(), 40), o.getClass().getName());
            }
            return o;
        }
    }

    //    @Override
    public <T> T create(Class<T> type) {
        Object one = instances.getOne(type);
        if (one != null) {
            //if static instance found, always return it!
            if (LOG.isLoggable(Level.CONFIG)) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.READ).formatted()
                .log("resolve {0} to singleton {1}", CoreStringUtils.alignLeft(type.getSimpleName(), 40), one.getClass().getName());
            }
            return (T) one;
        }
        for (ClassExtension e : classes.getAll(type)) {
            if(e.enabled){
                return (T) resolveInstance(e.clazz, type);
            }
        }
        for (Class<T> t : getImplementationTypes(type)) {
            return (T) instantiate0(t);
        }
        throw new NutsElementNotFoundException(workspace, "Type " + type + " not found");
    }

    @Override
    public <T> List<T> createAll(Class<T> type) {
        List<T> all = new ArrayList<T>();
        for (Object obj : instances.getAll(type)) {
            all.add((T) obj);
        }
        LinkedHashSet<Class> allTypes = new LinkedHashSet<>();
        allTypes.addAll(classes.getAll(type).stream().filter(x->x.enabled).map(x->x.clazz).collect(Collectors.toList()));
        allTypes.addAll(getImplementationTypes(type));
        for (Class c : allTypes) {
            T obj = null;
            try {
                obj = (T) resolveInstance(c, type);
            } catch (Exception e) {
                LOG.with().level(Level.FINEST).verb( NutsLogVerb.FAIL).formatted().error(e)
                .log( "unable to instantiate {0} for {1} : {2}" ,c,type, CoreStringUtils.exceptionToString(e));
            }
            if (obj != null) {
                all.add(obj);
            }
        }
        return all;
    }

    public <T> List<T> createAll(Class<T> type, Class[] argTypes, Object[] args) {
        List<T> all = new ArrayList<T>();
        for (ClassExtension cc : classes.getAll(type)) {
            if(cc.enabled) {
                Class c=cc.clazz;
                T obj = null;
                try {
                    obj = (T) resolveInstance(c, type, argTypes, args);
                } catch (Exception e) {
                    LOG.with().level(Level.WARNING).verb(NutsLogVerb.FAIL).formatted().error(e)
                            .log("unable to instantiate {0} for {1} : {2}", c, type, CoreStringUtils.exceptionToString(e));
                }
                if (obj != null) {
                    all.add(obj);
                }
            }
        }
//        ServiceLoader serviceLoader = ServiceLoader.load(type);
//        for (Object object : serviceLoader) {
//            all.add((T) object);
//        }
        return all;
    }

    @Override
    public <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters) {
        List<T> list = createAll(type, constructorParameterTypes, constructorParameters);
        int bestSupportLevel = Integer.MIN_VALUE;
        NutsSupportLevelContext<V> lc=new DefaultNutsSupportLevelContext<V>(workspace,supportCriteria);
        T bestObj = null;
        for (T t : list) {
            int supportLevel = t.getSupportLevel(lc);
            if (supportLevel > 0) {
                if (bestObj == null || supportLevel > bestSupportLevel) {
                    bestSupportLevel = supportLevel;
                    bestObj = t;
                }
            }
        }
//        if(bestObj==null){
//            throw new NutsElementNotFoundException("Not Found implementation for "+type.getName());
//        }
//        if(bestObj==null){
//            throw new NutsElementNotFoundException(workspace,"Missing Implementation for Extension Point "+type);
//        }
        return bestObj;
    }

    @Override
    public <T extends NutsComponent<V>, V> T createSupported(Class<T> type, V supportCriteria) {
        List<T> list = createAll(type);
        int bestSupportLevel = Integer.MIN_VALUE;
        T bestObj = null;
        DefaultNutsSupportLevelContext<V> context = new DefaultNutsSupportLevelContext<>(workspace, supportCriteria);
        for (T t : list) {
            int supportLevel = t.getSupportLevel(context);
            if (supportLevel > 0) {
                if (bestObj == null || supportLevel > bestSupportLevel) {
                    bestSupportLevel = supportLevel;
                    bestObj = t;
                }
            }
        }
//        if(bestObj==null){
//            throw new NutsElementNotFoundException("Not Found implementation for "+type.getName());
//        }
//        if(bestObj==null){
//            throw new NutsElementNotFoundException(workspace,"Missing Implementation for Extension Point "+type);
//        }
        return bestObj;
    }

    @Override
    public <T extends NutsComponent<V>, V> List<T> createAllSupported(Class<T> type, V supportCriteria) {
        List<T> list = createAll(type);
        DefaultNutsSupportLevelContext<V> context = new DefaultNutsSupportLevelContext<>(workspace, supportCriteria);
        for (Iterator<T> iterator = list.iterator(); iterator.hasNext();) {
            T t = iterator.next();
            int supportLevel = t.getSupportLevel(context);
            if (supportLevel <= 0) {
                iterator.remove();
            }
        }
        return list;
    }

}
