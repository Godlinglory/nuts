/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public class NutsURLClassLoader extends URLClassLoader {

    public NutsURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public NutsURLClassLoader(URL[] urls) {
        super(urls);
    }

    public NutsURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    public NutsURLClassLoader(String[] urls, ClassLoader parent) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls), parent);
    }

    public NutsURLClassLoader(String[] urls) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls));
    }

    public NutsURLClassLoader(String[] urls, ClassLoader parent, URLStreamHandlerFactory factory) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls), parent, factory);
    }

    public NutsURLClassLoader(File[] urls, ClassLoader parent) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls), parent);
    }

    public NutsURLClassLoader(File[] urls) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls));
    }

    public NutsURLClassLoader(File[] urls, ClassLoader parent, URLStreamHandlerFactory factory) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls), parent, factory);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    public void addFile(File url) {
        try {
            super.addURL(url.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new NutsIllegalArgumentException(url.toString());
        }
    }

    public void addURL(String url) {
        try {
            super.addURL(CoreIOUtils.toURL(new String[]{url})[0]);
        } catch (MalformedURLException e) {
            throw new NutsIllegalArgumentException(url.toString());
        }
    }
}