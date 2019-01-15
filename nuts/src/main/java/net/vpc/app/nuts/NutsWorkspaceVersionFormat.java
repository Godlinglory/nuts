package net.vpc.app.nuts;

import java.io.PrintStream;
import java.util.Properties;

public interface NutsWorkspaceVersionFormat {
    NutsWorkspaceVersionFormat addProperty(String key, String value);

    NutsWorkspaceVersionFormat addProperties(Properties p);

    NutsWorkspaceVersionFormat addOption(String o);

    NutsWorkspaceVersionFormat addOptions(String... o);

    String format();
    void format(PrintStream out);
}
