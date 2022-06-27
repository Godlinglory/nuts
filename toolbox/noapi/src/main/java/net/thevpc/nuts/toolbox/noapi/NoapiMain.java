package net.thevpc.nuts.toolbox.noapi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.toolbox.noapi.model.NoapiCmdData;
import net.thevpc.nuts.toolbox.noapi.service.NOpenAPIService;
import net.thevpc.nuts.util.NutsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NoapiMain implements NutsApplication, NutsAppCmdProcessor {

    private NOpenAPIService service;
    private NoapiCmdData ref = new NoapiCmdData();

    private List<NoapiCmdData> data = new ArrayList<>();

    public static void main(String[] args) {
        new NoapiMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        this.service = new NOpenAPIService(appContext);
        NutsCommandLine cmdLine = appContext.getCommandLine();
        ref.setCommand("pdf");
        appContext.processCommandLine(this);
    }

    @Override
    public boolean onCmdNextOption(NutsArgument option, NutsCommandLine commandline, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        switch (option.asString().get(session)) {
            case "--yaml": {
                commandline.nextBoolean();
                ref.setOpenAPIFormat("yaml");
                if (!data.isEmpty()) {
                    data.get(data.size() - 1).setOpenAPIFormat("yaml");
                }
                return true;
            }
            case "--json": {
                commandline.nextBoolean();
                ref.setOpenAPIFormat("json");
                if (!data.isEmpty()) {
                    data.get(data.size() - 1).setOpenAPIFormat("json");
                }
                return true;
            }
            case "--keep": {
                commandline.nextBoolean();
                ref.setKeep(true);
                if (!data.isEmpty()) {
                    data.get(data.size() - 1).setKeep(true);
                }
                return true;
            }
            case "--vars": {
                NutsArgument a = commandline.nextString().get();
                if (a.isActive()) {
                    String vars = a.getStringValue().get();
                    ref.setVars(vars);
                    if (!data.isEmpty()) {
                        data.get(data.size() - 1).setVars(vars);
                    }
                }
                return true;
            }
            case "--var": {
                NutsArgument a = commandline.nextString().get();
                if (a.isActive()) {
                    String vars = a.getStringValue().get();
                    NutsArgument b = NutsArgument.of(vars);
                    if (b.isActive()) {
                        ref.getVarsMap().put(b.getKey().toStringLiteral(), b.getValue().toStringLiteral());
                        if (!data.isEmpty()) {
                            data.get(data.size() - 1).getVarsMap().put(b.getKey().toStringLiteral(), b.getValue().toStringLiteral());
                        }
                    }
                }
                return true;
            }
            case "--open-api": {
                commandline.nextBoolean();
                ref.setOpenAPI(true);
                if (!data.isEmpty()) {
                    data.get(data.size() - 1).setOpenAPI(true);
                }
                return true;
            }
            case "--pdf": {
                commandline.nextBoolean();
                ref.setCommand("pdf");
                if (!data.isEmpty()) {
                    data.get(data.size() - 1).setCommand("pdf");
                }
                return true;
            }
            case "--target": {
                NutsArgument a = commandline.nextString().get();
                if (a.isActive()) {
                    String target = a.getStringValue().get();
                    if (target.contains("*")) {
                        ref.setTarget(target);
                    }
                    if (!data.isEmpty()) {
                        data.get(data.size() - 1).setTarget(target);
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCmdNextNonOption(NutsArgument nonOption, NutsCommandLine commandline, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        NoapiCmdData c = new NoapiCmdData();
        c.setCommand(ref.getCommand());
        c.setKeep(ref.isKeep());
        c.setOpenAPI(ref.isOpenAPI());
        c.setTarget(ref.getTarget());
        c.setVars(ref.getVars());
        c.setVarsMap(new HashMap<>(ref.getVarsMap()));
        NutsArgument pathArg = commandline.next().get(session);
        c.setPath(pathArg.getKey().asString().get(session));
        data.add(c);
        return true;
    }

    @Override
    public void onCmdFinishParsing(NutsCommandLine commandline, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        if (data.isEmpty()) {
            commandline.throwMissingArgument(session);
        }
        for (NoapiCmdData d : data) {
            NutsUtils.requireNonBlank(d.getPath(), session, "path");
            if (!"pdf".equals(d.getCommand())) {
                throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("unsupported command %s", d.getCommand()));
            }
        }
    }

    @Override
    public void onCmdExec(NutsCommandLine commandline, NutsApplicationContext context) {
        for (NoapiCmdData d : data) {
            switch (d.getCommand()) {
                case "pdf": {
                    NOpenAPIService service = new NOpenAPIService(context);
                    service.run(d.getPath(), d.getTarget(), d.getVars(), d.getVarsMap(), d.isKeep());
                    break;
                }
            }
        }
    }
}
