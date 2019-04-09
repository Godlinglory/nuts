/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 */
public interface NutsUpdateResult {

    NutsId getId();

    NutsDefinition getLocalVersion();

    NutsDefinition getAvailableVersion();

    boolean isUpdateForced();

    boolean isUpdateApplied();

    boolean isUpdateAvailable();

}
