/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
package net.thevpc.nuts.runtime;

import net.thevpc.nuts.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by vpc on 1/15/17.
 */
public class DefaultNutsExecutionContext implements NutsExecutionContext {

    private NutsDefinition definition;
    private Map<String, String> env;
    private String[] executorArguments;
    private Map<String, String> executorProperties;
    private String[] arguments;
    private NutsSession execSession;
    private NutsSession traceSession;
    private NutsWorkspace workspace;
    private NutsArtifactCall executorDescriptor;
    private String cwd;
    private String commandName;
    private boolean failFast;
    private boolean temporary;
    private long sleepMillis;
    private NutsExecutionType executionType;

    //    public NutsExecutionContextImpl(NutsDefinition nutsDefinition, NutsSession session, NutsWorkspace workspace,String cwd) {
//        this.nutsDefinition = nutsDefinition;
//        this.session = session;
//        if (nutsDefinition != null && nutsDefinition.getDescriptor() != null && nutsDefinition.getDescriptor().getInstaller() != null) {
//            NutsExecutorDescriptor ii = nutsDefinition.getDescriptor().getInstaller();
//            executorOptions = ii.getArguments();
//            executorProperties = ii.getProperties();
//        }
//        this.workspace = workspace;
//        if (args == null) {
//            args = new String[0];
//        }
//        if (executorOptions == null) {
//            executorOptions = new String[0];
//        }
//        if (executorProperties == null) {
//            executorProperties = new Properties();
//        }
//        this.cwd = cwd;
//    }
    public DefaultNutsExecutionContext(NutsDefinition definition,
                                       String[] arguments, String[] executorArgs, Map<String, String> env, Map<String, String> executorProperties,
                                       String cwd, NutsSession traceSession, NutsSession execSession, NutsWorkspace workspace, boolean failFast,
                                       boolean temporary,
                                       NutsExecutionType executionType,
                                       String commandName,
                                       long sleepMillis
                                       ) {
        if (arguments == null) {
            arguments = new String[0];
        }
        if (executorArgs == null) {
            executorArgs = new String[0];
        }
        if (executorProperties == null) {
            executorProperties = new LinkedHashMap<>();
        }
        this.commandName = commandName;
        this.definition = definition;
        this.arguments = arguments;
        this.execSession = execSession;
        this.traceSession = traceSession;
        this.workspace = workspace;
        this.executorArguments = executorArgs;
        this.executorProperties = executorProperties;
        this.sleepMillis = sleepMillis;
        this.cwd = cwd;
        if (env == null) {
            env = new LinkedHashMap<>();
        }
        this.env = env;
        this.failFast = failFast;
        this.temporary = temporary;
        this.executionType = executionType;
        this.executorDescriptor = definition.getDescriptor().getExecutor();
    }

    public DefaultNutsExecutionContext(NutsExecutionContext other) {
        this.commandName = other.getCommandName();
        this.definition = other.getDefinition();
        this.arguments = other.getArguments();
        this.execSession = other.getExecSession();
        this.traceSession = other.getTraceSession();
        this.workspace = other.getWorkspace();
        this.executorArguments = other.getExecutorArguments();
        this.executorProperties = other.getExecutorProperties();
        this.cwd = other.getCwd();
        this.env = other.getEnv();
        this.failFast = other.isFailFast();
        this.temporary = other.isTemporary();
        this.executionType = other.getExecutionType();
        this.executorDescriptor = other.getExecutorDescriptor();
        this.sleepMillis = other.getSleepMillis();
    }

    public long getSleepMillis() {
        return sleepMillis;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public String[] getExecutorArguments() {
        return executorArguments;
    }

    @Override
    public Map<String, String> getExecutorProperties() {
        return executorProperties;
    }

    @Override
    public NutsDefinition getDefinition() {
        return definition;
    }

    @Override
    public String[] getArguments() {
        return arguments;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NutsArtifactCall getExecutorDescriptor() {
        return executorDescriptor;
    }

    @Override
    public NutsSession getExecSession() {
        return execSession;
    }

    @Override
    public NutsSession getTraceSession() {
        return traceSession;
    }

    @Override
    public Map<String, String> getEnv() {
        return env;
    }

    @Override
    public String getCwd() {
        return cwd;
    }

    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public NutsExecutionType getExecutionType() {
        return executionType;
    }

    public DefaultNutsExecutionContext setDefinition(NutsDefinition definition) {
        this.definition = definition;
        return this;
    }

    public DefaultNutsExecutionContext setEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    public DefaultNutsExecutionContext setExecutorArguments(String[] executorArguments) {
        this.executorArguments = executorArguments;
        return this;
    }

    public DefaultNutsExecutionContext setExecutorProperties(Map<String, String> executorProperties) {
        this.executorProperties = executorProperties;
        return this;
    }

    public DefaultNutsExecutionContext setArguments(String[] arguments) {
        this.arguments = arguments;
        return this;
    }

    public DefaultNutsExecutionContext setExecSession(NutsSession execSession) {
        this.execSession = execSession;
        return this;
    }

    public DefaultNutsExecutionContext setTraceSession(NutsSession traceSession) {
        this.traceSession = traceSession;
        return this;
    }

    public DefaultNutsExecutionContext setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
        return this;
    }

    public DefaultNutsExecutionContext setExecutorDescriptor(NutsArtifactCall executorDescriptor) {
        this.executorDescriptor = executorDescriptor;
        return this;
    }

    public DefaultNutsExecutionContext setCwd(String cwd) {
        this.cwd = cwd;
        return this;
    }

    public DefaultNutsExecutionContext setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    public DefaultNutsExecutionContext setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    public DefaultNutsExecutionContext setTemporary(boolean temporary) {
        this.temporary = temporary;
        return this;
    }

    public DefaultNutsExecutionContext setExecutionType(NutsExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }
}
