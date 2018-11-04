package net.vpc.app.nuts.toolbox.ndi;

import net.vpc.app.nuts.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LinuxNdi {
    private NutsWorkspace ws;
    private NutsSession session;
    private NutsFormattedPrintStream out;

    public LinuxNdi(NutsWorkspace ws, NutsSession session) {
        this.ws = ws;
        this.session = session;
        out = session.getTerminal().getFormattedOut();
    }

    public void createNutsScript(String id, boolean force, boolean forceBoot,boolean fetch) throws IOException {
        if ("nuts".equals(id)) {
            createBootScript(forceBoot||force,false);
        } else {
            createBootScript(forceBoot,true);
            NutsId nutsId = ws.parseNutsId(id);
            if(fetch){
                NutsFile fetched = ws.fetch(id, null);
                out.printf("==%s== resolved as ==%s==\n", id,fetched.getId());
            }
            String n = nutsId.getName();
            File ff = getScriptFile(n);
            boolean exists = ff.exists();
            if (!force && exists) {
                out.printf("Script already exists ==%s==\n", ff.getPath());
            } else {
                String idContent = "RUN : " + nutsId;
                createScript(n, nutsId.toString(), idContent, "nuts " + nutsId);
            }
        }
    }

    public void createBootScript(boolean force,boolean silent) throws IOException {
        NutsId b = ws.getBootId();
        NutsFile f = ws.fetch(b.toString(), null);
        File ff = getScriptFile("nuts");
        if (!force && ff.exists()) {
            if (!silent) {
                out.printf("Script already exists ==%s==\n", ff.getPath());
            }
        } else {
            String idContent = "BOOT : " + f.getId().toString();
            createScript("nuts", f.getId().toString(), idContent, "java -jar " + f.getFile());
        }
    }

    public File getScriptFile(String name) {
        File bin = new File(System.getProperty("user.home"), "bin");
        return new File(bin, name);
    }

    public File createScript(String name, String desc, String idContent, String content) throws IOException {
        File script = getScriptFile(name);
        if (script.getParentFile() != null) {
            if (!script.getParentFile().exists()) {
                out.printf("Creating folder ==%s==\n", script.getParentFile().getPath());
                script.getParentFile().mkdirs();
            }
        }
        if (script.exists()) {
            out.printf("Override script ==%s== for ==%s==\n", script.getPath(), desc);
        } else {
            out.printf("Creating script ==%s== for ==%s==\n", script.getPath(), desc);
        }

        try (FileWriter w = new FileWriter(script)) {
            w.write("#!/bin/sh\n");
            w.write("# THIS FILE IS GENERATED BY\n");
            w.write("#      net.vpc.app.nuts.toolbox.ndi\n");
            w.write("# DO NOT EDIT IT MANUALLY\n");
            w.write("#\n");
            w.write("# START-ID\n");
            for (String s : idContent.split("\n")) {
                w.write("# "+s+"\n");
            }
            w.write("# END-ID\n");
            w.write("#\n");
            w.write("# START-COMMAND\n");
            w.write("\n");
            w.write(content);
            if(!content.endsWith("\n") && !content.endsWith("\r")){
                w.write("\n");
            }
            w.write("\n");
            w.write("# END-COMMAND\n");
            w.write("\n");
        }
        script.setExecutable(true);
        return script;
    }
}