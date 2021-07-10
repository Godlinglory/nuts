package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellFileContext;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellExecutionContext;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsCommandLine;

public interface NshExecutionContext extends JShellExecutionContext {

    @Override
    JShellFileContext getGlobalContext();

    NutsShellContext getNutsShellContext();

    NutsWorkspace getWorkspace();

    NutsSession getSession();

    boolean configureFirst(NutsCommandLine cmd);

    void configureLast(NutsCommandLine cmd);

    NutsApplicationContext getAppContext();

    @Override
    NutsJavaShell getShell();
}
