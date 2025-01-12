package net.thevpc.nuts.spi;

import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NPath;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

public interface NPaths extends NComponent {
    static NPaths of(NSession session) {
       return NExtensions.of(session).createComponent(NPaths.class).get();
    }

    /**
     * expand path to Workspace Location
     *
     * @param path path to expand
     * @return expanded path
     */
    NPath createPath(String path);

    NPath createPath(File path);

    NPath createPath(Path path);

    NPath createPath(URL path);

    NPath createPath(String path, ClassLoader classLoader);
    NPath createPath(NPathSPI path);

    NPaths addPathFactory(NPathFactory pathFactory);

    NPaths removePathFactory(NPathFactory pathFactory);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    NPath ofTempFile(String name);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    NPath ofTempFile();

    /**
     * create temp folder in the workspace's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    NPath ofTempFolder(String name);

    /**
     * create temp folder in the workspace's temp folder
     *
     * @return newly created temp folder
     */
    NPath ofTempFolder();

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    NPath ofTempRepositoryFile(String name, String repository);

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    NPath ofTempRepositoryFile(String repository);

    /**
     * create temp folder in the repository's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    NPath ofTempRepositoryFolder(String name, String repository);

    /**
     * create temp folder in the repository's temp folder
     *
     * @return newly created temp folder
     */
    NPath ofTempRepositoryFolder(String repository);
}
