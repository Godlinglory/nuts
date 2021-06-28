package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.toolbox.nsh.bundles.jshell.DefaultJShellOptionsParser;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellOptions;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsCommandLine;

import java.util.Arrays;
import java.util.List;

public class NshOptionsParser extends DefaultJShellOptionsParser {
    private NutsApplicationContext appContext;

    public NshOptionsParser(NutsApplicationContext appContext) {
        this.appContext = appContext;
    }

    protected JShellOptions createOptions() {
        return new NshOptions();
    }

    @Override
    protected void parseUnsupportedNextArgument(List<String> args, JShellOptions options) {
        NutsCommandLine a = appContext.getWorkspace().commandLine().create(args);
        if(appContext.getSession().configureFirst(a)){
            //replace remaining...
            args.clear();
            args.addAll(Arrays.asList(a.toStringArray()));
        }else{
            super.parseUnsupportedNextArgument(args, options);
        }
    }
}
