/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.servers;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.cmd.AdminServerConfig;
import net.vpc.app.nuts.extensions.util.CoreJsonUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.app.nuts.extensions.util.ListMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsHttpServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(NutsHttpServlet.class.getName());
    private NutsHttpServletFacade facade;
    private String serverId = "";
    private String workspaceLocation = null;
    private String root = null;
    private String runtimeId = null;
    private String runtimeSourceURL = null;
    private int adminServerPort = -1;
    private Map<String, String> workspaces = new HashMap<>();
    private boolean adminServer = true;
    private NutsServer adminServerRef;

    @Override
    public void init() throws ServletException {
        super.init();
        Map<String, NutsWorkspace> workspacesByLocation = new HashMap<>();
        Map<String, NutsWorkspace> workspacesByWebContextPath = new HashMap<>();
        NutsBootWorkspace bws = Nuts.openBootWorkspace(
                new NutsBootOptions().setRoot(root)
                        .setRuntimeId(runtimeId)
                        .setRuntimeSourceURL(runtimeSourceURL)
        );
        NutsWorkspace workspace = bws.openWorkspace(workspaceLocation, new NutsWorkspaceCreateOptions()
                .setCreateIfNotFound(true)
                .setSaveIfCreated(true)
                .setArchetype("server")
        );
        if (workspaces.isEmpty()) {
            String wl = workspaceLocation == null ? "" : workspaceLocation;
            workspaces.put("", wl);
            workspacesByLocation.put(wl, workspace);
        }
        for (Map.Entry<String, String> w : workspaces.entrySet()) {
            String webContext = w.getKey();
            String location = w.getValue();
            if (location == null) {
                location = "";
            }
            NutsWorkspace ws = workspacesByLocation.get(location);
            if (ws == null) {
                ws = bws.openWorkspace(location, new NutsWorkspaceCreateOptions()
                        .setCreateIfNotFound(true)
                        .setSaveIfCreated(true)
                        .setArchetype("server")
                );
                workspacesByLocation.put(location, ws);
            }
            workspacesByWebContextPath.put(webContext, ws);
        }

        if (CoreStringUtils.isEmpty(serverId)) {
            String serverName = NutsConstants.DEFAULT_HTTP_SERVER;
            try {
                serverName = InetAddress.getLocalHost().getHostName();
                if (serverName != null && serverName.length() > 0) {
                    serverName = "nuts-" + serverName;
                }
            } catch (Exception e) {
                //
            }
            if (serverName == null) {
                serverName = NutsConstants.DEFAULT_HTTP_SERVER;
            }

            serverId = serverName;
        }

        this.facade = new NutsHttpServletFacade(serverId, workspacesByWebContextPath);
        if (adminServer) {
            try {
                AdminServerConfig serverConfig = new AdminServerConfig();
                serverConfig.setPort(adminServerPort);
                adminServerRef = workspace.getServerManager().startServer(serverConfig);
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Unable to start admin server", ex);
            }
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        log.info("Starting Nuts Http Server at url http://<your-server>" + config.getServletContext().getContextPath() + "/service");
        if (adminServer) {
            log.info("Starting Nuts admin Server at <localhost>:" + (adminServerPort < 0 ? NutsConstants.DEFAULT_HTTP_SERVER_PORT : adminServerPort));
        }
        adminServerPort = CoreStringUtils.parseInt(config.getInitParameter("nuts-admin-server-port"), -1);
        workspaceLocation = config.getInitParameter("nuts-workspace-location");
        root = config.getInitParameter("nuts-workspace-root");
        runtimeId = config.getInitParameter("nuts-runtime-id");
        runtimeSourceURL = config.getInitParameter("nuts-source-url");
        adminServer = Boolean.valueOf(config.getInitParameter("nuts-admin"));
        try {
            workspaces = CoreJsonUtils.get().deserializeStringsMap(CoreJsonUtils.get().loadJsonStructure(config.getInitParameter("nuts-workspaces-map")), new LinkedHashMap<String, String>());
        } catch (Exception e) {
            //
        }
        if (workspaces == null) {
            workspaces = new LinkedHashMap<>();
        }
        super.init(config);
        config.getServletContext().setAttribute(NutsHttpServletFacade.class.getName(), facade);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (adminServerRef != null) {
            try {
                adminServerRef.stop();
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Unable to stop admin server", ex);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doService(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doService(req, resp);
    }

    protected void doService(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        facade.execute(new AbstractNutsHttpServletFacadeContext() {
            @Override
            public URI getRequestURI() throws IOException {
                try {
                    String cp = req.getContextPath();
                    String uri = req.getRequestURI();
                    if (uri.startsWith(cp)) {
                        uri = uri.substring(cp.length());
                        if (uri.startsWith(req.getServletPath())) {
                            uri = uri.substring(req.getServletPath().length());
                        }
                    }
                    return new URI(uri);
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
            }

            @Override
            public OutputStream getResponseBody() throws IOException {
                return resp.getOutputStream();
            }

            @Override
            public void sendError(int code, String msg) throws IOException {
                resp.sendError(code, msg);
            }

            @Override
            public void sendResponseHeaders(int code, long length) throws IOException {
                if (length > 0) {

                    resp.setHeader("Content-length", Long.toString(length));
                }
                resp.setStatus(code);
            }

            @Override
            public String getRequestHeaderFirstValue(String header) throws IOException {
                return req.getHeader(header);
            }

            @Override
            public Set<String> getRequestHeaderKeys(String header) throws IOException {
                return new HashSet<>(Collections.list(req.getHeaderNames()));
            }

            @Override
            public List<String> getRequestHeaderAllValues(String header) throws IOException {
                return Collections.list(req.getHeaders(header));
            }

            @Override
            public InputStream getRequestBody() throws IOException {
                return req.getInputStream();
            }

            @Override
            public ListMap<String, String> getParameters() throws IOException {
                ListMap<String, String> m = new ListMap<String, String>();
                for (String s : Collections.list(req.getParameterNames())) {
                    for (String v : req.getParameterValues(s)) {
                        m.add(s, v);
                    }
                }
                return m;//HttpUtils.queryToMap(getRequestURI().getQuery());
            }
        });
    }
}
