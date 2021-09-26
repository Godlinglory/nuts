/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.*;

import java.io.File;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class UnzipCommand extends SimpleNshBuiltin {

    public UnzipCommand() {
        super("unzip", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean l = false;
        boolean skipRoot = false;
        String dir = null;
        List<String> files = new ArrayList<>();
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a;
        if ((a = commandLine.nextBoolean("-l")) != null) {
            options.l = a.getValue().getBoolean();
            return true;
        } else if ((a = commandLine.nextString("-d")) != null) {
            options.dir = a.getValue().getString();
            return true;
        } else if (!commandLine.peek().isOption()) {
            while (commandLine.hasNext()) {
                options.files.add(commandLine.next().getString());
            }
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.files.isEmpty()) {
            commandLine.required();
        }
        for (String path : options.files) {
            File file = new File(context.getRootContext().getAbsolutePath(path));
            try {
                if (options.l) {
                    context.getSession().getWorkspace().io().uncompress()
                            .from(file.getPath())
                                    .visit(new NutsIOUncompressVisitor() {
                                        @Override
                                        public boolean visitFolder(String path) {
                                            return true;
                                        }

                                        @Override
                                        public boolean visitFile(String path, InputStream inputStream) {
                                            context.out().printf("%s\n", path);
                                            return true;
                                        }
                                    });
                } else {
                    String dir = options.dir;
                    if (NutsBlankable.isBlank(dir)) {
                        dir = context.getRootContext().getCwd();
                    }
                    dir = context.getRootContext().getAbsolutePath(dir);
                    context.getSession().getWorkspace().io().uncompress()
                                    .from(file.getPath())
                                    .to(dir)
                                            .setSkipRoot(options.skipRoot)
                                                    .run();
                }
            } catch (UncheckedIOException| NutsIOException ex) {
                throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("%s",ex), ex, 1);
            }
        }
    }
}
