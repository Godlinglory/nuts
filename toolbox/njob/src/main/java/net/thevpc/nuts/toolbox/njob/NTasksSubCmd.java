package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.format.NutsMutableTableModel;
import net.thevpc.nuts.format.NutsTableFormat;
import net.thevpc.nuts.text.NutsTextBuilder;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.toolbox.njob.model.*;
import net.thevpc.nuts.toolbox.njob.time.TimeParser;
import net.thevpc.nuts.toolbox.njob.time.TimePeriod;
import net.thevpc.nuts.toolbox.njob.time.TimespanPattern;

import javax.xml.crypto.Data;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NTasksSubCmd {

    private JobService service;
    private NutsApplicationContext context;
    private NutsSession session;
    private JobServiceCmd parent;

    public NTasksSubCmd(JobServiceCmd parent) {
        this.parent = parent;
        this.context = parent.context;
        this.service = parent.service;
        this.session = parent.session;
    }

    public void runTaskAdd(NutsCommandLine cmd) {
        boolean list = false;
        boolean show = false;
        boolean nameVisited = false;
        NutsArgument a;
        List<Consumer<NTask>> runLater = new ArrayList<>();
        while (cmd.hasNext()) {
            if ((a = cmd.nextBoolean("--list", "-l").orNull()) != null) {
                list = a.getBooleanValue().get(session);
            } else if ((a = cmd.nextBoolean("--show", "-s").orNull()) != null) {
                show = a.getBooleanValue().get(session);
            } else if ((a = cmd.nextString("--on", "--due", "-t").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> t.setDueTime(new TimeParser().parseInstant(s, false)));
            } else if ((a = cmd.nextString("--at").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> t.setDueTime(new TimeParser().setTimeOnly(true).parseInstant(s, false)));
            } else if ((a = cmd.nextString("--start").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> t.setStartTime(new TimeParser().parseInstant(s, false)));
            } else if ((a = cmd.nextString("--end").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> t.setEndTime(new TimeParser().parseInstant(s, false)));
            } else if ((a = cmd.nextString("--for").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> {
                    Instant u = new TimeParser().parseInstant(s, true);
                    if (u != null) {
                        t.setDueTime(u);
                    } else {
                        t.setProject(s);
                    }
                });
            } else if ((a = cmd.nextString("-p", "--project").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> t.setProject(s));
            } else if ((a = cmd.nextString("-n", "--name").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> t.setName(s));
            } else if ((a = cmd.nextString("-f", "--flag").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> {
                    String v = s;
                    NFlag f = null;
                    if ("random".equalsIgnoreCase(v)) {
                        f = NFlag.values()[(int) (Math.random() * NFlag.values().length)];
                    } else {
                        f = NFlag.valueOf(v.toUpperCase());
                    }
                    t.setFlag(f);
                });
            } else if ((a = cmd.nextString("-j", "--job").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> {
                    String jobId = s;
                    NJob job = service.jobs().getJob(jobId);
                    if (job == null) {
                        cmd.throwError(NutsMessage.ofCstyle("invalid job %s", jobId), session);
                    }
                    t.setJobId(job.getId());
                });
            } else if ((a = cmd.nextString("-T", "--parent").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> {
                    String taskId = s;
                    NTask parentTask = service.tasks().getTask(taskId);
                    if (parentTask == null) {
                        cmd.throwError(NutsMessage.ofCstyle("invalid parent task %s", taskId), session);
                    }
                    t.setParentTaskId(parentTask.getId());
                });
            } else if ((a = cmd.nextString("-P", "--priority").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> {
                    String v = s;
                    NPriority p = NPriority.NORMAL;
                    if (v.equalsIgnoreCase("higher")) {
                        p = p.higher();
                    } else if (v.equalsIgnoreCase("lower")) {
                        p = p.lower();
                    } else {
                        p = NPriority.valueOf(v.toLowerCase());
                    }
                    t.setPriority(p);
                });
            } else if ((a = cmd.nextString("-o", "--obs").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> {
                    t.setObservations(s);
                });
            } else if ((a = cmd.nextString("-d", "--duration").orNull()) != null) {
                String s = a.getStringValue().get(session);
                runLater.add(t -> {
                    t.setDuration(TimePeriod.parse(s, true));
                });
            } else if ((a = cmd.next("--wip").orNull()) != null) {
                runLater.add(t -> {
                    t.setStatus(NTaskStatus.WIP);
                });
            } else if ((a = cmd.next("--done").orNull()) != null) {
                runLater.add(t -> t.setStatus(NTaskStatus.DONE));
            } else if ((a = cmd.next("--cancel").orNull()) != null) {
                runLater.add(t -> t.setStatus(NTaskStatus.CANCELLED));
            } else if ((a = cmd.next("--todo").orNull()) != null) {
                runLater.add(t -> t.setStatus(NTaskStatus.TODO));
            } else if ((a = cmd.next("--high").orNull()) != null) {
                runLater.add(t -> t.setPriority(NPriority.HIGH));
            } else if ((a = cmd.next("--critical").orNull()) != null) {
                runLater.add(t -> t.setPriority(NPriority.CRITICAL));
            } else if ((a = cmd.next("--normal").orNull()) != null) {
                runLater.add(t -> t.setPriority(NPriority.NORMAL));
            } else {
                if (cmd.peek().get(session).isNonOption() && !nameVisited) {
                    String n = cmd.next("name").flatMap(NutsValue::asString).get(session);
                    runLater.add(t -> t.setName(n));
                } else {
                    cmd.throwUnexpectedArgument(session);
                }
            }
        }
        if (cmd.isExecMode()) {
            NTask t = new NTask();
            for (Consumer<NTask> c : runLater) {
                c.accept(t);
            }
            service.tasks().addTask(t);
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().printf("task %s (%s) added.\n",
                        NutsTexts.of(context.getSession()).ofStyled(t.getId(), NutsTextStyle.primary5()),
                        t.getName()
                );
            }
            if (show) {
                runTaskShow(NutsCommandLine.of(new String[]{t.getId()}));
            }
            if (list) {
                runTaskList(NutsCommandLine.of(new String[0]));
            }
        }
    }

    public void runTaskUpdate(NutsCommandLine cmd) {
        class Data{
            List<NTask> tasks = new ArrayList<>();
            boolean list = false;
            boolean show = false;
            List<Consumer<NTask>> runLater = new ArrayList<>();
        }
        Data d=new Data();
        while (cmd.hasNext()) {
            NutsArgument aa = cmd.peek().get(session);
            switch(aa.key()) {
                case "--list":
                case "-l": {
                    cmd.withNextBoolean((v, a, s) -> d.list=v,session);
                    break;
                }
                case "--show":
                case "-s": {
                    cmd.withNextBoolean((v, a, s) -> d.show=v,session);
                    break;
                }
                case "--start": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setStartTime(new TimeParser().parseInstant(v, false))),session);
                    break;
                }
                case "-t":
                case "--on":
                case "--due": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setDueTime(TimePeriod.parseOpPeriodAsInstant(v, t.getDueTime(), true))),session);
                    break;
                }
                case "--at": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setDueTime(new TimeParser().setTimeOnly(true).parseInstant(v, false))),session);
                    break;
                }
                case "--end": {
                    cmd.withNextString((v, a, s) -> d.runLater.add(t -> t.setEndTime(new TimeParser().parseInstant(v, false))),session);
                    break;
                }
                case "--wip": {
                    cmd.withNextTrue((v, a, s) -> d.runLater.add(t -> t.setStatus(NTaskStatus.WIP)),session);
                    break;
                }
                case "--done": {
                    cmd.withNextTrue((v, a, s) -> d.runLater.add(t -> t.setStatus(NTaskStatus.DONE)),session);
                    break;
                }
                case "--cancel": {
                    cmd.withNextTrue((v, a, s) -> d.runLater.add(t -> t.setStatus(NTaskStatus.CANCELLED)),session);
                    break;
                }
                case "--todo": {
                    cmd.withNextTrue((v, a, s) -> d.runLater.add(t -> t.setStatus(NTaskStatus.TODO)),session);
                    break;
                }
                case "--high": {
                    cmd.withNextTrue((v, a, s) -> d.runLater.add(t -> t.setPriority(NPriority.HIGH)),session);
                    break;
                }
                case "--critical": {
                    cmd.withNextTrue((v, a, s) -> d.runLater.add(t -> t.setPriority(NPriority.CRITICAL)),session);
                    break;
                }
                case "--normal": {
                    cmd.withNextTrue((v, a, s) -> d.runLater.add(t -> t.setPriority(NPriority.NORMAL)),session);
                    break;
                }
                case "++P":
                case "++prio":
                case "--prio++": {
                    cmd.withNextTrue((v, a, s) -> d.runLater.add(t -> t.setPriority((t.getPriority() == null ? NPriority.NORMAL : t.getPriority()).higher())),session);
                    break;
                }
                case "--P":
                case "--prio":
                case "--prio--": {
                    cmd.withNextString((v, a, s) -> {
                        if (!aa.getKey().asString().get(session).equals("--prio")) {
                            v = null;
                        }
                        if (v == null) {
                            d.runLater.add(t -> t.setPriority((t.getPriority() == null ? NPriority.NORMAL : t.getPriority()).lower()));
                        } else {
                            NPriority pp = NPriority.parse(v);
                            d.runLater.add(t -> t.setPriority(pp));
                        }
                    },session);
                    break;
                }
                case "--status": {
                    cmd.withNextString((v, a, s) -> {
                        d.runLater.add(t -> t.setStatus(NTaskStatus.parse(v)));
                    },session);
                    break;
                }
                case "-d":
                case "--duration": {
                    cmd.withNextString((v, a, s) -> {
                        d.runLater.add(t -> t.setDuration(TimePeriod.parse(v, false)));
                    },session);
                    break;
                }
                case "-n":
                case "--name": {
                    cmd.withNextString((v, a, s) -> {
                        d.runLater.add(t -> t.setName(v));
                    },session);
                    break;
                }
                case "-f":
                case "--flag": {
                    cmd.withNextString((v, a, s) -> {
                        NFlag f = NFlag.parse(v);
                        d.runLater.add(t -> t.setFlag(f));
                    },session);
                    break;
                }
                case "-j":
                case "--job": {
                    cmd.withNextString((v, a, s) -> {
                        NJob job = service.jobs().getJob(v);
                        if (job == null) {
                            cmd.throwError(NutsMessage.ofCstyle("invalid job %s", v), session);
                        }
                        d.runLater.add(t -> t.setJobId(job.getId()));
                    },session);
                    break;
                }
                case "-T":
                case "--parent": {
                    cmd.withNextString((v, a, s) -> {
                        NTask parentTask = service.tasks().getTask(v);
                        if (parentTask == null) {
                            cmd.throwError(NutsMessage.ofCstyle("invalid parent task %s", v), session);
                        }
                        d.runLater.add(t -> t.setParentTaskId(parentTask.getId()));
                    },session);
                    break;
                }
                case "-P":
                case "--priority": {
                    cmd.withNextString((v, a, s) -> {
                        d.runLater.add(t -> {
                            NPriority p = t.getPriority();
                            if (v.equalsIgnoreCase("higher")) {
                                p = p.higher();
                            } else if (v.equalsIgnoreCase("lower")) {
                                p = p.lower();
                            } else {
                                p = NPriority.parse(v);
                            }
                            t.setPriority(p);
                        });
                    },session);
                    break;
                }
                case "--for": {
                    cmd.withNextString((v, a, s) -> {
                        d.runLater.add(t -> {
                            Instant u = TimePeriod.parseOpPeriodAsInstant(v, t.getDueTime(), true);
                            if (u != null) {
                                t.setDueTime(u);
                            } else {
                                t.setProject(v);
                            }
                        });
                    },session);
                    break;
                }
                case "-p":
                case "--project": {
                    cmd.withNextString((v, a, s) -> {
                        d.runLater.add(t -> t.setProject(v));
                    },session);
                    break;
                }
                case "-o":
                case "--obs": {
                    cmd.withNextString((v, a, s) -> {
                        d.runLater.add(t -> t.setObservations(v));
                    },session);
                    break;
                }
                case "-o+":
                case "--obs+":
                case "+obs": {
                    cmd.withNextString((v, a, s) -> {
                        d.runLater.add(t -> {
                            String so = t.getObservations();
                            if (so == null) {
                                so = "";
                            }
                            so = so.trim();
                            if (!so.isEmpty()) {
                                so += "\n";
                            }
                            so += v;
                            so = so.trim();
                            t.setObservations(so);
                        });
                    },session);
                    break;
                }
                default: {
                    if (aa.isNonOption()) {
                        String pid = cmd.next().get(session).toString();
                        NTask t = findTask(pid, cmd);
                        d.tasks.add(t);
                    } else {
                        cmd.throwUnexpectedArgument(session);
                    }
                }
            }
        }
        if (d.tasks.isEmpty()) {
            cmd.throwError(NutsMessage.ofNtf("task id expected"), session);
        }
        if (cmd.isExecMode()) {
            for (NTask task : d.tasks) {
                for (Consumer<NTask> c : d.runLater) {
                    c.accept(task);
                }
            }
            NutsTexts text = NutsTexts.of(context.getSession());
            for (NTask task : new LinkedHashSet<>(d.tasks)) {
                service.tasks().updateTask(task);
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf("task %s (%s) updated.\n",
                            text.ofStyled(task.getId(), NutsTextStyle.primary5()),
                            text.ofStyled(task.getName(), NutsTextStyle.primary1())
                    );
                }
            }
            if (d.show) {
                for (NTask t : new LinkedHashSet<>(d.tasks)) {
                    runTaskList(NutsCommandLine.of(new String[]{t.getId()}));
                }
            }
            if (d.list) {
                runTaskList(NutsCommandLine.of(new String[0]));
            }
        }
    }

    private void runTaskList(NutsCommandLine cmd) {
        class Data{
            TimespanPattern hoursPerDay = TimespanPattern.WORK;
            int count = 100;
            NJobGroup groupBy = null;
            ChronoUnit countType = null;
            ChronoUnit timeUnit = null;
            Predicate<NTask> whereFilter = null;
            NTaskStatusFilter status = null;
        }
        Data d=new Data();
        while (cmd.hasNext()) {
            NutsArgument aa = cmd.peek().get(session);
            switch(aa.key()) {
                case "-w":
                case "--weeks": {
                    d.countType = ChronoUnit.WEEKS;
                    d.count = cmd.nextString().get(session).getValue().asInt().get(session);
                    break;
                }
                case "-m":
                case "--months": {
                    d.countType = ChronoUnit.MONTHS;
                    d.count = cmd.nextString().get(session).getValue().asInt().get(session);
                    break;
                }
                case "-l": {
                    d.countType = null;
                    d.count = cmd.nextString().get(session).getValue().asInt().get(session);
                    break;
                }
                case "-u":
                case "--unit": {
                    cmd.withNextString((v, a, s) -> {
                        d.timeUnit = TimePeriod.parseUnit(v, false);
                    },session);
                    break;
                }
                case "--todo": {
                    cmd.nextString();
                    d.status = NTaskStatusFilter.TODO;
                    break;
                }
                case "-a":
                case "--all": {
                    cmd.nextString();
                    d.status = NTaskStatusFilter.ALL;
                    break;
                }
                case "-r":
                case "--recent": {
                    cmd.nextString();
                    d.status = NTaskStatusFilter.RECENT;
                    break;
                }
                case "--cancelled": {
                    cmd.nextString();
                    d.status = NTaskStatusFilter.CANCELLED;
                    break;
                }
                case "--closed": {
                    cmd.nextString();
                    d.status = NTaskStatusFilter.CLOSED;
                    break;
                }
                case "--wip": {
                    cmd.nextString();
                    d.status = NTaskStatusFilter.WIP;
                    break;
                }
                case "-o":
                case "--open": {
                    cmd.nextString();
                    d.status = NTaskStatusFilter.OPEN;
                    break;
                }
                case "-g":
                case "--group":
                case "--groupBy":
                case "--groupby":
                case "--group-by": {
                    NutsArgument y = cmd.nextString().get(session);
                    switch (y.getStringValue().get(session)) {
                        case "p":
                        case "project": {
                            d.groupBy = NJobGroup.PROJECT_NAME;
                            break;
                        }
                        case "n":
                        case "name": {
                            d.groupBy = NJobGroup.NAME;
                            break;
                        }
                        case "s":
                        case "summary": {
                            d.groupBy = NJobGroup.SUMMARY;
                            break;
                        }
                        default: {
                            cmd.pushBack(y, session).throwUnexpectedArgument(NutsMessage.ofPlain("invalid value"), session);
                        }
                    }
                    break;
                }
                case "--project":
                case "-p": {
                    cmd.withNextString((v, a, s) -> {
                        Predicate<String> sp = parent.createProjectFilter(v);
                        Predicate<NTask> t = x -> sp.test(x.getProject());
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    },session);
                    break;
                }
                case "-n":
                case "--name": {
                    cmd.withNextString((v, a, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NTask> t = x -> sp.test(x.getName());
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    },session);
                    break;
                }
                case "-b":
                case "--beneficiary": {
                    cmd.withNextString((v, a, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NTask> t = x -> {
                            NProject project = service.projects().getProject(x.getProject());
                            return sp.test(project == null ? "" : project.getBeneficiary());
                        };
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    },session);
                    break;
                }
                case "-c":
                case "--company": {
                    cmd.withNextString((v, a, s) -> {
                        Predicate<String> sp = parent.createStringFilter(v);
                        Predicate<NTask> t = x -> {
                            NProject project = service.projects().getProject(x.getProject());
                            return sp.test(project == null ? "" : project.getCompany());
                        };
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    },session);
                    break;
                }
                case "-d":
                case "--duration": {
                    cmd.withNextString((v, a, s) -> {
                        Predicate<TimePeriod> p = TimePeriod.parseFilter(v, false);
                        Predicate<NTask> t = x -> p.test(x.getDuration());
                        d.whereFilter = parent.appendPredicate(d.whereFilter, t);
                    },session);
                    break;
                }
                case "-t":
                case "--startTime":
                case "--start-time": {
                    cmd.withNextString((v, a, s) -> {
                        Predicate<Instant> t = new TimeParser().parseInstantFilter(v, false);
                        d.whereFilter = parent.appendPredicate(d.whereFilter, x -> t.test(x.getStartTime()));
                    },session);
                    break;
                }
                default: {
                    cmd.throwUnexpectedArgument(session);
                }
            }
        }
        if (cmd.isExecMode()) {
            Stream<NTask> r = service.tasks().findTasks(d.status, null, d.count, d.countType, d.whereFilter, d.groupBy, d.timeUnit, d.hoursPerDay);

            if (context.getSession().isPlainTrace()) {
                NutsMutableTableModel m = NutsMutableTableModel.of(session);
                List<NTask> lastResults = new ArrayList<>();
                int[] index = new int[1];
                r.forEach(x -> {
                    index[0]++;
                    m.newRow().addCells(toTaskRowArray(x,
                            parent.createHashId(index[0], -1)
                    ));
                    lastResults.add(x);
                });
                context.getSession().setProperty("LastResults", lastResults.toArray(new NTask[0]));
                NutsTableFormat.of(session)
                        .setBorder("spaces")
                        .setValue(m).println();
            } else {
                context.getSession().out().printf(r.collect(Collectors.toList()));
            }
        }
    }

    private Object[] toTaskRowArray(NTask x, String index) {
        String project = x.getProject();
        NProject p = project == null ? null : service.projects().getProject(project);
        NTaskStatus s = x.getStatus();
        String dte0 = parent.getFormattedDate(x.getDueTime());
        NutsTextBuilder dte = NutsTexts.of(session).ofBuilder();
        if (s == NTaskStatus.CANCELLED || s == NTaskStatus.DONE) {
            dte.append(dte0, NutsTextStyle.pale());
        } else if (x.getDueTime() != null && x.getDueTime().compareTo(Instant.now()) < 0) {
            dte.append(dte0, NutsTextStyle.error());
        } else {
            dte.append(dte0, NutsTextStyle.keyword(2));
        }
        String projectName = p != null ? p.getName() : project != null ? project : "*";
        return new Object[]{
            index,
            NutsTexts.of(session).ofBuilder().append(x.getId(), NutsTextStyle.pale()),
            parent.getFlagString(x.getFlag()),
            parent.getStatusString(x.getStatus()),
            parent.getPriorityString(x.getPriority()),
            dte.immutable(),
            parent.getFormattedProject(projectName),
            x.getName()
        };
    }

    private void runTaskRemove(NutsCommandLine cmd) {
        NutsTexts text = NutsTexts.of(context.getSession());
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next().get(session);
            if (cmd.isExecMode()) {
                NTask t = findTask(a.toString(), cmd);
                if (service.tasks().removeTask(t.getId())) {
                    if (context.getSession().isPlainTrace()) {
                        context.getSession().out().printf("task %s removed.\n",
                                text.ofStyled(a.toString(), NutsTextStyle.primary5())
                        );
                    }
                } else {
                    context.getSession().out().printf("task %s %s.\n",
                            text.ofStyled(a.toString(), NutsTextStyle.primary5()),
                            text.ofStyled("not found", NutsTextStyle.error())
                    );
                }
            }
        }

    }

    private void runTaskShow(NutsCommandLine cmd) {
        while (cmd.hasNext()) {
            NutsArgument a = cmd.next().get(session);
            if (cmd.isExecMode()) {
                NTask task = findTask(a.toString(), cmd);
                if (task == null) {
                    context.getSession().out().printf("```kw %s```: ```error not found```.\n",
                            a.toString()
                    );
                } else {
                    context.getSession().out().printf("```kw %s```:\n",
                            task.getId()
                    );
                    String prefix = "\t                    ";
                    context.getSession().out().printf("\t```kw2 task name```     : %s\n", JobServiceCmd.formatWithPrefix(task.getName(), prefix));
                    context.getSession().out().printf("\t```kw2 status```        : %s\n", JobServiceCmd.formatWithPrefix(task.getStatus(), prefix));
                    context.getSession().out().printf("\t```kw2 priority```      : %s\n", JobServiceCmd.formatWithPrefix(task.getPriority(), prefix));
                    String project = task.getProject();
                    NProject p = service.projects().getProject(project);
                    if (project == null || project.length() == 0) {
                        context.getSession().out().printf("\t```kw2 project```       : %s\n", "");
                    } else {
                        context.getSession().out().printf("\t```kw2 project```       : %s (%s)\n", project, JobServiceCmd.formatWithPrefix((p == null ? "?" : p.getName()), prefix));
                    }
                    context.getSession().out().printf("\t```kw2 flag```          : %s\n", JobServiceCmd.formatWithPrefix(task.getFlag(), prefix));
                    context.getSession().out().printf("\t```kw2 parent id```     : %s\n", JobServiceCmd.formatWithPrefix(task.getParentTaskId(), prefix));
                    context.getSession().out().printf("\t```kw2 job id```        : %s\n", JobServiceCmd.formatWithPrefix(task.getJobId(), prefix));
                    context.getSession().out().printf("\t```kw2 due time```      : %s\n", JobServiceCmd.formatWithPrefix(task.getDueTime(), prefix));
                    context.getSession().out().printf("\t```kw2 start time```    : %s\n", JobServiceCmd.formatWithPrefix(task.getStartTime(), prefix));
                    context.getSession().out().printf("\t```kw2 end time```      : %s\n", JobServiceCmd.formatWithPrefix(task.getEndTime(), prefix));
                    context.getSession().out().printf("\t```kw2 duration```      : %s\n", JobServiceCmd.formatWithPrefix(task.getDuration(), prefix));
                    context.getSession().out().printf("\t```kw2 duration extra```: %s\n", JobServiceCmd.formatWithPrefix(task.getInternalDuration(), prefix));
                    context.getSession().out().printf("\t```kw2 creation time``` : %s\n", JobServiceCmd.formatWithPrefix(task.getCreationTime(), prefix));
                    context.getSession().out().printf("\t```kw2 modif. time```   : %s\n", JobServiceCmd.formatWithPrefix(task.getModificationTime(), prefix));
                    context.getSession().out().printf("\t```kw2 observations```  : %s\n", JobServiceCmd.formatWithPrefix(task.getObservations(), prefix));
                }
            }
        }
    }

    public boolean runTaskCommands(NutsCommandLine cmd) {
        if (cmd.next("a t", "t a", "ta", "at", "add task", "tasks add").isPresent()) {
            runTaskAdd(cmd);
            return true;
        } else if (cmd.next("t u", "u t", "tu", "ut", "update task", "tasks update").isPresent()) {
            runTaskUpdate(cmd);
            return true;
        } else if (cmd.next("l t", "t l", "lt", "tl", "list tasks", "tasks list").isPresent()) {
            runTaskList(cmd);
            return true;
        } else if (cmd.next("tr", "rt", "trm", "rmt", "t r", "r t", "t rm", "rm t", "remove task", "remove tasks", "rm task", "rm tasks",
                "tasks remove", "tasks rm").isPresent()) {
            runTaskRemove(cmd);
            return true;
        } else if (cmd.next("st", "ts", "s t", "t s", "show task", "show tasks", "tasks show").isPresent()) {
            runTaskShow(cmd);
            return true;
        } else if (cmd.next("t", "tasks").isPresent()) {
            if (cmd.next("--help") != null) {
                parent.showCustomHelp("njob-tasks");
            } else {
                runTaskList(cmd);
            }
            return true;
        }
        return false;
    }

    private NTask findTask(String pid, NutsCommandLine cmd) {
        NTask t = null;
        if (pid.startsWith("#")) {
            int x = NutsValue.of(pid.substring(1)).asInt().orElse(-1);
            if (x >= 1) {
                Object lastResults = context.getSession().getProperty("LastResults");
                if (lastResults instanceof NTask[] && x <= ((NTask[]) lastResults).length) {
                    t = ((NTask[]) lastResults)[x - 1];
                }
            }
        }
        if (t == null) {
            t = service.tasks().getTask(pid);
        }
        if (t == null) {
            cmd.throwError(NutsMessage.ofCstyle("task not found: %s", pid), session);
        }
        return t;
    }

}
