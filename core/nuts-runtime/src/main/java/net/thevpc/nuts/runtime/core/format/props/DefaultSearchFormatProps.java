/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format.props;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsContentType;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.runtime.core.format.DefaultSearchFormatBase;
import net.thevpc.nuts.runtime.core.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.core.format.NutsFormatUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.NutsSession;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatProps extends DefaultSearchFormatBase {

    public DefaultSearchFormatProps(NutsSession session, PrintStream writer, NutsFetchDisplayOptions options) {
        super(session, writer, NutsContentType.PROPS,options);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        if (getDisplayOptions().configureFirst(cmd)) {
            return true;
        }
        return false;
    }

    @Override
    public void start() {
    }

    @Override
    public void complete(long count) {

    }

    @Override
    public void next(Object object, long index) {
        Map<String, String> p = new LinkedHashMap<>();
        NutsFormatUtils.putAllInProps(String.valueOf(index + 1), p,
                getWorkspace().formats().element().convert(object, NutsElement.class)
        );
        CoreIOUtils.storeProperties(p, getWriter(), false);
        getWriter().flush();
    }

}
