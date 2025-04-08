package Absensi;

import java.awt.BorderLayout;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import DataBase.QueryExecutor;

public class AllAbsensi extends JFrame {

    private DefaultTableModel model;
    private JTable absensiTable;
    private Object[][] data = {};

    public AllAbsensi() {
        setTitle("All Absensi");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table data and columns setup
        String[] columnNames = new String[]{"ID", "Nama", "Waktu Masuk", "Waktu Pulang", "Status"};

        // Table model
        model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No cells are editable
            }
        };

        absensiTable = new JTable(model);

        // Adjust table column widths
        setTableColumnWidths(absensiTable);

        // Add table to scroll pane
        JScrollPane tableScrollPane = new JScrollPane(absensiTable);
        add(tableScrollPane, BorderLayout.CENTER);

        // Load the data from the database
        refreshTableData();
    }

    private void setTableColumnWidths(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
    }

    private void refreshTableData() {
        QueryExecutor executor = new QueryExecutor();
        String query = "SELECT a.id_absensi, u.nama_lengkap AS user_name, a.waktu_masuk, a.waktu_pulang, a.keterangan " +
                       "FROM absensi a " +
                       "JOIN user u ON a.id_user = u.id_user " +
                       "ORDER BY a.waktu_masuk DESC";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

        data = new Object[][]{};
        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                Object[] dataFromDatabase = new Object[]{
                    result.get("id_absensi"), result.get("user_name"), result.get("waktu_masuk"), result.get("waktu_pulang"), result.get("keterangan")
                };

                // Create a new array with an additional row
                Object[][] newData = new Object[data.length + 1][];

                // Copy the old data to the new array
                System.arraycopy(data, 0, newData, 0, data.length);

                // Add the new row to the new array
                newData[data.length] = dataFromDatabase;

                // Send back to original
                data = newData;
            }
        }

        model.setDataVector(data, new String[]{"ID", "Nama", "Waktu Masuk", "Waktu Pulang", "Status"});
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new AllAbsensi().setVisible(true);
        });
    }
}
