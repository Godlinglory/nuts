package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsInstallStatus;

import java.util.Iterator;
import java.util.Set;

public interface NutsInstalledRepository extends NutsRepository {
    boolean isDefaultVersion(NutsId id, NutsSession session);

    Iterator<NutsInstallInformation> searchInstallInformation(NutsSession session);

    String getDefaultVersion(NutsId id, NutsSession session);

    void setDefaultVersion(NutsId id, NutsSession session);

    NutsInstallInformation getInstallInformation(NutsId id, NutsSession session);

    Set<NutsInstallStatus> getInstallStatus(NutsId id, NutsSession session);

    void install(NutsId id, NutsSession session, NutsId forId);

    NutsInstallInformation install(NutsDefinition id, NutsSession session);
    void uninstall(NutsId id, NutsSession session);

    NutsInstallInformation require(NutsDefinition id, boolean deploy,NutsId[] forId, NutsDependencyScope scope, NutsSession session);

    void unrequire(NutsId id, NutsId forId, NutsDependencyScope scope, NutsSession session);

}
