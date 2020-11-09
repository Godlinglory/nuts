//package net.thevpc.nuts.runtime.util.fprint;
//
//import net.thevpc.nuts.runtime.io.NutsTerminalModeOp;
//import net.thevpc.nuts.runtime.util.fprint.FPrint;
//import net.thevpc.nuts.runtime.util.fprint.FormattedPrintStream;
//
//import java.io.OutputStream;
//import java.io.PrintStream;
//import NutsTerminalMode;
//import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
//
///**
// * Created by vpc on 2/20/17.
// */
//public class NutsPrintStreamFiltered extends FormattedPrintStream {
//
//    private final OutputStream out;
//    private PrintStream ps;
//
//    public NutsPrintStreamFiltered(OutputStream out) {
//        super(out, FPrint.RENDERER_ANSI_STRIPPER);
//        this.out = out;
//    }
//
//    @Override
//    public OutputStream baseOutputStream() {
//        return out;
//    }
//
////    @Override
////    public int getSupportLevel(NutsSupportLevelContext<OutputStream> criteria) {
////        return DEFAULT_SUPPORT + 2;
////    }
//
//    @Override
//    public NutsTerminalModeOp getModeOp() {
//        return NutsTerminalModeOp.FILTER;
//    }
//
//    @Override
//    public PrintStream basePrintStream() {
//        if (ps == null) {
//            ps = CoreIOUtils.toPrintStream(out,getWorkspace());
//        }
//        return ps;
//    }
//
//}
