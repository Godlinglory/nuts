/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.io.ByteArrayPrintStream;

/**
 *
 * @author vpc
 */
public abstract class DefaultFormatBase<T extends NutsFormat> extends DefaultFormatBase0<T> implements NutsFormat {

    public DefaultFormatBase(NutsWorkspace ws,String name) {
        super(ws,name);
    }

    @Override
    public PrintWriter getValidPrintWriter(Writer out) {
        if (out == null) {
            out = new PrintWriter(getValidSession().getTerminal().getOut());
        }
        PrintWriter pout = (out instanceof PrintWriter) ? ((PrintWriter) out) : new PrintWriter(out);
        return ws.io().getTerminalFormat().prepare(pout);
    }

    @Override
    public PrintWriter getValidPrintWriter() {
        return getValidPrintWriter(null);
    }

    @Override
    public PrintStream getValidPrintStream(PrintStream out) {
        if (out == null) {
            out = getValidSession().getTerminal().getOut();
        }
        return ws.io().getTerminalFormat().prepare(out);
    }

    @Override
    public PrintStream getValidPrintStream() {
        return getValidPrintStream(null);
    }

    @Override
    public String format() {
        ByteArrayPrintStream out = new ByteArrayPrintStream();
        print(out);
        return out.toString();
    }

    @Override
    public void print() {
        print(ws.io().getTerminal());
    }

    @Override
    public void println() {
        println(ws.io().getTerminal());
    }

    @Override
    public void print(NutsTerminal terminal) {
        print(terminal.out());
    }

    @Override
    public void println(NutsTerminal terminal) {
        println(terminal.out());
    }

    @Override
    public void print(File file) {
        print(file.toPath());
    }

    @Override
    public void println(File file) {
        println(file.toPath());
    }

    @Override
    public void print(Path path) {
        try (Writer w = Files.newBufferedWriter(path)) {
            print(w);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void println(Path path) {
        try (Writer w = Files.newBufferedWriter(path)) {
            println(w);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void print(PrintStream out) {
        PrintWriter p = new PrintWriter(out);
        print(p);
        p.flush();
    }

    @Override
    public void println(PrintStream out) {
        PrintWriter p = new PrintWriter(out);
        println(p);
        p.flush();
    }

    @Override
    public void println(Writer w) {
        print(w);
        try {
            w.write("\n");
            w.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String toString() {
        return format();
    }


    public abstract void print(Writer out);

}
