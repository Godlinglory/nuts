package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLineAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCmdLineHistory;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NAnsiTermHelper;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.spi.NSystemTerminalBaseImpl;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyles;

import java.io.InputStream;

class AnsiNPrintStreamTerminalBase extends NSystemTerminalBaseImpl {
    private NPrintStream out;
    private NCmdLineHistory history;
    private String commandHighlighter;
    private NCmdLineAutoCompleteResolver commandAutoCompleteResolver;

    public AnsiNPrintStreamTerminalBase(NPrintStream out) {
        this.out = out;
    }

    @Override
    public String readLine(NPrintStream out, NMsg message, NSession session) {
        return null;
    }

    @Override
    public char[] readPassword(NPrintStream out, NMsg message, NSession session) {
        return new char[0];
    }

    @Override
    public InputStream getIn() {
        return null;
    }

    @Override
    public NPrintStream getOut() {
        return out;
    }

    @Override
    public NPrintStream getErr() {
        return null;
    }

    @Override
    public NSystemTerminalBase setCommandAutoCompleteResolver(NCmdLineAutoCompleteResolver autoCompleteResolver) {
        this.commandAutoCompleteResolver = autoCompleteResolver;
        return this;
    }

    @Override
    public NCmdLineHistory getCommandHistory() {
        return history;
    }

    @Override
    public NSystemTerminalBase setCommandHistory(NCmdLineHistory history) {
        this.history = history;
        return this;
    }

    @Override
    public String getCommandHighlighter() {
        return commandHighlighter;
    }

    @Override
    public NSystemTerminalBase setCommandHighlighter(String commandHighlighter) {
        this.commandHighlighter = commandHighlighter;
        return this;
    }

    public NCmdLineAutoCompleteResolver getAutoCompleteResolver() {
        return commandAutoCompleteResolver;
    }

    @Override
    public Object run(NTerminalCommand command, NPrintStream printStream, NSession session) {
        String s = NAnsiTermHelper.of(session).command(command, session);
        if (s != null) {
            byte[] bytes = s.getBytes();
            out.write(bytes, 0, bytes.length);
            out.flush();
        }
        return null;
    }

    @Override
    public void setStyles(NTextStyles styles, NPrintStream printStream, NSession session) {
        String s = NAnsiTermHelper.of(session).styled(styles, session);
        if (s != null) {
            byte[] bytes = s.getBytes();
            out.write(bytes, 0, bytes.length);
            out.flush();
        }
    }
}
