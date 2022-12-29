/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.stream;

import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;

import java.util.*;
import java.util.stream.Stream;

/**
 * @param <T> collection element type
 * @author thevpc
 */
public class NEmptyStream<T> extends AbstractNStream<T> {

    public NEmptyStream(NSession session, String nutsBase) {
        super(session, nutsBase);
    }

    @Override
    public List<T> toList() {
        return Collections.emptyList();
    }

    @Override
    public Stream<T> stream() {
        return Collections.<T>emptyList().stream();
    }

    @Override
    public NIterator<T> iterator() {
        return IteratorBuilder.emptyIterator();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NEmptyStream<?> other = (NEmptyStream<?>) obj;
        return true;
    }

    @Override
    public String toString() {
        return "NullBasedResult" + "@" + Integer.toHexString(hashCode());
    }

}
