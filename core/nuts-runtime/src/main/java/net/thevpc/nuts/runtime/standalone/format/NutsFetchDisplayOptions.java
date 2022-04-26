/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreEnumUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;

/**
 *
 * @author thevpc
 */
public class NutsFetchDisplayOptions {

    public static NutsDisplayProperty[] DISPLAY_LONG = new NutsDisplayProperty[]{
        NutsDisplayProperty.STATUS,
        NutsDisplayProperty.INSTALL_DATE,
        NutsDisplayProperty.INSTALL_USER,
        NutsDisplayProperty.REPOSITORY,
        NutsDisplayProperty.ID
    };
    public static NutsDisplayProperty[] DISPLAY_LONG_LONG = new NutsDisplayProperty[]{
        NutsDisplayProperty.LONG_STATUS,
        NutsDisplayProperty.INSTALL_DATE,
        NutsDisplayProperty.INSTALL_USER,
        NutsDisplayProperty.REPOSITORY,
        NutsDisplayProperty.ID
    };
    public static NutsDisplayProperty[] DISPLAY_MIN = new NutsDisplayProperty[]{
        NutsDisplayProperty.ID
    };

    private NutsIdFormat idFormat;
    private List<NutsDisplayProperty> displays = new ArrayList<>();
    private NutsSession session;

    public NutsFetchDisplayOptions(NutsSession session) {
        this.session = session;
        this.idFormat = NutsIdFormat.of(session);
        this.idFormat.setHighlightImportedGroupId(true);
        this.idFormat.setOmitOtherProperties(true);
        this.idFormat.setOmitFace(true);
        this.idFormat.setOmitRepository(true);
//        this.idFormat.setOmitAlternative(false);
        this.idFormat.setOmitClassifier(false);
        this.idFormat.setOmitGroupId(false);
        this.idFormat.setOmitImportedGroupId(false);
    }

    public void setIdFormat(NutsIdFormat idFormat) {
        this.idFormat = idFormat;
    }

    public NutsIdFormat getIdFormat() {
        return idFormat;
    }

    public NutsDisplayProperty[] getDisplayProperties() {
        if (displays.isEmpty()) {
            return new NutsDisplayProperty[]{NutsDisplayProperty.ID};
        }
        return displays.toArray(new NutsDisplayProperty[0]);
    }

    public void addDisplay(String[] columns) {
        if (columns != null) {
            addDisplay(parseNutsDisplayProperty(Arrays.stream(columns).collect(Collectors.joining(","))));
        }
    }

    public void setDisplay(NutsDisplayProperty display) {
        if (display == null) {
            setDisplay(new NutsDisplayProperty[0]);
        } else {
            setDisplay(new NutsDisplayProperty[]{display});
        }
    }

    public void setDisplay(NutsDisplayProperty[] display) {
        displays.clear();
        addDisplay(display);
    }

    public void addDisplay(NutsDisplayProperty[] display) {
        if (display != null) {
            for (NutsDisplayProperty t : display) {
                if (t != null) {
                    displays.add(t);
                }
            }
        }
    }

    public void setDisplayLong(boolean longFormat) {
        if (longFormat) {
            setDisplay(DISPLAY_LONG);
        } else {
            setDisplay(DISPLAY_MIN);
        }
    }

    public boolean isRequireDefinition() {
        for (NutsDisplayProperty display : getDisplayProperties()) {
            if (!NutsDisplayProperty.ID.equals(display)) {
                return true;
            }
        }
        return false;
    }

    public final NutsFetchDisplayOptions configure(boolean skipUnsupported, String... args) {
        configure(false, NutsCommandLine.of(args));
        return this;
    }

