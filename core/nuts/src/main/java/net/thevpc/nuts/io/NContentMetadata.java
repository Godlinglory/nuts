/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;

/**
 * @author thevpc
 */
public interface NContentMetadata extends NBlankable {
    NOptional<Long> getContentLength();

    NOptional<NMsg> getMessage();


    NOptional<String> getContentType();

    NOptional<String> getName();

    NOptional<String> getKind();

    NContentMetadata setKind(String userKind);

    NContentMetadata setName(String name);

    NContentMetadata setMessage(NMsg message);


    NContentMetadata setContentType(String contentType);

    NContentMetadata setContentLength(Long contentLength);
}
