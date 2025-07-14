package Obat;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.SwingUtilities;

import Components.CustomDatePicker;
import Components.CustomTextField;
import Components.Dropdown;
import Components.RoundedButton;
import Components.ShowModalCenter;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import Helpers.OnObatAddedListener;
import Helpers.OnObatUpdateListener;
import User.RegisterUser;

public class RegisterObat extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(RegisterObat.class.getName());

    private OnObatAddedListener listener;
    private OnObatUpdateListener updateListener;
    private CustomTextField txtNamaObat, txtHarga, txtStock, txtExpired, txtHargaJual;
    private Dropdown txtJenisObat, txtBentukObat;
    private CustomDatePicker customDatePicker;
    private CustomTextField txtBarcode;

    public RegisterObat(OnObatAddedListener listener, OnObatUpdateListener updateListener) {
        this.listener = listener;
        this.updateListener = updateListener;
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(500, 500)); // Tambahkan ini

        QueryExecutor executor = new QueryExecutor();
        String query = "CALL all_obat()";
        java.util.List<Map<String, Object>> results = executor.executeSelectQuery(query, new Object[]{});
        Set<String> uniqueJenisObatSet = new HashSet<>();  // Use a Set to store unique 'nama_jenis_obat'

        if (!results.isEmpty()) {
            // Iterate through the results from the database
            for (Map<String, Object> result : results) {
                // Add 'nama_jenis_obat' to the Set (duplicates will be automatically removed)
                uniqueJenisObatSet.add((String) result.get("nama_jenis_obat"));
            }
        }

        // Convert the Set back to a List if needed (optional step)
        List<String> jenisObatList = new ArrayList<>(uniqueJenisObatSet);
        LOGGER.info("Unique jenisObatList: " + jenisObatList);

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
        txtNamaObat = new CustomTextField("Masukkan Nama Obat", 20, 15, Optional.empty());
        formPanel.add(txtNamaObat, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Jenis Obat:"), gbc);
        gbc.gridx = 1;
        txtJenisObat = new Dropdown(true, true, null);  // Create Dropdown
        txtJenisObat.setItems(jenisObatList, true, true, null); // Menambahkan jenis obat
        formPanel.add(txtJenisObat, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Bentuk Obat:"), gbc);
        gbc.gridx = 1;
        txtBentukObat = new Dropdown(true, true, null);  // Create Dropdown
        txtBentukObat.setItems(List.of("Tablet", "Kapsul", "Sirup", "Salep / Krim", "Inhaler", "Injeksi / Suntikan", "Tetes", "Patch (Patches)", "Item"), true, true, null); // Menambahkan jenis obat
        formPanel.add(txtBentukObat, gbc);

        // Harga Obat
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Harga Beli:"), gbc);
        gbc.gridx = 1;
        txtHarga = new CustomTextField("Masukkan Harga", 20, 15, Optional.empty());
        formPanel.add(txtHarga, gbc);

        // Harga Jual
        gbc.gridx = 0;
        gbc.gridy = 4; // Adjust the row index to fit the new field
        formPanel.add(new JLabel("Harga Jual:"), gbc);
        gbc.gridx = 1;
        txtHargaJual = new CustomTextField("Masukkan Harga Jual", 20, 15, Optional.empty());
        formPanel.add(txtHargaJual, gbc);

        // Stock Obat
        gbc.gridx = 0;
        gbc.gridy = 5; // Adjust the row indices of subsequent fields
        formPanel.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1;
        txtStock = new CustomTextField("Masukkan Stock", 20, 15, Optional.empty());
        formPanel.add(txtStock, gbc);

        // Expired Date
        gbc.gridx = 0;
        gbc.gridy = 6; // Adjust the row indices of subsequent fields
        formPanel.add(new JLabel("Expired Date:"), gbc);
        gbc.gridx = 1;
        txtExpired = new CustomTextField("Expired Date", 20, 15, Optional.empty());
        customDatePicker = new CustomDatePicker(txtExpired.getTextField(), true);
        txtExpired.getTextField().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                customDatePicker.showDatePicker(); // Show the date picker dialog
            }
        });
        formPanel.add(txtExpired, gbc);

        // Barcode Obat
        gbc.gridx = 0;
        gbc.gridy = 7; // Adjust the row indices of subsequent fields
        formPanel.add(new JLabel("Barcode:"), gbc);
        gbc.gridx = 1;
        txtBarcode = new CustomTextField("Masukkan Barcode", 20, 15, Optional.empty());
        formPanel.add(txtBarcode, gbc);

        // Submit button
        gbc.gridx = 0;
        gbc.gridy = 8; // Adjust the row index for the submit button
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        RoundedButton submitButton = new RoundedButton("Tambahkan");
        submitButton.setBackground(new Color(0, 150, 136));
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserSessionCache cache = new UserSessionCache();
                String uuid = (String) cache.getUUID();
                if (uuid != null) {
                    // Get input data directly from form fields
                    String namaObat = txtNamaObat.getText();
                    String jenisObat = (String) txtJenisObat.getSelectedItem();
                    String selectedBentukObat = (String) txtBentukObat.getSelectedItem();
                    LOGGER.info("Selected Bentuk Obat: " + selectedBentukObat);
                    String harga = txtHarga.getText();
                    String hargaJual = txtHargaJual.getText(); // Get the Harga Jual value
                    String stock = txtStock.getText();
                    String tanggalExpired = txtExpired.getText();
                    String barcode = txtBarcode.getText();

                    QueryExecutor executor = new QueryExecutor();
                    String Query = "SELECT id_obat, nama_obat, id_jenis_obat, bentuk_obat, harga_jual FROM obat WHERE nama_obat = ? ";
                    Object[] parameter = new Object[]{namaObat};
                    java.util.List<Map<String, Object>> results = executor.executeSelectQuery(Query, parameter);

                    boolean isUpdateObat = false, isInsertDetailObat = false;
                    int idObat = 0;
                    if (!results.isEmpty() && results.size() == 1) {
                        Integer getId = (Integer) results.get(0).get("id_obat");
                        Integer hargaInput = Integer.valueOf(harga);
                        Integer hargaJualInput = Integer.valueOf(hargaJual); // Parse Harga Jual
                        String QueryUpdate = "UPDATE obat SET harga_jual = ?, barcode = ? WHERE id_obat = ?";
                        Object[] parameterUpdate = new Object[]{hargaJualInput, barcode, getId};
                        isUpdateObat = QueryExecutor.executeUpdateQuery(QueryUpdate, parameterUpdate);
                        if (isUpdateObat) {
                            idObat = getId;
                        }
                        String queryDetail = "INSERT INTO detail_obat (id_obat, tanggal_expired, stock , harga_beli, harga_jual) VALUES (?, ?, ?, ?, ?)";
                        Object[] parameterDetail = new Object[]{getId, tanggalExpired, Integer.valueOf(stock), hargaJual, harga};
                        isInsertDetailObat = QueryExecutor.executeInsertQuery(queryDetail, parameterDetail);
                    } else {
                        Integer idJenisObat;
                        String getIdJenisObat = "SELECT * FROM jenis_obat WHERE nama_jenis_obat LIKE ? LIMIT 1";
                        Object[] paramJenis = new Object[]{namaObat.split(" ")[0]};
                        java.util.List<Map<String, Object>> resultJenisObat = executor.executeSelectQuery(getIdJenisObat, paramJenis);
                        if (!resultJenisObat.isEmpty()) {
                            idJenisObat = (Integer) resultJenisObat.get(0).get("id_jenis_obat");
                        } else {
                            String insertJenisObat = "INSERT INTO jenis_obat (nama_jenis_obat) VALUES (?)";
                            Object[] paramJenisObat = new Object[]{namaObat.split(" ")[0]};
                            int getIdJenis = (int) QueryExecutor.executeInsertQueryWithReturnID(insertJenisObat, paramJenisObat);
                            idJenisObat = getIdJenis == 404 ? 0 : getIdJenis;
                        }
                        Integer hargaInput = Integer.valueOf(harga);
                        Integer hargaJualInput = Integer.valueOf(hargaJual); // Parse Harga Jual
                        String QueryUpdate = "INSERT INTO obat (nama_obat, id_jenis_obat, bentuk_obat, harga_jual, barcode) VALUES (?, ?, ?, ?, ?)";
                        Object[] parameterUpdate = new Object[]{namaObat, idJenisObat, selectedBentukObat, hargaJualInput, barcode};
                        idObat = (int) QueryExecutor.executeInsertQueryWithReturnID(QueryUpdate, parameterUpdate);
                        isUpdateObat = idObat != 404;
                        if (isUpdateObat) {
                            String getObat = "SELECT id_obat FROM obat WHERE nama_obat = ?";
                            Object[] paramGetObat = new Object[]{namaObat};
                            java.util.List<Map<String, Object>> resultsGet = executor.executeSelectQuery(getObat, paramGetObat);
                            Integer getNewId = (Integer) resultsGet.get(0).get("id_obat");
                            String queryDetail = "INSERT INTO detail_obat (id_obat, tanggal_expired, stock , harga_jual, harga_beli) VALUES (?, ?, ?, ?, ?)";
                            Object[] parameterDetail = new Object[]{getNewId, tanggalExpired, Integer.valueOf(stock), hargaJual, harga};
                            isInsertDetailObat = QueryExecutor.executeInsertQuery(queryDetail, parameterDetail);
                        }
                    }

                    if (isInsertDetailObat && isUpdateObat) {
                        Integer getIDOut;
                        String searchForOutDrug = "SELECT id_pengeluaran FROM pengeluaran WHERE tanggal = CURDATE() AND keterangan LIKE ?";
                        java.util.List<Map<String, Object>> resultsOut = executor.executeSelectQuery(searchForOutDrug, new Object[]{"%Obat%"});
                        if (!resultsOut.isEmpty()) {
                            getIDOut = (Integer) resultsOut.get(0).get("id_pengeluaran");
                        } else {
                            String insertIntoOut = "INSERT INTO pengeluaran (tanggal, keterangan, id_user) VALUES (CURDATE(), ?, ?)";
                            Object[] parameterInsertOut = new Object[]{"Beli Obat", uuid};
                            getIDOut = (int) QueryExecutor.executeInsertQueryWithReturnID(insertIntoOut, parameterInsertOut);
                        }
                        String insertIntoDetailOut = "INSERT INTO pengeluaran_detail (id_pengeluaran, id_obat, id_jenis_pengeluaran, keterangan, total) VALUES (?, ?, ?, ?, ?)";
                        Object[] insertIntoDetailParameter = new Object[]{getIDOut, idObat, 1, "Restock Obat", Integer.valueOf(stock) * Integer.valueOf(harga)};
                        boolean isInsertFinal = QueryExecutor.executeInsertQuery(insertIntoDetailOut, insertIntoDetailParameter);
                        if (isInsertFinal) {
                            JOptionPane.showMessageDialog(null, "Data Obat dengan Nama: " + namaObat, "Success", JOptionPane.INFORMATION_MESSAGE);

                            // Close the form after submission
                            Components.ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(RegisterObat.this));
                        } else {
                            JOptionPane.showMessageDialog(null, "Data Obat Gagal.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else if (isInsertDetailObat || isUpdateObat) {
                        String message;
                        if (isInsertDetailObat) {
                            message = "Data Obat dengan Nama: " + namaObat + " Berhasil Update Stok";
                        } else {
                            message = "Data Obat dengan Nama: " + namaObat + " Berhasil Update Harga";
                        }
                        JOptionPane.showMessageDialog(null, message, "Sukses", JOptionPane.INFORMATION_MESSAGE);

                        // Close the form after submission
                        
                    Components.ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(RegisterObat.this));
                    } else {
                        JOptionPane.showMessageDialog(null, "Data Obat Gagal.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    if (isInsertDetailObat) {
                        if (isUpdateObat) {
                            updateListener.onObatUpdate(harga, stock);
                        } else {
                            listener.onObatAddedListener(namaObat, jenisObat, harga, stock);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Data Obat Gagal Simpan, User Belum Login", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        formPanel.add(submitButton, gbc);

        // Add form panel to main frame
        add(formPanel);
    }

    // Tambahkan static method untuk showModalCenter
    public static void showModalCenter(JFrame parent, OnObatAddedListener listener, OnObatUpdateListener updateListener) {
        RegisterObat panel = new RegisterObat(listener, updateListener);
        Components.ShowModalCenter.showCenterModal(parent, panel);
    }
}
