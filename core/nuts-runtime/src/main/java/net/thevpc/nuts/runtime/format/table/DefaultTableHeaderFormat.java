package net.thevpc.nuts.runtime.format.table;

import net.thevpc.nuts.NutsPositionType;
import net.thevpc.nuts.NutsTableCellFormat;

/**
 *
 * @author vpc
 * @since 0.5.5
 */
public class DefaultTableHeaderFormat implements NutsTableCellFormat {

    public static final NutsTableCellFormat INSTANCE = new DefaultTableHeaderFormat();

    public DefaultTableHeaderFormat() {
    }

    @Override
    public String format(int row, int col, Object value) {
        return "==" + String.valueOf(value) + "==";
    }

    @Override
    public NutsPositionType getHorizontalAlign(int row, int col, Object value) {
        return NutsPositionType.HEADER;
    }

}
