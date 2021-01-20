package net.thevpc.nuts;

import java.util.Collection;
import java.util.List;

/**
 * @category Format
 */
public interface NutsTextNodeBuilder extends NutsString {
    NutsTextNodeWriteConfiguration getConfiguration();

    NutsTextNodeBuilder setConfiguration(NutsTextNodeWriteConfiguration writeConfiguration);

    NutsTextNodeBuilder appendCommand(String command, String args);

    NutsTextNodeBuilder appendCode(String lang, String text);

//    /**
//     * This method escapes all special characters that are interpreted by
//     * "nuts print format" o that this exact string is printed on
//     * such print streams When str is null, an empty string is return
//     * Then, styles are applied to the text if found
//     * @param text input string
//     * @param styles styles
//     * @return {@code this} instance
//     */
//    NutsTextNodeBuilder append(String text, NutsTextNodeStyle... styles);

    NutsTextNodeBuilder appendHash(Object text);

    NutsTextNodeBuilder appendHash(Object text, Object hash);

    NutsTextNodeBuilder append(Object text, NutsTextNodeStyle... styles);

    NutsTextNodeBuilder append(Object node);

    NutsTextNodeBuilder append(NutsTextNode node);

//    NutsTextNodeBuilder append(NutsString str);

//    NutsTextNodeBuilder append(NutsFormattable str);

    NutsTextNodeBuilder appendJoined(Object separator, Collection<?> others);

    NutsTextNodeBuilder appendAll(Collection<?> others);

    NutsTextNode build();

    String toString();
    int size();

}
