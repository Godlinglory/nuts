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
package net.vpc.app.nuts.extensions.servers;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.extensions.util.ListMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Created by vpc on 1/24/17.
 */
public class FacadeCommandContext implements NutsHttpServletFacadeContext {

    private NutsHttpServletFacadeContext base;
    private NutsWorkspace workspace;
    private String serverId;
    private String command;
    private String path;
    private NutsSession session;

    public FacadeCommandContext(NutsHttpServletFacadeContext base, NutsWorkspace workspace, String serverId, String command, String path, NutsSession session) {
        this.base = base;
        this.workspace = workspace;
        this.serverId = serverId;
        this.command = command;
        this.path = path;
        this.session = session;
    }

    public NutsSession getSession() {
        return session;
    }

    public String getPath() {
        return path;
    }

    public String getCommand() {
        return command;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public String getServerId() {
        return serverId;
    }

    @Override
    public URI getRequestURI() throws IOException {
        return base.getRequestURI();
    }

    @Override
    public OutputStream getResponseBody() throws IOException {
        return base.getResponseBody();
    }

    @Override
    public void sendResponseHeaders(int code, long length) throws IOException {
        base.sendResponseHeaders(code, length);
    }

    @Override
    public void sendError(int code, String msg) throws IOException {
        base.sendError(code, msg);
    }

    @Override
    public void sendResponseText(int code, String text) throws IOException {
        base.sendResponseText(code, text);
    }

    @Override
    public void sendResponseFile(int code, File file) throws IOException {
        base.sendResponseFile(code, file);
    }

    @Override
    public Set<String> getRequestHeaderKeys(String header) throws IOException {
        return base.getRequestHeaderKeys(header);
    }

    @Override
    public String getRequestHeaderFirstValue(String header) throws IOException {
        return base.getRequestHeaderFirstValue(header);
    }

    @Override
    public List<String> getRequestHeaderAllValues(String header) throws IOException {
        return base.getRequestHeaderAllValues(header);
    }

    @Override
    public InputStream getRequestBody() throws IOException {
        return base.getRequestBody();
    }

    @Override
    public ListMap<String, String> getParameters() throws IOException {
        return base.getParameters();
    }
}
