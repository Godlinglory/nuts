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
package net.thevpc.nuts.runtime.security;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import net.thevpc.nuts.*;

/**
 * @author vpc
 */
public class ReadOnlyNutsWorkspaceOptions implements NutsWorkspaceOptions {

    private final NutsWorkspaceOptions options;

    public ReadOnlyNutsWorkspaceOptions(NutsWorkspaceOptions options) {
        this.options = options;
    }

    @Override
    public NutsWorkspaceOptionsBuilder copy() {
        return options.copy();
    }

    @Override
    public NutsWorkspaceOptionsFormat format() {
        return options.format();
    }

    @Override
    public String getApiVersion() {
        return options.getApiVersion();
    }

    @Override
    public String[] getApplicationArguments() {
        return options.getApplicationArguments();
    }

    @Override
    public String getArchetype() {
        return options.getArchetype();
    }

    @Override
    public Supplier<ClassLoader> getClassLoaderSupplier() {
        return options.getClassLoaderSupplier();
    }

    @Override
    public NutsConfirmationMode getConfirm() {
        return options.getConfirm();
    }

    @Override
    public long getCreationTime() {
        return options.getCreationTime();
    }

    @Override
    public boolean isDry() {
        return options.isDry();
    }

    @Override
    public String[] getExcludedExtensions() {
        return options.getExcludedExtensions();
    }

    @Override
    public String[] getExcludedRepositories() {
        return options.getExcludedRepositories();
    }

    @Override
    public NutsExecutionType getExecutionType() {
        return options.getExecutionType();
    }

    @Override
    public String[] getExecutorOptions() {
        String[] v = options.getExecutorOptions();
        return v == null ? null : Arrays.copyOf(v, v.length);
    }

    @Override
    public String getHomeLocation(NutsOsFamily layout, NutsStoreLocation location) {
        return options.getHomeLocation(layout, location);
    }

    @Override
    public Map<String, String> getHomeLocations() {
        Map<String, String> v = options.getHomeLocations();
        return v == null ? null : Collections.unmodifiableMap(v);
    }

    @Override
    public String getJavaCommand() {
        return options.getJavaCommand();
    }

    @Override
    public String getJavaOptions() {
        return options.getJavaOptions();
    }

    @Override
    public NutsLogConfig getLogConfig() {
        return options.getLogConfig();
    }

    @Override
    public String getName() {
        return options.getName();
    }

    @Override
    public NutsWorkspaceOpenMode getOpenMode() {
        return options.getOpenMode();
    }

    @Override
    public NutsContentType getOutputFormat() {
        return options.getOutputFormat();
    }

    @Override
    public String[] getOutputFormatOptions() {
        return options.getOutputFormatOptions();
    }

    @Override
    public char[] getCredentials() {
        return options.getCredentials();
    }

    @Override
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return options.getRepositoryStoreLocationStrategy();
    }

    @Override
    public String getRuntimeId() {
        return options.getRuntimeId();
    }

    @Override
    public String getStoreLocation(NutsStoreLocation folder) {
        return options.getStoreLocation(folder);
    }

    @Override
    public NutsOsFamily getStoreLocationLayout() {
        return options.getStoreLocationLayout();
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return options.getStoreLocationStrategy();
    }

    @Override
    public Map<String, String> getStoreLocations() {
        return options.getStoreLocations();
    }

    @Override
    public NutsTerminalMode getTerminalMode() {
        return options.getTerminalMode();
    }

    @Override
    public String[] getTransientRepositories() {
        return options.getTransientRepositories();
    }

    @Override
    public String getUserName() {
        return options.getUserName();
    }

    @Override
    public String getWorkspace() {
        return options.getWorkspace();
    }

    @Override
    public boolean isDebug() {
        return options.isDebug();
    }

    @Override
    public boolean isGlobal() {
        return options.isGlobal();
    }

    @Override
    public boolean isGui() {
        return options.isGui();
    }

    @Override
    public boolean isInherited() {
        return options.isInherited();
    }

    @Override
    public boolean isReadOnly() {
        return options.isReadOnly();
    }

    @Override
    public boolean isRecover() {
        return options.isRecover();
    }

    @Override
    public boolean isReset() {
        return options.isReset();
    }

    @Override
    public boolean isSkipCompanions() {
        return options.isSkipCompanions();
    }

    @Override
    public boolean isSkipWelcome() {
        return options.isSkipWelcome();
    }

    @Override
    public boolean isTrace() {
        return options.isTrace();
    }

    @Override
    public String getProgressOptions() {
        return options.getProgressOptions();
    }

    @Override
    public boolean isCached() {
        return options.isCached();
    }

    @Override
    public boolean isIndexed() {
        return options.isIndexed();
    }

    @Override
    public boolean isTransitive() {
        return options.isTransitive();
    }

    @Override
    public NutsFetchStrategy getFetchStrategy() {
        return options.getFetchStrategy();
    }

    @Override
    public InputStream getStdin() {
        return options.getStdin();
    }

    @Override
    public PrintStream getStdout() {
        return options.getStdout();
    }

    @Override
    public PrintStream getStderr() {
        return options.getStderr();
    }

    @Override
    public ExecutorService getExecutorService() {
        return options.getExecutorService();
    }

    @Override
    public String getBootRepositories() {
        return options.getBootRepositories();
    }

    @Override
    public boolean isSkipBoot() {
        return options.isSkipBoot();
    }

    @Override
    public Instant getExpireTime() {
        return options.getExpireTime();
    }

    @Override
    public boolean isSkipErrors() {
        return options.isSkipErrors();
    }

    @Override
    public String[] getErrors() {
        return options.getErrors();
    }

    @Override
    public Boolean getSwitchWorkspace() {
        return options.getSwitchWorkspace();
    }

    @Override
    public String getOutLinePrefix() {
        return options.getOutLinePrefix();
    }

    @Override
    public String getErrLinePrefix() {
        return options.getErrLinePrefix();
    }
}
