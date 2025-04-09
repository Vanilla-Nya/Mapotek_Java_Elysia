package Utils;

import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import DataBase.QueryExecutor;

public class TableUtils {

    /**
     * Refreshes the table data by executing a query and updating the table model.
     *
     * @param model       The table model to update.
     * @param query       The SQL query to execute.
     * @param parameters  The parameters for the query.
     * @param columnNames The column names for the table.
     */
    public static void refreshTable(DefaultTableModel model, String query, Object[] parameters, String[] columnNames) {
        QueryExecutor executor = new QueryExecutor();
        List<Map<String, Object>> results = executor.executeSelectQuery(query, parameters);

        // Clear the current data in the table model
        model.setRowCount(0);

        // Populate the table model with the new data
        for (Map<String, Object> result : results) {
            Object[] rowData = new Object[columnNames.length];
            for (int i = 0; i < columnNames.length; i++) {
                rowData[i] = result.get(columnNames[i].toLowerCase()); // Match column names with query result keys
            }
            model.addRow(rowData);
        }
    }
}
