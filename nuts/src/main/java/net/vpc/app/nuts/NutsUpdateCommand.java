/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.Collection;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsUpdateCommand extends NutsWorkspaceCommand{

    NutsUpdateCommand outputFormat(NutsOutputFormat outputFormat);

    NutsUpdateCommand setOutputFormat(NutsOutputFormat outputFormat);

    NutsUpdateCommand json();

    NutsUpdateCommand plain();

    NutsUpdateCommand props();

    NutsOutputFormat getOutputFormat();

    NutsUpdateCommand id(NutsId id);

    NutsUpdateCommand id(String id);

    NutsUpdateCommand removeId(NutsId id);

    NutsUpdateCommand addId(NutsId id);

    NutsUpdateCommand removeId(String id);

    NutsUpdateCommand addId(String id);

    NutsUpdateCommand ids(NutsId... id);

    NutsUpdateCommand ids(String... id);

    NutsUpdateCommand addIds(NutsId... ids);

    NutsUpdateCommand addIds(String... ids);

    NutsUpdateCommand clearIds();

    NutsId[] getIds();
//////

    NutsUpdateCommand frozenId(NutsId id);

    NutsUpdateCommand frozenId(String id);

    NutsUpdateCommand addFrozenId(NutsId id);

    NutsUpdateCommand addFrozenId(String id);

    NutsUpdateCommand frozenIds(NutsId... id);

    NutsUpdateCommand frozenIds(String... id);

    NutsUpdateCommand addFrozenIds(NutsId... ids);

    NutsUpdateCommand addFrozenIds(String... ids);

    NutsUpdateCommand clearFrozenIds();

    NutsId[] getFrozenIds();

    NutsUpdateCommand arg(String arg);

    NutsUpdateCommand addArg(String arg);

    NutsUpdateCommand args(String... arg);

    NutsUpdateCommand addArgs(Collection<String> args);

    NutsUpdateCommand args(Collection<String> arg);

    NutsUpdateCommand addArgs(String... args);

    NutsUpdateCommand clearArgs();

    String[] getArgs();

    NutsUpdateCommand session(NutsSession session);

    NutsUpdateCommand setSession(NutsSession session);

    NutsSession getSession();

    NutsUpdateCommand enableInstall();

    NutsUpdateCommand enableInstall(boolean enableInstall);

    NutsUpdateCommand setEnableInstall(boolean enableInstall);

    boolean isEnableInstall();

    NutsUpdateCommand ask();

    NutsUpdateCommand ask(boolean ask);

    NutsUpdateCommand setAsk(boolean ask);

    boolean isAsk();

    NutsUpdateCommand force();

    /**
     * @see #setForce(boolean)
     * @param enable
     * @return
     */
    NutsUpdateCommand force(boolean enable);

    /**
     * arm or disarm force install non already installed components
     *
     * @param enable if true force install if not yet installed
     * @return current builder instance
     */
    NutsUpdateCommand setForce(boolean enable);

    boolean isForce();

    NutsUpdateCommand trace();

    NutsUpdateCommand trace(boolean trace);

    NutsUpdateCommand setTrace(boolean trace);

    boolean isTrace();

    NutsUpdateCommand includeOptional();

    NutsUpdateCommand includeOptional(boolean includeOptional);

    NutsUpdateCommand setIncludeOptional(boolean includeOptional);

    boolean isIncludeOptional();

    NutsUpdateCommand apiVersion(String forceBootAPIVersion);

    NutsUpdateCommand setApiVersion(String forceBootAPIVersion);

    String getApiVersion();

    /**
     *
     * @return null if no updates
     */
    NutsUpdateCommand update();

    /**
     *
     * @return null if no updates
     */
    NutsUpdateCommand checkUpdates();

    /**
     *
     * @param applyUpdates
     * @return null if no updates
     */
    NutsUpdateCommand checkUpdates(boolean applyUpdates);

    NutsWorkspaceUpdateResult getResult();

    int getResultCount();

    NutsUpdateCommand workspace();

    NutsUpdateCommand updateWorkspace();

    NutsUpdateCommand updateWorkspace(boolean enable);

    NutsUpdateCommand setUpdateApi(boolean enable);

    boolean isUpdateApi();

    NutsUpdateCommand extensions();

    NutsUpdateCommand updateExtensions();

    NutsUpdateCommand updateExtensions(boolean enable);

    NutsUpdateCommand setUpdateExtensions(boolean enable);

    boolean isUpdateExtensions();

    NutsUpdateCommand runtime();

    NutsUpdateCommand updateRunime();

    NutsUpdateCommand updateRuntime(boolean enable);

    NutsUpdateCommand setUpdateRuntime(boolean enable);

    boolean isUpdateRuntime();

    NutsUpdateCommand installed();

    NutsUpdateCommand updateInstalled();

    NutsUpdateCommand updateInstalled(boolean updateExtensions);

    NutsUpdateCommand setUpdateInstalled(boolean updateExtensions);

    boolean isUpdateInstalled();

    NutsUpdateCommand all();

    NutsUpdateCommand scope(NutsDependencyScope scope);

    NutsUpdateCommand addScope(NutsDependencyScope scope);

    NutsUpdateCommand scopes(Collection<NutsDependencyScope> scopes);

    NutsUpdateCommand addScopes(Collection<NutsDependencyScope> scopes);

    NutsUpdateCommand scopes(NutsDependencyScope... scopes);

    NutsUpdateCommand addScopes(NutsDependencyScope... scopes);

    NutsUpdateCommand clearScopes();

    NutsUpdateCommand parseOptions(String... applicationArguments);

    NutsUpdateCommand api();

    NutsUpdateCommand api(boolean enable);

    NutsUpdateCommand runtime(boolean enable);

    NutsUpdateCommand extensions(boolean enable);

    NutsUpdateCommand installed(boolean enable);

}
