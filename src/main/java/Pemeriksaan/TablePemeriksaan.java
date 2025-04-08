/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Pemeriksaan;

import Components.CustomTable.CustomTable;
import Components.RoundedButton;
import DataBase.QueryExecutor;
import TransaksiDiagnosa.TransaksiDiagnosa;
import TransaksiDiagnosa.TransaksiDiagnosa.OnPemeriksaanUpdatedListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author asuna
 */
public class TablePemeriksaan extends JFrame implements OnPemeriksaanUpdatedListener {

    private DefaultTableModel model;
    private JScrollPane tableScrollPane;
    private JButton PeriksaButton;
    Object[][] data = {};
    Object[][] fullData = {};

    public static Object[] mapToArray(Map<String, Object> map) {
        // Create an Object array with the same size as the map
        Object[] array = new Object[map.size()];

        // Copy the values from the map into the array
        int i = 0;
        for (Object value : map.values()) {
            array[i++] = value;
        }

        return array;
    }

    private void refreshData() {
        // Clear the current data
        if (model != null) {
            model.setRowCount(0);
        }

        // Reload the data from the database (or wherever it comes from)
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_pemeriksaan(?)";
        Object[] parameter = new Object[]{"79f82701-9e35-11ef-944a-fc34974a9138"};
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, parameter);

        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                Object[] dataFromDatabase = new Object[]{result.get("no_antrian"), result.get("id_pasien"), result.get("nama_pasien"), result.get("status_antrian"), ""};
                // Create a new array with an additional row
                Object[][] newData = new Object[data.length + 1][];

                // Copy the old data to the new array
                System.arraycopy(data, 0, newData, 0, data.length);

                // Add the new row to the new array
                newData[data.length] = dataFromDatabase;

                // Send back to original
                data = newData;

                // Create a new array with an additional row
                Object[][] newDataFull = new Object[fullData.length + 1][];

                // Copy the old data to the new array
                System.arraycopy(fullData, 0, newDataFull, 0, fullData.length);

                // Add the new row to the new array
                newDataFull[fullData.length] = mapToArray(result);

                // Send back to original
                fullData = newDataFull;
            }
        }
    }

    public TablePemeriksaan() {
        // Define the original data for the table
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_pemeriksaan(?)";
        Object[] parameter = new Object[]{"79f82701-9e35-11ef-944a-fc34974a9138"};
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, parameter);

        refreshData();

        // Table and Model Data
        String[] columnNames = {"NO ANTRIAN", "ID PASIEN", "NAMA PASIEN", "STATUS", "AKSI"};
        model = new DefaultTableModel(data, columnNames) {
            public boolean isCellEditable(int row, int column) {
                return column == 4;  // Only "AKSI" column is editable
            }
        };

        CustomTable table = new CustomTable(model);
        table.getColumn("AKSI").setCellRenderer(new ActionCellRenderer());
        table.getColumn("AKSI").setCellEditor(new ActionCellEditor());

        tableScrollPane = new JScrollPane(table);

        setLayout(new BorderLayout());  // Set layout for JPanel
        setBackground(Color.white);

        // Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Color.WHITE);
        JLabel headerLabel = new JLabel("PEMERIKSAAN");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(headerLabel);

        // Filter the table based on the selected status
        add(headerPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
    }

    // Helper function to get the selected row in the table
    private int getSelectedRowInTable() {
        // Access the table from the JScrollPane's viewport
        CustomTable pemeriksaanTable = (CustomTable) tableScrollPane.getViewport().getView();
        return pemeriksaanTable.getSelectedRow();
    }

    @Override
    public void onPasienUpdated(String noAntrian, String idPasien, String nama_pasien, String status) {
        // Handle updating the patient in your model or UI
        int selectedRow = getSelectedRowInTable(); // Get the selected row in the table

        if (selectedRow != -1) {
            // Update the selected row with the new data from EditPasien
            model.setValueAt(noAntrian, selectedRow, 0); // Update name
            model.setValueAt(idPasien, selectedRow, 1); // Update age
            model.setValueAt(nama_pasien, selectedRow, 2); // Update gender
            model.setValueAt(status, selectedRow, 3); // Update address
            refreshData();
        }
    }

    // Renderer for "AKSI" column
    class ActionCellRenderer extends JPanel implements TableCellRenderer {

        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
            PeriksaButton = new RoundedButton("PERIKSA");
            PeriksaButton.setBackground(Color.GREEN);
            PeriksaButton.setForeground(Color.WHITE);
            PeriksaButton.setFocusPainted(false);
            add(PeriksaButton);
            setBackground(Color.WHITE);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Editor for "AKSI" column
    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {

        JPanel panel;
        int row;

        public ActionCellEditor() {
            panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));

            PeriksaButton = new RoundedButton("PERIKSA");
            PeriksaButton.setBackground(Color.GREEN);
            PeriksaButton.setForeground(Color.WHITE);
            PeriksaButton.setFocusPainted(false);
            PeriksaButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        new TransaksiDiagnosa(TablePemeriksaan.this, fullData[row]);
                    });
                }
            });
            add(PeriksaButton);
            setBackground(Color.WHITE);
            panel.add(PeriksaButton);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            return panel;
        }

        public Object getCellEditorValue() {
            return null;
        }
    }
}
