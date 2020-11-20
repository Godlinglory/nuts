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
package net.thevpc.nuts;

import java.util.Map;

/**
 * execution context used in {@link NutsExecutorComponent} and
 * {@link NutsInstallerComponent}.
 *
 * @author vpc
 * @since 0.5.4
 * %category Base
 */
public interface NutsExecutionContext {

    /**
     * command name
     *
     * @return command name
     */
    String getCommandName();

    long getSleepMillis();

    /**
     * executor options
     *
     * @return executor options
     */
    String[] getExecutorArguments();

    /**
     * executor properties
     *
     * @return executor properties
     */
    Map<String, String> getExecutorProperties();

    /**
     * command definition if any
     *
     * @return command definition if any
     */
    NutsDefinition getDefinition();

    /**
     * command arguments
     *
     * @return command arguments
     */
    String[] getArguments();

    /**
     * workspace
     *
     * @return workspace
     */
    NutsWorkspace getWorkspace();

    /**
     * executor descriptor
     *
     * @return executor descriptor
     */
    NutsArtifactCall getExecutorDescriptor();

    /**
     * current session
     *
     * @return current session
     */
    NutsSession getExecSession();

    NutsSession getTraceSession();

    /**
     * execution environment
     *
     * @return execution environment
     */
    Map<String, String> getEnv();

    /**
     * current working directory
     *
     * @return current working directory
     */
    String getCwd();

    /**
     * when true, any non 0 exited command will throw an Exception
     *
     * @return fail fast status
     */
    boolean isFailFast();

    /**
     * when true, the component is temporary and is not registered withing the
     * workspace
     *
     * @return true if the component is temporary and is not registered withing
     * the workspace
     */
    boolean isTemporary();

    /**
     * execution type
     *
     * @return execution type
     */
    NutsExecutionType getExecutionType();
}
