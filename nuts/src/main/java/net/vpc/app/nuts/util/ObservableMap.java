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
package net.vpc.app.nuts.util;

import java.util.*;

/**
 * Created by vpc on 1/21/17.
 */
public class ObservableMap<K, V> extends AbstractMap<K, V> {

    private Map<K, V> base = new HashMap<>();
    private List<MapListener<K, V>> listeners;

    public void addListener(MapListener<K, V> listener) {
        if (listener != null) {
            if (listeners == null) {
                listeners = new ArrayList<>();
            }
            listeners.add(listener);
        }
    }

    public void removeListener(MapListener<K, V> listener) {
        if (listener != null) {
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    public MapListener<K, V>[] getListeners() {
        return listeners.toArray(new MapListener[listeners.size()]);
    }

    @Override
    public V put(K key, V value) {
        if (base.containsKey(key)) {
            V old = base.put(key, value);
            if (listeners != null) {
                for (MapListener<K, V> listener : listeners) {
                    listener.elementUpdated(key, value, old);
                }
            }
            return old;
        } else {
            V old = base.put(key, value);
            if (listeners != null) {
                for (MapListener<K, V> listener : listeners) {
                    listener.elementAdded(key, value);
                }
            }
            return old;
        }
    }

    @Override
    public V remove(Object key) {
        boolean found = base.containsKey(key);
        V r = base.remove(key);
        if (found && listeners != null) {
            for (MapListener<K, V> listener : listeners) {
                listener.elementRemoved((K) key, r);
            }
        }
        return r;
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        return base.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return base.containsKey(key);
    }

    @Override
    public V get(Object key) {
        return base.get(key);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> baseEntries = base.entrySet();
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                Iterator<Entry<K, V>> baseIterator = baseEntries.iterator();
                return new Iterator<Entry<K, V>>() {
                    Entry<K, V> curr;

                    @Override
                    public boolean hasNext() {
                        return baseIterator.hasNext();
                    }

                    @Override
                    public Entry<K, V> next() {
                        curr = baseIterator.next();
                        return curr;
                    }

                    @Override
                    public void remove() {
                        baseIterator.remove();
                        for (MapListener<K, V> listener : listeners) {
                            listener.elementRemoved(curr.getKey(), curr.getValue());
                        }
                    }
                };
            }

            @Override
            public int size() {
                return baseEntries.size();
            }
        };
    }
}
