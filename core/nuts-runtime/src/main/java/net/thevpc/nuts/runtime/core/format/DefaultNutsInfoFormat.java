package net.thevpc.nuts.runtime.core.format;

import net.thevpc.nuts.*;

import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsJavaSdkUtils;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;
import net.thevpc.nuts.runtime.core.util.CoreTimeUtils;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNutsInfoFormat extends DefaultFormatBase<NutsInfoFormat> implements NutsInfoFormat {

    private final Map<String, String> extraProperties = new LinkedHashMap<>();
    private boolean showRepositories = false;
    private boolean fancy = false;
    private List<String> requests = new ArrayList<>();
    private Predicate<String> filter = NutsPredicates.always();
    private boolean lenient = false;

    public DefaultNutsInfoFormat(NutsWorkspace ws) {
        super(ws, "info");
    }

    @Override
    public NutsInfoFormat setShowRepositories(boolean enable) {
        this.showRepositories = true;
        return this;
    }

    @Override
    public boolean isShowRepositories() {
        return showRepositories;
    }

    @Override
    public NutsInfoFormat addProperty(String key, String value) {
        if (value == null) {
            extraProperties.remove(key);
        } else {
            extraProperties.put(key, value);
        }
        return this;
    }

    @Override
    public NutsInfoFormat addProperties(Map<String, String> customProperties) {
        if (customProperties != null) {
            for (Map.Entry<String, String> e : customProperties.entrySet()) {
                addProperty(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    @Override
    public boolean isFancy() {
        return fancy;
    }

    @Override
    public NutsInfoFormat setFancy(boolean fancy) {
        this.fancy = fancy;
        return this;
    }

    @Override
    public void print(PrintStream w) {
        checkSession();
        List<String> args = new ArrayList<>();
        args.add("--escape-text=false");
        if (isFancy()) {
            args.add("--multiline-property=nuts-runtime-path=;");
            args.add("--multiline-property=java-classpath=" + File.pathSeparator);
            args.add("--multiline-property=java-library-path=" + File.pathSeparator);

            args.add("--multiline-property=nuts-boot-runtime-path=:|;");
            args.add("--multiline-property=java.class.path=" + File.pathSeparator);
            args.add("--multiline-property=java-class-path=" + File.pathSeparator);
            args.add("--multiline-property=java.library.path=" + File.pathSeparator);
        }
        Object result = null;
        if (requests.isEmpty()) {
            result = buildWorkspaceMap(isShowRepositories());
        } else if (requests.size() == 1) {
            final Map<String, Object> t = buildWorkspaceMap(true);
            String key = requests.get(0);
            Object v = t.get(key);
            if (v != null) {
                result = v;
            } else {
                if (!isLenient()) {
                    throw new NutsIllegalArgumentException(getSession(), "property not found : " + key);
                }
            }
        } else {
            final Map<String, Object> t = buildWorkspaceMap(true);
            Map<String, Object> e = new LinkedHashMap<>();
            result = e;
            for (String request : requests) {
                if (t.containsKey(request)) {
                    e.put(request, t.get(request));
                } else {
                    if (!isLenient()) {
                        throw new NutsIllegalArgumentException(getSession(), "property not found : " + request);
                    }
                }
            }
        }
        getSession().formatObject(result).configure(true, args.toArray(new String[0])).print(w);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isEnabled();
        switch (a.getStringKey()) {
            case "-r":
            case "--repos": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    this.setShowRepositories(val);
                }
                return true;
            }
            case "--fancy": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    this.setFancy(val);
                }
                return true;
            }
            case "-l":
            case "--lenient": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    this.setLenient(val);
                }
                return true;
            }
            case "--add": {
                NutsArgument val = cmdLine.nextString().getArgumentValue();
                if (enabled) {
                    extraProperties.put(val.getStringKey(), val.getStringValue());
                }
                return true;
            }
            case "-p":
            case "--path": {
                cmdLine.skip();
                if (enabled) {
                    requests.add("nuts-workspace");
                    for (NutsStoreLocation folderType : NutsStoreLocation.values()) {
                        requests.add("nuts-workspace-" + folderType.id());
                    }
                    requests.add("user-home");
                    requests.add("user-dir");
                }
                return true;
            }
            case "-e":
            case "--env": {
                cmdLine.skip();
                if (enabled) {
                    requests.add("platform");
                    requests.add("java-version");
                    requests.add("java-home");
                    requests.add("java-executable");
                    requests.add("java-classpath");
                    requests.add("os-name");
                    requests.add("os-family");
                    requests.add("os-dist");
                    requests.add("os-arch");
                    requests.add("user-name");
                }
                return true;
            }
            case "-c":
            case "--cmd": {
                cmdLine.skip();
                if (enabled) {
                    requests.add("command-line-long");
                    requests.add("command-line-short");
                    requests.add("inherited");
                    requests.add("inherited-nuts-boot-args");
                    requests.add("inherited-nuts-args");
                }
                return true;
            }
            case "-g":
            case "--get": {
                String r = cmdLine.nextString().getStringValue();
                if (enabled) {
                    requests.add(r);
                }
                while (true) {
                    NutsArgument p = cmdLine.peek();
                    if (p != null && !p.isOption()) {
                        cmdLine.skip();
                        if (enabled) {
                            requests.add(p.getString());
                        }
                    } else {
                        break;
                    }
                }
                return true;
            }
            default: {
                if (getSession().configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String key(String prefix, String key) {
        if (CoreStringUtils.isBlank(prefix)) {
            return key;
        }
        return prefix + "." + key;
    }

    //    @Override
    private Map<String, Object> buildWorkspaceMap(boolean deep) {
        String prefix = null;
        FilteredMap props = new FilteredMap(filter);
        NutsWorkspace ws = getWorkspace();
        NutsWorkspaceConfigManager rt = ws.config();
        NutsWorkspaceOptions options = ws.config().getOptions();
        Set<String> extraKeys = new TreeSet<>();
        extraKeys = new TreeSet(extraProperties.keySet());

        props.put("name", stringValue(ws.getName()));
        props.put("nuts-api-version", stringValue(ws.getApiVersion()));
        NutsIdFormat idFormat = ws.id().formatter();
        props.put("nuts-api-id", idFormat.value(ws.getApiId()).format());
        props.put("nuts-runtime-id", idFormat.value(ws.getRuntimeId()).format());
        URL[] cl = rt.getBootClassWorldURLs();
        List<String> runtimeClassPath = new ArrayList<>();
        if (cl != null) {
            for (URL url : cl) {
                if (url != null) {
                    String s = url.toString();
                    try {
                        s = Paths.get(url.toURI()).toFile().getPath();
                    } catch (URISyntaxException ex) {
                        s = s.replace(":", "\\:");
                    }
                    runtimeClassPath.add(s);
                }
            }
        }

        props.put("nuts-runtime-path", stringValue(String.join(";", runtimeClassPath)));
        props.put("nuts-workspace-id", stringValue(ws.getUuid()));
        props.put("nuts-store-layout", stringValue(ws.locations().getStoreLocationLayout()));
        props.put("nuts-store-strategy", stringValue(ws.locations().getStoreLocationStrategy()));
        props.put("nuts-repo-store-strategy", stringValue(ws.locations().getRepositoryStoreLocationStrategy()));
        props.put("nuts-global", options.isGlobal());
        props.put("nuts-workspace", stringValue(ws.locations().getWorkspaceLocation().toString()));
        for (NutsStoreLocation folderType : NutsStoreLocation.values()) {
            props.put("nuts-workspace-" + folderType.id(), stringValue(ws.locations().getStoreLocation(folderType).toString()));
        }
        props.put("nuts-open-mode", stringValue(options.getOpenMode() == null ? NutsOpenMode.OPEN_OR_CREATE : options.getOpenMode()));
        props.put("nuts-secure", (ws.security().setSession(getSession()).isSecure()));
        props.put("nuts-gui", options.isGui());
        props.put("nuts-inherited", options.isInherited());
        props.put("nuts-recover", options.isRecover());
        props.put("nuts-reset", options.isReset());
        props.put("nuts-debug", options.isDebug());
        props.put("nuts-trace", options.isTrace());
        props.put("nuts-read-only", (options.isReadOnly()));
        props.put("nuts-skip-companions", options.isSkipCompanions());
        props.put("nuts-skip-welcome", options.isSkipWelcome());
        props.put("nuts-skip-boot", options.isSkipBoot());
        props.put("java-version", stringValue(System.getProperty("java.version")));
        props.put("platform", idFormat.value(ws.env().getPlatform()).format());
        props.put("java-home", stringValue(System.getProperty("java.home")));
        props.put("java-executable", stringValue(NutsJavaSdkUtils.of(ws).resolveJavaCommandByHome(null, getSession())));
        props.put("java-classpath", stringValue(System.getProperty("java.class.path")));
        props.put("java-library-path", stringValue(System.getProperty("java.library.path")));
        props.put("os-name", ws.env().getOs().toString());
        props.put("os-family", stringValue(ws.env().getOsFamily()));
        if (ws.env().getOsDist() != null) {
            props.put("os-dist", stringValue(ws.env().getOsDist().toString()));
        }
        props.put("os-arch", stringValue(ws.env().getArchFamily().id()));
        props.put("user-name", stringValue(System.getProperty("user.name")));
        props.put("user-home", stringValue(System.getProperty("user.home")));
        props.put("user-dir", stringValue(System.getProperty("user.dir")));
        props.put("command-line-long", ws.commandLine().formatter(ws.commandLine().create(ws.config().options().format().compact(false).getBootCommand())).format());
        props.put("command-line-short", ws.commandLine().formatter(ws.commandLine().create(ws.config().options().format().compact(true).getBootCommand())).format());
        props.put("inherited", ws.config().options().isInherited());
        props.put("inherited-nuts-boot-args", ws.commandLine().formatter(ws.commandLine().parse(System.getProperty("nuts.boot.args"))).format());
        props.put("inherited-nuts-args", ws.commandLine().formatter(ws.commandLine().parse(System.getProperty("nuts.args"))).format());
        props.put("creation-started", stringValue(Instant.ofEpochMilli(ws.config().getCreationStartTimeMillis())));
        props.put("creation-finished", stringValue(Instant.ofEpochMilli(ws.config().getCreationFinishTimeMillis())));
        props.put("creation-within", CoreTimeUtils.formatPeriodMilli(ws.config().getCreationTimeMillis()).trim());
        props.put("repositories-count", (ws.repos().setSession(getSession()).getRepositories().length));
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.get(extraKey));
        }
        if (deep) {
            Map<String, Object> repositories = new LinkedHashMap<>();
            props.put("repos", repositories);
            for (NutsRepository repository : ws.repos().setSession(getSession()).getRepositories()) {
                repositories.put(repository.getName(), buildRepoRepoMap(repository, deep, prefix));
            }
        }

        return props.build();
    }

    private Map<String, Object> buildRepoRepoMap(NutsRepository repo, boolean deep, String prefix) {
        FilteredMap props = new FilteredMap(filter);
        props.put(key(prefix, "name"), stringValue(repo.getName()));
        props.put(key(prefix, "global-name"), repo.config().getGlobalName());
        props.put(key(prefix, "uuid"), stringValue(repo.getUuid()));
        props.put(key(prefix, "type"), repo.config().getType());
        props.put(key(prefix, "speed"), (repo.config().getSpeed()));
        props.put(key(prefix, "enabled"), (repo.config().isEnabled()));
        props.put(key(prefix, "index-enabled"), (repo.config().isIndexEnabled()));
        props.put(key(prefix, "index-subscribed"), (repo.config().setSession(getSession()).isIndexSubscribed()));
        props.put(key(prefix, "location"), repo.config().getLocation(false));
        if (repo.config().getLocation(false) != null) {
            props.put(key(prefix, "location-expanded"), repo.config().getLocation(true));
        }
        props.put(key(prefix, "deploy-order"), stringValue(repo.config().getDeployOrder()));
        props.put(key(prefix, "store-location-strategy"), stringValue(repo.config().getStoreLocationStrategy()));
        props.put(key(prefix, "store-location"), stringValue(repo.config().getStoreLocation()));
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            props.put(key(prefix, "store-location-" + value.id()), stringValue(repo.config().getStoreLocation(value)));
        }
        props.put(key(prefix, "supported-mirroring"), (repo.config().isSupportedMirroring()));
        if (repo.config().isSupportedMirroring()) {
            props.put(key(prefix, "mirrors-count"), ((!repo.config()
                    .setSession(getSession())
                    .isSupportedMirroring()) ? 0 : repo.config()
                                    .setSession(getSession())
                                    .getMirrors().length));
        }
        if (deep) {
            if (repo.config().isSupportedMirroring()) {
                Map<String, Object> mirrors = new LinkedHashMap<>();
                props.put("mirrors", mirrors);
                for (NutsRepository mirror : repo.config()
                        .setSession(getSession())
                        .getMirrors()) {
                    mirrors.put(mirror.getName(), buildRepoRepoMap(mirror, deep, null));
                }
            }
        }
        return props.build();
    }

    private String stringValue(Object s) {
        return getWorkspace().formats().text().builder().append(CoreCommonUtils.stringValue(s)).toString();
    }

    public boolean isLenient() {
        return lenient;
    }

    public NutsInfoFormat setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    private static class FilteredMap {

        private Predicate<String> filter;
        private LinkedHashMap<String, Object> data = new LinkedHashMap<>();

        public FilteredMap(Predicate<String> filter) {
            this.filter = filter;
        }

        public boolean accept(String s) {
            return filter.test(s);
        }

        public void put(String s, Supplier<Object> v) {
            if (filter.test(s)) {
                data.put(s, v.get());
            }
        }

        public void putAnyway(String s, Object v) {
            data.put(s, v);
        }

        public void put(String s, Object v) {
            if (filter.test(s)) {
                data.put(s, v);
            }
        }

        public Map<String, Object> build() {
            return data;
        }
    }
}
