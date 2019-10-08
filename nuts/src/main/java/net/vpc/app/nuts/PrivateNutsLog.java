package net.vpc.app.nuts;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrivateNutsLog {
    public static final String START = "START";
    public static final String READ = "READ";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAIL = "FAIL";
    public static final String CACHE = "CACHE";
    public static final String WARNING = "WARNING";
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());
    private static final Pattern LOG_PARAM_PATTERN = Pattern.compile("\\{(?<v>[0-9]+)}");
    private NutsWorkspaceOptions options;

    public void log(Level lvl, String logVerb, String message) {
        log(lvl, logVerb, message, new Object[0]);
    }

    public void log(Level lvl, String logVerb, String message, Object object) {
        log(lvl, logVerb, message, new Object[]{object});
    }

    public void log(Level lvl, String logVerb, String message, Object[] objects) {
        if (isLoggable(lvl)) {
            Matcher m = LOG_PARAM_PATTERN.matcher(message);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String v = m.group("v");
                m.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(objects[Integer.parseInt(v)])));
            }
            m.appendTail(sb);
            doLog(lvl, logVerb, sb.toString());
        }
//        LOG.log(lvl, s, objects);
    }

    public void log(Level lvl, String message, Throwable err) {
        if (isLoggable(lvl)) {
            doLog(lvl, FAIL, message);
            err.printStackTrace(System.err);
        }
        //LOG.log(lvl, s, err);
    }

    private void doLog(Level lvl, String logVerb, String s) {
//        System.err.printf("%s %-6s %-7s : [%-7s] %s%n", DEFAULT_DATE_TIME_FORMATTER.format(Instant.now()), lvl, "BOOT", logVerb, s);
        System.err.printf("%s %-6s %-7s : %s%n", DEFAULT_DATE_TIME_FORMATTER.format(Instant.now()), lvl, logVerb, s);
    }

    public boolean isLoggable(Level lvl) {
        if (options == null) {
            return false;
        }
        if (/*options.isDebug() && */options.getLogConfig() != null && lvl.intValue() >= options.getLogConfig().getLogTermLevel().intValue()) {
            return true;
        }
        return false;
    }

    public void setOptions(NutsWorkspaceOptions options) {
        this.options = options;
    }
}
