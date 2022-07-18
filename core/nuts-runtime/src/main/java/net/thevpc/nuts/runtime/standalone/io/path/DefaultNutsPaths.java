package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.io.path.spi.FilePath;
import net.thevpc.nuts.runtime.standalone.io.path.spi.URLPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.StringBuilder2;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceConfigModel;
import net.thevpc.nuts.spi.NutsPathSPI;
import net.thevpc.nuts.spi.NutsPaths;
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public class DefaultNutsPaths implements NutsPaths {
    private final NutsSession session;

    public DefaultNutsPaths(NutsSession session) {
        this.session = session;
    }

    @Override
    public NutsPath createPath(String path) {
        checkSession(session);
        return createPath(path, null);
    }

    @Override
    public NutsPath createPath(File path) {
        checkSession(session);
        if (path == null) {
            return null;
        }
        return createPath(new FilePath(path.toPath(), session));
    }

    @Override
    public NutsPath createPath(Path path) {
        checkSession(session);
        if (path == null) {
            return null;
        }
        return createPath(new FilePath(path, session));
    }

    @Override
    public NutsPath createPath(URL path) {
        checkSession(session);
        if (path == null) {
            return null;
        }
        return createPath(new URLPath(path, session));
    }

    @Override
    public NutsPath createPath(String path, ClassLoader classLoader) {
        checkSession(session);
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        NutsPath p = getModel().resolve(path, session, classLoader);
        if (p == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("unable to resolve path from %s", path));
        }
        return p;
    }

    @Override
    public NutsPath createPath(NutsPathSPI path) {
        checkSession(session);
        if (path == null) {
            return null;
        }
        return new NutsPathFromSPI(path);
    }

    @Override
    public NutsPaths addPathFactory(NutsPathFactory pathFactory) {
        getModel().addPathFactory(pathFactory);
        return this;
    }

    @Override
    public NutsPaths removePathFactory(NutsPathFactory pathFactory) {
        getModel().removePathFactory(pathFactory);
        return this;
    }

    private void checkSession(NutsSession session) {
        NutsSessionUtils.checkSession(this.session.getWorkspace(), session);
    }

    private DefaultNutsWorkspaceConfigModel getModel() {
        return ((DefaultNutsWorkspaceConfigManager) session.config()).getModel();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }


    public NutsPath createTempFile(String name) {
        return createAnyTempFile(name, false, null);
    }

    @Override
    public NutsPath createTempFolder(String name) {
        return createAnyTempFile(name, true, null);
    }

    @Override
    public NutsPath createTempFile() {
        return createAnyTempFile(null, false, null);
    }

    @Override
    public NutsPath createTempFolder() {
        return createAnyTempFile(null, true, null);
    }


    public NutsPath createRepositoryTempFile(String name, String repository) {
        return createAnyTempFile(name, false, repository);
    }

    @Override
    public NutsPath createRepositoryTempFolder(String name, String repository) {
        return createAnyTempFile(name, true, repository);
    }

    @Override
    public NutsPath createRepositoryTempFile(String repository) {
        return createAnyTempFile(null, false, repository);
    }

    @Override
    public NutsPath createRepositoryTempFolder(String repository) {
        return createAnyTempFile(null, true, repository);
    }


    public NutsPath createAnyTempFile(String name, boolean folder, String repositoryId) {
        NutsPath rootFolder = null;
        NutsRepository repositoryById = null;
        if (repositoryId == null) {
            rootFolder = session.locations().setSession(session).getStoreLocation(NutsStoreLocation.TEMP);
        } else {
            repositoryById = session.repos().setSession(session).getRepository(repositoryId);
            rootFolder = repositoryById.config().setSession(session).getStoreLocation(NutsStoreLocation.TEMP);
        }
        NutsId appId = session.getAppId();
        if (appId == null) {
            appId = session.getWorkspace().getRuntimeId();
        }
        if (appId != null) {
            rootFolder = rootFolder.resolve(NutsConstants.Folders.ID).resolve(session.locations().getDefaultIdBasedir(appId));
        }
        if (name == null) {
            name = "";
        }
        rootFolder.mkdirs();
        StringBuilder2 ext = new StringBuilder2(CoreIOUtils.getFileExtension(name, true, true));
        StringBuilder2 prefix = new StringBuilder2((ext.length() > 0) ? name.substring(0, name.length() - ext.length()) : name);
        if (ext.isEmpty() && prefix.isEmpty()) {
            prefix.append("nuts-");
            if (!folder) {
                ext.append(".tmp");
            }
        } else if (ext.isEmpty()) {
            if (!folder) {
                ext.append("-tmp");
            }
        } else if (prefix.isEmpty()) {
            prefix.append(ext);
            ext.clear();
            ext.append("-tmp");
        }
        if (!prefix.endsWith("-")) {
            prefix.append('-');
        }
        if (prefix.length() < 3) {
            if (prefix.length() < 3) {
                prefix.append('A');
                if (prefix.length() < 3) {
                    prefix.append('B');
                }
            }
        }

        if (folder) {
            for (int i = 0; i < 15; i++) {
                File temp = null;
                try {
                    temp = File.createTempFile(prefix.toString(), ext.toString(), rootFolder.toFile().toFile());
                    if (temp.delete() && temp.mkdir()) {
                        return NutsPath.of(temp.toPath(), session)
                                .setUserTemporary(true);
                    }
                } catch (IOException ex) {
                    //
                }
            }
            throw new NutsIOException(session, NutsMessage.ofCstyle("could not create temp directory: %s*%s", rootFolder + File.separator + prefix, ext));
        } else {
            try {
                return NutsPath.of(File.createTempFile(prefix.toString(), ext.toString(), rootFolder.toFile().toFile()).toPath(), session)
                        .setUserTemporary(true);
            } catch (IOException e) {
                throw new NutsIOException(session, e);
            }
        }
    }

}
