package net.thevpc.nuts.runtime.standalone.text.renderer;

import net.thevpc.nuts.runtime.standalone.text.*;
import net.thevpc.nuts.runtime.standalone.util.CachedValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.thevpc.nuts.NutsSession;

import net.thevpc.nuts.runtime.standalone.text.renderer.ansi.AnsiStyle;
import net.thevpc.nuts.runtime.standalone.text.renderer.ansi.AnsiStyleStyleApplier;
import net.thevpc.nuts.runtime.standalone.text.renderer.ansi.DoNothingAnsiStyleStyleApplier;

public class AnsiUnixTermPrintRenderer implements FormattedPrintStreamRenderer {

    public static final FormattedPrintStreamRenderer ANSI_RENDERER = new AnsiUnixTermPrintRenderer();
    private static CachedValue<Integer> tput_cols;
    private static AnsiStyleStyleApplierResolver applierResolver=new AnsiStyleStyleApplierResolver() {
        private final Map<AnsiEscapeCommand, AnsiStyleStyleApplier> stylesAppliers = new HashMap<>();
        public AnsiStyleStyleApplier resolveStyleApplyer(AnsiEscapeCommand format) {
        if(format==null){
            return DoNothingAnsiStyleStyleApplier.INSTANCE;
        }
        if(format instanceof AnsiStyleStyleApplier){
            return (AnsiStyleStyleApplier) format;
        }
        AnsiStyleStyleApplier s = stylesAppliers.get(format);
        if (s != null) {
            return s;
        }
        return DoNothingAnsiStyleStyleApplier.INSTANCE;
    }
    };


    {
//        defineEscape(AnsiEscapeCommands.FG_BLACK, new ForegroundStyleApplier("30", 0));
//        defineEscape(AnsiEscapeCommands.FG_RED, new ForegroundStyleApplier("31", 0));
//        defineEscape(AnsiEscapeCommands.FG_GREEN, new ForegroundStyleApplier("32", 0));
//        defineEscape(AnsiEscapeCommands.FG_YELLOW, new ForegroundStyleApplier("33", 0));
//        defineEscape(AnsiEscapeCommands.FG_MAGENTA, new ForegroundStyleApplier("35", 0));
//        defineEscape(AnsiEscapeCommands.FG_BLUE, new ForegroundStyleApplier("34", 0));
//        defineEscape(AnsiEscapeCommands.FG_CYAN, new ForegroundStyleApplier("36", 0));
//        defineEscape(AnsiEscapeCommands.FG_WHITE, new ForegroundStyleApplier("37", 0));
//        defineEscape(AnsiEscapeCommands.FG_GREY, new ForegroundStyleApplier("37", 1));
//
//        defineEscape(AnsiEscapeCommands.BG_BLACK, new BackgroundStyleApplier("40"));
//        defineEscape(AnsiEscapeCommands.BG_RED, new BackgroundStyleApplier("41"));
//        defineEscape(AnsiEscapeCommands.BG_GREEN, new BackgroundStyleApplier("42"));
//        defineEscape(AnsiEscapeCommands.BG_YELLOW, new BackgroundStyleApplier("43"));
//        defineEscape(AnsiEscapeCommands.BG_BLUE, new BackgroundStyleApplier("44"));
//        defineEscape(AnsiEscapeCommands.BG_MAGENTA, new BackgroundStyleApplier("45"));
//        defineEscape(AnsiEscapeCommands.BG_CYAN, new BackgroundStyleApplier("46"));
//        defineEscape(AnsiEscapeCommands.BG_GREY, new BackgroundStyleApplier("100"));
//        defineEscape(AnsiEscapeCommands.BG_WHITE, new BackgroundStyleApplier("37"));


//        defineEscape(AnsiEscapeCommands.MOVE_LINE_START, new MoveLineStartCommandAnsiStyleStyleApplier());
//        defineEscape(AnsiEscapeCommands.LATER_RESET_LINE, new LaterResetLineCommandAnsiStyleStyleApplier());
//        defineEscape(AnsiEscapeCommands.MOVE_UP, new MoveUpCommandAnsiStyleStyleApplier());

//        defineEscape(AnsiEscapeCommands.MOVE_LINE_START, "\u001B[1000D", "");
//        defineEscape(AnsiEscapeCommands.BOLD, "\u001b[1m", "\u001B[0m");
//        defineEscape(AnsiEscapeCommands.UNDERLINED, "\u001b[4m", "\u001B[0m");
//        defineEscape(AnsiEscapeCommands.REVERSED, "\u001b[7m", "\u001B[0m");
    }

    public AnsiStyle createStyleRenderer(AnsiEscapeCommand format, RenderedRawStream out, NutsSession session) {
        AnsiStyleStyleApplier applier = applierResolver.resolveStyleApplyer(format);
        return applier.apply(AnsiStyle.PLAIN, out, session, applierResolver);
    }

    @Override
    public void startFormat(RenderedRawStream out, AnsiEscapeCommand format, NutsSession session)  throws IOException {
        createStyleRenderer(format, out, session).startFormat(out);
    }

    @Override
    public void endFormat(RenderedRawStream out, AnsiEscapeCommand format, NutsSession session) throws IOException {
        createStyleRenderer(format, out, session).endFormat(out);
    }
}
