/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.service.security;

/**
 *
 * @author vpc
 */
public interface PasswordHandler {

    String askForSavePassword(String path,String root);
    String askForLoadPassword(String path,String root);
    boolean reTypePasswordOnError();
}
