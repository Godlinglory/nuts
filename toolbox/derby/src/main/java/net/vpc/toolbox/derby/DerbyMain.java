/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.toolbox.derby;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vpc
 */
public class DerbyMain extends NutsApplication {

    private File derbyBinHome = new File(".");
    private String derbyVersion = null;
    private String derbyDataHome = null;
    private Command cmd = Command.start;
    private String host = null;
    private int port = -1;
    private SSLMode sslmode = null;
    private String extraArg = null;
    private NutsApplicationContext appContext;

    public enum SSLMode {
        off, basic, peerAuthentication
    }

    public enum Command {
        start, shutdown, sysinfo, help, ping, trace, runtimeinfo, maxthreads, timeslice, logconnections
    }

    public static void main(String[] args) {
        System.out.println(getAbsoluteFile("../~","/home/vpc/data-vpc/mm"));
        //new DerbyMain().launchAndExit(args);
    }

    @Override
    public int launch(NutsApplicationContext appContext) {
        String[] args = appContext.getArgs();
        this.appContext = appContext;
        CommandLine cmdLine = new CommandLine(args);
        Argument a;
        while (cmdLine.hasNext()) {
            if (appContext.configure(cmdLine)) {
                //
            } else if ((a = cmdLine.readStringOption("--derby-version")) != null) {
                derbyVersion = a.getStringValue();
            } else if ((a = cmdLine.readStringOption("--db")) != null) {
                derbyDataHome = getAbsoluteFile(a.getStringValue(),appContext.getVarFolder());
            } else if ((a = cmdLine.readStringOption("--netbeans")) != null) {
                derbyDataHome = System.getProperty("user.home") + "/.netbeans-derby";
            } else if ((a = cmdLine.readStringOption("-h", "--host")) != null) {
                host = a.getStringValue();
            } else if ((a = cmdLine.readStringOption("-p", "--port")) != null) {
                port = a.getIntValue();
            } else if ((a = cmdLine.readStringOption("-ssl", "--ssl")) != null) {
                sslmode = SSLMode.valueOf(a.getStringValue());
            } else if ((a = cmdLine.readNonOption("start")) != null) {
                cmd = Command.start;
            } else if ((a = cmdLine.readNonOption("sys", "sysinfo")) != null) {
                cmd = Command.sysinfo;
            } else if ((a = cmdLine.readNonOption("ping")) != null) {
                cmd = Command.ping;
            } else if ((a = cmdLine.readNonOption("rt", "runtime")) != null) {
                cmd = Command.runtimeinfo;
            } else if ((a = cmdLine.readStringOption("trace")) != null) {
                cmd = Command.trace;
                extraArg = a.getStringValue();
            } else if ((a = cmdLine.readStringOption("trace-directory")) != null) {
                cmd = Command.trace;
                extraArg = a.getStringValue();
            } else if ((a = cmdLine.readStringOption("max-threads")) != null) {
                cmd = Command.maxthreads;
                extraArg = a.getStringValue();
            } else if ((a = cmdLine.readStringOption("time-slice")) != null) {
                cmd = Command.timeslice;
                extraArg = a.getStringValue();
            } else if ((a = cmdLine.readStringOption("log-connections")) != null) {
                cmd = Command.logconnections;
                extraArg = a.getStringValue();
            } else if ((a = cmdLine.readNonOption("stop", "shutdown")) != null) {
                cmd = Command.shutdown;
            } else {
                cmdLine.unexpectedArgument("derby");
            }
        }
        return main();
    }

