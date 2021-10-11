package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.renderer.AnsiUnixTermPrintRenderer;

public class NutsPrintStreamFormatted extends NutsPrintStreamRendered {
    public NutsPrintStreamFormatted(NutsPrintStreamBase base, Bindings bindings) {
        super(base,NutsTerminalMode.FORMATTED,
                new AnsiUnixTermPrintRenderer(),
                bindings);
        if(bindings.formatted!=null){
            throw new IllegalArgumentException("formatted already bound");
        }
        bindings.formatted=this;
    }

    @Override
    public NutsPrintStream convertSession(NutsSession session) {
        if(session==null || session==this.session){
            return this;
        }
        return new NutsPrintStreamFormatted(base,new Bindings());
    }

    @Override
    protected NutsPrintStream convertImpl(NutsTerminalMode other) {
        switch (other){
            case FILTERED:{
                return new NutsPrintStreamFiltered(base,bindings);
            }
        }
        throw new NutsIllegalArgumentException(base.getSession(),NutsMessage.cstyle("unsupported %s -> %s",mode(), other));
    }

    @Override
    public NutsPrintStream run(NutsTerminalCommand command) {
        printf("%s", session.text().ofCommand(command));
        flush();
        return this;
    }
}
