package net.thevpc.nuts.runtime.standalone.repository.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NRepositorySessionAwareImpl;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class DefaultNRepositories implements NRepositories {

    private DefaultNRepositoryModel model;
    private NSession session;

    public DefaultNRepositories(NSession session) {
        this.session = session;
        NWorkspace w = this.session.getWorkspace();
        NWorkspaceExt e = (NWorkspaceExt) w;
        this.model = e.getModel().repositoryModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NRepositories setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    @Override
    public NRepositoryFilters filter() {
        return NRepositoryFilters.of(getSession());
    }

    private NRepository toSessionAwareRepo(NRepository x) {
        return NRepositorySessionAwareImpl.of(x, model.getWorkspace(), session);
    }

    @Override
    public List<NRepository> getRepositories() {
        return Arrays.stream(model.getRepositories(session)).map(x -> toSessionAwareRepo(x))
                .collect(Collectors.toList());
    }

    @Override
    public NRepository findRepositoryById(String repositoryNameOrId) {
        checkSession();
        return toSessionAwareRepo(model.findRepositoryById(repositoryNameOrId, session));
    }

    @Override
    public NRepository findRepositoryByName(String repositoryNameOrId) {
        checkSession();
        return toSessionAwareRepo(model.findRepositoryByName(repositoryNameOrId, session));
    }

    @Override
    public NRepository findRepository(String repositoryNameOrId) {
        checkSession();
        return toSessionAwareRepo(model.findRepository(repositoryNameOrId, session));
    }

    @Override
    public NRepository getRepository(String repositoryIdOrName) throws NRepositoryNotFoundException {
        checkSession();
        return toSessionAwareRepo(model.getRepository(repositoryIdOrName, session));
    }

    @Override
    public NRepositories removeRepository(String repositoryId) {
        checkSession();
        model.removeRepository(repositoryId, session);
        return this;
    }

    @Override
    public NRepositories removeAllRepositories() {
        checkSession();
        model.removeAllRepositories(session);
        return this;
    }

    @Override
    public NRepository addRepository(NAddRepositoryOptions options) {
        checkSession();
        NRepository r = model.addRepository(options, session);
        return r == null ? null : toSessionAwareRepo(r);
    }

    @Override
    public NRepository addRepository(String repositoryNamedUrl) {
        checkSession();
        NRepository r = model.addRepository(repositoryNamedUrl, session);
        return r == null ? null : toSessionAwareRepo(r);
    }

    private void checkSession() {
        NSessionUtils.checkSession(model.getWorkspace(), session);
    }

    public DefaultNRepositoryModel getModel() {
        return model;
    }

}
