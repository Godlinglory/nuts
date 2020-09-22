/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime;

import java.util.Arrays;
import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyTreeNode;

/**
 *
 * @author vpc
 */
public class MutableNutsDependencyTreeNode implements NutsDependencyTreeNode {

    public static final long serialVersionUID = 1L;
    private NutsDependency dependency;
    private NutsDependencyTreeNode[] children;
    private boolean partial;

    public MutableNutsDependencyTreeNode() {
    }
    
    public MutableNutsDependencyTreeNode(NutsDependencyTreeNode n) {
        this.dependency=n.getDependency();
        NutsDependencyTreeNode[] ch = n.getChildren();
        this.children=Arrays.copyOf(ch, ch.length);
        this.partial=n.isPartial();
    }
    

    public void setDependency(NutsDependency dependency) {
        this.dependency = dependency;
    }

    public void setChildren(NutsDependencyTreeNode[] children) {
        this.children = children;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }

    @Override
    public NutsDependency getDependency() {
        return dependency;
    }

    @Override
    public NutsDependencyTreeNode[] getChildren() {
        return children;
    }

    @Override
    public boolean isPartial() {
        return partial;
    }
}
