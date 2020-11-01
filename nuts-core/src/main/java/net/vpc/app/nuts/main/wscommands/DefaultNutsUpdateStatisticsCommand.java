/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.wscommands;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.wscommands.AbstractNutsUpdateStatisticsCommand;
import net.vpc.app.nuts.runtime.bridges.maven.MavenRepositoryFolderHelper;
import net.vpc.app.nuts.main.repos.NutsRepositoryFolderHelper;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceHelper;

/**
 * @author vpc
 */
public class DefaultNutsUpdateStatisticsCommand extends AbstractNutsUpdateStatisticsCommand {

    public DefaultNutsUpdateStatisticsCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsUpdateStatisticsCommand run() {
        boolean processed = false;
        NutsSession session = getSession();
        for (String repository : getRepositrories()) {
            processed = true;
            NutsRepository repo = ws.repos().getRepository(repository, session.copy().setTransitive(false));
            repo.updateStatistics()
                    .setSession(session)
//                    .setFetchMode(NutsFetchMode.LOCAL)
                    .run();
        }
        for (Path repositoryPath : getPaths()) {
            processed = true;
            if (repositoryPath == null) {
                throw new NutsIllegalArgumentException(ws, "Missing location " + repositoryPath);
            }
            if (!Files.isDirectory(repositoryPath)) {
                throw new NutsIllegalArgumentException(ws, "Expected folder at location " + repositoryPath);
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
                new MavenRepositoryFolderHelper(null, ws, repositoryPath).reindexFolder();
                if (session.isPlainTrace()) {
                    session.getTerminal().out().printf("[%s] updated maven index %s%n", getWorkspace().locations().getWorkspaceLocation(), repositoryPath);
                }
            } else {
                File[] nutsRepoRootFiles = repositoryPath.toFile().listFiles(x
                        -> x.getName().equals("nuts-repository.json")
                );
                if (nutsRepoRootFiles != null && nutsRepoRootFiles.length > 0) {
                    new NutsRepositoryFolderHelper(null, ws, repositoryPath,false).reindexFolder();
                } else {
                    throw new NutsIllegalArgumentException(ws, "Unsupported repository Folder");
                }
                if (session.isPlainTrace()) {
                    session.out().printf("[%s] updated stats %s%n", getWorkspace().locations().getWorkspaceLocation(), repositoryPath);
                }
            }
        }
        if (!processed) {
            if (session.isPlainTrace()) {
                session.out().printf("[[%s]] Updating workspace stats%n", getWorkspace().locations().getWorkspaceLocation());
            }
            for (NutsRepository repo : getWorkspace().repos().getRepositories(session)) {
                if (session.isPlainTrace()) {
                    session.out().printf("[[%s]] Updating stats %s%n", getWorkspace().locations().getWorkspaceLocation(), repo);
                }
                repo.updateStatistics()
                        .setSession(session)
//                        .setFetchMode(NutsFetchMode.LOCAL)
                        .run();
            }
        }
        return this;
    }

    @Override
    public void add(String repo) {
        if (repo == null) {
            throw new NutsIllegalArgumentException(getWorkspace(), "Missing repo or path");
        }
        if (repo.equals(".") || repo.equals("..") || repo.contains("/") || repo.contains("\\")) {
            addPath(Paths.get(repo));
        } else {
            addRepo(repo);
        }
    }
}
