package net.thevpc.nuts.toolbox.ntemplate.filetemplate;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.StringUtils;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProcessCmd extends SimpleJShellBuiltin {

    private final FileTemplater fileTemplater;

    public ProcessCmd(FileTemplater fileTemplater) {
        super("process", 10, Options.class);
        this.fileTemplater = fileTemplater;
    }

    @Override
    protected boolean configureFirst(NCommandLine commandLine, JShellExecutionContext context) {
        Options o = context.getOptions();
        NSession session = context.getSession();
        if (commandLine.isNonOption(0)) {
            o.args.add(commandLine.next().flatMap(NValue::asString).get(session));
            while (commandLine.hasNext()) {
                o.args.add(commandLine.next().flatMap(NValue::asString).get(session));
            }
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NCommandLine commandLine, JShellExecutionContext context) {
        Options o = context.getOptions();
        if (o.args.size() == 0) {
            throw new NExecutionException(context.getSession(), NMsg.ofCstyle("%s : invalid arguments count", getName()), 1);
        }
        for (String pathString : o.args) {
            fileTemplater.getLog().debug("eval", getName() + "(" + StringUtils.toLiteralString(pathString) + ")");
            fileTemplater.executeRegularFile(Paths.get(pathString), null);
        }
    }

    private static class Options {
        List<String> args = new ArrayList<>();
    }
}
