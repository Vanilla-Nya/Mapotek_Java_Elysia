/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Pemeriksaan;

import Components.CustomTable.CustomTable;
import Components.RoundedButton;
import Components.ShowModalCenter;
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
    private String uuid = "";
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

    public void refreshTableData() {
        // Clear the existing data
        data = new Object[][]{};
        fullData = new Object[][]{};

        // Fetch the latest data from the database
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_pemeriksaan()"; // Query dengan parameter
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{}); // Hapus parameter

        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                // Data untuk tabel
                Object[] dataForTable = new Object[]{
                    result.get("no_antrian"),
                    result.get("id_pasien"),
                    result.get("nama_pasien"),
                    result.get("status_antrian"),
                    "" // Kolom aksi
                };

                // Tambahkan data ke tabel
                Object[][] newData = new Object[data.length + 1][];
                System.arraycopy(data, 0, newData, 0, data.length);
                newData[data.length] = dataForTable;
                data = newData;

                // Data lengkap untuk TransaksiDiagnosa
                Object[] dataForTransaksiDiagnosa = new Object[]{
                    result.get("id_antrian"),         // 0
                    result.get("no_antrian"),         // 1
                    result.get("id_pasien"),          // 2
                    result.get("nama_pasien"),        // 3
                    result.get("status_antrian"),     // 4
                    result.get("umur"),               // 5
                    result.get("jenis_kelamin_pasien"),// 6
                    result.get("tanggal_antrian"),    // 7
                    result.get("jam_antrian"),        // 8
                    result.get("id_user"),            // 9
                    result.get("nama_user"),          // 10
                    result.get("role_user")           // 11
                };

                // Tambahkan data lengkap ke fullData
                Object[][] newDataFull = new Object[fullData.length + 1][];
                System.arraycopy(fullData, 0, newDataFull, 0, fullData.length);
                newDataFull[fullData.length] = dataForTransaksiDiagnosa;
                fullData = newDataFull;
            }
        }

        // Update the table model with the refreshed data
        model.setDataVector(data, new String[]{"NO ANTRIAN", "ID PASIEN", "NAMA PASIEN", "STATUS", "AKSI"});

        // Reapply the renderer and editor for the "AKSI" column
        CustomTable table = (CustomTable) tableScrollPane.getViewport().getView();
        table.getColumn("AKSI").setCellRenderer(new ActionCellRenderer());
        table.getColumn("AKSI").setCellEditor(new ActionCellEditor());
    }

    private void refreshData() {
        // Clear the current data
        if (model != null) {
            model.setRowCount(0);
        }

        // Reload the data from the database
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_pemeriksaan(   )"; // Query dengan parameter
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{}); // Hapus parameter

        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                // Data untuk tabel
                Object[] dataForTable = new Object[]{
                    result.get("no_antrian"),
                    result.get("id_pasien"),
                    result.get("nama_pasien"),
                    result.get("status_antrian"),
                    "" // Kolom aksi
                };

                // Tambahkan data ke tabel
                Object[][] newData = new Object[data.length + 1][];
                System.arraycopy(data, 0, newData, 0, data.length);
                newData[data.length] = dataForTable;
                data = newData;

                // Data lengkap untuk TransaksiDiagnosa
                Object[] dataForTransaksiDiagnosa = new Object[]{
                    result.get("id_antrian"),         // 0
                    result.get("no_antrian"),         // 1
                    result.get("id_pasien"),          // 2
                    result.get("nama_pasien"),        // 3
                    result.get("status_antrian"),     // 4
                    result.get("umur"),               // 5
                    result.get("jenis_kelamin_pasien"),// 6
                    result.get("tanggal_antrian"),    // 7
                    result.get("id_satusehat"),        // 8
                    result.get("jam_antrian"),        // 9
                    result.get("id_user"),            // 10
                    result.get("nama_user"),          // 11
                    result.get("role_user")           // 12
                };

                // Tambahkan data lengkap ke fullData
                Object[][] newDataFull = new Object[fullData.length + 1][];
                System.arraycopy(fullData, 0, newDataFull, 0, fullData.length);
                newDataFull[fullData.length] = dataForTransaksiDiagnosa;
                fullData = newDataFull;
            }
        }
    }

    public TablePemeriksaan() {
        // Define the original data for the table
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_pemeriksaan()"; // Query dengan parameter
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{}); // Hapus parameter

        refreshData();

        // Table and Model Data
        String[] columnNames = {"NO ANTRIAN", "ID PASIEN", "NAMA PASIEN", "STATUS", "AKSI"};
        model = new DefaultTableModel(data, columnNames) {
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only "AKSI" column is editable
            }
        };

        CustomTable table = new CustomTable(model);
        table.getColumn("AKSI").setCellRenderer(new ActionCellRenderer());
        table.getColumn("AKSI").setCellEditor(new ActionCellEditor());

        tableScrollPane = new JScrollPane(table);

        setLayout(new BorderLayout()); // Set layout for JPanel
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

            JButton periksaButton = new RoundedButton("PERIKSA");
            periksaButton.setBackground(Color.GREEN);
            periksaButton.setForeground(Color.WHITE);
            periksaButton.setFocusPainted(false);
            periksaButton.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    // Ambil data lengkap dari fullData[row]
                    Object[] dataForTransaksiDiagnosa = fullData[row];
                    String idSatusehatPasien = dataForTransaksiDiagnosa[8] != null ? dataForTransaksiDiagnosa[8].toString() : null;
                    String namaPasien = dataForTransaksiDiagnosa[3].toString();
                    System.out.println(namaPasien);

                    // Ambil id_satusehat dokter dari session
                    Global.UserSessionCache cache = new Global.UserSessionCache();
                    String idSatusehatDokter = cache.getIdSatusehat();
                    String namaDokter = cache.getusername(); // atau ambil dari dataForTransaksiDiagnosa[10] jika ingin nama dari tabel

                    // Validasi dan kirim Encounter ke SATUSEHAT
                    if (idSatusehatPasien == null || idSatusehatPasien.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "Pasien belum terdaftar di SATUSEHAT. Encounter tidak dikirim ke SATUSEHAT.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    } else if (idSatusehatDokter == null || idSatusehatDokter.isEmpty()) {
                        JOptionPane.showMessageDialog(panel, "User dokter belum terdaftar di SATUSEHAT. Tidak bisa melakukan pemeriksaan!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        boolean encounterSuccess = API.EncounterSatusehatApi.createEncounter(
                            idSatusehatPasien, namaPasien, idSatusehatDokter, namaDokter
                        );
                        System.out.println("Encounter success: " + encounterSuccess);
                        if (encounterSuccess) {
                            JOptionPane.showMessageDialog(panel, "Encounter ke SATUSEHAT berhasil dikirim.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(panel, "Encounter ke SATUSEHAT gagal.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    // Tampilkan TransaksiDiagnosa sebagai modal
                    TransaksiDiagnosa transaksiDiagnosa = new TransaksiDiagnosa(TablePemeriksaan.this, dataForTransaksiDiagnosa[0].toString(), dataForTransaksiDiagnosa);
                    ShowModalCenter.showCenterModal(
                        (JFrame) SwingUtilities.getWindowAncestor(panel),
                        transaksiDiagnosa,
                        true
                    );
                });
            });
            panel.add(periksaButton);
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
