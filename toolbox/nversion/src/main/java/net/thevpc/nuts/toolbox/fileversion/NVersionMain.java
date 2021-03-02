package net.thevpc.nuts.toolbox.fileversion;

import net.thevpc.nuts.*;
import net.thevpc.common.io.FileUtils;
import net.thevpc.common.xfile.XFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NVersionMain extends NutsApplication {

    private final List<PathVersionResolver> resolvers = new ArrayList<>();

    public static void main(String[] args) {
        new NVersionMain().runAndExit(args);
    }

    public NVersionMain() {
        resolvers.add(new JarPathVersionResolver());
        resolvers.add(new MavenFolderPathVersionResolver());
        resolvers.add(new ExePathVersionResolver());
    }

    private Set<VersionDescriptor> detectVersions(String filePath, NutsApplicationContext context, NutsWorkspace ws) throws IOException {
        for (PathVersionResolver r : resolvers) {
            Set<VersionDescriptor> x = r.resolve(filePath, context);
            if (x != null) {
                return x;
            }
        }
        try {
            Path p = Paths.get(filePath);
            if (!Files.exists(p)) {
                throw new NutsExecutionException(context.getWorkspace(), "nversion: file does not exist: " + filePath, 2);
            }
            if (!Files.isDirectory(p)) {
                throw new NutsExecutionException(context.getWorkspace(), "nversion: unsupported directory: " + filePath, 2);
            }
            if (!Files.isDirectory(p)) {
                throw new NutsExecutionException(context.getWorkspace(), "nversion: unsupported file: " + filePath, 2);
            }
        } catch (NutsExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            //
        }
        throw new NutsExecutionException(context.getWorkspace(), "nversion: unsupported path: " + filePath, 2);
    }

    @Override
    public void run(NutsApplicationContext context) {
        NutsWorkspace ws = context.getWorkspace();
        Set<String> unsupportedFileTypes = new HashSet<>();
        Set<String> jarFiles = new HashSet<>();
        Set<String> exeFiles = new HashSet<>();
        Map<String, Set<VersionDescriptor>> results = new HashMap<>();
        boolean maven = false;
        boolean winPE = false;
        boolean all = false;
        boolean longFormat = false;
        boolean nameFormat = false;
        boolean idFormat = false;
        boolean sort = false;
        boolean table = false;
        boolean error = false;
        NutsCommandLine commandLine = context.getCommandLine();
        NutsArgument a;
        int processed = 0;
        while (commandLine.hasNext()) {
            if ((a = commandLine.nextBoolean("--maven")) != null) {
                maven = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--win-pe")) != null) {
                winPE = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--exe")) != null) {
                winPE = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--dll")) != null) {
                winPE = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--long")) != null) {
                longFormat = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--name")) != null) {
                nameFormat = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--sort")) != null) {
                sort = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--id")) != null) {
                idFormat = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--all")) != null) {
                all = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--table")) != null) {
                table = a.getBooleanValue();
            } else if ((a = commandLine.nextBoolean("--error")) != null) {
                error = a.getBooleanValue();
            } else if (commandLine.peek().isNonOption()) {
                a = commandLine.next();
                jarFiles.add(a.getString());
            } else {
                context.configureLast(commandLine);
            }
        }
        if (commandLine.isExecMode()) {

            for (String arg : jarFiles) {
                Set<VersionDescriptor> value = null;
                try {
                    processed++;
                    value = detectVersions(context.getWorkspace().io().expandPath(arg), context, ws);
                } catch (IOException e) {
                    throw new NutsExecutionException(context.getWorkspace(), e, 2);
                }
                if (!value.isEmpty()) {
                    results.put(arg, value);
                }
            }
            if (processed == 0) {
                throw new NutsExecutionException(context.getWorkspace(), "nversion: Missing file", 2);
            }
            if (table && all) {
                throw new NutsExecutionException(context.getWorkspace(), "nversion: Options conflict --table --all", 1);
            }
            if (table && longFormat) {
                throw new NutsExecutionException(context.getWorkspace(), "nversion: Options conflict --table --long", 1);
            }

            PrintStream out = context.getSession().out();
            PrintStream err = context.getSession().out();
            NutsFormatManager text = context.getWorkspace().formats();
            NutsTextManager tfactory = text.text();
            if (table) {
                NutsPropertiesFormat tt = context.getWorkspace().formats().props().setSort(sort);
                Properties pp = new Properties();
                for (Map.Entry<String, Set<VersionDescriptor>> entry : results.entrySet()) {
                    VersionDescriptor o = entry.getValue().toArray(new VersionDescriptor[0])[0];
                    if (nameFormat) {
                        pp.setProperty(entry.getKey(), o.getId().getShortName());
                    } else if (idFormat) {
                        pp.setProperty(entry.getKey(), o.getId().toString());
                    } else if (longFormat) {
                        //should never happen
                    } else {
                        pp.setProperty(entry.getKey(), o.getId().toString());
                    }
                }
                if (error) {
                    for (String t : unsupportedFileTypes) {
                        File f = new File(context.getWorkspace().io().expandPath(t));
                        if (f.isFile()) {
                            pp.setProperty(t, text.text().builder().append("<<ERROR>>", NutsTextNodeStyle.error()).append(" unsupported file type").toString());
                        } else if (f.isDirectory()) {
                            pp.setProperty(t, text.text().builder().append("<<ERROR>>", NutsTextNodeStyle.error()).append(" ignored folder").toString()
                            );
                        } else {
                            pp.setProperty(t, text.text().builder().append("<<ERROR>>", NutsTextNodeStyle.error()).append(" file not found").toString()
                            );
                        }
                    }
                }
                tt.setValue(pp).print(out);
            } else {
                Set<String> keys = sort ? new TreeSet<>(results.keySet()) : new LinkedHashSet<>(results.keySet());
                for (String k : keys) {
                    if (results.size() > 1) {
                        if (longFormat || all) {
                            out.printf("%s:%n", tfactory.styled(k, NutsTextNodeStyle.primary(3)));
                        } else {
                            out.printf("%s: ", tfactory.styled(k, NutsTextNodeStyle.primary(3)));
                        }
                    }
                    Set<VersionDescriptor> v = results.get(k);
                    for (VersionDescriptor descriptor : v) {
                        if (nameFormat) {
                            out.printf("%s%n", tfactory.styled(descriptor.getId().getShortName(), NutsTextNodeStyle.primary(4)));
                        } else if (idFormat) {
                            out.printf("%s%n", tfactory.nodeFor(descriptor.getId()));
                        } else if (longFormat) {
                            out.printf("%s%n", tfactory.nodeFor(descriptor.getId()));
                            NutsPropertiesFormat f = context.getWorkspace().formats().props()
                                    .setSort(true);
                            f.setValue(descriptor.getProperties()).print(out);
                        } else {
                            out.printf("%s%n", tfactory.nodeFor(descriptor.getId().getVersion()));
                        }
                        if (!all) {
                            break;
                        }
                    }
                }
                if (error) {
                    if (!unsupportedFileTypes.isEmpty()) {
                        for (String t : unsupportedFileTypes) {
                            File f = new File(context.getWorkspace().io().expandPath(t));
                            if (f.isFile()) {
                                err.printf("%s : unsupported file type%n", t);
                            } else if (f.isDirectory()) {
                                err.printf("%s : ignored folder%n", t);
                            } else {
                                err.printf("%s : file not found%n", t);
                            }
                        }
                    }
                }
            }
            if (!unsupportedFileTypes.isEmpty()) {
                throw new NutsExecutionException(context.getWorkspace(), "nversion: unsupported file types " + unsupportedFileTypes, 3);
            }
        }
    }

    public static XFile xfileOf(String expression, String cwd) {
        if (expression.startsWith("file:") || expression.contains("://")) {
            return XFile.of(expression);
        }
        return XFile.of(FileUtils.getAbsoluteFile2(expression, cwd));
    }
}
