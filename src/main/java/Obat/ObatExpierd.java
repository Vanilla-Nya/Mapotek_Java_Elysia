package Obat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import Components.CustomDatePicker;
import Components.CustomTable.CustomTable;
import Components.CustomTextField;
import Components.RoundedButton;
import Components.RoundedPanel;
import DataBase.QueryExecutor;

public class ObatExpierd extends JPanel {

    private DefaultTableModel tableModel;
    private int selectedRow = -1;
    Object[][] data = {};
    private JLabel namaObatField, jenisObatField, tanggalExpiredField, stokField;
    private java.util.List<Map<String, Object>> results;
    private JTable obatTable;

    public ObatExpierd() {
        // Inisialisasi JTextField
        namaObatField = new JLabel();
        jenisObatField = new JLabel();
        tanggalExpiredField = new JLabel();
        stokField = new JLabel();

        // Frame setup
        setSize(800, 600);
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_obat_expired()";
        results = executor.executeSelectQuery(query, new Object[]{});
        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                Object[] dataFromDatabase = new Object[]{
                    data.length + 1, result.get("nama_obat"), result.get("nama_jenis_obat"),
                    result.get("tanggal_expired"), result.get("stock")
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

        setLayout(new BorderLayout());

        // Header Panel (Title)
        JPanel headerPanel = createHeaderPanel();

        // Top Panel (Search and Add Buttons)
        JPanel topPanel = createTopPanel();

        // Data Panel (Displays selected obat details)
        RoundedPanel dataPanel = createDataPanel();

        // Table Panel (Displays list of obats)
        JScrollPane tableScrollPane = createTablePanel();

        // Main Panel combining Data and Table Panels
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(dataPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Add components to the frame
        add(headerPanel, BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("OBAT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search field with button (left side)
        CustomTextField searchField = new CustomTextField("Cari Obat", 20, 30, Optional.empty());
        RoundedButton searchButton = new RoundedButton("Cari");
        searchButton.setBackground(new Color(0, 153, 102));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setPreferredSize(new Dimension(100, 40));

        // Panel to hold the search field and button together
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().toLowerCase();
            Object[][] filteredData = Arrays.stream(data)
                    .filter(row -> ((String) row[1]).toLowerCase().contains(searchTerm) // Filter by 'NAMA OBAT'
                            || ((String) row[2]).toLowerCase().contains(searchTerm)) // Filter by 'JENIS OBAT'
                    .toArray(Object[][]::new);

            // Update the table model with the filtered data
            tableModel.setDataVector(filteredData, new String[]{"NO", "NAMA OBAT", "JENIS OBAT", "TANGGAL EXPIRED", "STOCK"});
            setTableColumnWidths(obatTable);
        });

        // Add components to the top panel
        topPanel.add(searchPanel, BorderLayout.WEST);
        return topPanel;
    }

    private RoundedPanel createDataPanel() {
        RoundedPanel dataPanel = new RoundedPanel(15, Color.WHITE);
        dataPanel.setLayout(new GridLayout(6, 2, 10, 10));
        dataPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Inisialisasi JTextField
        namaObatField = new JLabel();
        jenisObatField = new JLabel();
        tanggalExpiredField = new JLabel();
        stokField = new JLabel();

        // Tambahkan ke panel
        dataPanel.add(new JLabel("NAMA OBAT : "));
        dataPanel.add(namaObatField);
        dataPanel.add(new JLabel("JENIS OBAT : "));
        dataPanel.add(jenisObatField);
        dataPanel.add(new JLabel("TANGGAL EXPIRED : "));
        dataPanel.add(tanggalExpiredField);
        dataPanel.add(new JLabel("STOK : "));
        dataPanel.add(stokField);

        // Tambahkan tombol "RESTOCK"
        JButton restockButton = new RoundedButton("RESTOCK");
        restockButton.setBackground(new Color(255, 153, 51));
        restockButton.setForeground(Color.WHITE);
        restockButton.setFocusPainted(false);
        restockButton.addActionListener(e -> {
            if (selectedRow >= 0) {
                // Ambil data dari results berdasarkan selectedRow
                Map<String, Object> detail = results.get(selectedRow);
                String idDetailObat = String.valueOf(detail.get("id_detail_obat")); // ID Detail Obat Lama
                String idObat = String.valueOf(detail.get("id_obat")); // ID Obat
                String namaObat = String.valueOf(detail.get("nama_obat"));
                String jenisObat = String.valueOf(detail.get("nama_jenis_obat"));
                String tanggalExpiredLama = String.valueOf(detail.get("tanggal_expired"));
                String stokLama = String.valueOf(detail.get("stock"));
                String hargaBeliLama = String.valueOf(detail.get("harga_beli"));
                String hargaJualLama = String.valueOf(detail.get("harga_jual"));

                // Panggil dialog Restock
                Restock.showRestockDialog(idObat, idDetailObat, namaObat, jenisObat, stokLama, tanggalExpiredLama, hargaBeliLama, hargaJualLama);

                // Refresh data tabel setelah Restock
                refreshTableData();
            } else {
                JOptionPane.showMessageDialog(this, "Pilih obat yang ingin direstock!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Tambahkan tombol "BUANG"
        JButton buangButton = new RoundedButton("BUANG");
        buangButton.setBackground(new Color(255, 51, 51));
        buangButton.setForeground(Color.WHITE);
        buangButton.setFocusPainted(false);
        buangButton.addActionListener(e -> {
            if (selectedRow >= 0) {
                int response = JOptionPane.showConfirmDialog(null,
                    "Apakah Anda yakin ingin membuang obat ini?",
                    "Konfirmasi Pembuangan",
                    JOptionPane.YES_NO_OPTION);

                if (response == JOptionPane.YES_OPTION) {
                    // Ambil data dari results berdasarkan selectedRow
                    Map<String, Object> detail = results.get(selectedRow);
                    String idDetailObat = String.valueOf(detail.get("id_detail_obat")); // ID Detail Obat

                    // Perbarui status_batch menjadi 'dibuang'
                    QueryExecutor executor = new QueryExecutor();
                    String query = "UPDATE detail_obat SET status_batch = 'dibuang' WHERE id_detail_obat = ?";
                    boolean success = executor.executeUpdateQuery(query, new Object[]{idDetailObat});

                    if (success) {
                        JOptionPane.showMessageDialog(null, "Obat berhasil dibuang.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        refreshTableData(); // Refresh data tabel
                    } else {
                        JOptionPane.showMessageDialog(null, "Gagal membuang obat.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Tambahkan tombol ke panel
        dataPanel.add(restockButton);
        dataPanel.add(buangButton);

        return dataPanel;
    }

    private JScrollPane createTablePanel() {
        // Kolom tabel
        String[] columns = {"NO", "NAMA OBAT", "JENIS OBAT", "TANGGAL EXPIRED", "STOCK"};

        // Model tabel
        tableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Semua kolom tidak dapat diedit
            }
        };

        // Gunakan CustomTable
        obatTable = new CustomTable(tableModel);

        // Atur lebar kolom
        setTableColumnWidths(obatTable);

        // Tambahkan listener untuk mendeteksi perubahan baris yang dipilih
        obatTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && obatTable.getSelectedRow() != -1) {
                int selectedRow = obatTable.getSelectedRow();
                updateDataPanel(obatTable, selectedRow);
            }
        });

        JScrollPane scrollPane = new JScrollPane(obatTable);
        return scrollPane;
    }

    private void updateDataPanel(JTable table, int row) {
        selectedRow = row; // Simpan indeks baris yang dipilih
        String namaObat = String.valueOf(table.getValueAt(row, 1));  // Kolom 1: NAMA OBAT
        String jenisObat = String.valueOf(table.getValueAt(row, 2));  // Kolom 2: JENIS OBAT
        String tanggalExpired = String.valueOf(table.getValueAt(row, 3)); // Kolom 3: TANGGAL EXPIRED
        String stock = String.valueOf(table.getValueAt(row, 4));      // Kolom 4: STOCK

        // Perbarui label di panel detail
        namaObatField.setText(namaObat);
        jenisObatField.setText(jenisObat);
        tanggalExpiredField.setText(tanggalExpired);
        stokField.setText(stock);
    }

    private void setTableColumnWidths(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // NO column
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // NAMA OBAT column
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // JENIS OBAT column
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // TANGGAL EXPIRED column
        table.getColumnModel().getColumn(4).setPreferredWidth(75);  // STOCK column
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ObatExpierd().setVisible(true));
    }

    public void onObatUpdate(String Harga, String Stock) {
        // Handle updating the row in the table when the "Edit" button is clicked
        if (selectedRow != -1) {
            // Update the selected row with the new data (Harga and Stock)
            tableModel.setValueAt(Harga, selectedRow, 3); // Update HARGA column
            tableModel.setValueAt(Stock, selectedRow, 4); // Update STOCK column
        }

        // Optionally, you can refresh the entire table data if needed
        refreshTableData();
    }

    public void refreshTableData() {
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_obat_expired()";
        results = executor.executeSelectQuery(query, new Object[]{});

        data = new Object[0][];
        if (!results.isEmpty()) {
            for (int i = 0; i < results.size(); i++) {
                Map<String, Object> result = results.get(i);
                Object[] dataFromDatabase = new Object[]{
                    i + 1, // NO
                    result.get("nama_obat"),      // Nama Obat
                    result.get("nama_jenis_obat"),// Jenis Obat
                    result.get("tanggal_expired"),// Tanggal Expired
                    result.get("stock")           // Stock
                };

                Object[][] newData = new Object[data.length + 1][];
                System.arraycopy(data, 0, newData, 0, data.length);
                newData[data.length] = dataFromDatabase;
                data = newData;
            }
        }

        tableModel.setDataVector(data, new String[]{"NO", "NAMA OBAT", "JENIS OBAT", "TANGGAL EXPIRED", "STOCK"});
        setTableColumnWidths(obatTable);
    }

    // Renderer for "AKSI" column
    class ActionCellRenderer extends JPanel implements TableCellRenderer {

        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
            
            // "RESTOCK" Button
            JButton restockButton = new RoundedButton("RESTOCK");
            restockButton.setBackground(new Color(255, 153, 51));
            restockButton.setForeground(Color.WHITE);
            restockButton.setFocusPainted(false);
            add(restockButton);

            // "BUANG" Button
            JButton buangButton = new RoundedButton("BUANG");
            buangButton.setBackground(new Color(255, 51, 51));
            buangButton.setForeground(Color.WHITE);
            buangButton.setFocusPainted(false);
            add(buangButton);

            setBackground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    public void addDrugToTable(String name, String type, int quantity, int price) {
        // Add the new drug data to the table
        Object[] newRow = {name, type, quantity, price, ""};
        tableModel.addRow(newRow);
    }
}
