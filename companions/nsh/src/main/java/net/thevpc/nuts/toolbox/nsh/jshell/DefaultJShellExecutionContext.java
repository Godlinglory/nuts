package net.thevpc.nuts.toolbox.nsh.jshell;


import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

public class DefaultJShellExecutionContext implements JShellExecutionContext {


    private JShellContext shellContext;
    private NSession session;
    private JShellBuiltin builtin;
    private NTerminalMode terminalMode = null;
    private boolean askHelp;
    private boolean askVersion;
    private Object options;

    public DefaultJShellExecutionContext(JShellContext shellContext, JShellBuiltin command) {
        this.shellContext = shellContext;
        //each execution has its very own session!
        this.session = shellContext.getSession().copy();
        this.builtin = command;
    }


    @Override
    public NWorkspace getWorkspace() {
        return shellContext.getWorkspace();
    }

    @Override
    public NSession getSession() {
        return session;
    }


    @Override
    public JShell getShell() {
        return shellContext.getShell();
    }

    @Override
    public NPrintStream out() {
        return getSession().out();
    }

    @Override
    public NPrintStream err() {
        return getSession().err();
    }

    @Override
    public InputStream in() {
        return getSession().in();
    }


    @Override
    public JShellContext getShellContext() {
        return shellContext;
    }
    @Override
    public boolean configureFirst(NCmdLine cmd) {
        NArg a = cmd.peek().get(session);
        if (a == null) {
            return false;
        }
        switch(a.key()) {
            case "--help": {
                cmd.skip();
                setAskHelp(true);
                while(cmd.hasNext()){
                    getSession().configureLast(cmd);
                }
                break;
            }
            case "--version": {
                cmd.skip();
                setAskVersion(true);
                while(cmd.hasNext()){
                    getSession().configureLast(cmd);
                }
                break;
//                cmd.skip();
//                if (cmd.isExecMode()) {
//                    out().print(NMsg.ofC("%s%n", NutsIdResolver.of(getSession()).resolveId(getClass()).getVersion().toString()));
//                    cmd.skipAll();
//                }
//                throw new NutsExecutionException(shellContext.getSession(), NMsg.ofC("Help"), 0);
            }
            default: {
                if (getSession() != null && getSession().configureFirst(cmd)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isAskVersion() {
        return askVersion;
    }

    public DefaultJShellExecutionContext setAskVersion(boolean askVersion) {
        this.askVersion = askVersion;
        return this;
    }

    @Override
    public void configureLast(NCmdLine cmd) {
        if (!configureFirst(cmd)) {
            cmd.throwUnexpectedArgument();
        }
    }

    @Override
    public NApplicationContext getAppContext() {
        return getShell().getAppContext();
    }

    @Override
    public NTerminalMode geTerminalMode() {
        return terminalMode;
    }

    @Override
    public boolean isAskHelp() {
        return askHelp;
    }

    @Override
    public DefaultJShellExecutionContext setAskHelp(boolean askHelp) {
        this.askHelp = askHelp;
        return this;
    }

    public <T> T getOptions() {
        return (T) options;
    }

    public DefaultJShellExecutionContext setOptions(Object options) {
        this.options = options;
        return this;
    }

    @Override
    public JShellNode getRootNode() {
        return shellContext.getRootNode();
    }

    @Override
    public JShellNode getParentNode() {
        return shellContext.getParentNode();
    }

    @Override
    public JShellVariables vars() {
        return shellContext.vars();
    }

    @Override
    public JShellFunctionManager functions() {
        return shellContext.functions();
    }

    @Override
    public JShellExecutionContext setOut(PrintStream out) {
        getSession().getTerminal().setErr(NPrintStream.of(out, getSession()));
        return this;
    }

    @Override
    public JShellExecutionContext setErr(PrintStream err) {
        getSession().getTerminal().setErr(NPrintStream.of(err, getSession()));
        return this;
    }

    @Override
    public JShellExecutionContext setIn(InputStream in) {
        getSession().getTerminal().setIn(in);
        return this;
    }

    @Override
    public JShellExecutionContext setEnv(Map<String, String> env) {
        shellContext.setEnv(env);
        return this;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return shellContext.getUserProperties();
    }

    @Override
    public String getCwd() {
        return shellContext.getCwd();
    }

    @Override
    public String getHome() {
        return shellContext.getHome();
    }

    @Override
    public void setCwd(String cwd) {
        shellContext.setCwd(cwd);
    }

    @Override
    public JShellFileSystem getFileSystem() {
        return shellContext.getFileSystem();
    }

    @Override
    public String getAbsolutePath(String path) {
        return shellContext.getAbsolutePath(path);
    }

    @Override
    public String[] expandPaths(String path) {
        return shellContext.expandPaths(path);
    }

    @Override
    public JShellContext getParentContext() {
        return shellContext.getParentContext();
    }

    @Override
    public JShellAliasManager aliases() {
        return shellContext.aliases();
    }

    @Override
    public JShellBuiltinManager builtins() {
        return shellContext.builtins();
    }

    @Override
    public String getServiceName() {
        return shellContext.getServiceName();
    }

//    public void setArgs(String[] args) {
//        shellContext.setArgs(args);
//    }
//
//    public String getArg(int index) {
//        return shellContext.getArg(index);
//    }
//
//    public int getArgsCount() {
//        return shellContext.getArgsCount();
//    }
//
//    public String[] getArgsArray() {
//        return shellContext.getArgsArray();
//    }
//
//    public List<String> getArgsList() {
//        return shellContext.getArgsList();
//    }

    @Override
    public JShellExecutionContext setSession(NSession session) {
        this.session=session;
        return this;
    }

    @Override
    public NCmdLineAutoComplete getAutoComplete() {
        return shellContext.getAutoComplete();
    }
}
