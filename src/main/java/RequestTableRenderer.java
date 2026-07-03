import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class RequestTableRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        Component component = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        setToolTipText(value == null ? null : value.toString());
        return component;
    }
}