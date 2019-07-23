/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.impl.def.commands;

import net.vpc.app.nuts.NutsExecutableType;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

/**
 *
 * @author vpc
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
        return getSession().getWorkspace().io().loadFormattedString(
                "/net/vpc/app/nuts/command/" + name + ".help",
                getClass().getClassLoader(), "no help found for " + name);
    }

    @Override
    public void dryExecute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            getSession().out().println("[dry] ==show-help==");
            return;
        }
        getSession().out().printf("[dry] ==internal== {{%s}} %s%n",getName(),String.join(" ",args));
    }

}