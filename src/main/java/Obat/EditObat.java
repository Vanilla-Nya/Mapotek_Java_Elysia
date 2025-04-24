package Obat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.itextpdf.layout.element.GridContainer;

import Components.CustomDatePicker;
import Components.CustomTable.CustomTable;
import Components.CustomTextField;
import Components.Dropdown;
import Components.RoundedButton;
import DataBase.QueryExecutor;
import java_cup.parse_action;

public class EditObat extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(EditObat.class.getName());

    private CustomTextField txtNamaObat, txtHargaJual, txtStock, txtBarcode;
    private Dropdown txtJenisObat, txtJenisBentukObat; 
    private JTable table;
    private int row;
    private CustomTable tableObatMasuk, tableObatKeluar;

    public EditObat(String namaObat, String jenisObat, String hargaJual, String stock, String barcode, JTable table, int row, String idObat) {
        QueryExecutor executor = new QueryExecutor();
        String query = "SELECT * from jenis_obat";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});
        Set<String> uniqueJenisObatSet = new HashSet<>();  // Use a Set to store unique 'nama_jenis_obat'

        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                String jenisObatValue = (String) result.get("nama_jenis_obat");
                LOGGER.info("Retrieved jenisObat: " + jenisObatValue);
                uniqueJenisObatSet.add(jenisObatValue);
            }
        }
        System.out.println(idObat);
        List<String> jenisObatList = new ArrayList<>(uniqueJenisObatSet);
        LOGGER.info("Unique jenisObatList: " + jenisObatList);

        setTitle("Edit Obat");
        setSize(1280, 720);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        this.table = table;
        this.row = row;

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Row 1: Nama Obat, Jenis Obat, Jenis Bentuk Obat
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Nama Obat:"), gbc);
        gbc.gridx = 1;
        txtNamaObat = new CustomTextField("Nama Obat", 20, 15, Optional.empty());
        txtNamaObat.setText(namaObat != null ? namaObat : "");
        formPanel.add(txtNamaObat, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Jenis Obat:"), gbc);
        gbc.gridx = 3;
        txtJenisObat = new Dropdown(true, true, null);
        txtJenisObat.setItems(jenisObatList, true, true, null);
        txtJenisObat.setSelectedItem(jenisObat);
        formPanel.add(txtJenisObat, gbc);

        gbc.gridx = 4;
        formPanel.add(new JLabel("Jenis Bentuk Obat:"), gbc);
        gbc.gridx = 5;
        txtJenisBentukObat = new Dropdown(true, true, null);
        txtJenisBentukObat.setItems(jenisObatList, true, true, null);
        txtJenisBentukObat.setSelectedItem(getJenisBentukObat(idObat));
        formPanel.add(txtJenisBentukObat, gbc);

        // Row 2: Barcode, Stock, and Restock Button
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Barcode:"), gbc);
        gbc.gridx = 1;
        txtBarcode = new CustomTextField("Barcode", 20, 15, Optional.empty());
        txtBarcode.setText(barcode != null ? barcode : "");
        formPanel.add(txtBarcode, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 3;
        JLabel lblStock = new JLabel(stock);
        formPanel.add(lblStock, gbc);

        // Add the "Tambahkan Stock" button
        gbc.gridx = 4; // Place it to the right of the "Stock" label
        RoundedButton btnTambahkanStock = new RoundedButton("Tambahkan Stock");
        btnTambahkanStock.setBackground(new Color(0, 123, 255)); // Set button color
        btnTambahkanStock.setForeground(Color.WHITE);
        formPanel.add(btnTambahkanStock, gbc);

        // Add ActionListener to the "Tambahkan Stock" button
        btnTambahkanStock.addActionListener(e -> {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints dialogGbc = new GridBagConstraints();
            dialogGbc.insets = new Insets(5, 5, 5, 5);
            dialogGbc.fill = GridBagConstraints.HORIZONTAL;

            // Input field for stock
            dialogGbc.gridx = 0;
            dialogGbc.gridy = 0;
            panel.add(new JLabel("Tambahkan Stock:"), dialogGbc);
            dialogGbc.gridx = 1;
            CustomTextField txtStockInput = new CustomTextField("Masukkan jumlah stock", 20, 15, Optional.empty());
            panel.add(txtStockInput, dialogGbc);

            // Date picker for stock addition
            dialogGbc.gridx = 0;
            dialogGbc.gridy = 1;
            panel.add(new JLabel("Tanggal:"), dialogGbc);
            dialogGbc.gridx = 1;
            CustomTextField txtDateInput = new CustomTextField("Pilih tanggal", 20, 15, Optional.empty());
            CustomDatePicker datePicker = new CustomDatePicker(txtDateInput.getTextField(), true);
            txtDateInput.getTextField().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    datePicker.showDatePicker(); // Show the date picker dialog
                }
            });
            panel.add(txtDateInput, dialogGbc);

            // Show the dialog
            int result = JOptionPane.showConfirmDialog(this, panel, "Tambahkan Stock", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String stockInput = txtStockInput.getText();
                String dateInput = txtDateInput.getText();

                if (stockInput != null && !stockInput.trim().isEmpty() && dateInput != null && !dateInput.trim().isEmpty()) {
                    try {
                        int stockValue = Integer.parseInt(stockInput.trim());
                        if (stockValue > 0) {
                            // Update the stock in the database
                            String insertQuery = "INSERT INTO detail_obat (id_obat, tanggal_expired, stock, status_batch, alasan) VALUES (?, ?, ?, 'aktif', NULL)";
                            try {
                                int idObatInt = Integer.parseInt(idObat); // Pastikan idObat adalah integer
                                executor.executeInsertQuery(insertQuery, new Object[]{idObatInt, dateInput, stockValue});
                                JOptionPane.showMessageDialog(this, "Stock berhasil ditambahkan!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(this, "ID Obat harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
                            }

                            // Update the stock label
                            int currentStock = Integer.parseInt(stock);
                            lblStock.setText(String.valueOf(currentStock + stockValue));

                            // Refresh the table
                            refreshTableObatMasuk(idObat);

                            JOptionPane.showMessageDialog(this, "Stock berhasil ditambahkan!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "Jumlah stock harus lebih dari 0!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Masukkan jumlah stock yang valid!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Submit button
        gbc.gridx = 0;
        gbc.gridy = 2; // Move the submit button down
        gbc.gridwidth = 6;
        gbc.anchor = GridBagConstraints.CENTER;
        RoundedButton submitButton = new RoundedButton("Simpan");
        submitButton.setBackground(new Color(0, 150, 136));
        submitButton.setForeground(Color.WHITE);
        formPanel.add(submitButton, gbc);

        // Set the preferred size of the form panel to match the table's width
        if (table != null) {
            formPanel.setPreferredSize(table.getPreferredSize());
        }

        // Add the form panel to the main layout
        add(formPanel, BorderLayout.NORTH);

        // Create the tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Add "Obat Masuk" tab
        JPanel panelObatMasuk = createObatMasukPanel(idObat);
        tabbedPane.addTab("Obat Masuk", panelObatMasuk);

        add(tabbedPane, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private String getJenisBentukObat(String idObat) {
        // Query the database to get the current "Jenis Bentuk Obat" for the given idObat
        QueryExecutor executor = new QueryExecutor();
        String query = "SELECT bentuk_obat FROM obat WHERE id_obat = ?";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{idObat});
        if (!results.isEmpty()) {
            return (String) results.get(0).get("bentuk_obat");
        }
        return null; // Return null if no result is found
    }

    private void updateObat(String idObat) {
        // Update the database with the new "Jenis Bentuk Obat" value
        String selectedJenisBentukObat = (String) txtJenisBentukObat.getSelectedItem();
        String updateQuery = "UPDATE obat SET bentuk_obat = ? WHERE id_obat = ?";
        QueryExecutor executor = new QueryExecutor();
        executor.executeUpdateQuery(updateQuery, new Object[]{selectedJenisBentukObat, idObat});
    }

    private JPanel createObatMasukPanel(String idObat) {
        if (idObat == null || idObat.isEmpty()) {
            throw new IllegalArgumentException("ID Obat tidak boleh kosong!");
        }

        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"Nama Obat", "Stock", "Created At", "harga_beli", "harga_jual", "AKSI"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        tableObatMasuk = new CustomTable(model); // Gunakan CustomTable

        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_obat_masuk(?)";
        List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{Integer.parseInt(idObat)});

        for (Map<String, Object> row : results) {
            model.addRow(new Object[]{
                row.get("nama_obat"),
                row.get("stock"),
                row.get("created_at"),
                row.get("harga_beli"),
                row.get("harga_jual"),
                "DETAIL"
            });
        }

        // Tambahkan aksi untuk tombol "DETAIL"
        tableObatMasuk.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = tableObatMasuk.getColumnModel().getColumnIndex("AKSI");
                int row = tableObatMasuk.getSelectedRow();
                if (row >= 0 && column == tableObatMasuk.getSelectedColumn()) {
                    int idDetailObat = (Integer) results.get(row).get("id_detail_obat");
                    showPengeluaranObatPerBatch(idDetailObat);
                }
            }
        });

        panel.add(new JScrollPane(tableObatMasuk), BorderLayout.CENTER);
        return panel;
    }

    private void showPengeluaranObatPerBatch(int idDetailObat) {
        QueryExecutor executor = new QueryExecutor();
        String query = "SELECT * FROM pemeriksaan_obat WHERE id_detail_obat = ?";
        List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{idDetailObat});

        JPanel detailPanel = new JPanel(new BorderLayout());
        String[] columns = {"Jumlah", "Signa", "Created At"};
        Object[][] data = new Object[results.size()][columns.length];
        for (int i = 0; i < results.size(); i++) {
            Map<String, Object> row = results.get(i);
            data[i][0] = row.get("jumlah");
            data[i][1] = row.get("signa");
            data[i][2] = row.get("created_at");
        }

        JTable detailTable = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(detailTable);
        detailPanel.add(scrollPane, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(null, detailPanel, "Detail Pengeluaran Obat Per Batch", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshTableObatMasuk(String idObat) {
        DefaultTableModel model = (DefaultTableModel) tableObatMasuk.getModel();
        model.setRowCount(0); // Clear the table

        // Fetch updated data for "Obat Masuk"
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_obat_masuk(?)";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{idObat});
        for (Map<String, Object> row : results) {
            model.addRow(new Object[]{
                row.get("id_detail_obat"),
                row.get("id_obat"),
                row.get("nama_obat"),
                row.get("tanggal_expired"),
                row.get("created_at"),
                row.get("stock")
            });
        }
    }

    public void addActionListeners(TableModel model) {
        List<JButton> buttons = new ArrayList<>(); // Initialize the buttons list
        // Example: Add buttons to the list (replace with actual buttons if needed)
        buttons.add(new JButton("DETAIL"));

        for (JButton button : buttons) {
            button.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String idDetailObat = table.getValueAt(row, 0).toString();
                    // Logika untuk menampilkan detail obat keluar
                    QueryExecutor executor = new QueryExecutor();
                    String query = "SELECT * FROM pemeriksaan_obat WHERE id_detail_obat = ?";
                    List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{idDetailObat});

                    JPanel detailPanel = new JPanel(new BorderLayout());
                    String[] columns = {"ID Pemeriksaan", "Jumlah", "Tanggal"};
                    Object[][] data = new Object[results.size()][columns.length];
                    for (int i = 0; i < results.size(); i++) {
                        Map<String, Object> rowMap = results.get(i);
                        data[i][0] = rowMap.get("id_pemeriksaan_obat");
                        data[i][1] = rowMap.get("jumlah");
                        data[i][2] = rowMap.get("created_at");
                    }

                    JTable detailTable = new JTable(data, columns);
                    JScrollPane scrollPane = new JScrollPane(detailTable);
                    detailPanel.add(scrollPane, BorderLayout.CENTER);

                    JOptionPane.showMessageDialog(null, detailPanel, "Detail Obat Keluar", JOptionPane.INFORMATION_MESSAGE);
                }
            });
        }
    }

    private String formatToRupiah(Object value) {
        if (value == null) return "Rp 0";
        try {
            double amount = Double.parseDouble(value.toString());
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            return formatter.format(amount);
        } catch (NumberFormatException e) {
            return "Rp 0";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String namaObat = "Paracetamol"; // Retrieve the value
            if (namaObat == null) {
                namaObat = ""; // Default to an empty string
            }
            new EditObat(namaObat, "Tablet", "1000", "100", "1234567890123", null, 0, "0"); // Pass the validated value
        });
    }
}
