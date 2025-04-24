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

    private JLabel idLabel, namaObatLabel, stockLabel, jenisObatLabel, tanggalExpierd;
    private DefaultTableModel tableModel;
    private JTable obatTable;
    private int selectedRow = -1;
    Object[][] data = {};
    private JLabel namaObatField, jenisObatField, tanggalExpiredField, stokField;

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
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});
        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                Object[] dataFromDatabase = new Object[]{
                    data.length + 1, result.get("nama_obat"), result.get("nama_jenis_obat"),
                    result.get("tanggal_expired"), result.get("stock"), ""
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
                    .filter(row -> ((String) row[1]).toLowerCase().contains(searchTerm)) // Check if 'NAMA OBAT' contains search term
                    .toArray(Object[][]::new);

            // Add the "AKSI" column with action buttons back to the filtered data
            Object[][] dataWithActions = new Object[filteredData.length][6]; // 6 columns including the "AKSI" column

            for (int i = 0; i < filteredData.length; i++) {
                dataWithActions[i] = Arrays.copyOf(filteredData[i], 6); // Copy data to the new array and ensure we have 6 columns
                dataWithActions[i][5] = "Action"; // Placeholder for the "AKSI" column (we will update this with buttons)
            }

            // Update the table model with the filtered data
            tableModel.setDataVector(filteredData, new String[]{"NO", "NAMA OBAT", "JENIS OBAT", "HARGA", "STOCK", "AKSI"});

            // Reapply the button rendering and editing to the "AKSI" column
            obatTable.getColumn("AKSI").setCellRenderer(new ActionCellRenderer());
            obatTable.getColumn("AKSI").setCellEditor(new ActionCellEditor(tableModel));
            setTableColumnWidths(obatTable);
        });

        // Add components to the top panel
        topPanel.add(searchPanel, BorderLayout.WEST);
        return topPanel;
    }

    private RoundedPanel createDataPanel() {
        RoundedPanel dataPanel = new RoundedPanel(15, Color.WHITE);
        dataPanel.setLayout(new GridLayout(5, 2, 10, 10));
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

        return dataPanel;
    }

    private JScrollPane createTablePanel() {
        // Table data and columns setup
        String[] columns = {"NO", "NAMA OBAT", "JENIS OBAT", "TANGGAL EXPIRED", "STOCK", "AKSI"};

        // Table model
        tableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only "AKSI" column is editable
            }
        };

        obatTable = new CustomTable(tableModel);
        obatTable.getColumn("AKSI").setCellRenderer(new ActionCellRenderer());
        obatTable.getColumn("AKSI").setCellEditor(new ActionCellEditor(tableModel));

        // Add Mouse Listener to update data panel when a row is selected
        obatTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = obatTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // Ambil data dari tabel
                    String namaObat = (String) tableModel.getValueAt(selectedRow, 1);
                    String jenisObat = (String) tableModel.getValueAt(selectedRow, 2);
                    Object tanggalExpiredObj = tableModel.getValueAt(selectedRow, 3);
                    String tanggalExpired = "";
                    if (tanggalExpiredObj instanceof java.sql.Date) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                        tanggalExpired = dateFormat.format(tanggalExpiredObj);
                    } else if (tanggalExpiredObj != null) {
                        tanggalExpired = tanggalExpiredObj.toString();
                    }
                    String stok = String.valueOf(tableModel.getValueAt(selectedRow, 4));

                    // Perbarui field di panel detail
                    if (namaObatField != null) namaObatField.setText(namaObat);
                    if (jenisObatField != null) jenisObatField.setText(jenisObat);
                    if (tanggalExpiredField != null) tanggalExpiredField.setText(tanggalExpired);
                    if (stokField != null) stokField.setText(stok);
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
        String namaObat = String.valueOf(table.getValueAt(row, 1));  // Column 1: NAMA OBAT
        String jenisObat = String.valueOf(table.getValueAt(row, 2));  // Column 2: JENIS OBAT
        Object tanggalExpiredObj = table.getValueAt(row, 3);          // Column 3: TANGGAL EXPIRED
        String tanggalExpired = "";
        if (tanggalExpiredObj instanceof java.sql.Date) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            tanggalExpired = dateFormat.format(tanggalExpiredObj);
        } else if (tanggalExpiredObj != null) {
            tanggalExpired = tanggalExpiredObj.toString();
        }
        String stock = String.valueOf(table.getValueAt(row, 4));      // Column 4: STOCK

        // Update the labels in the data panel
        namaObatField.setText(namaObat);
        jenisObatField.setText(jenisObat);
        tanggalExpiredField.setText(tanggalExpired);
        stokField.setText(stock);
    }

    private void setTableColumnWidths(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // NO column
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // NAMA OBAT column
        table.getColumnModel().getColumn(2).setPreferredWidth(75); // JENIS OBAT column
        table.getColumnModel().getColumn(3).setPreferredWidth(75); // HARGA column
        table.getColumnModel().getColumn(4).setPreferredWidth(75); // STOCK column
        table.getColumnModel().getColumn(5).setPreferredWidth(150); // AKSI column (Buttons)
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ObatExpierd().setVisible(true));
    }

    public void onObatAddedListener(String namaObat, String jenisObat, String harga, String stock) {
        tableModel.addRow(new Object[]{obatTable.getRowCount() + 1, namaObat, jenisObat, harga, stock});
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
        // Query the database again or get the updated data from elsewhere
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_obat()";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});

        // Clear the current data in the table
        data = new Object[0][];

        // Re-populate the table data with the updated results
        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                Object[] dataFromDatabase = new Object[]{
                    data.length + 1, result.get("nama_obat"), result.get("nama_jenis_obat"),
                    result.get("harga"), result.get("stock"), ""
                };

                // Create a new array with an additional row
                Object[][] newData = new Object[data.length + 1][];

                // Copy old data to the new array
                System.arraycopy(data, 0, newData, 0, data.length);

                // Add the new row to the new array
                newData[data.length] = dataFromDatabase;

                // Send back to original
                data = newData;
            }
        }

        // Update the table model with the refreshed data
        tableModel.setDataVector(data, new String[]{"NO", "NAMA OBAT", "JENIS OBAT", "HARGA", "STOCK", "AKSI"});

        // Reapply the button rendering and editing to the "AKSI" column
        obatTable.getColumn("AKSI").setCellRenderer(new ActionCellRenderer());
        obatTable.getColumn("AKSI").setCellEditor(new ActionCellEditor(tableModel));
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

    // Editor for "AKSI" column
    class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {

        JPanel panel;
        int row;
        DefaultTableModel model;

        public ActionCellEditor(DefaultTableModel model) {
            this.model = model;
            panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));

            // "RESTOCK" Button
            JButton restockButton = new RoundedButton("RESTOCK");
            restockButton.setBackground(new Color(255, 153, 51));
            restockButton.setForeground(Color.WHITE);
            restockButton.setFocusPainted(false);
            restockButton.addActionListener(e -> {
                if (row >= 0) {
                    // Ambil data dari tabel
                    String namaObat = (String) model.getValueAt(row, 1);  // NAMA OBAT
                    String jenisObat = (String) model.getValueAt(row, 2); // JENIS OBAT

                    // Ambil dan format tanggal expired
                    Object tanggalExpiredObj = model.getValueAt(row, 3); // TANGGAL EXPIRED
                    String tanggalExpired = "";
                    if (tanggalExpiredObj instanceof java.sql.Date) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                        tanggalExpired = dateFormat.format(tanggalExpiredObj);
                    } else if (tanggalExpiredObj != null) {
                        tanggalExpired = tanggalExpiredObj.toString();
                    }

                    String stokLama = String.valueOf(model.getValueAt(row, 4)); // STOCK LAMA

                    // Dialog untuk input stok baru
                    JPanel panel = new JPanel(new GridBagLayout());
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(5, 5, 5, 5);
                    gbc.fill = GridBagConstraints.HORIZONTAL;

                    // Detail Obat: Nama Obat
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    panel.add(new JLabel("Nama Obat:"), gbc);
                    gbc.gridx = 1;
                    JTextField namaObatField = new JTextField(namaObat, 20);
                    namaObatField.setEditable(false);
                    panel.add(namaObatField, gbc);

                    // Detail Obat: Jenis Obat
                    gbc.gridx = 0;
                    gbc.gridy = 1;
                    panel.add(new JLabel("Jenis Obat:"), gbc);
                    gbc.gridx = 1;
                    JTextField jenisObatField = new JTextField(jenisObat, 20);
                    jenisObatField.setEditable(false);
                    panel.add(jenisObatField, gbc);

                    // Detail Obat: Tanggal Expired
                    gbc.gridx = 0;
                    gbc.gridy = 2;
                    panel.add(new JLabel("Tanggal Expired:"), gbc);
                    gbc.gridx = 1;
                    JTextField tanggalExpiredField = new JTextField(tanggalExpired, 20);
                    tanggalExpiredField.setEditable(false);
                    panel.add(tanggalExpiredField, gbc);

                    // Detail Obat: Stok Lama
                    gbc.gridx = 0;
                    gbc.gridy = 3;
                    panel.add(new JLabel("Stok Lama:"), gbc);
                    gbc.gridx = 1;
                    JTextField stokLamaField = new JTextField(stokLama, 20);
                    stokLamaField.setEditable(false);
                    panel.add(stokLamaField, gbc);

                    // Input Stok Baru
                    gbc.gridx = 0;
                    gbc.gridy = 4;
                    panel.add(new JLabel("Stok Baru:"), gbc);
                    gbc.gridx = 1;
                    JTextField stokBaruField = new JTextField(10);
                    panel.add(stokBaruField, gbc);

                    // Input Tanggal Expired Baru
                    gbc.gridx = 0;
                    gbc.gridy = 5;
                    panel.add(new JLabel("Tanggal Expired Baru:"), gbc);
                    gbc.gridx = 1;
                    CustomTextField tanggalExpiredBaruField = new CustomTextField("Pilih tanggal expired", 20, 15, Optional.empty());
                    CustomDatePicker datePicker = new CustomDatePicker(tanggalExpiredBaruField.getTextField(), true);
                    tanggalExpiredBaruField.getTextField().addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            datePicker.showDatePicker(); // Tampilkan dialog pemilihan tanggal
                        }
                    });
                    panel.add(tanggalExpiredBaruField, gbc);

                    // Tampilkan dialog
                    int result = JOptionPane.showConfirmDialog(null, panel, "Restock Obat", JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        try {
                            int stokBaru = Integer.parseInt(stokBaruField.getText());
                            String tanggalExpiredBaru = tanggalExpiredBaruField.getText();

                            // Panggil logika restock
                            QueryExecutor executor = new QueryExecutor();
                            String idObat = ""; // Replace with actual idObat retrieval logic

                            // Tambahkan stok baru ke tabel `detail_obat` dan catat di `restock_log`
                            String insertDetailObatQuery = "INSERT INTO detail_obat (id_obat, tanggal_expired, stock) VALUES (?, ?, ?)";
                            String insertRestockLogQuery = "INSERT INTO restock_log (id_obat, tanggal_restock, jumlah_restock) VALUES (?, ?, ?)";

                            try {
                                // Insert ke tabel detail_obat
                                executor.executeInsertQuery(insertDetailObatQuery, new Object[]{idObat, tanggalExpiredBaru, stokBaru});

                                // Insert ke tabel restock_log
                                executor.executeInsertQuery(insertRestockLogQuery, new Object[]{idObat, new java.sql.Date(System.currentTimeMillis()), stokBaru});

                                JOptionPane.showMessageDialog(null, "Restock berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);

                                // Refresh data tabel
                                refreshTableData();
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, "Masukkan stok yang valid.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "Masukkan stok yang valid.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            panel.add(restockButton);

            // "BUANG" Button
            JButton buangButton = new RoundedButton("BUANG");
            buangButton.setBackground(new Color(255, 51, 51));
            buangButton.setForeground(Color.WHITE);
            buangButton.setFocusPainted(false);
            buangButton.addActionListener(e -> {
                if (row >= 0) {
                    // Show confirmation dialog
                    int response = JOptionPane.showConfirmDialog(
                        null,
                        "Apakah Anda yakin ingin membuang obat ini?",
                        "Konfirmasi Pembuangan",
                        JOptionPane.YES_NO_OPTION
                    );

                    if (response == JOptionPane.YES_OPTION) {
                        // Update the database to set `deleted` = 1
                        String namaObat = (String) model.getValueAt(row, 1); // NAMA OBAT
                        QueryExecutor executor = new QueryExecutor();
                        String query = "UPDATE obat SET deleted = 1 WHERE nama_obat = ?";
                        Object[] params = {namaObat};
                        boolean success = executor.executeUpdateQuery(query, params);

                        if (success) {
                            // Remove the row from the table
                            model.removeRow(row);
                            JOptionPane.showMessageDialog(null, "Obat berhasil dibuang.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Gagal membuang obat.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    stopCellEditing();
                }
            });
            panel.add(buangButton);
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
