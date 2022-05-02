package net.thevpc.nuts.runtime.standalone.format.table;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NutsPositionType;
import net.thevpc.nuts.format.NutsTableCellFormat;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;

/**
 *
 * @author thevpc
 * @since 0.5.5
 */
public class DefaultTableHeaderFormat implements NutsTableCellFormat {

    public static final NutsTableCellFormat INSTANCE = new DefaultTableHeaderFormat();

    public DefaultTableHeaderFormat() {
    }

    @Override
    public String format(int row, int col, Object value, NutsSession session) {
        return NutsTexts.of(session).builder().append(String.valueOf(value),
                NutsTextStyle.primary1()
                ).toString();
    }

    @Override
    public NutsPositionType getHorizontalAlign(int row, int col, Object value, NutsSession session) {
        return NutsPositionType.HEADER;
    }

}
