package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.util.NDescribable;
import net.thevpc.nuts.util.NPredicates;

import java.util.HashSet;

class DistinctPredicate<T> extends NPredicates.BasePredicate<T> implements NDescribable {
    private HashSet<T> visited = new HashSet<>();

    @Override
    public boolean test(T value) {
        if (visited.contains(value)) {
            return false;
        }
        visited.add(value);
        return true;
    }

    @Override
    public String toString() {
        return "Distinct";
    }

    @Override
    public NElement describe(NSession session) {
        return NElements.of(session).ofString("distinct");
    }

}
