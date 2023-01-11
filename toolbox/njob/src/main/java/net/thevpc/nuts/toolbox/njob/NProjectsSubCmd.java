package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.format.NMutableTableModel;
import net.thevpc.nuts.format.NTableFormat;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.njob.model.NProject;
import net.thevpc.nuts.toolbox.njob.time.TimeParser;
import net.thevpc.nuts.toolbox.njob.time.WeekDay;
import net.thevpc.nuts.util.NRef;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NProjectsSubCmd {

    private JobService service;
    private NApplicationContext context;
    private NSession session;
    private JobServiceCmd parent;

    public NProjectsSubCmd(JobServiceCmd parent) {
        this.parent = parent;
        this.context = parent.context;
        this.service = parent.service;
        this.session = parent.session;
    }

    public void runProjectAdd(NCommandLine cmd) {
        NProject t = new NProject();
        NRef<Boolean> list = NRef.of(false);
        NRef<Boolean> show = NRef.of(false);
        while (cmd.hasNext()) {
            NArg aa = cmd.peek().get(session);
            switch (aa.key()) {
                case "--list":
                case "-l": {
                    cmd.withNextBoolean((v, a, s) -> list.set(v));
                    break;
                }
                case "--show":
                case "-s": {
                    cmd.withNextBoolean((v, a, s) -> show.set(v));
                    break;
                }
                case "-t":
                case "--start":
                case "--on": {
                    cmd.withNextString((v, a, s) -> t.setStartTime(new TimeParser().parseInstant(v, false)));
                    break;
                }
                case "--at": {
                    cmd.withNextString((v, a, s) -> t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(v, false)));
                    break;
                }
                case "-b":
                case "--beneficiary":
                case "--for": {
                    cmd.withNextString((v, a, s) -> t.setBeneficiary(v));
                    break;
                }
                case "-c":
                case "--company":
                case "--via": {
                    cmd.withNextString((v, a, s) -> t.setCompany(v));
                    break;
                }
                case "-1":
                case "--day1": {
                    cmd.withNextString((v, a, s) -> t.setStartWeekDay(WeekDay.parse(v)));
                    break;
                }
                case "-o":
                case "--obs": {
                    cmd.withNextString((v, a, s) -> t.setObservations(v));
                    break;
                }
                default: {
                    if (aa.isNonOption()) {
                        if (t.getName() == null) {
                            t.setName(cmd.next().get(session).toString());
                        } else {
                            cmd.throwUnexpectedArgument();
                        }
                    } else {
                        cmd.throwUnexpectedArgument();
                    }
                }
            }
        }
        if (cmd.isExecMode()) {
            service.projects().addProject(t);
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().println(NMsg.ofC("project %s (%s) added.",
                        NTexts.of(context.getSession()).ofStyled(t.getId(), NTextStyle.primary5()),
                        t.getName()
                ));
            }
            if (show.get()) {
                runProjectShow(NCommandLine.of(new String[]{t.getId()}));
            }
            if (list.get()) {
                runProjectList(NCommandLine.of(new String[0]));
            }
        }
    }

    public void runProjectUpdate(NCommandLine cmd) {
        class Data {
            List<NProject> projects = new ArrayList<>();
            boolean list = false;
            boolean show = false;
            String mergeTo = null;
            List<Consumer<NProject>> runLater = new ArrayList<>();
        }
        Data d = new Data();
        while (cmd.hasNext()) {
            NArg aa = cmd.peek().get(session);
            switch (aa.key()) {
                case "-l":
                case "--list": {
                    cmd.withNextBoolean((v, a, s) -> d.list = v);
                    break;
                }
                case "-s":
                case "--show": {
                    cmd.withNextBoolean((v, a, s) -> d.show = v);
                    break;
                }
                case "--on":
                case "--start": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setStartTime(new TimeParser().parseInstant(v, false))));
                    break;
                }
                case "--at": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(v, false))));
                    break;
                }
                case "--for":
                case "--beneficiary":
                case "-b": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setBeneficiary(v)));
                    break;
                }
                case "--company":
                case "--via":
                case "-c": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setCompany(v)));
                    break;
                }
                case "--day1":
                case "-1": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setStartWeekDay(WeekDay.parse(v))));
                    break;
                }
                case "--obs":
                case "-o": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setObservations(v)));
                    break;
                }
                case "--merge-to": {
                    cmd.withNextString((v, a, s) -> {
                        if (d.mergeTo != null) {
                            cmd.pushBack(a);
                            cmd.throwUnexpectedArgument();
                        } else {
                            d.mergeTo = v;
                        }
                    });
                    break;
                }
                case "++obs":
                case "+o": {
                    cmd.withNextString((v, a, s) -> {
                        d.runLater.add(t -> {
                            String ss = t.getObservations();
                            if (ss == null) {
                                ss = "";
                            }
                            ss = ss.trim();
                            if (!ss.isEmpty()) {
                                ss += "\n";
                            }
                            ss += v;
                            ss = ss.trim();
                            t.setObservations(ss);
                        });
                    });
                    break;
                }
                default: {
                    if (aa.isNonOption()) {
                        String pid = cmd.next().get(session).toString();
                        NProject t = findProject(pid, cmd);
                        d.projects.add(t);
                    } else {
                        cmd.throwUnexpectedArgument();
                    }
                }
            }
        }
        if (d.projects.isEmpty()) {
            cmd.throwError(NMsg.ofNtf("project name expected"));
        }
        if (cmd.isExecMode()) {
            NTexts text = NTexts.of(context.getSession());
            for (NProject project : d.projects) {
                for (Consumer<NProject> c : d.runLater) {
                    c.accept(project);
                }
                service.projects().updateProject(project);
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().println(NMsg.ofC("project %s (%s) updated.",
                            text.ofStyled(project.getId(), NTextStyle.primary5()),
                            text.ofStyled(project.getName(), NTextStyle.primary1())
                    ));
                }
            }
            if (d.mergeTo != null) {
                service.projects().mergeProjects(d.mergeTo, d.projects.stream().map(x -> x.getId()).toArray(String[]::new));
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().println(NMsg.ofC("projects merged to %s.",
                            NTexts.of(context.getSession())
                                    .ofStyled(d.mergeTo, NTextStyle.primary5())
                    ));
                }
            }
            if (d.show) {
                for (NProject t : new LinkedHashSet<>(d.projects)) {
                    runProjectShow(NCommandLine.of(new String[]{t.getId()}));
                }
            }
            if (d.list) {
                runProjectList(NCommandLine.of(new String[0]));
            }
        }
    }

    private void runProjectList(NCommandLine cmd) {
        final NRef<Predicate<NProject>> whereFilter = NRef.ofNull();
        while (cmd.hasNext()) {
            NArg aa = cmd.peek().get(session);
            switch (aa.key()) {
                case "-b":
                case "-beneficiary": {
                    cmd.withNextString((v, a, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NProject> t = x -> sp.test(x.getBeneficiary());
                        parent.appendPredicateRef(whereFilter, t);
                    });
                    break;
                }
                case "-c":
                case "-company": {
                    cmd.withNextString((v, a, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NProject> t = x -> sp.test(x.getCompany());
                        parent.appendPredicateRef(whereFilter, t);
                    });
                    break;
                }
                case "-n":
                case "--name": {
                    cmd.withNextString((v, a, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NProject> t = x -> sp.test(x.getName());
                        parent.appendPredicateRef(whereFilter, t);
                    });
                    break;
                }
                case "--unused": {
                    cmd.withNextBoolean((v, a, s) -> {
                        Predicate<NProject> t = x -> service.projects().isUsedProject(x.getId()) != v;
                        parent.appendPredicateRef(whereFilter, t);
                    });
                    break;
                }
                case "-t":
                case "--startTime":
                case "--start-time": {
                    cmd.withNextString((v, a, s) -> {
                        Predicate<Instant> t = new TimeParser().parseInstantFilter(v, false);
                        parent.appendPredicateRef(whereFilter, x -> t.test(x.getStartTime()));
                    });
                    break;
                }
                default: {
                    cmd.throwUnexpectedArgument();
                }
            }
        }
        if (cmd.isExecMode()) {

            Stream<NProject> r
                    = service.projects().findProjects().filter(whereFilter.isNull() ? x -> true : whereFilter.get())
                    .sorted(
                            (x, y) -> {
                                Instant s1 = x.getStartTime();
                                Instant s2 = y.getStartTime();
                                int v = s2.compareTo(s1);
                                if (v != 0) {
                                    return v;
                                }
                                return x.getName().compareTo(y.getName());
                            }
                    );

            if (context.getSession().isPlainTrace()) {
                NMutableTableModel m = NMutableTableModel.of(session);
                List<NProject> lastResults = new ArrayList<>();
                int[] index = new int[1];
                r.forEach(x -> {
                    Instant st = x.getStartTime();
                    String sts = "";
                    if (st != null) {
                        LocalDateTime d = LocalDateTime.ofInstant(st, ZoneId.systemDefault());
                        sts = d.getYear() + " " + d.getMonth().toString().toLowerCase().substring(0, 3);
                    }
                    lastResults.add(x);
                    index[0]++;
                    m.newRow().addCells(
                            parent.createHashId(index[0], -1),
                            x.getId(),
                            sts,
                            x.getCompany(),
                            x.getBeneficiary(),
                            parent.getFormattedProject(x.getName() == null ? "*" : x.getName())
                    );
                });
                context.getSession().setProperty("LastResults", lastResults.toArray(new NProject[0]));
                NTableFormat.of(session)
                        .setBorder("spaces")
                        .setValue(m).println(context.getSession().out());
            } else {
                context.getSession().out().print(r.collect(Collectors.toList()));
            }
        }
    }

    private void runProjectRemove(NCommandLine cmd) {
        NTexts text = NTexts.of(context.getSession());
        while (cmd.hasNext()) {
            NArg a = cmd.next().get(session);
            if (cmd.isExecMode()) {
                NProject t = findProject(a.toString(), cmd);
                NOutputStream out = context.getSession().out();
                if (service.projects().removeProject(t.getId())) {
                    if (context.getSession().isPlainTrace()) {
                        out.println(NMsg.ofC("project %s removed.",
                                text.ofStyled(a.toString(), NTextStyle.primary5())
                        ));
                    }
                } else {
                    out.println(NMsg.ofC("project %s %s.",
                            text.ofStyled(a.toString(), NTextStyle.primary5()),
                            text.ofStyled("not found", NTextStyle.error())
                    ));
                }
            }
        }

    }

    private void runProjectShow(NCommandLine cmd) {
        while (cmd.hasNext()) {
            NArg a = cmd.next().get(session);
            NProject project = findProject(a.toString(), cmd);
            NOutputStream out = context.getSession().out();
            if (project == null) {
                out.println(NMsg.ofC("```kw %s```: ```error not found```.",
                        a.toString()
                ));
            } else {
                out.println(NMsg.ofC("```kw %s```:",
                        project.getId()
                ));
                String prefix = "\t                    ";
                out.println(NMsg.ofC("\t```kw2 project name```  : %s", JobServiceCmd.formatWithPrefix(project.getName(), prefix)));
                out.println(NMsg.ofC("\t```kw2 beneficiary```   : %s", JobServiceCmd.formatWithPrefix(project.getBeneficiary(), prefix)));
                out.println(NMsg.ofC("\t```kw2 company```       : %s", JobServiceCmd.formatWithPrefix(project.getCompany(), prefix)));
                out.println(NMsg.ofC("\t```kw2 start time```    : %s", JobServiceCmd.formatWithPrefix(project.getStartTime(), prefix)));
                out.println(NMsg.ofC("\t```kw2 start week day```: %s", JobServiceCmd.formatWithPrefix(project.getStartWeekDay(), prefix)));
                out.println(NMsg.ofC("\t```kw2 observations```  : %s", JobServiceCmd.formatWithPrefix(project.getObservations(), prefix)));
            }
        }

    }

    private NProject findProject(String pid, NCommandLine cmd) {
        NProject t = null;
        if (pid.startsWith("#")) {
            int x = JobServiceCmd.parseIntOrFF(pid.substring(1));
            if (x >= 1) {
                Object lastResults = context.getSession().getProperty("LastResults");
                if (lastResults instanceof NProject[] && x <= ((NProject[]) lastResults).length) {
                    t = ((NProject[]) lastResults)[x - 1];
                }
            }
        }
        if (t == null) {
            t = service.projects().getProject(pid);
        }
        if (t == null) {
            cmd.throwError(NMsg.ofC("project not found: %s", pid));
        }
        return t;
    }

    public boolean runProjectCommands(NCommandLine cmd) {
        if (cmd.next("ap", "a p", "pa", "p a", "add project", "projects add").isPresent()) {
            runProjectAdd(cmd);
            return true;
        } else if (cmd.next("pu", "up", "p u", "u p", "update project", "projects update").isPresent()) {
            runProjectUpdate(cmd);
            return true;
        } else if (cmd.next("lp", "pl", "l p", "p l", "list projects", "projects list").isPresent()) {
            runProjectList(cmd);
            return true;
        } else if (cmd.next("rp", "rmp", "pr", "prm", "r p", "rm p", "p r", "p rm", "remove project", "remove projects", "rm project", "rm projects", "projects remove").isPresent()) {
            runProjectRemove(cmd);
            return true;
        } else if (cmd.next("ps", "sp", "s p", "p s", "show project", "show projects", "projects show").isPresent()) {
            runProjectShow(cmd);
            return true;
        } else if (cmd.next("p", "projects").isPresent()) {
            if (cmd.next("--help").isPresent()) {
                parent.showCustomHelp("njob-projects");
            } else {
                runProjectList(cmd);
            }
            return true;
        }
        return false;
    }

}