    public int main() {
        NutsWorkspace ws = appContext.getWorkspace();
        List<String> command = new ArrayList<>();
        List<String> executorOptions = new ArrayList<>();
        derbyBinHome = new File(ws.getConfigManager().getStoreLocation(resolveNutsId(), NutsStoreFolder.PROGRAMS), "lib");
        String v = derbyVersion;
        String h = derbyDataHome;
        if (v == null) {
            NutsId best = ws.createQuery().addId("org.apache.derby:derbynet").setLatestVersions(true).findOne();
            v = best.getVersion().toString();
        }
        if (h == null) {
            h = new File(appContext.getVarFolder(),"derby-db").getPath();
        }
        File derby = download("org.apache.derby:derby#" + v);
        File derbynet = download("org.apache.derby:derbynet#" + v);
        File derbyoptionaltools = download("org.apache.derby:derbyoptionaltools#" + v);
        File derbyclient = download("org.apache.derby:derbyclient#" + v);
        File derbytools = download("org.apache.derby:derbytools#" + v);
        //use named jar because derby does test upon jar names at runtime (what a shame !!!)
        command.add("org.apache.derby:derbytools#" + v);
        executorOptions.add(
                "--classpath=" + derby.getPath() + ":" + derbynet.getPath() + ":" + derbyclient.getPath() + derbytools.getPath() + derbyoptionaltools.getPath()
        );
        if (appContext.isVerbose()) {
            executorOptions.add("--show-command");
        }
        executorOptions.add("--main-class=org.apache.derby.drda.NetworkServerControl");
        executorOptions.add("-Dderby.system.home=" + h);

        if (host != null) {
            command.add("-h");
            command.add(host);
        }
        if (port != -1) {
            command.add("-p");
            command.add(String.valueOf(port));
        }
        if (sslmode != null) {
            command.add("-ssl");
            command.add(String.valueOf(sslmode));
        }
        command.add(cmd.toString());
        if (extraArg != null) {
            command.add(extraArg);
        }
        return ws
                .createExecBuilder()
                .setExecutorOptions(executorOptions)
                .setCommand(command)
                .exec().getResult();
    }

    private File download(String id) {
        final NutsId iid = appContext.getWorkspace().getParseManager().parseId(id);
        File downloadBaseFolder = new File(derbyBinHome, iid.getVersion().getValue());
        File targetFile = new File(downloadBaseFolder, iid.getName() + ".jar");
        if (!targetFile.exists()) {
            appContext.getWorkspace().copyTo(id, targetFile.getPath(), null);
            if (appContext.isVerbose()) {
                appContext.getSession().getTerminal().getOut().println("downloading " + id + " to " + targetFile);
            }
        } else {
            if (appContext.isVerbose()) {
                appContext.getSession().getTerminal().getOut().println("using " + id + " form " + targetFile);
            }
        }
        return targetFile;
    }

    public NutsId resolveNutsId() {
        return appContext.getAppId();
    }


    /**
     * should promote this to FileUtils !!
     * @param path
     * @param cwd
     * @return
     */
    public static String getAbsoluteFile(String path, String cwd) {
        if (new File(path).isAbsolute()) {
            return path;
        }
        if (cwd == null) {
            cwd = System.getProperty("user.dir");
        }
        switch (path){
            case "~" : return System.getProperty("user.home");
            case "." : {
                File file = new File(cwd);
                try {
                    return file.getCanonicalPath();
                }catch (IOException ex){
                    return file.getAbsolutePath();
                }
            }
            case ".." : {
                File file = new File(cwd, "..");
                try {
                    return file.getCanonicalPath();
                }catch (IOException ex){
                    return file.getAbsolutePath();
                }
            }
        }
        int j=-1;
        char[] chars = path.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if(chars[i]=='/' || chars[i]=='\\'){
                j=i;
                break;
            }
        }
        if(j>0) {
            switch (path.substring(0,j)) {
                case "~":
                    String e = path.substring(j + 1);
                    if(e.isEmpty()){
                        return System.getProperty("user.home");
                    }
                    File file = new File(System.getProperty("user.home"), e);
                    try {
                        return file.getCanonicalPath();
                    }catch (IOException ex){
                        return file.getAbsolutePath();
                    }
            }
        }
        File file = new File(cwd, path);
        try {
            return file.getCanonicalPath();
        }catch (IOException ex){
            return file.getAbsolutePath();
        }
    }
}
