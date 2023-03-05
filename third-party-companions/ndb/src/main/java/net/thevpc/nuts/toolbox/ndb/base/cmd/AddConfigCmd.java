package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Arrays;

public class AddConfigCmd<C extends NdbConfig> extends NdbCmd<C> {
    public AddConfigCmd(NdbSupportBase<C> support, String... names) {
        super(support,"add-config");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
    public void run(NApplicationContext appContext, NCmdLine commandLine) {
        C options = createConfigInstance();
        NRef<Boolean> update = NRef.of(false);
        while (commandLine.hasNext()) {
            if (fillOption(commandLine, options)) {
                //
            } else if (
                    commandLine.withNextFlag((v, a, s) -> {
                        update.set(v);
                    }, "--update")
            ) {
            } else {
                appContext.configureLast(commandLine);
            }
        }
        options.setName(NStringUtils.trimToNull(options.getName()));
        if (NBlankable.isBlank(options.getName())) {
            options.setName("default");
        }

        NPath file = getSharedConfigFolder().resolve(asFullName(options.getName()) + NdbUtils.SERVER_CONFIG_EXT);
        NElements json = NElements.of(commandLine.getSession()).setNtf(false).json();
        if (file.exists()) {
            if (update.get()) {
                C old = json.parse(file, support.getConfigClass());
                String oldName = old.getName();
                old.setNonNull(options);
                old.setName(oldName);
                json.setValue(options).print(file);
            } else {
                throw new RuntimeException("already found");
            }
        } else {
            json.setValue(options).print(file);
        }
    }


}