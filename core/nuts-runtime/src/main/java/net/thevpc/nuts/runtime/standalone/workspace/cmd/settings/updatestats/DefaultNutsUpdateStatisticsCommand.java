/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.updatestats;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsRepositoryFolderHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenRepositoryFolderHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositorySPI;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsUtils;

/**
 * @author thevpc
 */
public class DefaultNutsUpdateStatisticsCommand extends AbstractNutsUpdateStatisticsCommand {

    public DefaultNutsUpdateStatisticsCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsUpdateStatisticsCommand run() {
        boolean processed = false;
        NutsSession session = getSession();
        checkSession();
        for (String repository : getRepositrories()) {
            processed = true;
            NutsRepository repo = getSession().repos().getRepository(repository);
            NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(repo);
            repoSPI.updateStatistics()
                    .setSession(session)
                    //                    .setFetchMode(NutsFetchMode.LOCAL)
                    .run();
        }
        for (Path repositoryPath : getPaths()) {
            processed = true;
            NutsUtils.requireNonBlank(repositoryPath, "location", session);
            if (!Files.isDirectory(repositoryPath)) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofCstyle("expected folder at location %s",repositoryPath));
            }
            File[] mavenRepoRootFiles = repositoryPath.toFile().listFiles(x
                    -> x.getName().equals("index.html")
                    || x.getName().equals("plugin-management.html")
                    || x.getName().equals("distribution-management.html")
                    || x.getName().equals("dependency-info.html")
                    || x.getName().equals("dependency-convergence.html")
                    || x.getName().equals("dependencies.html")
                    || x.getName().equals("plugins.html")
                    || x.getName().equals("project-info.html")
                    || x.getName().equals("project-summary.html")
            );
            if (mavenRepoRootFiles != null && mavenRepoRootFiles.length > 3) {
                new MavenRepositoryFolderHelper(null, getSession(), NutsPath.of(repositoryPath,session)).reindexFolder(getSession());
                if (session.isPlainTrace()) {
                    session.getTerminal().out().resetLine().printf("[%s] updated maven index %s%n", getSession().locations().getWorkspaceLocation(), repositoryPath);
                }
            } else {
                File[] nutsRepoRootFiles = repositoryPath.toFile().listFiles(x
                        -> x.getName().equals("nuts-repository.json")
                );
                if (nutsRepoRootFiles != null && nutsRepoRootFiles.length > 0) {
                    new NutsRepositoryFolderHelper(null, session, NutsPath.of(repositoryPath,session), false,"stats",null).reindexFolder(session);
                } else {
                    throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofPlain("unsupported repository folder"));
                }
                if (session.isPlainTrace()) {
                    session.out().resetLine().printf("[%s] updated stats %s%n", getSession().locations().getWorkspaceLocation(), repositoryPath);
                }
            }
        }
        NutsTexts factory = NutsTexts.of(getSession());
        if (!processed) {
            if (session.isPlainTrace()) {
                session.out().resetLine().printf("%s updating workspace stats%n", getSession().locations().getWorkspaceLocation());
            }
            for (NutsRepository repo : getSession().repos().getRepositories()) {
                if (session.isPlainTrace()) {
                    session.out().resetLine().printf("%s updating stats %s%n", getSession().locations().getWorkspaceLocation(), repo);
                }
                NutsWorkspaceUtils.of(session).repoSPI(repo).updateStatistics()
                        .setSession(session)
                        //                        .setFetchMode(NutsFetchMode.LOCAL)
                        .run();
            }
        }
        return this;
    }

    @Override
    public void add(String repo) {
        NutsUtils.requireNonBlank(repo, "repository or path", session);
        if (repo.equals(".") || repo.equals("..") || repo.contains("/") || repo.contains("\\")) {
            addPath(Paths.get(repo));
        } else {
            addRepo(repo);
        }
    }
}
