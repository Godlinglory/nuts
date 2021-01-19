package net.thevpc.nuts;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default Logging verb names
 *
 * @category Logging
 */
public final class NutsLogVerb {
    private static final Map<String, NutsLogVerb> cached = new HashMap<>();

    public static final NutsLogVerb INFO = of("INFO");
    public static final NutsLogVerb DEBUG = of("DEBUG");

    /**
     * Log verb used for tracing the start of an operation
     */
    public static final NutsLogVerb START = of("START");

    /**
     * Log verb used for tracing the successful termination of an operation
     */
    public static final NutsLogVerb SUCCESS = of("SUCCESS");

    /**
     * Log verb used for tracing general purpose warnings
     */
    public static final NutsLogVerb WARNING = of("WARNING");

    /**
     * Log verb used for tracing the failure to run an operation
     */
    public static final NutsLogVerb FAIL = of("FAIL");

    /**
     * Log verb used for tracing a I/O read operation
     */
    public static final NutsLogVerb READ = of("READ");

    public static final NutsLogVerb UPDATE = of("UPDATE");

    /**
     * Log verb used for tracing cache related operations
     */
    public static final NutsLogVerb CACHE = of("CACHE");

    private String name;

    public NutsLogVerb(String name) {
        if(name==null){
            throw new NullPointerException("null log verb");
        }
        this.name = name;
    }

    public static NutsLogVerb of(String name) {
        NutsLogVerb t = cached.get(name);
        if (t == null) {
            synchronized (cached) {
                t = cached.get(name);
                if (t == null) {
                    cached.put(name, t = new NutsLogVerb(name));
                }
            }
        }
        return t;
    }

    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsLogVerb that = (NutsLogVerb) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return name.toString();
    }
}