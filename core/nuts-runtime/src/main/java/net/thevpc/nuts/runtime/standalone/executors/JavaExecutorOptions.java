package net.thevpc.nuts.runtime.standalone.executors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class JavaExecutorOptions {

    private String javaVersion = null;//runnerProps.getProperty("java.parseVersion");
    private String javaHome = null;//runnerProps.getProperty("java.parseVersion");
    private String mainClass = null;
    private String dir = null;
    private boolean javaw = false;
    private boolean mainClassApp = false;
    private boolean excludeBase = false;
    private boolean showCommand = CoreCommonUtils.getSysBoolNutsProperty("show-command", false);
    private boolean jar = false;
    private List<String> classPath = new ArrayList<>();
    private List<String> nutsPath = new ArrayList<>();
    private String[] execArgs;
    private List<String> jvmArgs = new ArrayList<String>();
    private List<String> app;
    //    private NutsDefinition nutsMainDef;
    private NutsSession session;

    public JavaExecutorOptions(NutsDefinition def, boolean tempId, String[] args, String[] executorOptions, String dir, NutsSession session) {
        this.session = session;
        NutsId id = def.getId();
        NutsDescriptor descriptor = null;
        if (tempId) {
            descriptor = def.getDescriptor();
            if (!CoreNutsUtils.isEffectiveId(id)) {
                throw new NutsException(getWorkspace(), "id should be effective : " + id);
            }
            id = descriptor.getId();
        } else {
            descriptor = NutsWorkspaceUtils.of(getWorkspace()).getEffectiveDescriptor(def);
            if (!CoreNutsUtils.isEffectiveId(id)) {
                id = descriptor.getId();
            }
        }
        Path path = def.getPath();
        this.app = new ArrayList<>(Arrays.asList(args));
        this.dir = dir;
        this.execArgs = executorOptions;
        List<String> classPath0 = new ArrayList<>();
        NutsIdFormat nutsIdFormat = getWorkspace().id().formatter().omitNamespace();
        //will accept all -- and - based options!
        NutsCommandLine cmdLine = getWorkspace().commandLine().create(getExecArgs()).setExpandSimpleOptions(false);
        NutsArgument a;
        while (cmdLine.hasNext()) {
            a = cmdLine.peek();
            switch (a.getStringKey()) {
                case "--java-version":
                case "-java-version": {
                    javaVersion = cmdLine.nextString().getStringValue();
                    break;
                }
                case "--java-home":
                case "-java-home": {
                    javaHome = cmdLine.nextString().getStringValue();
                    break;
                }
                case "--class-path":
                case "-class-path":
                case "--classpath":
                case "-classpath":
                case "--cp":
                case "-cp": {
                    addToCp(classPath0, cmdLine.nextString().getStringValue());
                    break;
                }
                case "--nuts-path":
                case "-nuts-path":
                case "--nutspath":
                case "-nutspath":
                case "--np":
                case "-np": {
                    npToCp(classPath0, cmdLine.nextString().getStringValue());
                    break;
                }
                case "--main-class":
                case "-main-class":
                case "--class":
                case "-class": {
                    this.mainClass = cmdLine.nextString().getStringValue();
                    break;
                }
                case "--dir":
                case "-dir": {
                    this.dir = cmdLine.nextString().getStringValue();
                    break;
                }
                case "--win":
                case "--javaw": {
                    this.javaw = cmdLine.nextBoolean().getBooleanValue();
                    break;
                }
                case "--jar":
                case "-jar": {
                    this.jar = cmdLine.nextBoolean().getBooleanValue();
                    break;
                }
                case "--show-command":
                case "-show-command": {
                    this.showCommand = cmdLine.nextBoolean().getBooleanValue();
                    break;
                }
                case "--exclude-base":
                case "-exclude-base": {
                    this.excludeBase = cmdLine.nextBoolean().getBooleanValue();
                    break;
                }
                default: {
                    getJvmArgs().add(cmdLine.next().getString());
                }
            }
        }
        if (getJavaHome() == null) {
            if (javaw) {
                if (!CoreStringUtils.isBlank(getJavaVersion())) {
                    javaHome = "${javaw#" + getJavaVersion() + "}";
                } else {
                    javaHome = "${javaw}";
                }
            } else {
                if (!CoreStringUtils.isBlank(getJavaVersion())) {
                    javaHome = "${java#" + getJavaVersion() + "}";
                } else {
                    javaHome = "${java}";
                }
            }
        } else {
            javaHome = NutsJavaSdkUtils.of(session.getWorkspace()).resolveJavaCommandByHome(getJavaHome());
        }

        List<NutsDefinition> nutsDefinitions = new ArrayList<>();
        NutsSearchCommand se = getWorkspace().search().setSession(CoreNutsUtils.silent(session));
        if (tempId) {
            for (NutsDependency dependency : descriptor.getDependencies()) {
                se.addId(dependency.toId());
            }
        } else {
            se.addId(id);
        }
        if (se.getIds().length > 0) {
            nutsDefinitions.addAll(
                    se
                            .setTransitive(true)
                            .addScope(NutsDependencyScopePattern.RUN)
                            .setOptional(false)
                            .setDistinct(true)
                            .setContent(true)
                            .setDependencies(true)
                            .setLatest(true)
                            .setInlineDependencies(true)
                            .getResultDefinitions().list()
            );
        }
        if (this.jar) {
            if (this.mainClass != null) {
                if (session.isPlainOut()) {
                    session.getTerminal().err().printf("ignored main-class=%s. running jar!%n", getMainClass());
                }
            }
            if (!classPath0.isEmpty()) {
                if (session.isPlainOut()) {
                    session.getTerminal().err().printf("ignored class-path=%s. running jar!%n", classPath0);
                }
            }
            if (this.excludeBase) {
                throw new NutsIllegalArgumentException(getWorkspace(), "cannot exclude base with jar modifier");
            }
        } else {
            if (mainClass == null) {
                if (path != null) {
                    //check manifest!
                    NutsExecutionEntry[] classes = getWorkspace().apps().execEntries().parse(path);
                    if (classes.length > 0) {
                        mainClass = String.join(":",
                                Arrays.stream(classes).map(NutsExecutionEntry::getName)
                                        .collect(Collectors.toList())
                        );
                    }
                }
            }else if(!mainClass.contains(".")){
                NutsExecutionEntry[] classes = getWorkspace().apps().execEntries().parse(path);
                List<String> possibileClasses=Arrays.stream(classes).map(NutsExecutionEntry::getName)
                        .collect(Collectors.toList());
                String r = resolveMainClass(mainClass, possibileClasses);
                if(r!=null){
                    mainClass=r;
                }
            }
            if (mainClass == null) {
                throw new NutsIllegalArgumentException(getWorkspace(), "missing Main Class for " + id);
            }
            boolean baseDetected = false;
            for (NutsDefinition nutsDefinition : nutsDefinitions) {
                if (nutsDefinition.getPath() != null) {
                    if (id.getLongName().equals(nutsDefinition.getId().getLongName())) {
                        baseDetected = true;
                        if (!isExcludeBase()) {
                            classPath.add(nutsDefinition.getPath().toString());
                            nutsPath.add(nutsIdFormat.value(nutsDefinition.getId()).format());
                        }
                    } else {
                        classPath.add(nutsDefinition.getPath().toString());
                        nutsPath.add(nutsIdFormat.value(nutsDefinition.getId()).format());
                    }
                }
            }
            if (!isExcludeBase() && !baseDetected) {
                if (path == null) {
                    throw new NutsIllegalArgumentException(getWorkspace(), "missing Path for " + id);
                }
                nutsPath.add(0, nutsIdFormat.value(id).format());
                classPath.add(0, path.toString());
            }
            for (String cp : classPath0) {
                classPath.add(cp);
                nutsPath.add(cp);
            }
            if (this.mainClass.contains(":")) {
                List<String> possibleClasses = CoreStringUtils.split(getMainClass(), ":");
                switch (possibleClasses.size()) {
                    case 0:
                        throw new NutsIllegalArgumentException(getWorkspace(), "missing Main-Class in Manifest for " + id);
                    case 1:
                        //
                        break;
                    default: {
                        if (!session.isPlainOut()) {
                            throw new NutsExecutionException(getWorkspace(), "multiple runnable classes detected : " + possibleClasses, 102);
                        }
                        NutsTextFormatManager text = getWorkspace().formats().text();
                        NutsTextNodeBuilder msgString = text.builder();


                        NutsTextNodeFactory tfactory = text.factory();
                        msgString.append("multiple runnable classes detected  - actually ")
                                .append(tfactory.styled("" + possibleClasses.size(), NutsTextNodeStyle.primary(5)))
                                .append(" . Select one :\n");
                        int x = ((int) Math.log(possibleClasses.size())) + 2;
                        for (int i = 0; i < possibleClasses.size(); i++) {
                            StringBuilder clsIndex = new StringBuilder();
                            clsIndex.append((i + 1));
                            while (clsIndex.length() < x) {
                                clsIndex.append(' ');
                            }
                            msgString.append(clsIndex.toString(), NutsTextNodeStyle.primary(4));
                            msgString.append(possibleClasses.get(i), NutsTextNodeStyle.primary(4));
                            msgString.append("\n");
                        }
                        msgString.append("enter class ")
                                .append("#", NutsTextNodeStyle.primary(5)).append(" or ").append("name", NutsTextNodeStyle.primary(5))
                                .append(" to run it. type ").append("cancel!", NutsTextNodeStyle.error())
                                .append(" to cancel : ");

                        mainClass = session.getTerminal()
                                .ask().forString(msgString.toString())
                                .setValidator((value, question) -> {
                                    Integer anyInt = CoreCommonUtils.convertToInteger(value, null);
                                    if (anyInt != null) {
                                        int i = anyInt;
                                        if (i >= 1 && i <= possibleClasses.size()) {
                                            return possibleClasses.get(i - 1);
                                        }
                                    } else {
                                        for (String possibleClass : possibleClasses) {
                                            if (possibleClass.equals(value)) {
                                                return possibleClass;
                                            }
                                        }
                                    }
                                    throw new NutsValidationException(getWorkspace());
                                }).getValue();
                        break;
                    }
                }
            }
        }

    }

    private String resolveMainClass(String name, List<String> possibleClasses) {
        if(name!=null){
            Integer v = CoreCommonUtils.convertToInteger(name, null);
            if(v!=null){
                if (v >= 1 && v <= possibleClasses.size()) {
                    return possibleClasses.get(v - 1);
                }
            }else{
                if(possibleClasses.contains(name)){
                    return name;
                }else{
                    for (String possibleClass : possibleClasses) {
                        int x=possibleClass.indexOf('.');
                        if(x>0){
                            if(possibleClass.substring(x+1).equals(name)){
                                return name;
                            }
                        }
                    }
                    for (String possibleClass : possibleClasses) {
                        int x=possibleClass.indexOf('.');
                        if(x>0){
                            if(possibleClass.substring(x+1).equalsIgnoreCase(name)){
                                return name;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void addToCp(List<String> classPath, String value) {
        for (String n : CoreStringUtils.split(value, ":;, ")) {
            if (!CoreStringUtils.isBlank(n)) {
                classPath.add(n);
            }
        }
    }

    private void npToCp(List<String> classPath, String value) {
        NutsSession searchSession = CoreNutsUtils.silent(this.session);
        NutsSearchCommand ns = getWorkspace().search().setLatest(true)
                .setSession(searchSession);
        for (String n : CoreStringUtils.split(value, ";, ")) {
            if (!CoreStringUtils.isBlank(n)) {
                ns.addId(n);
            }
        }
        for (NutsId nutsId : ns.getResultIds()) {
            NutsDefinition f = getWorkspace()
                    .search().addId(nutsId).setSession(searchSession).setLatest(true).getResultDefinitions().required();
            classPath.add(f.getPath().toString());
        }
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String getDir() {
        return dir;
    }

    public boolean isMainClassApp() {
        return mainClassApp;
    }

    public boolean isExcludeBase() {
        return excludeBase;
    }

    public boolean isShowCommand() {
        return showCommand;
    }

    public boolean isJar() {
        return jar;
    }

    public List<String> getClassPath() {
        return classPath;
    }

    public List<String> getNutsPath() {
        return nutsPath;
    }

    public String[] getExecArgs() {
        return execArgs;
    }

    public List<String> getJvmArgs() {
        return jvmArgs;
    }

    public NutsWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    public List<String> getApp() {
        return app;
    }

    public NutsSession getSession() {
        return session;
    }
}
