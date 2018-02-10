/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.apps.javashell.parser.DefaultJavaShellEvalContext;
import net.vpc.apps.javashell.parser.Env;
import net.vpc.apps.javashell.parser.JavaShellEvalContext;
import net.vpc.apps.javashell.parser.nodes.Node;

import java.io.InputStream;
import java.io.OutputStream;

class NutsJavaShellEvalContext extends DefaultJavaShellEvalContext {

    private NutsCommandContext commandContext;
    private NutsWorkspace workspace;

    public NutsJavaShellEvalContext(JavaShellEvalContext parentContext) {
        super(parentContext);
        if (parentContext instanceof NutsJavaShellEvalContext) {
            this.commandContext = ((NutsJavaShellEvalContext) parentContext).commandContext;
            this.workspace = ((NutsJavaShellEvalContext) parentContext).workspace;
            this.commandContext.getUserProperties().put(JavaShellEvalContext.class.getName(), this);
        }
    }

    public NutsJavaShellEvalContext(NutsJavaShell shell, String[] args, Node root, Node parent, NutsCommandContext commandContext, NutsWorkspace workspace, Env env) {
        super(shell, env, root, parent, null, null, null, args);
        this.commandContext = commandContext;
        this.workspace = workspace;
    }

    public NutsCommandContext getCommandContext() {
        return commandContext;
    }

    @Override
    public InputStream getStdIn() {
        return commandContext.getTerminal().getIn();
    }

    @Override
    public OutputStream getStdOut() {
        return commandContext.getTerminal().getOut();
    }

    @Override
    public OutputStream getStdErr() {
        return commandContext.getTerminal().getErr();
    }

    @Override
    public JavaShellEvalContext setOut(OutputStream out) {
        commandContext.getTerminal().setOut(workspace.createEnhancedPrintStream(out));
        return this;
    }

    @Override
    public JavaShellEvalContext setIn(InputStream in) {
        commandContext.getTerminal().setIn(in);
        return this;
    }
}
