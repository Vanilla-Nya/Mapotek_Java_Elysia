package Obat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import Components.CustomDatePicker;
import Components.CustomTable.CustomTable;
import Components.CustomTextField;
import Components.Dropdown; 
import Components.RoundedButton;
import Components.ShowModalCenter;
import DataBase.QueryExecutor;
import Global.UserSessionCache;

public class EditObat extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(EditObat.class.getName());

    private CustomTextField txtNamaObat, txtHargaJual, txtStock, txtBarcode;
    private Dropdown txtJenisObat, txtJenisBentukObat; 
    private JTable table;
    private int row;
    private CustomTable tableObatMasuk, tableObatKeluar;

    public EditObat(String namaObat, String jenisObat, String stock, String barcode, JTable table, int row, String idObat, Runnable refreshCallback) {
        QueryExecutor executor = new QueryExecutor();
        String query = "SELECT nama_jenis_obat FROM jenis_obat";
        List<String> jenisObatList = new ArrayList<>();
        Map<String, String> jenisObatMap = new HashMap<>();
        System.out.println(idObat);
        LOGGER.info("Unique jenisObatList: " + jenisObatList);

        if (idObat == null || idObat.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID Obat tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1000, 650)); // Tambahkan ini

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
        // Samakan ukuran dengan CustomTextField
        txtJenisObat.setPreferredSize(txtNamaObat.getPreferredSize());
        formPanel.add(txtJenisObat, gbc);

        gbc.gridx = 4;
        formPanel.add(new JLabel("Jenis Bentuk Obat:"), gbc);
        gbc.gridx = 5;
        txtJenisBentukObat = new Dropdown(true, true, null);
        txtJenisBentukObat.setItems(jenisObatList, true, true, null);
        txtJenisBentukObat.setSelectedItem(getJenisBentukObat(idObat));
        // Samakan ukuran dengan CustomTextField
        txtJenisBentukObat.setPreferredSize(txtNamaObat.getPreferredSize());
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

            // Input field for harga beli
            dialogGbc.gridx = 0;
            dialogGbc.gridy = 1;
            panel.add(new JLabel("Harga Beli:"), dialogGbc);
            dialogGbc.gridx = 1;
            CustomTextField txtHargaBeliInput = new CustomTextField("Masukkan harga beli", 20, 15, Optional.empty());
            panel.add(txtHargaBeliInput, dialogGbc);

            // Input field for harga jual
            dialogGbc.gridx = 0;
            dialogGbc.gridy = 2;
            panel.add(new JLabel("Harga Jual:"), dialogGbc);
            dialogGbc.gridx = 1;
            CustomTextField txtHargaJualInput = new CustomTextField("Masukkan harga jual", 20, 15, Optional.empty());
            panel.add(txtHargaJualInput, dialogGbc);

            // Date picker for stock addition
            dialogGbc.gridx = 0;
            dialogGbc.gridy = 3;
            panel.add(new JLabel("Tanggal:"), dialogGbc);
            dialogGbc.gridx = 1;
            CustomTextField txtDateInput = new CustomTextField("Tanggal Expired", 20, 15, Optional.empty());
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
                String hargaBeliInput = txtHargaBeliInput.getText();
                String hargaJualInput = txtHargaJualInput.getText();
                String dateInput = txtDateInput.getText();

                if (stockInput != null && !stockInput.trim().isEmpty() &&
                    hargaBeliInput != null && !hargaBeliInput.trim().isEmpty() &&
                    hargaJualInput != null && !hargaJualInput.trim().isEmpty() &&
                    dateInput != null && !dateInput.trim().isEmpty()) {
                    try {
                        int stockValue = Integer.parseInt(stockInput.trim());
                        double hargaBeliValue = Double.parseDouble(hargaBeliInput.trim());
                        double hargaJualValue = Double.parseDouble(hargaJualInput.trim());

                        if (stockValue > 0 && hargaBeliValue > 0 && hargaJualValue > 0) {
                            // Update the stock in the database
                            String insertQuery = "INSERT INTO detail_obat (id_obat, tanggal_expired, stock, harga_beli, harga_jual, status_batch, alasan) VALUES (?, ?, ?, ?, ?,'aktif',NULL)";
                            executor.executeInsertQuery(insertQuery, new Object[]{Integer.parseInt(idObat), dateInput, stockValue, hargaBeliValue, hargaJualValue});

                            // Insert into pengeluaran
                            String tanggal = java.time.LocalDate.now().toString(); // Current date
                            String keterangan = "Restock Obat";
                            double totalPengeluaran = hargaBeliValue * stockValue;

                            // Insert into pengeluaran table
                            String pengeluaranQuery = "INSERT INTO pengeluaran (tanggal, keterangan, id_user) VALUES (?, ?, ?)";
                            UserSessionCache cache = new UserSessionCache();
                            String uuid = (String) cache.getUUID();
                            int pengeluaranId = (int) QueryExecutor.executeInsertQueryWithReturnID(pengeluaranQuery, new Object[]{tanggal, keterangan, uuid});

                            // Insert into pengeluaran_detail table
                            if (pengeluaranId != 0) {
                                String pengeluaranDetailQuery = "INSERT INTO pengeluaran_detail (id_pengeluaran, id_jenis_pengeluaran, keterangan, total) VALUES (?, ?, ?, ?)";
                                QueryExecutor.executeInsertQuery(pengeluaranDetailQuery, new Object[]{pengeluaranId, 2, keterangan, totalPengeluaran});
                            }

                            // Update the stock label
                            int currentStock = Integer.parseInt(stock);
                            lblStock.setText(String.valueOf(currentStock + stockValue));

                            // Refresh the table
                            refreshTableObatMasuk(idObat);

                            // Panggil callback untuk memperbarui tabel utama di Obat
                            if (refreshCallback != null) {
                                refreshCallback.run();
                            }

                            JOptionPane.showMessageDialog(this, "Stock berhasil ditambahkan dan pengeluaran dicatat!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "Jumlah stock, harga beli, dan harga jual harus lebih dari 0!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Masukkan nilai yang valid untuk stock, harga beli, dan harga jual!", "Error", JOptionPane.ERROR_MESSAGE);
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

        // Tambahkan ActionListener ke tombol "Simpan"
        submitButton.addActionListener(e -> {
            // Ambil data baru dari input field
            String newNamaObat = txtNamaObat.getText().trim();
            String newJenisObat = (String) txtJenisObat.getSelectedItem(); // Nama jenis obat
            String newJenisBentukObat = (String) txtJenisBentukObat.getSelectedItem();
            String newBarcode = txtBarcode.getText().trim();

            // Ambil data lama dari parameter atau tabel
            String oldNamaObat = namaObat;
            String oldJenisObat = jenisObat;
            String oldJenisBentukObat = getJenisBentukObat(idObat);
            String oldBarcode = barcode;

            // Periksa apakah ada perubahan
            boolean isChanged = false;
            if (!newNamaObat.equals(oldNamaObat)) isChanged = true;
            if (!newJenisObat.equals(oldJenisObat)) isChanged = true;
            if (!newJenisBentukObat.equals(oldJenisBentukObat)) isChanged = true;
            if (!newBarcode.equals(oldBarcode)) isChanged = true;

            if (isChanged) {
                try {
                    // Ambil id_jenis_obat berdasarkan nama jenis obat
                    String getIdJenisObatQuery = "SELECT id_jenis_obat FROM jenis_obat WHERE nama_jenis_obat = ?";
                    List<Map<String, Object>> jenisObatResults = executor.executeSelectQuery(getIdJenisObatQuery, new Object[]{newJenisObat});

                    if (jenisObatResults.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Jenis obat tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Ambil id_jenis_obat dari hasil query
                    String idJenisObat = String.valueOf(jenisObatResults.get(0).get("id_jenis_obat"));

                    // Simpan perubahan ke database
                    String updateQuery = "UPDATE obat SET nama_obat = ?, id_jenis_obat = ?, bentuk_obat = ?, barcode = ? WHERE id_obat = ?";
                    executor.executeUpdateQuery(updateQuery, new Object[]{
                        newNamaObat,
                        idJenisObat, // Gunakan id_jenis_obat
                        newJenisBentukObat,
                        newBarcode,
                        Integer.parseInt(idObat)
                    });

                    // Tampilkan pesan sukses
                    JOptionPane.showMessageDialog(this, "Perubahan berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);

                    // Panggil callback untuk memperbarui tabel utama
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }

                    // Tutup modal setelah berhasil menyimpan
                    ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(this));

                } catch (Exception ex) {
                    // Tampilkan pesan error jika gagal
                    JOptionPane.showMessageDialog(this, "Gagal menyimpan perubahan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Tampilkan pesan jika tidak ada perubahan
                JOptionPane.showMessageDialog(this, "Tidak ada perubahan yang disimpan.", "Info", JOptionPane.INFORMATION_MESSAGE);

                // Tutup modal jika tidak ada perubahan
                ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(this));
            }
        });

        // Set the preferred size of the form panel to match the table's width
        // if (table != null) {
        //     formPanel.setPreferredSize(table.getPreferredSize());
        // }

        // Add the form panel to the main layout
        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(tableObatMasuk), BorderLayout.CENTER);

        // Create the table for "Obat Masuk"
        String[] columns = {"Nama Obat", "Stock", "Dibuat", "Expired", "Harga Beli", "Harga Jual", "Status", "AKSI"}; // Include "AKSI" column
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        tableObatMasuk = new CustomTable(model); // Use CustomTable

        query = "CALL all_obat_masuk(?)";
        List<Map<String, Object>> updatedResults = executor.executeSelectQuery(query, new Object[]{Integer.parseInt(idObat)});
        for (Map<String, Object> resultRow : updatedResults) {
            model.addRow(new Object[]{
                resultRow.get("nama_obat"),
                resultRow.get("stock"),
                resultRow.get("created_at"),
                resultRow.get("tanggal_expired"),
                resultRow.get("harga_beli"),
                resultRow.get("harga_jual"),
                resultRow.get("status_batch"),
                "DETAIL" // Add "DETAIL" button text to the "AKSI" column
            });
        }

        // Add mouse listener for "DETAIL" button
        tableObatMasuk.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = tableObatMasuk.getColumnModel().getColumnIndex("AKSI");
                int row = tableObatMasuk.getSelectedRow();
                if (row >= 0 && column == tableObatMasuk.getSelectedColumn()) {
                    // Ambil nilai "status_batch" dari baris yang dipilih
                    String statusBatch = tableObatMasuk.getValueAt(row, 6).toString(); // Kolom ke-6 adalah "status_batch"
                    
                    // Cetak nilai "status_batch"
                    System.out.println("Status Batch: " + statusBatch);

                    // Lakukan tindakan lainnya jika diperlukan
                    int idDetailObat = (Integer) updatedResults.get(row).get("id_detail_obat");
                    showPengeluaranObatPerBatch(idDetailObat);
                }
            }
        });

        // Add the table to the main layout
        add(new JScrollPane(tableObatMasuk), BorderLayout.CENTER);

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

        // Panel untuk tombol di bawah tabel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Ambil status_batch dari database
        String statusBatchQuery = "SELECT status_batch FROM detail_obat WHERE id_detail_obat = ?";
        List<Map<String, Object>> statusResults = executor.executeSelectQuery(statusBatchQuery, new Object[]{idDetailObat});

        if (!statusResults.isEmpty()) {
            String statusBatch = (String) statusResults.get(0).get("status_batch");

            // Tambahkan tombol Restock hanya jika status_batch adalah "aktif"
            if ("aktif".equalsIgnoreCase(statusBatch) || "expired".equalsIgnoreCase(statusBatch)) {
                JButton btnRestock = new RoundedButton("Restock");
                btnRestock.setBackground(new Color(0, 123, 255));
                btnRestock.setForeground(Color.WHITE);

                // Tambahkan tombol ke panel
                buttonPanel.add(btnRestock);

                // Action listener untuk tombol Restock
                btnRestock.addActionListener(e -> {
                    String queryDetail = "CALL detail_obat_bacth(?)";
                    List<Map<String, Object>> detailResults = executor.executeSelectQuery(queryDetail, new Object[]{idDetailObat});

                    if (!detailResults.isEmpty()) {
                        Map<String, Object> detail = detailResults.get(0);

                        String idObat = String.valueOf(detail.get("id_obat"));
                        if (idObat == null || idObat.trim().isEmpty()) {
                            JOptionPane.showMessageDialog(null, "ID Obat tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        String namaObat = String.valueOf(detail.get("nama_obat"));
                        String jenisObat = String.valueOf(detail.get("nama_jenis_obat"));
                        String stokLama = String.valueOf(detail.get("stock"));
                        String tanggalExpiredLama = String.valueOf(detail.get("tanggal_expired"));
                        String hargaBeliLama = String.valueOf(detail.get("harga_beli"));
                        String hargaJualLama = String.valueOf(detail.get("harga_jual"));

                        boolean isRestockSuccessful = Restock.showRestockDialog(idObat, String.valueOf(idDetailObat), namaObat, jenisObat, stokLama, tanggalExpiredLama, hargaBeliLama, hargaJualLama);

                        if (isRestockSuccessful) {
                            refreshTableObatMasuk(idObat);
                        } else {
                            System.out.println("Restock dibatalkan oleh pengguna.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Data batch tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        }

        // Tambahkan panel tombol ke bawah tabel
        detailPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Tampilkan dialog
        JOptionPane.showMessageDialog(null, detailPanel, "Detail Pengeluaran Obat Per Batch", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshTableObatMasuk(String idObat) {
        if (idObat == null || idObat.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID Obat tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tableObatMasuk.getModel();
        model.setRowCount(0); // Clear the table

        // Fetch updated data for "Obat Masuk"
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_obat_masuk(?)";
        List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{idObat});

        for (Map<String, Object> row : results) {
            model.addRow(new Object[]{
                row.get("nama_obat"),
                row.get("stock"),
                row.get("created_at"),
                row.get("tanggal_expired"),
                row.get("harga_beli"),
                row.get("harga_jual"),
                row.get("status_batch"),
                "DETAIL" // Add "DETAIL" button text back to the "AKSI" column
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

    public static void showModalCenter(JFrame parent, String namaObat, String jenisObat, String stock, String barcode, JTable table, int row, String idObat, Runnable refreshCallback) {
        EditObat panel = new EditObat(namaObat, jenisObat, stock, barcode, table, row, idObat, refreshCallback);
        Components.ShowModalCenter.showCenterModal(parent, panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String namaObat = "Paracetamol"; // Retrieve the value
            if (namaObat == null) {
                namaObat = ""; // Default to an empty string
            }
            new EditObat(namaObat, "Tablet", "100", "1234567890123", null, 0, "0", null); // Pass the validated value
        });
    }
}
