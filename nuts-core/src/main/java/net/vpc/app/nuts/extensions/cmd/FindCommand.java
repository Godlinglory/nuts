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
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.NutsDescriptorJavascriptFilter;
import net.vpc.app.nuts.boot.NutsIdPatternFilter;
import net.vpc.app.nuts.extensions.cmd.cmdline.*;
import net.vpc.app.nuts.util.PlatformUtils;
import net.vpc.app.nuts.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
public class FindCommand extends AbstractNutsCommand {

    public FindCommand() {
        super("find", CORE_SUPPORT);
    }

    class FindWhat{
        String jsCode = null;
        HashSet<String> nonjs = new HashSet<String>();
    }
    class FindContext{
        HashSet<String> arch = new HashSet<String>();
        HashSet<String> pack = new HashSet<String>();
        HashSet<String> repos = new HashSet<String>();
        boolean longflag = false;
        boolean showFile = false;
        boolean showClass = false;
        boolean jsflag = false;
        FetchMode fecthMode = FetchMode.ONLINE;
        boolean desc = false;
        boolean eff = false;
        boolean executable = true;
        boolean library = true;
        Boolean installed = null;
        Boolean installedDependencies = null;
        Boolean updatable = null;
        NutsPrintStream out;
        String display = "id";
        NutsCommandContext context;
    }

    public void run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws Exception {
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        int currentFindWhat=0;
        List<FindWhat> findWhats=new ArrayList<>();
        FindContext findContext=new FindContext();
        findContext.context=context;
        findContext.out=context.getTerminal().getOut();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.acceptAndRemoveNoDuplicates("-js", "--javascript")) {
                if(currentFindWhat+1>=findWhats.size()){
                    findWhats.add(new FindWhat());
                }
                if (findWhats.get(currentFindWhat).nonjs.size() > 0) {
                    if (!cmdLine.isExecMode()) {
                        return;
                    }
                    throw new IllegalArgumentException("Unsupported");
                }
                findContext.jsflag = true;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-x", "--expression")) {
                findContext.jsflag = false;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-l", "--long")) {
                findContext.longflag = true;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-f", "--file")) {
                findContext.showFile = true;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-c", "--class")) {
                findContext.showClass = true;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-off", "--offline")) {
                findContext.fecthMode = FetchMode.OFFLINE;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-on", "--online")) {
                findContext.fecthMode = FetchMode.ONLINE;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-R", "--remote")) {
                findContext.fecthMode = FetchMode.REMOTE;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-e", "--exec")) {
                findContext.executable=true;
                findContext.library=false;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-l", "--lib")) {
                findContext.executable=false;
                findContext.library=true;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-s", "--descriptor")) {
                findContext.desc = true;
                findContext.eff = false;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-D", "--installed-dependencies")) {
                findContext.installedDependencies = true;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-d", "--dependencies")) {
                findContext.display = "dependencies";
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-i", "--installed")) {
                findContext.installed = true;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-!i", "--non-installed")) {
                findContext.installed = false;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-u", "--updatable")) {
                findContext.updatable = true;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-!u", "--non-updatable")) {
                findContext.updatable = false;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-S", "--effective-descriptor")) {
                findContext.desc = true;
                findContext.eff = true;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-I", "--display-id")) {
                findContext.display = "id";
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-N", "--display-name")) {
                findContext.display = "name";
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-P", "--display-packaging")) {
                findContext.display = "packaging";
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-A", "--display-arch")) {
                findContext.display = "arch";
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-F", "--display-file")) {
                findContext.display = "file";
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-C", "--display-class")) {
                findContext.display = "class";
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-h", "--help")) {
                cmdLine.requireEmpty();
                if (cmdLine.isExecMode()) {
                    String help = getHelp();
                    findContext.out.println("Command " + this);
                    findContext.out.println(help);
                }
                return;
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-p", "--pkg")) {
                findContext.pack.add(cmdLine.removeNonOptionOrError(new PackagingNonOption("Packaging", context)).getString());
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-a", "--arch")) {
                findContext.arch.add(cmdLine.removeNonOptionOrError(new ArchitectureNonOption("Architecture", context)).getString());
            } else if (currentFindWhat==0 && cmdLine.acceptAndRemoveNoDuplicates("-r", "--repo")) {
                findContext.repos.add(cmdLine.removeNonOptionOrError(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString());
            } else {
                CmdLine.Val val = cmdLine.removeNonOptionOrError(new DefaultNonOption("Expression"));
                if(currentFindWhat+1>=findWhats.size()){
                    findWhats.add(new FindWhat());
                }
                if (cmdLine.isExecMode()) {
                    if (findContext.jsflag) {
                        if (findWhats.get(currentFindWhat).jsCode == null) {
                            findWhats.get(currentFindWhat).jsCode = val.getString();
                        } else {
                            throw new IllegalArgumentException("Unsupported");
                        }
                    } else {
                        String arg = val.getString();
                        if (findWhats.get(currentFindWhat).jsCode == null) {
//                            if (!arg.startsWith("*")) {
//                                arg = "*" + arg;
//                            }
//                            if (arg.startsWith("*")) {
//                                arg = arg + "*";
//                            }
                            findWhats.get(currentFindWhat).nonjs.add(arg);
                        } else {
                            throw new IllegalArgumentException("Unsupported");
                        }
                    }
                }
                currentFindWhat++;
            }
        }
        if (!cmdLine.isExecMode()) {
            return;
        }

