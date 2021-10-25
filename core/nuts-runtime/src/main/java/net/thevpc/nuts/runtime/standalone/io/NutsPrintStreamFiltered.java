package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.renderer.StripperFormattedPrintStreamRenderer;

public class NutsPrintStreamFiltered extends NutsPrintStreamRendered {
    public NutsPrintStreamFiltered(NutsPrintStreamBase base, NutsSession session, Bindings bindings) {
        super(base, session,NutsTerminalMode.FILTERED,
                new StripperFormattedPrintStreamRenderer(),
                bindings);
        if (bindings.filtered != null) {
            throw new IllegalArgumentException("already bound ansi");
        }
        bindings.filtered = this;

    }

    @Override
    public NutsPrintStream setSession(NutsSession session) {
        if(session==null || session==this.session){
            return this;
        }
        return new NutsPrintStreamFiltered(base,session,new Bindings());
    }

    @Override
    protected NutsPrintStream convertImpl(NutsTerminalMode other) {
        switch (other) {
            case FORMATTED: {
                return new NutsPrintStreamFormatted(base, getSession(),bindings);
            }
        }
        throw new NutsIllegalArgumentException(base.getSession(),NutsMessage.cstyle("unsupported %s -> %s",mode(), other));
    }

    @Override
    public NutsPrintStream run(NutsTerminalCommand command) {
        //do nothing!!
        return this;
    }
}
