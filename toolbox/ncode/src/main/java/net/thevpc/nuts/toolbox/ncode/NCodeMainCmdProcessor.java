package net.thevpc.nuts.toolbox.ncode;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineProcessor;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.toolbox.ncode.bundles.strings.StringComparator;
import net.thevpc.nuts.toolbox.ncode.bundles.strings.StringComparators;
import net.thevpc.nuts.toolbox.ncode.filters.JavaSourceFilter;
import net.thevpc.nuts.toolbox.ncode.filters.PathSourceFilter;
import net.thevpc.nuts.toolbox.ncode.processors.JavaSourceFormatter;
import net.thevpc.nuts.toolbox.ncode.processors.PathSourceFormatter;
import net.thevpc.nuts.toolbox.ncode.sources.SourceFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.thevpc.nuts.toolbox.ncode.SourceNavigator.navigate;

class NCodeMainCmdProcessor implements NCmdLineProcessor {
    private List<String> paths = new ArrayList<>();
    private List<StringComparator> typeComparators = new ArrayList<>();
    private List<StringComparator> fileComparators = new ArrayList<>();
    private boolean caseInsensitive = false;
    private NApplicationContext applicationContext;

    public NCodeMainCmdProcessor(NApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onCmdInitParsing(NCmdLine commandLine, NCmdLineContext context) {
        commandLine.setExpandSimpleOptions(true);
    }

    @Override
    public boolean onCmdNextOption(NArg option, NCmdLine commandLine, NCmdLineContext context) {
        NSession session = applicationContext.getSession();
        switch (option.getStringKey().get(session)) {
            case "-i": {
                option = commandLine.nextFlag().get(session);
                caseInsensitive = option.getBooleanValue().get(session);
                return true;
            }
            case "-t": {
                typeComparators.add(comp(commandLine.nextEntry().get(session).getStringValue().get(session)));
                return true;
            }
            case "-f": {
                fileComparators.add(comp(commandLine.nextEntry().get(session).getStringValue().get(session)));
                return true;
            }
        }
        return false;
    }

    private StringComparator comp(String x) {
        boolean negated = false;
        if (x.startsWith("!")) {
            negated = true;
            x = x.substring(1);
        }
        if (!x.startsWith("^")) {
            x = "*" + x;
        }
        if (!x.startsWith("$")) {
            x = x + "*";
        }
        StringComparator c = caseInsensitive ? StringComparators.ilike(x) : StringComparators.like(x);
        if (negated) {
            c = StringComparators.not(c);
        }
        return c;
    }

    @Override
    public boolean onCmdNextNonOption(NArg nonOption, NCmdLine commandLine, NCmdLineContext context) {
        NSession session = applicationContext.getSession();
        paths.add(commandLine.next().flatMap(NLiteral::asString).get(session));
        return true;
    }

    @Override
    public void onCmdExec(NCmdLine commandLine, NCmdLineContext context) {
        NSession session = applicationContext.getSession();
        if (paths.isEmpty()) {
            paths.add(".");
        }
        if(typeComparators.isEmpty() && fileComparators.isEmpty()){
            commandLine.throwMissingArgumentByName("filter");
        }
        List<Object> results=new ArrayList<>();
        if(!typeComparators.isEmpty()) {
            for (String path : paths) {
                navigate(SourceFactory.create(new File(path)), new JavaSourceFilter(typeComparators, fileComparators), new JavaSourceFormatter(), session, results);
            }
        }else{
            for (String path : paths) {
                navigate(SourceFactory.create(new File(path)), new PathSourceFilter(fileComparators), new PathSourceFormatter(), session, results);
            }
        }
        session.out().println(results);
    }

}
