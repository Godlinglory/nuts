package net.vpc.app.nuts.core.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.impl.def.DefaultNutsWorkspace;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * type: Command Class
 *
 * @author vpc
 */
public abstract class AbstractNutsExecCommand extends NutsWorkspaceCommandBase<NutsExecCommand> implements NutsExecCommand {

    protected NutsDefinition commandDefinition;
    protected List<String> command;
    protected List<String> executorOptions;
    protected Properties env;
    protected NutsExecutionException result;
    protected boolean executed;
    protected String directory;
    protected PrintStream out;
    protected PrintStream err;
    protected InputStream in;
    protected NutsExecutionType executionType = NutsExecutionType.SPAWN;
    protected boolean redirectErrorStream;
    protected boolean failFast;
    protected boolean dry;
    protected NutsCommandLineFormat commandStringFormatter;

    public AbstractNutsExecCommand(DefaultNutsWorkspace ws) {
        super(ws, "exec");
    }

    private static String enforceDoubleQuote(String s) {
        if (s.isEmpty() || s.contains(" ") || s.contains("\"")) {
            s = "\"" + s.replace("\"", "\\\"") + "\"";
        }
        return s;
    }

    @Override
    public NutsExecCommand failFast() {
        return setFailFast(true);
    }

    @Override
    public NutsExecCommand setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public NutsExecCommand failFast(boolean failFast) {
        return setFailFast(failFast);
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public String[] getCommand() {
        return command == null ? new String[0] : command.toArray(new String[0]);
    }

    @Override
    public NutsExecCommand command(NutsDefinition definition) {
        this.commandDefinition = definition;
        if (this.commandDefinition != null) {
            this.commandDefinition.getContent();
            this.commandDefinition.getDependencies();
            this.commandDefinition.getEffectiveDescriptor();
            this.commandDefinition.getInstallInformation();
        }
        return this;
    }

    @Override
    public NutsExecCommand command(String... command) {
        return addCommand(command);
    }

    @Override
    public NutsExecCommand addCommand(String... command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        this.command.addAll(Arrays.asList(command));
        return this;
    }

    @Override
    public NutsExecCommand command(Collection<String> command) {
        return addCommand(command);
    }

    @Override
    public NutsExecCommand clearCommand() {
        this.command = null;
        return this;
    }

    @Override
    public NutsExecCommand addCommand(Collection<String> command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        this.command.addAll(command);
        return this;
    }

    @Override
    public NutsExecCommand addExecutorOption(String executorOption) {
        if (executorOption != null) {
            if (this.executorOptions == null) {
                this.executorOptions = new ArrayList<>();
            }
            this.executorOptions.add(executorOption);
        }
        return this;
    }

    @Override
    public NutsExecCommand executorOption(String executorOption) {
        return addExecutorOption(executorOption);
    }

    @Override
    public NutsExecCommand executorOptions(String... executorOptions) {
        return addExecutorOptions(executorOptions);
    }

    @Override
    public NutsExecCommand addExecutorOptions(String... executorOptions) {
        if (executorOptions != null) {
            for (String executorOption : executorOptions) {
                addExecutorOption(executorOption);
            }
        }
        return this;
    }

    @Override
    public NutsExecCommand executorOptions(Collection<String> executorOptions) {
        return addExecutorOptions(executorOptions);
    }

    @Override
    public NutsExecCommand addExecutorOptions(Collection<String> executorOptions) {
        if (executorOptions != null) {
            for (String executorOption : executorOptions) {
                addExecutorOption(executorOption);
            }
        }
        return this;
    }

    @Override
    public NutsExecCommand clearExecutorOptions() {
        this.executorOptions = null;
        return this;
    }

    @Override
    public Properties getEnv() {
        return env;
    }

    @Override
    public NutsExecCommand env(Map<String, String> env) {
        return setEnv(env);
    }

    @Override
    public NutsExecCommand addEnv(Properties env) {
        if (env != null) {
            if (this.env == null) {
                this.env = new Properties();
                this.env.putAll(env);
            } else {
                this.env.putAll(env);
            }
        }
        return this;
    }

    @Override
    public NutsExecCommand addEnv(Map<String, String> env) {
        if (env != null) {
            if (this.env == null) {
                this.env = new Properties();
                this.env.putAll(env);
            } else {
                this.env.putAll(env);
            }
        }
        return this;
    }

    @Override
    public NutsExecCommand env(String k, String val) {
        return setEnv(k, val);
    }

    @Override
    public NutsExecCommand setEnv(String k, String val) {
        if (env == null) {
            env = new Properties();
        }
        env.put(k, val);
        return this;
    }

    @Override
    public NutsExecCommand setEnv(Map<String, String> env) {
        this.env = env == null ? null : new Properties();
        if (env != null) {
            this.env.putAll(env);
        }
        return this;
    }

    @Override
    public NutsExecCommand setEnv(Properties env) {
        this.env = env == null ? null : new Properties();
        if (env != null) {
            this.env.putAll(env);
        }
        return this;
    }

    @Override
    public NutsExecCommand env(Properties env) {
        return setEnv(env);
    }

    @Override
    public NutsExecCommand clearEnv() {
        this.env = null;
        return this;
    }

    @Override
    public String getDirectory() {
        return directory;
    }

    @Override
    public NutsExecCommand setDirectory(String directory) {
        this.directory = directory;
        return this;
    }

    @Override
    public NutsExecCommand directory(String directory) {
        return setDirectory(directory);
    }

    @Override
    public InputStream getIn() {
        return in;
    }

    @Override
    public InputStream in() {
        return getIn();
    }

    @Override
    public NutsExecCommand in(InputStream in) {
        return setIn(in);
    }

    @Override
    public NutsExecCommand setIn(InputStream in) {
        this.in = in;
        return this;
    }

    @Override
    public PrintStream getOut() {
        return out;
    }

    @Override
    public PrintStream out() {
        return getOut();
    }

    @Override
    public NutsExecCommand grabOutputString() {
        // DO NOT CALL setOut :: setOut(new SPrintStream());
        this.out = new SPrintStream();
        return this;
    }

    @Override
    public NutsExecCommand grabErrorString() {
        // DO NOT CALL setOut :: setErr(new SPrintStream());
        this.err = new SPrintStream();
        return this;
    }

    @Override
    public String getOutputString() {
        PrintStream o = getOut();
        if (o instanceof SPrintStream) {
            return ((SPrintStream) o).getStringBuffer();
        }
        throw new NutsIllegalArgumentException(ws, "No Buffer was configured. Should call setOutString");
    }

    @Override
    public String getErrorString() {
        if (isRedirectErrorStream()) {
            return getOutputString();
        }
        PrintStream o = getErr();
        if (o instanceof SPrintStream) {
            return ((SPrintStream) o).getStringBuffer();
        }
        throw new NutsIllegalArgumentException(ws, "No Buffer was configured. Should call setOutString");
    }

    @Override
    public NutsExecCommand out(PrintStream out) {
        return setOut(out);
    }

    @Override
    public NutsExecCommand setOut(PrintStream out) {
        this.out = (out == null ? null : ws.io().createPrintStream(out, NutsTerminalMode.FORMATTED));
        return this;
    }

    @Override
    public NutsExecCommand err(PrintStream err) {
        return setErr(err);
    }

    @Override
    public NutsExecCommand setErr(PrintStream err) {
        this.err = (err == null ? null : ws.io().createPrintStream(err, NutsTerminalMode.FORMATTED));
        return this;
    }

    @Override
    public PrintStream getErr() {
        return err;
    }

    @Override
    public PrintStream err() {
        return getErr();
    }

    @Override
    public NutsExecutionType getExecutionType() {
        return executionType;
    }

    @Override
    public boolean isRedirectErrorStream() {
        return redirectErrorStream;
    }

    @Override
    public NutsExecCommand redirectErrorStream() {
        return setRedirectErrorStream(true);
    }

    @Override
    public NutsExecCommand setRedirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    @Override
    public NutsExecCommand redirectErrorStream(boolean redirectErrorStream) {
        return setRedirectErrorStream(redirectErrorStream);
    }

    @Override
    public NutsExecCommand setExecutionType(NutsExecutionType executionType) {
        if (executionType == null) {
            executionType = NutsExecutionType.SPAWN;
        }
        this.executionType = executionType;
        return this;
    }

    @Override
    public NutsExecCommand executionType(NutsExecutionType executionType) {
        return setExecutionType(executionType);
    }

    @Override
    public NutsExecCommand embedded() {
        return setExecutionType(NutsExecutionType.EMBEDDED);
    }

    @Override
    public NutsExecCommand copyFrom(NutsExecCommand other) {
        super.copyFromWorkspaceCommandBase((NutsWorkspaceCommandBase) other);
        addCommand(other.getCommand());
        addEnv(other.getEnv());
        addExecutorOptions(other.getExecutorOptions());
        setDirectory(other.getDirectory());
        setIn(other.getIn());
        setOut(other.getOut());
        setErr(other.getErr());
        setRedirectErrorStream(other.isRedirectErrorStream());
        setSession(other.getSession());
        setFailFast(other.isFailFast());
        setExecutionType(other.getExecutionType());
        return this;
    }

    @Override
    public NutsExecCommand copy() {
        return ws.exec().copyFrom(this);
    }

    @Override
    public int getResult() {
        if (!executed) {
            try {
                run();
            } catch (Exception ex) {
                // ignore;
            }
        }
        return result == null ? 0 : result.getExitCode();
    }

    @Override
    public String getCommandString() {
        NutsCommandLineFormat f = getCommandLineFormat();
        StringBuilder sb = new StringBuilder();
        if (env != null) {
            for (Map.Entry<Object, Object> e : env.entrySet()) {
                String k = (String) e.getKey();
                String v = (String) e.getValue();
                if (f != null) {
                    if (!f.acceptEnvName(k, v)) {
                        continue;
                    }
                    String k2 = f.replaceEnvName(k, v);
                    if (k2 != null) {
                        k = k2;
                    }
                    String v2 = f.replaceEnvValue(k, v);
                    if (v2 != null) {
                        v = v2;
                    }
                }
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(enforceDoubleQuote(k)).append("=").append(enforceDoubleQuote(v));
            }
        }
        for (int i = 0; i < command.size(); i++) {
            String s = command.get(i);
            if (f != null) {
                if (!f.acceptArgument(i, s)) {
                    continue;
                }
                String k2 = f.replaceArgument(i, s);
                if (k2 != null) {
                    s = k2;
                }
            }
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(enforceDoubleQuote(s));
        }
        if (isRedirectErrorStream()) {
            if (out != null) {
                if (f == null || f.acceptRedirectOutput()) {
                    sb.append(" > ").append("{stream}");
                }
                if (f == null || f.acceptRedirectError()) {
                    sb.append(" 2>&1");
                }
            }
            if (in != null) {
                if (f == null || f.acceptRedirectInput()) {
                    sb.append(" < ").append("{stream}");
                }
            }
        } else {
            if (out != null) {
                if (f == null || f.acceptRedirectOutput()) {
                    sb.append(" > ").append("{stream}");
                }
            }
            if (err != null) {
                if (f == null || f.acceptRedirectError()) {
                    sb.append(" 2> ").append("{stream}");
                }
            }
            if (in != null) {
                if (f == null || f.acceptRedirectInput()) {
                    sb.append(" < ").append("{stream}");
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String[] getExecutorOptions() {
        return executorOptions == null ? new String[0] : executorOptions.toArray(new String[0]);
    }

    @Override
    public NutsExecutionException getResultException() {
        if (!executed) {
            run();
        }
        return result;
    }

    @Override
    public NutsExecCommand syscall() {
        return setExecutionType(NutsExecutionType.SYSCALL);
    }

    @Override
    public NutsExecCommand spawn() {
        return setExecutionType(NutsExecutionType.SPAWN);
    }

    @Override
    public NutsCommandLineFormat getCommandLineFormat() {
        return commandStringFormatter;
    }

    @Override
    public NutsExecCommand setCommandLineFormat(NutsCommandLineFormat commandStringFormatter) {
        this.commandStringFormatter = commandStringFormatter;
        return this;
    }

    protected String getExtraErrorMessage() {
        if (isRedirectErrorStream()) {
            if (isGrabOutputString()) {
                return getOutputString();
            }
        } else {
            if (isGrabErrorString()) {
                return getErrorString();
            }
            if (isGrabOutputString()) {
                return getOutputString();
            }
        }
        return null;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        if (command == null) {
            command = new ArrayList<>();
        }
        if (!command.isEmpty()) {
            command.add(a.getString());
            return true;
        }
        switch (a.getStringKey()) {
            case "--external":
            case "--spawn":
            case "-x": {
                cmdLine.skip();
                setExecutionType(NutsExecutionType.SPAWN);
                return true;
            }
            case "--embedded":
            case "-b": {
                cmdLine.skip();
                setExecutionType(NutsExecutionType.EMBEDDED);
                return true;
            }
            case "--native":
            case "--syscall":
            case "-n": {
                cmdLine.skip();
                setExecutionType(NutsExecutionType.SYSCALL);
                return true;
            }
            case "-dry":
            case "-d": {
                setDry(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                cmdLine.skip();
                if (a.isOption()) {
                    addExecutorOption(a.getString());
                } else {
                    addCommand(a.getString());
                }
                return true;
            }
        }
    }

    @Override
    public boolean isDry() {
        return dry;
    }

    @Override
    public NutsExecCommand setDry(boolean value) {
        this.dry = value;
        return this;
    }

    @Override
    public NutsExecCommand dry(boolean value) {
        return setDry(value);
    }

    @Override
    public NutsExecCommand dry() {
        return dry(true);
    }

    public boolean isGrabOutputString() {
        return out instanceof SPrintStream;
    }

    public boolean isGrabErrorString() {
        return err instanceof SPrintStream;
    }

    protected static class SPrintStream extends PrintStream {

        private ByteArrayOutputStream out;

        public SPrintStream() {
            this(new ByteArrayOutputStream());
        }

        public SPrintStream(ByteArrayOutputStream out1) {
            super(out1);
            this.out = out1;
        }

        public String getStringBuffer() {
            flush();
            return new String(out.toByteArray());
        }
    }
}