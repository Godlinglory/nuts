package net.thevpc.nuts.runtime.core.filters.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.NutsTypedFiltersParser;

public class NutsRepositoryFilterParser extends NutsTypedFiltersParser<NutsRepositoryFilter> {
    public NutsRepositoryFilterParser(String str, NutsSession session) {
        super(str,session);
    }

    @Override
    protected NutsRepositoryFilterManager getTManager() {
        return getSession().filters().repository();
    }

    protected NutsRepositoryFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }


}
