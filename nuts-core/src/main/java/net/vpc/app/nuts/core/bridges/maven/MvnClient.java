package net.vpc.app.nuts.core.bridges.maven;

import net.vpc.app.nuts.*;

public class MvnClient {

    public static final String NET_VPC_APP_NUTS_MVN = "net.vpc.app.nuts.toolbox:mvn";
    private NutsWorkspace ws;
    private Status status = Status.INIT;

    public enum Status {
        INIT,
        DIRTY,
        SUCCESS,
        FAIL,
    }

    public MvnClient(NutsWorkspace ws) {
        this.ws = ws;
    }

    public boolean get(NutsId id, String repoURL, NutsSession session) {
        if (id.getSimpleName().equals(NET_VPC_APP_NUTS_MVN)) {
            return false;
        }
        NutsSession searchSession = session.copy().trace(false);
        switch (status) {
            case INIT: {
                status = Status.DIRTY;
                try {
                    NutsDefinition ff = ws.search()
                            .id(NET_VPC_APP_NUTS_MVN).setSession(searchSession)
                            .online()
                            .setOptional(false)
                            .inlineDependencies().latest().getResultDefinitions().required();
                    for (NutsId nutsId : ws.search().id(ff.getId()).inlineDependencies().getResultIds()) {
                        ws.fetch().id(nutsId).setSession(searchSession)
                                .online()
                                .setOptional(false)
                                .dependencies().getResultDefinition();
                    }
                    status = Status.SUCCESS;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    status = Status.FAIL;
                    return false;
                }
                break;
            }
            case FAIL: {
                return false;
            }
            case SUCCESS: {
                //OK
                break;
            }
            case DIRTY: {
                return false;
            }
        }
        try {
            NutsExecCommand b = ws
                    .exec()
                    .failFast()
                    .addCommand(
                            NET_VPC_APP_NUTS_MVN,
                            "--json",
                            "get",
                            id.toString(),
                            repoURL == null ? "" : repoURL
                    ).setSession(session).run();
            return (b.getResult() == 0);
        } catch (Exception ex) {
            return false;
        }
    }
}