    public final boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        boolean conf = false;
        while (commandLine.hasNext()) {
            if (!configureFirst(commandLine)) {
                if (skipUnsupported) {
                    commandLine.skip();
                } else {
                    commandLine.throwUnexpectedArgument(session);
                }
            } else {
                conf = true;
            }
        }
        return conf;
    }

    public boolean configureFirst(NutsCommandLine cmdLine) {
        if (idFormat.configureFirst(cmdLine)) {
            return true;
        }
        NutsArgument a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        switch(a.getStringKey().orElse("")) {
            case "-l":
            case "--long": {
                a = cmdLine.nextBoolean().get(session);
                if(a.isActive()) {
                    if(a.getBooleanValue().get(session)){
                        setDisplay(DISPLAY_LONG);
                    }else {
                        setDisplay(DISPLAY_MIN);
                    }
                }
                return true;
            }
            case "--ll":
            case "--long-long": {
                a = cmdLine.nextBoolean().get(session);
                if(a.isActive()) {
                    if(a.getBooleanValue().get(session)){
                        setDisplay(DISPLAY_LONG_LONG);
                    }else {
                        setDisplay(DISPLAY_MIN);
                    }
                }
                return true;
            }
            case "--display": {
                a = cmdLine.nextString().get(session);
                if(a.isActive()) {
                    setDisplay(parseNutsDisplayProperty(a.getStringValue().get(session)));
                }
                return true;
            }

        }
        return false;
    }

    public String[] toCommandLineOptions() {
        List<String> displayOptionsArgs = new ArrayList<>();
        if (this.getIdFormat() != null) {
            if (this.getIdFormat().isHighlightImportedGroupId()) {
                displayOptionsArgs.add("--highlight-imported-group");
            }
            if (this.getIdFormat().isOmitOtherProperties()) {
                displayOptionsArgs.add("--omit-env");
            }
            if (this.getIdFormat().isOmitFace()) {
                displayOptionsArgs.add("--omit-face");
            }
            if (this.getIdFormat().isOmitGroupId()) {
                displayOptionsArgs.add("--omit-group");
            }
            if (this.getIdFormat().isOmitImportedGroupId()) {
                displayOptionsArgs.add("--omit-imported-group");
            }
            if (this.getIdFormat().isOmitRepository()) {
                displayOptionsArgs.add("--omit-repo");
            }

            displayOptionsArgs.add("--display=" + String.join(",", Arrays.asList(getDisplayProperties()).stream().map(x -> CoreEnumUtils.getEnumString(x)).collect(Collectors.toList())));
        }
        return displayOptionsArgs.toArray(new String[0]);
    }

    public static NutsDisplayProperty[] parseNutsDisplayProperty(String str) {
        String[] dispNames = StringTokenizerUtils.splitDefault(str).toArray(new String[0]);
        //first pass, check is ALL is visited. In that case will be replaced by all non visited types
        Set<NutsDisplayProperty> visited = new HashSet<NutsDisplayProperty>();
        for (int i = 0; i < dispNames.length; i++) {
            switch (dispNames[i]) {
                case "all": {
                    //ignore in this pass
                    break;
                }
                case "long": {
                    visited.addAll(Arrays.asList(DISPLAY_LONG));
                    break;
                }
                case "long-long": {
                    visited.addAll(Arrays.asList(DISPLAY_LONG_LONG));
                    break;
                }
                default: {
                    visited.add(CoreEnumUtils.parseEnumString(dispNames[i], NutsDisplayProperty.class, true));
                }
            }
        }
        List<NutsDisplayProperty> all2 = new ArrayList<>();
        for (int i = 0; i < dispNames.length; i++) {
            switch (dispNames[i]) {
                case "all": {
                    for (NutsDisplayProperty value : NutsDisplayProperty.values()) {
                        if (!visited.contains(value)) {
                            all2.add(value);
                        }
                    }
                    break;
                }
                case "long": {
                    all2.addAll(Arrays.asList(DISPLAY_LONG));
                    break;
                }
                case "long-long": {
                    all2.addAll(Arrays.asList(DISPLAY_LONG_LONG));
                    break;
                }
                default: {
                    all2.add(CoreEnumUtils.parseEnumString(dispNames[i], NutsDisplayProperty.class, false));
                }
            }
        }
        return (all2.toArray(new NutsDisplayProperty[0]));
    }

    @Override
    public String toString() {
        return "NutsFetchDisplayOptions{" +
                "idFormat=" + idFormat +
                ", displays=" + displays +
                '}';
    }
}
