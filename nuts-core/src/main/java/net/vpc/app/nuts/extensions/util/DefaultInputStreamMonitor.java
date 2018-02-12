/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.util;

import java.text.DecimalFormat;
import net.vpc.app.nuts.NutsPrintStream;

/**
 *
 * @author vpc
 */
public class DefaultInputStreamMonitor implements InputStreamMonitor {

    private static DecimalFormat df = new DecimalFormat("000.00");
    private static MemorySizeFormatter mf = new MemorySizeFormatter("BTD1F");
    private NutsPrintStream out;

    public DefaultInputStreamMonitor(NutsPrintStream out) {
        this.out = out;
    }
    
    @Override
    public boolean onProgress(InputStreamEvent event) {
        double partialSeconds = event.getPartialNanos() / 1000000000.0;
        if (partialSeconds > 1 || event.getGlobalCount()==event.getLength()) {
            double globalSeconds = event.getGlobalNanos() / 1000000000.0;
            long globalSpeed = (long) (event.getGlobalCount() / globalSeconds);
            long partialSpeed = (long) (event.getPartialCount() / partialSeconds);
            double percent = 0;
            if (event.getLength() > 0) {
                percent = (double) (event.getGlobalCount() * 100.0 / event.getLength());
            }else{
                percent=0;
            }
            out.drawln("Downloading [[" + event.getSourceName() + "]] :: <<" + df.format(percent) +"%>> <<"+ (mf.format(partialSpeed)) + ">> /s (<<" + (mf.format(globalSpeed)) + ">> /s)");
            return true;
        }
        return false;
    }

}
