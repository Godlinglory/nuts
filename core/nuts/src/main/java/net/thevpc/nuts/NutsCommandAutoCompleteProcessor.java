package net.thevpc.nuts;

import java.util.List;

public interface NutsCommandAutoCompleteProcessor {
    List<NutsArgumentCandidate> resolveCandidates(NutsCommandLine commandline, int wordIndex,NutsWorkspace workspace);
}
