/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.io.NutsInputSource;
import net.thevpc.nuts.io.NutsInputSourceMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author thevpc
 */
public class InputStreamTee extends InputStream implements Interruptible {

    private final InputStream in;
    private final OutputStream out;
    private final Runnable onClose;
    private boolean interrupted;

    public InputStreamTee(InputStream in, OutputStream out, Runnable onClose) {
        this.in = in;
        this.out = out;
        this.onClose = onClose;
    }

    @Override
    public void interrupt() throws InterruptException {
        this.interrupted = true;
    }

    @Override
    public int read() throws IOException {
        if (interrupted) {
            throw new IOException(new InterruptException("Interrupted"));
        }
        int x = in.read();
        if (x >= 0) {
            out.write(x);
        }
        return x;
    }

    @Override
    public void close() throws IOException {
        if (interrupted) {
            throw new IOException(new InterruptException("Interrupted"));
        }
        in.close();
        out.close();
        if (onClose != null) {
            onClose.run();
        }
    }

    @Override
    public int available() throws IOException {
        if (interrupted) {
            throw new IOException(new InterruptException("Interrupted"));
        }
        return in.available();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (interrupted) {
            throw new IOException(new InterruptException("Interrupted"));
        }
        final int p = in.read(b, off, len);
        if (p > 0) {
            out.write(b, off, p);
        }
        return p;
    }
}
