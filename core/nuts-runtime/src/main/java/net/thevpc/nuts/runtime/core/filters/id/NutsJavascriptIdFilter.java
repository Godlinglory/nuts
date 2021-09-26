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
package net.thevpc.nuts.runtime.core.filters.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.common.JavascriptHelper;
import net.thevpc.nuts.runtime.core.util.Simplifiable;

import java.util.Objects;
import java.util.Set;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsJavascriptIdFilter extends AbstractIdFilter implements NutsIdFilter, Simplifiable<NutsIdFilter>, NutsScriptAwareIdFilter {

//    private static NutsId SAMPLE_NUTS_ID = new DefaultNutsId("sample", "sample", "sample", "sample", "sample");

    private String code;

    public static NutsIdFilter valueOf(String value, NutsSession ws) {
        if (NutsBlankable.isBlank(value)) {
            return ws.getWorkspace().id().filter().always();
        }
        return new NutsJavascriptIdFilter(ws,value);
    }


    public NutsJavascriptIdFilter(NutsSession ws,String code) {
        super(ws, NutsFilterOp.CUSTOM);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean acceptId(NutsId id, NutsSession session) {
        Set<String> blacklist = null;
        JavascriptHelper engineHelper = new JavascriptHelper(code, "var id=x.getId(); var version=id.getVersion();", blacklist, null, session);
        return engineHelper.accept(id);
    }

    @Override
    public NutsIdFilter simplify() {
        return this;
    }

    @Override
    public String toJsNutsIdFilterExpr() {
//        return "util.matches(dependency,'" + CoreStringUtils.escapeCoteStrings(code) + "')";
        return getCode();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.code);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutsJavascriptIdFilter other = (NutsJavascriptIdFilter) obj;
        if (!Objects.equals(this.code, other.code)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NutsIdJavascriptFilter{" + code + '}';
    }

}
