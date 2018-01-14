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
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsCommandAutoComplete;
import net.vpc.app.nuts.NutsCommandAutoCompleteComponent;
import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public abstract class AbstractNutsCommand implements NutsCommand {

    private static final Logger log = Logger.getLogger(AbstractNutsCommand.class.getName());
    private String name;
    private int supportLevel;
    private String help;

    public AbstractNutsCommand(String name, int supportLevel) {
        this.name = name;
        this.supportLevel = supportLevel;
    }

    @Override
    public int getSupportLevel(Object param) {
        return supportLevel;
    }

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
            if (!CoreStringUtils.isEmpty(line)) {
                return line;
            }
        }
        return "No help";
    }

    public String getHelp() {
        if (help == null) {
            try {
                InputStream s=null;
                try {
                    s = getClass().getResourceAsStream("/net/vpc/app/nuts/extensions/cmd/" + getName() + ".help");
                    if (s != null) {
                        help = CoreIOUtils.readStreamAsString(s, true);
                    }
                }finally {
                    if(s!=null){
                        s.close();
                    }
                }
            } catch (IOException e) {
                log.log(Level.SEVERE, "Unable to load help for " + getName(), e);
            }
            if (help == null) {
                help = "no help found for command " + getName();
            }
        }
        return help;
    }

    @Override
    public void autoComplete(NutsCommandAutoComplete autoComplete) {
        NutsCommandAutoCompleteComponent best = autoComplete.getCommandContext().getWorkspace().getFactory().createSupported(NutsCommandAutoCompleteComponent.class,this);
        if (best != null) {
            best.autoComplete(this, autoComplete);
        } else {
            String[] args = autoComplete.getWords().toArray(new String[autoComplete.getWords().size()]);
            try {
                run(args, autoComplete.getCommandContext(), autoComplete);
            } catch (Exception ex) {
                //ignore
            }
        }
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        return run(args, context, null);
    }

    public abstract int run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws Exception;
}
