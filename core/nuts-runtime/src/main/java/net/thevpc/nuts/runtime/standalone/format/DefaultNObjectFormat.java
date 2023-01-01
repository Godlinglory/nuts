/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
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
package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.format.NTableFormat;
import net.thevpc.nuts.format.NTreeFormat;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.runtime.standalone.format.plain.NFormatPlain;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.util.NFunction;

import java.io.File;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Collections;

/**
 * @author thevpc
 */
public class DefaultNObjectFormat extends DefaultFormatBase<NObjectFormat> implements NObjectFormat {

    private Object value;

    private boolean compact;
    private NContentType outputFormat;
//    private NutsObjectFormat base;

    public DefaultNObjectFormat(NSession session) {
        super(session, "object-format");
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NObjectFormat setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public DefaultNObjectFormat setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    public NContentTypeFormat getBase() {
        checkSession();
        NSession session = getSession();
        NContentTypeFormat base = createObjectFormat();
        base.setSession(session);
        base.configure(true, session.boot().getBootOptions().getOutputFormatOptions().orElseGet(Collections::emptyList).toArray(new String[0]));
        base.configure(true, session.getOutputFormatOptions().toArray(new String[0]));
        return base;
    }

    public NContentTypeFormat createObjectFormat() {
        checkSession();
        NSession session = getSession();
        Object value = getValue();
        switch (getSession().getOutputFormat()) {
            //structured formats!
            case XML:
            case JSON:
            case TSON:
            case YAML: {
                NElements ee = NElements.of(session).setNtf(isNtf())
                        .setCompact(isCompact())
                        .setContentType(getSession().getOutputFormat());
                if (value instanceof NString) {
                    NTextBuilder builder = ((NString) value).builder();
                    Object[] r = builder.lines().map(
                            NFunction.of(
                                    x -> {
                                        if (true) {
                                            return x.filteredText();
                                        }
                                        return (Object) x.filteredText();
                                    },
                                    "filteredText"
                            )
                    ).toArray(Object[]::new);
                    ee.setValue(r);
                } else {
                    ee.setValue(value);
                }
                return ee;
            }
            case PROPS: {
                NPropertiesFormat ee = NPropertiesFormat.of(session).setNtf(isNtf());
                if (value instanceof NString) {
                    NTextBuilder builder = ((NString) value).builder();
                    Object[] r = builder.lines().toArray(Object[]::new);
                    ee.setValue(r);
                } else {
                    ee.setValue(value);
                }
                return ee;
            }
            case TREE: {
                NTreeFormat ee = NTreeFormat.of(session).setNtf(isNtf());
                if (value instanceof NString) {
                    NTextBuilder builder = ((NString) value).builder();
                    Object[] r = builder.lines().toArray(Object[]::new);
                    ee.setValue(r);
                } else {
                    ee.setValue(value);
                }
                return ee;
            }
            case TABLE: {
                NTableFormat ee = NTableFormat.of(session).setNtf(isNtf());
                if (value instanceof NString) {
                    NTextBuilder builder = ((NString) value).builder();
                    Object[] r = builder.lines().toArray(Object[]::new);
                    ee.setValue(r);
                } else {
                    ee.setValue(value);
                }
                return ee;
            }
            case PLAIN: {
                NFormatPlain ee = new NFormatPlain(session).setCompact(isCompact()).setNtf(isNtf());
                ee.setValue(value);
                return ee;
            }
        }
        throw new NUnsupportedEnumException(getSession(), getSession().getOutputFormat());
    }

//    @Override
//    public NutsSession getSession() {
//        return base != null ? base.getSession() : super.getSession();
//    }

    @Override
    public NString format() {
        return getBase().format();
    }

    @Override
    public void print() {
        getBase().print();
    }

    @Override
    public void println() {
        getBase().println();
    }

    @Override
    public void print(NOutStream out) {
        getBase().print(out);
    }

    @Override
    public void print(Writer out) {
        getBase().print(out);
    }

    @Override
    public void print(Path out) {
        getBase().print(out);
    }

    @Override
    public void print(File out) {
        getBase().print(out);
    }

    @Override
    public void print(NSessionTerminal terminal) {
        getBase().print(terminal);
    }

    @Override
    public void println(Writer w) {
        getBase().println(w);
    }

    @Override
    public void println(NOutStream out) {
        getBase().println(out);
    }

    @Override
    public void println(Path path) {
        getBase().println(path);
    }

    @Override
    public void println(NSessionTerminal terminal) {
        getBase().println(terminal);
    }

    @Override
    public void println(File file) {
        getBase().println(file);
    }

    @Override
    public boolean configureFirst(NCommandLine commandLine) {
        return getBase().configureFirst(commandLine);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
