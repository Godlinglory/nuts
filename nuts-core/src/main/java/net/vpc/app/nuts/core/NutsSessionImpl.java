/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by vpc on 2/1/17.
 */
public class NutsSessionImpl implements Cloneable, NutsSession {

    private boolean transitive = true;
    private boolean indexEnabled = true;
    private NutsFetchMode fetchMode = NutsFetchMode.ONLINE;
    private NutsSessionTerminal terminal;
    private Map<String, Object> properties = new HashMap<>();
    private List<NutsListener> listeners = new ArrayList<>();

    public NutsSessionImpl() {
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NutsSession setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public NutsFetchMode getFetchMode() {
        return fetchMode;
    }

    @Override
    public NutsSession setFetchMode(NutsFetchMode fetchMode) {
        this.fetchMode = (fetchMode == null) ? NutsFetchMode.ONLINE : fetchMode;
        return this;
    }

    @Override
    public NutsSession copy() {
        try {
            NutsSessionImpl cloned = (NutsSessionImpl) clone();
            cloned.properties = properties == null ? null : new LinkedHashMap<>(properties);
            cloned.listeners = listeners == null ? null : new ArrayList<>(listeners);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new NutsUnsupportedOperationException(e);
        }
    }

    @Override
    public NutsSession addListeners(NutsListener listener) {
        if (listener != null) {
            if (listeners == null) {
                listeners = new ArrayList<>();
            }
            listeners.add(listener);
        }
        return this;
    }

    @Override
    public NutsSession removeListeners(NutsListener listener) {
        if (listener != null) {
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
        return this;
    }

    @Override
    public <T extends NutsListener> T[] getListeners(Class<T> type) {
        if (listeners != null) {
            List<NutsListener> found = new ArrayList<>();
            for (NutsListener listener : listeners) {
                if (type.isInstance(listener)) {
                    found.add(listener);
                }
            }
            return found.toArray((T[]) Array.newInstance(type,0));
        }
        return (T[]) Array.newInstance(type,0);
    }

    @Override
    public NutsListener[] getListeners() {
        if (listeners == null) {
            return new NutsListener[0];
        }
        return listeners.toArray(new NutsListener[0]);
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public NutsSession setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public Object getProperty(String key){
        return properties!=null?properties.get(key):null;
    }

    @Override
    public NutsSession setProperty(String key, Object value) {
        if(properties==null){
            if(value!=null){
                properties=new LinkedHashMap<>();
                properties.put(key,value);
            }
        }else{
            if(value!=null){
                properties.put(key,value);
            }else{
                properties.remove(key);
            }
        }
        return this;
    }

    @Override
    public boolean isIndexEnabled() {
        return indexEnabled;
    }

    @Override
    public NutsSession setIndexEnabled(boolean indexEnabled) {
        this.indexEnabled = indexEnabled;
        return this;
    }

    @Override
    public NutsSessionTerminal getTerminal() {
        return terminal;
    }

    @Override
    public NutsSession setTerminal(NutsSessionTerminal terminal) {
        this.terminal = terminal;
        return this;
    }

    @Override
    public String toString() {
        return "NutsSession(" + "transitive=" + transitive + ", fetchMode=" + fetchMode + ", properties=" + properties + '}';
    }

}
