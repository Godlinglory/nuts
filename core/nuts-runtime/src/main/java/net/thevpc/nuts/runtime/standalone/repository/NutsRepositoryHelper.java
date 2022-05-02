package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NutsPositionType;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsRepositoryExt;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NutsInstalledRepository;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.util.NutsLoggerVerb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.thevpc.nuts.runtime.standalone.repository.cmd.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.util.NutsSpeedQualifiers;
import net.thevpc.nuts.util.NutsUtilStrings;

public class NutsRepositoryHelper {
    private NutsLogger LOG;

    private final NutsRepository repo;

    private NutsRepositoryHelper(NutsRepository repo) {
        this.repo = repo;
//        LOG=repo.getWorkspace().log().of(.class);
    }

    public static NutsRepositoryHelper of(NutsRepository repo) {
        Map<String, Object> up = repo.getUserProperties();
        NutsRepositoryHelper wp = (NutsRepositoryHelper) up.get(NutsRepositoryHelper.class.getName());
        if (wp == null) {
            wp = new NutsRepositoryHelper(repo);
            up.put(NutsRepositoryHelper.class.getName(), wp);
        }
        return wp;
    }

    public static NutsSpeedQualifier getSupportSpeedLevel(NutsRepository repository, NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode mode, boolean transitive, NutsSession session) {
        if (repository instanceof NutsInstalledRepository) {
            return NutsSpeedQualifier.UNAVAILABLE;
        }
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(repository);
        List<NutsSpeedQualifier> speeds = new ArrayList<>();
        if (xrepo.acceptAction(id, supportedAction, mode, session)) {
            NutsSpeedQualifier r = repository.config().getSpeed();
            if (r != NutsSpeedQualifier.UNAVAILABLE) {
                speeds.add(r);
            }
        }
        if (transitive) {
            for (NutsRepository remote : repository.config()
                    .setSession(session)
                    .getMirrors()) {
                NutsSpeedQualifier r = getSupportSpeedLevel(remote, supportedAction, id, mode, transitive, session);
                if (r != NutsSpeedQualifier.UNAVAILABLE) {
                    speeds.add(r);
                }
            }
        }
        if (speeds.size() == 0) {
            return NutsSpeedQualifier.UNAVAILABLE;
        }
        return NutsSpeedQualifiers.max(speeds.toArray(new NutsSpeedQualifier[0]));
    }

    public static int getSupportDeployLevel(NutsRepository repository, NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode mode, boolean transitive, NutsSession session) {
        if (repository instanceof NutsInstalledRepository) {
            return 0;
        }
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(repository);
        int result = 0;
        if (xrepo.acceptAction(id, supportedAction, mode, session)) {
            int r = repository.config().getDeployWeight();
            if (r > 0 && r > result) {
                result = r;
            }
        }
        if (transitive) {
            for (NutsRepository remote : repository.config()
                    .setSession(session)
                    .getMirrors()) {
                int r = getSupportDeployLevel(remote, supportedAction, id, mode, transitive, session);
                if (r > 0 && r > result) {
                    result = r;
                }
            }
        }
        return result;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(NutsRepositoryHelper.class,session);
        }
        return LOG;
    }

    public Events events() {
        return new Events(this);
    }

    public static class Events {

        private final NutsRepositoryHelper u;

        public Events(NutsRepositoryHelper u) {
            this.u = u;
        }

        public void fireOnUndeploy(NutsContentEvent evt) {
            for (NutsRepositoryListener listener : u.repo.getRepositoryListeners()) {
                listener.onUndeploy(evt);
            }
            for (NutsRepositoryListener listener : evt.getSession().events().getRepositoryListeners()) {
                listener.onUndeploy(evt);
            }
        }

        public void fireOnDeploy(NutsContentEvent event) {
            for (NutsRepositoryListener listener : u.repo.getRepositoryListeners()) {
                listener.onDeploy(event);
            }
            for (NutsRepositoryListener listener : event.getSession().events().getRepositoryListeners()) {
                listener.onDeploy(event);
            }
        }

        public void fireOnPush(NutsContentEvent event) {
            for (NutsRepositoryListener listener : u.repo.getRepositoryListeners()) {
                listener.onPush(event);
            }
            for (NutsRepositoryListener listener : event.getSession().events().getRepositoryListeners()) {
                listener.onPush(event);
            }
            for (NutsRepositoryListener listener : event.getSession().getListeners(NutsRepositoryListener.class)) {
                listener.onPush(event);
            }
        }

        public void fireOnAddRepository(NutsRepositoryEvent event) {
            if (u._LOG(event.getSession()).isLoggable(Level.FINEST)) {
                u._LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLoggerVerb.ADD)
                        .log(NutsMessage.jstyle("{0} add    repo {1}", NutsUtilStrings.formatAlign(u.repo.getName(), 20, NutsPositionType.FIRST), event
                                .getRepository().getName())
                        );
            }
            for (NutsRepositoryListener listener : u.repo.getRepositoryListeners()) {
                listener.onAddRepository(event);
            }
            for (NutsRepositoryListener listener : event.getSession().events().getRepositoryListeners()) {
                listener.onAddRepository(event);
            }
            for (NutsRepositoryListener listener : event.getSession().getListeners(NutsRepositoryListener.class)) {
                listener.onAddRepository(event);
            }
        }

        public void fireOnRemoveRepository(NutsRepositoryEvent event) {
            if (u._LOG(event.getSession()).isLoggable(Level.FINEST)) {
                u._LOGOP(event.getSession()).level(Level.FINEST).verb(NutsLoggerVerb.REMOVE).log(
                        NutsMessage.jstyle("{0} remove repo {1}", NutsUtilStrings.formatAlign(u.repo.getName(), 20,NutsPositionType.FIRST), event
                                .getRepository().getName()));
            }
            for (NutsRepositoryListener listener : u.repo.getRepositoryListeners()) {
//            if (event == null) {
//                event = new DefaultNutsRepositoryEvent(getWorkspace(), this, event, "mirror", event, null);
//            }
                listener.onRemoveRepository(event);
            }
            for (NutsRepositoryListener listener : event.getSession().events().getRepositoryListeners()) {
//            if (event == null) {
//                event = new DefaultNutsRepositoryEvent(getWorkspace(), this, event, "mirror", event, null);
//            }
                listener.onRemoveRepository(event);
            }
            for (NutsRepositoryListener listener : event.getSession().getListeners(NutsRepositoryListener.class)) {
                listener.onRemoveRepository(event);
            }
        }

    }
}
