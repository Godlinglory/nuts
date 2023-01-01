/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
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
package net.thevpc.nuts.ext.term;

import java.awt.Color;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCommandHistory;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NLogger;
import net.thevpc.nuts.util.NLoggerVerb;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.util.function.IntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jline.reader.impl.LineReaderImpl;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * Created by vpc on 2/20/17.
 */
@NComponentScope(NComponentScopeType.PROTOTYPE)
public class NJLineTerminal extends NSystemTerminalBaseImpl {

    private static final Logger LOG = Logger.getLogger(NJLineTerminal.class.getName());
    private Terminal terminal;
    private LineReader reader;
    private NOutStream out;
    private NOutStream err;
    private InputStream in;
    private NCommandAutoCompleteResolver autoCompleteResolver;
    private NCommandHistory commandHistory;
    private String commandHighlighter;

    public NJLineTerminal() {
    }

    private AttributedString toAttributedString(NText n, NTextStyles styles, NSession session) {
        switch (n.getType()) {
            case PLAIN: {
                styles = NTexts.of(session).getTheme().toBasicStyles(styles, session);
                NTextPlain p = (NTextPlain) n;
                if (styles.isPlain()) {
                    return new AttributedString(p.getText());
                } else {
                    AttributedStyle s = AttributedStyle.DEFAULT;
                    for (int i = 0; i < styles.size(); i++) {
                        NTextStyle ii = styles.get(i);
                        switch (ii.getType()) {
                            case BACK_COLOR: {
                                s = s.background(ii.getVariant());
                                break;
                            }
                            case BACK_TRUE_COLOR: {
                                Color c = new Color(ii.getVariant());
                                s = s.background(c.getRed(), c.getGreen(), c.getBlue());
                                break;
                            }
                            case FORE_COLOR: {
                                s = s.foreground(ii.getVariant());
                                break;
                            }
                            case FORE_TRUE_COLOR: {
                                Color c = new Color(ii.getVariant());
                                s = s.foreground(c.getRed(), c.getGreen(), c.getBlue());
                                break;
                            }
                            case BLINK: {
                                s = s.blink();
                                break;
                            }
                            case UNDERLINED: {
                                s = s.underline();
                                break;
                            }
                            case STRIKED: {
                                s = s.crossedOut();
                                break;
                            }
                            case ITALIC: {
                                s = s.italic();
                                break;
                            }
                            case BOLD: {
                                s = s.bold();
                                break;
                            }
                        }
                    }
                    return new AttributedString(p.getText(), s);
                }
            }
            case ANCHOR: {
                return new AttributedString("");
            }
            case COMMAND: {
                return new AttributedString("");
            }
            case CODE: {
                NTextCode p = (NTextCode) n;
                NText nn = p.highlight(session);
                return toAttributedString(nn, NTextStyles.PLAIN, session);
            }
            case TITLE: {
                NTextTitle p = (NTextTitle) n;
                return toAttributedString(p.getChild(), NTextStyles.PLAIN, session);
            }
            case LINK: {
                NTextLink p = (NTextLink) n;
                return toAttributedString(
                        NTexts.of(session).ofPlain(p.getText()),
                        styles.append(NTextStyle.underlined()),
                        session);
            }
            case INCLUDE: {
                NTextLink p = (NTextLink) n;
                return toAttributedString(
                        NTexts.of(session).ofList(
                                NTexts.of(session).ofPlain("include"),
                                NTexts.of(session).ofPlain(p.getText())
                        ),
                        styles.append(NTextStyle.danger()),
                        session);
            }
            case LIST: {
                NTextList p = (NTextList) n;
                AttributedStringBuilder b = new AttributedStringBuilder();
                for (NText a : p) {
                    b.append(toAttributedString(a, styles, session));
                }
                return b.toAttributedString();
            }
            case STYLED: {
                NTextStyled p = (NTextStyled) n;
                if (styles.isPlain()) {
                    return toAttributedString(p.getChild(), p.getStyles(), session);
                } else {
                    return toAttributedString(
                            p.getChild(),
                            styles.append(p.getStyles()),
                            session);
                }
            }
        }
        return new AttributedString(n.toString());
    }

    public void prepare(NSession session) {
        if (terminal != null) {
            return;
        }
        TerminalBuilder builder = TerminalBuilder.builder();
        builder.streams(System.in, System.out);
        builder.system(true);
        builder.dumb(false);

        try {
            terminal = builder.build();
        } catch (Throwable ex) {
            //unable to create system terminal
            //Logger.getLogger(NutsJLineTerminal.class.getName()).log(Level.SEVERE, null, ex);
            throw new UncheckedIOException(new IOException("unable to create JLine system terminal: " + ex.getMessage(), ex));
        }
        reader = LineReaderBuilder.builder()
                .completer(new NJLineCompleter(session, this))
                .highlighter(new Highlighter() {
                    @Override
                    public AttributedString highlight(LineReader reader, String buffer) {
                        NTexts text = NTexts.of(session);
                        String ct = getCommandHighlighter();
                        if (NBlankable.isBlank(ct)) {
                            ct = "system";
                        }
                        NText n = NTexts.of(session).ofCode(ct, buffer).highlight(session);
                        return toAttributedString(n, NTextStyles.PLAIN, session);
                    }

                    @Override
                    public void setErrorPattern(Pattern ptrn) {

                    }

                    @Override
                    public void setErrorIndex(int i) {

                    }

                })
                .terminal(terminal)
                //                .completer(completer)
                //                .parse(parse)
                .build();
        reader.unsetOpt(LineReader.Option.INSERT_TAB);
        reader.setVariable(LineReader.HISTORY_FILE, session.locations().getWorkspaceLocation().resolve("history").normalize().toFile());
        if (reader instanceof LineReaderImpl) {
            ((LineReaderImpl) reader).setHistory(new NJLineHistory(reader, session, this));
        }
        this.out = NOutStream.of(
                new TransparentPrintStream(
                        new PrintStream(reader.getTerminal().output(), true),
                        System.out
                ),
                NTerminalMode.FORMATTED, this, session);
        this.err = NOutStream.of(
                new TransparentPrintStream(
                        new PrintStream(reader.getTerminal().output(), true),
                        System.err
                ),
                NTerminalMode.FORMATTED, this, session);//.setColor(NutsPrintStream.RED);
        this.in = new TransparentInputStream(reader.getTerminal().input(), System.in);
    }

