/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.term;

import net.vpc.app.nuts.*;
import net.vpc.common.io.FileUtils;
import net.vpc.common.javashell.InterruptShellException;
import org.jline.reader.*;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 2/20/17.
 */
public class NutsJLineTerminal implements NutsSystemTerminalBase {

    private Terminal terminal;
    private LineReader reader;
    private PrintStream out;
    private PrintStream err;
    private InputStream in;
    private NutsWorkspace workspace;
    private NutsTerminalMode outMode;
    private NutsTerminalMode errMode;

    public NutsJLineTerminal() {
    }

    @Override
    public void setOutMode(NutsTerminalMode mode) {
        this.outMode=mode;
    }

    @Override
    public NutsTerminalMode getOutMode() {
        return outMode;
    }

    @Override
    public void setErrorMode(NutsTerminalMode mode) {
        this.errMode=mode;
    }

    @Override
    public NutsTerminalMode getErrorMode() {
        return errMode;
    }

    public void install(NutsWorkspace workspace) {
        this.workspace = workspace;
        TerminalBuilder builder = TerminalBuilder.builder();
        builder.streams(System.in, System.out);
        builder.system(true);

        try {
            terminal = builder.build();
        } catch (Throwable ex) {
            //unable to create system terminal
        }
        System.out.println("Created "+terminal.getClass());
        if (terminal == null) {
            builder.system(false);
            try {
                terminal = builder.build();
            } catch (IOException ex) {
                Logger.getLogger(NutsJLineTerminal.class.getName()).log(Level.SEVERE, null, ex);
                throw new NutsIOException(ex);
            }
        }

        reader = LineReaderBuilder.builder()
                .completer(new NutsJLineCompleter(workspace))
                .terminal(terminal)
                //                .completer(completer)
                //                .parser(parser)
                .build();
        reader.setVariable(LineReader.HISTORY_FILE, FileUtils.getAbsoluteFile(new File(workspace.getConfigManager().getWorkspaceLocation()), "history"));
        ((LineReaderImpl) reader).setHistory(new NutsJLineHistory(reader, workspace));
        this.out = workspace.getIOManager().createPrintStream(
                new TransparentPrintStream(
                        reader.getTerminal().output(),
                        System.out
                )
                , NutsTerminalMode.FORMATTED);
        this.err = workspace.getIOManager().createPrintStream(
                new TransparentPrintStream(
                        reader.getTerminal().output(),
                        System.err
                )
                , NutsTerminalMode.FORMATTED);//.setColor(NutsPrintStream.RED);
        this.in = new TransparentInputStream(reader.getTerminal().input(), System.in);

    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT + 1;
    }

    @Override
    public String readLine(String promptFormat, Object... params) {
        String readLine = null;
        try {
            readLine = reader.readLine(promptFormat);
        } catch (UserInterruptException e) {
            throw new InterruptShellException();
        }
        try {
            reader.getHistory().save();
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
        return readLine;
    }

    @Override
    public String readPassword(String prompt) {
        return reader.readLine(prompt, '*');
    }

    @Override
    public InputStream getIn() {
        return in;
    }

    @Override
    public PrintStream getOut() {
        return out;
    }

    @Override
    public PrintStream getErr() {
        return err;
    }

    private static class TransparentInputStream extends FilterInputStream implements InputStreamTransparentAdapter {
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

    private static class TransparentPrintStream extends PrintStream implements OutputStreamTransparentAdapter {
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
