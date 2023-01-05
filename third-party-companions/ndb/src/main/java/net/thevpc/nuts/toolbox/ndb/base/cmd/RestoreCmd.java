package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NRef;

import java.util.Arrays;

public class RestoreCmd<C extends NdbConfig> extends NdbCmd<C> {
    public RestoreCmd(NdbSupportBase<C> support, String... names) {
        super(support,"restore");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
    public void run(NApplicationContext appContext, NCommandLine commandLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        ExtendedQuery eq = new ExtendedQuery(getName());
        C otherOptions = createConfigInstance();
        NSession session = appContext.getSession();

        String status = "";
        while (commandLine.hasNext()) {
            switch (status) {
                case "": {
                    switch (commandLine.peek().get(session).key()) {
                        case "--config": {
                            readConfigNameOption(commandLine, session, name);
                            break;
                        }
                        case "--entity":
                        case "--table":
                        case "--collection": {
                            commandLine.withNextString((v, a, s) -> eq.setTable(v));
                            break;
                        }
                        case "--where": {
                            status = "--where";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        case "--set": {
                            status = "--set";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            fillOptionLast(commandLine, otherOptions);
                        }
                    }
                    break;
                }
                case "--where": {
                    switch (commandLine.peek().get(session).key()) {
                        case "--set": {
                            status = "--set";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            eq.getWhere().add(commandLine.next().get().toString());
                        }
                    }
                    break;
                }
                case "--set": {
                    switch (commandLine.peek().get(session).key()) {
                        case "--where": {
                            status = "--where";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            eq.getSet().add(commandLine.next().get().toString());
                        }
                    }
                    break;
                }
            }
        }
        if (NBlankable.isBlank(eq.getTable())) {
            commandLine.throwMissingArgumentByName("--table");
        }

        C options = loadFromName(name, otherOptions);
        support.revalidateOptions(options);
        runInsert(eq, options, session);
    }

    protected void runInsert(ExtendedQuery eq, C options, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid"));
    }


}