    protected void close() {
        if (reader != null) {
            try {
                reader.getTerminal().close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "error closing terminal", ex);
            }
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        NSession session = criteria.getSession();
        try {
            prepare(session);
        } catch (Exception ex) {
            NLogger.of(NJLineTerminal.class, session)
                    .with().level(Level.FINEST).verb(NLoggerVerb.FAIL).error(ex)
                    .log(NMsg.ofPlain("unable to create NutsJLineTerminal. ignored."));
            return NO_SUPPORT;
        }
        return DEFAULT_SUPPORT + 1;
    }

    @Override
    public String readLine(NOutStream out, NMsg message, NSession session) {
        prepare(session);
        if (out == null) {
            out = getOut();
        }
        if (out == null) {
            out = NIO.of(session).stdout();
        }
        String readLine = null;
        try {
            readLine = reader.readLine(NTexts.of(session).ofText(message).toString());
        } catch (UserInterruptException e) {
            throw new NJLineInterruptException();
        }
        try {
            reader.getHistory().save();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return readLine;
    }

    @Override
    public char[] readPassword(NOutStream out, NMsg message, NSession session) {
        prepare(session);
        if (out == null) {
            return reader.readLine(NTexts.of(session).ofText(message).toString(), '*').toCharArray();
        } else {
            //should I use some out??
        }
        return reader.readLine(NTexts.of(session).ofText(message).toString(), '*').toCharArray();
    }

    @Override
    public InputStream getIn() {
        return in;
    }

    @Override
    public NOutStream getOut() {
        return out;
    }

    @Override
    public NOutStream getErr() {
        return err;
    }

    @Override
    public NCommandAutoCompleteResolver getAutoCompleteResolver() {
        return autoCompleteResolver;
    }

    @Override
    public boolean isAutoCompleteSupported() {
        return true;
    }

    @Override
    public NJLineTerminal setCommandAutoCompleteResolver(NCommandAutoCompleteResolver autoCompleteResolver) {
        this.autoCompleteResolver = autoCompleteResolver;
        return this;
    }

    @Override
    public NCommandHistory getCommandHistory() {
        return commandHistory;
    }

    @Override
    public NSystemTerminalBase setCommandHistory(NCommandHistory history) {
        this.commandHistory = history;
        return this;
    }

    public String getCommandHighlighter() {
        return commandHighlighter;
    }

    public NJLineTerminal setCommandHighlighter(String commandHighlighter) {
        this.commandHighlighter = commandHighlighter;
        return this;
    }

    @Override
    public Object run(NTerminalCommand command, NSession session) {
        switch (command.getName()) {
            case NTerminalCommand.Ids.GET_CURSOR: {
                org.jline.terminal.Cursor c = terminal.getCursorPosition(new IntConsumer() {
                    @Override
                    public void accept(int value) {
                        //
                    }
                });
                if (c != null) {
                    return new NSystemTerminalBase.Cursor(
                            c.getX(), c.getY()
                    );
                }
                return null;
            }
            case NTerminalCommand.Ids.GET_SIZE: {
                org.jline.terminal.Size c = terminal.getSize();
                if (c != null) {
                    return new NSystemTerminalBase.Size(
                            c.getColumns(), c.getRows()
                    );
                }
                return null;
            }
            default: {
                String s = NAnsiTermHelper.of(session).command(command, session);
                if (s != null) {
                    try {
                        reader.getTerminal().output().write(s.getBytes());
                    } catch (IOException e) {
                        throw new NIOException(session, e);
                    }
                }
                return null;
            }
        }
    }

    public void setStyles(NTextStyles styles, NSession session) {
        String s = NAnsiTermHelper.of(session).styled(styles, session);
        if (s != null) {
            try {
                reader.getTerminal().output().write(s.getBytes());
            } catch (IOException e) {
                throw new NIOException(session, e);
            }
        }
    }

    private static class TransparentInputStream extends FilterInputStream implements NInputStreamTransparentAdapter {

        private InputStream root;

        public TransparentInputStream(InputStream in, InputStream root) {
            super(in);
            this.root = root;
        }

        @Override
        public InputStream baseInputStream() {
            return root;
        }
    }

    private static class TransparentPrintStream extends PrintStream implements NOutputStreamTransparentAdapter {

        private OutputStream root;

        public TransparentPrintStream(OutputStream out, OutputStream root) {
            super(out, true);
            this.root = root;
        }

        @Override
        public OutputStream baseOutputStream() {
            return root;
        }

    }

}
