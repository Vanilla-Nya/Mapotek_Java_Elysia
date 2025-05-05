package Obat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import Components.CustomTable.CustomTable;
import Components.CustomTextField;
import Components.RoundedButton;
import Components.RoundedPanel;
import DataBase.QueryExecutor;

public class StockObatMenipis extends JPanel {

    private JLabel idLabel, namaObatLabel, stockLabel, jenisObatLabel;
    private DefaultTableModel tableModel;
    private JTable obatTable;
    private int selectedRow = -1;
    Object[][] data = {};
    private List<Map<String, Object>> allObatData;

    public StockObatMenipis() {
        // Frame setup
        setLayout(new BorderLayout());
        setSize(800, 600);

        // Query data obat
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_obat()";
        allObatData = executor.executeSelectQuery(query, new Object[]{});

        // Filter data untuk stok menipis
        if (!allObatData.isEmpty()) {
            for (Map<String, Object> result : allObatData) {
                String namaObat = String.valueOf(result.get("nama_obat"));
                String namaJenisObat = String.valueOf(result.get("nama_jenis_obat"));
                String bentukObat = String.valueOf(result.get("bentuk_obat"));
                int stock = Integer.parseInt(String.valueOf(result.get("stock")));

                // Check if stock is low based on the bentuk_obat
                if (isStockRunningLow(bentukObat, stock)) {
                    // Tambahkan data ke tabel
                    Object[] dataFromDatabase = new Object[]{
                        data.length + 1, namaObat, namaJenisObat, stock
                    };

                    // Tambahkan data ke array
                    Object[][] newData = new Object[data.length + 1][];
                    System.arraycopy(data, 0, newData, 0, data.length);
                    newData[data.length] = dataFromDatabase;
                    data = newData;
                }
            }
        }

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

    private boolean isStockRunningLow(String bentukObat, int stock) {
        // Check if stock is low based on the bentuk_obat type
        switch (bentukObat) {
            case "Tablet":
            case "Kapsul":
                return stock <= 20;
            case "Sirup":
            case "Salep / Krim":
            case "Inhaler":
            case "Injeksi / Suntikan":
            case "Tetes":
            case "Patch (Patches)":
            case "Item":
                return stock <= 2;
            default:
                return false;
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("STOK OBAT MENIPIS");
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
                    .filter(row -> ((String) row[1]).toLowerCase().contains(searchTerm)) // Check if 'NAMA OBAT' contains search term
                    .toArray(Object[][]::new);

            // Update the table model with the filtered data
            tableModel.setDataVector(filteredData, new String[]{"NO", "NAMA OBAT", "JENIS OBAT", "STOCK"});
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

        // Create Labels for data
        namaObatLabel = createDataLabel("NAMA OBAT : ");
        stockLabel = createDataLabel("STOCK : ");
        jenisObatLabel = createDataLabel("JENIS OBAT : ");

        // Add labels to the data panel
        dataPanel.add(namaObatLabel);
        dataPanel.add(new JLabel(""));
        dataPanel.add(jenisObatLabel);
        dataPanel.add(new JLabel(""));
        dataPanel.add(stockLabel);
        dataPanel.add(new JLabel(""));

        // Add "EDIT" and "HAPUS" buttons
        JButton editButton = new RoundedButton("EDIT");
        editButton.setBackground(new Color(255, 153, 51));
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);
        editButton.addActionListener(e -> {
            if (selectedRow != -1) {
                // Ambil data dari tabel berdasarkan baris yang dipilih
                String namaObat = String.valueOf(obatTable.getValueAt(selectedRow, 1)); // NAMA OBAT

                // Cari detail obat di allObatData
                Map<String, Object> selectedObat = allObatData.stream()
                    .filter(obat -> namaObat.equals(String.valueOf(obat.get("nama_obat"))))
                    .findFirst()
                    .orElse(null);

                if (selectedObat != null) {
                    String jenisObat = String.valueOf(selectedObat.get("nama_jenis_obat"));
                    String stock = String.valueOf(selectedObat.get("stock"));
                    String barcode = String.valueOf(selectedObat.get("barcode"));
                    String idObat = String.valueOf(selectedObat.get("id_obat"));

                    // Panggil EditObat dengan data yang sesuai
                    new EditObat(namaObat, jenisObat, stock, barcode, obatTable, selectedRow, idObat, null);
                } else {
                    JOptionPane.showMessageDialog(this, "Detail obat tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton hapusButton = new RoundedButton("HAPUS");
        hapusButton.setBackground(new Color(255, 51, 51));
        hapusButton.setForeground(Color.WHITE);
        hapusButton.setFocusPainted(false);
        hapusButton.addActionListener(e -> {
            if (selectedRow != -1) {
                int response = JOptionPane.showConfirmDialog(null,
                        "Apakah Anda yakin ingin menghapus obat ini?",
                        "Konfirmasi Penghapusan",
                        JOptionPane.YES_NO_OPTION);

                if (response == JOptionPane.YES_OPTION) {
                    // Implement delete logic here
                    JOptionPane.showMessageDialog(null, "Obat berhasil dihapus.");
                }
            }
        });

        // Add buttons to the panel
        dataPanel.add(editButton);
        dataPanel.add(hapusButton);

        return dataPanel;
    }

    private JScrollPane createTablePanel() {
        // Table data and columns setup
        String[] columns = {"NO", "NAMA OBAT", "JENIS OBAT", "STOCK"};

        // Table model
        tableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Semua kolom tidak dapat diedit
            }
        };

        obatTable = new CustomTable(tableModel);

        // Add Mouse Listener to update data panel when a row is selected
        obatTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = obatTable.getSelectedRow();
                if (selectedRow != -1) {
                    updateDataPanel(obatTable, selectedRow);
                }
            }
        });

        // Adjust table column widths for better readability
        setTableColumnWidths(obatTable);

        // Scroll pane for the table
        return new JScrollPane(obatTable);
    }

    private JLabel createDataLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private void updateDataPanel(JTable table, int row) {
        selectedRow = row; // Save the selected row index
        String namaObat = String.valueOf(table.getValueAt(row, 1));
        String jenisObat = String.valueOf(table.getValueAt(row, 2));
        String stock = String.valueOf(table.getValueAt(row, 3));

        namaObatLabel.setText("NAMA OBAT : " + namaObat);
        jenisObatLabel.setText("JENIS OBAT : " + jenisObat);
        stockLabel.setText("STOCK : " + stock);
    }

    private void setTableColumnWidths(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // NO column
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // NAMA OBAT column
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // JENIS OBAT column
        table.getColumnModel().getColumn(3).setPreferredWidth(75);  // STOCK column
    }
}
