/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.json;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatJson extends DefaultSearchFormatBase {

    private boolean compact;

    NTexts txt;
    private NCodeHighlighter codeFormat;

    public DefaultSearchFormatJson(NSession session, NOutStream writer, NFetchDisplayOptions options) {
        super(session, writer, NContentType.JSON, options);
        txt = NTexts.of(session);
        codeFormat = NTexts.of(session).setSession(session).getCodeHighlighter("json");
    }

    @Override
    public void start() {
        getWriter().println(codeFormat.tokenToText("[", "separator", txt, getSession()));
        getWriter().flush();
    }

    @Override
    public void complete(long count) {
        getWriter().println(codeFormat.tokenToText("]", "separator", txt, getSession()));
        getWriter().flush();
    }

    @Override
    public boolean configureFirst(NCommandLine commandLine) {
        NSession session = getSession();
        NArgument aa = commandLine.peek().get(session);
        if (aa == null) {
            return false;
        }
        if (getDisplayOptions().configureFirst(commandLine)) {
            return true;
        }
        switch(aa.key()) {
            case "--compact": {
                commandLine.withNextBoolean((v, a, s) -> this.compact=v);
                return true;
            }
        }
        return false;
    }

    @Override
    public void next(Object object, long index) {
        if (index > 0) {
            getWriter().print(", ");
        }else{
            getWriter().print("  ");
        }
        String json = NElements.of(getSession())
                .json().setNtf(false).setValue(object).setCompact(isCompact())
                .format()
                .filteredText()
                ;
        NText ee = codeFormat.stringToText(json, txt, getSession());
        getWriter().printf("%s%n", ee);
        getWriter().flush();
    }

    public boolean isCompact() {
        return compact;
    }

}
