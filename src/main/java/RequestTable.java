import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

public class RequestTable extends AbstractTableModel {
    private final String[] columnNames = {"Parameter", "Values"};
    private final Map<String, Set<String>> data;

    public RequestTable(Map<String, Set<String>> map) {
        this.data = map;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    private List<String> sortedKeys() {
        List<String> keys = new ArrayList<>(data.keySet());
        Collections.sort(keys);
        return keys;
    }

    @Override
    public Object getValueAt(int row, int col) {
        String key = sortedKeys().get(row);
        if (col == 0) {
            return key;
        }
        Set<String> values = data.get(key);
        return values == null ? "" : String.join(", ", values);
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void refresh() {
        fireTableDataChanged();
    }
}
