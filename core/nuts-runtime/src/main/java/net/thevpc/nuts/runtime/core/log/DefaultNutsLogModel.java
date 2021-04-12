/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.core.log;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import net.thevpc.nuts.NutsIOManager;
import net.thevpc.nuts.NutsLogConfig;
import net.thevpc.nuts.NutsLogger;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsSessionTerminal;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsWorkspaceInitInformation;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsLogModel {
     private NutsWorkspace workspace;

    private PrintStream out=System.err;
    private Handler consoleHandler;
    private Handler fileHandler;
    private NutsLogConfig logConfig=new NutsLogConfig();
    private List<Handler> extraHandlers = new ArrayList<>();
    private static Handler[] EMPTY = new Handler[0];
    private Path logFolder;
    private NutsSession defaultSession;
    private Map<String,NutsLogger> loaded=new LinkedHashMap<>();

    public DefaultNutsLogModel(NutsWorkspace ws, NutsWorkspaceInitInformation options) {
        this.workspace = ws;
        logFolder= Paths.get(options.getStoreLocation(NutsStoreLocation.LOG));
        NutsLogConfig lc = options.getOptions().getLogConfig();
        if(lc!=null){
            if(lc.getLogFileLevel()!=null){
                logConfig.setLogFileLevel(lc.getLogFileLevel());
            }
            if(lc.getLogTermLevel()!=null){
                logConfig.setLogTermLevel(lc.getLogTermLevel());
            }
            logConfig.setLogFileName(lc.getLogFileName());
            logConfig.setLogFileCount(lc.getLogFileCount());
            logConfig.setLogFileBase(lc.getLogFileBase());
            logConfig.setLogFileSize(lc.getLogFileSize());
            logConfig.setLogInherited(lc.isLogInherited());
        }
    }

    public NutsSession getDefaultSession() {
        return defaultSession;
    }

    public void setDefaultSession(NutsSession defaultSession) {
        this.defaultSession = defaultSession;
    }

    
    public Handler[] getHandlers() {
        if (extraHandlers.isEmpty()) {
            return EMPTY;
        }
        return extraHandlers.toArray(EMPTY);
    }

    
    public void removeHandler(Handler handler) {
        extraHandlers.remove(handler);
    }

    
    public void addHandler(Handler handler) {
        if (handler != null) {
            extraHandlers.add(handler);
        }
    }

    
    public Handler getTermHandler() {
        return consoleHandler;
    }

    
    public Handler getFileHandler() {
        return fileHandler;
    }

    
    public NutsLogger of(String name,NutsSession session) {
        NutsLogger y = loaded.get(name);
        if(y==null) {
            if(session==null){
                session=defaultSession;
            }
            y= new DefaultNutsLogger(workspace, session, name);
            loaded.put(name,y);
        }
        return y;
    }

    
    public NutsLogger of(Class clazz,NutsSession session) {
        NutsLogger y = loaded.get(clazz.getName());
        if(y==null) {
            if(session==null){
                session=defaultSession;
            }
            y= new DefaultNutsLogger(workspace, session, clazz);
            loaded.put(clazz.getName(),y);
        }
        return y;
    }

    
    public Level getTermLevel() {
        return this.logConfig.getLogTermLevel();
    }

    
    public void setTermLevel(Level level, NutsSession session) {
        if (level == null) {
            level = Level.INFO;
        }
        this.logConfig.setLogFileLevel(level);
//        session = CoreNutsUtils.validate(session, workspace);
        if (consoleHandler != null) {
            consoleHandler.setLevel(level);
        }
    }

    
    public Level getFileLevel() {
        return this.logConfig.getLogFileLevel();
    }

    
    public void setFileLevel(Level level, NutsSession session) {
        if (level == null) {
            level = Level.INFO;
        }
        this.logConfig.setLogFileLevel(level);
//        session = CoreNutsUtils.validate(session, workspace);
        if (fileHandler != null) {
            fileHandler.setLevel(level);
        }

    }

    public void updateHandlers(LogRecord record) {
        updateTermHandler(record);
        updateFileHandler(record);
    }

    public void updateFileHandler(LogRecord record) {
        if(fileHandler==null){
            if(logConfig.getLogFileLevel()!=Level.OFF){
                if(fileHandler==null){
                    try {
                        fileHandler = NutsLogFileHandler.create(workspace, logConfig, true,logFolder);
                        fileHandler.setLevel(logConfig.getLogFileLevel());
                    } catch (Exception ex) {
                        Logger.getLogger(DefaultNutsLogManager.class.getName()).log(Level.FINE, "Unable to create file handler", ex);
                    }
                }
            }
        }
    }

    public void updateTermHandler(LogRecord record) {
        PrintStream out=null;
        if (record instanceof NutsLogRecord) {
            NutsLogRecord rr = (NutsLogRecord) record;
            NutsSession session = rr.getSession();
            NutsWorkspace ws = rr.getWorkspace();
            if (session != null) {
                out = session.out();
            } else {
                NutsIOManager io = ws.io();
                if(io!=null){
                    NutsSessionTerminal term = ws.term().setSession(
                            NutsWorkspaceUtils.defaultSession(ws)
                    ).getTerminal();
                    if(term!=null){
                        out = term.out();
                    }
                }
            }
        }
        if(out==null){
            out=System.err;
        }
        if(out!=this.out || consoleHandler==null){
            this.out=out;
            if(consoleHandler!=null){
                if(consoleHandler instanceof NutsLogConsoleHandler){
                    ((NutsLogConsoleHandler) consoleHandler).setOutputStream(out,false);
                    consoleHandler.setLevel(logConfig.getLogTermLevel());
                }else {
                    consoleHandler.flush(); // do not close!!
                    consoleHandler.setLevel(logConfig.getLogTermLevel());
                }
            }else {
                consoleHandler = new NutsLogConsoleHandler(out, false);
                consoleHandler.setLevel(logConfig.getLogTermLevel());
            }
        }
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }
    
}
