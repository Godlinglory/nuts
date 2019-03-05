package net.vpc.app.nuts.indexer.services;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.indexer.NutsIndexerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RefreshDataService {

    @Autowired
    private DataService dataService;

    private static final Logger logger = LoggerFactory.getLogger(RefreshDataService.class);

    private NutsWorkspaceListManager workspaceManager= Nuts.openWorkspace().getConfigManager().createWorkspaceListManager("clown");

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void refreshData() {
        Collection<NutsWorkspaceLocation> workspaceLocations = this.workspaceManager.getWorkspaces().values();
        for (NutsWorkspaceLocation workspace : workspaceLocations) {
            if (!workspace.isEnabled()) {
                continue;
            }
            logger.info("Refreshing data for workspace " + workspace.getName() + " started!");

            logger.info("Refreshing repositories data started!");
            refreshRepositoriesData(workspace.getName());
            logger.info("Refreshing repositories data finished!");

            logger.info("Refreshing components data started!");
            refreshComponentsData(workspace.getName());
            logger.info("Refreshing components data finished!");

            logger.info("Refreshing data for workspace " + workspace.getName() + " finished!");
        }
    }

    private void refreshComponentsData(String workspace) {
        NutsWorkspace ws = NutsWorkspacePool.openWorkspace(workspace);
        Set<NutsId> oldData = this.dataService
                .getAllData(NutsIndexerUtils.getCacheDir(ws, "components"))
                .stream()
                .map((map)->NutsIndexerUtils.mapToNutsId(map,ws))
                .collect(Collectors.toSet());
        Iterator<NutsDefinition> definitions = ws.createQuery().setIgnoreNotFound(true).setIncludeInstallInformation(false).setIncludeFile(false).fetchIterator();
        List<Map<String, String>> dataToIndex = new ArrayList<>();
        while (definitions.hasNext()) {
            NutsDefinition definition=definitions.next();
            if (oldData.contains(definition.getId())) {
                oldData.remove(definition.getId());
                continue;
            }
            Map<String, String> entity = NutsIndexerUtils.nutsIdToMap(definition.getId());
            NutsDependency[] directDependencies = definition.getDescriptor().getDependencies();
            entity.put("dependencies",ws.getIOManager().toJsonString(Arrays.stream(directDependencies).map(Object::toString).collect(Collectors.toList()),true));
            List<NutsId> allDependencies = ws.createQuery().dependenciesOnly().addId(definition.getId()).setIgnoreNotFound(true).setIncludeInstallInformation(false).setIncludeFile(false).find();
            entity.put("allDependencies",ws.getIOManager().toJsonString(allDependencies.stream().map(Object::toString).collect(Collectors.toList()),true));
            dataToIndex.add(entity);
        }
        this.dataService.indexMultipleData(NutsIndexerUtils.getCacheDir(ws, "components"), dataToIndex);
        this.dataService.deleteMultipleData(NutsIndexerUtils.getCacheDir(ws, "components"),
                oldData.stream()
                        .map(NutsIndexerUtils::nutsIdToMap)
                        .collect(Collectors.toList()));
    }


    private void refreshRepositoriesData(String workspace) {
        NutsWorkspace ws = NutsWorkspacePool.openWorkspace(workspace);
        Set<NutsRepository> repositories = Arrays.stream(ws.getRepositoryManager().getRepositories())
                .collect(Collectors.toSet());
        Set<String> oldData = this.dataService
                .getAllData(NutsIndexerUtils.getCacheDir(ws, "repositories"))
                .stream()
                .map(map -> map.get("name").toString())
                .collect(Collectors.toSet());
        List<Map<String, String>> dataToIndex = new ArrayList<>();
        for (NutsRepository repository : repositories) {
            if (oldData.contains(repository.getName())) {
                oldData.remove(repository.getName());
                continue;
            }
            dataToIndex.add(NutsIndexerUtils.nutsRepositoryToMap(repository));
        }
        this.dataService.indexMultipleData(NutsIndexerUtils.getCacheDir(ws, "repositories"),
                dataToIndex);
        this.dataService.deleteMultipleData(NutsIndexerUtils.getCacheDir(ws, "repositories"),
                oldData.stream().map(name -> Collections.singletonMap("name", name)).collect(Collectors.toList()));
    }

}
