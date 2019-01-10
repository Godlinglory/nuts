package net.vpc.app.nuts.toolbox.nsh.term;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.toolbox.nsh.NutsConsoleContext;
import net.vpc.common.commandline.ArgumentCandidate;
import net.vpc.common.javashell.AutoCompleteCandidate;
import net.vpc.common.javashell.cmds.Command;
import net.vpc.common.strings.StringUtils;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.List;

class NutsJLineCompleter implements Completer {
    private final NutsWorkspace workspace;

    public NutsJLineCompleter(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public void complete(LineReader reader, final ParsedLine line, List<Candidate> candidates) {
        NutsConsoleContext nutsConsoleContext = (NutsConsoleContext) workspace.getUserProperties().get(NutsConsoleContext.class.getName());
        if (nutsConsoleContext != null) {
            if (line.wordIndex() == 0) {
                for (Command command : nutsConsoleContext.getShell().getCommands()) {
                    candidates.add(new Candidate(command.getName()));
                }
            } else {
                String commandName = line.words().get(0);
                int wordIndex = line.wordIndex() - 1;
                List<String> autoCompleteWords = new ArrayList<>(line.words().subList(1, line.words().size()));
                int x = commandName.length();
                String autoCompleteLine = line.line().substring(x);
                List<AutoCompleteCandidate> autoCompleteCandidates =
                        nutsConsoleContext.resolveAutoCompleteCandidates(commandName, autoCompleteWords, wordIndex, autoCompleteLine);
                for (Object cmdCandidate0 : autoCompleteCandidates) {
                    ArgumentCandidate cmdCandidate = (ArgumentCandidate) cmdCandidate0;
                    if (cmdCandidate != null) {
                        String value = cmdCandidate.getValue();
                        if (!StringUtils.isEmpty(value)) {
                            String display = cmdCandidate.getDisplay();
                            if (StringUtils.isEmpty(display)) {
                                display = value;
                            }
                            candidates.add(new Candidate(
                                    value,
                                    display,
                                    null, null, null, null, true
                            ));
                        }
                    }
                }
            }
        }
    }
}
