/**
 * ====================================================================
 * vpc-common-io : common reusable library for
 * input/output
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.util.io;

import java.io.*;
import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.app.DefaultNutsArgument;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

public class ProcessBuilder2 {

    private List<String> command = new ArrayList<>();
    private Map<String, String> env;
    private File directory;
    private ProcessBuilder base = new ProcessBuilder();
    private InputStream in;
    private PrintStream out;
    private PrintStream err;
    private int result;
    private boolean baseIO;
    private boolean failFast;
    //    private List<PipeThread> pipes = new ArrayList<>();
    private Process proc;
    private long sleepMillis = 1000;
    private NutsWorkspace ws;

    public ProcessBuilder2(NutsWorkspace ws) {
        this.ws = ws;
    }

    public long getSleepMillis() {
        return sleepMillis;
    }

    public ProcessBuilder2 setSleepMillis(long sleepMillis) {
        this.sleepMillis = sleepMillis;
        return this;
    }

    public Process getProc() {
        return proc;
    }

    public List<String> getCommand() {
        return command;
    }

    public ProcessBuilder2 addCommand(String... command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        this.command.addAll(Arrays.asList(command));
        return this;
    }

    public ProcessBuilder2 addCommand(List<String> command) {
        if (this.command == null) {
            this.command = new ArrayList<>();
        }
        this.command.addAll(command);
        return this;
    }

    public ProcessBuilder2 setCommand(String... command) {
        setCommand(Arrays.asList(command));
        return this;
    }

    public ProcessBuilder2 setCommand(List<String> command) {
        this.command = command == null ? null : new ArrayList<>(command);
        return this;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public ProcessBuilder2 addEnv(Map<String, String> env) {
        if (env != null) {
            if (this.env == null) {
                this.env = new HashMap<>(env);
            } else {
                this.env.putAll(env);
            }
        }
        return this;
    }

    public ProcessBuilder2 setEnv(String k, String val) {
        if (env == null) {
            env = new HashMap<>();
        }
        env.put(k, val);
        return this;
    }

    public ProcessBuilder2 setEnv(Map<String, String> env) {
        this.env = env == null ? null : new HashMap<>(env);
        return this;
    }

    public File getDirectory() {
        return directory;
    }

    public ProcessBuilder2 setDirectory(File directory) {
        this.directory = directory;
        base.directory(directory);
        return this;
    }

    public InputStream getIn() {
        return in;
    }

    public ProcessBuilder2 setIn(InputStream in) {
        if (baseIO) {
            throw new NutsIllegalArgumentException(ws, "Already used Base IO Redirection");
        }
        this.in = in;
        return this;
    }

    public PrintStream getOut() {
        return out;
    }

    public ProcessBuilder2 grabOutputString() {
        setOutput(new SPrintStream());
        return this;
    }

    public ProcessBuilder2 grabErrorString() {
        setOutput(new SPrintStream());
        return this;
    }

    public String getOutputString() {
        PrintStream o = getOut();
        if (o instanceof SPrintStream) {
            return ((SPrintStream) o).getStringBuffer();
        }
        throw new NutsIllegalArgumentException(ws, "No Buffer was configured. Should call setOutString");
    }

    public String getErrorString() {
        if (base.redirectErrorStream()) {
            return getOutputString();
        }
        PrintStream o = getErr();
        if (o instanceof SPrintStream) {
            return ((SPrintStream) o).getStringBuffer();
        }
        throw new NutsIllegalArgumentException(ws, "No Buffer was configured. Should call setOutString");
    }

    public ProcessBuilder2 setOutput(PrintStream out) {
        if (baseIO) {
            throw new IllegalArgumentException("Already used Base IO Redirection");
        }
        this.out = out;
        return this;
    }

    public PrintStream getErr() {
        return err;
    }

    public ProcessBuilder2 setErr(PrintStream err) {
        if (baseIO) {
            throw new NutsIllegalArgumentException(ws, "Already used Base IO Redirection");
        }
        this.err = err;
        return this;
    }

    public ProcessBuilder2 start() throws IOException {
        if (proc != null) {
            throw new IOException("Already started");
        }
        base.command(command);
        if (env != null) {
            Map<String, String> environment = base.environment();
            for (Map.Entry<String, String> e : env.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                if (k != null) {
                    if (v == null) {
                        v = "";
                    }
                    environment.put(k, v);
                }
            }
        }
//        System.out.println("command="+command);
//        System.out.println("env="+env);
//        System.out.println("directory="+directory);
        proc = base.start();
        return this;
    }

    public ProcessBuilder2 waitFor() throws IOException {
        if (proc == null) {
            start();
        }
        if (proc == null) {
            throw new IOException("Not started");
        }
        if (!baseIO) {
            NonBlockingInputStreamAdapter procInput;
            NonBlockingInputStreamAdapter procError;
            NonBlockingInputStreamAdapter termIn = null;
            List<PipeThread> pipes = new ArrayList<>();
            if (out != null) {
                procInput = new NonBlockingInputStreamAdapter("pipe-out-proc-" + proc.toString(), proc.getInputStream());
                pipes.add(pipe("pipe-out-proc-" + proc.toString(), procInput, out));
            }
            if (err != null) {
                procError = new NonBlockingInputStreamAdapter("pipe-err-proc-" + proc.toString(), proc.getErrorStream());
                if (base.redirectErrorStream()) {
                    pipes.add(pipe("pipe-err-proc-" + proc.toString(), procError, out));
                } else {
                    pipes.add(pipe("pipe-err-proc-" + proc.toString(), procError, err));
                }
            }
            if (in != null) {
                termIn = new NonBlockingInputStreamAdapter("pipe-in-proc-" + proc.toString(), in);
                pipes.add(pipe("pipe-in-proc-" + proc.toString(), termIn, proc.getOutputStream()));
            }
            while (proc.isAlive()) {
                if (termIn != null) {
                    if (!termIn.hasMoreBytes() && termIn.available() == 0) {
                        termIn.close();
                    }
                }
                boolean allFinished = true;
                for (PipeThread pipe : pipes) {
                    if (!pipe.isStopped()) {
                        allFinished = false;
                    } else {
                        pipe.getOut().close();
                    }
                }
                if (allFinished) {
                    break;
                }
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException e) {
                    throw new IOException(CoreStringUtils.exceptionToString(e));
                }
            }

            proc.getInputStream().close();
            proc.getErrorStream().close();
            proc.getOutputStream().close();

            waitFor0();
            for (PipeThread pipe : pipes) {
                pipe.requestStop();
            }
        } else {
            waitFor0();
        }
        return this;
    }

    private void waitFor0() throws IOException {
        try {
            result = proc.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(CoreStringUtils.exceptionToString(e));
        }
        if (result != 0) {
            if (isFailFast()) {
                if (base.redirectErrorStream()) {
                    if (isGrabOutputString()) {
                        throw new NutsExecutionException(ws, "execution failed with code " + result + " and message : " + getOutputString(), result);
                    }
                } else {
                    if (isGrabErrorString()) {
                        throw new NutsExecutionException(ws, "execution failed with code " + result + " and message : " + getErrorString(), result);
                    }
                    if (isGrabOutputString()) {
                        throw new NutsExecutionException(ws, "execution failed with code " + result + " and message : " + getOutputString(), result);
                    }
                }
                throw new NutsExecutionException(ws, "execution failed with code " + result, result);
            }
        }
    }

    public boolean isGrabOutputString() {
        return !baseIO && (out instanceof SPrintStream);
    }

    public boolean isGrabErrorString() {
        return !baseIO && (err instanceof SPrintStream);
    }

    private ProcessBuilder2 waitFor2() throws IOException {
        if (proc == null) {
            start();
        }
        if (proc == null) {
            throw new IOException("Not started");
        }
        NonBlockingInputStreamAdapter procInput = null;
        NonBlockingInputStreamAdapter procError = null;
        NonBlockingInputStreamAdapter termIn = null;
        MultiPipeThread mp = new MultiPipeThread("pipe-out-proc-" + proc.toString());
        if (out != null) {
            procInput = new NonBlockingInputStreamAdapter("pipe-out-proc-" + proc.toString(), proc.getInputStream());
            mp.add("pipe-out-proc-" + proc.toString(), procInput, out);
        }
        if (!base.redirectErrorStream()) {
            if (err != null) {
                procError = new NonBlockingInputStreamAdapter("pipe-err-proc-" + proc.toString(), proc.getErrorStream());
                mp.add("pipe-err-proc-" + proc.toString(), procError, err);
            }
        }
        if (in != null) {
            termIn = new NonBlockingInputStreamAdapter("pipe-in-proc-" + proc.toString(), in);
            mp.add("pipe-in-proc-" + proc.toString(), termIn, proc.getOutputStream());
        }
        mp.start();
        while (proc.isAlive()) {
            if (termIn != null) {
                if (!termIn.hasMoreBytes() && termIn.available() == 0) {
                    termIn.close();
                }
            }
            if (mp.isEmpty()) {
                break;
            }
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
        proc.getInputStream().close();
        proc.getErrorStream().close();
        proc.getOutputStream().close();
        try {
            result = proc.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        mp.requestStop();
        return this;
    }

    public int getResult() {
        return result;
    }

    public Process getProcess() {
        return proc;
    }

    public ProcessBuilder2 inheritIO() {
        this.baseIO = true;
        base.inheritIO();
        return this;
    }

    private static class SPrintStream extends PrintStream {

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

    public ProcessBuilder2 redirectInput(ProcessBuilder.Redirect source) {
        base.redirectInput(source);
        baseIO = true;
        return this;
    }

    public ProcessBuilder2 redirectOutput(ProcessBuilder.Redirect source) {
        base.redirectOutput(source);
        baseIO = true;
        return this;
    }

    public ProcessBuilder2 redirectInput(File source) {
        base.redirectInput(source);
        baseIO = true;
        return this;
    }

    public ProcessBuilder2 redirectOutput(File source) {
        base.redirectOutput(source);
        baseIO = true;
        return this;
    }

    public ProcessBuilder2 redirectError(File source) {
        base.redirectError(source);
        baseIO = true;
        return this;
    }

    public ProcessBuilder.Redirect getRedirectInput() {
        return base.redirectInput();
    }

    public ProcessBuilder.Redirect getRedirectOutput() {
        return base.redirectOutput();
    }

    public ProcessBuilder.Redirect getRedirectError() {
        return base.redirectError();
    }

    public boolean isRedirectErrorStream() {
        return base.redirectErrorStream();
    }

    public ProcessBuilder2 setRedirectErrorStream() {
        return setRedirectErrorStream(true);
    }

    public ProcessBuilder2 setRedirectErrorStream(boolean redirectErrorStream) {
        base.redirectErrorStream(redirectErrorStream);
        return this;
    }

    public interface CommandStringFormat {

        default boolean acceptArgument(int argIndex, String arg) {
            return true;
        }

        default String replaceArgument(int argIndex, String arg) {
            return null;
        }

        default boolean acceptEnvName(String envName, String envValue) {
            return true;
        }

        default boolean acceptRedirectInput() {
            return true;
        }

        default boolean acceptRedirectOutput() {
            return true;
        }

        default boolean acceptRedirectError() {
            return true;
        }

        default String replaceEnvName(String envName, String envValue) {
            return null;
        }

        default String replaceEnvValue(String envName, String envValue) {
            return null;
        }
    }

    public String getCommandString() {
        return getCommandString(null);
    }

    public String getCommandString(CommandStringFormat f) {
        StringBuilder sb = new StringBuilder();
        if (env != null) {
            for (Map.Entry<String, String> e : env.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                if (k == null) {
                    k = "";
                }
                if (v == null) {
                    v = "";
                }
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
                sb.append(CoreStringUtils.enforceDoubleQuote(k)).append("=").append(CoreStringUtils.enforceDoubleQuote(v));
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
            sb.append(CoreStringUtils.enforceDoubleQuote(s));
        }
        if (baseIO) {
            ProcessBuilder.Redirect r;
            if (f == null || f.acceptRedirectOutput()) {
                r = base.redirectOutput();
                if (null == r.type()) {
                    sb.append(" > ").append("{?}");
                } else {
                    switch (r.type()) {
                        //sb.append(" > ").append("{inherited}");
                        case INHERIT:
                            break;
                        case PIPE:
                            break;
                        case WRITE:
                            sb.append(" > ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                            break;
                        case APPEND:
                            sb.append(" >> ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                            break;
                        default:
                            sb.append(" > ").append("{?}");
                            break;
                    }
                }
            }
            if (f == null || f.acceptRedirectError()) {
                if (base.redirectErrorStream()) {
                    sb.append(" 2>&1");
                } else {
                    if (f == null || f.acceptRedirectError()) {
                        r = base.redirectError();
                        if (null == r.type()) {
                            sb.append(" 2> ").append("{?}");
                        } else {
                            switch (r.type()) {
                                //sb.append(" 2> ").append("{inherited}");
                                case INHERIT:
                                    break;
                                case PIPE:
                                    break;
                                case WRITE:
                                    sb.append(" 2> ").append(r.file().getPath());
                                    break;
                                case APPEND:
                                    sb.append(" 2>> ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                                    break;
                                default:
                                    sb.append(" 2> ").append("{?}");
                                    break;
                            }
                        }
                    }
                }
            }
            if (f == null || f.acceptRedirectInput()) {
                r = base.redirectInput();
                if (null == r.type()) {
                    sb.append(" < ").append("{?}");
                } else {
                    switch (r.type()) {
                        //sb.append(" < ").append("{inherited}");
                        case INHERIT:
                            break;
                        case PIPE:
                            break;
                        case READ:
                            sb.append(" < ").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                            break;
                        default:
                            sb.append(" < ").append("{?}");
                            break;
                    }
                }
            }
        } else if (base.redirectErrorStream()) {
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

    public String getFormattedCommandString(NutsWorkspace ws) {
        return getFormattedCommandString(ws, null);
    }

    public String getFormattedCommandString(NutsWorkspace ws, CommandStringFormat f) {
        NutsTextFormatManager tf = ws.formats().text();
        StringBuilder sb = new StringBuilder();
        File ff = getDirectory();
        if (ff == null) {
            ff = new File(".");
        }
        try {
            ff = ff.getCanonicalFile();
        } catch (Exception ex) {
            ff = ff.getAbsoluteFile();
        }
        sb.append("cwd=```error ").append(CoreStringUtils.enforceDoubleQuote(ff.getPath(), ws)).append("```");
        if (env != null) {
            for (Map.Entry<String, String> e : env.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                if (k == null) {
                    k = "";
                }
                if (v == null) {
                    v = "";
                }
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
                sb.append("#####").append(CoreStringUtils.enforceDoubleQuote(k, ws)).append("#####").append("=").append(CoreStringUtils.enforceDoubleQuote(v, ws));
            }
        }
        boolean commandFirstTokenVisited = false;
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
            if (!commandFirstTokenVisited) {
                commandFirstTokenVisited = true;
                sb.append("```error ").append(CoreStringUtils.enforceDoubleQuote(s, ws)).append("```");
            } else {
                sb.append(formatArg(s, ws));
            }
        }
        if (baseIO) {
            ProcessBuilder.Redirect r;
            if (f == null || f.acceptRedirectOutput()) {
                r = base.redirectOutput();
                if (null == r.type()) {
                    sb.append("```pale ").append(tf.escapeText(" > ")).append("```").append("{?}");
                } else {
                    switch (r.type()) {
                        //sb.append(" > ").append("{inherited}");
                        case INHERIT:
                            break;
                        case PIPE:
                            break;
                        case WRITE:
                            sb.append("```pale ").append(tf.escapeText(" > ")).append("```").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                            break;
                        case APPEND:
                            sb.append("```pale ").append(tf.escapeText(" >> ")).append("```").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                            break;
                        default:
                            sb.append("```pale ").append(tf.escapeText(" > ")).append("```").append("{?}");
                            break;
                    }
                }
            }
            if (f == null || f.acceptRedirectError()) {
                if (base.redirectErrorStream()) {
                    sb.append("```pale ").append(tf.escapeText(" 2>&1")).append("```");
                } else {
                    if (f == null || f.acceptRedirectError()) {
                        r = base.redirectError();
                        if (null == r.type()) {
                            sb.append("<<").append(tf.escapeText(" 2> ")).append(">>").append("{?}");
                        } else {
                            switch (r.type()) {
                                //sb.append(" 2> ").append("{inherited}");
                                case INHERIT:
                                    break;
                                case PIPE:
                                    break;
                                case WRITE:
                                    sb.append("```pale ").append(tf.escapeText(" 2> ")).append("```").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                                    break;
                                case APPEND:
                                    sb.append("```pale ").append(tf.escapeText(" 2>> ")).append("```").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                                    break;
                                default:
                                    sb.append("```pale ").append(tf.escapeText(" 2> ")).append("```").append("{?}");
                                    break;
                            }
                        }
                    }
                }
            }
            if (f == null || f.acceptRedirectInput()) {
                r = base.redirectInput();
                if (null == r.type()) {
                    sb.append("<<").append(tf.escapeText(" < ")).append(">>").append("{?}");
                } else {
                    switch (r.type()) {
                        //sb.append(" < ").append("{inherited}");
                        case INHERIT:
                            break;
                        case PIPE:
                            break;
                        case READ:
                            sb.append("<<").append(tf.escapeText(" < ")).append(">>").append(CoreStringUtils.enforceDoubleQuote(r.file().getPath()));
                            break;
                        default:
                            sb.append("<<").append(tf.escapeText(" < ")).append(">>").append("{?}");
                            break;
                    }
                }
            }
        } else if (base.redirectErrorStream()) {
            if (out != null) {
                if (f == null || f.acceptRedirectOutput()) {
                    sb.append("<<").append(tf.escapeText(" > ")).append(">>").append("{stream}");
                }
                if (f == null || f.acceptRedirectError()) {
                    sb.append("<<").append(tf.escapeText(" 2>&1")).append(">>");
                }
            }
            if (in != null) {
                if (f == null || f.acceptRedirectInput()) {
                    sb.append("<<").append(tf.escapeText(" < ")).append(">>").append("{stream}");
                }
            }
        } else {
            if (out != null) {
                if (f == null || f.acceptRedirectOutput()) {
                    sb.append("<<").append(tf.escapeText(" > ")).append(">>").append("{stream}");
                }
            }
            if (err != null) {
                if (f == null || f.acceptRedirectError()) {
                    sb.append("<<").append(tf.escapeText(" 2> ")).append(">>").append("{stream}");
                }
            }
            if (in != null) {
                if (f == null || f.acceptRedirectInput()) {
                    sb.append("<<").append(tf.escapeText(" < ")).append(">>").append("{stream}");
                }
            }
        }
        return sb.toString();
    }

    private static String formatArg(String s, NutsWorkspace ws) {
        DefaultNutsArgument a = new DefaultNutsArgument(s, '=');
        StringBuilder sb = new StringBuilder();
        if (a.isKeyValue()) {
            if (a.isOption()) {
                sb.append("####").append(CoreStringUtils.enforceDoubleQuote(a.getStringKey(), ws)).append("####");
                sb.append("=");
                sb.append(CoreStringUtils.enforceDoubleQuote(a.getStringValue(), ws));
            } else {
                sb.append("#####").append(CoreStringUtils.enforceDoubleQuote(a.getStringKey(), ws)).append("#####");
                sb.append("=");
                sb.append(CoreStringUtils.enforceDoubleQuote(a.getStringValue(), ws));
            }
        } else {
            if (a.isOption()) {
                sb.append("####").append(CoreStringUtils.enforceDoubleQuote(a.getString(), ws)).append("####");
            } else {
                sb.append(CoreStringUtils.enforceDoubleQuote(a.getString(), ws));
            }
        }
        return sb.toString();
    }

    public boolean isFailFast() {
        return failFast;
    }

    public ProcessBuilder2 setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    public ProcessBuilder2 setFailFast() {
        return setFailFast(true);
    }

    private static PipeThread pipe(String name, final NonBlockingInputStream in, final OutputStream out) {
        PipeThread p = new PipeThread(name, in, out);
        p.start();
        return p;
    }

}
