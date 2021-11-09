/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.executors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.collections.StringKeyValueList;
import net.thevpc.nuts.runtime.bundles.io.IProcessExecHelper;
import net.thevpc.nuts.runtime.core.DefaultNutsClassLoader;
import net.thevpc.nuts.runtime.core.util.CoreNumberUtils;
import net.thevpc.nuts.runtime.core.util.ProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.ext.DefaultNutsWorkspaceExtensionManager;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsExecutorComponent;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class JavaExecutorComponent implements NutsExecutorComponent {

    public static NutsId ID;
    NutsSession ws;

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public void exec(NutsExecutionContext executionContext) {
        execHelper(executionContext).exec();
    }

    @Override
    public void dryExec(NutsExecutionContext executionContext) throws NutsExecutionException {
        execHelper(executionContext).dryExec();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext ctx) {
        this.ws = ctx.getSession();
        if (ID == null) {
            ID = NutsId.of("net.thevpc.nuts.exec:java", ws);
        }
        NutsDefinition def = ctx.getConstraints(NutsDefinition.class);
        if (def != null) {
            String shortName = def.getId().getShortName();
            //for executors
            if ("net.thevpc.nuts.exec:exec-java".equals(shortName)) {
                return DEFAULT_SUPPORT + 1;
            }
            if ("java".equals(shortName)) {
                return DEFAULT_SUPPORT + 1;
            }
            if ("jar".equals(def.getDescriptor().getPackaging())) {
                return DEFAULT_SUPPORT + 1;
            }
        }
        return NO_SUPPORT;
    }

    //@Override
    public IProcessExecHelper execHelper(NutsExecutionContext executionContext) {
        NutsDefinition def = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
//        boolean inheritSystemIO=CoreCommonUtils.parseBoolean(String.valueOf(executionContext.getExecutorProperties().get("inheritSystemIO")),false);
//        final NutsWorkspace ws = executionContext.getWorkspace();
        Path contentFile = def.getPath();
        NutsSession session = executionContext.getSession();
        final JavaExecutorOptions joptions = new JavaExecutorOptions(
                def,
                executionContext.isTemporary(),
                executionContext.getArguments(),
                executionContext.getExecutorArguments(),
                NutsBlankable.isBlank(executionContext.getCwd()) ? System.getProperty("user.dir") : executionContext.getCwd(),
                session/*.copy().setProgressOptions("none")*/);
        final NutsSession execSession = executionContext.getExecSession();
        switch (executionContext.getExecutionType()) {
            case EMBEDDED: {
                return new EmbeddedProcessExecHelper(def, execSession, joptions, execSession.out());
            }
            case SPAWN:
            default: {
                StringKeyValueList runnerProps = new StringKeyValueList();
                if (executionContext.getExecutorDescriptor() != null) {
                    runnerProps.add(executionContext.getExecutorDescriptor().getProperties());
                }

                if (executionContext.getEnv() != null) {
                    runnerProps.add(executionContext.getEnv());
                }

                HashMap<String, String> osEnv = new HashMap<>();
                NutsWorkspaceOptionsBuilder options = ws.boot().getBootOptions().builder();

                //copy session parameters to the newly created workspace
                options.setDry(execSession.isDry());
                options.setGui(execSession.isGui());
                options.setOutLinePrefix(execSession.getOutLinePrefix());
                options.setErrLinePrefix(execSession.getErrLinePrefix());
                options.setDebug(execSession.isDebug());
                options.setTrace(execSession.isTrace());
                options.setBot(execSession.isBot());
                options.setCached(execSession.isCached());
                options.setIndexed(execSession.isIndexed());
                options.setConfirm(execSession.getConfirm());
                options.setTransitive(execSession.isTransitive());
                options.setOutputFormat(execSession.getOutputFormat());
                if (null == options.getTerminalMode()) {
                    options.setTerminalMode(execSession.getTerminal().out().mode());
                } else {
                    switch (options.getTerminalMode()) {
                        //retain filtered
                        case FILTERED:
                            break;
                        //retain inherited
                        case INHERITED:
                            break;
                        default:
                            options.setTerminalMode(execSession.getTerminal().out().mode());
                            break;
                    }
                }
                NutsVersion nutsDependencyVersion = null;
                for (String s : joptions.getClassPathNidStrings()) {
                    if (s.startsWith("net.thevpc.nuts:nuts#")) {
                        String v = s.substring("net.thevpc.nuts:nuts#".length());
                        nutsDependencyVersion = NutsVersion.of(v, session);
                    } else {
                        Pattern pp = Pattern.compile(".*[/\\\\]nuts-(?<v>[0-9.]+)[.]jar");
                        Matcher mm = pp.matcher(s);
                        if (mm.find()) {
                            String v = mm.group("v");
                            nutsDependencyVersion = NutsVersion.of(v, session);
                            break;
                        }
                    }
                }
//                List<String> validBootCommand = new ArrayList<>();
//                options.setTrace(execSession.isTrace());
                options.setExpireTime(execSession.getExpireTime());
//                options.setOutputFormat(execSession.getOutputFormat());
//                options.setConfirm(execSession.getConfirm());

                Filter logFileFilter = execSession.getLogFileFilter();
                Filter logTermFilter = execSession.getLogTermFilter();
                Level logTermLevel = execSession.getLogTermLevel();
                Level logFileLevel = execSession.getLogFileLevel();
                if (logFileFilter != null || logTermFilter != null || logTermLevel != null || logFileLevel != null) {
                    NutsLogConfig lc = options.getLogConfig();
                    if (lc == null) {
                        lc = new NutsLogConfig();
                    } else {
                        lc = lc.copy();
                    }
                    if (logTermLevel != null) {
                        lc.setLogTermLevel(logTermLevel);
                    }
                    if (logFileLevel != null) {
                        lc.setLogFileLevel(logFileLevel);
                    }
                    if (logTermFilter != null) {
                        lc.setLogTermFilter(logTermFilter);
                    }
                    if (logFileFilter != null) {
                        lc.setLogFileFilter(logFileFilter);
                    }
                }
//                options.setConfirm(execSession.getConfirm());

                String bootArgumentsString = options.formatter()
                        .setExported(true)
                        .setApiVersion(nutsDependencyVersion == null ? null : nutsDependencyVersion.toString())
                        .setCompact(true)
                        .getBootCommandLine().formatter().setShellFamily(NutsShellFamily.SH).setNtf(false).toString();
//                if(nutsDependencyVersion!=null && nutsDependencyVersion.compareTo(executionContext.getWorkspace().getApiVersion())<0){
//                    if(nutsDependencyVersion.compareTo("0.8.0")<0){
//                        for (String s : bootCommand) {
//                            if(s.startsWith("-N=") || s.startsWith("--expire=")){
//                                //ignore..
//                            }else{
//                                validBootCommand.add(s);
//                            }
//                        }
//                    }else{
//                validBootCommand.addAll(Arrays.asList(bootCommand));
//                    }
//                }
//                String bootArgumentsString = executionContext.getWorkspace().commandLine().create(bootCommand)
//                        .toString();
                if (!NutsBlankable.isBlank(bootArgumentsString)) {
                    osEnv.put("nuts_boot_args", bootArgumentsString);
                    joptions.getJvmArgs().add("-Dnuts.boot.args=" + bootArgumentsString);
                }
                //nuts.export properties should be propagated!!
                Properties sysProperties = System.getProperties();
                for (Object k : sysProperties.keySet()) {
                    String sk = (String) k;
                    if (sk.startsWith("nuts.export.")) {
                        joptions.getJvmArgs().add("-D" + sk + "=" + sysProperties.getProperty(sk));
                    }
                }
                // fix infinite recursion
                int maxDepth = Math.abs(CoreNumberUtils.convertToInteger(sysProperties.getProperty("nuts.export.watchdog.max-depth"), 24));
                if (maxDepth > 64) {
                    maxDepth = 64;
                }
                int currentDepth = CoreNumberUtils.convertToInteger(sysProperties.getProperty("nuts.export.watchdog.depth"), -1);
                currentDepth++;
                if (currentDepth > maxDepth) {
                    System.err.println("[[Process Stack Overflow Error]]");
                    System.err.println("it is very likely that you executed an infinite process creation recursion in your program.");
                    System.err.println("at least " + currentDepth + " (>=" + maxDepth + ") processes were created.");
                    System.err.println("are you aware of such misconception ?");
                    System.err.println("sorry but we need to end all of this disgracefully...");
                    System.exit(233);
                }

                List<NutsString> xargs = new ArrayList<>();
                List<String> args = new ArrayList<>();

                NutsTexts txt = NutsTexts.of(session);
                xargs.add(txt.ofPlain(joptions.getJavaCommand()));
                xargs.addAll(
                        joptions.getJvmArgs().stream()
                                .map(txt::ofPlain)
                                .collect(Collectors.toList())
                );

                args.add(joptions.getJavaCommand());
                args.addAll(joptions.getJvmArgs());

//                if (!NutsBlankable.isBlank(bootArgumentsString)) {
//                    String Dnuts_boot_args = "-Dnuts.boot.args=" + bootArgumentsString;
//                    xargs.add(Dnuts_boot_args);
//                    args.add(Dnuts_boot_args);
//                }
                String jdb = session.boot().getCustomBootOption("jdb").getString();
                if (jdb != null) {
                    boolean suspend = true;
                    int port = 5005;
                    for (String arg : jdb.split("[ ,]")) {
                        arg = arg.trim();
                        if (arg.length() > 0) {
                            if (arg.matches("[0-9]+")) {
                                port = CoreNumberUtils.convertToInteger(arg, port);
                            } else {
                                NutsArgument a = NutsArgument.of(arg, session);
                                switch (a.getKey().getString()) {
                                    case "suspend": {
                                        boolean b = a.getValue().getBoolean();
                                        if (a.isNegated()) {
                                            b = !b;
                                        }
                                        suspend = b;
                                        break;
                                    }
                                    case "port": {
                                        port = a.getValue().getInt();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    String ds = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=" + (suspend ? 'y' : 'n') + ",address=" + port;
                    xargs.add(txt.ofPlain(ds));
                    args.add(ds);
                }

                if (joptions.isJar()) {
                    xargs.add(txt.ofPlain("-jar"));
                    xargs.add(def.getId().formatter().format());

                    args.add("-jar");
                    args.add(contentFile.toString());
                } else {
                    xargs.add(txt.ofPlain("--nuts-path"));
                    xargs.add(
                            txt.builder().appendJoined(
                                    ";", joptions.getClassPathNidStrings()
                            ).immutable()
                    );
                    xargs.add(txt.ofPlain(
                                    joptions.getMainClass()
                            )
                    );

                    args.add("-classpath");
                    args.add(String.join(File.pathSeparator, joptions.getClassPathStrings()));
                    args.add(joptions.getMainClass());
                }
                xargs.addAll(
                        joptions.getApp().stream()
                                .map(txt::ofPlain)
                                .collect(Collectors.toList())
                );
                args.addAll(joptions.getApp());
                return new JavaProcessExecHelper(execSession, execSession, xargs, joptions, session, executionContext, def, args, osEnv);

            }

        }
    }

    static class EmbeddedProcessExecHelper extends AbstractSyncIProcessExecHelper {

        private final NutsDefinition def;
        private final JavaExecutorOptions joptions;
        private final NutsPrintStream out;

        public EmbeddedProcessExecHelper(NutsDefinition def, NutsSession session, JavaExecutorOptions joptions, NutsPrintStream out) {
            super(session);
            this.def = def;
            this.joptions = joptions;
            this.out = out;
        }

        @Override
        public void dryExec() {
            NutsSession session = getSession();
            NutsTexts text = NutsTexts.of(session);
            List<String> cmdLine = new ArrayList<>();
            cmdLine.add("embedded-java");
            cmdLine.add("-cp");
            cmdLine.add(String.join(":", joptions.getClassPathStrings()));
            cmdLine.add(joptions.getMainClass());
            cmdLine.addAll(joptions.getApp());

            session.out().printf("[dry] %s%n",
                    text.builder()
                            .append("exec", NutsTextStyle.pale())
                            .append(" ")
                            .append(NutsCommandLine.of(cmdLine, session))
            );
        }

        @Override
        public int exec() {
            NutsSession session = getSession();
            //we must make a copy not to alter caller session
            session = session.copy();

            if (session.out() != null) {
                session.out().resetLine();
            }
            DefaultNutsClassLoader classLoader = null;
            Throwable th = null;
            try {
                classLoader = ((DefaultNutsWorkspaceExtensionManager) session.extensions()).getModel().getNutsURLClassLoader(
                        def.getId().toString(),
                        null//getSession().getWorkspace().config().getBootClassLoader()
                        , session
                );
                for (NutsClassLoaderNode n : joptions.getClassPath()) {
                    classLoader.add(n);
                }
                Class<?> cls = Class.forName(joptions.getMainClass(), true, classLoader);
                new ClassloaderAwareRunnableImpl2(def.getId(), classLoader, cls, session, joptions).runAndWaitFor();
                return 0;
            } catch (InvocationTargetException e) {
                th = e.getTargetException();
            } catch (MalformedURLException | NoSuchMethodException | SecurityException
                    | IllegalAccessException | IllegalArgumentException
                    | ClassNotFoundException e) {
                th = e;
            } catch (Throwable ex) {
                th = ex;
            }
            if (th != null) {
                if (!(th instanceof NutsExecutionException)) {
                    throw new NutsExecutionException(session,
                            NutsMessage.cstyle("error executing %s : %s", def.getId(), th)
                            , th);
                }
                NutsExecutionException nex = (NutsExecutionException) th;
                if (nex.getExitCode() != 0) {
                    throw new NutsExecutionException(session, NutsMessage.cstyle("error executing %s : %s", def == null ? null : def.getId(), th), th);
                }
            }
            return 0;
        }
    }

    static class ClassloaderAwareRunnableImpl2 extends ClassloaderAwareRunnable {

        private final Class<?> cls;
        private final JavaExecutorOptions joptions;
        private final NutsId id;

        public ClassloaderAwareRunnableImpl2(NutsId id, ClassLoader classLoader, Class<?> cls, NutsSession session, JavaExecutorOptions joptions) {
            super(session.copy(), classLoader);
            this.id = id;
            this.cls = cls;
            this.joptions = joptions;
        }

        @Override
        public Object runWithContext() throws Throwable {
            if (cls.getName().equals("net.thevpc.nuts.Nuts")) {
                NutsWorkspace workspace = session.getWorkspace();
                NutsWorkspaceOptionsBuilder o = NutsWorkspaceOptionsBuilder.of().parseArguments(
                        joptions.getApp().toArray(new String[0])
                );
                String[] appArgs;
                if (o.getApplicationArguments().length == 0) {
                    if (o.isSkipWelcome()) {
                        return null;
                    }
                    appArgs = new String[]{"welcome"};
                } else {
                    appArgs = o.getApplicationArguments();
                }
                session.configure(o.build());
                Object oldId = NutsApplications.getSharedMap().get("nuts.embedded.application.id");
                NutsApplications.getSharedMap().put("nuts.embedded.application.id", id);
                try {
                    session.exec()
                            .addCommand(appArgs)
                            .addExecutorOptions(o.getExecutorOptions())
                            .setExecutionType(o.getExecutionType())
                            .setFailFast(true)
                            .setDry(session.isDry())
                            .run();
                } finally {
                    NutsApplications.getSharedMap().put("nuts.embedded.application.id", oldId);
                }
                return null;
            }
            Method mainMethod = null;
            boolean isNutsApp = false;
            Object nutsApp = null;
            try {
                mainMethod = cls.getMethod("run", NutsSession.class, String[].class);
                mainMethod.setAccessible(true);
                for (Class<?> pi : cls.getInterfaces()) {
                    //this is the old nuts apps (version >= 0.8.1)
                    if (pi.getName().equals("net.thevpc.nuts.NutsApplication")) {
                        isNutsApp = true;
                        break;
                    }
                }
                Class p = cls.getSuperclass();
                while (!isNutsApp && p != null) {
                    if (p.getName().equals("net.thevpc.nuts.NutsApplication")
                            //this is the old nuts apps (version < 0.8.0)
                            || p.getName().equals("net.vpc.app.nuts.NutsApplication")) {
                        isNutsApp = true;
                        break;
                    }
                    for (Class<?> pi : cls.getInterfaces()) {
                        //this is the old nuts apps (version >= 0.8.1)
                        if (pi.getName().equals("net.thevpc.nuts.NutsApplication")) {
                            isNutsApp = true;
                            break;
                        }
                    }
                    p = p.getSuperclass();
                }
                if (isNutsApp) {
                    isNutsApp = false;
                    nutsApp = cls.getConstructor().newInstance();
                    isNutsApp = true;
                }
            } catch (Exception rr) {
                //ignore

            }
            if (isNutsApp) {
                //NutsWorkspace
                NutsApplications.getSharedMap().put("nuts.embedded.application.id", id);
                mainMethod.invoke(nutsApp, getSession().copy(), joptions.getApp().toArray(new String[0]));
            } else {
                //NutsWorkspace
                System.setProperty("nuts.boot.args",
                        getSession().boot().getBootOptions()
                                .formatter().setExported(true).setCompact(true).getBootCommandLine()
                                .formatter().setShellFamily(NutsShellFamily.SH).toString()
                );
                mainMethod = cls.getMethod("main", String[].class);
//                List<String> nargs = new ArrayList<>();
//                nargs.addAll(joptions.getApp());
                mainMethod.invoke(null, new Object[]{joptions.getApp().toArray(new String[0])});
            }
            return null;
        }

    }

    private static class JavaProcessExecHelper extends AbstractSyncIProcessExecHelper {

        private final NutsSession execSession;
        private final List<NutsString> xargs;
        private final JavaExecutorOptions joptions;
        private final NutsSession ws;
        private final NutsExecutionContext executionContext;
        private final NutsDefinition def;
        private final List<String> args;
        private final HashMap<String, String> osEnv;

        public JavaProcessExecHelper(NutsSession ns, NutsSession execSession, List<NutsString> xargs, JavaExecutorOptions joptions, NutsSession ws, NutsExecutionContext executionContext, NutsDefinition def, List<String> args, HashMap<String, String> osEnv) {
            super(ns);
            this.execSession = execSession;
            this.xargs = xargs;
            this.joptions = joptions;
            this.ws = ws;
            this.executionContext = executionContext;
            this.def = def;
            this.args = args;
            this.osEnv = osEnv;
        }

        @Override
        public void dryExec() {
            NutsPrintStream out = execSession.out();
            out.println("[dry] ==[nuts-exec]== ");
            for (int i = 0; i < xargs.size(); i++) {
                NutsString xarg = xargs.get(i);
//                if (i > 0 && xargs.get(i - 1).equals("--nuts-path")) {
//                    for (String s : xarg.split(";")) {
//                        out.println("\t\t\t " + s);
//                    }
//                } else {
                out.println("\t\t " + xarg);
//                }
            }
            String directory = NutsBlankable.isBlank(joptions.getDir()) ? null : NutsPath.of(joptions.getDir(), ws).builder().withAppBaseDir().build().toString();
            ProcessExecHelper.ofDefinition(def,
                    args.toArray(new String[0]), osEnv, directory, executionContext.getExecutorProperties(), joptions.isShowCommand(), true, executionContext.getSleepMillis(), executionContext.isInheritSystemIO(), false, NutsBlankable.isBlank(executionContext.getRedirectOutputFile()) ? null : new File(executionContext.getRedirectOutputFile()), NutsBlankable.isBlank(executionContext.getRedirectInputFile()) ? null : new File(executionContext.getRedirectInputFile()), executionContext.getRunAs(), executionContext.getSession(),
                    execSession
            ).dryExec();
        }

        @Override
        public int exec() {
            return preExec().exec();
        }

        private ProcessExecHelper preExec() {
            if (joptions.isShowCommand() || getSession().boot().getCustomBootOption("show-command").getBoolean(false)) {
                NutsPrintStream out = execSession.out();
                out.printf("%s %n", NutsTexts.of(ws).ofStyled("nuts-exec", NutsTextStyle.primary1()));
                for (int i = 0; i < xargs.size(); i++) {
                    NutsString xarg = xargs.get(i);
//                    if (i > 0 && xargs.get(i - 1).equals("--nuts-path")) {
//                        for (String s : xarg.split(";")) {
//                            out.println("\t\t\t " + s);
//                        }
//                    } else {
                    out.println("\t\t " + xarg);
//                    }
                }
            }
            String directory = NutsBlankable.isBlank(joptions.getDir()) ? null : NutsPath.of(joptions.getDir(), ws).builder().withAppBaseDir().build().toString();
            return ProcessExecHelper.ofDefinition(def,
                    args.toArray(new String[0]), osEnv, directory, executionContext.getExecutorProperties(), joptions.isShowCommand(), true, executionContext.getSleepMillis(), executionContext.isInheritSystemIO(), false, NutsBlankable.isBlank(executionContext.getRedirectOutputFile()) ? null : new File(executionContext.getRedirectOutputFile()), NutsBlankable.isBlank(executionContext.getRedirectInputFile()) ? null : new File(executionContext.getRedirectInputFile()), executionContext.getRunAs(), executionContext.getSession(),
                    execSession
            );
        }

        @Override
        public Future<Integer> execAsync() {
            return preExec().execAsync();
        }
    }

}
