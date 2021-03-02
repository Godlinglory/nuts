package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.njob.model.*;
import net.thevpc.nuts.toolbox.njob.time.*;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NJobsSubCmd {
    private JobService service;
    private NutsApplicationContext context;
    private NutsWorkspace ws;
    private JobServiceCmd parent;

    public NJobsSubCmd(JobServiceCmd parent) {
        this.parent = parent;
        this.context = parent.context;
        this.service = parent.service;
        this.ws = parent.ws;
    }

    public void runJobAdd(NutsCommandLine cmd) {
        NJob t = new NJob();
        boolean list = false;
        boolean show = false;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "--list":
                case "-l": {
                    list = cmd.nextBoolean().getBooleanValue();
                    break;
                }
                case "--show":
                case "-s": {
                    show = cmd.nextBoolean().getBooleanValue();
                    break;
                }
                case "--time":
                case "--on":
                case "--start":
                case "-t": {
                    t.setStartTime(new TimeParser().parseInstant(cmd.nextString().getStringValue(), false));
                    break;
                }
                case "--at": {
                    t.setStartTime(new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getStringValue(), false));
                    break;
                }
                case "--for":
                case "--project":
                case "-p": {
                    t.setProject(cmd.nextString().getStringValue());
                    break;
                }
                case "--obs":
                case "-o": {
                    t.setObservations(cmd.nextString().getStringValue());
                    break;
                }
                case "--duration":
                case "-d": {
                    t.setDuration(TimePeriod.parse(cmd.nextString().getStringValue(), false));
                    break;
                }
                default: {
                    if (a.isNonOption()) {
                        if (t.getName() == null) {
                            t.setName(cmd.next().toString());
                        } else {
                            cmd.unexpectedArgument();
                        }
                    } else {
                        cmd.unexpectedArgument();
                    }
                }
            }
        }
        service.jobs().addJob(t);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("job %s (%s) added.\n",
                    context.getWorkspace().formats().text().styled(t.getId(), NutsTextNodeStyle.primary(5)),
                    t.getName()
            );
        }
        if (show) {
            runJobShow(ws.commandLine().create(t.getId()));
        }
        if (list) {
            runJobList(ws.commandLine().create());
        }
    }

    public void runJobUpdate(NutsCommandLine cmd) {
        List<NJob> jobs = new ArrayList<>();
        boolean list = false;
        boolean show = false;
        List<Consumer<NJob>> runLater = new ArrayList<>();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "--list":
                case "-l": {
                    list = cmd.nextBoolean().getBooleanValue();
                    break;
                }
                case "--show":
                case "-s": {
                    show = cmd.nextBoolean().getBooleanValue();
                    break;
                }
                case "--start": {
                    Instant v = new TimeParser().parseInstant(cmd.nextString().getStringValue(), false);
                    runLater.add(t -> t.setStartTime(v));
                    break;
                }
                case "-t":
                case "--on": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> t.setStartTime(TimePeriod.parseOpPeriodAsInstant(v, t.getStartTime(), true)));
                    break;
                }
                case "--at": {
                    Instant v = new TimeParser().setTimeOnly(true).parseInstant(cmd.nextString().getStringValue(), false);
                    runLater.add(t -> t.setStartTime(v));
                    break;
                }
                case "-d":
                case "--duration": {
                    TimePeriod v = TimePeriod.parse(cmd.nextString().getStringValue(), false);
                    runLater.add(t -> t.setDuration(v));
                    break;
                }
                case "-n":
                case "--name": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> t.setName(v));
                    break;
                }
                case "-p":
                case "--project": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> t.setProject(v));
                    break;
                }
                case "-o":
                case "--obs": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> t.setObservations(v));
                    break;
                }
                case "-o+":
                case "--obs+":
                case "+obs": {
                    String v = cmd.nextString().getStringValue();
                    runLater.add(t -> {
                        String s = t.getObservations();
                        if (s == null) {
                            s = "";
                        }
                        s = s.trim();
                        if (!s.isEmpty()) {
                            s += "\n";
                        }
                        s += v;
                        s = s.trim();
                        t.setObservations(s);
                    });
                    break;
                }
                default: {
                    if (a.isNonOption()) {
                        NJob t = findJob(cmd.next().toString(), cmd);
                        jobs.add(t);
                    } else {
                        cmd.unexpectedArgument();
                    }
                }
            }
        }
        if (jobs.isEmpty()) {
            cmd.throwError("job id expected");
        }
        for (NJob job : jobs) {
            for (Consumer<NJob> c : runLater) {
                c.accept(job);
            }
        }
        NutsFormatManager text = context.getWorkspace().formats();
        for (NJob job : new LinkedHashSet<>(jobs)) {
            service.jobs().updateJob(job);
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("job %s (%s) updated.\n",
                        text.text().styled(job.getId(), NutsTextNodeStyle.primary(5)),
                        text.text().styled(job.getName(), NutsTextNodeStyle.primary(1))
                );
            }
        }
        if (show) {
            for (NJob t : new LinkedHashSet<>(jobs)) {
                runJobList(ws.commandLine().create(t.getId()));
            }
        }
        if (list) {
            runJobList(ws.commandLine().create());
        }
    }

    public boolean runJobCommands(NutsCommandLine cmd) {
        if (cmd.next("aj", "ja", "a j", "j a", "add job", "jobs add") != null) {
            runJobAdd(cmd);
            return true;
        } else if (cmd.next("lj", "jl", "l j", "j l", "list jobs", "jobs list") != null) {
            runJobList(cmd);
            return true;
        } else if (cmd.next("rj", "jr", "jrm", "rmj", "j rm", "rm j", "j r", "r j", "remove job", "remove jobs", "jobs remove") != null) {
            runJobRemove(cmd);
            return true;
        } else if (cmd.next("uj", "ju", "j u", "u j", "update job", "update jobs", "jobs update", "jobs update") != null) {
            runJobUpdate(cmd);
            return true;
        } else if (cmd.next("js", "sj", "j s", "s j", "show job", "show jobs", "jobs show") != null) {
            runJobShow(cmd);
            return true;
        } else if (cmd.next("j", "jobs") != null) {
            if (cmd.next("--help") != null) {
                parent.showCustomHelp("njob-jobs");
            } else {
                runJobList(cmd);
            }
            return true;
        } else {
            return false;
        }
    }

    private void runJobRemove(NutsCommandLine cmd) {
        NutsFormatManager text = context.getWorkspace().formats();
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NJob t = findJob(a.toString(), cmd);
            if (service.jobs().removeJob(t.getId())) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("job %s removed.\n",
                            text.text().styled(a.toString(), NutsTextNodeStyle.primary(5))
                    );
                }
            } else {
                context.getSession().out().printf("job %s %s.\n",
                        text.text().styled(a.toString(), NutsTextNodeStyle.primary(5)),
                        text.text().styled("not found", NutsTextNodeStyle.error())
                );
            }
        }

    }


    private void runJobShow(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next();
            NJob job = findJob(a.toString(), cmd);
            if (job == null) {
                context.getSession().out().printf("```kw %s```: ```error not found```.\n",
                        a.toString()
                );
            } else {
                context.getSession().out().printf("```kw %s```:\n",
                        job.getId()
                );
                String prefix = "\t                    ";
                context.getSession().out().printf("\t```kw2 job name```      : %s:\n", parent.formatWithPrefix(job.getName(), prefix));
                String project = job.getProject();
                NProject p = service.projects().getProject(project);
                if (project == null || project.length() == 0) {
                    context.getSession().out().printf("\t```kw2 project```       : %s\n", "");
                } else {
                    context.getSession().out().printf("\t```kw2 project```       : %s (%s)\n", project, parent.formatWithPrefix(p == null ? "?" : p.getName(), prefix));
                }
                context.getSession().out().printf("\t```kw2 duration```      : %s\n", parent.formatWithPrefix(job.getDuration(), prefix));
                context.getSession().out().printf("\t```kw2 start time```    : %s\n", parent.formatWithPrefix(job.getStartTime(), prefix));
                context.getSession().out().printf("\t```kw2 duration extra```: %s\n", parent.formatWithPrefix(job.getInternalDuration(), prefix));
                context.getSession().out().printf("\t```kw2 observations```  : %s\n", parent.formatWithPrefix(job.getObservations(), prefix));
            }
        }

    }

    private void runJobList(NutsCommandLine cmd) {
        TimespanPattern hoursPerDay = TimespanPattern.WORK;
        int count = 100;
        NJobGroup groupBy = null;
        ChronoUnit countType = null;
        ChronoUnit timeUnit = null;
        Predicate<NJob> whereFilter = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.peek();
            switch (a.getStringKey()) {
                case "-w":
                case "--weeks": {
                    countType = ChronoUnit.WEEKS;
                    count = cmd.nextString().getIntValue();
                    break;
                }
                case "-m":
                case "--months": {
                    countType = ChronoUnit.MONTHS;
                    count = cmd.nextString().getIntValue();
                    break;
                }
                case "-l": {
                    countType = null;
                    count = cmd.nextString().getIntValue();
                    break;
                }
                case "-u":
                case "--unit": {
                    timeUnit = TimePeriod.parseUnit(cmd.nextString().getStringValue(), false);
                    break;
                }
                case "-g":
                case "--group":
                case "--groupBy":
                case "--groupby":
                case "--group-by": {
                    NutsArgument y = cmd.nextString();
                    switch (y.getStringValue()) {
                        case "p":
                        case "project": {
                            groupBy = NJobGroup.PROJECT_NAME;
                            break;
                        }
                        case "n":
                        case "name": {
                            groupBy = NJobGroup.NAME;
                            break;
                        }
                        case "s":
                        case "summary": {
                            groupBy = NJobGroup.SUMMARY;
                            break;
                        }
                        default: {
                            cmd.pushBack(y).unexpectedArgument("invalid value");
                        }
                    }
                    break;
                }
                case "-p":
                case "--project": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = parent.createProjectFilter(s);
                    Predicate<NJob> t = x -> sp.test(x.getProject());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "--name": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NJob> t = x -> sp.test(x.getName());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-b":
                case "--beneficiary": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NJob> t = x -> {
                        NProject project = service.projects().getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getBeneficiary());
                    };
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-c":
                case "--company": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<String> sp = parent.createStringFilter(s);
                    Predicate<NJob> t = x -> {
                        NProject project = service.projects().getProject(x.getProject());
                        return sp.test(project == null ? "" : project.getCompany());
                    };
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-d":
                case "--duration": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<TimePeriod> p = TimePeriod.parseFilter(s, false);
                    Predicate<NJob> t = x -> p.test(x.getDuration());
                    whereFilter = parent.appendPredicate(whereFilter, t);
                    break;
                }
                case "-t":
                case "--startTime":
                case "--start-time": {
                    String s = cmd.nextString().getStringValue();
                    Predicate<Instant> t = new TimeParser().parseInstantFilter(s, false);
                    whereFilter = parent.appendPredicate(whereFilter, x -> t.test(x.getStartTime()));
                    break;
                }
                default: {
                    cmd.unexpectedArgument();
                }
            }
        }
        Stream<NJob> r = service.jobs().findLastJobs(null, count, countType, whereFilter, groupBy, timeUnit, hoursPerDay);
        ChronoUnit timeUnit0 = timeUnit;
        if (context.getSession().isPlainTrace()) {
            NutsMutableTableModel m = ws.formats().table().createModel();
            NJobGroup finalGroupBy = groupBy;
            List<NJob> lastResults = new ArrayList<>();
            int[] index = new int[1];
            r.forEach(x -> {
                NutsString durationString = ws.formats().text().styled(String.valueOf(timeUnit0 == null ? x.getDuration() : x.getDuration().toUnit(timeUnit0, hoursPerDay)), NutsTextNodeStyle.keyword());
                index[0]++;
                lastResults.add(x);
                m.newRow().addCells(
                        (finalGroupBy != null) ?
                                new Object[]{
                                        parent.createHashId(index[0], -1),
                                        parent.getFormattedDate(x.getStartTime()),
                                        durationString,
                                        parent.getFormattedProject(x.getProject() == null ? "*" : x.getProject()),
                                        x.getName()

                                } : new Object[]{
                                parent.createHashId(index[0], -1),
                                ws.formats().text().styled(x.getId(), NutsTextNodeStyle.pale()),
                                parent.getFormattedDate(x.getStartTime()),
                                durationString,
                                parent.getFormattedProject(x.getProject() == null ? "*" : x.getProject()),
                                x.getName()

                        }
                );
            });
            context.getSession().setProperty("LastResults", lastResults.toArray(new NJob[0]));
            ws.formats().table()
                    .setBorder("spaces")
                    .setModel(m).println(context.getSession().out());
        } else {
            context.getSession().formatObject(r.collect(Collectors.toList())).print(context.getSession().out());
        }
    }

    private NJob findJob(String pid, NutsCommandLine cmd) {
        NJob t = null;
        if (pid.startsWith("#")) {
            int x = parent.parseIntOrFF(pid.substring(1));
            if (x >= 1) {
                Object lastResults = context.getSession().getProperty("LastResults");
                if (lastResults instanceof NJob[] && x <= ((NJob[]) lastResults).length) {
                    t = ((NJob[]) lastResults)[x - 1];
                }
            }
        }
        if (t == null) {
            t = service.jobs().getJob(pid);
        }
        if (t == null) {
            cmd.throwError("job not found: " + pid);
        }
        return t;
    }
}
