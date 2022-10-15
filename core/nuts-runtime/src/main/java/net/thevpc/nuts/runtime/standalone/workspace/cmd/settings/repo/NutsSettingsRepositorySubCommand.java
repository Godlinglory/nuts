/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.repo;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsArgumentName;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.format.NutsMutableTableModel;
import net.thevpc.nuts.format.NutsTableFormat;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNutsSettingsSubCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user.NutsSettingsUserSubCommand;
import net.thevpc.nuts.spi.NutsRepositoryDB;
import net.thevpc.nuts.spi.NutsRepositoryLocation;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;

import java.util.*;

/**
 * @author thevpc
 */
public class NutsSettingsRepositorySubCommand extends AbstractNutsSettingsSubCommand {

    public static RepoInfo repoInfo(NutsRepository x, boolean tree, NutsSession session) {
        return new RepoInfo(x.getName(), x.config().getType(), x.config().getLocationPath(), x.config().isEnabled() ? RepoStatus.enabled : RepoStatus.disabled,
                tree ? x.config().setSession(session).getMirrors()
                        .stream().map(e -> repoInfo(e, tree, session)).toArray(RepoInfo[]::new) : null
        );
    }

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {

//        NutsWorkspace ws = session.getWorkspace();
//        if (cmdLine.next("add repo", "cr") != null) {
//            String repositoryName = null;
//            String location = null;
//            String repoType = null;
//            while (cmdLine.hasNext()) {
//                if (cmdLine.next("-t", "--type") != null) {
//                    repoType = cmdLine.required().nextNonOption(commandLineFormat.createName("repository-type")).getString();
//                } else if (cmdLine.next("-l", "--location") != null) {
//                    location = cmdLine.nextNonOption(commandLineFormat.createName("folder")).getString();
//                } else if (cmdLine.next("-id", "--id") != null) {
//                    repositoryName = cmdLine.required().nextNonOption(commandLineFormat.createName("NewRepositoryName")).getString();
//                } else if (!cmdLine.isNextOption()) {
//                    location = cmdLine.nextNonOption(commandLineFormat.createName("RepositoryLocation")).getString();
//                } else {
//                    cmdLine.setCommandName("config add repo").throwUnexpectedArgument();
//                }
//            }
//            if (cmdLine.isExecMode()) {
//                NutsRepository repository = ws.repos().addRepository(
//                        new NutsAddRepositoryOptions()
//                                .setName(repositoryName)
//                                .setLocation(repositoryName)
//                                .setConfig(
//                                        new NutsRepositoryConfig507()
//                                                .setName(repositoryName)
//                                                .setLocation(location)
//                                                .setLocationType(repoType))
//                );
//                if (repository == null) {
//                    throw new NutsIllegalArgumentException(context.getWorkspace(), "unable to configure repository : " + repositoryName);
//                }
//                context.getWorkspace().config().save();
//            }
//            return true;
//
//        } else {
        NutsPrintStream out = session.out();
        if (cmdLine.next("add repo", "ar").isPresent()) {
            String location = null;
            String repositoryName = null;
            String parent = null;
            Map<String, String> env = new LinkedHashMap<>();
            while (cmdLine.hasNext()) {
                NutsArgument a = cmdLine.peek().get(session);
                boolean enabled = a.isActive();
                switch(a.getStringKey().orElse("")) {
                    case "-l":
                    case "--location": {
                        String val = cmdLine.nextStringValueLiteral().get(session);
                        if (enabled) {
                            location = val;
                        }
                        break;
                    }
                    case "--name": {
                        String val = cmdLine.nextStringValueLiteral().get(session);
                        if (enabled) {
                            repositoryName = val;
                        }
                        break;
                    }
                    case "--parent": {
                        String val = cmdLine.nextStringValueLiteral().get(session);
                        if (enabled) {
                            parent = val;
                        }
                        break;
                    }
                    case "--env": {
                        String val = cmdLine.nextStringValueLiteral().get(session);
                        if (enabled) {
                            NutsArgument vv = NutsArgument.of(val);
                            env.put(vv.getKey() == null ? null : vv.getKey().asString().get(session),
                                    vv.getValue() == null ? null : vv.getStringValue().get(session));
                        }
                        break;
                    }
                    default: {
                        if (!session.configureFirst(cmdLine)) {
                            if (a.isOption()) {
                                cmdLine.throwUnexpectedArgument(session);
                            } else if (a.isKeyValue()) {
                                NutsArgument n = cmdLine.nextString().get(session);
                                repositoryName = n.getStringKey().get(session);
                                location = n.getStringValue().get(session);
                            } else {
                                location = cmdLine.next().flatMap(NutsValue::asString).get(session);
                                String loc2 = NutsRepositoryDB.of(session).getRepositoryLocationByName(location);
                                if(loc2!=null){
                                    repositoryName=location;
                                    location=loc2;
                                }else{
                                    cmdLine.peek().get(session);
                                }
                            }
                        }
                        break;
                    }
                }
            }
            if (repositoryName == null) {
                cmdLine.peek().get(session);
            }

            if (cmdLine.isExecMode()) {
                NutsRepository repo = null;
                NutsAddRepositoryOptions o = new NutsAddRepositoryOptions()
                        .setName(repositoryName)
                        .setLocation(repositoryName)
                        .setConfig(
                                location == null ? null : new NutsRepositoryConfig()
                                        .setName(repositoryName)
                                        .setLocation(NutsRepositoryLocation.of(location))
                                        .setEnv(env));
                if (parent == null) {
                    repo = session.repos().addRepository(o);
                } else {
                    NutsRepository p = session.repos().getRepository(parent);
                    repo = p.config().addMirror(o);
                }
                out.printlnf("repository %s added successfully",repo.getName());
                session.config().save();

            }
            cmdLine.setCommandName("config add repo").throwUnexpectedArgument(session);
            return true;
        } else if (cmdLine.next("remove repo", "rr").isPresent()) {
            String repositoryName = null;
            String parent = null;
            while (cmdLine.hasNext()) {
                NutsArgument a = cmdLine.peek().get(session);
                boolean enabled = a.isActive();
                switch(a.getStringKey().orElse("")) {
                    case "--name": {
                        String val = cmdLine.nextStringValueLiteral().get(session);
                        if (enabled) {
                            repositoryName = val;
                        }
                        break;
                    }
                    case "--parent": {
                        String val = cmdLine.nextStringValueLiteral().get(session);
                        if (enabled) {
                            parent = val;
                        }
                        break;
                    }
                    default: {
                        if (!session.configureFirst(cmdLine)) {
                            if (a.isOption()) {
                                cmdLine.throwUnexpectedArgument(session);
                            } else if (repositoryName != null) {
                                cmdLine.throwUnexpectedArgument(session);
                            } else {
                                repositoryName = cmdLine.next().flatMap(NutsValue::asString).get(session);
                            }
                        }
                        break;
                    }
                }
            }
            if (repositoryName == null) {
                cmdLine.peek().get(session);
            }
            if (cmdLine.isExecMode()) {
                if (parent == null) {
                    session.repos().removeRepository(repositoryName);
                } else {
                    NutsRepository p = session.repos().getRepository(parent);
                    p.config().removeMirror(repositoryName);
                }
                session.config().save();
            }
            return true;

        } else if (cmdLine.next("list repos", "lr").isPresent()) {
            cmdLine.setCommandName("config list repos");
            String parent = null;
            while (cmdLine.hasNext()) {
                while (cmdLine.hasNext()) {
                    NutsArgument a = cmdLine.peek().get(session);
                    boolean enabled = a.isActive();
                    switch(a.getStringKey().orElse("")) {
                        case "--parent": {
                            String val = cmdLine.nextStringValueLiteral().get(session);
                            if (enabled) {
                                parent = val;
                            }
                            break;
                        }
                        default: {
                            if (!session.configureFirst(cmdLine)) {
                                if (a.isOption()) {
                                    cmdLine.throwUnexpectedArgument(session);
                                } else if (parent != null) {
                                    cmdLine.throwUnexpectedArgument(session);
                                } else {
                                    parent = cmdLine.next().flatMap(NutsValue::asString).get(session);
                                }
                            }
                            break;
                        }
                    }
                }
            }
            if (cmdLine.isExecMode()) {
                List<NutsRepository> r = parent == null ? session.repos().getRepositories() : session.repos().getRepository(parent).config().getMirrors();
                out.printlnf(
                        session.repos().getRepositories().stream()
                                .map(x -> repoInfo(x, session.getOutputFormat() != NutsContentType.TABLE && session.getOutputFormat() != NutsContentType.PLAIN, session)
                                )
                                .toArray()
                );
            }
            return true;

        } else if (cmdLine.next("enable repo", "er").isPresent()) {
            enableRepo(cmdLine, autoSave, session, true);
            return true;
        } else if (cmdLine.next("disable repo", "dr").isPresent()) {
            enableRepo(cmdLine, autoSave, session, false);
            return true;
        } else if (cmdLine.next("edit repo", "er").isPresent()) {
            String repoId = cmdLine.nextNonOption(NutsArgumentName.of("RepositoryName", session)).flatMap(NutsValue::asString).get(session);
            if (cmdLine.next("add repo", "ar").isPresent()) {
                String repositoryName = cmdLine.nextNonOption(NutsArgumentName.of("NewRepositoryName", session)).flatMap(NutsValue::asString).get(session);
                String location = cmdLine.nextNonOption(NutsArgumentName.of("folder", session)).flatMap(NutsValue::asString).get(session);

                NutsRepository editedRepo = session.repos().getRepository(repoId);
                NutsRepository repo = editedRepo.config().addMirror(
                        new NutsAddRepositoryOptions().setName(repositoryName).setLocation(repositoryName)
                                .setConfig(
                                        new NutsRepositoryConfig()
                                                .setName(repositoryName)
                                                .setLocation(NutsRepositoryLocation.of(location))
                                ));
                session.config().save();

            } else if (cmdLine.next("remove repo", "rr").isPresent()) {
                String location = cmdLine.nextNonOption(NutsArgumentName.of("RepositoryName", session)).flatMap(NutsValue::asString).get(session);
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                editedRepo.config().removeMirror(location);
                session.config().save();

            } else if (cmdLine.next("enable", "br").isPresent()) {
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                editedRepo.config().setEnabled(true);
                session.config().save();

            } else if (cmdLine.next("disable", "dr").isPresent()) {
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                editedRepo.config().setEnabled(true);
                session.config().save();
            } else if (cmdLine.next("list repos", "lr").isPresent()) {
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                List<NutsRepository> linkRepositories = editedRepo.config()
                        .setSession(session)
                        .isSupportedMirroring()
                        ? editedRepo.config().setSession(session).getMirrors() : Collections.emptyList();
                out.printf("%s sub repositories.%n", linkRepositories.size());
                NutsTableFormat t = NutsTableFormat.of(session);
                NutsMutableTableModel m = NutsMutableTableModel.of(session);
                t.setValue(m);
                m.addHeaderCells("Id", "Enabled", "Type", "Location");
                while (cmdLine.hasNext()) {
                    if (!t.configureFirst(cmdLine)) {
                        cmdLine.setCommandName("config edit repo").throwUnexpectedArgument(session);
                    }
                }
                for (NutsRepository repository : linkRepositories) {
                    m.addRow(
                            NutsTexts.of(session).ofStyled(repository.getName(), NutsTextStyle.primary4()),
                            repository.config().isEnabled()
                                    ? repository.isEnabled(session) ? NutsTexts.of(session).ofStyled("ENABLED", NutsTextStyle.success())
                                    : NutsTexts.of(session).ofStyled("<RT-DISABLED>", NutsTextStyle.error())
                                    : NutsTexts.of(session).ofStyled("<DISABLED>", NutsTextStyle.error()),
                            repository.getRepositoryType(),
                            repository.config().getLocation().toString()
                    );
                }
                out.printf(t.toString());
            } else if (cmdLine.next("-h", "-?", "--help").isPresent()) {
                out.printf("edit repository %s add repo ...%n", repoId);
                out.printf("edit repository %s remove repo ...%n", repoId);
                out.printf("edit repository %s list repos ...%n", repoId);
            } else {
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                if (NutsSettingsUserSubCommand.exec(editedRepo, cmdLine, autoSave, session)) {
                    //okkay
                } else {
                    throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("config edit repo: Unsupported command %s", cmdLine));
                }
            }
            return true;
        }
//        }
        return false;
    }

    private void enableRepo(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session, boolean enableRepo) {
        String repositoryName = null;
        while (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.peek().get(session);
            boolean enabled = a.isActive();
            switch(a.getStringKey().orElse("")) {
                case "--name": {
                    String val = cmdLine.nextStringValueLiteral().get(session);
                    if (enabled) {
                        repositoryName = val;
                    }
                    break;
                }
                default: {
                    if (!session.configureFirst(cmdLine)) {
                        if (a.isOption()) {
                            cmdLine.throwUnexpectedArgument(session);
                        } else if (repositoryName == null) {
                            repositoryName = cmdLine.next().flatMap(NutsValue::asString).get(session);
                        } else {
                            cmdLine.throwUnexpectedArgument(session);
                        }
                    }
                    break;
                }
            }
        }
        if (repositoryName == null) {
            cmdLine.peek().get(session);
        }
        if (cmdLine.isExecMode()) {
            NutsRepository editedRepo = session.repos().getRepository(repositoryName);
            editedRepo.config().setEnabled(enableRepo);
            session.config().save();
        }
    }

    public enum RepoStatus {
        enabled,
        disabled,
    }

    public static class RepoInfo {

        String name;
        String type;
        NutsPath location;
        RepoStatus enabled;
        RepoInfo[] mirrors;

        public RepoInfo(String name, String type, NutsPath location, RepoStatus enabled, RepoInfo[] mirrors) {
            this.name = name;
            this.type = type;
            this.location = location;
            this.enabled = enabled;
            this.mirrors = mirrors;
        }

        public RepoInfo() {
        }

        public RepoInfo[] getMirrors() {
            return mirrors;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public NutsPath getLocation() {
            return location;
        }

        public void setLocation(NutsPath location) {
            this.location = location;
        }

        public RepoStatus getEnabled() {
            return enabled;
        }

        public void setEnabled(RepoStatus enabled) {
            this.enabled = enabled;
        }
    }
}
