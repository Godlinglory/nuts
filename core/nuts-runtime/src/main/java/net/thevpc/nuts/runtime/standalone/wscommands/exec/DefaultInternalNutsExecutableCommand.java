/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

/**
 *
 * @author thevpc
 */
public abstract class DefaultInternalNutsExecutableCommand extends AbstractNutsExecutableCommand {

    protected String[] args;
    private NutsSession session;

    public DefaultInternalNutsExecutableCommand(String name, String[] args, NutsSession session) {
        super(name, name, NutsExecutableType.INTERNAL);
        this.args = args;
        this.session = session;
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsId getId() {
        return null;
    }

    protected void showDefaultHelp() {
        session.out().println(getHelpText());
    }


    @Override
    public String getHelpText() {
        NutsTexts txt = NutsTexts.of(getSession());
        NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/command/" + name + ".ntf",
                txt.parser().createLoader(getClass().getClassLoader())
        );
        if(n==null){
            return "no help found for " + name;
        }
        return n.toString();
    }

    @Override
    public void dryExecute() {
        NutsSession session = getSession();
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            session.out().println("[dry] ==show-help==");
            return;
        }
        NutsTexts text = NutsTexts.of(session);
        session.out().printf("[dry] %s%n",
                text.builder()
                        .append("internal", NutsTextStyle.pale())
                        .append(" ")
                        .append(getName(),NutsTextStyle.primary5())
                        .append(" ")
                        .append(NutsCommandLine.of(args,session))
                );
    }

    @Override
    public String toString() {
        return getName()+" "+ NutsCommandLine.of(args,getSession()).toString();
    }

}
