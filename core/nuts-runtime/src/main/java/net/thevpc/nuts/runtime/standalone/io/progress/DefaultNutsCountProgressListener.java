/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.util.BytesSizeFormat;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.text.NutsTextBuilder;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsProgressEvent;
import net.thevpc.nuts.util.NutsProgressListener;

import java.text.DecimalFormat;

/**
 * @author thevpc
 */
public class DefaultNutsCountProgressListener implements NutsProgressListener/*, NutsOutputStreamTransparentAdapter*/ {

    private NutsPrintStream out;
    private int minLength;

    public DefaultNutsCountProgressListener() {
    }

//    @Override
//    public OutputStream baseOutputStream() {
//        return out;
//    }

    public DecimalFormat df(NutsProgressEvent event) {
        return new DecimalFormat("##0.00");
    }

    public BytesSizeFormat mf(NutsProgressEvent event) {
        return new BytesSizeFormat("BTD1F",event.getSession());
    }

    @Override
    public boolean onProgress(NutsProgressEvent event) {
        switch (event.getState()){
            case START:{
                this.out = event.getSession().getTerminal().err();
                if (event.getSession().isPlainOut()) {
                    onProgress0(event, false);
                }
                break;
            }
            case COMPLETE:{
                if (event.getSession().isPlainOut()) {
                    onProgress0(event, true);
//            out.println();
                }
                break;
            }
            case PROGRESS:{
                if (event.getSession().isPlainOut()) {
                    return onProgress0(event, false);
                }
                break;
            }
        }
        return true;
    }

    public boolean onProgress0(NutsProgressEvent event, boolean end) {
        double partialSeconds = event.getPartialDuration().getTimeAsDoubleSeconds();
        if (event.getCurrentCount() == 0 || partialSeconds > 0.5 || event.getCurrentCount() == event.getMaxValue()) {
            NutsTexts text = NutsTexts.of(event.getSession());
            out.resetLine();
            double globalSeconds = event.getDuration().getTimeAsDoubleSeconds();
            long globalSpeed = globalSeconds == 0 ? 0 : (long) (event.getCurrentCount() / globalSeconds);
            long partialSpeed = partialSeconds == 0 ? 0 : (long) (event.getPartialCount() / partialSeconds);
            double percent = event.getProgress();
            if (event.isIndeterminate()) {
                percent = end ? 100 : 0;
            }
//            int x = (int) (20.0 / 100.0 * percent);

            NutsTextBuilder formattedLine = text.ofBuilder();
            CProgressBar cp= CProgressBar.of(event.getSession());

            formattedLine.append(cp.progress((int)percent));
//            if (x > 0) {
//                formattedLine.append(text.forStyled(
//                        CoreStringUtils.fillString("*", x),
//                        NutsTextStyle.primary1()
//                ));
//            }
//            CoreStringUtils.fillString(' ', 20 - x, formattedLine);
//            formattedLine.append("]");
            BytesSizeFormat mf = mf(event);
            DecimalFormat df = df(event);
            formattedLine.append(" ").append(text.ofStyled(String.format("%6s", df.format(percent)), NutsTextStyle.config())).append("% ");
            formattedLine.append(" ").append(text.ofStyled(mf.format(partialSpeed),NutsTextStyle.config())).append("/s");
            if (event.getMaxValue() < 0) {
                if (globalSpeed == 0) {
                    formattedLine.append(" ( -- )");
                } else {
                    formattedLine.append(" (").append(text.ofStyled(mf.format(globalSpeed),NutsTextStyle.info())).append(")");
                }
            } else {
                formattedLine.append(" (").append(text.ofStyled(mf.format(event.getMaxValue()),NutsTextStyle.warn())).append(")");
            }
            if (event.getError() != null) {
                formattedLine.append(" ").append(text.ofStyled("ERROR",NutsTextStyle.error())).append(" ");
            }
            formattedLine.append(" ").append(event.getMessage()).append(" ");
            String ff = formattedLine.toString();
            int length = text.ofBuilder().append(ff).textLength();
            if (length < minLength) {
                CoreStringUtils.fillString(' ', minLength - length, formattedLine);
            } else {
                minLength = length;
            }
            out.print(ff);
            out.flush();
            return true;
        }
        return false;
    }

}
