package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;
import net.thevpc.nuts.util.NRef;

import java.util.Arrays;

public class RemoveConfigCmd<C extends NdbConfig> extends NdbCmd<C> {
    public RemoveConfigCmd(NdbSupportBase<C> support, String... names) {
        super(support,"remove-config");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
    public void run(NApplicationContext appContext, NCmdLine commandLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        NSession session = commandLine.getSession();
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--config": {
                        readConfigNameOption(commandLine, session, name);
                        break;
                    }
                    default: {
                        session.configureLast(commandLine);
                    }
                }
            } else {
                if (name.isNull()) {
                    name.set(new AtName(commandLine.next().get(session).asString().get(session)));
                } else {
                    commandLine.throwUnexpectedArgument();
                }
            }
        }
        if (name.isNull()) {
            name.set(new AtName(""));
        }
        removeConfig(name.get());
    }

    private void removeConfig(AtName name) {
        NPath file = getSharedConfigFolder().resolve(asFullName(name) + NdbUtils.SERVER_CONFIG_EXT);
        if (file.exists()) {
            file.delete();
        }
    }


}