        if (findContext.installedDependencies == null) {
            findContext.installedDependencies = false;
        }
        for (FindWhat findWhat : findWhats) {
            search(findWhat,findContext);
        }
    }

    private void search(FindWhat findWhat,FindContext findContext) throws IOException {
        if (findWhat.nonjs.isEmpty() && findWhat.jsCode == null) {
            findWhat.nonjs.add("*");
        }
        Set<String> visitedItems=new HashSet<>();
        NutsDescriptorFilter filter = null;
        if (findWhat.jsCode != null) {
            filter = new NutsDescriptorJavascriptFilter(findWhat.jsCode);
        } else {
            filter = new NutsIdPatternFilter(findWhat.nonjs.toArray(new String[findWhat.nonjs.size()]), findContext.pack.toArray(new String[findContext.pack.size()]), findContext.arch.toArray(new String[findContext.arch.size()]));
        }
        NutsWorkspace ws = findContext.context.getValidWorkspace();
        Iterator<NutsId> it = ws.find(new NutsRepositoryFilter() {
                                          @Override
                                          public boolean accept(NutsRepository repository) {
                                              return findContext.repos.isEmpty() || findContext.repos.contains(repository.getRepositoryId());
                                          }
                                      }, filter, findContext.context.getSession().copy().setTransitive(true)
//                .setOffline(offline || (installed != null && installed))
                        .setFetchMode(findContext.fecthMode)
        );
        Set<String> visitedPackaging = new HashSet<>();
        Set<String> visitedArchs = new HashSet<>();
        while (it.hasNext()) {
            NutsInfo info = new NutsInfo(it.next(), findContext.context);
            if (findContext.installed != null) {
                if (findContext.installed != info.isInstalled(findContext.installedDependencies)) {
                    continue;
                }
            }
            if (findContext.updatable != null) {
                if (findContext.updatable != info.isUpdatable()) {
                    continue;
                }
            }
            if(!findContext.executable || !findContext.library){
                if(info.getDescriptor().isExecutable()!=findContext.executable){
                    continue;
                }
            }
            if ("id".equals(findContext.display) || "dependencies".equals(findContext.display)) {
                Set<String> imports = new HashSet<String>(Arrays.asList(ws.getConfig().getImports()));

                if (findContext.longflag) {
                    String status = (info.isInstalled(findContext.installedDependencies) ? "i"
                            : info.isFetched() ? "f"
                            : "r")
                            + (info.isUpdatable() ? "u" : ".")
                            + (info.getDescriptor().isExecutable() ? "x" : ".")
                            ;
                    findContext.out.print(status);
                    findContext.out.print(" ");
                    findContext.out.print(info.getDescriptor().getPackaging());
                    findContext.out.print(" ");
                    findContext.out.print(Arrays.asList(info.getDescriptor().getArch()));
                    findContext.out.print(" ");
                    if(StringUtils.isEmpty(info.nuts.getNamespace())){
                        findContext.out.print("?");
                    }else{
                        findContext.out.print(info.nuts.getNamespace());
                    }
                    findContext.out.print(" ");
                    findContext.out.draw(format(info.nuts,imports));
                    if(findContext.showFile){
                        findContext.out.print(" ");
                        if(info.getFile()==null){
                            findContext.out.print("?");
                        }else{
                            findContext.out.print(info.getFile().getPath());
                        }
                    }
                    if(findContext.showClass){
                        findContext.out.print(" ");
                        if(info.getFile()==null){
                            findContext.out.print("?");
                        }else{
                            String cls = PlatformUtils.getMainClass(info.getFile());
                            if(cls==null){
                                findContext.out.print("?");
                            }else{
                                findContext.out.print(cls);
                            }
                        }
                    }
                } else {
                    findContext.out.draw(format(info.nuts,imports));
                    if(findContext.showFile){
                        findContext.out.print(" ");
                        if(info.getFile()==null){
                            findContext.out.print("?");
                        }else{
                            findContext.out.print(info.getFile().getPath());
                        }
                    }
                    if(findContext.showClass){
                        findContext.out.print(" ");
                        if(info.getFile()==null){
                            findContext.out.print("?");
                        }else{
                            String cls = PlatformUtils.getMainClass(info.getFile());
                            if(cls==null){
                                findContext.out.print("?");
                            }else{
                                findContext.out.print(cls);
                            }
                        }
                    }
                }
                findContext.out.println();
                if (findContext.desc) {
                    findContext.out.println(info.getDescriptor().toString());
                    findContext.out.println("");
                }
                if ("dependencies".equals(findContext.display)) {
                    List<NutsFile> depsFiles = ws.fetchWithDependencies(info.nuts.toString(), false, null, findContext.context.getSession().copy().setTransitive(true)
                            .setFetchMode(findContext.fecthMode)
                    );
                    for (NutsFile dd : depsFiles) {
                        NutsInfo dinfo = new NutsInfo(dd.getId(), findContext.context);
                        dinfo.descriptor = dd.getDescriptor();
                        if (findContext.longflag) {
                            String status = (dinfo.isInstalled(findContext.installedDependencies) ? "i"
                                    : dinfo.isFetched() ? "f"
                                    : "r") + (dinfo.isUpdatable() ? "u" : ".");
                            findContext.out.print("\t");
                            findContext.out.print(status);
                            findContext.out.print(" ");
                            findContext.out.print(dinfo.getDescriptor().getPackaging());
                            findContext.out.print(" ");
                            findContext.out.print(Arrays.asList(dinfo.getDescriptor().getArch()));
                            findContext.out.print(" ");
                            findContext.out.drawln(format(dinfo.nuts,imports));
                        } else {
                            findContext.out.print("\t");
                            findContext.out.drawln(format(dinfo.nuts,imports));
                        }
                    }
                }
            }else if ("name".equals(findContext.display)) {
                String fullName = info.nuts.getFullName();
                if(!visitedItems.contains(fullName)) {
                    visitedItems.add(fullName);
                    if (findContext.longflag) {
                        String status = (info.isInstalled(findContext.installedDependencies) ? "i"
                                : info.isFetched() ? "f"
                                : "r") + (info.isUpdatable() ? "u" : ".");
                        findContext.out.print(status);
                        findContext.out.print(" ");
                        findContext.out.print(info.getDescriptor().getPackaging());
                        findContext.out.print(" ");
                        findContext.out.print(Arrays.asList(info.getDescriptor().getArch()));
                        findContext.out.print(" ");
                        findContext.out.println(info.nuts.getFullName());
                    } else {
                        findContext.out.println(info.nuts.getFullName());
                    }
                }
            }else if ("file".equals(findContext.display)) {
                File fullName = info.getFile();
                //if(fullName!=null && !visitedItems.contains(fullName.getPath())) {
                //visitedItems.add(fullName.getPath());
                findContext.out.println(fullName.getPath());
                //}
            }else if ("class".equals(findContext.display)) {
                String fullName = PlatformUtils.getMainClass(info.getFile());
                if(fullName!=null && !visitedItems.contains(fullName)) {
                    visitedItems.add(fullName);
                    findContext.out.println(fullName);
                }
            } else if ("packaging".equals(findContext.display)) {
                NutsDescriptor d = info.getDescriptor();
                String p = d.getPackaging();
                if (!StringUtils.isEmpty(p) && !visitedPackaging.contains(p)) {
                    visitedPackaging.add(p);
                    findContext.out.println(p);
                }
            } else if ("arch".equals(findContext.display)) {
                NutsDescriptor d = info.getDescriptor();
                for (String p : d.getArch()) {
                    if (!StringUtils.isEmpty(p) && !visitedArchs.contains(p)) {
                        visitedArchs.add(p);
                        findContext.out.println(p);
                    }
                }
            }
        }
    }
    private String format(NutsId id,Set<String> imports) {
        id=id.setNamespace(null)
                .setQueryProperty(NutsConstants.QUERY_FACE, null)
                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(id.getNamespace())) {
            sb.append(id.getNamespace()).append("://");
        }
        if (!StringUtils.isEmpty(id.getGroup())) {
            if(imports.contains(id.getGroup())){
                sb.append("==");
                sb.append(id.getGroup());
                sb.append("==");
            }else {
                sb.append(id.getGroup());
            }
            sb.append(":");
        }
        sb.append("[[");
        sb.append(id.getName());
        sb.append("]]");
        if (!StringUtils.isEmpty(id.getVersion().getValue())) {
            sb.append("#");
            sb.append(id.getVersion());
        }
        if (!StringUtils.isEmpty(id.getQuery())) {
            sb.append("?");
            sb.append(id.getQuery());
        }
        return sb.toString();
    }

    private static class NutsInfo {

        NutsId nuts;
        Boolean fetched;
        Boolean is_installed;
        Boolean is_updatable;
        NutsCommandContext context;
        NutsWorkspace ws;
        NutsSession session;
        NutsDescriptor descriptor;
        NutsFile _fetchedFile;

        public NutsInfo(NutsId nuts, NutsCommandContext context) throws IOException {
            this.nuts = nuts;
            this.context = context;
            ws = context.getValidWorkspace();
            session = context.getSession();
        }

        public boolean isFetched() {
            if (this.fetched == null) {
                try {
                    this.fetched = ws.isFetched(nuts.toString(), session);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return this.fetched;
        }

        public boolean isInstalled(boolean checkDependencies) {
            if (this.is_installed == null) {
                try {
                    this.is_installed = isFetched() && ws.isInstalled(nuts.toString(), checkDependencies, session);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return this.is_installed;
        }

        public boolean isUpdatable() {
            if (this.is_updatable == null) {
                try {
                    this.is_updatable = false;
                    if (this.isFetched()) {
                        NutsId nut2 = null;
                        try {
                            nut2 = ws.resolveId(nuts.setVersion(null).toString(), session.copy().setTransitive(true).setFetchMode(FetchMode.REMOTE));
                        } catch (Exception ex) {
                            //ignore
                        }
                        if (nut2 != null && nut2.getVersion().compareTo(nuts.getVersion()) > 0) {
                            this.is_updatable = true;
                        }
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
            return this.is_updatable;
        }


        public File getFile() {
            if(_fetchedFile==null) {
                try {
                    _fetchedFile = ws.fetch(nuts.toString(), session.copy().setTransitive(true).setFetchMode(FetchMode.OFFLINE));
                } catch (Exception ex) {
                    _fetchedFile = new NutsFile(null, null, null, false, false);
                }
            }
            return _fetchedFile.getFile();
        }

        public NutsDescriptor getDescriptor() {
            if (descriptor == null) {
                try {
//                    NutsDescriptor dd = ws.fetchDescriptor(nuts.toString(), true, session.copy().setTransitive(true).setFetchMode(FetchMode.ONLINE));
//                    if(dd.isExecutable()){
//                        System.out.println("");
//                    }
                    descriptor = ws.fetchDescriptor(nuts.toString(), true, session.copy().setTransitive(true).setFetchMode(FetchMode.ONLINE));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return descriptor;
        }

    }
}
