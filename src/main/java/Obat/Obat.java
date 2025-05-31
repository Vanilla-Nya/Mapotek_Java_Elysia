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
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import Components.CustomCard;
import Components.CustomTable.CustomTable;
import Components.CustomTextField;
import Components.RoundedButton;
import Components.RoundedPanel;
import DataBase.QueryExecutor;
import Helpers.OnObatAddedListener;
import Helpers.OnObatUpdateListener;
import static Pemeriksaan.TablePemeriksaan.mapToArray;

public class Obat extends JPanel implements OnObatAddedListener, OnObatUpdateListener {

    private JLabel idLabel, hargaLabel, namaObatLabel, stockLabel, jenisObatLabel;
    private JLabel expiredLabel, statusBatchLabel;
    private DefaultTableModel tableModel;
    private JTable obatTable;
    private int selectedRow = -1;
    Object[][] data = {};
    Object[][] fullData = {};

    public Obat() {
        // Frame setup
        setLayout(new BorderLayout());
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_obat()";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});
        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                Object[] dataFromDatabase = new Object[]{
                    data.length + 1,               // Column 0: 
                    result.get("barcode"),         // Column 2: BARCODE
                    result.get("nama_obat"),       // Column 3: NAMA OBAT
                    result.get("nama_jenis_obat"), // Column 4: JENIS OBAT
                    result.get("harga_jual"),      // Column 5: HARGA JUAL
                    result.get("stock"),           // Column 6: STOCK
                };

                // Update data array
                Object[][] newData = new Object[data.length + 1][];
                System.arraycopy(data, 0, newData, 0, data.length);
                newData[data.length] = dataFromDatabase;
                data = newData;

                // Update fullData array
                Object[][] newDataFull = new Object[fullData.length + 1][];
                System.arraycopy(fullData, 0, newDataFull, 0, fullData.length);
                newDataFull[fullData.length] = mapToArray(result);
                fullData = newDataFull;
            }
        }
        System.out.println(fullData[1][1]);
        System.out.println(fullData[1][2]);
        System.out.println(fullData[1][3]);
        System.out.println(fullData[1][4]);
        System.out.println(fullData[1][5]);
        System.out.println(fullData[1][6]);

        setLayout(new BorderLayout());

        // Top Panel (Search and Add Buttons)
        JPanel topPanel = createTopPanel();

        // Gabungkan headerPanel dan topPanel ke dalam satu panel
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        northPanel.setBackground(Color.WHITE);
        northPanel.add(topPanel, BorderLayout.CENTER);

        // Data Panel (Displays selected obat details)
        CustomCard dataPanel = createDataPanel();

        // Table Panel (Displays list of obats)
        JScrollPane tableScrollPane = createTablePanel();

        // Main Panel combining Data and Table Panels
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        // Tambahkan padding di sini
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20)); // padding bawah & kanan
        mainPanel.add(dataPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Add components to the frame
        add(northPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add the "Tambahkan Obat" button to the right side
        RoundedButton addButton = new RoundedButton("Tambahkan Obat");
        addButton.setBackground(new Color(0, 153, 102));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setPreferredSize(new Dimension(150, 40));
        addButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            RegisterObat.showModalCenter(
                (JFrame) SwingUtilities.getWindowAncestor(addButton),
                Obat.this,
                Obat.this
            );
        }));

        // Search field with button (left side)
        CustomTextField searchField = new CustomTextField("Cari Obat", 20, 30, Optional.empty());

        // Panel to hold the search field and button together
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(searchField, BorderLayout.CENTER);

        searchField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                performSearch();
            }

            private void performSearch() {
                String searchTerm = searchField.getText().toLowerCase();
                Object[][] filteredData = Arrays.stream(data)
                        .filter(row -> ((String) row[1]).toLowerCase().contains(searchTerm) // Check if 'BARCODE' contains search term
                                || ((String) row[2]).toLowerCase().contains(searchTerm)) // Check if 'NAMA OBAT' contains search term
                        .toArray(Object[][]::new);

                // Add the "AKSI" column with action buttons back to the filtered data
                Object[][] dataWithActions = new Object[filteredData.length][7]; // 7 columns including the "AKSI" column

                for (int i = 0; i < filteredData.length; i++) {
                    dataWithActions[i] = Arrays.copyOf(filteredData[i], 7); // Copy data to the new array and ensure we have 7 columns
                    dataWithActions[i][6] = "Action"; // Placeholder for the "AKSI" column (we will update this with buttons)
                }

                // Update the table model with the filtered data
                tableModel.setDataVector(filteredData, new String[]{"NO", "BARCODE", "NAMA OBAT", "JENIS OBAT", "HARGA", "STOCK"});

                // Reapply the button rendering and editing to the "AKSI" column
                setTableColumnWidths(obatTable);
            }
        });

        // Add components to the top panel
        topPanel.add(searchPanel, BorderLayout.WEST);  // Search panel on left
        topPanel.add(addButton, BorderLayout.EAST);    // "Tambahkan Obat" button on right
        return topPanel;
    }

    private CustomCard createDataPanel() {
        JPanel dataPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        dataPanel.setOpaque(false);
        dataPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create Labels for data
        hargaLabel = createDataLabel("HARGA : ");
        namaObatLabel = createDataLabel("NAMA OBAT : ");
        stockLabel = createDataLabel("STOCK : ");
        jenisObatLabel = createDataLabel("JENIS OBAT : ");

        // Add labels to the data panel
        dataPanel.add(namaObatLabel);
        dataPanel.add(new JLabel(""));
        dataPanel.add(jenisObatLabel);
        dataPanel.add(new JLabel(""));
        dataPanel.add(hargaLabel);
        dataPanel.add(new JLabel(""));
        dataPanel.add(stockLabel);
        dataPanel.add(new JLabel(""));

        // Baris terakhir: tombol di kanan bawah
        dataPanel.add(new JLabel("")); // Kolom kiri kosong

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroup.setOpaque(false);

        JButton editButton = new RoundedButton("EDIT", 15);
        editButton.setBackground(new Color(255, 153, 51));
        editButton.setForeground(Color.WHITE);
        editButton.setFocusPainted(false);
        editButton.addActionListener(e -> {
            if (selectedRow != -1) {
                String namaObat = String.valueOf(obatTable.getValueAt(selectedRow, 2));
                String jenisObat = String.valueOf(obatTable.getValueAt(selectedRow, 3));
                String hargaJual = String.valueOf(obatTable.getValueAt(selectedRow, 4));
                String stock = String.valueOf(obatTable.getValueAt(selectedRow, 5));
                String barcode = String.valueOf(obatTable.getValueAt(selectedRow, 1));
                String idObat = String.valueOf(fullData[selectedRow][1]);

                EditObat.showModalCenter(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    namaObat, jenisObat, stock, barcode, obatTable, selectedRow, idObat,
                    () -> refreshTableData()
                );
            } else {
                JOptionPane.showMessageDialog(this, "Silakan pilih data obat terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton hapusButton = new RoundedButton("HAPUS", 15);
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
                    try {
                        int idObat = Integer.parseInt(fullData[selectedRow][0].toString());
                        QueryExecutor executor = new QueryExecutor();
                        String deleteQuery = "UPDATE obat SET is_deleted = 1 WHERE id_obat = ?";
                        boolean isDeleted = executor.executeUpdateQuery(deleteQuery, new Object[]{idObat});

                        if (isDeleted) {
                            refreshTableData();
                            JOptionPane.showMessageDialog(null, "Obat berhasil ditandai sebagai dihapus.");
                        } else {
                            JOptionPane.showMessageDialog(null, "Gagal menandai obat sebagai dihapus.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Silakan pilih data obat terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonGroup.add(editButton);
        buttonGroup.add(hapusButton);
        dataPanel.add(buttonGroup); // Kolom kanan bawah

        return new CustomCard("DETAIL OBAT", dataPanel);
    }

    private JScrollPane createTablePanel() {
        // Table data and columns setup
        String[] columns = {"NO", "BARCODE", "NAMA OBAT", "JENIS OBAT", "HARGA JUAL", "STOCK"};
        tableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only "AKSI" column is editable
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
        JScrollPane scrollPane = new JScrollPane(obatTable);
        // Tambahkan padding atas agar ada jarak antara card dan tabel
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // 20px jarak atas
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return scrollPane;
    }

    private JLabel createDataLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private void updateDataPanel(JTable table, int row) {
        selectedRow = row; // Save the selected row index
        String namaObat = String.valueOf(table.getValueAt(row, 2));
        String jenisObat = String.valueOf(table.getValueAt(row, 3));
        double hargaJual = Double.parseDouble(table.getValueAt(row, 4).toString());
        String stock = String.valueOf(table.getValueAt(row, 5));

        namaObatLabel.setText("NAMA OBAT : " + namaObat);
        jenisObatLabel.setText("JENIS OBAT : " + jenisObat);
        hargaLabel.setText("HARGA JUAL : " + formatToRupiah(hargaJual));
        stockLabel.setText("STOCK : " + stock);
    }

    private void setTableColumnWidths(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // NO column
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // BARCODE column
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // NAMA OBAT column
        table.getColumnModel().getColumn(3).setPreferredWidth(50);  // JENIS OBAT column
        table.getColumnModel().getColumn(4).setPreferredWidth(50);  // HARGA column
        table.getColumnModel().getColumn(5).setPreferredWidth(50);  // STOCK column
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Obat().setVisible(true));
    }

    @Override
    public void onObatAddedListener(String namaObat, String jenisObat, String harga, String stock) {
        double hargaValue = Double.parseDouble(harga);
        tableModel.addRow(new Object[]{
            obatTable.getRowCount() + 1, 
            namaObat, 
            jenisObat, 
            formatToRupiah(hargaValue), // Format harga
            stock
        });
    }

    @Override
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
        String query = "CALL all_obat()";

        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

        data = new Object[0][];
        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                Object[] dataFromDatabase = new Object[]{
                    data.length + 1,               // Column 0: NO
                    result.get("barcode"),         // Column 1: BARCODE
                    result.get("nama_obat"),       // Column 2: NAMA OBAT
                    result.get("nama_jenis_obat"), // Column 3: JENIS OBAT
                    result.get("harga_jual"),      // Column 4: HARGA JUAL
                    result.get("stock"),            // Column 5: STOCK
                };

                Object[][] newData = new Object[data.length + 1][];
                System.arraycopy(data, 0, newData, 0, data.length);
                newData[data.length] = dataFromDatabase;
                data = newData;
            }
        }

        String[] columns = {"NO", "BARCODE", "NAMA OBAT", "JENIS OBAT", "HARGA JUAL", "STOCK"}; 
        tableModel.setDataVector(data, columns);
        setTableColumnWidths(obatTable);
    }

    public void addDrugToTable(String name, String type, int quantity, int price) {
        // Add the new drug data to the table
        Object[] newRow = {name, type, quantity, price, ""};
        tableModel.addRow(newRow);
    }

    private String formatToRupiah(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(amount).replace("Rp", "Rp."); // Replace default "Rp" with "Rp."
    }
}
