package net.thevpc.nuts.indexer.services;

import net.thevpc.nuts.indexer.*;
import net.thevpc.nuts.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

@Service
public class RefreshDataService {

    @Autowired
    private DataService dataService;

    private static final Logger logger = LoggerFactory.getLogger(RefreshDataService.class);

    @Autowired
    private NutsIndexSubscriberListManagerPool indexSubscriberListManagerPool;
    @Autowired
    private NutsWorkspacePool workspacePool;
    private NutsIndexSubscriberListManager subscriberManager;

    @PostConstruct
    private void init() {
        subscriberManager = indexSubscriberListManagerPool.openSubscriberListManager("default");
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void refreshData() {
        List<NutsIndexSubscriber> subscribers = subscriberManager.getSubscribers();
        for (NutsIndexSubscriber subscriber : subscribers) {
            logger.info("Refreshing data for subscriber " + subscriber.cacheFolderName() + " started!");

            logger.info("Refreshing components data for subscriber " + subscriber.cacheFolderName() + " started!");
            refreshSubscriberData(subscriber);
            logger.info("Refreshing components data for subscriber " + subscriber.cacheFolderName() + " finished!");

            logger.info("Refreshing data for subscriber " + subscriber.cacheFolderName() + " finished!");
        }
    }

    private void refreshSubscriberData(NutsIndexSubscriber subscriber) {
        Iterator<NutsWorkspaceLocation> iterator = subscriber.getWorkspaceLocations().values().iterator();
        if (iterator.hasNext()) {
            NutsWorkspaceLocation workspaceLocation = iterator.next();
            NutsWorkspace ws = workspacePool.openWorkspace(workspaceLocation.getLocation());
            NutsSession session = ws.createSession();
            Map<String, NutsId> oldData = this.dataService
                    .getAllData(NutsIndexerUtils.getCacheDir(session, subscriber.cacheFolderName()))
                    .stream()
                    .collect(Collectors.toMap(map -> map.get("stringId"), map -> NutsIndexerUtils.mapToNutsId(map, ws), (v1, v2) -> v1));
            Iterator<NutsDefinition> definitions = ws.search()
                    .setRepositoryFilter(ws.repos().filter().byUuid(subscriber.getUuid()))
                    .setFailFast(false)
                    .setContent(false)
                    .setEffective(true)
                    .getResultDefinitions().iterator();
            List<Map<String, String>> dataToIndex = new ArrayList<>();
            Map<String, Boolean> visited = new HashMap<>();
            while (definitions.hasNext()) {
                NutsDefinition definition = definitions.next();
                Map<String, String> id = NutsIndexerUtils.nutsIdToMap(definition.getId());
                if (oldData.containsKey(id.get("stringId"))) {
                    visited.put(id.get("stringId"), true);
                    oldData.remove(id.get("stringId"));
                    continue;
                }

                if (visited.getOrDefault(id.get("stringId"), false)) {
                    continue;
                }
                visited.put(id.get("stringId"), true);

                NutsDependency[] directDependencies = definition.getEffectiveDescriptor().getDependencies();
                id.put("dependencies",
                        ws.elem().setContentType(NutsContentType.JSON).setValue(Arrays.stream(directDependencies).map(Object::toString).collect(Collectors.toList()))
                                .setNtf(false).format().filteredText()
                );
                dataToIndex.add(id);
            }
            this.dataService.indexMultipleData(NutsIndexerUtils.getCacheDir(session, subscriber.cacheFolderName()), dataToIndex);
            this.dataService.deleteMultipleData(NutsIndexerUtils.getCacheDir(session, subscriber.cacheFolderName()),
                    oldData.values().stream()
                            .map(NutsIndexerUtils::nutsIdToMap)
                            .collect(Collectors.toList()));
        }
    }



}
