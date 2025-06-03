package TransaksiDiagnosa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
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

import Components.CustomDialog;
import Components.CustomTable.CustomTable;
import Components.CustomTextField;
import Components.RoundedButton;
import Components.ShowModalCenter;
import DataBase.QueryExecutor;

public class AddDrugs extends JDialog {

    private CustomTextField namaObatField, jenisObatField, hargaField, stockField, usageField;
    private String namaObat, jenisObat, usageInstructions;
    private Integer stock, idObat;
    private TransaksiDiagnosa parentForm;  // Reference to the parent form
    private JLabel idLabel, hargaLabel, namaObatLabel, stockLabel, jenisObatLabel;
    private DefaultTableModel tableModel;
    private JTable obatTable;
    private JScrollPane tableScrollPane;
    private double harga;
    Object[][] data = {};
    java.util.List id = new ArrayList<>();

    public AddDrugs(JFrame parent, TransaksiDiagnosa parentForm) {
        super(parent, "Tambah Obat", true); // Modal dialog
        this.parentForm = parentForm;

        // Setup dialog
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Tambahkan komponen
        JPanel headerPanel = createHeaderPanel();
        JPanel topPanel = createTopPanel();
        JPanel dataPanel = createDataPanel();
        JScrollPane tableScrollPane = createTablePanel();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(dataPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // Atur ukuran tabel
        obatTable.setPreferredScrollableViewportSize(new Dimension(600, 200)); // Lebar 600px, tinggi 200px

        // Panggil pack() untuk menyesuaikan ukuran
        pack();
        setLocationRelativeTo(parent); // Posisikan di tengah parent

        // Atur ukuran maksimum dialog jika diperlukan
        setMaximumSize(new Dimension(800, 600)); // Maksimum lebar 800px, tinggi 600px

        // Posisikan di tengah parent frame
        setLocationRelativeTo(parent);

        // Fetch data from the database
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_obat()"; // Use the new procedure
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});
        if (!results.isEmpty()) {
            for (Map<String, Object> result : results) {
                id.add(result.get("id_obat"));
                Object[] dataFromDatabase = new Object[]{
                    data.length + 1,
                    result.get("barcode") != null ? result.get("barcode") : "",
                    result.get("nama_obat") != null ? result.get("nama_obat") : "",
                    result.get("nama_jenis_obat") != null ? result.get("nama_jenis_obat") : "",
                    result.get("harga_jual") != null ? result.get("harga_jual") : 0,
                    result.get("stock") != null ? result.get("stock") : 0,
                    ""
                };

                // Tambahkan data ke array
                Object[][] newData = new Object[data.length + 1][];
                System.arraycopy(data, 0, newData, 0, data.length);
                newData[data.length] = dataFromDatabase;
                data = newData;
            }
        }

