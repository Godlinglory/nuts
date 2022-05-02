package net.thevpc.nuts.toolbox.ntomcat.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPsInfo;

import java.util.Objects;

public class RunningTomcat {
    private String pid;
    private NutsPath home;
    private String base;
    private String argsLine;

    public RunningTomcat(NutsPsInfo r, NutsSession session) {
        pid =r.getPid();
        argsLine=r.getCommandLine();
        NutsCommandLine cmdline = NutsCommandLine.parseSystem(r.getCommandLine(),session)
                .get(session).setExpandSimpleOptions(false);
        NutsArgument a=null;
        while(cmdline.hasNext()){
            if((a=cmdline.nextString("-Dcatalina.home").orNull())!=null) {
                home = NutsPath.of(a.getStringValue().get(session),session);
            }else if((a=cmdline.nextString("-Dcatalina.base").orNull())!=null){
                base=a.getStringValue().get(session);
            }else{
                cmdline.skip();
            }
        }
    }


    public String getPid() {
        return pid;
    }

    public NutsPath getHome() {
        return home;
    }

    public String getBase() {
        return base;
    }

    public String getArgsLine() {
        return argsLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunningTomcat that = (RunningTomcat) o;
        return Objects.equals(pid, that.pid) &&
                Objects.equals(home, that.home) &&
                Objects.equals(base, that.base) &&
                Objects.equals(argsLine, that.argsLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid, home, base, argsLine);
    }

    @Override
    public String toString() {
        return "RunningTomcat{" +
                "id='" + pid + '\'' +
                ", home='" + home + '\'' +
                ", base='" + base + '\'' +
                ", argsLine='" + argsLine + '\'' +
                '}';
    }
}
