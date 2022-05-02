/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsString;

/**
 * @author thevpc
 */
public interface NutsStreamMetadata {
    static NutsStreamMetadata resolve(Object is) {
        if (is instanceof NutsStreamMetadataAware) {
            return ((NutsStreamMetadataAware) is).getStreamMetadata();
        }
        return null;
    }

    static NutsStreamMetadata of(Object is) {
        NutsStreamMetadata a = resolve(is);
        if (a != null) {
            return a;
        }
        return new DefaultNutsStreamMetadata();
    }

    long getContentLength();

    NutsString getFormattedPath(NutsSession session);

    String getContentType();

    String getName();

    String getUserKind();

    NutsStreamMetadata setUserKind(String userKind);
}