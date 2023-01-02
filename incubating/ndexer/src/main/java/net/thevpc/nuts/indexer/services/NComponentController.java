package net.thevpc.nuts.indexer.services;

import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.indexer.*;
import net.thevpc.nuts.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

@RestController
@RequestMapping("indexer/" + NConstants.Folders.ID)
public class NComponentController {

    @Autowired
    private DataService dataService;
    private static final Logger LOG = LoggerFactory.getLogger(NComponentController.class);

    @Autowired
    private NWorkspaceListManagerPool listManagerPool;
    @Autowired
    private NIndexSubscriberListManagerPool indexSubscriberListManagerPool;
    @Autowired
    private NWorkspacePool workspacePool;
    private NWorkspaceListManager workspaceManager;
    private NIndexSubscriberListManager subscriberManager;

    @PostConstruct
    private void init() {
        workspaceManager = listManagerPool.openListManager("default");
        subscriberManager = indexSubscriberListManagerPool.openSubscriberListManager("default");
    }


    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> getAll(@RequestParam("repositoryUuid") String repositoryUuid) {
        NIndexSubscriber subscriber = subscriberManager.getSubscriber(repositoryUuid);
        if (subscriber != null) {
            LOG.info("Getting all components data for subscriber " + subscriber.cacheFolderName());
            Iterator<NWorkspaceLocation> iterator = subscriber.getWorkspaceLocations().values().iterator();
            if (iterator.hasNext()) {
                NWorkspaceLocation workspaceLocation = iterator.next();
                NSession session = Nuts.openWorkspace("--workspace", workspaceLocation.getLocation());
                List<Map<String, String>> rows = this.dataService.
                        getAllData(NIndexerUtils.getCacheDir(session, subscriber.cacheFolderName()));
                List<Map<String, Object>> resData = cleanNutsIdMap(session, rows);
                return ResponseEntity.ok(resData);
            }
        }
        LOG.error("Error in getting all components data for subscriber " + repositoryUuid);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping(value = "dependencies", produces = "application/json")
    public ResponseEntity<List<Map<String, String>>> getDependencies(@RequestParam("repositoryUuid") String repositoryUuid,
                                                                     @RequestParam("name") String name,
                                                                     @RequestParam("namespace") String namespace,
                                                                     @RequestParam("group") String group,
                                                                     @RequestParam("version") String version,
                                                                     @RequestParam("os") String os,
                                                                     @RequestParam("osdist") String osdist,
                                                                     @RequestParam("arch") String arch,
                                                                     @RequestParam("face") String face,
//                                                                     @RequestParam(NutsConstants.IdProperties.ALTERNATIVE) String alternative,
                                                                     @RequestParam("all") Boolean all) {
        NIndexSubscriber subscriber = subscriberManager.getSubscriber(repositoryUuid);
        if (subscriber != null) {
            LOG.info("Getting dependencies of component " + name + " data for subscriber " + subscriber.cacheFolderName());
            Iterator<NWorkspaceLocation> iterator = subscriber.getWorkspaceLocations().values().iterator();
            if (iterator.hasNext()) {
                NWorkspaceLocation workspaceLocation = iterator.next();
                NSession session = Nuts.openWorkspace("--workspace", workspaceLocation.getLocation());
                NId id = NIdBuilder.of(group,name)
                        .setRepository(namespace)
                        .setVersion(version)
                        .setCondition(
                                new DefaultNEnvConditionBuilder()
                                        .setArch(Arrays.asList(arch))
                                        .setOs(Arrays.asList(os))
                                        .setOsDist(Arrays.asList(osdist)).build()
                        )
                        .setFace(face)
//                        .setAlternative(alternative)
                        .build();
                List<Map<String, String>> result;
                if (all) {
                    result = this.dataService.getAllDependencies(session, NIndexerUtils.getCacheDir(session, subscriber.cacheFolderName()), id);
                } else {
                    result = this.dataService.getDependencies(session, NIndexerUtils.getCacheDir(session, subscriber.cacheFolderName()), id);
                }
                return ResponseEntity.ok(result);
            }
        }
        LOG.error("Error in getting dependencies of component " + name + " data for subscriber " + repositoryUuid);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping(value = "allVersions", produces = "application/json")
    public ResponseEntity<List<Map<String, Object>>> getAllVersions(@RequestParam("repositoryUuid") String repositoryUuid,
                                                                    @RequestParam("name") String name,
                                                                    @RequestParam("namespace") String namespace,
                                                                    @RequestParam("group") String group,
                                                                    @RequestParam("os") String os,
                                                                    @RequestParam("osdist") String osdist,
                                                                    @RequestParam("arch") String arch,
                                                                    @RequestParam("platform") String platform,
                                                                    @RequestParam("desktop") String desktopEnvironment,
                                                                    @RequestParam("face") String face
//            ,@RequestParam(NutsConstants.IdProperties.ALTERNATIVE) String alternative
    ) {
        NIndexSubscriber subscriber = subscriberManager.getSubscriber(repositoryUuid);
        if (subscriber != null) {
            LOG.info("Getting all versions of component " + name + " data for subscriber " + subscriber.cacheFolderName());
            Iterator<NWorkspaceLocation> iterator = subscriber.getWorkspaceLocations().values().iterator();
            if (iterator.hasNext()) {
                NWorkspaceLocation workspaceLocation = iterator.next();
                NSession session = Nuts.openWorkspace("--workspace", workspaceLocation.getLocation());
                NId id = NIdBuilder.of(group,name)
                        .setRepository(namespace)
                        .setCondition(new DefaultNEnvConditionBuilder()
                                .setArch(Arrays.asList(arch))
                                .setOs(Arrays.asList(os))
                                .setOsDist(Arrays.asList(osdist))
                                .setPlatform(Arrays.asList(platform))
                                .setDesktopEnvironment(Arrays.asList(desktopEnvironment))
                        )
                        .setFace(face)
//                        .setAlternative(alternative)
                        .build();
                List<Map<String, String>> rows = this.dataService.getAllVersions(session, NIndexerUtils.getCacheDir(session, subscriber.cacheFolderName()), id);
                List<Map<String, Object>> resData = cleanNutsIdMap(session, rows);
                return ResponseEntity.ok(resData);
            }
        }
        LOG.error("Error in getting all versions of component " + name + " data for subscriber " + repositoryUuid);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping(value = "delete", produces = "application/json")
    public ResponseEntity<Boolean> deleteComponent(@RequestParam("repositoryUuid") String repositoryUuid,
                                                   @RequestParam("name") String name,
                                                   @RequestParam("namespace") String namespace,
                                                   @RequestParam("group") String group,
                                                   @RequestParam("version") String version,
                                                   @RequestParam("os") String os,
                                                   @RequestParam("osdist") String osdist,
                                                   @RequestParam("platform") String platform,
                                                   @RequestParam("desktop") String desktopEnvironment,
                                                   @RequestParam("arch") String arch,
                                                   @RequestParam("face") String face
//            ,@RequestParam(NutsConstants.IdProperties.ALTERNATIVE) String alternative
    ) {
        NIndexSubscriber subscriber = subscriberManager.getSubscriber(repositoryUuid);
        if (subscriber != null) {
            LOG.info("Deleting the component " + name + " data for subscriber " + subscriber.cacheFolderName());
            Iterator<NWorkspaceLocation> iterator = subscriber.getWorkspaceLocations().values().iterator();
            if (iterator.hasNext()) {
                NWorkspaceLocation workspaceLocation = iterator.next();
                NSession session = Nuts.openWorkspace("--workspace", workspaceLocation.getLocation());
                NWorkspace ws = session.getWorkspace();
                Map<String, String> data = NIndexerUtils.nutsIdToMap(
                        NIdBuilder.of()
                                .setArtifactId(name)
                                .setRepository(namespace)
                                .setGroupId(group)
                                .setVersion(version)
                                .setCondition(new DefaultNEnvConditionBuilder()
                                        .setArch(Arrays.asList(arch))
                                        .setOs(Arrays.asList(os))
                                        .setOsDist(Arrays.asList(osdist))
                                        .setPlatform(Arrays.asList(platform))
                                        .setDesktopEnvironment(Arrays.asList(desktopEnvironment))
                                )
                                .setFace(face)
//                                .setAlternative(alternative)
                                .build());
                this.dataService.deleteData(NIndexerUtils.getCacheDir(session, subscriber.cacheFolderName()), data);
                return ResponseEntity.ok(true);
            }
        }
        LOG.error("Error in deleting the component " + name + " data for subscriber " + repositoryUuid);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping(value = "addData", produces = "application/json")
    public ResponseEntity<Boolean> addDocument(@RequestParam("repositoryUuid") String repositoryUuid,
                                               @RequestParam("name") String name,
                                               @RequestParam("namespace") String namespace,
                                               @RequestParam("group") String group,
                                               @RequestParam("version") String version,
                                               @RequestParam("os") String os,
                                               @RequestParam("osdist") String osdist,
                                               @RequestParam("platform") String platform,
                                               @RequestParam("desktop") String desktopEnvironment,
                                               @RequestParam("arch") String arch,
                                               @RequestParam("face") String face
//            ,@RequestParam(NutsConstants.IdProperties.ALTERNATIVE) String alternative
    ) {
        NIndexSubscriber subscriber = subscriberManager.getSubscriber(repositoryUuid);
        if (subscriber != null) {
            LOG.info("Getting all versions of component " + name + " data for subscriber " + subscriber.cacheFolderName());
            Iterator<NWorkspaceLocation> iterator = subscriber.getWorkspaceLocations().values().iterator();
            if (iterator.hasNext()) {
                NWorkspaceLocation workspaceLocation = iterator.next();
                NSession session = Nuts.openWorkspace("--workspace", workspaceLocation.getLocation());
                NId id = NIdBuilder.of()
                        .setArtifactId(name)
                        .setRepository(namespace)
                        .setGroupId(group)
                        .setVersion(version)
                        .setCondition(new DefaultNEnvConditionBuilder()
                                .setArch(Arrays.asList(arch))
                                .setOs(Arrays.asList(os))
                                .setOsDist(Arrays.asList(osdist))
                                .setPlatform(Arrays.asList(platform))
                                .setDesktopEnvironment(Arrays.asList(desktopEnvironment))
                        )
                        .setFace(face)
//                        .setAlternative(alternative)
                        .build();
                Map<String, String> data = NIndexerUtils.nutsIdToMap(id);
                List<Map<String, String>> list = this.dataService.searchData(NIndexerUtils.getCacheDir(session, subscriber.cacheFolderName()), data, null);
                if (list.isEmpty()) {
                    Iterator<NDefinition> it = NSearchCommand.of(session)
                            .setRepositoryFilter(
                                    NRepositories.of(session).filter().byUuid(subscriber.getUuid())
                            )
                            .addId(id)
                            .setFailFast(false)
                            .setContent(false)
                            .setEffective(true)
                            .getResultDefinitions().iterator();
                    if (it.hasNext()) {
                        NDefinition definition = it.next();
                        List<NDependency> directDependencies = definition.getEffectiveDescriptor().get(session).getDependencies();
                        data.put("dependencies", NElements.of(session).json()
                                .setValue(directDependencies.stream().map(Object::toString)
                                        .collect(Collectors.toList()))
                                .setNtf(false)
                                .format()
                                .toString()
                        );

                        this.dataService.indexData(NIndexerUtils.getCacheDir(session, subscriber.cacheFolderName()), data);
                    } else {
                        ResponseEntity.ok(false);
                    }
                }
                return ResponseEntity.ok(true);
            }
        }
        LOG.error("Error in deleting the component " + name + " data for subscriber " + repositoryUuid);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private List<Map<String, Object>> cleanNutsIdMap(NSession session, List<Map<String, String>> rows) {
        List<Map<String, Object>> resData = new ArrayList<>();
        for (Map<String, String> row : rows) {
            Map<String, Object> d = new HashMap<>(row);
            if (d.containsKey("dependencies")) {
                String[] array = NElements.of(session).json().parse(new StringReader(row.get("dependencies")), String[].class);
                List<Map<String, String>> dependencies = new ArrayList<>();
                for (String s : array) {
                    dependencies.add(NIndexerUtils.nutsIdToMap(NId.of(s).get(session)));
                }
                d.put("dependencies", dependencies);
            }
            if (d.containsKey("allDependencies")) {
                String[] array = NElements.of(session).json().parse(new StringReader(row.get("allDependencies")), String[].class);
                List<Map<String, String>> allDependencies = new ArrayList<>();
                for (String s : array) {
                    allDependencies.add(NIndexerUtils.nutsIdToMap(NId.of(s).get(session)));
                }
                d.put("allDependencies", allDependencies);
            }
            resData.add(d);
        }
        return resData;
    }
}
