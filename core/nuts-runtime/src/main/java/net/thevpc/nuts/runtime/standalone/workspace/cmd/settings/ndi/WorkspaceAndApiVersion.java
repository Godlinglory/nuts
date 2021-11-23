package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi;

import net.thevpc.nuts.NutsVersion;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WorkspaceAndApiVersion {
    private String workspace;
    private NutsVersion apiVersion;
    private List<PathInfo> updatedPaths;

    public WorkspaceAndApiVersion(String workspace, NutsVersion apiVersion, PathInfo[] updatedPaths) {
        this.workspace = workspace;
        this.apiVersion = apiVersion;
        this.updatedPaths = new ArrayList<>(Arrays.asList(updatedPaths));
    }

    public String getWorkspace() {
        return workspace;
    }

    public NutsVersion getApiVersion() {
        return apiVersion;
    }

    public List<PathInfo> getUpdatedPaths() {
        return updatedPaths;
    }

    public Set<String> getUpdatedPathStrings() {
        return updatedPaths.stream().filter(x->x.getStatus()!= PathInfo.Status.DISCARDED).map(x->x.getPath().toString()).collect(Collectors.toSet());
    }
}