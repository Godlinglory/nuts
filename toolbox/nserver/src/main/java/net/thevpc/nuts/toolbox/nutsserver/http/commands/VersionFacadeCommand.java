package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;

import java.io.IOException;

public class VersionFacadeCommand extends AbstractFacadeCommand {
    public VersionFacadeCommand() {
        super("version");
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        context.sendResponseText(200,
                context.getSession()
                        .id().builder()
                        .setRepository(context.getServerId())
                        .setGroupId("net.thevpc.nuts")
                        .setArtifactId("nuts-server")
                        .setVersion(context.getSession().getWorkspace().getRuntimeId().getVersion().toString())
                        .build().toString()
        );
    }
}
