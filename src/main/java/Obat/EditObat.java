package Obat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import Components.CustomTextField;
import Components.Dropdown;
import Components.RoundedButton;
import DataBase.QueryExecutor;

public class EditObat extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(EditObat.class.getName());

    private CustomTextField txtNamaObat, txtHarga, txtStock, txtBarcode;
    private Dropdown txtJenisObat;
    private JTable table;
    private int row; // The row index of the selected obat

    public EditObat(String namaObat, String jenisObat, String harga, String stock, String barcode, JTable table, int row) {
        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_obat()";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});
        Set<String> uniqueJenisObatSet = new HashSet<>();  // Use a Set to store unique 'nama_jenis_obat'

        if (!results.isEmpty()) {
            // Iterate through the results from the database
            for (Map<String, Object> result : results) {
                // Add 'nama_jenis_obat' to the Set (duplicates will be automatically removed)
                String jenisObatValue = (String) result.get("nama_jenis_obat");
                LOGGER.info("Retrieved jenisObat: " + jenisObatValue);
                uniqueJenisObatSet.add(jenisObatValue);
            }
        }
        List<String> jenisObatList = new ArrayList<>(uniqueJenisObatSet);
        LOGGER.info("Unique jenisObatList: " + jenisObatList);

        setTitle("Edit Obat");
        setSize(400, 300);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        this.table = table;
        this.row = row;

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Nama Obat
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Nama Obat:"), gbc);
        gbc.gridx = 1;
        txtNamaObat = new CustomTextField("Nama Obat", 20, 15, Optional.empty());
        txtNamaObat.setText(namaObat);
        formPanel.add(txtNamaObat, gbc);

        // Jenis Obat
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Jenis Obat:"), gbc);
        gbc.gridx = 1;
        txtJenisObat = new Dropdown(true, true, null);  // Create Dropdown
        txtJenisObat.setItems(jenisObatList, true, true, null); // Menambahkan jenis obat
        txtJenisObat.setSelectedItem(jenisObat); // Set the selected item
        formPanel.add(txtJenisObat, gbc);

        // Harga Obat
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Harga:"), gbc);
        gbc.gridx = 1;
        txtHarga = new CustomTextField("Harga", 20, 15, Optional.empty());
        txtHarga.setText(harga);
        formPanel.add(txtHarga, gbc);

        // Stock Obat
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1;
        txtStock = new CustomTextField("Stok", 20, 15, Optional.empty());
        txtStock.setText(stock);
        formPanel.add(txtStock, gbc);

        // Barcode Obat
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Barcode:"), gbc);
        gbc.gridx = 1;
        txtBarcode = new CustomTextField("Barcode", 20, 15, Optional.empty());
        txtBarcode.setText(barcode);
        formPanel.add(txtBarcode, gbc);

        // Submit button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        RoundedButton submitButton = new RoundedButton("Simpan");
        submitButton.setBackground(new Color(0, 150, 136));
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get updated data from form fields
                String namaObat = txtNamaObat.getText();
                String jenisObat = (String) txtJenisObat.getSelectedItem();
                String harga = txtHarga.getText();
                String stock = txtStock.getText();
                String barcode = txtBarcode.getText();

                // Ensure table data is updated
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                model.setValueAt(namaObat, row, 1);  // Update NAMA OBAT
                model.setValueAt(jenisObat, row, 2);  // Update JENIS OBAT
                model.setValueAt(harga, row, 3);      // Update HARGA
                model.setValueAt(stock, row, 4);      // Update STOCK
                model.setValueAt(barcode, row, 5);    // Update BARCODE

                // Confirmation message
                JOptionPane.showMessageDialog(null, "Obat berhasil diperbarui!");

                // Close the form after saving
                dispose();
            }
        });
        formPanel.add(submitButton, gbc);

        add(formPanel);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EditObat("Paracetamol", "Tablet", "5000", "100", "1234567890123", null, 0));
    }
}
