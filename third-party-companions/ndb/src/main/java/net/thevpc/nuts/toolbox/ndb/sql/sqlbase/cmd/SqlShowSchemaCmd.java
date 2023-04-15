package net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;
import net.thevpc.nuts.toolbox.ndb.sql.util.SqlDB;
import net.thevpc.nuts.toolbox.ndb.sql.util.SqlHelper;
import net.thevpc.nuts.util.NRef;

import java.util.Arrays;

public class SqlShowSchemaCmd<C extends NdbConfig> extends NdbCmd<C> {
    public SqlShowSchemaCmd(NdbSupportBase<C> support, String... names) {
        super(support, "show-schema");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
    public void run(NSession session, NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        C otherOptions = createConfigInstance();
        ExtendedQuery eq = new ExtendedQuery(getName());
        NRef<NPath> file = NRef.ofNull();
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get(session).key()) {
                    case "--config": {
                        readConfigNameOption(cmdLine, session, name);
                        break;
                    }
                    case "--long": {
                        cmdLine.withNextFlag((v, a, s) -> eq.setLongMode(v));
                        break;
                    }
                    case "--file": {
                        cmdLine.withNextEntryValue((v, a, s) -> file.set(NPath.of(v.toString(), session)));
                        break;
                    }
                    default: {
                        fillOptionLast(cmdLine, otherOptions);
                    }
                }
            } else {
                cmdLine.throwUnexpectedArgument();
            }
        }
        //if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
        //    commandLine.throwMissingArgumentByName("--dbname");
        //}
        C options = loadFromName(name, otherOptions);
        support.revalidateOptions(options);
        runShowSchema(eq, options, file.get(), session);
    }


    protected void runShowSchema(ExtendedQuery eq, C options, NPath path, NSession session) {
        SqlDB sqlDB = SqlHelper.computeSchema(eq, (SqlSupport<C>) getSupport(), options, session);
        if (path == null) {
            session.out().println(sqlDB);
        } else {
            NElements.of(session).setContentType(session.getOutputFormat()).setNtf(false).print(path);
        }
    }


}
