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
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsHttpConnectionFacade;
import net.vpc.app.nuts.NutsTransportComponent;
import net.vpc.app.nuts.NutsTransportParamPart;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsUnsupportedOperationException;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;

/**
 * Created by vpc on 1/21/17.
 */
public class DefaultHttpTransportComponent implements NutsTransportComponent {

    public static final NutsTransportComponent INSTANCE = new DefaultHttpTransportComponent();
    private static final Logger log = Logger.getLogger(DefaultHttpTransportComponent.class.getName());

    @Override
    public int getSupportLevel(String url) {
        return BOOT_SUPPORT;
    }

    @Override
    public NutsHttpConnectionFacade open(String url) throws IOException {
        return new DefaultNutsHttpConnectionFacade(new URL(url));
    }

    private static class DefaultNutsHttpConnectionFacade implements NutsHttpConnectionFacade {

        private final URL url;

        public DefaultNutsHttpConnectionFacade(URL url) {
            this.url = url;
        }

        @Override
        public InputStream open() throws IOException {
            return url.openStream();
        }
        
        @Override
        public long length() throws IOException {
            return CoreIOUtils.getURLSize(url);
        }

        public InputStream upload(NutsTransportParamPart... parts) throws IOException {
            throw new NutsUnsupportedOperationException("Upload unsupported");
        }
    }
}
