package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;

import java.nio.file.Paths;

public class NDocusaurusMain implements NutsApplication, NutsAppCmdProcessor {

    boolean start;
    boolean build;
    String workdir = null;
    boolean buildPdf = false;

    public static void main(String[] args) {
        NutsApplication.main(NDocusaurusMain.class, args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        appContext.processCommandLine(this);
    }

    @Override
    public boolean onCmdNextOption(NutsArgument option, NutsCommandLine commandline, NutsApplicationContext context) {
        switch (option.getKey().getString()) {
            case "-d":
            case "--dir": {
                if (workdir == null) {
                    workdir = commandline.nextString().getValue().getString();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onCmdNextNonOption(NutsArgument nonOption, NutsCommandLine commandline, NutsApplicationContext context) {
        switch (nonOption.getString()) {
            case "start": {
                start = commandline.nextBoolean().getBooleanValue();
                return true;
            }
            case "build": {
                build = commandline.nextBoolean().getBooleanValue();
                return true;
            }
            case "pdf": {
                buildPdf = commandline.nextBoolean().getBooleanValue();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCmdFinishParsing(NutsCommandLine commandline, NutsApplicationContext context) {
        if (!start && !build && !buildPdf) {
            commandline.required(NutsMessage.cstyle("missing command. try ```sh ndocusaurus pdf | start | build```"));
        }
    }

    @Override
    public void onCmdExec(NutsCommandLine commandline, NutsApplicationContext context) {
        if (workdir == null) {
            workdir = ".";
        }
        DocusaurusProject docusaurusProject = new DocusaurusProject(workdir,
                Paths.get(workdir).resolve(".dir-template").resolve("src").toString(),
                context.getSession());
        new DocusaurusCtrl(docusaurusProject, context)
                .setBuildWebSite(build)
                .setStartWebSite(start)
                .setBuildPdf(buildPdf)
                .setAutoInstallNutsPackages(context.getSession().boot().getBootOptions().getConfirm() == NutsConfirmationMode.YES)
                .run();
    }

}