package net.thevpc.nuts.ext.term;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgumentCandidate;
import net.thevpc.nuts.cmdline.NCommandAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCommandLine;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

class NJLineCompleter implements Completer {

    private final NSession session;
    private final NJLineTerminal nutsJLineTerminal;

    public NJLineCompleter(NSession session, NJLineTerminal nutsJLineTerminal) {
        this.session = session;
        this.nutsJLineTerminal = nutsJLineTerminal;
    }

    @Override
    public void complete(LineReader reader, final ParsedLine line, List<Candidate> candidates) {
        NCommandAutoCompleteResolver autoCompleteResolver = nutsJLineTerminal.getAutoCompleteResolver();
        if (autoCompleteResolver != null) {

            NCommandLine commandLine = NCommandLine.of(line.words());
            if (line.words().size() > 0) {
                commandLine.setCommandName(line.words().get(0));
            }
            List<NArgumentCandidate> nArgumentCandidates = autoCompleteResolver.resolveCandidates(commandLine, line.wordIndex(), session);
            if (nArgumentCandidates != null) {
                for (NArgumentCandidate cmdCandidate : nArgumentCandidates) {
                    if (cmdCandidate != null) {
                        String value = cmdCandidate.getValue();
                        if (value != null && value.length() > 0) {
                            String display = cmdCandidate.getDisplay();
                            if (display == null || display.length() == 0) {
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
