/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.util;

/**
 *
 * @author vpc
 */
public interface InputStreamMonitor {

    boolean onProgress(InputStreamEvent event);
    
}