        // Perbarui tableModel dengan data yang diambil
        tableModel.setDataVector(data, new String[]{"NO", "BARCODE", "NAMA OBAT", "JENIS OBAT", "HARGA JUAL", "STOCK"});
    }

    private JPanel createHeaderPanel() {
        JPanel dataPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        dataPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create Labels for data
        hargaLabel = createDataLabel("HARGA : ");
        namaObatLabel = createDataLabel("NAMA OBAT : ");
        stockLabel = createDataLabel("STOCK : ");
        jenisObatLabel = createDataLabel("JENIS OBAT : ");

        return dataPanel;
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
        // Adding action listener for the button
        addButton.addActionListener(e -> {
            System.out.println("Add button clicked");  // Debugging line

            // Check if a row is selected in the table
            int selectedRow = obatTable.getSelectedRow();
            if (selectedRow == -1) {
                // No row selected, show a warning message
                JOptionPane.showMessageDialog(this, "Anda harus memilih obat terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;  // Exit without doing anything
            }

            // Check if stock field is empty
            if (stockField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Jumlah harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;  // Exit if stock is empty
            }

            // Check if usage field is empty
            if (usageField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cara penggunaan harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;  // Exit if usage instructions are empty
            }

            // Get stock input
            String stockText = stockField.getText();

            // Parse stock as an integer
            int enteredStock;
            try {
                enteredStock = Integer.parseInt(stockText);  // Parse entered stock quantity
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Jumlah yang dimasukkan tidak valid!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;  // Exit if stock input is invalid
            }

            // Get available stock from the table (make sure to parse it as integer)
            int availableStock = Integer.parseInt(String.valueOf(obatTable.getValueAt(selectedRow, 5))); // Assuming column 5 holds stock

            // Check if entered stock exceeds available stock
            if (enteredStock > availableStock) {
                System.out.println("Stock exceeds available stock"); // Debugging line

                // Ganti CustomDialog dengan JDialog
                JDialog dialog = new JDialog(this, "Peringatan", true); // Modal dialog
                dialog.setLayout(new BorderLayout(10, 10));

                // Tambahkan pesan
                JLabel messageLabel = new JLabel("Jumlah stok tidak cukup! Stok yang tersedia: " + availableStock);
                messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                dialog.add(messageLabel, BorderLayout.CENTER);

                // Tambahkan tombol
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
                JButton yesButton = new JButton("OK");

                yesButton.addActionListener(evt -> {
                    dialog.dispose(); // Tutup dialog
                    // Jangan tambahkan obat ke tabel jika stok tidak mencukupi
                });

                buttonPanel.add(yesButton);
                dialog.add(buttonPanel, BorderLayout.SOUTH);

                // Atur ukuran dan posisi dialog
                dialog.pack();
                dialog.setLocationRelativeTo(this); // Posisikan di tengah parent
                dialog.setVisible(true); // Tampilkan dialog
            } else {
                // Proceed with adding the drug if stock is sufficient
                addDrugToTableAndClose();
            }
        });

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
                    .filter(row -> ((String) row[2]).toLowerCase().contains(searchTerm) // Check if 'NAMA OBAT' contains search term
                            || ((String) row[1]).toLowerCase().contains(searchTerm)) // Check if 'BARCODE' contains search term
                    .toArray(Object[][]::new);

            // Update the table model with the filtered data
            tableModel.setDataVector(filteredData, new String[]{"NO", "BARCODE", "NAMA OBAT", "JENIS OBAT", "HARGA", "STOCK"});
        });

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
                    .filter(row -> {
                        String namaObat = row[2] != null ? ((String) row[2]).toLowerCase() : ""; // Default ke string kosong jika null
                        String barcode = row[1] != null ? ((String) row[1]).toLowerCase() : ""; // Default ke string kosong jika null
                        return namaObat.contains(searchTerm) || barcode.contains(searchTerm);
                    })
                    .toArray(Object[][]::new);

                // Update the table model with the filtered data    
                tableModel.setDataVector(filteredData, new String[]{"NO", "BARCODE", "NAMA OBAT", "JENIS OBAT", "HARGA", "STOCK"});
            }
        });

        topPanel.add(searchPanel, BorderLayout.WEST);  // Search panel on left
        topPanel.add(addButton, BorderLayout.EAST);    // "Tambahkan Obat" button on right
        return topPanel;
    }

    private JPanel createDataPanel() {
        JPanel dataPanel = new JPanel(new GridLayout(6, 2, 10, 10)); // Adjusted to 6 rows
        dataPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create Labels for data
        hargaLabel = createDataLabel("HARGA : ");
        namaObatLabel = createDataLabel("NAMA OBAT : ");
        stockLabel = createDataLabel("STOCK : ");
        jenisObatLabel = createDataLabel("JENIS OBAT : ");

        dataPanel.add(namaObatLabel);
        dataPanel.add(new JLabel(""));  // Placeholder for Nama Obat value
        dataPanel.add(jenisObatLabel);
        dataPanel.add(new JLabel(""));  // Placeholder for Jenis Obat value
        dataPanel.add(hargaLabel);
        dataPanel.add(new JLabel(""));  // Placeholder for Harga value
        stockField = new CustomTextField("Jumlah", 0, 0, Optional.empty());
        dataPanel.add(stockField);  // Placeholder for Stock value
        usageField = new CustomTextField("Cara Penggunaan", 0, 0, Optional.empty()); // New field for usage instructions
        dataPanel.add(usageField);

        return dataPanel;
    }

    private JScrollPane createTablePanel() {
        // Table data and columns setup
        String[] columns = {"NO", "BARCODE", "NAMA OBAT", "JENIS OBAT", "HARGA JUAL", "STOCK"};

        // Table model
        tableModel = new DefaultTableModel(data, columns); // Pastikan data langsung diatur di sini

        obatTable = new CustomTable(tableModel);
        setTableColumnWidths(obatTable);

        // Add Mouse Listener to update data panel when a row is selected
        obatTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = obatTable.getSelectedRow();
                idObat = Integer.valueOf(id.get(selectedRow).toString());
                namaObat = String.valueOf(obatTable.getValueAt(selectedRow, 2));
                jenisObat = String.valueOf(obatTable.getValueAt(selectedRow, 3));
                harga = Double.parseDouble(String.valueOf(obatTable.getValueAt(selectedRow, 4))); // Update harga
                stock = Integer.valueOf(String.valueOf(obatTable.getValueAt(selectedRow, 5)));   // Update stock

                // Update data labels with the selected row details
                namaObatLabel.setText("NAMA OBAT : " + namaObat);
                jenisObatLabel.setText("JENIS OBAT : " + jenisObat);
                hargaLabel.setText("HARGA : " + harga);
            }
        });

        return new JScrollPane(obatTable);
    }

    private void addDrugToTableAndClose() {
        System.out.println("Memulai proses penutupan frame AddDrugs...");

        // Tambahkan obat ke tabel di parent form
        usageInstructions = usageField.getText(); // Ambil instruksi penggunaan
        parentForm.addOrUpdateDrug(idObat, namaObat, jenisObat, Integer.parseInt(stockField.getText()), harga * Integer.parseInt(stockField.getText()), usageInstructions);

        System.out.println("Obat berhasil ditambahkan ke tabel."); // Debugging

        // Sembunyikan frame
        setVisible(false);

        // Tutup frame ini
        dispose();
        System.out.println("Frame AddDrugs berhasil ditutup.");
    }

    private JLabel createDataLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private void setTableColumnWidths(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(50); // NO
        table.getColumnModel().getColumn(1).setPreferredWidth(200); // BARCODE
        table.getColumnModel().getColumn(2).setPreferredWidth(200); // NAMA OBAT
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // JENIS OBAT
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // HARGA
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // STOCK
    }

    @Override
    public JComponent getContentPane() {
        return (JComponent) super.getContentPane(); // Mengembalikan konten utama JFrame
    }
}
