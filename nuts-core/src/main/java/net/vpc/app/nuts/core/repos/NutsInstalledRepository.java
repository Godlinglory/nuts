package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;

import java.util.Iterator;

public interface NutsInstalledRepository {
    boolean isDefaultVersion(NutsId id);

    String getDefaultVersion(NutsId id);

    void setDefaultVersion(NutsId id, NutsSession session);

    NutsInstallInformation getInstallInfo(NutsId id);

    boolean isInstalled(NutsId id);

    Iterator<NutsId> findAll(NutsIdFilter all, NutsSession session);

    Iterator<NutsId> findVersions(NutsId id, NutsIdFilter filter, NutsSession session);

    NutsId[] findInstalledVersions(NutsId id, NutsSession session);

    void uninstall(NutsId id, NutsSession session);

    NutsId[] findDeployedVersions(NutsId id, NutsSession session);

    NutsInstallInformation install(NutsDefinition id, NutsSession session);
}
