package net.thevpc.nuts.runtime.standalone.id;

import net.thevpc.nuts.NutsId;

import java.util.Comparator;

public class DefaultNutsIdComparator implements Comparator<NutsId> {

    public static final DefaultNutsIdComparator INSTANCE = new DefaultNutsIdComparator();

    @Override
    public int compare(NutsId o1, NutsId o2) {
        int x = o1.getShortName().compareTo(o2.getShortName());
        if (x != 0) {
            return x;
        }
        //latest versions first
        x = o1.getVersion().compareTo(o2.getVersion());
        return -x;
    }
}
