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
 *
 * <br>
 * <p>
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


import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NApplicationContexts;
import net.thevpc.nuts.util.NClock;

import java.util.List;

/**
 * Application context that store all relevant information about application
 * execution mode, workspace, etc.
 *
 * @author thevpc
 * @app.category Application
 * @since 0.5.5
 */
public interface NApplicationContext extends NCmdLineConfigurable,NSessionProvider {
    /**
     * string that prefix each auto complete candidate
     */
    String AUTO_COMPLETE_CANDIDATE_PREFIX = "```error Candidate```: ";

    /**
     * create a new instance of {@link NApplicationContext}
     *
     * @param session         session context session. If null will consider {@code getSession()} that should not be null as well.
     * @param args            application arguments
     * @param startTime application start time
     * @param appClass        application class
     * @param storeId         application store id or null
     * @return new instance of {@link NApplicationContext}
     */
    static NApplicationContext of(String[] args, NClock startTime, Class appClass, String storeId, NSession session) {
        return NApplicationContexts.of(session).create(args, startTime, appClass, storeId);
    }

    /**
     * application execution mode
     *
     * @return application execution mode
     */
    NApplicationMode getMode();

    /**
     * application execution mode extra arguments
     *
     * @return application execution mode extra arguments
     */
    List<String> getModeArguments();

    /**
     * Auto complete instance associated with the
     * {@link NApplicationMode#AUTO_COMPLETE} mode
     *
     * @return Auto complete instance
     */
    NCmdLineAutoComplete getAutoComplete();

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NApplicationContext configure(boolean skipUnsupported, String... args);

    /**
     * calls configureFirst and ensure this is the last test.
     * If the argument is not supported, throws unsupported argument
     * by calling {@link NCmdLine#throwUnexpectedArgument()}
     *
     * @param commandLine arguments to configure with
     * @since 0.7.1
     */
    void configureLast(NCmdLine commandLine);

    /**
     * print application help to the default out ({@code getSession().out()})
     * print stream.
     */
    void printHelp();

    /**
     * application class reference
     *
     * @return application class reference
     */
    Class getAppClass();

    /**
     * current workspace
     *
     * @return current workspace
     */
    NWorkspace getWorkspace();

    /**
     * create a new session
     *
     * @return create a new session
     */
    NSession createSession();

    /**
     * update session
     *
     * @param session new session
     * @return {@code this} instance
     */
    NApplicationContext setSession(NSession session);

    /**
     * path to the apps folder of this application
     *
     * @return path to the apps folder of this application
     */
    NPath getAppsFolder();

    /**
     * path to the configuration folder of this application
     *
     * @return path to the configuration folder of this application
     */
    NPath getConfigFolder();

    /**
     * path to the log folder of this application
     *
     * @return path to the log folder of this application
     */
    NPath getLogFolder();

    /**
     * path to the temporary files folder of this application
     *
     * @return path to the temporary files folder of this application
     */
    NPath getTempFolder();

    /**
     * path to the variable files (aka /var in POSIX systems) folder of this
     * application
     *
     * @return path to the variable files (aka /var in POSIX systems) folder of
     * this application
     */
    NPath getVarFolder();

    /**
     * path to the libraries files (non applications) folder of this application
     *
     * @return path to the libraries files (non applications) folder of this
     * application
     */
    NPath getLibFolder();

    /**
     * path to the temporary run files (non essential sockets etc...) folder of
     * this application
     *
     * @return path to the temporary run files (non essential sockets etc...)
     * folder of this application
     */
    NPath getRunFolder();

    /**
     * path to the cache files folder of this application
     *
     * @return path to the cache files folder of this application
     */
    NPath getCacheFolder();

    NPath getVersionFolder(NStoreLocation location, String version);

    NPath getSharedAppsFolder();

    NPath getSharedConfigFolder();

    NPath getSharedLogFolder();

    NPath getSharedTempFolder();

    NPath getSharedVarFolder();

    NPath getSharedLibFolder();

    NPath getSharedRunFolder();

    NPath getSharedFolder(NStoreLocation location);

    /**
     * application nuts id
     *
     * @return application nuts id
     */
    NId getAppId();

    /**
     * application version
     *
     * @return application version
     */
    NVersion getAppVersion();

    /**
     * application arguments
     *
     * @return application arguments
     */
    List<String> getArguments();

    /**
     * application start time in milli-seconds
     *
     * @return application start time in milli-seconds
     */
    NClock getStartTime();

    /**
     * previous version (applicable in update mode)
     *
     * @return previous version
     */
    NVersion getAppPreviousVersion();

    /**
     * a new instance of command line arguments to process filled
     * with application's arguments.
     *
     * @return a new instance of command line arguments to process
     */
    NCmdLine getCommandLine();

    /**
     * create new NutsCommandLine and consume it with the given processor.
     * This method is equivalent to the following code
     * <pre>
     * getCommandLine().process(commandLineProcessor, new DefaultNCmdLineContext(this));
     * </pre>
     *
     * @param commandLineProcessor commandLineProcessor
     * @throws NullPointerException if the commandLineProcessor is null
     * @since 0.7.0
     */
    void processCommandLine(NCmdLineProcessor commandLineProcessor);

    /**
     * application store folder path for the given {@code location}
     *
     * @param location location type
     * @return application store folder path for the given {@code location}
     */
    NPath getFolder(NStoreLocation location);

    /**
     * return true if {@code getAutoComplete()==null }
     *
     * @return true if {@code getAutoComplete()==null }
     */
    boolean isExecMode();

    NAppStoreLocationResolver getStoreLocationResolver();

    NApplicationContext setAppVersionStoreLocationSupplier(NAppStoreLocationResolver appVersionStoreLocationSupplier);
}
