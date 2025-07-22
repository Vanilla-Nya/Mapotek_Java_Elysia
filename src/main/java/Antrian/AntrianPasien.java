package Antrian;

import Components.CustomDialog;
import Components.CustomTable.CustomTable;
import Components.Dropdown;
import Components.RoundedButton;
import Components.ShowModalCenter;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import Transaksi.FormPembayaran;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class AntrianPasien extends JPanel {

    private DefaultTableModel model;
    private CustomTable table;
    public int role = 0;
    Object[][] data = {};
    java.util.List idList = new ArrayList<>();
    UserSessionCache cache = new UserSessionCache();
    String uuid = cache.getUUID();  

    public AntrianPasien() {
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_antrian(?)"; // Query dengan parameter
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{uuid}); // Hapus parameter

        String Query = "SELECT id_role FROM user_role WHERE id_user = ? ORDER BY id_role DESC LIMIT 1";
        Object[] userrole = new Object[]{uuid};
        java.util.List<Map<String, Object>> resultsPasien = executor.executeSelectQuery(Query, userrole);

        if (!resultsPasien.isEmpty()) {
            role = (int) resultsPasien.get(0).get("id_role");
        }

        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                Object[] dataFromDatabase = new Object[]{
                    result.get("tanggal_antrian"),
                    result.get("no_antrian"),
                    result.get("nama_pasien"),
                    result.get("status_antrian"),
                    ""
                };
                idList.add(result.get("id_antrian"));

                // Tambahkan data baru ke array
                Object[][] newData = new Object[data.length + 1][];
                System.arraycopy(data, 0, newData, 0, data.length);
                newData[data.length] = dataFromDatabase;
                data = newData;
            }
        }

        String[] columnNames;
        if (role == 1) {
            columnNames = new String[]{"TANGGAL ANTRIAN", "NO ANTRIAN", "NAMA PASIEN", "STATUS"};
        } else {
            columnNames = new String[]{"TANGGAL ANTRIAN", "NO ANTRIAN", "NAMA PASIEN", "STATUS", "AKSI"};
        }

        // Table and Model Data
        model = new DefaultTableModel(data, columnNames) {
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only "AKSI" column is editable
            }
        };

        // Create the JComboBox with options
        String[] states = {"All", "Belum Periksa", "Di Terima", "Sedang Diperiksa", "Melakukan Pembayaran", "Selesai"};

        table = new CustomTable(model);

        if (role != 1) {
            table.getColumn("AKSI").setCellRenderer(new ActionCellRenderer(model));
            table.getColumn("AKSI").setCellEditor(new ActionCellEditor(model));
            table.getColumn("AKSI").setMinWidth(150);
        }

        JScrollPane scrollPane = new JScrollPane(table);

        setLayout(new BorderLayout()); // Set layout for JPanel
        setBackground(Color.white);

        // Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Color.WHITE);
        JLabel headerLabel = new JLabel("ANTRIAN");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(headerLabel);

        Dropdown chooseStatus = new Dropdown(false, false, null);
        chooseStatus.setItems(java.util.List.of(states), false, false, null);

        chooseStatus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedStatus = (String) chooseStatus.getSelectedItem();
                filterTable(model, selectedStatus, data);
            }
        });

        JButton tambahButton = new RoundedButton("+ TAMBAH ANTRIAN BARU");
        tambahButton.setBackground(new Color(0, 153, 102));
        tambahButton.setForeground(Color.WHITE);
        tambahButton.setFont(new Font("Arial", Font.BOLD, 12));
        tambahButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ShowModalCenter.showCenterModal(
                    (JFrame) SwingUtilities.getWindowAncestor(AntrianPasien.this),
                    new TambahkanAntrian(model, AntrianPasien.this)
                );
            }
        });

        // Filter the table based on the selected status
        filterTable(model, "All", data); // Initially show all rows

        if (role != 1) {
            headerPanel.add(tambahButton);
        }
        headerPanel.add(chooseStatus);
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Method to filter the table based on the selected status
    private static void filterTable(DefaultTableModel model, String selectedStatus, Object[][] data) {
        // Reset the table model (remove all rows)
        model.setRowCount(0);

        // Add rows that match the selected status
        for (Object[] row : data) {
            String status = (String) row[3];  // Get the "Status" column value
            if (selectedStatus.equals("All") || selectedStatus.equals(status)) {
                model.addRow(row);  // Add row to table if it matches the selected status
            }
        }
    }

    public void refreshTableData() {
        // Clear the existing data
        data = new Object[][]{};
        idList.clear();

        // Define the column names based on the role
        String[] columnNames;
        if (role == 1) {
            columnNames = new String[]{"TANGGAL ANTRIAN", "NO ANTRIAN", "NAMA PASIEN", "STATUS"};
        } else {
            columnNames = new String[]{"TANGGAL ANTRIAN", "NO ANTRIAN", "NAMA PASIEN", "STATUS", "AKSI"};
        }

        // Update the table model with the refreshed data
        model.setDataVector(data, columnNames);

        // Reapply the button rendering and editing to the "AKSI" column if the role allows it
        if (role != 1) {
            table.getColumn("AKSI").setCellRenderer(new ActionCellRenderer(model));
            table.getColumn("AKSI").setCellEditor(new ActionCellEditor(model));
        }

        // Repaint and revalidate the table to reflect changes
        table.repaint();
        table.revalidate();
    }

    // Renderer for "AKSI" column
    class ActionCellRenderer extends JPanel implements TableCellRenderer {

        int row;
        DefaultTableModel model;

        public ActionCellRenderer(DefaultTableModel model) {
            this.model = model;
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
            setBackground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Clear previous buttons
            removeAll();

            // Get the status for the current row
            String status = (String) model.getValueAt(row, 3);

            // Create "TERIMA" button only if status is "Belum Periksa"
            if ("Belum Periksa".equals(status) && role != 1) {
                JButton terimaButton = new RoundedButton("TERIMA");
                terimaButton.setBackground(Color.GREEN);
                terimaButton.setForeground(Color.WHITE);
                terimaButton.setFocusPainted(false);
                terimaButton.addActionListener(e -> {
                    if (row >= 0 && row < idList.size()) {
                        String QueryUpdate = "UPDATE antrian SET status_antrian = ? WHERE id_antrian = ?";
                        Object[] parameterUpdate = new Object[]{"Diterima", idList.get(row)};
                        boolean isUpdateStatus = QueryExecutor.executeUpdateQuery(QueryUpdate, parameterUpdate);
                        if (isUpdateStatus) {
                            model.setValueAt("Diterima", row, 3);
                            terimaButton.setVisible(false);
                        }
                    }
                });
                add(terimaButton);
            }

            // Create "BAYAR" button only if status is "Sudah Diperiksa"
            if ("Selesai Diperiksa".equals(status) && role != 1) {
                JButton bayarButton = new RoundedButton("BAYAR");
                bayarButton.setBackground(Color.BLUE);
                bayarButton.setForeground(Color.WHITE);
                bayarButton.setFocusPainted(false);
                bayarButton.addActionListener(e -> {
                    if (row >= 0 && row < idList.size()) {
                        // Ambil data pasien dan ID antrian
                        Object[] patientData = getPatientData(row);
                        String idAntrian = idList.get(row).toString();

                        // Buat instance FormPembayaran
                        FormPembayaran formPembayaran = new FormPembayaran(patientData, idAntrian, status, AntrianPasien.this);

                        // Tampilkan FormPembayaran menggunakan ShowModalCenter
                        ShowModalCenter.showCenterModal(
                            (JFrame) SwingUtilities.getWindowAncestor(AntrianPasien.this), // Parent frame
                            formPembayaran // Panel yang akan ditampilkan
                        );
                    }
                });
                add(bayarButton);
            }

            if (role != 1) {
                JButton hapusButton = new RoundedButton("HAPUS");
                hapusButton.setBackground(new Color(255, 51, 51));
                hapusButton.setForeground(Color.WHITE);
                hapusButton.setFocusPainted(false);
                if (!"Selesai".equals(status)) {
                    add(hapusButton);
                }
            }

            // Revalidate to update the panel layout
            revalidate();
            return this;
        }
    }

    // Editor for "AKSI" column
    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {

        JPanel panel;
        int row;
        DefaultTableModel model;

        public ActionCellEditor(DefaultTableModel model) {
            this.model = model;
            panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Ensure row index is set properly
            if (row >= 0 && row < table.getRowCount()) {
                this.row = row;  // Set the row index when the cell enters editing mode
            } else {
                System.out.println("Invalid row index passed to cell editor");
            }
            this.row = row; // Set the row index when entering the cell editor

            // Clear previous buttons
            panel.removeAll();

            // Get the status for the current row
            String status = (String) model.getValueAt(row, 3);

            // Add "TERIMA" button only if status is "Belum Periksa"
            if ("Belum Periksa".equals(status) && role != 1) {
                JButton terimaButton = new RoundedButton("TERIMA");
                terimaButton.setBackground(Color.GREEN);
                terimaButton.setForeground(Color.WHITE);
                terimaButton.setFocusPainted(false);
                terimaButton.addActionListener(e -> {
                    if (row >= 0 && row < idList.size()) {
                        String QueryUpdate = "UPDATE antrian SET status_antrian = ? WHERE id_antrian = ?";
                        Object[] parameterUpdate = new Object[]{"Diterima", idList.get(row)};
                        boolean isUpdateStatus = QueryExecutor.executeUpdateQuery(QueryUpdate, parameterUpdate);
                        if (isUpdateStatus) {
                            model.setValueAt("Diterima", row, 3);
                            terimaButton.setVisible(false);
                        }
                    }
                });
                panel.add(terimaButton);
            }
            // Add "BAYAR" button only if status is "Sudah Diperiksa"
            if ("Selesai Diperiksa".equals(status) && role != 1) {
                JButton bayarButton = new RoundedButton("BAYAR");
                bayarButton.setBackground(Color.BLUE);
                bayarButton.setForeground(Color.WHITE);
                bayarButton.setFocusPainted(false);
                bayarButton.addActionListener(e -> {
                    if (row >= 0 && row < idList.size()) {
                        // Ambil data pasien dan ID antrian
                        Object[] patientData = getPatientData(row);
                        String idAntrian = idList.get(row).toString();

                        // Buat instance FormPembayaran
                        FormPembayaran formPembayaran = new FormPembayaran(patientData, idAntrian, status, AntrianPasien.this);

                        // Tampilkan FormPembayaran menggunakan ShowModalCenter
                        ShowModalCenter.showCenterModal(
                            (JFrame) SwingUtilities.getWindowAncestor(AntrianPasien.this), // Parent frame
                            formPembayaran // Panel yang akan ditampilkan
                        );
                    }
                });
                panel.add(bayarButton);
            }

            // Add "HAPUS" button
            JButton hapusButton = new RoundedButton("HAPUS");
            hapusButton.setBackground(new Color(255, 51, 51));
            hapusButton.setForeground(Color.WHITE);
            hapusButton.setFocusPainted(false);
            hapusButton.addActionListener((ActionEvent e) -> {
                // Create and show the confirmation dialog
                CustomDialog confirmDialog = new CustomDialog(null, "Apakah Anda yakin ingin menghapus pasien ini?", "Konfirmasi Penghapusan");
                // Get the user's response
                int response = confirmDialog.showDialog();

                // If the user clicks "Yes" (JOptionPane.YES_OPTION), proceed with deletion
                if (response == JOptionPane.YES_OPTION) {

                    // Check if the row index is valid before attempting to remove
                    if (row >= 0 && row < model.getRowCount()) {

                        // Check if the table was in an editing state and stop editing
                        if (table.isEditing()) {
                            table.getCellEditor().stopCellEditing();  // Stop editing the cell if it is being edited
                            System.out.println("Cell editing stopped.");
                        }

                        // Log the row index and row count before removal for debugging
                        System.out.println("Attempting to remove row: " + row);
                        String query = "UPDATE antrian SET is_deleted = ? WHERE id_antrian = ?";
                        boolean isDeleted = QueryExecutor.executeUpdateQuery(query, new Object[]{1, idList.get(row)});
                        if (isDeleted) {
                            // Proceed with row removal if index is valid
                            model.removeRow(row);
                            JOptionPane.showMessageDialog(null, "Update Success", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
                        }

                        // Refresh the table view after the row is removed
                        table.revalidate();
                        table.repaint();

                        // Handle edge case if the last row was removed
                        if (model.getRowCount() == 0) {
                            System.out.println("Last row deleted, table is empty.");
                        } else {
                            // After removing the last row, we might want to focus or highlight the new "last row"
                            int lastRowIndex = model.getRowCount() - 1;
                            table.setRowSelectionInterval(lastRowIndex, lastRowIndex);
                        }
                    } else {
                        System.out.println("Invalid row index for deletion: " + row);
                    }
                } else {
                    // If the user clicked "No", simply log that the deletion was canceled
                    System.out.println("Deletion canceled by user.");
                }
            });
            if (!"Selesai".equals(status) && role != 1) {
                panel.add(hapusButton);
            }

            // Revalidate the panel to update layout
            panel.revalidate();
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }

    private Object[] getPatientData(int row) {
        // Retrieve patient data from the table row
        Object[] patientData = new Object[11];
        patientData[1] = model.getValueAt(row, 2); // Name
        patientData[2] = model.getValueAt(row, 2); // Age (assuming it's in the same column for simplicity)
        patientData[9] = model.getValueAt(row, 2); // Gender (assuming it's in the same column for simplicity)
        // Add other necessary patient data here
        return patientData;
    }

    private List<Object[]> getDrugData(int row) {
        // Retrieve drug data for the patient from the database
        List<Object[]> drugData = new ArrayList<>();
        QueryExecutor executor = new QueryExecutor();
        String query = "SELECT nama_obat, jenis_obat, jumlah, harga, cara_penggunaan FROM detail_pembayaran WHERE id_antrian = ?";
        Object[] parameter = new Object[]{idList.get(row)};
        List<Map<String, Object>> results = executor.executeSelectQuery(query, parameter);

        for (Map<String, Object> result : results) {
            Object[] drug = new Object[]{
                result.get("nama_obat"),
                result.get("jenis_obat"),
                result.get("jumlah"),
                result.get("harga"),
                result.get("cara_penggunaan")
            };
            drugData.add(drug);
        }
        return drugData;
    }

    private double calculateTotal(List<Object[]> drugData) {
        // Calculate the total price of the drugs
        double total = 0;
        for (Object[] drug : drugData) {
            int quantity = (int) drug[2];
            Number priceNumber = (Number) drug[3]; // Cast to Number to handle both Integer and Double
            double price = priceNumber.doubleValue(); // Convert to double
            total += quantity * price;
        }
        return total;
    }
}
