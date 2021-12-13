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
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom;

/**
 *
 * @author thevpc
 */
public class URLPart {

    private String type;
    private String path;

    public URLPart(String type, String path) {
        this.type = type;
        this.path = path;
    }

    public String getName() {
        String n = path;
        final int p = n.lastIndexOf('/');
        if (p > 0) {
            n = path.substring(p + 1);
        }
        return n;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "PathItem{" + "type=" + type + ", path=" + path + '}';
    }

}
