/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.format.props;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.runtime.format.DefaultSearchFormatBase;
import net.thevpc.nuts.runtime.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.format.NutsFormatUtils;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import net.thevpc.nuts.NutsOutputFormat;
import net.thevpc.nuts.NutsSession;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatProps extends DefaultSearchFormatBase {

    public DefaultSearchFormatProps(NutsSession session, PrintStream writer, NutsFetchDisplayOptions options) {
        super(session, writer, NutsOutputFormat.PROPS,options);
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
                getWorkspace().formats().element().toElement(object)
        );
        CoreIOUtils.storeProperties(p, getWriter(), false);
        getWriter().flush();
    }

}
