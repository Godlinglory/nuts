/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode.processors;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextStyle;
import net.thevpc.nuts.toolbox.ncode.SourceProcessor;
import net.thevpc.nuts.toolbox.ncode.Source;
import net.thevpc.nuts.toolbox.ncode.sources.JavaTypeSource;

/**
 * @author thevpc
 */
public class JavaSourceFormatter implements SourceProcessor {

    private int clsNameSize = 20;

    public JavaSourceFormatter() {
    }

    @Override
    public Object process(Source source, NutsSession session) {
        if (source instanceof JavaTypeSource) {
            JavaTypeSource s = (JavaTypeSource) source;
            String v1 = s.getClassVersion(false);
            String v2 = s.getClassVersion(true);
            String n = s.getClassName();
            if (n.length() > clsNameSize) {
                clsNameSize = n.length();
            }
            return NutsMessage.cstyle(
                    "%s %s %s %s",
                    session.text().forStyled(leftAlign(v1, 4), NutsTextStyle.config()),
                    session.text().forStyled(leftAlign(v2, 4), NutsTextStyle.info()),
                    session.text().forStyled(leftAlign(n, clsNameSize), NutsTextStyle.primary1()),
                    session.text().forStyled(source.getExternalPath(), NutsTextStyle.path()),
                    source.toString()
            );
        } else {
            return NutsMessage.cstyle(
                    "%s : %s",
                    session.text().forStyled("invalid source", NutsTextStyle.error()),
                    source.toString()
            );
        }
    }

    private String leftAlign(String n, int size) {
        StringBuilder sb = new StringBuilder(size);
        sb.append(n);
        int x = size - n.length();
        while (x > 0) {
            sb.append(' ');
            x--;
        }
        return sb.toString();
    }

}
