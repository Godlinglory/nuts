package net.thevpc.nuts;

import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;

public interface NutsPath extends NutsFormattable {
    String name();

    String asString();

    String location();

    NutsPath compressedForm();

    URL toURL();

    boolean isURL();

    boolean isFilePath();

    Path toFilePath();


    String toString();

    URL asURL();

    Path asFilePath();

    NutsInput input();

    NutsOutput output();

    NutsSession getSession();

    void delete(boolean recurse);

    void mkdir(boolean parents);

    boolean exists();

    long length();

    Instant lastModifiedInstant();
}
