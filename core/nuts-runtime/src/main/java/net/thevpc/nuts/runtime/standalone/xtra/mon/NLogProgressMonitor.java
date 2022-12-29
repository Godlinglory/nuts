package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.*;

import java.text.DecimalFormat;
import java.util.logging.Level;

public class NLogProgressMonitor implements NProgressHandler {
    private static NMemorySizeFormat MF = NMemorySizeFormat.FIXED;
    public static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#00.00%");
    private NLogger logger;

    public NLogProgressMonitor(NLogger logger, NSession session) {
        if (logger == null) {
            logger = NLogger.of(NLogProgressMonitor.class,session);
        }
        this.logger = logger;
    }

    @Override
    public void onEvent(NProgressHandlerEvent event) {
        NMsg message = event.getModel().getMessage();
        NLoggerOp w = logger.with().level(message.getLevel() == null ? Level.INFO : message.getLevel());
        w.log(message);
    }

}
