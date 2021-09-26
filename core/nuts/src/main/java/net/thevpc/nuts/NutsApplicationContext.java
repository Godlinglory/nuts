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


/**
 * Application context that store all relevant information about application
 * execution mode, workspace, etc.
 *
 * @author thevpc
 * @since 0.5.5
 * @app.category Application
 */
public interface NutsApplicationContext extends NutsCommandLineConfigurable {

    /**
     * string that prefix each auto complete candidate
     */
    String AUTO_COMPLETE_CANDIDATE_PREFIX = "```error Candidate```: ";

    /**
     * application execution mode
     *
     * @return application execution mode
     */
    NutsApplicationMode getMode();

    /**
     * application execution mode extra arguments
     *
     * @return application execution mode extra arguments
     */
    String[] getModeArguments();

    /**
     * Auto complete instance associated with the
     * {@link NutsApplicationMode#AUTO_COMPLETE} mode
     *
     * @return Auto complete instance
     */
    NutsCommandAutoComplete getAutoComplete();

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsApplicationContext configure(boolean skipUnsupported, String... args);

    /**
     * calls configureFirst and ensure this is the last test.
     * If the argument is not supported, throws unsupported argument
     * by calling {@link NutsCommandLine#unexpectedArgument()}
     *
     * @param commandLine arguments to configure with
     * @since 0.7.1
     */
    void configureLast(NutsCommandLine commandLine);

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
    NutsWorkspace getWorkspace();

    /**
     * current session
     *
     * @return current session
     */
    NutsSession getSession();

    /**
     * update session
     *
     * @param session new session
     * @return {@code this} instance
     */
    NutsApplicationContext setSession(NutsSession session);

    /**
     * path to the apps folder of this application
     *
     * @return path to the apps folder of this application
     */
    String getAppsFolder();

    /**
     * path to the configuration folder of this application
     *
     * @return path to the configuration folder of this application
     */
    String getConfigFolder();

    /**
     * path to the log folder of this application
     *
     * @return path to the log folder of this application
     */
    String getLogFolder();

    /**
     * path to the temporary files folder of this application
     *
     * @return path to the temporary files folder of this application
     */
    String getTempFolder();

    /**
     * path to the variable files (aka /var in POSIX systems) folder of this
     * application
     *
     * @return path to the variable files (aka /var in POSIX systems) folder of
     * this application
     */
    String getVarFolder();

    /**
     * path to the libraries files (non applications) folder of this application
     *
     * @return path to the libraries files (non applications) folder of this
     * application
     */
    String getLibFolder();

    /**
     * path to the temporary run files (non essential sockets etc...) folder of
     * this application
     *
     * @return path to the temporary run files (non essential sockets etc...)
     * folder of this application
     */
    String getRunFolder();

    /**
     * path to the cache files folder of this application
     *
     * @return path to the cache files folder of this application
     */
    String getCacheFolder();

    String getVersionFolderFolder(NutsStoreLocation location, String version);

    String getSharedAppsFolder();

    String getSharedConfigFolder();

    String getSharedLogFolder();

    String getSharedTempFolder();

    String getSharedVarFolder();

    String getSharedLibFolder();

    String getSharedRunFolder();

    String getSharedFolder(NutsStoreLocation location);

    /**
     * application nuts id
     *
     * @return application nuts id
     */
    NutsId getAppId();

    /**
     * application version
     *
     * @return application version
     */
    NutsVersion getAppVersion();

    /**
     * application arguments
     *
     * @return application arguments
     */
    String[] getArguments();

    /**
     * application start time in milli-seconds
     *
     * @return application start time in milli-seconds
     */
    long getStartTimeMillis();

    /**
     * previous version (applicable in update mode)
     *
     * @return previous version
     */
    NutsVersion getAppPreviousVersion();

    /**
     * a new instance of command line arguments to process filled
     * with application's arguments.
     *
     * @return a new instance of command line arguments to process
     */
    NutsCommandLine getCommandLine();

    /**
     * create new NutsCommandLine and consume it with the given processor.
     * This method is equivalent to the following code
     * <pre>
     *         NutsCommandLine cmdLine=getCommandLine();
     *         NutsArgument a;
     *         while (cmdLine.hasNext()) {
     *             if (!this.configureFirst(cmdLine)) {
     *                 a = cmdLine.peek();
     *                 if(a.isOption()){
     *                     if(!commandLineProcessor.processOption(a,cmdLine)){
     *                         cmdLine.unexpectedArgument();
     *                     }
     *                 }else{
     *                     if(!commandLineProcessor.processNonOption(a,cmdLine)){
     *                         cmdLine.unexpectedArgument();
     *                     }
     *                 }
     *             }
     *         }
     *         // test if application is running in exec mode
     *         // (and not in autoComplete mode)
     *         if (cmdLine.isExecMode()) {
     *             //do the good staff here
     *             commandLineProcessor.exec();
     *         }
     * </pre>
     *
     * This as an example of its usage
     * <pre>
     *     applicationContext.processCommandLine(new NutsCommandLineProcessor() {
     *             HLCWithOptions hl = new HL().withOptions();
     *             boolean noMoreOptions=false;
     *             &#64;Override
     *             public boolean processOption(NutsArgument argument, NutsCommandLine cmdLine) {
     *                 if(!noMoreOptions){
     *                     return false;
     *                 }
     *                 switch (argument.getKey().getString()) {
     *                     case "--clean": {
     *                         hl.clean(cmdLine.nextBoolean().getBooleanValue());
     *                         return true;
     *                     }
     *                     case "-i":
     *                     case "--incremental":{
     *                         hl.setIncremental(cmdLine.nextBoolean().getBooleanValue());
     *                         return true;
     *                     }
     *                     case "-r":
     *                     case "--root":{
     *                         hl.setProjectRoot(cmdLine.nextString().getStringValue());
     *                         return true;
     *                     }
     *                 }
     *                 return false;
     *             }
     *
     *             &#64;Override
     *             public boolean processNonOption(NutsArgument argument, NutsCommandLine cmdLine) {
     *                 String s = argument.getString();
     *                 if(isURL(s)){
     *                     hl.includeFileURL(s);
     *                 }else{
     *                     hl.includeFile(s);
     *                 }
     *                 noMoreOptions=true;
     *                 return true;
     *             }
     *
     *             private boolean isURL(String s) {
     *                 return
     *                         s.startsWith("file:")
     *                         ||s.startsWith("http:")
     *                         ||s.startsWith("https:")
     *                         ;
     *             }
     *
     *             &#64;Override
     *             public void exec() {
     *                 hl.compile();
     *             }
     *         });
     * </pre>
     *
     * @param commandLineProcessor commandLineProcessor
     * @throws NullPointerException if the commandLineProcessor is null
     * @since 0.7.0
     */
    void processCommandLine(NutsCommandLineProcessor commandLineProcessor);

    /**
     * application store folder path for the given {@code location}
     * @param location location type
     * @return application store folder path for the given {@code location}
     */
    String getFolder(NutsStoreLocation location);

    /**
     * return true if {@code getAutoComplete()==null }
     * @return true if {@code getAutoComplete()==null }
     */
    boolean isExecMode();

    NutsAppStoreLocationResolver getStoreLocationResolver();

    NutsApplicationContext setAppVersionStoreLocationSupplier(NutsAppStoreLocationResolver appVersionStoreLocationSupplier);
}
