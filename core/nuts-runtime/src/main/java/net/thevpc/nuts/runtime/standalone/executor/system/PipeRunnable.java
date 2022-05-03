/**
 * ====================================================================
 * vpc-common-io : common reusable library for
 * input/output
 * <p>
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
package net.thevpc.nuts.runtime.standalone.executor.system;

import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.io.util.NonBlockingInputStream;
import net.thevpc.nuts.runtime.standalone.io.util.StopMonitor;

import java.io.OutputStream;
import java.util.logging.Level;

public class PipeRunnable implements Runnable, StopMonitor {

//    private static final Set<PipeRunnable> running = new LinkedHashSet<>();
    private final NonBlockingInputStream in;
    private final OutputStream out;
    private final Object lock = new Object();
    private long pipedBytesCount = 0;
    private boolean requestStop = false;
    private boolean stopped = false;
    private final NutsSession session;
    private final String cmd;
    private final String desc;
    private final String name;
    private final boolean renameThread;
    private byte[] bytesBuffer = new byte[10240];

    public PipeRunnable(String name, String cmd, String desc, NonBlockingInputStream in, OutputStream out, boolean renameThread, NutsSession session) {
        this.name = name;
        this.renameThread = renameThread;
        this.in = in;
        this.out = out;
        this.session = session;
        this.cmd = cmd;
        this.desc = desc;
    }

    public String getCmd() {
        return cmd;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public boolean shouldStop() {
        return requestStop;
    }

    public void requestStop() {
        requestStop = true;
//        if (!stopped) {
//            synchronized (lock) {
//                try {
//                    lock.wait();
//                } catch (InterruptedException e) {
//                    NutsLoggerOp.of(PipeRunnable.class, session)
//                            .error(e)
//                            .level(Level.FINEST)
//                            .verb(NutsLogVerb.WARNING)
//                            .log(NutsMessage.jstyle("lock-wait interrupted"));
//                }
//            }
//        }
    }

    public boolean incrementalCopy() {
        if (in.hasMoreBytes()) {
            try {
                int count = in.readNonBlocking(bytesBuffer, 500);
                if (count > 0) {
                    pipedBytesCount += count;
                    out.write(bytesBuffer, 0, count);
                    out.flush();
                    return true;
                }
            } catch (Exception ex) {
                NutsLoggerOp.of(PipeRunnable.class, session)
                        .error(ex)
                        .level(Level.FINEST)
                        .verb(NutsLoggerVerb.WARNING)
                        .log(NutsMessage.ofJstyle("pipe-thread exits with error: {0}", ex));
                markAsEffectivelyStopped();
            }
        } else {
            markAsEffectivelyStopped();
        }
        return false;
    }

    private void markAsEffectivelyStopped() {
        if (!stopped) {
            stopped = true;
//            synchronized (lock) {
//                lock.notify();
//            }
        }
    }

    public boolean runOnce() {
        if(!incrementalCopy()){
            if (isStopped()) {
                return false;
            }else if (this.shouldStop()) {
                markAsEffectivelyStopped();
                return false;
            }
        }
        return true;
    }
    @Override
    public void run() {
        String oldThreadName = null;
        Thread currentThread = null;
        if (renameThread) {
            currentThread = Thread.currentThread();
            oldThreadName = currentThread.getName();
            currentThread.setName(name);
        }
        try {
            while (runOnce()) {
                //
            }
        } finally {
            markAsEffectivelyStopped();
            if (renameThread && currentThread != null) {
                currentThread.setName(oldThreadName);
            }
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public NonBlockingInputStream getIn() {
        return in;
    }

    public OutputStream getOut() {
        return out;
    }
}
