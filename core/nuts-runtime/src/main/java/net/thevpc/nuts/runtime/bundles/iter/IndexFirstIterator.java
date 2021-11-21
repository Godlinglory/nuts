/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsIndexerNotAccessibleException;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author thevpc
 */
public class IndexFirstIterator<T> extends IterInfoNodeAware2Base<T> {

    private static final Logger LOG = Logger.getLogger(IndexFirstIterator.class.getName());
    private Iterator<T> index;
    private Iterator<T> other;
    private long readFromIndex;
    private T nextItem;
    private boolean hasNextItem;

    public IndexFirstIterator(Iterator<T> index, Iterator<T> other) {
        this.index = index;
        this.other = other;
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        return info("IndexFirst", IterInfoNode.resolveOrNull("base", other, session));
    }

    @Override
    public boolean hasNext() {
        if (index != null) {
            if (readFromIndex == 0) {
                try {
                    boolean v = index.hasNext();
                    if (v) {
                        hasNextItem = true;
                        nextItem = index.next();
                        readFromIndex++;
                    }
                    return v;
                } catch (NutsIndexerNotAccessibleException ex) {
                    index = null;
                }
            } else {
                try {
                    if (index.hasNext()) {
                        return true;
                    }
                    index = null;
                } catch (NutsIndexerNotAccessibleException ex) {
                    LOG.log(Level.SEVERE, "error evaluating Iterator 'hasNext()' : " + CoreStringUtils.exceptionToString(ex), ex);
                    other = null;
                    return false;
                }
            }
        }
        if (other != null) {
            return other.hasNext();
        }
        return false;
    }

    @Override
    public T next() {
        if (hasNextItem) {
            hasNextItem = false;
            T t = nextItem;
            nextItem = null;
            return t;
        }
        if (index != null) {
            return index.next();
        }
        return other.next();
    }

    @Override
    public void remove() {
        if (index != null) {
            index.remove();
        } else if (other != null) {
            other.remove();
        }
    }

    @Override
    public String toString() {
        return "IndexFirstIterator(" + other + ")";
    }
}
