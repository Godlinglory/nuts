package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NutsCommandHistory;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsPrintStreams;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.runtime.standalone.io.printstream.NutsPrintStreamSystem;
import net.thevpc.nuts.runtime.standalone.util.NutsCachedValue;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NutsTerminalCommand;
import net.thevpc.nuts.text.NutsTextStyles;
import net.thevpc.nuts.util.NutsLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

@NutsComponentScope(NutsComponentScopeType.PROTOTYPE)
public class DefaultNutsSystemTerminalBase extends NutsSystemTerminalBaseImpl {
    public static final int THIRTY_SECONDS = 30000;
    NutsCachedValue<Cursor> termCursor;
    NutsCachedValue<Size> termSize;

    private NutsLogger LOG;
    private Scanner scanner;
    private NutsTerminalMode outMode = NutsTerminalMode.FORMATTED;
    private NutsTerminalMode errMode = NutsTerminalMode.FORMATTED;
    private NutsPrintStream out;
    private NutsPrintStream err;
    private InputStream in;
    private NutsWorkspace workspace;
    private NutsSession session;
    private NutsCommandHistory history;
    private String commandHighlighter;
    private NutsCommandAutoCompleteResolver commandAutoCompleteResolver;

    public DefaultNutsSystemTerminalBase() {

    }

    private NutsLogger _LOG() {
        if (LOG == null && session != null) {
            LOG = NutsLogger.of(NutsSystemTerminalBase.class, session);
        }
        return LOG;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        this.session = criteria.getSession();
        this.workspace = session.getWorkspace();
        NutsWorkspaceOptions options = session.boot().getBootOptions();
        NutsTerminalMode terminalMode = options.getTerminalMode().orElse(NutsTerminalMode.DEFAULT);
        NutsBootTerminal bootStdFd = NutsWorkspaceExt.of(session).getModel().bootModel.getBootTerminal();
        if (terminalMode == NutsTerminalMode.DEFAULT) {
            if (options.getBot().orElse(false) || !bootStdFd.getFlags().contains("ansi")) {
                terminalMode = NutsTerminalMode.FILTERED;
            } else {
                terminalMode = NutsTerminalMode.FORMATTED;
            }
        }
        if(bootStdFd.getFlags().contains("tty")) {
            termCursor =new NutsCachedValue<>(CoreAnsiTermHelper::evalCursor, THIRTY_SECONDS);
            termSize =new NutsCachedValue<>(CoreAnsiTermHelper::evalSize, THIRTY_SECONDS);
        }else{
            termCursor =new NutsCachedValue<>(session -> null, THIRTY_SECONDS);
            termSize =new NutsCachedValue<>(session -> null, THIRTY_SECONDS);
        }
        this.out = new NutsPrintStreamSystem(bootStdFd.getOut(), null, null, bootStdFd.getFlags().contains("ansi"),
                session, this).setMode(terminalMode);
        this.err = new NutsPrintStreamSystem(bootStdFd.getErr(), null, null, bootStdFd.getFlags().contains("ansi"),
                session, this).setMode(terminalMode);
        this.in = bootStdFd.getIn();
        this.scanner = new Scanner(this.in);
        return DEFAULT_SUPPORT;
    }

    @Override
    public String readLine(NutsPrintStream out, NutsMessage message, NutsSession session) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = NutsPrintStreams.of(session).stdout();
        }
        if (message != null) {
            out.printf("%s", message);
            out.flush();
        }
        return scanner.nextLine();
    }

    @Override
    public char[] readPassword(NutsPrintStream out, NutsMessage message, NutsSession session) {
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = NutsPrintStreams.of(session).stdout();
        }
        if (message != null) {
            out.printf("%s", message);
            out.flush();
        }
        return scanner.nextLine().toCharArray();
    }

    @Override
    public InputStream getIn() {
        return this.in;
    }

    @Override
    public NutsPrintStream getOut() {
        return this.out;
    }

    @Override
    public NutsPrintStream getErr() {
        return this.err;
    }

    @Override
    public NutsCommandAutoCompleteResolver getAutoCompleteResolver() {
        return commandAutoCompleteResolver;
    }

    @Override
    public boolean isAutoCompleteSupported() {
        return false;
    }

    @Override
    public NutsSystemTerminalBase setCommandAutoCompleteResolver(NutsCommandAutoCompleteResolver autoCompleteResolver) {
        this.commandAutoCompleteResolver = autoCompleteResolver;
        return this;
    }

    @Override
    public NutsCommandHistory getCommandHistory() {
        return history;
    }

    @Override
    public NutsSystemTerminalBase setCommandHistory(NutsCommandHistory history) {
        this.history = history;
        return this;
    }

    @Override
    public String getCommandHighlighter() {
        return commandHighlighter;
    }

    @Override
    public DefaultNutsSystemTerminalBase setCommandHighlighter(String commandHighlighter) {
        this.commandHighlighter = commandHighlighter;
        return this;
    }

    @Override
    public Object run(NutsTerminalCommand command, NutsSession session) {
        switch (command.getName()) {
            case NutsTerminalCommand.Ids.GET_CURSOR: {
                return termCursor.getValue(session);
            }
            case NutsTerminalCommand.Ids.GET_SIZE: {
                return termSize.getValue(session);
            }
        }
        String s = NutsAnsiTermHelper.of(session).command(command, session);
        if(s!=null) {
            try {
                NutsBootTerminal bootStdFd = session.boot().getBootTerminal();
                bootStdFd.getOut().write(s.getBytes());
            } catch (IOException e) {
                throw new NutsIOException(session, e);
            }
        }
        return null;
    }

    public void setStyles(NutsTextStyles styles, NutsSession session) {
        String s = NutsAnsiTermHelper.of(session).styled(styles, session);
        if (s != null) {
            try {
                NutsBootTerminal bootStdFd = session.boot().getBootTerminal();
                bootStdFd.getOut().write(s.getBytes());
            } catch (IOException e) {
                throw new NutsIOException(session, e);
            }
        }
    }

    //    @Override
//    public int getColumns() {
//        int tputCallTimeout = session.boot().getBootCustomArgument("---nuts.term.tput.call.timeout").getValue().getInt(60);
//        Integer w = session.boot().getBootCustomArgument("---nuts.term.width").getValue().getInt(null);
//        if (w == null) {
//            if (tput_cols == null) {
//                tput_cols = new NutsCachedValue<>(new DefaultAnsiEscapeCommand.TputEvaluator(session), tputCallTimeout);
//            }
//            Integer v = tput_cols.getValue();
//            return v == null ? -1 : v;
//        }
//        return -1;
//    }

}
