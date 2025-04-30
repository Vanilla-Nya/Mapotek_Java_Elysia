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

        // Reload the data from the database
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_pemeriksaan(?)";
        Object[] parameter = new Object[]{"79f82701-9e35-11ef-944a-fc34974a9138"}; // Sesuaikan UUID
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, parameter);

        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                // Ambil data yang akan ditampilkan di tabel
                Object[] dataFromDatabase = new Object[]{
                    result.get("no_antrian"), // Kolom 0
                    result.get("id_pasien"), // Kolom 1
                    result.get("nama_pasien"), // Kolom 2
                    result.get("status_antrian"), // Kolom 3
                    "" // Placeholder untuk kolom "AKSI"
                };

                // Tambahkan data ke tabel
                Object[][] newData = new Object[data.length + 1][];
                System.arraycopy(data, 0, newData, 0, data.length);
                newData[data.length] = dataFromDatabase;
                data = newData;

                // Tambahkan data lengkap (termasuk id_antrian) ke fullData
                Object[] fullRowData = new Object[]{
                    result.get("id_antrian"),      // Indeks 0
                    result.get("no_antrian"),      // Indeks 1
                    result.get("id_pasien"),       // Indeks 2
                    result.get("nama_pasien"),     // Indeks 3
                    result.get("status_antrian"),  // Indeks 4
                    result.get("jenis_kelamin_pasien"), // Indeks 5
                    result.get("umur"),            // Indeks 6
                    result.get("alamat_pasien"),   // Indeks 7
                    result.get("id_user"),         // Indeks 8
                    result.get("nama_user")        // Indeks 9
                };

                Object[][] newDataFull = new Object[fullData.length + 1][];
                System.arraycopy(fullData, 0, newDataFull, 0, fullData.length);
                newDataFull[fullData.length] = fullRowData;
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
                    // Ambil data dari fullData berdasarkan baris yang dipilih
                    Object[] selectedRowData = fullData[row];

                    // Validasi data
                    if (selectedRowData == null || selectedRowData.length < 10) {
                        System.err.println("Data tidak lengkap atau null.");
                        JOptionPane.showMessageDialog(null, "Data tidak lengkap. Tidak dapat melanjutkan.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String idAntrian = selectedRowData[0].toString(); // Indeks 0 adalah id_antrian
                    System.out.println("ID Antrian: " + idAntrian);

                    // Panggil form TransaksiDiagnosa dengan data yang relevan
                    SwingUtilities.invokeLater(() -> {
                        new TransaksiDiagnosa(TablePemeriksaan.this, selectedRowData);
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
