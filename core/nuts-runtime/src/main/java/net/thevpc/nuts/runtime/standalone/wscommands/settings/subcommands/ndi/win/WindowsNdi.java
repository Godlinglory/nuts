package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.win;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsShellFamily;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.PathInfo;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.FreeDesktopEntryWriter;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.NdiScriptInfo;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base.BaseSystemNdi;
import net.thevpc.nuts.runtime.core.shell.NutsShellHelper;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class WindowsNdi extends BaseSystemNdi {

    public WindowsNdi(NutsSession session) {
        super(session);
    }

    protected NutsShellFamily[] getShellGroups() {
        Set<NutsShellFamily> all=new LinkedHashSet<>(Arrays.asList(session.env().getShellFamilies()));
        all.retainAll(Arrays.asList(NutsShellFamily.WIN_CMD,NutsShellFamily.WIN_POWER_SHELL));
        return all.toArray(new NutsShellFamily[0]);
    }

    //    @Override
//    public NdiScriptInfo getNutsTerm(NdiScriptOptions options) {
//        return new NdiScriptInfo() {
//            @Override
//            public Path path() {
//                return options.resolveBinFolder().resolve(getExecFileName("nuts-term"));
//            }
//
//            @Override
//            public PathInfo create() {
//                Path apiConfigFile = path();
//                return addFileLine("nuts-term",
//                        options.resolveNutsApiId(),
//                        apiConfigFile, getCommentLineConfigHeader(),
//                        "@ECHO OFF" + newlineString() +
//                                createNutsEnvString(options, true, true) + newlineString()
//                                + "cmd.exe /K " + getExecFileName("nuts") + " welcome " + newlineString()
//                        ,
//                        getShebanSh());
//            }
//        };
//    }


    @Override
    protected String createNutsScriptContent(NutsId fnutsId, NdiScriptOptions options, NutsShellFamily shellFamily) {
        StringBuilder command = new StringBuilder();
        command.append(getExecFileName("nuts")).append(" ").append(NutsShellHelper.of(shellFamily).varRef("NUTS_OPTIONS")).append(" ");
        if (options.getLauncher().getNutsOptions() != null) {
            for (String o : options.getLauncher().getNutsOptions()) {
                command.append(" ").append(o);
            }
        }
        command.append(" \"").append(fnutsId).append("\"");
        command.append(" %*");
        return command.toString();
    }

    public void onPostGlobal(NdiScriptOptions options, PathInfo[] updatedPaths) {

    }


    @Override
    public String getExecFileName(String name) {
        return name + ".cmd";
    }

    @Override
    protected FreeDesktopEntryWriter createFreeDesktopEntryWriter() {
        return new WindowFreeDesktopEntryWriter(
                session.env().getDesktopPath()==null?null:NutsPath.of(session.env().getDesktopPath(),getSession())
                , session);
    }

    protected int resolveIconExtensionPriority(String extension) {
        extension = extension.toLowerCase();
        switch (extension) {
            //support only ico
            case "ico":
                return 3;
        }
        return -1;
    }

    public boolean isShortcutFileNameUserFriendly() {
        return true;
    }


    @Override
    public String getTemplateName(String name, NutsShellFamily shellFamily) {
        switch (shellFamily){
            case WIN_CMD:{
                return "template-" + name + ".cmd";
            }
            case WIN_POWER_SHELL:{
                return "template-" + name + ".ps1";
            }
        }
        return "template-" + name + ".cmd";
    }


    public NdiScriptInfo[] getNutsTerm(NdiScriptOptions options) {
//        return Arrays.stream(getShellGroups())
//                .map(x -> getNutsTerm(options, x))
//                .filter(Objects::nonNull)
//                .toArray(NdiScriptInfo[]::new);
        return Arrays.stream(new NutsShellFamily[]{NutsShellFamily.WIN_CMD})
                .map(x -> getNutsTerm(options, x))
                .filter(Objects::nonNull)
                .toArray(NdiScriptInfo[]::new);

    }

    public NdiScriptInfo getNutsTerm(NdiScriptOptions options,NutsShellFamily shellFamily) {
        switch (shellFamily){
            case WIN_CMD:
            {
                return new NdiScriptInfo() {
                    @Override
                    public NutsPath path() {
                        return options.resolveBinFolder().resolve("nuts-term.cmd");
                    }

                    @Override
                    public PathInfo create() {
                        return scriptBuilderTemplate("nuts-term",NutsShellFamily.WIN_CMD, "nuts-term", options.resolveNutsApiId(), options)
                                .setPath(path())
                                .build();
                    }
                };
            }
            case WIN_POWER_SHELL:
            {
                return new NdiScriptInfo() {
                    @Override
                    public NutsPath path() {
                        return options.resolveBinFolder().resolve("nuts-term.ps1");
                    }

                    @Override
                    public PathInfo create() {
                        return scriptBuilderTemplate("nuts-term",NutsShellFamily.WIN_POWER_SHELL, "nuts-term", options.resolveNutsApiId(), options)
                                .setPath(path())
                                .build();
                    }
                };
            }
        }
        return null;
    }

    public NdiScriptInfo getIncludeNutsEnv(NdiScriptOptions options, NutsShellFamily shellFamily) {
        switch (shellFamily) {
            case WIN_CMD:{
                return new NdiScriptInfo() {
                    @Override
                    public NutsPath path() {
                        return options.resolveIncFolder().resolve(".nuts-env.cmd");
                    }

                    @Override
                    public PathInfo create() {
                        return scriptBuilderTemplate("nuts-env", NutsShellFamily.WIN_CMD, "nuts-env", options.resolveNutsApiId(), options)
                                .setPath(path())
                                .build();
                    }
                };
            }
            case WIN_POWER_SHELL: {
                return new NdiScriptInfo() {
                    @Override
                    public NutsPath path() {
                        return options.resolveIncFolder().resolve(".nuts-env.ps1");
                    }

                    @Override
                    public PathInfo create() {
                        return scriptBuilderTemplate("nuts-env", NutsShellFamily.WIN_POWER_SHELL, "nuts-env", options.resolveNutsApiId(), options)
                                .setPath(path())
                                .build();
                    }
                };
            }
        }
        return null;
    }
    public NdiScriptInfo getIncludeNutsTermInit(NdiScriptOptions options, NutsShellFamily shellFamily) {
        switch (shellFamily) {
            case WIN_CMD:{
                return
                        new NdiScriptInfo() {
                            @Override
                            public NutsPath path() {
                                return options.resolveIncFolder().resolve(".nuts-term-init.cmd");
                            }

                            @Override
                            public PathInfo create() {
                                return scriptBuilderTemplate("nuts-term-init", NutsShellFamily.WIN_CMD, "nuts-term-init", options.resolveNutsApiId(), options)
                                        .setPath(path())
                                        .build();
                            }
                        }
                        ;
            }
            case WIN_POWER_SHELL: {
                return
                        new NdiScriptInfo() {
                            @Override
                            public NutsPath path() {
                                return options.resolveIncFolder().resolve(".nuts-term-init.ps1");
                            }

                            @Override
                            public PathInfo create() {
                                return scriptBuilderTemplate("nuts-term-init", NutsShellFamily.WIN_POWER_SHELL, "nuts-term-init", options.resolveNutsApiId(), options)
                                        .setPath(path())
                                        .build();
                            }
                        }
                        ;
            }
        }
        return null;
    }

    public NdiScriptInfo getIncludeNutsInit(NdiScriptOptions options, NutsShellFamily shellFamily) {
        switch (shellFamily) {
            case WIN_CMD:{
                return new NdiScriptInfo() {
                    @Override
                    public NutsPath path() {
                        return options.resolveIncFolder().resolve(".nuts-init.cmd");
                    }

                    @Override
                    public PathInfo create() {
                        NutsPath apiConfigFile = path();
                        return scriptBuilderTemplate("nuts-init", NutsShellFamily.WIN_CMD, "nuts-init", options.resolveNutsApiId(), options)
                                .setPath(apiConfigFile)
                                .buildAddLine(WindowsNdi.this);
                    }
                };
            }
            case WIN_POWER_SHELL: {
                return new NdiScriptInfo() {
                    @Override
                    public NutsPath path() {
                        return options.resolveIncFolder().resolve(".nuts-init.ps1");
                    }

                    @Override
                    public PathInfo create() {
                        NutsPath apiConfigFile = path();
                        return scriptBuilderTemplate("nuts-init", NutsShellFamily.WIN_POWER_SHELL, "nuts-init", options.resolveNutsApiId(), options)
                                .setPath(apiConfigFile)
                                .buildAddLine(WindowsNdi.this);
                    }
                };
            }
        }
        return null;
    }
}
