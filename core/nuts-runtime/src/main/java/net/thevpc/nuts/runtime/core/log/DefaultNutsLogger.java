package net.thevpc.nuts.runtime.core.log;

import net.thevpc.nuts.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Supplier;
import java.util.logging.*;

public class DefaultNutsLogger implements NutsLogger {
    private NutsWorkspace workspace;
    private NutsSession session;
    private boolean defaultFormatted=true;
    private long defaultTime;
    private NutsTextFormatStyle defaultStyle= NutsTextFormatStyle.JSTYLE;
    private Logger log;
    private static final int offValue = Level.OFF.intValue();
    private LinkedList<LogRecord> suspendedTerminalRecords = new LinkedList<>();
    private int suspendedMax = 100;
    private boolean suspendTerminalMode = false;

    public DefaultNutsLogger(NutsWorkspace workspace, NutsSession session,Class log) {
        this(workspace,session, log.getName());
    }

    public DefaultNutsLogger(NutsWorkspace workspace, NutsSession session,String log) {
        this(workspace, session, Logger.getLogger(log));
    }

    public DefaultNutsLogger(NutsWorkspace workspace, NutsSession session,Logger log) {
        this.workspace = workspace;
        this.log = log;
        this.session = session;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public NutsSession getSession() {
        return session;
    }

    public Filter getFilter() {
        //may have a filter ?
        return null;
    }

    private boolean isLoggable(Level level, Level current) {
        int levelValue = current.intValue();
        if (!(level.intValue() < levelValue || levelValue == offValue)) {
            return true;
        }
        return false;
    }

    public boolean isLoggable(Level level) {
        NutsLogManager _log = getSession().log();
        if (isLoggable(level, _log.getTermLevel())) {
            return true;
        }
        if (isLoggable(level, _log.getFileLevel())) {
            return true;
        }
        for (Handler handler : _log.getHandlers()) {
            if (isLoggable(level, handler.getLevel())) {
                return true;
            }
        }

        if (log.isLoggable(level)) {
            return true;
        }
        return false;
    }

    public void log(Level level, String msg, Throwable thrown) {
        log(session, level, msg, thrown);
    }

    public void log(NutsSession session, Level level, String msg, Throwable thrown) {
        if (!isLoggable(level)) {
            return;
        }
        if(session==null){
            session=this.session;
        }
        LogRecord lr = new NutsLogRecord(session, level, NutsLogVerb.FAIL, msg, DefaultNutsLoggerOp.OBJECTS0, defaultFormatted, defaultTime,defaultStyle);
        lr.setThrown(thrown);
        doLog(lr);
    }

    public void log(Level level, NutsLogVerb verb, String msg) {
        log(session,level, verb, msg);
    }

    public void log(NutsSession session,Level level, NutsLogVerb verb, String msg) {
        if (!isLoggable(level)) {
            return;
        }
        if(session==null){
            session=this.session;
        }
        LogRecord lr = new NutsLogRecord(session, level, verb, msg, DefaultNutsLoggerOp.OBJECTS0, defaultFormatted, defaultTime,defaultStyle);
        doLog(lr);
    }

    public void log(Level level, NutsLogVerb verb, Supplier<String> msgSupplier) {
        log(session, level, verb, msgSupplier);
    }
    public void log(NutsSession session,Level level, NutsLogVerb verb, Supplier<String> msgSupplier) {
        if (!isLoggable(level)) {
            return;
        }
        if(session==null){
            session=this.session;
        }
        LogRecord lr = new NutsLogRecord(session, level, verb, msgSupplier.get(), DefaultNutsLoggerOp.OBJECTS0, defaultFormatted, defaultTime,defaultStyle);
        doLog(lr);
    }

    public void log(Level level, NutsLogVerb verb, String msg, Object[] params) {
        log(session,level, verb, msg, params);
    }

    public void log(NutsSession session,Level level, NutsLogVerb verb, String msg, Object[] params) {
        if (!isLoggable(level)) {
            return;
        }
        if(session==null){
            session=this.session;
        }
        LogRecord lr = new NutsLogRecord(session,  level, verb, msg, params, defaultFormatted, defaultTime,defaultStyle);
        lr.setParameters(params);
        doLog(lr);
    }

    // private support method for logging.
    private void doLog(LogRecord record) {
        record.setLoggerName(log.getName());
        //ignore resource bundling...
        if (!isLoggable(record)) {
            return;
        }
        log0(record);
    }

    @Override
    public NutsLoggerOp with() {
        return new DefaultNutsLoggerOp(this).session(getSession());
    }

    private boolean isLoggable(LogRecord record) {
        Filter theFilter = getFilter();
        if (theFilter != null && !theFilter.isLoggable(record)) {
            return false;
        }
        return true;
    }

    public void log(LogRecord record) {
        if (!isLoggable(record.getLevel())) {
            return;
        }
        if (!isLoggable(record)) {
            return;
        }
        //compile for once the parameter based text!
        //record=NutsLogRichFormatter.compile(record);
        log0(record);
    }

    public void suspendTerminal() {
        suspendTerminalMode = true;
    }


    /**
     * Log a LogRecord.
     * <br>
     * All the other logging methods in this class call through
     * this method to actually perform any logging.  Subclasses can
     * override this single method to capture all log activity.
     *
     * @param record the LogRecord to be published
     */
    private void log0(LogRecord record) {
        DefaultNutsLogManager logManager = (DefaultNutsLogManager) getSession().log();
        logManager.getModel().updateHandlers(record);
        Handler ch = logManager.getModel().getTermHandler();
        if (ch != null) {
            if (ch.isLoggable(record)) {
                if (suspendTerminalMode) {
                    suspendedTerminalRecords.add(record);
                    if (suspendedTerminalRecords.size() > suspendedMax) {
                        LogRecord r = suspendedTerminalRecords.removeFirst();
                        logManager.getModel().updateTermHandler(r);
                        ch.publish(r);
                        ch.flush();
                    }
                } else {
                    ch.publish(record);
                    ch.flush();
                }
            }
        }
        Handler fh = logManager.getModel().getFileHandler();
        if (fh != null) {
            if (fh.isLoggable(record)) {
                fh.publish(record);
            }
        }
        for (Handler handler : logManager.getModel().getHandlers()) {
            if (handler.isLoggable(record)) {
                handler.publish(record);
            }
        }
        this.log.log(record);
    }

    public void resumeTerminal(NutsSession session) {
        suspendTerminalMode = false;
        DefaultNutsLogManager logManager = (DefaultNutsLogManager) session.log().setSession(session);
        Handler ch = logManager.getTermHandler();
        for (Iterator<LogRecord> iterator = suspendedTerminalRecords.iterator(); iterator.hasNext(); ) {
            LogRecord r = iterator.next();
            iterator.remove();
            logManager.getModel().updateHandlers(r);
            if (ch != null) {
                ch.publish(r);
            }
        }
        if (ch != null) {
            ch.flush();
        }
    }
}
