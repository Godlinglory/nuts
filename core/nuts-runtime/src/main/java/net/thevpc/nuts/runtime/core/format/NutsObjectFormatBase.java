/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format;

import net.thevpc.nuts.NutsObjectFormat;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsCommandLine;

/**
 *
 * @author thevpc
 */
public abstract class NutsObjectFormatBase extends DefaultFormatBase<NutsObjectFormat>
        implements NutsObjectFormat {

    private Object value;

    public NutsObjectFormatBase(NutsSession session, String name) {
        super(session, name);
    }

    @Override
    public NutsObjectFormat setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return false;
    }

//    @Override
//    public void print(PrintStream w) {
//        PrintWriter pw = new ExtendedFormatAwarePrintWriter(w);
//        print(pw);
//    }
}
