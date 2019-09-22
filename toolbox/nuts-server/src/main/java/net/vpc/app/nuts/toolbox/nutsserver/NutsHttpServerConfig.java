/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nutsserver;

import net.vpc.app.nuts.NutsWorkspace;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by vpc on 1/23/17.
 */
public final class NutsHttpServerConfig extends ServerConfig {

    private String serverId;
    private InetAddress address;
    private int port;
    private int backlog;
    private Executor executor;
    private boolean tls;
    private byte[] sslKeystoreCertificate;
    private char[] sslKeystorePassphrase;
    private Map<String, NutsWorkspace> workspaces = new HashMap<>();

    public NutsHttpServerConfig setWorkspaces(Map<String, NutsWorkspace> workspaces) {
        this.workspaces = workspaces;
        return this;
    }

    public String getServerId() {
        return serverId;
    }

    public NutsHttpServerConfig setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public Map<String, NutsWorkspace> getWorkspaces() {
        return workspaces;
    }

    public boolean isTls() {
        return tls;
    }

    public byte[] getSslKeystoreCertificate() {
        return sslKeystoreCertificate;
    }

    public NutsHttpServerConfig setSslKeystoreCertificate(byte[] sslKeystoreCertificate) {
        this.sslKeystoreCertificate = sslKeystoreCertificate;
        return this;
    }

    public char[] getSslKeystorePassphrase() {
        return sslKeystorePassphrase;
    }

    public NutsHttpServerConfig setSslKeystorePassphrase(char[] sslKeystorePassphrase) {
        this.sslKeystorePassphrase = sslKeystorePassphrase;
        return this;
    }

    public NutsHttpServerConfig setTls(boolean tls) {
        this.tls = tls;
        return this;
    }

    public InetAddress getAddress() {
        return address;
    }

    public NutsHttpServerConfig setAddress(InetAddress address) {
        this.address = address;
        return this;
    }

    public int getPort() {
        return port;
    }

    public NutsHttpServerConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public int getBacklog() {
        return backlog;
    }

    public NutsHttpServerConfig setBacklog(int backlog) {
        this.backlog = backlog;
        return this;
    }

    public Executor getExecutor() {
        return executor;
    }

    public NutsHttpServerConfig setExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }
}
