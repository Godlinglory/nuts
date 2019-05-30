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
package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsCommandAutoComplete;

/**
 * Created by vpc on 1/7/17.
 */
public abstract class AbstractNshCommand implements NshCommand {

    private static final Logger LOG = Logger.getLogger(AbstractNshCommand.class.getName());
    private final String name;
    private final int supportLevel;
    private String help;
    private boolean enabled = true;

    public AbstractNshCommand(String name, int supportLevel) {
        this.name = name;
        this.supportLevel = supportLevel;
    }

    protected NutsCommand cmdLine(String[] args, NutsCommandContext context) {
        return context.getWorkspace().parser()
                .parseCommand(args)
                .autoComplete(context.shellContext().getAutoComplete())
                .commandName(getName());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int getSupportLevel(NutsJavaShell param) {
        return supportLevel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHelpHeader() {
        String h = getHelp();
        BufferedReader r = new BufferedReader(new StringReader(h));
        while (true) {
            String line = null;
            try {
                line = r.readLine();
            } catch (IOException e) {
                //
            }
            if (line == null) {
                break;
            }
            if (!StringUtils.isEmpty(line)) {
                return line;
            }
        }
        return "No help";
    }

    @Override
    public String getHelp() {
        if (help == null) {
            try {
                URL resource = getClass().getResource("/net/vpc/app/nuts/toolbox/nsh/cmd/" + getName() + ".help");
                if (resource != null) {
                    help = IOUtils.loadString(resource);
                }
            } catch (Exception e) {
                LOG.log(Level.CONFIG, "Unable to load help for " + getName(), e);
            }
            if (help == null) {
                help = "@@no help@@ found for command " + getName();
            }
        }
        return help;
    }

    @Override
    public void autoComplete(NutsCommandContext context, NutsCommandAutoComplete autoComplete) {
        NutsCommandAutoComplete oldAutoComplete = context.shellContext().getAutoComplete();
        context.shellContext().setAutoComplete(autoComplete);
        try {
            if (autoComplete == null) {
                throw new NutsIllegalArgumentException(context.getWorkspace(), "Missing Auto Complete");
            }
            NutsCommandAutoCompleteComponent best = context.getWorkspace().extensions().createServiceLoader(
                    NutsCommandAutoCompleteComponent.class, NshCommand.class, NutsCommandAutoCompleteComponent.class.getClassLoader())
                    .loadBest(this);
            if (best != null) {
                best.autoComplete(this, context);
            } else {
                String[] args = autoComplete.getWords().toArray(new String[0]);
                try {
                    exec(args, context);
                } catch (Exception ex) {
                    //ignore
                }
            }
        } finally {
            context.shellContext().setAutoComplete(oldAutoComplete);
        }
    }

    @Override
    public abstract int exec(String[] args, NutsCommandContext context) throws Exception;

}
