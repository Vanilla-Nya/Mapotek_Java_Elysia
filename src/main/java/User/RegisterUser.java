package User;

import Components.CustomDatePicker;
import Components.CustomTextField;
import Components.Dropdown;
import Components.RoundedButton;
import DataBase.QueryExecutor;
import Global.UserSessionCache;
import Helpers.OnUserAddedListener;
import Helpers.TypeNumberHelper;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.JTabbedPane;

public class RegisterUser extends JPanel {
    // Field untuk hasil API Practitioner (tab Dokter)
    private String hasilNIK, hasilNama, hasilTanggalLahir, hasilGender, hasilAlamat, hasilIdSatuSehat;

    private CustomTextField txtName, txtAddress, txtPhone, txtPassword, txtRFID;
    private Dropdown txtRole, txtGender;
    private OnUserAddedListener listener;
    private CustomDatePicker customDatePicker;

    public RegisterUser(OnUserAddedListener listener, DefaultTableModel model) {
        this.listener = listener;   
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));

        // === Panel Admin ===
        JPanel adminPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcAdmin = new GridBagConstraints();
        gbcAdmin.insets = new Insets(5, 5, 5, 5);
        gbcAdmin.gridx = 0; gbcAdmin.gridy = 0;
        adminPanel.add(new JLabel("Nama Admin:"), gbcAdmin);
        gbcAdmin.gridx = 1;
        CustomTextField txtAdminName = new CustomTextField("Masukan Nama", 20, 15, Optional.empty());
        adminPanel.add(txtAdminName, gbcAdmin);
        gbcAdmin.gridx = 0; gbcAdmin.gridy++;
        adminPanel.add(new JLabel("Jenis Kelamin:"), gbcAdmin);
        gbcAdmin.gridx = 1;
        Dropdown txtAdminGender = new Dropdown(false, true, null);
        txtAdminGender.setItems(List.of("Laki-Laki", "Perempuan", "Tidak Bisa Dijelaskan"), false, true, null);
        adminPanel.add(txtAdminGender, gbcAdmin);

        gbcAdmin.gridx = 0; gbcAdmin.gridy++;
        adminPanel.add(new JLabel("Alamat:"), gbcAdmin);
        gbcAdmin.gridx = 1;
        CustomTextField txtAdminAddress = new CustomTextField("Masukan Alamat", 20, 15, Optional.empty());
        adminPanel.add(txtAdminAddress, gbcAdmin);

        gbcAdmin.gridx = 0; gbcAdmin.gridy++;
        adminPanel.add(new JLabel("No. Telp:"), gbcAdmin);
        gbcAdmin.gridx = 1;
        CustomTextField txtAdminPhone = new CustomTextField("Masukan No. Telp", 20, 15, Optional.empty());
        adminPanel.add(txtAdminPhone, gbcAdmin);

        gbcAdmin.gridx = 0; gbcAdmin.gridy++;
        adminPanel.add(new JLabel("Password:"), gbcAdmin);
        gbcAdmin.gridx = 1;
        CustomTextField txtAdminPassword = new CustomTextField("Masukan Password", 20, 15, Optional.of(true));
        adminPanel.add(txtAdminPassword, gbcAdmin);

        gbcAdmin.gridx = 0; gbcAdmin.gridy++;
        adminPanel.add(new JLabel("RFID:"), gbcAdmin);
        gbcAdmin.gridx = 1;
        CustomTextField txtAdminRFID = new CustomTextField("Masukan RFID", 20, 15, Optional.empty());
        adminPanel.add(txtAdminRFID, gbcAdmin);

        // Tombol simpan
        gbcAdmin.gridx = 0; gbcAdmin.gridy++; gbcAdmin.gridwidth = 2;
        RoundedButton btnSimpanAdmin = new RoundedButton("Simpan Admin");
        adminPanel.add(btnSimpanAdmin, gbcAdmin);

        btnSimpanAdmin.addActionListener(e -> {
            String nama = txtAdminName.getText().trim();
            String gender = (String) txtAdminGender.getSelectedItem();
            String alamat = txtAdminAddress.getText().trim();
            String phone = txtAdminPhone.getText().trim();
            String password = txtAdminPassword.getText().trim();
            String rfid = txtAdminRFID.getText().trim();

            if (nama.isEmpty() || gender == null || gender.isEmpty() || alamat.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            QueryExecutor queryExecutor = new QueryExecutor();
            String insertUserQuery = "INSERT INTO user (nama_lengkap, username, jenis_kelamin, alamat, no_telp, password, rfid) VALUES (?, ?, ?, ?, ?, ?, ?)";
            boolean userInserted = queryExecutor.executeInsertQuery(insertUserQuery, new Object[]{
                nama, nama, gender, alamat, phone, password, rfid.isEmpty() ? null : rfid
            });
            if (!userInserted) {
                JOptionPane.showMessageDialog(this, "Gagal menambah admin.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String getLastInsertIdQuery = "SELECT id_user as userId from user where username = ?";
            List<Map<String, Object>> result = queryExecutor.executeSelectQuery(getLastInsertIdQuery, new Object[]{nama});
            String userId = (String) result.get(0).get("userId");
            String insertUserRoleQuery = "INSERT INTO user_role (id_user, id_role) SELECT ?, id_role FROM role WHERE nama_role = ?";
            boolean userRoleInserted = queryExecutor.executeInsertQuery(insertUserRoleQuery, new Object[]{userId, "Admin"});
            if (userRoleInserted) {
                JOptionPane.showMessageDialog(this, "User Admin berhasil disimpan!");
                if (listener != null) {
                    listener.onUserAdded(userId, "Admin", nama, gender, alamat, phone);
                }
                Components.ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(this));
            } else {
                JOptionPane.showMessageDialog(this, "Gagal assign role ke admin.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // === Panel Dokter ===
        JPanel dokterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Dropdown metode pencarian
        gbc.gridx = 0; gbc.gridy = 0;
        dokterPanel.add(new JLabel("Metode Pencarian:"), gbc);
        gbc.gridx = 1;
        Dropdown searchMethodDropdown = new Dropdown(false, true, null);
        searchMethodDropdown.setItems(List.of("", "NIK", "Nama, Tanggal Lahir, Gender"), false, true, null);
        dokterPanel.add(searchMethodDropdown, gbc);

        // Panel input dinamis
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JPanel searchInputPanel = new JPanel(new GridBagLayout());
        dokterPanel.add(searchInputPanel, gbc);

        // Field untuk masing-masing metode
        CustomTextField txtNIK = new CustomTextField("Masukan NIK", 20, 15, Optional.empty());
        CustomTextField txtNama = new CustomTextField("Masukan Nama", 20, 15, Optional.empty());
        CustomTextField txtTanggalLahir = new CustomTextField("Tanggal Lahir", 20, 15, Optional.empty());
        Dropdown txtGenderSearch = new Dropdown(false, true, null);
        txtGenderSearch.setItems(List.of("Laki-Laki", "Perempuan"), false, true, null);

        // Area hasil
        gbc.gridy = 2;
        JTextArea hasilArea = new JTextArea(6, 30);
        hasilArea.setEditable(false);
        hasilArea.setBackground(new Color(245, 245, 245));
        hasilArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        hasilArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        hasilArea.setLineWrap(true);
        hasilArea.setWrapStyleWord(true);
        dokterPanel.add(hasilArea, gbc);

        // Listener dropdown untuk ganti input
        searchMethodDropdown.addActionListener(e -> {
            String selectedMethod = (String) searchMethodDropdown.getSelectedItem();
            searchInputPanel.removeAll();
            GridBagConstraints searchGbc = new GridBagConstraints();
            searchGbc.insets = new Insets(2, 2, 2, 2);
            searchGbc.gridx = 0; searchGbc.gridy = 0;
            if ("NIK".equals(selectedMethod)) {
                searchInputPanel.add(new JLabel("NIK:"), searchGbc);
                searchGbc.gridx = 1;
                searchInputPanel.add(txtNIK, searchGbc);
            } else if ("Nama, Tanggal Lahir, Gender".equals(selectedMethod)) {
                searchInputPanel.add(new JLabel("Nama:"), searchGbc);
                searchGbc.gridx = 1;
                searchInputPanel.add(txtNama, searchGbc);
                searchGbc.gridx = 0; searchGbc.gridy = 1;
                searchInputPanel.add(new JLabel("Tanggal Lahir:"), searchGbc);
                searchGbc.gridx = 1;
                searchInputPanel.add(txtTanggalLahir, searchGbc);
                searchGbc.gridx = 0; searchGbc.gridy = 2;
                searchInputPanel.add(new JLabel("Gender:"), searchGbc);
                searchGbc.gridx = 1;
                searchInputPanel.add(txtGenderSearch, searchGbc);
            }
            hasilArea.setText("");
            searchInputPanel.revalidate();
            searchInputPanel.repaint();
        });

        // Tombol cari ke API Practitioner
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        RoundedButton btnCariPractitioner = new RoundedButton("Cari Data Practitioner");
        dokterPanel.add(btnCariPractitioner, gbc);

        btnCariPractitioner.addActionListener(e -> {
            String method = (String) searchMethodDropdown.getSelectedItem();
            if ("NIK".equals(method)) {
                String nik = txtNIK.getText().trim();
                if (nik.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Masukkan NIK terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                new Thread(() -> {
                    API.ApiClient api = new API.ApiClient();
                    try {
                        String identifier = "https://fhir.kemkes.go.id/id/nik|" + nik;
                        String response = api.get(api.encodeUrl("/Practitioner?identifier=", identifier));
                        tampilkanDataPractitioner(response, hasilArea);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this, "Gagal mengambil data dari API SATUSEHAT", "Error", JOptionPane.ERROR_MESSAGE)
                        );
                    } finally {
                        try { api.close(); } catch (Exception ignore) {}
                    }
                }).start();
            } else if ("Nama, Tanggal Lahir, Gender".equals(method)) {
                String nama = txtNama.getText().trim();
                String tglLahir = txtTanggalLahir.getText().trim();
                String gender = (String) txtGenderSearch.getSelectedItem();
                if (nama.isEmpty() || tglLahir.isEmpty() || gender == null || gender.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                new Thread(() -> {
                    API.ApiClient api = new API.ApiClient();
                    try {
                        String url = "/Practitioner?name=" +
                                java.net.URLEncoder.encode(nama, "UTF-8") +
                                "&birthdate=" + java.net.URLEncoder.encode(tglLahir, "UTF-8") +
                                "&gender=" + (gender.equalsIgnoreCase("Laki-Laki") ? "male" : "female");
                        String response = api.get(url);
                        tampilkanDataPractitioner(response, hasilArea);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this, "Gagal mengambil data dari API SATUSEHAT", "Error", JOptionPane.ERROR_MESSAGE)
                        );
                    } finally {
                        try { api.close(); } catch (Exception ignore) {}
                    }
                }).start();
            }
        });

        // Tombol simpan ke database
        gbc.gridy = 4;
        RoundedButton btnSimpanDB = new RoundedButton("Simpan ke Database");
        dokterPanel.add(btnSimpanDB, gbc);

        btnSimpanDB.addActionListener(e -> {
            // Validasi data hasil pencarian
            if (hasilNIK == null || hasilNama == null || hasilGender == null || hasilAlamat == null) {
                JOptionPane.showMessageDialog(this, "Tidak ada data practitioner yang bisa disimpan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Insert ke tabel user, tambahkan id_satusehat
            QueryExecutor queryExecutor = new QueryExecutor();
            String insertUserQuery = "INSERT INTO user (id_satusehat, nama_lengkap, username, jenis_kelamin, alamat, no_telp, password, rfid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            boolean userInserted = queryExecutor.executeInsertQuery(insertUserQuery, new Object[]{
                hasilIdSatuSehat, hasilNama, hasilNama, hasilGender, hasilAlamat, "0", "default", null
            });
            if (!userInserted) {
                JOptionPane.showMessageDialog(this, "Gagal menambah user.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String getLastInsertIdQuery = "SELECT id_user as userId from user where username = ?";
            List<Map<String, Object>> result = queryExecutor.executeSelectQuery(getLastInsertIdQuery, new Object[]{hasilNama});
            String userId = (String) result.get(0).get("userId");
            String insertUserRoleQuery = "INSERT INTO user_role (id_user, id_role) SELECT ?, id_role FROM role WHERE nama_role = ?";
            boolean userRoleInserted = queryExecutor.executeInsertQuery(insertUserRoleQuery, new Object[]{userId, "Dokter"});
            if (userRoleInserted) {
                JOptionPane.showMessageDialog(this, "User Dokter berhasil disimpan!");
                if (listener != null) {
                    listener.onUserAdded(userId, "Dokter", hasilNama, hasilGender, hasilAlamat, "0");
                }
                Components.ShowModalCenter.closeCenterModal((JFrame) SwingUtilities.getWindowAncestor(this));
            } else {
                JOptionPane.showMessageDialog(this, "Gagal assign role ke user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Method tampilkanDataPractitioner mirip tampilkanDataPasien, parsing JSON Practitioner
        // Simpan hasil ke field: hasilNIK, hasilNama, hasilTanggalLahir, hasilGender, hasilAlamat, hasilIdSatuSehat

        // === CustomTabbedPane ===
        String[] tabTitles = {"Admin", "Dokter"};
        JComponent[] tabContents = {adminPanel, dokterPanel};
        Components.CustomTabbedPane customTabbedPane = new Components.CustomTabbedPane(tabTitles, tabContents);

        add(customTabbedPane, BorderLayout.CENTER);
    }

    // Contoh method tampilkanDataPractitioner
    private void tampilkanDataPractitioner(String responseString, JTextArea hasilArea) {
        org.json.JSONObject response = new org.json.JSONObject(responseString);
        org.json.JSONArray entries = response.optJSONArray("entry");
        if (entries != null && entries.length() > 0) {
            org.json.JSONObject resource = entries.getJSONObject(0).getJSONObject("resource");
            hasilIdSatuSehat = resource.optString("id", null);
            hasilNama = resource.getJSONArray("name").getJSONObject(0).optString("text");
            hasilTanggalLahir = resource.optString("birthDate");
            String genderApi = resource.optString("gender");
            if ("male".equalsIgnoreCase(genderApi)) {
                hasilGender = "Laki-Laki";
            } else if ("female".equalsIgnoreCase(genderApi)) {
                hasilGender = "Perempuan";
            } else {
                hasilGender = "Tidak Bisa Dijelaskan";
            }
            hasilAlamat = "";
            if (resource.has("address")) {
                org.json.JSONObject address = resource.getJSONArray("address").getJSONObject(0);
                hasilAlamat = address.getJSONArray("line").getString(0);
            }
            hasilNIK = "";
            org.json.JSONArray identifiers = resource.getJSONArray("identifier");
            for (int i = 0; i < identifiers.length(); i++) {
                org.json.JSONObject id = identifiers.getJSONObject(i);
                if (id.getString("system").equals("https://fhir.kemkes.go.id/id/nik")) {
                    hasilNIK = id.getString("value");
                    break;
                }
            }
            String hasil = "=== Data Practitioner ===\n"
                    + "Nama         : " + hasilNama + "\n"
                    + "Tanggal Lahir: " + hasilTanggalLahir + "\n"
                    + "Jenis Kelamin: " + hasilGender + "\n"
                    + "NIK          : " + hasilNIK + "\n"
                    + "Alamat       : " + hasilAlamat;
            SwingUtilities.invokeLater(() -> hasilArea.setText(hasil));
        } else {
            hasilIdSatuSehat = null;
            hasilNIK = hasilNama = hasilTanggalLahir = hasilGender = hasilAlamat = null;
            String hasil = "⚠️ Tidak ada data practitioner ditemukan.";
            SwingUtilities.invokeLater(() -> hasilArea.setText(hasil));
        }
    }

    public static void showModalCenter(JFrame parent, OnUserAddedListener listener, DefaultTableModel model) {
        RegisterUser panel = new RegisterUser(listener, model);
        Components.ShowModalCenter.showCenterModal(parent, panel);
    }
}
