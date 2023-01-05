package net.thevpc.nuts.toolbox.ndb;

import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.toolbox.ndb.sql.derby.NDerbyMain;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoMain;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.NMysqlMain;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgreSQLMain;

public class NdbMain implements NApplication {

    public static void main(String[] args) {
        new NdbMain().runAndExit(args);
    }

    @Override
    public void run(NApplicationContext context) {
        run(context.getCommandLine(),context);
    }

    public void run(NCommandLine commandLine,NApplicationContext context) {
        while (commandLine.hasNext()) {
            if (commandLine.next("mysql", "mariadb").isPresent()) {
                new NMysqlMain(context).run(context, commandLine);
                return;
            } else if (commandLine.next("derby").isPresent()) {
                new NDerbyMain(context).run(context, commandLine);
                return;
            } else if (commandLine.next("mongo", "mongodb").isPresent()) {
                new NMongoMain(context).run(context, commandLine);
                return;
            } else if (commandLine.next("postgres", "postgresql").isPresent()) {
                new NPostgreSQLMain(context).run(context, commandLine);
                return;
            } else {
                context.configureLast(commandLine);
            }
        }
        context.printHelp();
    }
}
