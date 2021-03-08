package net.thevpc.nuts;

import java.util.Collection;

/**
 * @category Format
 */
public interface NutsTextManager {

    NutsTextManager setSession(NutsSession session);

    NutsSession getSession();

    NutsTextNode blank();

    NutsTextNodeBuilder builder();

    NutsTextNode nodeFor(Object t);

    NutsTextNode plain(String t);

    NutsTextNode list(NutsTextNode... nodes);

    NutsTextNode list(Collection<NutsTextNode> nodes);

    NutsTextNode styled(String other, NutsTextNodeStyles decorations);

    NutsTextNode styled(NutsString other, NutsTextNodeStyles decorations);

    NutsTextNode styled(NutsTextNode other, NutsTextNodeStyles decorations);

    NutsTextNode styled(String other, NutsTextNodeStyle decorations);

    NutsTextNode styled(NutsString other, NutsTextNodeStyle decorations);

    NutsTextNode styled(NutsTextNode other, NutsTextNodeStyle decorations);

    NutsTextNode command(String command, String args);

    NutsTextNode command(String command);

    NutsTextNodeCode code(String lang, String text);

    NutsTitleNumberSequence createTitleNumberSequence();

    NutsTitleNumberSequence createTitleNumberSequence(String pattern);

    public NutsTextNode parse(String t);

    public NutsTextNodeParser parser();

}
