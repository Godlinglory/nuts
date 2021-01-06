package net.thevpc.nuts.toolbox.nutsserver.http.commands;

import net.thevpc.nuts.toolbox.nutsserver.AbstractFacadeCommand;
import net.thevpc.nuts.toolbox.nutsserver.FacadeCommandContext;
import net.thevpc.nuts.toolbox.nutsserver.util.ItemStreamInfo;
import net.thevpc.nuts.toolbox.nutsserver.util.MultipartStreamHelper;
import net.thevpc.nuts.toolbox.nutsserver.util.NutsServerUtils;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.toolbox.nutsserver.*;
import net.thevpc.nuts.toolbox.nutsserver.http.NutsHttpServletFacade;
import net.thevpc.common.io.IOUtils;
import net.thevpc.common.strings.StringUtils;

import java.io.IOException;
import java.util.Iterator;

public class SearchFacadeCommand extends AbstractFacadeCommand {
    private final NutsHttpServletFacade nutsHttpServletFacade;

    public SearchFacadeCommand(NutsHttpServletFacade nutsHttpServletFacade) {
        super("search");
        this.nutsHttpServletFacade = nutsHttpServletFacade;
    }

    @Override
    public void executeImpl(FacadeCommandContext context) throws IOException {
        //Content-type
        String boundary = context.getRequestHeaderFirstValue("Content-type");
        if (StringUtils.isBlank(boundary)) {
            context.sendError(400, "Invalid JShellCommandNode Arguments : " + getName() + " . Invalid format.");
            return;
        }
        MultipartStreamHelper stream = new MultipartStreamHelper(context.getRequestBody(), boundary,context.getWorkspace());
        boolean transitive = true;
        String root = null;
        String pattern = null;
        String js = null;
        for (ItemStreamInfo info : stream) {
            String name = info.resolveVarInHeader("Content-Disposition", "name");
            switch (name) {
                case "root":
                    root = IOUtils.loadString(info.getContent(), true).trim();
                    break;
                case "transitive":
                    transitive = Boolean.parseBoolean(IOUtils.loadString(info.getContent(), true).trim());
                    break;
                case "pattern":
                    pattern = IOUtils.loadString(info.getContent(), true).trim();
                    break;
                case "js":
                    js = IOUtils.loadString(info.getContent(), true).trim();
                    break;
            }
        }
        Iterator<NutsId> it = context.getWorkspace().search()
                .setSession(context.getSession())
                .setTransitive(transitive)
                .addScripts(js).addId(pattern).getResultIds().iterator();
//                    Writer ps = new OutputStreamWriter(context.getResponseBody());
        context.sendResponseText(200, NutsServerUtils.iteratorNutsIdToString(it));
    }
}
