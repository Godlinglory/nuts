package net.thevpc.nuts.runtime.standalone.io.terminals;

import net.thevpc.nuts.NutsExecutionType;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

public class CoreAnsiTermHelper {
    public static boolean isXTerm(NutsSession session) {
        try {
            String str = "cols";
            session.exec().setExecutionType(NutsExecutionType.SYSTEM)
                    .grabOutputString()
                    .addCommand("tput", str)
                    .setFailFast(true)
                    .getOutputString();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static NutsSystemTerminalBase.Size evalSize(NutsSession session) {
        Integer c = NutsApiUtils.parseInt(evalCapability("cols", session),null,null);
        Integer l = NutsApiUtils.parseInt(evalCapability("lines", session),null,null);
        if (c != null && l != null) {
            return new NutsSystemTerminalBase.Size(c, l);
        }
        return null;
    }

    public static NutsSystemTerminalBase.Cursor evalCursor(NutsSession session) {
        String c = evalCapability("u7", session);
        if (c != null) {
            return null;
        }
        return null;
    }

    public static String evalCapability(String str, NutsSession session) {
        try {
            String d = session.exec().setExecutionType(NutsExecutionType.SYSTEM)
                    .grabOutputString()
                    .addCommand("tput", str)
                    .getOutputString();
            String s = d.trim();
            if (s.isEmpty()) {
                return null;
            }
            //add 500 of sleep time!
            d = session.exec().setExecutionType(NutsExecutionType.SYSTEM)
                    .grabOutputString()
                    .addCommand("tput", str)
                    .setSleepMillis(500)
                    .getOutputString();
            s = d.trim();
            if (s.isEmpty()) {
                return null;
            }
            return s;
        } catch (Exception ex) {
            return null;
        }
    }
}
